package app.afterglow.extension.playback;

import android.view.View;
import android.view.ViewTreeObserver;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import app.afterglow.extension.Afterglow;
import app.afterglow.extension.InstalledTweaks;
import app.afterglow.extension.settings.AfterglowPrefs;
import app.afterglow.extension.settings.LiteMode;

/** Rebinds the current native action model after either hide preference changes. */
public final class QuickActions {
    private static final Map<View, WeakReference<Binding>> BOUND = new WeakHashMap<>();

    private QuickActions() {}

    private static int hiddenButtons() {
        if (!InstalledTweaks.has("player-buttons-menus")) return 0;
        return (LiteMode.effectiveBool("noPlayerShareButton") ? 1 : 0)
                | (LiteMode.effectiveBool("noPlayerSaveButton") ? 2 : 0);
    }

    public static void bind(QuickActionsController controller) {
        try {
            if (!InstalledTweaks.has("player-buttons-menus") || controller == null) return;
            View view = controller.afterglow_quickActionsView();
            if (view == null) return;
            WeakReference<Binding> reference = BOUND.get(view);
            Binding binding = reference == null ? null : reference.get();
            if (binding != null) {
                binding.controller = new WeakReference<>(controller);
                return;
            }
            binding = new Binding(view, controller);
            BOUND.put(view, new WeakReference<>(binding));
            view.addOnAttachStateChangeListener(binding);
            if (view.isAttachedToWindow()) binding.onViewAttachedToWindow(view);
        } catch (Throwable error) {
            Afterglow.error("QuickActions.bind", error);
        }
    }

    private static final class Binding implements View.OnAttachStateChangeListener,
            ViewTreeObserver.OnPreDrawListener {
        final View view;
        WeakReference<QuickActionsController> controller;
        ViewTreeObserver observer;
        long revision = AfterglowPrefs.revision();
        int desired = hiddenButtons(), rendered = desired;
        boolean pending, failed;

        Binding(View view, QuickActionsController controller) {
            this.view = view;
            this.controller = new WeakReference<>(controller);
        }

        @Override public void onViewAttachedToWindow(View v) {
            if (observer != null || failed) return;
            observer = view.getViewTreeObserver();
            observer.addOnPreDrawListener(this);
        }

        @Override public void onViewDetachedFromWindow(View v) {
            if (observer != null && observer.isAlive()) observer.removeOnPreDrawListener(this);
            observer = null;
        }

        @Override public boolean onPreDraw() {
            long current = AfterglowPrefs.revision();
            if (current != revision) {
                desired = hiddenButtons();
                revision = current;
            }
            if (!pending && !failed && rendered != desired && view.isShown()) {
                pending = true;
                // Rebuilding a component tree during traversal is unsafe. Recheck the
                // attached host and use the native presenter's latest model afterward.
                view.post(() -> {
                    pending = false;
                    QuickActionsController target = controller.get();
                    if (!view.isAttachedToWindow() || !view.isShown() || target == null) return;
                    int next = hiddenButtons();
                    if (rendered == next) return;
                    try {
                        if (target.afterglow_quickActionsView() == view
                                && target.afterglow_refreshQuickActions()) rendered = next;
                    } catch (Throwable error) {
                        failed = true;
                        onViewDetachedFromWindow(view);
                        Afterglow.error("QuickActions.refresh", error);
                    }
                });
            }
            return true;
        }
    }
}
