package com.abc.service;

import com.abc.model.Result;
import com.abc.model.StatisticInfo;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class StatisticService {

    private final EmailService emailService;
    private final CardService cardService;
    private final IdService idService;

    public StatisticService(EmailService emailService, CardService cardService, IdService idService) {
        this.emailService = emailService;
        this.cardService = cardService;
        this.idService = idService;
    }

    public Result<StatisticInfo> getStatisticInfo() {
        return Result.success(
                new StatisticInfo()
                        .setUnbindEmailCount(emailService.getUnusedEmailCount())
                        .setUsaCardCount(cardService.getCardCount())
                        .setUnbindIdCount(idService.getUnbindIdCount())
                        .setUnAssignIdCount(idService.getUnAssignIdCount())
        );
    }

}
