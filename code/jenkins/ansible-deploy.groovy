#!groovy

pipeline {
    agent { label 'esxi-ids-build' }

    parameters {
        string defaultValue: '10.25.10.126', description: '部署设备的IP地址', name: 'device_ip'
        choice choices: ['root'], description: '登录用户', name: 'user'
        password defaultValue: 'Netvine123', description: '密码: 默认N*****3', name: 'passwd'
        string description: '如果不想从头运行，可以选择从哪个阶段开始', name: 'start'
        choice choices: ['firewall_deploy', 'ids_deploy'], description: '待部署的项目:\nfirewall_deploy 防火墙\n ids_deploy 入侵项目', name: 'branch'
    }

    environment {
        CODE_DIR = '/data/jenkins-ansible/build-scripts'
        CODE_REPO = 'git@gitlab.inetvine.com:web/build-scripts.git'
        CODE_BRANCH = "${params.branch}"
    }

    stages {
        stage('准备') {
            steps {
                echo "ip:${params.device_ip}\nuser:${params.user}\nstart:${params.start}\n"
            }
        }

        stage('更新代码') {
            steps {
                sh "mkdir -p ${CODE_DIR}"
                dir("${CODE_DIR}") {
                    git branch: "${CODE_BRANCH}" ,url: "${CODE_REPO}"
                    /* groovylint-disable-next-line GStringExpressionWithinString */
                    sh '''
                        git checkout -f ${CODE_BRANCH}
                        git fetch
                        git add .
                        git reset --hard origin/${CODE_BRANCH}
                    '''
                }
            }
        }

        stage('Ansile host') {
            steps {
                echo '准备host..'
                dir("${CODE_DIR}") {
                    script {
                        def hostData = 's1 ansible_ssh_host=' + params.device_ip + ' ansible_ssh_port=22 ansible_ssh_user=' + params.user + " ansible_ssh_pass=\""+params.passwd +"\"\n[target]\ns1"
                        writeFile(file: 'host', text: hostData)
                    }
                }
            }
        }

        stage('Ansile Run') {
            steps {
                dir("${CODE_DIR}") {
                    script {
                        if (params.start == '') {
                            sh 'ansible-playbook main.yml -i host'
                        } else {
                            sh "ansible-playbook main.yml -i host --start=\"${params.start}\""
                        }
                    }
                }
            }
        }
    }
}
