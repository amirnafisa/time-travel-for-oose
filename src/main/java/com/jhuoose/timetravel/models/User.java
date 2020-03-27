package com.jhuoose.timetravel.models;

public class User {
    private String login;
    private String password;
    private String secQuestion;
    private String secAns;

    public User(String login, String password, String secQuestion, String secAns) {
        this.login = login;
        this.password = password;
        this.secQuestion = secQuestion;
        this.secAns = secAns;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getSecQuestion(){
        return secQuestion;
    }

    public String getSecAns(){
        return secAns;
    }
}