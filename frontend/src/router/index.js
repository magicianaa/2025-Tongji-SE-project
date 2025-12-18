import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const routes = [
  // 公共路由
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '无权访问' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '404' }
  },

  // 管理端路由（ADMIN）
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN'] },
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { 
          title: '管理看板',
          requiresAuth: true,
          roles: ['ADMIN']
        }
      }
    ]
  },

  // 前台管理端路由（STAFF）
  {
    path: '/staff',
    component: () => import('@/layouts/StaffLayout.vue'),
    meta: { requiresAuth: true, roles: ['STAFF'] },
    redirect: '/staff/workbench',
    children: [
      {
        path: 'workbench',
        name: 'StaffWorkbench',
        component: () => import('@/views/staff/Workbench.vue'),
        meta: { 
          title: '前台工作台',
          requiresAuth: true,
          roles: ['STAFF']
        }
      },
      {
        path: 'checkin',
        name: 'StaffCheckIn',
        component: () => import('@/views/staff/CheckIn.vue'),
        meta: { 
          title: '入住登记',
          requiresAuth: true,
          roles: ['STAFF']
        }
      },
      {
        path: 'rooms',
        name: 'StaffRoomManagement',
        component: () => import('@/views/staff/RoomManagement.vue'),
        meta: { 
          title: '房态管理',
          requiresAuth: true,
          roles: ['STAFF']
        }
      },
      {
        path: 'hardware',
        name: 'StaffHardwareMonitor',
        component: () => import('@/views/staff/HardwareMonitor.vue'),
        meta: { 
          title: '硬件监控',
          requiresAuth: true,
          roles: ['STAFF']
        }
      },
      {
        path: 'products',
        name: 'StaffProductManagement',
        component: () => import('@/views/staff/ProductManagement.vue'),
        meta: { 
          title: '商品管理',
          requiresAuth: true,
          roles: ['STAFF']
        }
      },
      {
        path: 'pos-orders',
        name: 'StaffPosOrders',
        component: () => import('@/views/staff/PosOrders.vue'),
        meta: { 
          title: 'POS订单',
          requiresAuth: true,
          roles: ['STAFF']
        }
      },
      {
        path: 'maintenance',
        name: 'StaffMaintenanceTickets',
        component: () => import('@/views/staff/MaintenanceTickets.vue'),
        meta: { 
          title: '维修工单',
          requiresAuth: true,
          roles: ['STAFF']
        }
      },
      {
        path: 'tasks',
        name: 'StaffTaskManagement',
        component: () => import('@/views/staff/TaskManagement.vue'),
        meta: { 
          title: '任务管理',
          requiresAuth: true,
          roles: ['STAFF']
        }
      },
      {
        path: 'points-shop-manage',
        name: 'StaffPointsShopManagement',
        component: () => import('@/views/staff/PointsShopManagement.vue'),
        meta: { 
          title: '积分商城管理',
          requiresAuth: true,
          roles: ['STAFF']
        }
      }
    ]
  },

  // 住客端路由（GUEST）
  {
    path: '/guest',
    component: () => import('@/layouts/GuestLayout.vue'),
    meta: { requiresAuth: true, roles: ['GUEST'] },
    redirect: '/guest/home',
    children: [
      {
        path: 'home',
        name: 'GuestHome',
        component: () => import('@/views/guest/Home.vue'),
        meta: { 
          title: '住客首页',
          requiresAuth: true,
          roles: ['GUEST']
        }
      },
      {
        path: 'booking',
        name: 'GuestBooking',
        component: () => import('@/views/guest/Booking.vue'),
        meta: { 
          title: '预订房间',
          requiresAuth: true,
          roles: ['GUEST']
        }
      },
      {
        path: 'mall',
        name: 'GuestShoppingMall',
        component: () => import('@/views/guest/ShoppingMall.vue'),
        meta: { 
          title: '商城点餐',
          requiresAuth: true,
          roles: ['GUEST']
        }
      },
      {
        path: 'billing',
        name: 'GuestBilling',
        component: () => import('@/views/guest/Billing.vue'),
        meta: { 
          title: '退房结算',
          requiresAuth: true,
          roles: ['GUEST']
        }
      },
      {
        path: 'repair',
        name: 'GuestDeviceRepair',
        component: () => import('@/views/guest/DeviceRepair.vue'),
        meta: { 
          title: '设备报修',
          requiresAuth: true,
          roles: ['GUEST']
        }
      },
      {
        path: 'tasks',
        name: 'GuestTasksAndPoints',
        component: () => import('@/views/guest/TasksAndPoints.vue'),
        meta: { 
          title: '任务积分',
          requiresAuth: true,
          roles: ['GUEST']
        }
      },
      {
        path: 'points-shop',
        name: 'GuestPointsShop',
        component: () => import('@/views/guest/PointsShop.vue'),
        meta: { 
          title: '积分商城',
          requiresAuth: true,
          roles: ['GUEST']
        }
      }
    ]
  },

  // 根路径重定向（根据角色）
  {
    path: '/',
    redirect: () => {
      const userStore = useUserStore()
      const userType = userStore.userInfo?.userType || 'GUEST'
      
      if (userType === 'ADMIN') {
        return '/admin/dashboard'
      } else if (userType === 'STAFF') {
        return '/staff/workbench'
      } else {
        return '/guest/home'
      }
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫 - RBAC鉴权
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 智慧电竞酒店管理系统` : '智慧电竞酒店管理系统'
  
  // 检查路由是否需要登录
  if (to.meta.requiresAuth) {
    if (!userStore.isLoggedIn()) {
      // 未登录，跳转到登录页
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
      return
    }

    // 已登录的住客用户，检查入住状态（仅在首次加载或从登录页跳转时）
    if (userStore.userInfo?.userType === 'GUEST' && !from.name) {
      await userStore.refreshCheckInStatus()
    }

    // 已登录，检查角色权限
    const userType = userStore.userInfo?.userType
    const requiredRoles = to.meta.roles

    if (requiredRoles && requiredRoles.length > 0) {
      if (!requiredRoles.includes(userType)) {
        // 无权限，提示并跳转到403或用户首页
        ElMessage.error('您没有权限访问该页面')
        
        // 跳转到对应角色的首页
        if (userType === 'ADMIN') {
          next('/admin/dashboard')
        } else if (userType === 'STAFF') {
          next('/staff/workbench')
        } else if (userType === 'GUEST') {
          next('/guest/home')
        } else {
          next('/403')
        }
        return
      }
    }

    next()
  } else {
    // 不需要登录的路由，直接放行
    next()
  }
})

export default router
