#!groovy
pipeline {
    agent { label 'esxi-ids-build' }

    parameters {
        choice choices: ['x86', 'phytium-d2000'], description: '版本\nPhytium-D2000 银河麒麟v10', name: 'version'
    }

    stages {
        stage('Input') {
            steps {
                script {
                    if (params.version == 'x86') {
                        node('smp') {
                            sh '''
                                cd /opt/netvine/smp/smp-watcher
                                /usr/local/go/bin/go build -ldflags="-s -w" -o smp-watcher
                            '''
                        }
                    }else if (params.version == 'phytium-d2000') {
                        node('KylinV10') {
                             sh '''
                                smp_watcher_dir=$(find /data -name smp-watcher)
                                cd ${smp_watcher_dir}
                                # go env -w GONOPROXY=https://git.netvine.com 
                                # go env -w GOPRIVATE=https://git.netvine.com
                                # go env -w GOSUMDB=off
                                # go env -w GONOSUMDB=https://git.netvine.com
                                # git config --global http.sslVerify false
                                # go mod vendor
                                go build -ldflags="-s -w" -o smp-watcher
                            '''
                        }
                    }
                }
            }
        }
    }
}
