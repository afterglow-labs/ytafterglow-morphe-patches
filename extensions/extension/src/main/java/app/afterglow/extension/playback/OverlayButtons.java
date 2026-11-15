package app.afterglow.extension.playback;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import app.afterglow.extension.Afterglow;
import app.afterglow.extension.InstalledTweaks;
import app.afterglow.extension.diagnostics.DebugLog;
import app.afterglow.extension.settings.AfterglowPrefs;
import app.afterglow.extension.settings.LiteMode;
import app.afterglow.extension.ui.AfterglowActivity;
import app.afterglow.extension.video.PlayerType;
import app.afterglow.extension.video.VideoState;

/** Owned standard-player utility row; native views retain all touches outside its buttons. */
public final class OverlayButtons {
    private static final Map<View, WeakReference<Binding>> BOUND = new WeakHashMap<>();
    private static WeakReference<OverlayActionController> controller = new WeakReference<>(null);
    private static final OverlayLockRequest REQUEST = new OverlayLockRequest();
    private OverlayButtons() {}

    public static void setController(OverlayActionController value) {
        Afterglow.runOnMain(() -> {
            controller = new WeakReference<>(value);
            for (WeakReference<Binding> reference : BOUND.values()) {
                Binding binding = reference.get();
                if (binding != null) binding.refresh();
            }
        });
    }

    public static void bind(FrameLayout root) {
        if (root == null || !InstalledTweaks.has("overlay-buttons")) return;
        Afterglow.runOnMain(() -> {
            WeakReference<Binding> reference = BOUND.get(root);
            Binding old = reference == null ? null : reference.get();
            if (old != null) { old.refresh(); return; }
            Binding binding = new Binding(root);
            BOUND.put(root, new WeakReference<>(binding));
            root.addOnAttachStateChangeListener(binding);
            if (root.isAttachedToWindow()) binding.onViewAttachedToWindow(root);
        });
    }

    private static boolean enabled(String key) {
        return InstalledTweaks.has("overlay-buttons") && LiteMode.effectiveBool(key);
    }

