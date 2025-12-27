# Scam Detector App (AntifraudApp)

這是一個專為偵測詐騙內容設計的 Android 應用程式。透過整合 Cloudflare Workers API Gateway，提供即時的電話號碼、網址 (URL) 及文字內容 (AI 分析) 風險檢測服務。

## ✨ 主要功能 (Features)

*   **電話號碼檢測**：查詢來電號碼是否為已知的詐騙或高風險電話。
*   **網址安全掃描**：檢查 URL 是否包含惡意軟體、釣魚網站特徵。
*   **AI 文字分析**：利用 AI 模型分析簡訊或文字內容，判斷是否含有詐騙話術或意圖。
*   **安全架構**：採用 NDK (C++) 隱藏 API Key，並透過 API Gateway 進行資料清洗與轉發。

## 🛠 技術架構 (Tech Stack)

本專案採用現代化的 Android 開發標準與 **Clean Architecture**：

*   **語言**: Kotlin
*   **UI 框架**: Jetpack Compose (Material Design 3)
*   **架構模式**: MVVM (Model-View-ViewModel)
*   **網路連線**: Retrofit2 + OkHttp3
*   **非同步處理**: Coroutines + Flow
*   **安全性**: NDK (C++) / CMake (用於保護 API Key)
*   **依賴注入**: (目前手動注入，預留 Hilt 擴充空間)

### 架構分層
*   **Presentation Layer**: `ViewModel`, `Compose UI`, `UI Model`
*   **Domain Layer**: `Domain Entity` (純粹的業務資料結構)
*   **Data Layer**: `Repository`, `Retrofit Client`, `DTO` (Data Transfer Objects)

## 🚀 開發環境設定 (Setup Guide)

若您更換電腦或首次下載本專案，請務必按照以下步驟設定，否則 App 無法連線至後端。

### 1. 環境需求
*   Android Studio Ladybug | 2024.2.1 或更新版本
*   **NDK (Side by side)** 與 **CMake** (若尚未安裝，Android Studio 在 Gradle Sync 時會提示自動安裝，或可透過 SDK Manager 手動安裝)
*   JDK 17

### 2. 設定 API Key (關鍵步驟)
為了安全起見，API Key **不會** 包含在 Git 版本控制中。您必須在本地端手動設定。

1.  在專案根目錄下找到 `local.properties` 檔案 (如果沒有，請自行建立)。
2.  開啟 `local.properties`，新增以下一行：

    ```properties
    CLOUDFLARE_API_KEY=您的_Cloudflare_Worker_API_Key
    ```

    > **注意**：請將 `您的_Cloudflare_Worker_API_Key` 替換為真實的密鑰。

3.  完成後，點擊 Android Studio 上方的 **"Sync Project with Gradle Files"** (大象圖示)。
4.  Gradle Sync 成功後，C++ 編譯器會自動讀取這個 Key 並將其編譯進 `.so` 檔中。

## 📂 專案結構說明

```text
app/src/main/
├── cpp/                    <-- [Native] C++ 程式碼，用於存放與讀取 API Key
├── java/com/example/scamdetectorapp/
│   ├── presentation/       <-- [UI 層] 畫面與狀態管理
│   │   ├── screens/        <-- Compose 畫面 (DetectionScreen, HomeScreen...)
│   │   ├── viewmodel/      <-- ViewModel (處理 UI 邏輯)
│   │   └── model/          <-- UI 專用的資料模型 (ScanUiModel)
│   ├── domain/             <-- [領域層] 核心業務邏輯
│   │   └── model/          <-- 跨層級通用的資料實體 (ScanResult)
│   └── data/               <-- [資料層] 資料來源管理
│       ├── repository/     <-- Repository (統一對外窗口)
│       ├── remote/         <-- Retrofit Client 與 API Interface
│       └── model/          <-- DTO (對應 Server JSON 格式)
```

## ⚠️ 注意事項
*   本專案使用了 **NDK**，初次編譯時間可能會稍長。
*   若遇到 `UnsatisfiedLinkError`，請確認 `local.properties` 是否正確設定並重新 Clean & Rebuild Project。