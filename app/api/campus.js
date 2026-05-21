import request from '@/utils/request.js';

export function campusSchoolListApi() {
  return request.get('campus/school/list', {}, { noAuth: true });
}

export function campusBuildingListApi(data) {
  return request.get('campus/building/list', data, { noAuth: true });
}

export function campusAddressListApi(data) {
  return request.get('campus/address/list', data);
}

export function campusAddressEditApi(data) {
  return request.post('campus/address/edit', data);
}

export function campusAddressDetailApi(id) {
  return request.get('campus/address/detail/' + id);
}

export function campusAddressDeleteApi(id) {
  return request.post('campus/address/del', { id });
}

export function campusAddressDefaultApi() {
  return request.get('campus/address/default');
}

export function campusAddressSetDefaultApi(id) {
  return request.post('campus/address/default/set', { id });
}

export function campusDeliveryQuoteApi(data) {
  return request.get('campus/delivery/quote', data);
}

export function campusStoreListApi(data) {
  return request.get('campus/store/list', data, { noAuth: true });
}