    private static final class Binding implements View.OnAttachStateChangeListener,
            ViewTreeObserver.OnPreDrawListener, AfterglowPrefs.Listener, VideoState.Listener {
        final FrameLayout root;
        final LinearLayout row;
        final Button mute, lock, settings;
        final int containerId;
        FrameLayout host;
        ViewTreeObserver observer;
        boolean attached, failed;

        Binding(FrameLayout root) {
            this.root = root;
            containerId = root.getResources().getIdentifier("player_overlays_custom_views_container",
                    "id", root.getContext().getPackageName());
            row = new LinearLayout(root.getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setClipChildren(false);
            row.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            mute = new Button(root, "Mute", 0);
            lock = new Button(root, "Lock controls", 1);
            settings = new Button(root, "Afterglow Settings", 2);
            for (Button button : new Button[] {mute, lock, settings}) {
                row.addView(button, new LinearLayout.LayoutParams(dp(44), dp(44)));
            }
            mute.setOnClickListener(v -> toggleMute());
            lock.setOnClickListener(v -> toggleLock());
            settings.setOnClickListener(v -> {
                try {
                    if (!foreground()) return;
                    root.getContext().startActivity(new Intent(root.getContext(), AfterglowActivity.class));
                } catch (Throwable error) { Afterglow.error("OverlayButtons.settings", error); }
            });
            FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, dp(44), Gravity.TOP | Gravity.END);
            layout.topMargin = dp(54);
            layout.setMarginEnd(dp(14));
            row.setLayoutParams(layout);
            row.setVisibility(View.GONE);
        }

        int dp(float value) { return Math.round(value * root.getResources().getDisplayMetrics().density); }

        @Override public void onViewAttachedToWindow(View view) {
            if (attached) return;
            attached = true;
            observer = root.getViewTreeObserver();
            observer.addOnPreDrawListener(this);
            AfterglowPrefs.addListener(this);
            VideoState.addListener(this);
            refresh();
        }

        @Override public void onViewDetachedFromWindow(View view) {
            attached = false;
            if (host != null) host.removeView(row);
            host = null;
            if (observer != null && observer.isAlive()) observer.removeOnPreDrawListener(this);
            observer = null;
            AfterglowPrefs.removeListener(this);
            VideoState.removeListener(this);
        }

        @Override public boolean onPreDraw() { refresh(); return true; }
        @Override public void onChanged(String key) { Afterglow.runOnMain(this::refresh); }
        @Override public void onPlayerType(PlayerType type) { Afterglow.runOnMain(this::refresh); }
        @Override public void onNewVideo(String id) {
            Afterglow.runOnMain(() -> { REQUEST.cancel(); refresh(); });
        }

        boolean watch() {
            return attached && InstalledTweaks.has("overlay-buttons")
                    && VideoState.playerType().isMaximizedOrFullscreen() && !VideoState.isShortsPlayerInFront()
                    && !VideoState.isAdPlaying();
        }

        boolean foreground() {
            Activity activity = Afterglow.mainActivity();
            return watch() && root.isAttachedToWindow() && root.isShown() && root.hasWindowFocus()
                    && activity != null && !activity.isFinishing() && !activity.isDestroyed()
                    && activity.hasWindowFocus();
        }

        OverlayActionController actions() {
            OverlayActionController value = controller.get();
            String id = VideoState.videoId();
            if (value == null || value.afterglow_overlayActivity() != Afterglow.mainActivity()
                    || id.isEmpty() || !id.equals(value.afterglow_overlayVideoId())) return null;
            return value;
        }

        boolean attachRow() {
            if (host != null) return true;
            if (containerId == 0) return false;
            // YouTube's lock UI is a full-player sibling above the controls root.
            // Its native custom-view container sits above that layer and already
            // tracks player bounds/insets. Only our three button hit areas consume
            // input here; taps elsewhere still reach native lock/unlock controls.
            for (View view = root; view != null;) {
                View candidate = view.findViewById(containerId);
                if (candidate instanceof FrameLayout) {
                    host = (FrameLayout) candidate;
                    host.addView(row);
                    return true;
                }
                ViewParent parent = view.getParent();
                view = parent instanceof View ? (View) parent : null;
            }
            return false;
        }

        void refresh() {
            try {
                boolean visible = watch() && attachRow();
                setVisibility(row, visible);
                if (!visible) return;
                setVisibility(mute, enabled("muteButton"));
                setVisibility(lock, enabled("lockButton"));
                if (!enabled("lockButton")) REQUEST.cancel();
                OverlayActionController value = actions();
                boolean ready = value != null;
                mute.setEnabled(ready);
                lock.setEnabled(ready && value.afterglow_lockState() != 0 && !REQUEST.pending());
                if (ready) {
                    mute.setState(value.afterglow_isMuted(), "Mute", "Unmute");
                    lock.setState(value.afterglow_lockState() >= 2, "Lock controls", "Unlock controls");
                }
                if (host.indexOfChild(row) != host.getChildCount() - 1) row.bringToFront();
                failed = false;
            } catch (Throwable error) {
                mute.setEnabled(false);
                lock.setEnabled(false);
                if (!failed) Afterglow.error("OverlayButtons.refresh", error);
                failed = true;
            }
        }

        void toggleMute() {
            try {
                OverlayActionController value = actions();
                if (!foreground() || !enabled("muteButton") || value == null) return;
                boolean muted = !value.afterglow_isMuted();
                value.afterglow_setMuted(muted);
                // The current iOS router preserves this value, but the retired YouMute
                // setter override is outside its production source graph. Follow native state.
                AfterglowPrefs.setBool("YouMuteKeepMuted", muted);
                DebugLog.write("overlay-mute", muted ? "Native mute requested" : "Native unmute requested");
                refresh();
                row.postDelayed(this::refresh, 150);
            } catch (Throwable error) { Afterglow.error("OverlayButtons.mute", error); }
        }

        void toggleLock() {
            try {
                OverlayActionController value = actions();
                if (!foreground() || !enabled("lockButton") || value == null || REQUEST.pending()) return;
                int state = value.afterglow_lockState();
                if (state == 0) return;
                if (state >= 2) {
                    value.afterglow_setLocked(false);
                    DebugLog.write("overlay-lock", "Native unlock requested");
                    refresh();
                    row.postDelayed(this::refresh, 150);
                    return;
                }
                REQUEST.begin(value, VideoState.videoId(), SystemClock.uptimeMillis());
                if (!Fullscreen.enterFromOverlayButton()) { REQUEST.cancel(); return; }
                finishLock();
            } catch (Throwable error) {
                REQUEST.cancel();
                Afterglow.error("OverlayButtons.lock", error);
            }
        }

        void finishLock() {
            try {
                OverlayActionController value = actions();
                // Native fullscreen may rebuild this root. The activity, native owner and
                // video identify the request; an old view is not required to stay attached.
                Activity activity = Afterglow.mainActivity();
                boolean active = activity != null && !activity.isFinishing() && !activity.isDestroyed()
                        && activity.hasWindowFocus() && VideoState.playerType().isMaximizedOrFullscreen()
                        && !VideoState.isShortsPlayerInFront() && !VideoState.isAdPlaying();
                int decision = REQUEST.advance(value, VideoState.videoId(), SystemClock.uptimeMillis(),
                        active, enabled("lockButton"), VideoState.playerType() == PlayerType.WATCH_WHILE_FULLSCREEN);
                if (decision == OverlayLockRequest.APPLY) {
                    value.afterglow_setLocked(true);
                    DebugLog.write("overlay-lock", "Native fullscreen lock requested");
                    row.postDelayed(this::refresh, 150);
                } else if (decision == OverlayLockRequest.WAIT) {
                    Afterglow.runOnMainDelayed(this::finishLock, 100);
                }
                refresh();
            } catch (Throwable error) {
                REQUEST.cancel();
                Afterglow.error("OverlayButtons.finishLock", error);
            }
        }
    }

