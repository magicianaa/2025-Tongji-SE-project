# Smart Esports Hotel Management System - Backend

## 技术栈

- **Java**: 21 (JDK 21)
- **Spring Boot**: 3.2.0
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **ORM**: MyBatis-Plus 3.5.5
- **认证**: JWT
- **文档**: SpringDoc OpenAPI 3

## 快速开始

### 1. 前置条件

- JDK 21+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+

### 2. 数据库初始化

```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE esports_hotel_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 执行建表脚本
mysql -u root -p esports_hotel_db < ../database/schema.sql
```

### 3. 修改配置

编辑 `src/main/resources/application.yml`，修改数据库和Redis连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/esports_hotel_db
    username: root
    password: 你的密码
  data:
    redis:
      host: localhost
      password: 你的密码
```

### 4. 启动项目

```bash
# 方式1：Maven 命令
mvn spring-boot:run

# 方式2：IDE 运行
直接运行 EsportsHotelApplication.java 的 main 方法
```

### 5. 访问接口文档

启动成功后访问：
- Swagger UI: http://localhost:8080/api/swagger-ui.html
- API Docs: http://localhost:8080/api/v3/api-docs

## 项目结构

```
backend/
├── src/main/java/com/esports/hotel/
│   ├── common/          # 通用类（Result、异常等）
│   ├── config/          # 配置类
│   ├── controller/      # 控制器层
│   ├── dto/             # 数据传输对象
│   ├── entity/          # 实体类
│   ├── mapper/          # MyBatis Mapper
│   ├── service/         # 业务逻辑层
│   ├── util/            # 工具类
│   └── EsportsHotelApplication.java  # 启动类
└── src/main/resources/
    ├── application.yml  # 配置文件
    └── mapper/          # MyBatis XML（可选）
```

## 已实现功能

### 🔐 认证与权限
✅ 用户注册（手机号 + 验证码）
✅ 用户登录（JWT认证）
✅ 短信验证码（模拟实现）
✅ 二次鉴权拦截器（客房权限动态绑定）

### 🏨 客房管理 (PMS)
✅ 房态管理（查询所有/空闲房间）
✅ 办理入住（生成 Room-Auth-Token）
✅ 办理退房（汇总账单 + 回收权限）
✅ 房费计算（按小时计费）
✅ 积分赠送（消费1元=10积分）

### 🖥️ 硬件监控系统
✅ 硬件模拟器（正态分布生成数据）
✅ 实时状态监控（CPU/GPU温度、网络延迟）
✅ 三色健康等级（GREEN/YELLOW/RED）
✅ 自动报警机制（连续3次异常触发）
✅ 自动生成维修工单
✅ WebSocket 实时推送
✅ 设备日志归档

### 📊 其他
✅ 全局异常处理
✅ 统一响应封装
✅ API 接口文档（Swagger）
✅ 硬件监控大屏（测试页面）

## 下一步开发

⏳ 前端 Vue 3 项目初始化
⏳ POS 销售点系统（商品管理、挂账）
⏳ 社交匹配系统（组队、招募）
⏳ 游戏化积分系统（任务、商城）
⏳ 报表与数据分析

## API 示例

### 1. 认证接口

#### 发送验证码
```bash
POST /api/auth/sms/send?phone=13800138000
```

#### 用户注册
```bash
POST /api/auth/register
Content-Type: application/json

{
  "phone": "13800138000",
  "password": "abc123",
  "smsCode": "123456",
  "realName": "张三"
}
```

#### 用户登录
```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "13800138000",
  "password": "abc123"
}
```

### 2. 客房管理接口

#### 查询空闲房间
```bash
GET /api/rooms/vacant
Authorization: Bearer {token}
```

#### 办理入住
```bash
POST /api/rooms/checkin
Authorization: Bearer {token}
Content-Type: application/json

{
  "guestId": 1,
  "roomId": 1,
  "realName": "张三",
  "identityCard": "320102199001011234",
  "expectedCheckout": "2025-12-16T12:00:00"
}
```

#### 办理退房
```bash
POST /api/rooms/checkout/1?paymentMethod=WECHAT
Authorization: Bearer {token}
```

### 3. 硬件监控接口

#### 获取所有房间硬件状态
```bash
GET /api/hardware/status
```

#### 手动触发故障模拟（测试用）
```bash
POST /api/hardware/trigger-failure/1
```

#### 获取未处理报警
```bash
GET /api/hardware/alerts/unhandled
```

### 4. WebSocket 连接

#### 前端连接示例（JavaScript）
```javascript
const socket = new SockJS('http://localhost:8080/api/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    // 订阅硬件数据流（每5秒推送）
    stompClient.subscribe('/topic/hardware', function(message) {
        const data = JSON.parse(message.body);
        console.log('硬件数据:', data);
    });

    // 订阅报警通知
    stompClient.subscribe('/topic/alerts', function(message) {
        const alert = JSON.parse(message.body);
        console.log('报警通知:', alert);
    });
});
```

#### 测试页面
启动项目后访问：http://localhost:8080/api/hardware-monitor.html
