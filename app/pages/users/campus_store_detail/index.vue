<template>
	<view :data-theme="theme" class="campus-store-detail">
		<view class="store-head borRadius14" v-if="store">
			<image class="store-logo" :src="store.image" mode="aspectFill"></image>
			<view class="store-main">
				<view class="store-name">{{store.name}}</view>
				<view class="store-rating">{{replyCount ? replyScore + '分' : '暂无评分'}}<text>{{replyCount || 0}}条评价</text></view>
				<view class="store-line" v-if="store.dayTime">营业时间 {{store.dayTime}}</view>
				<view class="store-line line2">{{store.address}} {{store.detailedAddress}}</view>
			</view>
			<view class="phone" v-if="store.phone" @click="callStore">联系</view>
		</view>
		<view class="store-notice borRadius14" v-if="store && store.notice">
			<text>公告</text>{{store.notice}}
		</view>
		<view class="store-note borRadius14" v-if="store && store.introduction">{{store.introduction}}</view>
		<view class="store-closed borRadius14" v-if="store && openNow === false">商家休息中，当前可浏览商品，暂不可提交校园配送订单。</view>
		<view class="reply-section" v-if="replyLoaded">
			<view class="product-title">商家评价</view>
			<view class="reply-item borRadius14" v-for="item in replyList" :key="item.id">
				<view class="reply-head">
					<text>{{item.nickname || '匿名用户'}}</text>
					<text>{{item.createTime}}</text>
				</view>
				<view class="reply-score">商家商品 {{item.productScore || 0}} 分 · 配送服务 {{item.serviceScore || 0}} 分</view>
				<view class="reply-comment">{{item.comment}}</view>
				<view class="reply-product" v-if="item.storeProduct">{{item.storeProduct.storeName}}</view>
				<view class="reply-merchant" v-if="item.isReply">商家回复：{{item.merchantReplyContent}}</view>
			</view>
			<view class="empty reply-empty" v-if="!replyList.length">暂无评价</view>
		</view>
		<view class="product-title">商品</view>
		<scroll-view class="category-tabs" scroll-x v-if="categoryList.length">
			<view class="category-track">
				<view class="category-item" :class="{ active: !cid }" @click="changeCategory('')">All</view>
				<view class="category-item" :class="{ active: cid === String(item.id) }" v-for="item in categoryList" :key="item.id" @click="changeCategory(item.id)">{{item.name}}</view>
			</view>
		</scroll-view>
		<view class="product-grid" v-if="productList.length">
			<view class="product-item borRadius14" v-for="item in productList" :key="item.id" @click="goDetail(item)">
				<image class="product-image" :src="item.image" mode="aspectFill"></image>
				<view class="product-name line2">{{item.storeName}}</view>
				<view class="product-price">￥{{item.price}}</view>
			</view>
		</view>
		<view class="empty" v-else-if="loadend">当前商家还没有可售商品</view>
		<view class="loading" v-if="productList.length">{{loadTitle}}</view>
	</view>
</template>

<script>
	import {
		campusStoreListApi,
		campusStoreReplyListApi
	} from '@/api/campus.js';
	import {
		getProductslist,
		getStoreCategoryList
	} from '@/api/store.js';
	import {
		goShopDetail
	} from '@/libs/order.js';
	import {
		mapGetters
	} from 'vuex';
	let app = getApp();
	export default {
		data() {
			return {
				schoolId: 0,
				storeId: 0,
				store: null,
				openNow: null,
				replyScore: 0,
				replyCount: 0,
				replyList: [],
				replyLoaded: false,
				categoryList: [],
				cid: '',
				productList: [],
				page: 1,
				limit: 20,
				loading: false,
				loadend: false,
				loadTitle: '加载更多',
				theme: app.globalData.theme
			}
		},
		computed: mapGetters(['uid']),
		onLoad(options) {
			this.schoolId = Number(options.schoolId || 0);
			this.storeId = Number(options.storeId || 0);
			this.loadStore();
			this.loadReplies();
			this.loadCategories();
			this.getProducts();
		},
		onReachBottom() {
			this.getProducts();
		},
		methods: {
			loadStore() {
				campusStoreListApi({ schoolId: this.schoolId }).then(res => {
					let range = (res.data || []).find(item => item.storeId === this.storeId);
					this.store = range ? range.systemStore : null;
					this.openNow = range ? range.openNow : null;
					this.replyScore = range ? range.replyScore : 0;
					this.replyCount = range ? range.replyCount : 0;
					if (this.store) {
						uni.setNavigationBarTitle({ title: this.store.name });
					}
				}).catch(err => {
					this.$util.Tips({ title: err });
				});
			},
			loadCategories() {
				if (!this.storeId) return;
				getStoreCategoryList(this.storeId).then(res => {
					this.categoryList = res.data || [];
				}).catch(err => {
					this.$util.Tips({ title: err });
				});
			},
			loadReplies() {
				if (!this.schoolId || !this.storeId) return;
				campusStoreReplyListApi({
					schoolId: this.schoolId,
					storeId: this.storeId,
					page: 1,
					limit: 3
				}).then(res => {
					this.replyList = res.data.list || [];
					this.replyLoaded = true;
				}).catch(err => {
					this.replyLoaded = true;
					this.$util.Tips({ title: err });
				});
			},
			getProducts() {
				if (!this.storeId || this.loading || this.loadend) return;
				this.loading = true;
				getProductslist({
					merId: this.storeId,
					cid: this.cid,
					page: this.page,
					limit: this.limit
				}).then(res => {
					let list = res.data.list || [];
					this.productList = this.productList.concat(list);
					this.loadend = list.length < this.limit;
					this.loadTitle = this.loadend ? '已全部加载' : '加载更多';
					this.page = this.page + 1;
				}).catch(err => {
					this.loadend = true;
					this.$util.Tips({ title: err });
				}).finally(() => {
					this.loading = false;
				});
			},
			changeCategory(id) {
				let nextCid = id ? String(id) : '';
				if (this.cid === nextCid) return;
				this.cid = nextCid;
				this.productList = [];
				this.page = 1;
				this.loadend = false;
				this.getProducts();
			},
			goDetail(item) {
				goShopDetail(item, this.uid).then(() => {
					uni.navigateTo({
						url: '/pages/goods/goods_details/index?id=' + item.id
					});
				});
			},
			callStore() {
				uni.makePhoneCall({ phoneNumber: this.store.phone });
			}
		}
	}
