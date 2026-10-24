package com.kclms.app;

import android.app.Activity;
import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.view.MotionEvent;
import android.webkit.MimeTypeMap;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import androidx.core.content.FileProvider;
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
    private String pendingMediaType = "images";
    private String pendingSearchQuery = "";
    private boolean pendingStorageSearch = false;
    private File pendingPrivateFolder;



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


    private float edgeStartX = 0;
    private float edgeStartY = 0;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            edgeStartX = event.getX();
            edgeStartY = event.getY();
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            float dx = event.getX() - edgeStartX;
            float dy = Math.abs(event.getY() - edgeStartY);
            if (edgeStartX <= dp(24) && dx >= dp(90) && dy <= dp(80)
                    && drawer != null && frame != null) {
                openDrawer();
                return true;
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void showFilesMenu() {
        String[] options = {"Photos", "Videos", "Audio", "Documents", "Search files"};
        new AlertDialog.Builder(this)
            .setTitle(t("Files"))
            .setItems(options, (dialog, which) -> {
                if (which == 0) openMediaBrowser("images");
                else if (which == 1) openMediaBrowser("videos");
                else if (which == 2) openMediaBrowser("audio");
                else if (which == 3) openMediaBrowser("documents");
                else showSearch();
            })
            .setNegativeButton(t("Cancel"), null)
            .show();
    }

    private void showSettings() {
        String[] options = {"My Profile", "App Language", "Dark Mode", "About KC LMS"};
        new AlertDialog.Builder(this)
            .setTitle(t("Settings"))
            .setItems(options, (dialog, which) -> {
                if (which == 0) showProfileDialog();
                else if (which == 1) chooseLanguage();
                else if (which == 2) toggleDarkMode();
                else showInfo(t("About KC LMS"), "KC LMS — Version 1.0");
            })
            .setNegativeButton(t("Cancel"), null)
            .show();
    }

    private void createPrivateFolder() {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint("Folder name");
        new AlertDialog.Builder(this)
            .setTitle("Create private folder")
            .setMessage("This folder is stored in KC LMS app-private storage.")
            .setView(input)
            .setNegativeButton(t("Cancel"), null)
            .setPositiveButton("Create", (dialog, which) -> {
                String name = input.getText().toString().trim();
                if (name.isEmpty() || name.equals(".") || name.equals("..")
                        || name.contains("/") || name.contains("\\")) {
                    toast("Enter a valid folder name.");
                    return;
                }
                File base = new File(getFilesDir(), "collections");
                File folder = new File(base, name);
                if ((!base.exists() && !base.mkdirs())
                        || (!folder.exists() && !folder.mkdir())) {
                    toast("Could not create folder.");
                    return;
                }
                toast("Private folder created: " + name);
                showPrivateCollections();
            })
            .show();
    }

    private void showPrivateCollections() {
    File base = new File(getFilesDir(), "collections");
    if (!base.exists() && !base.mkdirs()) {
        toast("Could not open private folders.");
        return;
    }
    File[] folders = base.listFiles(File::isDirectory);
    if (folders == null) {
        toast("Could not read private folders.");
        return;
    }
    java.util.Arrays.sort(folders,
        (a, b) -> a.getName().compareToIgnoreCase(b.getName()));

    String[] names = new String[folders.length + 1];
    names[0] = "+ New folder";
    for (int i = 0; i < folders.length; i++) {
        names[i + 1] = folders[i].getName();
    }

    new AlertDialog.Builder(this)
        .setTitle("Private folders")
        .setItems(names, (dialog, which) -> {
            if (which == 0) {
                createPrivateFolder();
            } else {
                showPrivateFolderContents(folders[which - 1]);
            }
        })
        .setNegativeButton(t("Cancel"), null)
        .show();
}

private void showPrivateFolderContents(File folder) {
    File base = new File(getFilesDir(), "collections");
    try {
        String root = base.getCanonicalPath() + File.separator;
        String selected = folder.getCanonicalPath();
        if (!selected.startsWith(root) || !folder.isDirectory()) {
            toast("Invalid private folder.");
            return;
        }
    } catch (java.io.IOException e) {
        toast("Could not open private folder.");
        return;
    }

    File[] contents = folder.listFiles();
    if (contents == null) {
        toast("Could not read this folder.");
        return;
    }
    java.util.Arrays.sort(contents, (a, b) -> {
        if (a.isDirectory() != b.isDirectory()) {
            return a.isDirectory() ? -1 : 1;
        }
        return a.getName().compareToIgnoreCase(b.getName());
    });

    String[] labels = new String[contents.length + 1];
    labels[0] = "+ Add file";
    for (int i = 0; i < contents.length; i++) {
        labels[i + 1] = (contents[i].isDirectory() ? "[Folder] " : "[File] ")
            + contents[i].getName();
    }

    new AlertDialog.Builder(this)
        .setTitle(folder.getName())
        .setItems(labels, (dialog, which) -> {
            if (which == 0) {
                pendingPrivateFolder = folder;
                Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                pick.setType("*/*");
                pick.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    startActivityForResult(pick, 414);
                } catch (Exception e) {
                    pendingPrivateFolder = null;
                    toast("Unable to open the document picker.");
                }
            } else {
                File chosen = contents[which - 1];
                if (chosen.isDirectory()) {
                    showPrivateFolderContents(chosen);
                } else {
                    openPrivateCollectionFile(chosen);
                }
            }
        })
        .setNegativeButton(t("Cancel"), null)
        .show();
}

