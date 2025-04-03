<template>
  <div class="room-container">
    <div class="page-header">
      <h1>会议室列表</h1>
      <div class="header-description">查看和管理所有会议室</div>
    </div>

    <el-card class="filter-card">
      <div class="toolbar">
        <el-input
          v-model="searchParams.name"
          placeholder="会议室名称"
          clearable
          style="width: 200px"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-input
          v-model="searchParams.location"
          placeholder="会议室位置"
          clearable
          style="width: 200px"
        >
          <template #prefix>
            <el-icon><Location /></el-icon>
          </template>
        </el-input>
        <el-select v-model="searchParams.capacity" placeholder="容纳人数" clearable style="width: 200px">
          <el-option label=">10人" :value="10" />
          <el-option label=">20人" :value="20" />
          <el-option label=">30人" :value="30" />
          <el-option label=">50人" :value="50" />
          <el-option label=">100人" :value="100" />
        </el-select>
        <el-select v-model="searchParams.status" placeholder="会议室状态" clearable style="width: 200px">
          <el-option label="可用" :value="1" />
          <el-option label="维护中" :value="0" />
        </el-select>
        <div class="button-group">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button v-permission-button="'add'" type="success" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加会议室
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="data-card">
      <div class="card-header">
        <span class="card-title">会议室管理</span>
        <span class="card-subtitle">共 {{ total }} 个会议室</span>
      </div>

      <el-table 
        :data="roomList" 
        border 
        stripe 
        v-loading="loading"
        :header-cell-style="{ 
          background: '#f5f7fa', 
          color: '#606266', 
          fontWeight: 'bold' 
        }"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="名称" align="center">
          <template #header>
            <div class="header-with-icon">
              <el-icon><House /></el-icon>
              <span>名称</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="位置" align="center">
          <template #header>
            <div class="header-with-icon">
              <el-icon><Location /></el-icon>
              <span>位置</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="capacity" label="容纳人数" align="center">
          <template #header>
            <div class="header-with-icon">
              <el-icon><UserFilled /></el-icon>
              <span>容纳人数</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #header>
            <div class="header-with-icon">
              <el-icon><InfoFilled /></el-icon>
              <span>状态</span>
            </div>
          </template>
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="light">
              {{ row.status === 1 ? '可用' : '维护中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" align="center">
          <template #header>
            <div class="header-with-icon">
              <el-icon><Setting /></el-icon>
              <span>操作</span>
            </div>
          </template>
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" size="small" @click="handleView(row)">
                <el-icon><View /></el-icon>
                查看
              </el-button>
              <el-button v-permission-button="'edit'" type="warning" size="small" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button v-permission-operation="'delete'" type="danger" size="small" @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
              <el-button type="success" size="small" @click="navigateToBooking(row)" :disabled="row.status !== 1">
                <el-icon><Calendar /></el-icon>
                预约
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:currentPage="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        class="pagination"
        background
      />
    </el-card>

    <!-- 会议室详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="会议室详情"
      width="800px"
      destroy-on-close
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="会议室名称">{{ selectedRoom.name }}</el-descriptions-item>
        <el-descriptions-item label="位置">{{ selectedRoom.location }}</el-descriptions-item>
        <el-descriptions-item label="容纳人数">{{ selectedRoom.capacity }}人</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="selectedRoom.status === 1 ? 'success' : 'danger'">
            {{ selectedRoom.status === 1 ? '可用' : '维护中' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="设备" :span="2">{{ selectedRoom.equipment }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ selectedRoom.description }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ formatDateTime(selectedRoom.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间" :span="2">{{ formatDateTime(selectedRoom.updateTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 添加/编辑会议室对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
    >
      <el-form :model="roomForm" :rules="roomRules" ref="roomFormRef" label-width="100px">
        <el-form-item label="会议室名称" prop="name">
          <el-input v-model="roomForm.name" />
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="roomForm.location" />
        </el-form-item>
        <el-form-item label="容纳人数" prop="capacity">
          <el-input-number v-model="roomForm.capacity" :min="1" />
        </el-form-item>
        <el-form-item label="设备" prop="equipment">
          <el-input v-model="roomForm.equipment" type="textarea" placeholder="请输入会议室设备，如投影仪、白板等" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="roomForm.description" type="textarea" placeholder="请输入会议室描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="roomForm.status">
            <el-radio :label="1">可用</el-radio>
            <el-radio :label="0">维护中</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRoomForm" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { usePermissionStore } from '@/stores/permission';
import { useUserStore } from '@/stores/user';
import { getRoomList, addRoom, updateRoom, deleteRoom, getRoomDetail } from '@/api/room';
import { 
  Search, 
  Location, 
  UserFilled, 
  Calendar, 
  Refresh, 
  Plus, 
  House, 
  InfoFilled, 
  Setting, 
  View, 
  Edit, 
  Delete 
} from '@element-plus/icons-vue';

export default {
  name: 'RoomManagementPage',
  components: {
    Search,
    Location,
    UserFilled,
    Calendar,
    Refresh,
    Plus,
    House,
    InfoFilled,
    Setting,
    View,
    Edit,
    Delete
  },
  setup() {
    const router = useRouter();
    const permissionStore = usePermissionStore();
    const userStore = useUserStore();
    const loading = ref(false);
    const currentPage = ref(1);
    const pageSize = ref(10);
    const total = ref(0);
    const roomList = ref([]);
    const searchParams = reactive({
      name: '',
      location: '',
      capacity: '',
      status: ''
    });

    // 会议室详情相关
    const detailVisible = ref(false);
    const selectedRoom = ref(null);
    
    // 添加/编辑对话框相关
    const dialogVisible = ref(false);
    const dialogType = ref('add');
    const dialogTitle = computed(() => dialogType.value === 'add' ? '添加会议室' : '编辑会议室');
    const submitLoading = ref(false);
    const roomFormRef = ref(null);
    
    const roomForm = reactive({
      id: null,
      name: '',
      location: '',
      capacity: 10,
      equipment: '',
      description: '',
      status: 1
    });
    
    const roomRules = {
      name: [
        { required: true, message: '请输入会议室名称', trigger: 'blur' },
        { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
      ],
      location: [
        { required: true, message: '请输入位置', trigger: 'blur' }
      ],
      capacity: [
        { required: true, message: '请输入容纳人数', trigger: 'blur' },
        { type: 'number', min: 1, message: '容纳人数必须大于0', trigger: 'blur' }
      ],
      status: [
        { required: true, message: '请选择状态', trigger: 'change' }
      ]
    };

    // 加载会议室列表
    const loadRoomList = async () => {
      try {
        loading.value = true;
        const params = {
          page: currentPage.value,
          pageSize: pageSize.value,
          name: searchParams.name || undefined,
          location: searchParams.location || undefined,
          capacity: searchParams.capacity || undefined,
          status: searchParams.status !== '' ? searchParams.status : undefined
        };
        console.log('查询参数:', params);
        const res = await getRoomList(params);
        if (res.code === 1) {
          const { rows, total: totalCount } = res.data;
          roomList.value = rows;
          total.value = totalCount;
        } else {
          ElMessage.error(res.msg || '获取会议室列表失败');
        }
      } catch (error) {
        console.error('获取会议室列表失败:', error);
        ElMessage.error('获取会议室列表失败');
      } finally {
        loading.value = false;
      }
    };

    // 搜索
    const handleSearch = () => {
      currentPage.value = 1;
      loadRoomList();
    };
    
    // 重置搜索
    const resetSearch = () => {
      Object.keys(searchParams).forEach(key => {
        searchParams[key] = '';
      });
      currentPage.value = 1;
      loadRoomList();
    };

    // 分页
    const handleSizeChange = (size) => {
      pageSize.value = size;
      loadRoomList();
    };
    
    const handleCurrentChange = (page) => {
      currentPage.value = page;
      loadRoomList();
    };

    // 添加会议室
    const handleAdd = () => {
      dialogType.value = 'add';
      roomForm.id = null;
      roomForm.name = '';
      roomForm.location = '';
      roomForm.capacity = 10;
      roomForm.equipment = '';
      roomForm.description = '';
      roomForm.status = 1;
      dialogVisible.value = true;
    };

    // 编辑会议室
    const handleEdit = (row) => {
      dialogType.value = 'edit';
      roomForm.id = row.id;
      roomForm.name = row.name;
      roomForm.location = row.location;
      roomForm.capacity = row.capacity;
      roomForm.equipment = row.equipment || '';
      roomForm.description = row.description || '';
      roomForm.status = row.status;
      dialogVisible.value = true;
    };

    // 查看会议室详情
    const handleView = async (row) => {
      try {
        loading.value = true;
        // 调用API获取详细信息
        const res = await getRoomDetail(row.id);
        if (res.code === 1) {
          selectedRoom.value = res.data;
          detailVisible.value = true;
        } else {
          ElMessage.error(res.msg || '获取会议室详情失败');
        }
      } catch (error) {
        console.error('获取会议室详情失败:', error);
        ElMessage.error('获取会议室详情失败');
      } finally {
        loading.value = false;
      }
    };
    
    // 删除会议室
    const handleDelete = (row) => {
      ElMessageBox.confirm('确定要删除该会议室吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          loading.value = true;
          const res = await deleteRoom(row.id);
          if (res.code === 1) {
            ElMessage.success('删除成功');
            loadRoomList();
          } else {
            ElMessage.error(res.msg || '删除失败');
          }
        } catch (error) {
          console.error('删除失败:', error);
          ElMessage.error('删除失败');
        } finally {
          loading.value = false;
        }
      }).catch(() => {});
    };

    // 提交会议室表单
    const submitRoomForm = async () => {
      try {
        await roomFormRef.value.validate();
        submitLoading.value = true;
        let res;
        if (dialogType.value === 'add') {
          res = await addRoom(roomForm);
        } else {
          res = await updateRoom(roomForm.id, roomForm);
        }
        if (res.code === 1) {
          ElMessage.success(dialogType.value === 'add' ? '添加成功' : '更新成功');
          dialogVisible.value = false;
          loadRoomList();
        } else {
          ElMessage.error(res.msg || (dialogType.value === 'add' ? '添加失败' : '更新失败'));
        }
      } catch (error) {
        console.error(dialogType.value === 'add' ? '添加失败:' : '更新失败:', error);
        ElMessage.error(dialogType.value === 'add' ? '添加失败' : '更新失败');
      } finally {
        submitLoading.value = false;
      }
    };
    
    // 跳转到预约页面
    const navigateToBooking = (row) => {
      router.push({
        path: '/room/booking',
        query: { 
          roomId: row.id, 
          roomName: row.name 
        }
      });
    };

    // 格式化日期时间
    const formatDateTime = (date) => {
      if (!date) return '';
      date = new Date(date);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`;
    };
    
    // 当前用户角色
    const userRole = computed(() => {
      return userStore.userInfo?.roleId || 0;
    });
    
    onMounted(() => {
      loadRoomList();
    });
    
    return {
      permissionStore,
      userRole,
      loading,
      searchParams,
      roomList,
      currentPage,
      pageSize,
      total,
      dialogVisible,
      dialogType,
      dialogTitle,
      submitLoading,
      roomForm,
      roomRules,
      roomFormRef,
      detailVisible,
      selectedRoom,
      handleSearch,
      handleAdd,
      handleEdit,
      handleDelete,
      handleView,
      navigateToBooking,
      submitRoomForm,
      handleSizeChange,
      handleCurrentChange,
      resetSearch,
      formatDateTime
    };
  }
};
</script>

<style scoped>
.room-container {
  padding: 20px;
  width: 100%;
  box-sizing: border-box;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  font-size: 24px;
  color: #303133;
  margin-bottom: 8px;
}

.header-description {
  color: #909399;
  font-size: 14px;
}

.filter-card {
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.data-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.card-subtitle {
  font-size: 13px;
  color: #909399;
}

.toolbar {
  margin-bottom: 20px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.button-group {
  display: flex;
  gap: 10px;
  margin-left: auto;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  padding: 15px;
}

.header-with-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}

.action-buttons {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
  justify-content: center;
}

.action-buttons .el-button {
  margin: 0;
}

@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .button-group {
    margin-left: 0;
    justify-content: flex-end;
  }
}
</style> 