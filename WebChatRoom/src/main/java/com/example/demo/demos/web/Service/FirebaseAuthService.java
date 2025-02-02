package com.example.demo.demos.web.Service;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class FirebaseAuthService {

    /**
     * 驗證 Firebase ID Token 並返回用戶名
     *
     * @param idToken Firebase ID Token
     * @return 用戶名（從 ID Token 中提取）
     * @throws RuntimeException 如果 ID Token 驗證失敗
     */
    public String verifyIdToken(String idToken) {
        try {
            // 使用 Firebase SDK 驗證 ID Token
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            
            // 從 Token 中提取UID
            String uid = decodedToken.getUid();
            if (uid == null || uid.isEmpty()) {
                throw new RuntimeException("UID 不存在於 ID Token 中！");
            }

            // 從 Token 中提取用戶名
            String userName = decodedToken.getName();
            if (userName == null || userName.isEmpty()) {
                throw new RuntimeException("用戶名不存在於 ID Token 中！");
            }
            createUserCollection(uid);
            return userName;
        } catch (Exception e) {
            // 捕獲異常並轉換為 RuntimeException
            throw new RuntimeException("無法驗證 ID Token：" + e.getMessage(), e);
        }
    }
    
    private void createUserCollection(String uid) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            CollectionReference userCollection = db.collection(uid);

            // 檢查集合是否已經存在（通過檢查是否有文檔）
            DocumentReference docRef = userCollection.document("defaultDoc");
            if (!docRef.get().get().exists()) {
                // 初始化集合中的第一條文檔
                Map<String, Object> defaultData = new HashMap<>();
                defaultData.put("description", "Default chatroom for UID: " + uid);
                defaultData.put("friends", new ArrayList<>()); // 初始化為空數組
                defaultData.put("createdAt", System.currentTimeMillis());
                docRef.set(defaultData); // 創建文檔
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("無法為用戶創建集合：" + e.getMessage(), e);
        }
    }
    
}




