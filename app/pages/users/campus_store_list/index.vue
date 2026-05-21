<template>
	<view :data-theme="theme" class="campus-store-page">
		<view class="school-panel borRadius14">
			<view class="school-label">当前学校</view>
			<picker :range="schoolNames" :value="schoolIndex" @change="changeSchool">
				<view class="school-picker">{{schoolNames[schoolIndex] || '请选择学校校区'}}</view>
			</picker>
		</view>
		<view class="store-list" v-if="storeList.length">
			<view class="store-item borRadius14" v-for="item in storeList" :key="item.id" @click="openStore(item)">
				<image class="store-logo" :src="item.systemStore.image" mode="aspectFill"></image>
				<view class="store-main">
					<view class="store-name line1">{{item.systemStore.name}}</view>
					<view class="store-info line1" v-if="item.systemStore.introduction">{{item.systemStore.introduction}}</view>
					<view class="store-meta">
						<text v-if="item.systemStore.dayTime">营业 {{item.systemStore.dayTime}}</text>
						<text v-if="item.systemStore.phone" @click="callStore(item.systemStore.phone)">联系商家</text>
					</view>
					<view class="store-address line1">{{item.systemStore.address}} {{item.systemStore.detailedAddress}}</view>
				</view>
			</view>
		</view>
		<view class="empty" v-else-if="loaded">当前学校还没有可服务商家</view>
	</view>
</template>

<script>
	import {
		campusSchoolListApi,
		campusStoreListApi
	} from '@/api/campus.js';
	let app = getApp();
	export default {
		data() {
			return {
				schools: [],
				schoolIndex: 0,
				schoolId: 0,
				storeList: [],
				loaded: false,
				theme: app.globalData.theme
			}
		},
		computed: {
			schoolNames() {
				return this.schools.map(item => item.schoolName + ' ' + item.campusName);
			}
		},
		onLoad(options) {
			this.schoolId = Number(options.schoolId || 0);
			this.loadSchools();
		},
		methods: {
			loadSchools() {
				campusSchoolListApi().then(res => {
					this.schools = res.data || [];
					let index = this.schools.findIndex(item => item.id === this.schoolId);
					this.selectSchool(index > -1 ? index : 0);
				}).catch(err => {
					this.$util.Tips({ title: err });
				});
			},
			selectSchool(index) {
				let school = this.schools[index];
				this.schoolIndex = index;
				this.schoolId = school ? school.id : 0;
				if (!this.schoolId) {
					this.storeList = [];
					this.loaded = true;
					return;
				}
				this.loaded = false;
				campusStoreListApi({ schoolId: this.schoolId }).then(res => {
					this.storeList = (res.data || []).filter(item => item.systemStore);
					this.loaded = true;
				}).catch(err => {
					this.storeList = [];
					this.loaded = true;
					this.$util.Tips({ title: err });
				});
			},
			changeSchool(e) {
				this.selectSchool(parseInt(e.detail.value));
			},
			callStore(phone) {
				uni.makePhoneCall({ phoneNumber: phone });
			},
			openStore(item) {
				uni.navigateTo({
					url: '/pages/users/campus_store_detail/index?schoolId=' + item.schoolId + '&storeId=' + item.storeId
				});
			}
		}
	}
</script>

<style lang="scss" scoped>
	.campus-store-page {
		padding: 24rpx 30rpx 40rpx;
	}

	.school-panel {
		background: #fff;
		padding: 28rpx 24rpx;
		margin-bottom: 20rpx;
	}

	.school-label {
		font-size: 24rpx;
		color: #999;
		margin-bottom: 12rpx;
	}

	.school-picker {
		font-size: 30rpx;
		color: #282828;
		font-weight: 600;
	}

	.store-item {
		display: flex;
		gap: 20rpx;
		background: #fff;
		padding: 24rpx;
		margin-bottom: 20rpx;
	}

	.store-logo {
		width: 132rpx;
		height: 132rpx;
		border-radius: 10rpx;
		background: #f4f4f4;
		flex: 0 0 132rpx;
	}

	.store-main {
		flex: 1;
		min-width: 0;
	}

	.store-name {
		font-size: 31rpx;
		font-weight: 600;
		color: #282828;
	}

	.store-info,
	.store-address {
		font-size: 24rpx;
		line-height: 36rpx;
		color: #777;
		margin-top: 8rpx;
	}

	.store-meta {
		display: flex;
		flex-wrap: wrap;
		gap: 12rpx;
		margin-top: 10rpx;
		font-size: 24rpx;
		color: #666;
	}

	.store-meta text:last-child {
		color: #e93323;
	}

	.empty {
		color: #999;
		font-size: 26rpx;
		text-align: center;
		padding: 120rpx 0 40rpx;
	}
</style>
