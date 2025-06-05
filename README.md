# WebChatRoom

本專案是一個基於 Spring Boot 的即時聊天室，支援 Google 第三方登入，並使用 Firebase Firestore 作為後端數據存儲。

## 🚀 功能
- 📢 即時聊天
- 👥 群組聊天室
- 🖼️ 發送圖片
- 🔐 Google OAuth 登入
- 📂 Firebase Firestore 數據存儲
- ☁️ Firebase Storage 多媒體類型存儲


## 📦 開發環境
1. **Eclipse IDE**[Spring Boot install](https://ithelp.ithome.com.tw/m/articles/10214203)
2. **Spring Boot (Spring Boot Starter Template)**[JDK8](https://blog.csdn.net/weixin_67793092/article/details/134645650)
3. **Firebase Firestore Database**
4. **Google 第三方登入**

## 🔧 事前準備
### **1. 設定 Firebase**
請前往 [Firebase Console](https://console.firebase.google.com/) 建立專案，並取得以下配置：

#### **Firebase Admin SDK 私鑰**
前往：
> `Firebase Console > 專案總覽 > 專案設定 > 服務帳戶`

下載 `firebase-service-account.json` 並放置於 `src/main/resources/` 目錄下。

#### **Firebase 一般設定**
前往：
> `Firebase Console > 專案總覽 > 專案設定 > 一般設定`

將 Firebase 設定存為 `firebase-config.json`：
```json
{
  "apiKey": "",
  "authDomain": "",
  "databaseURL": "",
  "projectId": "",
  "storageBucket": "",
  "messagingSenderId": "",
  "appId": "",
  "measurementId": ""
}
```
#### **Firebase複合索引**
chatId 遞增 timestamp 遞增

# ⚠️ Firebase 安全規則的問題與改進

在設計 Firestore 資料庫時，因一開始沒有縝密思考資料結構，導致 Firebase 安全規則較難設計 (主要用 UID 較容易判斷存取權限)，以下是一些問題點與可能的改進方向。

## 🔴 目前存在的問題：
1. `messages` collection 目前允許所有人讀寫，可能導致：
   - 隱私問題：任何人都能讀取聊天室內容
   - 資安風險：外部攻擊者可寫入惡意訊息
2. 主要用MVCS架構但設計不甚理想存在一定耦合性

## ✅ 改進建議：
### **1. messages collection Storage 的權限強化**
**目前規則 (不安全，所有人可讀寫)**
```Firebase 安全規則
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // 允許用戶訪問以其 UID 命名的集合
    match /{uid}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }
    // 限制只有群組成員存取Groups/{groupId}
   	match /Groups/{groupId} {
  		allow read, write: if request.auth != null && request.auth.uid in resource.data.members_uid;
	 }
    //允許所有讀寫訊息
    match /messages/{messageId} {
      allow read, write: if true; 
    }
  }
}

rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /chat_images/{imagePath=**} {
      allow write: if true;
      allow read: if true;
    }
  }
}

```
## 成果影片
[成果影片](https://youtu.be/TeeBbP5ke-Y)
![圖片發送](images/圖片發送.jpg)
![圖加文發送](images/圖加文發送.jpg)

## 小提醒
1. 經測試當網頁a.target ="_blank"時Firebase Authentication 狀態似乎無法保持所以如需用到uid等參數需在該分頁重新登入