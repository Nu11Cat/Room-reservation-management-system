<template>
  <div class="main-layout">
    <el-container class="layout-container">
      <!-- 顶部导航栏 -->
      <el-header height="60px" class="app-header">
        <div class="header-logo">
          <img src="@/assets/logo.png" alt="Logo" class="logo-img" v-if="false" />
          <h2>{{ systemName }}</h2>
        </div>
        <div class="header-menu">
          <NotificationIcon />
          <div class="divider"></div>
          <el-dropdown @command="handleCommand" trigger="click">
            <span class="user-dropdown">
              <el-avatar :size="32" class="user-avatar">{{ userInfo.username ? userInfo.username.substring(0, 1).toUpperCase() : 'U' }}</el-avatar>
              <span class="username">{{ userInfo.username || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  <span>个人中心</span>
                </el-dropdown-item>
                <el-dropdown-item command="about">
                  <el-icon><InfoFilled /></el-icon>
                  <span>关于系统</span>
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-container>
        <!-- 侧边栏导航 -->
        <el-aside width="220px" class="app-sidebar">
          <el-menu
            :default-active="activeMenu"
            class="sidebar-menu"
            router
            :collapse="isCollapse"
            background-color="#304156"
            text-color="#bfcbd9"
            active-text-color="#409EFF"
          >
            <!-- 管理员菜单 -->
            <el-sub-menu index="/system" v-if="hasPermission([1])">
              <template #title>
                <el-icon><Setting /></el-icon>
                <span>系统管理</span>
              </template>
              <el-menu-item index="/system/user">
                <el-icon><User /></el-icon>
                <span>用户管理</span>
              </el-menu-item>
              <el-menu-item index="/system/config">
                <el-icon><Tools /></el-icon>
                <span>系统配置</span>
              </el-menu-item>
              <el-menu-item index="/system/logs">
                <el-icon><Document /></el-icon>
                <span>操作日志</span>
              </el-menu-item>
              <el-menu-item index="/system/permission">
                <el-icon><Lock /></el-icon>
                <span>权限管理</span>
              </el-menu-item>
            </el-sub-menu>
            
            <!-- 会议室相关 -->
            <el-sub-menu index="/room">
              <template #title>
                <el-icon><OfficeBuilding /></el-icon>
                <span>会议室管理</span>
              </template>
              <el-menu-item index="/room/booking">
                <el-icon><Notebook /></el-icon>
                <span>会议室预订</span>
              </el-menu-item>
              <el-menu-item index="/room/list">
                <el-icon><List /></el-icon>
                <span>会议室列表</span>
              </el-menu-item>
            </el-sub-menu>
            
            <!-- 预订相关 -->
            <el-sub-menu index="/booking">
              <template #title>
                <el-icon><Calendar /></el-icon>
                <span>预订管理</span>
              </template>
              <el-menu-item index="/booking/list">
                <el-icon><Tickets /></el-icon>
                <span>预订列表</span>
              </el-menu-item>
              <el-menu-item index="/booking/calendar">
                <el-icon><Calendar /></el-icon>
                <span>日历视图</span>
              </el-menu-item>
              <el-menu-item index="/admin/bookings" v-if="hasPermission([1])">
                <el-icon><Management /></el-icon>
                <span>全部预约管理</span>
              </el-menu-item>
            </el-sub-menu>
            
            <!-- 审批相关 -->
            <el-menu-item index="/approval/list" v-if="hasPermission([1, 3])">
              <el-icon><Checked /></el-icon>
              <template #title>审批管理</template>
            </el-menu-item>
            
            <!-- 通知相关 -->
            <el-menu-item index="/notification/list">
              <el-icon><Bell /></el-icon>
              <template #title>通知中心</template>
            </el-menu-item>
            
            <!-- 特殊时间段管理 -->
            <el-menu-item index="/special-time" v-if="hasPermission([1])">
              <el-icon><Timer /></el-icon>
              <span>特殊时间段管理</span>
            </el-menu-item>
            
            <!-- 统计分析 -->
            <el-menu-item index="/statistics" v-if="hasPermission([1])">
              <el-icon><TrendCharts /></el-icon>
              <span>统计分析</span>
            </el-menu-item>

            <!-- 评价管理 -->
            <el-sub-menu index="/review">
              <template #title>
                <el-icon><ChatLineSquare /></el-icon>
                <span>评价管理</span>
              </template>
              <el-menu-item index="/review/list">
                <el-icon><ChatDotSquare /></el-icon>
                <span>我的评价</span>
              </el-menu-item>
              <el-menu-item index="/review/admin" v-if="hasPermission([1])">
                <el-icon><ChatSquare /></el-icon>
                <span>评价管理</span>
              </el-menu-item>
            </el-sub-menu>
            
            <!-- 个人中心 -->
            <el-menu-item index="/profile">
              <el-icon><User /></el-icon>
              <template #title>个人中心</template>
            </el-menu-item>
          </el-menu>
          
          <!-- 折叠按钮 -->
          <div class="collapse-btn" @click="toggleCollapse">
            <el-icon v-if="!isCollapse"><DArrowLeft /></el-icon>
            <el-icon v-else><DArrowRight /></el-icon>
          </div>
        </el-aside>
        
        <!-- 主内容区域 -->
        <el-main>
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import { ref, computed, onMounted, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessageBox } from 'element-plus';
import { 
  ArrowDown, 
  User, 
  OfficeBuilding, 
  Calendar, 
  Checked, 
  Bell, 
  Setting, 
  Tools, 
  Management, 
  TrendCharts, 
  Document, 
  Lock, 
  SwitchButton,
  DArrowLeft,
  DArrowRight,
  Notebook,
  List,
  Tickets,
  InfoFilled,
  Timer,
  ChatLineSquare,
  ChatDotSquare,
  ChatSquare
} from '@element-plus/icons-vue';
import { useUserStore } from '@/stores/user';
import { useConfigStore } from '@/stores/config';
import NotificationIcon from '@/components/NotificationIcon.vue';
import { getUnreadCount } from '@/api/notification';

export default {
  name: 'MainLayout',
  components: {
    ArrowDown,
    User,
    OfficeBuilding,
    Calendar,
    Checked,
    Bell,
    Setting,
    Tools,
    NotificationIcon,
    Management,
    TrendCharts,
    Document,
    Lock,
    SwitchButton,
    DArrowLeft,
    DArrowRight,
    Notebook,
    List,
    Tickets,
    InfoFilled,
    Timer,
    ChatLineSquare,
    ChatDotSquare,
    ChatSquare
  },
  setup() {
    const router = useRouter();
    const userStore = useUserStore();
    const configStore = useConfigStore();
    const isCollapse = ref(false);
    
    const userInfo = computed(() => userStore.userInfo || {});
    
    const activeMenu = computed(() => {
      const route = router.currentRoute.value;
      return route.path;
    });
    
    // 获取系统名称，如果未配置则使用默认值
    const systemName = computed(() => {
      return configStore.systemName || '会议室预约管理系统';
    });
    
    // 确保配置已初始化
    if (!configStore.initialized) {
      configStore.initialize();
    }
    
    // 切换折叠状态
    const toggleCollapse = () => {
      isCollapse.value = !isCollapse.value;
    };
    
    // 检查用户是否有权限
    const hasPermission = (roles) => {
      const userRole = userInfo.value.roleId;
      return roles.includes(userRole);
    };
    
    // 下拉菜单命令处理
    const handleCommand = (command) => {
      if (command === 'profile') {
        router.push('/profile');
      } else if (command === 'about') {
        window.open('https://github.com/Nu11Cat/Room-reservation-management-system', '_blank');
      } else if (command === 'logout') {
        ElMessageBox.confirm('确定要退出登录吗?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          userStore.logout();
          router.push('/login');
        }).catch(() => {});
      }
    };
    
    // 获取未读通知数量
    const updateUnreadCount = async () => {
      try {
        const res = await getUnreadCount();
        if (res.code === 1) {
          userStore.setUnreadCount(res.data);
        }
      } catch (error) {
        console.error('获取未读通知数量失败:', error);
      }
    };
    
    onMounted(async () => {
      // 延迟执行，减少ResizeObserver错误
      await nextTick();
      
      // 获取未读通知数量
      updateUnreadCount();
    });
    
    return {
      isCollapse,
      userInfo,
      activeMenu,
      hasPermission,
      handleCommand,
      toggleCollapse,
      systemName
    };
  }
};
</script>

<style scoped>
.main-layout {
  height: 100vh;
  width: 100%;
}

.layout-container {
  height: 100%;
  will-change: transform;
  transform: translateZ(0);
}

.app-header {
  background: linear-gradient(90deg, #304156 0%, #2c5282 100%);
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  position: relative;
  z-index: 10;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-logo {
  display: flex;
  align-items: center;
}

.logo-img {
  height: 36px;
  margin-right: 10px;
}

.header-logo h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.header-menu {
  display: flex;
  align-items: center;
  gap: 15px;
}

.divider {
  height: 24px;
  width: 1px;
  background-color: rgba(255, 255, 255, 0.3);
  margin: 0 5px;
}

.user-dropdown {
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 8px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-dropdown:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.user-avatar {
  background-color: #409EFF;
  color: white;
  font-weight: bold;
}

.username {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-sidebar {
  background-color: #304156;
  color: #bfcbd9;
  transition: all 0.3s;
  position: relative;
  z-index: 5;
  will-change: transform;
  box-shadow: 2px 0 6px rgba(0, 0, 0, 0.1);
}

.sidebar-menu {
  height: 100%;
  border-right: none;
  overflow-y: auto;
  overflow-x: hidden;
}

.sidebar-menu::-webkit-scrollbar {
  width: 6px;
}

.sidebar-menu::-webkit-scrollbar-thumb {
  background-color: rgba(144, 147, 153, 0.3);
  border-radius: 3px;
}

.sidebar-menu::-webkit-scrollbar-track {
  background-color: transparent;
}

.collapse-btn {
  position: absolute;
  bottom: 20px;
  right: 10px;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.1);
  color: #bfcbd9;
  border-radius: 50%;
  cursor: pointer;
  z-index: 10;
  transition: all 0.3s;
}

.collapse-btn:hover {
  background-color: rgba(255, 255, 255, 0.2);
  color: #409EFF;
}

.el-menu-item.is-active {
  background-color: #263445 !important;
  border-left: 4px solid #409EFF;
  padding-left: 16px !important;
}

.el-sub-menu.is-active > .el-sub-menu__title {
  color: #409EFF !important;
}

.el-menu-item:hover, .el-sub-menu__title:hover {
  background-color: #263445 !important;
}

.el-main {
  background-color: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
  position: relative;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 添加响应式样式 */
@media (max-width: 768px) {
  .el-aside {
    width: 64px !important;
  }
  
  .el-menu--collapse {
    width: 64px !important;
  }
  
  .header-logo h2 {
    display: none;
  }
  
  .username {
    display: none;
  }
}
</style>