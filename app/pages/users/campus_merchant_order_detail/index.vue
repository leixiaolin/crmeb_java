<template>
	<view :data-theme="theme" class="merchant-order-detail" v-if="order.orderId">
		<view class="status">
			<view class="title">{{statusText}}</view>
			<view class="time">{{order.payTime || order.createTime}}</view>
		</view>
		<view class="section">
			<view class="line strong">{{order.realName}} <text>{{order.userPhone}}</text></view>
			<view class="line address">{{order.userAddress}}</view>
		</view>
		<view class="section">
			<view class="product acea-row row-between row-top" v-for="item in order.orderInfoList" :key="item.attrId + '-' + item.productId">
				<image :src="item.image"></image>
				<view class="product-info">
					<view class="acea-row row-between row-top">
						<view class="name line2">{{item.storeName}}</view>
						<view class="price">￥{{item.price}}</view>
					</view>
					<view class="meta acea-row row-between">
						<view>{{item.sku}}</view>
						<view>x{{item.cartNum}}</view>
					</view>
				</view>
			</view>
			<view class="price-line acea-row row-between"><view>商品总价</view><view>￥{{order.proTotalPrice}}</view></view>
			<view class="price-line acea-row row-between" v-if="order.payPostage > 0"><view>配送费</view><view>￥{{order.payPostage}}</view></view>
			<view class="price-line total acea-row row-between"><view>实付款</view><view>￥{{order.payPrice}}</view></view>
		</view>
		<view class="section">
			<view class="info acea-row row-between"><view>订单编号</view><view>{{order.orderId}}</view></view>
			<view class="info acea-row row-between"><view>下单时间</view><view>{{order.createTime}}</view></view>
			<view class="info acea-row row-between" v-if="order.campusAppointmentDate"><view>预约配送</view><view>{{order.campusAppointmentDate}} {{formatAppointmentSlot(order.campusAppointmentSlot)}}</view></view>
			<view class="info acea-row row-between"><view>餐具份数</view><view>{{order.cutleryCount ? order.cutleryCount + '份' : '无需餐具'}}</view></view>
			<view class="mark" v-if="order.mark">
				<view>买家留言</view>
				<view class="mark-text">{{order.mark}}</view>
			</view>
		</view>
		<view class="section refund" v-if="canRefund">
			<view class="refund-title">退款处理</view>
			<view class="refund-line acea-row row-between row-middle">
				<view>可退金额</view>
				<view>￥{{order.payPrice}}</view>
			</view>
			<view class="refund-field">
				<view>退款金额</view>
				<input v-model="refundAmount" type="digit" placeholder="输入本次退款金额" />
			</view>
			<view class="refund-field">
				<view>拒绝原因</view>
				<textarea v-model="refundReason" placeholder="拒绝退款时填写原因"></textarea>
			</view>
		</view>
		<view class="footer acea-row row-right row-middle" v-if="canProcess || canRefund">
			<view class="btn plain" v-if="canProcess" @tap="reject">拒单</view>
			<view class="btn fill" v-if="canProcess" @tap="accept">接单</view>
			<view class="btn plain" v-if="canRefund" @tap="refundRefuse">拒绝退款</view>
			<view class="btn fill" v-if="canRefund" @tap="refund">确认退款</view>
		</view>
	</view>
</template>

<script>
	import {
		campusMerchantOrderDetailApi,
		campusMerchantOrderAcceptApi,
		campusMerchantOrderRejectApi,
		campusMerchantOrderRefundApi,
		campusMerchantOrderRefundRefuseApi
	} from '@/api/campus.js';
	const app = getApp();

	export default {
		data() {
			return {
				theme: app.globalData.theme,
				orderNo: '',
				order: {},
				refundAmount: '',
				refundReason: ''
			};
		},
		computed: {
			canProcess() {
				return this.order.paid && this.order.refundStatus === 0 && this.order.campusStatus === 10;
			},
			canRefund() {
				return this.order.paid && this.order.refundStatus === 1;
			},
			statusText() {
				if (this.order.refundStatus === 1 || this.order.refundStatus === 3) return '退款中';
				if (this.order.refundStatus === 2) return '已退款';
				if (this.order.campusStatus === 10) return '待接单';
				if (this.order.campusStatus === 20) return '配送中';
				if (this.order.campusStatus === -10) return '已取消';
				return this.order.orderStatusMsg || '待支付';
			}
		},
		onLoad(options) {
			this.orderNo = options.orderNo || '';
		},
		onShow() {
			if (this.orderNo) this.getDetail();
		},
		methods: {
			formatAppointmentSlot(slot) {
				return slot ? slot.replace(',', ' - ') : '';
			},
			getDetail() {
				campusMerchantOrderDetailApi(this.orderNo).then(res => {
					this.order = res.data || {};
					this.refundAmount = this.order.refundApplyPrice || this.order.payPrice || '';
				}).catch(err => this.$util.Tips({ title: err }));
			},
			accept() {
				campusMerchantOrderAcceptApi(this.orderNo).then(() => {
					this.order.campusStatus = 20;
					this.$util.Tips({ title: '接单成功', icon: 'success' });
				}).catch(err => this.$util.Tips({ title: err }));
			},
			reject() {
				uni.showModal({
					title: '拒绝订单',
					content: '拒单将发起全额退款，确认继续？',
					success: res => {
						if (!res.confirm) return;
						campusMerchantOrderRejectApi(this.orderNo).then(() => {
							this.order.campusStatus = -10;
							this.order.refundStatus = 3;
							this.$util.Tips({ title: '拒单已提交', icon: 'success' });
						}).catch(err => this.$util.Tips({ title: err }));
					}
				});
			},
			refund() {
				let amount = Number(this.refundAmount);
				if (!amount || amount <= 0) {
					this.$util.Tips({ title: '请输入有效退款金额' });
					return;
				}
				campusMerchantOrderRefundApi({
					orderNo: this.orderNo,
					amount: amount
				}).then(() => {
					this.order.refundStatus = 3;
					this.$util.Tips({ title: '退款已提交', icon: 'success' });
				}).catch(err => this.$util.Tips({ title: err }));
			},
			refundRefuse() {
				if (!this.refundReason) {
					this.$util.Tips({ title: '请填写拒绝原因' });
					return;
				}
				campusMerchantOrderRefundRefuseApi(this.orderNo, this.refundReason).then(() => {
					this.order.refundStatus = 0;
					this.$util.Tips({ title: '已拒绝退款', icon: 'success' });
				}).catch(err => this.$util.Tips({ title: err }));
			}
		}
	};
