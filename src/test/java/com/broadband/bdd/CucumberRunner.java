package com.broadband.bdd;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

/**
 * BDD 测试运行入口
 * 作用：把 Feature 文件和 Step 代码关联起来，并生成报告
 */
@CucumberOptions(
        features = "src/test/resources/features", // 指定 Feature 文件位置
        glue = "com.broadband.bdd.steps",         // 指定 Step 代码位置
        plugin = {
                "pretty",                             // 控制台打印漂亮的日志
                "html:target/cucumber-reports.html",  // 生成 HTML 报告
                "json:target/cucumber-reports.json"   // 生成 JSON 报告
        }
)
public class CucumberRunner extends AbstractTestNGCucumberTests {
    // 里面什么都不用写，全靠注解干活
}