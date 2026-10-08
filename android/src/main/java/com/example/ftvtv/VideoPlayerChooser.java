package com.example.ftvtv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ============================================================================
 * FILE: app/src/main/java/com/example/ftvtv/VideoPlayerChooser.java
 * ============================================================================
 * Builds the "which player should open this film?" popup.
 *
 * WHY THIS CLASS EXISTS
 * The original approach - firing a bare Intent.ACTION_VIEW with MIME "video/*" -
 * has two problems on a TV box:
 *
 *   1. Browsers register the http/https scheme too, so Chrome / Silk / the
 *      built-in browser appeared in the list and opened the URL as a web page
 *      instead of playing it. That is the "it opens in a browser" symptom.
 *   2. The platform chooser cannot offer the app's OWN player, because that
 *      player is not a separate app - it is an Activity in this package.
 *
 * So instead of the system chooser we build our own list containing:
 *   - "Play in this app"  (PlayerActivity / ExoPlayer) - always first
 *   - every real video player found on the device, browsers filtered out
 *   - a "remember my choice" switch so playback becomes one press next time
 *
 * HOW BROWSERS ARE FILTERED OUT
 * A package is dropped when it:
 *   - is this app itself
 *   - handles the BROWSABLE category (i.e. it is a web browser)
 *   - is a known browser package name (belt and braces, for odd OEM builds)
 * ============================================================================
 */
public final class VideoPlayerChooser {

    /** SharedPreferences key: the package the user pinned as the default player. */
    public static final String KEY_PREFERRED_PLAYER = "preferred_player";

    /** Sentinel value stored when the user picks the in-app player. */
    public static final String BUILTIN = "com.example.ftvtv.builtin";

    /**
     * Packages that are browsers (or web wrappers) rather than video players.
     * They still match the http VIEW intent, so they must be excluded explicitly.
     */
    private static final Set<String> BROWSER_PACKAGES = new HashSet<String>();

    static {
        Collections.addAll(BROWSER_PACKAGES,
                "com.android.chrome",
                "com.android.browser",
                "com.chrome.beta",
                "com.chrome.dev",
                "com.chrome.canary",
                "org.mozilla.firefox",
                "org.mozilla.firefox_beta",
                "org.mozilla.focus",
                "com.opera.browser",
                "com.opera.mini.native",
                "com.brave.browser",
                "com.microsoft.emmx",
                "com.sec.android.app.sbrowser",
                "com.uc.browser.en",
                "com.UCMobile.intl",
                "com.amazon.cloud9",
                "com.amazon.firewebapp",
                "com.amazon.webview",
                "com.ksmobile.cb",
                "com.quark.browser",
                "com.heytap.browser",
                "com.mi.globalbrowser",
                "com.vivo.browser",
                "com.transsion.phoenix",
                "com.duckduckgo.mobile.android",
                "com.ecosia.android",
                "com.yandex.browser",
                "com.tencent.mtt",
                "com.tencent.mm",          // WeChat's built-in viewer
                "com.google.android.apps.docs",
                "com.google.android.gms"   // Plays Movies - a storefront, not a player
        );
    }

    private VideoPlayerChooser() {
        // no instances
    }

    /** One row in the chooser. */
    public static class PlayerOption {
        public final String label;        // shown to the user
        public final String packageName;  // null for the built-in player
        public final Drawable icon;
        public final boolean isBuiltin;

        PlayerOption(String label, String packageName, Drawable icon, boolean isBuiltin) {
            this.label = label;
            this.packageName = packageName;
            this.icon = icon;
            this.isBuiltin = isBuiltin;
        }
    }

    /* =====================================================================
     * PUBLIC ENTRY POINT
     * =================================================================== */