</script>

<style scoped lang="scss">
	.merchant-order-detail {
		min-height: 100vh;
		padding: 24rpx 24rpx 128rpx;
		background: #f5f5f5;
		color: #282828;
	}
	.status,
	.section {
		margin-bottom: 20rpx;
		border-radius: 8rpx;
		background: #fff;
	}
	.status {
		padding: 34rpx 28rpx;
	}
	.status .title {
		color: #e93323;
		font-size: 36rpx;
		font-weight: 600;
	}
	.status .time {
		margin-top: 10rpx;
		color: #999;
		font-size: 24rpx;
	}
	.section {
		padding: 26rpx;
	}
	.strong {
		font-size: 29rpx;
		font-weight: 600;
	}
	.strong text {
		margin-left: 16rpx;
		font-weight: 400;
	}
	.address {
		margin-top: 12rpx;
		color: #666;
		font-size: 25rpx;
		line-height: 38rpx;
	}
	.product {
		margin-bottom: 24rpx;
	}
	.product image {
		width: 112rpx;
		height: 112rpx;
		border-radius: 6rpx;
	}
	.product-info {
		width: calc(100% - 132rpx);
		font-size: 25rpx;
	}
	.name {
		width: calc(100% - 126rpx);
	}
	.price {
		width: 112rpx;
		text-align: right;
	}
	.meta {
		margin-top: 15rpx;
		color: #888;
	}
	.price-line,
	.info {
		min-height: 54rpx;
		color: #666;
		font-size: 25rpx;
	}
	.price-line.total {
		padding-top: 12rpx;
		border-top: 1rpx solid #f2f2f2;
		color: #282828;
		font-size: 29rpx;
	}
	.price-line.total view:last-child {
		color: #e93323;
	}
	.info view:last-child {
		max-width: 480rpx;
		text-align: right;
		word-break: break-all;
	}
	.mark {
		padding-top: 16rpx;
		border-top: 1rpx solid #f2f2f2;
		color: #666;
		font-size: 25rpx;
	}
	.mark-text {
		margin-top: 10rpx;
		color: #282828;
		line-height: 40rpx;
		word-break: break-all;
	}
	.refund-title {
		margin-bottom: 18rpx;
		font-size: 29rpx;
		font-weight: 600;
	}
	.refund-line,
	.refund-field {
		color: #666;
		font-size: 25rpx;
	}
	.refund-line view:last-child {
		color: #e93323;
	}
	.refund-field {
		margin-top: 18rpx;
	}
	.refund-field input,
	.refund-field textarea {
		box-sizing: border-box;
		width: 100%;
		margin-top: 10rpx;
		padding: 16rpx 18rpx;
		border: 1rpx solid #eee;
		border-radius: 6rpx;
		background: #fafafa;
		color: #282828;
		font-size: 25rpx;
	}
	.refund-field textarea {
		height: 112rpx;
	}
	.footer {
		position: fixed;
		right: 0;
		bottom: 0;
		left: 0;
		height: 104rpx;
		padding: 0 24rpx;
		background: #fff;
		box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.05);
	}
	.btn {
		min-width: 150rpx;
		height: 62rpx;
		margin-left: 16rpx;
		padding: 0 28rpx;
		border-radius: 8rpx;
		font-size: 27rpx;
		line-height: 62rpx;
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
