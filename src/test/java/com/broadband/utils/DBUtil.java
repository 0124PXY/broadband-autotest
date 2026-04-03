package com.broadband.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBUtil {

    // 声明 Hikari 数据源
    private static HikariDataSource dataSource;

    // 使用静态代码块，确保只在类加载时初始化一次连接池
    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://192.168.78.173:3306/pxy?useSSL=false&serverTimezone=UTC&characterEncoding=utf8");
        config.setUsername("root");
        config.setPassword("0502pxyzy");

        config.setMaximumPoolSize(10); // 自动化测试并发不高，10个连接绰绰有余
        config.setMinimumIdle(2);      // 最小空闲连接数
        config.setIdleTimeout(30000);  // 空闲连接存活时间
        config.addDataSourceProperty("cachePrepStmts", "true"); // 开启预编译缓存，提升验证查询速度
        config.addDataSourceProperty("prepStmtCacheSize", "250");

        // 实例化数据源
        dataSource = new HikariDataSource(config);
    }

    /**
     * 对外提供获取连接的方法
     */
    public static Connection getConnection() throws SQLException {
        // 从池中“借用”一个连接，而不是新建
        return dataSource.getConnection();
    }

    /**
     * 你的核心断言方法：利用 SQL 追踪数据流向
     */
    public void verifyOrderConsistency(String username, String expectedPackage, BigDecimal expectedFee) {
        String sql = "SELECT name, fee FROM sys_order WHERE username = ? ORDER BY id DESC LIMIT 1";

        // try-with-resources 语法：执行完毕后会自动调用 conn.close()
        // 注意：在 HikariCP 中，conn.close() 不是断开物理连接，而是将连接“归还”给连接池
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String actualPackage = rs.getString("name");
                    BigDecimal actualFee = rs.getBigDecimal("fee");

                    if (!expectedPackage.equals(actualPackage) || expectedFee.compareTo(actualFee) != 0) {
                        throw new AssertionError("【数据一致性缺陷】期望套餐: " + expectedPackage + ", 实际套餐: " + actualPackage);
                    }
                    System.out.println("✅ [HikariCP 池化校验] 数据库底层闭环验证通过！用户：" + username);
                } else {
                    throw new AssertionError("【数据丢失缺陷】未查到用户 " + username + " 的落库订单，存在静默回滚风险！");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("数据库校验执行异常: " + e.getMessage());
        }
    }
}