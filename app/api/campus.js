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

export function campusStoreReplyListApi(data) {
  return request.get('campus/store/reply/list', data, { noAuth: true });
}

export function campusSearchApi(data) {
  return request.get('campus/search', data, { noAuth: true });
}

export function campusMerchantOrderListApi(data) {
  return request.get('campus/merchant/order/list', data);
}

export function campusMerchantAccessApi() {
  return request.get('campus/merchant/order/access');
}

export function campusMerchantTodaySummaryApi() {
  return request.get('campus/merchant/order/summary/today');
}

export function campusMerchantRefundListApi(data) {
  return request.get('campus/merchant/order/refund/list', data);
}

export function campusMerchantOrderDetailApi(orderNo) {
  return request.get('campus/merchant/order/detail/' + orderNo);
}

export function campusMerchantOrderAcceptApi(orderNo) {
  return request.post('campus/merchant/order/accept/' + orderNo);
}

export function campusMerchantOrderRejectApi(orderNo) {
  return request.post('campus/merchant/order/reject/' + orderNo);
}

export function campusMerchantOrderRefundApi(data) {
  return request.post('campus/merchant/order/refund', data);
}

export function campusMerchantOrderRefundRefuseApi(orderNo, reason) {
  return request.post('campus/merchant/order/refund/refuse/' + orderNo + '?reason=' + encodeURIComponent(reason));
}

export function campusMerchantProductListApi(data) {
  return request.get('campus/merchant/product/list', data);
}

export function campusMerchantProductPutOnApi(productId) {
  return request.post('campus/merchant/product/putOn/' + productId);
}

export function campusMerchantProductOffShelfApi(productId) {
  return request.post('campus/merchant/product/offShelf/' + productId);
}

export function campusMerchantProductSimpleUpdateApi(productId, data) {
  return request.post('campus/merchant/product/simple/' + productId, data);
}

export function campusMerchantProductSpecListApi(productId) {
  return request.get('campus/merchant/product/spec/' + productId);
}

export function campusMerchantProductSpecUpdateApi(productId, data) {
  return request.post('campus/merchant/product/spec/' + productId, data);
}

export function campusMerchantReplyListApi(data) {
  return request.get('campus/merchant/reply/list', data);
}

export function campusMerchantReplyCommentApi(data) {
  return request.post('campus/merchant/reply/comment', data);
}
