package com.abc.model;

import lombok.Data;

@Data
public class Email {

    /**
     * 邮箱状态：未使用
     */
    public static final int STATUS_UNUSED = 0;

    /**
     * 邮箱状态：已使用
     */
    public static final int STATUS_USED = 1;

    /**
     * ID
     */
    private Long id;

    /**
     * 地址
     */
    private String email;

    /**
     * 密码
     */
    private String password;

    /**
     * 状态  0：待使用   1：已使用
     */
    private int status;

}
