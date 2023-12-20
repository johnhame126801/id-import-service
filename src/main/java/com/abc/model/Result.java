package com.abc.model;

import lombok.Data;

@Data
public class Result<T> {

    public static final int CODE_OK = 200;

    public static final int CODE_BAD = 400;

    public static final int CODE_ERROR = 500;

    private int code;

    private String msg;

    private T data;

    public Result(int code, T data) {
        this.code = code;
        this.data = data;
    }

    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(CODE_OK, "success", data);
    }

    public static <T> Result<T> badRequest(String msg) {
        return new Result<>(CODE_BAD, msg, null);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(CODE_ERROR, msg, null);
    }

}
