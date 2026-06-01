# 📱 TÀI LIỆU PHÂN TÍCH CHUYÊN SÂU — DỰ ÁN SPLITMATE (BTCK-APP-FE)

> **Loại dự án**: Ứng dụng Android Native (Java) — Bài tập cuối kỳ môn Lập trình ứng dụng di động  
> **Ngày cập nhật**: 2026-05-27  
> **Backend đang chạy tại**: `http://e1.chiasegpu.vn:34093/api/v1/`

---

## MỤC LỤC

1. [Tổng quan & Ý tưởng sản phẩm](#1-tổng-quan--ý-tưởng-sản-phẩm)
2. [Công nghệ sử dụng (Tech Stack)](#2-công-nghệ-sử-dụng-tech-stack)
3. [Cấu trúc thư mục Source Code](#3-cấu-trúc-thư-mục-source-code)
4. [Kiến trúc MVVM & Repository Pattern (phân tích từ code)](#4-kiến-trúc-mvvm--repository-pattern)
5. [Tầng Application Entry Point — BTCKApplication](#5-tầng-application-entry-point--btckapplication)
6. [Tầng Kết Nối Mạng (Network Layer)](#6-tầng-kết-nối-mạng-network-layer)
7. [Tầng Lưu Trữ Cục Bộ — TokenManager](#7-tầng-lưu-trữ-cục-bộ--tokenmanager)
8. [Tầng Repository — Xử lý dữ liệu & API](#8-tầng-repository)
9. [Tầng ViewModel — Quản lý trạng thái UI](#9-tầng-viewmodel)
10. [Bản đồ các màn hình & Luồng điều hướng](#10-bản-đồ-màn-hình--luồng-điều-hướng)
11. [Chi tiết từng màn hình (Activities & Fragments)](#11-chi-tiết-từng-màn-hình)
12. [Tính năng OCR đọc hóa đơn tự động](#12-tính-năng-ocr-đọc-hóa-đơn-tự-động)
13. [Tính năng VietQR thanh toán thông minh](#13-tính-năng-vietqr-thanh-toán-thông-minh)
14. [Hệ thống thông báo đẩy FCM](#14-hệ-thống-thông-báo-đẩy-fcm)
15. [Xử lý lỗi từ Backend — ErrorUtils](#15-xử-lý-lỗi-từ-backend--errorutils)
16. [Design System & Giao diện](#16-design-system--giao-diện)
17. [Các kỹ thuật xử lý nâng cao trong code](#17-các-kỹ-thuật-xử-lý-nâng-cao-trong-code)
18. [Danh sách toàn bộ API Endpoints](#18-danh-sách-toàn-bộ-api-endpoints)

---

## 1. TỔNG QUAN & Ý TƯỞNG SẢN PHẨM

**SplitMate** là ứng dụng Android giúp quản lý và chia sẻ chi tiêu nhóm. Bất kỳ khi nào một nhóm người cùng chi trả cho một khoản chung (ăn uống, du lịch, tiền nhà, tiệc,...), SplitMate sẽ:

- **Ghi chép chi tiêu**: Ai trả, bao nhiêu tiền, chia cho những ai.
- **Tính toán số dư**: Ai đang nợ ai, nợ bao nhiêu trong từng nhóm.
- **Đơn giản hóa nợ nần**: Backend sử dụng thuật toán tối ưu để giảm thiểu số lượt chuyển tiền.
- **Thanh toán tức thì**: Tạo mã VietQR động để chuyển khoản ngân hàng không cần gõ tay thông tin.
- **Thông báo thời gian thực**: Push notification qua Firebase khi có chi tiêu mới hoặc thay đổi trong nhóm.

**Server Backend** là một dịch vụ FastAPI (Python) đang được host tại địa chỉ thực: `http://e1.chiasegpu.vn:34093/api/v1/`.  
Tất cả dữ liệu của ứng dụng (user, nhóm, chi tiêu, thanh toán,...) đều được lưu trữ và xử lý tại đây.

---

## 2. CÔNG NGHỆ SỬ DỤNG (TECH STACK)

| Công nghệ | Phiên bản | Mục đích sử dụng |
|---|---|---|
| **Java (Android SDK)** | API 24+ | Ngôn ngữ lập trình chính |
| **Retrofit 2** | 2.9+ | Gọi REST API với annotations |
| **OkHttp 3** | 4.x | HTTP client, logging, timeout |
| **Gson** | 2.x | Chuyển đổi JSON ↔ Java Object |
| **Glide** | 4.x | Load & cache ảnh (avatar, QR) |
| **Lottie** | Latest | Animation JSON (splash/loading) |
| **Firebase Cloud Messaging** | Latest | Push notifications |
| **ML Kit Text Recognition** | Latest | OCR đọc chữ từ ảnh hóa đơn |
| **Material Components 3** | Latest | UI components (Card, Button, Dialog,...) |
| **ViewBinding** | Built-in | Truy cập View an toàn, thay thế findViewById |
| **LiveData + ViewModel** | Jetpack | Quản lý trạng thái UI lifecycle-aware |
| **SharedPreferences** | Built-in | Lưu JWT token, thông tin user cục bộ |
| **FileProvider** | Built-in | Chia sẻ file ảnh an toàn với Camera app |

---

## 3. CẤU TRÚC THƯ MỤC SOURCE CODE

```
app/src/main/java/com/example/btck/
│
├── BTCKApplication.java          ← Application class (điểm khởi đầu toàn app)
│
├── api/                           ← Tầng Network
│   ├── ApiService.java            ← Định nghĩa TẤT CẢ endpoints Retrofit
│   ├── AuthInterceptor.java       ← Tự động đính kèm JWT token vào mọi request
│   └── RetrofitClient.java        ← Singleton cấu hình OkHttp + Retrofit
│
├── managers/
│   └── TokenManager.java          ← Đọc/ghi token & user info trong SharedPreferences
│
├── models/                        ← ~35 Data Transfer Object (DTO) mapping JSON
│   ├── TokenResponse.java         ← access_token, refresh_token
│   ├── UserPublic.java            ← id, email, full_name, bank_name, account_number,...
│   ├── EventPublic.java           ← id, name, description, member_count, expense_count
│   ├── ExpensePublic.java         ← id, amount, description, category, splits[]
│   ├── SettlementPublic.java      ← id, from_user, to_user, amount, note
│   ├── SimplifiedDebt.java        ← from_user_id, to_user_id, amount (nợ đã tối ưu)
│   ├── UserBalance.java           ← user, net_balance, bank_name, account_number
│   └── ...                        ← và 28 model khác
│
├── repository/                    ← Tầng truy cập dữ liệu (trung gian ViewModel - API)
│   ├── AuthRepository.java        ← login(), register(), recoverPassword(), getCurrentUser()
│   ├── EventRepository.java       ← CRUD nhóm, thành viên, balance, mã mời
│   ├── ExpenseRepository.java     ← CRUD chi tiêu
│   ├── SettlementRepository.java  ← Tạo & lấy danh sách thanh toán
│   └── UserRepository.java        ← Cập nhật profile, avatar, FCM, tìm kiếm user
│
├── viewmodel/                     ← Tầng ViewModel (quản lý trạng thái UI)
│   ├── AuthViewModel.java         ← login, register, logout, fetchCurrentUser
│   ├── EventViewModel.java        ← groups, balances, stats, invite code
│   ├── ExpenseViewModel.java      ← expenses, create, delete
│   ├── SettlementViewModel.java   ← settlements, createSettlement
│   └── NotificationViewModel.java ← notifications, markRead, markAllRead
│
├── activities/                    ← 13 màn hình Activity
│   ├── SplashActivity.java        ← Màn hình khởi động, kiểm tra đăng nhập
│   ├── LoginActivity.java         ← Đăng nhập
│   ├── RegisterActivity.java      ← Đăng ký
│   ├── ForgotPasswordActivity.java← Quên mật khẩu
│   ├── MainActivity.java          ← Trang chủ, BottomNav + FCM setup
│   ├── GroupDetailActivity.java   ← Chi tiết nhóm, 3 tabs (652 dòng code)
│   ├── AddExpenseActivity.java    ← Thêm chi tiêu + OCR + chia tiền (558 dòng)
│   ├── ExpenseDetailActivity.java ← Chi tiết & xóa một chi tiêu
│   ├── SettlementActivity.java    ← Lịch sử & ghi nhận thanh toán
│   ├── PaymentQrActivity.java     ← Tạo & hiển thị mã VietQR
│   ├── ProfileActivity.java       ← Hồ sơ cá nhân (31KB - lớn nhất)
│   ├── NotificationsActivity.java ← Danh sách thông báo
│   └── JoinEventActivity.java     ← Tham gia nhóm qua mã mời / deep link
│
├── fragments/                     ← 4 fragment trong MainActivity
│   ├── HomeFragment.java          ← Tab chủ: balance tổng + nhóm gần đây
│   ├── GroupsFragment.java        ← Tab nhóm: danh sách + tạo/tham gia nhóm
│   ├── NotificationsFragment.java ← Tab thông báo: list + mark all read
│   └── ProfileFragment.java       ← Tab hồ sơ: thông tin + ngân hàng + logout
│
├── adapters/                      ← 6 RecyclerView Adapters
│   ├── EventAdapter.java          ← Danh sách nhóm (kèm balance hint)
│   ├── ExpenseAdapter.java        ← Danh sách chi tiêu
│   ├── MemberAdapter.java         ← Danh sách thành viên (avatar + role badge)
│   ├── MemberSplitAdapter.java    ← Chia tiền cho từng thành viên
│   ├── SettlementAdapter.java     ← Danh sách thanh toán đã ghi nhận
│   └── NotificationAdapter.java   ← Danh sách thông báo (read/unread)
│
├── services/
│   └── MyFirebaseMessagingService.java ← FCM push notifications handler
│
└── utils/
    ├── ErrorUtils.java            ← Parse lỗi từ backend → tiếng Việt
    ├── CurrencyTextWatcher.java   ← Format số tiền khi gõ (1000000 → 1.000.000)
    └── InviteCodeUtils.java       ← Normalize mã mời (xóa khoảng trắng, uppercase)
```

---

## 4. KIẾN TRÚC MVVM & REPOSITORY PATTERN

### Sơ đồ luồng dữ liệu

```
┌─────────────────────────────────────────────────────────────────────┐
│                         UI LAYER                                     │
│  Activity / Fragment                                                  │
│  • Inflate layout qua ViewBinding                                     │
│  • Lắng nghe LiveData từ ViewModel (lifecycle-aware observer)         │
│  • Gọi method ViewModel khi user thao tác (nhấn nút,...)             │
└──────────────────────────────┬──────────────────────────────────────┘
                               │ viewModel.login(email, pass)
                               │ viewModel.loginResult.observe(this, ...)
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       VIEWMODEL LAYER                                │
│  AndroidViewModel (giữ nguyên qua rotation màn hình)                 │
│  • Chứa MutableLiveData<T> cho từng loại dữ liệu                    │
│  • isLoading, errorMessage để UI phản hồi trực quan                  │
│  • Gọi Repository để lấy/gửi dữ liệu                                │
│  • Không biết gì về View (tách biệt hoàn toàn)                       │
└──────────────────────────────┬──────────────────────────────────────┘
                               │ repository.login(email, pass, onSuccess, onError)
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      REPOSITORY LAYER                                │
│  • Single Source of Truth — quyết định lấy data từ đâu              │
│  • Hiện tại: tất cả từ Remote API (chưa có local cache/DB)           │
│  • Nhận kết quả Retrofit Callback → postValue vào LiveData           │
│  • Dùng ErrorUtils.parseError() để chuyển lỗi thành tiếng Việt      │
└──────────────────────────────┬──────────────────────────────────────┘
                               │ api.login(email, password).enqueue(...)
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       NETWORK LAYER                                  │
│  RetrofitClient (Singleton)                                          │
│  • OkHttpClient với AuthInterceptor + HttpLoggingInterceptor         │
│  • Timeout: connect=30s, read=30s, write=30s                         │
│  • GsonConverterFactory: tự động parse JSON ↔ Java Object            │
│  ├─ AuthInterceptor: chèn "Authorization: Bearer <token>"            │
│  └─ ApiService: interface định nghĩa 40+ endpoint với annotations    │
└──────────────────────────────┬──────────────────────────────────────┘
                               │ HTTP Request
                               ▼
            FastAPI Backend: http://e1.chiasegpu.vn:34093/api/v1/
```

---

## 5. TẦNG APPLICATION ENTRY POINT — BTCKApplication

**File**: `BTCKApplication.java`

Đây là class đầu tiên được Android khởi tạo khi ứng dụng mở. Nó làm **3 việc chính**:

```java
public class BTCKApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // 1. Khởi tạo RetrofitClient Singleton với Context của Application
        //    (Quan trọng: phải có context để TokenManager đọc SharedPreferences)
        RetrofitClient.init(this);

        // 2. Tạo Notification Channel (bắt buộc từ Android 8.0 Oreo)
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Kênh chính cho chi tiêu nhóm
            NotificationChannel channel = new NotificationChannel(
                "btck_notifications",
                "BTCK Thông báo",
                NotificationManager.IMPORTANCE_HIGH  // Hiện heads-up, có âm thanh
            );

            // Kênh dự phòng của Firebase (workaround khi BE gửi notification-only message)
            NotificationChannel fallbackChannel = new NotificationChannel(
                "fcm_fallback_notification_channel",
                "Thông báo ứng dụng",
                NotificationManager.IMPORTANCE_HIGH
            );
        }
    }
}
```

**Lý do cần 2 Notification Channel**: Backend FastAPI đôi khi gửi loại `notification-only` message thay vì `data message`. Với loại này, Firebase SDK sẽ tự render notification và dùng channel mặc định tên `fcm_fallback_notification_channel`. Nếu không tạo trước channel này, thông báo sẽ không hiện trên Android 8+.

---

## 6. TẦNG KẾT NỐI MẠNG (NETWORK LAYER)

### 6.1. RetrofitClient.java — Singleton HTTP Client

```java
public class RetrofitClient {

    // URL thực của server đang deploy (không phải localhost)
    public static final String BASE_URL = "http://e1.chiasegpu.vn:34093/api/v1/";

    private static Retrofit retrofit = null;
    private static ApiService apiService = null;
    private static TokenManager tokenManager = null;

    // Gọi một lần duy nhất trong BTCKApplication.onCreate()
    public static void init(Context context) {
        tokenManager = new TokenManager(context);
        retrofit = null;    // reset để lazy init lại
        apiService = null;
    }

    private static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            // Logging toàn bộ request/response body vào Logcat (debug mode)
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(tokenManager)) // JWT injection
                .addInterceptor(logging)                           // log HTTP traffic
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

            retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create()) // JSON auto-parse
                .build();
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        if (apiService == null) {
            // Retrofit tạo một implementation proxy cho interface ApiService
            apiService = getRetrofitInstance().create(ApiService.class);
        }
        return apiService;
    }

    // Gọi sau khi đăng nhập hoặc đăng xuất để reset token
    public static void reset() {
        retrofit = null;
        apiService = null;
    }
}
```

**Điểm quan trọng**: Phương thức `reset()` được gọi trong 2 trường hợp:
- **Sau khi đăng nhập** (`AuthViewModel.saveTokens()`): reset để lần gọi tiếp theo `AuthInterceptor` đọc được token mới đã lưu.
- **Khi đăng xuất** (`AuthViewModel.logout()`): reset để xóa token cũ khỏi bộ nhớ.

### 6.2. AuthInterceptor.java — Tự động đính kèm JWT

```java
public class AuthInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        String token = tokenManager.getAccessToken(); // lấy từ SharedPreferences
        Request original = chain.request();

        if (token != null && !token.isEmpty()) {
            // Tạo request mới với header Authorization
            Request request = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .method(original.method(), original.body())
                .build();
            return chain.proceed(request); // gửi request đã có JWT
        }
        return chain.proceed(original); // request không có token (login, register)
    }
}
```

Nhờ interceptor này, tất cả 40+ API endpoint **không cần lập trình viên thêm token thủ công**. Interceptor tự động xử lý.

### 6.3. ApiService.java — Toàn bộ 40+ API Endpoints

Đây là interface sử dụng annotation của Retrofit để ánh xạ Java method → HTTP request. Được chia thành 6 nhóm chức năng:

**Nhóm AUTH** — Xác thực người dùng:
```java
@FormUrlEncoded
@POST("auth/login")
Call<TokenResponse> login(@Field("username") String username, @Field("password") String password);
// Lưu ý: dùng @FormUrlEncoded vì FastAPI OAuth2 yêu cầu form-data, không phải JSON

@POST("auth/register")
Call<UserPublic> register(@Body UserRegister body);

@POST("auth/password-recovery/{email}")
Call<MessageResponse> recoverPassword(@Path("email") String email);

@POST("auth/refresh")
Call<TokenResponse> refreshToken(@Body RefreshTokenRequest body);

@POST("auth/logout")
Call<MessageResponse> logout();

@GET("auth/me")
Call<UserPublic> getMe();
```

**Nhóm USERS** — Quản lý hồ sơ:
```java
@GET("users/me")
Call<UserPublic> getCurrentUser();

@PATCH("users/me")
Call<UserPublic> updateMe(@Body UpdateUserMeRequest body);

@PATCH("users/me/password")
Call<MessageResponse> updatePassword(@Body UpdatePasswordRequest body);

@Multipart
@POST("users/me/avatar")
Call<UserPublic> uploadAvatar(@Part MultipartBody.Part file);
// Dùng Multipart để upload file ảnh đại diện dạng binary

@POST("users/me/fcm-token")
Call<MessageResponse> registerFcmToken(@Body FCMTokenRequest body);

@GET("users/{user_id}/payment-qr")
Call<ResponseBody> getPaymentQr(
    @Path("user_id") String userId,
    @Query("amount") long amount,
    @Query("description") String description
);
// Trả về URL ảnh mã QR VietQR (raw string, không phải JSON object)

@GET("users/search")
Call<List<UserPublic>> searchUsers(@Query("email") String email);
```

**Nhóm EVENTS (Groups)** — Quản lý nhóm:
```java
@GET("events/")
Call<EventsPublic> getEvents(@Query("skip") int skip, @Query("limit") int limit);

@POST("events/")
Call<EventPublic> createEvent(@Body EventCreate body);

@GET("events/{event_id}/balances")
Call<EventBalances> getEventBalances(@Path("event_id") String eventId);
// Trả về số dư chi tiết từng thành viên

@GET("events/{event_id}/balances/simplify")
Call<SimplifiedDebtsResponse> getSimplifiedDebts(@Path("event_id") String eventId);
// Trả về danh sách nợ đã tối ưu hóa (giảm số giao dịch)

@POST("events/{event_id}/invite")
Call<InviteCodePublic> createInviteCode(@Path("event_id") String eventId, @Body InviteCodeCreate body);

@POST("events/join/{code}")
Call<EventMemberPublic> joinEventByCode(@Path("code") String code);

@GET("events/me/balance")
Call<MyBalanceDetail> getMyBalance();
// Tổng hợp toàn bộ số dư cá nhân qua tất cả nhóm
```

**Nhóm EXPENSES** — Quản lý chi tiêu:
```java
@POST("events/{event_id}/expenses/")
Call<ExpensePublic> createExpense(@Path("event_id") String eventId, @Body ExpenseCreate body);

@Multipart
@POST("events/{event_id}/expenses/{expense_id}/image")
Call<ExpensePublic> uploadExpenseImage(
    @Path("event_id") String eventId,
    @Path("expense_id") String expenseId,
    @Part MultipartBody.Part file
);
// Upload ảnh hóa đơn sau khi tạo expense xong (2 bước riêng biệt)
```

---

## 7. TẦNG LƯU TRỮ CỤC BỘ — TokenManager

**File**: `TokenManager.java` — Sử dụng Android `SharedPreferences` với tên file là `"btck_prefs"`.

Các key được lưu trữ:
```java
private static final String KEY_ACCESS_TOKEN  = "access_token";
private static final String KEY_REFRESH_TOKEN = "refresh_token";
private static final String KEY_USER_ID       = "user_id";
private static final String KEY_USER_EMAIL    = "user_email";
private static final String KEY_USER_NAME     = "user_name";
private static final String KEY_FCM_TOKEN     = "fcm_token";
```

Phương thức kiểm tra đăng nhập:
```java
public boolean isLoggedIn() {
    String token = getAccessToken();
    return token != null && !token.isEmpty();
    // Chỉ kiểm tra token có tồn tại trong SharedPreferences không
    // Không kiểm tra thời gian hết hạn (expiry) — backend trả 401 nếu hết hạn
}
```

Phương thức xóa phiên đăng nhập (đăng xuất):
```java
public void clearAll() {
    prefs.edit().clear().apply();
    // Xóa toàn bộ SharedPreferences — kể cả FCM token và thông tin user
}
```

**Lưu ý quan trọng**: `mode = Context.MODE_PRIVATE` đảm bảo file này chỉ ứng dụng của bạn mới đọc được. Không app nào khác trên máy có thể truy cập token của bạn.

---

## 8. TẦNG REPOSITORY

Repository là tầng trung gian nhận request từ ViewModel và thực hiện gọi Retrofit. Tất cả đều tuân theo **cùng một pattern** chuẩn:

```java
// Pattern chuẩn trong AuthRepository.login():
public void login(String email, String password,
                  MutableLiveData<TokenResponse> onSuccess,
                  MutableLiveData<String> onError) {

    api.login(email, password).enqueue(new Callback<TokenResponse>() {

        @Override
        public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
            if (response.isSuccessful() && response.body() != null) {
                // Thành công: cập nhật LiveData onSuccess
                onSuccess.postValue(response.body());
            } else {
                // Thất bại từ server (4xx, 5xx): parse lỗi → tiếng Việt
                onError.postValue(ErrorUtils.parseError(response));
            }
        }

        @Override
        public void onFailure(Call<TokenResponse> call, Throwable t) {
            // Mất mạng / server không phản hồi
            onError.postValue("Không kết nối được máy chủ");
        }
    });
}
```

Lý do dùng `postValue()` thay vì `setValue()`: `postValue()` an toàn khi gọi từ background thread (OkHttp chạy request ở worker thread). `setValue()` chỉ dùng được trên Main Thread.

---

## 9. TẦNG VIEWMODEL

### AuthViewModel.java — Đặc biệt nhất trong hệ thống

```java
public class AuthViewModel extends AndroidViewModel {

    // Tất cả là public MutableLiveData để Activity quan sát trực tiếp
    public final MutableLiveData<TokenResponse> loginResult    = new MutableLiveData<>();
    public final MutableLiveData<UserPublic>    registerResult = new MutableLiveData<>();
    public final MutableLiveData<UserPublic>    currentUser    = new MutableLiveData<>();
    public final MutableLiveData<String>        errorMessage   = new MutableLiveData<>();
    public final MutableLiveData<Boolean>       isLoading      = new MutableLiveData<>(false);

    public void saveTokens(String accessToken, String refreshToken) {
        tokenManager.saveTokens(accessToken, refreshToken);
        // Quan trọng: Reset và tái khởi tạo Retrofit để AuthInterceptor
        // đọc được access_token mới vừa lưu
        RetrofitClient.reset();
        RetrofitClient.init(getApplication());
    }

    public void logout() {
        // Bước 1: Hủy đăng ký FCM token trên server (chạy nền, không block UI)
        String token = tokenManager.getFcmToken();
        if (token != null) {
            RetrofitClient.getApiService()
                .unregisterFcmToken(token)
                .enqueue(...); // fire and forget
        }
        // Bước 2: Xóa toàn bộ dữ liệu cục bộ
        tokenManager.clearAll();
        RetrofitClient.reset();
    }
}
```

**Tại sao kế thừa `AndroidViewModel` thay vì `ViewModel`?**  
`AndroidViewModel` nhận `Application` trong constructor, cho phép truy cập `Context` (cần cho `TokenManager`) mà không bị memory leak. `ViewModel` thông thường không được giữ reference đến `Activity`/`Context`.

---

## 10. BẢN ĐỒ MÀN HÌNH & LUỒNG ĐIỀU HƯỚNG

```
Khởi động App
    ↓
SplashActivity (1.8 giây)
    ├── tokenManager.isLoggedIn() = true  →  MainActivity
    └── tokenManager.isLoggedIn() = false →  LoginActivity
                                                   ↕ (ActivityResultLauncher)
                                              RegisterActivity
                                              ForgotPasswordActivity
    ↓ Đăng nhập thành công
MainActivity (BottomNavigationView — 4 Tab)
    ├── Tab 1: HomeFragment
    │     └── Click nhóm → GroupDetailActivity
    ├── Tab 2: GroupsFragment
    │     ├── Click nhóm → GroupDetailActivity
    │     ├── FAB "Tạo nhóm" → Dialog inline
    │     └── FAB "Tham gia" → JoinEventActivity (hoặc qua Deep Link)
    ├── Tab 3: NotificationsFragment
    │     └── Click thông báo → GroupDetailActivity (có event_id)
    └── Tab 4: ProfileFragment
          ├── "Chỉnh sửa hồ sơ" → Dialog inline (update name, bank)
          ├── "Đổi mật khẩu" → Dialog inline
          └── "Đăng xuất" → LoginActivity (finishAffinity)

GroupDetailActivity (3 Tabs nội bộ)
    ├── Tab 0: "Chi tiêu" → danh sách ExpensePublic
    │     ├── Click chi tiêu → ExpenseDetailActivity
    │     └── Menu "Thêm chi tiêu" → AddExpenseActivity
    │                                     → Camera / Gallery + OCR
    ├── Tab 1: "Số dư" → render động UserBalance cards
    └── Tab 2: "Đơn giản hóa" → SimplifiedDebt cards
          ├── Button "Mã QR" → PaymentQrActivity (VietQR động)
          └── Button "Tôi đã gửi tiền" → SettlementActivity (ghi nhận)
    Menu Options:
    ├── "Tạo mã mời" → Share sheet (text + mã code)
    ├── "Thêm thành viên" → Dialog nhập email
    └── "Lịch sử thanh toán" → SettlementActivity

JoinEventActivity
    ├── Nhập mã thủ công → joinByCode(code)
    └── Deep Link: btck://join?code=XXXX → tự động joinByCode(code)
```

---

## 11. CHI TIẾT TỪNG MÀN HÌNH

### 11.1. SplashActivity — Màn hình khởi động

```java
// Hiệu ứng: Logo fade-in 800ms, Tagline fade-in sau 400ms offset
AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
fadeIn.setDuration(800);
logo.startAnimation(fadeIn);

// Sau 1800ms, kiểm tra đăng nhập và điều hướng
new Handler(Looper.getMainLooper()).postDelayed(() -> {
    if (tokenManager.isLoggedIn()) {
        startActivity(new Intent(this, MainActivity.class));
    } else {
        startActivity(new Intent(this, LoginActivity.class));
    }
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    finish();
}, 1800);
```

### 11.2. LoginActivity — Đăng nhập

Validation phía client (trước khi gọi API):
```java
// 1. Kiểm tra rỗng
if (TextUtils.isEmpty(email)) { binding.tilEmail.setError("Vui lòng nhập email"); return; }
// 2. Kiểm tra không có khoảng trắng
if (email.contains(" ")) { binding.tilEmail.setError("Email không được chứa khoảng trắng"); return; }
// 3. Kiểm tra ký tự @
if (!email.contains("@")) { binding.tilEmail.setError("Email thiếu ký tự @"); return; }
// 4. Kiểm tra định dạng email chuẩn bằng Patterns của Android
if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { ... return; }
```

Đặc biệt — **Kết hợp mượt mà với RegisterActivity**:
```java
// Dùng ActivityResultLauncher (API hiện đại, thay thế startActivityForResult deprecated)
private final ActivityResultLauncher<Intent> registerLauncher =
    registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            // Sau khi đăng ký xong, nhận email+password từ RegisterActivity
            // và tự điền vào form đăng nhập → UX tốt hơn
            String email = result.getData().getStringExtra("email");
            binding.etEmail.setText(email);
        }
    });
```

### 11.3. RegisterActivity — Đăng ký tài khoản

Validation phía client bao gồm cả kiểm tra confirm password:
```java
if (password.length() < 8)       { binding.tilPassword.setError("Mật khẩu tối thiểu 8 ký tự"); return; }
if (!password.equals(confirmPwd)) { binding.tilConfirmPassword.setError("Mật khẩu không khớp"); return; }
```

Sau khi đăng ký thành công, tự động trả email+password về LoginActivity:
```java
viewModel.registerResult.observe(this, user -> {
    if (user != null) {
        Intent data = new Intent();
        data.putExtra("email", email);
        data.putExtra("password", password);
        setResult(RESULT_OK, data);
        finish();
    }
});
```

### 11.4. MainActivity — Trung tâm điều hướng

**Setup FCM khi khởi động**:
```java
private void setupFcm() {
    // Android 13+ yêu cầu xin quyền POST_NOTIFICATIONS rõ ràng
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
    }

    // Lấy FCM Token mới nhất từ Firebase
    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
        String token = task.getResult();
        tokenManager.saveFcmToken(token); // lưu local
        if (tokenManager.isLoggedIn()) {
            sendTokenToServer(token); // đồng bộ lên server
        }
    });
}
```

**Badge thông báo chưa đọc** — Gọi mỗi lần `onResume()`:
```java
public void updateNotificationBadge() {
    RetrofitClient.getApiService().getUnreadCount().enqueue(new Callback<>() {
        @Override
        public void onResponse(...) {
            int count = response.body().count;
            BadgeDrawable badge = binding.bottomNav.getOrCreateBadge(R.id.nav_notifications);
            if (count > 0) {
                badge.setVisible(true);
                badge.setMaxNumber(10); // Hiện "10+" thay vì số lớn
                badge.setNumber(count);
            } else {
                badge.setVisible(false);
                binding.bottomNav.removeBadge(R.id.nav_notifications);
            }
        }
    });
}
```

### 11.5. HomeFragment — Trang chủ thông minh

**Shimmer Loading Effect** — Trước khi có dữ liệu:
```java
private void loadData() {
    binding.shimmerLayout.startShimmer(); // bắt đầu hiệu ứng shimmer bóng mờ chạy
    binding.shimmerLayout.setVisibility(View.VISIBLE);
    binding.contentLayout.setVisibility(View.GONE); // ẩn content thật
    eventViewModel.loadEvents();
    eventViewModel.loadMyBalance();
}
// Khi có dữ liệu → shimmer ẩn, contentLayout hiện ra
```

**Balance Card hiển thị thông minh**:
```java
long owe  = balance.summary.totalYouOwe;   // Tổng bạn đang nợ người khác
long owed = balance.summary.totalOwedToYou; // Tổng người khác đang nợ bạn
long net  = balance.summary.netBalance;     // Số dư ròng (owed - owe)

binding.tvTotalOwe.setText("-" + formatMoney(owe));   // Màu đỏ
binding.tvTotalOwed.setText("+" + formatMoney(owed)); // Màu xanh
```

**Format tiền tự động theo đơn vị**:
```java
private String formatMoney(long amount) {
    if (amount >= 1_000_000_000) return String.format("%.1f tỷđ", amount / 1e9);
    if (amount >= 1_000_000)     return String.format("%.1f triệuđ", amount / 1e6);
    return String.format("%,dđ", amount); // dưới 1 triệu: hiện đầy đủ (1.000đ)
}
```

**Balance Hint cho từng nhóm gần đây** — Gọi song song `getSimplifiedDebts()` cho từng group:
```java
// Với mỗi nhóm, gọi API riêng để lấy nợ đơn giản hóa
// → hiện tóm tắt như "Bạn nợ Huy 50.000đ +2 khoản" ngay trên card nhóm
RetrofitClient.getApiService().getSimplifiedDebts(event.id).enqueue(...);
```

### 11.6. GroupDetailActivity — Phức tạp nhất (652 dòng code)

**Ba ViewModel hoạt động song song**:
```java
private EventViewModel eventViewModel;     // group info, balances, stats, invite code
private ExpenseViewModel expenseViewModel; // danh sách chi tiêu
private SettlementViewModel settlementViewModel; // ghi nhận thanh toán
```

**Cơ chế 3 Tab với ContentView toggle** (không dùng ViewPager):
```java
private void switchTab(int position) {
    currentTab = position;
    switch (position) {
        case 0: // Tab Chi tiêu
            binding.rvExpenses.setVisibility(expenseList.isEmpty() ? GONE : VISIBLE);
            binding.layoutBalance.setVisibility(GONE);
            binding.layoutSimplified.setVisibility(GONE);
            break;
        case 1: // Tab Số dư
            eventViewModel.loadEventBalances(eventId); // Gọi API ngay khi chọn tab
            binding.layoutBalance.setVisibility(VISIBLE);
            break;
        case 2: // Tab Đơn giản hóa
            eventViewModel.loadSimplifiedDebts(eventId); // Gọi API ngay khi chọn tab
            binding.layoutSimplified.setVisibility(VISIBLE);
            break;
    }
}
```

**Render Balance Cards bằng code Java (không dùng XML adapter)**:
Thay vì dùng RecyclerView, toàn bộ các card số dư được xây dựng động bằng Java:
```java
MaterialCardView card = new MaterialCardView(this);
card.setCardElevation(dp(1));
card.setRadius(dp(12));

TextView initial = new TextView(this);
initial.setText(name.substring(0, 1).toUpperCase()); // Chữ cái đầu tên làm avatar
initial.setBackgroundResource(R.drawable.bg_mint_circle);
```

**Cơ chế an toàn tránh double-submit thanh toán**:
```java
private final Set<String> pendingSettlementKeys = new HashSet<>();

private void confirmDebtPaid(SimplifiedDebt debt) {
    // Tạo unique key từ "fromUserId:toUserId:amount"
    String pendingKey = debt.fromUserId + ":" + debt.toUserId + ":" + debt.amount;
    if (!pendingSettlementKeys.add(pendingKey)) {
        // Key đã có trong set → đang chờ kết quả → bỏ qua
        Toast.makeText(this, "Đang ghi nhận thanh toán, vui lòng chờ...", Toast.LENGTH_SHORT).show();
        return;
    }
    settlementViewModel.createSettlement(...);
}
// Khi server trả kết quả → pendingSettlementKeys.clear()
```

**Điều hướng mở màn hình cụ thể khi được target từ FCM**:
```java
// Màn hình có thể được mở trực tiếp ở tab bất kỳ khi đến từ FCM notification
int targetTab = getIntent().getIntExtra("target_tab", 0);
if (targetTab > 0) {
    binding.tabLayout.getTabAt(targetTab).select();
}
```

**Chia sẻ mã mời với thời gian hết hạn**:
```java
eventViewModel.inviteCode.observe(this, invite -> {
    if (invite != null && invite.code != null) {
        String expiry = ""; // ví dụ: "(Hết hạn: 2026-05-28 10:30 UTC)"
        if (invite.expiresAt != null) {
            expiry = "\n(Hết hạn: " + invite.expiresAt.replace("T", " ").substring(0, 16) + " UTC)";
        }
        String shareText = "Tham gia nhóm \"" + eventName + "\" với mã: " + invite.code + expiry;

        // Copy mã vào clipboard
        ClipboardManager clipboard = getSystemService(CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("invite_code", invite.code));

        // Mở share sheet
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Chia sẻ mã mời"));

        // Reset LiveData để không trigger lại khi onResume
        eventViewModel.inviteCode.setValue(null);
    }
});
```

**Phát hiện và mở QR thanh toán (lấy bank info từ cached data)**:
```java
private void openDebtQr(SimplifiedDebt debt) {
    Intent intent = new Intent(this, PaymentQrActivity.class);
    intent.putExtra("user_id", debt.toUserId);
    intent.putExtra("amount", debt.amount);

    // Tái sử dụng data đã load trong Tab "Số dư" (không cần gọi API mới)
    if (eventViewModel.eventBalances.getValue() != null) {
        for (UserBalance balance : eventViewModel.eventBalances.getValue().balances) {
            if (balance.userId.equals(debt.toUserId)) {
                // Truyền thẳng thông tin ngân hàng của người nhận
                intent.putExtra("bank_name", balance.bankName);
                intent.putExtra("account_number", balance.accountNumber);
                intent.putExtra("account_holder", balance.accountHolder);
                break;
            }
        }
    }
    startActivity(intent);
}
```

**Reload data thông minh, tránh gọi 2 lần khi khởi tạo**:
```java
private boolean isFirstLoad = true;

@Override
protected void onResume() {
    super.onResume();
    if (isFirstLoad) {
        isFirstLoad = false; // onCreate đã gọi loadData() rồi, bỏ qua
    } else {
        loadData(); // Chỉ reload khi thực sự quay lại từ Activity con
    }
}
```

### 11.7. AddExpenseActivity — Thêm chi tiêu (558 dòng code)

**Hai chế độ chia tiền qua RadioGroup**:
```java
binding.rgSplitMethod.setOnCheckedChangeListener((group, checkedId) -> {
    splitAdapter.setEqualSplitMode(checkedId == R.id.rbEqual); // bật/tắt input tùy chỉnh
    recalculateSplits();
});
```

**Thuật toán chia đều (Equal Split)**:
```java
private void recalculateSplits() {
    long total = Long.parseLong(amountStr);
    List<SplitItem> selected = getSelectedMembers();

    long each = total / selected.size();       // phần bằng nhau
    long remainder = total % selected.size();  // số dư (nếu không chia hết)

    for (int i = 0; i < selected.size(); i++) {
        // Người đầu tiên trong list gánh thêm phần dư (tránh làm tròn mất tiền)
        selected.get(i).amountOwed = each + (i == 0 ? remainder : 0);
    }
}
```

**Validation cuối cùng trước khi gửi API**:
```java
long totalSplit = 0;
for (SplitItem item : splitItems) {
    if (item.isSelected && item.amountOwed > 0) totalSplit += item.amountOwed;
}
// Bắt buộc: tổng phần chia PHẢI khớp chính xác với tổng chi tiêu
if (totalSplit != amount) {
    Toast.makeText(this, "Tổng chia tiền phải bằng tổng chi tiêu", Toast.LENGTH_SHORT).show();
    return;
}
```

**Tải thành viên qua dữ liệu balance** (không có endpoint riêng cho members list trong flow này):
```java
// Thay vì gọi GET /events/{id}/members, tận dụng GET /events/{id}/balances
// vì balances đã chứa thông tin user của tất cả thành viên
eventViewModel.eventBalances.observe(this, balances -> {
    for (UserBalance b : balances.balances) {
        EventMemberPublic m = new EventMemberPublic();
        m.userId = b.userId;
        m.userFullName = b.userFullName;
        memberList.add(m); // dùng làm danh sách người chia tiền
    }
    setupPayerSpinner();
});
```

### 11.8. JoinEventActivity — Tham gia qua mã mời & Deep Link

```java
private void handleDeepLink() {
    Intent intent = getIntent();
    if (intent != null && intent.getData() != null) {
        // Xử lý URI dạng: btck://join?code=A1B2C3D4
        String code = intent.getData().getQueryParameter("code");
        if (code != null) {
            String normalized = InviteCodeUtils.normalize(code); // uppercase + trim
            binding.etCode.setText(normalized);
            joinByCode(normalized); // tự động tham gia không cần bấm nút
        }
    }
}
```

Lỗi từ server được dịch chính xác (nhờ ErrorUtils):
- `"Invalid or expired invite code"` → `"Mã mời không hợp lệ hoặc đã hết hạn"`
- `"Already a member of this event"` → `"Bạn đã là thành viên của nhóm này rồi"`

### 11.9. SettlementActivity — Ghi nhận thanh toán

**Dialog ghi nhận thanh toán tự điền thông tin nợ hiện tại**:
```java
// Tìm và chọn sẵn người nhận là người bạn đang nợ nhiều nhất
for (SimplifiedDebt debt : debtList) {
    if (currentUserId.equals(debt.fromUserId)) {
        // Chọn sẵn người nhận trong spinner
        spTo.setSelection(indexOfUser(debt.toUserId));
        // Điền sẵn số tiền nợ
        etAmount.setText(String.valueOf(debt.amount));
        break;
    }
}
// Khóa spinner "Người trả" (luôn là chính bạn, không cho thay đổi)
spFrom.setSelection(currentUserIndex);
spFrom.setEnabled(false);
```

---

## 12. TÍNH NĂNG OCR ĐỌC HÓA ĐƠN TỰ ĐỘNG

Tích hợp **Google ML Kit Text Recognition** để quét ảnh hóa đơn và tự động điền số tiền + mô tả vào form:

### Luồng hoạt động
```
User chụp ảnh/chọn từ gallery
    ↓
ML Kit TextRecognizer.process(InputImage)
    ↓
parseReceiptText(visionText.getText())
    ↓
Tìm từ khóa: "tổng cộng", "thành tiền", "total", "grand total",...
    ↓
Extract số tiền hợp lệ (1.000đ - 50.000.000đ)
    ↓
Điền vào binding.etAmount và binding.etDescription
```

### Chi tiết thuật toán phân tích văn bản hóa đơn

```java
private void parseReceiptText(String text) {
    String[] lines = text.split("\n");
    long detectedAmount = 0;
    String detectedDescription = "";

    // Danh sách từ khóa "tổng tiền" bằng cả tiếng Việt có dấu và không dấu
    List<String> totalKeywords = Arrays.asList(
        "tong cong", "tổng cộng", "thành tiền", "thanh tien",
        "thanh toán", "thanh toan", "tổng tiền", "tong tien",
        "total", "grand total", "net amount", "cộng", "cong"
    );

    // Pattern nhận dạng số tiền: "1.500.000" hoặc "1,500,000" hoặc "1500000"
    Pattern numberPattern = Pattern.compile("\\b\\d{1,3}([.,]\\d{3})+\\b|\\b\\d{4,9}\\b");

    List<Long> numbers = new ArrayList<>();

    for (String line : lines) {
        // Lấy dòng đầu tiên không phải số làm mô tả (tên quán ăn, tên sản phẩm)
        if (detectedDescription.isEmpty()) {
            if (line.trim().length() > 3 && !line.matches(".*\\d{5,}.*")) {
                detectedDescription = line.trim();
            }
        }

        // Kiểm tra dòng có chứa từ khóa tổng tiền không
        boolean hasTotalKeyword = totalKeywords.stream().anyMatch(line.toLowerCase()::contains);

        if (hasTotalKeyword) {
            // Ưu tiên lấy số trên dòng có từ khóa tổng tiền
            Matcher m = numberPattern.matcher(line);
            while (m.find()) {
                long val = Long.parseLong(m.group().replaceAll("[.,]", ""));
                if (val >= 1000 && val <= 50_000_000) { // lọc số hợp lý
                    detectedAmount = val;
                }
            }
            if (detectedAmount > 0) break; // tìm thấy rồi, dừng
        }

        // Lưu tất cả số tìm được làm backup
        Matcher m = numberPattern.matcher(line);
        while (m.find()) {
            long val = Long.parseLong(m.group().replaceAll("[.,]", ""));
            if (val >= 1000 && val <= 50_000_000) numbers.add(val);
        }
    }

    // Nếu không tìm được từ khóa tổng → lấy số lớn nhất (thường là tổng)
    if (detectedAmount == 0 && !numbers.isEmpty()) {
        detectedAmount = Collections.max(numbers);
    }

    // Cập nhật UI
    if (detectedAmount > 0) binding.etAmount.setText(String.valueOf(detectedAmount));
    if (!detectedDescription.isEmpty()) binding.etDescription.setText(detectedDescription);
}
```

---

## 13. TÍNH NĂNG VIETQR THANH TOÁN THÔNG MINH

### Luồng đầy đủ từ A→Z

```
GroupDetailActivity (Tab "Đơn giản hóa")
    │
    │ User nhấn nút "Mã QR" trên debt card
    ↓
openDebtQr(SimplifiedDebt debt):
    • Lấy bank_name, account_number, account_holder của debt.toUserId
      từ eventBalances LiveData đã có sẵn (không gọi API thêm)
    • Tạo Intent → PaymentQrActivity với extras:
        user_id, amount, description, bank_name, account_number, account_holder
    ↓
PaymentQrActivity.loadQr():
    RetrofitClient.getApiService()
        .getPaymentQr(userId, amount, "Thanh toan " + eventName)
        .enqueue(...)
    ↓
Backend FastAPI /api/v1/users/{user_id}/payment-qr:
    • Lấy thông tin ngân hàng của user từ DB
    • Gọi dịch vụ VietQR.io tạo URL ảnh QR
    • Trả về URL dạng: "https://img.vietqr.io/image/..."
    ↓
PaymentQrActivity nhận URL (dưới dạng JSON string có dấu nháy kép):
    // Strip dấu nháy kép nếu có
    if (qrUrl.startsWith("\"") && qrUrl.endsWith("\"")) {
        qrUrl = qrUrl.substring(1, qrUrl.length() - 1);
    }
    ↓
Glide.with(this).asBitmap().load(qrUrl) → hiển thị lên ImageView
    ↓
User có thể:
    ├── Lưu QR vào Gallery: saveQrToGallery() dùng MediaStore API (Android Q+)
    └── Chia sẻ QR: shareQr() dùng Intent.ACTION_SEND dạng image/png
```

### Xử lý lưu ảnh QR chuẩn Android Q+

```java
private void saveQrToGallery() {
    ContentValues values = new ContentValues();
    values.put(MediaStore.Images.Media.DISPLAY_NAME, "btck_payment_qr_" + System.currentTimeMillis() + ".png");
    values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Android 10+: không cần quyền WRITE_EXTERNAL_STORAGE
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/BTCK");
        values.put(MediaStore.Images.Media.IS_PENDING, 1); // đang ghi, chưa visible
    }

    Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    try (OutputStream out = getContentResolver().openOutputStream(imageUri)) {
        qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        values.put(MediaStore.Images.Media.IS_PENDING, 0); // hoàn thành, visible
        getContentResolver().update(imageUri, values, null, null);
    }
}
```

---

## 14. HỆ THỐNG THÔNG BÁO ĐẨY FCM

### Vòng đời đầy đủ của một thông báo FCM

```
Khởi động app lần đầu / Token thay đổi
    ↓
MyFirebaseMessagingService.onNewToken(token)
    ├── tokenManager.saveFcmToken(token)   ← lưu local
    └── sendTokenToServer(token)           ← POST /api/v1/users/me/fcm-token
                                              (chỉ gửi nếu đã đăng nhập)
    ↓
Backend nhận token, lưu vào DB gắn với user_id

Khi có event trong nhóm (chi tiêu mới, thanh toán,...):
Backend gửi FCM message đến tất cả thành viên có FCM token
    ↓
MyFirebaseMessagingService.onMessageReceived(remoteMessage)
    ├── Đọc notification payload: title, body
    └── Đọc data payload: type, event_id, title, message
    ↓
showNotification(title, body, type, eventId):
    • Xác định Intent điều hướng thông minh:
        if (eventId != null && type.contains("expense" || "settlement"))
            → Intent → GroupDetailActivity (event_id = eventId)
        else
            → Intent → MainActivity
    ↓
NotificationCompat.Builder:
    • setAutoCancel(true)      ← tự đóng khi user click
    • setPriority(HIGH)        ← Heads-up notification (hiện nổi trên app hiện tại)
    • setStyle(BigTextStyle)   ← nội dung dài hiện đầy đủ khi expand
    • setSound(defaultSound)   ← âm thanh thông báo mặc định của máy

Đăng xuất:
    AuthViewModel.logout()
    → RetrofitClient.getApiService().unregisterFcmToken(fcmToken)
    → DELETE /api/v1/users/me/fcm-token/{token}
    → Backend xóa token → không push thông báo nữa
```

---

## 15. XỬ LÝ LỖI TỪ BACKEND — ErrorUtils

Đây là lớp utility quan trọng đảm bảo UX tốt khi có lỗi:

```java
public static String parseError(Response<?> response) {
    int code = response.code();

    // Server error (5xx) hoặc rate limit (429) → thông báo chung
    if (code == 429 || code >= 500) return "Không kết nối được máy chủ";

    try {
        String body = response.errorBody().string();

        // FastAPI trả lỗi dạng: {"detail": "Error message here"}
        if (body.contains("\"detail\":\"")) {
            int start = body.indexOf("\"detail\":\"") + 10;
            int end = body.indexOf("\"", start);
            String detail = body.substring(start, end);

            // Map các lỗi tiếng Anh từ BE sang tiếng Việt thân thiện
            if ("Invalid or expired invite code".equals(detail))
                return "Mã mời không hợp lệ hoặc đã hết hạn";
            if ("Already a member of this event".equals(detail))
                return "Bạn đã là thành viên của nhóm này rồi";
            if ("Event not found".equals(detail))
                return "Không tìm thấy nhóm";

            return detail; // Các lỗi khác: hiện nguyên text từ backend
        }

        // Lỗi HTTP chuẩn nếu không có detail
        if (code == 400) return "Email hoặc mật khẩu không đúng";
        if (code == 401) return "Phiên đăng nhập hết hạn";
        if (code == 404) return "Không tìm thấy dữ liệu";
        if (code == 422) return "Dữ liệu không hợp lệ";
        return "Lỗi: " + code;

    } catch (Exception e) {
        return "Lỗi không xác định";
    }
}
```

---

## 16. DESIGN SYSTEM & GIAO DIỆN

### Color Palette (từ `values/colors.xml`)

| Token | Hex | Sử dụng |
|---|---|---|
| `mint_primary` | `#B8F2E6` | Màu nền chip, badge, button phụ |
| `mint_dark` | `#52D9C1` | Text nhấn, border, button chính |
| `accent` | `#00C9A7` | FAB, icon active |
| `text_dark` | `#1A1A2E` | Text chính |
| `text_secondary` | `#6B7280` | Text phụ, subtitle |
| `color_owe` | `#FF6B6B` | Số tiền đang nợ (màu đỏ nhạt) |
| `color_owed` | `#00B894` | Số tiền được nhận (màu xanh lá) |
| `background_white` | `#FFFFFF` | Nền card |
| `background_light` | `#F8FFFE` | Nền screen |

### Typography
- Font **Inter** (Regular + Bold) từ Google Fonts, nhúng trực tiếp vào assets (`res/font/`)
- Sử dụng xuyên suốt qua `Typeface.DEFAULT, Typeface.BOLD` và CSS trong theme

### UI Components Pattern
- **Cards**: Tất cả dùng `MaterialCardView` với `radius=12dp`, `elevation=1dp-2dp`
- **Buttons**: `MaterialButton` style từ Material Design 3
- **Dialogs**: `MaterialAlertDialogBuilder` (tự động theo theme hệ thống Dark/Light)
- **Inputs**: `TextInputLayout` + `TextInputEditText` với error state support

---

## 17. CÁC KỸ THUẬT XỬ LÝ NÂNG CAO TRONG CODE

### 17.1. Chống Memory Leak với Lifecycle-aware Observer

**Trước (Sai - gây memory leak)**:
```java
// Đăng ký lắng nghe không bao giờ tự hủy
viewModel.someData.observeForever(data -> updateUI(data));
```

**Sau (Đúng - lifecycle-aware)**:
```java
// Tự động hủy lắng nghe khi Activity bị destroy
viewModel.someData.observe(this, data -> updateUI(data));
// Hoặc trong Fragment:
viewModel.someData.observe(getViewLifecycleOwner(), data -> updateUI(data));
```

### 17.2. Reset LiveData sau khi xử lý (tránh trigger lại khi rotate màn hình)

```java
settlementViewModel.createdSettlement.observe(this, settlement -> {
    if (settlement != null) {
        Toast.makeText(this, "Đã ghi nhận thanh toán", Toast.LENGTH_SHORT).show();
        // Quan trọng: set null để Observer không kích hoạt lại lần sau
        settlementViewModel.createdSettlement.setValue(null);
        loadData();
    }
});
```

### 17.3. Chuyển đổi px ↔ dp chuẩn xác

```java
// Hàm utility tính dp → pixel theo density màn hình
private int dp(int value) {
    return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
}
// Dùng: card.setRadius(dp(12));  → 12dp bất kể mật độ màn hình
```

### 17.4. CurrencyTextWatcher — Format tiền khi gõ

Khi người dùng gõ số tiền vào ô nhập, `CurrencyTextWatcher` tự động định dạng lại:
- Input: `1500000` → Hiển thị: `1.500.000`  
- Khi đọc lại value: xóa dấu chấm `amountStr.replace(".", "")` trước khi parse.

### 17.5. InviteCodeUtils — Chuẩn hóa mã mời

```java
// normalize("  a1b2c3d4  ") → "A1B2C3D4"
public static String normalize(String code) {
    return code == null ? "" : code.trim().toUpperCase();
}
```

---

## 18. DANH SÁCH TOÀN BỘ API ENDPOINTS

**Base URL**: `http://e1.chiasegpu.vn:34093/api/v1/`

| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| `POST` | `auth/login` | Đăng nhập (form-encoded: username, password) | ❌ |
| `POST` | `auth/register` | Đăng ký tài khoản | ❌ |
| `POST` | `auth/refresh` | Làm mới access token | ❌ |
| `POST` | `auth/logout` | Đăng xuất | ✅ |
| `POST` | `auth/password-recovery/{email}` | Gửi email reset mật khẩu | ❌ |
| `POST` | `auth/reset-password/` | Đặt mật khẩu mới | ❌ |
| `GET` | `auth/me` | Lấy thông tin user hiện tại | ✅ |
| `GET` | `users/me` | Lấy thông tin user (alias) | ✅ |
| `PATCH` | `users/me` | Cập nhật thông tin hồ sơ | ✅ |
| `PATCH` | `users/me/password` | Đổi mật khẩu | ✅ |
| `DELETE` | `users/me` | Xóa tài khoản | ✅ |
| `POST` | `users/me/avatar` | Upload ảnh đại diện (multipart) | ✅ |
| `POST` | `users/me/fcm-token` | Đăng ký FCM token thiết bị | ✅ |
| `DELETE` | `users/me/fcm-token/{token}` | Hủy đăng ký FCM token | ✅ |
| `GET` | `users/search?email=` | Tìm kiếm user theo email | ✅ |
| `GET` | `users/{user_id}/payment-qr` | Lấy URL ảnh mã QR VietQR | ✅ |
| `GET` | `utils/banks` | Danh sách ngân hàng Việt Nam | ✅ |
| `GET` | `events/` | Lấy danh sách nhóm (phân trang) | ✅ |
| `POST` | `events/` | Tạo nhóm mới | ✅ |
| `GET` | `events/{event_id}` | Lấy chi tiết một nhóm | ✅ |
| `PUT` | `events/{event_id}` | Cập nhật thông tin nhóm | ✅ |
| `DELETE` | `events/{event_id}` | Xóa nhóm | ✅ |
| `GET` | `events/me/balance` | Tổng hợp số dư cá nhân toàn bộ nhóm | ✅ |
| `POST` | `events/{event_id}/members` | Thêm thành viên bằng email | ✅ |
| `DELETE` | `events/{event_id}/members/{user_id}` | Xóa thành viên | ✅ |
| `GET` | `events/{event_id}/balances` | Số dư chi tiết từng thành viên nhóm | ✅ |
| `GET` | `events/{event_id}/balances/simplify` | Danh sách nợ đã tối ưu hóa | ✅ |
| `GET` | `events/{event_id}/stats` | Thống kê nhóm (tổng chi, số dư bạn) | ✅ |
| `POST` | `events/{event_id}/invite` | Tạo mã mời (có thời hạn) | ✅ |
| `POST` | `events/join/{code}` | Tham gia nhóm bằng mã mời | ✅ |
| `GET` | `events/{event_id}/expenses/` | Danh sách chi tiêu (phân trang) | ✅ |
| `POST` | `events/{event_id}/expenses/` | Tạo chi tiêu mới | ✅ |
| `GET` | `events/{event_id}/expenses/{expense_id}` | Chi tiết một chi tiêu | ✅ |
| `PUT` | `events/{event_id}/expenses/{expense_id}` | Sửa chi tiêu | ✅ |
| `DELETE` | `events/{event_id}/expenses/{expense_id}` | Xóa chi tiêu | ✅ |
| `POST` | `events/{event_id}/expenses/{expense_id}/image` | Upload ảnh hóa đơn (multipart) | ✅ |
| `GET` | `events/{event_id}/settlements/` | Danh sách thanh toán (phân trang) | ✅ |
| `POST` | `events/{event_id}/settlements/` | Ghi nhận thanh toán | ✅ |
| `GET` | `events/{event_id}/settlements/{settlement_id}` | Chi tiết một thanh toán | ✅ |
| `DELETE` | `events/{event_id}/settlements/{settlement_id}` | Xóa thanh toán | ✅ |
| `GET` | `notifications/` | Danh sách thông báo | ✅ |
| `GET` | `notifications/unread-count` | Đếm thông báo chưa đọc | ✅ |
| `PATCH` | `notifications/{notification_id}/read` | Đánh dấu một thông báo đã đọc | ✅ |
| `POST` | `notifications/mark-all-read` | Đánh dấu tất cả đã đọc | ✅ |

---

*Tài liệu này được biên soạn chi tiết từ mã nguồn thực tế của dự án.*  
*Phục vụ báo cáo đồ án, ôn tập và nghiên cứu kiến trúc Android MVVM.*
