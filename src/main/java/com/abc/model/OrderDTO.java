package com.abc.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper=true)
public class OrderDTO extends Order {

    /**
     * 已分配个数
     */
    private Integer assignNum;

    public OrderDTO(Order order) {
        this.setId(order.getId());
        this.setPriority(order.getPriority());
        this.setCreateTime(order.getCreateTime());
        this.setIdCount(order.getIdCount());
        this.setStatus(order.getStatus());
        this.setRemark(order.getRemark());
    }
}
