package com.example.demo.demos.web.Model;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetUsersResult;
import com.google.firebase.auth.UidIdentifier;
import com.google.firebase.auth.UserIdentifier;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FirebaseUserModel {

    /**
     * 根據 UID 獲取用戶信息
     *
     * @param uid Firebase 用戶 UID
     * @return UserRecord 對象，包含用戶詳細信息
     * @throws Exception 如果用戶不存在或操作失敗
     */
    public UserRecord getUserByUID(String uid) throws Exception {
        return FirebaseAuth.getInstance().getUser(uid);
    }

    /**
     * 根據電子郵件獲取用戶信息
     *
     * @param email 用戶的電子郵件
     * @return UserRecord 對象，包含用戶詳細信息
     * @throws Exception 如果用戶不存在或操作失敗
     */
    public UserRecord getUserByEmail(String email) throws Exception {
        return FirebaseAuth.getInstance().getUserByEmail(email);
    }
    
    /**
     * 獲取用戶的供應商信息
     *
     * @param uid 用戶的 UID
     * @return 包含供應商名稱的列表
     * @throws Exception 如果用戶不存在或操作失敗
     */
    public List<String> getUserProviders(String uid) throws Exception {
        UserRecord user = getUserByUID(uid);

        // 使用 Collectors.toList() 將流收集為列表
        return Arrays.stream(user.getProviderData())
                     .map(UserInfo::getProviderId)
                     .collect(Collectors.toList());
    }
}
