# 🚀 快速启动指南

## Step 1: 环境准备

### 必需组件
- ✅ JDK 21
- ✅ Maven 3.8+
- ✅ MySQL 8.0
- ✅ Redis 6.0+

### 验证安装
```bash
# 检查 Java 版本
java -version

# 检查 Maven 版本
mvn -version

# 检查 MySQL
mysql --version

# 检查 Redis
redis-cli --version
```

---

## Step 2: 数据库初始化

### 1. 启动 MySQL
```bash
# Windows (如果使用 MySQL 服务)
net start MySQL80

# 或直接启动 MySQL 客户端
mysql -u root -p
```

### 2. 创建数据库并执行建表脚本
```sql
-- 创建数据库
CREATE DATABASE esports_hotel_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE esports_hotel_db;

-- 执行建表脚本
SOURCE D:/java_project2/database/schema.sql;

-- 验证表是否创建成功
SHOW TABLES;
```

**预期输出（应该看到以下表）：**
```
+------------------------------+
| Tables_in_esports_hotel_db   |
+------------------------------+
| tb_alert_log                 |
| tb_checkin_record            |
| tb_device_log                |
| tb_guest                     |
| tb_hardware_status           |
| tb_maintenance_ticket        |
| tb_room                      |
| tb_system_config             |
| tb_user                      |
| ... (其他表)                  |
+------------------------------+
```

---

## Step 3: 启动 Redis

```bash
# Windows
redis-server

# 验证 Redis 是否运行
redis-cli ping
# 应该返回: PONG
```

---

## Step 4: 配置应用

编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/esports_hotel_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: 你的MySQL密码  # ⚠️ 修改这里
  
  data:
    redis:
      host: localhost
      port: 6379
      password:  # 如果Redis设置了密码，填写在这里
```

---

## Step 5: 启动后端服务

### 方式1：使用 Maven（推荐）
```bash
cd D:/java_project2/backend
mvn clean spring-boot:run
```

### 方式2：使用 IDE
1. 用 IntelliJ IDEA 或 Eclipse 打开 `backend` 目录
2. 找到 `EsportsHotelApplication.java`
3. 右键 → Run 'EsportsHotelApplication.main()'

### 启动成功标志
看到以下输出说明启动成功：
```
╔═══════════════════════════════════════════════════════════╗
║                                                           ║
║   Smart Esports Hotel Management System Started!         ║
║   智慧电竞酒店管理系统启动成功                               ║
║                                                           ║
║   Swagger UI: http://localhost:8080/api/swagger-ui.html  ║
║   API Docs:   http://localhost:8080/api/v3/api-docs      ║
║                                                           ║
╚═══════════════════════════════════════════════════════════╝
```

---

## Step 6: 验证功能

### 1. 访问 API 文档
浏览器打开：http://localhost:8080/api/swagger-ui.html

### 2. 测试接口（Swagger UI）

#### a. 发送验证码
```
POST /api/auth/sms/send?phone=13800138000
```
**响应：**
```json
{
  "code": 200,
  "message": "验证码已发送",
  "timestamp": 1702654321000
}
```

**查看后端日志，找到验证码：**
```
【模拟短信】手机号: 13800138000, 验证码: 123456 (5分钟内有效)
```

#### b. 用户注册
```json
POST /api/auth/register
{
  "phone": "13800138000",
  "password": "abc123",
  "smsCode": "123456",
  "realName": "测试用户"
}
```

#### c. 用户登录
```json
POST /api/auth/login
{
  "username": "13800138000",
  "password": "abc123"
}
```
**复制返回的 `accessToken`，后续请求需要用到！**

#### d. 查询空闲房间
```
GET /api/rooms/vacant
Authorization: Bearer {上面复制的token}
```

### 3. 测试硬件监控大屏
浏览器打开：http://localhost:8080/api/hardware-monitor.html

**预期效果：**
- 看到所有房间的实时硬件状态
- 温度、延迟等数据每5秒自动刷新
- 绿色/黄色/红色卡片根据健康状态动态变化

### 4. 手动触发故障测试
在 Swagger UI 中执行：
```
POST /api/hardware/trigger-failure/1
```
然后观察监控大屏，应该看到：
- 房间1的卡片变为红色并闪烁
- 右侧弹出报警通知
- 后端日志输出：`🚨 触发报警！房间: 201...`

---

## 常见问题排查

### ❌ 问题1：启动报错 "Cannot load driver class: com.mysql.cj.jdbc.Driver"
**解决：** 确认 `pom.xml` 中包含 MySQL 驱动依赖

### ❌ 问题2：数据库连接失败
**检查：**
1. MySQL 服务是否启动？
2. 数据库名称是否正确？
3. 用户名密码是否正确？
4. 防火墙是否阻止了 3306 端口？

### ❌ 问题3：Redis 连接失败
**解决：**
```bash
# 启动 Redis
redis-server

# 验证
redis-cli ping
```

### ❌ 问题4：WebSocket 连接失败
**检查：**
1. 后端是否正常启动？
2. 浏览器控制台是否有跨域错误？
3. 端口 8080 是否被占用？

---

## 下一步

✅ 后端已完成：认证、PMS、硬件监控、WebSocket
⏳ 开始前端开发（Vue 3 + Element Plus）

---

## 技术支持

遇到问题？检查：
1. 后端日志：`logs/esports-hotel.log`
2. MySQL 日志
3. Redis 日志

祝开发顺利！🎉
