# 📘 CẨM NANG PHÂN TÍCH TOÀN BỘ CÁC LUỒNG HOẠT ĐỘNG TỪ MÀN HÌNH ĐẾN API (SCREEN TO API FLOWS GUIDE)
> **Dự án**: SplitMate (Quản lý và chia sẻ chi tiêu nhóm)  
> **Vị trí lưu trữ file này**: Lưu tại thư mục gốc của dự án: `/Users/leminhdao168/Documents/học tập/code/mobile/bài tập cuối kì/BTCK-APP-FE/MVVM_FLOW_GUIDE.md`

---

## 🗺️ PHẦN 1: BẢN ĐỒ KIẾN TRÚC TỔNG QUAN (MVVM SYSTEM ARCHITECTURE)

Ứng dụng SplitMate được xây dựng chặt chẽ theo mô hình **MVVM (Model - View - ViewModel)** kết hợp với **Repository Pattern** để quản lý luồng dữ liệu một cách độc lập:

```text
  [ VIEW LAYER (Giao diện) ]           [ VIEWMODEL LAYER ]             [ DATA LAYER (Dữ liệu) ]
   Activity / Fragment                 AndroidViewModel                 Repository (Single Source)
         │                                    │                                    │
   Bắt sự kiện Click,                  Giữ trạng thái dữ liệu,          Quyết định lấy từ DB/Mạng,
   Hiển thị Toast, Progress            Cung cấp LiveData cho View       Post kết quả về ViewModel
         │                                    │                                    │
         ▼ (1. Thao tác Click)                ▼ (2. Gọi Repo)                      ▼ (3. Gọi API)
   LoginActivity  ───────────────►  AuthViewModel  ────────────────►  AuthRepository
         ▲                                                                 │
         │                                                                 ▼
   (5. Tự động Render UI) ◄─────── [ LiveData (observe) ] ◄──(4. postValue)┘
```

---

## 🔗 PHẦN 2: LUỒNG KHỞI TẠO DÂY CHUYỀN (INITIALIZATION CHAIN)
*Kích hoạt ngay khi bạn mở app để lắp ráp các bộ phận của kiến trúc lại với nhau.*

### Sơ đồ phản ứng dây chuyền:
```text
[1. LoginActivity.onCreate] 
       │ 
       ▼ (Chạy lệnh: new ViewModelProvider(this).get(AuthViewModel.class))
[2. AuthViewModel Constructor]
       │
       ▼ (Chạy lệnh: repository = new AuthRepository();)
[3. AuthRepository Constructor]
       │
       ▼ (Chạy lệnh: api = RetrofitClient.getApiService();)
[4. RetrofitClient / ApiService] (Hoàn tất kết nối đường ống mạng)
```

