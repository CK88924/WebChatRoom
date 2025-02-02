package com.example.demo.demos.web;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.demos.web.Service.FirebaseUserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;

/**
 * 用戶控制器，處理與用戶相關的請求
 */
@Controller
@RequestMapping("Users/api")
public class UserController {

    // 使用 SLF4J 記錄器
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final FirebaseUserService firebaseUserService;
    

    @Autowired
    public UserController(FirebaseUserService firebaseUserService) {
        this.firebaseUserService = firebaseUserService;
    }

    /**
     * 重定向到 login.html 靜態頁面
     *
     * @return ResponseEntity，重定向到靜態頁面
     */
    @GetMapping("/login")
    public ResponseEntity<String> loginPage() {
        // 記錄訪問 login 的請求
        logger.info("Redirecting to login.html");

        // 返回重定向響應，定位到靜態頁面
        return ResponseEntity.status(HttpStatus.FOUND)
                             .header("Location", "/login.html")
                             .build();
    }
    
    @PostMapping("/add")
    @ResponseBody
    public Map<String, String> addFriendOrGroup(
            @RequestParam("input") String input,
            @RequestParam("currentUserEmail") String currentUserEmail) {
        Map<String, String> response = new HashMap<>();
        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"; // Email 格式正則

        try {
            // 檢查是否為單個好友添加（Email）
            if (input.matches(emailRegex)) {
                // 判斷是否為當前用戶
                if (input.equals(currentUserEmail)) {
                    response.put("status", "error");
                    response.put("message", "不能添加自己為好友！");
                    return response;
                }

                // 嘗試查詢該 Email 的用戶
                UserRecord user = firebaseUserService.getUserByEmail(input); // 確保此方法返回用戶信息
                UserRecord currentUser = firebaseUserService.getUserByEmail(currentUserEmail); // 獲取當前用户信息
                firebaseUserService.addFriend(currentUser.getUid(), user.getDisplayName()); // 調用服務層添加好友
                firebaseUserService.addFriend(user.getUid(), currentUser.getDisplayName()); // 調用服務層添加好友(互相添加)
                response.put("status", "success");
                response.put("message", "好友已互相添加: " + user.getEmail());
                return response;
            }
            
            // **處理用戶加入群組**
            if (input.startsWith("group:")) {
                String groupId = input.replace("group:", "").trim();
                UserRecord currentUser = firebaseUserService.getUserByEmail(currentUserEmail);

                // **先檢查群組是否存在**
                boolean groupExists = firebaseUserService.doesGroupExist(groupId);
                if (!groupExists) {
                    response.put("status", "error");
                    response.put("message", "群組 " + groupId + " 不存在，無法添加用戶。");
                    return response;
                }

                // **群組存在，執行添加**
                firebaseUserService.addUserToGroup(groupId, currentUser.getUid(), currentUser.getDisplayName(), currentUser.getEmail());

                response.put("status", "success");
                response.put("message", "成功將當前用戶添加至群組: " + groupId);
                return response;
            }
            

            // 如果不是好友添加，則處理群組創建
            if (!input.contains(" ") || !input.contains(";")) {
                response.put("status", "error");
                response.put("message", "群組輸入格式不正確，請確保包含群組名稱和成員 Email 列表！");
                return response;
            }

            // 解析輸入
            String[] parts = input.split(" ", 2);
            String groupName = parts[0].trim(); // 群組名稱
            String[] emails = parts[1].split(";"); // Email 列表

            // 過濾掉當前用戶的 Email，並驗證 Email 格式
            List<String> emailList = Arrays.stream(emails)
                    .map(String::trim) // 移除首尾空格
                    .filter(email -> email.matches(emailRegex)) // 驗證 Email 格式
                    .filter(email -> !email.equals(currentUserEmail)) // 排除當前用戶
                    .collect(Collectors.toList());

            // 如果過濾後的列表為空，返回錯誤
            if (emailList.isEmpty()) {
                response.put("status", "error");
                response.put("message", "群組必須至少包含一個有效成員，不能只有自己！");
                return response;
            }

            // 查詢所有 Email 的 UID 並準備成員數據
            Map<String, Map<String, String>> members = new HashMap<>();
            for (String email : emailList) {
                UserRecord user = firebaseUserService.getUserByEmail(email); // 如果某個 Email 找不到則直接拋異常
                Map<String, String> memberInfo = new HashMap<>();
                memberInfo.put("username", user.getDisplayName());
                memberInfo.put("email", user.getEmail());
                members.put(user.getUid(), memberInfo);
            }

            // 添加當前用戶到群組
            String currentUserUid = firebaseUserService.getUidByEmail(currentUserEmail);
            Map<String, String> currentUserInfo = new HashMap<>();
            currentUserInfo.put("username", firebaseUserService.getDisplayNameByEmail(currentUserEmail));
            currentUserInfo.put("email", currentUserEmail);
            members.put(currentUserUid, currentUserInfo);

            // 調用服務層創建群組
            String groupUid = firebaseUserService.createGroup(groupName, members);

            // 返回成功結果
            response.put("status", "success");
            response.put("message", "群組: " + groupName + " 已創建，群組 UID: " + groupUid);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "添加失敗: " + e.getMessage());
        }
        return response;
    }
    
    /**
     * 返回動態用戶主頁模板
     *
     * @param username 用戶名，作為必填參數
     * @param model    模型對象，用於向模板傳遞數據
     * @return 視圖模板名稱，Thymeleaf 將加載 templates/UserHomePage.html
     */
    @GetMapping("/UserHomePage")
    public String userHomePage(@RequestParam String username, Model model) {
        // 記錄訪問用戶主頁的請求
        logger.info("UserHomePage accessed by username: {}", username);

        // 返回視圖名稱 "UserHomePage"，Thymeleaf 會加載 templates/UserHomePage.html
        return "UserHomePage";
    }
    @GetMapping("/ChatRoom")
    public String handleRequest(@RequestParam String send, @RequestParam String to, Model model) {
        System.out.println("send: " + send);
        System.out.println("to: " + to);
        
        model.addAttribute("sender", send);
        model.addAttribute("receiver", to);
        return "ChatRoom";
    }
}
