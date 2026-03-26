package com.broadband.ui;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.net.URL;
import java.time.Duration;

public class BaseTest {

    // 被测前端地址：建议仅在这里改一次，其他测试统一引用该常量
    public static final String BASE_URL = "http://host.docker.internal:8080";

    // 声明为 static，保证整个测试套件共享同一个浏览器实例
    protected static WebDriver driver;
    protected static WebDriverWait wait;

    @BeforeSuite
    public void setUp() {
        // 1. 设置 Chrome 选项 (Jenkins 必备配置)
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--start-maximized"); // 本地调试用

        // 🟢【Jenkins 关键配置】
        options.addArguments("--headless=new"); // 无头模式（容器更稳定）
        options.addArguments("--no-sandbox"); // Linux/容器常用
        options.addArguments("--disable-dev-shm-usage"); // 防止 /dev/shm 太小导致崩溃
        options.addArguments("--window-size=1920,1080"); // 指定分辨率，防止元素挤压不可见

        // 2. 启动驱动
        // 推荐：CI（Jenkins Docker）里使用远程 Selenium（Standalone Chrome / Grid）
        // 用环境变量注入，例如：
        // SELENIUM_REMOTE_URL=http://host.docker.internal:4444/wd/hub
        String remoteUrl = System.getenv("SELENIUM_REMOTE_URL");
        if (remoteUrl != null && !remoteUrl.isBlank()) {
            try {
                driver = new RemoteWebDriver(new URL(remoteUrl), options);
            } catch (Exception e) {
                throw new RuntimeException("连接远程 Selenium 失败: " + remoteUrl, e);
            }
        } else {
            // 本地模式：要求运行环境已安装 Chrome + chromedriver
            driver = new ChromeDriver(options);
        }

        // 3. 全局隐式等待 (找元素最长等 5 秒)
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

        // 4. 显式等待 (用于复杂交互)
        // CI 环境比本地慢，适当加大等待时间更稳
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @AfterSuite
    public void tearDown() {
        if (driver != null) {
            // 退出浏览器，释放资源
            driver.quit();
        }
    }

    /**
     * 封装一个简单的硬等待，避免测试代码里到处是 try-catch
     */
    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}