package app.afterglow.extension.ui;

import android.app.AlertDialog;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import app.afterglow.extension.settings.ThemeCatalog;
import app.afterglow.extension.settings.ThemePalette;
import app.afterglow.extension.settings.ThemeRole;
import app.afterglow.extension.settings.ThemeStore;

/** Working preset portion of iOS YTAGThemesViewController; no placeholder editor/library actions. */
final class ThemeStudioScreen extends SettingsScreen {
    private final AfterglowActivity activity;

    ThemeStudioScreen(AfterglowActivity activity) { this.activity = activity; }

    @Override String title() { return "Theme Studio"; }

    @Override void build(LinearLayout content) {
        RowViews views = activity.views();
        if (Build.VERSION.SDK_INT < 34) {
            content.addView(views.detailRow("Android 14 or newer required",
                    "Native YouTube theme colors require Android 14 or newer.", null, true, null));
            return;
        }
        ThemePalette active = ThemeStore.snapshot().palette;
        content.addView(views.sectionTitle("Active Theme"));
        content.addView(views.detailRow(active.name, active.summary, null, true, null));
        content.addView(views.detailRow(null,
                "Native YouTube uses solid backgrounds. Gradients, glow and custom theme editing are not supported yet.",
                null, false, null));
        for (boolean holiday : new boolean[]{false, true}) {
            content.addView(views.sectionTitle(holiday ? "Holiday Themes" : "Presets"));
            for (ThemePalette preset : ThemeCatalog.PRESETS) {
                if (preset.identifier.startsWith("holiday-") != holiday) continue;
                LinearLayout row = new LinearLayout(activity);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                ImageView swatch = new ImageView(activity);
                swatch.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                swatch.setImageDrawable(new Swatch(preset));
                LinearLayout.LayoutParams swatchParams = new LinearLayout.LayoutParams(views.dp(44), views.dp(28));
                swatchParams.leftMargin = views.dp(16);
                row.addView(swatch, swatchParams);
                View detail = views.detailRow(preset.name, preset.summary,
                        preset.identifier.equals(active.identifier) ? "✓" : null, true, null);
                row.addView(detail, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                row.setBackground(views.touchFeedback());
                row.setOnClickListener(v -> activate(preset));
                content.addView(row);
            }
        }
        content.addView(views.sectionTitle("Library"));
        content.addView(views.detailRow("Reset to YouTube Colors",
                "Clears the active palette. Saved themes stay in My Themes.", null, true,
                v -> new AlertDialog.Builder(activity).setTitle("Reset to YouTube Colors")
                        .setMessage("Reset the active colors and restart YouTube?")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Reset & Restart", (dialog, which) -> {
                            ThemeStore.resetToSystem();
                            AppRestart.restart(activity);
                        }).show()));
    }

    private void activate(ThemePalette preset) {
        new AlertDialog.Builder(activity).setTitle(preset.name)
                .setMessage("Use this theme and restart YouTube to apply its colors?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Use Theme & Restart", (dialog, which) -> {
                    ThemeStore.applyPreset(preset.identifier);
                    AppRestart.restart(activity);
                }).show();
    }

    /** Same four roles/order as the iOS preset swatch, with no screenshot-derived colors. */
    private static final class Swatch extends Drawable {
        private final ThemePalette palette;
        private final Paint paint = new Paint();
        private static final ThemeRole[] ROLES = {ThemeRole.BACKGROUND, ThemeRole.NAVIGATION_SURFACE,
                ThemeRole.ACCENT, ThemeRole.TAB_ICON};
        Swatch(ThemePalette palette) { this.palette = palette; }
        @Override public void draw(Canvas canvas) {
            float width = getBounds().width() / 4f;
            for (int i = 0; i < ROLES.length; i++) {
                Integer color = palette.color(ROLES[i]);
                paint.setColor(color == null ? 0xFF888888 : color);
                canvas.drawRect(getBounds().left + width * i, getBounds().top,
                        getBounds().left + width * (i + 1), getBounds().bottom, paint);
            }
        }
        @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); }
        @Override public void setColorFilter(ColorFilter filter) { paint.setColorFilter(filter); }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }
}
