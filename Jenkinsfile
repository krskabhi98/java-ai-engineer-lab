//AWS deployemnt
pipeline {
    agent any

    environment {
        AILEAD_HOST = '172.31.44.91'
        AILEAD_USER = 'ec2-user'
        AILEAD_JAR = '/home/ec2-user/java-ai-engineer-lab/build/libs/ailead-1.0.0.jar'

        NEXUS_URL = 'http://172.31.43.26:8081'
        NEXUS_REPOSITORY = 'ailead-maven-releases'
    }

    stages {

        stage('Build') {
            steps {
                sh './gradlew clean build'
            }
        }

        stage('Publish to Nexus') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'nexus-jenkins-credentials',
                        usernameVariable: 'NEXUS_USERNAME',
                        passwordVariable: 'NEXUS_PASSWORD'
                    )
                ]) {
                    sh './gradlew publish'
                }
            }
        }

        stage('Download Artifact') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'nexus-jenkins-credentials',
                        usernameVariable: 'NEXUS_USERNAME',
                        passwordVariable: 'NEXUS_PASSWORD'
                    )
                ]) {
                    sh '''
                        curl -f -u "${NEXUS_USERNAME}:${NEXUS_PASSWORD}" \
                            -o ailead-1.0.0.jar \
                            "${NEXUS_URL}/repository/${NEXUS_REPOSITORY}/com/AI/ailead/1.0.0/ailead-1.0.0.jar"
                    '''
                }
            }
        }

        stage('Deploy') {
            steps {
                sshagent(['ailead-ec2-ssh']) {
                    sh '''
                        scp -o StrictHostKeyChecking=no \
                            ailead-1.0.0.jar \
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
