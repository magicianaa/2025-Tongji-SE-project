# WebSocket实时招募系统测试指南

## 功能概述

实现了基于WebSocket的实时招募通知系统，当申请者申请加入招募时，发布者会实时收到弹窗通知，可以立即同意或拒绝申请。

## 技术实现

### 后端
- **WebSocket配置**: `WebSocketConfig.java`
  - 端点: `/ws`
  - 消息代理: `/topic`, `/queue`
  - 应用前缀: `/app`
  - 用户前缀: `/user`

- **Service层**: `RecruitmentService.java`
  - `applyToRecruitment()`: 发送申请，通过WebSocket推送给发布者
  - `approveApplication()`: 同意申请，直接创建/加入战队
  - `rejectApplication()`: 拒绝申请，仅通知申请者

- **Controller层**: `RecruitmentController.java`
  - `POST /{recruitmentId}/apply`: 申请加入招募
  - `POST /{recruitmentId}/approve?applicantId=`: 同意申请
  - `POST /{recruitmentId}/reject?applicantId=`: 拒绝申请

### 前端
- **WebSocket管理器**: `src/utils/websocket.js`
  - 自动重连机制（最多5次）
  - 订阅用户消息队列: `/user/{guestId}/queue/recruitment`
  - 通过自定义事件分发通知

- **OnlineLobby组件**: `src/views/guest/OnlineLobby.vue`
  - 启动时自动连接WebSocket
  - 监听 `recruitment-notification` 事件
  - 处理两种通知类型：
    - `NEW_APPLICATION`: 新申请通知（发布者）
    - `APPLICATION_RESULT`: 申请结果通知（申请者）

## 测试步骤

### 准备工作

1. **启动服务**
```bash
cd d:\practiecCode\java\HotelManagementSystem\2025-Tongji-SE-project
docker-compose up -d
```

2. **确认服务运行**
- 前端: http://localhost:8081
- 后端: http://localhost:8080
- 数据库: localhost:3306

### 测试场景1：实时申请通知

**步骤：**

1. **用户A（发布者）**
   - 登录系统
   - 导航到"组队招募大厅"
   - 点击"发布招募"按钮
   - 填写招募信息：
     - 游戏类型：王者荣耀
     - 段位要求：铂金
     - 描述：快来打排位
     - 需要人数：5
   - 点击"发布"
   - 等待在招募大厅页面（保持在线）

2. **用户B（申请者）**
   - 另一个浏览器/隐私窗口登录
   - 导航到"组队招募大厅"
   - 找到用户A发布的招募
   - 点击"申请加入"按钮
   - 确认申请

3. **预期结果（用户A）**
   - ✅ 立即收到浏览器弹窗通知
   - 弹窗内容：`{申请者姓名}（房间{房间号}）申请加入您的 王者荣耀 招募`
   - 弹窗按钮：[同意] [拒绝]

4. **预期结果（用户B）**
   - ✅ 显示"申请已发送！等待队长审核"提示

### 测试场景2：同意申请

**步骤：**

1. 继续场景1
2. 用户A点击弹窗中的"同意"按钮

**预期结果：**

- **用户A:**
  - ✅ 显示"已同意申请，队员已加入战队"
  - ✅ 招募列表自动刷新

- **用户B:**
  - ✅ 收到右上角成功通知："您的申请已被同意，已加入战队！"
  - ✅ 1秒后弹出确认框："是否立即查看我的战队？"
  - ✅ 点击"查看战队"跳转到战队管理页面

- **数据库验证:**
```sql
-- 查询tb_team_member表，应该有新记录
SELECT * FROM tb_team_member 
WHERE guest_id = {申请者ID} 
AND status = 'ACTIVE'
ORDER BY join_time DESC LIMIT 1;

-- 查询tb_team表，验证战队信息
SELECT * FROM tb_team 
WHERE team_id = (
  SELECT team_id FROM tb_team_member 
  WHERE guest_id = {申请者ID} 
  AND status = 'ACTIVE'
  LIMIT 1
);
```

### 测试场景3：拒绝申请

**步骤：**

1. 重复场景1中用户B申请的步骤
2. 用户A点击弹窗中的"拒绝"按钮

**预期结果：**

- **用户A:**
  - ✅ 显示"已拒绝申请"提示

- **用户B:**
  - ✅ 收到右上角警告通知："很遗憾，您的申请未被通过"

- **数据库验证:**
```sql
-- 不应该有任何相关记录（因为直接拒绝，不保存）
SELECT COUNT(*) FROM tb_team_member 
WHERE guest_id = {申请者ID} 
AND join_time > NOW() - INTERVAL 1 MINUTE;
-- 结果应为 0
```

### 测试场景4：重复申请

**步骤：**

1. 用户B对同一个招募再次点击"申请加入"

**预期结果：**

- ✅ 用户A再次收到实时弹窗
- ✅ 可以再次同意或拒绝
- （前端可以添加防抖优化，避免短时间内重复申请）

### 测试场景5：离线场景

**步骤：**

1. 用户A发布招募后关闭浏览器或离开页面
2. 用户B申请加入
3. 用户A重新打开页面

**预期结果：**

