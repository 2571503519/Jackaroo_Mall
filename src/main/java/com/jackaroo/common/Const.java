package com.jackaroo.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * 常量类，用于存储常用的常量
 */
public class Const {

    // 将登录用户存入session中时的key
    public static final String CURRENT_USER = "currentUser";
    public static final String EMAIL = "email";
    public static final String USERNAME = "username";

    public interface Role {
        // 普通用户
        int ROLE_CUSTOMER = 0;
        // 管理员用户
        int ROLE_ADMIN = 1;
    }

    public interface ProductListOrderBy {
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc", "price_asc");
    }

    public interface Cart{
        int CHECKED = 1;//即购物车选中状态
        int UN_CHECKED = 0;//购物车中未选中状态

        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
    }

    public enum ProductStatusEnum {
        ON_SALE(1, "在线");
        private String value;
        private int code;
        ProductStatusEnum(int code, String value) {
            this.code = code;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
    }


}
