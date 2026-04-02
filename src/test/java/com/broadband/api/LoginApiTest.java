package com.broadband.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response; // 新增：用于接收服务器响应
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.broadband.utils.TokenManager; // 新增：引入咱们刚刚建的工具类

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class LoginApiTest {

    @BeforeClass
    public void setup() {
        // 本地启动是 http://localhost:9090
        RestAssured.baseURI = "http://localhost:9090";
    }

    @Test
    public void testLoginSuccess() {
        // 1. 准备登录数据
        String loginJson = "{ \"username\": \"admin\", \"password\": \"admin123\" }";

        // 2. 发送请求，做断言，并【提取出完整的响应体】
        Response response = given()
                .contentType(ContentType.JSON) // 告诉后端我发的是 JSON 格式
                .body(loginJson)               // 塞入用户名密码
                .when()
                .post("/user/login")
                .then()
                .log().all()                // 打印结果，方便调试
                .statusCode(200)            // 验证：状态码必须是 200 (成功)
                .body("msg", notNullValue()) // 验证：必须要有返回信息
                .extract().response();      //提取完整的响应体存入 response 对象

        // 3. 从响应体中提取 Token，并存入我们的 ThreadLocal “储物柜”
        // ⚠️ 注意：这里的 "data.token" 需要根据你后端实际返回的 JSON 结构来写。
        // 比如后端返回 {"code": 200, "data": {"token": "xxx..."}}，就写 "data.token"
        // 如果后端返回 {"code": 200, "token": "xxx..."}，就直接写 "token"
        String token = response.jsonPath().getString("data.token");

        // 4. 正式存入 TokenManager
        TokenManager.setToken(token);

        // 打印出来确认一下
        System.out.println("✅ 登录成功！Token已存入当前线程：" + TokenManager.getToken());
    }
}