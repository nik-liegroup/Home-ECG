package com.th_nuernberg.homeekg.login;

public class User {

    //Login attributes
    public String name = "";
    public String mail = "";
    public String mobile = "";

    //Additional personal information
    public String birthday = "";
    public String gender = "";
    public String height = "";
    public String weight = "";
    public String country = "";
    public String address = "";
    public String insurance = "";

    public User() {

    }

    public User(String name, String mail, String mobile) {
        this.name = name;
        this.mail = mail;
        this.mobile = mobile;
    }

}
