import request from '@/utils/request'

export function campusSchoolListApi(params) {
  return request({ url: '/admin/campus/school/list', method: 'get', params })
}

export function campusSchoolSaveApi(data) {
  return request({ url: '/admin/campus/school/save', method: 'post', data })
}

export function campusSchoolUpdateApi(id, data) {
  return request({ url: '/admin/campus/school/update', method: 'post', params: { id }, data })
}

export function campusSchoolStatusApi(params) {
  return request({ url: '/admin/campus/school/update/status', method: 'get', params })
}

export function campusSchoolDeleteApi(params) {
  return request({ url: '/admin/campus/school/delete', method: 'get', params })
}

export function campusBuildingListApi(params) {
  return request({ url: '/admin/campus/building/list', method: 'get', params })
}

export function campusBuildingSaveApi(data) {
  return request({ url: '/admin/campus/building/save', method: 'post', data })
}

export function campusBuildingUpdateApi(id, data) {
  return request({ url: '/admin/campus/building/update', method: 'post', params: { id }, data })
}

export function campusBuildingStatusApi(params) {
  return request({ url: '/admin/campus/building/update/status', method: 'get', params })
}

export function campusBuildingDeleteApi(params) {
  return request({ url: '/admin/campus/building/delete', method: 'get', params })
}

export function campusDeliveryConfigApi(params) {
  return request({ url: '/admin/campus/delivery/config', method: 'get', params })
}

export function campusDeliveryConfigSaveApi(data) {
  return request({ url: '/admin/campus/delivery/config/save', method: 'post', data })
}

export function campusFloorFeeListApi(params) {
  return request({ url: '/admin/campus/delivery/fee/list', method: 'get', params })
}

export function campusFloorFeeSaveApi(data) {
  return request({ url: '/admin/campus/delivery/fee/save', method: 'post', data })
}

export function campusFloorFeeUpdateApi(id, data) {
  return request({ url: '/admin/campus/delivery/fee/update', method: 'post', params: { id }, data })
}

export function campusFloorFeeDeleteApi(params) {
  return request({ url: '/admin/campus/delivery/fee/delete', method: 'get', params })
}

export function campusStoreListApi(params) {
  return request({ url: '/admin/campus/store/list', method: 'get', params })
}

export function campusStoreCandidateListApi(params) {
  return request({ url: '/admin/campus/store/candidate/list', method: 'get', params })
}

export function campusStoreSaveApi(data) {
  return request({ url: '/admin/campus/store/save', method: 'post', data })
}

export function campusStoreUpdateApi(id, data) {
  return request({ url: '/admin/campus/store/update', method: 'post', params: { id }, data })
}

export function campusStoreStatusApi(params) {
  return request({ url: '/admin/campus/store/update/status', method: 'get', params })
}

export function campusStoreDeleteApi(params) {
  return request({ url: '/admin/campus/store/delete', method: 'get', params })
}
