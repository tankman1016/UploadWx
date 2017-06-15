package com.pcjh.uploadwx.data;


import com.pcjh.uploadwx.data.entity.User;

//app 单例

public class AppHolder {

    private User user;
    private static AppHolder ourInstance = new AppHolder();

    public static AppHolder getInstance() {
        return ourInstance;
    }

    private AppHolder() {
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
