package com.jackaroo.common;

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


}
