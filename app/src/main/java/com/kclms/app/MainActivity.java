package com.kclms.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private final int navy = Color.rgb(16, 24, 47);
    private final int blue = Color.rgb(65, 105, 225);
    private final int background = Color.rgb(245, 247, 252);
    private LinearLayout content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showHome();
    }

    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        return d;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER_VERTICAL);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private void showHome() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(background);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(20, 24, 20, 28);
        scroll.addView(content);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(20, 24, 20, 24);
        header.setBackground(shape(navy, 28));

        TextView brand = text("KC LMS", 28, Color.WHITE, true);
        header.addView(brand);

        TextView subtitle = text(
            "Your private media space",
            15, Color.rgb(205, 216, 245), false
        );
        header.addView(subtitle);
        addGap(content, 18);
        content.addView(header);

        addGap(content, 22);
        content.addView(text("Your Media", 22, navy, true));
        addGap(content, 10);

        addCard("📁", "My Files", "Browse files on this device",
            "फाइल मैनेजर अगले चरण में जोड़ा जाएगा।");
        addCard("🗂️", "New Folder", "Create a private media folder",
            "नया फोल्डर बनाने की सुविधा अगले चरण में आएगी।");
        addCard("🔐", "Password Manager", "Manage folder and file passwords",
            "सुरक्षित पासवर्ड सिस्टम अगले चरण में आएगा।");
        addCard("📱", "Connect Device", "Connect another KC LMS device",
            "P2P कनेक्शन अगले चरण में बनाया जाएगा।");
        addCard("⚙️", "Settings", "Security and app preferences",
            "ऐप सेटिंग अगले चरण में जोड़ी जाएगी।");

        addGap(content, 20);
        TextView footer = text(
            "KC LMS  •  Private Media Access",
            12, Color.GRAY, false
        );
        footer.setGravity(Gravity.CENTER);
        content.addView(footer);

        setContentView(scroll);
    }

    private void addGap(LinearLayout parent, int height) {
        View gap = new View(this);
        parent.addView(gap, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, height
        ));
    }

    private void addCard(String icon, String title,
                         String description, String message) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(18, 16, 18, 16);
        card.setBackground(shape(Color.WHITE, 22));
        card.setElevation(2);

        TextView heading = text(
            icon + "   " + title, 18, navy, true
        );
        card.addView(heading);

        TextView detail = text(description, 13,
            Color.rgb(95, 103, 122), false);
        LinearLayout.LayoutParams detailParams =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
        detailParams.topMargin = 7;
        card.addView(detail, detailParams);

        Button button = new Button(this);
        button.setText("Open");
        button.setTextColor(blue);
        button.setAllCaps(false);
        button.setOnClickListener(v ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        );

        LinearLayout.LayoutParams buttonParams =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
        buttonParams.topMargin = 8;
        card.addView(button, buttonParams);

        content.addView(card, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        addGap(content, 12);
    }
}
