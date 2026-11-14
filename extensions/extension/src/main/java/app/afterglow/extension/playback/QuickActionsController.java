package app.afterglow.extension.playback;

import android.view.View;

/** Bridge to the native fullscreen action presenter; its current model stays native. */
public interface QuickActionsController {
    View afterglow_quickActionsView();
    boolean afterglow_refreshQuickActions();
}
