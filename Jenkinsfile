pipeline {
    agent any

    environment {
        AILEAD_HOST = '172.31.44.91'
        AILEAD_USER = 'ec2-user'
        AILEAD_JAR = '/home/ec2-user/java-ai-engineer-lab/build/libs/AILEAD-0.0.1-SNAPSHOT.jar'
    }

    stages {

        stage('Build') {
            steps {
                sh './gradlew clean build'
            }
        }

        stage('Deploy') {
            steps {
                sshagent(['ailead-ec2-ssh']) {
                    sh '''
                        scp -o StrictHostKeyChecking=no \
                            build/libs/AILEAD-0.0.1-SNAPSHOT.jar \
                            ${AILEAD_USER}@${AILEAD_HOST}:${AILEAD_JAR}

                        ssh -o StrictHostKeyChecking=no \
                            ${AILEAD_USER}@${AILEAD_HOST} \
                            'sudo systemctl restart ailead'

                        ssh -o StrictHostKeyChecking=no \
                            ${AILEAD_USER}@${AILEAD_HOST} \
                            'systemctl is-active --quiet ailead'
                    '''
                }
            }
        }
    }
}
