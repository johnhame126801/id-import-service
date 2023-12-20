package com.abc.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StatisticInfo {

    /**
     * 剩余未绑定邮箱数量
     */
    private int unbindEmailCount;

    /**
     * 美卡总量
     */
    private int usaCardCount;

    /**
     * 剩余未绑定ID数量
     */
    private int unbindIdCount;

    /**
     * 剩余未分配ID数量
     */
    private int unAssignIdCount;

}
