package com.binzc.oj.model.dto.user;

import lombok.Data;

/**
 * 用户确认修改头像
 * @ Author binzc
 *
 */
@Data
public class UserConfirmChangeAvatar {
    private String url;
    private boolean yesOrNo;
    private static final long serialVersionUID = 3191241716373120793L;

}
