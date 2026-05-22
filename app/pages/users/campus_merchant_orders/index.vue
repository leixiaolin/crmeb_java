<template>
	<view :data-theme="theme" class="merchant-orders">
		<view class="summary">
			<view class="summary-item">
				<view class="value">{{summary.todayOrderCount}}</view>
				<view class="label">今日校园单</view>
			</view>
			<view class="summary-item">
				<view class="value">¥{{summary.todayPayAmount}}</view>
				<view class="label">今日实付</view>
			</view>
		</view>
		<view class="hot-products" v-if="summary.hotProductList && summary.hotProductList.length">
			<view class="hot-title">今日热销</view>
			<view class="hot-item" v-for="(item,index) in summary.hotProductList" :key="item.productId">
				<view class="hot-rank">{{index + 1}}</view>
				<view class="hot-name line1">{{item.productName}}</view>
				<view class="hot-num">x{{item.payNum}}</view>
			</view>
		</view>
		<view class="status-tabs">
			<view class="tab" :class="{ on: !refundOnly && campusStatus === '' }" @tap="changeStatus('')">全部</view>
			<view class="tab" :class="{ on: campusStatus === 10 }" @tap="changeStatus(10)">待接单</view>
			<view class="tab" :class="{ on: campusStatus === 20 }" @tap="changeStatus(20)">配送中</view>
			<view class="tab" :class="{ on: campusStatus === -10 }" @tap="changeStatus(-10)">已取消</view>
			<view class="tab" :class="{ on: refundOnly }" @tap="changeRefund">退款</view>
		</view>
		<view class="order-list">
			<view class="order" v-for="(item,index) in orderList" :key="item.orderId">
				<view class="order-main" @tap="goDetail(item.orderId)">
					<view class="head acea-row row-between-wrapper">
						<view class="order-no">{{item.orderId}}</view>
						<view class="state">{{item.orderStatus}}</view>
					</view>
					<view class="time">{{item.createTime}}</view>
					<view class="product acea-row row-between row-top" v-for="product in item.orderInfoList" :key="product.attrId + '-' + product.productId">
						<image :src="product.image"></image>
						<view class="product-info acea-row row-between">
							<view class="name line2">{{product.storeName}}</view>
							<view class="amount">
								<view>￥{{product.price}}</view>
								<view>x{{product.cartNum}}</view>
							</view>
						</view>
					</view>
					<view class="total">共{{item.totalNum}}件，实付 <text>￥{{item.payPrice}}</text></view>
				</view>
				<view class="actions acea-row row-right row-middle">
					<view class="btn plain" @tap="goDetail(item.orderId)">详情</view>
					<view class="btn plain" v-if="canProcess(item)" @tap="reject(item,index)">拒单</view>
					<view class="btn fill" v-if="canProcess(item)" @tap="accept(item,index)">接单</view>
				</view>
			</view>
		</view>
		<view class="loadingicon acea-row row-center-wrapper" v-if="orderList.length">
			<text class="loading iconfont icon-jiazai" :hidden="!loading"></text>{{loadTitle}}
		</view>
		<emptyPage v-if="!loading && !orderList.length" title="暂无校园订单~"></emptyPage>
	</view>
</template>

<script>
	import emptyPage from '@/components/emptyPage.vue';
	import {
		campusMerchantOrderListApi,
		campusMerchantTodaySummaryApi,
		campusMerchantRefundListApi,
		campusMerchantOrderAcceptApi,
		campusMerchantOrderRejectApi
	} from '@/api/campus.js';
	import { toLogin } from '@/libs/login.js';
	import { mapGetters } from 'vuex';
	const app = getApp();

	export default {
		components: { emptyPage },
		computed: mapGetters(['isLogin']),
		data() {
			return {
				theme: app.globalData.theme,
				campusStatus: '',
				refundOnly: false,
				summary: {
					todayOrderCount: 0,
					todayPayAmount: '0.00',
					hotProductList: []
				},
				orderList: [],
				page: 1,
				limit: 10,
				loading: false,
				loadend: false,
				loadTitle: '加载更多'
			};
		},
		onShow() {
			if (!this.isLogin) {
				toLogin();
				return;
			}
			this.getTodaySummary();
			this.reset();
		},
		onReachBottom() {
			this.getList();
		},
		methods: {
			reset() {
				this.page = 1;
				this.loadend = false;
				this.orderList = [];
				this.getList();
			},
			getTodaySummary() {
				campusMerchantTodaySummaryApi().then(res => {
					this.summary = res.data || this.summary;
				}).catch(err => this.$util.Tips({ title: err }));
			},
			changeStatus(status) {
				if (!this.refundOnly && this.campusStatus === status) return;
				this.refundOnly = false;
				this.campusStatus = status;
				this.reset();
			},
			changeRefund() {
				if (this.refundOnly) return;
				this.refundOnly = true;
				this.campusStatus = '';
				this.reset();
			},
			getList() {
				if (this.loading || this.loadend) return;
				this.loading = true;
				let data = {
					page: this.page,
					limit: this.limit
				};
				if (this.campusStatus !== '') data.campusStatus = this.campusStatus;
				let listApi = this.refundOnly ? campusMerchantRefundListApi : campusMerchantOrderListApi;
				listApi(data).then(res => {
					let list = res.data.list || [];
					this.orderList = this.orderList.concat(list);
					this.loadend = list.length < this.limit;
					this.page = this.page + 1;
					this.loadTitle = this.loadend ? '我也是有底线的' : '加载更多';
					this.loading = false;
				}).catch(err => {
					this.loading = false;
					this.$util.Tips({ title: err });
				});
			},
			canProcess(item) {
				return item.paid && item.refundStatus === 0 && item.campusStatus === 10;
			},
			goDetail(orderNo) {
				uni.navigateTo({
					url: '/pages/users/campus_merchant_order_detail/index?orderNo=' + orderNo
				});
			},
			accept(item, index) {
				campusMerchantOrderAcceptApi(item.orderId).then(() => {
					this.$set(this.orderList[index], 'campusStatus', 20);
					this.$set(this.orderList[index], 'orderStatus', '配送中');
					this.$util.Tips({ title: '接单成功', icon: 'success' });
				}).catch(err => this.$util.Tips({ title: err }));
			},
			reject(item, index) {
				uni.showModal({
					title: '拒绝订单',
					content: '拒单将发起全额退款，确认继续？',
					success: res => {
						if (!res.confirm) return;
						campusMerchantOrderRejectApi(item.orderId).then(() => {
							this.$set(this.orderList[index], 'campusStatus', -10);
							this.$set(this.orderList[index], 'orderStatus', '已取消');
							this.$set(this.orderList[index], 'refundStatus', 3);
							this.$util.Tips({ title: '拒单已提交', icon: 'success' });
						}).catch(err => this.$util.Tips({ title: err }));
					}
				});
			}
		}
	};
