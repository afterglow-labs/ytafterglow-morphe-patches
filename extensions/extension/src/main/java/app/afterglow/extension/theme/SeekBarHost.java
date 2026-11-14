package app.afterglow.extension.theme;

import android.graphics.Rect;

/** Implemented on the verified native player bar by SeekBarThemePatch. */
public interface SeekBarHost {
    Rect afterglow_trackBounds();
    int afterglow_playedPosition();
}
