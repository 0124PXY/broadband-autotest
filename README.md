#  Broadband AutoTest 自动化测试项目

##  项目简介

本项目是一个基于接口自动化 + UI 自动化 + BDD（行为驱动开发）的综合测试框架，模拟宽带业务系统的核心流程（如下单、套餐推荐、数据一致性校验等）。

实现了从 **接口调用 → UI操作 → 数据库校验** 的完整自动化测试闭环。

---

##  技术栈

* Java
* TestNG
* RestAssured（接口自动化）
* Selenium（UI自动化）
* Cucumber（BDD）
* MySQL（数据库校验）
* Maven

---

## 📂 项目结构

```
src
├── main
│   └── java
├── test
│   └── java
│       ├── api        # 接口测试
│       ├── ui         # UI自动化
│       ├── bdd        # Cucumber步骤定义
│       └── utils      # 工具类（DBUtil等）
└── resources
    └── features       # BDD用例
```

---

##  核心功能

### ✅ 接口自动化测试

* 使用 RestAssured 发送请求
* 校验接口返回结果
* 支持参数化请求

### ✅ UI 自动化测试

* 使用 Selenium 模拟用户操作
* 实现登录、下单等流程自动化

### ✅ BDD 测试（Cucumber）

* 使用 Gherkin（Given/When/Then）描述业务场景
* 实现测试与业务解耦

### ✅ 数据一致性校验

* 接口调用后自动查询数据库
* 校验订单数据是否成功入库
* 实现“接口 + DB”双重验证

---

##  如何运行

### 1️⃣ 克隆项目

```
git clone https://github.com/0124PXY/broadband-autotest.git
```

---

### 2️⃣ 配置数据库

修改配置文件：

```
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/pxy
    username: root
    password: 123456
```

---

### 3️⃣ 执行测试

#### ▶ 运行接口/UI测试

```
testng.xml
```

#### ▶ 运行BDD测试

```
CucumberRunner.java
```

---

##  项目亮点

* ✅ 实现接口 + UI + 数据库三层自动化测试
* ✅ 支持端到端（E2E）测试流程
* ✅ 自定义 DBUtil 实现数据库校验
* ✅ 使用 Cucumber 实现 BDD 测试模式
* ✅ 项目结构清晰，具备测试框架雏形

---

## 📌 作者

GitHub: https://github.com/0124PXY
