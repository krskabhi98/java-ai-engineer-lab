pipeline {
    agent any

    environment {
        AILEAD_HOST = '172.31.44.91'
        AILEAD_USER = 'ec2-user'

        NEXUS_URL = 'http://172.31.43.26:8081'
        NEXUS_REPOSITORY = 'ailead-maven-releases'
    }

    stages {

        stage('Get Application Version') {
            steps {
                script {
                    env.APP_VERSION = sh(
                        script: "./gradlew properties -q | grep '^version:' | awk '{print \$2}'",
                        returnStdout: true
                    ).trim()

                    echo "Application version: ${env.APP_VERSION}"
                }
            }
        }

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
                            ${AILEAD_USER}@${AILEAD_HOST}:/home/ec2-user/java-ai-engineer-lab/build/libs/ailead-${APP_VERSION}.jar

                        ssh -o StrictHostKeyChecking=no \
                            ${AILEAD_USER}@${AILEAD_HOST} \
                            "sudo sed -i 's#build/libs/ailead-[^ ]*\\.jar#build/libs/ailead-${APP_VERSION}.jar#' /etc/systemd/system/ailead.service"

                        ssh -o StrictHostKeyChecking=no \
                            ${AILEAD_USER}@${AILEAD_HOST} \
                            'sudo systemctl daemon-reload'

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

                        echo "Deployment verification successful."
                        echo "Running version: ${APP_VERSION}"
                    '''
                }
            }
        }
    }
}