private void openPrivateCollectionFile(File file) {
    if (file == null || !file.isFile()) {
        toast("File is no longer available.");
        return;
    }
    try {
        Uri uri = FileProvider.getUriForFile(
            this, getPackageName() + ".fileprovider", file);
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        String ext = dot >= 0
            ? name.substring(dot + 1).toLowerCase(java.util.Locale.ROOT) : "";
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
        if (mime == null) mime = "*/*";

        Intent view = new Intent(Intent.ACTION_VIEW);
        view.setDataAndType(uri, mime);
        view.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(view);
    } catch (Exception e) {
        toast("No compatible app can open this file.");
    }
}

private String safeImportedFileName(String name) {
    if (name == null || name.trim().isEmpty()) name = "Imported file";
    name = name.replace('\\', '/');
    int slash = name.lastIndexOf('/');
    if (slash >= 0) name = name.substring(slash + 1);
    name = name.replaceAll("[\\p{Cntrl}]", "_").trim();
    if (name.isEmpty() || name.equals(".") || name.equals("..")) {
        name = "Imported file";
    }
    return name;
}

private File uniqueDestination(File folder, String name) {
    File destination = new File(folder, name);
    if (!destination.exists()) return destination;

    int dot = name.lastIndexOf('.');
    String stem = dot > 0 ? name.substring(0, dot) : name;
    String extension = dot > 0 ? name.substring(dot) : "";
    for (int i = 2; i < 10000; i++) {
        destination = new File(folder, stem + " (" + i + ")" + extension);
        if (!destination.exists()) return destination;
    }
    return null;
}

private void importPrivateDocument(Uri uri, File folder) {
    if (uri == null || folder == null || !folder.isDirectory()) {
        toast("Choose a valid private folder first.");
        return;
    }

    String name = null;
    try (Cursor cursor = getContentResolver().query(
            uri,
            new String[]{android.provider.OpenableColumns.DISPLAY_NAME},
            null, null, null)) {
        if (cursor != null && cursor.moveToFirst()) {
            int column = cursor.getColumnIndex(
                android.provider.OpenableColumns.DISPLAY_NAME);
            if (column >= 0) name = cursor.getString(column);
        }
    } catch (Exception ignored) {
        // Fall back to a safe generic name if provider metadata is unavailable.
    }

    name = safeImportedFileName(name);
    File destination = uniqueDestination(folder, name);
    if (destination == null) {
        toast("Could not choose a unique file name.");
        return;
    }

    try (java.io.InputStream input = getContentResolver().openInputStream(uri);
         java.io.FileOutputStream output =
             new java.io.FileOutputStream(destination)) {
        if (input == null) throw new java.io.IOException("No input stream");
        byte[] buffer = new byte[8192];
        int count;
        while ((count = input.read(buffer)) != -1) {
            output.write(buffer, 0, count);
        }
        output.flush();
        toast("File saved in private folder.");
        showPrivateFolderContents(folder);
    } catch (Exception e) {
        // Remove a partial copy so it is not mistaken for a complete import.
        if (destination.exists()) destination.delete();
        toast("Could not import this file.");
    }
}

