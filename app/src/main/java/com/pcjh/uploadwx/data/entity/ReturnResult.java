package com.pcjh.uploadwx.data.entity;

/**
 * 服务器返回结果
 */

public class ReturnResult {

    //状态码
    private int status;
    //信息
    private String msg;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
