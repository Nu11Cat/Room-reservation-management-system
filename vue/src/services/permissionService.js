import { useUserStore } from '@/stores/user'
import { PERMISSIONS, OPERATIONS, BUTTONS } from '@/config/permissions'

// 检查操作权限
export const checkOperation = (operation) => {
  const userStore = useUserStore()
  const userRole = userStore.userInfo?.roleId
  return PERMISSIONS[userRole]?.operations.includes(operation)
}

// 检查按钮权限
export const checkButton = (button) => {
  const userStore = useUserStore()
  const userRole = userStore.userInfo?.roleId
  return PERMISSIONS[userRole]?.buttons.includes(button)
}

// 检查是否为管理员
export const isAdmin = () => {
  const userStore = useUserStore()
  return userStore.userInfo?.roleId === 1
}

// 导出权限常量
export { OPERATIONS, BUTTONS } 