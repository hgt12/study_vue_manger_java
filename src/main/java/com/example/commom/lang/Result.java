package com.example.commom.lang;

import lombok.Data;

import java.io.Serializable;

// 因为后端结果内容，需与前端所展示的结果/格式保持一致，即使得前后端交互
// 所以要有一个类实现有统一的标准，约定结果返回的数据是正常的或异常。
@Data
public class Result implements Serializable
{
    private int code;
    private String msg;
    private Object data;

    public static Result succ(Object data) {
        return succ(200,"操作成功",data);
    }

    public static Result succ(int code, String msg, Object data) {
        Result result = new Result();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    public static Result succ(int code, String msg) {
        return succ(400,msg,null);
    }

    public static Result fail(int code, String msg, Object data) {
        Result result = new Result();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    public static Result fail(String msg) {
        return fail(400, msg, null);
    }

}
