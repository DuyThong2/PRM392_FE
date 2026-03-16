package com.example.prm392fe.views;

import android.content.ClipboardManager;
import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;

public class OtpEditText extends AppCompatEditText {

    public OtpEditText(Context context) {
        super(context);
    }

    public OtpEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OtpEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        // Intercept paste action
        if (id == android.R.id.paste) {
            try {
                ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                if (cm != null && cm.getPrimaryClip() != null && cm.getPrimaryClip().getItemCount() > 0) {
                    CharSequence pasted = cm.getPrimaryClip().getItemAt(0).coerceToText(getContext());
                    if (pasted != null) {
                        String raw = pasted.toString();
                        // If host activity implements PasteReceiver, forward the raw text
                        if (getContext() instanceof com.example.prm392fe.activities.ForgotPasswordOtpActivity) {
                            ((com.example.prm392fe.activities.ForgotPasswordOtpActivity) getContext()).onOtpPaste(raw);
                            return true; // we handled the paste
                        }
                    }
                }
            } catch (Exception e) {
                // fallback to default
            }
        }
        return super.onTextContextMenuItem(id);
    }
}

