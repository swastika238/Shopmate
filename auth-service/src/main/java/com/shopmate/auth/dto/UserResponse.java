package com.shopmate.auth.dto;

import com.shopmate.auth.model.Role;
import com.shopmate.auth.model.User;

public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;

    public static UserResponse from(User u){
        UserResponse r=new UserResponse();
        r.id=u.getId();
        r.name=u.getName();
        r.email=u.getEmail();
        r.role=u.getRole();
        return r;


    }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
}
