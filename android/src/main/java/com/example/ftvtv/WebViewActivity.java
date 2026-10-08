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
import android.widget.Toast;

public class WebViewActivity extends Activity {

    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_SERVER_NAME = "extra_server_name";
    public static final String EXTRA_SERVER_URL = "extra_server_url";
    public static final String EXTRA_ACCENT = "extra_accent";

    private WebView webView;
    private ProgressBar progressBar;
    private String targetUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        webView = findViewById(R.id.web_view);
        
        // ID অমিল রোধ করার জন্য সেফ আইডি চেকিং
        int progressId = getResources().getIdentifier("web_progress", "id", getPackageName());
        if (progressId == 0) {
            progressId = getResources().getIdentifier("progress_bar", "id", getPackageName());
        }
        if (progressId != 0) {
            progressBar = findViewById(progressId);
        }

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(EXTRA_URL)) {
                targetUrl = intent.getStringExtra(EXTRA_URL);
            } else if (intent.hasExtra(EXTRA_SERVER_URL)) {
                targetUrl = intent.getStringExtra(EXTRA_SERVER_URL);
            }

            String title = intent.hasExtra(EXTRA_TITLE) ? intent.getStringExtra(EXTRA_TITLE) : intent.getStringExtra(EXTRA_SERVER_NAME);
            if (title != null && getActionBar() != null) {
                getActionBar().setTitle(title);
            }
        }

        if (targetUrl == null || targetUrl.isEmpty()) {
            Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                return handleUrl(url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrl(url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        webView.loadUrl(targetUrl);
    }

    private boolean handleUrl(String url) {
        String lower = url.toLowerCase();
        if (lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".m3u8") || 
            lower.endsWith(".avi") || lower.endsWith(".webm") || lower.endsWith(".ts")) {
            
            Intent intent = new Intent(this, PlayerActivity.class);
            intent.putExtra(PlayerActivity.EXTRA_VIDEO_URL, url);
            startActivity(intent);
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
