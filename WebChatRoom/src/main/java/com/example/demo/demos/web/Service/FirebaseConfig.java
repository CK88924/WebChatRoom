package com.example.demo.demos.web.Service;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    public FirebaseConfig() {
        try {
            // 從資源目錄中加載 firebase-service-account.json 文件
            InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream("firebase-service-account.json");

            if (serviceAccount == null) {
                throw new RuntimeException("無法找到 firebase-service-account.json 文件！");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://springbootwebchatroom.firebaseio.com") // 替換為你的 Firebase URL
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            System.out.println("Firebase 已成功初始化！");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("初始化 Firebase 時出現錯誤：" + e.getMessage());
        }
    }
}
