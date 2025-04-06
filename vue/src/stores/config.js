import { defineStore } from 'pinia';
// eslint-disable-next-line no-unused-vars
import { getConfig, getConfigList } from '@/api/system';

// 缓存键，用于localStorage备份
const CONFIG_CACHE_KEY = 'system_config_cache';
// 配置缓存时间，单位毫秒 (5分钟)
const CONFIG_CACHE_TTL = 5 * 60 * 1000;

export const useConfigStore = defineStore('config', {
  state: () => ({
    // 预约规则配置
    bookingStartTime: '',
    bookingEndTime: '',
    // 固定时间间隔为15分钟
    bookingTimeInterval: 15,
    bookingMaxDuration: 0,
    // 固定最小预约间隔为15分钟
    bookingMinInterval: 15,
    bookingMaxAdvanceDays: 0,
    bookingCancelLimit: 0,
    
    // 审批规则配置
    approvalRequired: true,
    
    // 系统参数
    systemName: '会议室预约管理系统',
    adminContact: '',
    
    // 通用状态
    initialized: false,
    loading: false,
    lastUpdated: null,
    forceRefresh: false,
    
    // 调试信息
    loadCount: 0,
    configSource: 'default', // 标记配置来源: default, api, cache, ws
    
    // 缓存控制
    cacheTimestamp: 0, // 缓存时间戳
  }),
  
  actions: {
    /**
     * 初始化配置，从API或缓存加载
     */
    async initialize() {
      if (this.initialized && !this.forceRefresh) {
        console.log('[config] 已初始化，跳过');
        return;
      }
      
      this.loading = true;
      this.loadCount++;
      
      try {
        console.log('[config] 初始化配置，加载次数:', this.loadCount);
        
        // 尝试从localStorage读取缓存
        let useCache = false;
        const cachedConfigStr = localStorage.getItem(CONFIG_CACHE_KEY);
        
        if (cachedConfigStr) {
          try {
            const cachedConfig = JSON.parse(cachedConfigStr);
            const now = Date.now();
            
            // 检查缓存是否过期 (5分钟)
            if (cachedConfig.cacheTimestamp && (now - cachedConfig.cacheTimestamp < CONFIG_CACHE_TTL)) {
              console.log('[config] 使用本地缓存配置', cachedConfig);
              
              // 更新状态
              Object.keys(cachedConfig).forEach(key => {
                if (key in this.$state) {
                  this[key] = cachedConfig[key];
                }
              });
              
              this.configSource = 'cache';
              this.lastUpdated = new Date(cachedConfig.cacheTimestamp);
              useCache = true;
            } else {
              console.log('[config] 缓存已过期，将从API刷新');
            }
          } catch (e) {
            console.error('[config] 解析缓存配置出错:', e);
          }
        } else {
          console.log('[config] 未找到本地缓存配置');
        }
        
        // 如果没有使用缓存或需要强制刷新，则从API加载
        if (!useCache || this.forceRefresh) {
          await this.loadFromAPI();
        }
        
        this.initialized = true;
        this.forceRefresh = false;
        
        console.log('[config] 配置初始化完成:', this.$state);
      } catch (error) {
        console.error('[config] 初始化配置失败:', error);
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    /**
     * 强制刷新所有配置
     */
    async refresh() {
      console.log('[Config] 强制刷新配置开始');
      this.forceRefresh = true;
      try {
        await this.initialize();
        console.log('[Config] 强制刷新配置完成');
        // 发送全局刷新事件
        this.broadcastRefreshEvent();
        return true;
      } catch (error) {
        console.error('[Config] 强制刷新配置失败:', error);
        return false;
      }
    },
    
    /**
     * 广播全局配置刷新事件
     */
    broadcastRefreshEvent() {
      document.dispatchEvent(new CustomEvent('config-refreshed', {
        detail: { timestamp: new Date(), source: this.configSource }
      }));
      console.log('[Config] 已广播全局配置刷新事件');
    },
    
    /**
     * 从API加载配置
     */
    async loadFromAPI() {
      console.log('[config] 从API加载配置');
      
      try {
        // 获取所有BOOKING_RULE类型的配置
        const bookingRes = await getConfigList('BOOKING_RULE');
        if (bookingRes.code === 1 && bookingRes.data) {
          this.processBookingConfigs(bookingRes.data);
        } else {
          console.warn('[config] 获取BOOKING_RULE配置失败:', bookingRes);
        }
        
        // 获取所有APPROVAL_RULE类型的配置
        const approvalRes = await getConfigList('APPROVAL_RULE');
        if (approvalRes.code === 1 && approvalRes.data) {
          this.processApprovalConfigs(approvalRes.data);
        } else {
          console.warn('[config] 获取APPROVAL_RULE配置失败:', approvalRes);
        }
        
        // 获取所有SYSTEM_PARAM类型的配置
        const systemRes = await getConfigList('SYSTEM_PARAM');
        if (systemRes.code === 1 && systemRes.data) {
          this.processSystemConfigs(systemRes.data);
        } else {
          console.warn('[config] 获取SYSTEM_PARAM配置失败:', systemRes);
        }
        
        this.configSource = 'api';
        this.lastUpdated = new Date();
        this.cacheTimestamp = Date.now();
        
        // 保存到缓存
        this.saveToCache();
        
        return true;
      } catch (error) {
        console.error('[config] 从API加载配置失败:', error);
        throw error;
      }
    },
    
    /**
     * 将配置保存到localStorage
     */
    saveToCache() {
      try {
        // 更新缓存时间戳
        this.cacheTimestamp = Date.now();
        
        // 选择需要缓存的字段
        const cacheData = {
          bookingStartTime: this.bookingStartTime,
          bookingEndTime: this.bookingEndTime,
          bookingTimeInterval: this.bookingTimeInterval,
          bookingMaxDuration: this.bookingMaxDuration,
          bookingMinInterval: this.bookingMinInterval,
          bookingMaxAdvanceDays: this.bookingMaxAdvanceDays,
          bookingCancelLimit: this.bookingCancelLimit,
          approvalRequired: this.approvalRequired,
          systemName: this.systemName,
          adminContact: this.adminContact,
          cacheTimestamp: this.cacheTimestamp
        };
        
        localStorage.setItem(CONFIG_CACHE_KEY, JSON.stringify(cacheData));
        console.log('[config] 配置已保存到本地缓存');
        
        // 触发事件通知其他组件配置已更新
        document.dispatchEvent(new CustomEvent('config-refreshed'));
      } catch (e) {
        console.error('[config] 保存配置到缓存失败:', e);
      }
    },
    
    /**
     * 更新指定配置的值
     */
    updateConfigValue(configKey, configValue) {
      const stateKey = this.getStateKey(configKey);
      if (!stateKey) {
        console.warn(`[config] 未知配置键: ${configKey}`);
        return false;
      }
      
      console.log(`[config] 更新配置: ${configKey} (${stateKey}) = ${configValue}`);
      
      // 时间间隔和最小预约间隔固定为15分钟
      if (stateKey === 'bookingTimeInterval' || stateKey === 'bookingMinInterval') {
        this[stateKey] = 15;
        console.log(`[config] ${stateKey}已固定为15分钟`);
      }
      // 最大预约时长只能是这些固定值
      else if (stateKey === 'bookingMaxDuration') {
        const validValues = [60, 120, 180, 240, 360, 480];
        const value = Number(configValue);
        if (validValues.includes(value)) {
          this[stateKey] = value;
        } else {
          console.warn(`[config] 无效的${stateKey}值: ${value}，使用默认值480`);
          this[stateKey] = 480;
        }
      }
      // 最大提前预约天数只能是这些固定值
      else if (stateKey === 'bookingMaxAdvanceDays') {
        const validValues = [7, 14, 30, 60, 90];
        const value = Number(configValue);
        if (validValues.includes(value)) {
          this[stateKey] = value;
        } else {
          console.warn(`[config] 无效的${stateKey}值: ${value}，使用默认值30`);
          this[stateKey] = 30;
        }
      }
      // 预约取消时限只能是这些固定值
      else if (stateKey === 'bookingCancelLimit') {
        const validValues = [0, 30, 60, 120, 1440];
        const value = Number(configValue);
        if (validValues.includes(value)) {
          this[stateKey] = value;
        } else {
          console.warn(`[config] 无效的${stateKey}值: ${value}，使用默认值120`);
          this[stateKey] = 120;
        }
      }
      // 转换布尔类型配置值
      else if (stateKey === 'approvalRequired') {
        this[stateKey] = configValue === 'true' || configValue === true;
      } 
      // 字符串类型，直接赋值
      else {
        this[stateKey] = configValue;
      }
      
      // 更新时间戳并保存到缓存
      this.lastUpdated = new Date();
      this.cacheTimestamp = Date.now();
      this.saveToCache();
      
      // 刷新配置
      if (['bookingStartTime', 'bookingEndTime', 'bookingTimeInterval'].includes(stateKey)) {
        console.log(`[config] 时间相关配置已更新: ${stateKey} = ${this[stateKey]}`);
      }
      
      return true;
    },
    
    /**
     * 获取配置键对应的状态字段名
     */
    getStateKey(configKey) {
      const keyMap = {
        'BOOKING_START_TIME': 'bookingStartTime',
        'BOOKING_END_TIME': 'bookingEndTime',
        'BOOKING_TIME_INTERVAL': 'bookingTimeInterval',
        'BOOKING_MAX_DURATION': 'bookingMaxDuration',
        'BOOKING_MIN_INTERVAL': 'bookingMinInterval',
        'BOOKING_MAX_ADVANCE_DAYS': 'bookingMaxAdvanceDays',
        'BOOKING_CANCEL_LIMIT': 'bookingCancelLimit',
        'APPROVAL_REQUIRED': 'approvalRequired',
        'SYSTEM_NAME': 'systemName',
        'ADMIN_CONTACT': 'adminContact'
      };
      
      return keyMap[configKey];
    },
    
    /**
     * 处理预约规则配置
     */
    processBookingConfigs(configs) {
      console.log('[config] 处理预约规则配置:', configs);
      for (const config of configs) {
        this.updateConfigValue(config.configKey, config.configValue);
      }
    },
    
    /**
     * 处理审批规则配置
     */
    processApprovalConfigs(configs) {
      console.log('[config] a处理审批规则配置:', configs);
      for (const config of configs) {
        this.updateConfigValue(config.configKey, config.configValue);
      }
    },
    
    /**
     * 处理系统参数配置
     */
    processSystemConfigs(configs) {
      console.log('[config] 处理系统参数配置:', configs);
      for (const config of configs) {
        this.updateConfigValue(config.configKey, config.configValue);
      }
    },
    
    /**
     * 强制刷新配置
     */
    forceRefreshConfig() {
      console.log('[config] 强制刷新配置');
      this.forceRefresh = true;
      return this.initialize();
    },
    
    /**
     * 清除配置缓存
     */
    clearCache() {
      localStorage.removeItem(CONFIG_CACHE_KEY);
      console.log('[config] 配置缓存已清除');
      return this.forceRefreshConfig();
    }
  },
  
  getters: {
    /**
     * 获取预约时间选项（根据时间粒度）
     */
    timeOptions: (state) => {
      const options = [];
      const interval = state.bookingTimeInterval || 30;
      
      // 按照配置的时间粒度生成时间选项
      const [startHour, startMinute] = state.bookingStartTime.split(':').map(Number);
      const [endHour, endMinute] = state.bookingEndTime.split(':').map(Number);
      
      const startTotalMinutes = startHour * 60 + startMinute;
      const endTotalMinutes = endHour * 60 + endMinute;
      
      for (let mins = startTotalMinutes; mins <= endTotalMinutes; mins += interval) {
        const hours = Math.floor(mins / 60);
        const minutes = mins % 60;
        
        const timeValue = `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;
        options.push({
          label: timeValue,
          value: timeValue
        });
      }
      
      return options;
    }
  }
});