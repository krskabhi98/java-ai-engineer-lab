pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                git branch: 'develop',
                    url: 'https://github.com/krskabhi98/java-ai-engineer-lab.git'
            }
        }

        stage('Build') {
            steps {
                sh './gradlew clean build'
            }
        }

    }
}
