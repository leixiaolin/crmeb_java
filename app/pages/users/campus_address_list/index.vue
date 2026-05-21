<template>
	<view :data-theme="theme" class="campus-address-page">
		<view class="address-list" v-if="addressList.length">
			<radio-group @change="changeDefault">
				<view class="address-item borRadius14" v-for="(item, index) in addressList" :key="item.id">
					<view class="address-main" @click="selectAddress(item)">
						<view class="contact">{{item.contactName}}<text>{{item.contactPhone}}</text></view>
						<view class="detail">{{item.schoolName}} {{item.campusName}} {{item.buildingName}} {{item.floorNo}}层 {{item.roomNo}}</view>
						<view class="remark" v-if="item.remark">{{item.remark}}</view>
					</view>
					<view class="address-actions acea-row row-between-wrapper">
						<radio :value="index.toString()" :checked="item.isDefault">默认地址</radio>
						<view class="action-links">
							<text @click="editAddress(item.id)">编辑</text>
							<text @click="deleteAddress(index)">删除</text>
						</view>
					</view>
				</view>
			</radio-group>
			<view class="load-state">{{loadTitle}}</view>
		</view>
		<view class="empty" v-else-if="loadend">还没有校园地址</view>
		<view class="footer">
			<view class="store-link" @click="openStores">查看校园商家</view>
			<view class="address-button bg_color" @click="addAddress">添加校园地址</view>
		</view>
	</view>
</template>

<script>
	import {
		campusAddressListApi,
		campusAddressDeleteApi,
		campusAddressSetDefaultApi
	} from '@/api/campus.js';
	import {
		toLogin
	} from '@/libs/login.js';
	import {
		mapGetters
	} from 'vuex';
	let app = getApp();
	export default {
		data() {
			return {
				addressList: [],
				page: 1,
				limit: 20,
				loading: false,
				loadend: false,
				loadTitle: '加载更多',
				preOrderNo: '',
				theme: app.globalData.theme
			}
		},
		computed: mapGetters(['isLogin']),
		onLoad(options) {
			if (!this.isLogin) return toLogin();
			this.preOrderNo = options.preOrderNo || '';
			this.getAddressList(true);
		},
		onShow() {
			if (this.isLogin) this.getAddressList(true);
		},
		onReachBottom() {
			this.getAddressList();
		},
		methods: {
			getAddressList(reset) {
				if (reset) {
					this.page = 1;
					this.loadend = false;
					this.addressList = [];
				}
				if (this.loading || this.loadend) return;
				this.loading = true;
				campusAddressListApi({
					page: this.page,
					limit: this.limit
				}).then(res => {
					let list = res.data.list || [];
					this.addressList = this.addressList.concat(list);
					this.loadend = list.length < this.limit;
					this.loadTitle = this.loadend ? '没有更多了' : '加载更多';
					this.page = this.page + 1;
				}).catch(err => {
					this.$util.Tips({
						title: err
					});
				}).finally(() => {
					this.loading = false;
				});
			},
			addAddress() {
				uni.navigateTo({
					url: '/pages/users/campus_address/index'
				});
			},
			openStores() {
				let address = this.addressList.find(item => item.isDefault) || this.addressList[0];
				let query = address ? '?schoolId=' + address.schoolId : '';
				uni.navigateTo({
					url: '/pages/users/campus_store_list/index' + query
				});
			},
			editAddress(id) {
				uni.navigateTo({
					url: '/pages/users/campus_address/index?id=' + id
				});
			},
			selectAddress(item) {
				if (!this.preOrderNo) return this.editAddress(item.id);
				uni.redirectTo({
					url: '/pages/order/order_confirm/index?is_address=1&preOrderNo=' + this.preOrderNo +
						'&campusAddressId=' + item.id
				});
			},
			changeDefault(e) {
				let item = this.addressList[parseInt(e.detail.value)];
				if (!item) return;
				campusAddressSetDefaultApi(item.id).then(() => {
					this.addressList.forEach(address => {
						address.isDefault = address.id === item.id;
					});
					this.$util.Tips({
						title: '默认校园地址已更新',
						icon: 'success'
					});
				}).catch(err => {
					this.$util.Tips({
						title: err
					});
				});
			},
			deleteAddress(index) {
				let item = this.addressList[index];
				if (!item) return;
				uni.showModal({
					content: '确定删除该校园地址吗？',
					success: res => {
						if (!res.confirm) return;
						campusAddressDeleteApi(item.id).then(() => {
							this.addressList.splice(index, 1);
							this.$util.Tips({
								title: '删除成功',
								icon: 'success'
							});
						}).catch(err => {
							this.$util.Tips({
								title: err
							});
						});
					}
				});
			}
		}
	}
</script>

<style lang="scss" scoped>
	.bg_color {
		@include main_bg_color(theme);
	}

	.campus-address-page {
		padding: 24rpx 30rpx 140rpx;
	}

	.address-item {
		background: #fff;
		margin-bottom: 20rpx;
		padding: 28rpx 24rpx 18rpx;
	}

	.contact {
		font-size: 30rpx;
		font-weight: 600;
		color: #282828;
	}

	.contact text {
		font-size: 26rpx;
		font-weight: 400;
		margin-left: 18rpx;
		color: #666;
	}

	.detail,
	.remark {
		font-size: 26rpx;
		line-height: 40rpx;
		color: #666;
		margin-top: 10rpx;
	}

	.address-actions {
		border-top: 1rpx solid #eee;
		margin-top: 20rpx;
		padding-top: 16rpx;
		font-size: 26rpx;
		color: #666;
	}

	.action-links text {
		margin-left: 28rpx;
		color: #333;
	}

	.load-state,
	.empty {
		text-align: center;
		color: #999;
		font-size: 26rpx;
		padding: 36rpx 0;
	}

	.footer {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		padding: 18rpx 30rpx calc(18rpx + env(safe-area-inset-bottom));
		background: #fff;
		display: flex;
		gap: 18rpx;
	}

	.address-button,
	.store-link {
		flex: 1;
		color: #fff;
		text-align: center;
		border-radius: 44rpx;
		height: 88rpx;
		line-height: 88rpx;
		font-size: 30rpx;
	}

	.store-link {
		border: 1rpx solid #ddd;
		color: #333;
		background: #fff;
	}
</style>
