package com.kanako.modules.auth.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanako.common.exception.BusinessException;
import com.kanako.common.jwt.JwtUtil;
import com.kanako.common.response.R;
import com.kanako.modules.auth.entity.User;
import com.kanako.modules.auth.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * GitHub OAuth 登录（与前端 /auth/github/callback 配套）
 */
@RestController
@RequestMapping("/api-common/auth/oauth")
@RequiredArgsConstructor
public class GithubOAuthController {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Value("${kanako.razor.github-client-id:}")
    private String clientId;
    @Value("${kanako.razor.github-client-secret:}")
    private String clientSecret;
    @Value("${kanako.razor.github-redirect-uri:}")
    private String defaultRedirectUri;

    @GetMapping("/github/start")
    public R<Map<String, String>> start(@RequestParam(required = false) String redirectUri) {
        if (!StringUtils.hasText(clientId)) {
            throw new BusinessException("GitHub OAuth 未配置（kanako.razor.github-client-id）");
        }
        String redirect = StringUtils.hasText(redirectUri) ? redirectUri : defaultRedirectUri;
        String url = "https://github.com/login/oauth/authorize?client_id=" + clientId
                + "&redirect_uri=" + java.net.URLEncoder.encode(redirect, java.nio.charset.StandardCharsets.UTF_8)
                + "&scope=user:email";
        return R.ok(Map.of("url", url));
    }

    @GetMapping("/github/callback")
    public R<Map<String, Object>> callback(@RequestParam String code,
                                           @RequestParam(required = false) String redirectUri) {
        if (!StringUtils.hasText(clientId) || !StringUtils.hasText(clientSecret)) {
            throw new BusinessException("GitHub OAuth 未配置");
        }
        String redirect = StringUtils.hasText(redirectUri) ? redirectUri : defaultRedirectUri;

        RestClient client = RestClient.create("https://github.com");
        String tokenBody = client.post()
                .uri("/login/oauth/access_token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("client_id", clientId, "client_secret", clientSecret,
                        "code", code, "redirect_uri", redirect))
                .header("Accept", "application/json")
                .retrieve()
                .body(String.class);

        try {
            JsonNode tokenNode = objectMapper.readTree(tokenBody);
            String accessToken = tokenNode.path("access_token").asText("");
            if (!StringUtils.hasText(accessToken)) {
                throw new BusinessException("GitHub 授权失败：" + tokenNode.path("error_description").asText("unknown error"));
            }
            String userBody = RestClient.create("https://api.github.com")
                    .get()
                    .uri("/user")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/json")
                    .retrieve()
                    .body(String.class);
            JsonNode gh = objectMapper.readTree(userBody);
            Long ghId = gh.path("id").asLong();
            String ghLogin = gh.path("login").asText();
            String ghName = gh.path("name").asText("");

            User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getGithubId, ghId).last("limit 1"));
            if (user == null) {
                user = new User();
                user.setUsername(StringUtils.hasText(ghLogin) ? ghLogin : "gh-" + ghId);
                user.setAvatar(gh.path("avatar_url").asText(""));
                user.setPassword(encoder.encode(UUID.randomUUID().toString()));
                user.setRole("USER");
                user.setGithubId(ghId);
                user.setGithubLogin(ghLogin);
                user.setCreatedAt(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());
                userMapper.insert(user);
            }
            String token = jwtUtil.createToken(user.getId(), user.getUsername(), user.getRole());
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("user", user);
            return R.ok(result);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("GitHub 登录失败：" + e.getMessage());
        }
    }
}
