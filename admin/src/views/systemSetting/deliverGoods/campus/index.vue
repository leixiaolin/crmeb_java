<template>
  <div class="divBox campus-config">
    <el-card shadow="never">
      <div slot="header" class="toolbar">
        <el-form inline :model="schoolQuery" @submit.native.prevent>
          <el-form-item label="学校/校区">
            <el-input v-model="schoolQuery.keywords" size="small" clearable placeholder="请输入学校或校区名称" />
          </el-form-item>
          <el-button type="primary" size="small" @click="searchSchools">搜索</el-button>
        </el-form>
        <el-button v-hasPermi="['admin:campus:school:save']" type="primary" size="small" @click="openSchoolDialog()">
          新增学校
        </el-button>
      </div>
      <el-table v-loading="schoolLoading" :data="schoolList" size="small" highlight-current-row @row-click="selectSchool">
        <el-table-column prop="schoolName" label="学校" min-width="150" />
        <el-table-column prop="campusName" label="校区" min-width="150" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="启用" width="130">
          <template slot-scope="{ row }">
            <el-switch
              v-if="hasPermi(['admin:campus:school:update:status'])"
              v-model="row.status"
              :active-value="true"
              :inactive-value="false"
              @change="changeSchoolStatus(row)"
            />
            <span v-else>{{ row.status ? '启用' : '停用' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template slot-scope="{ row }">
            <a v-hasPermi="['admin:campus:school:update']" @click.stop="openSchoolDialog(row)">编辑</a>
            <el-divider direction="vertical" />
            <a v-hasPermi="['admin:campus:school:delete']" @click.stop="deleteSchool(row)">删除</a>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        class="mt20"
        :current-page="schoolQuery.page"
        :page-size="schoolQuery.limit"
        :page-sizes="[10, 20, 40]"
        :total="schoolTotal"
        layout="total, sizes, prev, pager, next"
        background
        @current-change="changeSchoolPage"
        @size-change="changeSchoolSize"
      />
    </el-card>

    <el-row v-if="currentSchool" :gutter="14" class="mt14">
      <el-col :xs="24" :lg="10">
        <el-card shadow="never">
          <div slot="header" class="card-title">{{ currentSchool.schoolName }} - 配送规则</div>
          <el-form ref="deliveryForm" :model="deliveryForm" :rules="deliveryRules" label-width="110px">
            <el-form-item label="起送价" prop="startPrice">
              <el-input-number v-model="deliveryForm.startPrice" :min="0" :precision="2" :step="1" />
            </el-form-item>
            <el-form-item label="雨天附加费">
              <el-switch v-model="deliveryForm.rainFeeEnabled" />
            </el-form-item>
            <el-form-item label="附加金额" prop="rainFee">
              <el-input-number v-model="deliveryForm.rainFee" :min="0" :precision="2" :step="1" />
            </el-form-item>
            <el-form-item>
              <el-button
                v-hasPermi="['admin:campus:delivery:config:save']"
                type="primary"
                :loading="deliverySaving"
                @click="saveDeliveryConfig"
              >
                保存规则
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="14">
        <el-card shadow="never">
          <div slot="header" class="toolbar">
            <span class="card-title">宿舍楼栋</span>
            <el-button
              v-hasPermi="['admin:campus:building:save']"
              type="primary"
              size="small"
              @click="openBuildingDialog()"
            >
              新增楼栋
            </el-button>
          </div>
          <el-table
            v-loading="buildingLoading"
            :data="buildingList"
            size="small"
            highlight-current-row
            @row-click="selectBuilding"
          >
            <el-table-column prop="buildingName" label="楼栋" min-width="120" />
            <el-table-column prop="maxFloor" label="最高楼层" width="95" />
            <el-table-column prop="sort" label="排序" width="70" />
            <el-table-column label="启用" width="110">
              <template slot-scope="{ row }">
                <el-switch
                  v-if="hasPermi(['admin:campus:building:update:status'])"
                  v-model="row.status"
                  :active-value="true"
                  :inactive-value="false"
                  @change="changeBuildingStatus(row)"
                />
                <span v-else>{{ row.status ? '启用' : '停用' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template slot-scope="{ row }">
                <a v-hasPermi="['admin:campus:building:update']" @click.stop="openBuildingDialog(row)">编辑</a>
                <el-divider direction="vertical" />
                <a v-hasPermi="['admin:campus:building:delete']" @click.stop="deleteBuilding(row)">删除</a>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            class="mt20"
            :current-page="buildingQuery.page"
            :page-size="buildingQuery.limit"
            :page-sizes="[10, 20, 40]"
            :total="buildingTotal"
            layout="total, sizes, prev, pager, next"
            background
            @current-change="changeBuildingPage"
            @size-change="changeBuildingSize"
          />
        </el-card>
      </el-col>
    </el-row>

    <el-card v-if="currentSchool" shadow="never" class="mt14">
      <div slot="header" class="toolbar">
        <span class="card-title">{{ currentSchool.schoolName }} - 可服务商家</span>
        <el-button
          v-hasPermi="['admin:campus:store:save']"
          type="primary"
          size="small"
          @click="openStoreDialog()"
        >
          绑定商家
        </el-button>
      </div>
      <el-table v-loading="storeLoading" :data="storeList" size="small">
        <el-table-column prop="storeId" label="门店 ID" width="90" />
        <el-table-column label="商家" min-width="160">
          <template slot-scope="{ row }">{{ row.systemStore ? row.systemStore.name : '-' }}</template>
        </el-table-column>
        <el-table-column label="电话" min-width="130">
          <template slot-scope="{ row }">{{ row.systemStore ? row.systemStore.phone : '-' }}</template>
        </el-table-column>
        <el-table-column label="营业时间" min-width="150">
          <template slot-scope="{ row }">{{ row.systemStore ? row.systemStore.dayTime : '-' }}</template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="服务状态" width="130">
          <template slot-scope="{ row }">
            <el-switch
              v-if="hasPermi(['admin:campus:store:update:status'])"
              v-model="row.status"
              :active-value="true"
              :inactive-value="false"
              @change="changeStoreStatus(row)"
            />
            <span v-else>{{ row.status ? '启用' : '停用' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template slot-scope="{ row }">
            <a v-hasPermi="['admin:campus:store:update']" @click="openStoreDialog(row)">编辑</a>
            <el-divider direction="vertical" />
            <a v-hasPermi="['admin:campus:store:delete']" @click="deleteStore(row)">解绑</a>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        class="mt20"
        :current-page="storeQuery.page"
        :page-size="storeQuery.limit"
        :page-sizes="[10, 20, 40]"
        :total="storeTotal"
        layout="total, sizes, prev, pager, next"
        background
        @current-change="changeStorePage"
        @size-change="changeStoreSize"
      />
    </el-card>

    <el-card v-if="currentBuilding" shadow="never" class="mt14">
      <div slot="header" class="toolbar">
        <span class="card-title">{{ currentBuilding.buildingName }} - 楼层配送费</span>
        <el-button
          v-hasPermi="['admin:campus:delivery:fee:save']"
          type="primary"
          size="small"
          @click="openFeeDialog()"
        >
          新增楼层费
        </el-button>
      </div>
      <el-table v-loading="feeLoading" :data="feeList" size="small">
        <el-table-column prop="floorNo" label="楼层" min-width="120" />
        <el-table-column prop="deliveryFee" label="配送费" min-width="120" />
        <el-table-column prop="updateTime" label="更新时间" min-width="170" />
        <el-table-column label="操作" width="150">
          <template slot-scope="{ row }">
            <a v-hasPermi="['admin:campus:delivery:fee:update']" @click="openFeeDialog(row)">编辑</a>
            <el-divider direction="vertical" />
            <a v-hasPermi="['admin:campus:delivery:fee:delete']" @click="deleteFee(row)">删除</a>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-empty v-if="!currentSchool && !schoolLoading" description="请先选择学校维护校园配送配置" />

    <el-dialog :title="schoolDialog.id ? '编辑学校' : '新增学校'" :visible.sync="schoolDialog.visible" width="460px">
      <el-form ref="schoolForm" :model="schoolDialog.form" :rules="schoolRules" label-width="90px">
        <el-form-item label="学校" prop="schoolName"><el-input v-model="schoolDialog.form.schoolName" maxlength="64" /></el-form-item>
        <el-form-item label="校区" prop="campusName"><el-input v-model="schoolDialog.form.campusName" maxlength="64" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="schoolDialog.form.sort" :min="0" :step="1" /></el-form-item>
        <el-form-item label="启用"><el-switch v-model="schoolDialog.form.status" /></el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="schoolDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="schoolDialog.saving" @click="saveSchool">确定</el-button>
      </span>
    </el-dialog>

    <el-dialog :title="buildingDialog.id ? '编辑楼栋' : '新增楼栋'" :visible.sync="buildingDialog.visible" width="460px">
      <el-form ref="buildingForm" :model="buildingDialog.form" :rules="buildingRules" label-width="90px">
        <el-form-item label="楼栋" prop="buildingName"><el-input v-model="buildingDialog.form.buildingName" maxlength="64" /></el-form-item>
        <el-form-item label="最高楼层" prop="maxFloor"><el-input-number v-model="buildingDialog.form.maxFloor" :min="1" :step="1" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="buildingDialog.form.sort" :min="0" :step="1" /></el-form-item>
        <el-form-item label="启用"><el-switch v-model="buildingDialog.form.status" /></el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="buildingDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="buildingDialog.saving" @click="saveBuilding">确定</el-button>
      </span>
    </el-dialog>

    <el-dialog :title="storeDialog.id ? '编辑服务商家' : '绑定服务商家'" :visible.sync="storeDialog.visible" width="520px">
      <el-form ref="storeForm" :model="storeDialog.form" :rules="storeRules" label-width="90px">
        <el-form-item label="商家" prop="storeId">
          <el-select
            v-model="storeDialog.form.storeId"
            filterable
            clearable
            class="store-select"
            placeholder="选择已启用门店"
            :disabled="Boolean(storeDialog.id)"
          >
            <el-option
              v-for="item in storeCandidates"
              :key="item.id"
              :label="item.name + ' #' + item.id"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="storeDialog.form.sort" :min="0" :step="1" /></el-form-item>
        <el-form-item label="启用"><el-switch v-model="storeDialog.form.status" /></el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="storeDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="storeDialog.saving" @click="saveStore">确定</el-button>
      </span>
    </el-dialog>

    <el-dialog :title="feeDialog.id ? '编辑楼层费' : '新增楼层费'" :visible.sync="feeDialog.visible" width="420px">
      <el-form ref="feeForm" :model="feeDialog.form" :rules="feeRules" label-width="90px">
        <el-form-item label="楼层" prop="floorNo"><el-input-number v-model="feeDialog.form.floorNo" :min="1" :step="1" /></el-form-item>
        <el-form-item label="配送费" prop="deliveryFee"><el-input-number v-model="feeDialog.form.deliveryFee" :min="0" :precision="2" :step="1" /></el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="feeDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="feeDialog.saving" @click="saveFee">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { checkPermi } from '@/utils/permission'
import * as campus from '@/api/campus'

const schoolForm = () => ({ schoolName: '', campusName: '', status: true, sort: 0 })
const buildingForm = () => ({ buildingName: '', maxFloor: 1, status: true, sort: 0 })
const feeForm = () => ({ floorNo: 1, deliveryFee: 0 })
const storeForm = () => ({ storeId: '', status: true, sort: 0 })

export default {
  name: 'CampusConfig',
  data() {
    return {
      schoolQuery: { page: 1, limit: 10, keywords: '' },
      schoolList: [],
      schoolTotal: 0,
      schoolLoading: false,
      currentSchool: null,
      deliveryForm: { schoolId: 0, startPrice: 0, rainFeeEnabled: false, rainFee: 0 },
      deliverySaving: false,
      buildingQuery: { page: 1, limit: 10 },
      buildingList: [],
      buildingTotal: 0,
      buildingLoading: false,
      currentBuilding: null,
      storeQuery: { page: 1, limit: 10 },
      storeList: [],
      storeTotal: 0,
      storeLoading: false,
      storeCandidates: [],
      feeList: [],
      feeLoading: false,
      schoolDialog: { visible: false, id: 0, saving: false, form: schoolForm() },
      buildingDialog: { visible: false, id: 0, saving: false, form: buildingForm() },
      storeDialog: { visible: false, id: 0, saving: false, form: storeForm() },
      feeDialog: { visible: false, id: 0, saving: false, form: feeForm() },
      schoolRules: {
        schoolName: [{ required: true, message: '请输入学校名称', trigger: 'blur' }],
        campusName: [{ required: true, message: '请输入校区名称', trigger: 'blur' }]
      },
      deliveryRules: {
        startPrice: [{ required: true, message: '请填写起送价', trigger: 'change' }],
        rainFee: [{ required: true, message: '请填写雨天附加费', trigger: 'change' }]
      },
      buildingRules: {
        buildingName: [{ required: true, message: '请输入楼栋名称', trigger: 'blur' }],
        maxFloor: [{ required: true, message: '请填写最高楼层', trigger: 'change' }]
      },
      storeRules: {
        storeId: [{ required: true, message: '请选择商家', trigger: 'change' }]
      },
      feeRules: {
        floorNo: [{ required: true, message: '请填写楼层', trigger: 'change' }],
        deliveryFee: [{ required: true, message: '请填写配送费', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getSchoolList()
  },
  methods: {
    hasPermi: checkPermi,
    getSchoolList() {
      this.schoolLoading = true
      campus.campusSchoolListApi(this.schoolQuery)
        .then((res) => {
          this.schoolList = res.list
          this.schoolTotal = res.total
          if (!this.currentSchool && this.schoolList.length) this.selectSchool(this.schoolList[0])
        })
        .finally(() => {
          this.schoolLoading = false
        })
    },
    searchSchools() {
      this.schoolQuery.page = 1
      this.getSchoolList()
    },
    changeSchoolPage(page) {
      this.schoolQuery.page = page
      this.getSchoolList()
    },
    changeSchoolSize(limit) {
      this.schoolQuery.limit = limit
      this.schoolQuery.page = 1
      this.getSchoolList()
    },
    selectSchool(row) {
      this.currentSchool = row
      this.currentBuilding = null
      this.feeList = []
      this.getDeliveryConfig()
      this.buildingQuery.page = 1
      this.getBuildingList()
      this.storeQuery.page = 1
      this.getStoreList()
    },
    openSchoolDialog(row) {
      this.schoolDialog.id = row ? row.id : 0
      this.schoolDialog.form = row
        ? { schoolName: row.schoolName, campusName: row.campusName, status: row.status, sort: row.sort }
        : schoolForm()
      this.schoolDialog.visible = true
      this.$nextTick(() => this.$refs.schoolForm && this.$refs.schoolForm.clearValidate())
    },
    saveSchool() {
      this.$refs.schoolForm.validate((valid) => {
        if (!valid) return
        this.schoolDialog.saving = true
        const action = this.schoolDialog.id
          ? campus.campusSchoolUpdateApi(this.schoolDialog.id, this.schoolDialog.form)
          : campus.campusSchoolSaveApi(this.schoolDialog.form)
        action.then(() => {
          this.$message.success('学校保存成功')
          this.schoolDialog.visible = false
          this.getSchoolList()
        }).finally(() => {
          this.schoolDialog.saving = false
        })
      })
    },
    changeSchoolStatus(row) {
      campus.campusSchoolStatusApi({ id: row.id, status: row.status }).then(() => {
        this.$message.success('学校状态已更新')
      }).catch(() => {
        row.status = !row.status
      })
    },
    deleteSchool(row) {
      this.$modalSure('删除该学校及其配置入口吗？').then(() => {
        campus.campusSchoolDeleteApi({ id: row.id }).then(() => {
          if (this.currentSchool && this.currentSchool.id === row.id) this.currentSchool = null
          this.$message.success('学校删除成功')
          this.getSchoolList()
        })
      })
    },
    getDeliveryConfig() {
      campus.campusDeliveryConfigApi({ schoolId: this.currentSchool.id }).then((res) => {
        this.deliveryForm = {
          schoolId: this.currentSchool.id,
          startPrice: res && res.startPrice !== null ? res.startPrice : 0,
          rainFeeEnabled: res ? res.rainFeeEnabled : false,
          rainFee: res && res.rainFee !== null ? res.rainFee : 0
        }
      })
    },
    saveDeliveryConfig() {
      this.$refs.deliveryForm.validate((valid) => {
        if (!valid) return
        this.deliverySaving = true
        campus.campusDeliveryConfigSaveApi(this.deliveryForm).then(() => {
          this.$message.success('配送规则保存成功')
        }).finally(() => {
          this.deliverySaving = false
        })
      })
    },
    getBuildingList() {
      this.buildingLoading = true
      campus.campusBuildingListApi({ schoolId: this.currentSchool.id, ...this.buildingQuery })
        .then((res) => {
          this.buildingList = res.list
          this.buildingTotal = res.total
          if (!this.currentBuilding && this.buildingList.length) this.selectBuilding(this.buildingList[0])
        })
        .finally(() => {
          this.buildingLoading = false
        })
    },
    changeBuildingPage(page) {
      this.buildingQuery.page = page
      this.getBuildingList()
    },
    changeBuildingSize(limit) {
      this.buildingQuery.limit = limit
      this.buildingQuery.page = 1
      this.getBuildingList()
    },
    selectBuilding(row) {
      this.currentBuilding = row
      this.getFeeList()
    },
    openBuildingDialog(row) {
      this.buildingDialog.id = row ? row.id : 0
      this.buildingDialog.form = row
        ? { buildingName: row.buildingName, maxFloor: row.maxFloor, status: row.status, sort: row.sort }
        : buildingForm()
      this.buildingDialog.visible = true
      this.$nextTick(() => this.$refs.buildingForm && this.$refs.buildingForm.clearValidate())
    },
    saveBuilding() {
      this.$refs.buildingForm.validate((valid) => {
        if (!valid) return
        this.buildingDialog.saving = true
        const data = { schoolId: this.currentSchool.id, ...this.buildingDialog.form }
        const action = this.buildingDialog.id
          ? campus.campusBuildingUpdateApi(this.buildingDialog.id, data)
          : campus.campusBuildingSaveApi(data)
        action.then(() => {
          this.$message.success('楼栋保存成功')
          this.buildingDialog.visible = false
          this.getBuildingList()
        }).finally(() => {
          this.buildingDialog.saving = false
        })
      })
    },
    changeBuildingStatus(row) {
      campus.campusBuildingStatusApi({ id: row.id, status: row.status }).then(() => {
        this.$message.success('楼栋状态已更新')
      }).catch(() => {
        row.status = !row.status
      })
    },
    deleteBuilding(row) {
      this.$modalSure('删除该楼栋及楼层配送费入口吗？').then(() => {
        campus.campusBuildingDeleteApi({ id: row.id }).then(() => {
          if (this.currentBuilding && this.currentBuilding.id === row.id) this.currentBuilding = null
          this.$message.success('楼栋删除成功')
          this.getBuildingList()
        })
      })
    },
    getStoreList() {
      this.storeLoading = true
      campus.campusStoreListApi({ schoolId: this.currentSchool.id, ...this.storeQuery })
        .then((res) => {
          this.storeList = res.list
          this.storeTotal = res.total
        })
        .finally(() => {
          this.storeLoading = false
        })
    },
    getStoreCandidates() {
      campus.campusStoreCandidateListApi({ page: 1, limit: 100 }).then((res) => {
        this.storeCandidates = res.list
      })
    },
    changeStorePage(page) {
      this.storeQuery.page = page
      this.getStoreList()
    },
    changeStoreSize(limit) {
      this.storeQuery.limit = limit
      this.storeQuery.page = 1
      this.getStoreList()
    },
    openStoreDialog(row) {
      this.storeDialog.id = row ? row.id : 0
      this.storeDialog.form = row ? { storeId: row.storeId, status: row.status, sort: row.sort } : storeForm()
      this.storeDialog.visible = true
      this.getStoreCandidates()
      this.$nextTick(() => this.$refs.storeForm && this.$refs.storeForm.clearValidate())
    },
    saveStore() {
      this.$refs.storeForm.validate((valid) => {
        if (!valid) return
        this.storeDialog.saving = true
        const data = { schoolId: this.currentSchool.id, ...this.storeDialog.form }
        const action = this.storeDialog.id
          ? campus.campusStoreUpdateApi(this.storeDialog.id, data)
          : campus.campusStoreSaveApi(data)
        action.then(() => {
          this.$message.success('服务商家保存成功')
          this.storeDialog.visible = false
          this.getStoreList()
        }).finally(() => {
          this.storeDialog.saving = false
        })
      })
    },
    changeStoreStatus(row) {
      campus.campusStoreStatusApi({ id: row.id, status: row.status }).then(() => {
        this.$message.success('商家服务状态已更新')
      }).catch(() => {
        row.status = !row.status
      })
    },
    deleteStore(row) {
      this.$modalSure('解绑该学校的服务商家吗？').then(() => {
        campus.campusStoreDeleteApi({ id: row.id }).then(() => {
          this.$message.success('服务商家已解绑')
          this.getStoreList()
        })
      })
    },
    getFeeList() {
      this.feeLoading = true
      campus.campusFloorFeeListApi({ buildingId: this.currentBuilding.id }).then((res) => {
        this.feeList = res
      }).finally(() => {
        this.feeLoading = false
      })
    },
    openFeeDialog(row) {
      this.feeDialog.id = row ? row.id : 0
      this.feeDialog.form = row ? { floorNo: row.floorNo, deliveryFee: row.deliveryFee } : feeForm()
      this.feeDialog.visible = true
      this.$nextTick(() => this.$refs.feeForm && this.$refs.feeForm.clearValidate())
    },
    saveFee() {
      this.$refs.feeForm.validate((valid) => {
        if (!valid) return
        this.feeDialog.saving = true
        const data = { buildingId: this.currentBuilding.id, ...this.feeDialog.form }
        const action = this.feeDialog.id
          ? campus.campusFloorFeeUpdateApi(this.feeDialog.id, data)
          : campus.campusFloorFeeSaveApi(data)
        action.then(() => {
          this.$message.success('楼层配送费保存成功')
          this.feeDialog.visible = false
          this.getFeeList()
        }).finally(() => {
          this.feeDialog.saving = false
        })
      })
    },
    deleteFee(row) {
      this.$modalSure('删除该楼层配送费吗？').then(() => {
        campus.campusFloorFeeDeleteApi({ id: row.id }).then(() => {
          this.$message.success('楼层配送费删除成功')
          this.getFeeList()
        })
      })
    }
  }
}
</script>

<style scoped lang="scss">
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.toolbar .el-form-item {
  margin-bottom: 0;
}

.card-title {
  font-weight: 600;
}

.store-select {
  width: 100%;
}

@media (max-width: 1199px) {
  .campus-config .el-col + .el-col {
    margin-top: 14px;
  }
}
</style>
