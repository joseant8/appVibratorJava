package com.android.appvibrator;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.core.app.NotificationCompat;

/**
 * Servicio en primer plano encargado de gestionar la vibración periódica del dispositivo.
 * Ejecuta un ciclo continuo de vibración + espera hasta que se detiene o expira el tiempo.
 */
public class VibrationService extends Service {

    private Handler handler;
    private Runnable runnable;
    private Vibrator vibrator;

    private long vibrationMs; // Duración de vibración en milisegundos
    private long intervalMs; // Intervalo entre vibraciones en milisegundos
    private long endTime; // Tiempo en el que el servicio debe finalizar

    /**
     * Método llamado cuando el servicio es iniciado.
     *
     * @param intent Intent con parámetros de configuración
     * @param flags flags del sistema
     * @param startId identificador de inicio
     * @return modo de persistencia del servicio
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        float vib = intent.getFloatExtra("vibration", 0.5f);
        int interval = intent.getIntExtra("interval", 12);
        int duration = intent.getIntExtra("duration", 1);
        boolean delay = intent.getBooleanExtra("delay", false);

        vibrationMs = (long)(vib * 1000);
        intervalMs = interval * 1000;
        endTime = System.currentTimeMillis() + (duration * 60 * 1000);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        handler = new Handler(Looper.getMainLooper());

        startForeground(1, createNotification());

        // tarea que se ejecutará repetidamente
        runnable = new Runnable() {
            @Override
            public void run() {

                if (System.currentTimeMillis() >= endTime) {
                    stopSelf();
                    return;
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(vibrationMs, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(vibrationMs);
                }

                handler.postDelayed(this, vibrationMs + intervalMs);
            }
        };

        if (delay) {
            handler.postDelayed(runnable, 120000); // retrasa la ejecución 2 minutos (solo la primera vez)
        } else {
            handler.post(runnable);
        }

        return START_STICKY;
    }

    /**
     * Crea la notificación persistente del servicio en primer plano.
     *
     * @return objeto Notification
     */
    private Notification createNotification() {
        String channelId = "vibration_channel";

        NotificationChannel channel = new NotificationChannel(
                channelId,
                "Vibration Service",
                NotificationManager.IMPORTANCE_LOW
        );

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Vibración activa")
                .setContentText("El servicio está funcionando")
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .build();
    }

    /**
     * Se ejecuta cuando el servicio es destruido.
     * Limpia el handler para evitar ejecuciones pendientes.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) handler.removeCallbacks(runnable);
    }

    /**
     * No se utiliza binding en este servicio.
     *
     * @param intent intent de enlace
     * @return siempre null
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}