</script>

<style scoped lang="scss">
	.merchant-orders {
		min-height: 100vh;
		padding: 24rpx 24rpx 48rpx;
		background: #f5f5f5;
	}
	.status-tabs {
		display: grid;
		grid-template-columns: repeat(5, minmax(0, 1fr));
		height: 76rpx;
		margin-bottom: 22rpx;
		padding: 6rpx;
		border-radius: 8rpx;
		background: #fff;
	}
	.summary {
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 18rpx;
		margin-bottom: 20rpx;
	}
	.summary-item {
		min-height: 128rpx;
		padding: 24rpx;
		border-radius: 8rpx;
		background: #fff;
	}
	.summary-item .value {
		color: #282828;
		font-size: 38rpx;
		font-weight: 600;
		line-height: 48rpx;
	}
	.summary-item .label {
		margin-top: 12rpx;
		color: #777;
		font-size: 24rpx;
	}
	.hot-products {
		margin-bottom: 20rpx;
		padding: 24rpx;
		border-radius: 8rpx;
		background: #fff;
	}
	.hot-title {
		margin-bottom: 14rpx;
		color: #282828;
		font-size: 28rpx;
		font-weight: 600;
	}
	.hot-item {
		display: grid;
		grid-template-columns: 40rpx minmax(0, 1fr) 80rpx;
		align-items: center;
		min-height: 54rpx;
		color: #555;
		font-size: 25rpx;
	}
	.hot-rank {
		color: #e93323;
		font-weight: 600;
	}
	.hot-name {
		min-width: 0;
	}
	.hot-num {
		text-align: right;
	}
	.tab {
		display: flex;
		align-items: center;
		justify-content: center;
		color: #666;
		font-size: 26rpx;
	}
	.tab.on {
		border-radius: 6rpx;
		background: #e93323;
		color: #fff;
	}
	.order {
		margin-bottom: 20rpx;
		border-radius: 8rpx;
		background: #fff;
		overflow: hidden;
	}
	.order-main {
		padding: 26rpx;
	}
	.head {
		font-size: 26rpx;
	}
	.order-no {
		max-width: 480rpx;
		color: #282828;
		word-break: break-all;
	}
	.state,
	.total text {
		color: #e93323;
	}
	.time {
		margin: 10rpx 0 20rpx;
		color: #999;
		font-size: 23rpx;
	}
	.product {
		margin-top: 18rpx;
	}
	.product image {
		width: 106rpx;
		height: 106rpx;
		border-radius: 6rpx;
	}
	.product-info {
		width: calc(100% - 126rpx);
		color: #333;
		font-size: 25rpx;
	}
	.product-info .name {
		width: calc(100% - 120rpx);
	}
	.amount {
		width: 108rpx;
		text-align: right;
		color: #666;
	}
	.total {
		margin-top: 22rpx;
		text-align: right;
		color: #666;
		font-size: 25rpx;
	}
	.actions {
		height: 88rpx;
		padding: 0 24rpx;
		border-top: 1rpx solid #f2f2f2;
	}
	.btn {
		min-width: 118rpx;
		height: 54rpx;
		margin-left: 14rpx;
		padding: 0 24rpx;
		border-radius: 8rpx;
		font-size: 25rpx;
		line-height: 54rpx;
		text-align: center;
	}
	.btn.plain {
		border: 1rpx solid #ddd;
		color: #555;
	}
	.btn.fill {
		background: #e93323;
		color: #fff;
	}
</style>
