package com.jackaroo.service;

import com.github.pagehelper.PageInfo;
import com.jackaroo.common.ServerResponse;
import com.jackaroo.pojo.Shipping;

public interface IShippingService {

    ServerResponse add(Integer userId, Shipping shipping);

    ServerResponse delete(Integer userId, Integer shippingId);

    ServerResponse<Shipping> select(Integer userId, Integer shippingId);

    ServerResponse update(Integer userId, Shipping shipping);

    ServerResponse<PageInfo> list(Integer userId, int pageNum, int pageSize);

}
