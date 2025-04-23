package com.zhm.user_center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhm.user_center.model.domain.User;

import javax.servlet.http.HttpServletRequest;

/**
* @author zhm
* @description 针对表【user】的数据库操作Service
* @createDate 2025-04-02 14:38:16
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param checkPassword 用户校验密码
     * @return  新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode);

    /**
     * 用户登录
     * @param account
     * @param userPassword
     * @param request
     * @return
     */
    User userLogin(String account, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     * @param request
     * @return
     */
    Integer userLogout(HttpServletRequest request);
}
