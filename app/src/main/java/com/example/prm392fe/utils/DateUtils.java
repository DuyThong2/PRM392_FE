package com.example.prm392fe.utils;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    static final String TAG = "DATE_UTILS_TAG";

    // Định dạng cho giờ và phút
    private static final String TIME_FORMAT = "HH:mm";

    // Định dạng cho ngày và tháng (ví dụ: 15 thg 11)
    private static final String DATE_MONTH_FORMAT = "dd MMM";

    // Tên các ngày trong tuần (Locale.getDefault() sẽ chọn ngôn ngữ của máy)
    private static final String DAY_OF_WEEK_FORMAT = "EEE"; // Th2, Th3, Mon, Tue, ...

    /**
     * Format timestamp dựa trên quy tắc:
     * 1. Trong ngày hôm nay: HH:mm
     * 2. Trong 7 ngày gần nhất (không phải hôm nay): Tên ngày (Th2, T3, ...)
     * 3. Hơn 7 ngày: dd thg MM (Ngày tháng)
     *
     * @param date Đối tượng Date cần format
     * @return Chuỗi thời gian đã format
     */
    public static String formatChatTimestamp(Date date) {

        if (date == null) {
            Log.e(TAG, "formatChatTimestamp: Date nulllllll");
            return "";
        }

        // Khởi tạo Calendar cho thời điểm hiện tại và thời điểm của tin nhắn
        Calendar now = Calendar.getInstance();
        Calendar messageTime = Calendar.getInstance();
        messageTime.setTime(date);

        // --- 1. KIỂM TRA TRONG NGÀY HÔM NAY ---
        if (isSameDay(now, messageTime)) {
            // Trường hợp 1: Còn trong ngày -> HH:mm
            SimpleDateFormat formatter = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
            return formatter.format(date);
        }

        // --- 2. KIỂM TRA TRONG TUẦN NÀY (hoặc 7 ngày gần nhất) ---
        // Tính toán sự khác biệt về số ngày
        long diffMillis = now.getTimeInMillis() - messageTime.getTimeInMillis();
        long diffDays = diffMillis / (24 * 60 * 60 * 1000);

        if (diffDays < 7) {
            // Trường hợp 2: Trong 7 ngày (nhưng không phải hôm nay) -> Tên ngày
            SimpleDateFormat formatter = new SimpleDateFormat(DAY_OF_WEEK_FORMAT, Locale.getDefault());
            return formatter.format(date);
        }

        // --- 3. HƠN 7 NGÀY ---
        // Trường hợp 3: Hơn 7 ngày -> dd thg MM
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_MONTH_FORMAT, Locale.getDefault());
        return formatter.format(date);
    }

    /**
     * Hàm kiểm tra xem hai mốc Calendar có cùng ngày (bỏ qua giờ, phút, giây)
     */
    private static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}
