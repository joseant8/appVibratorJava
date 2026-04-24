package com.android.appvibrator;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private SeekBar seekVibration, seekInterval, seekDuration;
    private TextView txtVibration, txtInterval, txtDuration;
    private Button btnStart, btnStop, btnSalir;
    private CheckBox checkDelay;

    private float vibrationTime = 0.5f; // Duración de la vibración en segundos
    private int intervalTime = 12; // Intervalo entre vibraciones en segundos
    private int totalTime = 10; // Duración total del servicio en minutos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        seekVibration = findViewById(R.id.seekVibration);
        seekInterval = findViewById(R.id.seekInterval);
        seekDuration = findViewById(R.id.seekDuration);

        txtVibration = findViewById(R.id.txtVibration);
        txtInterval = findViewById(R.id.txtInterval);
        txtDuration = findViewById(R.id.txtDuration);

        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        btnSalir = findViewById(R.id.btnSalir);

        checkDelay = findViewById(R.id.checkDelay);

        // Vibración (0.1 a 2s)
        seekVibration.setMax(19); // 0.1 steps
        seekVibration.setProgress(1);

        seekVibration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                vibrationTime = 0.1f + (progress * 0.1f);
                txtVibration.setText("Duración vibración: " + vibrationTime + " s");
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Intervalo (1 a 10s)
        seekInterval.setMax(9);
        seekInterval.setProgress(1);

        seekInterval.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                intervalTime = 1 + progress;
                txtInterval.setText("Intervalo: " + intervalTime + " s");
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Duración total (1 a 240 min)
        seekDuration.setMax(239);
        seekDuration.setProgress(9);

        seekDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                totalTime = 1 + progress;
                txtDuration.setText("Duración total: " + totalTime + " min");
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        /**
         * Inicia el servicio de vibración en primer plano.
         */
        btnStart.setOnClickListener(v -> {
            Intent intent = new Intent(this, VibrationService.class);
            intent.putExtra("vibration", vibrationTime);
            intent.putExtra("interval", intervalTime);
            intent.putExtra("duration", totalTime);
            intent.putExtra("delay", checkDelay.isChecked());

            ContextCompat.startForegroundService(this, intent);

            // Mantener pantalla encendida
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            btnStop.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        });

        /**
         * Detiene completamente el servicio.
         */
        btnStop.setOnClickListener(v -> {
            stopService(new Intent(this, VibrationService.class));

            // Permitir que la pantalla se apague
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            btnStop.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
        });

        btnSalir.setOnClickListener(v -> {
            finishAffinity(); // Cierra todas las actividades de la app
            System.exit(0); // Termina el proceso de la app
        });
    }
}