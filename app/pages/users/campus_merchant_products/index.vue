<template>
	<view :data-theme="theme" class="merchant-products">
		<view class="search acea-row row-between row-middle">
			<input v-model="keywords" placeholder="搜索商品名称或编号" confirm-type="search" @confirm="reset" />
			<view class="search-btn" @tap="reset">搜索</view>
		</view>
		<view class="product" v-for="(item,index) in productList" :key="item.id">
			<view class="head acea-row row-between row-top">
				<image :src="item.image"></image>
				<view class="info">
					<view class="name line2">{{item.storeName}}</view>
					<view class="meta">商品编号 {{item.id}}</view>
					<view class="state" :class="{ off: !item.isShow }">{{item.isShow ? '售卖中' : '已下架'}}</view>
				</view>
			</view>
			<view class="fields acea-row row-between" v-if="!item.specType">
				<view class="field">
					<view>价格</view>
					<input v-model="item.editPrice" type="digit" />
				</view>
				<view class="field">
					<view>库存</view>
					<input v-model="item.editStock" type="number" />
				</view>
			</view>
			<view class="spec-tip" v-else>多规格商品请逐规格维护价格和库存</view>
			<view class="actions acea-row row-right row-middle">
				<view class="btn plain" v-if="item.isShow" @tap="changeShelf(item,index,false)">下架</view>
				<view class="btn plain" v-else @tap="changeShelf(item,index,true)">上架</view>
				<view class="btn fill" v-if="item.specType" @tap="goSpecs(item.id)">规格维护</view>
				<view class="btn fill" v-if="!item.specType" @tap="save(item,index)">保存</view>
			</view>
		</view>
		<view class="loadingicon acea-row row-center-wrapper" v-if="productList.length">
			<text class="loading iconfont icon-jiazai" :hidden="!loading"></text>{{loadTitle}}
		</view>
		<emptyPage v-if="!loading && !productList.length" title="暂无可维护商品~"></emptyPage>
	</view>
</template>

<script>
	import emptyPage from '@/components/emptyPage.vue';
	import {
		campusMerchantProductListApi,
		campusMerchantProductPutOnApi,
		campusMerchantProductOffShelfApi,
		campusMerchantProductSimpleUpdateApi
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
				keywords: '',
				productList: [],
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
			this.reset();
		},
		onReachBottom() {
			this.getList();
		},
		methods: {
			reset() {
				this.page = 1;
				this.loadend = false;
				this.productList = [];
				this.getList();
			},
			getList() {
				if (this.loading || this.loadend) return;
				this.loading = true;
				campusMerchantProductListApi({
					page: this.page,
					limit: this.limit,
					keywords: this.keywords
				}).then(res => {
					let list = (res.data.list || []).map(item => {
						item.editPrice = item.price;
						item.editStock = item.stock;
						return item;
					});
					this.productList = this.productList.concat(list);
					this.loadend = list.length < this.limit;
					this.page = this.page + 1;
					this.loadTitle = this.loadend ? '我也是有底线的' : '加载更多';
					this.loading = false;
				}).catch(err => {
					this.loading = false;
					this.$util.Tips({ title: err });
				});
			},
			changeShelf(item, index, putOn) {
				let shelfApi = putOn ? campusMerchantProductPutOnApi : campusMerchantProductOffShelfApi;
				shelfApi(item.id).then(() => {
					this.$set(this.productList[index], 'isShow', putOn);
					this.$util.Tips({ title: putOn ? '商品已上架' : '商品已下架', icon: 'success' });
				}).catch(err => this.$util.Tips({ title: err }));
			},
			goSpecs(productId) {
				uni.navigateTo({
					url: '/pages/users/campus_merchant_product_specs/index?productId=' + productId
				});
			},
			save(item, index) {
				let price = Number(item.editPrice);
				let stock = Number(item.editStock);
				if (!price || price <= 0) {
					this.$util.Tips({ title: '请输入有效价格' });
					return;
				}
				if (stock < 0 || !Number.isInteger(stock)) {
					this.$util.Tips({ title: '库存必须为非负整数' });
					return;
				}
				campusMerchantProductSimpleUpdateApi(item.id, {
					price: price,
					stock: stock
				}).then(() => {
					this.$set(this.productList[index], 'price', price);
					this.$set(this.productList[index], 'stock', stock);
					this.$util.Tips({ title: '商品已更新', icon: 'success' });
				}).catch(err => this.$util.Tips({ title: err }));
			}
		}
	};
</script>

<style scoped lang="scss">
	.merchant-products {
		min-height: 100vh;
		padding: 24rpx 24rpx 48rpx;
		background: #f5f5f5;
		color: #282828;
	}
	.search {
		height: 76rpx;
		margin-bottom: 22rpx;
		padding: 0 12rpx 0 24rpx;
		border-radius: 8rpx;
		background: #fff;
	}
	.search input {
		width: calc(100% - 132rpx);
		font-size: 26rpx;
	}
	.search-btn {
		width: 108rpx;
		height: 54rpx;
		border-radius: 6rpx;
		background: #e93323;
		color: #fff;
		font-size: 25rpx;
		line-height: 54rpx;
		text-align: center;
	}
	.product {
		margin-bottom: 20rpx;
		padding: 26rpx;
		border-radius: 8rpx;
		background: #fff;
	}
	.head image {
		width: 112rpx;
		height: 112rpx;
		border-radius: 6rpx;
	}
	.info {
		width: calc(100% - 132rpx);
	}
	.name {
		min-height: 62rpx;
		font-size: 28rpx;
		font-weight: 600;
	}
	.meta {
		margin-top: 6rpx;
		color: #888;
		font-size: 23rpx;
	}
	.state {
		display: inline-flex;
		margin-top: 8rpx;
		color: #169c58;
		font-size: 24rpx;
	}
	.state.off {
		color: #999;
	}
	.fields {
		margin-top: 22rpx;
	}
	.field {
		width: calc(50% - 10rpx);
		color: #666;
		font-size: 24rpx;
	}
	.field input {
		box-sizing: border-box;
		width: 100%;
		height: 66rpx;
		margin-top: 10rpx;
		padding: 0 18rpx;
		border: 1rpx solid #eee;
		border-radius: 6rpx;
		background: #fafafa;
		color: #282828;
		font-size: 27rpx;
	}
	.spec-tip {
		margin-top: 22rpx;
		padding: 18rpx;
		border-radius: 6rpx;
		background: #f7f7f7;
		color: #888;
		font-size: 24rpx;
	}
	.actions {
		margin-top: 22rpx;
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
