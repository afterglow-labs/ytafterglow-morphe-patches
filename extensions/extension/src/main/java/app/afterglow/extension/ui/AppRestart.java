package app.afterglow.extension.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import app.afterglow.extension.Afterglow;

/**
 * Restarts YouTube, the Android counterpart of the iOS {@code exit(0)} used by
 * Restart Now, Reset & Restart and Restore default settings. The launch intent
 * is handed to the system before the process exits, so YouTube comes back on
 * its own; no alarm/PendingIntent relaunch (blocked by background-start rules).
 */
final class AppRestart {
    private AppRestart() {}

    static void restart(Activity activity) {
        try {
            Intent launch = activity.getPackageManager().getLaunchIntentForPackage(activity.getPackageName());
            if (launch != null) {
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(launch);
            }
        } catch (Throwable error) {
            Afterglow.error("AppRestart", error);
        }
        activity.finishAffinity();
        // Let the start request reach the system before the process goes away.
        new Handler(Looper.getMainLooper()).postDelayed(() -> Runtime.getRuntime().exit(0), 300);
    }
}
