# 智慧电竞酒店管理系统 - 项目总览

## 📋 项目概述

**项目名称：** Smart Esports Hotel Management System（智慧电竞酒店管理系统）  
**项目类型：** B/S架构全栈Web应用  
**开发阶段：** Step 4 已完成（前端完整实现）  
**完成进度：** 75% （核心功能已完成，POS/社交/报表待开发）  
**最后更新：** 2025-12-15

---

## 🎯 核心创新点

### 1. 二次鉴权机制（关键亮点）
- **普通登录**：用户获得基础浏览权限（JWT Token）
- **客房权限**：只有当用户有"有效入住记录"时才能访问客房服务
- **动态绑定**：权限与房间号、入住记录ID绑定
- **自动回收**：退房时立即销毁客房权限，防止越权

**技术实现：**
```java
@RoomAuthRequired  // 标注需要客房权限的接口
public Result<String> testRoomAuth() { ... }

// 拦截器自动验证：
// actual_checkin <= NOW <= expected_checkout 
// AND is_gaming_auth_active = true
```

### 2. 硬件模拟器（纯软件实现）
- **正态分布生成数据**：CPU/GPU温度符合真实设备特征
- **故障模拟**：可配置概率生成过热场景（用于演示）
- **智能报警**：连续3次异常才触发（防止误报）
- **自动工单**：报警后自动生成维修工单并更新房态

### 3. 实时监控 + WebSocket
- **5秒刷新**：定时任务每5秒生成一次数据
- **实时推送**：通过WebSocket广播到所有在线管理端
- **三色预警**：GREEN（正常）/YELLOW（预警）/RED（严重）
- **可视化大屏**：HTML5原生实现，无需额外框架

---

## 🏗️ 技术架构

### 后端技术栈
| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 21 | 核心语言 |
| Spring Boot | 3.2.0 | 应用框架 |
| MyBatis-Plus | 3.5.5 | ORM持久化 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 6.0+ | 缓存 + 会话 |
| JWT | 0.12.3 | Token认证 |
| WebSocket | - | 实时通信 |
| SpringDoc | 2.3.0 | API文档 |

### 前端技术栈
| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | 3.4.0 | 前端框架（Composition API） |
| Vite | 5.0.0 | 构建工具 |
| Element Plus | 2.5.0 | UI组件库 |
| Vue Router | 4.2.5 | 路由管理 |
| Pinia | 2.1.7 | 状态管理 |
| Axios | 1.6.2 | HTTP客户端 |
| SockJS | 1.6.1 | WebSocket客户端 |
| STOMP | 2.3.3 | 消息协议 |

---

## 📂 项目结构

```
java_project2/
├── database/
│   └── schema.sql                          # 数据库建表脚本
├── backend/                                # Spring Boot 后端
│   ├── src/main/java/com/esports/hotel/
│   │   ├── annotation/                     # 自定义注解
│   │   │   └── RoomAuthRequired.java      # 二次鉴权注解
│   │   ├── common/                         # 通用类
│   │   │   ├── Result.java                # 统一响应
│   │   │   ├── ResultCode.java            # 响应码
│   │   │   └── BusinessException.java     # 业务异常
│   │   ├── config/                         # 配置类
│   │   │   ├── WebConfig.java             # Web配置（拦截器）
│   │   │   ├── WebSocketConfig.java       # WebSocket配置
│   │   │   ├── JwtProperties.java         # JWT配置
│   │   │   └── HardwareSimulatorProperties.java
│   │   ├── controller/                     # 控制器
│   │   │   ├── AuthController.java        # 认证接口
│   │   │   ├── RoomController.java        # 客房管理
│   │   │   └── HardwareController.java    # 硬件监控
│   │   ├── dto/                            # 数据传输对象
│   │   ├── entity/                         # 实体类
│   │   │   ├── User.java
│   │   │   ├── Guest.java
│   │   │   ├── Room.java
│   │   │   ├── CheckInRecord.java         # 二次鉴权核心表
│   │   │   ├── HardwareStatus.java
│   │   │   ├── MaintenanceTicket.java
│   │   │   └── AlertLog.java
│   │   ├── interceptor/
│   │   │   └── RoomAuthInterceptor.java   # 🔑 二次鉴权拦截器
│   │   ├── mapper/                         # MyBatis接口
│   │   ├── service/
│   │   │   ├── AuthService.java
│   │   │   ├── RoomService.java
│   │   │   └── HardwareSimulationService.java  # 🖥️ 硬件模拟核心
│   │   ├── util/
│   │   │   └── JwtUtil.java
│   │   └── EsportsHotelApplication.java   # 启动类
│   ├── src/main/resources/
│   │   ├── application.yml                 # 配置文件
│   │   └── static/
│   │       └── hardware-monitor.html       # 监控大屏测试页面
│   ├── pom.xml
│   └── README.md
├── frontend/                                # Vue 3 前端
│   ├── src/
│   │   ├── api/                            # API接口封装
│   │   │   ├── auth.js                    # 认证接口
│   │   │   ├── room.js                    # 房间管理接口
│   │   │   └── hardware.js                # 硬件监控接口
│   │   ├── components/                    # 公共组件
│   │   ├── router/
│   │   │   └── index.js                   # 路由配置 + 守卫
│   │   ├── stores/
│   │   │   └── user.js                    # 用户状态管理（Pinia）
│   │   ├── utils/
│   │   │   └── request.js                 # Axios封装
│   │   ├── views/
│   │   │   ├── Login.vue                  # 登录/注册页
│   │   │   ├── Dashboard.vue              # 控制台首页
│   │   │   ├── RoomManagement.vue         # 房态管理看板
│   │   │   ├── HardwareMonitor.vue        # 硬件监控大屏
│   │   │   └── NotFound.vue               # 404页面
│   │   ├── App.vue
│   │   └── main.js
│   ├── index.html
│   ├── package.json
│   ├── vite.config.js
│   ├── FRONTEND_GUIDE.md                  # 前端详细指南
│   └── README.md
├── QUICKSTART.md                           # 快速启动指南
├── TESTING.md                              # 测试清单
├── STEP4_SUMMARY.md                        # Step 4完成总结
└── README.md                               # 项目总览
```

