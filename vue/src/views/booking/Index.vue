<template>
  <router-view />
</template>

<script>
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';

export default {
  name: 'BookingIndex',
  setup() {
    const router = useRouter();
    
    onMounted(() => {
      // 重定向到会议室预订页面
      router.replace('/room/booking');
    });
    
    return {};
  }
};
</script> 