    /**
     * Shows the player chooser.
     *
     * @param activity   the calling activity (WebViewActivity or PlayerActivity)
     * @param videoUrl   the direct video URL
     * @param title      file name / server name, shown in the dialog header
     * @param onFinished run after the dialog closes (may be null)
     */
    public static void show(final Activity activity,
                            final String videoUrl,
                            final String title,
                            final Runnable onFinished) {

        if (activity == null || videoUrl == null || videoUrl.length() == 0) {
            return;
        }

        final List<PlayerOption> options = buildOptions(activity);

        // ---- If a player is already pinned, use it without asking ----------
        String pinned = getPreferredPlayer(activity);
        if (pinned != null) {
            if (BUILTIN.equals(pinned)) {
                openBuiltin(activity, videoUrl, title);
                if (onFinished != null) onFinished.run();
                return;
            }
            for (PlayerOption option : options) {
                if (pinned.equals(option.packageName)) {
                    if (launchPackage(activity, option.packageName, videoUrl)) {
                        if (onFinished != null) onFinished.run();
                        return;
                    }
                    // The pinned app was uninstalled - forget it and fall through.
                    forgetPreferredPlayer(activity);
                    break;
                }
            }
        }

        // ---- Otherwise: build the dialog ----------------------------------
        final boolean[] remember = {getRememberChoice(activity)};

        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(activity, 8);
        root.setPadding(pad, pad, pad, pad);

        final AlertDialog[] dialogRef = new AlertDialog[1];

        for (int i = 0; i < options.size(); i++) {
            final PlayerOption option = options.get(i);
            root.addView(buildRow(activity, option, dialogRef, videoUrl, title,
                    options, remember, onFinished));
        }

        // ---- "Remember my choice" ------------------------------------------
        final android.widget.CheckBox rememberBox = new android.widget.CheckBox(activity);
        rememberBox.setText(R.string.player_remember_choice);
        rememberBox.setTextColor(Color.parseColor("#C9D1DC"));
        rememberBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        rememberBox.setChecked(remember[0]);
        rememberBox.setPadding(dp(activity, 14), dp(activity, 10),
                dp(activity, 14), dp(activity, 4));
        rememberBox.setFocusable(true);
        rememberBox.setOnCheckedChangeListener(
                new android.widget.CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(android.widget.CompoundButton b, boolean checked) {
                        remember[0] = checked;
                    }
                });
        root.addView(rememberBox);

