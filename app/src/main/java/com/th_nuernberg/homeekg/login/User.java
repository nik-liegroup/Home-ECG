package com.th_nuernberg.homeekg.login;

public class User {

    //Login attributes
    public String name = "default";
    public String mail = "default";
    public String mobile = "default";

    //Additional personal information
    public String age = "default";
    public String gender = "default";
    public String height = "default";
    public String weight = "default";
    public String country = "default";
    public String city = "default";
    public String postcode = "default";
    public String street = "default";
    public String insurance = "default";

    public User() {

    }

    public User(String name, String mail, String mobile) {
        this.name = name;
        this.mail = mail;
        this.mobile = mobile;
    }

}
