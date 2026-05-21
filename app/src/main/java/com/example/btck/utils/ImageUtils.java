package com.example.btck.utils;

import android.net.Uri;
import android.util.Log;

public class ImageUtils {
    /**
     * Rewrites the URL to use the correct MinIO port for fetching images.
     * Replaces the host and port of the original URL with e1.chiasegpu.vn:22224.
     */
    public static String getFullImageUrl(String originalUrl) {
        if (originalUrl == null || originalUrl.trim().isEmpty()) {
            return originalUrl;
        }

        // Do not rewrite external URLs (like Google avatars or VietQR)
        if (originalUrl.contains("googleusercontent.com") || 
            originalUrl.contains("vietqr.io") || 
            originalUrl.contains("facebook.com")) {
            return originalUrl;
        }

        // If the URL already contains e1.chiasegpu.vn:22224, just return it
        if (originalUrl.contains("e1.chiasegpu.vn:22224")) {
            return originalUrl;
        }

        try {
            Uri uri = Uri.parse(originalUrl);
            String path = uri.getPath();
            
            // Reconstruct the URL with the correct MinIO port
            return "http://e1.chiasegpu.vn:22224" + (path != null && path.startsWith("/") ? path : "/" + path);
        } catch (Exception e) {
            Log.e("ImageUtils", "Error parsing image url: " + originalUrl, e);
            return originalUrl;
        }
    }
}
