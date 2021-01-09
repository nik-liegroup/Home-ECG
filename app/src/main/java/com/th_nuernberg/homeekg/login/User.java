package com.th_nuernberg.homeekg.login;

public class User {

    //Login attributes
    public String name = "default";
    public String mail = "default";
    public String mobile = "default";

    //Additional personal information
    public String birthday = "default";
    public String gender = "default";
    public String height = "default";
    public String weight = "default";

    public User() {

    }

    public User(String name, String mail, String mobile) {
        this.name = name;
        this.mail = mail;
        this.mobile = mobile;
    }

}
