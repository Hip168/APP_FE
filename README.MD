# 📱 BTCK - SplitMate Android App

> Ứng dụng chia tiền nhóm (Share Bill / Split Expense) - Android Native Java + FastAPI Backend

---

## 🗂️ Cấu trúc dự án

```
bài tập cuối kì/
├── BTCK-APP-BE/          # Backend FastAPI (đã có sẵn)
└── BTCK-APP-FE/          # Frontend Android (đang xây dựng)
```

---

## ✅ Danh sách file đã tạo (BTCK-APP-FE)

### 📦 Build & Config
| File | Mô tả | Trạng thái |
|------|-------|-----------|
| `app/build.gradle` | Dependencies: Retrofit, Material3, Glide, Lottie, FCM... | ✅ |
| `build.gradle` | Root build file | ✅ |
| `settings.gradle` | Project settings & repositories | ✅ |
| `gradle/libs.versions.toml` | Version catalog | ✅ |
| `app/src/main/AndroidManifest.xml` | Permissions, Activities, Services | ✅ |

### 🔧 Application Class
| File | Mô tả | Trạng thái |
|------|-------|-----------|
| `BTCKApplication.java` | Application class khởi tạo RetrofitClient singleton | ✅ |

### 🌐 Networking (`api/`)
| File | Mô tả | Trạng thái |
|------|-------|-----------|
| `api/ApiService.java` | Retrofit interface – tất cả endpoints (Auth, Events, Expenses, Settlements, Notifications) | ✅ |
| `api/AuthInterceptor.java` | Gắn JWT Bearer token vào mọi request | ✅ |
| `api/RetrofitClient.java` | Singleton Retrofit client (BASE_URL: `10.0.2.2:8000`) | ✅ |

### 📐 Models (`models/`)
| File | Mô tả | Trạng thái |
|------|-------|-----------|
| `models/TokenResponse.java` | Response đăng nhập (access_token, refresh_token) | ✅ |
| `models/UserPublic.java` | Thông tin user (id, email, full_name, avatar...) | ✅ |
| `models/UserRegister.java` | Request body đăng ký | ✅ |
| `models/UpdateUserMeRequest.java` | Request cập nhật hồ sơ | ✅ |
| `models/UpdatePasswordRequest.java` | Request đổi mật khẩu | ✅ |
| `models/MessageResponse.java` | Generic message response | ✅ |
| `models/FCMTokenRequest.java` | Request gửi FCM token | ✅ |
| `models/EventPublic.java` | Thông tin sự kiện/nhóm | ✅ |
| `models/EventsPublic.java` | List sự kiện (data + count) | ✅ |
| `models/EventCreate.java` | Request tạo nhóm | ✅ |
| `models/EventMemberPublic.java` | Thành viên nhóm | ✅ |
| `models/AddMemberRequest.java` | Request thêm thành viên | ✅ |
| `models/ExpensePublic.java` | Chi tiêu (id, amount, description, splits...) | ✅ |
| `models/ExpensesPublic.java` | List chi tiêu | ✅ |
| `models/ExpenseCreate.java` | Request tạo chi tiêu | ✅ |
| `models/ExpenseUpdate.java` | Request cập nhật chi tiêu | ✅ |
| `models/ExpenseSplitPublic.java` | Phân chia chi tiêu (read) | ✅ |
| `models/ExpenseSplitCreate.java` | Phân chia chi tiêu (write) | ✅ |
| `models/SettlementPublic.java` | Thanh toán đã ghi nhận | ✅ |
| `models/SettlementsPublic.java` | List thanh toán | ✅ |
| `models/SettlementCreate.java` | Request tạo thanh toán | ✅ |
| `models/UserBalance.java` | Số dư của user trong nhóm | ✅ |
| `models/EventBalances.java` | Tổng hợp số dư cả nhóm | ✅ |
| `models/MyBalanceSummary.java` | Tóm tắt số dư cá nhân | ✅ |
| `models/MyBalanceDetail.java` | Chi tiết số dư theo nhóm | ✅ |
| `models/SimplifiedDebt.java` | Khoản nợ đơn giản hóa | ✅ |
| `models/SimplifiedDebtsResponse.java` | List khoản nợ đơn giản | ✅ |
| `models/EventStats.java` | Thống kê nhóm | ✅ |
| `models/NotificationPublic.java` | Thông báo | ✅ |
| `models/NotificationsPublic.java` | List thông báo | ✅ |
| `models/UnreadCountResponse.java` | Số thông báo chưa đọc | ✅ |
| `models/InviteCodePublic.java` | Mã mời nhóm | ✅ |
| `models/InviteCodeCreate.java` | Request tạo mã mời | ✅ |
| `models/RefreshTokenRequest.java` | Request refresh token | ✅ |
| `models/NewPasswordRequest.java` | Request đặt mật khẩu mới | ✅ |

