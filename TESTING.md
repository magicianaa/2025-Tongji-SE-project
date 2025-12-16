# 🧪 功能测试清单

## 测试前准备

### 后端
- [ ] MySQL 服务已启动
- [ ] Redis 服务已启动
- [ ] 数据库已初始化（执行 schema.sql）
- [ ] 后端服务已启动（看到启动成功提示）
- [ ] Swagger UI 可访问（http://localhost:8080/api/swagger-ui.html）

### 前端
- [ ] Node.js 已安装（node -v 查看版本）
- [ ] 前端依赖已安装（cd frontend && npm install）
- [ ] 前端开发服务器已启动（npm run dev）
- [ ] 浏览器可访问 http://localhost:5173

---

## 🎯 推荐测试顺序

建议使用**前端页面**进行测试，更直观且包含完整交互流程。  
如需 API 级别测试，可使用 Swagger UI 或 Postman。

---

## Module 1: 认证与权限系统

### 1.1 用户注册流程
- [ ] **发送验证码**
  - 接口：`POST /api/auth/sms/send?phone=13800138000`
  - 预期：返回 200，后端日志显示验证码
  - 实际验证码：________（从日志复制）

- [ ] **用户注册**
  - 接口：`POST /api/auth/register`
  - 请求体：
    ```json
    {
      "phone": "13800138000",
      "password": "abc123",
      "smsCode": "填入上面的验证码",
      "realName": "测试用户"
    }
    ```
  - 预期：返回 200，提示"注册成功"
  - 数据库验证：`SELECT * FROM tb_user WHERE phone='13800138000'`

### 1.2 用户登录
- [ ] **正常登录**
  - 接口：`POST /api/auth/login`
  - 请求体：
    ```json
    {
      "username": "13800138000",
      "password": "abc123"
    }
    ```
  - 预期：返回 accessToken
  - **保存Token**：______________________________

- [ ] **错误密码登录**
  - 使用错误密码：`password: "wrongpassword"`
  - 预期：返回 4002，提示"密码错误"

---

## 前端集成测试（推荐）

### 场景 1: 用户注册与登录
1. [ ] 打开浏览器访问 http://localhost:5173
2. [ ] 看到登录页面，点击"注册"标签
3. [ ] 输入手机号：13800138001
4. [ ] 点击"获取验证码"按钮
5. [ ] 查看后端控制台输出的验证码
6. [ ] 填写验证码、密码、确认密码、姓名、身份证号
7. [ ] 点击"注册"按钮
8. [ ] 看到"注册成功"提示
9. [ ] 自动切换到"登录"标签
10. [ ] 输入手机号和密码
11. [ ] 点击"登录"按钮
12. [ ] 成功跳转到控制台首页

### 场景 2: 房态管理
1. [ ] 点击左侧菜单"房态管理"
2. [ ] 看到所有房间卡片展示
3. [ ] 点击"空闲"筛选按钮，只显示空闲房间
4. [ ] 选择一个空闲房间，点击"办理入住"
5. [ ] 在弹窗中选择退房时间（明天）
6. [ ] 设置押金金额（默认500）
7. [ ] 点击"确认入住"
8. [ ] 看到"入住成功"提示
9. [ ] 该房间卡片变为红色（已入住状态）
10. [ ] 点击该房间的"办理退房"按钮
11. [ ] 确认退房
12. [ ] 看到"退房成功"提示
13. [ ] 该房间卡片变为黄色（待清洁状态）

### 场景 3: 硬件监控大屏
1. [ ] 点击左侧菜单"硬件监控"
2. [ ] 看到连接状态显示"WebSocket 已连接"（绿色脉动图标）
3. [ ] 观察所有房间的硬件监控卡片
4. [ ] 每5秒自动更新数据（观察时间戳变化）
5. [ ] 房间卡片根据健康状态显示不同颜色
   - 绿色：正常
   - 黄色：预警
   - 红色：告警（会有脉动动画）
6. [ ] 选择一个房间，点击"触发故障"按钮
7. [ ] 该房间卡片立即变为红色
8. [ ] 收到告警弹窗提示
9. [ ] 点击顶部"告警记录"按钮
10. [ ] 在弹窗中看到刚才的告警记录
11. [ ] 点击"处理"按钮
12. [ ] 告警状态变为"已处理"

### 场景 4: 退出登录
1. [ ] 点击右上角"退出登录"按钮
2. [ ] 自动跳转回登录页
3. [ ] localStorage 中的 token 已清除
4. [ ] 尝试直接访问 http://localhost:5173/rooms
5. [ ] 自动重定向到登录页

