package app.afterglow.extension.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

/**
 * The glowing "Afterglow" wordmark, as iOS draws it on its YouTube settings entry
 * (Settings.x:338-380, 426-431, 486-514):
 * <ul>
 * <li>bold version of the cell's font; "After" white, "glow" (characters 5-8) orange
 *     (1.00, 0.45, 0.02);</li>
 * <li>label layer shadow: orange, opacity 0.60, radius 20, offset 0 (behind the text);</li>
 * <li>secondary glow layer: layer opacity 0.50, shadow opacity 1.0, radius 20, shadow path =
 *     the label bounds inset by (-2, -1) with corner radius 6. Sublayers composite above
 *     the label's own content in Core Animation, so this glow is drawn over the text
 *     (my reading of the layer order; see RECT_GLOW_ABOVE_TEXT).</li>
 * </ul>
 * Framework-only TextView so the patched YouTube settings layout can inflate it by name
 * with id android:id/title (androidx.preference methods are obfuscated in YouTube, so a
 * Preference subclass is not an option). The glow is drawn natively: blurred alpha masks
 * rendered once on a software bitmap with BlurMaskFilter, then drawn with an orange paint.
 */
public class AfterglowWordmarkView extends TextView {
    /** iOS YTAGAfterglowSettingsDisplayTitle. */
    public static final String DISPLAY_TITLE = "Afterglow";
    /** iOS YTAGAfterglowWordmarkWhiteColor: white even on light backgrounds, as on iOS. */
    static final int WORDMARK_WHITE = Color.WHITE;
    /** iOS YTAGAfterglowWordmarkPorangeColor (1.00, 0.45, 0.02). */
    static final int WORDMARK_ORANGE = Color.rgb(255, 115, 5);

    // The values a side-by-side screenshot would tune.
    /** CALayer shadowRadius 20 taken as the Gaussian sigma, in dp. */
    static final float GLOW_SIGMA_DP = 20f;
    /** Label layer shadowOpacity. */
    static final float LABEL_GLOW_OPACITY = 0.60f;
    /** Secondary layer opacity 0.50 x shadowOpacity 1.0. */
    static final float RECT_GLOW_OPACITY = 0.50f;
    /** Secondary glow drawn above the text (Core Animation sublayer order). */
    static final boolean RECT_GLOW_ABOVE_TEXT = true;
    /** Rounded-rect glow hugs the text (true) or spans the whole label width (false). */
    static final boolean RECT_TIGHT_TO_TEXT = true;
    static final float RECT_INSET_X_DP = -2f;
    static final float RECT_INSET_Y_DP = -1f;
    static final float RECT_CORNER_DP = 6f;
    /** Ancestors unclipped by default so the glow spills past the row (column, row, list). */
    static final int UNCLIP_DEPTH = 3;

    private Paint glowPaint;
    private int unclipDepth;
    private Bitmap textGlow;
    private Bitmap rectGlow;
    private int[] textGlowOffset;
    private float rectGlowLeft;
    private float rectGlowTop;
    private String glowKey;

    public AfterglowWordmarkView(Context context) {
        super(context);
        init();
    }

