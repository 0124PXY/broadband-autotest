pipeline {
    agent any

    // 每天触发一次 Daily Build（建议结合时区/峰值调整 cron 时间）
    triggers {
        cron('H H * * *')
    }

    parameters {
        string(name: 'RECIPIENTS', defaultValue: '2869735383@qq.com', description: '测试报告接收人（逗号分隔）')
    }

    options {
        timestamps()
        // 构建失败也继续执行 post 阶段，便于发报告
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
                    // 使用 Jenkins 全局工具配置中的 Maven（名称：maven3）
                    env.MVN_HOME = tool 'maven3'
                    env.PATH = "${env.MVN_HOME}/bin:${env.PATH}"
                    sh 'mvn -v'
                }
            }
        }

        stage('2. 环境准备') {
            steps {
                // 你的 Jenkins 实际运行在 Linux 容器中，因此统一使用 sh 执行 Maven
                sh 'mvn -B -U -Dfile.encoding=UTF-8 clean'
            }
        }

        stage('3. 执行测试套件') {
            steps {
                echo '正在启动自动化测试框架...'
                // pom.xml 会自动读取 src/test/resources/testng.xml
                sh 'mvn -B -U -Dfile.encoding=UTF-8 test'
            }
        }
    }

    post {
        always {
            script {
                def result = currentBuild.currentResult ?: 'UNKNOWN'

                // 1) JUnit 插件：解析 surefire-reports 下的 XML，生成可视化测试报告
                //    allowEmptyResults：避免“未生成 XML”导致 post 阶段直接报错
                junit testResults: 'target/surefire-reports/**/*.xml', allowEmptyResults: true

                // 2) 归档整个报告目录（包含 HTML/LOG 等），便于回溯
                archiveArtifacts artifacts: 'target/surefire-reports/**/*.*', allowEmptyArchive: true

                // 3) 邮件通知：发送构建结果 + 报告链接，并附上 HTML 报告（可选）
                //    依赖：需要 Jenkins 已安装并配置 Email Extension Plugin + SMTP 服务
                def reportDir = 'target/surefire-reports'

                if (fileExists(reportDir)) {
                    emailext(
                        to: params.RECIPIENTS,
                        subject: "[Jenkins] ${env.JOB_NAME} #${env.BUILD_NUMBER} - ${result}",
                        body: """
                            <p>构建结果：<b>${result}</b></p>
                            <p>构建链接：<a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                            <p>测试报告：已归档到 <code>${reportDir}/</code>（并附带 HTML）</p>
                        """,
                        // 只附带 HTML 报告，避免附件过大/匹配规则差异导致失败
                        attachmentsPattern: "${reportDir}/**/*.html"
                    )
                } else {
                    emailext(
                        to: params.RECIPIENTS,
                        subject: "[Jenkins] ${env.JOB_NAME} #${env.BUILD_NUMBER} - ${result}",
                        body: """
                            <p>构建结果：<b>${result}</b></p>
                            <p>构建链接：<a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                            <p>测试报告：未检测到 <code>${reportDir}/</code>（可能是编译/执行阶段未生成）</p>
                        """
                    )
                }
            }
        }
    }
}