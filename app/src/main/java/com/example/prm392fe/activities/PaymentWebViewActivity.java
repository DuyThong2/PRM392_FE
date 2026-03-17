package com.example.prm392fe.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392fe.R;


public class PaymentWebViewActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_web_view);

        webView = findViewById(R.id.wvPayment);

        // Bật JS và DOM storage
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);

        // Lấy URL thanh toán từ intent
        String paymentUrl = getIntent().getStringExtra("paymentUrl");
        if (paymentUrl == null) {
            Toast.makeText(this, "URL thanh toán không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set WebViewClient để handle deep link
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("myapp://payment-success")) {
                    Intent intent = new Intent(PaymentWebViewActivity.this, PaymentSuccessActivity.class);
                    Uri uri = Uri.parse(url);
                    intent.putExtra("orderId", uri.getQueryParameter("orderId"));
                    startActivity(intent);
                    finish();
                    return true;
                } else if (url.startsWith("myapp://payment-failed")) {
                    Intent intent = new Intent(PaymentWebViewActivity.this, PaymentFailedActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false; // load URL bình thường
            }
        });

        // Load URL thanh toán
        webView.loadUrl(paymentUrl);
    }
}