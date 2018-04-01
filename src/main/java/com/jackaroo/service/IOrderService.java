package com.jackaroo.service;

import com.github.pagehelper.PageInfo;
import com.jackaroo.common.ServerResponse;
import com.jackaroo.vo.OrderVo;

import java.util.Map;

public interface IOrderService {

    ServerResponse createOrder(Integer userId, Integer shippingId);

    ServerResponse<String> cancel(Integer userId,Long orderNo);

    ServerResponse getOrderCartProduct(Integer userId);

    ServerResponse<OrderVo> getOrderDetail(Integer userId, Long orderNo);

    ServerResponse<PageInfo> getOrderList(Integer userId, int pageNum, int pageSize);

    ServerResponse aliCallback(Map<String,String> params);

    ServerResponse pay(Long orderNo,Integer userId,String path);

    ServerResponse queryOrderPayStatus(Integer userId,Long orderNo);

    ServerResponse<PageInfo> manageList(int pageNum, int pageSize);

    ServerResponse<OrderVo> manageDetail(Long orderNo);

    ServerResponse<PageInfo> manageSearch(Long orderNo,int pageNum,int pageSize);

    ServerResponse<String> manageSendGoods(Long orderNo);
}
