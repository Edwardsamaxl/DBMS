package com.example.dbms.Pojo;

public class SharedDataStorage {
    private static final SharedDataStorage instance = new SharedDataStorage();

    private String username=null;

    private SharedDataStorage() {
        // 私有构造函数，防止外部实例化
    }

    public static SharedDataStorage getInstance() {
        return instance;
    }

    public void setThreadLocalData(String data) {
        this.username=data;
    }

    public String getThreadLocalData() {
        return username;
    }

    public void clearThreadLocalData() {
       this.username=null;
    }
}

