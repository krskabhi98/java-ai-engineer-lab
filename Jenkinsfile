pipeline {
    agent any

    environment {
        APP_VERSION = '1.0.1'

        AILEAD_HOST = '172.31.44.91'
        AILEAD_USER = 'ec2-user'
        AILEAD_JAR = "/home/ec2-user/java-ai-engineer-lab/build/libs/ailead-${APP_VERSION}.jar"

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
                            -o "ailead-${APP_VERSION}.jar" \
                            "${NEXUS_URL}/repository/${NEXUS_REPOSITORY}/com/AI/ailead/${APP_VERSION}/ailead-${APP_VERSION}.jar"
                    '''
                }
            }
        }

        stage('Deploy') {
            steps {
                sshagent(['ailead-ec2-ssh']) {
                    sh '''
                        scp -o StrictHostKeyChecking=no \
                            "ailead-${APP_VERSION}.jar" \
                            ${AILEAD_USER}@${AILEAD_HOST}:${AILEAD_JAR}

                        ssh -o StrictHostKeyChecking=no \
                            ${AILEAD_USER}@${AILEAD_HOST} \
                            'sudo systemctl restart ailead'
                    '''
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                sshagent(['ailead-ec2-ssh']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no \
                            ${AILEAD_USER}@${AILEAD_HOST} \
                            "systemctl is-active --quiet ailead"

                        ssh -o StrictHostKeyChecking=no \
                            ${AILEAD_USER}@${AILEAD_HOST} \
                            "pgrep -f 'ailead-${APP_VERSION}.jar' > /dev/null"
                    '''
                }
            }
        }
    }
}
