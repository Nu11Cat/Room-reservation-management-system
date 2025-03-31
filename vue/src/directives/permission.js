import { checkButton, checkOperation } from '@/services/permissionService'

// 按钮权限指令
export const vButton = {
  mounted(el, binding) {
    const { value } = binding
    if (value && !checkButton(value)) {
      el.parentNode?.removeChild(el)
    }
  }
}

// 操作权限指令
export const vOperation = {
  mounted(el, binding) {
    const { value } = binding
    if (value && !checkOperation(value)) {
      el.parentNode?.removeChild(el)
    }
  }
} 