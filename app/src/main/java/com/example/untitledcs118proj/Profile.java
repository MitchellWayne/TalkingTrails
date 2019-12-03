package com.example.untitledcs118proj;

public class Profile {

    public String username;
    public String password;

    public Profile() {
        // Default constructor
    }

    public Profile(Profile p){
        username = p.username;
        password = p.password;
    }

    public Profile(String u, String p) {
        username = u;
        password = p;
    }

    public String getUsername(){
        return username;
    }

    public String getPassword(){
        return password;
    }


}