</script>

<style lang="scss" scoped>
	.campus-store-detail {
		padding: 24rpx 30rpx 40rpx;
	}

	.store-head {
		display: flex;
		align-items: flex-start;
		background: #fff;
		padding: 24rpx;
	}

	.store-logo {
		width: 126rpx;
		height: 126rpx;
		border-radius: 10rpx;
		background: #f4f4f4;
		flex: 0 0 126rpx;
		margin-right: 20rpx;
	}

	.store-main {
		flex: 1;
		min-width: 0;
	}

	.store-name {
		font-size: 32rpx;
		font-weight: 600;
		color: #282828;
		margin-bottom: 8rpx;
	}

	.store-line,
	.store-note {
		font-size: 24rpx;
		line-height: 36rpx;
		color: #777;
	}

	.phone {
		color: #e93323;
		font-size: 24rpx;
		padding-left: 16rpx;
	}

	.store-note {
		background: #fff;
		padding: 22rpx 24rpx;
		margin-top: 20rpx;
	}

	.store-notice {
		color: #555;
		background: #fff7e8;
		font-size: 24rpx;
		line-height: 36rpx;
		padding: 22rpx 24rpx;
		margin-top: 20rpx;
	}

	.store-notice text {
		color: #b65c15;
		font-weight: 600;
		margin-right: 12rpx;
	}

	.store-rating {
		color: #b65c15;
		font-size: 24rpx;
		line-height: 34rpx;
		margin-bottom: 8rpx;
	}

	.store-rating text {
		color: #777;
		margin-left: 12rpx;
	}

	.store-closed {
		color: #8c4a13;
		background: #fff2df;
		font-size: 24rpx;
		line-height: 36rpx;
		padding: 22rpx 24rpx;
		margin-top: 20rpx;
	}

	.reply-item {
		background: #fff;
		margin-bottom: 16rpx;
		padding: 22rpx 24rpx;
	}

	.reply-head {
		display: flex;
		justify-content: space-between;
		color: #888;
		font-size: 22rpx;
		line-height: 32rpx;
	}

	.reply-score {
		color: #b65c15;
		font-size: 23rpx;
		line-height: 34rpx;
		margin-top: 10rpx;
	}

	.reply-comment {
		color: #333;
		font-size: 25rpx;
		line-height: 38rpx;
		margin-top: 10rpx;
		word-break: break-all;
	}

	.reply-product,
	.reply-merchant {
		color: #777;
		font-size: 23rpx;
		line-height: 34rpx;
		margin-top: 10rpx;
	}

	.reply-merchant {
		background: #f7f7f7;
		border-radius: 6rpx;
		padding: 12rpx 14rpx;
	}

	.reply-empty {
		background: #fff;
		border-radius: 8rpx;
		padding-top: 36rpx;
	}

	.product-title {
		font-size: 30rpx;
		font-weight: 600;
		color: #282828;
		margin: 30rpx 0 18rpx;
	}

	.category-tabs {
		white-space: nowrap;
		margin-bottom: 18rpx;
	}

	.category-track {
		display: inline-flex;
		min-width: 100%;
	}

	.category-item {
		flex: 0 0 auto;
		height: 58rpx;
		line-height: 58rpx;
		font-size: 24rpx;
		color: #666;
		background: #fff;
		border-radius: 8rpx;
		padding: 0 24rpx;
		margin-right: 14rpx;
	}

	.category-item.active {
		color: #fff;
		background: #e93323;
	}

	.product-grid {
		display: flex;
		flex-wrap: wrap;
		justify-content: space-between;
	}

	.product-item {
		width: calc(50% - 10rpx);
		background: #fff;
		padding: 18rpx;
		margin-bottom: 20rpx;
		box-sizing: border-box;
	}

	.product-image {
		width: 100%;
		height: 288rpx;
		border-radius: 8rpx;
		background: #f4f4f4;
	}

	.product-name {
		min-height: 72rpx;
		font-size: 26rpx;
		line-height: 36rpx;
		color: #282828;
		margin-top: 14rpx;
	}

	.product-price {
		font-size: 30rpx;
		font-weight: 600;
		color: #e93323;
		margin-top: 8rpx;
	}

	.loading,
	.empty {
		text-align: center;
		color: #999;
		font-size: 26rpx;
		padding: 36rpx 0;
	}

	.empty {
		padding-top: 100rpx;
	}
</style>
