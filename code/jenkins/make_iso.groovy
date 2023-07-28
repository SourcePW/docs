#!groovy
/* groovylint-disable NestedBlockDepth */
pipeline {
    agent { label 'exsi-make-iso' }

    parameters {
        string defaultValue: '10.25.10.126', description: '部署设备的IP地址', name: 'device_ip'
        choice choices: ['root'], description: '登录用户', name: 'user'
        password defaultValue: 'Netvine123', description: '密码: 默认N*****3', name: 'passwd'
        extendedChoice description: '产品类型', multiSelectDelimiter: ',', name: 'product', quoteValue: false, saveJSONParameterToFile: false, type: 'PT_RADIO', visibleItemCount: 5
    }

    environment {
        CODE_DIR = '/data/jenkins-iso/ubuntu-autoinstall-generator-main'
        JOB_DIR = '/root/jenkins/workspace/make_iso'
        ISO_FILE = 'ubuntu-custom-super.iso'
        ISO_TARGET_DIR = '/root/iso'
        VERSION = createVersion()
    }

    stages {
        stage('准备') {
            steps {
                echo "ip:${params.device_ip}\nuser:${params.user}\n"
            }
        }

        stage('backup') {
            steps {
                echo 'backup'

                script {
                    // 删除当前备份包
                    sh "mkdir -p ${ISO_TARGET_DIR}"
                    sh "rm -fr ${JOB_DIR}/backup.tar.gz"

                    remoteServer = GetRemoteServer(params.device_ip, params.user, params.passwd)
                    // 准备生产包环境
                    sshCommand remote: remoteServer, command: 'rm -fr backup.tar.gz; rm -fr /var/log/journal/*; '

                    if (product == 'firewall' || product == 'ids') {
                        // 删除license
                        sshCommand remote: remoteServer, command: "/bin/mysql -u root -p'Netvine123#@!' -e'truncate firewall.license_record'"
                    }else if (product == 'audit'){
                        // 删除license
                        sshCommand remote: remoteServer, command: "/bin/mysql -u root -p'Netvine123#@!' -e'truncate audit.license_record'"
                    }
                    // 打包
                    sshCommand remote: remoteServer, command: 'tar   --exclude=backup.tar.gz   --exclude=/lost+found   --exclude=/proc --exclude=/mnt --exclude=/etc/fstab --exclude=/sys --exclude=/dev --exclude=/boot --exclude=/tmp --exclude=/var/cache/apt/archives --exclude=/run --warning=no-file-changed --exclude=/home --exclude=/usr/lib/debug --exclude=/var/lib/libvirt --exclude=/root --exclude=/swap.img --exclude=/etc/netplan --exclude=/root/ --exclude=/home/ -cvpzf backup.tar.gz /'
                }
            }
        }

        stage('scp') {
            steps {
                echo 'scp'
                script {
                    sh "sshpass -p '${params.passwd}' scp -r ${params.user}@${params.device_ip}:/root/backup.tar.gz ."
                    // 监测文件
                    sh '''
                        filename=/root/backup.tar.gz
                        filesize=`ls -l $filename | awk '{ print $5 }'`
                        maxsize=$((1024*1024*1024*4))
                        if [ $filesize -gt $maxsize ]
                        then
                            echo "$filesize > $maxsize"
                            echo "备份包不能大于4G"
                            exit 1
                        fi
                    '''
                }
            }
        }

        stage('make_iso') {
            steps {
                echo 'make_iso'
                dir("${CODE_DIR}") {
                    script {
                        sh "rm -fr ${ISO_FILE}"
                        sh "bash -x ubuntu-autoinstall-generator.sh -a -u user-data.example -s ubuntu-20.04.5-live-server-amd64.iso -d ${ISO_FILE} -ed ${JOB_DIR}/backup.tar.gz"
                        sh "mv ${ISO_FILE} ${ISO_TARGET_DIR}/ubuntu_custom_${VERSION}.iso"
                        echo "拷贝iso: scp root@10.25.10.40:${ISO_TARGET_DIR}/ubuntu_custom_${VERSION}.iso ."
                    }
                }
            }
        }

        stage('upload') {
            steps {
                echo 'ftp upload'
            }
        }
    }
}

def GetRemoteServer(ip, user, pass) {
    def remote = [:]
    remote.name = ip
    remote.host = ip
    remote.port = 22
    remote.user = user
    remote.password = pass
    remote.allowAnyHosts = true

    return remote
}

def createVersion() {
    // 定义一个版本号作为当次构建的版本，输出结果 20191210175842_69
    return new Date().format('yyyyMMdd-HHmmss') + "_${env.BUILD_ID}"
}
