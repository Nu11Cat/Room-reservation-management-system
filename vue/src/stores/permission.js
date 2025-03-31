import { defineStore } from 'pinia';

// 定义角色对应的权限
const rolePermissions = {
  1: { // 超级管理员
    menus: ['system', 'room', 'booking', 'approval', 'notification'],
    buttons: ['add', 'edit', 'delete', 'view', 'approve']
  },
  2: { // 普通用户
    menus: ['room', 'booking', 'notification'],
    buttons: ['view', 'book']
  },
  3: { // 审批人
    menus: ['room', 'booking', 'approval', 'notification'],
    buttons: ['view', 'approve']
  }
};

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    roleId: null,
    menus: [],
    buttons: []
  }),

  getters: {
    hasMenu: (state) => (menu) => {
      const permissions = rolePermissions[state.roleId];
      return permissions ? permissions.menus.includes(menu) : false;
    },
    
    hasButton: (state) => (button) => {
      const permissions = rolePermissions[state.roleId];
      return permissions ? permissions.buttons.includes(button) : false;
    }
  },

  actions: {
    // 设置角色ID
    setRoleId(roleId) {
      this.roleId = roleId;
      const permissions = rolePermissions[roleId];
      if (permissions) {
        this.menus = permissions.menus;
        this.buttons = permissions.buttons;
      }
    },

    // 清除权限信息
    clearPermissions() {
      this.roleId = null;
      this.menus = [];
      this.buttons = [];
    }
  }
}); 