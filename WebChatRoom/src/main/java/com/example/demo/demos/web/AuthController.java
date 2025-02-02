package com.example.demo.demos.web;

import org.springframework.web.bind.annotation.*;
import com.example.demo.demos.web.Service.FirebaseAuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private FirebaseAuthService firebaseAuthService;

    /**
     * 驗證 Firebase ID Token 並跳轉到用戶主頁
     *
     * @param payload 包含 idToken 的請求 JSON
     * @return RedirectView 跳轉到用戶主頁或錯誤頁面
     */
    @PostMapping("/verify")
    public RedirectView verifyUser(@RequestBody Map<String, String> payload,HttpServletRequest request) {
        if (payload == null || !payload.containsKey("idToken")) {
            throw new IllegalArgumentException("缺少 idToken！");
        }

        try {
            String idToken = payload.get("idToken"); // 從 JSON 請求中獲取 idToken
            String userName = firebaseAuthService.verifyIdToken(idToken); // 驗證並獲取用戶名
            
            // 驗證成功後，跳轉到主頁，附加用戶名參數
            String encodedUserName = URLEncoder.encode(userName, StandardCharsets.UTF_8.name());
            return new RedirectView("/Users/api/UserHomePage?username=" + encodedUserName);

        } catch (Exception e) {
            throw new RuntimeException("身份驗證失敗：" + e.getMessage());
        }
    }
}
