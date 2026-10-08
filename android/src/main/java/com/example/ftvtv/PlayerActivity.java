package com.example.ftvtv;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

public class PlayerActivity extends Activity {

    private VideoView videoView;
    private String videoUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ফুলস্ক্রিন ভিউ
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        videoView = new VideoView(this);
        setContentView(videoView);

        if (getIntent() != null && getIntent().hasExtra("video_url")) {
            videoUrl = getIntent().getStringExtra("video_url");
        }

        if (videoUrl != null && !videoUrl.isEmpty()) {
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);
            videoView.setVideoURI(Uri.parse(videoUrl));
            videoView.requestFocus();
            videoView.start();
        }
    }

    // টিভির রিমোট কন্ট্রোল
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (videoView != null) {
            int currentPos = videoView.getCurrentPosition();
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_RIGHT: // ১০ সেকেন্ড সামনে
                    videoView.seekTo(currentPos + 10000);
                    return true;
                case KeyEvent.KEYCODE_DPAD_LEFT: // ১০ সেকেন্ড পিছনে
                    videoView.seekTo(Math.max(0, currentPos - 10000));
                    return true;
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE: // প্লে / পজ
                    if (videoView.isPlaying()) {
                        videoView.pause();
                    } else {
                        videoView.start();
                    }
                    return true;
                case KeyEvent.KEYCODE_BACK: // প্লেয়ার বন্ধ করা
                    finish();
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