---

## 后端 API 测试（可选）

### Module 2: 客房管理（PMS）

#### 2.1 查询房间
- [ ] **查询所有房间**
  - 接口：`GET /api/rooms`
  - 请求头：`Authorization: Bearer {Token}`
  - 预期：返回4个房间（201, 202, 301, 302）

- [ ] **查询空闲房间**
  - 接口：`GET /api/rooms/vacant`
  - 预期：返回状态为 VACANT 的房间

#### 2.2 办理入住
- [ ] **正常入住**
  - 接口：`POST /api/rooms/checkin`
  - 请求头：`Authorization: Bearer {Token}`
  - 请求体：
    ```json
    {
      "guestId": 1,
      "roomId": 1,
      "realName": "张三",
      "identityCard": "320102199001011234",
      "expectedCheckout": "2025-12-16T18:00:00"
    }
    ```
  - 预期：返回 recordId 和 roomAuthToken
  - **保存 Room-Auth-Token**：______________________________

- [ ] **验证房态更新**
  - 数据库查询：`SELECT status FROM tb_room WHERE room_id=1`
  - 预期：status = 'OCCUPIED'

- [ ] **验证入住记录**
  - 数据库查询：`SELECT * FROM tb_checkin_record WHERE guest_id=1 AND actual_checkout IS NULL`
  - 预期：is_gaming_auth_active = 1

### 2.3 二次鉴权测试
- [ ] **使用 Room-Auth-Token 访问受保护接口**
  - 接口：`GET /api/rooms/test-room-auth`
  - 请求头：`Authorization: Bearer {Room-Auth-Token}`
  - 预期：返回 200，提示"客房权限验证通过"

- [ ] **使用普通 Token 访问受保护接口**
  - 接口：`GET /api/rooms/test-room-auth`
  - 请求头：`Authorization: Bearer {普通登录Token}`
  - 预期：返回 4101，提示"未找到有效的入住记录"

### 2.4 办理退房
- [ ] **正常退房**
  - 接口：`POST /api/rooms/checkout/{recordId}?paymentMethod=WECHAT`
  - 使用上面入住时返回的 recordId
  - 预期：返回账单详情（房费、入住时长等）

- [ ] **验证权限回收**
  - 数据库查询：`SELECT is_gaming_auth_active FROM tb_checkin_record WHERE record_id={recordId}`
  - 预期：is_gaming_auth_active = 0

- [ ] **验证房态更新**
  - 数据库查询：`SELECT status FROM tb_room WHERE room_id=1`
  - 预期：status = 'DIRTY'

- [ ] **验证积分赠送**
  - 数据库查询：`SELECT current_points FROM tb_guest WHERE guest_id=1`
  - 预期：积分 > 0（根据消费金额计算）

---

## Module 3: 硬件监控系统

### 3.1 查询硬件状态
- [ ] **获取所有房间硬件状态**
  - 接口：`GET /api/hardware/status`
  - 预期：返回所有房间的实时数据（CPU温度、GPU温度、网络延迟等）

### 3.2 模拟故障
- [ ] **手动触发故障**
  - 接口：`POST /api/hardware/trigger-failure/1`
  - 预期：
    - 返回 200
    - 后端日志显示：`🚨 触发报警！房间: 201...`
  
- [ ] **验证报警记录**
  - 数据库查询：`SELECT * FROM tb_alert_log WHERE room_id=1 ORDER BY alert_time DESC LIMIT 1`
  - 预期：alert_type = 'OVERHEAT', is_handled = 0

- [ ] **验证工单生成**
  - 数据库查询：`SELECT * FROM tb_maintenance_ticket WHERE room_id=1 ORDER BY create_time DESC LIMIT 1`
  - 预期：status = 'OPEN', priority = 'URGENT'

- [ ] **验证房态更新（如果原本是空闲）**
  - 数据库查询：`SELECT status FROM tb_room WHERE room_id=1`
  - 可能结果：status = 'MAINTENANCE'

### 3.3 报警管理
- [ ] **查询未处理报警**
  - 接口：`GET /api/hardware/alerts/unhandled`
  - 预期：返回至少1条报警记录

- [ ] **标记报警已处理**
  - 接口：`PUT /api/hardware/alerts/{alertId}/handle?handlerId=1`
  - 预期：返回 200

- [ ] **验证处理状态**
  - 数据库查询：`SELECT is_handled FROM tb_alert_log WHERE alert_id={alertId}`
  - 预期：is_handled = 1

### 3.4 工单管理
- [ ] **查询所有工单**
  - 接口：`GET /api/hardware/tickets`
  - 预期：返回所有工单列表

