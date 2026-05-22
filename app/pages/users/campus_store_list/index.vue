<template>
	<view :data-theme="theme" class="campus-store-page">
		<view class="school-panel borRadius14">
			<view class="school-label">当前学校</view>
			<picker :range="schoolNames" :value="schoolIndex" @change="changeSchool">
				<view class="school-picker">{{schoolNames[schoolIndex] || '请选择学校校区'}}</view>
			</picker>
		</view>
		<view class="search-panel borRadius14">
			<input v-model="keyword" confirm-type="search" placeholder="搜索商家或商品" @confirm="search" />
			<view class="search-btn" @click="search">{{keyword ? '搜索' : '全部'}}</view>
		</view>
		<view class="filter-panel borRadius14">
			<view class="sort-tabs">
				<view class="sort-tab" :class="{ on: sortMode === 'default' }" @click="changeSort('default')">综合</view>
				<view class="sort-tab" :class="{ on: sortMode === 'score' }" @click="changeSort('score')">评分优先</view>
			</view>
			<view class="open-filter" :class="{ on: showOpenOnly }" @click="toggleOpenOnly">只看营业中</view>
		</view>
		<view class="product-result" v-if="searching && productList.length">
			<view class="result-title">商品结果</view>
			<view class="product-grid">
				<view class="product-item borRadius14" v-for="item in productList" :key="item.id" @click="openProduct(item)">
					<image :src="item.image" mode="aspectFill"></image>
					<view class="product-name line2">{{item.storeName}}</view>
					<view class="product-price">￥{{item.price}}</view>
				</view>
			</view>
		</view>
		<view class="result-title" v-if="searching">商家结果</view>
		<view class="store-list" v-if="displayStores.length">
			<view class="store-item borRadius14" v-for="item in displayStores" :key="item.id" @click="openStore(item)">
				<image class="store-logo" :src="item.systemStore.image" mode="aspectFill"></image>
				<view class="store-main">
					<view class="store-name line1">{{item.systemStore.name}}</view>
					<view class="store-state" :class="{ closed: !item.openNow }">{{item.openNow ? '营业中' : '休息中'}}</view>
					<view class="store-info line1" v-if="item.systemStore.introduction">{{item.systemStore.introduction}}</view>
					<view class="store-rating">
						<text>{{item.replyCount ? item.replyScore + '分' : '暂无评分'}}</text>
						<text>{{item.replyCount || 0}}条评价</text>
					</view>
					<view class="store-meta">
						<text v-if="item.startPrice !== null && item.startPrice !== undefined">起送 ￥{{item.startPrice}}</text>
						<text v-if="item.systemStore.dayTime">营业 {{item.systemStore.dayTime}}</text>
						<text v-if="item.systemStore.phone" @click="callStore(item.systemStore.phone)">联系商家</text>
					</view>
					<view class="store-address line1">{{item.systemStore.address}} {{item.systemStore.detailedAddress}}</view>
				</view>
			</view>
		</view>
		<view class="empty" v-else-if="loaded">{{emptyText}}</view>
	</view>
</template>