- ❌ 用户A不会收到通知（实时通知已丢失）
- ✅ 用户B显示"申请已发送"
- 这是设计行为：实时通知系统，发布者必须在线才能收到

### 测试场景6：多个申请者

**步骤：**

1. 用户A发布招募
2. 用户B、C、D同时申请

**预期结果：**

- ✅ 用户A依次收到3个弹窗通知
- ✅ 每个弹窗独立处理
- ✅ 用户A可以选择性同意部分申请者

## 浏览器控制台日志

### 成功连接WebSocket
```
正在连接WebSocket，用户ID: 123
WebSocket Connected: ...
Subscribed to /user/123/queue/recruitment
WebSocket连接成功
```

### 收到新申请通知
```
Received notification: {
  type: "NEW_APPLICATION",
  recruitmentId: 1,
  applicantId: 456,
  applicantName: "张三",
  applicantRoom: "201",
  gameType: "王者荣耀",
  message: "张三 申请加入您的招募",
  timestamp: "2025-12-20 15:30:00"
}
收到招募通知: {...}
```

### 收到申请结果通知
```
Received notification: {
  type: "APPLICATION_RESULT",
  recruitmentId: 1,
  approved: true,
  message: "您的申请已被同意，已加入战队！",
  gameType: "王者荣耀",
  timestamp: "2025-12-20 15:30:05"
}
收到招募通知: {...}
```

## 后端日志验证

### 申请请求
```
Guest [张三] 申请加入招募 [1]，已通过WebSocket推送
```

### 同意申请
```
Guest [456] 已加入战队 [10]
```

## 常见问题排查

### 1. WebSocket连接失败

**问题：** 控制台显示 `WebSocket Connection Error`

**检查：**
- 后端是否启动: `docker ps | grep backend`
- WebSocket端点是否可访问: 浏览器访问 `http://localhost:8080/ws`
- CORS配置是否正确: 检查 `WebSocketConfig.java` 中的 `setAllowedOrigins`

**解决：**
```java
// 确保WebSocketConfig.java中允许前端域名
registry.addEndpoint("/ws")
        .setAllowedOrigins("http://localhost:8081")
        .withSockJS();
```

### 2. 收不到通知

**问题：** WebSocket已连接但收不到通知

**检查：**
- 用户ID是否正确: 控制台查看 `正在连接WebSocket，用户ID: ?`
- 订阅路径是否正确: 控制台查看 `Subscribed to /user/{guestId}/queue/recruitment`
- 后端是否正确发送: 检查后端日志 `Guest [xxx] 申请加入招募 [x]，已通过WebSocket推送`

**解决：**
```java
// 后端发送时使用convertAndSendToUser
messagingTemplate.convertAndSendToUser(
    publisherId.toString(),  // 必须是字符串
    "/queue/recruitment",     // 不需要/user前缀
    notification
);
```

### 3. 申请后没有加入战队

**问题：** 点击同意后用户没有加入战队

**检查：**
- 数据库 `tb_team_member` 表
- 后端日志中是否有错误
- 网络请求是否成功: 浏览器Network标签

**调试SQL：**
```sql
-- 检查是否有待处理的成员
SELECT * FROM tb_team_member 
WHERE guest_id = {申请者ID}
ORDER BY join_time DESC;

-- 检查发布者是否有战队
SELECT * FROM tb_team_member 
WHERE guest_id = {发布者ID}
AND status = 'ACTIVE';
```

## 性能考虑

- **连接数限制**: 每个在线用户一个WebSocket连接
- **消息频率**: 仅在申请时发送，不是持续推送
- **断线重连**: 最多尝试5次，每次间隔3秒
- **内存占用**: 订阅信息存储在Map中，断开连接时清理

## 安全考虑

- ✅ 使用 `@RoomAuthRequired` 验证用户身份
- ✅ 后端验证发布者和申请者关系
- ✅ 不允许自己申请自己的招募
- ✅ 验证招募状态（OPEN）
- ⚠️ 建议添加申请频率限制（防止恶意刷屏）

## 后续优化建议

1. **前端防抖**: 申请按钮添加防抖，避免短时间内重复点击
2. **申请历史**: 可选择保存申请记录供后续查询（需要恢复tb_recruitment_applications表）
3. **离线消息**: 集成消息持久化，发布者上线时显示未读通知
4. **批量操作**: 发布者可以批量同意/拒绝多个申请
5. **通知设置**: 用户可以选择关闭实时通知
6. **音效提醒**: 收到申请时播放提示音

## 测试清单

- [ ] WebSocket连接成功
- [ ] 申请者点击申请，发布者实时收到弹窗
- [ ] 发布者同意，申请者收到成功通知
- [ ] 发布者拒绝，申请者收到拒绝通知
- [ ] 同意后战队数据正确创建
- [ ] 多个申请者依次通知
- [ ] 页面刷新后重新连接WebSocket
- [ ] 断网后自动重连
- [ ] 浏览器控制台无错误日志
- [ ] 后端日志正常输出

## 测试完成标志

✅ 所有测试场景通过
✅ 数据库记录正确
✅ 无控制台错误
✅ 用户体验流畅
