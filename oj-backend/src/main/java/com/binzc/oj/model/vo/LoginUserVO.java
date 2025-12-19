package com.binzc.oj.model.vo;

import java.io.Serializable;
import java.util.Date;

import com.binzc.oj.model.entity.User;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 * 已登录用户视图（脱敏）
 *
 * @author binzc
 * @Contact 2019500261@qq.com
 **/
@Data
public class LoginUserVO implements Serializable {

    /**
     * 用户 id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;

    public static LoginUserVO generateVo(User user){
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }
}