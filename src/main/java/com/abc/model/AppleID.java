package com.abc.model;

import lombok.Data;

@Data
public class AppleID {

    /**
     * 状态：待绑定
     */
    public static final int STATUS_WAIT_BIND = 0;

    /**
     * 状态：已取出
     */
    public static final int STATUS_TAKE_OUT = 1;

    /**
     * 状态：已注册
     */
    public static final int STATUS_REGISTERED = 2;

    /**
     * 状态：已分配
     */
    public static final int STATUS_ASSIGNED = 3;

    /**
     * ID
     */
    private Long id;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 密码
     */
    private String password;

    /**
     * 邮箱密码
     */
    private String emailPassword;

    /**
     * 生日
     */
    private String birthday;

    /**
     * 美卡
     */
    private String usaCard;

    /**
     * 美卡链接
     */
    private String usaCardLink;

    /**
     * ID状态  0:待绑定 1:已取出 2: 已注册  3: 已分配
     */
    private int status;

}
