package com.broadband.utils;

import java.util.Random;

public class DataGenerator {
    // 修改生成规则：生成 8 位长度的用户名 (test + 4位随机数)
    public static String generateUsername() {
        // 生成一个 1000 到 9999 的随机数
        int randomNum = new Random().nextInt(9000) + 1000;
        return "test" + randomNum; // 结果类似 test5678 (8位)，符合 3-10 规则
    }
}