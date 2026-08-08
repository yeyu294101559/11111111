package com.example.gpsspeedblocker;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public final class MainActivity extends Activity {
    private static final String PREFS_NAME = "settings";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_SPEED = "speed_mps";
    private static final String KEY_PACKAGES = "target_packages";

    private Switch enabledSwitch;
    private EditText speedInput;
    private EditText packagesInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("GPS速度屏蔽模块");

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        enabledSwitch = new Switch(this);
        speedInput = new EditText(this);
        packagesInput = new EditText(this);

        buildView(preferences);
    }

    private void buildView(SharedPreferences preferences) {
        int pagePadding = dp(20);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(pagePadding, dp(16), pagePadding, dp(24));
        content.setBackgroundColor(Color.rgb(247, 249, 255));

        TextView title = text("✅  GPS速度屏蔽模块", 26, true, Color.rgb(52, 56, 75));
        content.addView(title, matchWrap());

        TextView subtitle = text(
                "LSPosed / Xposed 模块 · Hook android.location.Location",
                14,
                false,
                Color.rgb(115, 120, 138)
        );
        subtitle.setPadding(0, dp(8), 0, dp(20));
        content.addView(subtitle, matchWrap());

        enabledSwitch.setText("启用速度拦截");
        enabledSwitch.setTextSize(17);
        enabledSwitch.setTextColor(Color.rgb(52, 56, 75));
        enabledSwitch.setChecked(preferences.getBoolean(KEY_ENABLED, false));
        content.addView(enabledSwitch, matchWrap());

        content.addView(label("固定速度值（m/s）"), matchWrap());
        speedInput.setSingleLine(true);
        speedInput.setHint("例如 0、1.4、27.8");
        speedInput.setText(String.valueOf(preferences.getFloat(KEY_SPEED, 0.0f)));
        speedInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        content.addView(speedInput, matchWrap());

        TextView speedHint = text(
                "0 = 静止，1.4 ≈ 步行，27.8 ≈ 100 km/h",
                13,
                false,
                Color.rgb(115, 120, 138)
        );
        speedHint.setPadding(0, dp(6), 0, dp(18));
        content.addView(speedHint, matchWrap());

        content.addView(label("目标应用包名（可选）"), matchWrap());
        packagesInput.setHint("留空跟随 LSPosed 作用域；多个包名用逗号或换行分隔");
        packagesInput.setGravity(Gravity.TOP | Gravity.START);
        packagesInput.setMinLines(3);
        packagesInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        packagesInput.setText(preferences.getString(KEY_PACKAGES, ""));
        content.addView(packagesInput, matchWrap());

        TextView scopeHint = text(
                "建议优先在 LSPosed 中勾选目标应用。填写包名后，只有匹配的应用才会返回固定速度。",
                13,
                false,
                Color.rgb(115, 120, 138)
        );
        scopeHint.setPadding(0, dp(6), 0, dp(20));
        content.addView(scopeHint, matchWrap());

        Button saveButton = new Button(this);
        saveButton.setText("保存配置");
        saveButton.setTextColor(Color.WHITE);
        saveButton.setTextSize(16);
        saveButton.setAllCaps(false);
        saveButton.setBackgroundColor(Color.rgb(63, 100, 216));
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSettings();
            }
        });
        content.addView(saveButton, matchWrap());

        TextView methods = text(
                "\n拦截方法\n"
                        + "getSpeed()\n"
                        + "hasSpeed()\n"
                        + "getSpeedAccuracyMetersPerSecond()\n"
                        + "hasSpeedAccuracy()",
                14,
                false,
                Color.rgb(92, 96, 113)
        );
        methods.setTypeface(Typeface.MONOSPACE);
        methods.setPadding(0, dp(24), 0, 0);
        content.addView(methods, matchWrap());

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.addView(content);
        setContentView(scrollView);
    }

    private void saveSettings() {
        float speed;
        try {
            speed = Float.parseFloat(speedInput.getText().toString().trim());
        } catch (NumberFormatException exception) {
            speed = 0.0f;
        }
        speed = Math.max(0.0f, Math.min(speed, 343.0f));

        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_ENABLED, enabledSwitch.isChecked())
                .putFloat(KEY_SPEED, speed)
                .putString(KEY_PACKAGES, packagesInput.getText().toString().trim())
                .apply();

        Toast.makeText(this, "配置已保存，重启目标应用后生效", Toast.LENGTH_LONG).show();
    }

    private TextView label(String value) {
        TextView view = text(value, 16, true, Color.rgb(52, 56, 75));
        view.setPadding(0, dp(8), 0, dp(8));
        return view;
    }

    private TextView text(String value, float size, boolean bold, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL);
        return view;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
