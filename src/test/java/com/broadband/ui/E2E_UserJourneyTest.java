package com.broadband.ui;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.OutputType;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * 继承 BaseTest，专注于业务流程
 */
public class E2E_UserJourneyTest extends BaseTest {

    // 存当前生成的用户名
    private String currentUsername;
    private final String currentPassword = "Password123!";
    private final String baseUrl = System.getenv().getOrDefault("BASE_URL", "http://localhost:8080");

    @BeforeClass
    public void initData() {
        // 生成随机用户名
        int randomNum = (int) (Math.random() * 900) + 100;
        currentUsername = "User" + randomNum;
        System.out.println("🚀 [Jenkins] 当前测试用户名: " + currentUsername);
    }

    @Test
    public void test01_Register() {
        driver.get(baseUrl + "/page/login.html");
        // 打印当前 URL 便于确认页面是否真的加载到了登录页
        System.out.println("当前URL: " + driver.getCurrentUrl());

        // 点击注册
        // normalize-space 兼容 ElementUI/Vue 可能导致文本两端有空格/换行
        String registerXpath = "//*[contains(normalize-space(.),'注册')]";
        try {
            WebElement registerLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(registerXpath)));
            // 滚动到可视区域，避免“不可点击/被遮挡”问题
            try {
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView({block:'center'});",
                        registerLink
                );
            } catch (Exception ignore) {
                // 不影响后续点击
            }
            registerLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(registerXpath)));
            registerLink.click();
        } catch (Exception e) {
            // 失败时保存截图+页面片段，方便你在 Jenkins 报告里直接定位原因
            dumpDebugToReports("test01_Register");
            throw e;
        }

        // 填表
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='请输入用户名']")))
                .sendKeys(currentUsername);
        driver.findElement(By.xpath("//input[@placeholder='请输入密码']"))
                .sendKeys(currentPassword);
        driver.findElement(By.xpath("//input[@placeholder='请确认密码']"))
                .sendKeys(currentPassword);

        // 提交
        driver.findElement(By.xpath("//button[contains(.,'注册')]")).click();

        // 验证
        boolean isSuccess = wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "成功"));
        Assert.assertTrue(isSuccess, "注册失败");
    }

    private void dumpDebugToReports(String stepName) {
        try {
            System.out.println("【Debug】 step=" + stepName + ", url=" + driver.getCurrentUrl());
            System.out.println("【Debug】 pageTitle=" + driver.getTitle());
            String pageSource = driver.getPageSource();
            if (pageSource != null) {
                int end = Math.min(pageSource.length(), 1500);
                System.out.println("【Debug】 pageSource 前1500字符：\n" + pageSource.substring(0, end));
            }
        } catch (Exception ignore) {
            // 不影响截图
        }

        try {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File dest = new File("target/surefire-reports/screenshots/" + stepName + "_" + System.currentTimeMillis() + ".png");
            File parent = dest.getParentFile();
            if (parent != null) parent.mkdirs();
            Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ignore) {
            // 不影响后续异常抛出
        }
    }

    @Test(dependsOnMethods = "test01_Register")
    public void test02_Login() {
        driver.get(baseUrl + "/page/login.html");

        // 填表
        WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='text']")));
        usernameInput.clear();
        usernameInput.sendKeys(currentUsername);
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys(currentPassword);

        // 点击登录
        driver.findElement(By.xpath("//button[contains(.,'登录')]")).click();

        // 🟢 你的稳定逻辑：等待 Vue 处理
        sleep(1500);

        // 🟢 你的兜底逻辑：Force Navigation
        if (driver.getCurrentUrl().contains("login")) {
            System.out.println("⚠️ 检测到未自动跳转，执行强制跳转兜底...");
            driver.get(baseUrl + "/front/home");
            sleep(1000);
        }

        // 验证用户名 (前提是你已经改了 Front.vue 让它显示 username)
        boolean hasUsername = driver.getPageSource().contains(currentUsername);
        Assert.assertTrue(hasUsername, "登录后页面未显示用户名！");
    }

    @Test(dependsOnMethods = "test02_Login")
    public void test03_SelectPackage() {
        driver.get(baseUrl + "/front/Bandwidth");

        // 下单
        WebElement orderBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("(//button[contains(.,'下单')])[1]")
        ));
        orderBtn.click();

        sleep(1000); // 等弹窗动画

        // 填手机号
        try {
            driver.findElement(By.xpath("//div[@class='el-dialog__body']//input")).sendKeys("13800138000");
        } catch (Exception e) {
            // 忽略异常
        }

        // 确定
        driver.findElement(By.cssSelector(".el-dialog__footer button.el-button--primary")).click();

        boolean isSuccess = wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "成功"));
        Assert.assertTrue(isSuccess, "下单保存失败");
    }

    @Test(dependsOnMethods = "test03_SelectPackage")
    public void test04_Pay() {
        driver.get(baseUrl + "/front/Order");

        // 支付
        WebElement payBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("(//button[contains(.,'支付')])[1]")
        ));
        payBtn.click();

        sleep(1000); // 等弹窗

        // 确定
        WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".el-message-box__btns button.el-button--primary")
        ));
        confirmBtn.click();

        // 验证
        boolean isPaid = wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.className("el-message__content"), "成功"
        ));
        Assert.assertTrue(isPaid, "支付失败");
    }
}