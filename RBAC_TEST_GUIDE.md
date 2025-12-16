# RBAC功能测试指南

## 📋 任务完成情况

### ✅ 任务1.1：前端架构重构（视图拆分）
- [x] 创建三套Layout组件：AdminLayout、StaffLayout、GuestLayout
- [x] 创建角色目录结构：views/admin/、views/staff/、views/guest/
- [x] 移动硬件监控代码到前台端目录（staff/HardwareMonitor.vue）
- [x] 创建各角色的主要视图页面

### ✅ 任务1.2：鉴权与RBAC修复
- [x] 后端User实体包含userType字段（GUEST/STAFF/ADMIN）
- [x] 注册默认角色为GUEST
- [x] 登录返回userType并根据角色跳转到对应首页
- [x] 路由添加meta.roles权限控制
- [x] 实现router.beforeEach全局RBAC守卫

---

## 🧪 测试账号

| 手机号（即用户名）| 密码   | 角色  | 用途说明       |
|-------------------|--------|-------|----------------|
| 13900000001       | Test123456 | ADMIN | 酒店管理员     |
| 13900000002       | Test123456 | STAFF | 前台员工1      |
| 13900000003       | Test123456 | STAFF | 前台员工2      |
| 13900000004       | Test123456 | GUEST | 测试住客       |
| 13800138000       | Test123456 | GUEST | 原有住客账号   |

---

## 🎯 测试场景

### 场景1：角色路由隔离测试

#### 1.1 管理员登录
1. 打开 http://localhost
2. 使用管理员账号登录（**手机号：13900000001** / 密码：123456）
3. **预期结果**：
   - ✅ 自动跳转到 `/admin/dashboard`
   - ✅ 看到紫色渐变顶栏「智慧电竞酒店 - 管理端」
   - ✅ 侧边栏显示：数据看板、运营报表、员工管理、系统配置
   - ✅ 主区域显示管理看板（营收、入住率等统计卡片）

#### 1.2 前台员工登录
1. 退出管理员账号
2. 使用前台员工账号登录（**手机号：13900000002** / 密码：123456）
3. **预期结果**：
   - ✅ 自动跳转到 `/staff/workbench`
   - ✅ 看到绿色渐变顶栏「智慧电竞酒店 - 前台工作台」
   - ✅ 侧边栏显示：工作台、房态管理、硬件监控、POS订单、维修工单、任务审核
   - ✅ 可访问硬件监控功能（已从根目录移动到staff/）

#### 1.3 住客登录
1. 退出前台员工账号
2. 使用住客账号登录（**手机号：13900000004** / 密码：123456）
3. **预期结果**：
   - ✅ 自动跳转到 `/guest/home`
   - ✅ 看到紫色渐变顶栏「智慧电竞酒店 - 住客端」
   - ✅ 顶部显示提示：「您尚未绑定房间」
   - ✅ 侧边栏显示：首页、预订房间
   - ✅ 「商城点餐」等功能显示锁定状态（需入住后二次鉴权）

---

### 场景2：权限守卫测试

#### 2.1 跨角色访问拦截
1. 使用住客账号登录（**手机号：13900000004**，住客身份）
2. 手动在地址栏输入 `/admin/dashboard`
3. **预期结果**：
   - ❌ 被拦截，提示「您没有权限访问该页面」
   - ✅ 自动重定向回 `/guest/home`

#### 2.2 未登录访问拦截
1. 退出登录
2. 手动在地址栏输入 `/staff/rooms`
3. **预期结果**：
   - ❌ 被拦截
   - ✅ 跳转到 `/login?redirect=/staff/rooms`
   - ✅ 登录后自动跳转回目标页面

---

### 场景3：注册默认角色测试

#### 3.1 新用户注册
1. 点击「注册」标签
2. 填写注册信息：
   - 手机号：13900000005
   - 获取验证码（查看后端日志）
   - 填写验证码
   - 设置密码：123456
   - 姓名：测试用户5
   - 身份证号：110101199001011234
3. 点击注册
4. **预期结果**：
   - ✅ 注册成功
   - ✅ 自动切换到登录标签
   - ✅ 手机号自动填充

#### 3.2 验证新账号角色
1. 使用刚注册的账号登录（13900000005 / 123456）
2. **预期结果**：
   - ✅ 自动跳转到 `/guest/home`（默认GUEST角色）
   - ✅ 看到住客端界面

---

## 🔍 后端验证SQL

```sql
-- 查看所有用户及角色
SELECT user_id, username, phone, user_type, status 
FROM tb_user 
ORDER BY user_type, user_id;

-- 查看JWT是否包含userType
-- 登录后解码accessToken（使用 jwt.io）
-- 验证payload包含：
-- {
--   "sub": "1",
--   "userType": "ADMIN",
--   "iat": 1702745123,
--   "exp": 1702831523
-- }
```

---

## 📊 RBAC架构说明

### 路由层级结构

```
/
├── /login (公共)
├── /403 (公共)
├── /admin (ADMIN专用)
│   ├── /dashboard (管理看板)
│   └── ...
├── /staff (STAFF专用)
│   ├── /workbench (工作台)
│   ├── /rooms (房态管理)
│   ├── /hardware (硬件监控) ⭐️ 已从根目录移动
│   └── ...
└── /guest (GUEST专用)
    ├── /home (住客首页)
    ├── /booking (预订房间)
    └── ...
```

### 权限守卫逻辑

```javascript
router.beforeEach((to, from, next) => {
  // 1. 检查是否需要登录
  if (to.meta.requiresAuth && !userStore.isLoggedIn()) {
    return next('/login?redirect=' + to.fullPath)
  }
  
  // 2. 检查角色权限
  if (to.meta.roles && !to.meta.roles.includes(userType)) {
    ElMessage.error('您没有权限访问该页面')
    return next(roleHomePage) // 跳转到对应角色首页
  }
  
  next()
})
```

### 登录流程

```
1. 用户输入账号密码
2. 调用 /api/auth/login
3. 后端返回 LoginResponse（包含accessToken和userType）
4. 前端保存token和userInfo到localStorage
5. 根据userType跳转：
   - ADMIN → /admin/dashboard
   - STAFF → /staff/workbench
   - GUEST → /guest/home
```

---

## ⚠️ 注意事项

1. **硬件监控功能**：已从根目录移动到 `staff/HardwareMonitor.vue`，仅前台员工可访问
2. **二次鉴权功能**：住客端的POS、社交、积分等功能需要入住后绑定房间（roomAuthToken），将在Phase 2实现
3. **清理旧代码**：原 `views/Dashboard.vue` 已被Layout替代，可删除
4. **Token续期**：当前JWT有效期24小时，生产环境建议添加refresh token机制

---

## 🚀 下一步工作

### Phase 1 剩余任务
- **任务1.3**：修复PMS状态机
  - CheckInRecord作为入住记录主体（而非User）
  - 实现房间生命周期：VACANT → OCCUPIED → DIRTY → VACANT
  - 添加「清洁完成」功能

### Phase 2
- 二次鉴权：房间绑定机制（RoomToken）

### Phase 3
- POS系统、社交匹配、积分任务、智能报表

---

**测试完成后请向我汇报结果，确认无误后我们将继续任务1.3！**
