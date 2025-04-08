<script>
	export default {
		onLaunch: function() {
			console.log('App Launch')
			// 检查用户是否已登录，如果没有则跳转到登录页
			const token = uni.getStorageSync('token')
			if (!token) {
				uni.reLaunch({
					url: '/pages/login/index'
				})
			}
		},
		onShow: function() {
			console.log('App Show')
		},
		onHide: function() {
			console.log('App Hide')
		}
	}
</script>

<style>
	/*每个页面公共css */
	/* 全局样式 */
	page {
		font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
		font-size: 28rpx;
		color: #333;
		background-color: #f5f5f5;
	}
</style>
