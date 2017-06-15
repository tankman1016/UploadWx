package com.pcjh.uploadwx.data.entity;

public class User {

    private String username;
    private String wxdbpwd;
    private String uin;
    private String filename;
    private String alias;
    private String token;
    private String wxdbpath;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getWxdbpwd() {
        return wxdbpwd;
    }

    public void setWxdbpwd(String wxdbpwd) {
        this.wxdbpwd = wxdbpwd;
    }

    public String getUin() {
        return uin;
    }

    public void setUin(String uin) {
        this.uin = uin;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getWxdbpath() {
        return wxdbpath;
    }

    public void setWxdbpath(String wxdbpath) {
        this.wxdbpath = wxdbpath;
    }

    @Override
    public String toString() {
        return "/username/:"+this.username+"/wxdbpwd/:"+this.wxdbpwd+"/uin/:"+this.uin+"/filename/:"
                +this.filename+"/alias/:"+this.alias+"/token/:"+this.token+"/wxdbpath/:"+this.wxdbpath;
    }
}
