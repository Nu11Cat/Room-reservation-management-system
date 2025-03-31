import { createApp } from 'vue';
import ElementPlus from 'element-plus';
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css';
import { createPinia } from 'pinia';
import App from './App.vue';
import router from './router';
import { vButton, vOperation } from './directives/permission';

const app = createApp(App);
const pinia = createPinia();

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 注册权限指令
app.directive('permission-button', vButton);
app.directive('permission-operation', vOperation);

app.use(ElementPlus);
app.use(pinia);
app.use(router);

// 抑制 ResizeObserver 循环错误
const resizeHandler = () => {
  const messages = [
    'ResizeObserver loop limit exceeded',
    'ResizeObserver loop completed with undelivered notifications'
  ];
  
  window.addEventListener('error', (e) => {
    if (messages.includes(e.message)) {
      e.stopPropagation();
      e.preventDefault();
    }
  }, true);
};
resizeHandler();

// 忽略ResizeObserver错误，这是Element Plus组件的常见警告
const originalConsoleError = window.console.error;
window.console.error = (...args) => {
  if (args[0] && args[0].includes && args[0].includes('ResizeObserver')) {
    return;
  }
  originalConsoleError(...args);
};

app.mount('#app');
