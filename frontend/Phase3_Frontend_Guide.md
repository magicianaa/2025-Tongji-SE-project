# Phase 3: 社交匹配子系统 - 前端开发文档

## 📋 功能概述

Phase 3为智慧电竞酒店管理系统添加了完整的社交匹配功能，帮助住客找到合适的游戏队友。

## 🎯 已实现的功能

### 1. 游戏档案管理 (FR-SOC-01)
**路由**: `/guest/gaming-profile`  
**组件**: `frontend/src/views/guest/GamingProfile.vue`

**功能特性**:
- ✅ 创建/编辑游戏档案（游戏类型、账号、段位、位置、风格）
- ✅ 查看当前游戏档案
- ✅ 删除游戏档案
- ✅ 智能推荐队友（基于段位和位置匹配）
- ✅ 设置组队状态（寻找队友中/暂不组队）

**支持的游戏**:
- 英雄联盟 (LOL)
- DOTA2
- CS:GO
- 王者荣耀 (WZRY)
- 和平精英 (HPJY)
- 永劫无间 (YJWJ)
- 绝地求生 (PUBG)
- 守望先锋 (OW)

### 2. 在线玩家大厅 (FR-SOC-02)
**路由**: `/guest/online-lobby`  
**组件**: `frontend/src/views/guest/OnlineLobby.vue`

**功能特性**:
- ✅ 浏览所有在线玩家档案
- ✅ 按游戏类型筛选
- ✅ 按段位筛选
- ✅ 查看玩家详细信息（房间号、位置、风格）
- ✅ 发送组队邀请

### 3. 组队招募 (FR-SOC-03)
**路由**: `/guest/recruitment`  
**组件**: `frontend/src/views/guest/Recruitment.vue`

**功能特性**:
- ✅ 发布招募信息（指定游戏、段位、位置、人数）
- ✅ 浏览招募广场（分页展示）
- ✅ 申请加入招募
- ✅ 管理我的招募（关闭、删除）
- ✅ 招募状态管理（开放中/已满/已关闭）

### 4. 战队管理 (FR-SOC-04)
**路由**: `/guest/team-management`  
**组件**: `frontend/src/views/guest/TeamManagement.vue`

**功能特性**:
- ✅ 创建战队（队长自动加入）
- ✅ 查看战队详情和成员列表
- ✅ 成员操作（加入、离开、踢出）
- ✅ 解散战队（队长权限）
- ✅ 更新战队游戏时长（用于奖励计算）
- ✅ 战队状态管理（活跃/已解散）

## 🔗 API接口

### API封装文件

#### `frontend/src/api/gaming.js`
游戏档案和匹配推荐相关API：
```javascript
- createOrUpdateProfile(data)      // 创建/更新档案
- getProfile(profileId)             // 获取档案详情
- getCurrentProfile(gameType)       // 获取当前档案
- deleteProfile(profileId)          // 删除档案
- getOnlinePlayers(params)          // 获取在线玩家
- recommendTeammates(gameType, limit) // 推荐队友
```

#### `frontend/src/api/team.js`
招募和战队管理相关API：
```javascript
// 招募管理
- publishRecruitment(data)          // 发布招募
- searchRecruitments(params)        // 搜索招募
- getRecruitment(recruitmentId)     // 获取招募详情
- closeRecruitment(recruitmentId)   // 关闭招募
- deleteRecruitment(recruitmentId)  // 删除招募
- getMyRecruitments()               // 获取我的招募
- applyToRecruitment(recruitmentId) // 申请加入

// 战队管理
- createTeam(data)                  // 创建战队
- joinTeam(teamId)                  // 加入战队
- leaveTeam(teamId)                 // 离开战队
- kickMember(teamId, memberId)      // 踢出成员
- disbandTeam(teamId)               // 解散战队
- getTeam(teamId)                   // 获取战队详情
- getMyTeam()                       // 获取我的战队
- updatePlaytime(teamId, minutes)   // 更新游戏时长
```

## 🎨 页面访问路径

| 功能 | 路由路径 | 说明 |
|------|---------|------|
| 游戏档案 | `/guest/gaming-profile` | 创建和管理游戏档案 |
| 在线大厅 | `/guest/online-lobby` | 浏览在线玩家 |
| 组队招募 | `/guest/recruitment` | 发布和响应招募 |
| 战队管理 | `/guest/team-management` | 创建和管理战队 |

**入口**: 住客首页 → 社交匹配卡片 → 在线玩家大厅

## 🔐 权限说明

所有Phase 3功能仅对**已入住的GUEST用户**开放，需要满足：
1. 用户类型为GUEST
2. 有有效的入住记录
3. 客房权限未被回收 (is_gaming_auth_active = true)

后端通过`@RoomAuthRequired`注解进行二次鉴权。

## 🎮 游戏数据配置

### 段位系统
- **英雄联盟**: 黑铁 → 青铜 → 白银 → 黄金 → 铂金 → 钻石 → 大师 → 宗师 → 王者
- **DOTA2**: 先锋 → 卫士 → 十字军 → 传奇 → 万古流芳 → 超凡入圣 → 冠绝一世
- **其他游戏**: 青铜 → 白银 → 黄金 → 铂金 → 钻石

### 位置系统
- **英雄联盟**: 上单、打野、中单、ADC、辅助
- **DOTA2**: 一号位、二号位、三号位、四号位、五号位
- **其他游戏**: 前锋、中场、后卫、自由人

### 游戏风格
- 激进进攻 (AGGRESSIVE)
- 稳健防守 (DEFENSIVE)
- 战术指挥 (STRATEGIC)
- 辅助支援 (SUPPORTIVE)
- 全能型 (BALANCED)

## 📦 技术栈

- **框架**: Vue 3 (Composition API)
- **UI库**: Element Plus
- **路由**: Vue Router 4
- **状态管理**: Pinia
- **HTTP客户端**: Axios
- **构建工具**: Vite

## 🚀 部署说明

### 开发环境
```bash
cd frontend
npm install
npm run dev
```

### 生产构建
```bash
npm run build
```

### Docker部署
```bash
# 构建并启动所有服务
docker-compose up -d

# 仅重建前端
docker-compose up -d --build frontend
```

前端服务运行在: `http://localhost:8081`

## 📝 待优化项

1. **实时通信**: 添加WebSocket支持队友邀请通知
2. **语音功能**: 集成语音通话API
3. **战队聊天室**: 添加战队内部聊天功能
4. **数据统计**: 展示个人/战队游戏统计数据
5. **排行榜**: 战队排行榜和个人排行榜
6. **成就系统**: 游戏成就和徽章展示

## 🐛 已知问题

无

## 📄 相关文档

- [后端API文档](../Phase3_API_Test_Document.md)
- [需求验证文档](../Phase3_Requirements_Verification.md)
- [Backend README](../backend/README.md)

---

**开发完成时间**: 2025-12-19  
**开发者**: GitHub Copilot  
**版本**: 1.0.0
