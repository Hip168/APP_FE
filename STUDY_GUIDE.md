# HƯỚNG DẪN HỌC NHANH DỰ ÁN (STUDY GUIDE) 🚀
*Dành cho báo cáo cuối kỳ - Nắm trọn dự án chỉ trong 80 phút*

Chào bạn! File này được thiết kế theo phương pháp **học theo luồng (Use-case)** thay vì đọc từng file code khô khan. Nó giúp bạn hiểu bản chất dự án cực nhanh, tự tin trả lời mọi câu hỏi của giảng viên khi bảo vệ đồ án.

---

## 📅 LỊCH TRÌNH HỌC 80 PHÚT (BÁO CÁO THỬ)

| Thời gian | Nội dung học | Mục tiêu cần đạt |
| :--- | :--- | :--- |
| **15 phút đầu** | **Big Picture & Luồng đi dữ liệu** | Vẽ lại được sơ đồ MVVM và hiểu cách màn hình gọi API. |
| **35 phút tiếp** | **3 Use-case cốt lõi** | Nhắm mắt lại vẫn hình dung được luồng Đăng nhập, Thêm giao dịch và Quét QR Trả nợ. |
| **20 phút tiếp** | **Phản xạ câu hỏi phản biện** | Học thuộc lòng 5 câu hỏi "kinh điển" của Thầy/Cô. |
| **10 phút cuối** | **Tự kiểm tra (Self-test)** | Tự điền vào sơ đồ trống ở cuối file để kiểm tra trí nhớ. |

---

## 🗺️ PHẦN 1: BỨC TRANH TOÀN CẢNH (BIG PICTURE)

### 1. Sơ đồ điều hướng các màn hình (Screen Flow)
```text
[ SplashActivity ] (Màn hình chào)
       │ (Kiểm tra Token)
       ├─► Đã login ──► [ MainActivity ] (Trang chủ / Danh sách giao dịch)
       │                        │
       │                        ├─► [ AddExpenseActivity ] (Thêm chi tiêu)
       │                        ├─► [ GroupDetailActivity ] (Nhóm / QR trả nợ)
       │                        └─► [ ProfileFragment ] (Cá nhân)
       │
       └─► Chưa login ─► [ LoginActivity ] (Đăng nhập)
                                │
                                └─► [ RegisterActivity ] (Đăng ký)
```

