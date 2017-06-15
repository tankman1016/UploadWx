package com.pcjh.uploadwx.data.entity;

/**
 * 需要上传的联系人实体类
 */

public class RContactForUpload {

    //粉丝昵称
    private String fans_nickname;
    //粉丝微信id
    private String fans_username;
    //粉丝微信号
    private String fans_alias;
    //粉丝备注
    private String fans_remark;

    public String getFans_nickname() {
        return fans_nickname;
    }

    public void setFans_nickname(String fans_nickname) {
        this.fans_nickname = fans_nickname;
    }

    public String getFans_username() {
        return fans_username;
    }

    public void setFans_username(String fans_username) {
        this.fans_username = fans_username;
    }

    public String getFans_alias() {
        return fans_alias;
    }

    public void setFans_alias(String fans_alias) {
        this.fans_alias = fans_alias;
    }

    public String getFans_remark() {
        return fans_remark;
    }

    public void setFans_remark(String fans_remark) {
        this.fans_remark = fans_remark;
    }

}
