package com.example.btck.utils;

import retrofit2.Response;

public class ErrorUtils {
    public static String parseError(Response<?> response) {
        if (response == null) return "Không kết nối được máy chủ";
        
        int code = response.code();
        if (code == 429 || code >= 500) {
            return "Không kết nối được máy chủ";
        }
        
        try {
            String body = response.errorBody() != null ? response.errorBody().string() : "";
            if (body.contains("\"detail\":\"")) {
                int s = body.indexOf("\"detail\":\"") + 10;
                int e = body.indexOf("\"", s);
                if (e > s) {
                    String detail = body.substring(s, e);
                    // Translate common errors to Vietnamese
                    if ("Invalid or expired invite code".equals(detail)) {
                        return "Mã mời không hợp lệ hoặc đã hết hạn";
                    }
                    if ("Already a member of this event".equals(detail)) {
                        return "Bạn đã là thành viên của nhóm này rồi";
                    }
                    if ("Event not found".equals(detail)) {
                        return "Không tìm thấy nhóm";
                    }
                    return detail;
                }
            }
            if (code == 400) return "Email hoặc mật khẩu không đúng";
            if (code == 401) return "Phiên đăng nhập hết hạn";
            if (code == 404) return "Không tìm thấy dữ liệu";
            if (code == 422) return "Dữ liệu không hợp lệ";
            return "Lỗi: " + code;
        } catch (Exception e) {
            return "Lỗi không xác định";
        }
    }
}
