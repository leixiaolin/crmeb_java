<template>
	<view :data-theme="theme" class="merchant-replies">
		<view class="tabs">
			<view class="tab" :class="{ on: replied === '' }" @tap="changeTab('')">全部</view>
			<view class="tab" :class="{ on: replied === false }" @tap="changeTab(false)">待回复</view>
			<view class="tab" :class="{ on: replied === true }" @tap="changeTab(true)">已回复</view>
		</view>
		<view class="reply" v-for="(item,index) in replyList" :key="item.id">
			<view class="head acea-row row-between row-top">
				<image :src="item.storeProduct ? item.storeProduct.image : ''"></image>
				<view class="product">
					<view class="name line2">{{item.storeProduct ? item.storeProduct.storeName : '商品已失效'}}</view>
					<view class="meta">商品 {{item.productId}} · {{item.createTime}}</view>
				</view>
			</view>
			<view class="scores acea-row">
				<view>商家商品 {{item.productScore || 0}} 分</view>
				<view>配送服务 {{item.serviceScore || 0}} 分</view>
			</view>
			<view class="user">{{item.nickname || '匿名用户'}}</view>
			<view class="comment">{{item.comment}}</view>
			<view class="merchant-reply" v-if="item.isReply">
				<view>商家回复</view>
				<text>{{item.merchantReplyContent}}</text>
			</view>
			<view class="reply-form" v-else>
				<textarea v-model="item.editReply" maxlength="512" placeholder="填写回复内容" />
				<view class="btn fill" @tap="submitReply(item,index)">提交回复</view>
			</view>
		</view>
		<view class="loadingicon acea-row row-center-wrapper" v-if="replyList.length">
			<text class="loading iconfont icon-jiazai" :hidden="!loading"></text>{{loadTitle}}
		</view>
		<emptyPage v-if="!loading && !replyList.length" title="暂无评价~"></emptyPage>
	</view>
</template>

<script>
	import emptyPage from '@/components/emptyPage.vue';
	import {
		campusMerchantReplyListApi,
		campusMerchantReplyCommentApi
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
				replied: false,
				replyList: [],
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
			changeTab(replied) {
				if (this.replied === replied) return;
				this.replied = replied;
				this.reset();
			},
			reset() {
				this.page = 1;
				this.loadend = false;
				this.replyList = [];
				this.getList();
			},
			getList() {
				if (this.loading || this.loadend) return;
				this.loading = true;
				let data = {
					page: this.page,
					limit: this.limit
				};
				if (this.replied !== '') data.replied = this.replied;
				campusMerchantReplyListApi(data).then(res => {
					let list = (res.data.list || []).map(item => {
						item.editReply = '';
						return item;
					});
					this.replyList = this.replyList.concat(list);
					this.loadend = list.length < this.limit;
					this.page = this.page + 1;
					this.loadTitle = this.loadend ? '我也是有底线的' : '加载更多';
					this.loading = false;
				}).catch(err => {
					this.loading = false;
					this.$util.Tips({ title: err });
				});
			},
			submitReply(item, index) {
				let content = (item.editReply || '').trim();
				if (!content) {
					this.$util.Tips({ title: '请填写回复内容' });
					return;
				}
				campusMerchantReplyCommentApi({
					ids: item.id,
					merchantReplyContent: content
				}).then(() => {
					this.$set(this.replyList[index], 'isReply', true);
					this.$set(this.replyList[index], 'merchantReplyContent', content);
					this.$util.Tips({ title: '回复已提交', icon: 'success' });
				}).catch(err => this.$util.Tips({ title: err }));
			}
		}
	};
</script>

<style scoped lang="scss">
	.merchant-replies {
		min-height: 100vh;
		padding: 24rpx 24rpx 48rpx;
		background: #f5f5f5;
		color: #282828;
	}
	.tabs {
		display: grid;
		grid-template-columns: repeat(3, minmax(0, 1fr));
		height: 76rpx;
		margin-bottom: 22rpx;
		padding: 6rpx;
		border-radius: 8rpx;
		background: #fff;
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
	.reply {
		margin-bottom: 20rpx;
		padding: 26rpx;
		border-radius: 8rpx;
		background: #fff;
	}
	.head image {
		width: 108rpx;
		height: 108rpx;
		border-radius: 6rpx;
		background: #f4f4f4;
	}
	.product {
		width: calc(100% - 128rpx);
	}
	.name {
		min-height: 60rpx;
		font-size: 28rpx;
		font-weight: 600;
	}
	.meta,
	.user {
		color: #888;
		font-size: 23rpx;
	}
	.scores {
		gap: 16rpx;
		margin-top: 18rpx;
		color: #b65c15;
		font-size: 24rpx;
	}
	.scores view {
		padding: 6rpx 12rpx;
		border-radius: 6rpx;
		background: #fff4e6;
	}
	.user {
		margin-top: 18rpx;
	}
	.comment {
		margin-top: 10rpx;
		color: #333;
		font-size: 26rpx;
		line-height: 38rpx;
		word-break: break-all;
	}
	.merchant-reply,
	.reply-form {
		margin-top: 18rpx;
		padding-top: 18rpx;
		border-top: 1rpx solid #f2f2f2;
	}
	.merchant-reply view {
		margin-bottom: 8rpx;
		color: #888;
		font-size: 23rpx;
	}
	.merchant-reply text {
		font-size: 25rpx;
		line-height: 36rpx;
	}
	.reply-form textarea {
		box-sizing: border-box;
		width: 100%;
		height: 132rpx;
		padding: 18rpx;
		border-radius: 6rpx;
		background: #f7f7f7;
		font-size: 25rpx;
	}
	.btn {
		min-width: 132rpx;
		height: 54rpx;
		margin: 16rpx 0 0 auto;
		padding: 0 24rpx;
		border-radius: 8rpx;
		font-size: 25rpx;
		line-height: 54rpx;
		text-align: center;
	}
	.btn.fill {
		background: #e93323;
		color: #fff;
	}
</style>
