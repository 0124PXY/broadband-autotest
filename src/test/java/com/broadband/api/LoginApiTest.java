package com.broadband.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
        // 2. 准备登录数据
        String loginJson = "{ \"username\": \"admin\", \"password\": \"admin123\" }";

        given()
                .contentType(ContentType.JSON) // 告诉后端我发的是 JSON 格式
                .body(loginJson)               // 塞入用户名密码
                .when()
                .post("/user/login")
                .then()
                .log().all()                // 打印结果，方便调试
                .statusCode(200)            // 验证：状态码必须是 200 (成功)
                .body("code", equalTo("200  ")) // 验证：业务状态码
                .body("msg", notNullValue()); // 验证：必须要有返回信息
    }
}