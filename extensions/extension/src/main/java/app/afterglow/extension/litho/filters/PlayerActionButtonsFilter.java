/*
 * YTAfterglow for Android.
 *
 * The accessibility ids are the iOS tweak's; the action bar identifier and the
 * share, save and remix icon names are the ones the video action buttons filter
 * of Morphe patches v2.2.0 (https://github.com/MorpheApp/morphe-patches, GPLv3)
 * uses for YouTube 21.43.523. Not an official Morphe file.
 */

package app.afterglow.extension.litho.filters;

import app.afterglow.extension.litho.ByteArrayFilterGroup;
import app.afterglow.extension.litho.ByteArrayFilterGroupList;
import app.afterglow.extension.litho.LithoComponent;
import app.afterglow.extension.litho.LithoFilter;
import app.afterglow.extension.litho.StringFilterGroup;
import app.afterglow.extension.litho.StringFilterGroupList;

/**
 * The buttons under the video on the watch page, for the iOS settings
 * {@code noPlayerDownloadButton}, {@code noPlayerShareButton},
 * {@code noPlayerSaveButton}, {@code noPlayerRemixButton} and
 * {@code noPlayerClipButton}. Each hides one button.
 *
 * <p>iOS looks inside the scrollable action bar for each button's
 * accessibility id ({@code id.video.share.button}, {@code id.video.add_to.button},
 * {@code id.video.remix.button}, {@code id.ui.add_to.offline.button}) or, for
 * Clip, its {@code clip_button.eml} component, and falls back to the English
 * label. The same ids come from YouTube's server here; instead of English labels
 * the fallback is each button's icon, which works in every language.
 */
public final class PlayerActionButtonsFilter extends LithoFilter {
    private final StringFilterGroup actionBar;
    private final StringFilterGroupList buttonsById = new StringFilterGroupList();
    private final StringFilterGroup clipButton;
    private final ByteArrayFilterGroupList buttonIcons = new ByteArrayFilterGroupList();

    public PlayerActionButtonsFilter() {
        // Every component of the action bar element has the bar's identifier.
        actionBar = StringFilterGroup.anyOf(
                new String[] {"noPlayerDownloadButton", "noPlayerShareButton", "noPlayerSaveButton",
                        "noPlayerRemixButton", "noPlayerClipButton"},
                "video_action_bar.e"
        );

        buttonsById.addAll(
                new StringFilterGroup("noPlayerDownloadButton", "id.ui.add_to.offline.button"),
                new StringFilterGroup("noPlayerShareButton", "id.video.share.button"),
                new StringFilterGroup("noPlayerSaveButton", "id.video.add_to.button"),
                new StringFilterGroup("noPlayerRemixButton", "id.video.remix.button")
        );

        clipButton = new StringFilterGroup("noPlayerClipButton", "clip_button.e");

        buttonIcons.addAll(
                new ByteArrayFilterGroup("noPlayerDownloadButton",
                        "yt_outline_download_", "yt_outline_experimental_download_"),
                new ByteArrayFilterGroup("noPlayerShareButton",
                        "yt_outline_share_", "yt_outline_experimental_share_"),
                new ByteArrayFilterGroup("noPlayerSaveButton",
                        "yt_outline_bookmark_", "yt_outline_experimental_bookmark_", "yt_outline_list_add_"),
                new ByteArrayFilterGroup("noPlayerRemixButton",
                        "yt_outline_youtube_shorts_plus", "yt_outline_experimental_remix", "yt_outline_remix_"),
                new ByteArrayFilterGroup("noPlayerClipButton",
                        "yt_outline_scissors_", "yt_outline_experimental_scissors_")
        );

        addIdentifierCallbacks(actionBar);
    }

    @Override
    public boolean isFiltered(LithoComponent component, StringFilterGroup matchedGroup,
                              MatchType matchType, int matchIndex) {
        // Never the bar itself, only the components inside it.
        if (component.pathIndexOf(".e", component.identifier().length()) < 0) return false;

        // The "more" button lists the actions that did not fit, with their icons: never it.
        String name = component.componentName();
        if (name.startsWith("overflow_menu_button")) return false;

        if (buttonsById.matches(component.accessibility())) return true;
        if (clipButton.matches(name)) return true;

        // A button without a known id: tell it by its icon. Only a button itself, so the bar's
        // other parts are never encoded.
        return name.contains("button.e") && buttonIcons.matches(component);
    }
}