**Chi tiết luồng gọi và xử lý file cụ thể:**
* **`SplashActivity` ──► `MainActivity` / `LoginActivity`**:
  * *File xử lý:* [SplashActivity.java](file:///Users/leminhdao168/Documents/học tập/code/mobile/bài tập cuối kì/BTCK-APP-FE/app/src/main/java/com/example/btck/activities/SplashActivity.java)
  * *Cách gọi:* Sử dụng lớp `TokenManager` kiểm tra trạng thái lưu trữ access_token trong `SharedPreferences`. Nếu `tokenManager.isLoggedIn()` bằng `true`, app gọi `startActivity(new Intent(this, MainActivity.class))`. Ngược lại sẽ điều hướng đến `LoginActivity` bằng `Intent`.
* **`LoginActivity` ──► `RegisterActivity`**:
  * *File xử lý:* [LoginActivity.java](file:///Users/leminhdao168/Documents/học tập/code/mobile/bài tập cuối kì/BTCK-APP-FE/app/src/main/java/com/example/btck/activities/LoginActivity.java)
  * *Cách gọi:* Khởi tạo màn hình Đăng ký thông qua `registerLauncher.launch(new Intent(this, RegisterActivity.class))`. Sử dụng `ActivityResultLauncher` hiện đại để sau khi đăng ký thành công ở [RegisterActivity.java](file:///Users/leminhdao168/Documents/học tập/code/mobile/bài tập cuối kì/BTCK-APP-FE/app/src/main/java/com/example/btck/activities/RegisterActivity.java), email và mật khẩu sẽ tự động trả về để điền vào form đăng nhập, tăng tối đa trải nghiệm người dùng (UX).
* **`MainActivity` ──► `ProfileFragment`**:
  * *File xử lý:* [MainActivity.java](file:///Users/leminhdao168/Documents/học tập/code/mobile/bài tập cuối kì/BTCK-APP-FE/app/src/main/java/com/example/btck/activities/MainActivity.java)
  * *Cách gọi:* `MainActivity` chứa thanh điều hướng `BottomNavigationView`. Khi nhấn tab cá nhân, ứng dụng gọi `getSupportFragmentManager().beginTransaction().replace(...)` để nạp [ProfileFragment.java](file:///Users/leminhdao168/Documents/học tập/code/mobile/bài tập cuối kì/BTCK-APP-FE/app/src/main/java/com/example/btck/fragments/ProfileFragment.java) vào container giao diện.
* **`MainActivity` / `GroupDetailActivity` ──► `AddExpenseActivity`**:
  * *File xử lý:* [GroupDetailActivity.java](file:///Users/leminhdao168/Documents/học tập/code/mobile/bài tập cuối kì/BTCK-APP-FE/app/src/main/java/com/example/btck/activities/GroupDetailActivity.java) hoặc [AddExpenseActivity.java](file:///Users/leminhdao168/Documents/học tập/code/mobile/bài tập cuối kì/BTCK-APP-FE/app/src/main/java/com/example/btck/activities/AddExpenseActivity.java)
  * *Cách gọi:* Gọi qua `startActivity(intent)` kèm theo các Extras chứa `event_id` và `event_name` để form thêm chi tiêu biết giao dịch này thuộc về nhóm nào và hiển thị tên nhóm.
* **`MainActivity` (Tab Home / Groups) ──► `GroupDetailActivity`**:
  * *File xử lý:* [HomeFragment.java](file:///Users/leminhdao168/Documents/học tập/code/mobile/bài tập cuối kì/BTCK-APP-FE/app/src/main/java/com/example/btck/fragments/HomeFragment.java) hoặc [GroupsFragment.java](file:///Users/leminhdao168/Documents/học tập/code/mobile/bài tập cuối kì/BTCK-APP-FE/app/src/main/fragments/GroupsFragment.java)
  * *Cách gọi:* Lắng nghe sự kiện click trên các item của RecyclerView trong Adapter, sau đó khởi chạy `GroupDetailActivity` bằng `Intent` truyền kèm `event_id`.

### 2. Bản đồ 4 tầng kiến trúc (MVVM Architecture)
Khi có một hành động (ví dụ: nhấn nút "Đăng nhập"):
```text
[ UI / VIEW ] ──(1. Gọi hàm)──► [ VIEWMODEL ] ──(2. Gọi hàm)──► [ REPOSITORY ]
     ▲                                                                │ (3. Gọi API)
     │                                                                ▼
     └──────────(5. Tự động cập nhật qua LiveData)─────────── [ RETROFIT / API ]
```

**Bảng tóm tắt nhiệm vụ từng tầng:**
| Tầng | Vai trò | Ví dụ trong code |
| :--- | :--- | :--- |
| **1. View (Activity/Fragment)** | Hiển thị giao diện, bắt sự kiện Click, hiển thị Toast thông báo. | `LoginActivity.java`, `fragment_profile.xml` |
| **2. ViewModel** | Xử lý logic nghiệp vụ, giữ dữ liệu bằng `LiveData` để View quan sát. | `AuthViewModel.java`, `ExpenseViewModel.java` |
| **3. Repository** | Trung gian quyết định lấy dữ liệu từ đâu (API Server hay Database). | `UserRepository.java`, `ExpenseRepository.java` |
| **4. Retrofit Client** | Định nghĩa các HTTP Request (`GET`, `POST`) để kết nối với Backend. | `ApiService.java`, `RetrofitClient.java` |

---

## 🔄 PHẦN 2: 3 USE-CASE CỐT LÕI (HỌC THEO LUỒNG)

### 🔑 Use-case 1: Đăng nhập hệ thống (Authentication)
*Luồng đi của dữ liệu từ khi nhập mật khẩu đến khi lưu Token:*

```text
[LoginActivity] ──(1. Nhập username, password & Click)──► [AuthViewModel]
                                                                  │
                                                          (2. Gọi login())
                                                                  ▼
[TokenManager] ◄──(5. Lưu Access Token vô SharedPref)── [AuthRepository]
      │                                                           │
 (Lưu trữ)                                                (3. Gọi API POST)
      │                                                           ▼
      └──────────────────────────────────────────────────► [Backend FastAPI]
```

* **Điểm mấu chốt:** Sau khi đăng nhập thành công, Server trả về một chuỗi **JWT Token**. Token này được lưu trữ bằng `TokenManager` (sử dụng `SharedPreferences` - bộ nhớ tạm của Android) để tự động đăng nhập những lần sau và gắn vào Header của các API khác thông qua `AuthInterceptor`.

---

### 💵 Use-case 2: Tạo giao dịch chi tiêu mới (Add Expense)
*Làm sao để thêm một khoản chi tiêu vào nhóm và tải lên/hiển thị ảnh hóa đơn?*

```text
[AddExpenseActivity] ──(1. Nhập Số tiền, Mô tả)──► [ExpenseViewModel]
         │                                               │
   (Chụp/Up ảnh)                                   (2. Gọi createExpense())
         │                                               ▼
   (Lưu photoUri) ◄──(4. Nhận expense_id thành công)─── [ExpenseRepository]
         │                                               │
   (5. uploadReceiptImage())                             (3. Gọi API POST)
         ▼                                               ▼
[Backend FastAPI] ◄───────────────────────────────── [Backend FastAPI]
```

* **Cơ chế tải và phóng to ảnh hóa đơn:** 
  1. **Tải ảnh hóa đơn lên (Upload Receipt):** Trong file [AddExpenseActivity.java](file:///Users/leminhdao168/Documents/học tập/code/mobile/bài tập cuối kì/BTCK-APP-FE/app/src/main/java/com/example/btck/activities/AddExpenseActivity.java), sau khi gọi API tạo chi tiêu thành công và nhận được ID giao dịch từ server, ứng dụng sẽ thực hiện gọi hàm `uploadReceiptImage()` để upload file ảnh hóa đơn dạng **MultipartBody** lên server qua endpoint của `ApiService.java`.
  2. **Xem ảnh hóa đơn toàn màn hình (Fullscreen Receipt Preview):** Ở màn hình xem chi tiết giao dịch [ExpenseDetailActivity.java](file:///Users/leminhdao168/Documents/học tập/code/mobile/bài tập cuối kì/BTCK-APP-FE/app/src/main/java/com/example/btck/activities/ExpenseDetailActivity.java), khi nhấn vào ảnh hóa đơn (`ivReceipt`), hàm `showFullImage(String imageUrl)` sẽ tạo một Dialog nền đen toàn màn hình (`Theme_Black_NoTitleBar_Fullscreen`) và dùng Glide tải ảnh chất lượng gốc sắc nét. Người dùng chạm lại vào màn hình hoặc ấn nút (X) để đóng ảnh.

---

### 📲 Use-case 3: Trả nợ bằng mã QR VietQR (Settlement & QR)
*Luồng sinh mã QR động để chuyển khoản ngân hàng trả nợ nhanh giữa các thành viên:*

```text
[GroupDetailActivity] (Chọn "Mã QR") ──► [PaymentQrActivity]
                                               │
                                     (1. Gửi request lấy QR)
                                               ▼
[Ứng dụng Ngân hàng] ◄─(Mở App/Lưu QR)─ [Retrofit / ApiService]
(Quét VietQR tự điền)                       │ (2. API Trả về URL VietQR.io)
                                            ▼
                                     [Backend FastAPI]
```

* **Cách hoạt động:** 
  1. Khi người dùng nhấn nút "Mã QR" trên thẻ nợ của Tab *"Đơn giản hóa"*, app sẽ điều hướng sang màn hình [PaymentQrActivity.java](file:///Users/leminhdao168/Documents/học tập/code/mobile/bài tập cuối kì/BTCK-APP-FE/app/src/main/java/com/example/btck/activities/PaymentQrActivity.java).
  2. App gửi yêu cầu lấy QR qua API `getPaymentQr` của `ApiService.java`.
  3. Server FastAPI gọi API của dịch vụ **VietQR.io** để sinh mã QR động chứa sẵn thông tin tài khoản ngân hàng của người nhận, số tiền, và nội dung chuyển khoản.
  4. App tải ảnh QR từ URL trả về bằng **Glide** để hiển thị. Người dùng có thể lưu ảnh QR vào máy (`saveQrToGallery()`) hoặc mở app Ngân hàng để quét chuyển khoản tự động mà không cần gõ tay thông tin ngân hàng.

---

## ❓ PHẦN 3: 5 CÂU HỎI PHẢN BIỆN "KINH ĐIỂN" CỦA GIÁO VIÊN

### 💬 Câu 1: "Tại sao lại chọn kiến trúc MVVM mà không dùng MVC hay MVP?"
* **Trả lời:** 
  > *"Thưa thầy/cô, MVVM giúp **tách biệt hoàn toàn giao diện (View)** và **logic xử lý (ViewModel)**. Điều này giúp code dễ bảo trì, dễ viết Unit Test và tránh rò rỉ bộ nhớ (memory leak). Hơn nữa, MVVM tận dụng cơ chế **LiveData** giúp UI tự động cập nhật khi dữ liệu thay đổi mà không cần gọi lại hàm hiển thị thủ công."*

### 💬 Câu 2: "Ứng dụng gọi API bằng thư viện nào? Cơ chế bảo mật API ra sao?"
* **Trả lời:**
  > *"Dự án sử dụng thư viện **Retrofit 2** kết hợp với **OkHttp** để gọi API dạng RESTful. Để bảo mật, ứng dụng sử dụng cơ chế **JWT (JSON Web Token)**. Mỗi khi đăng nhập thành công, Server cấp 1 Token. Token này được tự động chèn vào Header của mọi request tiếp theo thông qua **AuthInterceptor**."*

### 💬 Câu 3: "Làm thế nào để xử lý Token bị hết hạn (Expired Token)?"
* **Trả lời:**
  > *"Thưa thầy/cô, trong class `RetrofitClient`, em đã cấu hình một **AuthInterceptor**. Nếu API trả về lỗi `401` (Unauthorized - do token hết hạn), app sẽ tự động chuyển hướng người dùng về màn hình `LoginActivity` để yêu cầu đăng nhập lại nhằm đảm bảo an toàn thông tin."*

### 💬 Câu 4: "SharedPreferences dùng để làm gì trong dự án này?"
* **Trả lời:**
  > *"Dạ, `SharedPreferences` được đóng gói trong class `TokenManager` để lưu trữ dữ liệu dung lượng nhỏ dưới dạng Key-Value trong bộ nhớ riêng của thiết bị. Cụ thể ở đây là dùng để lưu **Access Token** và **Thông tin User đăng nhập** để người dùng không phải đăng nhập lại mỗi lần mở app."*

### 💬 Câu 5: "Làm thế nào để chuyển dữ liệu giữa các Activity với nhau?"
* **Trả lời:**
  > *"Em sử dụng đối tượng **Intent** kết hợp với phương thức `putExtra()` để truyền các dữ liệu nguyên thủy (như ID nhóm, Số tiền) hoặc dùng thư viện **Gson** để chuyển đổi Object thành chuỗi JSON trước khi truyền qua Activity khác."*

---

## 📝 PHẦN 4: BÀI TẬP TỰ KIỂM TRA (SELF-TEST)
*Hãy thử điền vào các dấu ba chấm `...` dưới đây để tự hệ thống lại kiến thức nhé!*

1. Khi người dùng nhập sai mật khẩu, lỗi hiển thị trên màn hình được xử lý bởi file tiện ích có tên là `.....................`
2. Để định nghĩa các đường dẫn API như `/auth/login` hay `/transactions`, chúng ta khai báo trong file `.....................`
3. Tầng đóng vai trò làm cầu nối giữa giao diện (View) và tầng dữ liệu (Repository) là `.....................`
4. Để lưu trữ trạng thái đăng nhập lâu dài trên thiết bị, ứng dụng sử dụng `.....................`

*(Đáp án gợi ý: 1. ErrorUtils | 2. ApiService | 3. ViewModel | 4. SharedPreferences / TokenManager)*

---
**💡 Lời khuyên cuối cùng:** Hãy mở ứng dụng lên chạy thử, vừa bấm nút trên giao diện vừa đối chiếu với luồng đi trong file này. Chúc bạn bảo vệ đồ án đạt kết quả xuất sắc nhất! 🎉
