# 招募系统简化 - 移除申请表

## 设计理念

由于使用WebSocket实现实时推送，申请者点击申请后，发布者会立即收到通知弹窗，可以实时同意或拒绝。因此不需要持久化申请记录到数据库。

## 新流程

```
申请者点击申请
    ↓
后端接收请求，通过WebSocket实时推送给发布者
    ↓
发布者立即看到弹窗（包含申请者信息：姓名、房间号、游戏类型）
    ↓
发布者点击"同意" → 后端直接创建/加入战队，并通过WebSocket通知申请者成功
发布者点击"拒绝" → 后端通过WebSocket通知申请者被拒绝（不保存任何记录）
```

## 技术变更

### 1. 后端服务层 (RecruitmentService.java)

#### 移除的内容
- ❌ `RecruitmentApplicationMapper` 依赖
- ❌ `getRecruitmentApplications()` 方法（获取申请列表）
- ❌ `sendApplicationNotification()` 私有方法
- ❌ `sendHandleResultNotification()` 私有方法
- ❌ `convertToApplicationResponse()` 转换方法
- ❌ 申请记录的数据库插入/更新/查询逻辑

#### 新增/修改的方法

**`applyToRecruitment(Long recruitmentId, Long guestId)`**
```java
// 功能：申请加入招募
// 实现：
// 1. 验证招募存在且状态为OPEN
// 2. 验证不是自己的招募
// 3. 获取申请者信息（姓名、房间号）
// 4. 构建RecruitmentNotification消息
// 5. 通过WebSocket推送给发布者：messagingTemplate.convertAndSendToUser(publisherId, "/queue/recruitment", notification)
```

**`approveApplication(Long recruitmentId, Long applicantId, Long captainId)`**
```java
// 功能：同意申请并创建/加入战队
// 实现：
// 1. 验证招募存在
// 2. 验证是发布者本人
// 3. 调用createOrJoinTeam()直接创建或加入战队
// 4. 通过WebSocket通知申请者成功
```

**`rejectApplication(Long recruitmentId, Long applicantId, Long captainId)`**
```java
// 功能：拒绝申请
// 实现：
// 1. 验证招募存在
// 2. 验证是发布者本人
// 3. 通过WebSocket通知申请者被拒绝
// 4. 不保存任何记录
```

**保留方法**
- ✅ `createOrJoinTeam()` - 自动创建战队或加入现有战队（逻辑不变）

### 2. 后端控制层 (RecruitmentController.java)

#### 移除的端点
- ❌ `GET /{recruitmentId}/applications` - 获取申请列表
- ❌ `PUT /applications/{applicationId}` - 处理申请（统一接口）

#### 新增端点

**`POST /{recruitmentId}/approve?applicantId={applicantId}`**
```java
// 功能：发布者同意申请
// 参数：recruitmentId（路径）、applicantId（查询参数）
// 权限：RoomAuthRequired
// 实现：调用approveApplication()
```

**`POST /{recruitmentId}/reject?applicantId={applicantId}`**
```java
// 功能：发布者拒绝申请
// 参数：recruitmentId（路径）、applicantId（查询参数）
// 权限：RoomAuthRequired
// 实现：调用rejectApplication()
```

**保留端点**
- ✅ `POST /{recruitmentId}/apply` - 申请加入招募（逻辑修改为实时推送）

### 3. DTO层 (RecruitmentNotification.java)

#### 字段变更

**移除字段**
- ❌ `applicationId` - 不再需要申请ID
- ❌ `teamId` - 不在通知中返回战队ID

**新增字段**
- ✅ `approved` - Boolean类型，表示申请是否被同意（仅申请结果通知使用）

**保留字段**
- ✅ `type` - 通知类型：`NEW_APPLICATION`（新申请）、`APPLICATION_RESULT`（申请结果）
- ✅ `recruitmentId` - 招募ID
- ✅ `applicantId` - 申请者ID
- ✅ `applicantName` - 申请者姓名
- ✅ `applicantRoom` - 申请者房间号
- ✅ `gameType` - 游戏类型
- ✅ `message` - 通知消息
- ✅ `timestamp` - 时间戳