<script>
	import {
		campusSchoolListApi,
		campusStoreListApi,
		campusSearchApi
	} from '@/api/campus.js';
	import { goShopDetail } from '@/libs/order.js';
	import { mapGetters } from 'vuex';
	let app = getApp();
	export default {
		data() {
			return {
				schools: [],
				schoolIndex: 0,
				schoolId: 0,
				keyword: '',
				storeList: [],
				searchStoreList: [],
				productList: [],
				sortMode: 'default',
				showOpenOnly: false,
				searching: false,
				loaded: false,
				theme: app.globalData.theme
			}
		},
		computed: {
			...mapGetters(['uid']),
			schoolNames() {
				return this.schools.map(item => item.schoolName + ' ' + item.campusName);
			},
			displayStores() {
				let list = this.searching ? this.searchStoreList : this.storeList;
				if (this.showOpenOnly) list = list.filter(item => item.openNow);
				if (this.sortMode === 'score') {
					return list.slice().sort((left, right) => {
						let scoreDiff = Number(right.replyScore || 0) - Number(left.replyScore || 0);
						if (scoreDiff) return scoreDiff;
						return Number(right.replyCount || 0) - Number(left.replyCount || 0);
					});
				}
				return list;
			},
			emptyText() {
				if (this.showOpenOnly) return '当前筛选下暂无营业中商家';
				return this.searching ? '未找到可下单结果' : '当前学校还没有可服务商家';
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
				this.resetSearch();
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
			changeSort(mode) {
				this.sortMode = mode;
			},
			toggleOpenOnly() {
				this.showOpenOnly = !this.showOpenOnly;
			},
			resetSearch() {
				this.searching = false;
				this.searchStoreList = [];
				this.productList = [];
			},
			search() {
				if (!this.keyword) {
					this.resetSearch();
					return;
				}
				this.loaded = false;
				campusSearchApi({
					schoolId: this.schoolId,
					keyword: this.keyword,
					page: 1,
					limit: 10
				}).then(res => {
					this.searchStoreList = res.data.storeList || [];
					this.productList = res.data.productList || [];
					this.searching = true;
					this.loaded = true;
				}).catch(err => {
					this.searching = true;
					this.searchStoreList = [];
					this.productList = [];
					this.loaded = true;
					this.$util.Tips({ title: err });
				});
			},
			callStore(phone) {
				uni.makePhoneCall({ phoneNumber: phone });
			},
			openStore(item) {
				uni.navigateTo({
					url: '/pages/users/campus_store_detail/index?schoolId=' + item.schoolId + '&storeId=' + item.storeId
				});
			},
			openProduct(item) {
				goShopDetail(item, this.uid).then(() => {
					uni.navigateTo({
						url: '/pages/goods/goods_details/index?id=' + item.id
					});
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

	.search-panel {
		display: flex;
		align-items: center;
		height: 76rpx;
		margin-bottom: 20rpx;
		padding: 0 12rpx 0 24rpx;
		background: #fff;
	}

	.search-panel input {
		flex: 1;
		font-size: 26rpx;
	}

	.search-btn {
		width: 104rpx;
		height: 54rpx;
		border-radius: 6rpx;
		background: #e93323;
		color: #fff;
		font-size: 25rpx;
		line-height: 54rpx;
		text-align: center;
	}

	.filter-panel {
		display: flex;
		align-items: center;
		justify-content: space-between;
		min-height: 76rpx;
		margin-bottom: 20rpx;
		padding: 14rpx 18rpx;
		background: #fff;
	}

	.sort-tabs {
		display: flex;
		align-items: center;
		gap: 12rpx;
	}

	.sort-tab,
	.open-filter {
		height: 48rpx;
		padding: 0 18rpx;
		border-radius: 8rpx;
		background: #f4f5f7;
		color: #666;
		font-size: 24rpx;
		line-height: 48rpx;
	}

	.sort-tab.on,
	.open-filter.on {
		background: #fff0ec;
		color: #e93323;
	}

	.result-title {
		color: #282828;
		font-size: 29rpx;
		font-weight: 600;
		margin: 26rpx 0 16rpx;
	}

	.product-grid {
		display: flex;
		flex-wrap: wrap;
		justify-content: space-between;
	}

	.product-item {
		box-sizing: border-box;
		width: calc(50% - 10rpx);
		margin-bottom: 20rpx;
		padding: 18rpx;
		background: #fff;
	}

	.product-item image {
		width: 100%;
		height: 276rpx;
		border-radius: 8rpx;
		background: #f4f4f4;
	}

	.product-name {
		min-height: 72rpx;
		color: #282828;
		font-size: 26rpx;
		line-height: 36rpx;
		margin-top: 12rpx;
	}

	.product-price {
		color: #e93323;
		font-size: 29rpx;
		font-weight: 600;
		margin-top: 8rpx;
	}

	.store-state {
		display: inline-flex;
		align-items: center;
		height: 34rpx;
		line-height: 34rpx;
		font-size: 21rpx;
		color: #16863c;
		background: #e9f8ef;
		border-radius: 6rpx;
		padding: 0 10rpx;
		margin-top: 8rpx;
	}

	.store-state.closed {
		color: #8c4a13;
		background: #fff2df;
	}

	.store-info,
	.store-address {
		font-size: 24rpx;
		line-height: 36rpx;
		color: #777;
		margin-top: 8rpx;
	}

	.store-rating {
		display: flex;
		flex-wrap: wrap;
		gap: 12rpx;
		color: #b65c15;
		font-size: 23rpx;
		line-height: 34rpx;
		margin-top: 8rpx;
	}

	.store-rating text:last-child {
		color: #777;
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
