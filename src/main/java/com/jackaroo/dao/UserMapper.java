package com.jackaroo.dao;

import com.jackaroo.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);
    // 插入用户记录，不论对象的属性是否为空
    int insert(User record);
    // 插入用户记录，不插入空值
    int insertSelective(User record);
    // 通过主键查询
    User selectByPrimaryKey(Integer id);
    // 通过主键更新，不会更新对象中值为空的属性对应的数据库中的字段
    int updateByPrimaryKeySelective(User record);
    // 通过主键更新，会更新对象中值为空的属性对应的数据库中的字段
    int updateByPrimaryKey(User record);
    // 校验用户名是否被使用
    int checkUsername(String str);
    // 校验邮箱是否被使用
    int checkEmail(String str);
    // 根据用户名和密码查询用户
    User selectLogin(@Param("username") String username, @Param("password") String password);
    // 根据用户名查询密保问题
    String selectQuestionByUsername(String username);
    // 根据用户名、密保问题和密保答案查询
    int checkAnswer(@Param("username") String username, @Param("question") String question, @Param("answer") String answer);
    // 根据用户名更新密码
    int updatePasswordByUsername(@Param("username") String username, @Param("password") String password);
    // 根据userId校验密码
    int checkPassword(@Param(value="password")String password,@Param("userId")Integer userId);
    // 根据userId校验邮箱
    int checkEmailByUserId(@Param(value="email")String email,@Param(value="userId")Integer userId);
}