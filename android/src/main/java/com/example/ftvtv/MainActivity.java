package com.example.ftvtv;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends Activity {

    private static final int COLUMNS = 3;
    private static final float FOCUS_SCALE = 1.08f;
    private static final int FOCUS_ANIM_MS = 140;

    public static final String[] VIDEO_EXTENSIONS = {
            "mp4", "mkv", "avi", "webm", "m3u8", "mpg", "mpeg",
            "mov", "flv", "wmv", "m4v", "ts", "m2ts", "3gp", "ogv", "divx", "vob"
    };

    public static final String PREFS = "ftvtv_prefs";
    public static final String KEY_LAST_SERVER = "last_server_index";

    private GridLayout grid;
    private TextView resumeBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        grid = (GridLayout) findViewById(R.id.server_grid);
        resumeBar = (TextView) findViewById(R.id.resume_bar);

        grid.setColumnCount(COLUMNS);
        buildDashboard();
        setupResumeBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateResumeBarText();
    }

    private void buildDashboard() {
        grid.removeAllViews();

        final List<Server> servers = ServerRepository.all();

        int cardWidth = (int) dp(240);
        int cardHeight = (int) dp(150);
        int gap = (int) dp(18);

        for (int i = 0; i < servers.size(); i++) {
            Server server = servers.get(i);

            View card = createServerCard(server, i);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = cardWidth;
            lp.height = cardHeight;
            lp.setMargins(gap / 2, gap / 2, gap / 2, gap / 2);
            card.setLayoutParams(lp);

            final int index = i;
            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openServer(index);
                }
            });

            card.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    animateCard(v, hasFocus);
                }
            });

            card.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() != KeyEvent.ACTION_DOWN) {
                        return false;
                    }
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            moveFocus(index - 1);
                            return true;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            moveFocus(index + 1);
                            return true;
                        case KeyEvent.KEYCODE_DPAD_UP:
                            moveFocus(index - COLUMNS);
                            return true;
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            moveFocus(index + COLUMNS);
                            return true;
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                        case KeyEvent.KEYCODE_NUMPAD_ENTER:
                        case KeyEvent.KEYCODE_BUTTON_A:
                            openServer(index);
                            return true;
                        default:
                            return false;
                    }
                }
            });

            grid.addView(card);
        }

        // --- Developer Credits View (ড্যাশবোর্ডের নিচে নাম ও কন্টাক্ট নম্বর) ---
        addDeveloperCredits();

        grid.post(new Runnable() {
            @Override
            public void run() {
                View first = grid.getChildAt(0);
                if (first != null) {
                    first.requestFocus();
                }
            }
        });
    }

    /** Developer Credits ফুটার যুক্ত করার জন্য মেথড */
    private void addDeveloperCredits() {
        TextView credits = new TextView(this);
        credits.setText("Developed by: Sayful Islam | Contact: 01676714139");
        credits.setTextColor(0xFF8B96A8);
        credits.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        credits.setGravity(Gravity.CENTER);
        credits.setPadding((int) dp(12), (int) dp(20), (int) dp(12), (int) dp(20));

        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.columnSpec = GridLayout.spec(0, COLUMNS); // ৩টি কলাম জুড়ে থাকবে
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        credits.setLayoutParams(lp);

        grid.addView(credits);
    }

    private View createServerCard(Server server, int index) {
        FrameLayout card = new FrameLayout(this);
        card.setFocusable(true);
        card.setFocusableInTouchMode(false);
        card.setClickable(true);
        card.setClipToPadding(false);
        card.setClipChildren(false);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dp(14));
        bg.setColor(0xFF141A24);
        bg.setStroke((int) dp(2), 0xFF2A3342);
        card.setBackground(bg);

        View stripe = new View(this);
        GradientDrawable stripeBg = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{server.accentColor, blend(server.accentColor, Color.BLACK, 0.45f)});
        stripeBg.setCornerRadii(new float[]{dp(14), dp(14), 0, 0, 0, 0, dp(14), dp(14)});
        stripe.setBackground(stripeBg);
        FrameLayout.LayoutParams stripeLp = new FrameLayout.LayoutParams(
                (int) dp(8), ViewGroup.LayoutParams.MATCH_PARENT);
        stripeLp.gravity = Gravity.START;
        stripe.setLayoutParams(stripeLp);
        card.addView(stripe);

        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.CENTER_VERTICAL);
        column.setPadding((int) dp(26), (int) dp(16), (int) dp(16), (int) dp(16));
        FrameLayout.LayoutParams colLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        column.setLayoutParams(colLp);
        card.addView(column);

        TextView initials = new TextView(this);
        initials.setText(server.initials);
        initials.setTextColor(Color.WHITE);
        initials.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        initials.setTypeface(initials.getTypeface(), android.graphics.Typeface.BOLD);
        initials.setGravity(Gravity.CENTER);
        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setShape(GradientDrawable.OVAL);
        badgeBg.setColor(server.accentColor);
        initials.setBackground(badgeBg);
        LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(
                (int) dp(40), (int) dp(40));
        badgeLp.bottomMargin = (int) dp(12);
        initials.setLayoutParams(badgeLp);
        column.addView(initials);

        TextView name = new TextView(this);
        name.setText(server.name);
        name.setTextColor(Color.WHITE);
        name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        name.setTypeface(name.getTypeface(), android.graphics.Typeface.BOLD);
        name.setSingleLine(true);
        name.setEllipsize(android.text.TextUtils.TruncateAt.END);
        column.addView(name);

        TextView tagline = new TextView(this);
        tagline.setText(server.tagline);
        tagline.setTextColor(0xFF8B96A8);
        tagline.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tagline.setSingleLine(true);
        column.addView(tagline);

        TextView host = new TextView(this);
        host.setText(server.host());
        host.setTextColor(server.accentColor);
        host.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        host.setSingleLine(true);
        host.setEllipsize(android.text.TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams hostLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hostLp.topMargin = (int) dp(8);
        host.setLayoutParams(hostLp);
        column.addView(host);

        return card;
    }

    private void moveFocus(int index) {
        if (grid == null || grid.getChildCount() == 0) {
            return;
        }
        if (index < 0 || index >= grid.getChildCount() - 1) { // Credits ভিউ বাদ দিয়ে ফোকাস হ্যান্ডলিং
            return;
        }
        View target = grid.getChildAt(index);
        if (target != null && target.isFocusable()) {
            target.requestFocus();
        }
    }

    private void animateCard(View card, boolean focused) {
        float targetScale = focused ? FOCUS_SCALE : 1f;

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(card, "scaleX", targetScale);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(card, "scaleY", targetScale);
        scaleX.setDuration(FOCUS_ANIM_MS);
        scaleY.setDuration(FOCUS_ANIM_MS);
        scaleX.setInterpolator(new DecelerateInterpolator());
        scaleY.setInterpolator(new DecelerateInterpolator());
        scaleX.start();
        scaleY.start();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            card.setElevation(focused ? dp(16) : dp(0));
        }

        android.graphics.drawable.Drawable d = card.getBackground();
        if (d instanceof GradientDrawable) {
            GradientDrawable g = (GradientDrawable) d;
            g.setStroke((int) dp(focused ? 3 : 2),
                    focused ? 0xFFFF0000 : 0xFF2A3342);
        }

        if (focused && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            card.setStateListAnimator(null);
        }
    }

    private void setupResumeBar() {
        if (resumeBar == null) {
            return;
        }
        resumeBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int idx = lastServerIndex();
                if (idx >= 0) {
                    openServer(idx);
                }
            }
        });
        resumeBar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                        .remove(KEY_LAST_SERVER).apply();
                updateResumeBarText();
                Toast.makeText(MainActivity.this, "Resume cleared", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        updateResumeBarText();
    }

    private int lastServerIndex() {
        int idx = getSharedPreferences(PREFS, MODE_PRIVATE).getInt(KEY_LAST_SERVER, -1);
        if (idx < 0 || idx >= ServerRepository.size()) {
            return -1;
        }
        return idx;
    }

    private void updateResumeBarText() {
        if (resumeBar == null) {
            return;
        }
        int idx = lastServerIndex();
        if (idx < 0) {
            resumeBar.setVisibility(View.GONE);
        } else {
            resumeBar.setVisibility(View.VISIBLE);
            resumeBar.setText(getString(R.string.resume_hint,
                    ServerRepository.get(idx).name));
        }
    }

    public void openServer(int index) {
        Server server = ServerRepository.get(index);

        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putInt(KEY_LAST_SERVER, index).apply();

        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_SERVER_NAME, server.name);
        intent.putExtra(WebViewActivity.EXTRA_SERVER_URL, server.url);
        intent.putExtra(WebViewActivity.EXTRA_ACCENT, server.accentColor);
        startActivity(intent);
    }

    public static boolean looksLikeVideoUrl(String url) {
        if (url == null) {
            return false;
        }
        String lower = url.toLowerCase();

        int cut = lower.length();
        int q = lower.indexOf('?');
        if (q >= 0 && q < cut) cut = q;
        int h = lower.indexOf('#');
        if (h >= 0 && h < cut) cut = h;
        String path = lower.substring(0, cut);

        for (String ext : VIDEO_EXTENSIONS) {
            if (path.endsWith("." + ext)) {
                return true;
            }
        }
        for (String ext : VIDEO_EXTENSIONS) {
            if (path.contains("." + ext + "/")) {
                return true;
            }
        }
        return false;
    }

    public static String guessMimeType(String url) {
        String lower = url.toLowerCase();
        int cut = lower.length();
        int q = lower.indexOf('?');
        if (q >= 0) cut = q;
        int slash = lower.lastIndexOf('.', cut - 1);
        if (slash < 0) {
            return "video/*";
        }
        String ext = lower.substring(slash + 1, cut);
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
        return (mime == null || !mime.startsWith("video")) ? "video/*" : mime;
    }

    public static boolean openVideoInExternalPlayer(Activity activity,
                                                   String videoUrl,
                                                   String title) {
        if (activity == null || videoUrl == null || videoUrl.length() == 0) {
            return false;
        }
        VideoPlayerChooser.show(activity, videoUrl, title, null);
        return true;
    }

    public static boolean isIntentResolvable(Context context, Intent intent) {
        try {
            PackageManager pm = context.getPackageManager();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ResolveInfo info = pm.resolveActivity(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
                return info != null;
            } else {
                List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
                return list != null && !list.isEmpty();
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.exit_title)
                .setMessage(R.string.exit_message)
                .setPositiveButton(R.string.exit_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(R.string.exit_no, null)
                .show();
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private static int blend(int a, int b, float ratio) {
        float ir = 1f - ratio;
        int r = (int) (Color.red(a) * ir + Color.red(b) * ratio);
        int g = (int) (Color.green(a) * ir + Color.green(b) * ratio);
        int bl = (int) (Color.blue(a) * ir + Color.blue(b) * ratio);
        return Color.rgb(r, g, bl);
    }
}
