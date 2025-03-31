<template>
  <div class="room-container">
    <h1>会议室列表</h1>
    <div class="toolbar">
      <el-input
        v-model="searchParams.name"
        placeholder="会议室名称"
        clearable
        style="width: 200px"
      />
      <el-input
        v-model="searchParams.location"
        placeholder="会议室位置"
        clearable
        style="width: 200px"
      />
      <el-select v-model="searchParams.capacity" placeholder="容纳人数" clearable style="width: 200px">
        <el-option label="10人以下" :value="10" />
        <el-option label="10-20人" :value="20" />
        <el-option label="20-50人" :value="50" />
        <el-option label="50人以上" :value="100" />
      </el-select>
      <el-select v-model="searchParams.status" placeholder="会议室状态" clearable style="width: 200px">
        <el-option label="可用" :value="1" />
        <el-option label="维护中" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
      <el-button v-permission-button="'add'" type="success" @click="handleAdd">添加会议室</el-button>
    </div>

    <el-table :data="roomList" border stripe v-loading="loading">
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="location" label="位置" />
      <el-table-column prop="capacity" label="容纳人数" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '可用' : '维护中' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250">
        <template #default="{ row }">
          <el-button type="primary" @click="handleView(row)">查看</el-button>
          <el-button v-permission-button="'edit'" type="warning" @click="handleEdit(row)">编辑</el-button>
          <el-button v-permission-operation="'delete'" type="danger" @click="handleDelete(row)">删除</el-button>
          <el-button type="success" @click="navigateToBooking(row)" :disabled="row.status !== 1">预约</el-button>
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
    />

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
        <el-form-item label="设备" prop="facilities">
          <el-input v-model="roomForm.facilities" type="textarea" />
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

export default {
  name: 'RoomManagementPage',
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
      facilities: '',
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
      roomForm.facilities = '';
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
      roomForm.facilities = row.facilities || '';
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
}

.toolbar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.el-pagination {
  margin-top: 20px;
  justify-content: center;
}
</style> 