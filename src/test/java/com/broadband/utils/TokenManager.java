package com.broadband.utils;

public class TokenManager {

    // 声明一个 ThreadLocal，专门用来装每个线程专属的 Token
    private static ThreadLocal<String> tokenThreadLocal = new ThreadLocal<>();

    // 存 Token (通常在 Login 接口成功后调用)
    public static void setToken(String token) {
        tokenThreadLocal.set(token);
    }

    // 取 Token (在后续发业务请求前调用)
    public static String getToken() {
        return tokenThreadLocal.get();
    }

    // 清理 Token (用例跑完后调用，防止内存泄漏)
    public static void removeToken() {
        tokenThreadLocal.remove();
    }
}