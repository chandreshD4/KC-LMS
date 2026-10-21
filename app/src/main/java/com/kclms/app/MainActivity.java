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
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.app.AlertDialog;
import android.content.SharedPreferences;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Switch;
import java.util.UUID;
import android.widget.ImageView;

public class MainActivity extends Activity {

    private int navy = Color.rgb(17, 27, 54);
    private int blue = Color.rgb(54, 100, 244);
    private int bg = Color.rgb(246, 248, 253);
    private int muted = Color.rgb(116, 126, 148);
    private int ink = Color.rgb(30, 41, 68);
    private int surface = Color.WHITE;
    private boolean darkMode = false;
    private LinearLayout root;
    private LinearLayout bottomNav;
private FrameLayout frame;
private LinearLayout drawer;
private SharedPreferences prefs;

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
        v.setBackgroundColor(darkMode ? Color.rgb(54, 64, 85) : Color.rgb(235, 238, 246));
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
        showHome();
    }

    private void showHome() {
        prefs = getSharedPreferences("kc_lms_settings", MODE_PRIVATE);
        darkMode = prefs.getBoolean("dark_mode", false);

        navy = darkMode ? Color.rgb(10, 16, 32) : Color.rgb(17, 27, 54);
        blue = Color.rgb(54, 100, 244);
        bg = darkMode ? Color.rgb(15, 20, 34) : Color.rgb(246, 248, 253);
        muted = darkMode ? Color.rgb(166, 178, 201) : Color.rgb(116, 126, 148);
        ink = darkMode ? Color.rgb(235, 240, 250) : Color.rgb(30, 41, 68);
        surface = darkMode ? Color.rgb(29, 37, 56) : Color.WHITE;

        getWindow().setStatusBarColor(navy);
        getWindow().setNavigationBarColor(bg);
        getWindow().getDecorView().setSystemUiVisibility(
            darkMode ? 0 : View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );

        LinearLayout screen = column();
        screen.setBackgroundColor(bg);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        scroll.setClipToPadding(false);
        scroll.setVerticalScrollBarEnabled(false);

        root = column();
        pad(root, 16, 0, 16, 20);
        scroll.addView(root);

        screen.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        buildHeader();
        buildWelcome();
        buildConnectionCard();
        buildMediaSection();
        buildFoldersSection();
        buildRecentSection();

        buildBottomNav(screen);

        frame = new FrameLayout(this);
        frame.addView(screen, new FrameLayout.LayoutParams(-1, -1));
        buildDrawer();
        setContentView(frame);
    }

    private void buildHeader() {
        LinearLayout header = row();

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.kc_lms_logo);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        header.addView(logo, lp(46, 46));

        LinearLayout names = column();
        pad(names, 11, 0, 0, 0);
        names.addView(label("KC LMS", 21, navy, true));
        names.addView(label(t("PRIVATE MEDIA SPACE"), 9, muted, true));
        header.addView(names, new LinearLayout.LayoutParams(0, -2, 1));

        TextView menu = label("☰", 25, Color.WHITE, true);
        menu.setGravity(Gravity.CENTER);
        menu.setBackground(shape(Color.rgb(40, 55, 91), 14));
        header.addView(menu, lp(46, 46));
        menu.setOnClickListener(v -> openDrawer());

        root.addView(header);
        gap(root, 14);

        LinearLayout search = row();
        pad(search, 14, 0, 12, 0);
        search.setBackground(shape(surface, 15));
        TextView searchIcon = label("⌕", 25, muted, true);
        search.addView(searchIcon);
        TextView searchText = label(t("Search files, folders..."), 13, muted, false);
        LinearLayout.LayoutParams searchTextParams =
                new LinearLayout.LayoutParams(0, dp(48), 1);
        searchTextParams.leftMargin = dp(10);
        search.addView(searchText, searchTextParams);
        TextView filter = label("☷", 21, blue, true);
        search.addView(filter);
        root.addView(search);
        search.setOnClickListener(v -> toast("Search will be connected to the media browser."));
        gap(root, 20);
    }

    private void buildWelcome() {
        root.addView(label(t("Your personal space"), 25, navy, true));
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
        titleBox.addView(label(t("Connect a device"), 18, Color.WHITE, true));
        gap(titleBox, 4);
        titleBox.addView(label(
            "Access your media remotely",
            12, Color.rgb(206, 218, 246), false
        ));
        top.addView(titleBox, new LinearLayout.LayoutParams(0, -2, 1));

        card.addView(top);
        gap(card, 17);

        TextView button = label(t("＋   Connect another device     →"), 14,
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
        sectionHeading(t("Browse media"), "View all", () ->
            toast("Media browser will be added later.")
        );

        LinearLayout first = row();
        LinearLayout second = row();

        addMediaTile(first, "▧", t("Photos"), "Images",
            Color.rgb(229, 237, 255), blue);
        addMediaTile(first, "▷", t("Videos"), "Movies",
            Color.rgb(255, 233, 226), Color.rgb(233, 100, 74));

        addMediaTile(second, "♫", t("Audio"), "Music & sounds",
            Color.rgb(235, 229, 255), Color.rgb(127, 86, 220));
        addMediaTile(second, "▤", t("Documents"), "PDF & files",
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
        card.setBackground(shape(surface, 19));
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
        sectionHeading(t("My folders"), "+ New folder", () ->
            toast("Folder creation will be added later.")
        );

        LinearLayout card = column();
        pad(card, 15, 3, 15, 3);
        card.setBackground(shape(surface, 19));

        addFolderRow(card, "▰", t("Private collection"),
            "Password-protected folder", blue);
        line(card);
        addFolderRow(card, "▰", t("Shared with devices"),
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
        sectionHeading(t("Recent media"), "See all", () ->
            toast("Recent media will appear when file access is implemented.")
        );

        LinearLayout empty = column();
        empty.setGravity(Gravity.CENTER);
        pad(empty, 15, 22, 15, 22);
        empty.setBackground(shape(surface, 19));

        TextView icon = label("▤", 30, blue, true);
        icon.setGravity(Gravity.CENTER);
        empty.addView(icon, lp(56, 52));

        gap(empty, 8);
        TextView title = label(t("Your recent files will appear here"),
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
        bottomNav.setBackgroundColor(surface);
        bottomNav.setElevation(dp(12));
        pad(bottomNav, 5, 7, 5, 7);

        addNavItem("⌂", t("Home"), true);
        addNavItem("▤", t("Files"), false);
        addNavItem("⌕", t("Search"), false);
        addNavItem("↔", t("Devices"), false);
        addNavItem("⚙", t("Settings"), false);

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
            if (title.equals(t("Search"))) {
                showSearch();
            } else if (!title.equals(t("Home"))) {
                toast(title + " screen will be implemented later.");
            }
        });
    }


    private void buildDrawer() {
        drawer = column();
        drawer.setBackgroundColor(surface);
        drawer.setElevation(dp(18));
        pad(drawer, 0, 0, 0, 0);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            dp(310), -1, Gravity.LEFT
        );
        params.leftMargin = -dp(310);
        frame.addView(drawer, params);
        drawer.setTag(params);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout items = column();

        LinearLayout brand = column();
        pad(brand, 20, 26, 20, 22);
        brand.setBackground(gradient(navy, Color.rgb(39, 66, 129), 0));

        ImageView brandLogo = new ImageView(this);
        brandLogo.setImageResource(R.drawable.kc_lms_logo);
        brandLogo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        brand.addView(brandLogo, lp(58, 58));

        gap(brand, 12);
        String displayName = prefs.getString("display_name", "KC LMS User");
        brand.addView(label(displayName, 21, Color.WHITE, true));
        gap(brand, 5);
        brand.addView(label("KC LMS  •  PRIVATE MEDIA SPACE", 10,
            Color.rgb(210, 220, 245), true));
        gap(brand, 10);
        brand.addView(label("User ID: " + getOrCreateUserId(), 11,
            Color.rgb(225, 232, 250), false));

        items.addView(brand);
        gap(items, 15);

        addDrawerItem(items, "◉", t("My Profile"), "Account and user details");
        addDrawerItem(items, "▤", t("My Files"), "Browse device media");
        addDrawerItem(items, "▣", t("My Folders"), "Your private collections");
        addDrawerItem(items, "🔑", t("Password Manager"), "Saved passwords");
        addDrawerItem(items, "↔", t("Connected Devices"), "Manage connections");

        gap(items, 8);
        line(items);
        gap(items, 12);
        items.addView(label(t("PREFERENCES"), 11, muted, true));
        gap(items, 6);

        addDrawerItem(items, "⚙", "Settings", "App preferences");
        addDrawerItem(items, "文", t("App Language"), currentLanguage());
        addDarkModeItem(items);

        gap(items, 8);
        line(items);
        gap(items, 12);
        items.addView(label(t("SECURITY & INFORMATION"), 11, muted, true));
        gap(items, 6);

        addDrawerItem(items, "♢", t("Privacy Policy"), "How your data is handled");
        addDrawerItem(items, "ⓘ", t("About KC LMS"), "App information");
        addDrawerItem(items, "✓", t("App Version"), "Version 1.0");

        gap(items, 18);
        TextView footer = label("KC LMS  •  Built for your privacy",
            11, muted, false);
        footer.setGravity(Gravity.CENTER);
        items.addView(footer);

        scroll.addView(items);
        drawer.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
    }

    private String getOrCreateUserId() {
        String id = prefs.getString("user_id", null);
        if (id == null || id.isEmpty()) {
            id = UUID.randomUUID().toString()
                .replace("-", "").substring(0, 10).toUpperCase();
            prefs.edit().putString("user_id", id).apply();
        }
        return id;
    }

    private void addDarkModeItem(LinearLayout parent) {
        LinearLayout item = row();
        item.setGravity(Gravity.CENTER_VERTICAL);
        pad(item, 16, 8, 14, 8);

        LinearLayout texts = column();
        texts.addView(label(t("Dark Mode"), 14, ink, true));
        gap(texts, 2);
        texts.addView(label("Switch app appearance", 10, muted, false));
        item.addView(texts, new LinearLayout.LayoutParams(0, -2, 1));

        Switch modeSwitch = new Switch(this);
        modeSwitch.setTextOn("ON");
        modeSwitch.setTextOff("OFF");
        modeSwitch.setShowText(true);
        modeSwitch.setChecked(prefs.getBoolean("dark_mode", false));
        item.addView(modeSwitch);
        parent.addView(item);

        modeSwitch.setOnCheckedChangeListener((button, enabled) -> {
            prefs.edit().putBoolean("dark_mode", enabled).apply();
            showHome();
        });
        item.setOnClickListener(v ->
            modeSwitch.setChecked(!modeSwitch.isChecked()));
    }

    private void openDrawer() {
        View shade = frame.findViewWithTag("drawer_shade");
        if (shade == null) {
            shade = new View(this);
            shade.setTag("drawer_shade");
            shade.setBackgroundColor(0x99000000);
            FrameLayout.LayoutParams shadeParams =
                new FrameLayout.LayoutParams(-1, -1);
            frame.addView(shade, 1, shadeParams);
            shade.setOnClickListener(v -> closeDrawer());
        }

        shade.setVisibility(View.VISIBLE);
        shade.bringToFront();

        FrameLayout.LayoutParams params =
            (FrameLayout.LayoutParams) drawer.getTag();
        params.leftMargin = 0;
        drawer.setLayoutParams(params);
        drawer.bringToFront();
    }

    private void closeDrawer() {
        if (drawer == null || frame == null) return;
        FrameLayout.LayoutParams params =
            (FrameLayout.LayoutParams) drawer.getTag();
        params.leftMargin = -dp(310);
        drawer.setLayoutParams(params);
        View shade = frame.findViewWithTag("drawer_shade");
        if (shade != null) shade.setVisibility(View.GONE);
    }

    private void addDrawerItem(LinearLayout parent, String icon,
                               String title, String subtitle) {
        LinearLayout item = row();
        pad(item, 8, 10, 4, 10);

        TextView symbol = label(icon, 19, blue, true);
        symbol.setGravity(Gravity.CENTER);
        item.addView(symbol, lp(35, 38));

        LinearLayout texts = column();
        pad(texts, 10, 0, 0, 0);
        texts.addView(label(title, 14, ink, true));
        gap(texts, 2);
        texts.addView(label(t(subtitle), 10, muted, false));
        item.addView(texts, new LinearLayout.LayoutParams(0, -2, 1));

        parent.addView(item);
        item.setOnClickListener(v -> {
            closeDrawer();

            if (title.equals(t("App Language"))) {
                chooseLanguage();
            } else if (title.equals(t("Dark Mode"))) {
                toggleDarkMode();
            } else if (title.equals(t("My Profile"))) {
                showProfileDialog();
            } else if (title.equals(t("Password Manager"))) {
                showInfo(t("Password Manager"),
                    t("Secure password storage and authenticated viewing are planned next."));
            } else if (title.equals(t("Privacy Policy"))) {
                showInfo(t("Privacy Policy"),
                    t("The full privacy policy will be added before release."));
            } else if (title.equals(t("App Version"))) {
                showInfo("KC LMS", "Version 1.0");
            } else if (title.equals(t("About KC LMS"))) {
                showInfo(t("About KC LMS"),
                    "Private media access and secure device connections.");
            } else {
                toast(title + " will be implemented in a later stage.");
            }
        });
    }

    private String t(String en) {
        String lang = currentLanguage();
        if ("हिंदी".equals(lang)) {
            switch (en) {
                case "PRIVATE MEDIA SPACE": return "निजी मीडिया स्पेस";
                case "Search files, folders...": return "फ़ाइलें और फ़ोल्डर खोजें...";
                case "Your personal space": return "आपकी निजी जगह";
                case "Your files. Your privacy. Your control.": return "आपकी फ़ाइलें। आपकी गोपनीयता। आपका नियंत्रण।";
                case "Connect a device": return "डिवाइस कनेक्ट करें";
                case "Access your media remotely": return "दूर से अपना मीडिया देखें";
                case "＋   Connect another device     →": return "＋   दूसरा डिवाइस कनेक्ट करें     →";
                case "Browse media": return "मीडिया देखें";
                case "View all": return "सभी देखें";
                case "Photos": return "फ़ोटो";
                case "Images": return "तस्वीरें";
                case "Videos": return "वीडियो";
                case "Movies": return "फ़िल्में";
                case "Audio": return "ऑडियो";
                case "Music & sounds": return "संगीत और आवाज़ें";
                case "Documents": return "दस्तावेज़";
                case "PDF & files": return "PDF और फ़ाइलें";
                case "My folders": return "मेरे फ़ोल्डर";
                case "+ New folder": return "+ नया फ़ोल्डर";
                case "Private collection": return "निजी संग्रह";
                case "Password-protected folder": return "पासवर्ड से सुरक्षित फ़ोल्डर";
                case "Shared with devices": return "डिवाइस के साथ साझा";
                case "Your selected media folders": return "आपके चुने हुए मीडिया फ़ोल्डर";
                case "Recent media": return "हाल का मीडिया";
                case "See all": return "सभी देखें";
                case "Your recent files will appear here": return "आपकी हाल की फ़ाइलें यहाँ दिखेंगी";
                case "Allow media access when the file manager is ready.": return "फ़ाइल मैनेजर तैयार होने पर मीडिया एक्सेस दें।";
                case "Home": return "होम";
                case "Files": return "फ़ाइलें";
                case "Search": return "खोजें";
                case "Devices": return "डिवाइस";
                case "Settings": return "सेटिंग्स";
                case "My Profile": return "मेरी प्रोफ़ाइल";
                case "My Files": return "मेरी फ़ाइलें";
                case "My Folders": return "मेरे फ़ोल्डर";
                case "Password Manager": return "पासवर्ड मैनेजर";
                case "Connected Devices": return "कनेक्टेड डिवाइस";
                case "PREFERENCES": return "प्राथमिकताएँ";
                case "App preferences": return "ऐप की प्राथमिकताएँ";
                case "App Language": return "ऐप की भाषा";
                case "Dark Mode": return "डार्क मोड";
                case "SECURITY & INFORMATION": return "सुरक्षा और जानकारी";
                case "Privacy Policy": return "गोपनीयता नीति";
                case "How your data is handled": return "आपका डेटा कैसे संभाला जाता है";
                case "About KC LMS": return "KC LMS के बारे में";
                case "App information": return "ऐप की जानकारी";
                case "App Version": return "ऐप संस्करण";
                case "Account and user details": return "खाता और उपयोगकर्ता विवरण";
                case "Browse device media": return "डिवाइस मीडिया देखें";
                case "Your private collections": return "आपके निजी संग्रह";
                case "Saved passwords": return "सेव किए गए पासवर्ड";
                case "Manage connections": return "कनेक्शन प्रबंधित करें";
                case "Built for your privacy": return "आपकी गोपनीयता के लिए बनाया गया";
                case "Choose a language": return "भाषा चुनें";
                case "Cancel": return "रद्द करें";
                case "OK": return "ठीक है";
                case "Search KC LMS": return "KC LMS में खोजें";
                case "Type a file, folder or media name": return "फ़ाइल, फ़ोल्डर या मीडिया का नाम लिखें";
                case "No matching items found": return "कोई मिलता-जुलता आइटम नहीं मिला";
                case "Search results": return "खोज परिणाम";
            }
        } else if ("ગુજરાતી".equals(lang)) {
            switch (en) {
                case "PRIVATE MEDIA SPACE": return "ખાનગી મીડિયા સ્પેસ";
                case "Search files, folders...": return "ફાઇલો અને ફોલ્ડર શોધો...";
                case "Your personal space": return "તમારી વ્યક્તિગત જગ્યા";
                case "Your files. Your privacy. Your control.": return "તમારી ફાઇલો. તમારી ગોપનીયતા. તમારું નિયંત્રણ.";
                case "Connect a device": return "ડિવાઇસ જોડો";
                case "Access your media remotely": return "દૂરથી તમારું મીડિયા જુઓ";
                case "＋   Connect another device     →": return "＋   બીજું ડિવાઇસ જોડો     →";
                case "Browse media": return "મીડિયા જુઓ";
                case "View all": return "બધું જુઓ";
                case "Photos": return "ફોટા";
                case "Images": return "છબીઓ";
                case "Videos": return "વિડિયો";
                case "Movies": return "ફિલ્મો";
                case "Audio": return "ઑડિયો";
                case "Music & sounds": return "સંગીત અને અવાજ";
                case "Documents": return "દસ્તાવેજો";
                case "PDF & files": return "PDF અને ફાઇલો";
                case "My folders": return "મારા ફોલ્ડર";
                case "+ New folder": return "+ નવું ફોલ્ડર";
                case "Private collection": return "ખાનગી સંગ્રહ";
                case "Password-protected folder": return "પાસવર્ડથી સુરક્ષિત ફોલ્ડર";
                case "Shared with devices": return "ડિવાઇસ સાથે શેર કરેલું";
                case "Your selected media folders": return "તમારા પસંદ કરેલા મીડિયા ફોલ્ડર";
                case "Recent media": return "તાજેતરનું મીડિયા";
                case "See all": return "બધું જુઓ";
                case "Your recent files will appear here": return "તમારી તાજેતરની ફાઇલો અહીં દેખાશે";
                case "Allow media access when the file manager is ready.": return "ફાઇલ મેનેજર તૈયાર થાય ત્યારે મીડિયા ઍક્સેસ આપો.";
                case "Home": return "હોમ";
                case "Files": return "ફાઇલો";
                case "Search": return "શોધ";
                case "Devices": return "ડિવાઇસ";
                case "Settings": return "સેટિંગ્સ";
                case "My Profile": return "મારી પ્રોફાઇલ";
                case "My Files": return "મારી ફાઇલો";
                case "My Folders": return "મારા ફોલ્ડર";
                case "Password Manager": return "પાસવર્ડ મેનેજર";
                case "Connected Devices": return "જોડાયેલા ડિવાઇસ";
                case "PREFERENCES": return "પસંદગીઓ";
                case "App preferences": return "એપની પસંદગીઓ";
                case "App Language": return "એપની ભાષા";
                case "Dark Mode": return "ડાર્ક મોડ";
                case "SECURITY & INFORMATION": return "સુરક્ષા અને માહિતી";
                case "Privacy Policy": return "ગોપનીયતા નીતિ";
                case "How your data is handled": return "તમારો ડેટા કેવી રીતે સંભાળાય છે";
                case "About KC LMS": return "KC LMS વિશે";
                case "App information": return "એપની માહિતી";
                case "App Version": return "એપનું વર્ઝન";
                case "Account and user details": return "ખાતું અને વપરાશકર્તાની વિગતો";
                case "Browse device media": return "ડિવાઇસનું મીડિયા જુઓ";
                case "Your private collections": return "તમારા ખાનગી સંગ્રહ";
                case "Saved passwords": return "સાચવેલા પાસવર્ડ";
                case "Manage connections": return "કનેક્શન સંચાલિત કરો";
                case "Built for your privacy": return "તમારી ગોપનીયતા માટે બનાવેલું";
                case "Choose a language": return "ભાષા પસંદ કરો";
                case "Cancel": return "રદ કરો";
                case "OK": return "બરાબર";
                case "Search KC LMS": return "KC LMS માં શોધો";
                case "Type a file, folder or media name": return "ફાઇલ, ફોલ્ડર અથવા મીડિયાનું નામ લખો";
                case "No matching items found": return "મેળ ખાતી વસ્તુ મળી નથી";
                case "Search results": return "શોધનાં પરિણામો";
            }
        }
        return en;
    }

    private void showSearch() {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint(t("Type a file, folder or media name"));
        input.setTextColor(ink);
        input.setHintTextColor(muted);

        LinearLayout wrap = column();
        pad(wrap, 20, 8, 20, 0);
        wrap.addView(input);

        new AlertDialog.Builder(this)
            .setTitle(t("Search KC LMS"))
            .setView(wrap)
            .setNegativeButton(t("Cancel"), null)
            .setPositiveButton(t("Search"), (dialog, which) -> {
                String q = input.getText().toString().trim().toLowerCase(java.util.Locale.ROOT);
                if (q.isEmpty()) {
                    toast(t("Type a file, folder or media name"));
                    return;
                }

                String[] names = {
                    t("Photos"), t("Videos"), t("Audio"), t("Documents"),
                    t("Private collection"), t("Shared with devices"),
                    t("My Files"), t("My Folders"), t("Connected Devices")
                };
                StringBuilder matches = new StringBuilder();
                for (String name : names) {
                    if (name.toLowerCase(java.util.Locale.ROOT).contains(q)) {
                        if (matches.length() > 0) matches.append("\n");
                        matches.append("• ").append(name);
                    }
                }

                if (matches.length() == 0) {
                    matches.append(t("No matching items found"));
                }
                new AlertDialog.Builder(this)
                    .setTitle(t("Search results"))
                    .setMessage(matches.toString())
                    .setPositiveButton(t("OK"), null)
                    .show();
            })
            .show();
    }

    private String currentLanguage() {
        return prefs == null ? "English" :
            prefs.getString("language", "English");
    }

    private void chooseLanguage() {
        final String[] languages = {"English", "हिंदी", "ગુજરાતી"};
        new AlertDialog.Builder(this)
            .setTitle(t("Choose a language"))
            .setSingleChoiceItems(languages,
                java.util.Arrays.asList(languages).indexOf(currentLanguage()),
                (dialog, which) -> {
                    String selected = languages[which];
                    prefs.edit().putString("language", selected).apply();
                    dialog.dismiss();
                    toast("Language saved: " + selected);
                    showHome();
                })
            .setNegativeButton(t("Cancel"), null)
            .show();
    }

    private void toggleDarkMode() {
        boolean enabled = !prefs.getBoolean("dark_mode", false);
        prefs.edit().putBoolean("dark_mode", enabled).apply();
        toast("Dark mode: " + (enabled ? "On" : "Off"));
        showHome();
    }

    private void showProfileDialog() {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint("Enter your display name");
        input.setText(prefs.getString("display_name", "KC LMS User"));
        input.setSelection(input.getText().length());

        new AlertDialog.Builder(this)
            .setTitle(t("My Profile"))
            .setMessage("Your name is shown in the navigation drawer.")
            .setView(input)
            .setPositiveButton("Save", (dialog, which) -> {
                String name = input.getText().toString().trim();
                if (name.isEmpty()) {
                    name = "KC LMS User";
                }
                prefs.edit().putString("display_name", name).apply();
                toast("Profile saved");
                showHome();
            })
            .setNegativeButton(t("Cancel"), null)
            .show();
    }

    private void showInfo(String title, String message) {
        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show();
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
