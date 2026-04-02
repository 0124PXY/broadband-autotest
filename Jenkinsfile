pipeline {
    agent any

    environment {
        // 🌟 1. 定义环境变量：被测前端系统地址（注意宿主机映射）宿主机端口 : 容器内部端口 8888/8080
        BASE_URL = 'http://host.docker.internal:8080'

        // 🌟 2. 远程 Selenium 节点地址
        SELENIUM_REMOTE_URL = 'http://selenium-chrome:4444/wd/hub'
    }

    triggers {
        cron('H H * * *')
    }

    parameters {
        string(name: 'RECIPIENTS', defaultValue: '2869735383@qq.com', description: '测试报告接收人（逗号分隔）')
    }

    options {
        timestamps()
        skipStagesAfterUnstable()
    }

    stages {
        stage('1. Checkout') {
            steps {
                checkout scm
            }
        }

        stage('1.1 Maven工具初始化') {
            steps {
                script {
                    env.MVN_HOME = tool 'maven3'
                    env.PATH = "${env.MVN_HOME}/bin:${env.PATH}"
                    sh 'mvn -v'
                }
            }
        }

        stage('2. 环境准备') {
            steps {
                sh 'mvn -B -U -Dfile.encoding=UTF-8 clean'
            }
        }

        stage('3. 执行测试套件') {
            steps {
                echo '正在启动自动化测试框架 (CI/CD 模式)...'
                // 🌟 核心修改：
                // 1. 必须用双引号 """，这样 Groovy 才能把 ${BASE_URL} 替换成真实地址
                // 2. 通过 -D 把 env.url 和 headless=true 传递给底层的 BaseTest.java
                sh """
                mvn -B -U -Dfile.encoding=UTF-8 \
                    -Denv.url=${BASE_URL} \
                    -Dheadless=true \
                    test
                """
            }
        }
    }

    post {
        always {
            script {
                def result = currentBuild.currentResult ?: 'UNKNOWN'

                junit testResults: 'target/surefire-reports/**/*.xml', allowEmptyResults: true
                archiveArtifacts artifacts: 'target/surefire-reports/**/*.*', allowEmptyArchive: true

                def reportDir = 'target/surefire-reports'

                if (fileExists(reportDir)) {
                    try {
                        emailext(
                                to: params.RECIPIENTS,
                                subject: "[Jenkins] ${env.JOB_NAME} #${env.BUILD_NUMBER} - ${result}",
                                body: """
                                <p>构建结果：<b>${result}</b></p>
                                <p>构建链接：<a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                                <p>测试报告：已归档到 <code>${reportDir}/</code>（并附带 HTML）</p>
                            """,
                                attachmentsPattern: "${reportDir}/**/*.html"
                        )
                    } catch (e) {
                        echo "邮件发送失败（忽略不影响构建结果）：${e}"
                    }
                } else {
                    try {
                        emailext(
                                to: params.RECIPIENTS,
                                subject: "[Jenkins] ${env.JOB_NAME} #${env.BUILD_NUMBER} - ${result}",
                                body: """
                                <p>构建结果：<b>${result}</b></p>
                                <p>构建链接：<a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                                <p>测试报告：未检测到 <code>${reportDir}/</code>（可能是编译/执行阶段未生成）</p>
                            """
                        )
                    } catch (e) {
                        echo "邮件发送失败 ：${e}"
                    }
                }
            }
        }
    }
}