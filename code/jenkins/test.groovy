pipeline {
    agent { label 'exsi-make-iso'}

    stages {
        stage('Build') {
            steps {
                input message: "test"
                echo 'Building..'
            }
        }
        stage('Test') {
            steps {
                echo 'Testing..'
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying....'
            }
        }
        
    }
}