---

## ✅ 已完成功能（Step 1-4）

### Module 1: 认证与权限
- [x] 用户注册（手机号 + 短信验证码）
- [x] 用户登录（JWT Token）
- [x] 短信验证码发送（Redis + 模拟实现）
- [x] 二次鉴权拦截器（`@RoomAuthRequired`）
- [x] Room-Auth-Token 生成与验证

### Module 2: 客房管理（PMS）
- [x] 房态管理（VACANT/OCCUPIED/DIRTY/MAINTENANCE）
- [x] 查询所有房间
- [x] 查询空闲房间
- [x] 办理入住（分配房间 + 生成权限Token）
- [x] 办理退房（计算房费 + 回收权限）
- [x] 房费自动计算（按小时计费）
- [x] 积分自动赠送（消费1元=10积分）

### Module 3: 硬件监控系统
- [x] 硬件模拟器（正态分布数据生成）
- [x] 实时状态表（`tb_hardware_status`）
- [x] 设备日志归档（`tb_device_log`）
- [x] 三色健康等级判断（GREEN/YELLOW/RED）
- [x] 自动报警机制（连续3次异常触发）
- [x] 维修工单自动生成
- [x] WebSocket 实时推送
- [x] 报警通知推送
- [x] 工单管理接口
- [x] 硬件监控大屏（HTML5测试页面）

### Module 4: 前端系统（Vue 3）
- [x] Vue 3 + Vite 项目初始化
- [x] Element Plus UI组件库集成
- [x] Axios 请求封装（拦截器）
- [x] Pinia 状态管理（用户、Token）
- [x] Vue Router 路由配置
- [x] 路由守卫（登录验证）
- [x] 登录/注册页面
  - [x] 手机号 + 密码登录
  - [x] 手机号 + 验证码注册
  - [x] 表单验证（手机号、身份证号）
  - [x] 验证码倒计时
- [x] 控制台首页
  - [x] 房间状态统计卡片
  - [x] 侧边栏导航菜单
  - [x] 快速导航按钮
- [x] 房态管理看板
  - [x] 房间状态筛选
  - [x] 房间卡片展示
  - [x] 办理入住对话框
  - [x] 办理退房功能
  - [x] 房间详情查看
- [x] 硬件监控大屏
  - [x] WebSocket 实时连接
  - [x] 连接状态指示
  - [x] 实时硬件数据展示
  - [x] 健康状态分级显示
  - [x] 告警实时弹窗提示
  - [x] 告警记录管理
  - [x] 手动触发故障测试
  - [x] 断线自动重连

### 基础设施
- [x] 全局异常处理
- [x] 统一响应封装
- [x] MyBatis-Plus 自动填充
- [x] Swagger API 文档
- [x] 跨域配置（CORS）
- [x] WebSocket 配置（STOMP）
- [x] 前后端分离架构
- [x] API代理配置（Vite）

---

## 🔄 待开发功能（Step 5+）

### Step 5: 增值业务
- [ ] POS 销售点系统（商品管理、库存、挂账）
- [ ] 社交匹配系统（游戏档案、组队招募）
- [ ] 游戏化积分系统（任务、商城、会员等级）
- [ ] 评价管理系统（低分预警、回访闭环）

