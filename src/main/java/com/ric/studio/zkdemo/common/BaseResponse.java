package com.ric.studio.zkdemo.common;

/**
 * @author Richard_yyf
 * @version 1.0 2019/3/28
 */
public class BaseResponse<T> {

    private boolean success;

    private T t;

    public BaseResponse(boolean success, T t) {
        this.success = success;
        this.t = t;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }
}
