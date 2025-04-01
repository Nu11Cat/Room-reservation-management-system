<template>
  <div class="main-layout">
    <el-container class="layout-container">
      <!-- 顶部导航栏 -->
      <el-header height="60px">
        <div class="header-logo">
          <h2>会议室预订系统</h2>
        </div>
        <div class="header-menu">
          <NotificationIcon />
          <el-dropdown @command="handleCommand">
            <span class="user-dropdown">
              {{ userInfo.username || '用户' }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-container>
        <!-- 侧边栏导航 -->
        <el-aside width="220px">
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
            </el-sub-menu>
            
            <!-- 会议室相关 -->
            <el-sub-menu index="/room">
              <template #title>
                <el-icon><OfficeBuilding /></el-icon>
                <span>会议室管理</span>
              </template>
              <el-menu-item index="/room/booking">会议室预订</el-menu-item>
              <el-menu-item index="/room/list">会议室列表</el-menu-item>
            </el-sub-menu>
            
            <!-- 预订相关 -->
            <el-sub-menu index="/booking">
              <template #title>
                <el-icon><Calendar /></el-icon>
                <span>预订管理</span>
              </template>
              <el-menu-item index="/booking/list">预订列表</el-menu-item>
              <el-menu-item index="/booking/calendar">日历视图</el-menu-item>
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
            
            <!-- 个人中心 -->
            <el-menu-item index="/profile">
              <el-icon><Setting /></el-icon>
              <template #title>个人中心</template>
            </el-menu-item>
            
            <!-- 统计分析 -->
            <el-menu-item index="/statistics" v-if="hasPermission([1])">
              <el-icon><TrendCharts /></el-icon>
              <span>统计分析</span>
            </el-menu-item>
          </el-menu>
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
import { ArrowDown, User, OfficeBuilding, Calendar, Checked, Bell, Setting, Tools, Management, TrendCharts } from '@element-plus/icons-vue';
import { useUserStore } from '@/stores/user';
import NotificationIcon from '@/components/NotificationIcon.vue';

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
    TrendCharts
  },
  setup() {
    const router = useRouter();
    const userStore = useUserStore();
    const isCollapse = ref(false);
    
    const userInfo = computed(() => userStore.userInfo || {});
    
    const activeMenu = computed(() => {
      const route = router.currentRoute.value;
      return route.path;
    });
    
    // 检查用户是否有权限
    const hasPermission = (roles) => {
      const userRole = userInfo.value.roleId;
      return roles.includes(userRole);
    };
    
    // 下拉菜单命令处理
    const handleCommand = (command) => {
      if (command === 'profile') {
        router.push('/profile');
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
    
    onMounted(async () => {
      // 延迟执行，减少ResizeObserver错误
      await nextTick();
    });
    
    return {
      isCollapse,
      userInfo,
      activeMenu,
      hasPermission,
      handleCommand
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

.el-header {
  background-color: #304156;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  position: relative;
  z-index: 10;
}

.header-logo {
  display: flex;
  align-items: center;
}

.header-logo h2 {
  margin: 0;
  font-size: 18px;
}

.header-menu {
  display: flex;
  align-items: center;
}

.user-dropdown {
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
}

.el-aside {
  background-color: #304156;
  color: #bfcbd9;
  transition: width 0.3s;
  position: relative;
  z-index: 5;
  will-change: transform;
}

.sidebar-menu {
  height: 100%;
  border-right: none;
  overflow-y: auto;
  overflow-x: hidden;
}

.el-main {
  background-color: #f0f2f5;
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
</style> 