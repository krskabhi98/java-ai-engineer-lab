pipeline {
    agent any

    environment {
        // Application
        AILEAD_HOST = '172.31.44.91'
        AILEAD_USER = 'ec2-user'

        // Nexus
        NEXUS_URL = 'http://172.31.43.26:8081'
        NEXUS_REPOSITORY = 'ailead-maven-releases'

        // AWS / ECR
        AWS_REGION = 'ap-southeast-2'
        ECR_REGISTRY = '958280224408.dkr.ecr.ap-southeast-2.amazonaws.com'
        ECR_REPOSITORY = 'ailead'
    }

    stages {

        stage('Get Application Version') {
            steps {
                script {
                    env.APP_VERSION = sh(
                        script: "./gradlew properties -q | grep '^version:' | cut -d' ' -f2",
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

        stage('Docker Build') {
            steps {
                sh '''
                    echo "Building Docker image..."

                    docker build \
                        --build-arg JAR_FILE="ailead-${APP_VERSION}.jar" \
                        -t "${ECR_REGISTRY}/${ECR_REPOSITORY}:${APP_VERSION}" \
                        .

                    echo "Docker image built successfully."

                    docker images \
                        "${ECR_REGISTRY}/${ECR_REPOSITORY}:${APP_VERSION}"
                '''
            }
        }

        stage('Push Image to ECR') {
            steps {
                sh '''
                    echo "Logging in to Amazon ECR..."

                    aws ecr get-login-password \
                        --region "${AWS_REGION}" | \
                        docker login \
                        --username AWS \
                        --password-stdin "${ECR_REGISTRY}"

                    echo "Pushing image to ECR..."

                    docker push \
                        "${ECR_REGISTRY}/${ECR_REPOSITORY}:${APP_VERSION}"

                    echo "Image pushed successfully."
                '''
            }
        }

        stage('Deploy Container') {
            steps {
                withCredentials([
                    string(
                        credentialsId: 'google-genai-api-key',
                        variable: 'GOOGLE_GENAI_API_KEY'
                    )
                ]) {
                    sshagent(['ailead-ec2-ssh']) {
                        sh '''
                            echo "Deploying AILEAD version ${APP_VERSION}..."

                            echo "Preparing Docker network..."

                            ssh -o StrictHostKeyChecking=no \
                                ${AILEAD_USER}@${AILEAD_HOST} \
                                "docker network inspect ailead-net >/dev/null 2>&1 || docker network create ailead-net"

                            echo "Logging in to ECR on AILEAD EC2..."

                            ssh -o StrictHostKeyChecking=no \
                                ${AILEAD_USER}@${AILEAD_HOST} \
                                "aws ecr get-login-password \
                                --region ${AWS_REGION} | \
                                docker login \
                                --username AWS \
                                --password-stdin ${ECR_REGISTRY}"

                            echo "Pulling image from ECR..."

                            ssh -o StrictHostKeyChecking=no \
                                ${AILEAD_USER}@${AILEAD_HOST} \
                                "docker pull ${ECR_REGISTRY}/${ECR_REPOSITORY}:${APP_VERSION}"

                            echo "Preparing application secret..."

                            printf 'GOOGLE_GENAI_API_KEY=%s\\n' "${GOOGLE_GENAI_API_KEY}" | \
                                ssh -o StrictHostKeyChecking=no \
                                ${AILEAD_USER}@${AILEAD_HOST} \
                                'cat > /tmp/ailead.env && chmod 600 /tmp/ailead.env'

                            cleanup_secret() {
                                echo "Cleaning up temporary secret..."

                                ssh -o StrictHostKeyChecking=no \
                                    ${AILEAD_USER}@${AILEAD_HOST} \
                                    "rm -f /tmp/ailead.env"
                            }

                            trap cleanup_secret EXIT

                            echo "Stopping previous AILEAD container..."

                            ssh -o StrictHostKeyChecking=no \
                                ${AILEAD_USER}@${AILEAD_HOST} \
                                "docker rm -f ailead >/dev/null 2>&1 || true"

                            echo "Starting new AILEAD container..."

                            ssh -o StrictHostKeyChecking=no \
                                ${AILEAD_USER}@${AILEAD_HOST} \
                                "docker run -d \
                                    --name ailead \
                                    --network ailead-net \
                                    --env-file /tmp/ailead.env \
                                    -e DB_URL=jdbc:postgresql://ailead-postgres:5432/ailead \
                                    -p 8080:8080 \
                                    --restart unless-stopped \
                                    ${ECR_REGISTRY}/${ECR_REPOSITORY}:${APP_VERSION}"

                            echo "Deployment completed."
                        '''
                    }
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                sshagent(['ailead-ec2-ssh']) {
                    sh '''
                        echo "Verifying deployment..."

                        echo "Checking Docker container..."

                        ssh -o StrictHostKeyChecking=no \
                            ${AILEAD_USER}@${AILEAD_HOST} \
                            "docker ps --filter name=ailead --format '{{.Names}} {{.Status}}'"

                        echo "Checking container is running..."

                        ssh -o StrictHostKeyChecking=no \
                            ${AILEAD_USER}@${AILEAD_HOST} \
                            "docker inspect -f '{{.State.Running}}' ailead" | grep -q true

                        echo "Checking deployed image..."

                        RUNNING_IMAGE=$(ssh -o StrictHostKeyChecking=no \
                            ${AILEAD_USER}@${AILEAD_HOST} \
                            "docker inspect -f '{{.Config.Image}}' ailead")

                        echo "Running image: ${RUNNING_IMAGE}"

                        EXPECTED_IMAGE="${ECR_REGISTRY}/${ECR_REPOSITORY}:${APP_VERSION}"

                        if [ "${RUNNING_IMAGE}" != "${EXPECTED_IMAGE}" ]; then
                            echo "ERROR: Wrong image running."
                            echo "Expected: ${EXPECTED_IMAGE}"
                            echo "Actual:   ${RUNNING_IMAGE}"
                            exit 1
                        fi

                        echo "Checking application logs..."

                        ssh -o StrictHostKeyChecking=no \
                            ${AILEAD_USER}@${AILEAD_HOST} \
                            "docker logs --tail 30 ailead"

                        echo "Deployment verification successful."
                        echo "Running version: ${APP_VERSION}"
                    '''
                }
            }
        }
    }
}
