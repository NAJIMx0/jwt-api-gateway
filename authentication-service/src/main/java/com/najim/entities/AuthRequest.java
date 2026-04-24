package com.najim.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthRequest {
    public String email;
    public String password;
    public String name;

}
