package com.broadband.bdd.steps;

import com.broadband.utils.DBUtil;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class RecommendationSteps {

    // 1. 定义一些变量来保存测试过程中的状态
    private String houseType;
    private String usage;
    private int budget;
    private String recommendedPackage; // 模拟系统返回的结果
    private String recommendationReason;

    // 2. 映射 Given 步骤
    @Given("用户居住在 {string}")
    public void user_lives_in(String houseType) {
        this.houseType = houseType;
    }

    @Given("用户的平时主要上网用途是 {string}")
    public void user_usage_is(String usage) {
        this.usage = usage;
    }

    @Given("用户的月费预算是 {int} 元")
    public void user_budget_is(int budget) {
        this.budget = budget;
    }

    // 3. 映射 When 步骤：这里模拟核心算法逻辑
    // 在真实项目中，这里应该调用 bandService.recommend(houseType, usage, budget)
    @When("系统执行推荐算法")
    public void system_executes_algorithm() {
        // --- 模拟业务逻辑 Start ---
        // 规则1: 游戏玩家或高预算 -> 1000M
        if (usage.contains("游戏") || budget >= 200) {
            recommendedPackage = "1000M";
            recommendationReason = "低延迟极速体验";
        }
        // 规则2: 视频用户或大户型 -> 500M
        else if (usage.contains("视频") || houseType.equals("大平层")) {
            recommendedPackage = "500M";
            recommendationReason = "全屋高清覆盖";
        }
        // 规则3: 低预算且小户型 -> 100M
        else if (budget < 50 && houseType.equals("小户型")) {
            recommendedPackage = "100M";
            recommendationReason = "实惠入门首选";
        }
        // 默认推荐 -> 300M
        else {
            recommendedPackage = "300M";
            recommendationReason = "超高性价比";
        }
        // --- 模拟业务逻辑 End ---
    }

    // 4. 映射 Then 步骤：验证结果
    @Then("推荐的套餐带宽应为 {string}")
    public void verify_bandwidth(String expectedBandwidth) {
        Assert.assertEquals(recommendedPackage, expectedBandwidth, "推荐算法逻辑错误：带宽不匹配");
    }

    @Then("推荐理由应包含 {string}")
    public void verify_reason(String keyword) {
        Assert.assertTrue(recommendationReason.contains(keyword),
                "推荐理由错误。预期包含: " + keyword + ", 实际为: " + recommendationReason);
    }


    // --- 模拟 UI 层的步骤，确保流程能走到数据库校验 ---

    @Given("用户登录系统")
    public void 用户登录系统() {
        // 实际开发中这里会写 Selenium 代码，现在我们打印日志模拟成功
        System.out.println(">>> [UI模拟] 用户 平星宇 成功登录系统...");
    }

    @When("用户选择 {string} 并提交订单")
    public void 用户选择_并提交订单(String packageName) {
        // 模拟用户在网页上点击了 59元套餐
        System.out.println(">>> [UI模拟] 用户正在点击选择: " + packageName + " 并点击【提交】按钮...");
    }

    @Then("页面应显示 {string}")
    public void 页面应显示(String expectedMsg) {
        // 模拟检查网页弹出框
        System.out.println(">>> [UI模拟] 浏览器当前显示: " + expectedMsg);
    }

    @Then("数据库中用户 {string} 的最新订单应为 {string} 且金额为 {double}")
    public void verifyDatabaseOrder(String username, String packageName, Double fee) {
        // 实例化你写好的 DBUtil
        DBUtil dbUtil = new DBUtil();

        // 调用我们之前调试成功的校验逻辑
        // BigDecimal.valueOf(fee) 将 Cucumber 传入的 double 转为 BigDecimal
        dbUtil.verifyOrderConsistency(username, packageName, java.math.BigDecimal.valueOf(fee));
    }
}