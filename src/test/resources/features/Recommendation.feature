Feature: 宽带套餐智能推荐算法
  为了提高用户转化率
  作为销售系统
  我需要根据用户的家庭场景和预算，自动推荐最合适的宽带套餐

  # Scenario Outline 允许我们用一张表格测试多种情况 (数据驱动)
  Scenario Outline: 根据用户画像推荐套餐
    Given 用户居住在 "<HouseType>"
    And 用户的平时主要上网用途是 "<Usage>"
    And 用户的月费预算是 <Budget> 元
    When 系统执行推荐算法
    Then 推荐的套餐带宽应为 "<ExpectedBandwidth>"
    And 推荐理由应包含 "<ReasonKeyword>"

    Examples: 核心测试数据集
      | HouseType | Usage       | Budget | ExpectedBandwidth | ReasonKeyword |
      | 小户型     | 网页浏览     | 30     | 100M              | 实惠          |
      | 大平层     | 4K视频/直播  | 100    | 500M              | 高清          |
      | 别墅       | 电竞游戏     | 200    | 1000M             | 低延迟        |
      | 出租屋     | 少量办公     | 40     | 300M              | 性价比        |

  Scenario: 验证套餐推荐并校验订单落库一致性
    Given 用户登录系统
    When 用户选择 "59元套餐" 并提交订单
    Then 页面应显示 "下单成功"
    And 数据库中用户 "平星宇" 的最新订单应为 "59元套餐" 且金额为 59.00