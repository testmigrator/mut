package com.test.migration.controller;

import com.test.migration.entity.response.CommonReturnType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.*;
import utils.Log;

@RestController
@RequestMapping(value = "/user")
public class UserController {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class User {
        String username;
        String password;
        String token;
    }

    @PostMapping(value = "/login")
    public CommonReturnType login(@RequestBody User user) {
        Log.info(user.getUsername());
        user.setToken("token abcde");
        return CommonReturnType.create(user);
    }

    @GetMapping(value = "/info")
    public CommonReturnType info() {
        return CommonReturnType.create(true);
    }

}
