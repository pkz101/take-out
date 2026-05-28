<<<<<<< HEAD
# take-out
外卖点餐系统
=======
# Sky Take Out

苍穹外卖后端服务，采用 Maven 多模块组织：

- `sky-common`: 通用结果、常量、异常、工具类和配置属性
- `sky-pojo`: DTO、Entity、VO
- `sky-server`: Spring Boot Web 服务、控制器、业务实现、MyBatis Mapper

## 技术栈

- Spring Boot 2.7.3
- MyBatis + PageHelper
- MySQL + Druid
- Redis
- Knife4j / Swagger
- Lombok

## 本地启动

1. 准备 MySQL 和 Redis。
2. 复制 `sky-server/src/main/resources/application-dev.example.yml` 为 `application-dev.yml`。
3. 按本地环境填写数据库、Redis、OSS、微信和高德地图配置。
4. 编译并测试：

```bash
mvn test
```

5. 启动服务：

```bash
mvn -pl sky-server -am spring-boot:run
```

默认端口为 `8080`，接口文档地址为 `http://localhost:8080/doc.html`。

## 开发约定

- 不提交真实密钥、支付证书、OSS 访问密钥等敏感配置。
- 新增业务逻辑时优先补充单元测试或控制器测试。
- Redis 缓存 key 需要集中命名并在数据变更后及时清理。

## 已覆盖功能

- 管理端：员工管理、分类管理、菜品管理、套餐管理、订单管理、店铺营业状态设置、图片上传。
- 用户端：微信登录、商品浏览、购物车、地址簿、用户下单、订单支付、历史订单、再来一单、客户催单。
- 缓存与消息：菜品缓存、店铺状态 Redis 缓存、来单提醒和催单 WebSocket 推送。
- 任务调度：超时未支付订单自动取消、配送中订单定时自动完成。
- 数据统计：营业额、用户、订单、销量 Top10、经营概览和 Excel 报表导出。

## 关键接口补充

- `POST /user/order/submit` 用户下单
- `PUT /user/order/payment` 订单支付
- `GET /user/order/payment/status/{orderNumber}` 支付成功回调
- `GET /user/order/reminder/{id}` 客户催单
- `GET /admin/report/turnoverStatistics` 营业额统计
- `GET /admin/report/userStatistics` 用户统计
- `GET /admin/report/ordersStatistics` 订单统计
- `GET /admin/report/top10` 销量 Top10
- `GET /admin/report/businessData` 经营概览
- `GET /admin/report/export` Excel 报表导出
- `ws://host:port/ws/{sid}` 管理端 WebSocket 消息通道
>>>>>>> master
