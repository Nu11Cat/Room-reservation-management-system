<template>
  <div class="forbidden-container">
    <el-result
      icon="warning"
      title="403 Forbidden"
      sub-title="抱歉，您没有权限访问此页面"
    >
      <template #extra>
        <el-button type="primary" @click="$router.push('/room')">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script>
export default {
  name: 'ForbiddenPage'
}
</script>

<style scoped>
.forbidden-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style> 