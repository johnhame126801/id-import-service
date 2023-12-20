package com.abc.controller;

import com.abc.model.Email;
import com.abc.model.Result;
import com.abc.service.EmailService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@RestController
@RequestMapping("/email")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/upload")
    public Result<Void> upload(MultipartHttpServletRequest multipartRequest) throws Exception {
        return emailService.upload(multipartRequest.getFile("file"));
    }

    @GetMapping("")
    public Result<Email> getUnusedEmail() {
        return emailService.getUnusedEmail();
    }

    @DeleteMapping("/clear")
    public Result<Void> clear() {
        return emailService.clear();
    }

}
