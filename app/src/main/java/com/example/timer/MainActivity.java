package com.example.timer;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final long DEFAULT_MILLIS = 5 * 60 * 1000; // 5 minutes

    private long remainingMillis = DEFAULT_MILLIS;
    private long originalMillis = DEFAULT_MILLIS; // target duration when timer started
    private CountDownTimer countDownTimer;
    private boolean isRunning = false;

    private TextView txtTime;

    private ListView lvLogs;
    private ArrayAdapter<String> logsAdapter;
    private final List<String> logs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtTime = findViewById(R.id.txt_time);
        Button btnStart = findViewById(R.id.btn_start);
        Button btnPause = findViewById(R.id.btn_pause);
        Button btnReset = findViewById(R.id.btn_reset);

        // direct references to preset buttons and logs ListView
        Button btn3 = findViewById(R.id.btn_preset_3);
        Button btn5 = findViewById(R.id.btn_preset_5);
        Button btn10 = findViewById(R.id.btn_preset_10);
        Button btn15 = findViewById(R.id.btn_preset_15);
        Button btn25 = findViewById(R.id.btn_preset_25);
        Button btn30 = findViewById(R.id.btn_preset_30);

        lvLogs = findViewById(R.id.lv_logs);

        logsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, logs);
        if (lvLogs != null) {
            lvLogs.setAdapter(logsAdapter);
            setListViewHeightBasedOnChildren(lvLogs);
        }

        updateTimeDisplay();

        btnStart.setOnClickListener(v -> {
            if (!isRunning) {
                originalMillis = remainingMillis;
                startTimer();
            }
        });

        btnPause.setOnClickListener(v -> {
            if (isRunning) {
                long elapsed = originalMillis - remainingMillis; // millis that passed
                String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
                String entry = String.format(Locale.getDefault(), "%s — %s / %s", ts, formatMillis(elapsed), formatMillis(originalMillis));
                addLog(entry);
                pauseTimer();
            }
        });

        btnReset.setOnClickListener(v -> resetTimer());

        View.OnClickListener presetClick = v -> {
            Object tag = v.getTag();
            int minutes = 5; // fallback
            if (tag instanceof Integer) {
                minutes = (Integer) tag;
            } else {
                String txt = "";
                if (v instanceof Button) txt = ((Button) v).getText().toString();
                try {
                    minutes = Integer.parseInt(txt.split(" ")[0]);
                } catch (Exception ignored) {}
            }

            remainingMillis = minutes * 60L * 1000L;
            originalMillis = remainingMillis;
            updateTimeDisplay();
        };

        // set tags and listeners only if buttons exist
        if (btn3 != null) { btn3.setTag(3); btn3.setOnClickListener(presetClick); }
        if (btn5 != null) { btn5.setTag(5); btn5.setOnClickListener(presetClick); }
        if (btn10 != null) { btn10.setTag(10); btn10.setOnClickListener(presetClick); }
        if (btn15 != null) { btn15.setTag(15); btn15.setOnClickListener(presetClick); }
        if (btn25 != null) { btn25.setTag(25); btn25.setOnClickListener(presetClick); }
        if (btn30 != null) { btn30.setTag(30); btn30.setOnClickListener(presetClick); }
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(remainingMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingMillis = millisUntilFinished;
                updateTimeDisplay();
            }

            @Override
            public void onFinish() {
                // timer completed normally — log elapsed vs planned
                long elapsed = originalMillis; // full duration elapsed
                String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
                String entry = String.format(Locale.getDefault(), "%s — %s / %s", ts, formatMillis(elapsed), formatMillis(originalMillis));
                addLog(entry);

                remainingMillis = 0;
                isRunning = false;
                updateTimeDisplay();
            }
        };
        isRunning = true;
        countDownTimer.start();
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isRunning = false;
    }

    private void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        remainingMillis = DEFAULT_MILLIS;
        originalMillis = DEFAULT_MILLIS;
        isRunning = false;
        updateTimeDisplay();
    }

    private void updateTimeDisplay() {
        int totalSeconds = (int) (remainingMillis / 1000);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        txtTime.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
    }

    private void addLog(String entry) {
        logs.add(0, entry); // newest first
        if (logs.size() > 20) logs.remove(logs.size() - 1);
        if (logsAdapter != null) logsAdapter.notifyDataSetChanged();
        if (lvLogs != null) setListViewHeightBasedOnChildren(lvLogs);
    }

    private static String formatMillis(long millis) {
        if (millis < 0) millis = 0;
        int totalSeconds = (int) (millis / 1000);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        int dividerHeight = listView.getDividerHeight() * (listAdapter.getCount() - 1);
        params.height = totalHeight + dividerHeight + listView.getPaddingTop() + listView.getPaddingBottom();
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

}