package com.broadband.ui;

import com.broadband.utils.DBUtil;
import org.testng.annotations.Test;
import java.math.BigDecimal;

public class OrderDataConsistencyTest {

    @Test(description = "验证 sys_order 表中真实数据的一致性")
    public void verifyOrderDataConsistency() {
        // 1. 模拟你在 UI 或 API 操作后的预期值
        String username = "平星宇";
        String expectedPackageName = "59元套餐";
        BigDecimal expectedFee = new BigDecimal("59.00");

        // 2. 初始化 DBUtil 并执行“数据流向追踪”
        DBUtil dbUtil = new DBUtil();

        // 3. 执行校验（如果数据库里是 59.00，这里就会 Pass）
        dbUtil.verifyOrderConsistency(username, expectedPackageName, expectedFee);
    }
}