        // ---- Footer hint ---------------------------------------------------
        TextView hint = new TextView(activity);
        hint.setText(R.string.player_chooser_hint);
        hint.setTextColor(Color.parseColor("#66738A"));
        hint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        hint.setPadding(dp(activity, 14), dp(activity, 2), dp(activity, 14), dp(activity, 8));
        root.addView(hint);

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.player_chooser_title,
                        title != null && title.length() > 0 ? title : "video"))
                .setView(root)
                .setNegativeButton(R.string.player_chooser_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                if (onFinished != null) onFinished.run();
                            }
                        })
                .setCancelable(true)
                .create();

        dialogRef[0] = dialog;
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface d) {
                // Persist the "remember" switch state even if the user cancelled.
                setRememberChoice(activity, remember[0]);
            }
        });
        dialog.show();
    }

    /* =====================================================================
     * ROW BUILDING
     * =================================================================== */

    private static View buildRow(final Activity activity,
                                 final PlayerOption option,
                                 final AlertDialog[] dialogRef,
                                 final String videoUrl,
                                 final String title,
                                 final List<PlayerOption> options,
                                 final boolean[] remember,
                                 final Runnable onFinished) {

        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setFocusable(true);
        row.setClickable(true);
        int padH = dp(activity, 14);
        int padV = dp(activity, 11);
        row.setPadding(padH, padV, padH, padV);

        // Focus highlight: red outline, matching the app's TV focus language.
        final android.graphics.drawable.GradientDrawable bg =
                new android.graphics.drawable.GradientDrawable();
        bg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dp(activity, 10));
        bg.setColor(Color.TRANSPARENT);
        row.setBackground(bg);

        row.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    bg.setColor(Color.parseColor("#33FF0000"));
                    bg.setStroke(dp(activity, 3), Color.parseColor("#FF0000"));
                } else {
                    bg.setColor(Color.TRANSPARENT);
                    bg.setStroke(0, Color.TRANSPARENT);
                }
            }
        });

        // ---- Icon ----------------------------------------------------------
        ImageView icon = new ImageView(activity);
        int iconSize = dp(activity, 34);
        LinearLayout.LayoutParams iconLp =
                new LinearLayout.LayoutParams(iconSize, iconSize);
        iconLp.rightMargin = dp(activity, 14);
        icon.setLayoutParams(iconLp);
        if (option.icon != null) {
            icon.setImageDrawable(option.icon);
        }
        row.addView(icon);

        // ---- Label + subtitle ---------------------------------------------
        LinearLayout textCol = new LinearLayout(activity);
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView label = new TextView(activity);
        label.setText(option.label);
        label.setTextColor(Color.WHITE);
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        label.setSingleLine(true);
        label.setEllipsize(TextUtils.TruncateAt.END);
        textCol.addView(label);

        TextView sub = new TextView(activity);
        sub.setText(option.isBuiltin
                ? activity.getString(R.string.player_builtin_subtitle)
                : option.packageName);
        sub.setTextColor(Color.parseColor("#8B96A8"));
        sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        sub.setSingleLine(true);
        sub.setEllipsize(TextUtils.TruncateAt.END);
        textCol.addView(sub);

        row.addView(textCol);

        // ---- Click ---------------------------------------------------------
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRememberChoice(activity, remember[0]);
                if (remember[0]) {
                    setPreferredPlayer(activity, option.packageName);
                } else {
                    forgetPreferredPlayer(activity);
                }
                if (dialogRef[0] != null) {
                    dialogRef[0].dismiss();
                }
                if (option.isBuiltin) {
                    openBuiltin(activity, videoUrl, title);
                } else if (!launchPackage(activity, option.packageName, videoUrl)) {
                    Toast.makeText(activity,
                            option.label + " could not open this link",
                            Toast.LENGTH_LONG).show();
                }
                if (onFinished != null) onFinished.run();
            }
        });

        return row;
    }

    /* =====================================================================
     * OPTION DISCOVERY
     * =================================================================== */

    /**
     * @return the built-in player first, then every real video player installed,
     *         with browsers filtered out.
     */
    public static List<PlayerOption> buildOptions(Activity activity) {
        List<PlayerOption> out = new ArrayList<PlayerOption>();

        // 1. Always offer the app's own ExoPlayer-based player.
        Drawable appIcon = null;
        try {
            appIcon = activity.getPackageManager().getApplicationIcon(activity.getPackageName());
        } catch (Exception ignored) {
        }
        out.add(new PlayerOption(
                activity.getString(R.string.player_builtin_name),
                null, appIcon, true));

        // 2. Discover installed players.
        PackageManager pm = activity.getPackageManager();
        List<ResolveInfo> resolved = new ArrayList<ResolveInfo>();

        // Typed query: the most precise way to ask "who plays video?".
        Intent typed = new Intent(Intent.ACTION_VIEW);
        typed.setDataAndType(Uri.parse("http://example.com/video.mp4"), "video/*");
        addAll(resolved, pm, typed);

        // Scheme query: catches players that only register http/https.
        Intent scheme = new Intent(Intent.ACTION_VIEW, Uri.parse("http://example.com/video.mp4"));
        addAll(resolved, pm, scheme);

        String self = activity.getPackageName();
        Set<String> seen = new HashSet<String>();

        for (ResolveInfo info : resolved) {
            if (info == null || info.activityInfo == null) {
                continue;
            }
            String pkg = info.activityInfo.packageName;
            if (pkg == null || pkg.equals(self)) {
                continue;
            }
            if (!seen.add(pkg)) {
                continue; // already added
            }
            if (BROWSER_PACKAGES.contains(pkg)) {
                continue;
            }
            if (handlesBrowsableWeb(pm, pkg)) {
                continue; // it is a browser, not a player
            }
            if (!isNotDisabled(activity, pkg)) {
                continue;
            }

            String label;
            try {
                label = String.valueOf(pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)));
            } catch (Exception e) {
                label = pkg;
            }
            Drawable icon = null;
            try {
                icon = pm.getApplicationIcon(pkg);
            } catch (Exception ignored) {
            }
            out.add(new PlayerOption(label, pkg, icon, false));
        }

        // 3. Stable, predictable order: built-in first, then alphabetical.
        final PlayerOption first = out.isEmpty() ? null : out.get(0);
        List<PlayerOption> rest = new ArrayList<PlayerOption>(out);
        if (first != null) {
            rest.remove(0);
        }
        Collections.sort(rest, new Comparator<PlayerOption>() {
            @Override
            public int compare(PlayerOption a, PlayerOption b) {
                return a.label.compareToIgnoreCase(b.label);
            }
        });
        List<PlayerOption> sorted = new ArrayList<PlayerOption>();
        if (first != null) {
            sorted.add(first);
        }
        sorted.addAll(rest);
        return sorted;
    }

    private static void addAll(List<ResolveInfo> target, PackageManager pm, Intent intent) {
        try {
            List<ResolveInfo> found = pm.queryIntentActivities(intent, 0);
            if (found != null) {
                target.addAll(found);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * @return true when the package declares the BROWSABLE category for http(s),
     *         which is the signature of a web browser.
     */
    private static boolean handlesBrowsableWeb(PackageManager pm, String pkg) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://example.com"));
            i.addCategory(Intent.CATEGORY_BROWSABLE);
            List<ResolveInfo> list = pm.queryIntentActivities(i, 0);
            if (list != null) {
                for (ResolveInfo r : list) {
                    if (r.activityInfo != null && pkg.equals(r.activityInfo.packageName)) {
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private static boolean isNotDisabled(Activity activity, String pkg) {
        try {
            int state = activity.getPackageManager()
                    .getApplicationEnabledSetting(pkg);
            return state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    && state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER;
        } catch (Exception e) {
            return true;
        }
    }

    /* =====================================================================
     * LAUNCHING
     * =================================================================== */

    /** Starts the app's own ExoPlayer activity. */
    public static void openBuiltin(Activity activity, String videoUrl, String title) {
        Intent i = new Intent(activity, PlayerActivity.class);
        i.putExtra(PlayerActivity.EXTRA_VIDEO_URL, videoUrl);
        i.putExtra(PlayerActivity.EXTRA_TITLE, title);
        try {
            activity.startActivity(i);
        } catch (Exception e) {
            Toast.makeText(activity, "Could not open the built-in player",
                    Toast.LENGTH_LONG).show();
        }
    }

    /** Launches a specific player package, trying a typed then a plain intent. */
    public static boolean launchPackage(Activity activity, String packageName, String videoUrl) {
        if (packageName == null || packageName.length() == 0) {
            return false;
        }

        // Attempt 1: typed video/* intent.
        try {
            Intent typed = new Intent(Intent.ACTION_VIEW);
            typed.setPackage(packageName);
            typed.setDataAndType(Uri.parse(videoUrl), MainActivity.guessMimeType(videoUrl));
            typed.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            typed.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (isResolvable(activity, typed)) {
                activity.startActivity(typed);
                return true;
            }
        } catch (Exception ignored) {
        }

        // Attempt 2: plain http/https intent - how VLC and MX Player usually
        // register themselves.
        try {
            Intent plain = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
            plain.setPackage(packageName);
            plain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (isResolvable(activity, plain)) {
                activity.startActivity(plain);
                return true;
            }
        } catch (Exception ignored) {
        }

        // Attempt 3: launch the package's main activity with the URL as data.
        try {
            Intent launch = activity.getPackageManager()
                    .getLaunchIntentForPackage(packageName);
            if (launch != null) {
                launch.setDataAndType(Uri.parse(videoUrl),
                        MainActivity.guessMimeType(videoUrl));
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(launch);
                return true;
            }
        } catch (Exception ignored) {
        }

        return false;
    }

    private static boolean isResolvable(Context context, Intent intent) {
        try {
            PackageManager pm = context.getPackageManager();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ResolveInfo info = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                return info != null;
            }
            List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
            return list != null && !list.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /* =====================================================================
     * PREFERENCES
     * =================================================================== */

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE);
    }

    public static String getPreferredPlayer(Context context) {
        return prefs(context).getString(KEY_PREFERRED_PLAYER, null);
    }

    public static void setPreferredPlayer(Context context, String packageName) {
        prefs(context).edit().putString(KEY_PREFERRED_PLAYER, packageName).apply();
    }

    public static void forgetPreferredPlayer(Context context) {
        prefs(context).edit().remove(KEY_PREFERRED_PLAYER).apply();
    }

    public static boolean getRememberChoice(Context context) {
        return prefs(context).getBoolean("remember_player_choice", true);
    }

    public static void setRememberChoice(Context context, boolean value) {
        prefs(context).edit().putBoolean("remember_player_choice", value).apply();
    }

    /** Human-readable name of the pinned player, for the settings menu. */
    public static String describePreferredPlayer(Context context) {
        String pkg = getPreferredPlayer(context);
        if (pkg == null) {
            return context.getString(R.string.player_always_ask);
        }
        if (BUILTIN.equals(pkg)) {
            return context.getString(R.string.player_builtin_name);
        }
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(pkg, 0);
            return String.valueOf(pm.getApplicationLabel(info));
        } catch (Exception e) {
            return pkg;
        }
    }

    private static int dp(Context context, int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density);
    }
}
