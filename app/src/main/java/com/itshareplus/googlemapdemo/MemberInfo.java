package com.itshareplus.googlemapdemo;

/**
 * Created by 貴花 on 2017/8/31.
 */

public class MemberInfo {

    String name;
    String address;
    String email;

    public MemberInfo(){

    }

    public MemberInfo(String name, String address, String email) {
        this.name = name;
        this.address = address;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getEmail() {
        return email;
    }
}
