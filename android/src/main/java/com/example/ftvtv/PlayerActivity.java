package com.example.ftvtv;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.ui.PlayerView;

/**
 * ============================================================================
 * FILE: app/src/main/java/com/example/ftvtv/PlayerActivity.java
 * ============================================================================
 * THE APP'S OWN INTERNAL VIDEO PLAYER.
 *
 * This is the activity that makes the app "play the movie itself" instead of
 * throwing the link at a browser.
 *
 * Built on ExoPlayer (Media3), not VideoView, because ExoPlayer actually plays
 * what these directory servers serve:
 *   - MKV containers
 *   - H.264 / H.265 (HEVC) video
 *   - HLS (.m3u8) playlists
 *   - proper HTTP seeking, buffering and retry
 *
 * >>> IF A FILE HAS NO SOUND (AC3 / DTS / EAC3 audio tracks):
 *     Those codecs are licensed and are NOT in the open-source ExoPlayer build.
 *     This activity detects the failure and offers a one-press hand-off to
 *     VLC / MX Player, which ship their own licensed decoders. That is the
 *     intended behaviour, not a bug - see showPlaybackError() below.
 *
 * Remote control:
 *   OK / Center    play / pause
 *   LEFT / RIGHT   rewind / fast-forward 10 s
 *   UP / DOWN      show the controls
 *   BACK           exit the player
 * ============================================================================
 */
@UnstableApi
public class PlayerActivity extends Activity {

    public static final String EXTRA_VIDEO_URL = "extra_video_url";
    public static final String EXTRA_TITLE = "extra_title";

    /** How far the LEFT / RIGHT keys seek. */
    private static final long SEEK_MS = 10_000L;

    /** Remembered position so a pause/resume keeps your place. */
    private static final String STATE_POSITION = "state_position";

    private PlayerView playerView;
    private ExoPlayer player;
    private ProgressBar buffering;
    private TextView titleView;
    private View errorView;
    private TextView errorText;

    private String videoUrl = "";
    private String title = "";
    private long resumePosition = 0L;
    private boolean playbackStarted = false;

    private final Handler handler = new Handler(Looper.getMainLooper());

    /** Hides the title overlay a few seconds after playback begins. */
    private final Runnable hideOverlay = new Runnable() {
        @Override
        public void run() {
            if (errorView != null && errorView.getVisibility() == View.VISIBLE) {
                return; // keep the error panel up
            }
            if (titleView != null) {
                titleView.setVisibility(View.GONE);
            }
        }
    };

    /* =====================================================================
     * LIFECYCLE
     * =================================================================== */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Keep the TV awake while a film is playing.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        playerView = (PlayerView) findViewById(R.id.player_view);
        buffering = (ProgressBar) findViewById(R.id.player_buffering);
        titleView = (TextView) findViewById(R.id.player_title);
        errorView = findViewById(R.id.player_error);
        errorText = (TextView) findViewById(R.id.player_error_text);