private void shareSelectedDocument(Uri uri) {
    if (uri == null) {
        toast("No file selected.");
        return;
    }
    try {
        String mime = getContentResolver().getType(uri);
        if (mime == null) mime = "*/*";
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType(mime);
        send.putExtra(Intent.EXTRA_STREAM, uri);
        send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(send, "Share file"));
    } catch (Exception e) {
        toast("Unable to share this file.");
    }
}

private void openConnectionSettings(String action) {
    try {
        startActivity(new Intent(action));
    } catch (Exception e) {
        try {
            startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
        } catch (Exception ignored) {
            toast("Connection settings are not available on this device.");
        }
    }
}

private void showConnectedDevices() {
    String[] choices = {
        "Bluetooth settings",
        "Wi-Fi settings",
        "Wireless and network settings",
        "Share a file"
    };
    new AlertDialog.Builder(this)
        .setTitle(t("Connected Devices"))
        .setMessage("Manage connections in Android settings, or choose a file to share with a compatible app or nearby-device service.")
        .setItems(choices, (dialog, which) -> {
            if (which == 0) {
                openConnectionSettings(Settings.ACTION_BLUETOOTH_SETTINGS);
            } else if (which == 1) {
                openConnectionSettings(Settings.ACTION_WIFI_SETTINGS);
            } else if (which == 2) {
                openConnectionSettings(Settings.ACTION_WIRELESS_SETTINGS);
            } else {
                Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                pick.setType("*/*");
                pick.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    startActivityForResult(pick, 415);
                } catch (Exception e) {
                    toast("Unable to open the document picker.");
                }
            }
        })
        .setNegativeButton(t("Cancel"), null)
        .show();
}


    private void showRecentMedia() {
        if (Build.VERSION.SDK_INT >= 33) {
            String[] required = Build.VERSION.SDK_INT >= 34
                ? new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                }
                : new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
                };
            java.util.ArrayList<String> missing = new java.util.ArrayList<>();
            for (String permission : required) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    missing.add(permission);
                }
            }
            if (!missing.isEmpty()) {
                pendingMediaType = "recent";
                requestPermissions(missing.toArray(new String[0]), 413);
                return;
            }
        } else if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            pendingMediaType = "recent";
            requestPermissions(
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 413);
            return;
        }
        queryRecentMedia();
    }

    private void queryRecentMedia() {
        ArrayList<String> names = new ArrayList<>();
        ArrayList<Uri> uris = new ArrayList<>();
        ArrayList<String> mimes = new ArrayList<>();
        Uri collection = MediaStore.Files.getContentUri("external");
        String[] columns = {
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.MEDIA_TYPE
        };
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + " IN (?,?,?)";
        String[] args = {
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO)
        };
        try (Cursor c = getContentResolver().query(
                collection, columns, selection, args,
                MediaStore.MediaColumns.DATE_ADDED + " DESC")) {
            if (c != null) {
                int idCol = c.getColumnIndexOrThrow(MediaStore.MediaColumns._ID);
                int nameCol = c.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
                int mimeCol = c.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE);
                while (c.moveToNext() && names.size() < 100) {
                    long id = c.getLong(idCol);
                    String name = c.getString(nameCol);
                    names.add(name == null ? "Media file" : name);
                    uris.add(ContentUris.withAppendedId(collection, id));
                    String mime = c.getString(mimeCol);
                    mimes.add(mime == null ? "*/*" : mime);
                }
            }
        } catch (Exception e) {
            toast("Could not read recent media. Check media permissions.");
            return;
        }
        if (names.isEmpty()) {
            showInfo(t("Recent media"), "No accessible recent media was found.");
            return;
        }
        new AlertDialog.Builder(this)
            .setTitle(t("Recent media"))
            .setItems(names.toArray(new String[0]), (dialog, which) -> {
                Intent view = new Intent(Intent.ACTION_VIEW);
                view.setDataAndType(uris.get(which), mimes.get(which));
                view.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try {
                    startActivity(view);
                } catch (Exception e) {
                    toast("No compatible app can open this media.");
                }
            })
            .setNegativeButton(t("Cancel"), null)
            .show();
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
        pad(root, 16, 10, 16, 20);
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
        names.addView(label("KC LMS", 21, ink, true));
        names.addView(label(t("LIVE MEDIA SPACE"), 9, muted, true));
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
        search.setElevation(dp(2));
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
        search.setOnClickListener(v -> showSearch());
        gap(root, 20);
    }

    private void buildWelcome() {
        root.addView(label(t("Your personal space"), 25, ink, true));
        gap(root, 4);
        root.addView(label(
            t("Your files. Your privacy. Your control."),
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
        "Open Bluetooth, Wi-Fi or share a file",
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
    button.setOnClickListener(v -> showConnectedDevices());

    gap(root, 24);
    root.addView(card);
    gap(root, 25);
}


    private void sectionHeading(String title, String action,
                                Runnable callback) {
        LinearLayout r = row();
        r.addView(label(title, 19, ink, true),
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
            openMediaBrowser("images")
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

        card.setOnClickListener(v -> {
            if (title.equals(t("Photos"))) {
                openMediaBrowser("images");
            } else if (title.equals(t("Videos"))) {
                openMediaBrowser("videos");
            } else if (title.equals(t("Audio"))) {
                openMediaBrowser("audio");
            } else if (title.equals(t("Documents"))) {
                openMediaBrowser("documents");
            }
        });
    }

    private void buildFoldersSection() {
        sectionHeading(t("My folders"), "+ New folder", () -> createPrivateFolder());

        LinearLayout card = column();
        pad(card, 15, 3, 15, 3);
        card.setBackground(shape(surface, 19));

        addFolderRow(card, "▰", t("Private collection"),
            "Stored privately inside KC LMS", blue);
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

        r.setOnClickListener(v -> {
            if (title.equals(t("Private collection"))) {
                showPrivateCollections();
            } else {
                showConnectedDevices();
            }
        });
    }


    private void buildRecentSection() {
        sectionHeading(t("Recent media"), "See all", () -> showRecentMedia());
        LinearLayout card = column();
        pad(card, 15, 14, 15, 14);
        card.setBackground(shape(surface, 19));
        TextView title = label("Recently added photos, videos and audio", 13, ink, true);
        card.addView(title);
        gap(card, 5);
        TextView subtitle = label("Tap to view the latest media on this device.", 11, muted, false);
        card.addView(subtitle);
        card.setOnClickListener(v -> showRecentMedia());
        root.addView(card);
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
        TextView symbol = label(icon, 22, selected ? blue : muted, true);
        symbol.setGravity(Gravity.CENTER);
        item.addView(symbol);
        TextView caption = label(title, 10, selected ? blue : muted, selected);
        caption.setGravity(Gravity.CENTER);
        item.addView(caption);
        bottomNav.addView(item, new LinearLayout.LayoutParams(0, -1, 1));
        item.setOnClickListener(v -> {
            if (title.equals(t("Search"))) {
                showSearch();
            } else if (title.equals(t("Files"))) {
                showFilesMenu();
            } else if (title.equals(t("Devices"))) {
                showConnectedDevices();
            } else if (title.equals(t("Settings"))) {
                showSettings();
            } else {
                showHome();
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

        LinearLayout brand = row();
        pad(brand, 16, 22, 16, 22);
        brand.setBackground(gradient(navy, Color.rgb(39, 66, 129), 0));

        ImageView brandLogo = new ImageView(this);
        brandLogo.setImageResource(R.drawable.kc_lms_logo);
        brandLogo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams logoParams = lp(58, 58);
        logoParams.rightMargin = dp(12);
        brand.addView(brandLogo, logoParams);

        LinearLayout profileDetails = column();
        profileDetails.setGravity(Gravity.CENTER_VERTICAL);
        String displayName = prefs.getString("display_name", "KC LMS User");

        TextView profileName = label(displayName, 18, Color.WHITE, true);
        profileName.setMaxLines(2);
        profileName.setEllipsize(android.text.TextUtils.TruncateAt.END);
        profileDetails.addView(profileName);

        LinearLayout userIdRow = row();
        TextView userIdText = label("ID: " + getOrCreateUserId(),
            11, Color.rgb(50, 205, 110), true);
        userIdText.setSingleLine(true);
        userIdText.setEllipsize(android.text.TextUtils.TruncateAt.END);
        userIdRow.addView(userIdText,
            new LinearLayout.LayoutParams(0, -2, 1));

        TextView copyIcon = label("⧉", 19, Color.WHITE, true);
        copyIcon.setGravity(Gravity.CENTER);
        copyIcon.setContentDescription("Copy User ID");
        copyIcon.setOnClickListener(v -> {
            String id = getOrCreateUserId();
            android.content.ClipboardManager clipboard =
                (android.content.ClipboardManager)
                getSystemService(CLIPBOARD_SERVICE);
            if (clipboard != null) {
                clipboard.setPrimaryClip(
                    android.content.ClipData.newPlainText("KC LMS User ID", id));
                toast("User ID copied");
            } else {
                toast("Could not copy User ID");
            }
        });
        userIdRow.addView(copyIcon, lp(30, 30));
        profileDetails.addView(userIdRow);

        TextView brandSubtitle = label("LIVE MEDIA SPACE", 10,
            Color.rgb(210, 220, 245), true);
        brandSubtitle.setMaxLines(1);
        brandSubtitle.setEllipsize(android.text.TextUtils.TruncateAt.END);
        profileDetails.addView(brandSubtitle);

        brand.addView(profileDetails,
            new LinearLayout.LayoutParams(0, -2, 1));

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
        modeSwitch.setShowText(false);
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
                    "KC LMS media organizer with private folders and system connection shortcuts.");
            } else if (title.equals(t("My Files"))) {
                showFilesMenu();
            } else if (title.equals(t("My Folders"))) {
                showPrivateCollections();
            } else if (title.equals(t("Connected Devices"))) {
                showConnectedDevices();
            } else if (title.equals("Settings")) {
                showSettings();
            } else {
                toast(title + " is not available yet.");
            }
        });
    }

    private String t(String en) {
        String lang = currentLanguage();
        if ("हिंदी".equals(lang)) {
            switch (en) {
                case "LIVE MEDIA SPACE": return "लाइव मीडिया स्पेस";
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
                case "Stored privately inside KC LMS": return "KC LMS के निजी स्टोरेज में सहेजा गया";
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
                case "LIVE MEDIA SPACE": return "લાઇવ મીડિયા સ્પેસ";
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
                case "Stored privately inside KC LMS": return "KC LMS ના ખાનગી સ્ટોરેજમાં સંગ્રહિત";
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
            .setMessage("Searches shared-storage filenames, folders, photos, videos and audio. Some protected Android folders may be unavailable.")
            .setView(wrap)
            .setNegativeButton(t("Cancel"), null)
            .setPositiveButton(t("Search"), (dialog, which) -> {
                String q = input.getText().toString().trim();
                if (q.isEmpty()) {
                    toast(t("Type a file, folder or media name"));
                    return;
                }
                startMediaSearch(q);
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


    private void startMediaSearch(String query) {
        pendingSearchQuery = query;
        if (Build.VERSION.SDK_INT >= 33) {
            String[] permissions = {
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            };
            java.util.ArrayList<String> missing = new java.util.ArrayList<>();
            for (String permission : permissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    missing.add(permission);
                }
            }
            if (Build.VERSION.SDK_INT >= 34
                    && (missing.contains(Manifest.permission.READ_MEDIA_IMAGES)
                        || missing.contains(Manifest.permission.READ_MEDIA_VIDEO))
                    && checkSelfPermission(
                        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                        != PackageManager.PERMISSION_GRANTED) {
                missing.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED);
            }
            if (!missing.isEmpty()) {
                if (Build.VERSION.SDK_INT >= 30 && Environment.isExternalStorageManager()) {
                    searchDeviceMedia(query);
                    return;
                }
                requestPermissions(missing.toArray(new String[0]), 412);
                return;
            }
        } else if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= 30 && Environment.isExternalStorageManager()) {
                searchDeviceMedia(query);
                return;
            }
            requestPermissions(
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 412);
            return;
        }
        searchDeviceMedia(query);
    }

    private void searchDeviceMedia(String query) {
        String q = query.toLowerCase(java.util.Locale.ROOT);
        java.util.ArrayList<String> names = new java.util.ArrayList<>();
        java.util.ArrayList<Uri> uris = new java.util.ArrayList<>();
        java.util.ArrayList<String> mimeTypes = new java.util.ArrayList<>();
        java.util.ArrayList<File> localFiles = new java.util.ArrayList<>();
        Uri[] sources = {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        };
        String[] types = {"image/*", "video/*", "audio/*"};
        String[] permissions = {
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO
        };

        for (int i = 0; i < sources.length; i++) {
            if (Build.VERSION.SDK_INT >= 33) {
                boolean hasMediaPermission =
                    checkSelfPermission(permissions[i]) == PackageManager.PERMISSION_GRANTED;
                if (!hasMediaPermission && Build.VERSION.SDK_INT >= 34 && i < 2) {
                    hasMediaPermission = checkSelfPermission(
                        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                        == PackageManager.PERMISSION_GRANTED;
                }
                if (!hasMediaPermission) continue;
            } else if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                continue;
            }

            Uri source = sources[i];
            String[] columns = {
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME
            };
            try (Cursor c = getContentResolver().query(
                    source, columns, null, null,
                    MediaStore.MediaColumns.DATE_ADDED + " DESC")) {
                if (c == null) continue;
                int idCol = c.getColumnIndexOrThrow(MediaStore.MediaColumns._ID);
                int nameCol = c.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
                while (c.moveToNext() && names.size() < 200) {
                    String name = c.getString(nameCol);
                    if (name != null &&
                            name.toLowerCase(java.util.Locale.ROOT).contains(q)) {
                        names.add(name);
                        uris.add(ContentUris.withAppendedId(source, c.getLong(idCol)));
                                    localFiles.add(null);
                        mimeTypes.add(types[i]);
                    }
                }
            } catch (Exception e) {
                android.util.Log.w("KCLMS", "Media search source failed", e);
            }
            if (names.size() >= 200) break;
        }

        if (Build.VERSION.SDK_INT < 30 || Environment.isExternalStorageManager()) {
            for (File f : UniversalFileSearch.search(query, 100)) {
                if (names.size() >= 200) break;
                names.add((f.isDirectory() ? "[Folder] " : "[File] ") + f.getName());
                uris.add(null);
                localFiles.add(f);
                mimeTypes.add(null);
            }
        }
        if (names.isEmpty()) {
            AlertDialog.Builder noResults = new AlertDialog.Builder(this)
                    .setTitle("Search results")
                    .setMessage("No matching files found. Check permissions or try another filename.")
                    .setPositiveButton("OK", null);
            if (Build.VERSION.SDK_INT >= 30 && !Environment.isExternalStorageManager()) {
                noResults.setNeutralButton("Enable file search", (d, w) -> requestAllFilesAccess());
            }
            noResults.show();
            return;
        }

        AlertDialog.Builder resultsDialog = new AlertDialog.Builder(this)
                .setTitle("Search results (" + names.size() + ")")
                .setItems(names.toArray(new String[0]), (dialog, which) -> {
                    if (localFiles.get(which) != null) { openSearchFile(localFiles.get(which)); return; }
                    Intent view = new Intent(Intent.ACTION_VIEW);
                    view.setDataAndType(uris.get(which), mimeTypes.get(which));
                    view.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    try { startActivity(view); }
                    catch (Exception e) { toast("No app can open this file."); }
                })
                .setNegativeButton("Close", null);
        if (Build.VERSION.SDK_INT >= 30 && !Environment.isExternalStorageManager()) {
            resultsDialog.setNeutralButton("Enable file search", (d, w) -> requestAllFilesAccess());
        }
        resultsDialog.show();
    }

    private void openSearchFile(File file) {
        if (file == null || !file.exists()) { toast("File is no longer available."); return; }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children == null) { toast("Cannot access this folder."); return; }
            java.util.Arrays.sort(children, (x, y) -> Boolean.compare(y.isDirectory(), x.isDirectory()));
            int count = Math.min(children.length, 150);
            String[] labels = new String[count];
            for (int i = 0; i < count; i++) labels[i] = (children[i].isDirectory() ? "[Folder] " : "[File] ") + children[i].getName();
            new AlertDialog.Builder(this).setTitle(file.getName()).setItems(labels, (dialog, which) -> openSearchFile(children[which])).setNegativeButton("Close", null).show();
            return;
        }
        try {
            String name = file.getName();
            int dot = name.lastIndexOf(".");
            String ext = dot >= 0 ? name.substring(dot + 1).toLowerCase(java.util.Locale.ROOT) : "";
            String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
            if (mime == null) mime = "*/*";
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
            Intent view = new Intent(Intent.ACTION_VIEW);
            view.setDataAndType(uri, mime);
            view.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(view);
        } catch (Exception e) { toast("No compatible app can open this file."); }
    }

    private void requestAllFilesAccess() {
        if (Build.VERSION.SDK_INT < 30) return;
        pendingStorageSearch = true;
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception first) {
            try { startActivity(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)); }
            catch (Exception second) { pendingStorageSearch = false; toast("Unable to open All files access settings."); }
        }
    }

    private void openMediaBrowser(String type) {
        if ("documents".equals(type)) {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.setType("*/*");
            i.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(i, 410);
            return;
        }

        String permission;
        if (Build.VERSION.SDK_INT >= 33) {
            permission = "audio".equals(type)
                ? Manifest.permission.READ_MEDIA_AUDIO
                : ("videos".equals(type)
                    ? Manifest.permission.READ_MEDIA_VIDEO
                    : Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        boolean hasPermission =
            checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        boolean visualType = !"audio".equals(type);
        if (!hasPermission && Build.VERSION.SDK_INT >= 34 && visualType) {
            hasPermission = checkSelfPermission(
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                == PackageManager.PERMISSION_GRANTED;
        }
        if (!hasPermission) {
            pendingMediaType = type;
            if (Build.VERSION.SDK_INT >= 34 && visualType) {
                requestPermissions(new String[]{
                    permission,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                }, 411);
            } else {
                requestPermissions(new String[]{permission}, 411);
            }
            return;
        }
        showMediaList(type);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 414) {
            File targetFolder = pendingPrivateFolder;
            pendingPrivateFolder = null;
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                importPrivateDocument(data.getData(), targetFolder);
            }
        } else if (requestCode == 415) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                shareSelectedDocument(data.getData());
            }
        } else if (requestCode == 410) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                shareSelectedDocument(data.getData());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pendingStorageSearch) {
            pendingStorageSearch = false;
            if (Build.VERSION.SDK_INT >= 30 && !Environment.isExternalStorageManager()) toast("All files access not enabled; media search is still available.");
            if (!pendingSearchQuery.isEmpty()) startMediaSearch(pendingSearchQuery);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == 413) {
            boolean granted = false;
            for (int result : results) {
                if (result == PackageManager.PERMISSION_GRANTED) {
                    granted = true;
                    break;
                }
            }
            if (granted) queryRecentMedia();
            else toast("Media permission was not granted.");
        } else if (requestCode == 412) {
            boolean granted = false;
            for (int result : results) {
                if (result == PackageManager.PERMISSION_GRANTED) {
                    granted = true;
                    break;
                }
            }
            if (!granted) {
                toast("Media permission was not granted; showing other accessible files.");
            }
            searchDeviceMedia(pendingSearchQuery);
        } else if (requestCode == 411) {
            boolean granted = false;
            for (int result : results) {
                if (result == PackageManager.PERMISSION_GRANTED) {
                    granted = true;
                    break;
                }
            }
            if (granted) showMediaList(pendingMediaType);
            else toast("Media permission was not granted.");
        }
    }

    private void showMediaList(String type) {
        Uri collection;
        String mime;
        if ("videos".equals(type)) {
            collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            mime = "video/*";
        } else if ("audio".equals(type)) {
            collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            mime = "audio/*";
        } else {
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            mime = "image/*";
        }

        java.util.ArrayList<String> names = new java.util.ArrayList<>();
        java.util.ArrayList<Uri> uris = new java.util.ArrayList<>();
        String[] columns = {
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME
        };

        try (Cursor c = getContentResolver().query(
                collection, columns, null, null,
                MediaStore.MediaColumns.DATE_ADDED + " DESC")) {
            if (c != null) {
                int idCol = c.getColumnIndexOrThrow(
                    MediaStore.MediaColumns._ID);
                int nameCol = c.getColumnIndexOrThrow(
                    MediaStore.MediaColumns.DISPLAY_NAME);
                while (c.moveToNext() && names.size() < 100) {
                    long id = c.getLong(idCol);
                    String name = c.getString(nameCol);
                    names.add(name == null ? "Media file" : name);
                    uris.add(ContentUris.withAppendedId(collection, id));
                }
            }
        } catch (Exception e) {
            toast("Could not read media: " + e.getMessage());
            return;
        }

        if (names.isEmpty()) {
            showInfo("Media", "No matching files were found on this device.");
            return;
        }

        new AlertDialog.Builder(this)
            .setTitle("audio".equals(type) ? "Audio"
                : ("videos".equals(type) ? "Videos" : "Photos"))
            .setItems(names.toArray(new String[0]), (dialog, which) -> {
                Intent view = new Intent(Intent.ACTION_VIEW);
                view.setDataAndType(uris.get(which), mime);
                view.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try {
                    startActivity(view);
                } catch (Exception e) {
                    toast("No app can open this file.");
                }
            })
            .setNegativeButton("Close", null)
            .show();
    }

}
