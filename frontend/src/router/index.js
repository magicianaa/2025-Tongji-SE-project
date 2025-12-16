import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/views/Dashboard.vue'),
    meta: { requiresAuth: true },
    redirect: '/dashboard',
    children: [
      {
        path: '/dashboard',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { 
          title: '控制台',
          requiresAuth: true 
        }
      },
      {
        path: '/rooms',
        name: 'RoomManagement',
        component: () => import('@/views/RoomManagement.vue'),
        meta: { 
          title: '房态管理',
          requiresAuth: true 
        }
      },
      {
        path: '/hardware',
        name: 'HardwareMonitor',
        component: () => import('@/views/HardwareMonitor.vue'),
        meta: { 
          title: '硬件监控',
          requiresAuth: true 
        }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '404' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 智慧电竞酒店管理系统` : '智慧电竞酒店管理系统'
  
  // 检查路由是否需要登录
  if (to.meta.requiresAuth) {
    if (userStore.isLoggedIn()) {
      next()
    } else {
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
    }
  } else {
    next()
  }
})

export default router
