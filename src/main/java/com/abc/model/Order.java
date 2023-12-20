package com.abc.model;

import lombok.Data;

/**
 * 订单 (卡密)
 */
@Data
public class Order {

    public static final int STATUS_PROGRESSING = 0;

    public static final int STATUS_COMPLETED = 1;

    /**
     * ID/卡密号
     */
    private String id;

    /**
     * 优先级  0：高  1：正常
     */
    private int priority;

    /**
     * 需要分配的苹果ID数量
     */
    private int idCount;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 订单状态  0：进行中  1：已完成
     */
    private int status;

    /**
     * 备注
     */
    private String remark;

}
