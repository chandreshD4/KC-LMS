package com.kclms.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private final int navy = Color.rgb(17, 27, 54);
    private final int blue = Color.rgb(54, 100, 244);
    private final int bg = Color.rgb(246, 248, 253);
    private final int muted = Color.rgb(116, 126, 148);
    private final int ink = Color.rgb(30, 41, 68);
    private LinearLayout root;
    private LinearLayout bottomNav;

    private int dp(float n) {
        return (int) (n * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private GradientDrawable gradient(int a, int b, int radius) {
        GradientDrawable d = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[]{a, b}
        );
        d.setCornerRadius(dp(radius));
        return d;
    }

    private TextView label(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setIncludeFontPadding(true);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    private LinearLayout row() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    private void gap(LinearLayout parent, int height) {
        View v = new View(this);
        parent.addView(v, new LinearLayout.LayoutParams(
            1, dp(height)
        ));
    }

    private void line(LinearLayout parent) {
        View v = new View(this);
        v.setBackgroundColor(Color.rgb(235, 238, 246));
        parent.addView(v, new LinearLayout.LayoutParams(
            -1, dp(1)
        ));
    }

    private LinearLayout.LayoutParams lp(int w, int h) {
        return new LinearLayout.LayoutParams(
            w < 0 ? w : dp(w),
            h < 0 ? h : dp(h)
        );
    }

    private void pad(LinearLayout v, int l, int t, int r, int b) {
        v.setPadding(dp(l), dp(t), dp(r), dp(b));
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(navy);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );
        showHome();
    }

    private void showHome() {
        LinearLayout screen = column();
        screen.setBackgroundColor(bg);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        scroll.setClipToPadding(false);
        scroll.setVerticalScrollBarEnabled(false);

        root = column();
        pad(root, 20, 12, 20, 20);
        scroll.addView(root);

        screen.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        buildHeader();
        buildWelcome();
        buildConnectionCard();
        buildMediaSection();
        buildFoldersSection();
        buildRecentSection();

        buildBottomNav(screen);
        setContentView(screen);
    }

    private void buildHeader() {
        LinearLayout header = row();

        LinearLayout logo = new LinearLayout(this);
        logo.setGravity(Gravity.CENTER);
        logo.setBackground(gradient(blue, Color.rgb(113, 83, 248), 15));
        TextView mark = label("K", 24, Color.WHITE, true);
        mark.setGravity(Gravity.CENTER);
        logo.addView(mark);
        header.addView(logo, lp(46, 46));

        LinearLayout names = column();
        pad(names, 11, 0, 0, 0);
        names.addView(label("KC LMS", 21, navy, true));
        names.addView(label("PRIVATE MEDIA SPACE", 9, muted, true));
        header.addView(names, new LinearLayout.LayoutParams(0, -2, 1));

        TextView profile = label("CR", 14, blue, true);
        profile.setGravity(Gravity.CENTER);
        profile.setBackground(shape(Color.rgb(227, 235, 255), 50));
        header.addView(profile, lp(43, 43));

        root.addView(header);
        gap(root, 24);
    }

    private void buildWelcome() {
        root.addView(label("Your personal space", 25, navy, true));
        gap(root, 4);
        root.addView(label(
            "Your files. Your privacy. Your control.",
            13, muted, false
        ));
        gap(root, 20);
    }

    private void buildConnectionCard() {
        LinearLayout card = column();
        pad(card, 19, 20, 19, 19);
        card.setBackground(gradient(navy, Color.rgb(38, 64, 124), 23));

        LinearLayout top = row();

        LinearLayout iconBox = new LinearLayout(this);
        iconBox.setGravity(Gravity.CENTER);
        iconBox.setBackground(shape(Color.rgb(48, 65, 111), 14));
        TextView icon = label("↔", 29, Color.WHITE, true);
        icon.setGravity(Gravity.CENTER);
        iconBox.addView(icon);
        top.addView(iconBox, lp(54, 54));

        LinearLayout titleBox = column();
        pad(titleBox, 13, 0, 0, 0);
        titleBox.addView(label("Connect a device", 18, Color.WHITE, true));
        gap(titleBox, 4);
        titleBox.addView(label(
            "Access your media remotely",
            12, Color.rgb(206, 218, 246), false
        ));
        top.addView(titleBox, new LinearLayout.LayoutParams(0, -2, 1));

        card.addView(top);
        gap(card, 17);

        TextView button = label("＋   Connect another device     →", 14,
            navy, true);
        button.setGravity(Gravity.CENTER);
        button.setBackground(shape(Color.WHITE, 14));
        card.addView(button, lp(-1, 49));
        button.setOnClickListener(v -> toast(
            "Device connection will be added in a later stage."
        ));

        gap(root, 24);
        root.addView(card);
        gap(root, 25);
    }

    private void sectionHeading(String title, String action,
                                Runnable callback) {
        LinearLayout r = row();
        r.addView(label(title, 19, navy, true),
            new LinearLayout.LayoutParams(0, -2, 1));
        TextView more = label(action, 12, blue, true);
        more.setGravity(Gravity.CENTER);
        r.addView(more);
        if (callback != null) more.setOnClickListener(v -> callback.run());
        root.addView(r);
        gap(root, 13);
    }

    private void buildMediaSection() {
        sectionHeading("Browse media", "View all", () ->
            toast("Media browser will be added later.")
        );

        LinearLayout first = row();
        LinearLayout second = row();

        addMediaTile(first, "▧", "Photos", "Images",
            Color.rgb(229, 237, 255), blue);
        addMediaTile(first, "▷", "Videos", "Movies",
            Color.rgb(255, 233, 226), Color.rgb(233, 100, 74));

        addMediaTile(second, "♫", "Audio", "Music & sounds",
            Color.rgb(235, 229, 255), Color.rgb(127, 86, 220));
        addMediaTile(second, "▤", "Documents", "PDF & files",
            Color.rgb(220, 246, 235), Color.rgb(32, 153, 106));

        root.addView(first);
        gap(root, 12);
        root.addView(second);
        gap(root, 26);
    }

    private void addMediaTile(LinearLayout parent, String symbol,
                              String title, String detail,
                              int tint, int accent) {
        LinearLayout card = column();
        pad(card, 14, 15, 10, 14);
        card.setBackground(shape(Color.WHITE, 19));
        card.setElevation(dp(1));

        TextView icon = label(symbol, 25, accent, true);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(shape(tint, 13));
        card.addView(icon, lp(48, 45));

        gap(card, 13);
        card.addView(label(title, 15, ink, true));
        gap(card, 3);
        card.addView(label(detail, 10, muted, false));

        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(0, dp(128), 1);
        params.setMargins(dp(4), 0, dp(4), 0);
        parent.addView(card, params);

        card.setOnClickListener(v -> toast(
            title + " browsing will be added later."
        ));
    }

    private void buildFoldersSection() {
        sectionHeading("My folders", "+ New folder", () ->
            toast("Folder creation will be added later.")
        );

        LinearLayout card = column();
        pad(card, 15, 3, 15, 3);
        card.setBackground(shape(Color.WHITE, 19));

        addFolderRow(card, "▰", "Private collection",
            "Password-protected folder", blue);
        line(card);
        addFolderRow(card, "▰", "Shared with devices",
            "Your selected media folders", Color.rgb(127, 86, 220));

        root.addView(card);
        gap(root, 25);
    }

    private void addFolderRow(LinearLayout parent, String symbol,
                              String title, String subtitle, int accent) {
        LinearLayout r = row();
        pad(r, 0, 12, 0, 12);

        TextView icon = label(symbol, 23, accent, true);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(shape(Color.rgb(238, 241, 255), 12));
        r.addView(icon, lp(47, 47));

        LinearLayout details = column();
        pad(details, 12, 0, 0, 0);
        details.addView(label(title, 14, ink, true));
        gap(details, 3);
        details.addView(label(subtitle, 11, muted, false));
        r.addView(details, new LinearLayout.LayoutParams(0, -2, 1));

        TextView arrow = label("›", 27, muted, false);
        r.addView(arrow);
        parent.addView(r);

        r.setOnClickListener(v -> toast(
            "Folder access will be available after implementation."
        ));
    }

    private void buildRecentSection() {
        sectionHeading("Recent media", "See all", () ->
            toast("Recent media will appear when file access is implemented.")
        );

        LinearLayout empty = column();
        empty.setGravity(Gravity.CENTER);
        pad(empty, 15, 22, 15, 22);
        empty.setBackground(shape(Color.WHITE, 19));

        TextView icon = label("▤", 30, blue, true);
        icon.setGravity(Gravity.CENTER);
        empty.addView(icon, lp(56, 52));

        gap(empty, 8);
        TextView title = label("Your recent files will appear here",
            14, ink, true);
        title.setGravity(Gravity.CENTER);
        empty.addView(title);

        gap(empty, 5);
        TextView description = label(
            "Allow media access when the file manager is ready.",
            11, muted, false
        );
        description.setGravity(Gravity.CENTER);
        empty.addView(description);

        root.addView(empty);
        gap(root, 20);
    }

    private void buildBottomNav(LinearLayout screen) {
        bottomNav = row();
        bottomNav.setBackgroundColor(Color.WHITE);
        bottomNav.setElevation(dp(12));
        pad(bottomNav, 5, 7, 5, 7);

        addNavItem("⌂", "Home", true);
        addNavItem("▤", "Files", false);
        addNavItem("↔", "Devices", false);
        addNavItem("⚙", "Settings", false);

        screen.addView(bottomNav, new LinearLayout.LayoutParams(-1, dp(66)));
    }

    private void addNavItem(String icon, String title, boolean selected) {
        LinearLayout item = column();
        item.setGravity(Gravity.CENTER);

        TextView symbol = label(icon, 22,
            selected ? blue : muted, true);
        symbol.setGravity(Gravity.CENTER);
        item.addView(symbol);

        TextView caption = label(title, 10,
            selected ? blue : muted, selected);
        caption.setGravity(Gravity.CENTER);
        item.addView(caption);

        bottomNav.addView(item,
            new LinearLayout.LayoutParams(0, -1, 1));

        item.setOnClickListener(v -> {
            if (!title.equals("Home")) {
                toast(title + " screen will be implemented later.");
            }
        });
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
