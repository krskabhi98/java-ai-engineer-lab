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
                        echo "Deploying AILEAD version ${APP_VERSION}"

                        echo "Copying artifact to AILEAD EC2..."

                        scp -o StrictHostKeyChecking=no \
                            "ailead-${APP_VERSION}.jar" \
                            ${AILEAD_USER}@${AILEAD_HOST}:/home/ec2-user/java-ai-engineer-lab/build/libs/ailead-${APP_VERSION}.jar

                        echo "Updating current application symlink..."

                        ssh -o StrictHostKeyChecking=no \
                            ${AILEAD_USER}@${AILEAD_HOST} \
                            "cd /home/ec2-user/java-ai-engineer-lab/build/libs && \
                             ln -sfn ailead-${APP_VERSION}.jar ailead-current.jar"

                        echo "Restarting AILEAD service..."

                        ssh -o StrictHostKeyChecking=no \
                            ${AILEAD_USER}@${AILEAD_HOST} \
                            'sudo systemctl restart ailead'

                        echo "Deployment command completed."
                    '''
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                sshagent(['ailead-ec2-ssh']) {
                    sh '''
                        echo "Verifying deployment..."

                        echo "Checking systemd service..."

                        ssh -o StrictHostKeyChecking=no \
                            ${AILEAD_USER}@${AILEAD_HOST} \
                            "systemctl is-active --quiet ailead"

                        echo "Checking current symlink..."

                        CURRENT_JAR=$(ssh -o StrictHostKeyChecking=no \
                            ${AILEAD_USER}@${AILEAD_HOST} \
                            "readlink /home/ec2-user/java-ai-engineer-lab/build/libs/ailead-current.jar")

                        echo "Current JAR: ${CURRENT_JAR}"

                        if [ "${CURRENT_JAR}" != "ailead-${APP_VERSION}.jar" ]; then
                            echo "ERROR: Symlink points to ${CURRENT_JAR}"
                            echo "Expected: ailead-${APP_VERSION}.jar"
                            exit 1
                        fi

                        echo "Checking running application process..."

                        ssh -o StrictHostKeyChecking=no \
                            ${AILEAD_USER}@${AILEAD_HOST} \
                            "pgrep -f 'ailead-current.jar' > /dev/null"

                        echo "Deployment verification successful."
                        echo "Running version: ${APP_VERSION}"
                    '''
                }
            }
        }
    }
}
