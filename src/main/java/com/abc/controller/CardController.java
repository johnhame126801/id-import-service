package com.abc.controller;

import com.abc.model.Result;
import com.abc.model.UsaCard;
import com.abc.service.CardService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@RestController
@RequestMapping("/card")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping("/upload")
    public Result<Void> upload(MultipartHttpServletRequest multipartRequest) throws Exception {
        return cardService.upload(multipartRequest.getFile("file"));
    }

    @GetMapping("")
    public Result<UsaCard> getCard() {
        return cardService.getCard();
    }

    @DeleteMapping("/clear")
    public Result<Void> clear() {
        return cardService.clear();
    }

}
