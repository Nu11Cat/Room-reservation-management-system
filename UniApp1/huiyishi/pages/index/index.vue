<template>
	<view class="container">
		<view class="welcome">
			<text class="welcome-text">欢迎使用会议室预约系统</text>
		</view>
		
		<view class="content">
			<view class="card">
				<view class="card-title">系统公告</view>
				<view class="card-content">
					<text>欢迎使用会议室预约系统，您可以在此系统中预约会议室、查看会议日程等。</text>
				</view>
			</view>
			
			<view class="button-group">
				<button class="btn" @tap="goToBooking">预约会议室</button>
				<button class="btn" @tap="goToMyBookings">我的预约</button>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				title: '首页'
			}
		},
		onLoad() {
			// 检查是否已登录
			const token = uni.getStorageSync('token')
			if (!token) {
				uni.reLaunch({
					url: '/pages/login/index'
				})
			}
		},
		methods: {
			goToBooking() {
				uni.showToast({
					title: '功能开发中...',
					icon: 'none'
				})
			},
			goToMyBookings() {
				uni.showToast({
					title: '功能开发中...',
					icon: 'none'
				})
			}
		}
	}
</script>

<style>
.container {
	padding: 30rpx;
}

.welcome {
	margin-bottom: 40rpx;
	padding: 40rpx;
	background-color: #409EFF;
	border-radius: 16rpx;
	text-align: center;
}

.welcome-text {
	font-size: 36rpx;
	color: #fff;
	font-weight: bold;
}

.card {
	background-color: #fff;
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 30rpx;
	box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.1);
}

.card-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 20rpx;
	border-bottom: 1px solid #eee;
	padding-bottom: 10rpx;
}

.card-content {
	font-size: 28rpx;
	color: #666;
	line-height: 1.5;
}

.button-group {
	display: flex;
	justify-content: space-between;
}

.btn {
	flex: 1;
	margin: 0 20rpx;
	background-color: #409EFF;
	color: #fff;
}

.btn:first-child {
	margin-left: 0;
}

.btn:last-child {
	margin-right: 0;
}
</style>
