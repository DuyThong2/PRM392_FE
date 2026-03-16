package com.example.prm392fe.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

public class MyUtils {

    public static final String USER_TYPE_GOOGLE = "Google";
    public static final String USER_TYPE_EMAIL = "Email";
    public static final String USER_TYPE_PHONE = "Phone";

    public static void toast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static long timestamp() {
        return System.currentTimeMillis();
    }


    public JSONObject decodeJwtPayload(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                String payloadJson = new String(Base64.decode(parts[1], Base64.URL_SAFE));
                return new JSONObject(payloadJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Drawable getScaledVectorDrawable(Context context, @DrawableRes int drawableResId, int sizeDp) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableResId);
        if (drawable == null) return null;

        // Chuyển DP → pixel
        float scale = context.getResources().getDisplayMetrics().density;
        int sizePx = (int) (sizeDp * scale + 0.5f);

        // Vẽ lại vector lên bitmap với kích thước mới
        Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, sizePx, sizePx);
        drawable.draw(canvas);

        return new BitmapDrawable(context.getResources(), bitmap);
    }
}
