// 角色ID定义
export const ROLES = {
  ADMIN: 1,
  APPROVER: 2,
  USER: 3
}

// 操作权限定义
export const OPERATIONS = {
  CREATE: 'create',
  UPDATE: 'update',
  DELETE: 'delete',
  READ: 'read'
}

// 按钮权限定义
export const BUTTONS = {
  ADD: 'add',
  EDIT: 'edit',
  DELETE: 'delete',
  VIEW: 'view',
  BOOK: 'book'
}

// 权限配置
export const PERMISSIONS = {
  [ROLES.ADMIN]: {
    buttons: [BUTTONS.ADD, BUTTONS.EDIT, BUTTONS.DELETE, BUTTONS.VIEW, BUTTONS.BOOK],
    operations: [OPERATIONS.CREATE, OPERATIONS.UPDATE, OPERATIONS.DELETE, OPERATIONS.READ]
  },
  [ROLES.APPROVER]: {
    buttons: [BUTTONS.VIEW, BUTTONS.BOOK],
    operations: [OPERATIONS.READ]
  },
  [ROLES.USER]: {
    buttons: [BUTTONS.VIEW, BUTTONS.BOOK],
    operations: [OPERATIONS.READ]
  }
} 