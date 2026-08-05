package com.kanako.modules.auth.controller;

import com.kanako.common.jwt.UserContext;
import com.kanako.common.response.R;
import com.kanako.modules.auth.entity.User;
import com.kanako.modules.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api-common/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        return R.ok(authService.login(body.get("username"), body.get("password")));
    }

    @GetMapping("/me")
    public R<User> me() {
        Long userId = UserContext.userId();
        if (userId == null) {
            return R.fail(401, "登录已失效");
        }
        return R.ok(authService.me(userId));
    }
}