package com.broadband.ui;

import com.broadband.utils.DBUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.OutputType;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
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
    protected static String currentPackageName;

    @BeforeClass
    public void initData() {
        // 生成随机用户名
        int randomNum = (int) (Math.random() * 900) + 100;
        currentUsername = "User" + randomNum;
        System.out.println("[Jenkins] 当前测试用户名: " + currentUsername);
    }

    @Test
    public void test01_Register() {
        driver.get(BASE_URL + "/page/login.html");
        // 打印当前 URL 便于确认页面是否真的加载到了登录页
        System.out.println("当前URL: " + driver.getCurrentUrl());

        // 点击注册
        String registerXpath = "//button[contains(., '注') and contains(., '册')]";
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
        driver.get(BASE_URL + "/page/login.html");

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
            System.out.println("登陆失败，未跳转到首页，还在登录页");
            driver.get(BASE_URL + "/front/home");
            sleep(1000);
        }

        // 验证用户名 (前提是你已经改了 Front.vue 让它显示 username)
        boolean hasUsername = driver.getPageSource().contains(currentUsername);
        Assert.assertTrue(hasUsername, "登录后页面未显示用户名！");
    }

    @Test(dependsOnMethods = "test02_Login")
    public void test03_SelectPackage() {
        driver.get(BASE_URL + "/front/Bandwidth");

        // 下单
        WebElement orderBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("(//button[contains(.,'下单')])[1]")
        ));
        orderBtn.click();

        // ... 前面的点击“下单”按钮代码保持不变 ...
        sleep(1000); // 等待弹窗动画完全展开

        try {
            // 🌟 1. 填【用户名】
            // 强力特征定位：找前面 label 写着“用户名”的那个输入框！
            WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//label[contains(.,'用户名')]/following-sibling::div//input")
            ));
            usernameInput.clear();
            usernameInput.sendKeys(currentUsername);

            // 🌟 2. 填【电话】
            WebElement phoneInput = driver.findElement(
                    By.xpath("//label[contains(.,'电话')]/following-sibling::div//input")
            );
            phoneInput.clear();
            phoneInput.sendKeys("13800138000");

            // 🌟 3. 填【地址】
            WebElement addressInput = driver.findElement(
                    By.xpath("//label[contains(.,'地址')]/following-sibling::div//input")
            );
            addressInput.clear();
            addressInput.sendKeys("安徽省滁州市琅琊区滁州学院");

            WebElement packageNameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//label[contains(.,'套餐名称')]/following-sibling::div//input")
            ));
            currentPackageName = packageNameInput.getAttribute("value");

        } catch (Exception e) {
            System.out.println("⚠️ 弹窗表单填写失败，请检查定位器！");
            throw e; // 如果填表失败，直接抛出异常中断测试
        }

        // 4. 点击【确定】按钮提交表单
        driver.findElement(By.cssSelector(".el-dialog__footer button.el-button--primary")).click();

        // 5. 验证是否保存成功
        boolean isSuccess = wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "成功"));
        Assert.assertTrue(isSuccess, "下单保存失败");
    }

    @Test(dependsOnMethods = "test03_SelectPackage")
    public void test04_Pay() {
        driver.get(BASE_URL + "/front/Order");

        //  1. 【动态提取金额】：智能锁定当前用户的行，拿第 6 列的价格！
        String uiPriceText = "0.00";
        try {
            // 大白话：找一个 <tr> (行)，要求这行里的某个 <td> 包含当前用户名。然后取这行的第 6 个 <td> 里的 div
            String priceXpath = "//tr[td[contains(., '" + currentUsername + "')]]/td[6]/div";

            WebElement priceElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(priceXpath)));
            uiPriceText = priceElement.getText().trim();
            System.out.println("【数据流追踪】成功锁定用户 " + currentUsername + " 的订单，抓取金额: " + uiPriceText);
        } catch (Exception e) {
            System.out.println("⚠️ 未能抓取到页面金额，请检查列数是否为第 6 列！");
            dumpDebugToReports("test04_抓取金额失败");
        }

        BigDecimal expectedFeeFromUI = new BigDecimal(uiPriceText);


        //  2. 只点当前用户那行的支付按钮！
        try {
            // 大白话：在当前用户所在的那一行里，寻找“支付”按钮
            String payBtnXpath = "//tr[td[contains(., '" + currentUsername + "')]]//button[contains(.,'支付')]";
            WebElement payBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(payBtnXpath)));

            // 使用 JS 点击法，防止 ElementUI 按钮被遮挡
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", payBtn);
        } catch (Exception e) {
            System.out.println("⚠️ 找不到该用户的支付按钮！");
            throw e;
        }

        sleep(1000); // 等待弹窗出现

        // 3. 点击弹窗上的【确定】
        WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".el-message-box__btns button.el-button--primary")
        ));
        confirmBtn.click();

        // 3. 原有的 UI 表面验证（验证弹窗）
        boolean isPaid = wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.className("el-message__content"), "成功"
        ));
        Assert.assertTrue(isPaid, "支付失败弹窗未出现");
        System.out.println("✅ UI层验证通过：前端已弹出支付成功提示。");

        // 🌟 4. 【终极对账】：UI 跑完别急着结束，立刻呼叫 DBUtil 去底层查账！
        System.out.println("🔍 开始执行底层数据一致性校验...");
        DBUtil dbUtil = new DBUtil();

        // 调用你之前写好的 verifyOrderConsistency 方法
        // 参数1：刚才注册时生成的随机用户名 currentUsername
        // 参数2：套餐名称（你可以写死，或者也用 getText() 抓下来）
        // 参数3：刚刚从前端动态抓下来并转换的预期金额 expectedFeeFromUI
        dbUtil.verifyOrderConsistency(currentUsername, currentPackageName, expectedFeeFromUI);
        System.out.println("🎉 全链路闭环测试通过：前端展示金额与底层物理落库金额完全一致！");
    }
}
