<template>
	<view :data-theme="theme" class="campus-address-edit">
		<view class="form-panel borRadius14">
			<view class="form-row"><text>联系人</text><input v-model="form.contactName" maxlength="32" placeholder="请输入联系人" /></view>
			<view class="form-row"><text>联系电话</text><input v-model="form.contactPhone" type="number" maxlength="11" placeholder="请输入手机号" /></view>
			<view class="form-row">
				<text>学校校区</text>
				<picker :range="schoolNames" :value="schoolIndex" @change="changeSchool">
					<view class="picker">{{schoolNames[schoolIndex] || '请选择学校校区'}}</view>
				</picker>
			</view>
			<view class="form-row">
				<text>宿舍楼栋</text>
				<picker :range="buildingNames" :value="buildingIndex" @change="changeBuilding">
					<view class="picker">{{buildingNames[buildingIndex] || '请选择楼栋'}}</view>
				</picker>
			</view>
			<view class="form-row"><text>楼层</text><input v-model="form.floorNo" type="number" placeholder="请输入楼层" @blur="refreshQuote" /></view>
			<view class="form-row"><text>宿舍号</text><input v-model="form.roomNo" maxlength="32" placeholder="例如 302" /></view>
			<view class="form-row"><text>备注</text><input v-model="form.remark" maxlength="128" placeholder="可填写配送说明" /></view>
		</view>
		<view class="default-row borRadius14">
			<checkbox-group @change="changeDefault"><checkbox :checked="form.isDefault" />设为默认校园地址</checkbox-group>
		</view>
		<view class="quote borRadius14" v-if="quote">
			<view>当前楼层配送费：<text>￥{{quote.deliveryFee}}</text></view>
			<view v-if="quote.rainFeeEnabled">雨天附加费：￥{{quote.rainFee}}</view>
			<view>学校起送价：￥{{quote.startPrice}}</view>
		</view>
		<button class="save-button bg_color" @click="saveAddress">保存校园地址</button>
	</view>
</template>

<script>
	import {
		campusSchoolListApi,
		campusBuildingListApi,
		campusAddressDetailApi,
		campusAddressEditApi,
		campusDeliveryQuoteApi
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
				id: 0,
				form: {
					id: 0,
					schoolId: 0,
					buildingId: 0,
					floorNo: '',
					roomNo: '',
					contactName: '',
					contactPhone: '',
					isDefault: false,
					remark: ''
				},
				schools: [],
				buildings: [],
				schoolIndex: 0,
				buildingIndex: 0,
				quote: null,
				theme: app.globalData.theme
			}
		},
		computed: {
			...mapGetters(['isLogin']),
			schoolNames() {
				return this.schools.map(item => item.schoolName + ' ' + item.campusName);
			},
			buildingNames() {
				return this.buildings.map(item => item.buildingName);
			}
		},
		onLoad(options) {
			if (!this.isLogin) return toLogin();
			this.id = options.id || 0;
			uni.setNavigationBarTitle({
				title: this.id ? '编辑校园地址' : '添加校园地址'
			});
			this.loadSchools();
		},
		methods: {
			loadSchools() {
				campusSchoolListApi().then(res => {
					this.schools = res.data || [];
					if (this.id) return this.loadDetail();
					if (this.schools.length) this.selectSchool(0);
				}).catch(err => {
					this.$util.Tips({ title: err });
				});
			},
			loadDetail() {
				campusAddressDetailApi(this.id).then(res => {
					this.form = Object.assign({}, this.form, res.data || {});
					let index = this.schools.findIndex(item => item.id === this.form.schoolId);
					this.selectSchool(index > -1 ? index : 0, this.form.buildingId);
				});
			},
			selectSchool(index, buildingId) {
				let school = this.schools[index];
				if (!school) return;
				this.schoolIndex = index;
				this.form.schoolId = school.id;
				campusBuildingListApi({ schoolId: school.id }).then(res => {
					this.buildings = res.data || [];
					let nextIndex = buildingId ? this.buildings.findIndex(item => item.id === buildingId) : 0;
					this.selectBuilding(nextIndex > -1 ? nextIndex : 0);
				});
			},
			selectBuilding(index) {
				let building = this.buildings[index];
				this.buildingIndex = index;
				this.form.buildingId = building ? building.id : 0;
				this.refreshQuote();
			},
			changeSchool(e) {
				this.form.buildingId = 0;
				this.selectSchool(parseInt(e.detail.value));
			},
			changeBuilding(e) {
				this.selectBuilding(parseInt(e.detail.value));
			},
			changeDefault(e) {
				this.form.isDefault = e.detail.value.length > 0;
			},
			refreshQuote() {
				if (!this.form.buildingId || !this.form.floorNo) {
					this.quote = null;
					return;
				}
				campusDeliveryQuoteApi({
					buildingId: this.form.buildingId,
					floorNo: this.form.floorNo
				}).then(res => {
					this.quote = res.data;
				}).catch(() => {
					this.quote = null;
				});
			},
			saveAddress() {
				if (!this.form.contactName) return this.$util.Tips({ title: '请输入联系人' });
				if (!/^1(3|4|5|6|7|8|9)\d{9}$/i.test(this.form.contactPhone)) return this.$util.Tips({ title: '请输入正确手机号' });
				if (!this.form.schoolId || !this.form.buildingId) return this.$util.Tips({ title: '请选择学校和楼栋' });
				if (!this.form.floorNo || parseInt(this.form.floorNo) < 1) return this.$util.Tips({ title: '请输入正确楼层' });
				if (!this.form.roomNo) return this.$util.Tips({ title: '请输入宿舍号' });
				this.form.id = this.id;
				campusAddressEditApi(this.form).then(() => {
					this.$util.Tips({
						title: '校园地址保存成功',
						icon: 'success'
					}, () => {
						uni.navigateBack({ delta: 1 });
					});
				}).catch(err => {
					this.$util.Tips({ title: err });
				});
			}
		}
	}
</script>

<style lang="scss" scoped>
	.bg_color {
		@include main_bg_color(theme);
	}

	.campus-address-edit {
		padding: 24rpx 30rpx 40rpx;
	}

	.form-panel,
	.default-row,
	.quote {
		background: #fff;
		margin-bottom: 20rpx;
		padding: 0 24rpx;
	}

	.form-row {
		display: flex;
		align-items: center;
		min-height: 98rpx;
		border-bottom: 1rpx solid #eee;
		font-size: 28rpx;
	}

	.form-row:last-child {
		border-bottom: none;
	}

	.form-row text {
		width: 170rpx;
		color: #333;
	}

	.form-row input,
	.form-row picker,
	.picker {
		flex: 1;
		color: #555;
		min-width: 0;
	}

	.default-row {
		padding: 28rpx 24rpx;
		font-size: 28rpx;
	}

	.quote {
		padding: 24rpx;
		font-size: 26rpx;
		line-height: 42rpx;
		color: #666;
	}

	.quote text {
		color: #e93323;
		font-weight: 600;
	}

	.save-button {
		color: #fff;
		border-radius: 44rpx;
		height: 88rpx;
		line-height: 88rpx;
		font-size: 30rpx;
	}
</style>
