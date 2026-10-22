package com.volox.ai;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.*;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {
    private LinearLayout messages;
    private EditText serverInput, questionInput;
    private ScrollView scroll;
    private Button sendButton;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(11, 18, 38));
        getWindow().setNavigationBarColor(Color.rgb(11, 18, 38));
        buildUi();
    }

    private int dp(float value) {
        return (int)(value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(14), dp(16), dp(12));
        root.setBackgroundColor(Color.rgb(11, 18, 38));
        setContentView(root);

        TextView title = text("VoloX AI", 28, Color.WHITE, true);
        root.addView(title);
        TextView subtitle = text("Version 5.8  •  Your AI assistant", 13,
                Color.rgb(166, 190, 230), false);
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(-1, -2);
        subParams.bottomMargin = dp(12);
        root.addView(subtitle, subParams);

        TextView serverLabel = text("Backend HTTPS URL", 12,
                Color.rgb(190, 205, 230), true);
        root.addView(serverLabel);
        serverInput = new EditText(this);
        serverInput.setSingleLine(true);
        serverInput.setTextColor(Color.WHITE);
        serverInput.setHintTextColor(Color.GRAY);
        serverInput.setHint("https://your-secure-server.example");
        serverInput.setText(getPreferences(MODE_PRIVATE).getString("server_url", ""));
        serverInput.setTextSize(14);
        serverInput.setPadding(dp(12), 0, dp(12), 0);
        serverInput.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                Color.rgb(90, 145, 255)));
        root.addView(serverInput, new LinearLayout.LayoutParams(-1, dp(48)));

        TextView help = text("Enter your deployed API address (no trailing slash). "
                + "A local Colab URL won't work from your phone.", 11,
                Color.rgb(166, 190, 210), false);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(-1, -2);
        hp.topMargin = dp(4);
        hp.bottomMargin = dp(10);
        root.addView(help, hp);

        scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        messages = new LinearLayout(this);
        messages.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(messages);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));
        addMessage("VoloX", "Hey bro! 👋 I'm VoloX AI 5.8. Add your secure backend URL above to start chatting.", false);

        questionInput = new EditText(this);
        questionInput.setHint("Ask VoloX anything...");
        questionInput.setTextSize(15);
        questionInput.setSingleLine(false);
        questionInput.setMinLines(1);
        questionInput.setMaxLines(4);
        questionInput.setTextColor(Color.WHITE);
        questionInput.setHintTextColor(Color.GRAY);
        questionInput.setPadding(dp(12), dp(8), dp(12), dp(8));
        questionInput.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                Color.rgb(90, 145, 255)));

        LinearLayout.LayoutParams qp = new LinearLayout.LayoutParams(-1, -2);
        qp.topMargin = dp(10);
        root.addView(questionInput, qp);

        sendButton = new Button(this);
        sendButton.setText("Send to VoloX  ➤");
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, dp(48));
        bp.topMargin = dp(8);
        root.addView(sendButton, bp);
        sendButton.setOnClickListener(v -> sendQuestion());
    }

    private void addMessage(String who, String body, boolean user) {
        TextView bubble = text(who + "\n" + body, 14,
                Color.WHITE, false);
        bubble.setPadding(dp(12), dp(10), dp(12), dp(10));
        bubble.setBackgroundColor(user ? Color.rgb(35, 74, 140) : Color.rgb(28, 39, 65));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.topMargin = dp(6);
        messages.addView(bubble, p);
        scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
    }

    private void sendQuestion() {
        String question = questionInput.getText().toString().trim();
        String base = serverInput.getText().toString().trim();
        if (question.isEmpty()) {
            Toast.makeText(this, "Type a question first, bro.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!base.startsWith("https://")) {
            Toast.makeText(this, "Enter a valid HTTPS backend URL first.", Toast.LENGTH_LONG).show();
            return;
        }
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        getPreferences(MODE_PRIVATE).edit().putString("server_url", base).apply();
        addMessage("You", question, true);
        questionInput.setText("");
        sendButton.setEnabled(false);
        sendButton.setText("Thinking...");
        final String api = base + "/api/chat";
        final String msg = question;
        new Thread(() -> {
            String result;
            try {
                HttpURLConnection c = (HttpURLConnection) new URL(api).openConnection();
                c.setRequestMethod("POST");
                c.setConnectTimeout(15000);
                c.setReadTimeout(120000);
                c.setDoOutput(true);
                c.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                JSONObject payload = new JSONObject();
                payload.put("message", msg);
                try (OutputStream out = c.getOutputStream()) {
                    out.write(payload.toString().getBytes("UTF-8"));
                }
                int code = c.getResponseCode();
                InputStream stream = (code >= 200 && code < 400)
                        ? c.getInputStream() : c.getErrorStream();
                StringBuilder sb = new StringBuilder();
                if (stream != null) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"))) {
                        String line;
                        while ((line = br.readLine()) != null) sb.append(line);
                    }
                }
                if (code >= 200 && code < 300) {
                    JSONObject json = new JSONObject(sb.toString());
                    result = json.optString("answer", "The server returned no answer.");
                } else {
                    result = "Server error HTTP " + code + ": " + sb;
                }
                c.disconnect();
            } catch (Exception e) {
                result = "Couldn't reach VoloX server. Check the HTTPS URL and make sure the backend is running.";
            }
            final String reply = result;
            runOnUiThread(() -> {
                addMessage("VoloX AI", reply, false);
                sendButton.setEnabled(true);
                sendButton.setText("Send to VoloX  ➤");
            });
        }).start();
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(questionInput.getWindowToken(), 0);
    }
}
