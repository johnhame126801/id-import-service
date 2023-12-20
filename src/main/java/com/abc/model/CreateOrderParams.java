package com.abc.model;

import lombok.Data;

@Data
public class CreateOrderParams {

    /**
     * 账号数量
     */
    private Integer idCount;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 订单个数
     */
    private Integer orderNum;

    /**
     * 备注
     */
    private String remark;

}