        Intent intent = getIntent();
        if (intent != null) {
            videoUrl = intent.getStringExtra(EXTRA_VIDEO_URL);
            title = intent.getStringExtra(EXTRA_TITLE);
        }
        if (videoUrl == null || videoUrl.length() == 0) {
            Toast.makeText(this, "No video URL supplied", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (savedInstanceState != null) {
            resumePosition = savedInstanceState.getLong(STATE_POSITION, 0L);
        }

        titleView.setText(title != null && title.length() > 0 ? title : lastSegment(videoUrl));

        findViewById(R.id.player_open_external).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handOffToExternalPlayer();
            }
        });

        buildPlayer();
    }

    /** Creates the ExoPlayer instance and wires every callback. */
    private void buildPlayer() {
        // Track selector: let ExoPlayer pick the best audio/video track.
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);

        // Load control: a larger buffer than default makes seeking inside a big
        // MKV over LAN far smoother.
        DefaultLoadControl loadControl = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                        30_000,   // min buffer
                        90_000,   // max buffer
                        1_500,    // buffer needed to start playback
                        3_000)    // buffer needed after a seek
                .build();

        player = new ExoPlayer.Builder(this)
                .setTrackSelector(trackSelector)
                .setLoadControl(loadControl)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(this))
                .build();

        playerView.setPlayer(player);

        // TV-friendly controller: OK toggles it, arrows seek.
        playerView.setUseController(true);
        playerView.setControllerShowTimeoutMs(3500);
        playerView.setControllerHideOnTouch(false);
        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER); // we draw our own

        player.addListener(new Player.Listener() {

            @Override
            public void onPlaybackStateChanged(int state) {
                switch (state) {
                    case Player.STATE_BUFFERING:
                        buffering.setVisibility(View.VISIBLE);
                        break;
                    case Player.STATE_READY:
                        buffering.setVisibility(View.GONE);
                        if (!playbackStarted) {
                            playbackStarted = true;
                            handler.postDelayed(hideOverlay, 4000);
                        }
                        break;
                    case Player.STATE_ENDED:
                        buffering.setVisibility(View.GONE);
                        finish(); // return to the folder listing
                        break;
                    default:
                        buffering.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying) {
                    playerView.showController();
                    handler.removeCallbacks(hideOverlay);
                    handler.postDelayed(hideOverlay, 4000);
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                buffering.setVisibility(View.GONE);
                showPlaybackError(error);
            }
        });

        play(videoUrl);
    }

    /** Points the player at a URL and starts it. */
    private void play(String url) {
        try {
            MediaItem item = buildMediaItem(url);
            player.setMediaItem(item, resumePosition > 0 ? resumePosition : 0L);
            player.setPlayWhenReady(true);
            player.prepare();
            buffering.setVisibility(View.VISIBLE);
            errorView.setVisibility(View.GONE);
        } catch (Exception e) {
            showSimpleError("Could not open this stream.\n" + e.getMessage());
        }
    }

    /**
     * Builds the MediaItem, telling ExoPlayer which extractor to use.
     *
     * The MIME hint matters: some directory servers send
     * "Content-Type: application/octet-stream" or no type at all for .mkv,
     * and ExoPlayer then refuses to guess. Setting it explicitly from the file
     * extension makes MKV / TS / MP4 all open reliably.
     */
    private MediaItem buildMediaItem(String url) {
        MediaItem.Builder builder = MediaItem.Builder().setUri(Uri.parse(url));
        String mime = mimeFor(url);
        if (mime != null) {
            builder.setMimeType(mime);
        }
        return builder.build();
    }

    /** Maps a URL's extension to an ExoPlayer MIME type, or null to auto-detect. */
    private String mimeFor(String url) {
        String lower = url.toLowerCase();
        int cut = lower.length();
        int q = lower.indexOf('?');
        if (q >= 0) cut = q;
        int hash = lower.indexOf('#');
        if (hash >= 0 && hash < cut) cut = hash;
        String path = lower.substring(0, cut);

        if (path.endsWith(".m3u8")) return MimeTypes.APPLICATION_M3U8;
        if (path.endsWith(".mpd"))  return MimeTypes.APPLICATION_MPD;
        if (path.endsWith(".mp4") || path.endsWith(".m4v")) return MimeTypes.VIDEO_MP4;
        if (path.endsWith(".webm")) return MimeTypes.VIDEO_WEBM;
        if (path.endsWith(".mkv"))  return MimeTypes.VIDEO_MATROSKA;
        if (path.endsWith(".ts") || path.endsWith(".m2ts")) return MimeTypes.VIDEO_MP2T;
        // avi / flv / wmv / mov etc: let ExoPlayer sniff it.
        return null;
    }

    /* =====================================================================
     * ERRORS  -  the important part for AC3 / DTS audio
     * =================================================================== */

    /**
     * Turns a PlaybackException into something a normal person can act on.
     *
     * The AC3/DTS case is the one that matters in practice: the video track is
     * fine, only the audio decoder is missing. Instead of a dead end we say so
     * and offer the external player.
     */
    private void showPlaybackError(PlaybackException error) {
        String message;

        switch (error.errorCode) {
            case PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND:
                message = "The file was not found on the server.\nIt may have been moved or deleted.";
                break;
            case PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS:
                message = "The server refused to serve this file (HTTP error).\n"
                        + "It may need a login, or the link has expired.";
                break;
            case PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED:
            case PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED:
                message = "This file's container format is not supported by the built-in player.";
                break;
            case PlaybackException.ERROR_CODE_DECODER_INIT_FAILED:
            case PlaybackException.ERROR_CODE_DECODING_FAILED:
            case PlaybackException.ERROR_CODE_AUDIO_TRACK_INIT_FAILED:
            case PlaybackException.ERROR_CODE_AUDIO_TRACK_WRITE_FAILED:
                // >>> The classic MKV-with-AC3/DTS case lands here. <<<
                // The video track is fine; only the audio decoder is missing,
                // because AC3/DTS/EAC3 are licensed and are not part of the
                // open-source ExoPlayer build.
                message = "This file uses an audio or video codec the built-in player "
                        + "cannot decode (usually AC3, DTS or EAC3 audio inside an MKV).\n\n"
                        + "VLC or MX Player can play it - they include their own decoders.";
                break;
            default:
                // Everything else: show Media3's own name for the error code so
                // the message stays useful without guessing at constants.
                message = "Playback failed.\n\n"
                        + "Reason: " + error.getErrorCodeName()
                        + "\n\nIf this repeats, open the file with VLC or MX Player.";
                break;
        }

        showSimpleError(message);
    }

    private void showSimpleError(String message) {
        errorText.setText(message);
        errorView.setVisibility(View.VISIBLE);
        handler.removeCallbacks(hideOverlay);
        if (titleView != null) {
            titleView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Hands the current stream to an external player (VLC / MX Player) using the
     * app's own chooser, so the user picks and the choice is remembered.
     */
    private void handOffToExternalPlayer() {
        // Release our player first so the other app gets the full decoder pool.
        releasePlayer();
        VideoPlayerChooser.show(this, videoUrl, title, null);
        finish();
    }

    /* =====================================================================
     * REMOTE CONTROL
     * =================================================================== */

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (player == null) {
            return super.onKeyDown(keyCode, event);
        }
        switch (keyCode) {

            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_NUMPAD_ENTER:
            case KeyEvent.KEYCODE_BUTTON_A:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_SPACE:
                // If the error panel is showing, OK retries with an external player.
                if (errorView != null && errorView.getVisibility() == View.VISIBLE) {
                    handOffToExternalPlayer();
                    return true;
                }
                if (player.isPlaying()) {
                    player.pause();
                } else {
                    player.play();
                }
                playerView.showController();
                return true;

            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_MEDIA_REWIND:
                player.seekTo(Math.max(0, player.getCurrentPosition() - SEEK_MS));
                playerView.showController();
                return true;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                player.seekTo(Math.min(player.getDuration(),
                        player.getCurrentPosition() + SEEK_MS));
                playerView.showController();
                return true;

            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_MENU:
                // Bring the controls up; the D-Pad then moves inside them.
                playerView.showController();
                return true;

            case KeyEvent.KEYCODE_MEDIA_STOP:
                finish();
                return true;

            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    /* =====================================================================
     * LIFECYCLE HOUSEKEEPING
     * =================================================================== */

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (player != null) {
            outState.putLong(STATE_POSITION, player.getCurrentPosition());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            resumePosition = player.getCurrentPosition();
            player.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null && !player.isPlaying()) {
            player.seekTo(resumePosition);
            player.setPlayWhenReady(true);
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        releasePlayer();
        super.onDestroy();
    }

    /** Idempotent teardown: safe to call more than once. */
    private void releasePlayer() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
        if (playerView != null) {
            playerView.setPlayer(null);
        }
    }

    @Override
    public void onBackPressed() {
        // Remember where we were, then leave.
        if (player != null) {
            resumePosition = player.getCurrentPosition();
        }
        releasePlayer();
        super.onBackPressed();
    }

    private static String lastSegment(String url) {
        try {
            String clean = url;
            int q = clean.indexOf('?');
            if (q > 0) clean = clean.substring(0, q);
            int slash = clean.lastIndexOf('/');
            return Uri.decode(slash >= 0 ? clean.substring(slash + 1) : clean);
        } catch (Exception e) {
            return url;
        }
    }
}
