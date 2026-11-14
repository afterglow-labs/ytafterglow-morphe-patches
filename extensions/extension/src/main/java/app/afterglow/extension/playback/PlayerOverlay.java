package app.afterglow.extension.playback;

import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import java.util.Map;
import java.util.WeakHashMap;

import app.afterglow.extension.Afterglow;
import app.afterglow.extension.InstalledTweaks;
import app.afterglow.extension.settings.AfterglowPrefs;
import app.afterglow.extension.settings.LiteMode;

/** Native controls retain their layout, draw callbacks and native visibility state. */
public final class PlayerOverlay {
    private static final Map<View, Boolean> BOUND = new WeakHashMap<>();

    private PlayerOverlay() {}

    public static void controls(View root) {
        if (root == null || !InstalledTweaks.has("player-overlay")) return;
        // The native constructor is still assembling this layout at the injection point.
        root.post(() -> {
            bindNamed(root, "player_control_previous_button_touch_area", "hidePrevNext");
            bindNamed(root, "player_control_next_button_touch_area", "hidePrevNext");
            bindNamed(root, "player_overflow_button", "hidePlayerOverflowButton");
        });
    }

    public static void captions(ImageView view) { bind(view, "hideSubs"); }
    public static void collapse(ImageView view) { bind(view, "hidePlayerCollapseButton"); }
    public static void fullscreen(ImageView view) { bind(view, "hidePlayerFullscreenButton"); }

    private static void bindNamed(View root, String name, String key) {
        int id = root.getResources().getIdentifier(name, "id", root.getContext().getPackageName());
        if (id != 0) bind(root.findViewById(id), key);
    }

    private static void bind(View view, String key) {
        if (view == null || !InstalledTweaks.has("player-overlay") || BOUND.containsKey(view)) return;
        BOUND.put(view, true);
        Binding binding = new Binding(view, key);
        view.addOnAttachStateChangeListener(binding);
        if (view.isAttachedToWindow()) binding.onViewAttachedToWindow(view);
    }

    private static final class Binding implements View.OnAttachStateChangeListener,
            ViewTreeObserver.OnPreDrawListener {
        final View view;
        final String key;
        ViewTreeObserver observer;
        long revision = -1;
        boolean requested, hidden, focusable;
        int accessibility;
        float nativeTranslation, appliedTranslation;

        Binding(View view, String key) { this.view = view; this.key = key; }

        @Override public void onViewAttachedToWindow(View v) {
            if (observer != null) return;
            observer = view.getViewTreeObserver();
            observer.addOnPreDrawListener(this);
        }

        @Override public void onViewDetachedFromWindow(View v) {
            if (observer != null && observer.isAlive()) observer.removeOnPreDrawListener(this);
            observer = null;
        }

        @Override public boolean onPreDraw() {
            try {
                long current = AfterglowPrefs.revision();
                if (revision != current) {
                    requested = InstalledTweaks.has("player-overlay") && LiteMode.effectiveBool(key);
                    revision = current;
                }
                if (requested) {
                    if (!hidden) {
                        nativeTranslation = view.getTranslationY();
                        accessibility = view.getImportantForAccessibility();
                        focusable = view.isFocusable();
                        view.clearFocus();
                    } else if (view.getTranslationY() != appliedTranslation) {
                        // Preserve a fresh position set by the native controller while hidden.
                        nativeTranslation = view.getTranslationY();
                    }
                    // The fullscreen control drives the modern overlay's fade callbacks.
                    // Keep it drawing, as Morphe does, by moving it outside the viewport.
                    appliedTranslation = nativeTranslation - Math.max(16384f, view.getRootView().getHeight() * 4f);
                    if (view.getTranslationY() != appliedTranslation) view.setTranslationY(appliedTranslation);
                    view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
                    view.setFocusable(false);
                    hidden = true;
                } else if (hidden) {
                    if (view.getTranslationY() == appliedTranslation) view.setTranslationY(nativeTranslation);
                    view.setImportantForAccessibility(accessibility);
                    view.setFocusable(focusable);
                    hidden = false;
                }
            } catch (Throwable error) {
                onViewDetachedFromWindow(view);
                Afterglow.error("PlayerOverlay." + key, error);
            }
            return true;
        }
    }
}
