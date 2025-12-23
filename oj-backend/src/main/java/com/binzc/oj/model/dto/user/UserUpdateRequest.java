package com.binzc.oj.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 请求用户列表
 *
 */
@Data
public class UserUpdateRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    private Long id;

    private String userAccount;

    private String userRole;
}