### Chi tiết các file và hàm thực thi:
1. **Tại [LoginActivity.java](file:///Users/leminhdao168/Documents/học tập/code/mobile/bài tập cuối kì/BTCK-APP-FE/app/src/main/java/com/example/btck/activities/LoginActivity.java#L37)**:
   * **Dòng 37:** `viewModel = new ViewModelProvider(this).get(AuthViewModel.class);`
   * **Ý nghĩa:** Giao diện yêu cầu hệ thống cung cấp (hoặc tạo mới) đối tượng `AuthViewModel`.
2. **Tại [AuthViewModel.java](file:///Users/leminhdao168/Documents/học tập/code/mobile/bài tập cuối kì/BTCK-APP-FE/app/src/main/java/com/example/btck/viewmodel/AuthViewModel.java#L23-L28)**:
   * **Hàm dựng:** `public AuthViewModel(Application application)`
   * **Dòng 27:** `repository = new AuthRepository();`
   * **Ý nghĩa:** ViewModel tự động tạo mới đối tượng `AuthRepository` để sẵn sàng làm cầu nối lấy dữ liệu.
3. **Tại [AuthRepository.java](file:///Users/leminhdao168/Documents/học tập/code/mobile/bài tập cuối kì/BTCK-APP-FE/app/src/main/java/com/example/btck/repository/AuthRepository.java#L16-L18)**:
   * **Hàm dựng:** `public AuthRepository()`
   * **Dòng 17:** `api = RetrofitClient.getApiService();`
   * **Ý nghĩa:** Tầng dữ liệu tự động gọi `RetrofitClient` lấy về proxy kết nối của interface `ApiService` sẵn sàng thực hiện gọi mạng.

---

## 🔄 PHẦN 3: PHÂN TÍCH CHI TIẾT 4 LUỒNG TỪ MÀN HÌNH XUỐNG HẲN API

### 🔑 Luồng 1: Xác Thực & Đăng Nhập (Từ Màn Hình -> Xuống Hẳn API)
*Luồng đi của tài khoản/mật khẩu từ lúc nhấn nút Đăng nhập lên Server, lấy mã Token và đồng bộ thông tin cá nhân.*

```text
 [1. MÀN HÌNH: LoginActivity.java] 
       │ 
       ▼ (Người dùng click binding.btnLogin)
       ▼ (Kiểm tra dữ liệu đầu vào: etEmail, etPassword)
       ▼ (Gọi lệnh: viewModel.login(email, password))
       │
 [2. VIEWMODEL: AuthViewModel.java]
       │
       ▼ (Gọi lệnh: repository.login(email, password, loginResult, errorMessage))
       │
 [3. REPOSITORY: AuthRepository.java]
       │
       ▼ (Gọi lệnh: api.login(email, password).enqueue(new Callback<TokenResponse>()))
       │
 [4. ĐƯỜNG MẠNG: ApiService.java]
       │
       ▼ (Dịch Annotation @POST("auth/login") thành yêu cầu HTTP POST gửi đi)
       │
 [5. SERVER BACKEND FASTAPI] (http://e1.chiasegpu.vn:34093/api/v1/auth/login)
```

#### Chi tiết các bước thực hiện trong Code:
* **Bước 1: Bắt đầu tại Giao diện (`LoginActivity.java`):**
  * Sự kiện click nút đăng nhập được kích hoạt: `binding.btnLogin.setOnClickListener`.
  * Sau khi kiểm tra email và mật khẩu đúng định dạng, app gọi:
    `viewModel.login(email, password);`
* **Bước 2: Trung chuyển tại ViewModel (`AuthViewModel.java`):**
  * Hàm `login(String email, String password)` nhận dữ liệu, đặt trạng thái quay tròn loading `isLoading.setValue(true)`, rồi chuyển tiếp:
    `repository.login(email, password, loginResult, errorMessage);`
* **Bước 3: Tầng xử lý dữ liệu (`AuthRepository.java`):**
  * Hàm `login(...)` nhận email/password và gọi `api.login(...)` chạy ngầm bằng `enqueue()` để không làm treo giao diện điện thoại:
    ```java
    api.login(email, password).enqueue(new Callback<TokenResponse>() { ... });
    ```
* **Bước 4: Gọi API thực tế mạng (`ApiService.java`):**
  * Interface `ApiService.java` định nghĩa cấu trúc yêu cầu HTTP gửi đến Server Backend FastAPI:
    ```java
    @FormUrlEncoded
    @POST("auth/login")
    Call<TokenResponse> login(@Field("username") String username, @Field("password") String password);
    ```
* **Bước 5: Xử lý phản hồi (Callback) & Đồng bộ User:**
  * Server trả về Token dạng JSON. Repository nhận được qua hàm `onResponse()` và đẩy vào LiveData `onSuccess.postValue(response.body())`.
  * `AuthViewModel` nhận được dữ liệu, gọi `saveTokens()` để lưu Token vào **SharedPreferences** qua lớp `TokenManager`, đồng thời **làm mới cấu hình Retrofit** để tự động đính kèm Token này vào Header các API sau.
  * Tiếp tục gọi API thứ hai **`GET auth/me`** để lấy thông tin chi tiết cá nhân:
    * **Định nghĩa trong `ApiService.java`:**
      ```java
      @GET("auth/me")
      Call<UserPublic> getMe();
      ```
    * **Mục tiêu:** Lưu thông tin `id`, `email`, `fullName` vào SharedPreferences rồi đưa người dùng vào màn hình chính **`MainActivity`**.

---

### 👥 Luồng 2: Tạo Nhóm Mới (Từ Màn Hình -> Xuống Hẳn API)
*Luồng đi của thông tin nhóm mới từ nút nhấn đến Server Backend để lưu trữ.*

```text
 [1. MÀN HÌNH: GroupsFragment.java] 
       │ 
       ▼ (Người dùng mở Dialog tạo nhóm, nhập tên/mô tả -> Bấm "Tạo")
       ▼ (Gọi lệnh: eventViewModel.createEvent(body))
       │
 [2. VIEWMODEL: EventViewModel.java]
       │
       ▼ (Gọi lệnh: repository.createEvent(body, createdEvent, errorMessage))
       │
 [3. REPOSITORY: EventRepository.java]
       │
       ▼ (Gọi lệnh: api.createEvent(body).enqueue(new Callback<EventPublic>()))
       │
 [4. ĐƯỜNG MẠNG: ApiService.java]
       │
       ▼ (Dịch Annotation @POST("events/") thành yêu cầu HTTP POST gửi đi)
       │
 [5. SERVER BACKEND FASTAPI] (http://e1.chiasegpu.vn:34093/api/v1/events/)
```

#### Chi tiết các bước thực hiện trong Code:
* **Bước 1: Bắt đầu tại Giao diện (`GroupsFragment.java`):**
  * Khi người dùng nhấn nút "Tạo" trên Dialog tạo nhóm, app thu thập thông tin tên nhóm và mô tả nhóm đóng gói vào đối tượng `EventCreate`, sau đó gọi:
    `eventViewModel.createEvent(body);`
* **Bước 2: Trung chuyển tại ViewModel (`EventViewModel.java`):**
  * Hàm `createEvent(EventCreate body)` đặt vòng quay loading và gọi:
    `repository.createEvent(body, createdEvent, errorMessage);`
* **Bước 3: Tầng xử lý dữ liệu (`EventRepository.java`):**
  * Lớp gọi API chạy mạng không đồng bộ:
    ```java
    api.createEvent(body).enqueue(new Callback<EventPublic>() { ... });
    ```
* **Bước 4: Gọi API thực tế mạng (`ApiService.java`):**
  * Interface `ApiService.java` định nghĩa cấu trúc:
    ```java
    @POST("events/")
    Call<EventPublic> createEvent(@Body EventCreate body);
    ```
* **Bước 5: Hoàn tất:**
  * Server tạo nhóm mới thành công và trả về thông tin nhóm dạng JSON. `GroupsFragment` lắng nghe thấy dữ liệu thay đổi, lập tức đóng Dialog và tự động tải lại danh sách nhóm để hiển thị nhóm mới lên màn hình.

---

### 💵 Luồng 3: Tạo Chi Tiêu Mới & Upload Ảnh Hóa Đơn (Từ Màn Hình -> Xuống Hẳn API)
*Luồng đi cực kỳ phức tạp gồm 2 giai đoạn: Lưu thông tin chi tiêu trước, sau đó upload ảnh hóa đơn dạng Multipart lên sau.*

```text
 [1. MÀN HÌNH: AddExpenseActivity.java] 
       │ 
       ▼ (Người dùng nhập thông tin -> Click biểu tượng Lưu / action_save)
       ▼ (Gọi lệnh: expenseViewModel.createExpense(eventId, body))
       │
 [2. VIEWMODEL: ExpenseViewModel.java]
       │
       ▼ (Gọi lệnh: repository.createExpense(eventId, body, createdExpense, errorMessage))
       │
 [3. REPOSITORY: ExpenseRepository.java]
       │
       ▼ (Giai đoạn A - Lưu giao dịch: api.createExpense(eventId, body).enqueue(...))
       │
 [4. ĐƯỜNG MẠNG: ApiService.java] ──► (Dịch @POST("events/{event_id}/expenses/") gửi lên BE)
       │
       ▼ (Nhận thành công expense_id từ BE -> Quay lại AddExpenseActivity)
       │
 [5. MÀN HÌNH: AddExpenseActivity.java] 
       │ 
       ▼ (Kích hoạt hàm: uploadReceiptImage(expenseId))
       ▼ (Đóng gói File ảnh thành MultipartBody.Part)
       ▼ (Gọi lệnh API upload ảnh lên Server)
       │
 [6. ĐƯỜNG MẠNG: ApiService.java]
       │
       ▼ (Dịch Annotation @Multipart @POST(".../image") gửi ảnh nhị phân đi)
       │
 [7. SERVER BACKEND FASTAPI] (Lưu trữ ảnh hóa đơn gắn với ID chi tiêu)
```

#### Chi tiết các bước thực hiện trong Code:
* **Bước 1: Bắt đầu tại Giao diện (`AddExpenseActivity.java`):**
  * Khi người dùng nhấn nút "Lưu" trên Toolbar (`action_save`), app chạy thuật toán tính toán chia tiền của các thành viên. Nếu hợp lệ, app gọi:
    `expenseViewModel.createExpense(eventId, body);`
* **Bước 2: Gọi API lưu thông tin chi tiêu (Giai đoạn A):**
  * `ExpenseRepository.java` nhận lệnh và gọi API tạo hóa đơn chi tiêu lên Server:
    * **Định nghĩa trong `ApiService.java`:**
      ```java
      @POST("events/{event_id}/expenses/")
      Call<ExpensePublic> createExpense(@Path("event_id") String eventId, @Body ExpenseCreate body);
      ```
    * **HTTP Endpoint:** `POST http://e1.chiasegpu.vn:34093/api/v1/events/{event_id}/expenses/`
* **Bước 3: Nhận kết quả và kích hoạt tải ảnh (Giai đoạn B):**
  * Khi Server tạo giao dịch thành công, nó trả về thông tin chi tiêu chứa ID (`expense.id`).
  * `AddExpenseActivity` lắng nghe thấy (`expenseViewModel.createdExpense.observe`), lập tức kích hoạt hàm **`uploadReceiptImage(expense.id)`**.
  * Hàm này lấy file ảnh hóa đơn (`photoFile`), đóng gói thành định dạng nhị phân **`MultipartBody.Part`** và trực tiếp gọi API tải ảnh lên Server.
  * **Định nghĩa trong `ApiService.java`:**
    ```java
    @Multipart
    @POST("events/{event_id}/expenses/{expense_id}/image")
    Call<ExpensePublic> uploadExpenseImage(
        @Path("event_id") String eventId,
        @Path("expense_id") String expenseId,
        @Part MultipartBody.Part file
    );
    ```
  * **HTTP Endpoint:** `POST http://e1.chiasegpu.vn:34093/api/v1/events/{event_id}/expenses/{expense_id}/image`
  * **Hoàn tất:** Khi Server lưu ảnh thành công, màn hình sẽ tắt ProgressBar, hiển thị thông báo "Thêm chi tiêu thành công!" và đóng Activity quay lại chi tiết nhóm.

---

### 🧮 Luồng 4: Trả Nợ Bằng Mã QR VietQR Động (Từ Màn Hình -> Xuống Hẳn API)
*Luồng đi từ lúc nhấn nút lấy mã QR trên màn hình nợ đến khi gọi API sinh ảnh QR ngân hàng tự động.*

```text
 [1. MÀN HÌNH: GroupDetailActivity.java] 
       │ 
       ▼ (Người dùng vào Tab "Đơn giản hóa" -> Nhấn nút "Mã QR" trên thẻ nợ)
       ▼ (Truyền tham số: user_id, amount qua Intent mở PaymentQrActivity)
       │
 [2. MÀN HÌNH: PaymentQrActivity.java]
       │
       ▼ (Chạy hàm loadQr() khi vừa hiển thị màn hình)
       ▼ (Gọi API: api.getPaymentQr(userId, amount, description).enqueue(...))
       │
 [3. ĐƯỜNG MẠNG: ApiService.java]
       │
       ▼ (Dịch Annotation @GET("users/{user_id}/payment-qr") thành HTTP GET gửi đi)
       │
 [4. SERVER BACKEND FASTAPI] (http://e1.chiasegpu.vn:34093/api/v1/users/{user_id}/payment-qr)
       │ (Server tự lấy stk ngân hàng của người nhận, gọi API VietQR.io sinh mã QR)
       ▼
 [5. MÀN HÌNH: PaymentQrActivity.java] (Nhận URL ảnh QR, Glide tải ảnh lên màn hình)
```

#### Chi tiết các bước thực hiện trong Code:
* **Bước 1: Bắt đầu tại Thẻ nợ (`GroupDetailActivity.java`):**
  * Khi người dùng nhấn nút "Mã QR" trên thẻ nợ ở Tab *"Đơn giản hóa"*, app lấy thông tin người cần trả (`toUserId`) và số tiền cần trả (`amount`), đóng gói vào `Intent` rồi khởi chạy `PaymentQrActivity`.
* **Bước 2: Yêu cầu lấy mã QR (`PaymentQrActivity.java`):**
  * Trong hàm `onCreate()` của `PaymentQrActivity.java`, app lập tức gọi hàm `loadQr()`.
  * Hàm này sử dụng `RetrofitClient.getApiService().getPaymentQr(...)` để gửi request lên Server:
    ```java
    api.getPaymentQr(userId, amount, "Thanh toan " + eventName).enqueue(new Callback<ResponseBody>() { ... });
    ```
* **Bước 3: Định nghĩa mạng (`ApiService.java`):**
  * Interface `ApiService.java` định nghĩa cấu trúc:
    ```java
    @GET("users/{user_id}/payment-qr")
    Call<ResponseBody> getPaymentQr(
        @Path("user_id") String userId,
        @Query("amount") long amount,
        @Query("description") String description
    );
    ```
  * **HTTP Endpoint:** `GET http://e1.chiasegpu.vn:34093/api/v1/users/{user_id}/payment-qr?amount=...&description=...`
* **Bước 4: Đồng bộ hiển thị QR:**
  * Server FastAPI tiếp nhận yêu cầu, truy xuất Database lấy thông tin số tài khoản, mã ngân hàng của thụ hưởng rồi gọi API của bên thứ ba **VietQR.io** để tạo mã QR động.
  * Server trả về chuỗi URL ảnh QR ngân hàng.
  * `PaymentQrActivity` nhận được URL, sử dụng thư viện **Glide** để tải hình ảnh mã QR động này hiển thị lên giao diện điện thoại. Người dùng chỉ cần mở app Ngân hàng trên máy quét ảnh QR này để chuyển khoản tự động tức thì.

---

## 📝 PHẦN 4: BÀI TẬP TỰ KIỂM TRA PHẢN XẠ (SELF-TEST)
*Hãy thử tự điền các từ khóa vào dấu ba chấm để kiểm tra mức độ nắm vững hệ thống luồng nhé!*

1. Luồng Deep Link mở màn hình tham gia nhóm tự động lấy mã code thông qua cấu hình trong file `.....................` và phân tích dữ liệu ở lớp `JoinEventActivity`.
2. Hàm xử lý chia đều tiền hóa đơn gánh phần lẻ chia không hết nằm ở file `.....................` thông qua phương thức `recalculateSplits()`.
3. Để tạo Dialog hiển thị ảnh hóa đơn toàn màn hình không dùng file layout XML, ứng dụng khởi dựng cấu trúc View bằng mã code Java thông qua lớp layout `.....................` của hệ thống.
4. Khi nhận được thông báo FCM, app sẽ tự động phân tích và chuyển hướng người dùng trực tiếp vào màn hình chi tiết nhóm nhờ lấy dữ liệu trường `.....................` trong data payload.
5. Tiện ích xử lý dịch thông báo lỗi kỹ thuật tiếng Anh từ FastAPI Server sang tiếng Việt thân thiện nằm ở lớp `.....................`.

*(Đáp án gợi ý: 1. AndroidManifest.xml | 2. AddExpenseActivity.java | 3. FrameLayout / Dialog | 4. event_id | 5. ErrorUtils)*

---

## 🗺️ PHẦN 5: BÀI BÁO CÁO CÁC FILE VÀ HÀM CỐT LÕI

| Tên File | Vai trò | Vị trí thư mục | Hàm quan trọng cần nhớ |
| :--- | :--- | :--- | :--- |
| **`SplashActivity.java`** | Màn hình khởi động, tự động đăng nhập | `app/src/main/java/com/example/btck/activities/` | `onCreate()`, `tokenManager.isLoggedIn()` |
| **`LoginActivity.java`** | Màn hình đăng nhập, kiểm tra định dạng nhập | `app/src/main/java/com/example/btck/activities/` | `observeViewModel()`, `setupClickListeners()` |
| **`AuthViewModel.java`** | ViewModel trung gian xử lý logic xác thực | `app/src/main/java/com/example/btck/viewmodel/` | `login()`, `saveTokens()`, `logout()` |
| **`AuthRepository.java`** | Repository xử lý gọi mạng lấy Token & User | `app/src/main/java/com/example/btck/repository/` | `login()`, `getCurrentUser()` |
| **`ApiService.java`** | Định nghĩa các URL Endpoint kết nối với Backend | `app/src/main/java/com/example/btck/api/` | `@POST("auth/login") Call<TokenResponse> login()` |
| **`RetrofitClient.java`** | Singleton cấu hình Retrofit, OkHttpClient, Base URL | `app/src/main/java/com/example/btck/api/` | `getRetrofitInstance()`, `getApiService()` |
| **`TokenManager.java`** | Quản lý lưu trữ Token và User vào SharedPreferences | `app/src/main/java/com/example/btck/managers/` | `saveTokens()`, `getAccessToken()`, `isLoggedIn()` |
| **`ErrorUtils.java`** | Dịch lỗi kỹ thuật tiếng Anh sang tiếng Việt thân thiện | `app/src/main/java/com/example/btck/utils/` | `parseError(Response<?> response)` |
| **`AddExpenseActivity.java`** | Thêm chi tiêu, tải ảnh hóa đơn (tắt OCR) | `app/src/main/java/com/example/btck/activities/` | `uploadReceiptImage()`, `saveExpense()` |
| **`ExpenseDetailActivity.java`** | Chi tiết chi tiêu, xem full ảnh hóa đơn | `app/src/main/java/com/example/btck/activities/` | `displayExpense()`, `showFullImage()` |
| **`PaymentQrActivity.java`** | Tạo & hiển thị VietQR động để trả nợ | `app/src/main/java/com/example/btck/activities/` | `loadQr()`, `saveQrToGallery()` |
| **`MyFirebaseMessagingService.java`**| Nhận thông điệp đẩy FCM, hiển thị notification | `app/src/main/java/com/example/btck/services/` | `onNewToken()`, `onMessageReceived()` |

---
*Tài liệu hướng dẫn trực quan phục vụ báo cáo đồ án, tự nghiên cứu cấu trúc mã nguồn SplitMate.*
