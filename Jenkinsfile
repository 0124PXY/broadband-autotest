pipeline {
    agent any

    stages {
        stage('1. 环境准备') {
            steps {
                // Windows 用 bat，Linux 用 sh
                bat 'mvn clean'
            }
        }

        stage('2. 执行测试套件') {
            steps {
                echo '正在启动自动化测试框架...'
                // 🟢 关键：这里不需要 -Dtest 了！
                // 因为 pom.xml 会自动去读 testng.xml，知道要跑哪些测试
                bat 'mvn test'
            }
        }
    }

    post {
        always {
            echo '测试结束，正在生成报告...'
            // 如果你有 Allure 插件可以在这里加，没有就先空着
        }
    }
}