<template>
	<view :data-theme="theme" class="merchant-specs">
		<view class="spec" v-for="item in specList" :key="item.id">
			<view class="title">{{item.suk || item.attrValue || '默认规格'}}</view>
			<view class="fields acea-row row-between">
				<view class="field">
					<view>价格</view>
					<input v-model="item.editPrice" type="digit" />
				</view>
				<view class="field">
					<view>库存</view>
					<input v-model="item.editStock" type="number" />
				</view>
			</view>
		</view>
		<view class="footer" v-if="specList.length">
			<view class="btn" @tap="save">保存规格</view>
		</view>
		<emptyPage v-if="!loading && !specList.length" title="暂无商品规格~"></emptyPage>
	</view>
</template>

<script>
	import emptyPage from '@/components/emptyPage.vue';
	import {
		campusMerchantProductSpecListApi,
		campusMerchantProductSpecUpdateApi
	} from '@/api/campus.js';
	const app = getApp();

	export default {
		components: { emptyPage },
		data() {
			return {
				theme: app.globalData.theme,
				productId: 0,
				specList: [],
				loading: false
			};
		},
		onLoad(options) {
			this.productId = Number(options.productId || 0);
			if (this.productId) this.getSpecs();
		},
		methods: {
			getSpecs() {
				this.loading = true;
				campusMerchantProductSpecListApi(this.productId).then(res => {
					this.specList = (res.data || []).map(item => {
						item.editPrice = item.price;
						item.editStock = item.stock;
						return item;
					});
					this.loading = false;
				}).catch(err => {
					this.loading = false;
					this.$util.Tips({ title: err });
				});
			},
			save() {
				let invalid = this.specList.some(item => {
					let price = Number(item.editPrice);
					let stock = Number(item.editStock);
					return !price || price <= 0 || stock < 0 || !Number.isInteger(stock);
				});
				if (invalid) {
					this.$util.Tips({ title: '规格价格和库存格式不正确' });
					return;
				}
				campusMerchantProductSpecUpdateApi(this.productId, {
					attrValueList: this.specList.map(item => ({
						id: item.id,
						price: Number(item.editPrice),
						stock: Number(item.editStock)
					}))
				}).then(() => {
					this.$util.Tips({ title: '规格已更新', icon: 'success' });
				}).catch(err => this.$util.Tips({ title: err }));
			}
		}
	};
</script>

<style scoped lang="scss">
	.merchant-specs {
		min-height: 100vh;
		padding: 24rpx 24rpx 138rpx;
		background: #f5f5f5;
		color: #282828;
	}
	.spec {
		margin-bottom: 20rpx;
		padding: 26rpx;
		border-radius: 8rpx;
		background: #fff;
	}
	.title {
		font-size: 28rpx;
		font-weight: 600;
		line-height: 40rpx;
		word-break: break-all;
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
	.footer {
		position: fixed;
		right: 0;
		bottom: 0;
		left: 0;
		padding: 18rpx 24rpx calc(18rpx + env(safe-area-inset-bottom));
		background: #fff;
		box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.05);
	}
	.btn {
		height: 72rpx;
		border-radius: 8rpx;
		background: #e93323;
		color: #fff;
		font-size: 28rpx;
		line-height: 72rpx;
		text-align: center;
	}
</style>
