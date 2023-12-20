package com.abc.controller;

import com.abc.model.AppleID;
import com.abc.model.Bind;
import com.abc.model.ResetPwdParams;
import com.abc.model.Result;
import com.abc.service.IdService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/id")
public class IdController {

    private final IdService idService;

    public IdController(IdService idService) {
        this.idService = idService;
    }

    /**
     * 上传AppleID
     * @param appleID  AppleID
     * @return Result
     */
    @PostMapping("/upload")
    public Result<Void> upload(@RequestBody AppleID appleID) {
        return idService.uploadAppleID(appleID);
    }

    /**
     * 获取AppleID
     * @return Result
     */
    @GetMapping("")
    public Result<AppleID> getAppleID() {
        return idService.getAppleID();
    }

    /**
     * 绑定
     * @param bind  绑定信息
     * @return Result
     */
    @PostMapping("/bind")
    public Result<Void> bind(@RequestBody Bind bind) {
        return idService.bind(bind);
    }

    @DeleteMapping("/clear")
    public Result<Void> clear() {
        return idService.clear();
    }

    @PostMapping("/pwd/reset")
    public Result<Void> resetPassword(@RequestBody ResetPwdParams params) {
        return idService.resetPassword(params.getId(), params.getPassword());
    }

}