### Step 6: 数据分析
- [ ] 运营数据看板（入住率、RevPAR）
- [ ] 财务报表（日报、月报）
- [ ] 硬件损耗分析与采购预测

---

## 🚀 快速开始

### 1. 环境要求

#### 后端
- JDK 21+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+

#### 前端
- Node.js 18+
- npm 或 yarn
- Redis 6.0+

### 2. 启动步骤

#### 后端启动
```bash
# 1. 初始化数据库
mysql -u root -p < database/schema.sql

# 2. 启动 Redis
redis-server

# 3. 修改配置
# 编辑 backend/src/main/resources/application.yml

# 4. 启动后端
cd backend
mvn spring-boot:run
```

#### 前端启动
```bash
# 1. 安装依赖
cd frontend
npm install

# 2. 启动开发服务器
npm run dev
```

### 3. 访问地址
- **前端系统**：http://localhost:5173
- **后端 API**：http://localhost:8080/api
- **API 文档**：http://localhost:8080/api/swagger-ui.html
- **监控大屏测试页**：http://localhost:8080/api/hardware-monitor.html

**详细教程请查看：** 
- [QUICKSTART.md](./QUICKSTART.md) - 后端快速启动
- [frontend/FRONTEND_GUIDE.md](./frontend/FRONTEND_GUIDE.md) - 前端详细指南

---

## 📊 数据库设计

### 核心表结构
1. **tb_user** - 用户基础表
2. **tb_guest** - 住客扩展表（会员等级、积分）
3. **tb_room** - 房间信息表
4. **tb_checkin_record** - 入住记录表（⭐ 二次鉴权核心）
5. **tb_hardware_status** - 硬件实时状态表
6. **tb_device_log** - 设备监控日志表
7. **tb_alert_log** - 报警记录表
8. **tb_maintenance_ticket** - 维修工单表

**完整 DDL 请查看：** [database/schema.sql](./database/schema.sql)

---

## 🔌 API 接口文档

### 认证模块（/api/auth）
- `POST /sms/send` - 发送验证码
- `POST /register` - 用户注册
- `POST /login` - 用户登录

### 客房管理（/api/rooms）
- `GET /` - 查询所有房间
- `GET /vacant` - 查询空闲房间
- `POST /checkin` - 办理入住
- `POST /checkout/{recordId}` - 办理退房
- `GET /test-room-auth` 🔒 - 测试二次鉴权

### 硬件监控（/api/hardware）
- `GET /status` - 获取所有房间硬件状态
- `POST /trigger-failure/{roomId}` - 手动触发故障（测试）
- `GET /alerts/unhandled` - 获取未处理报警
- `PUT /alerts/{alertId}/handle` - 标记报警已处理
- `GET /tickets` - 获取维修工单
- `PUT /tickets/{ticketId}/status` - 更新工单状态

### WebSocket 订阅地址
- `/topic/hardware` - 硬件数据流（每5秒推送）
- `/topic/alerts` - 报警通知

---

## 🧪 测试场景

### 场景1：完整入住流程
1. 用户注册 → 登录 → 获得普通Token
2. 前台办理入住 → 获得 Room-Auth-Token
3. 用户访问客房服务（需携带Room-Auth-Token）
4. 退房 → 权限自动回收

### 场景2：硬件故障处理
1. 模拟器生成过热数据
2. 系统连续3次检测到异常
3. 自动触发报警 + 生成工单
4. WebSocket 推送报警通知到管理端
5. 前台更新房态为"维修中"

### 场景3：实时监控大屏
1. 打开监控页面
2. WebSocket自动连接
3. 每5秒接收所有房间数据
4. 房间卡片根据健康等级变色
5. 报警时右侧弹出通知

---

## 📝 开发规范

### 代码风格
- Java: 遵循阿里巴巴Java开发手册
- 前端: Vue 3 Composition API + TypeScript
- 接口: RESTful 风格
- 响应: 统一使用 `Result<T>` 封装

### Git 提交规范
```
feat: 新增功能
fix: 修复Bug
docs: 文档更新
refactor: 代码重构
test: 测试相关
chore: 构建/工具链
```

---

## 🤝 贡献指南

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'feat: Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

---

## 📄 许可证

本项目仅用于学习交流，未经许可请勿商用。

---

## 📧 联系方式

- 项目文档：查看各模块 README.md
- 问题反馈：提交 Issue
- 技术支持：查看 QUICKSTART.md 常见问题

---

**最后更新：** 2025-12-15  
**当前进度：** Step 3 已完成（后端核心 60%）  
**下一步：** Vue 3 前端开发
