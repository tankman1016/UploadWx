package com.pcjh.uploadwx.data.entity;


public class UploadMessage {

    // 上传聊天消息，包括 粉丝、文本、音视频
    // 粉丝、文本信息使用 chat.txt文件
    // [{"fans_wx":"","content":"","add_time":"","direct":"","type":"","server":"","filename":""}]
    // type 0文本 1音视频  2图片 3发红包  4领红包  5转账  6收款  7别人收到我的红包
    // filename 文件名称（不包含路径）

    private String fan_alias;
    private String create_time;
    private int is_send;
    private int type;
    private String content;
    private String file_name;


    public String getFan_alias() {
        return fan_alias;
    }

    public void setFan_alias(String fan_alias) {
        this.fan_alias = fan_alias;
    }



    public int getIs_send() {
        return is_send;
    }

    public void setIs_send(int is_send) {
        this.is_send = is_send;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }
}