    public AfterglowWordmarkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AfterglowWordmarkView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        unclipDepth = UNCLIP_DEPTH;
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        glowPaint.setColor(WORDMARK_ORANGE);
        // iOS ytag_boldenSettingsLabel: bold version of the cell's own font and size.
        setTypeface(Typeface.create(getTypeface(), Typeface.BOLD));
        // Re-apply the wordmark to whatever the layout or the Preference set before init.
        CharSequence text = getText();
        if (text != null && text.length() > 0) setText(text, BufferType.SPANNABLE);
    }

    /** True when the text is the Afterglow title (iOS checks the visible title text). */
    static boolean isWordmark(CharSequence text) {
        return text != null && DISPLAY_TITLE.contentEquals(text.toString());
    }

    /**
     * Preference binding calls setText(title); the one-argument setText is final, so the
     * wordmark is applied here. Runs during TextView's constructor too, before this class's
     * fields exist, so it must not touch them except to clear the cache.
     */
    @Override
    public void setText(CharSequence text, BufferType type) {
        if (isWordmark(text) && !(text instanceof Spanned && ((Spanned) text).getSpans(0, text.length(), ForegroundColorSpan.class).length > 0)) {
            SpannableString wordmark = new SpannableString(DISPLAY_TITLE);
            wordmark.setSpan(new ForegroundColorSpan(WORDMARK_WHITE), 0, DISPLAY_TITLE.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            wordmark.setSpan(new ForegroundColorSpan(WORDMARK_ORANGE), 5, DISPLAY_TITLE.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            super.setText(wordmark, BufferType.SPANNABLE);
        } else {
            super.setText(text, type);
        }
        glowKey = null;
    }

    /**
     * How many ancestors may let the glow spill past them. The YouTube settings row uses the
     * default (text column, row, RecyclerView); the scroll view or list above still clips, as
     * the iOS collection view clips a top-of-list cell's glow.
     */
    public void setUnclipDepth(int depth) {
        unclipDepth = Math.max(0, depth);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // iOS: masksToBounds = NO, the glow spills over neighbouring content. Let it spill
        // past this view, its text column, its row and the list holding the row. Padding
        // clipping is left alone, so the list's own container still bounds the glow.
        ViewParent parent = getParent();
        for (int depth = 0; depth < unclipDepth && parent instanceof ViewGroup; depth++) {
            ((ViewGroup) parent).setClipChildren(false);
            parent = parent.getParent();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Layout layout = getLayout();
        if (!isWordmark(getText()) || layout == null || glowPaint == null) {
            super.onDraw(canvas);
            return;
        }
        ensureGlow(layout);
        float originX = getCompoundPaddingLeft();
        float originY = getExtendedPaddingTop() + verticalOffset(layout);

        // 1. Label shadow, beneath the text.
        glowPaint.setAlpha(Math.round(LABEL_GLOW_OPACITY * 255));
        canvas.drawBitmap(textGlow, originX + textGlowOffset[0], originY + textGlowOffset[1], glowPaint);
        if (!RECT_GLOW_ABOVE_TEXT) drawRectGlow(canvas, originX, originY);
        // 2. The text (TextView clips its own drawing; the glows above are drawn outside it).
        super.onDraw(canvas);
        // 3. Secondary glow layer.
        if (RECT_GLOW_ABOVE_TEXT) drawRectGlow(canvas, originX, originY);
    }

    private void drawRectGlow(Canvas canvas, float originX, float originY) {
        glowPaint.setAlpha(Math.round(RECT_GLOW_OPACITY * 255));
        canvas.drawBitmap(rectGlow, originX + rectGlowLeft, originY + rectGlowTop, glowPaint);
    }

    /** TextView's vertical gravity offset for the text block. */
    private int verticalOffset(Layout layout) {
        int gravity = getGravity() & Gravity.VERTICAL_GRAVITY_MASK;
        int box = getMeasuredHeight() - getExtendedPaddingTop() - getExtendedPaddingBottom();
        int text = layout.getHeight();
        if (gravity == Gravity.BOTTOM) return Math.max(0, box - text);
        if (gravity == Gravity.CENTER_VERTICAL) return Math.max(0, (box - text) / 2);
        return 0;
    }

    /** Renders the two blurred alpha masks once per text, size and density. */
    private void ensureGlow(Layout layout) {
        float density = getResources().getDisplayMetrics().density;
        String key = getText() + "|" + layout.getWidth() + "x" + layout.getHeight() + "|" + getTextSize() + "|" + density;
        if (key.equals(glowKey) && textGlow != null && rectGlow != null) return;

        float sigma = GLOW_SIGMA_DP * density;
        // Skia converts a blur radius to sigma as 0.57735 * radius + 0.5.
        float radius = Math.max(0.5f, (sigma - 0.5f) / 0.57735f);
        Paint blur = new Paint();
        blur.setMaskFilter(new BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL));

        // Label shadow: the glyph shapes of the whole title.
        Bitmap glyphs = Bitmap.createBitmap(Math.max(1, layout.getWidth()), Math.max(1, layout.getHeight()), Bitmap.Config.ARGB_8888);
        layout.draw(new Canvas(glyphs));
        int[] glyphOffset = new int[2];
        Bitmap blurredText = glyphs.extractAlpha(blur, glyphOffset);
        glyphs.recycle();

        // Secondary layer: rounded rect around the label (iOS bounds inset (-2, -1), radius 6).
        float left = RECT_TIGHT_TO_TEXT ? layout.getLineLeft(0) : 0;
        float right = RECT_TIGHT_TO_TEXT ? layout.getLineRight(0) : layout.getWidth();
        RectF rect = new RectF(left + RECT_INSET_X_DP * density, layout.getLineTop(0) + RECT_INSET_Y_DP * density,
                right - RECT_INSET_X_DP * density, layout.getLineBottom(layout.getLineCount() - 1) - RECT_INSET_Y_DP * density);
        Bitmap shape = Bitmap.createBitmap(Math.max(1, (int) Math.ceil(rect.width())), Math.max(1, (int) Math.ceil(rect.height())), Bitmap.Config.ALPHA_8);
        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        new Canvas(shape).drawRoundRect(new RectF(0, 0, rect.width(), rect.height()), RECT_CORNER_DP * density, RECT_CORNER_DP * density, fill);
        int[] shapeOffset = new int[2];
        Bitmap blurredRect = shape.extractAlpha(blur, shapeOffset);
        shape.recycle();

        if (textGlow != null) textGlow.recycle();
        if (rectGlow != null) rectGlow.recycle();
        textGlow = blurredText;
        textGlowOffset = glyphOffset;
        rectGlow = blurredRect;
        rectGlowLeft = rect.left + shapeOffset[0];
        rectGlowTop = rect.top + shapeOffset[1];
        glowKey = key;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (textGlow != null) textGlow.recycle();
        if (rectGlow != null) rectGlow.recycle();
        textGlow = null;
        rectGlow = null;
        glowKey = null;
    }
}
