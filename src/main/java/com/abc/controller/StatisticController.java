package com.abc.controller;

import com.abc.model.Result;
import com.abc.model.StatisticInfo;
import com.abc.service.StatisticService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/statistic")
public class StatisticController {

    private final StatisticService statisticService;

    public StatisticController(StatisticService statisticService) {
        this.statisticService = statisticService;
    }

    @GetMapping("")
    public Result<StatisticInfo> getStatistic() {
        return statisticService.getStatisticInfo();
    }
}