### 🔧 Managers (`managers/`)
| File | Mô tả | Trạng thái |
|------|-------|-----------|
| `managers/TokenManager.java` | Lưu/đọc JWT token, user info qua SharedPreferences | ✅ |

### 🗃️ Repository (`repository/`)
| File | Mô tả | Trạng thái |
|------|-------|-----------|
| `repository/AuthRepository.java` | Login, Register, Password Recovery, Get Current User | ✅ |
| `repository/EventRepository.java` | CRUD nhóm, thành viên, số dư, mã mời | ✅ |
| `repository/ExpenseRepository.java` | CRUD chi tiêu | ✅ |
| `repository/SettlementRepository.java` | Tạo & lấy danh sách thanh toán | ✅ |
| `repository/UserRepository.java` | Cập nhật profile, đổi mật khẩu, tìm kiếm user, FCM – hỗ trợ cả LiveData & Callback | ✅ |

### 🧠 ViewModels (`viewmodel/`)
| File | Mô tả | Trạng thái |
|------|-------|-----------|
| `viewmodel/AuthViewModel.java` | Login/Register/Logout + LiveData | ✅ |
| `viewmodel/EventViewModel.java` | Quản lý nhóm + số dư + thống kê | ✅ |
| `viewmodel/ExpenseViewModel.java` | Quản lý chi tiêu | ✅ |
| `viewmodel/SettlementViewModel.java` | Quản lý thanh toán | ✅ |
| `viewmodel/NotificationViewModel.java` | Load/markRead/markAllRead thông báo | ✅ |

### 📱 Activities (`activities/`)
| File | Mô tả | Trạng thái |
|------|-------|-----------|
| `activities/SplashActivity.java` | Splash screen, kiểm tra đăng nhập | ✅ |
| `activities/LoginActivity.java` | Màn hình đăng nhập | ✅ |
| `activities/RegisterActivity.java` | Màn hình đăng ký | ✅ |
| `activities/ForgotPasswordActivity.java` | Quên mật khẩu | ✅ |
| `activities/MainActivity.java` | Main với BottomNavigationView + FAB | ✅ |
| `activities/GroupDetailActivity.java` | Chi tiết nhóm (3 tab: Chi tiêu / Số dư / Đơn giản) | ✅ |
| `activities/AddExpenseActivity.java` | Thêm chi tiêu + chia tiền | ✅ |
| `activities/ExpenseDetailActivity.java` | Chi tiết & xóa chi tiêu | ✅ |
| `activities/SettlementActivity.java` | Ghi nhận thanh toán + lịch sử | ✅ |
| `activities/ProfileActivity.java` | Hồ sơ cá nhân: chỉnh sửa, đổi mật khẩu, đăng xuất | ✅ |
| `activities/NotificationsActivity.java` | Danh sách thông báo + mark all read | ✅ |
| `activities/JoinEventActivity.java` | Tham gia nhóm bằng mã mời + deep link | ✅ |

