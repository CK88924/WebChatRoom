package com.example.demo.demos.web.Service;

import com.example.demo.demos.web.Model.FirebaseUserModel;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.FirestoreClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;
@Service
public class FirebaseUserService {

    private final FirebaseUserModel firebaseUserModel;
    private final Firestore firestore;

    // 建構方法，注入 Model
    public FirebaseUserService() {
        this.firebaseUserModel = new FirebaseUserModel();
        this.firestore = FirestoreClient.getFirestore();
    }

    /**
     * 根據 UID 獲取用戶
     *
     * @param uid 用戶 UID
     * @return 用戶詳細信息
     */
    public UserRecord getUserByUID(String uid) {
        try {
            return firebaseUserModel.getUserByUID(uid);
        } catch (Exception e) {
            throw new RuntimeException("無法根據 UID 獲取用戶，UID: " + uid, e);
        }
    }

    /**
     * 根據電子郵件獲取用戶
     *
     * @param email 用戶電子郵件
     * @return 用戶詳細信息
     */
    public UserRecord getUserByEmail(String email) {
        try {
            return firebaseUserModel.getUserByEmail(email);
        } catch (Exception e) {
            throw new RuntimeException("無法根據電子郵件獲取用戶，Email: " + email, e);
        }
    }
    
    /**
     * 根據 Email 獲取用戶的 UID
     *
     * @param email 用戶的電子郵件
     * @return 用戶的 UID
     * @throws Exception 如果用戶不存在或操作失敗
     */
    public String getUidByEmail(String email) {
        try {
            UserRecord user = firebaseUserModel.getUserByEmail(email);
            return user.getUid();
        } catch (Exception e) {
            throw new RuntimeException("無法根據電子郵件獲取 UID，Email: " + email, e);
        }
    }

    /**
     * 根據 Email 獲取用戶的名稱
     *
     * @param email 用戶的電子郵件
     * @return 用戶的名稱
     */
    public String getDisplayNameByEmail(String email) {
        try {
            UserRecord user = firebaseUserModel.getUserByEmail(email);
            return user.getDisplayName() != null ? user.getDisplayName() : "未命名用戶";
        } catch (Exception e) {
            throw new RuntimeException("無法根據電子郵件獲取名稱，Email: " + email, e);
        }
    }

    /**
     * 獲取用戶的供應商信息
     *
     * @param uid 用戶 UID
     * @return 登錄供應商列表
     */
    public List<String> getUserProviders(String uid) {
        try {
            return firebaseUserModel.getUserProviders(uid);
        } catch (Exception e) {
            throw new RuntimeException("無法獲取用戶的供應商信息，UID: " + uid, e);
        }
    }
    
    /**
     * 添加好友
     * @param currentUserUid 當前用戶的 UID
     * @param friendUid 好友的 UID
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void addFriend(String currentUserUid, String friend_displayName) throws ExecutionException, InterruptedException {
        DocumentReference userDocRef = firestore.collection(currentUserUid).document("defaultDoc");

        // 更新好友列表
        ApiFuture<WriteResult> future = userDocRef.update("friends", FieldValue.arrayUnion(friend_displayName));
        future.get(); // 等待操作完成
        System.out.println("好友添加成功: " + friend_displayName);
    }
    
    public void addUserToGroup(String groupId, String userId, String username, String email) throws Exception {
        DocumentReference groupDocRef = firestore.collection("Groups").document(groupId);
        // 更新 Firestore 中的群組信息
        groupDocRef.update(
            "members_uid", FieldValue.arrayUnion(userId),
            "members_username", FieldValue.arrayUnion(username),
            "members_email", FieldValue.arrayUnion(email)
        ).get(); // 確保 Firestore 操作完成
    }
    
    public boolean doesGroupExist(String groupId) throws Exception {
        DocumentReference groupDocRef = firestore.collection("Groups").document(groupId);
        DocumentSnapshot document = groupDocRef.get().get();
        return document.exists();
    }
    
    
    
    /**
     * 創建群組
     * @param groupName 群組名稱
     * @param members 用戶資料 Map (key: uid, value: Map 包含 username 和 email)
     * @return 新創建群組的 UID
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public String createGroup(String groupName, Map<String, Map<String, String>> members) throws ExecutionException, InterruptedException {
        CollectionReference groupsCollection = firestore.collection("Groups");

        // 自動生成群組文檔 UID
        DocumentReference newGroupRef = groupsCollection.document();
        String groupUid = newGroupRef.getId();

        // 構建群組數據
        Map<String, Object> groupData = new HashMap<>();
        groupData.put("group_name", groupName);
        groupData.put("group_uid", groupUid);
        
        // 提取成員資料
        List<String> membersUid = new ArrayList<>();
        List<String> membersUsername = new ArrayList<>();
        List<String> membersEmail = new ArrayList<>();

        for (Map.Entry<String, Map<String, String>> entry : members.entrySet()) {
            String uid = entry.getKey();
            Map<String, String> memberInfo = entry.getValue();

            membersUid.add(uid);
            membersUsername.add(memberInfo.get("username"));
            membersEmail.add(memberInfo.get("email"));
        }

        groupData.put("members_uid", membersUid);
        groupData.put("members_username", membersUsername);
        groupData.put("members_email", membersEmail);

        // 保存群組數據到 Firestore
        ApiFuture<WriteResult> future = newGroupRef.set(groupData);
        future.get(); // 等待操作完成

        System.out.println("群組創建成功，群組 UID: " + groupUid);
        return groupUid;
    }

}


