package com.binzc.oj.service;

import com.binzc.oj.common.BaseResponse;
import com.binzc.oj.model.dto.user.UserListRequest;
import com.binzc.oj.model.dto.user.UserUpdateRequest;
import com.binzc.oj.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.binzc.oj.model.vo.ListUserVO;
import com.binzc.oj.model.vo.LoginUserVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author binzc
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-05-08 21:02:49
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword,String userRole);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);


    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);


    /**
     * 更新头像
     * @param file
     * @param request
     * @return
     *
     */

    String uploadAvatar(MultipartFile file, HttpServletRequest request);

    /**
     * 确认修改头像是否
     * @param url
     * @param yesOrNo
     * @param request
     * @return
     */
    String confirmChangeAvatar(String url, boolean yesOrNo, HttpServletRequest request);

    /**
     * 获取当前所有用户
     */
    List<ListUserVO> getAllUser(UserListRequest userListRequest, HttpServletRequest request);

    /**
     * 修改用户
     * @param userListRequest
     * @param request
     * @return
     */
    String updateUser(UserUpdateRequest userListRequest, HttpServletRequest request);

    /**
     * 删除用户
     * @param userDeleteRequest
     * @param request
     * @return
     */
    String deleteUser(UserUpdateRequest userDeleteRequest, HttpServletRequest request);
}
