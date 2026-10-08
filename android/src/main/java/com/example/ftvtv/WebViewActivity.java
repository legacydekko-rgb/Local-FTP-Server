package com.example.ftvtv;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class WebViewActivity extends Activity {

    public static final String EXTRA_SERVER_URL = "extra_server_url";
    public static final String EXTRA_SERVER_NAME = "extra_server_name";

    private WebView webView;
    private ProgressBar progressBar;
    private String targetUrl = "http://172.16.50.14/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        webView = findViewById(R.id.web_view);
        int progressId = getResources().getIdentifier("web_progress", "id", getPackageName());
        if (progressId != 0) {
            progressBar = findViewById(progressId);
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_SERVER_URL)) {
            targetUrl = intent.getStringExtra(EXTRA_SERVER_URL);
        }

        // WebView Settings for Android TV & Remote Navigation
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);

        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleVideoOrNavigation(request.getUrl().toString());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleVideoOrNavigation(url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

                // টিভিতে রিমোটের ফোকাস পরিষ্কার বোঝার জন্য লাল রঙের আউটলাইন (Red Focus Border)
                webView.evaluateJavascript(
                    "javascript:(function() {" +
                    "   var style = document.createElement('style');" +
                    "   style.innerHTML = 'a:focus, button:focus, [tabindex]:focus { outline: 4px solid #FF0000 !important; background-color: rgba(255,0,0,0.2) !important; }';" +
                    "   document.head.appendChild(style);" +
                    "})()", null);
            }
        });

        webView.loadUrl(targetUrl);
    }

    private boolean handleVideoOrNavigation(String url) {
        String lower = url.toLowerCase();
        // ভিডিও ফরম্যাট পেলেই অ্যাপের নিজের প্লেয়ার চালূ হবে
        if (lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".avi") || 
            lower.endsWith(".m3u8") || lower.endsWith(".webm") || lower.endsWith(".ts")) {
            
            Intent intent = new Intent(this, PlayerActivity.class);
            intent.putExtra("video_url", url);
            startActivity(intent);
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack(); // রিমোটের ব্যাকে দিলে ফোল্ডারের পেছনে যাবে
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