### 🧩 Fragments (`fragments/`)
| File | Mô tả | Trạng thái |
|------|-------|-----------|
| `fragments/HomeFragment.java` | Trang chủ: số dư tổng quan + nhóm gần đây | ✅ |
| `fragments/GroupsFragment.java` | Danh sách nhóm + tạo/tham gia nhóm | ✅ |
| `fragments/NotificationsFragment.java` | Danh sách thông báo + đánh dấu đã đọc | ✅ |
| `fragments/ProfileFragment.java` | Hồ sơ: chỉnh sửa, đổi mật khẩu, đăng xuất | ✅ |

### 🔄 Adapters (`adapters/`)
| File | Mô tả | Trạng thái |
|------|-------|-----------|
| `adapters/EventAdapter.java` | RecyclerView adapter cho danh sách nhóm | ✅ |
| `adapters/ExpenseAdapter.java` | RecyclerView adapter cho danh sách chi tiêu | ✅ |
| `adapters/MemberAdapter.java` | Adapter thành viên nhóm (avatar, role badge) | ✅ |
| `adapters/MemberSplitAdapter.java` | Adapter chia tiền cho từng thành viên | ✅ |
| `adapters/NotificationAdapter.java` | Adapter thông báo (read/unread state) | ✅ |
| `adapters/SettlementAdapter.java` | Adapter danh sách thanh toán | ✅ |

### 🔔 FCM & Services
| File | Mô tả | Trạng thái |
|------|-------|-----------|
| `services/MyFirebaseMessagingService.java` | Handle FCM push notifications + điều hướng màn hình | ✅ |

### 🎨 Layouts (`res/layout/`)
| File | Mô tả | Trạng thái |
|------|-------|-----------|
| `activity_splash.xml` | Splash screen (mint green, fade animation) | ✅ |
| `activity_login.xml` | Login form | ✅ |
| `activity_register.xml` | Register form | ✅ |
| `activity_forgot_password.xml` | Forgot password | ✅ |
| `activity_main.xml` | BottomNav + FAB | ✅ |
| `activity_group_detail.xml` | Group detail với stats bar + 3 tabs | ✅ |
| `activity_add_expense.xml` | Add expense với category chips | ✅ |
| `activity_expense_detail.xml` | Expense detail + amount card | ✅ |
| `activity_settlement.xml` | Settlement list + debt summary card + FAB | ✅ |
| `activity_profile.xml` | Profile: avatar header card mint + menu items | ✅ |
| `activity_notifications.xml` | Notifications list + mark all read + swipe refresh | ✅ |
| `activity_join_event.xml` | Tham gia nhóm bằng mã 8 ký tự | ✅ |
| `fragment_home.xml` | Home: balance card + recent groups | ✅ |
| `fragment_groups.xml` | Groups list | ✅ |
| `fragment_notifications.xml` | Notifications list | ✅ |
| `fragment_profile.xml` | Profile: avatar header + menu | ✅ |
| `item_event.xml` | Group/Event card item | ✅ |
| `item_expense.xml` | Expense item | ✅ |
| `item_member.xml` | Member row với avatar, tên, email, role badge | ✅ |
| `item_split_member.xml` | Split member row | ✅ |
| `item_notification.xml` | Notification item | ✅ |
| `item_settlement.xml` | Settlement item (ai trả → ai nhận, số tiền) | ✅ |
| `dialog_create_group.xml` | Dialog tạo nhóm (tên + mô tả) | ✅ |
| `dialog_join_group.xml` | Dialog tham gia nhóm bằng mã mời | ✅ |
| `dialog_edit_profile.xml` | Dialog chỉnh sửa hồ sơ | ✅ |
| `dialog_change_password.xml` | Dialog đổi mật khẩu (3 trường) | ✅ |
| `dialog_settle.xml` | Dialog ghi nhận thanh toán | ✅ |