### 4. 不存在的文件（确认已移除）

以下文件在项目中不存在（如果存在需要删除）：
- `backend/.../entity/RecruitmentApplication.java`
- `backend/.../mapper/RecruitmentApplicationMapper.java`
- `backend/.../mapper/RecruitmentApplicationMapper.xml`
- `backend/.../dto/RecruitmentApplicationResponse.java`
- `database/create_recruitment_applications.sql`

## WebSocket配置

### 端点配置 (WebSocketConfig.java)

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:8081")
                .withSockJS();
    }
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }
}
```

### 消息发送方式

**发送给发布者（新申请通知）**
```java
messagingTemplate.convertAndSendToUser(
    publisherId.toString(),     // 用户ID
    "/queue/recruitment",        // 目标路径
    notification                 // 消息内容
);
```

**发送给申请者（申请结果通知）**
```java
messagingTemplate.convertAndSendToUser(
    applicantId.toString(),     // 用户ID
    "/queue/recruitment",        // 目标路径
    notification                 // 消息内容
);
```

## 前端实现（待完成）

### 需要添加的内容

1. **安装依赖**
```bash
npm install sockjs-client @stomp/stompjs
```

2. **创建WebSocket连接**
```javascript
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
  // 订阅个人消息队列
  const guestId = userStore.user.guestId;
  stompClient.subscribe(`/user/${guestId}/queue/recruitment`, (message) => {
    const notification = JSON.parse(message.body);
    handleNotification(notification);
  });
});
```

3. **处理通知**
```javascript
function handleNotification(notification) {
  if (notification.type === 'NEW_APPLICATION') {
    // 显示弹窗：XX申请加入您的招募
    ElMessageBox.confirm(
      `${notification.applicantName}（房间${notification.applicantRoom}）申请加入您的${notification.gameType}招募`,
      '新的申请',
      {
        confirmButtonText: '同意',
        cancelButtonText: '拒绝',
        type: 'info'
      }
    ).then(() => {
      // 调用同意接口
      approveApplication(notification.recruitmentId, notification.applicantId);
    }).catch(() => {
      // 调用拒绝接口
      rejectApplication(notification.recruitmentId, notification.applicantId);
    });
  } else if (notification.type === 'APPLICATION_RESULT') {
    // 显示申请结果
    if (notification.approved) {
      ElNotification.success({
        title: '申请成功',
        message: notification.message
      });
    } else {
      ElNotification.warning({
        title: '申请被拒绝',
        message: notification.message
      });
    }
  }
}
```

4. **API调用**
```javascript
// 同意申请
async function approveApplication(recruitmentId, applicantId) {
  await axios.post(`/recruitments/${recruitmentId}/approve`, null, {
    params: { applicantId }
  });
  ElMessage.success('已同意申请');
}

// 拒绝申请
async function rejectApplication(recruitmentId, applicantId) {
  await axios.post(`/recruitments/${recruitmentId}/reject`, null, {
    params: { applicantId }
  });
  ElMessage.info('已拒绝申请');
}
```

## 优势

1. **简化架构** - 移除了整个申请表相关的entity、mapper、DTO、endpoint
2. **实时响应** - 发布者立即收到通知，申请者立即收到结果
3. **减少存储** - 不保存临时申请记录，减少数据库冗余
4. **代码更清晰** - 流程直接明了，减少了状态管理的复杂度

## 注意事项

1. **发布者必须在线** - 如果发布者离线，申请通知会丢失（但这符合"实时"的设计理念）
2. **无申请历史** - 不保存历史申请记录，拒绝的申请不会留痕
3. **幂等性** - 申请者可以重复点击申请（前端可以添加防抖处理）

## 测试建议

1. 用户A发布招募
2. 用户B申请加入
3. 验证A收到实时弹窗
4. A点击同意 → 验证B收到成功通知 → 验证team_member表有新记录
5. 重复2-3步，A点击拒绝 → 验证B收到拒绝通知 → 验证无数据库记录

## 编译状态

✅ RecruitmentService.java - 无错误
✅ RecruitmentController.java - 无错误
✅ RecruitmentNotification.java - 已更新
✅ 无编译错误
