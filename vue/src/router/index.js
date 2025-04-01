import { createRouter, createWebHistory } from 'vue-router';
import { useUserStore } from '@/stores/user';

const routes = [
  {
    path: '/',
    redirect: () => {
      const userStore = useUserStore();
      return userStore.token ? '/room/booking' : '/login';
    }
  },
  {
    path: '/login',
    name: 'UserLogin',
    component: () => import('@/components/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'UserRegister',
    component: () => import('@/components/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/dashboard',
    component: () => import('@/components/MainLayout.vue'),
    redirect: '/room/booking',
    meta: { requiresAuth: true },
    children: [
      {
        path: '/system/user',
        name: 'UserManagement',
        component: () => import('@/components/UserManagement.vue'),
        meta: {
          requiresAuth: true,
          roles: [1] // 只有管理员可访问
        }
      },
      {
        path: '/system/config',
        name: 'SystemConfig',
        component: () => import('@/components/SystemConfig.vue'),
        meta: {
          requiresAuth: true,
          roles: [1] // 仅管理员可访问
        }
      },
      {
        path: '/room/list',
        name: 'RoomManagement',
        component: () => import('@/components/RoomManagement.vue'),
        meta: {
          requiresAuth: true,
          roles: [1, 2, 3] // 所有角色都可访问
        }
      },
      {
        path: '/room/booking',
        name: 'RoomBooking',
        component: () => import('@/components/RoomBooking.vue'),
        meta: {
          requiresAuth: true,
          roles: [1, 2, 3] // 所有角色都可以预订会议室
        }
      },
      {
        path: '/booking',
        component: () => import('@/views/booking/Index.vue'),
        name: 'Booking',
        meta: { 
          title: '会议室预订', 
          icon: 'calendar',
          requiresAuth: true,
          roles: [1, 2, 3]
        }
      },
      {
        path: '/booking/list',
        component: () => import('@/views/booking/List.vue'),
        name: 'BookingList',
        meta: { 
          title: '我的预订', 
          icon: 'list',
          requiresAuth: true,
          roles: [1, 2, 3]
        }
      },
      {
        path: '/booking/calendar',
        name: 'BookingCalendar',
        component: () => import('@/components/BookingCalendar.vue'),
        meta: {
          requiresAuth: true,
          roles: [1, 2, 3] // 所有角色都可访问
        }
      },
      {
        path: '/approval/list',
        name: 'ApprovalManagement',
        component: () => import('@/views/approval/List.vue'),
        meta: {
          requiresAuth: true,
          roles: [1, 3] // 管理员和审批人可访问
        }
      },
      {
        path: '/notification/list',
        name: 'NotificationManagement',
        component: () => import('@/views/notification/List.vue'),
        meta: {
          requiresAuth: true,
          roles: [1, 2, 3] // 所有角色都可访问
        }
      },
      {
        path: '/admin/bookings',
        name: 'AdminBookingManagement',
        component: () => import('@/components/AdminBookingManagement.vue'),
        meta: {
          requiresAuth: true,
          roles: [1] // 只有管理员可访问
        }
      },
      {
        path: '/statistics',
        name: 'StatisticsPage',
        component: () => import('@/components/StatisticsPage.vue'),
        meta: {
          requiresAuth: true,
          roles: [1] // 只有最高管理员可访问
        }
      },
      {
        path: '/profile',
        name: 'UserProfile',
        component: () => import('@/views/profile/Index.vue'),
        meta: {
          requiresAuth: true,
          roles: [1, 2, 3] // 所有角色都可访问
        }
      }
    ]
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/components/403.vue'),
    meta: { requiresAuth: false }
  }
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
});

// 路由守卫
router.beforeEach((to, from, next) => {
  // 在进入登录页时清除本地存储，避免缓存问题
  if (to.path === '/login') {
    localStorage.removeItem('token');
    localStorage.removeItem('userInfo');
    localStorage.removeItem('userSettings');
  }
  
  // 检查路由是否需要登录
  if (to.matched.some(record => record.meta.requiresAuth)) {
    // 检查用户是否已登录
    const token = localStorage.getItem('token');
    if (!token) {
      // 未登录，重定向到登录页
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      });
    } else {
      // 已登录，检查角色权限
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
      const userRole = userInfo.roleId;
      
      // 如果路由指定了访问角色
      if (to.meta.roles && to.meta.roles.length > 0) {
        // 检查用户角色是否符合要求
        if (to.meta.roles.includes(userRole)) {
          next(); // 允许访问
        } else {
          next('/403'); // 无权限访问
        }
      } else {
        next(); // 没有指定角色限制，允许访问
      }
    }
  } else {
    next(); // 不需要登录的路由，允许访问
  }
});

export default router; 