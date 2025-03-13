package com.sumer.sumerstores.entities;


import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "LoginResponse", namespace = "http://www.web.xds.co.za/XDSConnectWS")
public class LoginResult {

    private String loginResult;

    @XmlElement(name = "LoginResult")
    public String getLoginResult() {
        return loginResult;
    }

    public void setLoginResult(String loginResult) {
        this.loginResult = loginResult;
    }

    @Override
    public String toString() {
        return "LoginResult{" +
                "loginResult='" + loginResult + '\'' +
                '}';
    }
}

