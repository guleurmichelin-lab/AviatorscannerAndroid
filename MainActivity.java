package com.example.aviatorscanner;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {
    private static final int PICK_IMAGE = 41;
    private static final int BG = Color.rgb(9, 9, 9);
    private static final int PANEL = Color.rgb(25, 25, 25);
    private static final int RED = Color.rgb(255, 65, 65);
    private static final int WHITE = Color.WHITE;
    private static final int MUTED = Color.rgb(175, 175, 175);
    private LinearLayout root, historyBox;
    private TextView status, result, extracted;
    private final ArrayList<String> history = new ArrayList<>();

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        buildUi();
    }

    private TextView label(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        t.setPadding(12, 10, 12, 10);
        return t;
    }

    private Button button(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(WHITE);
        b.setAllCaps(false);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(RED));
        return b;
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 18, 20, 24);
        scroll.addView(root);

        TextView brand = label("✈  AVIATOR SCANNER", 25, RED);
        brand.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(brand);
        root.addView(label("SCREENSHOT / IMAGE ANALYZER", 12, MUTED));

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        panel.setBackgroundColor(PANEL);
        panel.setPadding(14, 18, 14, 18);
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(-1, -2);
        pp.setMargins(0, 20, 0, 12);
        root.addView(panel, pp);

        panel.addView(label("RESULTAT SCANNÉ", 13, MUTED));
        result = label("—", 44, WHITE);
        result.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        panel.addView(result);
        status = label("Misafidiana capture écran na sary.", 14, MUTED);
        panel.addView(status);

        Button scan = button("📷  Misafidiana sary / screenshot");
        scan.setOnClickListener(v -> chooseImage());
        root.addView(scan, new LinearLayout.LayoutParams(-1, -2));

        root.addView(label("SORATRA HITA AMIN'NY SARY", 14, RED));
        extracted = label("Ny OCR dia hitady multiplier toy ny 1.24x, 2.50x, 10.00x.", 13, MUTED);
        extracted.setGravity(Gravity.START);
        extracted.setBackgroundColor(PANEL);
        root.addView(extracted, new LinearLayout.LayoutParams(-1, -2));

        root.addView(label("HISTORIQUE", 17, WHITE));
        historyBox = new LinearLayout(this);
        historyBox.setOrientation(LinearLayout.VERTICAL);
        root.addView(historyBox);

        TextView disclaimer = label(
            "Fanamarihana: ity app ity dia mamaky tarehimarika hita amin'ny sary ihany. Tsy maminavina na miantoka ny multiplier manaraka izy.",
            12, MUTED);
        disclaimer.setBackgroundColor(PANEL);
        LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(-1, -2);
        dp.setMargins(0, 22, 0, 0);
        root.addView(disclaimer, dp);
        renderHistory();
        setContentView(scroll);
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Safidio ny screenshot na sary"), PICK_IMAGE);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            status.setText("Mamaky sary...");
            result.setText("…");
            try {
                InputImage image = InputImage.fromFilePath(this, uri);
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                    .process(image)
                    .addOnSuccessListener(this::handleOcr)
                    .addOnFailureListener(e -> {
                        status.setText("Tsy voavaky ilay sary. Andramo sary mazava kokoa.");
                        result.setText("—");
                    });
            } catch (Exception e) {
                status.setText("Tsy afaka nanokatra ilay sary.");
                result.setText("—");
            }
        }
    }

    private void handleOcr(Text visionText) {
        String fullText = visionText.getText();
        extracted.setText(fullText.isEmpty() ? "Tsy nahita soratra ny scanner." : fullText);
        Pattern pattern = Pattern.compile("(?i)(\\d{1,4}(?:[.,]\\d{1,3})?)\\s*[xх]");
        Matcher matcher = pattern.matcher(fullText);
        ArrayList<String> found = new ArrayList<>();
        while (matcher.find()) {
            String number = matcher.group(1).replace(',', '.');
            try {
                double val = Double.parseDouble(number);
                if (val >= 1.0 && val <= 100000.0) {
                    String formatted = String.format(Locale.US, "%.2fx", val);
                    if (!found.contains(formatted)) found.add(formatted);
                }
            } catch (NumberFormatException ignored) {}
        }

        if (found.isEmpty()) {
            result.setText("Tsy hita");
            status.setText("Tsy nahita multiplier mazava. Andramo screenshot hafa.");
            return;
        }
        String latest = found.get(found.size() - 1);
        result.setText(latest);
        status.setText("Multiplier hita: " + found.size() + " • Zahao raha marina ny OCR.");
        for (int i = found.size() - 1; i >= 0; i--) {
            String item = found.get(i);
            if (!history.contains(item)) history.add(0, item);
        }
        if (history.size() > 30) while (history.size() > 30) history.remove(history.size() - 1);
        renderHistory();
    }

    private void renderHistory() {
        historyBox.removeAllViews();
        if (history.isEmpty()) {
            historyBox.addView(label("Tsy mbola misy résultat voatahiry.", 13, MUTED));
            return;
        }
        for (String item : history) {
            TextView row = label("●   " + item, 16, WHITE);
            row.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            row.setBackgroundColor(PANEL);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
            p.setMargins(0, 3, 0, 3);
            historyBox.addView(row, p);
        }
    }
}
