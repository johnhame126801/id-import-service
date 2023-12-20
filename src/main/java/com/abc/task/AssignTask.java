package com.abc.task;

import com.abc.service.AssignService;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class AssignTask {

    private final AssignService assignService;

    public AssignTask(AssignService assignService) {
        this.assignService = assignService;
    }

    /**
     * 分配库存 (每分钟执行一次)
     */
    @Scheduled(fixedDelay = 60000)
    public void assign() {
        log.debug("定时任务 - 开始分配库存");
        assignService.assignStock();
        log.debug("定时任务 - 分配库存完成");
    }

}
