# WebSocket 实时招募通知测试指南

## 测试场景说明

### 角色分配
- **用户A（发布者/队长）**: 发布招募信息的用户，会收到申请通知
- **用户B（申请者）**: 申请加入队伍的用户，会收到审核结果通知

### 测试步骤

#### 1. 准备两个浏览器会话
- **浏览器1**: 登录用户A账号（例如：userId=9, guestId=5, 用户名xl）
- **浏览器2**: 登录用户B账号（例如：userId=8, guestId=4, 用户名lzh）

#### 2. 用户A发布招募
1. 打开浏览器1，进入"组队招募大厅"
2. 点击"发布招募"按钮
3. 填写招募信息：
   - 游戏类型：选择一个游戏（如：英雄联盟）
   - 段位要求：可选
   - 位置要求：可选
   - 招募描述：输入描述
   - 需要人数：设置人数
4. 点击"发布"按钮
5. 确认控制台显示：`WebSocket连接成功` 和 `实时通知已启用`

#### 3. 用户B申请加入
1. 打开浏览器2，进入"组队招募大厅"
2. 确认控制台显示：`WebSocket连接成功` 和 `实时通知已启用`
3. 找到用户A发布的招募信息
4. 点击"申请加入"按钮
5. 在确认对话框中点击"确定"

#### 4. 期望结果
**用户A（浏览器1）应该立即收到：**
- 右下角弹出确认对话框
- 标题：`新的申请`
- 内容：`用户B姓名（房间号）申请加入您的 游戏类型 招募`
- 两个按钮：`同意` 和 `拒绝`

**用户B（浏览器2）应该：**
- 看到提示：`申请已发送！等待队长审核`

#### 5. 用户A审核申请
**场景1：同意申请**
1. 用户A点击"同意"按钮
2. 用户A看到提示：`已同意申请，队员已加入战队`
3. 用户B立即收到通知：`申请成功 - 您的申请已被同意，已加入战队！`

**场景2：拒绝申请**
1. 用户A点击"拒绝"按钮
2. 用户A看到提示：`已拒绝申请`
3. 用户B立即收到通知：`申请被拒绝 - 很遗憾，您的申请未被通过`

## WebSocket 连接验证

### 前端控制台应该显示
```javascript
正在连接WebSocket，用户ID: <guestId>
WebSocket连接成功
实时通知已启用
Subscribed to /user/<guestId>/queue/recruitment
```

### 后端日志应该显示
```
WebSocket握手成功，用户ID: <guestId>
WebSocket STOMP连接，用户ID: <guestId>
```

### 申请发送时后端日志
```
=== WebSocket推送详情 ===
招募ID: <recruitmentId>
发布者ID (publisherId): <发布者guestId>
申请者ID (guestId): <申请者guestId>
申请者姓名: <申请者真实姓名>
目标用户: <发布者guestId>
目标路径: /user/<发布者guestId>/queue/recruitment
通知类型: NEW_APPLICATION
========================
Guest [申请者姓名] 申请加入招募 [recruitmentId]，已通过WebSocket推送
```

## 常见问题排查

### 问题1：没有收到WebSocket通知
**检查项：**
1. 确认两个浏览器登录的是不同用户
2. 确认发布者的浏览器保持打开且在招募页面
3. 查看前端控制台是否有 `WebSocket连接成功` 消息
4. 查看后端日志中的 `目标用户` 是否与发布者的guestId一致

### 问题2：WebSocket连接失败
**检查项：**
1. 确认后端服务正常运行：`docker-compose ps`
2. 确认WebSocket端点可访问：http://localhost:8080/api/ws/info
3. 查看浏览器控制台的错误信息
4. 查看后端日志是否有异常

### 问题3：用户ID混淆
**注意：**
- **userId**: 用户表的主键（如：8, 9）
- **guestId**: 住客表的主键（如：4, 5）
- WebSocket使用的是 **guestId** 而不是 userId
- 确认你登录的账号对应的guestId

## 测试账号示例

### 账号A（发布者）
- 手机号：13900139000
- 密码：mmmm8888
- userId: 9
- guestId: 5
- 姓名：xl
- 房间：204

### 账号B（申请者）
- 手机号：13800138000
- 密码：mmmm8888
- userId: 8
- guestId: 4
- 姓名：lzh
- 房间：201

## 调试技巧

1. **查看后端日志**：
   ```bash
   docker-compose logs -f backend | grep -E "WebSocket|申请加入|推送详情"
   ```

2. **查看前端Network**：
   - 打开浏览器开发者工具 → Network → WS（WebSocket）
   - 查看WebSocket连接状态和消息

3. **查看前端Console**：
   - 所有WebSocket相关日志都会输出到控制台
   - 包括连接状态、订阅信息、收到的通知等

4. **测试API端点**：
   ```bash
   # 测试SockJS info端点
   curl http://localhost:8080/api/ws/info
   ```