    private static void setVisibility(View view, boolean visible) {
        int value = visible ? View.VISIBLE : View.GONE;
        if (view.getVisibility() != value) view.setVisibility(value);
    }

    /** 44dp hit target, 26dp neutral circle, 16dp white glyph, matching the iOS utility row. */
    private static final class Button extends View {
        final int kind;
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boolean active;

        Button(View root, String label, int kind) {
            super(root.getContext());
            this.kind = kind;
            setContentDescription(label);
            setClickable(true);
            setFocusable(true);
            setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        }

        @Override public CharSequence getAccessibilityClassName() { return "android.widget.Button"; }

        void setState(boolean value, String off, String on) {
            String label = value ? on : off;
            if (!label.contentEquals(getContentDescription())) setContentDescription(label);
            if (active != value) { active = value; invalidate(); }
        }

        @Override protected void drawableStateChanged() { super.drawableStateChanged(); invalidate(); }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float density = getResources().getDisplayMetrics().density;
            canvas.save();
            canvas.translate(getWidth() / 2f, getHeight() / 2f);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(isPressed() ? 0xb0000000 : 0x7a000000);
            canvas.drawCircle(0, 0, 13 * density, paint);
            canvas.scale(16 * density / 24f, 16 * density / 24f);
            canvas.translate(-12, -12);
            paint.setColor(isEnabled() ? 0xf0ffffff : 0x66ffffff);
            paint.setStrokeWidth(2.2f);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            if (kind == 0) {
                Path speaker = new Path();
                speaker.moveTo(3, 9); speaker.lineTo(7, 9); speaker.lineTo(12, 4);
                speaker.lineTo(12, 20); speaker.lineTo(7, 15); speaker.lineTo(3, 15); speaker.close();
                canvas.drawPath(speaker, paint);
                paint.setStyle(Paint.Style.STROKE);
                if (active) {
                    canvas.drawLine(16, 8, 22, 16, paint);
                    canvas.drawLine(22, 8, 16, 16, paint);
                } else {
                    canvas.drawArc(new RectF(8, 7, 20, 17), -55, 110, false, paint);
                    canvas.drawArc(new RectF(6, 3, 24, 21), -55, 110, false, paint);
                }
            } else if (kind == 1) {
                canvas.drawRoundRect(new RectF(5, 10, 19, 22), 2, 2, paint);
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawArc(new RectF(7, 2, 17, 15), active ? 180 : 205,
                        active ? 180 : 150, false, paint);
                if (active) canvas.drawLine(17, 8, 17, 11, paint);
                canvas.drawLine(7, 8, 7, 11, paint);
            } else {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(3);
                canvas.drawCircle(12, 12, 6.5f, paint);
                for (int i = 0; i < 8; i++) {
                    canvas.save(); canvas.rotate(i * 45, 12, 12);
                    canvas.drawLine(12, 3, 12, 6, paint); canvas.restore();
                }
                paint.setStrokeWidth(2);
                canvas.drawCircle(12, 12, 2.2f, paint);
            }
            canvas.restore();
        }
    }
}
