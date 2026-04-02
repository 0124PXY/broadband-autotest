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

    // 被测前端地址
    // 🌟 核心大招：动态读取系统变量。如果读不到（本地），就默认用 localhost
    public static final String BASE_URL = System.getProperty("env.url", "http://localhost:8080");

    // 声明为 static，保证整个测试套件共享同一个浏览器实例
    protected static WebDriver driver;
    protected static WebDriverWait wait;

    @BeforeSuite
    public void setUp() {
        // 1. 设置 Chrome 选项
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        // 分辨率无论是无头还是本地都建议加上，保证渲染的页面结构一致
        options.addArguments("--window-size=1920,1080");

        // 🌟 核心魔法：读取 Maven 传进来的环境变量。默认是 false（可视化）
        boolean isHeadless = Boolean.parseBoolean(System.getProperty("headless", "false"));

        if (isHeadless) {
            System.out.println("🐳 [CI/CD 环境] 启动 Chrome 无头模式...");
            // 🟢【Jenkins 关键配置】
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
        } else {
            System.out.println("🖥️ [本地调试] 启动可视化 Chrome 浏览器...");
            // 本地调试时，开启界面并最大化窗口
            options.addArguments("--start-maximized");
        }

        // 2. 启动驱动
        // 推荐：CI（Jenkins Docker）里使用远程 Selenium（Standalone Chrome / Grid）
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