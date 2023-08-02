pipeline {
    agent { label 'exsi-make-iso'}

    parameters {
        choice choices: ['1','2'], description: '硬盘数量', name: 'disk_num'
    }

    environment {
        USER_DATA = 'user-data.example'
    }

    stages {
        stage('Test') {
            steps {
                script{
                    if (params.disk_num == '1') {
                       sh 'echo $params.disk_num'
                    }
                }
            }
        }
    }
}