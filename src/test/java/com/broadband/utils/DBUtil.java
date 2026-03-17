package com.broadband.utils;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import java.math.BigDecimal;
import java.util.Map;

public class DBUtil {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/pxy?serverTimezone=GMT%2b8";
    private static final String DB_USER = "root";
    private static final String DB_PWD = "0502pxyzy";
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";

    private final JdbcTemplate jdbcTemplate;

    public DBUtil() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(DB_DRIVER);
        dataSource.setUrl(DB_URL);
        dataSource.setUsername(DB_USER);
        dataSource.setPassword(DB_PWD);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * 适配 sys_order 表的查询逻辑
     */
    public Map<String, Object> getLatestOrderByUsername(String username) {
        String sql = "SELECT name, fee FROM sys_order WHERE username = ? ORDER BY create_time DESC LIMIT 1";
        try {
            return jdbcTemplate.queryForMap(sql, username);
        } catch (Exception e) {
            throw new RuntimeException("【缺陷定位】sys_order 表中未找到用户 [" + username + "] 的记录。建议检查前端是否成功调用存盘接口。");
        }
    }

    /**
     对比套餐名称和费用
     */
    public void verifyOrderConsistency(String username, String expectedName, BigDecimal expectedFee) {
        Map<String, Object> actual = getLatestOrderByUsername(username);

        String actName = (String) actual.get("name");
        BigDecimal actFee = (BigDecimal) actual.get("fee");

        StringBuilder errorMsg = new StringBuilder("\n=== [数据一致性缺陷报告] ===\n");
        boolean hasError = false;

        if (!expectedName.equals(actName)) {
            errorMsg.append(String.format("【套餐名错误】预期: %s, 实际: %s. 建议: 检查套餐选择组件的 ID 与 Name 映射关系。\n", expectedName, actName));
            hasError = true;
        }
        if (expectedFee.compareTo(actFee) != 0) {
            errorMsg.append(String.format("【金额错误】预期: %s, 实际: %s. 建议: 检查后端计算 Fee 的逻辑是否存在精度丢失或配置错误。\n", expectedFee, actFee));
            hasError = true;
        }

        if (hasError) {
            throw new AssertionError(errorMsg.toString());
        } else {
            System.out.println(">>> [校验通过] 用户 " + username + " 的订单数据一致！(套餐:" + actName + ", 金额:" + actFee + ")");
        }
    }
}