### 🎨 Drawables (`res/drawable/`)
| File | Mô tả | Trạng thái |
|------|-------|-----------|
| `ic_home.xml` | Icon trang chủ (Material Design) | ✅ |
| `ic_groups.xml` | Icon nhóm (Material Design) | ✅ |
| `ic_notifications.xml` | Icon thông báo (Material Design) | ✅ |
| `ic_person.xml` | Icon người dùng (Material Design) | ✅ |
| `ic_add.xml` | Icon thêm / dấu + (Material Design) | ✅ |
| `bg_mint_circle.xml` | Vòng tròn mint (avatar placeholder) | ✅ |
| `bg_mint_circle_light.xml` | Vòng tròn mint nhạt | ✅ |
| `bg_avatar_circle.xml` | Avatar circle (trắng) | ⏳ |
| `bg_chip.xml` | Chip background | ⏳ |
| `bg_dot_mint.xml` | Unread dot indicator | ⏳ |
| `bg_spinner.xml` | Spinner background | ⏳ |
| `shimmer_placeholder.xml` | Shimmer loading placeholder | ⏳ |

### 🎨 Resources (`res/`)
| File | Mô tả | Trạng thái |
|------|-------|-----------|
| `values/colors.xml` | Palette: mint green + fintech dark | ✅ |
| `values/strings.xml` | Strings tiếng Việt | ✅ |
| `values/themes.xml` | Material 3 theme + styles | ✅ |
| `menu/bottom_nav_menu.xml` | Bottom nav menu | ✅ |
| `menu/group_detail_menu.xml` | Group detail options menu | ⏳ |
| `menu/add_expense_menu.xml` | Add expense save menu | ⏳ |
| `color/bottom_nav_tint.xml` | Bottom nav color selector | ⏳ |
| `font/inter_regular.xml` | Inter Regular font | ⏳ |
| `font/inter_bold.xml` | Inter Bold font | ⏳ |
| `xml/data_extraction_rules.xml` | Android backup rules | ⏳ |
| `xml/backup_rules.xml` | Android backup rules | ⏳ |

---

## 🚀 Hướng dẫn chạy dự án

### 1. Backend (BTCK-APP-BE)
```bash
cd BTCK-APP-BE
docker-compose up -d
# Backend chạy tại http://localhost:8000
```

### 2. Frontend (BTCK-APP-FE)
1. Mở **Android Studio**
2. Open `BTCK-APP-FE/`
3. Đặt `google-services.json` vào `app/`
4. Sync Gradle
5. Chạy trên **Emulator** (BASE_URL = `10.0.2.2:8000`)

> **Lưu ý thiết bị thật:** Đổi BASE_URL trong `RetrofitClient.java` thành IP máy tính (VD: `192.168.1.x:8000`)

---

## 🏗️ Kiến trúc

```
UI Layer         → Activities / Fragments (ViewBinding)
ViewModel Layer  → AndroidViewModel + LiveData
Repository Layer → Retrofit Callbacks / Callback Interfaces
Network Layer    → OkHttp + AuthInterceptor
Local Storage    → SharedPreferences (TokenManager)
Push Notify      → Firebase Cloud Messaging (FCM)
```

---

## 🎨 Design System

| Token | Giá trị |
|-------|---------|
| Primary | `#B8F2E6` (Mint Green) |
| Primary Dark | `#52D9C1` |
| Accent | `#00C9A7` |
| Text Dark | `#1A1A2E` |
| Error | `#FF6B6B` |
| Success | `#00B894` |
| Font | Inter (Regular + Bold) |

---

## 📋 Tiến độ tổng quát

| Phần | Trạng thái |
|------|-----------|
| Models (35 files) | ✅ Hoàn thành |
| API Layer | ✅ Hoàn thành |
| Repository Layer | ✅ Hoàn thành |
| ViewModel Layer | ✅ Hoàn thành |
| Activities (12 screens) | ✅ Hoàn thành |
| Fragments (4 tabs) | ✅ Hoàn thành |
| Adapters (6 adapters) | ✅ Hoàn thành |
| FCM Service | ✅ Hoàn thành |
| XML Layouts (27 files) | ✅ Hoàn thành |
| Drawables (icons + bg) | ✅ Hoàn thành |
| Menu Resources | ✅ Hoàn thành |
| Color/Font Resources | ✅ Hoàn thành |
| XML Backup Rules | ✅ Hoàn thành |

---

*Cập nhật lần cuối: 2026-05-14 18:50*
