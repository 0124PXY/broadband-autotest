package com.broadband.ui;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.time.Duration;

public class BaseTest {

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
//         如果是在 Jenkins/Linux 服务器跑，必须解开下面这几行的注释：
         options.addArguments("--headless"); // 无头模式，不显示浏览器界面
         options.addArguments("--no-sandbox"); // Linux 必备
         options.addArguments("--disable-dev-shm-usage"); // 防止内存溢出
         options.addArguments("--window-size=1920,1080"); // 指定分辨率，防止元素挤压不可见

        // 2. 启动驱动
        driver = new ChromeDriver(options);

        // 3. 全局隐式等待 (找元素最长等 5 秒)
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

        // 4. 显式等待 (用于复杂交互)
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
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