- [ ] **筛选待处理工单**
  - 接口：`GET /api/hardware/tickets?status=OPEN`
  - 预期：只返回 status='OPEN' 的工单

- [ ] **更新工单状态**
  - 接口：`PUT /api/hardware/tickets/{ticketId}/status?status=RESOLVED&notes=已更换散热器`
  - 预期：返回 200

- [ ] **验证工单更新**
  - 数据库查询：`SELECT status, resolution_notes, resolve_time FROM tb_maintenance_ticket WHERE ticket_id={ticketId}`
  - 预期：status='RESOLVED', resolve_time 不为空

---

## Module 4: WebSocket 实时通信

### 4.1 监控大屏测试
- [ ] **打开监控页面**
  - 地址：http://localhost:8080/api/hardware-monitor.html
  - 预期：
    - 页面显示"已连接"
    - 看到所有房间的卡片

- [ ] **观察数据刷新**
  - 等待 5 秒
  - 预期：房间卡片的温度、延迟数据自动更新

- [ ] **观察颜色变化**
  - 预期：
    - 绿色卡片：温度 < 85°C
    - 黄色卡片：温度 85-95°C
    - 红色卡片：温度 >= 95°C（闪烁动画）

### 4.2 报警通知测试
- [ ] **触发报警并观察推送**
  1. 打开监控大屏
  2. 在 Swagger UI 执行：`POST /api/hardware/trigger-failure/1`
  3. 预期：
     - 监控大屏右侧弹出报警通知
     - 房间1的卡片变为红色并闪烁

### 4.3 自动报警测试（可选）
- [ ] **等待自动故障触发**
  - 持续观察监控大屏 2-3 分钟
  - 预期：由于配置了 5% 故障率，偶尔会自动触发报警

---

## Module 5: 数据归档

### 5.1 设备日志归档
- [ ] **等待60秒后查询日志**
  - 数据库查询：`SELECT COUNT(*) FROM tb_device_log`
  - 预期：每分钟增加 N 条记录（N = 房间数量）

- [ ] **验证日志内容**
  - 数据库查询：`SELECT * FROM tb_device_log ORDER BY log_time DESC LIMIT 10`
  - 预期：包含 cpu_temp, gpu_temp, network_latency

---

## 性能与稳定性测试

### 并发测试
- [ ] **多客户端同时连接 WebSocket**
  - 打开 3 个浏览器标签页，都访问监控大屏
  - 预期：所有页面都能正常接收数据

### 长时间运行测试
- [ ] **后端持续运行 30 分钟**
  - 观察后端日志
  - 预期：
    - 无内存泄漏
    - 定时任务正常执行
    - 数据库连接池正常

---

## 异常处理测试

### 数据库异常
- [ ] **停止 MySQL 服务**
  - 预期：后端返回 500 错误，不会崩溃

### Redis 异常
- [ ] **停止 Redis 服务**
  - 尝试发送验证码
  - 预期：返回连接错误提示

### 权限异常
- [ ] **使用过期 Token**
  - 预期：返回 401 未授权

- [ ] **访问不存在的房间**
  - 接口：`POST /api/hardware/trigger-failure/999`
  - 预期：不会触发错误（系统自动忽略）

---

## 测试结果汇总

| 模块 | 测试项 | 通过 | 失败 | 备注 |
|------|--------|------|------|------|
| 认证系统 | 用户注册 | ☐ | ☐ | |
| 认证系统 | 用户登录 | ☐ | ☐ | |
| PMS | 查询房间 | ☐ | ☐ | |
| PMS | 办理入住 | ☐ | ☐ | |
| PMS | 二次鉴权 | ☐ | ☐ | |
| PMS | 办理退房 | ☐ | ☐ | |
| 硬件监控 | 查询状态 | ☐ | ☐ | |
| 硬件监控 | 触发故障 | ☐ | ☐ | |
| 硬件监控 | 报警管理 | ☐ | ☐ | |
| 硬件监控 | 工单管理 | ☐ | ☐ | |
| WebSocket | 实时推送 | ☐ | ☐ | |
| WebSocket | 报警通知 | ☐ | ☐ | |

---

## 测试完成后

- [ ] 整理发现的问题清单
- [ ] 验证所有数据库表结构是否正确
- [ ] 检查日志文件是否有异常
- [ ] 准备进入下一阶段开发（前端）

---

**测试日期：** ________________  
**测试人员：** ________________  
**测试环境：** Windows / macOS / Linux  
**测试结果：** 通过 / 部分通过 / 未通过
