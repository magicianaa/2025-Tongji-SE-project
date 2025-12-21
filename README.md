# 智慧电竞酒店管理系统

<div align="center">

**Smart Esports Hotel Management System**

一站式电竞酒店管理解决方案，融合住宿管理与电竞特色服务

[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-brightgreen)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4.0-42b883)](https://vuejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-Academic-yellow)](LICENSE)

</div>

---

## 📋 项目概述

**项目名称：** 智慧电竞酒店管理系统  
**项目类型：** B/S架构全栈Web应用  
**适用场景：** 20-50间客房规模的小型电竞酒店  
**核心创新：** 取代"酒店管理系统 + 网吧管理系统"双系统模式，实现住宿管理与电竞特色服务深度融合  
**开发阶段：** ✅ Phase 5 已完成（报表与决策支持子系统）  
**最后更新：** 2025-12-21  
**下一阶段：** 持续优化与功能增强

---

## 🎯 核心特性

### 1. 一体化系统架构
- **三端统一**：住客端、前台端、管理端共用一套后端系统
- **角色分离**：基于RBAC的权限管理，不同角色看到不同功能
- **数据打通**：房态、订单、账单、硬件状态实时同步

### 2. 二次鉴权机制（关键创新）
**问题背景**：传统酒店系统只验证"是否登录"，无法区分"是否有权使用房间设施"

**解决方案**：
- **普通登录**：获得JWT Token（基础浏览权限）
- **客房权限**：入住时生成Room-Auth-Token（房间操作权限）
- **动态绑定**：权限与房间号、入住记录绑定，支持多人共享房间
- **自动回收**：退房时立即销毁权限，防止越权访问

**技术实现**：
```java
@RoomAuthRequired  // 标注需要客房权限的接口
public Result<Hardware> getHardwareStatus() { ... }

// 验证逻辑：actual_checkin ≤ NOW ≤ expected_checkout 
// AND is_gaming_auth_active = true
```

### 3. POS销售点系统
- **商品管理**：支持零食、饮料、外设租赁三大类
- **挂账机制**：消费自动关联入住记录，退房统一结算
- **库存预警**：实时监控库存，低于阈值自动提醒
- **租赁计费**：支持按小时、按天、按次计费

### 4. 账单聚合系统
- **房间级账单**：同房间所有住客共享一个账单
- **自动计算**：房费（天数×单价）+ POS消费（实时累加）
- **支付传播**：任一住客支付后，全部住客待支付金额归零
- **退房校验**：前台办理退房时自动检查费用是否结清

### 5. 硬件模拟与监控
- **数据模拟**：正态分布生成CPU/GPU温度（符合真实特征）
- **智能报警**：连续3次异常才触发，避免误报
- **自动工单**：报警后自动生成维修工单并更新房态
- **实时推送**：WebSocket推送硬件数据和告警信息

---

## 🏗️ 技术架构

### 后端技术栈
| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 21 | 核心开发语言 |
| Spring Boot | 3.1.5 | 应用框架 |
| MyBatis-Plus | 3.5.9 | ORM持久化框架 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 8.0 (Alpine) | 缓存 + 会话管理 |
| JWT | 0.12.3 | 无状态Token认证 |
| WebSocket | STOMP | 实时双向通信 |
| Docker | 20.10+ | 容器化部署 |
| Lombok | 1.18.30 | 简化Java代码 |

### 前端技术栈
| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | 3.4.0 | 前端MVVM框架（Composition API） |
| Vite | 5.0.0 | 新一代构建工具 |
| Element Plus | 2.5.0 | 企业级UI组件库 |
| Vue Router | 4.2.5 | 单页应用路由 |
| Pinia | 2.1.7 | Vue 3官方状态管理 |
| Axios | 1.6.2 | Promise HTTP客户端 |
| Nginx | 1.27 (Alpine) | 前端静态资源服务器 |

### 部署架构
```
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│   Nginx     │─────▶│ Spring Boot │─────▶│   MySQL     │
│   (80)      │      │   (8080)    │      │   (3307)    │
└─────────────┘      └─────────────┘      └─────────────┘
                            │
                            ▼
                     ┌─────────────┐
                     │    Redis    │
                     │   (6379)    │
                     └─────────────┘
```

---

## 📂 项目结构

```
java_project2/
├── database/
│   ├── schema.sql                          # 数据库DDL（23张表）
│   └── migration_task1.3.sql              # 数据迁移脚本
├── backend/                                # Spring Boot 后端
│   ├── src/main/java/com/esports/hotel/
│   │   ├── annotation/                     # 自定义注解
│   │   │   └── RoomAuthRequired.java      # 🔑 二次鉴权注解
│   │   ├── common/                         # 通用封装
│   │   │   ├── Result.java                # 统一响应体
│   │   │   ├── ResultCode.java            # 响应码枚举
│   │   │   └── BusinessException.java     # 业务异常
│   │   ├── config/                         # 配置类
│   │   │   ├── WebConfig.java             # Web MVC配置
│   │   │   ├── WebSocketConfig.java       # WebSocket配置
│   │   │   ├── CorsConfig.java            # 跨域配置
│   │   │   └── JwtProperties.java         # JWT配置
│   │   ├── controller/                     # 控制器
│   │   │   ├── AuthController.java        # 认证与注册
│   │   │   ├── RoomController.java        # 房间管理
│   │   │   ├── BookingController.java     # 预订管理
│   │   │   ├── CheckInController.java     # 入住管理
│   │   │   ├── PosProductController.java  # 商品管理
│   │   │   ├── PosOrderController.java    # POS订单管理
│   │   │   ├── BillingController.java     # 账单结算
│   │   │   ├── HardwareController.java    # 硬件监控
│   │   │   ├── TaskController.java        # ✨ 任务管理（Phase 2）
│   │   │   ├── PointsController.java      # ✨ 积分管理（Phase 2）
│   │   │   └── PointsShopController.java  # ✨ 积分商城（Phase 2）
│   │   ├── dto/                            # 数据传输对象
│   │   │   ├── LoginDTO.java
│   │   │   ├── CheckInDTO.java
│   │   │   ├── BillDetailDTO.java         # 账单详情DTO
│   │   │   └── PosOrderDTO.java           # POS订单DTO
│   │   ├── entity/                         # 实体类（23个表对应）
│   │   │   ├── User.java                  # 用户基础表
│   │   │   ├── Guest.java                 # 住客扩展表（✨ 含会员等级、积分）
│   │   │   ├── Staff.java                 # 员工扩展表
│   │   │   ├── Room.java                  # 房间信息表
│   │   │   ├── Booking.java               # 预订记录表
│   │   │   ├── CheckInRecord.java         # 🔑 入住记录表（二次鉴权核心）
│   │   │   ├── Product.java               # 商品主表
│   │   │   ├── PosOrder.java              # POS订单表
│   │   │   ├── OrderItem.java             # 订单明细表
│   │   │   ├── HardwareStatus.java        # 硬件状态表
│   │   │   ├── Task.java                  # ✨ 任务模板表（Phase 2）
│   │   │   ├── TaskRecord.java            # ✨ 任务完成记录表（Phase 2）
│   │   │   ├── PointTransaction.java      # ✨ 积分流水表（Phase 2）
│   │   │   ├── PointsProduct.java         # ✨ 积分商城商品表（Phase 2）
│   │   │   ├── PointsRedemption.java      # ✨ 积分兑换记录表（Phase 2）
│   │   │   └── ...                        # 其他实体
│   │   ├── interceptor/
│   │   │   ├── RoomAuthInterceptor.java   # 🔑 二次鉴权拦截器
│   │   │   └── LoggingInterceptor.java    # 日志拦截器
│   │   ├── mapper/                         # MyBatis-Plus Mapper
│   │   ├── service/
│   │   │   ├── impl/                      # 服务实现类
│   │   │   ├── AuthService.java
│   │   │   ├── RoomService.java
│   │   │   ├── PosService.java            # POS业务逻辑
│   │   │   ├── BillingService.java        # ✨ 账单聚合逻辑（含会员折扣）
│   │   │   ├── HardwareSimulationService.java
│   │   │   ├── TaskService.java           # ✨ 任务业务逻辑（Phase 2）
│   │   │   ├── PointsService.java         # ✨ 积分业务逻辑（Phase 2）
│   │   │   └── PointsShopService.java     # ✨ 积分商城逻辑（Phase 2）
│   │   └── util/
│   │       └── JwtUtil.java               # JWT工具类
│   ├── src/main/resources/
│   │   ├── application.yml                 # 主配置
│   │   ├── application-docker.yml          # Docker环境配置
│   │   ├── sql/
│   │   │   └── insert_test_users_rbac.sql  # 测试用户数据
│   │   └── static/
│   │       └── hardware-monitor.html       # 监控大屏
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                                # Vue 3 前端
│   ├── src/
│   │   ├── api/                            # API接口封装
│   │   │   ├── auth.js
│   │   │   ├── room.js
│   │   │   ├── hardware.js
│   │   │   ├── pos.js                     # POS商品/订单API
│   │   │   ├── billing.js                 # 账单结算API
│   │   │   ├── task.js                    # ✨ 任务API（Phase 2）
│   │   │   ├── points.js                  # ✨ 积分API（Phase 2）
│   │   │   └── pointsShop.js              # ✨ 积分商城API（Phase 2）
│   │   ├── components/                    # 公共组件
│   │   ├── layouts/                       # 布局组件
│   │   │   ├── AdminLayout.vue            # 管理端布局
│   │   │   ├── StaffLayout.vue            # 前台端布局
│   │   │   └── GuestLayout.vue            # 住客端布局
│   │   ├── router/
│   │   │   └── index.js                   # 路由配置 + 权限守卫
│   │   ├── stores/
│   │   │   ├── user.js                    # 用户状态
│   │   │   └── pos.js                     # POS购物车
│   │   ├── utils/
│   │   │   └── request.js                 # Axios拦截器
│   │   ├── views/
│   │   │   ├── Login.vue                  # 登录页
│   │   │   ├── Dashboard.vue              # 控制台首页
│   │   │   ├── NotFound.vue               # 404页面
│   │   │   ├── admin/
│   │   │   │   └── Dashboard.vue          # 管理端首页
│   │   │   ├── staff/
│   │   │   │   ├── RoomManagement.vue     # 房态管理（含账单）
│   │   │   │   ├── CheckIn.vue            # 办理入住
│   │   │   │   ├── Workbench.vue          # 工作台
│   │   │   │   ├── HardwareMonitor.vue    # 硬件监控
│   │   │   │   ├── TaskManagement.vue     # ✨ 任务管理（Phase 2）
│   │   │   │   └── PointsShopManagement.vue # ✨ 积分商城管理（Phase 2）
│   │   │   └── guest/
│   │   │       ├── Home.vue               # 住客首页
│   │   │       ├── Booking.vue            # 房间预订
│   │   │       ├── PosOrders.vue          # POS点单
│   │   │       ├── Billing.vue            # 账单结算
│   │   │       ├── TasksAndPoints.vue     # ✨ 任务积分（Phase 2）
│   │   │       └── PointsShop.vue         # ✨ 积分商城（Phase 2）
│   │   ├── App.vue
│   │   └── main.js
│   ├── Dockerfile
│   ├── nginx.conf                         # Nginx反向代理配置
│   ├── package.json
│   └── vite.config.js
├── docker-compose.yml                      # Docker Compose编排
├── DOCKER.md                               # Docker部署文档
├── POS_SYSTEM_GUIDE.md                     # POS系统使用指南
├── RBAC_TEST_GUIDE.md                      # 权限测试指南
├── BILLING_DISCOUNT_AND_POINTS_FIX.md      # ✨ 账单折扣与积分奖励修复文档（Phase 2）
├── SRS.txt                                 # 需求规格说明书
└── README.md                               # 项目总览
```

---

## ✅ 已完成功能

### Module 1: 用户认证与权限管理 (P0)
- [x] 多角色用户体系（Guest住客、Staff员工、Admin管理员）
- [x] BCrypt密码加密存储
- [x] JWT Token无状态认证
- [x] 短信验证码注册（Redis存储，5分钟有效期）
- [x] 手机号登录/密码登录
- [x] **二次鉴权机制**（`@RoomAuthRequired` + Room-Auth-Token）
- [x] RBAC权限拦截（基于用户类型的路由守卫）
- [x] Token自动刷新与续期

### Module 2: 客房与入住管理 (P0)
- [x] 房态管理（VACANT空闲/OCCUPIED入住/CLEANING清洁/MAINTENANCE维修）
- [x] 多房型支持（单人/双人/五黑/VIP）
- [x] 房间预订系统
  - [x] 预订时间段查询
  - [x] 预订冲突检测
  - [x] 预订取消功能
- [x] 入住管理
  - [x] Walk-in直接入住
  - [x] 基于预订的入住
  - [x] 身份证号绑定与验证
  - [x] Room-Auth-Token生成
  - [x] 预期退房时间设置
- [x] 退房管理
  - [x] 房费自动计算（按天计费，进1制）
  - [x] 账单结清校验
  - [x] 权限自动回收
  - [x] 房态自动更新为CLEANING

### Module 3: POS销售点子系统 (P1) ✨ **Phase 1 完成**
- [x] 商品管理
  - [x] 三大类商品（零食/饮料/外设）
  - [x] 商品增删改查
  - [x] 库存管理与预警
  - [x] 商品上下架控制
  - [x] 租赁计费支持（按小时/按天/按次/不计费）
- [x] POS点单系统
  - [x] 购物车功能
  - [x] 实时库存校验
  - [x] 订单自动挂账到入住记录
  - [x] 订单状态管理（待处理/已配送/已取消）
- [x] 订单管理
  - [x] 订单列表查询
  - [x] 订单详情查看
  - [x] 订单状态更新
  - [x] 订单取消功能

### Module 4: 账单结算系统 (P0) ✨ **Phase 1 完成**
- [x] **房间级账单聚合**
  - [x] 房费 = 入住天数 × 每天价格
  - [x] POS消费 = 该房间所有住客订单总额
  - [x] 同房间所有住客共享同一账单
- [x] 账单查询
  - [x] 住客端：通过recordId查询个人账单
  - [x] 前台端：通过roomId查询房间账单
  - [x] 账单明细展示（房费明细 + POS订单列表）
- [x] 账单结算
  - [x] 多种支付方式（现金/微信/支付宝/银行卡）
  - [x] **支付传播机制**：任一住客支付后，全部住客待支付金额归零
  - [x] 支付状态实时同步
- [x] 退房校验
  - [x] 前台办理退房时检查未结清账单
  - [x] 待支付金额>0时阻止退房并提示

### Module 5: 硬件模拟与监控 (P1)
- [x] 硬件数据模拟
  - [x] 正态分布生成CPU/GPU温度
  - [x] 网络延迟模拟
  - [x] 外设连接状态模拟
  - [x] 可配置故障概率
- [x] 实时监控系统
  - [x] 5秒定时采集数据
  - [x] 三色健康等级（GREEN/YELLOW/RED）
  - [x] 连续异常判断（3次才触发报警）
- [x] 报警系统
  - [x] 过热报警（CPU>85°C或GPU>90°C）
  - [x] 网络故障报警（延迟>100ms）
  - [x] WebSocket实时推送报警
- [x] 维修工单管理
  - [x] 住客端设备报修界面
  - [x] 前台端工单管理界面
  - [x] 工单创建API（用户报修）
  - [x] 工单自动生成（系统报警触发）
  - [x] 工单状态管理（待处理/处理中/已解决/已关闭）
  - [x] 优先级管理（低/中/高/紧急）
  - [x] 房态联动更新

### Module 6: 游戏化与积分子系统 (P1) ✨ **Phase 2 完成** 🎉
- [x] **任务系统**
  - [x] 任务模板管理（发布、编辑、删除）
  - [x] 任务类型支持（自动检测AUTO、人工审核MANUAL_AUDIT）
  - [x] 任务提交系统（住客端截图上传+描述）
  - [x] 人工审核流程（员工审核凭证、通过/拒绝）
  - [x] 积分自动发放（审核通过后触发）
  - [x] 可重复任务标记
- [x] **积分商城**
  - [x] 积分商品管理（增删改查、上下架）
  - [x] 实物商品兑换（阿狸手办400分、盖亚皮肤钥匙扣200分等）
  - [x] 服务权益兑换（延迟退房1小时500分、房费抵扣券1000分）
  - [x] 兑换订单管理（待配送/已配送/已取消）
  - [x] 积分余额检查与扣减
  - [x] 库存自动扣减
  - [x] 兑换记录查询
- [x] **会员等级体系**
  - [x] 四级会员制度（青铜BRONZE/白银SILVER/黄金GOLD/铂金PLATINUM）
  - [x] 经验值累积系统（完成任务+账单清付奖励）
  - [x] 自动升级机制（1000/5000/10000经验值门槛）
  - [x] **会员折扣体系**（白银95折/黄金90折/铂金85折）
  - [x] **账单清付奖励积分**（消费金额÷人数，向上取整）
  - [x] **折扣自动应用**（选择最高会员等级折扣）
- [x] **积分流水管理**
  - [x] 积分变动记录（任务奖励/商品兑换/账单清付/管理员调整）
  - [x] 余额快照机制
  - [x] 流水查询API
  - [x] 积分余额查询
- [x] **前端界面完善**
  - [x] 住客端任务积分界面（可用任务+提交凭证+任务记录+积分流水）
  - [x] 住客端积分商城界面（商品浏览+兑换+兑换记录）
  - [x] 前台端任务管理界面（发布任务+审核任务）
  - [x] 前台端积分商城管理界面（商品管理+兑换订单管理）
  - [x] 积分商城菜单项（StaffLayout和GuestLayout）
- [x] **账单折扣集成** ⭐ 关键功能
  - [x] 账单查询时自动应用会员折扣
  - [x] 多人房间选择最高会员等级折扣
  - [x] 账单清付后所有住客获得积分和经验值
  - [x] 防止待支付金额为负数保护
  - [x] 退房时使用ADMIN_ADJUST类型记录积分

### Module 7: 声誉与评价管理子系统 (P2) ✨ **Phase 4 完成** 🎉
- [x] **评价提交功能** (FR-FDB-01)
  - [x] 住客退房后提交评分（1-5星）和文字评论
  - [x] 防重复评价机制（一个入住记录只能评价一次）
  - [x] 退房状态验证（未退房不允许评价）
  - [x] 星级评分组件（Element Plus Rate）
  - [x] 文字评价输入（最大500字）
  - [x] 提交成功后显示感谢语并跳转
- [x] **低分预警功能** (FR-FDB-02)
  - [x] 数据库虚拟列自动标记低分（评分<3星）
  - [x] 后端实体类正确配置（不参与插入/更新）
  - [x] 管理端低分评价列表（红色高亮显示）
  - [x] 自动排序（低分优先）
- [x] **回访闭环功能** (FR-FDB-03)
  - [x] 三阶段回访状态（NONE未处理/CONTACTED已联系/RESOLVED已解决）
  - [x] 回访备注记录
  - [x] 前台员工回访操作界面
  - [x] 回访记录查询与更新
- [x] **酒店回复功能** (FR-FDB-04)
  - [x] 管理员统一回复功能
  - [x] 回复内容展示
  - [x] 住客端查看酒店回复
  - [x] 评价与回复关联显示

### Module 8: 报表与决策支持子系统 (P2) ✨ **Phase 5 完成** 🎉
- [x] **运营看板** (FR-RPT-01)
  - [x] 实时展示入住率（当前入住房间数/总房间数）
  - [x] RevPAR指标（平均客房收益 = 总客房收入/可用房间数）
  - [x] 待处理报警数统计
  - [x] 待处理维修工单数统计
  - [x] 今日入住/退房数量
  - [x] 本月总收入和订单数
  - [x] 活跃会员数（近30天有入住记录）
  - [x] 自动刷新机制（每30秒）
  - [x] 响应式卡片布局
- [x] **财务报表** (FR-RPT-02)
  - [x] 财务日报生成（指定日期）
  - [x] 财务月报生成（指定年月）
  - [x] 收入分类统计（客房收入/POS收入/积分收入）
  - [x] 运营指标统计（入住订单数/POS订单数/平均客单价）
  - [x] 绩效指标统计（入住率/RevPAR）
  - [x] 报表导出功能（CSV格式，支持Excel打开）
  - [x] 报表类型切换（日报/月报）
  - [x] 可视化展示界面
- [x] **硬件损耗分析** (FR-RPT-03)
  - [x] 基于维修日志的设备故障统计（近30/60/90天可选）
  - [x] 设备类型智能识别（鼠标/键盘/显示器/耳机/电竞椅/主机等）
  - [x] 故障率计算（故障次数/总设备数）
  - [x] 涉及房间数统计
  - [x] 平均修复时间计算
  - [x] TOP3高频故障设备展示（彩色卡片）
  - [x] 采购建议算法（故障次数≥5：建议备货30%，≥3：少量备货）
  - [x] 采购清单生成（设备类型/品牌型号/建议数量/原因）
  - [x] 详细损耗统计表格（可排序）
  - [x] 可视化分析界面

### Module 9: 前端系统（三端统一）
- [x] **住客端 (Guest)**
  - [x] 房间预订界面（日期选择 + 可预订房间列表）
  - [x] POS点单界面（商品浏览 + 购物车 + 下单）
  - [x] 账单结算界面（账单明细 + 支付功能）
  - [x] 设备报修界面（报修表单 + 我的报修记录 + 工单详情）
  - [x] 任务积分界面（任务列表 + 提交任务 + 积分流水）✨ **Phase 2新增**
  - [x] 积分商城界面（商品浏览 + 兑换 + 兑换记录）✨ **Phase 2新增**
  - [x] 评价提交界面（星级评分 + 文字评论）✨ **Phase 4新增**
- [x] **前台端 (Staff)**
  - [x] 房态管理看板（房间卡片 + 状态筛选）
  - [x] 办理入住功能（身份信息录入 + 房间分配）
  - [x] 办理退房功能（费用结清校验 + 权限回收）
  - [x] 房间账单管理（查看待支付金额 + 代客结算）
  - [x] 工作台界面
  - [x] 硬件监控大屏（实时数据 + 告警弹窗）
  - [x] 维修工单管理界面（工单列表 + 状态更新 + 工单详情）
- [x] **管理端 (Admin)**
  - [x] 运营看板（核心指标实时展示）✨ **Phase 5新增**
  - [x] 财务报表界面（日报/月报切换 + CSV导出）✨ **Phase 5新增**
  - [x] 硬件损耗分析界面（故障统计 + 采购建议）✨ **Phase 5新增**
  - [x] 商品管理界面（增删改查 + 库存管理）
  - [x] 订单管理界面（订单列表 + 状态更新）

### 基础设施
- [x] Docker容器化部署（4个容器编排）
- [x] 前后端分离架构
- [x] Nginx反向代理（API转发 + 静态资源）
- [x] MySQL数据库持久化（23张表）
- [x] Redis缓存与会话管理
- [x] 全局异常处理与统一响应
- [x] MyBatis-Plus代码生成器
- [x] 跨域配置（CORS）
- [x] WebSocket实时通信
- [x] API接口文档（自动生成）

---

## 🔄 待开发功能（Phase 3+）

根据SRS.txt需求规格说明书，以下功能计划在后续阶段实现：

### Phase 3: 社交匹配子系统 (P1) ✅ **已完成**
- [x] 游戏档案管理（游戏类型、段位、擅长位置）
- [x] 组队招募系统（发布招募信息、筛选队友）
- [x] 临时战队管理（组队、解散、游戏时长记录）
- [x] 在线玩家大厅（浏览在线玩家）
- [x] WebSocket实时通知（招募申请推送）

### Phase 4: 评价管理子系统 (P2) ✅ **已完成** 🎉
- [x] **评价提交功能**
  - [x] 退房后评价（1-5星评分 + 文字评论）
  - [x] 入住信息自动关联
  - [x] 重复评价验证
  - [x] 星级评分组件（Element Plus Rate）
- [x] **低分预警系统**
  - [x] 数据库虚拟列自动标记（is_low_score）
  - [x] 评分<3星自动触发预警
  - [x] 低分评价列表高亮显示
  - [x] 待处理低分快速筛选
- [x] **回访闭环管理**
  - [x] 回访状态管理（NONE/CONTACTED/RESOLVED）
  - [x] 回访备注登记
  - [x] 处理人追溯记录
  - [x] 回访进度跟踪
- [x] **酒店回复功能**
  - [x] 管理员公开回复评价
  - [x] 回复内容展示
  - [x] 回复历史记录
- [x] **评价管理控制台**
  - [x] 评价列表分页查询
  - [x] 多维度筛选（评分/回访状态）
  - [x] 评价详情查看
  - [x] 低分行背景高亮
  - [x] 统计数据展示
- [x] **前端界面完善**
  - [x] 住客端评价提交页面
  - [x] 管理端评价管理页面
  - [x] 回访登记对话框
  - [x] 酒店回复对话框
  - [x] 菜单项集成

### Phase 5: 报表与决策支持子系统 (P2) 📅 计划中
- [ ] 运营数据看板
  - [ ] 入住率统计（日/周/月）
  - [ ] RevPAR计算（每间可销售房收入）
  - [ ] 房型收益分析
- [ ] 财务报表
  - [ ] 日报表（每日收入/支出）
  - [ ] 月报表（月度汇总）
  - [ ] 收入构成分析（房费/POS/其他）
- [ ] 硬件分析
  - [ ] 设备损耗统计
  - [ ] 维修成本分析
  - [ ] 采购需求预测

### Phase 6: 系统优化与扩展
- [ ] 消息推送系统（站内信、邮件、短信）
- [ ] 多语言支持（中文/英文）
- [ ] 移动端H5适配
- [ ] 数据备份与恢复
- [ ] 系统监控与日志分析
- [ ] 性能优化（缓存策略、数据库索引）

---

## 🚀 快速开始

### 方式一：Docker Compose一键启动（推荐）

#### 环境要求
- Docker 20.10+
- Docker Compose 2.0+

#### 启动步骤
```bash
# 1. 克隆项目
git clone <repository-url>
cd java_project2

# 2. 一键启动所有服务
docker-compose up -d

# 3. 查看服务状态
docker-compose ps

# 4. 查看日志
docker-compose logs -f backend
```

#### 服务访问
- **前端系统**：http://localhost （Nginx）
- **后端API**：http://localhost:8080
- **MySQL数据库**：localhost:3307（用户名：root，密码：root）
- **Redis缓存**：localhost:6379

#### 停止服务
```bash
# 停止所有服务
docker-compose down

# 停止并删除数据卷
docker-compose down -v
```

---

### 方式二：本地开发环境

#### 环境要求
- **后端**：JDK 21+、Maven 3.8+、MySQL 8.0+、Redis 6.0+
- **前端**：Node.js 18+、npm 9+

#### 后端启动
```bash
# 1. 启动MySQL（端口3307）
# 创建数据库
mysql -u root -p -e "CREATE DATABASE esports_hotel CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

# 2. 导入数据库schema
mysql -u root -p esports_hotel < database/schema.sql

# 3. 启动Redis
redis-server

# 4. 修改后端配置
# 编辑 backend/src/main/resources/application.yml
# 根据实际情况修改数据库连接信息

# 5. 启动Spring Boot
cd backend
mvn clean spring-boot:run
```

#### 前端启动
```bash
# 1. 安装依赖
cd frontend
npm install

# 2. 启动开发服务器
npm run dev
```

#### 访问地址
- **前端开发服务器**：http://localhost:5173
- **后端API**：http://localhost:8080/api
- **硬件监控测试页**：http://localhost:8080/api/hardware-monitor.html

---

### 测试账号

系统初始化后自动创建以下测试账号：

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 住客 | guest01 | Guest123 | 普通住客账号 |
| 住客 | 13800138000 | 123456 | 手机号登录账号 |
| 前台 | staff01 | Staff123 | 前台员工账号 |
| 管理员 | admin01 | Admin123 | 系统管理员 |

**注意**：首次登录后建议修改默认密码

---

### 详细文档
- 📘 [DOCKER.md](./DOCKER.md) - Docker部署完整指南
- 📗 [POS_SYSTEM_GUIDE.md](./POS_SYSTEM_GUIDE.md) - POS系统使用手册
- 📕 [RBAC_TEST_GUIDE.md](./RBAC_TEST_GUIDE.md) - 权限测试指南
- 📙 [SRS.txt](./SRS.txt) - 需求规格说明书

---

## 📊 数据库设计

系统采用MySQL 8.0，共设计23张表，分为8大模块：

### 1. 用户认证与权限模块（3张表）
- `tb_user` - 用户基础表（username、password_hash、user_type）
- `tb_guest` - 住客扩展表（会员等级、积分、累计入住天数）
- `tb_staff` - 员工扩展表（工号、角色、部门）

### 2. 客房与入住管理模块（4张表）
- `tb_room` - 房间主表（房号、房型、价格、房态、设施配置）
- `tb_booking` - 预订表（预订时间、计划入住/退房、押金）
- `tb_checkin_record` - ⭐ **入住记录表**（二次鉴权核心表）
  - 字段：actual_checkin, expected_checkout, is_gaming_auth_active
  - 用途：记录实际入住时间、授权客房权限
- `tb_room_change_log` - 换房历史表

### 3. 销售点模块（3张表）
- `tb_product` - 商品主表（商品名称、类型、价格、租赁单位、库存）
- `tb_pos_order` - POS订单表（订单号、订单类型、总金额、关联入住记录）
- `tb_order_item` - 订单明细表（商品、数量、单价、租赁时间段）

### 4. 硬件监控模块（4张表）
- `tb_hardware_status` - 硬件实时状态表（CPU/GPU温度、网络延迟、健康等级）
- `tb_device_log` - 设备日志表（历史数据归档）
- `tb_maintenance_ticket` - 维修工单表（故障描述、优先级、处理状态）
- `tb_alert_log` - 报警记录表（报警类型、触发值、处理状态）

### 5. 社交匹配模块（4张表）
- `tb_gaming_profile` - 游戏档案表
- `tb_recruitment` - 招募信息表
- `tb_team` - 临时战队表
- `tb_team_member` - 战队成员表

### 6. 游戏化与积分模块（5张表）
- `tb_task` - 任务模板表
- `tb_task_record` - 任务完成记录表
- `tb_point_transaction` - 积分流水表
- `tb_points_product` - 积分商城商品表
- `tb_points_redemption` - 积分兑换记录表

### 7. 评价管理模块（1张表）
- `tb_review` - 评价表（评分、评论、低分标识、回访状态）

### 8. 系统配置与日志（2张表）
- `tb_system_config` - 系统配置表
- `tb_operation_log` - 操作日志表

**关键设计要点：**
- 所有表使用InnoDB引擎，支持事务
- 字符集UTF-8（utf8mb4），支持中文和Emoji
- 外键约束确保数据一致性
- 索引优化高频查询（如房间查询、入住记录查询）
- 时间字段使用DATETIME类型，支持毫秒级精度

**完整DDL请查看：** [database/schema.sql](./database/schema.sql)

---

## 🔌 API 接口文档

### 认证模块（/api/auth）
| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| POST | `/sms/send` | 发送短信验证码 | ❌ |
| POST | `/register` | 用户注册 | ❌ |
| POST | `/login` | 用户登录 | ❌ |
| POST | `/logout` | 用户登出 | ✅ |
| GET | `/me` | 获取当前用户信息 | ✅ |

### 房间管理（/api/rooms）
| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| GET | `/` | 查询所有房间 | ✅ |
| GET | `/available` | 查询指定时间段可用房间 | ✅ |
| GET | `/status/{status}` | 按房态查询房间 | ✅ |
| GET | `/{roomId}` | 获取房间详情 | ✅ |
| PUT | `/{roomId}/status` | 更新房态（仅员工） | ✅ |

### 预订管理（/api/bookings）
| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| POST | `/` | 创建预订 | ✅ Guest |
| GET | `/my` | 查询我的预订 | ✅ Guest |
| PUT | `/{bookingId}/cancel` | 取消预订 | ✅ Guest |
| GET | `/` | 查询所有预订（管理） | ✅ Staff |

### 入住管理（/api/checkin）
| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| POST | `/` | 办理入住 | ✅ Staff |
| POST | `/{recordId}/checkout` | 办理退房 | ✅ Staff |
| GET | `/records` | 查询入住记录 | ✅ Staff |
| GET | `/current` | 查询我的当前入住状态 | ✅ Guest |

### POS商品管理（/api/pos/products）
| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| GET | `/` | 查询所有商品 | ✅ |
| GET | `/{productId}` | 获取商品详情 | ✅ |
| POST | `/` | 创建商品 | ✅ Admin |
| PUT | `/{productId}` | 更新商品信息 | ✅ Admin |
| DELETE | `/{productId}` | 删除商品 | ✅ Admin |
| PUT | `/{productId}/disable` | 下架商品 | ✅ Admin |
| PUT | `/{productId}/stock` | 更新库存 | ✅ Admin |

### POS订单管理（/api/pos/orders）
| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| POST | `/` | 创建订单 | 🔑 Room-Auth |
| GET | `/my` | 查询我的订单 | ✅ Guest |
| GET | `/{orderId}` | 获取订单详情 | ✅ |
| PUT | `/{orderId}/cancel` | 取消订单 | ✅ Guest |
| PUT | `/{orderId}/status` | 更新订单状态 | ✅ Staff |
| GET | `/all` | 查询所有订单 | ✅ Staff |

### 账单结算（/api/billing）
| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| GET | `/detail/{recordId}` | 查询账单详情（按入住记录） | ✅ Guest |
| GET | `/detail/room/{roomId}` | 查询账单详情（按房间号） | ✅ Staff |
| POST | `/settle/{recordId}` | 结算账单 | ✅ Guest |

### 硬件监控（/api/hardware）
| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| GET | `/status` | 获取所有房间硬件状态 | ✅ Staff |
| GET | `/status/{roomId}` | 获取指定房间硬件状态 | 🔑 Room-Auth |
| POST | `/trigger-failure/{roomId}` | 手动触发故障（测试用） | ✅ Admin |
| GET | `/alerts/unhandled` | 获取未处理报警 | ✅ Staff |
| PUT | `/alerts/{alertId}/handle` | 标记报警已处理 | ✅ Staff |
| GET | `/tickets` | 获取维修工单列表 | ✅ Staff |
| PUT | `/tickets/{ticketId}` | 更新工单状态 | ✅ Staff |

### WebSocket 订阅地址
| 地址 | 说明 | 推送频率 |
|------|------|----------|
| `/topic/hardware` | 硬件数据流 | 每5秒 |
| `/topic/alerts` | 报警通知 | 实时 |

**鉴权说明：**
- ❌ 无需认证
- ✅ 需要JWT Token
- 🔑 需要Room-Auth-Token（二次鉴权）
- Admin/Staff/Guest 表示仅特定角色可访问

---

## 🧪 测试场景

### 场景一：住客完整流程（Guest端）
**目标**：验证从注册到退房的完整业务链路

1. **用户注册与登录**
   ```
   访问前端：http://localhost:5173
   手机号：18800000001
   验证码：123456（测试环境固定）
   登录成功 → 跳转到住客首页
   ```

2. **预订房间**
   - 查看可用房间列表（201豪华单人间 ¥360/天、202豪华双人间 ¥600/天等）
   - 选择房间201，预订时间：今天14:00 - 明天12:00
   - 提交预订，状态变更为"待入住"

3. **等待前台办理入住**
   - 前台员工（staff01）在工作台确认预订
   - 验证住客身份信息，办理入住手续
   - 系统生成入住记录，分配Room-Auth-Token

4. **POS点单（客房服务）**
   ```
   访问点单页面（需已入住）
   商品列表：
     - 可口可乐 ¥5
     - 辣条 ¥3
     - 辣条Pro ¥15
     - 小米充电宝（租赁）¥10/天
   加入购物车：可口可乐 × 2
   提交订单：选择"挂账结算"
   ```

5. **查看账单明细**
   - 进入"我的账单"
   - 查看费用构成：
     - 房费：360元（1天 × 360元/天）
     - POS消费：10元（可口可乐 × 2）
     - 未支付金额合计：370元

6. **账单结算**
   - 点击"结算账单"按钮
   - 输入支付密码（测试环境可跳过）
   - 支付成功，未支付金额清零
   - **关键验证**：同房间其他住客的未支付金额同步清零

7. **办理退房**
   - 前台员工验证账单已结清
   - 点击"退房"按钮
   - 房间状态变更为"空闲"，可接受新预订

**预期结果**：
- ✅ 所有订单状态正确流转（待确认 → 待支付 → 已完成）
- ✅ 账单金额计算准确（房费 + POS消费）
- ✅ 同房间多位住客支付状态同步更新
- ✅ 房间状态正确变更（空闲 → 已入住 → 空闲）

---

### 场景二：前台员工工作流程（Staff端）
**目标**：验证员工日常操作和权限边界

1. **登录员工账号**
   ```
   用户名：staff01
   密码：Staff123
   登录成功 → 进入员工工作台
   ```

2. **办理入住**
   - 查看"待入住"预订列表
   - 选择住客"guest01"的预订
   - 验证身份证信息：110101199001011234
   - 点击"确认入住"
   - 系统分配Room-Auth-Token，住客获得客房权限

3. **房间管理**
   - 查看房间状态列表：
     - 🟢 201房间：已入住（guest01）
     - 🟢 202房间：空闲
     - 🔴 301房间：维修中（GPU过热）
   - 点击房间卡片查看详情

4. **账单查询**
   - 打开201房间的"账单明细"弹窗
   - 查看消费详情：
     ```
     房费：360元（2025-12-18 14:00 - 2025-12-19 12:00）
     POS订单：
       - 可口可乐 × 2 = 10元
       - 辣条Pro × 1 = 15元
     未支付金额：385元
     ```
   - 点击"账单清付"按钮，通知住客支付

5. **处理硬件报警**
   - 访问"硬件监控"页面
   - 实时数据显示：
     - 301房间GPU温度：85°C（正常：< 75°C）
     - 健康等级：🔴 异常
   - 查看报警记录：
     ```
     报警ID：#001
     房间：301
     设备：GPU
     触发值：85°C
     触发时间：2025-12-18 15:30:45
     状态：未处理
     ```
   - 创建维修工单：
     ```
     工单号：TK001
     故障描述：GPU温度过高
     优先级：高
     状态：待处理 → 处理中 → 已完成
     ```
   - 更新房间状态为"维修中"

6. **办理退房**
   - 确认账单已结清（未支付金额 = 0）
   - 点击"退房"按钮
   - 系统回收Room-Auth-Token，住客失去客房权限
   - 房间状态变更为"空闲"

**预期结果**：
- ✅ 可办理入住/退房，但不能修改商品价格
- ✅ 可查看所有房间账单，但不能删除订单
- ✅ 可处理硬件报警，但不能修改报警阈值

---

### 场景三：POS系统完整测试（Admin端）
**目标**：验证商品管理、库存管理、挂账结算

1. **商品管理（管理员）**
   ```
   登录：admin01 / Admin123
   进入"商品管理"页面
   ```

2. **新增商品**
   - 点击"新增商品"
   - 填写信息：
     ```
     商品名称：可口可乐
     商品类型：BEVERAGE（饮料）
     价格：¥5
     库存：100瓶
     状态：上架
     ```
   - 上传商品图片
   - 提交保存

3. **库存管理**
   - 查看库存预警：
     ```
     ⚠️ 辣条：库存3份（低于10）
     ⚠️ 充电宝：库存2个（低于10）
     ```
   - 批量更新库存：
     - 可口可乐：100 → 150（+50瓶）
     - 辣条：3 → 53（+50份）
   - 查看库存变动日志

4. **订单处理流程**
   - 住客在房间内下单：
     ```
     购物车：
       - 可口可乐 × 2 = ¥10
       - 辣条 × 3 = ¥9
       - 小米充电宝（租赁1天）= ¥10
     合计：¥29
     支付方式：挂账结算
     ```
   - 前台员工收到订单通知
   - 员工配送商品到房间
   - 更新订单状态：待处理 → 处理中 → 已完成
   - 系统自动扣减库存：
     - 可口可乐：150 → 148
     - 辣条：53 → 50

5. **挂账结算验证**
   - 住客下单后，订单金额自动计入房间账单
   - 查看201房间账单：
     ```
     房费：360元
     POS订单1：10元（可口可乐 × 2）
     POS订单2：29元（新订单）
     未支付金额：399元
     ```
   - 住客退房前统一结算所有消费
   - 验证账单明细包含所有POS订单

**预期结果**：
- ✅ 商品增删改查功能正常
- ✅ 库存变动准确记录
- ✅ 订单状态流转正确
- ✅ 挂账金额准确计入账单
- ✅ 结算后库存正确扣减

---

### 场景四：硬件监控实时测试
**目标**：验证WebSocket实时通信和报警机制

1. **启动硬件监控页面**
   ```
   访问：http://localhost:5173/staff/hardware-monitor
   或：http://localhost:8080/api/hardware-monitor.html（独立页面）
   ```

2. **WebSocket自动连接**
   - 控制台输出：
     ```
     [WebSocket] 连接成功
     [WebSocket] 订阅 /topic/hardware
     [WebSocket] 订阅 /topic/alerts
     ```

3. **实时数据推送（每5秒）**
   - 接收到的数据示例：
     ```json
     [
       {
         "roomNumber": "201",
         "cpuTemperature": 45.2,
         "gpuTemperature": 68.5,
         "cpuUsage": 35.0,
         "memoryUsage": 60.0,
         "networkLatency": 25,
         "healthLevel": "GOOD"
       },
       {
         "roomNumber": "301",
         "cpuTemperature": 55.8,
         "gpuTemperature": 85.0,
         "healthLevel": "BAD"
       }
     ]
     ```
   - 房间卡片根据健康等级变色：
     - 🟢 EXCELLENT（优秀）：绿色
     - 🟡 GOOD（良好）：黄色
     - 🔴 BAD（异常）：红色

4. **手动触发故障（测试用）**
   ```bash
   # 使用管理员Token调用API
   curl -X POST http://localhost:8080/api/hardware/trigger-failure/301 \
     -H "Authorization: Bearer <admin_token>"
   ```

5. **报警触发流程**
   - 301房间GPU温度连续3次超过75°C
   - 系统自动生成报警记录
   - WebSocket推送报警通知：
     ```json
     {
       "alertId": 1,
       "roomNumber": "301",
       "deviceType": "GPU",
       "metric": "temperature",
       "currentValue": 85.0,
       "threshold": 75.0,
       "severity": "HIGH",
       "message": "301房间GPU温度过高：85.0°C"
     }
     ```
   - 监控页面右上角弹出红色报警通知

6. **处理报警工单**
   - 前台员工点击报警通知
   - 查看工单详情：
     ```
     工单号：TK001
     房间：301
     设备：GPU
     故障描述：GPU温度过高（85.0°C）
     优先级：高
     创建时间：2025-12-18 15:30:45
     ```
   - 更新工单状态：待处理 → 处理中
   - 联系维修人员，处理GPU散热问题
   - 温度恢复正常后，关闭工单：处理中 → 已完成
   - 报警记录标记为"已处理"

**预期结果**：
- ✅ WebSocket连接稳定，无断连
- ✅ 数据推送频率准确（5秒/次）
- ✅ 报警触发逻辑正确（连续3次异常）
- ✅ 工单创建和状态流转正常
- ✅ 前端UI实时更新，无延迟

---

### 场景五：角色权限边界测试
**目标**：验证RBAC权限控制

#### 1. 住客权限测试（Guest）
```
登录：guest01 / Guest123
```

**✅ 允许操作：**
- 查看房间列表（GET /api/rooms）
- 创建预订（POST /api/bookings）
- 查看我的预订（GET /api/bookings/my）
- 取消我的预订（PUT /api/bookings/{id}/cancel）
- POS下单（POST /api/pos/orders，需Room-Auth-Token）
- 查看我的订单（GET /api/pos/orders/my）
- 查看我的账单（GET /api/billing/detail/{recordId}）
- 结算账单（POST /api/billing/settle/{recordId}）

**❌ 禁止操作（返回403）：**
- 办理入住（POST /api/checkin）
- 办理退房（POST /api/checkin/{recordId}/checkout）
- 查看所有预订（GET /api/bookings）
- 管理商品（POST /api/pos/products）
- 处理硬件报警（PUT /api/hardware/alerts/{id}/handle）

#### 2. 前台员工权限测试（Staff）
```
登录：staff01 / Staff123
```

**✅ 允许操作：**
- 所有Guest端功能
- 办理入住/退房
- 查看所有预订和入住记录
- 查看所有POS订单
- 更新订单状态（已完成/已取消）
- 查看所有房间账单
- 查看硬件监控数据
- 处理报警和工单

**❌ 禁止操作（返回403）：**
- 创建/删除商品（POST/DELETE /api/pos/products）
- 修改商品价格（PUT /api/pos/products/{id}）
- 更新库存（PUT /api/pos/products/{id}/stock）
- 手动触发故障（POST /api/hardware/trigger-failure）
- 删除用户
- 修改系统配置

#### 3. 管理员权限测试（Admin）
```
登录：admin01 / Admin123
```

**✅ 允许操作：**
- 所有Staff端功能
- 所有商品管理功能（增删改查、上下架、库存管理）
- 查看所有用户信息
- 手动触发硬件故障（测试用）
- 修改系统配置
- 查看系统日志

**❌ 禁止操作：**
- （无限制，拥有最高权限）

#### 4. 二次鉴权测试（Room-Auth-Token）
**场景**：住客未入住时尝试POS下单

```bash
# 1. 住客登录获取JWT Token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"guest01","password":"Guest123"}'

# 2. 尝试下单（未办理入住，无Room-Auth-Token）
curl -X POST http://localhost:8080/api/pos/orders \
  -H "Authorization: Bearer <jwt_token>" \
  -H "Content-Type: application/json" \
  -d '{"items":[{"productId":1,"quantity":2}]}'

# 预期返回：403 Forbidden
# 错误信息："未找到有效的入住记录，无法访问客房服务"
```

**场景**：住客办理入住后下单

```bash
# 1. 前台办理入住（staff01）
curl -X POST http://localhost:8080/api/checkin \
  -H "Authorization: Bearer <staff_token>" \
  -H "Content-Type: application/json" \
  -d '{"bookingId":1,"roomId":201,"guestIds":[1]}'

# 2. 系统生成Room-Auth-Token（自动关联到住客）
# 3. 住客再次尝试下单（此时有Room-Auth-Token）
curl -X POST http://localhost:8080/api/pos/orders \
  -H "Authorization: Bearer <jwt_token>" \
  -H "Content-Type: application/json" \
  -d '{"items":[{"productId":1,"quantity":2}]}'

# 预期返回：200 OK
# 订单创建成功，金额计入房间账单
```

**预期结果**：
- ✅ 未入住时无法访问客房服务（POS下单、硬件查询等）
- ✅ 入住后自动获得客房权限
- ✅ 退房后权限自动回收
- ✅ 跨房间访问被拒绝（201房间住客无法访问202房间服务）

---

**详细测试文档请参考：**
- 📕 [RBAC_TEST_GUIDE.md](./RBAC_TEST_GUIDE.md) - 完整权限测试用例
- 📗 [POS_SYSTEM_GUIDE.md](./backend/src/main/resources/static/POS_SYSTEM_GUIDE.md) - POS系统功能测试

---

## 📝 开发规范

### 代码风格

#### Java后端规范
- **编码标准**：遵循阿里巴巴Java开发手册
- **命名规范**：
  - 类名：大驼峰（PascalCase），如 `UserService`
  - 方法名：小驼峰（camelCase），如 `getUserById`
  - 常量：全大写下划线分隔，如 `MAX_RETRY_COUNT`
- **注释规范**：
  - 类和方法必须添加JavaDoc注释
  - 复杂业务逻辑添加行内注释
  - 示例：
    ```java
    /**
     * 办理入住业务
     * @param bookingId 预订ID
     * @param roomId 房间ID
     * @return 入住记录
     */
    public CheckinRecord processCheckin(Long bookingId, Integer roomId) {
        // 业务逻辑
    }
    ```

#### 前端规范
- **框架标准**：Vue 3 Composition API + `<script setup>`
- **类型安全**：优先使用TypeScript（.vue文件中使用`<script setup lang="ts">`）
- **命名规范**：
  - 组件名：大驼峰，如 `RoomCard.vue`
  - 变量/函数：小驼峰，如 `const userName = ref('')`
  - Props：小驼峰，如 `const props = defineProps<{ roomId: number }>()`
- **组件结构**：
  ```vue
  <script setup lang="ts">
  // 1. imports
  // 2. defineProps / defineEmits
  // 3. reactive state
  // 4. computed
  // 5. methods
  // 6. lifecycle hooks
  </script>

  <template>
    <!-- 模板内容 -->
  </template>

  <style scoped>
    /* 样式 */
  </style>
  ```

#### API接口规范
- **RESTful风格**：
  - GET：查询资源
  - POST：创建资源
  - PUT：更新资源
  - DELETE：删除资源
- **URL命名**：全小写，多个单词用`-`连接
  - ✅ `/api/pos/products`
  - ❌ `/api/PosProducts`
- **统一响应格式**：
  ```json
  {
    "code": 200,
    "message": "操作成功",
    "data": { ... }
  }
  ```
- **错误码规范**：
  - 200：成功
  - 400：参数错误
  - 401：未认证
  - 403：无权限
  - 404：资源不存在
  - 500：服务器错误

### Git 提交规范

#### Commit Message格式
```
<type>(<scope>): <subject>

<body>

<footer>
```

#### Type类型
- `feat`: 新增功能
- `fix`: 修复Bug
- `docs`: 文档更新
- `style`: 代码格式（不影响代码运行）
- `refactor`: 代码重构
- `perf`: 性能优化
- `test`: 测试相关
- `chore`: 构建/工具链配置
- `revert`: 回滚提交

#### Scope范围（可选）
- `auth`: 认证模块
- `room`: 房间管理
- `pos`: POS系统
- `billing`: 账单结算
- `hardware`: 硬件监控
- `frontend`: 前端
- `backend`: 后端
- `docker`: Docker配置

#### 示例
```bash
# 新增功能
git commit -m "feat(pos): 新增商品库存预警功能"

# 修复Bug
git commit -m "fix(billing): 修复账单重复支付问题"

# 文档更新
git commit -m "docs(readme): 更新快速开始指南"

# 代码重构
git commit -m "refactor(room): 优化房间状态查询逻辑"

# 多行提交
git commit -m "feat(hardware): 新增硬件监控WebSocket推送

- 实现每5秒推送硬件状态数据
- 添加报警实时通知功能
- 优化前端数据展示性能

Closes #123"
```

### 分支管理规范

#### 分支命名
- `main`: 主分支（生产环境）
- `develop`: 开发分支
- `feature/*`: 功能分支，如 `feature/pos-inventory-alert`
- `bugfix/*`: Bug修复分支，如 `bugfix/billing-duplicate-payment`
- `hotfix/*`: 紧急修复分支，如 `hotfix/security-vulnerability`
- `release/*`: 发布分支，如 `release/v1.0.0`

#### 工作流程
1. 从`develop`创建功能分支
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/pos-inventory-alert
   ```

2. 开发功能并提交
   ```bash
   git add .
   git commit -m "feat(pos): 新增库存预警功能"
   git push origin feature/pos-inventory-alert
   ```

3. 创建Pull Request
   - 目标分支：`develop`
   - 填写PR描述：功能说明、测试结果、截图等
   - 请求Code Review

4. 合并到主分支
   ```bash
   # 审核通过后合并
   git checkout develop
   git merge --no-ff feature/pos-inventory-alert
   git push origin develop
   
   # 删除功能分支
   git branch -d feature/pos-inventory-alert
   git push origin --delete feature/pos-inventory-alert
   ```

### 代码审查规范

#### 提交PR前检查清单
- [ ] 代码通过本地测试
- [ ] 代码符合编码规范
- [ ] 添加了必要的注释
- [ ] 更新了相关文档
- [ ] 没有console.log等调试代码
- [ ] 没有硬编码的敏感信息（密码、Token等）

#### Code Review重点
1. **功能正确性**：业务逻辑是否正确
2. **代码质量**：是否有重复代码、过长方法
3. **性能考虑**：是否有性能瓶颈（N+1查询、大循环等）
4. **安全性**：是否有SQL注入、XSS等安全风险
5. **可维护性**：代码是否易读、易维护

### 测试规范

#### 单元测试
- 使用JUnit 5 + Mockito
- 测试覆盖率目标：核心业务逻辑 > 80%
- 命名规范：`方法名_测试场景_预期结果`
  ```java
  @Test
  void settleBill_whenRoomHasMultipleGuests_shouldClearAllUnpaidAmount() {
      // Given
      // When
      // Then
  }
  ```

#### 集成测试
- 使用Spring Boot Test
- 测试完整业务流程（入住 → POS下单 → 账单结算 → 退房）
- Mock外部依赖（Redis、第三方API）

#### 前端测试
- 组件测试：Vue Test Utils + Vitest
- E2E测试：Playwright（可选）

---

## 🤝 贡献指南

欢迎为智慧电竞酒店管理系统贡献代码！请遵循以下流程：

### 参与方式

1. **提交Bug报告**
   - 搜索[Issues](../../issues)确认问题未被报告
   - 使用Bug模板创建Issue
   - 提供详细的复现步骤和环境信息

2. **提交功能请求**
   - 搜索现有Issue避免重复
   - 清晰描述功能需求和使用场景
   - 如果可能，提供设计方案或原型图

3. **贡献代码**
   - Fork本项目到你的GitHub账号
   - 创建功能分支（`git checkout -b feature/AmazingFeature`）
   - 编写代码并遵循[开发规范](#📝-开发规范)
   - 提交更改（`git commit -m 'feat: Add some AmazingFeature'`）
   - 推送到分支（`git push origin feature/AmazingFeature`）
   - 创建Pull Request

### Pull Request规范

#### PR标题格式
```
<type>(<scope>): <subject>
```
示例：
- `feat(pos): 新增商品分类管理功能`
- `fix(billing): 修复账单计算精度问题`
- `docs(readme): 更新Docker部署文档`

#### PR描述模板
```markdown
## 功能描述
<!-- 简要描述本次PR的目的和实现内容 -->

## 相关Issue
<!-- 关联的Issue编号，如：Closes #123 -->

## 修改内容
- [ ] 后端API实现
- [ ] 前端UI开发
- [ ] 数据库表结构变更
- [ ] 文档更新

## 测试结果
<!-- 提供测试截图或测试报告 -->
- [ ] 单元测试通过
- [ ] 集成测试通过
- [ ] 手动测试通过

## 截图
<!-- 如果有UI变更，请提供前后对比截图 -->

## 注意事项
<!-- 需要特别注意的问题或向后不兼容的变更 -->
```

### 本地开发环境搭建

#### 1. Fork项目
点击右上角"Fork"按钮，将项目复制到你的GitHub账号

#### 2. 克隆到本地
```bash
git clone https://github.com/YOUR_USERNAME/java_project2.git
cd java_project2
```

#### 3. 添加上游仓库
```bash
git remote add upstream https://github.com/ORIGINAL_OWNER/java_project2.git
git remote -v  # 验证配置
```

#### 4. 同步上游更新
```bash
git fetch upstream
git checkout develop
git merge upstream/develop
git push origin develop
```

#### 5. 创建功能分支
```bash
git checkout develop
git checkout -b feature/your-feature-name
```

#### 6. 启动开发环境
参考[快速开始](#🚀-快速开始)章节

### 代码提交前检查

在提交PR前，请确认以下事项：

#### 代码质量
- [ ] 代码通过所有测试（`mvn test` 和 `npm test`）
- [ ] 代码符合编码规范（运行Lint检查）
- [ ] 没有遗留的调试代码（`console.log`、`System.out.println`等）
- [ ] 没有硬编码的敏感信息（密码、API Key等）

#### 文档
- [ ] 更新了相关API文档
- [ ] 添加了必要的代码注释
- [ ] 更新了README（如有新功能）
- [ ] 更新了数据库schema（如有表结构变更）

#### 测试
- [ ] 新增代码有对应的单元测试
- [ ] 测试覆盖率 ≥ 80%（核心业务逻辑）
- [ ] 手动测试通过（完整业务流程）

#### Git规范
- [ ] Commit message符合规范
- [ ] 一个PR只做一件事（避免混合多个不相关的修改）
- [ ] 代码基于最新的`develop`分支

### 运行测试

#### 后端测试
```bash
cd backend

# 运行所有测试
mvn test

# 运行单个测试类
mvn test -Dtest=BillingServiceTest

# 生成测试覆盖率报告
mvn jacoco:report
# 查看报告：backend/target/site/jacoco/index.html
```

#### 前端测试
```bash
cd frontend

# 运行单元测试
npm test

# 运行Lint检查
npm run lint

# 修复Lint错误
npm run lint:fix
```

### 常见问题

#### 1. 如何解决合并冲突？
```bash
# 同步上游最新代码
git fetch upstream
git checkout develop
git merge upstream/develop

# 切换到功能分支并合并develop
git checkout feature/your-feature
git merge develop

# 手动解决冲突后提交
git add .
git commit -m "chore: 解决合并冲突"
git push origin feature/your-feature
```

#### 2. 如何撤销本地提交？
```bash
# 撤销最后一次提交，保留代码修改
git reset --soft HEAD~1

# 撤销最后一次提交，丢弃代码修改
git reset --hard HEAD~1
```

#### 3. 如何修改提交信息？
```bash
# 修改最后一次提交信息
git commit --amend -m "feat(pos): 新的提交信息"

# 强制推送到远程（已推送的情况）
git push origin feature/your-feature --force-with-lease
```

### 贡献者名单

感谢所有为本项目做出贡献的开发者！

<!-- 此处将自动生成贡献者列表 -->

### 联系维护者

如有任何问题，请通过以下方式联系：
- GitHub Issues：https://github.com/OWNER/java_project2/issues
- Email：（如有项目邮箱）

---

## 📄 许可证

本项目采用 **MIT License** 开源协议。

### 使用条款

#### ✅ 允许
- 个人学习和研究使用
- 修改源代码并创建衍生作品
- 在遵守许可证的前提下用于商业项目
- 私有使用和分发

#### ⚠️ 限制
- 不提供任何形式的担保和保证
- 作者不对使用本软件造成的任何损失负责
- 商业使用需保留原作者版权声明

#### 📋 义务
- 保留原作者版权声明和许可证文本
- 修改后的代码需注明修改内容
- 衍生作品建议（非强制）使用相同许可证

### 完整许可证文本

```
MIT License

Copyright (c) 2025 [项目作者]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 📧 联系方式

### 项目资源
- **项目主页**：https://github.com/OWNER/java_project2
- **问题反馈**：https://github.com/OWNER/java_project2/issues
- **功能请求**：https://github.com/OWNER/java_project2/issues/new?template=feature_request.md
- **Bug报告**：https://github.com/OWNER/java_project2/issues/new?template=bug_report.md

### 文档导航
- 📘 [Docker部署指南](./DOCKER.md) - 完整的容器化部署文档
- 📗 [POS系统使用手册](./POS_SYSTEM_GUIDE.md) - 销售点系统详细说明
- 📕 [权限测试指南](./RBAC_TEST_GUIDE.md) - RBAC权限体系测试
- 📙 [需求规格说明书](./SRS.txt) - 完整的功能需求文档
- 📄 [登录问题修复](./LOGIN_FIX.md) - 常见登录问题解决方案
- 📊 [Phase 1开发报告](./PHASE1_TASK1.1_1.2_REPORT.md) - 第一阶段开发总结

### 获取帮助

#### 常见问题
参考文档中的FAQ部分，大部分问题都能找到答案。

#### 报告Bug
提交Issue时请包含以下信息：
- 操作系统和版本
- Docker/Node.js/JDK版本
- 详细的复现步骤
- 错误日志和截图
- 预期行为和实际行为

#### 功能建议
欢迎提交功能请求，请说明：
- 功能的使用场景
- 预期的实现方式
- 是否愿意贡献代码

#### 技术支持
- 查看[开发规范](#📝-开发规范)
- 阅读[贡献指南](#🤝-贡献指南)
- 加入讨论区（如有）

### 致谢

感谢以下开源项目为本系统提供支持：
- [Spring Boot](https://spring.io/projects/spring-boot) - 后端框架
- [Vue.js](https://vuejs.org/) - 前端框架
- [Element Plus](https://element-plus.org/) - UI组件库
- [MyBatis-Plus](https://baomidou.com/) - ORM框架
- [Docker](https://www.docker.com/) - 容器化平台

---

## 📊 项目统计

![GitHub stars](https://img.shields.io/github/stars/OWNER/java_project2?style=social)
![GitHub forks](https://img.shields.io/github/forks/OWNER/java_project2?style=social)
![GitHub issues](https://img.shields.io/github/issues/OWNER/java_project2)
![GitHub license](https://img.shields.io/github/license/OWNER/java_project2)

### 代码统计
- **总行数**：约 50,000 行
- **后端代码**：约 30,000 行（Java + XML）
- **前端代码**：约 15,000 行（Vue + TypeScript）
- **数据库脚本**：约 5,000 行（SQL）

### 开发进度
- ✅ **Phase 1**：POS销售点子系统（100%）
- ✅ **Phase 2**：游戏化与积分子系统（100%）🎉
  - ✅ 任务系统（发布、提交、审核、奖励）
  - ✅ 积分商城（商品管理、兑换、订单）
  - ✅ 会员等级体系（四级会员、自动升级）
  - ✅ 账单折扣集成（会员折扣、积分奖励）
  - ✅ 积分流水管理（变动记录、余额查询）
- 📅 **Phase 3**：社交匹配子系统（规划中）
- ⏳ **Phase 4**：评价管理系统（待启动）
- ⏳ **Phase 5**：报表与决策支持（待启动）
- ⏳ **Phase 6**：系统优化与扩展（待启动）

---

<div align="center">

**如果这个项目对你有帮助，欢迎 ⭐ Star 支持！**

**最后更新：** 2025-12-18  
**当前版本：** v1.1.0-Phase2  
**开发状态：** 🎉 Phase 2已完成！游戏化与积分系统全面上线

Made with ❤️ by [项目作者]

</div>
