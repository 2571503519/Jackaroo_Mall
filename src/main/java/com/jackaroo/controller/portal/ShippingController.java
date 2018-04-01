package com.jackaroo.controller.portal;


import com.github.pagehelper.PageInfo;
import com.jackaroo.common.Const;
import com.jackaroo.common.ResponseCode;
import com.jackaroo.common.ServerResponse;
import com.jackaroo.pojo.Shipping;
import com.jackaroo.pojo.User;
import com.jackaroo.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/shipping/")
public class ShippingController {

    @Autowired
    private IShippingService iShippingService;

    @RequestMapping("add.do")
    @ResponseBody
    public ServerResponse add(HttpSession session, Shipping shipping) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode()
                    , ResponseCode.NEED_LOGIN.getDesc());
        } else {
            return iShippingService.add(user.getId(), shipping);
        }
    }


    @RequestMapping("del.do")
    @ResponseBody
    public ServerResponse delete(HttpSession session, Integer shippingId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode()
                    , ResponseCode.NEED_LOGIN.getDesc());
        } else {
            return iShippingService.delete(user.getId(), shippingId);
        }
    }

    @RequestMapping("update.do")
    @ResponseBody
    public ServerResponse update(HttpSession session, Shipping shipping) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode()
                    , ResponseCode.NEED_LOGIN.getDesc());
        } else {
            return iShippingService.update(user.getId(), shipping);
        }
    }

    @RequestMapping("select.do")
    @ResponseBody
    public ServerResponse<Shipping> select(HttpSession session, Integer shippingId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode()
                    , ResponseCode.NEED_LOGIN.getDesc());
        } else {
            return iShippingService.select(user.getId(), shippingId);
        }
    }

    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> list(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum
                                        , @RequestParam(value = "pageSize", defaultValue = "10") int pageSize
                                        , HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode()
                    , ResponseCode.NEED_LOGIN.getDesc());
        } else {
            return iShippingService.list(user.getId(), pageNum, pageSize);
        }
    }











}