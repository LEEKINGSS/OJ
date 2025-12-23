package com.binzc.oj.controller;

import com.binzc.oj.common.BaseResponse;
import com.binzc.oj.common.ErrorCode;
import com.binzc.oj.common.ResultUtils;
import com.binzc.oj.exception.BusinessException;
import com.binzc.oj.model.dto.user.*;
import com.binzc.oj.model.entity.User;
import com.binzc.oj.model.vo.ListUserVO;
import com.binzc.oj.model.vo.LoginUserVO;
import com.binzc.oj.service.UserService;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.List;

import static com.binzc.oj.constant.UserConstant.USER_LOGIN_STATE;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired

    private UserService userService;


    /**
     * 测试接口
     *
     * @return
     * @Author binzc
     */
    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String userRole = userRegisterRequest.getUserRole();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return null;
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, userRole);
        if (result < 0) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "账号已存在");
        }
        return ResultUtils.success(result);
    }

    /**
     * 用户登录接口
     *
     * @param userLoginRequest
     * @param request
     * @return
     * @Author binzc
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    /**
     * 获取当前所有用户
     */
    @PostMapping("/get/list")
    public BaseResponse<List<ListUserVO>> getAllUser(@RequestBody UserListRequest userListRequest, HttpServletRequest request) {
        return ResultUtils.success(userService.getAllUser(userListRequest, request));
    }


    /**
     * 修改用户
     */
    @PostMapping("/update")
    public BaseResponse<String> updateUser(@RequestBody UserUpdateRequest userListRequest, HttpServletRequest request) {
        return ResultUtils.success(userService.updateUser(userListRequest, request));
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete")
    public BaseResponse<String> deleteUser(@RequestBody UserUpdateRequest userDeleteRequest, HttpServletRequest request) {
        return ResultUtils.success(userService.deleteUser(userDeleteRequest, request));
    }

    /**
     * 上传头像
     *
     * @param file
     * @param request
     * @return
     *
     */
    @PostMapping("/uploadAvatar")
    public BaseResponse<String> uploadAvatar(@RequestPart("file") MultipartFile file, HttpServletRequest request) {
        String url = userService.uploadAvatar(file, request);
        return ResultUtils.success(url);
    }

    /**
     * 确认修改头像是否
     */
    @PostMapping("/confirmChangeAvatar")
    public BaseResponse confirmChangeAvatar(@RequestBody UserConfirmChangeAvatar userConfirmChangeAvatar, HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR);
        }
        String message = userService.confirmChangeAvatar(userConfirmChangeAvatar.getUrl(), userConfirmChangeAvatar.isYesOrNo(), request);
        return ResultUtils.success(message);
    }


}
