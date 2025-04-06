import { createApp } from 'vue';
import ElementPlus from 'element-plus';
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css';
import { createPinia } from 'pinia';
import App from './App.vue';
import router from './router';
import { vButton, vOperation } from './directives/permission';
import { useConfigStore } from './stores/config';

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

// 初始化配置store
const configStore = useConfigStore();
configStore.initialize();

// 处理ResizeObserver错误
const debounce = (fn, delay) => {
  let timer = null;
  return function() {
    clearTimeout(timer);
    timer = setTimeout(() => {
      fn.apply(this, arguments);
    }, delay);
  };
};

// 阻止ResizeObserver错误引起的页面崩溃
window.addEventListener('error', (e) => {
  if (e.message === 'ResizeObserver loop limit exceeded' || e.message.includes('ResizeObserver')) {
    console.warn('ResizeObserver error caught and handled');
    e.stopImmediatePropagation();
  }
}, true);

// 防止ResizeObserver循环问题
window.addEventListener('resize', debounce(() => {
  // 空函数，用于缓解ResizeObserver问题
}, 100));

// 抑制ResizeObserver错误
const originalConsoleError = window.console.error;
window.console.error = (...args) => {
  if (args[0] && typeof args[0] === 'string' && args[0].includes('ResizeObserver')) {
    // 忽略ResizeObserver错误
    return;
  }
  originalConsoleError(...args);
};

app.mount('#app');
