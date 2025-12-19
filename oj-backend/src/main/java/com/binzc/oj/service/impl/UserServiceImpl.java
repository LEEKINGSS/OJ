package com.binzc.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.binzc.oj.common.ErrorCode;
import com.binzc.oj.common.FileUtils;
import com.binzc.oj.exception.BusinessException;
import com.binzc.oj.mapper.UserMapper;
import com.binzc.oj.model.vo.LoginUserVO;
import com.binzc.oj.service.UserService;
import com.binzc.oj.model.entity.User;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.weaver.AbstractAnnotationAJ;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static com.binzc.oj.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author binzc
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2025-05-08 21:02:49
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService, Serializable {
    private static final String SALT = "binzc";
    private static final long serialVersionUID = 1L;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String userRole) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                //账号重复
                return -1;
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setUserRole(userRole);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 上传头像
     *
     * @param file
     * @param request
     * @return
     */

    @Override
    public String uploadAvatar(MultipartFile file, HttpServletRequest request) {
        //final User user = this.getLoginUser(request);
        //long userId=user.getId();
        long userId = 1;
        try {
            // 获取运行目录（backend），跳出到其父目录
            File projectDir = new File(System.getProperty("user.dir"));
            File saveDir = new File(projectDir, "static/images");

            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }

            // 构造文件名格式为 年月日_用户id.后缀
            String originalFilename = file.getOriginalFilename();
            String suffix = FileUtils.getFileExtension(originalFilename);
            String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String fileName = dateStr + "_" + userId + suffix;
            File dest = new File(saveDir, fileName);
            file.transferTo(dest);
            String imgUrl = ServletUriComponentsBuilder.fromCurrentRequest()
                    .replacePath("/static/images/" + fileName)
                    .build()
                    .toUriString();
            return imgUrl;

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        }
    }


    /**
     * 确认修改头像
     *
     * @param url
     * @param yesOrNo
     * @param request
     * @return
     */
    @Override
    public String confirmChangeAvatar(String url, boolean yesOrNo, HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        long userId = currentUser.getId();
        if (yesOrNo) {
            currentUser.setUserAvatar(url);
            this.baseMapper.updateById(currentUser);
            return "修改成功";
        } else {
            // 处理文件路径
            String filename = url.substring(url.lastIndexOf("/") + 1);
            // static 目录在项目的同级目录，需要向上一级跳出到 static 目录
            String basePath = "../static/images/"; // 根据部署方式调整
            File file = new File(basePath + filename);
            if (file.exists()) {
                if (file.delete()) {
                    return "修改成功";
                } else {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件删除失败");
                }
            } else {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不存在");
            }
        }
    }
}




