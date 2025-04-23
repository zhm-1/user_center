package com.zhm.user_center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhm.user_center.common.ErrorCode;
import com.zhm.user_center.exception.BusinessException;
import com.zhm.user_center.model.domain.User;
import com.zhm.user_center.service.UserService;
import com.zhm.user_center.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.zhm.user_center.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author zhm
* @description 针对表【user】的数据库操作Service实现
* @createDate 2025-04-02 14:38:16
*/
@Service
@Slf4j
public  class UserServiceImpl extends ServiceImpl<UserMapper, User>

    implements UserService{
    @Resource
    UserMapper userMapper;
    /**
     * 盐值，混淆密码
     */
    private final String SALT = "zhm";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        // 1.校验
        if(StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账户过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "校验密码过短");
        }
        if (planetCode.length() > 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编码过长");
        }
        // 定义特殊字符的正则表达式
        String validPattern = "\"[a-zA-Z0-9_.-]+\"";
        // 创建匹配器对用户账户进行校验
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            // 如果匹配到特殊字符，则返回错误
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户有特殊字符");
        }
        // 密码和校验密码不相等
        if(!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码和校验密码不相等");
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已存在");
        }
        // 星球编号不能重复
        QueryWrapper<User> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq("planetCode", planetCode);
        long count1 = userMapper.selectCount(queryWrapper1);
        if (count1 > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号已存在");
        }

        // 2.加密
        final String SALT = "zhm";
        String newPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes(StandardCharsets.UTF_8));

        // 3.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(newPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user);
        if(!saveResult){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册用户失败");
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest httpServletRequest) {
        // 1.校验
        if(StringUtils.isAnyBlank(userAccount, userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号信息不对");
        }
        if (userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号长度小于4");
        }
        if (userPassword.length() < 6 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码长度小于6");
        }
        // 定义特殊字符的正则表达式
        String validPattern = "\"[a-zA-Z0-9_.-]+\"";
        // 创建匹配器对用户账户进行校验
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            // 如果匹配到特殊字符，则返回错误
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号有特殊字符");
        }

        // 2.加密
        String newPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes(StandardCharsets.UTF_8));
        // 查询用户是否存在
        QueryWrapper<User> QueryWrapper = new QueryWrapper<>();
        QueryWrapper.eq("userAccount", userAccount);
        QueryWrapper.eq("userPassword", newPassword);
        User user = userMapper.selectOne(QueryWrapper);
        if(user == null){
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        // 3.判断用户是否已经登录
        Object attribute = httpServletRequest.getSession().getAttribute(USER_LOGIN_STATE);
        if(attribute != null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已登录");
        }
        // 4.记录用户的登录状态
        httpServletRequest.getSession().setAttribute(USER_LOGIN_STATE, user);
        // 5.用户脱敏
        return getSafetyUser(user);
    }

    /**
     * 用户脱敏
     * @param originUser
     * @return
     */
    public User getSafetyUser(User originUser){
        if (originUser == null){
            return null;
        }
        User safeUser = new User();
        safeUser.setId(originUser.getId());
        safeUser.setUserName(originUser.getUserName());
        safeUser.setUserAccount(originUser.getUserAccount());
        safeUser.setUserPassword(null);
        safeUser.setAvatarUrl(originUser.getAvatarUrl());
        safeUser.setGender(originUser.getGender());
        safeUser.setPhone(originUser.getPhone());
        safeUser.setEmail(originUser.getEmail());
        safeUser.setUserStatus(originUser.getUserStatus());
        safeUser.setCreateTime(originUser.getCreateTime());
        safeUser.setUpdateTime(originUser.getUpdateTime());
        safeUser.setUserRole(originUser.getUserRole());
        safeUser.setPlanetCode(originUser.getPlanetCode());
        return safeUser;
    }

    @Override
    public Integer userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }
}




