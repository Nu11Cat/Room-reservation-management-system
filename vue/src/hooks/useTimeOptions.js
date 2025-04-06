import { computed, ref, watch } from 'vue';
import { useConfigStore } from '@/stores/config';

/**
 * 提供基于系统配置的时间选项生成功能
 * @returns {Object} - 返回时间相关计算属性和工具函数
 */
export default function useTimeOptions() {
  const configStore = useConfigStore();
  const forceRefresh = ref(0);
  const configReady = ref(false);
  
  // 检查配置是否已加载
  watch(() => [
    configStore.bookingStartTime, 
    configStore.bookingEndTime
  ], ([startTime, endTime]) => {
    if (startTime && endTime) {
      configReady.value = true;
      console.log('[useTimeOptions] 配置已加载完成，使用固定15分钟间隔');
    }
  }, { immediate: true });
  
  // 确保配置已初始化
  if (!configStore.initialized) {
    configStore.initialize();
  }
  
  // 更新forceRefresh以触发计算属性重新计算
  const triggerRefresh = () => {
    forceRefresh.value++;
    console.log('[useTimeOptions] 强制刷新时间选项，当前计数:', forceRefresh.value);
  };
  
  /**
   * 时间范围（小时数组）
   */
  const timeRange = computed(() => {
    // forceRefresh作为依赖确保在值变化时重新计算
    forceRefresh.value;
    
    // 检查必要的配置是否存在
    if (!configStore.bookingStartTime || !configStore.bookingEndTime) {
      console.warn('[useTimeOptions] 配置不完整，使用默认值');
      // 应急默认值
      const startHour = 8;
      const endHour = 20;
      return Array.from(
        { length: endHour - startHour }, 
        (_, i) => i + startHour
      );
    }
    
    const startTime = configStore.bookingStartTime;
    const endTime = configStore.bookingEndTime;
    
    console.log(`[useTimeOptions] 计算时间范围: ${startTime} - ${endTime}`);
    
    // 解析时间
    const startHour = parseInt(startTime.split(':')[0]);
    const endHour = parseInt(endTime.split(':')[0]);
    
    // 生成小时范围数组 - 使用开区间，不包含结束小时
    return Array.from(
      { length: endHour - startHour }, 
      (_, i) => i + startHour
    );
  });
  
  /**
   * 时间选项列表（按配置的时间间隔）
   */
  const timeOptions = computed(() => {
    // forceRefresh作为依赖确保在值变化时重新计算
    forceRefresh.value;
    
    // 检查必要的配置是否存在
    if (!configStore.bookingStartTime || !configStore.bookingEndTime) {
      console.warn('[useTimeOptions] 配置不完整，使用默认值');
      // 应急默认值
      const options = [];
      const interval = 15; // 固定15分钟间隔
      const startTotalMinutes = 8 * 60;
      const endTotalMinutes = 20 * 60;
      
      // 按15分钟间隔生成时间选项
      for (let mins = startTotalMinutes; mins < endTotalMinutes; mins += interval) {
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
    
    const options = [];
    // 固定使用15分钟间隔
    const interval = 15;
    const startTime = configStore.bookingStartTime;
    const endTime = configStore.bookingEndTime;
    
    console.log(`[useTimeOptions] 计算时间选项: 固定间隔=${interval}分钟, ${startTime} - ${endTime}`);
    
    // 解析时间范围
    const [startHour, startMinute] = startTime.split(':').map(Number);
    const [endHour, endMinute] = endTime.split(':').map(Number);
    
    const startTotalMinutes = startHour * 60 + startMinute;
    const endTotalMinutes = endHour * 60 + endMinute;
    
    // 按15分钟间隔生成时间选项
    for (let mins = startTotalMinutes; mins < endTotalMinutes; mins += interval) {
      const hours = Math.floor(mins / 60);
      const minutes = mins % 60;
      
      const timeValue = `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;
      options.push({
        label: timeValue,
        value: timeValue
      });
    }
    
    return options;
  });
  
  /**
   * 获取分钟格式的时间
   * @param {string} timeStr - HH:MM格式的时间字符串
   * @returns {number} - 转换为分钟的时间
   */
  const getMinutes = (timeStr) => {
    const [hours, minutes] = timeStr.split(':').map(Number);
    return hours * 60 + minutes;
  };
  
  /**
   * 计算两个时间之间的分钟数
   * @param {string} startTime - 开始时间（HH:MM）
   * @param {string} endTime - 结束时间（HH:MM）
   * @returns {number} - 分钟差值
   */
  const getDurationMinutes = (startTime, endTime) => {
    return getMinutes(endTime) - getMinutes(startTime);
  };
  
  /**
   * 验证时间间隔是否符合系统配置
   * @param {string} startTime - 开始时间（HH:MM）
   * @param {string} endTime - 结束时间（HH:MM）
   * @returns {Object} - 验证结果{valid, message}
   */
  const validateTimeRange = (startTime, endTime) => {
    if (!startTime || !endTime) {
      return { valid: false, message: '请选择完整的时间范围' };
    }
    
    if (endTime <= startTime) {
      return { valid: false, message: '结束时间必须晚于开始时间' };
    }
    
    const durationMinutes = getDurationMinutes(startTime, endTime);
    
    // 确保预约时长是15分钟的整数倍
    if (durationMinutes % 15 !== 0) {
      return { 
        valid: false, 
        message: '预约时长必须是15分钟的整数倍'
      };
    }
    
    const maxDuration = configStore.bookingMaxDuration || 480; // 默认8小时
    
    if (durationMinutes > maxDuration) {
      // 将分钟转换为更友好的显示
      const durationHours = Math.floor(maxDuration / 60);
      const durationMinutesRemainder = maxDuration % 60;
      let durationDisplay = '';
      
      if (durationHours > 0) {
        durationDisplay += `${durationHours}小时`;
      }
      if (durationMinutesRemainder > 0) {
        durationDisplay += `${durationMinutesRemainder}分钟`;
      }
      
      return { 
        valid: false, 
        message: `预约时长不能超过${durationDisplay}`
      };
    }
    
    // 固定最小预约间隔为15分钟
    const minInterval = 15;
    if (durationMinutes < minInterval) {
      return {
        valid: false,
        message: `预约时长不能少于${minInterval}分钟`
      };
    }
    
    return { valid: true, message: '' };
  };
  
  /**
   * 获取可预约的最早日期（当前日期）
   */
  const earliestBookingDate = computed(() => {
    const today = new Date();
    return new Date(today.getFullYear(), today.getMonth(), today.getDate());
  });
  
  /**
   * 获取可预约的最晚日期（基于配置的最大提前天数）
   */
  const latestBookingDate = computed(() => {
    // 如果配置未加载，使用默认30天
    const maxDays = configStore.bookingMaxAdvanceDays || 0;
    if (maxDays === 0) {
      console.warn('[useTimeOptions] 最大提前预约天数配置不存在');
      return new Date(8640000000000000); // 远未来日期
    }
    
    const date = new Date();
    date.setDate(date.getDate() + maxDays);
    return date;
  });
  
  /**
   * 日期选择器的禁用日期函数
   */
  const disabledDate = (date) => {
    // 如果配置未加载完成，禁用所有日期
    if (!configReady.value) {
      return true;
    }
    
    const today = earliestBookingDate.value;
    const maxDate = latestBookingDate.value;
    
    return date < today || date > maxDate;
  };
  
  return {
    timeRange,
    timeOptions,
    getMinutes,
    getDurationMinutes,
    validateTimeRange,
    earliestBookingDate,
    latestBookingDate,
    disabledDate,
    configReady,
    maxBookingDuration: computed(() => configStore.bookingMaxDuration || 0),
    timeInterval: computed(() => 15), // 固定为15分钟
    minBookingInterval: computed(() => 15), // 固定为15分钟
    maxAdvanceDays: computed(() => configStore.bookingMaxAdvanceDays || 0),
    refreshTimeOptions: triggerRefresh
  };
} 