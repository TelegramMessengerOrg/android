package org.telegram.ui.ActionBar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build.VERSION;
import android.os.SystemClock;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.StateSet;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.beta.R;
import org.telegram.messenger.support.widget.helper.ItemTouchHelper.Callback;
import org.telegram.messenger.time.SunDate;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.ThemeEditorView;

public class Theme {
    public static final int ACTION_BAR_AUDIO_SELECTOR_COLOR = 788529152;
    public static final int ACTION_BAR_MEDIA_PICKER_COLOR = -13421773;
    public static final int ACTION_BAR_PHOTO_VIEWER_COLOR = 2130706432;
    public static final int ACTION_BAR_PICKER_SELECTOR_COLOR = -12763843;
    public static final int ACTION_BAR_PLAYER_COLOR = -1;
    public static final int ACTION_BAR_VIDEO_EDIT_COLOR = -16777216;
    public static final int ACTION_BAR_WHITE_SELECTOR_COLOR = 1090519039;
    public static final int ARTICLE_VIEWER_MEDIA_PROGRESS_COLOR = -1;
    public static final int AUTO_NIGHT_TYPE_AUTOMATIC = 2;
    public static final int AUTO_NIGHT_TYPE_NONE = 0;
    public static final int AUTO_NIGHT_TYPE_SCHEDULED = 1;
    private static Field BitmapDrawable_mColorFilter = null;
    private static final int LIGHT_SENSOR_THEME_SWITCH_DELAY = 1800;
    private static final int LIGHT_SENSOR_THEME_SWITCH_NEAR_DELAY = 12000;
    private static final int LIGHT_SENSOR_THEME_SWITCH_NEAR_THRESHOLD = 12000;
    private static final float MAXIMUM_LUX_BREAKPOINT = 500.0f;
    private static Method StateListDrawable_getStateDrawableMethod = null;
    private static SensorEventListener ambientSensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            float lux = event.values[0];
            if (lux <= 0.0f) {
                lux = 0.1f;
            }
            if (!ApplicationLoader.mainInterfacePaused) {
                if (ApplicationLoader.isScreenOn) {
                    if (lux > Theme.MAXIMUM_LUX_BREAKPOINT) {
                        Theme.lastBrightnessValue = 1.0f;
                    } else {
                        Theme.lastBrightnessValue = ((float) Math.ceil((9.932299613952637d * Math.log((double) lux)) + 27.05900001525879d)) / 100.0f;
                    }
                    if (Theme.lastBrightnessValue > Theme.autoNightBrighnessThreshold) {
                        if (Theme.switchNightRunnableScheduled) {
                            Theme.switchNightRunnableScheduled = false;
                            AndroidUtilities.cancelRunOnUIThread(Theme.switchNightBrightnessRunnable);
                        }
                        if (!Theme.switchDayRunnableScheduled) {
                            Theme.switchDayRunnableScheduled = true;
                            AndroidUtilities.runOnUIThread(Theme.switchDayBrightnessRunnable, Theme.getAutoNightSwitchThemeDelay());
                        }
                    } else if (!MediaController.getInstance().isRecordingOrListeningByProximity()) {
                        if (Theme.switchDayRunnableScheduled) {
                            Theme.switchDayRunnableScheduled = false;
                            AndroidUtilities.cancelRunOnUIThread(Theme.switchDayBrightnessRunnable);
                        }
                        if (!Theme.switchNightRunnableScheduled) {
                            Theme.switchNightRunnableScheduled = true;
                            AndroidUtilities.runOnUIThread(Theme.switchNightBrightnessRunnable, Theme.getAutoNightSwitchThemeDelay());
                        }
                    }
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    public static float autoNightBrighnessThreshold = 0.0f;
    public static String autoNightCityName = null;
    public static int autoNightDayEndTime = 0;
    public static int autoNightDayStartTime = 0;
    public static int autoNightLastSunCheckDay = 0;
    public static double autoNightLocationLatitude = 0.0d;
    public static double autoNightLocationLongitude = 0.0d;
    public static boolean autoNightScheduleByLocation = false;
    public static int autoNightSunriseTime = 0;
    public static int autoNightSunsetTime = 0;
    public static Paint avatar_backgroundPaint = null;
    public static Drawable avatar_broadcastDrawable = null;
    public static Drawable avatar_photoDrawable = null;
    public static Drawable avatar_savedDrawable = null;
    private static boolean canStartHolidayAnimation = false;
    public static Paint chat_actionBackgroundPaint = null;
    public static TextPaint chat_actionTextPaint = null;
    public static TextPaint chat_adminPaint = null;
    public static Drawable[] chat_attachButtonDrawables = new Drawable[8];
    public static TextPaint chat_audioPerformerPaint = null;
    public static TextPaint chat_audioTimePaint = null;
    public static TextPaint chat_audioTitlePaint = null;
    public static TextPaint chat_botButtonPaint = null;
    public static Drawable chat_botInlineDrawable = null;
    public static Drawable chat_botLinkDrawalbe = null;
    public static Paint chat_botProgressPaint = null;
    public static Paint chat_composeBackgroundPaint = null;
    public static Drawable chat_composeShadowDrawable = null;
    public static Drawable[] chat_contactDrawable = new Drawable[2];
    public static TextPaint chat_contactNamePaint = null;
    public static TextPaint chat_contactPhonePaint = null;
    public static TextPaint chat_contextResult_descriptionTextPaint = null;
    public static Drawable chat_contextResult_shadowUnderSwitchDrawable = null;
    public static TextPaint chat_contextResult_titleTextPaint = null;
    public static Drawable[] chat_cornerInner = new Drawable[4];
    public static Drawable[] chat_cornerOuter = new Drawable[4];
    public static Paint chat_deleteProgressPaint = null;
    public static Paint chat_docBackPaint = null;
    public static TextPaint chat_docNamePaint = null;
    public static TextPaint chat_durationPaint = null;
    public static CombinedDrawable[][] chat_fileMiniStatesDrawable = ((CombinedDrawable[][]) Array.newInstance(CombinedDrawable.class, new int[]{6, 2}));
    public static Drawable[][] chat_fileStatesDrawable = ((Drawable[][]) Array.newInstance(Drawable.class, new int[]{10, 2}));
    public static TextPaint chat_forwardNamePaint = null;
    public static TextPaint chat_gamePaint = null;
    public static Drawable chat_goIconDrawable = null;
    public static TextPaint chat_infoPaint = null;
    public static Drawable chat_inlineResultAudio = null;
    public static Drawable chat_inlineResultFile = null;
    public static Drawable chat_inlineResultLocation = null;
    public static TextPaint chat_instantViewPaint = null;
    public static Paint chat_instantViewRectPaint = null;
    public static Drawable[][] chat_ivStatesDrawable = ((Drawable[][]) Array.newInstance(Drawable.class, new int[]{4, 2}));
    public static TextPaint chat_livePaint = null;
    public static TextPaint chat_locationAddressPaint = null;
    public static Drawable[] chat_locationDrawable = new Drawable[2];
    public static TextPaint chat_locationTitlePaint = null;
    public static Drawable chat_lockIconDrawable = null;
    public static Drawable chat_msgAvatarLiveLocationDrawable = null;
    public static TextPaint chat_msgBotButtonPaint = null;
    public static Drawable chat_msgBroadcastDrawable = null;
    public static Drawable chat_msgBroadcastMediaDrawable = null;
    public static Drawable chat_msgCallDownGreenDrawable = null;
    public static Drawable chat_msgCallDownRedDrawable = null;
    public static Drawable chat_msgCallUpGreenDrawable = null;
    public static Drawable chat_msgCallUpRedDrawable = null;
    public static Drawable chat_msgErrorDrawable = null;
    public static Paint chat_msgErrorPaint = null;
    public static TextPaint chat_msgGameTextPaint = null;
    public static Drawable chat_msgInCallDrawable = null;
    public static Drawable chat_msgInCallSelectedDrawable = null;
    public static Drawable chat_msgInClockDrawable = null;
    public static Drawable chat_msgInDrawable = null;
    public static Drawable chat_msgInInstantDrawable = null;
    public static Drawable chat_msgInMediaDrawable = null;
    public static Drawable chat_msgInMediaSelectedDrawable = null;
    public static Drawable chat_msgInMediaShadowDrawable = null;
    public static Drawable chat_msgInMenuDrawable = null;
    public static Drawable chat_msgInMenuSelectedDrawable = null;
    public static Drawable chat_msgInSelectedClockDrawable = null;
    public static Drawable chat_msgInSelectedDrawable = null;
    public static Drawable chat_msgInShadowDrawable = null;
    public static Drawable chat_msgInViewsDrawable = null;
    public static Drawable chat_msgInViewsSelectedDrawable = null;
    public static Drawable chat_msgMediaBroadcastDrawable = null;
    public static Drawable chat_msgMediaCheckDrawable = null;
    public static Drawable chat_msgMediaClockDrawable = null;
    public static Drawable chat_msgMediaHalfCheckDrawable = null;
    public static Drawable chat_msgMediaMenuDrawable = null;
    public static Drawable chat_msgMediaViewsDrawable = null;
    public static Drawable chat_msgOutBroadcastDrawable = null;
    public static Drawable chat_msgOutCallDrawable = null;
    public static Drawable chat_msgOutCallSelectedDrawable = null;
    public static Drawable chat_msgOutCheckDrawable = null;
    public static Drawable chat_msgOutCheckSelectedDrawable = null;
    public static Drawable chat_msgOutClockDrawable = null;
    public static Drawable chat_msgOutDrawable = null;
    public static Drawable chat_msgOutHalfCheckDrawable = null;
    public static Drawable chat_msgOutHalfCheckSelectedDrawable = null;
    public static Drawable chat_msgOutInstantDrawable = null;
    public static Drawable chat_msgOutLocationDrawable = null;
    public static Drawable chat_msgOutMediaDrawable = null;
    public static Drawable chat_msgOutMediaSelectedDrawable = null;
    public static Drawable chat_msgOutMediaShadowDrawable = null;
    public static Drawable chat_msgOutMenuDrawable = null;
    public static Drawable chat_msgOutMenuSelectedDrawable = null;
    public static Drawable chat_msgOutSelectedClockDrawable = null;
    public static Drawable chat_msgOutSelectedDrawable = null;
    public static Drawable chat_msgOutShadowDrawable = null;
    public static Drawable chat_msgOutViewsDrawable = null;
    public static Drawable chat_msgOutViewsSelectedDrawable = null;
    public static Drawable chat_msgStickerCheckDrawable = null;
    public static Drawable chat_msgStickerClockDrawable = null;
    public static Drawable chat_msgStickerHalfCheckDrawable = null;
    public static Drawable chat_msgStickerViewsDrawable = null;
    public static TextPaint chat_msgTextPaint = null;
    public static TextPaint chat_msgTextPaintOneEmoji = null;
    public static TextPaint chat_msgTextPaintThreeEmoji = null;
    public static TextPaint chat_msgTextPaintTwoEmoji = null;
    public static Drawable chat_muteIconDrawable = null;
    public static TextPaint chat_namePaint = null;
    public static Drawable[][] chat_photoStatesDrawables = ((Drawable[][]) Array.newInstance(Drawable.class, new int[]{13, 2}));
    public static Paint chat_radialProgress2Paint = null;
    public static Paint chat_radialProgressPaint = null;
    public static Drawable chat_replyIconDrawable = null;
    public static Paint chat_replyLinePaint = null;
    public static TextPaint chat_replyNamePaint = null;
    public static TextPaint chat_replyTextPaint = null;
    public static Drawable chat_roundVideoShadow = null;
    public static Drawable chat_shareDrawable = null;
    public static Drawable chat_shareIconDrawable = null;
    public static TextPaint chat_shipmentPaint = null;
    public static Paint chat_statusPaint = null;
    public static Paint chat_statusRecordPaint = null;
    public static Drawable chat_systemDrawable = null;
    public static Paint chat_textSearchSelectionPaint = null;
    public static Paint chat_timeBackgroundPaint = null;
    public static TextPaint chat_timePaint = null;
    public static Paint chat_urlPaint = null;
    public static Paint checkboxSquare_backgroundPaint = null;
    public static Paint checkboxSquare_checkPaint = null;
    public static Paint checkboxSquare_eraserPaint = null;
    public static PorterDuffColorFilter colorFilter = null;
    public static PorterDuffColorFilter colorPressedFilter = null;
    private static int currentColor = 0;
    private static HashMap<String, Integer> currentColors = new HashMap();
    private static ThemeInfo currentDayTheme = null;
    private static ThemeInfo currentNightTheme = null;
    private static int currentSelectedColor = 0;
    private static ThemeInfo currentTheme = null;
    private static HashMap<String, Integer> defaultColors = new HashMap();
    private static ThemeInfo defaultTheme = null;
    public static Drawable dialogs_botDrawable = null;
    public static Drawable dialogs_broadcastDrawable = null;
    public static Drawable dialogs_checkDrawable = null;
    public static Drawable dialogs_clockDrawable = null;
    public static Paint dialogs_countGrayPaint = null;
    public static Paint dialogs_countPaint = null;
    public static TextPaint dialogs_countTextPaint = null;
    public static Drawable dialogs_errorDrawable = null;
    public static Paint dialogs_errorPaint = null;
    public static Drawable dialogs_groupDrawable = null;
    public static Drawable dialogs_halfCheckDrawable = null;
    private static Drawable dialogs_holidayDrawable = null;
    private static int dialogs_holidayDrawableOffsetX = 0;
    private static int dialogs_holidayDrawableOffsetY = 0;
    public static Drawable dialogs_lockDrawable = null;
    public static Drawable dialogs_mentionDrawable = null;
    public static TextPaint dialogs_messagePaint = null;
    public static TextPaint dialogs_messagePrintingPaint = null;
    public static Drawable dialogs_muteDrawable = null;
    public static TextPaint dialogs_nameEncryptedPaint = null;
    public static TextPaint dialogs_namePaint = null;
    public static TextPaint dialogs_offlinePaint = null;
    public static TextPaint dialogs_onlinePaint = null;
    public static Drawable dialogs_pinnedDrawable = null;
    public static Paint dialogs_pinnedPaint = null;
    public static Paint dialogs_tabletSeletedPaint = null;
    public static TextPaint dialogs_timePaint = null;
    public static Drawable dialogs_verifiedCheckDrawable = null;
    public static Drawable dialogs_verifiedDrawable = null;
    public static Paint dividerPaint = null;
    private static HashMap<String, String> fallbackKeys = new HashMap();
    private static boolean isCustomTheme = false;
    public static final String key_actionBarActionModeDefault = "actionBarActionModeDefault";
    public static final String key_actionBarActionModeDefaultIcon = "actionBarActionModeDefaultIcon";
    public static final String key_actionBarActionModeDefaultSelector = "actionBarActionModeDefaultSelector";
    public static final String key_actionBarActionModeDefaultTop = "actionBarActionModeDefaultTop";
    public static final String key_actionBarDefault = "actionBarDefault";
    public static final String key_actionBarDefaultIcon = "actionBarDefaultIcon";
    public static final String key_actionBarDefaultSearch = "actionBarDefaultSearch";
    public static final String key_actionBarDefaultSearchPlaceholder = "actionBarDefaultSearchPlaceholder";
    public static final String key_actionBarDefaultSelector = "actionBarDefaultSelector";
    public static final String key_actionBarDefaultSubmenuBackground = "actionBarDefaultSubmenuBackground";
    public static final String key_actionBarDefaultSubmenuItem = "actionBarDefaultSubmenuItem";
    public static final String key_actionBarDefaultSubtitle = "actionBarDefaultSubtitle";
    public static final String key_actionBarDefaultTitle = "actionBarDefaultTitle";
    public static final String key_actionBarWhiteSelector = "actionBarWhiteSelector";
    public static final String key_avatar_actionBarIconBlue = "avatar_actionBarIconBlue";
    public static final String key_avatar_actionBarIconCyan = "avatar_actionBarIconCyan";
    public static final String key_avatar_actionBarIconGreen = "avatar_actionBarIconGreen";
    public static final String key_avatar_actionBarIconOrange = "avatar_actionBarIconOrange";
    public static final String key_avatar_actionBarIconPink = "avatar_actionBarIconPink";
    public static final String key_avatar_actionBarIconRed = "avatar_actionBarIconRed";
    public static final String key_avatar_actionBarIconViolet = "avatar_actionBarIconViolet";
    public static final String key_avatar_actionBarSelectorBlue = "avatar_actionBarSelectorBlue";
    public static final String key_avatar_actionBarSelectorCyan = "avatar_actionBarSelectorCyan";
    public static final String key_avatar_actionBarSelectorGreen = "avatar_actionBarSelectorGreen";
    public static final String key_avatar_actionBarSelectorOrange = "avatar_actionBarSelectorOrange";
    public static final String key_avatar_actionBarSelectorPink = "avatar_actionBarSelectorPink";
    public static final String key_avatar_actionBarSelectorRed = "avatar_actionBarSelectorRed";
    public static final String key_avatar_actionBarSelectorViolet = "avatar_actionBarSelectorViolet";
    public static final String key_avatar_backgroundActionBarBlue = "avatar_backgroundActionBarBlue";
    public static final String key_avatar_backgroundActionBarCyan = "avatar_backgroundActionBarCyan";
    public static final String key_avatar_backgroundActionBarGreen = "avatar_backgroundActionBarGreen";
    public static final String key_avatar_backgroundActionBarOrange = "avatar_backgroundActionBarOrange";
    public static final String key_avatar_backgroundActionBarPink = "avatar_backgroundActionBarPink";
    public static final String key_avatar_backgroundActionBarRed = "avatar_backgroundActionBarRed";
    public static final String key_avatar_backgroundActionBarViolet = "avatar_backgroundActionBarViolet";
    public static final String key_avatar_backgroundBlue = "avatar_backgroundBlue";
    public static final String key_avatar_backgroundCyan = "avatar_backgroundCyan";
    public static final String key_avatar_backgroundGreen = "avatar_backgroundGreen";
    public static final String key_avatar_backgroundGroupCreateSpanBlue = "avatar_backgroundGroupCreateSpanBlue";
    public static final String key_avatar_backgroundInProfileBlue = "avatar_backgroundInProfileBlue";
    public static final String key_avatar_backgroundInProfileCyan = "avatar_backgroundInProfileCyan";
    public static final String key_avatar_backgroundInProfileGreen = "avatar_backgroundInProfileGreen";
    public static final String key_avatar_backgroundInProfileOrange = "avatar_backgroundInProfileOrange";
    public static final String key_avatar_backgroundInProfilePink = "avatar_backgroundInProfilePink";
    public static final String key_avatar_backgroundInProfileRed = "avatar_backgroundInProfileRed";
    public static final String key_avatar_backgroundInProfileViolet = "avatar_backgroundInProfileViolet";
    public static final String key_avatar_backgroundOrange = "avatar_backgroundOrange";
    public static final String key_avatar_backgroundPink = "avatar_backgroundPink";
    public static final String key_avatar_backgroundRed = "avatar_backgroundRed";
    public static final String key_avatar_backgroundSaved = "avatar_backgroundSaved";
    public static final String key_avatar_backgroundViolet = "avatar_backgroundViolet";
    public static final String key_avatar_nameInMessageBlue = "avatar_nameInMessageBlue";
    public static final String key_avatar_nameInMessageCyan = "avatar_nameInMessageCyan";
    public static final String key_avatar_nameInMessageGreen = "avatar_nameInMessageGreen";
    public static final String key_avatar_nameInMessageOrange = "avatar_nameInMessageOrange";
    public static final String key_avatar_nameInMessagePink = "avatar_nameInMessagePink";
    public static final String key_avatar_nameInMessageRed = "avatar_nameInMessageRed";
    public static final String key_avatar_nameInMessageViolet = "avatar_nameInMessageViolet";
    public static final String key_avatar_subtitleInProfileBlue = "avatar_subtitleInProfileBlue";
    public static final String key_avatar_subtitleInProfileCyan = "avatar_subtitleInProfileCyan";
    public static final String key_avatar_subtitleInProfileGreen = "avatar_subtitleInProfileGreen";
    public static final String key_avatar_subtitleInProfileOrange = "avatar_subtitleInProfileOrange";
    public static final String key_avatar_subtitleInProfilePink = "avatar_subtitleInProfilePink";
    public static final String key_avatar_subtitleInProfileRed = "avatar_subtitleInProfileRed";
    public static final String key_avatar_subtitleInProfileViolet = "avatar_subtitleInProfileViolet";
    public static final String key_avatar_text = "avatar_text";
    public static final String key_calls_callReceivedGreenIcon = "calls_callReceivedGreenIcon";
    public static final String key_calls_callReceivedRedIcon = "calls_callReceivedRedIcon";
    public static final String key_calls_ratingStar = "calls_ratingStar";
    public static final String key_calls_ratingStarSelected = "calls_ratingStarSelected";
    public static final String key_changephoneinfo_image = "changephoneinfo_image";
    public static final String key_chat_addContact = "chat_addContact";
    public static final String key_chat_adminSelectedText = "chat_adminSelectedText";
    public static final String key_chat_adminText = "chat_adminText";
    public static final String key_chat_botButtonText = "chat_botButtonText";
    public static final String key_chat_botKeyboardButtonBackground = "chat_botKeyboardButtonBackground";
    public static final String key_chat_botKeyboardButtonBackgroundPressed = "chat_botKeyboardButtonBackgroundPressed";
    public static final String key_chat_botKeyboardButtonText = "chat_botKeyboardButtonText";
    public static final String key_chat_botProgress = "chat_botProgress";
    public static final String key_chat_botSwitchToInlineText = "chat_botSwitchToInlineText";
    public static final String key_chat_editDoneIcon = "chat_editDoneIcon";
    public static final String key_chat_emojiPanelBackground = "chat_emojiPanelBackground";
    public static final String key_chat_emojiPanelBackspace = "chat_emojiPanelBackspace";
    public static final String key_chat_emojiPanelEmptyText = "chat_emojiPanelEmptyText";
    public static final String key_chat_emojiPanelIcon = "chat_emojiPanelIcon";
    public static final String key_chat_emojiPanelIconSelected = "chat_emojiPanelIconSelected";
    public static final String key_chat_emojiPanelIconSelector = "chat_emojiPanelIconSelector";
    public static final String key_chat_emojiPanelMasksIcon = "chat_emojiPanelMasksIcon";
    public static final String key_chat_emojiPanelMasksIconSelected = "chat_emojiPanelMasksIconSelected";
    public static final String key_chat_emojiPanelNewTrending = "chat_emojiPanelNewTrending";
    public static final String key_chat_emojiPanelShadowLine = "chat_emojiPanelShadowLine";
    public static final String key_chat_emojiPanelStickerPackSelector = "chat_emojiPanelStickerPackSelector";
    public static final String key_chat_emojiPanelStickerSetName = "chat_emojiPanelStickerSetName";
    public static final String key_chat_emojiPanelStickerSetNameIcon = "chat_emojiPanelStickerSetNameIcon";
    public static final String key_chat_emojiPanelTrendingDescription = "chat_emojiPanelTrendingDescription";
    public static final String key_chat_emojiPanelTrendingTitle = "chat_emojiPanelTrendingTitle";
    public static final String key_chat_emojiSearchBackground = "chat_emojiSearchBackground";
    public static final String key_chat_fieldOverlayText = "chat_fieldOverlayText";
    public static final String key_chat_gifSaveHintBackground = "chat_gifSaveHintBackground";
    public static final String key_chat_gifSaveHintText = "chat_gifSaveHintText";
    public static final String key_chat_goDownButton = "chat_goDownButton";
    public static final String key_chat_goDownButtonCounter = "chat_goDownButtonCounter";
    public static final String key_chat_goDownButtonCounterBackground = "chat_goDownButtonCounterBackground";
    public static final String key_chat_goDownButtonIcon = "chat_goDownButtonIcon";
    public static final String key_chat_goDownButtonShadow = "chat_goDownButtonShadow";
    public static final String key_chat_inAudioCacheSeekbar = "chat_inAudioCacheSeekbar";
    public static final String key_chat_inAudioDurationSelectedText = "chat_inAudioDurationSelectedText";
    public static final String key_chat_inAudioDurationText = "chat_inAudioDurationText";
    public static final String key_chat_inAudioPerfomerText = "chat_inAudioPerfomerText";
    public static final String key_chat_inAudioProgress = "chat_inAudioProgress";
    public static final String key_chat_inAudioSeekbar = "chat_inAudioSeekbar";
    public static final String key_chat_inAudioSeekbarFill = "chat_inAudioSeekbarFill";
    public static final String key_chat_inAudioSeekbarSelected = "chat_inAudioSeekbarSelected";
    public static final String key_chat_inAudioSelectedProgress = "chat_inAudioSelectedProgress";
    public static final String key_chat_inAudioTitleText = "chat_inAudioTitleText";
    public static final String key_chat_inBubble = "chat_inBubble";
    public static final String key_chat_inBubbleSelected = "chat_inBubbleSelected";
    public static final String key_chat_inBubbleShadow = "chat_inBubbleShadow";
    public static final String key_chat_inContactBackground = "chat_inContactBackground";
    public static final String key_chat_inContactIcon = "chat_inContactIcon";
    public static final String key_chat_inContactNameText = "chat_inContactNameText";
    public static final String key_chat_inContactPhoneText = "chat_inContactPhoneText";
    public static final String key_chat_inFileBackground = "chat_inFileBackground";
    public static final String key_chat_inFileBackgroundSelected = "chat_inFileBackgroundSelected";
    public static final String key_chat_inFileIcon = "chat_inFileIcon";
    public static final String key_chat_inFileInfoSelectedText = "chat_inFileInfoSelectedText";
    public static final String key_chat_inFileInfoText = "chat_inFileInfoText";
    public static final String key_chat_inFileNameText = "chat_inFileNameText";
    public static final String key_chat_inFileProgress = "chat_inFileProgress";
    public static final String key_chat_inFileProgressSelected = "chat_inFileProgressSelected";
    public static final String key_chat_inFileSelectedIcon = "chat_inFileSelectedIcon";
    public static final String key_chat_inForwardedNameText = "chat_inForwardedNameText";
    public static final String key_chat_inInstant = "chat_inInstant";
    public static final String key_chat_inInstantSelected = "chat_inInstantSelected";
    public static final String key_chat_inLoader = "chat_inLoader";
    public static final String key_chat_inLoaderPhoto = "chat_inLoaderPhoto";
    public static final String key_chat_inLoaderPhotoIcon = "chat_inLoaderPhotoIcon";
    public static final String key_chat_inLoaderPhotoIconSelected = "chat_inLoaderPhotoIconSelected";
    public static final String key_chat_inLoaderPhotoSelected = "chat_inLoaderPhotoSelected";
    public static final String key_chat_inLoaderSelected = "chat_inLoaderSelected";
    public static final String key_chat_inLocationBackground = "chat_inLocationBackground";
    public static final String key_chat_inLocationIcon = "chat_inLocationIcon";
    public static final String key_chat_inMenu = "chat_inMenu";
    public static final String key_chat_inMenuSelected = "chat_inMenuSelected";
    public static final String key_chat_inPreviewInstantSelectedText = "chat_inPreviewInstantSelectedText";
    public static final String key_chat_inPreviewInstantText = "chat_inPreviewInstantText";
    public static final String key_chat_inPreviewLine = "chat_inPreviewLine";
    public static final String key_chat_inReplyLine = "chat_inReplyLine";
    public static final String key_chat_inReplyMediaMessageSelectedText = "chat_inReplyMediaMessageSelectedText";
    public static final String key_chat_inReplyMediaMessageText = "chat_inReplyMediaMessageText";
    public static final String key_chat_inReplyMessageText = "chat_inReplyMessageText";
    public static final String key_chat_inReplyNameText = "chat_inReplyNameText";
    public static final String key_chat_inSentClock = "chat_inSentClock";
    public static final String key_chat_inSentClockSelected = "chat_inSentClockSelected";
    public static final String key_chat_inSiteNameText = "chat_inSiteNameText";
    public static final String key_chat_inTimeSelectedText = "chat_inTimeSelectedText";
    public static final String key_chat_inTimeText = "chat_inTimeText";
    public static final String key_chat_inVenueInfoSelectedText = "chat_inVenueInfoSelectedText";
    public static final String key_chat_inVenueInfoText = "chat_inVenueInfoText";
    public static final String key_chat_inVenueNameText = "chat_inVenueNameText";
    public static final String key_chat_inViaBotNameText = "chat_inViaBotNameText";
    public static final String key_chat_inViews = "chat_inViews";
    public static final String key_chat_inViewsSelected = "chat_inViewsSelected";
    public static final String key_chat_inVoiceSeekbar = "chat_inVoiceSeekbar";
    public static final String key_chat_inVoiceSeekbarFill = "chat_inVoiceSeekbarFill";
    public static final String key_chat_inVoiceSeekbarSelected = "chat_inVoiceSeekbarSelected";
    public static final String key_chat_inlineResultIcon = "chat_inlineResultIcon";
    public static final String key_chat_linkSelectBackground = "chat_linkSelectBackground";
    public static final String key_chat_lockIcon = "chat_lockIcon";
    public static final String key_chat_mediaBroadcast = "chat_mediaBroadcast";
    public static final String key_chat_mediaInfoText = "chat_mediaInfoText";
    public static final String key_chat_mediaLoaderPhoto = "chat_mediaLoaderPhoto";
    public static final String key_chat_mediaLoaderPhotoIcon = "chat_mediaLoaderPhotoIcon";
    public static final String key_chat_mediaLoaderPhotoIconSelected = "chat_mediaLoaderPhotoIconSelected";
    public static final String key_chat_mediaLoaderPhotoSelected = "chat_mediaLoaderPhotoSelected";
    public static final String key_chat_mediaMenu = "chat_mediaMenu";
    public static final String key_chat_mediaProgress = "chat_mediaProgress";
    public static final String key_chat_mediaSentCheck = "chat_mediaSentCheck";
    public static final String key_chat_mediaSentClock = "chat_mediaSentClock";
    public static final String key_chat_mediaTimeBackground = "chat_mediaTimeBackground";
    public static final String key_chat_mediaTimeText = "chat_mediaTimeText";
    public static final String key_chat_mediaViews = "chat_mediaViews";
    public static final String key_chat_messageLinkIn = "chat_messageLinkIn";
    public static final String key_chat_messageLinkOut = "chat_messageLinkOut";
    public static final String key_chat_messagePanelBackground = "chat_messagePanelBackground";
    public static final String key_chat_messagePanelCancelInlineBot = "chat_messagePanelCancelInlineBot";
    public static final String key_chat_messagePanelHint = "chat_messagePanelHint";
    public static final String key_chat_messagePanelIcons = "chat_messagePanelIcons";
    public static final String key_chat_messagePanelSend = "chat_messagePanelSend";
    public static final String key_chat_messagePanelShadow = "chat_messagePanelShadow";
    public static final String key_chat_messagePanelText = "chat_messagePanelText";
    public static final String key_chat_messagePanelVoiceBackground = "chat_messagePanelVoiceBackground";
    public static final String key_chat_messagePanelVoiceDelete = "chat_messagePanelVoiceDelete";
    public static final String key_chat_messagePanelVoiceDuration = "chat_messagePanelVoiceDuration";
    public static final String key_chat_messagePanelVoiceLock = "key_chat_messagePanelVoiceLock";
    public static final String key_chat_messagePanelVoiceLockBackground = "key_chat_messagePanelVoiceLockBackground";
    public static final String key_chat_messagePanelVoiceLockShadow = "key_chat_messagePanelVoiceLockShadow";
    public static final String key_chat_messagePanelVoicePressed = "chat_messagePanelVoicePressed";
    public static final String key_chat_messagePanelVoiceShadow = "chat_messagePanelVoiceShadow";
    public static final String key_chat_messageTextIn = "chat_messageTextIn";
    public static final String key_chat_messageTextOut = "chat_messageTextOut";
    public static final String key_chat_muteIcon = "chat_muteIcon";
    public static final String key_chat_outAudioCacheSeekbar = "chat_outAudioCacheSeekbar";
    public static final String key_chat_outAudioDurationSelectedText = "chat_outAudioDurationSelectedText";
    public static final String key_chat_outAudioDurationText = "chat_outAudioDurationText";
    public static final String key_chat_outAudioPerfomerText = "chat_outAudioPerfomerText";
    public static final String key_chat_outAudioProgress = "chat_outAudioProgress";
    public static final String key_chat_outAudioSeekbar = "chat_outAudioSeekbar";
    public static final String key_chat_outAudioSeekbarFill = "chat_outAudioSeekbarFill";
    public static final String key_chat_outAudioSeekbarSelected = "chat_outAudioSeekbarSelected";
    public static final String key_chat_outAudioSelectedProgress = "chat_outAudioSelectedProgress";
    public static final String key_chat_outAudioTitleText = "chat_outAudioTitleText";
    public static final String key_chat_outBroadcast = "chat_outBroadcast";
    public static final String key_chat_outBubble = "chat_outBubble";
    public static final String key_chat_outBubbleSelected = "chat_outBubbleSelected";
    public static final String key_chat_outBubbleShadow = "chat_outBubbleShadow";
    public static final String key_chat_outContactBackground = "chat_outContactBackground";
    public static final String key_chat_outContactIcon = "chat_outContactIcon";
    public static final String key_chat_outContactNameText = "chat_outContactNameText";
    public static final String key_chat_outContactPhoneText = "chat_outContactPhoneText";
    public static final String key_chat_outFileBackground = "chat_outFileBackground";
    public static final String key_chat_outFileBackgroundSelected = "chat_outFileBackgroundSelected";
    public static final String key_chat_outFileIcon = "chat_outFileIcon";
    public static final String key_chat_outFileInfoSelectedText = "chat_outFileInfoSelectedText";
    public static final String key_chat_outFileInfoText = "chat_outFileInfoText";
    public static final String key_chat_outFileNameText = "chat_outFileNameText";
    public static final String key_chat_outFileProgress = "chat_outFileProgress";
    public static final String key_chat_outFileProgressSelected = "chat_outFileProgressSelected";
    public static final String key_chat_outFileSelectedIcon = "chat_outFileSelectedIcon";
    public static final String key_chat_outForwardedNameText = "chat_outForwardedNameText";
    public static final String key_chat_outInstant = "chat_outInstant";
    public static final String key_chat_outInstantSelected = "chat_outInstantSelected";
    public static final String key_chat_outLoader = "chat_outLoader";
    public static final String key_chat_outLoaderPhoto = "chat_outLoaderPhoto";
    public static final String key_chat_outLoaderPhotoIcon = "chat_outLoaderPhotoIcon";
    public static final String key_chat_outLoaderPhotoIconSelected = "chat_outLoaderPhotoIconSelected";
    public static final String key_chat_outLoaderPhotoSelected = "chat_outLoaderPhotoSelected";
    public static final String key_chat_outLoaderSelected = "chat_outLoaderSelected";
    public static final String key_chat_outLocationBackground = "chat_outLocationBackground";
    public static final String key_chat_outLocationIcon = "chat_outLocationIcon";
    public static final String key_chat_outMenu = "chat_outMenu";
    public static final String key_chat_outMenuSelected = "chat_outMenuSelected";
    public static final String key_chat_outPreviewInstantSelectedText = "chat_outPreviewInstantSelectedText";
    public static final String key_chat_outPreviewInstantText = "chat_outPreviewInstantText";
    public static final String key_chat_outPreviewLine = "chat_outPreviewLine";
    public static final String key_chat_outReplyLine = "chat_outReplyLine";
    public static final String key_chat_outReplyMediaMessageSelectedText = "chat_outReplyMediaMessageSelectedText";
    public static final String key_chat_outReplyMediaMessageText = "chat_outReplyMediaMessageText";
    public static final String key_chat_outReplyMessageText = "chat_outReplyMessageText";
    public static final String key_chat_outReplyNameText = "chat_outReplyNameText";
    public static final String key_chat_outSentCheck = "chat_outSentCheck";
    public static final String key_chat_outSentCheckSelected = "chat_outSentCheckSelected";
    public static final String key_chat_outSentClock = "chat_outSentClock";
    public static final String key_chat_outSentClockSelected = "chat_outSentClockSelected";
    public static final String key_chat_outSiteNameText = "chat_outSiteNameText";
    public static final String key_chat_outTimeSelectedText = "chat_outTimeSelectedText";
    public static final String key_chat_outTimeText = "chat_outTimeText";
    public static final String key_chat_outVenueInfoSelectedText = "chat_outVenueInfoSelectedText";
    public static final String key_chat_outVenueInfoText = "chat_outVenueInfoText";
    public static final String key_chat_outVenueNameText = "chat_outVenueNameText";
    public static final String key_chat_outViaBotNameText = "chat_outViaBotNameText";
    public static final String key_chat_outViews = "chat_outViews";
    public static final String key_chat_outViewsSelected = "chat_outViewsSelected";
    public static final String key_chat_outVoiceSeekbar = "chat_outVoiceSeekbar";
    public static final String key_chat_outVoiceSeekbarFill = "chat_outVoiceSeekbarFill";
    public static final String key_chat_outVoiceSeekbarSelected = "chat_outVoiceSeekbarSelected";
    public static final String key_chat_previewDurationText = "chat_previewDurationText";
    public static final String key_chat_previewGameText = "chat_previewGameText";
    public static final String key_chat_recordTime = "chat_recordTime";
    public static final String key_chat_recordVoiceCancel = "chat_recordVoiceCancel";
    public static final String key_chat_recordedVoiceBackground = "chat_recordedVoiceBackground";
    public static final String key_chat_recordedVoiceDot = "chat_recordedVoiceDot";
    public static final String key_chat_recordedVoicePlayPause = "chat_recordedVoicePlayPause";
    public static final String key_chat_recordedVoicePlayPausePressed = "chat_recordedVoicePlayPausePressed";
    public static final String key_chat_recordedVoiceProgress = "chat_recordedVoiceProgress";
    public static final String key_chat_recordedVoiceProgressInner = "chat_recordedVoiceProgressInner";
    public static final String key_chat_replyPanelClose = "chat_replyPanelClose";
    public static final String key_chat_replyPanelIcons = "chat_replyPanelIcons";
    public static final String key_chat_replyPanelLine = "chat_replyPanelLine";
    public static final String key_chat_replyPanelMessage = "chat_replyPanelMessage";
    public static final String key_chat_replyPanelName = "chat_replyPanelName";
    public static final String key_chat_reportSpam = "chat_reportSpam";
    public static final String key_chat_searchPanelIcons = "chat_searchPanelIcons";
    public static final String key_chat_searchPanelText = "chat_searchPanelText";
    public static final String key_chat_secretChatStatusText = "chat_secretChatStatusText";
    public static final String key_chat_secretTimeText = "chat_secretTimeText";
    public static final String key_chat_secretTimerBackground = "chat_secretTimerBackground";
    public static final String key_chat_secretTimerText = "chat_secretTimerText";
    public static final String key_chat_selectedBackground = "chat_selectedBackground";
    public static final String key_chat_sentError = "chat_sentError";
    public static final String key_chat_sentErrorIcon = "chat_sentErrorIcon";
    public static final String key_chat_serviceBackground = "chat_serviceBackground";
    public static final String key_chat_serviceBackgroundSelected = "chat_serviceBackgroundSelected";
    public static final String key_chat_serviceIcon = "chat_serviceIcon";
    public static final String key_chat_serviceLink = "chat_serviceLink";
    public static final String key_chat_serviceText = "chat_serviceText";
    public static final String key_chat_stickerNameText = "chat_stickerNameText";
    public static final String key_chat_stickerReplyLine = "chat_stickerReplyLine";
    public static final String key_chat_stickerReplyMessageText = "chat_stickerReplyMessageText";
    public static final String key_chat_stickerReplyNameText = "chat_stickerReplyNameText";
    public static final String key_chat_stickerViaBotNameText = "chat_stickerViaBotNameText";
    public static final String key_chat_stickersHintPanel = "chat_stickersHintPanel";
    public static final String key_chat_textSelectBackground = "chat_textSelectBackground";
    public static final String key_chat_topPanelBackground = "chat_topPanelBackground";
    public static final String key_chat_topPanelClose = "chat_topPanelClose";
    public static final String key_chat_topPanelLine = "chat_topPanelLine";
    public static final String key_chat_topPanelMessage = "chat_topPanelMessage";
    public static final String key_chat_topPanelTitle = "chat_topPanelTitle";
    public static final String key_chat_unreadMessagesStartArrowIcon = "chat_unreadMessagesStartArrowIcon";
    public static final String key_chat_unreadMessagesStartBackground = "chat_unreadMessagesStartBackground";
    public static final String key_chat_unreadMessagesStartText = "chat_unreadMessagesStartText";
    public static final String key_chat_wallpaper = "chat_wallpaper";
    public static final String key_chats_actionBackground = "chats_actionBackground";
    public static final String key_chats_actionIcon = "chats_actionIcon";
    public static final String key_chats_actionMessage = "chats_actionMessage";
    public static final String key_chats_actionPressedBackground = "chats_actionPressedBackground";
    public static final String key_chats_attachMessage = "chats_attachMessage";
    public static final String key_chats_date = "chats_date";
    public static final String key_chats_draft = "chats_draft";
    public static final String key_chats_menuBackground = "chats_menuBackground";
    public static final String key_chats_menuCloud = "chats_menuCloud";
    public static final String key_chats_menuCloudBackgroundCats = "chats_menuCloudBackgroundCats";
    public static final String key_chats_menuItemCheck = "chats_menuItemCheck";
    public static final String key_chats_menuItemIcon = "chats_menuItemIcon";
    public static final String key_chats_menuItemText = "chats_menuItemText";
    public static final String key_chats_menuName = "chats_menuName";
    public static final String key_chats_menuPhone = "chats_menuPhone";
    public static final String key_chats_menuPhoneCats = "chats_menuPhoneCats";
    public static final String key_chats_menuTopShadow = "chats_menuTopShadow";
    public static final String key_chats_message = "chats_message";
    public static final String key_chats_muteIcon = "chats_muteIcon";
    public static final String key_chats_name = "chats_name";
    public static final String key_chats_nameIcon = "chats_nameIcon";
    public static final String key_chats_nameMessage = "chats_nameMessage";
    public static final String key_chats_pinnedIcon = "chats_pinnedIcon";
    public static final String key_chats_pinnedOverlay = "chats_pinnedOverlay";
    public static final String key_chats_secretIcon = "chats_secretIcon";
    public static final String key_chats_secretName = "chats_secretName";
    public static final String key_chats_sentCheck = "chats_sentCheck";
    public static final String key_chats_sentClock = "chats_sentClock";
    public static final String key_chats_sentError = "chats_sentError";
    public static final String key_chats_sentErrorIcon = "chats_sentErrorIcon";
    public static final String key_chats_tabletSelectedOverlay = "chats_tabletSelectedOverlay";
    public static final String key_chats_unreadCounter = "chats_unreadCounter";
    public static final String key_chats_unreadCounterMuted = "chats_unreadCounterMuted";
    public static final String key_chats_unreadCounterText = "chats_unreadCounterText";
    public static final String key_chats_verifiedBackground = "chats_verifiedBackground";
    public static final String key_chats_verifiedCheck = "chats_verifiedCheck";
    public static final String key_checkbox = "checkbox";
    public static final String key_checkboxCheck = "checkboxCheck";
    public static final String key_checkboxSquareBackground = "checkboxSquareBackground";
    public static final String key_checkboxSquareCheck = "checkboxSquareCheck";
    public static final String key_checkboxSquareDisabled = "checkboxSquareDisabled";
    public static final String key_checkboxSquareUnchecked = "checkboxSquareUnchecked";
    public static final String key_contacts_inviteBackground = "contacts_inviteBackground";
    public static final String key_contacts_inviteText = "contacts_inviteText";
    public static final String key_contextProgressInner1 = "contextProgressInner1";
    public static final String key_contextProgressInner2 = "contextProgressInner2";
    public static final String key_contextProgressInner3 = "contextProgressInner3";
    public static final String key_contextProgressOuter1 = "contextProgressOuter1";
    public static final String key_contextProgressOuter2 = "contextProgressOuter2";
    public static final String key_contextProgressOuter3 = "contextProgressOuter3";
    public static final String key_dialogBackground = "dialogBackground";
    public static final String key_dialogBackgroundGray = "dialogBackgroundGray";
    public static final String key_dialogBadgeBackground = "dialogBadgeBackground";
    public static final String key_dialogBadgeText = "dialogBadgeText";
    public static final String key_dialogButton = "dialogButton";
    public static final String key_dialogButtonSelector = "dialogButtonSelector";
    public static final String key_dialogCheckboxSquareBackground = "dialogCheckboxSquareBackground";
    public static final String key_dialogCheckboxSquareCheck = "dialogCheckboxSquareCheck";
    public static final String key_dialogCheckboxSquareDisabled = "dialogCheckboxSquareDisabled";
    public static final String key_dialogCheckboxSquareUnchecked = "dialogCheckboxSquareUnchecked";
    public static final String key_dialogGrayLine = "dialogGrayLine";
    public static final String key_dialogIcon = "dialogIcon";
    public static final String key_dialogInputField = "dialogInputField";
    public static final String key_dialogInputFieldActivated = "dialogInputFieldActivated";
    public static final String key_dialogLineProgress = "dialogLineProgress";
    public static final String key_dialogLineProgressBackground = "dialogLineProgressBackground";
    public static final String key_dialogLinkSelection = "dialogLinkSelection";
    public static final String key_dialogProgressCircle = "dialogProgressCircle";
    public static final String key_dialogRadioBackground = "dialogRadioBackground";
    public static final String key_dialogRadioBackgroundChecked = "dialogRadioBackgroundChecked";
    public static final String key_dialogRoundCheckBox = "dialogRoundCheckBox";
    public static final String key_dialogRoundCheckBoxCheck = "dialogRoundCheckBoxCheck";
    public static final String key_dialogScrollGlow = "dialogScrollGlow";
    public static final String key_dialogTextBlack = "dialogTextBlack";
    public static final String key_dialogTextBlue = "dialogTextBlue";
    public static final String key_dialogTextBlue2 = "dialogTextBlue2";
    public static final String key_dialogTextBlue3 = "dialogTextBlue3";
    public static final String key_dialogTextBlue4 = "dialogTextBlue4";
    public static final String key_dialogTextGray = "dialogTextGray";
    public static final String key_dialogTextGray2 = "dialogTextGray2";
    public static final String key_dialogTextGray3 = "dialogTextGray3";
    public static final String key_dialogTextGray4 = "dialogTextGray4";
    public static final String key_dialogTextHint = "dialogTextHint";
    public static final String key_dialogTextLink = "dialogTextLink";
    public static final String key_dialogTextRed = "dialogTextRed";
    public static final String key_dialogTopBackground = "dialogTopBackground";
    public static final String key_dialog_liveLocationProgress = "location_liveLocationProgress";
    public static final String key_divider = "divider";
    public static final String key_emptyListPlaceholder = "emptyListPlaceholder";
    public static final String key_fastScrollActive = "fastScrollActive";
    public static final String key_fastScrollInactive = "fastScrollInactive";
    public static final String key_fastScrollText = "fastScrollText";
    public static final String key_featuredStickers_addButton = "featuredStickers_addButton";
    public static final String key_featuredStickers_addButtonPressed = "featuredStickers_addButtonPressed";
    public static final String key_featuredStickers_addedIcon = "featuredStickers_addedIcon";
    public static final String key_featuredStickers_buttonProgress = "featuredStickers_buttonProgress";
    public static final String key_featuredStickers_buttonText = "featuredStickers_buttonText";
    public static final String key_featuredStickers_delButton = "featuredStickers_delButton";
    public static final String key_featuredStickers_delButtonPressed = "featuredStickers_delButtonPressed";
    public static final String key_featuredStickers_unread = "featuredStickers_unread";
    public static final String key_files_folderIcon = "files_folderIcon";
    public static final String key_files_folderIconBackground = "files_folderIconBackground";
    public static final String key_files_iconText = "files_iconText";
    public static final String key_graySection = "graySection";
    public static final String key_groupcreate_checkbox = "groupcreate_checkbox";
    public static final String key_groupcreate_checkboxCheck = "groupcreate_checkboxCheck";
    public static final String key_groupcreate_cursor = "groupcreate_cursor";
    public static final String key_groupcreate_hintText = "groupcreate_hintText";
    public static final String key_groupcreate_offlineText = "groupcreate_offlineText";
    public static final String key_groupcreate_onlineText = "groupcreate_onlineText";
    public static final String key_groupcreate_sectionShadow = "groupcreate_sectionShadow";
    public static final String key_groupcreate_sectionText = "groupcreate_sectionText";
    public static final String key_groupcreate_spanBackground = "groupcreate_spanBackground";
    public static final String key_groupcreate_spanText = "groupcreate_spanText";
    public static final String key_inappPlayerBackground = "inappPlayerBackground";
    public static final String key_inappPlayerClose = "inappPlayerClose";
    public static final String key_inappPlayerPerformer = "inappPlayerPerformer";
    public static final String key_inappPlayerPlayPause = "inappPlayerPlayPause";
    public static final String key_inappPlayerTitle = "inappPlayerTitle";
    public static final String key_listSelector = "listSelectorSDK21";
    public static final String key_location_liveLocationProgress = "location_liveLocationProgress";
    public static final String key_location_markerX = "location_markerX";
    public static final String key_location_placeLocationBackground = "location_placeLocationBackground";
    public static final String key_location_sendLiveLocationBackground = "location_sendLiveLocationBackground";
    public static final String key_location_sendLocationBackground = "location_sendLocationBackground";
    public static final String key_location_sendLocationIcon = "location_sendLocationIcon";
    public static final String key_login_progressInner = "login_progressInner";
    public static final String key_login_progressOuter = "login_progressOuter";
    public static final String key_musicPicker_buttonBackground = "musicPicker_buttonBackground";
    public static final String key_musicPicker_buttonIcon = "musicPicker_buttonIcon";
    public static final String key_musicPicker_checkbox = "musicPicker_checkbox";
    public static final String key_musicPicker_checkboxCheck = "musicPicker_checkboxCheck";
    public static final String key_picker_badge = "picker_badge";
    public static final String key_picker_badgeText = "picker_badgeText";
    public static final String key_picker_disabledButton = "picker_disabledButton";
    public static final String key_picker_enabledButton = "picker_enabledButton";
    public static final String key_player_actionBar = "player_actionBar";
    public static final String key_player_actionBarItems = "player_actionBarItems";
    public static final String key_player_actionBarSelector = "player_actionBarSelector";
    public static final String key_player_actionBarSubtitle = "player_actionBarSubtitle";
    public static final String key_player_actionBarTitle = "player_actionBarTitle";
    public static final String key_player_actionBarTop = "player_actionBarTop";
    public static final String key_player_background = "player_background";
    public static final String key_player_button = "player_button";
    public static final String key_player_buttonActive = "player_buttonActive";
    public static final String key_player_placeholder = "player_placeholder";
    public static final String key_player_placeholderBackground = "player_placeholderBackground";
    public static final String key_player_progress = "player_progress";
    public static final String key_player_progressBackground = "player_progressBackground";
    public static final String key_player_progressCachedBackground = "key_player_progressCachedBackground";
    public static final String key_player_time = "player_time";
    public static final String key_profile_actionBackground = "profile_actionBackground";
    public static final String key_profile_actionIcon = "profile_actionIcon";
    public static final String key_profile_actionPressedBackground = "profile_actionPressedBackground";
    public static final String key_profile_adminIcon = "profile_adminIcon";
    public static final String key_profile_creatorIcon = "profile_creatorIcon";
    public static final String key_profile_title = "profile_title";
    public static final String key_profile_verifiedBackground = "profile_verifiedBackground";
    public static final String key_profile_verifiedCheck = "profile_verifiedCheck";
    public static final String key_progressCircle = "progressCircle";
    public static final String key_radioBackground = "radioBackground";
    public static final String key_radioBackgroundChecked = "radioBackgroundChecked";
    public static final String key_returnToCallBackground = "returnToCallBackground";
    public static final String key_returnToCallText = "returnToCallText";
    public static final String key_sessions_devicesImage = "sessions_devicesImage";
    public static final String key_sharedMedia_linkPlaceholder = "sharedMedia_linkPlaceholder";
    public static final String key_sharedMedia_linkPlaceholderText = "sharedMedia_linkPlaceholderText";
    public static final String key_sharedMedia_startStopLoadIcon = "sharedMedia_startStopLoadIcon";
    public static final String key_stickers_menu = "stickers_menu";
    public static final String key_stickers_menuSelector = "stickers_menuSelector";
    public static final String key_switchThumb = "switchThumb";
    public static final String key_switchThumbChecked = "switchThumbChecked";
    public static final String key_switchTrack = "switchTrack";
    public static final String key_switchTrackChecked = "switchTrackChecked";
    public static final String key_windowBackgroundGray = "windowBackgroundGray";
    public static final String key_windowBackgroundGrayShadow = "windowBackgroundGrayShadow";
    public static final String key_windowBackgroundWhite = "windowBackgroundWhite";
    public static final String key_windowBackgroundWhiteBlackText = "windowBackgroundWhiteBlackText";
    public static final String key_windowBackgroundWhiteBlueHeader = "windowBackgroundWhiteBlueHeader";
    public static final String key_windowBackgroundWhiteBlueText = "windowBackgroundWhiteBlueText";
    public static final String key_windowBackgroundWhiteBlueText2 = "windowBackgroundWhiteBlueText2";
    public static final String key_windowBackgroundWhiteBlueText3 = "windowBackgroundWhiteBlueText3";
    public static final String key_windowBackgroundWhiteBlueText4 = "windowBackgroundWhiteBlueText4";
    public static final String key_windowBackgroundWhiteBlueText5 = "windowBackgroundWhiteBlueText5";
    public static final String key_windowBackgroundWhiteBlueText6 = "windowBackgroundWhiteBlueText6";
    public static final String key_windowBackgroundWhiteBlueText7 = "windowBackgroundWhiteBlueText7";
    public static final String key_windowBackgroundWhiteGrayIcon = "windowBackgroundWhiteGrayIcon";
    public static final String key_windowBackgroundWhiteGrayLine = "windowBackgroundWhiteGrayLine";
    public static final String key_windowBackgroundWhiteGrayText = "windowBackgroundWhiteGrayText";
    public static final String key_windowBackgroundWhiteGrayText2 = "windowBackgroundWhiteGrayText2";
    public static final String key_windowBackgroundWhiteGrayText3 = "windowBackgroundWhiteGrayText3";
    public static final String key_windowBackgroundWhiteGrayText4 = "windowBackgroundWhiteGrayText4";
    public static final String key_windowBackgroundWhiteGrayText5 = "windowBackgroundWhiteGrayText5";
    public static final String key_windowBackgroundWhiteGrayText6 = "windowBackgroundWhiteGrayText6";
    public static final String key_windowBackgroundWhiteGrayText7 = "windowBackgroundWhiteGrayText7";
    public static final String key_windowBackgroundWhiteGrayText8 = "windowBackgroundWhiteGrayText8";
    public static final String key_windowBackgroundWhiteGreenText = "windowBackgroundWhiteGreenText";
    public static final String key_windowBackgroundWhiteGreenText2 = "windowBackgroundWhiteGreenText2";
    public static final String key_windowBackgroundWhiteHintText = "windowBackgroundWhiteHintText";
    public static final String key_windowBackgroundWhiteInputField = "windowBackgroundWhiteInputField";
    public static final String key_windowBackgroundWhiteInputFieldActivated = "windowBackgroundWhiteInputFieldActivated";
    public static final String key_windowBackgroundWhiteLinkSelection = "windowBackgroundWhiteLinkSelection";
    public static final String key_windowBackgroundWhiteLinkText = "windowBackgroundWhiteLinkText";
    public static final String key_windowBackgroundWhiteRedText = "windowBackgroundWhiteRedText";
    public static final String key_windowBackgroundWhiteRedText2 = "windowBackgroundWhiteRedText2";
    public static final String key_windowBackgroundWhiteRedText3 = "windowBackgroundWhiteRedText3";
    public static final String key_windowBackgroundWhiteRedText4 = "windowBackgroundWhiteRedText4";
    public static final String key_windowBackgroundWhiteRedText5 = "windowBackgroundWhiteRedText5";
    public static final String key_windowBackgroundWhiteRedText6 = "windowBackgroundWhiteRedText6";
    public static final String key_windowBackgroundWhiteValueText = "windowBackgroundWhiteValueText";
    public static String[] keys_avatar_actionBarIcon = new String[]{key_avatar_actionBarIconRed, key_avatar_actionBarIconOrange, key_avatar_actionBarIconViolet, key_avatar_actionBarIconGreen, key_avatar_actionBarIconCyan, key_avatar_actionBarIconBlue, key_avatar_actionBarIconPink};
    public static String[] keys_avatar_actionBarSelector = new String[]{key_avatar_actionBarSelectorRed, key_avatar_actionBarSelectorOrange, key_avatar_actionBarSelectorViolet, key_avatar_actionBarSelectorGreen, key_avatar_actionBarSelectorCyan, key_avatar_actionBarSelectorBlue, key_avatar_actionBarSelectorPink};
    public static String[] keys_avatar_background = new String[]{key_avatar_backgroundRed, key_avatar_backgroundOrange, key_avatar_backgroundViolet, key_avatar_backgroundGreen, key_avatar_backgroundCyan, key_avatar_backgroundBlue, key_avatar_backgroundPink};
    public static String[] keys_avatar_backgroundActionBar = new String[]{key_avatar_backgroundActionBarRed, key_avatar_backgroundActionBarOrange, key_avatar_backgroundActionBarViolet, key_avatar_backgroundActionBarGreen, key_avatar_backgroundActionBarCyan, key_avatar_backgroundActionBarBlue, key_avatar_backgroundActionBarPink};
    public static String[] keys_avatar_backgroundInProfile = new String[]{key_avatar_backgroundInProfileRed, key_avatar_backgroundInProfileOrange, key_avatar_backgroundInProfileViolet, key_avatar_backgroundInProfileGreen, key_avatar_backgroundInProfileCyan, key_avatar_backgroundInProfileBlue, key_avatar_backgroundInProfilePink};
    public static String[] keys_avatar_nameInMessage = new String[]{key_avatar_nameInMessageRed, key_avatar_nameInMessageOrange, key_avatar_nameInMessageViolet, key_avatar_nameInMessageGreen, key_avatar_nameInMessageCyan, key_avatar_nameInMessageBlue, key_avatar_nameInMessagePink};
    public static String[] keys_avatar_subtitleInProfile = new String[]{key_avatar_subtitleInProfileRed, key_avatar_subtitleInProfileOrange, key_avatar_subtitleInProfileViolet, key_avatar_subtitleInProfileGreen, key_avatar_subtitleInProfileCyan, key_avatar_subtitleInProfileBlue, key_avatar_subtitleInProfilePink};
    private static float lastBrightnessValue = 1.0f;
    private static long lastHolidayCheckTime;
    private static long lastThemeSwitchTime;
    private static Sensor lightSensor;
    private static boolean lightSensorRegistered;
    public static Paint linkSelectionPaint;
    public static Drawable listSelector;
    private static Paint maskPaint = new Paint(1);
    private static ArrayList<ThemeInfo> otherThemes = new ArrayList();
    private static ThemeInfo previousTheme;
    public static TextPaint profile_aboutTextPaint;
    public static Drawable profile_verifiedCheckDrawable;
    public static Drawable profile_verifiedDrawable;
    public static int selectedAutoNightType;
    private static int selectedColor;
    private static SensorManager sensorManager;
    private static int serviceMessageColor;
    private static int serviceSelectedMessageColor;
    private static Runnable switchDayBrightnessRunnable = new Runnable() {
        public void run() {
            Theme.switchDayRunnableScheduled = false;
            Theme.applyDayNightThemeMaybe(false);
        }
    };
    private static boolean switchDayRunnableScheduled;
    private static Runnable switchNightBrightnessRunnable = new Runnable() {
        public void run() {
            Theme.switchNightRunnableScheduled = false;
            Theme.applyDayNightThemeMaybe(true);
        }
    };
    private static boolean switchNightRunnableScheduled;
    private static final Object sync = new Object();
    private static Drawable themedWallpaper;
    private static int themedWallpaperFileOffset;
    public static ArrayList<ThemeInfo> themes = new ArrayList();
    private static HashMap<String, ThemeInfo> themesDict = new HashMap();
    private static Drawable wallpaper;
    private static final Object wallpaperSync = new Object();

    public static class ThemeInfo {
        public String assetName;
        public String name;
        public String pathToFile;

        public JSONObject getSaveJson() {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", this.name);
                jsonObject.put("path", this.pathToFile);
                return jsonObject;
            } catch (Throwable e) {
                FileLog.e(e);
                return null;
            }
        }

        public String getName() {
            if ("Default".equals(this.name)) {
                return LocaleController.getString("Default", R.string.Default);
            }
            if ("Blue".equals(this.name)) {
                return LocaleController.getString("ThemeBlue", R.string.ThemeBlue);
            }
            if ("Dark".equals(this.name)) {
                return LocaleController.getString("ThemeDark", R.string.ThemeDark);
            }
            return this.name;
        }

        public static ThemeInfo createWithJson(JSONObject object) {
            if (object == null) {
                return null;
            }
            try {
                ThemeInfo themeInfo = new ThemeInfo();
                themeInfo.name = object.getString("name");
                themeInfo.pathToFile = object.getString("path");
                return themeInfo;
            } catch (Throwable e) {
                FileLog.e(e);
                return null;
            }
        }

        public static ThemeInfo createWithString(String string) {
            if (TextUtils.isEmpty(string)) {
                return null;
            }
            String[] args = string.split("\\|");
            if (args.length != 2) {
                return null;
            }
            ThemeInfo themeInfo = new ThemeInfo();
            themeInfo.name = args[0];
            themeInfo.pathToFile = args[1];
            return themeInfo;
        }
    }

    private static java.util.HashMap<java.lang.String, java.lang.Integer> getThemeFileValues(java.io.File r1, java.lang.String r2) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.telegram.ui.ActionBar.Theme.getThemeFileValues(java.io.File, java.lang.String):java.util.HashMap<java.lang.String, java.lang.Integer>
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: java.lang.NullPointerException
*/
        /*
        r1 = 0;
        r2 = new java.util.HashMap;
        r2.<init>();
        r3 = 1024; // 0x400 float:1.435E-42 double:5.06E-321;
        r3 = new byte[r3];	 Catch:{ Throwable -> 0x00fe, all -> 0x00f8 }
        r4 = 0;	 Catch:{ Throwable -> 0x00fe, all -> 0x00f8 }
        if (r21 == 0) goto L_0x0012;	 Catch:{ Throwable -> 0x00fe, all -> 0x00f8 }
    L_0x000d:
        r6 = getAssetFile(r21);	 Catch:{ Throwable -> 0x00fe, all -> 0x00f8 }
        goto L_0x0014;
    L_0x0012:
        r6 = r20;
    L_0x0014:
        r7 = new java.io.FileInputStream;	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
        r7.<init>(r6);	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
        r1 = r7;	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
        r7 = 0;	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
        r8 = -1;	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
        themedWallpaperFileOffset = r8;	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
        r9 = r1.read(r3);	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
        r10 = r9;	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
        if (r9 == r8) goto L_0x00dd;	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
    L_0x0025:
        r9 = r4;	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
        r11 = 0;	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
        r13 = r4;	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
        r4 = 0;	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
    L_0x0029:
        if (r4 >= r10) goto L_0x00be;	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
    L_0x002b:
        r14 = r3[r4];	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
        r15 = 10;	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
        if (r14 != r15) goto L_0x00b1;	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
    L_0x0031:
        r14 = r4 - r11;	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
        r14 = r14 + 1;	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
        r15 = new java.lang.String;	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
        r12 = r14 + -1;	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
        r8 = "UTF-8";	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
        r15.<init>(r3, r11, r12, r8);	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
        r8 = r15;	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
        r12 = "WPS";	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
        r12 = r8.startsWith(r12);	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
        if (r12 == 0) goto L_0x005f;
    L_0x0047:
        r12 = r13 + r14;
        themedWallpaperFileOffset = r12;	 Catch:{ Throwable -> 0x0059, all -> 0x0053 }
        r7 = 1;
        r17 = r3;
        r18 = r6;
        goto L_0x00c2;
    L_0x0053:
        r0 = move-exception;
        r3 = r1;
        r18 = r6;
        goto L_0x00fc;
    L_0x0059:
        r0 = move-exception;
        r3 = r1;
        r18 = r6;
        goto L_0x0102;
    L_0x005f:
        r12 = 61;
        r12 = r8.indexOf(r12);	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
        r15 = r12;	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
        r17 = r3;	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
        r3 = -1;	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
        if (r12 == r3) goto L_0x00ac;	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
    L_0x006b:
        r12 = 0;	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
        r16 = r8.substring(r12, r15);	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
        r12 = r16;	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
        r3 = r15 + 1;	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
        r3 = r8.substring(r3);	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
        r16 = r3.length();	 Catch:{ Throwable -> 0x00f2, all -> 0x00ec }
        if (r16 <= 0) goto L_0x009a;
    L_0x007e:
        r18 = r6;
        r5 = 0;
        r6 = r3.charAt(r5);	 Catch:{ Throwable -> 0x00db, all -> 0x00d9 }
        r5 = 35;
        if (r6 != r5) goto L_0x009c;
    L_0x0089:
        r5 = android.graphics.Color.parseColor(r3);	 Catch:{ Exception -> 0x008e }
    L_0x008d:
        goto L_0x00a4;
    L_0x008e:
        r0 = move-exception;
        r5 = r0;
        r6 = org.telegram.messenger.Utilities.parseInt(r3);	 Catch:{ Throwable -> 0x00db, all -> 0x00d9 }
        r6 = r6.intValue();	 Catch:{ Throwable -> 0x00db, all -> 0x00d9 }
        r5 = r6;	 Catch:{ Throwable -> 0x00db, all -> 0x00d9 }
        goto L_0x008d;	 Catch:{ Throwable -> 0x00db, all -> 0x00d9 }
    L_0x009a:
        r18 = r6;	 Catch:{ Throwable -> 0x00db, all -> 0x00d9 }
    L_0x009c:
        r5 = org.telegram.messenger.Utilities.parseInt(r3);	 Catch:{ Throwable -> 0x00db, all -> 0x00d9 }
        r5 = r5.intValue();	 Catch:{ Throwable -> 0x00db, all -> 0x00d9 }
    L_0x00a4:
        r6 = java.lang.Integer.valueOf(r5);	 Catch:{ Throwable -> 0x00db, all -> 0x00d9 }
        r2.put(r12, r6);	 Catch:{ Throwable -> 0x00db, all -> 0x00d9 }
        goto L_0x00ae;	 Catch:{ Throwable -> 0x00db, all -> 0x00d9 }
    L_0x00ac:
        r18 = r6;	 Catch:{ Throwable -> 0x00db, all -> 0x00d9 }
    L_0x00ae:
        r11 = r11 + r14;	 Catch:{ Throwable -> 0x00db, all -> 0x00d9 }
        r13 = r13 + r14;	 Catch:{ Throwable -> 0x00db, all -> 0x00d9 }
        goto L_0x00b5;	 Catch:{ Throwable -> 0x00db, all -> 0x00d9 }
    L_0x00b1:
        r17 = r3;	 Catch:{ Throwable -> 0x00db, all -> 0x00d9 }
        r18 = r6;	 Catch:{ Throwable -> 0x00db, all -> 0x00d9 }
    L_0x00b5:
        r4 = r4 + 1;	 Catch:{ Throwable -> 0x00db, all -> 0x00d9 }
        r3 = r17;	 Catch:{ Throwable -> 0x00db, all -> 0x00d9 }
        r6 = r18;	 Catch:{ Throwable -> 0x00db, all -> 0x00d9 }
        r8 = -1;	 Catch:{ Throwable -> 0x00db, all -> 0x00d9 }
        goto L_0x0029;	 Catch:{ Throwable -> 0x00db, all -> 0x00d9 }
    L_0x00be:
        r17 = r3;	 Catch:{ Throwable -> 0x00db, all -> 0x00d9 }
        r18 = r6;	 Catch:{ Throwable -> 0x00db, all -> 0x00d9 }
    L_0x00c2:
        if (r9 != r13) goto L_0x00c5;	 Catch:{ Throwable -> 0x00db, all -> 0x00d9 }
    L_0x00c4:
        goto L_0x00df;	 Catch:{ Throwable -> 0x00db, all -> 0x00d9 }
    L_0x00c5:
        r3 = r1.getChannel();	 Catch:{ Throwable -> 0x00db, all -> 0x00d9 }
        r4 = (long) r13;	 Catch:{ Throwable -> 0x00db, all -> 0x00d9 }
        r3.position(r4);	 Catch:{ Throwable -> 0x00db, all -> 0x00d9 }
        if (r7 == 0) goto L_0x00d0;
    L_0x00cf:
        goto L_0x00df;
        r4 = r13;
        r3 = r17;
        r6 = r18;
        r8 = -1;
        goto L_0x001e;
    L_0x00d9:
        r0 = move-exception;
        goto L_0x00fb;
    L_0x00db:
        r0 = move-exception;
        goto L_0x0101;
    L_0x00dd:
        r18 = r6;
    L_0x00df:
        if (r1 == 0) goto L_0x00eb;
        r1.close();	 Catch:{ Exception -> 0x00e5 }
        goto L_0x00eb;
    L_0x00e5:
        r0 = move-exception;
        r3 = r0;
        org.telegram.messenger.FileLog.e(r3);
        goto L_0x0114;
        goto L_0x0114;
    L_0x00ec:
        r0 = move-exception;
        r18 = r6;
        r3 = r1;
        r1 = r0;
        goto L_0x0117;
    L_0x00f2:
        r0 = move-exception;
        r18 = r6;
        r3 = r1;
        r1 = r0;
        goto L_0x0103;
    L_0x00f8:
        r0 = move-exception;
        r18 = r20;
        r3 = r1;
    L_0x00fc:
        r1 = r0;
        goto L_0x0117;
    L_0x00fe:
        r0 = move-exception;
        r18 = r20;
        r3 = r1;
    L_0x0102:
        r1 = r0;
        org.telegram.messenger.FileLog.e(r1);	 Catch:{ all -> 0x0115 }
        if (r3 == 0) goto L_0x0112;
        r3.close();	 Catch:{ Exception -> 0x010c }
        goto L_0x0112;
    L_0x010c:
        r0 = move-exception;
        r1 = r0;
        org.telegram.messenger.FileLog.e(r1);
        goto L_0x0113;
        r1 = r3;
        return r2;
    L_0x0115:
        r0 = move-exception;
        goto L_0x00fc;
        if (r3 == 0) goto L_0x0123;
        r3.close();	 Catch:{ Exception -> 0x011d }
        goto L_0x0123;
    L_0x011d:
        r0 = move-exception;
        r4 = r0;
        org.telegram.messenger.FileLog.e(r4);
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ActionBar.Theme.getThemeFileValues(java.io.File, java.lang.String):java.util.HashMap<java.lang.String, java.lang.Integer>");
    }

    public static android.graphics.drawable.Drawable getThemedWallpaper(boolean r1) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.telegram.ui.ActionBar.Theme.getThemedWallpaper(boolean):android.graphics.drawable.Drawable
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: java.lang.NullPointerException
*/
        /*
        r0 = currentColors;
        r1 = "chat_wallpaper";
        r0 = r0.get(r1);
        r0 = (java.lang.Integer) r0;
        if (r0 == 0) goto L_0x0016;
    L_0x000c:
        r1 = new android.graphics.drawable.ColorDrawable;
        r2 = r0.intValue();
        r1.<init>(r2);
        return r1;
    L_0x0016:
        r1 = themedWallpaperFileOffset;
        r2 = 0;
        if (r1 <= 0) goto L_0x00bb;
    L_0x001b:
        r1 = currentTheme;
        r1 = r1.pathToFile;
        if (r1 != 0) goto L_0x0027;
    L_0x0021:
        r1 = currentTheme;
        r1 = r1.assetName;
        if (r1 == 0) goto L_0x00bb;
    L_0x0027:
        r1 = r2;
        r3 = 0;
        r4 = currentTheme;	 Catch:{ Throwable -> 0x009e }
        r4 = r4.assetName;	 Catch:{ Throwable -> 0x009e }
        if (r4 == 0) goto L_0x0038;	 Catch:{ Throwable -> 0x009e }
    L_0x002f:
        r4 = currentTheme;	 Catch:{ Throwable -> 0x009e }
        r4 = r4.assetName;	 Catch:{ Throwable -> 0x009e }
        r4 = getAssetFile(r4);	 Catch:{ Throwable -> 0x009e }
        goto L_0x0041;	 Catch:{ Throwable -> 0x009e }
    L_0x0038:
        r4 = new java.io.File;	 Catch:{ Throwable -> 0x009e }
        r5 = currentTheme;	 Catch:{ Throwable -> 0x009e }
        r5 = r5.pathToFile;	 Catch:{ Throwable -> 0x009e }
        r4.<init>(r5);	 Catch:{ Throwable -> 0x009e }
    L_0x0041:
        r5 = new java.io.FileInputStream;	 Catch:{ Throwable -> 0x009e }
        r5.<init>(r4);	 Catch:{ Throwable -> 0x009e }
        r1 = r5;	 Catch:{ Throwable -> 0x009e }
        r5 = r1.getChannel();	 Catch:{ Throwable -> 0x009e }
        r6 = themedWallpaperFileOffset;	 Catch:{ Throwable -> 0x009e }
        r6 = (long) r6;	 Catch:{ Throwable -> 0x009e }
        r5.position(r6);	 Catch:{ Throwable -> 0x009e }
        r5 = new android.graphics.BitmapFactory$Options;	 Catch:{ Throwable -> 0x009e }
        r5.<init>();	 Catch:{ Throwable -> 0x009e }
        r6 = 1;	 Catch:{ Throwable -> 0x009e }
        if (r11 == 0) goto L_0x0079;	 Catch:{ Throwable -> 0x009e }
    L_0x0059:
        r7 = 1;	 Catch:{ Throwable -> 0x009e }
        r5.inJustDecodeBounds = r7;	 Catch:{ Throwable -> 0x009e }
        r7 = r5.outWidth;	 Catch:{ Throwable -> 0x009e }
        r7 = (float) r7;	 Catch:{ Throwable -> 0x009e }
        r8 = r5.outHeight;	 Catch:{ Throwable -> 0x009e }
        r8 = (float) r8;	 Catch:{ Throwable -> 0x009e }
        r9 = 1120403456; // 0x42c80000 float:100.0 double:5.53552857E-315;	 Catch:{ Throwable -> 0x009e }
        r9 = org.telegram.messenger.AndroidUtilities.dp(r9);	 Catch:{ Throwable -> 0x009e }
    L_0x0068:
        r10 = (float) r9;	 Catch:{ Throwable -> 0x009e }
        r10 = (r7 > r10 ? 1 : (r7 == r10 ? 0 : -1));	 Catch:{ Throwable -> 0x009e }
        if (r10 > 0) goto L_0x0072;	 Catch:{ Throwable -> 0x009e }
    L_0x006d:
        r10 = (float) r9;	 Catch:{ Throwable -> 0x009e }
        r10 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1));	 Catch:{ Throwable -> 0x009e }
        if (r10 <= 0) goto L_0x0079;	 Catch:{ Throwable -> 0x009e }
    L_0x0072:
        r6 = r6 * 2;	 Catch:{ Throwable -> 0x009e }
        r10 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;	 Catch:{ Throwable -> 0x009e }
        r7 = r7 / r10;	 Catch:{ Throwable -> 0x009e }
        r8 = r8 / r10;	 Catch:{ Throwable -> 0x009e }
        goto L_0x0068;	 Catch:{ Throwable -> 0x009e }
    L_0x0079:
        r7 = 0;	 Catch:{ Throwable -> 0x009e }
        r5.inJustDecodeBounds = r7;	 Catch:{ Throwable -> 0x009e }
        r5.inSampleSize = r6;	 Catch:{ Throwable -> 0x009e }
        r7 = android.graphics.BitmapFactory.decodeStream(r1, r2, r5);	 Catch:{ Throwable -> 0x009e }
        if (r7 == 0) goto L_0x0096;	 Catch:{ Throwable -> 0x009e }
    L_0x0084:
        r8 = new android.graphics.drawable.BitmapDrawable;	 Catch:{ Throwable -> 0x009e }
        r8.<init>(r7);	 Catch:{ Throwable -> 0x009e }
        if (r1 == 0) goto L_0x0094;
    L_0x008b:
        r1.close();	 Catch:{ Exception -> 0x008f }
        goto L_0x0094;
    L_0x008f:
        r2 = move-exception;
        org.telegram.messenger.FileLog.e(r2);
        goto L_0x0095;
        return r8;
    L_0x0096:
        if (r1 == 0) goto L_0x00ad;
        r1.close();	 Catch:{ Exception -> 0x00a8 }
        goto L_0x00ad;
    L_0x009c:
        r2 = move-exception;
        goto L_0x00ae;
    L_0x009e:
        r3 = move-exception;
        org.telegram.messenger.FileLog.e(r3);	 Catch:{ all -> 0x009c }
        if (r1 == 0) goto L_0x00ad;
        r1.close();	 Catch:{ Exception -> 0x00a8 }
        goto L_0x00ad;
    L_0x00a8:
        r3 = move-exception;
        org.telegram.messenger.FileLog.e(r3);
        goto L_0x00bb;
        goto L_0x00bb;
        if (r1 == 0) goto L_0x00ba;
        r1.close();	 Catch:{ Exception -> 0x00b5 }
        goto L_0x00ba;
    L_0x00b5:
        r3 = move-exception;
        org.telegram.messenger.FileLog.e(r3);
        throw r2;
    L_0x00bb:
        return r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ActionBar.Theme.getThemedWallpaper(boolean):android.graphics.drawable.Drawable");
    }

    public static void saveCurrentTheme(java.lang.String r1, boolean r2) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.telegram.ui.ActionBar.Theme.saveCurrentTheme(java.lang.String, boolean):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: java.lang.NullPointerException
*/
        /*
        r0 = new java.lang.StringBuilder;
        r0.<init>();
        r1 = currentColors;
        r1 = r1.entrySet();
        r1 = r1.iterator();
    L_0x000f:
        r2 = r1.hasNext();
        if (r2 == 0) goto L_0x0036;
    L_0x0015:
        r2 = r1.next();
        r2 = (java.util.Map.Entry) r2;
        r3 = r2.getKey();
        r3 = (java.lang.String) r3;
        r0.append(r3);
        r3 = "=";
        r0.append(r3);
        r3 = r2.getValue();
        r0.append(r3);
        r3 = "\n";
        r0.append(r3);
        goto L_0x000f;
    L_0x0036:
        r1 = new java.io.File;
        r2 = org.telegram.messenger.ApplicationLoader.getFilesDirFixed();
        r1.<init>(r2, r8);
        r2 = 0;
        r3 = new java.io.FileOutputStream;	 Catch:{ Exception -> 0x00e4 }
        r3.<init>(r1);	 Catch:{ Exception -> 0x00e4 }
        r2 = r3;	 Catch:{ Exception -> 0x00e4 }
        r3 = r0.toString();	 Catch:{ Exception -> 0x00e4 }
        r3 = r3.getBytes();	 Catch:{ Exception -> 0x00e4 }
        r2.write(r3);	 Catch:{ Exception -> 0x00e4 }
        r3 = themedWallpaper;	 Catch:{ Exception -> 0x00e4 }
        r3 = r3 instanceof android.graphics.drawable.BitmapDrawable;	 Catch:{ Exception -> 0x00e4 }
        if (r3 == 0) goto L_0x0086;	 Catch:{ Exception -> 0x00e4 }
    L_0x0057:
        r3 = themedWallpaper;	 Catch:{ Exception -> 0x00e4 }
        r3 = (android.graphics.drawable.BitmapDrawable) r3;	 Catch:{ Exception -> 0x00e4 }
        r3 = r3.getBitmap();	 Catch:{ Exception -> 0x00e4 }
        if (r3 == 0) goto L_0x007a;	 Catch:{ Exception -> 0x00e4 }
    L_0x0061:
        r4 = 4;	 Catch:{ Exception -> 0x00e4 }
        r4 = new byte[r4];	 Catch:{ Exception -> 0x00e4 }
        r4 = {87, 80, 83, 10};	 Catch:{ Exception -> 0x00e4 }
        r2.write(r4);	 Catch:{ Exception -> 0x00e4 }
        r4 = android.graphics.Bitmap.CompressFormat.JPEG;	 Catch:{ Exception -> 0x00e4 }
        r5 = 87;	 Catch:{ Exception -> 0x00e4 }
        r3.compress(r4, r5, r2);	 Catch:{ Exception -> 0x00e4 }
        r4 = 5;	 Catch:{ Exception -> 0x00e4 }
        r4 = new byte[r4];	 Catch:{ Exception -> 0x00e4 }
        r4 = {10, 87, 80, 69, 10};	 Catch:{ Exception -> 0x00e4 }
        r2.write(r4);	 Catch:{ Exception -> 0x00e4 }
    L_0x007a:
        if (r9 == 0) goto L_0x0086;	 Catch:{ Exception -> 0x00e4 }
    L_0x007c:
        r4 = themedWallpaper;	 Catch:{ Exception -> 0x00e4 }
        wallpaper = r4;	 Catch:{ Exception -> 0x00e4 }
        r4 = wallpaper;	 Catch:{ Exception -> 0x00e4 }
        r5 = 2;	 Catch:{ Exception -> 0x00e4 }
        calcBackgroundColor(r4, r5);	 Catch:{ Exception -> 0x00e4 }
    L_0x0086:
        r3 = themesDict;	 Catch:{ Exception -> 0x00e4 }
        r3 = r3.get(r8);	 Catch:{ Exception -> 0x00e4 }
        r3 = (org.telegram.ui.ActionBar.Theme.ThemeInfo) r3;	 Catch:{ Exception -> 0x00e4 }
        r4 = r3;	 Catch:{ Exception -> 0x00e4 }
        if (r3 != 0) goto L_0x00b6;	 Catch:{ Exception -> 0x00e4 }
    L_0x0091:
        r3 = new org.telegram.ui.ActionBar.Theme$ThemeInfo;	 Catch:{ Exception -> 0x00e4 }
        r3.<init>();	 Catch:{ Exception -> 0x00e4 }
        r4 = r3;	 Catch:{ Exception -> 0x00e4 }
        r3 = r1.getAbsolutePath();	 Catch:{ Exception -> 0x00e4 }
        r4.pathToFile = r3;	 Catch:{ Exception -> 0x00e4 }
        r4.name = r8;	 Catch:{ Exception -> 0x00e4 }
        r3 = themes;	 Catch:{ Exception -> 0x00e4 }
        r3.add(r4);	 Catch:{ Exception -> 0x00e4 }
        r3 = themesDict;	 Catch:{ Exception -> 0x00e4 }
        r5 = r4.name;	 Catch:{ Exception -> 0x00e4 }
        r3.put(r5, r4);	 Catch:{ Exception -> 0x00e4 }
        r3 = otherThemes;	 Catch:{ Exception -> 0x00e4 }
        r3.add(r4);	 Catch:{ Exception -> 0x00e4 }
        saveOtherThemes();	 Catch:{ Exception -> 0x00e4 }
        sortThemes();	 Catch:{ Exception -> 0x00e4 }
    L_0x00b6:
        currentTheme = r4;	 Catch:{ Exception -> 0x00e4 }
        r3 = currentTheme;	 Catch:{ Exception -> 0x00e4 }
        r5 = currentNightTheme;	 Catch:{ Exception -> 0x00e4 }
        if (r3 == r5) goto L_0x00c2;	 Catch:{ Exception -> 0x00e4 }
    L_0x00be:
        r3 = currentTheme;	 Catch:{ Exception -> 0x00e4 }
        currentDayTheme = r3;	 Catch:{ Exception -> 0x00e4 }
    L_0x00c2:
        r3 = org.telegram.messenger.MessagesController.getGlobalMainSettings();	 Catch:{ Exception -> 0x00e4 }
        r5 = r3.edit();	 Catch:{ Exception -> 0x00e4 }
        r6 = "theme";	 Catch:{ Exception -> 0x00e4 }
        r7 = currentDayTheme;	 Catch:{ Exception -> 0x00e4 }
        r7 = r7.name;	 Catch:{ Exception -> 0x00e4 }
        r5.putString(r6, r7);	 Catch:{ Exception -> 0x00e4 }
        r5.commit();	 Catch:{ Exception -> 0x00e4 }
        if (r2 == 0) goto L_0x00e1;
    L_0x00d8:
        r2.close();	 Catch:{ Exception -> 0x00dc }
        goto L_0x00e1;
    L_0x00dc:
        r3 = move-exception;
        org.telegram.messenger.FileLog.e(r3);
        goto L_0x00ee;
    L_0x00e1:
        goto L_0x00ee;
    L_0x00e2:
        r3 = move-exception;
        goto L_0x00ef;
    L_0x00e4:
        r3 = move-exception;
        org.telegram.messenger.FileLog.e(r3);	 Catch:{ all -> 0x00e2 }
        if (r2 == 0) goto L_0x00e1;
        r2.close();	 Catch:{ Exception -> 0x00dc }
        goto L_0x00e1;
    L_0x00ee:
        return;
        if (r2 == 0) goto L_0x00fb;
        r2.close();	 Catch:{ Exception -> 0x00f6 }
        goto L_0x00fb;
    L_0x00f6:
        r4 = move-exception;
        org.telegram.messenger.FileLog.e(r4);
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ActionBar.Theme.saveCurrentTheme(java.lang.String, boolean):void");
    }

    public static void setEmojiDrawableColor(android.graphics.drawable.Drawable r1, int r2, boolean r3) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.telegram.ui.ActionBar.Theme.setEmojiDrawableColor(android.graphics.drawable.Drawable, int, boolean):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: java.lang.NullPointerException
*/
        /*
        r0 = r3 instanceof android.graphics.drawable.StateListDrawable;
        if (r0 == 0) goto L_0x0047;
    L_0x0004:
        if (r5 == 0) goto L_0x0027;
    L_0x0006:
        r0 = 0;
        r0 = getStateDrawable(r3, r0);	 Catch:{ Throwable -> 0x0025 }
        r1 = r0 instanceof android.graphics.drawable.ShapeDrawable;	 Catch:{ Throwable -> 0x0025 }
        if (r1 == 0) goto L_0x001a;	 Catch:{ Throwable -> 0x0025 }
    L_0x000f:
        r1 = r0;	 Catch:{ Throwable -> 0x0025 }
        r1 = (android.graphics.drawable.ShapeDrawable) r1;	 Catch:{ Throwable -> 0x0025 }
        r1 = r1.getPaint();	 Catch:{ Throwable -> 0x0025 }
        r1.setColor(r4);	 Catch:{ Throwable -> 0x0025 }
        goto L_0x0024;	 Catch:{ Throwable -> 0x0025 }
    L_0x001a:
        r1 = new android.graphics.PorterDuffColorFilter;	 Catch:{ Throwable -> 0x0025 }
        r2 = android.graphics.PorterDuff.Mode.MULTIPLY;	 Catch:{ Throwable -> 0x0025 }
        r1.<init>(r4, r2);	 Catch:{ Throwable -> 0x0025 }
        r0.setColorFilter(r1);	 Catch:{ Throwable -> 0x0025 }
    L_0x0024:
        goto L_0x0045;	 Catch:{ Throwable -> 0x0025 }
    L_0x0025:
        r0 = move-exception;	 Catch:{ Throwable -> 0x0025 }
        goto L_0x0046;	 Catch:{ Throwable -> 0x0025 }
    L_0x0027:
        r0 = 1;	 Catch:{ Throwable -> 0x0025 }
        r0 = getStateDrawable(r3, r0);	 Catch:{ Throwable -> 0x0025 }
        r1 = r0 instanceof android.graphics.drawable.ShapeDrawable;	 Catch:{ Throwable -> 0x0025 }
        if (r1 == 0) goto L_0x003b;	 Catch:{ Throwable -> 0x0025 }
        r1 = r0;	 Catch:{ Throwable -> 0x0025 }
        r1 = (android.graphics.drawable.ShapeDrawable) r1;	 Catch:{ Throwable -> 0x0025 }
        r1 = r1.getPaint();	 Catch:{ Throwable -> 0x0025 }
        r1.setColor(r4);	 Catch:{ Throwable -> 0x0025 }
        goto L_0x0045;	 Catch:{ Throwable -> 0x0025 }
        r1 = new android.graphics.PorterDuffColorFilter;	 Catch:{ Throwable -> 0x0025 }
        r2 = android.graphics.PorterDuff.Mode.MULTIPLY;	 Catch:{ Throwable -> 0x0025 }
        r1.<init>(r4, r2);	 Catch:{ Throwable -> 0x0025 }
        r0.setColorFilter(r1);	 Catch:{ Throwable -> 0x0025 }
    L_0x0045:
        goto L_0x0047;
    L_0x0047:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ActionBar.Theme.setEmojiDrawableColor(android.graphics.drawable.Drawable, int, boolean):void");
    }

    public static void setSelectorDrawableColor(android.graphics.drawable.Drawable r1, int r2, boolean r3) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.telegram.ui.ActionBar.Theme.setSelectorDrawableColor(android.graphics.drawable.Drawable, int, boolean):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: java.lang.NullPointerException
*/
        /*
        r0 = r6 instanceof android.graphics.drawable.StateListDrawable;
        r1 = 1;
        r2 = 0;
        if (r0 == 0) goto L_0x0067;
    L_0x0006:
        if (r8 == 0) goto L_0x0046;
    L_0x0008:
        r0 = getStateDrawable(r6, r2);	 Catch:{ Throwable -> 0x0044 }
        r2 = r0 instanceof android.graphics.drawable.ShapeDrawable;	 Catch:{ Throwable -> 0x0044 }
        if (r2 == 0) goto L_0x001b;	 Catch:{ Throwable -> 0x0044 }
    L_0x0010:
        r2 = r0;	 Catch:{ Throwable -> 0x0044 }
        r2 = (android.graphics.drawable.ShapeDrawable) r2;	 Catch:{ Throwable -> 0x0044 }
        r2 = r2.getPaint();	 Catch:{ Throwable -> 0x0044 }
        r2.setColor(r7);	 Catch:{ Throwable -> 0x0044 }
        goto L_0x0025;	 Catch:{ Throwable -> 0x0044 }
    L_0x001b:
        r2 = new android.graphics.PorterDuffColorFilter;	 Catch:{ Throwable -> 0x0044 }
        r3 = android.graphics.PorterDuff.Mode.MULTIPLY;	 Catch:{ Throwable -> 0x0044 }
        r2.<init>(r7, r3);	 Catch:{ Throwable -> 0x0044 }
        r0.setColorFilter(r2);	 Catch:{ Throwable -> 0x0044 }
    L_0x0025:
        r1 = getStateDrawable(r6, r1);	 Catch:{ Throwable -> 0x0044 }
        r0 = r1;	 Catch:{ Throwable -> 0x0044 }
        r1 = r0 instanceof android.graphics.drawable.ShapeDrawable;	 Catch:{ Throwable -> 0x0044 }
        if (r1 == 0) goto L_0x0039;	 Catch:{ Throwable -> 0x0044 }
    L_0x002e:
        r1 = r0;	 Catch:{ Throwable -> 0x0044 }
        r1 = (android.graphics.drawable.ShapeDrawable) r1;	 Catch:{ Throwable -> 0x0044 }
        r1 = r1.getPaint();	 Catch:{ Throwable -> 0x0044 }
        r1.setColor(r7);	 Catch:{ Throwable -> 0x0044 }
        goto L_0x0043;	 Catch:{ Throwable -> 0x0044 }
    L_0x0039:
        r1 = new android.graphics.PorterDuffColorFilter;	 Catch:{ Throwable -> 0x0044 }
        r2 = android.graphics.PorterDuff.Mode.MULTIPLY;	 Catch:{ Throwable -> 0x0044 }
        r1.<init>(r7, r2);	 Catch:{ Throwable -> 0x0044 }
        r0.setColorFilter(r1);	 Catch:{ Throwable -> 0x0044 }
    L_0x0043:
        goto L_0x0066;	 Catch:{ Throwable -> 0x0044 }
    L_0x0044:
        r0 = move-exception;	 Catch:{ Throwable -> 0x0044 }
        goto L_0x0065;	 Catch:{ Throwable -> 0x0044 }
    L_0x0046:
        r0 = 2;	 Catch:{ Throwable -> 0x0044 }
        r0 = getStateDrawable(r6, r0);	 Catch:{ Throwable -> 0x0044 }
        r1 = r0 instanceof android.graphics.drawable.ShapeDrawable;	 Catch:{ Throwable -> 0x0044 }
        if (r1 == 0) goto L_0x005a;	 Catch:{ Throwable -> 0x0044 }
        r1 = r0;	 Catch:{ Throwable -> 0x0044 }
        r1 = (android.graphics.drawable.ShapeDrawable) r1;	 Catch:{ Throwable -> 0x0044 }
        r1 = r1.getPaint();	 Catch:{ Throwable -> 0x0044 }
        r1.setColor(r7);	 Catch:{ Throwable -> 0x0044 }
        goto L_0x0066;	 Catch:{ Throwable -> 0x0044 }
        r1 = new android.graphics.PorterDuffColorFilter;	 Catch:{ Throwable -> 0x0044 }
        r2 = android.graphics.PorterDuff.Mode.MULTIPLY;	 Catch:{ Throwable -> 0x0044 }
        r1.<init>(r7, r2);	 Catch:{ Throwable -> 0x0044 }
        r0.setColorFilter(r1);	 Catch:{ Throwable -> 0x0044 }
        goto L_0x0066;
    L_0x0066:
        goto L_0x00ac;
    L_0x0067:
        r0 = android.os.Build.VERSION.SDK_INT;
        r3 = 21;
        if (r0 < r3) goto L_0x00ac;
        r0 = r6 instanceof android.graphics.drawable.RippleDrawable;
        if (r0 == 0) goto L_0x00ac;
        r0 = r6;
        r0 = (android.graphics.drawable.RippleDrawable) r0;
        if (r8 == 0) goto L_0x0089;
        r3 = new android.content.res.ColorStateList;
        r4 = new int[r1][];
        r5 = android.util.StateSet.WILD_CARD;
        r4[r2] = r5;
        r1 = new int[r1];
        r1[r2] = r7;
        r3.<init>(r4, r1);
        r0.setColor(r3);
        goto L_0x00ac;
        r1 = r0.getNumberOfLayers();
        if (r1 <= 0) goto L_0x00ac;
        r1 = r0.getDrawable(r2);
        r2 = r1 instanceof android.graphics.drawable.ShapeDrawable;
        if (r2 == 0) goto L_0x00a2;
        r2 = r1;
        r2 = (android.graphics.drawable.ShapeDrawable) r2;
        r2 = r2.getPaint();
        r2.setColor(r7);
        goto L_0x00ac;
        r2 = new android.graphics.PorterDuffColorFilter;
        r3 = android.graphics.PorterDuff.Mode.MULTIPLY;
        r2.<init>(r7, r3);
        r1.setColorFilter(r2);
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ActionBar.Theme.setSelectorDrawableColor(android.graphics.drawable.Drawable, int, boolean):void");
    }

    static {
        Throwable themeInfo;
        String theme;
        ThemeInfo t;
        long val;
        selectedAutoNightType = 0;
        autoNightBrighnessThreshold = 0.25f;
        autoNightDayStartTime = 1320;
        autoNightDayEndTime = 480;
        autoNightSunsetTime = 1320;
        autoNightLastSunCheckDay = -1;
        autoNightSunriseTime = 480;
        autoNightCityName = TtmlNode.ANONYMOUS_REGION_ID;
        autoNightLocationLatitude = 10000.0d;
        autoNightLocationLongitude = 10000.0d;
        defaultColors.put(key_dialogBackground, Integer.valueOf(-1));
        defaultColors.put(key_dialogBackgroundGray, Integer.valueOf(-986896));
        defaultColors.put(key_dialogTextBlack, Integer.valueOf(-14606047));
        defaultColors.put(key_dialogTextLink, Integer.valueOf(-14255946));
        defaultColors.put(key_dialogLinkSelection, Integer.valueOf(862104035));
        defaultColors.put(key_dialogTextRed, Integer.valueOf(-3319206));
        defaultColors.put(key_dialogTextBlue, Integer.valueOf(-13660983));
        defaultColors.put(key_dialogTextBlue2, Integer.valueOf(-12940081));
        defaultColors.put(key_dialogTextBlue3, Integer.valueOf(-12664327));
        defaultColors.put(key_dialogTextBlue4, Integer.valueOf(-15095832));
        defaultColors.put(key_dialogTextGray, Integer.valueOf(-13333567));
        defaultColors.put(key_dialogTextGray2, Integer.valueOf(-9079435));
        defaultColors.put(key_dialogTextGray3, Integer.valueOf(-6710887));
        defaultColors.put(key_dialogTextGray4, Integer.valueOf(-5000269));
        defaultColors.put(key_dialogTextHint, Integer.valueOf(-6842473));
        defaultColors.put(key_dialogIcon, Integer.valueOf(-7697782));
        defaultColors.put(key_dialogGrayLine, Integer.valueOf(-2960686));
        defaultColors.put(key_dialogTopBackground, Integer.valueOf(-9456923));
        defaultColors.put(key_dialogInputField, Integer.valueOf(-2368549));
        defaultColors.put(key_dialogInputFieldActivated, Integer.valueOf(-13129232));
        defaultColors.put(key_dialogCheckboxSquareBackground, Integer.valueOf(-12345121));
        defaultColors.put(key_dialogCheckboxSquareCheck, Integer.valueOf(-1));
        defaultColors.put(key_dialogCheckboxSquareUnchecked, Integer.valueOf(-9211021));
        defaultColors.put(key_dialogCheckboxSquareDisabled, Integer.valueOf(-5197648));
        defaultColors.put(key_dialogRadioBackground, Integer.valueOf(-5000269));
        defaultColors.put(key_dialogRadioBackgroundChecked, Integer.valueOf(-13129232));
        defaultColors.put(key_dialogProgressCircle, Integer.valueOf(-11371101));
        defaultColors.put(key_dialogLineProgress, Integer.valueOf(-11371101));
        defaultColors.put(key_dialogLineProgressBackground, Integer.valueOf(-2368549));
        defaultColors.put(key_dialogButton, Integer.valueOf(-11955764));
        defaultColors.put(key_dialogButtonSelector, Integer.valueOf(251658240));
        defaultColors.put(key_dialogScrollGlow, Integer.valueOf(-657673));
        defaultColors.put(key_dialogRoundCheckBox, Integer.valueOf(-12664327));
        defaultColors.put(key_dialogRoundCheckBoxCheck, Integer.valueOf(-1));
        defaultColors.put(key_dialogBadgeBackground, Integer.valueOf(-12664327));
        defaultColors.put(key_dialogBadgeText, Integer.valueOf(-1));
        defaultColors.put(key_windowBackgroundWhite, Integer.valueOf(-1));
        defaultColors.put(key_progressCircle, Integer.valueOf(-11371101));
        defaultColors.put(key_windowBackgroundWhiteGrayIcon, Integer.valueOf(-9211021));
        defaultColors.put(key_windowBackgroundWhiteBlueText, Integer.valueOf(-12876608));
        defaultColors.put(key_windowBackgroundWhiteBlueText2, Integer.valueOf(-13333567));
        defaultColors.put(key_windowBackgroundWhiteBlueText3, Integer.valueOf(-14255946));
        defaultColors.put(key_windowBackgroundWhiteBlueText4, Integer.valueOf(-11697229));
        defaultColors.put(key_windowBackgroundWhiteBlueText5, Integer.valueOf(-11759926));
        defaultColors.put(key_windowBackgroundWhiteBlueText6, Integer.valueOf(-12940081));
        defaultColors.put(key_windowBackgroundWhiteBlueText7, Integer.valueOf(-13141330));
        defaultColors.put(key_windowBackgroundWhiteGreenText, Integer.valueOf(-14248148));
        defaultColors.put(key_windowBackgroundWhiteGreenText2, Integer.valueOf(-13129447));
        defaultColors.put(key_windowBackgroundWhiteRedText, Integer.valueOf(-3319206));
        defaultColors.put(key_windowBackgroundWhiteRedText2, Integer.valueOf(-2404015));
        defaultColors.put(key_windowBackgroundWhiteRedText3, Integer.valueOf(-2995895));
        defaultColors.put(key_windowBackgroundWhiteRedText4, Integer.valueOf(-3198928));
        defaultColors.put(key_windowBackgroundWhiteRedText5, Integer.valueOf(-1229511));
        defaultColors.put(key_windowBackgroundWhiteRedText6, Integer.valueOf(-39322));
        defaultColors.put(key_windowBackgroundWhiteGrayText, Integer.valueOf(-5723992));
        defaultColors.put(key_windowBackgroundWhiteGrayText2, Integer.valueOf(-7697782));
        defaultColors.put(key_windowBackgroundWhiteGrayText3, Integer.valueOf(-6710887));
        defaultColors.put(key_windowBackgroundWhiteGrayText4, Integer.valueOf(-8355712));
        defaultColors.put(key_windowBackgroundWhiteGrayText5, Integer.valueOf(-6052957));
        defaultColors.put(key_windowBackgroundWhiteGrayText6, Integer.valueOf(-9079435));
        defaultColors.put(key_windowBackgroundWhiteGrayText7, Integer.valueOf(-3750202));
        defaultColors.put(key_windowBackgroundWhiteGrayText8, Integer.valueOf(-9605774));
        defaultColors.put(key_windowBackgroundWhiteGrayLine, Integer.valueOf(-2368549));
        defaultColors.put(key_windowBackgroundWhiteBlackText, Integer.valueOf(-14606047));
        defaultColors.put(key_windowBackgroundWhiteHintText, Integer.valueOf(-6842473));
        defaultColors.put(key_windowBackgroundWhiteValueText, Integer.valueOf(-13660983));
        defaultColors.put(key_windowBackgroundWhiteLinkText, Integer.valueOf(-14255946));
        defaultColors.put(key_windowBackgroundWhiteLinkSelection, Integer.valueOf(862104035));
        defaultColors.put(key_windowBackgroundWhiteBlueHeader, Integer.valueOf(-12676913));
        defaultColors.put(key_windowBackgroundWhiteInputField, Integer.valueOf(-2368549));
        defaultColors.put(key_windowBackgroundWhiteInputFieldActivated, Integer.valueOf(-13129232));
        defaultColors.put(key_switchThumb, Integer.valueOf(-1184275));
        defaultColors.put(key_switchTrack, Integer.valueOf(-3684409));
        defaultColors.put(key_switchThumbChecked, Integer.valueOf(-12211217));
        defaultColors.put(key_switchTrackChecked, Integer.valueOf(-6236422));
        defaultColors.put(key_checkboxSquareBackground, Integer.valueOf(-12345121));
        defaultColors.put(key_checkboxSquareCheck, Integer.valueOf(-1));
        defaultColors.put(key_checkboxSquareUnchecked, Integer.valueOf(-9211021));
        defaultColors.put(key_checkboxSquareDisabled, Integer.valueOf(-5197648));
        defaultColors.put(key_listSelector, Integer.valueOf(251658240));
        defaultColors.put(key_radioBackground, Integer.valueOf(-5000269));
        defaultColors.put(key_radioBackgroundChecked, Integer.valueOf(-13129232));
        defaultColors.put(key_windowBackgroundGray, Integer.valueOf(-986896));
        defaultColors.put(key_windowBackgroundGrayShadow, Integer.valueOf(ACTION_BAR_VIDEO_EDIT_COLOR));
        defaultColors.put(key_emptyListPlaceholder, Integer.valueOf(-6974059));
        defaultColors.put(key_divider, Integer.valueOf(-2500135));
        defaultColors.put(key_graySection, Integer.valueOf(-855310));
        defaultColors.put(key_contextProgressInner1, Integer.valueOf(-4202506));
        defaultColors.put(key_contextProgressOuter1, Integer.valueOf(-13920542));
        defaultColors.put(key_contextProgressInner2, Integer.valueOf(-4202506));
        defaultColors.put(key_contextProgressOuter2, Integer.valueOf(-1));
        defaultColors.put(key_contextProgressInner3, Integer.valueOf(-5000269));
        defaultColors.put(key_contextProgressOuter3, Integer.valueOf(-1));
        defaultColors.put(key_fastScrollActive, Integer.valueOf(-11361317));
        defaultColors.put(key_fastScrollInactive, Integer.valueOf(-10263709));
        defaultColors.put(key_fastScrollText, Integer.valueOf(-1));
        defaultColors.put(key_avatar_text, Integer.valueOf(-1));
        defaultColors.put(key_avatar_backgroundSaved, Integer.valueOf(-10043398));
        defaultColors.put(key_avatar_backgroundRed, Integer.valueOf(-1743531));
        defaultColors.put(key_avatar_backgroundOrange, Integer.valueOf(-881592));
        defaultColors.put(key_avatar_backgroundViolet, Integer.valueOf(-7436818));
        defaultColors.put(key_avatar_backgroundGreen, Integer.valueOf(-8992691));
        defaultColors.put(key_avatar_backgroundCyan, Integer.valueOf(-10502443));
        defaultColors.put(key_avatar_backgroundBlue, Integer.valueOf(-11232035));
        defaultColors.put(key_avatar_backgroundPink, Integer.valueOf(-887654));
        defaultColors.put(key_avatar_backgroundGroupCreateSpanBlue, Integer.valueOf(-4204822));
        defaultColors.put(key_avatar_backgroundInProfileRed, Integer.valueOf(-2592923));
        defaultColors.put(key_avatar_backgroundInProfileOrange, Integer.valueOf(-615071));
        defaultColors.put(key_avatar_backgroundInProfileViolet, Integer.valueOf(-7570990));
        defaultColors.put(key_avatar_backgroundInProfileGreen, Integer.valueOf(-9981091));
        defaultColors.put(key_avatar_backgroundInProfileCyan, Integer.valueOf(-11099461));
        defaultColors.put(key_avatar_backgroundInProfileBlue, Integer.valueOf(-11500111));
        defaultColors.put(key_avatar_backgroundInProfilePink, Integer.valueOf(-819290));
        defaultColors.put(key_avatar_backgroundActionBarRed, Integer.valueOf(-3514282));
        defaultColors.put(key_avatar_backgroundActionBarOrange, Integer.valueOf(-947900));
        defaultColors.put(key_avatar_backgroundActionBarViolet, Integer.valueOf(-8557884));
        defaultColors.put(key_avatar_backgroundActionBarGreen, Integer.valueOf(-11099828));
        defaultColors.put(key_avatar_backgroundActionBarCyan, Integer.valueOf(-12283220));
        defaultColors.put(key_avatar_backgroundActionBarBlue, Integer.valueOf(-10907718));
        defaultColors.put(key_avatar_backgroundActionBarPink, Integer.valueOf(-10907718));
        defaultColors.put(key_avatar_subtitleInProfileRed, Integer.valueOf(-406587));
        defaultColors.put(key_avatar_subtitleInProfileOrange, Integer.valueOf(-139832));
        defaultColors.put(key_avatar_subtitleInProfileViolet, Integer.valueOf(-3291923));
        defaultColors.put(key_avatar_subtitleInProfileGreen, Integer.valueOf(-4133446));
        defaultColors.put(key_avatar_subtitleInProfileCyan, Integer.valueOf(-4660496));
        defaultColors.put(key_avatar_subtitleInProfileBlue, Integer.valueOf(-2626822));
        defaultColors.put(key_avatar_subtitleInProfilePink, Integer.valueOf(-2626822));
        defaultColors.put(key_avatar_nameInMessageRed, Integer.valueOf(-3516848));
        defaultColors.put(key_avatar_nameInMessageOrange, Integer.valueOf(-2589911));
        defaultColors.put(key_avatar_nameInMessageViolet, Integer.valueOf(-11627828));
        defaultColors.put(key_avatar_nameInMessageGreen, Integer.valueOf(-11488718));
        defaultColors.put(key_avatar_nameInMessageCyan, Integer.valueOf(-12406360));
        defaultColors.put(key_avatar_nameInMessageBlue, Integer.valueOf(-11627828));
        defaultColors.put(key_avatar_nameInMessagePink, Integer.valueOf(-11627828));
        defaultColors.put(key_avatar_actionBarSelectorRed, Integer.valueOf(-4437183));
        defaultColors.put(key_avatar_actionBarSelectorOrange, Integer.valueOf(-1674199));
        defaultColors.put(key_avatar_actionBarSelectorViolet, Integer.valueOf(-9216066));
        defaultColors.put(key_avatar_actionBarSelectorGreen, Integer.valueOf(-12020419));
        defaultColors.put(key_avatar_actionBarSelectorCyan, Integer.valueOf(-13007715));
        defaultColors.put(key_avatar_actionBarSelectorBlue, Integer.valueOf(-11959891));
        defaultColors.put(key_avatar_actionBarSelectorPink, Integer.valueOf(-11959891));
        defaultColors.put(key_avatar_actionBarIconRed, Integer.valueOf(-1));
        defaultColors.put(key_avatar_actionBarIconOrange, Integer.valueOf(-1));
        defaultColors.put(key_avatar_actionBarIconViolet, Integer.valueOf(-1));
        defaultColors.put(key_avatar_actionBarIconGreen, Integer.valueOf(-1));
        defaultColors.put(key_avatar_actionBarIconCyan, Integer.valueOf(-1));
        defaultColors.put(key_avatar_actionBarIconBlue, Integer.valueOf(-1));
        defaultColors.put(key_avatar_actionBarIconPink, Integer.valueOf(-1));
        defaultColors.put(key_actionBarDefault, Integer.valueOf(-11371101));
        defaultColors.put(key_actionBarDefaultIcon, Integer.valueOf(-1));
        defaultColors.put(key_actionBarActionModeDefault, Integer.valueOf(-1));
        defaultColors.put(key_actionBarActionModeDefaultTop, Integer.valueOf(-1728053248));
        defaultColors.put(key_actionBarActionModeDefaultIcon, Integer.valueOf(-9211021));
        defaultColors.put(key_actionBarDefaultTitle, Integer.valueOf(-1));
        defaultColors.put(key_actionBarDefaultSubtitle, Integer.valueOf(-2758409));
        defaultColors.put(key_actionBarDefaultSelector, Integer.valueOf(-12554860));
        defaultColors.put(key_actionBarWhiteSelector, Integer.valueOf(ACTION_BAR_AUDIO_SELECTOR_COLOR));
        defaultColors.put(key_actionBarDefaultSearch, Integer.valueOf(-1));
        defaultColors.put(key_actionBarDefaultSearchPlaceholder, Integer.valueOf(-1996488705));
        defaultColors.put(key_actionBarDefaultSubmenuItem, Integer.valueOf(-14606047));
        defaultColors.put(key_actionBarDefaultSubmenuBackground, Integer.valueOf(-1));
        defaultColors.put(key_actionBarActionModeDefaultSelector, Integer.valueOf(-986896));
        defaultColors.put(key_chats_unreadCounter, Integer.valueOf(-11613090));
        defaultColors.put(key_chats_unreadCounterMuted, Integer.valueOf(-3684409));
        defaultColors.put(key_chats_unreadCounterText, Integer.valueOf(-1));
        defaultColors.put(key_chats_name, Integer.valueOf(-14606047));
        defaultColors.put(key_chats_secretName, Integer.valueOf(-16734706));
        defaultColors.put(key_chats_secretIcon, Integer.valueOf(-15093466));
        defaultColors.put(key_chats_nameIcon, Integer.valueOf(-14408668));
        defaultColors.put(key_chats_pinnedIcon, Integer.valueOf(-5723992));
        defaultColors.put(key_chats_message, Integer.valueOf(-7368817));
        defaultColors.put(key_chats_draft, Integer.valueOf(-2274503));
        defaultColors.put(key_chats_nameMessage, Integer.valueOf(-11697229));
        defaultColors.put(key_chats_attachMessage, Integer.valueOf(-11697229));
        defaultColors.put(key_chats_actionMessage, Integer.valueOf(-11697229));
        defaultColors.put(key_chats_date, Integer.valueOf(-6710887));
        defaultColors.put(key_chats_pinnedOverlay, Integer.valueOf(134217728));
        defaultColors.put(key_chats_tabletSelectedOverlay, Integer.valueOf(251658240));
        defaultColors.put(key_chats_sentCheck, Integer.valueOf(-12146122));
        defaultColors.put(key_chats_sentClock, Integer.valueOf(-9061026));
        defaultColors.put(key_chats_sentError, Integer.valueOf(-2796974));
        defaultColors.put(key_chats_sentErrorIcon, Integer.valueOf(-1));
        defaultColors.put(key_chats_verifiedBackground, Integer.valueOf(-13391642));
        defaultColors.put(key_chats_verifiedCheck, Integer.valueOf(-1));
        defaultColors.put(key_chats_muteIcon, Integer.valueOf(-5723992));
        defaultColors.put(key_chats_menuBackground, Integer.valueOf(-1));
        defaultColors.put(key_chats_menuItemText, Integer.valueOf(-12303292));
        defaultColors.put(key_chats_menuItemCheck, Integer.valueOf(-10907718));
        defaultColors.put(key_chats_menuItemIcon, Integer.valueOf(-9211021));
        defaultColors.put(key_chats_menuName, Integer.valueOf(-1));
        defaultColors.put(key_chats_menuPhone, Integer.valueOf(-1));
        defaultColors.put(key_chats_menuPhoneCats, Integer.valueOf(-4004353));
        defaultColors.put(key_chats_menuCloud, Integer.valueOf(-1));
        defaultColors.put(key_chats_menuCloudBackgroundCats, Integer.valueOf(-12420183));
        defaultColors.put(key_chats_actionIcon, Integer.valueOf(-1));
        defaultColors.put(key_chats_actionBackground, Integer.valueOf(-9788978));
        defaultColors.put(key_chats_actionPressedBackground, Integer.valueOf(-11038014));
        defaultColors.put(key_chat_lockIcon, Integer.valueOf(-1));
        defaultColors.put(key_chat_muteIcon, Integer.valueOf(-5124893));
        defaultColors.put(key_chat_inBubble, Integer.valueOf(-1));
        defaultColors.put(key_chat_inBubbleSelected, Integer.valueOf(-1902337));
        defaultColors.put(key_chat_inBubbleShadow, Integer.valueOf(-14862509));
        defaultColors.put(key_chat_outBubble, Integer.valueOf(-1048610));
        defaultColors.put(key_chat_outBubbleSelected, Integer.valueOf(-2820676));
        defaultColors.put(key_chat_outBubbleShadow, Integer.valueOf(-14781172));
        defaultColors.put(key_chat_messageTextIn, Integer.valueOf(ACTION_BAR_VIDEO_EDIT_COLOR));
        defaultColors.put(key_chat_messageTextOut, Integer.valueOf(ACTION_BAR_VIDEO_EDIT_COLOR));
        defaultColors.put(key_chat_messageLinkIn, Integer.valueOf(-14255946));
        defaultColors.put(key_chat_messageLinkOut, Integer.valueOf(-14255946));
        defaultColors.put(key_chat_serviceText, Integer.valueOf(-1));
        defaultColors.put(key_chat_serviceLink, Integer.valueOf(-1));
        defaultColors.put(key_chat_serviceIcon, Integer.valueOf(-1));
        defaultColors.put(key_chat_mediaTimeBackground, Integer.valueOf(1711276032));
        defaultColors.put(key_chat_outSentCheck, Integer.valueOf(-10637232));
        defaultColors.put(key_chat_outSentCheckSelected, Integer.valueOf(-10637232));
        defaultColors.put(key_chat_outSentClock, Integer.valueOf(-9061026));
        defaultColors.put(key_chat_outSentClockSelected, Integer.valueOf(-9061026));
        defaultColors.put(key_chat_inSentClock, Integer.valueOf(-6182221));
        defaultColors.put(key_chat_inSentClockSelected, Integer.valueOf(-7094838));
        defaultColors.put(key_chat_mediaSentCheck, Integer.valueOf(-1));
        defaultColors.put(key_chat_mediaSentClock, Integer.valueOf(-1));
        defaultColors.put(key_chat_inViews, Integer.valueOf(-6182221));
        defaultColors.put(key_chat_inViewsSelected, Integer.valueOf(-7094838));
        defaultColors.put(key_chat_outViews, Integer.valueOf(-9522601));
        defaultColors.put(key_chat_outViewsSelected, Integer.valueOf(-9522601));
        defaultColors.put(key_chat_mediaViews, Integer.valueOf(-1));
        defaultColors.put(key_chat_inMenu, Integer.valueOf(-4801083));
        defaultColors.put(key_chat_inMenuSelected, Integer.valueOf(-6766130));
        defaultColors.put(key_chat_outMenu, Integer.valueOf(-7221634));
        defaultColors.put(key_chat_outMenuSelected, Integer.valueOf(-7221634));
        defaultColors.put(key_chat_mediaMenu, Integer.valueOf(-1));
        defaultColors.put(key_chat_outInstant, Integer.valueOf(-11162801));
        defaultColors.put(key_chat_outInstantSelected, Integer.valueOf(-12019389));
        defaultColors.put(key_chat_inInstant, Integer.valueOf(-12940081));
        defaultColors.put(key_chat_inInstantSelected, Integer.valueOf(-13600331));
        defaultColors.put(key_chat_sentError, Integer.valueOf(-2411211));
        defaultColors.put(key_chat_sentErrorIcon, Integer.valueOf(-1));
        defaultColors.put(key_chat_selectedBackground, Integer.valueOf(1714664933));
        defaultColors.put(key_chat_previewDurationText, Integer.valueOf(-1));
        defaultColors.put(key_chat_previewGameText, Integer.valueOf(-1));
        defaultColors.put(key_chat_inPreviewInstantText, Integer.valueOf(-12940081));
        defaultColors.put(key_chat_outPreviewInstantText, Integer.valueOf(-11162801));
        defaultColors.put(key_chat_inPreviewInstantSelectedText, Integer.valueOf(-13600331));
        defaultColors.put(key_chat_outPreviewInstantSelectedText, Integer.valueOf(-12019389));
        defaultColors.put(key_chat_secretTimeText, Integer.valueOf(-1776928));
        defaultColors.put(key_chat_stickerNameText, Integer.valueOf(-1));
        defaultColors.put(key_chat_botButtonText, Integer.valueOf(-1));
        defaultColors.put(key_chat_botProgress, Integer.valueOf(-1));
        defaultColors.put(key_chat_inForwardedNameText, Integer.valueOf(-13072697));
        defaultColors.put(key_chat_outForwardedNameText, Integer.valueOf(-11162801));
        defaultColors.put(key_chat_inViaBotNameText, Integer.valueOf(-12940081));
        defaultColors.put(key_chat_outViaBotNameText, Integer.valueOf(-11162801));
        defaultColors.put(key_chat_stickerViaBotNameText, Integer.valueOf(-1));
        defaultColors.put(key_chat_inReplyLine, Integer.valueOf(-10903592));
        defaultColors.put(key_chat_outReplyLine, Integer.valueOf(-9520791));
        defaultColors.put(key_chat_stickerReplyLine, Integer.valueOf(-1));
        defaultColors.put(key_chat_inReplyNameText, Integer.valueOf(-12940081));
        defaultColors.put(key_chat_outReplyNameText, Integer.valueOf(-11162801));
        defaultColors.put(key_chat_stickerReplyNameText, Integer.valueOf(-1));
        defaultColors.put(key_chat_inReplyMessageText, Integer.valueOf(ACTION_BAR_VIDEO_EDIT_COLOR));
        defaultColors.put(key_chat_outReplyMessageText, Integer.valueOf(ACTION_BAR_VIDEO_EDIT_COLOR));
        defaultColors.put(key_chat_inReplyMediaMessageText, Integer.valueOf(-6182221));
        defaultColors.put(key_chat_outReplyMediaMessageText, Integer.valueOf(-10112933));
        defaultColors.put(key_chat_inReplyMediaMessageSelectedText, Integer.valueOf(-7752511));
        defaultColors.put(key_chat_outReplyMediaMessageSelectedText, Integer.valueOf(-10112933));
        defaultColors.put(key_chat_stickerReplyMessageText, Integer.valueOf(-1));
        defaultColors.put(key_chat_inPreviewLine, Integer.valueOf(-9390872));
        defaultColors.put(key_chat_outPreviewLine, Integer.valueOf(-7812741));
        defaultColors.put(key_chat_inSiteNameText, Integer.valueOf(-12940081));
        defaultColors.put(key_chat_outSiteNameText, Integer.valueOf(-11162801));
        defaultColors.put(key_chat_inContactNameText, Integer.valueOf(-11625772));
        defaultColors.put(key_chat_outContactNameText, Integer.valueOf(-11162801));
        defaultColors.put(key_chat_inContactPhoneText, Integer.valueOf(-13683656));
        defaultColors.put(key_chat_outContactPhoneText, Integer.valueOf(-13286860));
        defaultColors.put(key_chat_mediaProgress, Integer.valueOf(-1));
        defaultColors.put(key_chat_inAudioProgress, Integer.valueOf(-1));
        defaultColors.put(key_chat_outAudioProgress, Integer.valueOf(-1048610));
        defaultColors.put(key_chat_inAudioSelectedProgress, Integer.valueOf(-1902337));
        defaultColors.put(key_chat_outAudioSelectedProgress, Integer.valueOf(-2820676));
        defaultColors.put(key_chat_mediaTimeText, Integer.valueOf(-1));
        defaultColors.put(key_chat_inTimeText, Integer.valueOf(-6182221));
        defaultColors.put(key_chat_outTimeText, Integer.valueOf(-9391780));
        defaultColors.put(key_chat_adminText, Integer.valueOf(-4143413));
        defaultColors.put(key_chat_adminSelectedText, Integer.valueOf(-7752511));
        defaultColors.put(key_chat_inTimeSelectedText, Integer.valueOf(-7752511));
        defaultColors.put(key_chat_outTimeSelectedText, Integer.valueOf(-9391780));
        defaultColors.put(key_chat_inAudioPerfomerText, Integer.valueOf(-13683656));
        defaultColors.put(key_chat_outAudioPerfomerText, Integer.valueOf(-13286860));
        defaultColors.put(key_chat_inAudioTitleText, Integer.valueOf(-11625772));
        defaultColors.put(key_chat_outAudioTitleText, Integer.valueOf(-11162801));
        defaultColors.put(key_chat_inAudioDurationText, Integer.valueOf(-6182221));
        defaultColors.put(key_chat_outAudioDurationText, Integer.valueOf(-10112933));
        defaultColors.put(key_chat_inAudioDurationSelectedText, Integer.valueOf(-7752511));
        defaultColors.put(key_chat_outAudioDurationSelectedText, Integer.valueOf(-10112933));
        defaultColors.put(key_chat_inAudioSeekbar, Integer.valueOf(-1774864));
        defaultColors.put(key_chat_inAudioCacheSeekbar, Integer.valueOf(1071966960));
        defaultColors.put(key_chat_outAudioSeekbar, Integer.valueOf(-4463700));
        defaultColors.put(key_chat_outAudioCacheSeekbar, Integer.valueOf(1069278124));
        defaultColors.put(key_chat_inAudioSeekbarSelected, Integer.valueOf(-4399384));
        defaultColors.put(key_chat_outAudioSeekbarSelected, Integer.valueOf(-5644906));
        defaultColors.put(key_chat_inAudioSeekbarFill, Integer.valueOf(-9259544));
        defaultColors.put(key_chat_outAudioSeekbarFill, Integer.valueOf(-8863118));
        defaultColors.put(key_chat_inVoiceSeekbar, Integer.valueOf(-2169365));
        defaultColors.put(key_chat_outVoiceSeekbar, Integer.valueOf(-4463700));
        defaultColors.put(key_chat_inVoiceSeekbarSelected, Integer.valueOf(-4399384));
        defaultColors.put(key_chat_outVoiceSeekbarSelected, Integer.valueOf(-5644906));
        defaultColors.put(key_chat_inVoiceSeekbarFill, Integer.valueOf(-9259544));
        defaultColors.put(key_chat_outVoiceSeekbarFill, Integer.valueOf(-8863118));
        defaultColors.put(key_chat_inFileProgress, Integer.valueOf(-1314571));
        defaultColors.put(key_chat_outFileProgress, Integer.valueOf(-2427453));
        defaultColors.put(key_chat_inFileProgressSelected, Integer.valueOf(-3413258));
        defaultColors.put(key_chat_outFileProgressSelected, Integer.valueOf(-3806041));
        defaultColors.put(key_chat_inFileNameText, Integer.valueOf(-11625772));
        defaultColors.put(key_chat_outFileNameText, Integer.valueOf(-11162801));
        defaultColors.put(key_chat_inFileInfoText, Integer.valueOf(-6182221));
        defaultColors.put(key_chat_outFileInfoText, Integer.valueOf(-10112933));
        defaultColors.put(key_chat_inFileInfoSelectedText, Integer.valueOf(-7752511));
        defaultColors.put(key_chat_outFileInfoSelectedText, Integer.valueOf(-10112933));
        defaultColors.put(key_chat_inFileBackground, Integer.valueOf(-1314571));
        defaultColors.put(key_chat_outFileBackground, Integer.valueOf(-2427453));
        defaultColors.put(key_chat_inFileBackgroundSelected, Integer.valueOf(-3413258));
        defaultColors.put(key_chat_outFileBackgroundSelected, Integer.valueOf(-3806041));
        defaultColors.put(key_chat_inVenueNameText, Integer.valueOf(-11625772));
        defaultColors.put(key_chat_outVenueNameText, Integer.valueOf(-11162801));
        defaultColors.put(key_chat_inVenueInfoText, Integer.valueOf(-6182221));
        defaultColors.put(key_chat_outVenueInfoText, Integer.valueOf(-10112933));
        defaultColors.put(key_chat_inVenueInfoSelectedText, Integer.valueOf(-7752511));
        defaultColors.put(key_chat_outVenueInfoSelectedText, Integer.valueOf(-10112933));
        defaultColors.put(key_chat_mediaInfoText, Integer.valueOf(-1));
        defaultColors.put(key_chat_linkSelectBackground, Integer.valueOf(862104035));
        defaultColors.put(key_chat_textSelectBackground, Integer.valueOf(1717742051));
        defaultColors.put(key_chat_emojiPanelBackground, Integer.valueOf(-657673));
        defaultColors.put(key_chat_emojiSearchBackground, Integer.valueOf(-1578003));
        defaultColors.put(key_chat_emojiPanelShadowLine, Integer.valueOf(-1907225));
        defaultColors.put(key_chat_emojiPanelEmptyText, Integer.valueOf(-5723992));
        defaultColors.put(key_chat_emojiPanelIcon, Integer.valueOf(-5723992));
        defaultColors.put(key_chat_emojiPanelIconSelected, Integer.valueOf(-13920542));
        defaultColors.put(key_chat_emojiPanelStickerPackSelector, Integer.valueOf(-1907225));
        defaultColors.put(key_chat_emojiPanelIconSelector, Integer.valueOf(-13920542));
        defaultColors.put(key_chat_emojiPanelBackspace, Integer.valueOf(-5723992));
        defaultColors.put(key_chat_emojiPanelMasksIcon, Integer.valueOf(-1));
        defaultColors.put(key_chat_emojiPanelMasksIconSelected, Integer.valueOf(-10305560));
        defaultColors.put(key_chat_emojiPanelTrendingTitle, Integer.valueOf(-14606047));
        defaultColors.put(key_chat_emojiPanelStickerSetName, Integer.valueOf(-8156010));
        defaultColors.put(key_chat_emojiPanelStickerSetNameIcon, Integer.valueOf(-5130564));
        defaultColors.put(key_chat_emojiPanelTrendingDescription, Integer.valueOf(-7697782));
        defaultColors.put(key_chat_botKeyboardButtonText, Integer.valueOf(-13220017));
        defaultColors.put(key_chat_botKeyboardButtonBackground, Integer.valueOf(-1775639));
        defaultColors.put(key_chat_botKeyboardButtonBackgroundPressed, Integer.valueOf(-3354156));
        defaultColors.put(key_chat_unreadMessagesStartArrowIcon, Integer.valueOf(-6113849));
        defaultColors.put(key_chat_unreadMessagesStartText, Integer.valueOf(-11102772));
        defaultColors.put(key_chat_unreadMessagesStartBackground, Integer.valueOf(-1));
        defaultColors.put(key_chat_editDoneIcon, Integer.valueOf(-11420173));
        defaultColors.put(key_chat_inFileIcon, Integer.valueOf(-6113849));
        defaultColors.put(key_chat_inFileSelectedIcon, Integer.valueOf(-7883067));
        defaultColors.put(key_chat_outFileIcon, Integer.valueOf(-8011912));
        defaultColors.put(key_chat_outFileSelectedIcon, Integer.valueOf(-8011912));
        defaultColors.put(key_chat_inLocationBackground, Integer.valueOf(-1314571));
        defaultColors.put(key_chat_inLocationIcon, Integer.valueOf(-6113849));
        defaultColors.put(key_chat_outLocationBackground, Integer.valueOf(-2427453));
        defaultColors.put(key_chat_outLocationIcon, Integer.valueOf(-7880840));
        defaultColors.put(key_chat_inContactBackground, Integer.valueOf(-9259544));
        defaultColors.put(key_chat_inContactIcon, Integer.valueOf(-1));
        defaultColors.put(key_chat_outContactBackground, Integer.valueOf(-8863118));
        defaultColors.put(key_chat_outContactIcon, Integer.valueOf(-1048610));
        defaultColors.put(key_chat_outBroadcast, Integer.valueOf(-12146122));
        defaultColors.put(key_chat_mediaBroadcast, Integer.valueOf(-1));
        defaultColors.put(key_chat_searchPanelIcons, Integer.valueOf(-10639908));
        defaultColors.put(key_chat_searchPanelText, Integer.valueOf(-11625772));
        defaultColors.put(key_chat_secretChatStatusText, Integer.valueOf(-8421505));
        defaultColors.put(key_chat_fieldOverlayText, Integer.valueOf(-12940081));
        defaultColors.put(key_chat_stickersHintPanel, Integer.valueOf(-1));
        defaultColors.put(key_chat_replyPanelIcons, Integer.valueOf(-11032346));
        defaultColors.put(key_chat_replyPanelClose, Integer.valueOf(-5723992));
        defaultColors.put(key_chat_replyPanelName, Integer.valueOf(-12940081));
        defaultColors.put(key_chat_replyPanelMessage, Integer.valueOf(-14540254));
        defaultColors.put(key_chat_replyPanelLine, Integer.valueOf(-1513240));
        defaultColors.put(key_chat_messagePanelBackground, Integer.valueOf(-1));
        defaultColors.put(key_chat_messagePanelText, Integer.valueOf(ACTION_BAR_VIDEO_EDIT_COLOR));
        defaultColors.put(key_chat_messagePanelHint, Integer.valueOf(-5066062));
        defaultColors.put(key_chat_messagePanelShadow, Integer.valueOf(ACTION_BAR_VIDEO_EDIT_COLOR));
        defaultColors.put(key_chat_messagePanelIcons, Integer.valueOf(-5723992));
        defaultColors.put(key_chat_recordedVoicePlayPause, Integer.valueOf(-1));
        defaultColors.put(key_chat_recordedVoicePlayPausePressed, Integer.valueOf(-2495749));
        defaultColors.put(key_chat_recordedVoiceDot, Integer.valueOf(-2468275));
        defaultColors.put(key_chat_recordedVoiceBackground, Integer.valueOf(-11165981));
        defaultColors.put(key_chat_recordedVoiceProgress, Integer.valueOf(-6107400));
        defaultColors.put(key_chat_recordedVoiceProgressInner, Integer.valueOf(-1));
        defaultColors.put(key_chat_recordVoiceCancel, Integer.valueOf(-6710887));
        defaultColors.put(key_chat_messagePanelSend, Integer.valueOf(-10309397));
        defaultColors.put(key_chat_messagePanelVoiceLock, Integer.valueOf(-5987164));
        defaultColors.put(key_chat_messagePanelVoiceLockBackground, Integer.valueOf(-1));
        defaultColors.put(key_chat_messagePanelVoiceLockShadow, Integer.valueOf(ACTION_BAR_VIDEO_EDIT_COLOR));
        defaultColors.put(key_chat_recordTime, Integer.valueOf(-11711413));
        defaultColors.put(key_chat_emojiPanelNewTrending, Integer.valueOf(-11688214));
        defaultColors.put(key_chat_gifSaveHintText, Integer.valueOf(-1));
        defaultColors.put(key_chat_gifSaveHintBackground, Integer.valueOf(-871296751));
        defaultColors.put(key_chat_goDownButton, Integer.valueOf(-1));
        defaultColors.put(key_chat_goDownButtonShadow, Integer.valueOf(ACTION_BAR_VIDEO_EDIT_COLOR));
        defaultColors.put(key_chat_goDownButtonIcon, Integer.valueOf(-5723992));
        defaultColors.put(key_chat_goDownButtonCounter, Integer.valueOf(-1));
        defaultColors.put(key_chat_goDownButtonCounterBackground, Integer.valueOf(-11689240));
        defaultColors.put(key_chat_messagePanelCancelInlineBot, Integer.valueOf(-5395027));
        defaultColors.put(key_chat_messagePanelVoicePressed, Integer.valueOf(-1));
        defaultColors.put(key_chat_messagePanelVoiceBackground, Integer.valueOf(-11037236));
        defaultColors.put(key_chat_messagePanelVoiceShadow, Integer.valueOf(218103808));
        defaultColors.put(key_chat_messagePanelVoiceDelete, Integer.valueOf(-9211021));
        defaultColors.put(key_chat_messagePanelVoiceDuration, Integer.valueOf(-1));
        defaultColors.put(key_chat_inlineResultIcon, Integer.valueOf(-11037236));
        defaultColors.put(key_chat_topPanelBackground, Integer.valueOf(-1));
        defaultColors.put(key_chat_topPanelClose, Integer.valueOf(-5723992));
        defaultColors.put(key_chat_topPanelLine, Integer.valueOf(-9658414));
        defaultColors.put(key_chat_topPanelTitle, Integer.valueOf(-12940081));
        defaultColors.put(key_chat_topPanelMessage, Integer.valueOf(-6710887));
        defaultColors.put(key_chat_reportSpam, Integer.valueOf(-3188393));
        defaultColors.put(key_chat_addContact, Integer.valueOf(-11894091));
        defaultColors.put(key_chat_inLoader, Integer.valueOf(-9259544));
        defaultColors.put(key_chat_inLoaderSelected, Integer.valueOf(-10114080));
        defaultColors.put(key_chat_outLoader, Integer.valueOf(-8863118));
        defaultColors.put(key_chat_outLoaderSelected, Integer.valueOf(-9783964));
        defaultColors.put(key_chat_inLoaderPhoto, Integer.valueOf(-6113080));
        defaultColors.put(key_chat_inLoaderPhotoSelected, Integer.valueOf(-6113849));
        defaultColors.put(key_chat_inLoaderPhotoIcon, Integer.valueOf(-197380));
        defaultColors.put(key_chat_inLoaderPhotoIconSelected, Integer.valueOf(-1314571));
        defaultColors.put(key_chat_outLoaderPhoto, Integer.valueOf(-8011912));
        defaultColors.put(key_chat_outLoaderPhotoSelected, Integer.valueOf(-8538000));
        defaultColors.put(key_chat_outLoaderPhotoIcon, Integer.valueOf(-2427453));
        defaultColors.put(key_chat_outLoaderPhotoIconSelected, Integer.valueOf(-4134748));
        defaultColors.put(key_chat_mediaLoaderPhoto, Integer.valueOf(1711276032));
        defaultColors.put(key_chat_mediaLoaderPhotoSelected, Integer.valueOf(ACTION_BAR_PHOTO_VIEWER_COLOR));
        defaultColors.put(key_chat_mediaLoaderPhotoIcon, Integer.valueOf(-1));
        defaultColors.put(key_chat_mediaLoaderPhotoIconSelected, Integer.valueOf(-2500135));
        defaultColors.put(key_chat_secretTimerBackground, Integer.valueOf(-868326258));
        defaultColors.put(key_chat_secretTimerText, Integer.valueOf(-1));
        defaultColors.put(key_profile_creatorIcon, Integer.valueOf(-11888682));
        defaultColors.put(key_profile_adminIcon, Integer.valueOf(-8026747));
        defaultColors.put(key_profile_actionIcon, Integer.valueOf(-9211021));
        defaultColors.put(key_profile_actionBackground, Integer.valueOf(-1));
        defaultColors.put(key_profile_actionPressedBackground, Integer.valueOf(-855310));
        defaultColors.put(key_profile_verifiedBackground, Integer.valueOf(-5056776));
        defaultColors.put(key_profile_verifiedCheck, Integer.valueOf(-11959368));
        defaultColors.put(key_profile_title, Integer.valueOf(-1));
        defaultColors.put(key_player_actionBar, Integer.valueOf(-1));
        defaultColors.put(key_player_actionBarSelector, Integer.valueOf(ACTION_BAR_AUDIO_SELECTOR_COLOR));
        defaultColors.put(key_player_actionBarTitle, Integer.valueOf(-13683656));
        defaultColors.put(key_player_actionBarTop, Integer.valueOf(-1728053248));
        defaultColors.put(key_player_actionBarSubtitle, Integer.valueOf(-7697782));
        defaultColors.put(key_player_actionBarItems, Integer.valueOf(-7697782));
        defaultColors.put(key_player_background, Integer.valueOf(-1));
        defaultColors.put(key_player_time, Integer.valueOf(-7564650));
        defaultColors.put(key_player_progressBackground, Integer.valueOf(419430400));
        defaultColors.put(key_player_progressCachedBackground, Integer.valueOf(419430400));
        defaultColors.put(key_player_progress, Integer.valueOf(-14438417));
        defaultColors.put(key_player_placeholder, Integer.valueOf(-5723992));
        defaultColors.put(key_player_placeholderBackground, Integer.valueOf(-986896));
        defaultColors.put(key_player_button, Integer.valueOf(ACTION_BAR_MEDIA_PICKER_COLOR));
        defaultColors.put(key_player_buttonActive, Integer.valueOf(-11753238));
        defaultColors.put(key_files_folderIcon, Integer.valueOf(-6710887));
        defaultColors.put(key_files_folderIconBackground, Integer.valueOf(-986896));
        defaultColors.put(key_files_iconText, Integer.valueOf(-1));
        defaultColors.put(key_sessions_devicesImage, Integer.valueOf(-6908266));
        defaultColors.put(key_location_markerX, Integer.valueOf(-8355712));
        defaultColors.put(key_location_sendLocationBackground, Integer.valueOf(-9592620));
        defaultColors.put(key_location_sendLiveLocationBackground, Integer.valueOf(-39836));
        defaultColors.put(key_location_sendLocationIcon, Integer.valueOf(-1));
        defaultColors.put("location_liveLocationProgress", Integer.valueOf(-13262875));
        defaultColors.put(key_location_placeLocationBackground, Integer.valueOf(-11753238));
        defaultColors.put("location_liveLocationProgress", Integer.valueOf(-13262875));
        defaultColors.put(key_calls_callReceivedGreenIcon, Integer.valueOf(-16725933));
        defaultColors.put(key_calls_callReceivedRedIcon, Integer.valueOf(-47032));
        defaultColors.put(key_featuredStickers_addedIcon, Integer.valueOf(-11491093));
        defaultColors.put(key_featuredStickers_buttonProgress, Integer.valueOf(-1));
        defaultColors.put(key_featuredStickers_addButton, Integer.valueOf(-11491093));
        defaultColors.put(key_featuredStickers_addButtonPressed, Integer.valueOf(-12346402));
        defaultColors.put(key_featuredStickers_delButton, Integer.valueOf(-2533545));
        defaultColors.put(key_featuredStickers_delButtonPressed, Integer.valueOf(-3782327));
        defaultColors.put(key_featuredStickers_buttonText, Integer.valueOf(-1));
        defaultColors.put(key_featuredStickers_unread, Integer.valueOf(-11688214));
        defaultColors.put(key_inappPlayerPerformer, Integer.valueOf(-13683656));
        defaultColors.put(key_inappPlayerTitle, Integer.valueOf(-13683656));
        defaultColors.put(key_inappPlayerBackground, Integer.valueOf(-1));
        defaultColors.put(key_inappPlayerPlayPause, Integer.valueOf(-10309397));
        defaultColors.put(key_inappPlayerClose, Integer.valueOf(-5723992));
        defaultColors.put(key_returnToCallBackground, Integer.valueOf(-12279325));
        defaultColors.put(key_returnToCallText, Integer.valueOf(-1));
        defaultColors.put(key_sharedMedia_startStopLoadIcon, Integer.valueOf(-13196562));
        defaultColors.put(key_sharedMedia_linkPlaceholder, Integer.valueOf(-986896));
        defaultColors.put(key_sharedMedia_linkPlaceholderText, Integer.valueOf(-1));
        defaultColors.put(key_checkbox, Integer.valueOf(-10567099));
        defaultColors.put(key_checkboxCheck, Integer.valueOf(-1));
        defaultColors.put(key_stickers_menu, Integer.valueOf(-4801083));
        defaultColors.put(key_stickers_menuSelector, Integer.valueOf(ACTION_BAR_AUDIO_SELECTOR_COLOR));
        defaultColors.put(key_changephoneinfo_image, Integer.valueOf(-5723992));
        defaultColors.put(key_groupcreate_hintText, Integer.valueOf(-6182221));
        defaultColors.put(key_groupcreate_cursor, Integer.valueOf(-11361317));
        defaultColors.put(key_groupcreate_sectionShadow, Integer.valueOf(ACTION_BAR_VIDEO_EDIT_COLOR));
        defaultColors.put(key_groupcreate_sectionText, Integer.valueOf(-8617336));
        defaultColors.put(key_groupcreate_onlineText, Integer.valueOf(-12545331));
        defaultColors.put(key_groupcreate_offlineText, Integer.valueOf(-8156010));
        defaultColors.put(key_groupcreate_checkbox, Integer.valueOf(-10567099));
        defaultColors.put(key_groupcreate_checkboxCheck, Integer.valueOf(-1));
        defaultColors.put(key_groupcreate_spanText, Integer.valueOf(-14606047));
        defaultColors.put(key_groupcreate_spanBackground, Integer.valueOf(-855310));
        defaultColors.put(key_contacts_inviteBackground, Integer.valueOf(-11157919));
        defaultColors.put(key_contacts_inviteText, Integer.valueOf(-1));
        defaultColors.put(key_login_progressInner, Integer.valueOf(-1971470));
        defaultColors.put(key_login_progressOuter, Integer.valueOf(-10313520));
        defaultColors.put(key_musicPicker_checkbox, Integer.valueOf(-14043401));
        defaultColors.put(key_musicPicker_checkboxCheck, Integer.valueOf(-1));
        defaultColors.put(key_musicPicker_buttonBackground, Integer.valueOf(-10702870));
        defaultColors.put(key_musicPicker_buttonIcon, Integer.valueOf(-1));
        defaultColors.put(key_picker_enabledButton, Integer.valueOf(-15095832));
        defaultColors.put(key_picker_disabledButton, Integer.valueOf(-6710887));
        defaultColors.put(key_picker_badge, Integer.valueOf(-14043401));
        defaultColors.put(key_picker_badgeText, Integer.valueOf(-1));
        defaultColors.put(key_chat_botSwitchToInlineText, Integer.valueOf(-12348980));
        defaultColors.put(key_calls_ratingStar, Integer.valueOf(Integer.MIN_VALUE));
        defaultColors.put(key_calls_ratingStarSelected, Integer.valueOf(-11888682));
        fallbackKeys.put(key_chat_adminText, key_chat_inTimeText);
        fallbackKeys.put(key_chat_adminSelectedText, key_chat_inTimeSelectedText);
        fallbackKeys.put(key_player_progressCachedBackground, key_player_progressBackground);
        fallbackKeys.put(key_chat_inAudioCacheSeekbar, key_chat_inAudioSeekbar);
        fallbackKeys.put(key_chat_outAudioCacheSeekbar, key_chat_outAudioSeekbar);
        fallbackKeys.put(key_chat_emojiSearchBackground, key_chat_emojiPanelStickerPackSelector);
        ThemeInfo themeInfo2 = new ThemeInfo();
        themeInfo2.name = "Default";
        ArrayList arrayList = themes;
        defaultTheme = themeInfo2;
        currentTheme = themeInfo2;
        currentDayTheme = themeInfo2;
        arrayList.add(themeInfo2);
        themesDict.put("Default", defaultTheme);
        themeInfo2 = new ThemeInfo();
        themeInfo2.name = "Dark";
        themeInfo2.assetName = "dark.attheme";
        arrayList = themes;
        currentNightTheme = themeInfo2;
        arrayList.add(themeInfo2);
        themesDict.put("Dark", themeInfo2);
        themeInfo2 = new ThemeInfo();
        themeInfo2.name = "Blue";
        themeInfo2.assetName = "bluebubbles.attheme";
        themes.add(themeInfo2);
        themesDict.put("Blue", themeInfo2);
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("themeconfig", 0);
        String themesString = preferences.getString("themes2", null);
        ThemeInfo themeInfo3;
        if (TextUtils.isEmpty(themesString)) {
            themesString = preferences.getString("themes", null);
            if (TextUtils.isEmpty(themesString)) {
            } else {
                String[] themesArr = themesString.split("&");
                themeInfo3 = themeInfo2;
                for (String createWithString : themesArr) {
                    themeInfo3 = ThemeInfo.createWithString(createWithString);
                    if (themeInfo3 != null) {
                        otherThemes.add(themeInfo3);
                        themes.add(themeInfo3);
                        themesDict.put(themeInfo3.name, themeInfo3);
                    }
                }
            }
            saveOtherThemes();
            preferences.edit().remove("themes").commit();
        } else {
            try {
                JSONArray jsonArray = new JSONArray(themesString);
                themeInfo2 = null;
                while (themeInfo2 < jsonArray.length()) {
                    try {
                        themeInfo3 = ThemeInfo.createWithJson(jsonArray.getJSONObject(themeInfo2));
                        if (themeInfo3 != null) {
                            otherThemes.add(themeInfo3);
                            themes.add(themeInfo3);
                            themesDict.put(themeInfo3.name, themeInfo3);
                        }
                        themeInfo2++;
                    } catch (Exception e) {
                        themeInfo = e;
                    }
                }
            } catch (Throwable e2) {
                themeInfo3 = themeInfo2;
                themeInfo = e2;
                FileLog.e(themeInfo);
                sortThemes();
                themeInfo2 = null;
                preferences = MessagesController.getGlobalMainSettings();
                theme = preferences.getString("theme", null);
                if (theme != null) {
                    themeInfo2 = (ThemeInfo) themesDict.get(theme);
                }
                theme = preferences.getString("nighttheme", null);
                if (theme != null) {
                    t = (ThemeInfo) themesDict.get(theme);
                    if (t != null) {
                        currentNightTheme = t;
                    }
                }
                selectedAutoNightType = preferences.getInt("selectedAutoNightType", 0);
                autoNightScheduleByLocation = preferences.getBoolean("autoNightScheduleByLocation", false);
                autoNightBrighnessThreshold = preferences.getFloat("autoNightBrighnessThreshold", 0.25f);
                autoNightDayStartTime = preferences.getInt("autoNightDayStartTime", 1320);
                autoNightDayEndTime = preferences.getInt("autoNightDayEndTime", 480);
                autoNightSunsetTime = preferences.getInt("autoNightSunsetTime", 1320);
                autoNightSunriseTime = preferences.getInt("autoNightSunriseTime", 480);
                autoNightCityName = preferences.getString("autoNightCityName", TtmlNode.ANONYMOUS_REGION_ID);
                val = preferences.getLong("autoNightLocationLatitude3", 10000);
                if (val == 10000) {
                    autoNightLocationLatitude = 10000.0d;
                } else {
                    autoNightLocationLatitude = Double.longBitsToDouble(val);
                }
                val = preferences.getLong("autoNightLocationLongitude3", 10000);
                if (val == 10000) {
                    autoNightLocationLongitude = 10000.0d;
                } else {
                    autoNightLocationLongitude = Double.longBitsToDouble(val);
                }
                autoNightLastSunCheckDay = preferences.getInt("autoNightLastSunCheckDay", -1);
                if (themeInfo2 != null) {
                    currentDayTheme = themeInfo2;
                } else {
                    themeInfo2 = defaultTheme;
                }
                applyTheme(themeInfo2, false, false, false);
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public void run() {
                        Theme.checkAutoNightThemeConditions();
                    }
                });
            }
        }
        sortThemes();
        themeInfo2 = null;
        try {
            preferences = MessagesController.getGlobalMainSettings();
            theme = preferences.getString("theme", null);
            if (theme != null) {
                themeInfo2 = (ThemeInfo) themesDict.get(theme);
            }
            theme = preferences.getString("nighttheme", null);
            if (theme != null) {
                t = (ThemeInfo) themesDict.get(theme);
                if (t != null) {
                    currentNightTheme = t;
                }
            }
            selectedAutoNightType = preferences.getInt("selectedAutoNightType", 0);
            autoNightScheduleByLocation = preferences.getBoolean("autoNightScheduleByLocation", false);
            autoNightBrighnessThreshold = preferences.getFloat("autoNightBrighnessThreshold", 0.25f);
            autoNightDayStartTime = preferences.getInt("autoNightDayStartTime", 1320);
            autoNightDayEndTime = preferences.getInt("autoNightDayEndTime", 480);
            autoNightSunsetTime = preferences.getInt("autoNightSunsetTime", 1320);
            autoNightSunriseTime = preferences.getInt("autoNightSunriseTime", 480);
            autoNightCityName = preferences.getString("autoNightCityName", TtmlNode.ANONYMOUS_REGION_ID);
            val = preferences.getLong("autoNightLocationLatitude3", 10000);
            if (val == 10000) {
                autoNightLocationLatitude = Double.longBitsToDouble(val);
            } else {
                autoNightLocationLatitude = 10000.0d;
            }
            val = preferences.getLong("autoNightLocationLongitude3", 10000);
            if (val == 10000) {
                autoNightLocationLongitude = Double.longBitsToDouble(val);
            } else {
                autoNightLocationLongitude = 10000.0d;
            }
            autoNightLastSunCheckDay = preferences.getInt("autoNightLastSunCheckDay", -1);
        } catch (Throwable e3) {
            FileLog.e(e3);
        }
        if (themeInfo2 != null) {
            themeInfo2 = defaultTheme;
        } else {
            currentDayTheme = themeInfo2;
        }
        applyTheme(themeInfo2, false, false, false);
        AndroidUtilities.runOnUIThread(/* anonymous class already generated */);
    }

    public static void saveAutoNightThemeConfig() {
        Editor editor = MessagesController.getGlobalMainSettings().edit();
        editor.putInt("selectedAutoNightType", selectedAutoNightType);
        editor.putBoolean("autoNightScheduleByLocation", autoNightScheduleByLocation);
        editor.putFloat("autoNightBrighnessThreshold", autoNightBrighnessThreshold);
        editor.putInt("autoNightDayStartTime", autoNightDayStartTime);
        editor.putInt("autoNightDayEndTime", autoNightDayEndTime);
        editor.putInt("autoNightSunriseTime", autoNightSunriseTime);
        editor.putString("autoNightCityName", autoNightCityName);
        editor.putInt("autoNightSunsetTime", autoNightSunsetTime);
        editor.putLong("autoNightLocationLatitude3", Double.doubleToRawLongBits(autoNightLocationLatitude));
        editor.putLong("autoNightLocationLongitude3", Double.doubleToRawLongBits(autoNightLocationLongitude));
        editor.putInt("autoNightLastSunCheckDay", autoNightLastSunCheckDay);
        if (currentNightTheme != null) {
            editor.putString("nighttheme", currentNightTheme.name);
        } else {
            editor.remove("nighttheme");
        }
        editor.commit();
    }

    @SuppressLint({"PrivateApi"})
    private static Drawable getStateDrawable(Drawable drawable, int index) {
        if (StateListDrawable_getStateDrawableMethod == null) {
            try {
                StateListDrawable_getStateDrawableMethod = StateListDrawable.class.getDeclaredMethod("getStateDrawable", new Class[]{Integer.TYPE});
            } catch (Throwable th) {
            }
        }
        if (StateListDrawable_getStateDrawableMethod == null) {
            return null;
        }
        try {
            return (Drawable) StateListDrawable_getStateDrawableMethod.invoke(drawable, new Object[]{Integer.valueOf(index)});
        } catch (Exception e) {
            return null;
        }
    }

    public static Drawable createEmojiIconSelectorDrawable(Context context, int resource, int defaultColor, int pressedColor) {
        Resources resources = context.getResources();
        Drawable defaultDrawable = resources.getDrawable(resource).mutate();
        if (defaultColor != 0) {
            defaultDrawable.setColorFilter(new PorterDuffColorFilter(defaultColor, Mode.MULTIPLY));
        }
        Drawable pressedDrawable = resources.getDrawable(resource).mutate();
        if (pressedColor != 0) {
            pressedDrawable.setColorFilter(new PorterDuffColorFilter(pressedColor, Mode.MULTIPLY));
        }
        StateListDrawable stateListDrawable = new StateListDrawable() {
            public boolean selectDrawable(int index) {
                if (VERSION.SDK_INT >= 21) {
                    return super.selectDrawable(index);
                }
                Drawable drawable = Theme.getStateDrawable(this, index);
                ColorFilter colorFilter = null;
                if (drawable instanceof BitmapDrawable) {
                    colorFilter = ((BitmapDrawable) drawable).getPaint().getColorFilter();
                } else if (drawable instanceof NinePatchDrawable) {
                    colorFilter = ((NinePatchDrawable) drawable).getPaint().getColorFilter();
                }
                boolean result = super.selectDrawable(index);
                if (colorFilter != null) {
                    drawable.setColorFilter(colorFilter);
                }
                return result;
            }
        };
        stateListDrawable.setEnterFadeDuration(1);
        stateListDrawable.setExitFadeDuration(Callback.DEFAULT_DRAG_ANIMATION_DURATION);
        stateListDrawable.addState(new int[]{16842913}, pressedDrawable);
        stateListDrawable.addState(new int[0], defaultDrawable);
        return stateListDrawable;
    }

    public static Drawable createEditTextDrawable(Context context, boolean alert) {
        Resources resources = context.getResources();
        Drawable defaultDrawable = resources.getDrawable(R.drawable.search_dark).mutate();
        defaultDrawable.setColorFilter(new PorterDuffColorFilter(getColor(alert ? key_dialogInputField : key_windowBackgroundWhiteInputField), Mode.MULTIPLY));
        Drawable pressedDrawable = resources.getDrawable(R.drawable.search_dark_activated).mutate();
        pressedDrawable.setColorFilter(new PorterDuffColorFilter(getColor(alert ? key_dialogInputFieldActivated : key_windowBackgroundWhiteInputFieldActivated), Mode.MULTIPLY));
        StateListDrawable stateListDrawable = new StateListDrawable() {
            public boolean selectDrawable(int index) {
                if (VERSION.SDK_INT >= 21) {
                    return super.selectDrawable(index);
                }
                Drawable drawable = Theme.getStateDrawable(this, index);
                ColorFilter colorFilter = null;
                if (drawable instanceof BitmapDrawable) {
                    colorFilter = ((BitmapDrawable) drawable).getPaint().getColorFilter();
                } else if (drawable instanceof NinePatchDrawable) {
                    colorFilter = ((NinePatchDrawable) drawable).getPaint().getColorFilter();
                }
                boolean result = super.selectDrawable(index);
                if (colorFilter != null) {
                    drawable.setColorFilter(colorFilter);
                }
                return result;
            }
        };
        stateListDrawable.addState(new int[]{16842910, 16842908}, pressedDrawable);
        stateListDrawable.addState(new int[]{16842908}, pressedDrawable);
        stateListDrawable.addState(StateSet.WILD_CARD, defaultDrawable);
        return stateListDrawable;
    }

    public static boolean canStartHolidayAnimation() {
        return canStartHolidayAnimation;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static android.graphics.drawable.Drawable getCurrentHolidayDrawable() {
        /*
        r0 = java.lang.System.currentTimeMillis();
        r2 = lastHolidayCheckTime;
        r4 = r0 - r2;
        r0 = 60000; // 0xea60 float:8.4078E-41 double:2.9644E-319;
        r2 = (r4 > r0 ? 1 : (r4 == r0 ? 0 : -1));
        if (r2 < 0) goto L_0x0079;
    L_0x000f:
        r0 = java.lang.System.currentTimeMillis();
        lastHolidayCheckTime = r0;
        r0 = java.util.Calendar.getInstance();
        r1 = java.lang.System.currentTimeMillis();
        r0.setTimeInMillis(r1);
        r1 = 2;
        r1 = r0.get(r1);
        r2 = 5;
        r2 = r0.get(r2);
        r3 = 12;
        r3 = r0.get(r3);
        r4 = 11;
        r5 = r0.get(r4);
        r6 = 0;
        r7 = 1;
        if (r1 != 0) goto L_0x0045;
    L_0x003a:
        if (r2 != r7) goto L_0x0045;
    L_0x003c:
        r8 = 10;
        if (r3 > r8) goto L_0x0045;
    L_0x0040:
        if (r5 != 0) goto L_0x0045;
    L_0x0042:
        canStartHolidayAnimation = r7;
        goto L_0x0047;
    L_0x0045:
        canStartHolidayAnimation = r6;
    L_0x0047:
        r8 = dialogs_holidayDrawable;
        if (r8 != 0) goto L_0x0079;
    L_0x004b:
        if (r1 != r4) goto L_0x005b;
    L_0x004d:
        r4 = org.telegram.messenger.BuildVars.DEBUG_PRIVATE_VERSION;
        r8 = 31;
        if (r4 == 0) goto L_0x0056;
    L_0x0053:
        r4 = 29;
        goto L_0x0057;
    L_0x0056:
        r4 = r8;
    L_0x0057:
        if (r2 < r4) goto L_0x005b;
    L_0x0059:
        if (r2 <= r8) goto L_0x005f;
    L_0x005b:
        if (r1 != 0) goto L_0x0079;
    L_0x005d:
        if (r2 != r7) goto L_0x0079;
    L_0x005f:
        r4 = org.telegram.messenger.ApplicationLoader.applicationContext;
        r4 = r4.getResources();
        r7 = 2131165537; // 0x7f070161 float:1.7945294E38 double:1.0529356774E-314;
        r4 = r4.getDrawable(r7);
        dialogs_holidayDrawable = r4;
        r4 = 1077936128; // 0x40400000 float:3.0 double:5.325712093E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = -r4;
        dialogs_holidayDrawableOffsetX = r4;
        dialogs_holidayDrawableOffsetY = r6;
    L_0x0079:
        r0 = dialogs_holidayDrawable;
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ActionBar.Theme.getCurrentHolidayDrawable():android.graphics.drawable.Drawable");
    }

    public static int getCurrentHolidayDrawableXOffset() {
        return dialogs_holidayDrawableOffsetX;
    }

    public static int getCurrentHolidayDrawableYOffset() {
        return dialogs_holidayDrawableOffsetY;
    }

    public static Drawable createSimpleSelectorDrawable(Context context, int resource, int defaultColor, int pressedColor) {
        Resources resources = context.getResources();
        Drawable defaultDrawable = resources.getDrawable(resource).mutate();
        if (defaultColor != 0) {
            defaultDrawable.setColorFilter(new PorterDuffColorFilter(defaultColor, Mode.MULTIPLY));
        }
        Drawable pressedDrawable = resources.getDrawable(resource).mutate();
        if (pressedColor != 0) {
            pressedDrawable.setColorFilter(new PorterDuffColorFilter(pressedColor, Mode.MULTIPLY));
        }
        StateListDrawable stateListDrawable = new StateListDrawable() {
            public boolean selectDrawable(int index) {
                if (VERSION.SDK_INT >= 21) {
                    return super.selectDrawable(index);
                }
                Drawable drawable = Theme.getStateDrawable(this, index);
                ColorFilter colorFilter = null;
                if (drawable instanceof BitmapDrawable) {
                    colorFilter = ((BitmapDrawable) drawable).getPaint().getColorFilter();
                } else if (drawable instanceof NinePatchDrawable) {
                    colorFilter = ((NinePatchDrawable) drawable).getPaint().getColorFilter();
                }
                boolean result = super.selectDrawable(index);
                if (colorFilter != null) {
                    drawable.setColorFilter(colorFilter);
                }
                return result;
            }
        };
        stateListDrawable.addState(new int[]{16842919}, pressedDrawable);
        stateListDrawable.addState(new int[]{16842913}, pressedDrawable);
        stateListDrawable.addState(StateSet.WILD_CARD, defaultDrawable);
        return stateListDrawable;
    }

    public static Drawable createCircleDrawable(int size, int color) {
        OvalShape ovalShape = new OvalShape();
        ovalShape.resize((float) size, (float) size);
        ShapeDrawable defaultDrawable = new ShapeDrawable(ovalShape);
        defaultDrawable.getPaint().setColor(color);
        return defaultDrawable;
    }

    public static CombinedDrawable createCircleDrawableWithIcon(int size, int iconRes) {
        return createCircleDrawableWithIcon(size, iconRes, 0);
    }

    public static CombinedDrawable createCircleDrawableWithIcon(int size, int iconRes, int stroke) {
        Drawable drawable;
        if (iconRes != 0) {
            drawable = ApplicationLoader.applicationContext.getResources().getDrawable(iconRes).mutate();
        } else {
            drawable = null;
        }
        return createCircleDrawableWithIcon(size, drawable, stroke);
    }

    public static CombinedDrawable createCircleDrawableWithIcon(int size, Drawable drawable, int stroke) {
        OvalShape ovalShape = new OvalShape();
        ovalShape.resize((float) size, (float) size);
        ShapeDrawable defaultDrawable = new ShapeDrawable(ovalShape);
        Paint paint = defaultDrawable.getPaint();
        paint.setColor(-1);
        if (stroke == 1) {
            paint.setStyle(Style.STROKE);
            paint.setStrokeWidth((float) AndroidUtilities.dp(2.0f));
        } else if (stroke == 2) {
            paint.setAlpha(0);
        }
        CombinedDrawable combinedDrawable = new CombinedDrawable(defaultDrawable, drawable);
        combinedDrawable.setCustomSize(size, size);
        return combinedDrawable;
    }

    public static Drawable createRoundRectDrawableWithIcon(int rad, int iconRes) {
        ShapeDrawable defaultDrawable = new ShapeDrawable(new RoundRectShape(new float[]{(float) rad, (float) rad, (float) rad, (float) rad, (float) rad, (float) rad, (float) rad, (float) rad}, null, null));
        defaultDrawable.getPaint().setColor(-1);
        return new CombinedDrawable(defaultDrawable, ApplicationLoader.applicationContext.getResources().getDrawable(iconRes).mutate());
    }

    public static void setCombinedDrawableColor(Drawable combinedDrawable, int color, boolean isIcon) {
        if (combinedDrawable instanceof CombinedDrawable) {
            Drawable drawable;
            if (isIcon) {
                drawable = ((CombinedDrawable) combinedDrawable).getIcon();
            } else {
                drawable = ((CombinedDrawable) combinedDrawable).getBackground();
            }
            drawable.setColorFilter(new PorterDuffColorFilter(color, Mode.MULTIPLY));
        }
    }

    public static Drawable createSimpleSelectorCircleDrawable(int size, int defaultColor, int pressedColor) {
        OvalShape ovalShape = new OvalShape();
        ovalShape.resize((float) size, (float) size);
        ShapeDrawable defaultDrawable = new ShapeDrawable(ovalShape);
        defaultDrawable.getPaint().setColor(defaultColor);
        ShapeDrawable pressedDrawable = new ShapeDrawable(ovalShape);
        if (VERSION.SDK_INT >= 21) {
            pressedDrawable.getPaint().setColor(-1);
            return new RippleDrawable(new ColorStateList(new int[][]{StateSet.WILD_CARD}, new int[]{pressedColor}), defaultDrawable, pressedDrawable);
        }
        pressedDrawable.getPaint().setColor(pressedColor);
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{16842919}, pressedDrawable);
        stateListDrawable.addState(new int[]{16842908}, pressedDrawable);
        stateListDrawable.addState(StateSet.WILD_CARD, defaultDrawable);
        return stateListDrawable;
    }

    public static Drawable createRoundRectDrawable(int rad, int defaultColor) {
        ShapeDrawable defaultDrawable = new ShapeDrawable(new RoundRectShape(new float[]{(float) rad, (float) rad, (float) rad, (float) rad, (float) rad, (float) rad, (float) rad, (float) rad}, null, null));
        defaultDrawable.getPaint().setColor(defaultColor);
        return defaultDrawable;
    }

    public static Drawable createSimpleSelectorRoundRectDrawable(int rad, int defaultColor, int pressedColor) {
        int i = rad;
        ShapeDrawable defaultDrawable = new ShapeDrawable(new RoundRectShape(new float[]{(float) i, (float) i, (float) i, (float) i, (float) i, (float) i, (float) i, (float) i}, null, null));
        defaultDrawable.getPaint().setColor(defaultColor);
        ShapeDrawable pressedDrawable = new ShapeDrawable(new RoundRectShape(new float[]{(float) i, (float) i, (float) i, (float) i, (float) i, (float) i, (float) i, (float) i}, null, null));
        pressedDrawable.getPaint().setColor(pressedColor);
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{16842919}, pressedDrawable);
        stateListDrawable.addState(new int[]{16842913}, pressedDrawable);
        stateListDrawable.addState(StateSet.WILD_CARD, defaultDrawable);
        return stateListDrawable;
    }

    public static Drawable getRoundRectSelectorDrawable() {
        if (VERSION.SDK_INT >= 21) {
            return new RippleDrawable(new ColorStateList(new int[][]{StateSet.WILD_CARD}, new int[]{getColor(key_dialogButtonSelector)}), null, createRoundRectDrawable(AndroidUtilities.dp(3.0f), -1));
        }
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{16842919}, createRoundRectDrawable(AndroidUtilities.dp(3.0f), getColor(key_dialogButtonSelector)));
        stateListDrawable.addState(new int[]{16842913}, createRoundRectDrawable(AndroidUtilities.dp(3.0f), getColor(key_dialogButtonSelector)));
        stateListDrawable.addState(StateSet.WILD_CARD, new ColorDrawable(0));
        return stateListDrawable;
    }

    public static Drawable getSelectorDrawable(boolean whiteBackground) {
        if (!whiteBackground) {
            return createSelectorDrawable(getColor(key_listSelector), 2);
        }
        if (VERSION.SDK_INT >= 21) {
            return new RippleDrawable(new ColorStateList(new int[][]{StateSet.WILD_CARD}, new int[]{getColor(key_listSelector)}), new ColorDrawable(getColor(key_windowBackgroundWhite)), new ColorDrawable(-1));
        }
        int color = getColor(key_listSelector);
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{16842919}, new ColorDrawable(color));
        stateListDrawable.addState(new int[]{16842913}, new ColorDrawable(color));
        stateListDrawable.addState(StateSet.WILD_CARD, new ColorDrawable(getColor(key_windowBackgroundWhite)));
        return stateListDrawable;
    }

    public static Drawable createSelectorDrawable(int color) {
        return createSelectorDrawable(color, 1);
    }

    public static Drawable createSelectorDrawable(int color, int maskType) {
        if (VERSION.SDK_INT >= 21) {
            Drawable maskDrawable = null;
            if (maskType == 1) {
                maskPaint.setColor(-1);
                maskDrawable = new Drawable() {
                    public void draw(Canvas canvas) {
                        Rect bounds = getBounds();
                        canvas.drawCircle((float) bounds.centerX(), (float) bounds.centerY(), (float) AndroidUtilities.dp(18.0f), Theme.maskPaint);
                    }

                    public void setAlpha(int alpha) {
                    }

                    public void setColorFilter(ColorFilter colorFilter) {
                    }

                    public int getOpacity() {
                        return 0;
                    }
                };
            } else if (maskType == 2) {
                maskDrawable = new ColorDrawable(-1);
            }
            return new RippleDrawable(new ColorStateList(new int[][]{StateSet.WILD_CARD}, new int[]{color}), null, maskDrawable);
        }
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{16842919}, new ColorDrawable(color));
        stateListDrawable.addState(new int[]{16842913}, new ColorDrawable(color));
        stateListDrawable.addState(StateSet.WILD_CARD, new ColorDrawable(0));
        return stateListDrawable;
    }

    public static void applyPreviousTheme() {
        if (previousTheme != null) {
            applyTheme(previousTheme, true, false, false);
            previousTheme = null;
            checkAutoNightThemeConditions();
        }
    }

    private static void sortThemes() {
        Collections.sort(themes, new Comparator<ThemeInfo>() {
            public int compare(ThemeInfo o1, ThemeInfo o2) {
                if (o1.pathToFile == null && o1.assetName == null) {
                    return -1;
                }
                if (o2.pathToFile == null && o2.assetName == null) {
                    return 1;
                }
                return o1.name.compareTo(o2.name);
            }
        });
    }

    public static ThemeInfo applyThemeFile(File file, String themeName, boolean temporary) {
        try {
            if (!(themeName.equals("Default") || themeName.equals("Dark"))) {
                if (!themeName.equals("Blue")) {
                    File finalFile = new File(ApplicationLoader.getFilesDirFixed(), themeName);
                    if (!AndroidUtilities.copyFile(file, finalFile)) {
                        return null;
                    }
                    boolean newTheme = false;
                    ThemeInfo themeInfo = (ThemeInfo) themesDict.get(themeName);
                    if (themeInfo == null) {
                        newTheme = true;
                        themeInfo = new ThemeInfo();
                        themeInfo.name = themeName;
                        themeInfo.pathToFile = finalFile.getAbsolutePath();
                    }
                    if (temporary) {
                        previousTheme = currentTheme;
                    } else {
                        previousTheme = null;
                        if (newTheme) {
                            themes.add(themeInfo);
                            themesDict.put(themeInfo.name, themeInfo);
                            otherThemes.add(themeInfo);
                            sortThemes();
                            saveOtherThemes();
                        }
                    }
                    applyTheme(themeInfo, temporary ^ 1, true, false);
                    return themeInfo;
                }
            }
            return null;
        } catch (Throwable e) {
            FileLog.e(e);
            return null;
        }
    }

    public static void applyTheme(ThemeInfo themeInfo) {
        applyTheme(themeInfo, true, true, false);
    }

    public static void applyTheme(ThemeInfo themeInfo, boolean animated) {
        applyTheme(themeInfo, true, true, animated);
    }

    public static void applyTheme(ThemeInfo themeInfo, boolean save, boolean removeWallpaperOverride, final boolean nightTheme) {
        if (themeInfo != null) {
            ThemeEditorView editorView = ThemeEditorView.getInstance();
            if (editorView != null) {
                editorView.destroy();
            }
            try {
                Editor editor;
                if (themeInfo.pathToFile == null) {
                    if (themeInfo.assetName == null) {
                        if (!nightTheme && save) {
                            editor = MessagesController.getGlobalMainSettings().edit();
                            editor.remove("theme");
                            if (removeWallpaperOverride) {
                                editor.remove("overrideThemeWallpaper");
                            }
                            editor.commit();
                        }
                        currentColors.clear();
                        wallpaper = null;
                        themedWallpaper = null;
                        currentTheme = themeInfo;
                        if (!nightTheme) {
                            currentDayTheme = currentTheme;
                        }
                        reloadWallpaper();
                        applyCommonTheme();
                        applyDialogsTheme();
                        applyProfileTheme();
                        applyChatTheme(false);
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            public void run() {
                                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didSetNewTheme, Boolean.valueOf(nightTheme));
                            }
                        });
                    }
                }
                if (!nightTheme && save) {
                    editor = MessagesController.getGlobalMainSettings().edit();
                    editor.putString("theme", themeInfo.name);
                    if (removeWallpaperOverride) {
                        editor.remove("overrideThemeWallpaper");
                    }
                    editor.commit();
                }
                if (themeInfo.assetName != null) {
                    currentColors = getThemeFileValues(null, themeInfo.assetName);
                } else {
                    currentColors = getThemeFileValues(new File(themeInfo.pathToFile), null);
                }
                currentTheme = themeInfo;
                if (nightTheme) {
                    currentDayTheme = currentTheme;
                }
                reloadWallpaper();
                applyCommonTheme();
                applyDialogsTheme();
                applyProfileTheme();
                applyChatTheme(false);
                AndroidUtilities.runOnUIThread(/* anonymous class already generated */);
            } catch (Throwable e) {
                FileLog.e(e);
            }
        }
    }

    private static void saveOtherThemes() {
        int a = 0;
        Editor editor = ApplicationLoader.applicationContext.getSharedPreferences("themeconfig", 0).edit();
        JSONArray array = new JSONArray();
        while (a < otherThemes.size()) {
            JSONObject jsonObject = ((ThemeInfo) otherThemes.get(a)).getSaveJson();
            if (jsonObject != null) {
                array.put(jsonObject);
            }
            a++;
        }
        editor.putString("themes2", array.toString());
        editor.commit();
    }

    public static HashMap<String, Integer> getDefaultColors() {
        return defaultColors;
    }

    public static String getCurrentThemeName() {
        String text = currentDayTheme.getName();
        if (text.toLowerCase().endsWith(".attheme")) {
            return text.substring(0, text.lastIndexOf(46));
        }
        return text;
    }

    public static String getCurrentNightThemeName() {
        if (currentNightTheme == null) {
            return TtmlNode.ANONYMOUS_REGION_ID;
        }
        String text = currentNightTheme.getName();
        if (text.toLowerCase().endsWith(".attheme")) {
            text = text.substring(0, text.lastIndexOf(46));
        }
        return text;
    }

    public static ThemeInfo getCurrentTheme() {
        return currentDayTheme != null ? currentDayTheme : defaultTheme;
    }

    public static ThemeInfo getCurrentNightTheme() {
        return currentNightTheme;
    }

    public static boolean isCurrentThemeNight() {
        return currentTheme == currentNightTheme;
    }

    private static long getAutoNightSwitchThemeDelay() {
        if (Math.abs(lastThemeSwitchTime - SystemClock.uptimeMillis()) >= 12000) {
            return 1800;
        }
        return 12000;
    }

    public static void setCurrentNightTheme(ThemeInfo theme) {
        boolean apply = currentTheme == currentNightTheme;
        currentNightTheme = theme;
        if (apply) {
            applyDayNightThemeMaybe(true);
        }
    }

    public static void checkAutoNightThemeConditions() {
        checkAutoNightThemeConditions(false);
    }

    public static void checkAutoNightThemeConditions(boolean force) {
        if (previousTheme == null) {
            boolean z = false;
            if (force) {
                if (switchNightRunnableScheduled) {
                    switchNightRunnableScheduled = false;
                    AndroidUtilities.cancelRunOnUIThread(switchNightBrightnessRunnable);
                }
                if (switchDayRunnableScheduled) {
                    switchDayRunnableScheduled = false;
                    AndroidUtilities.cancelRunOnUIThread(switchDayBrightnessRunnable);
                }
            }
            if (selectedAutoNightType != 2) {
                if (switchNightRunnableScheduled) {
                    switchNightRunnableScheduled = false;
                    AndroidUtilities.cancelRunOnUIThread(switchNightBrightnessRunnable);
                }
                if (switchDayRunnableScheduled) {
                    switchDayRunnableScheduled = false;
                    AndroidUtilities.cancelRunOnUIThread(switchDayBrightnessRunnable);
                }
                if (lightSensorRegistered) {
                    lastBrightnessValue = 1.0f;
                    sensorManager.unregisterListener(ambientSensorListener, lightSensor);
                    lightSensorRegistered = false;
                    if (BuildVars.LOGS_ENABLED) {
                        FileLog.d("light sensor unregistered");
                    }
                }
            }
            int switchToTheme = 0;
            if (selectedAutoNightType == 1) {
                int day;
                int timeStart;
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                int time = (calendar.get(11) * 60) + calendar.get(12);
                if (autoNightScheduleByLocation) {
                    day = calendar.get(5);
                    if (!(autoNightLastSunCheckDay == day || autoNightLocationLatitude == 10000.0d || autoNightLocationLongitude == 10000.0d)) {
                        int[] t = SunDate.calculateSunriseSunset(autoNightLocationLatitude, autoNightLocationLongitude);
                        autoNightSunriseTime = t[0];
                        autoNightSunsetTime = t[1];
                        autoNightLastSunCheckDay = day;
                        saveAutoNightThemeConfig();
                    }
                    timeStart = autoNightSunsetTime;
                    day = autoNightSunriseTime;
                } else {
                    timeStart = autoNightDayStartTime;
                    day = autoNightDayEndTime;
                }
                if (timeStart < day) {
                    if (timeStart > time || time > day) {
                        switchToTheme = 1;
                    } else {
                        switchToTheme = 2;
                    }
                } else if ((timeStart > time || time > 1440) && (time < 0 || time > day)) {
                    switchToTheme = 1;
                } else {
                    switchToTheme = 2;
                }
            } else if (selectedAutoNightType == 2) {
                if (lightSensor == null) {
                    sensorManager = (SensorManager) ApplicationLoader.applicationContext.getSystemService("sensor");
                    lightSensor = sensorManager.getDefaultSensor(5);
                }
                if (!(lightSensorRegistered || lightSensor == null)) {
                    sensorManager.registerListener(ambientSensorListener, lightSensor, 500000);
                    lightSensorRegistered = true;
                    if (BuildVars.LOGS_ENABLED) {
                        FileLog.d("light sensor registered");
                    }
                }
                if (lastBrightnessValue <= autoNightBrighnessThreshold) {
                    if (!switchNightRunnableScheduled) {
                        switchToTheme = 2;
                    }
                } else if (!switchDayRunnableScheduled) {
                    switchToTheme = 1;
                }
            } else if (selectedAutoNightType == 0) {
                switchToTheme = 1;
            }
            if (switchToTheme != 0) {
                if (switchToTheme == 2) {
                    z = true;
                }
                applyDayNightThemeMaybe(z);
            }
            if (force) {
                lastThemeSwitchTime = 0;
            }
        }
    }

    private static void applyDayNightThemeMaybe(boolean night) {
        if (night) {
            if (currentTheme != currentNightTheme) {
                lastThemeSwitchTime = SystemClock.uptimeMillis();
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needSetDayNightTheme, currentNightTheme);
            }
        } else if (currentTheme != currentDayTheme) {
            lastThemeSwitchTime = SystemClock.uptimeMillis();
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needSetDayNightTheme, currentDayTheme);
        }
    }

    public static boolean deleteTheme(ThemeInfo themeInfo) {
        if (themeInfo.pathToFile == null) {
            return false;
        }
        boolean currentThemeDeleted = false;
        if (currentTheme == themeInfo) {
            applyTheme(defaultTheme, true, false, false);
            currentThemeDeleted = true;
        }
        otherThemes.remove(themeInfo);
        themesDict.remove(themeInfo.name);
        themes.remove(themeInfo);
        new File(themeInfo.pathToFile).delete();
        saveOtherThemes();
        return currentThemeDeleted;
    }

    public static File getAssetFile(String assetName) {
        long size;
        File file = new File(ApplicationLoader.getFilesDirFixed(), assetName);
        try {
            InputStream stream = ApplicationLoader.applicationContext.getAssets().open(assetName);
            size = (long) stream.available();
            stream.close();
        } catch (Throwable e) {
            size = 0;
            FileLog.e(e);
        }
        if (!(file.exists() && (size == 0 || file.length() == size))) {
            stream = null;
            try {
                stream = ApplicationLoader.applicationContext.getAssets().open(assetName);
                AndroidUtilities.copyFile(stream, file);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (Exception e2) {
                    }
                }
            } catch (Throwable e3) {
                FileLog.e(e3);
                if (stream != null) {
                    stream.close();
                }
            } catch (Throwable th) {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (Exception e4) {
                    }
                }
            }
        }
        return file;
    }

    public static void createCommonResources(Context context) {
        if (dividerPaint == null) {
            dividerPaint = new Paint();
            dividerPaint.setStrokeWidth(1.0f);
            avatar_backgroundPaint = new Paint(1);
            checkboxSquare_checkPaint = new Paint(1);
            checkboxSquare_checkPaint.setStyle(Style.STROKE);
            checkboxSquare_checkPaint.setStrokeWidth((float) AndroidUtilities.dp(2.0f));
            checkboxSquare_eraserPaint = new Paint(1);
            checkboxSquare_eraserPaint.setColor(0);
            checkboxSquare_eraserPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
            checkboxSquare_backgroundPaint = new Paint(1);
            linkSelectionPaint = new Paint();
            Resources resources = context.getResources();
            avatar_broadcastDrawable = resources.getDrawable(R.drawable.broadcast_w);
            avatar_savedDrawable = resources.getDrawable(R.drawable.bookmark_large);
            avatar_photoDrawable = resources.getDrawable(R.drawable.photo_w);
            applyCommonTheme();
        }
    }

    public static void applyCommonTheme() {
        if (dividerPaint != null) {
            dividerPaint.setColor(getColor(key_divider));
            linkSelectionPaint.setColor(getColor(key_windowBackgroundWhiteLinkSelection));
            setDrawableColorByKey(avatar_broadcastDrawable, key_avatar_text);
            setDrawableColorByKey(avatar_savedDrawable, key_avatar_text);
            setDrawableColorByKey(avatar_photoDrawable, key_avatar_text);
        }
    }

    public static void createDialogsResources(Context context) {
        createCommonResources(context);
        if (dialogs_namePaint == null) {
            Resources resources = context.getResources();
            dialogs_namePaint = new TextPaint(1);
            dialogs_namePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            dialogs_nameEncryptedPaint = new TextPaint(1);
            dialogs_nameEncryptedPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            dialogs_messagePaint = new TextPaint(1);
            dialogs_messagePrintingPaint = new TextPaint(1);
            dialogs_timePaint = new TextPaint(1);
            dialogs_countTextPaint = new TextPaint(1);
            dialogs_countTextPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            dialogs_onlinePaint = new TextPaint(1);
            dialogs_offlinePaint = new TextPaint(1);
            dialogs_tabletSeletedPaint = new Paint();
            dialogs_pinnedPaint = new Paint();
            dialogs_countPaint = new Paint(1);
            dialogs_countGrayPaint = new Paint(1);
            dialogs_errorPaint = new Paint(1);
            dialogs_lockDrawable = resources.getDrawable(R.drawable.list_secret);
            dialogs_checkDrawable = resources.getDrawable(R.drawable.list_check);
            dialogs_halfCheckDrawable = resources.getDrawable(R.drawable.list_halfcheck);
            dialogs_clockDrawable = resources.getDrawable(R.drawable.msg_clock).mutate();
            dialogs_errorDrawable = resources.getDrawable(R.drawable.list_warning_sign);
            dialogs_groupDrawable = resources.getDrawable(R.drawable.list_group);
            dialogs_broadcastDrawable = resources.getDrawable(R.drawable.list_broadcast);
            dialogs_muteDrawable = resources.getDrawable(R.drawable.list_mute).mutate();
            dialogs_verifiedDrawable = resources.getDrawable(R.drawable.verified_area);
            dialogs_verifiedCheckDrawable = resources.getDrawable(R.drawable.verified_check);
            dialogs_mentionDrawable = resources.getDrawable(R.drawable.mentionchatslist);
            dialogs_botDrawable = resources.getDrawable(R.drawable.list_bot);
            dialogs_pinnedDrawable = resources.getDrawable(R.drawable.list_pin);
            applyDialogsTheme();
        }
        dialogs_namePaint.setTextSize((float) AndroidUtilities.dp(17.0f));
        dialogs_nameEncryptedPaint.setTextSize((float) AndroidUtilities.dp(17.0f));
        dialogs_messagePaint.setTextSize((float) AndroidUtilities.dp(16.0f));
        dialogs_messagePrintingPaint.setTextSize((float) AndroidUtilities.dp(16.0f));
        dialogs_timePaint.setTextSize((float) AndroidUtilities.dp(13.0f));
        dialogs_countTextPaint.setTextSize((float) AndroidUtilities.dp(13.0f));
        dialogs_onlinePaint.setTextSize((float) AndroidUtilities.dp(16.0f));
        dialogs_offlinePaint.setTextSize((float) AndroidUtilities.dp(16.0f));
    }

    public static void applyDialogsTheme() {
        if (dialogs_namePaint != null) {
            dialogs_namePaint.setColor(getColor(key_chats_name));
            dialogs_nameEncryptedPaint.setColor(getColor(key_chats_secretName));
            TextPaint textPaint = dialogs_messagePaint;
            TextPaint textPaint2 = dialogs_messagePaint;
            int color = getColor(key_chats_message);
            textPaint2.linkColor = color;
            textPaint.setColor(color);
            dialogs_tabletSeletedPaint.setColor(getColor(key_chats_tabletSelectedOverlay));
            dialogs_pinnedPaint.setColor(getColor(key_chats_pinnedOverlay));
            dialogs_timePaint.setColor(getColor(key_chats_date));
            dialogs_countTextPaint.setColor(getColor(key_chats_unreadCounterText));
            dialogs_messagePrintingPaint.setColor(getColor(key_chats_actionMessage));
            dialogs_countPaint.setColor(getColor(key_chats_unreadCounter));
            dialogs_countGrayPaint.setColor(getColor(key_chats_unreadCounterMuted));
            dialogs_errorPaint.setColor(getColor(key_chats_sentError));
            dialogs_onlinePaint.setColor(getColor(key_windowBackgroundWhiteBlueText3));
            dialogs_offlinePaint.setColor(getColor(key_windowBackgroundWhiteGrayText3));
            setDrawableColorByKey(dialogs_lockDrawable, key_chats_secretIcon);
            setDrawableColorByKey(dialogs_checkDrawable, key_chats_sentCheck);
            setDrawableColorByKey(dialogs_halfCheckDrawable, key_chats_sentCheck);
            setDrawableColorByKey(dialogs_clockDrawable, key_chats_sentClock);
            setDrawableColorByKey(dialogs_errorDrawable, key_chats_sentErrorIcon);
            setDrawableColorByKey(dialogs_groupDrawable, key_chats_nameIcon);
            setDrawableColorByKey(dialogs_broadcastDrawable, key_chats_nameIcon);
            setDrawableColorByKey(dialogs_botDrawable, key_chats_nameIcon);
            setDrawableColorByKey(dialogs_pinnedDrawable, key_chats_pinnedIcon);
            setDrawableColorByKey(dialogs_muteDrawable, key_chats_muteIcon);
            setDrawableColorByKey(dialogs_verifiedDrawable, key_chats_verifiedBackground);
            setDrawableColorByKey(dialogs_verifiedCheckDrawable, key_chats_verifiedCheck);
        }
    }

    public static void destroyResources() {
        for (int a = 0; a < chat_attachButtonDrawables.length; a++) {
            if (chat_attachButtonDrawables[a] != null) {
                chat_attachButtonDrawables[a].setCallback(null);
            }
        }
    }

    public static void createChatResources(Context context, boolean fontsOnly) {
        int i = sync;
        synchronized (i) {
            try {
                if (chat_msgTextPaint == null) {
                    chat_msgTextPaint = new TextPaint(1);
                    chat_msgGameTextPaint = new TextPaint(1);
                    chat_msgTextPaintOneEmoji = new TextPaint(1);
                    chat_msgTextPaintTwoEmoji = new TextPaint(1);
                    chat_msgTextPaintThreeEmoji = new TextPaint(1);
                    chat_msgBotButtonPaint = new TextPaint(1);
                    chat_msgBotButtonPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                }
            } finally {
                Object obj = r0;
            }
        }
        i = 2;
        if (!fontsOnly && chat_msgInDrawable == null) {
            chat_infoPaint = new TextPaint(1);
            chat_docNamePaint = new TextPaint(1);
            chat_docNamePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            chat_docBackPaint = new Paint(1);
            chat_deleteProgressPaint = new Paint(1);
            chat_botProgressPaint = new Paint(1);
            chat_botProgressPaint.setStrokeCap(Cap.ROUND);
            chat_botProgressPaint.setStyle(Style.STROKE);
            chat_locationTitlePaint = new TextPaint(1);
            chat_locationTitlePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            chat_locationAddressPaint = new TextPaint(1);
            chat_urlPaint = new Paint();
            chat_textSearchSelectionPaint = new Paint();
            chat_radialProgressPaint = new Paint(1);
            chat_radialProgressPaint.setStrokeCap(Cap.ROUND);
            chat_radialProgressPaint.setStyle(Style.STROKE);
            chat_radialProgressPaint.setColor(-1610612737);
            chat_radialProgress2Paint = new Paint(1);
            chat_radialProgress2Paint.setStrokeCap(Cap.ROUND);
            chat_radialProgress2Paint.setStyle(Style.STROKE);
            chat_audioTimePaint = new TextPaint(1);
            chat_livePaint = new TextPaint(1);
            chat_livePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            chat_audioTitlePaint = new TextPaint(1);
            chat_audioTitlePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            chat_audioPerformerPaint = new TextPaint(1);
            chat_botButtonPaint = new TextPaint(1);
            chat_botButtonPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            chat_contactNamePaint = new TextPaint(1);
            chat_contactNamePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            chat_contactPhonePaint = new TextPaint(1);
            chat_durationPaint = new TextPaint(1);
            chat_gamePaint = new TextPaint(1);
            chat_gamePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            chat_shipmentPaint = new TextPaint(1);
            chat_timePaint = new TextPaint(1);
            chat_adminPaint = new TextPaint(1);
            chat_namePaint = new TextPaint(1);
            chat_namePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            chat_forwardNamePaint = new TextPaint(1);
            chat_replyNamePaint = new TextPaint(1);
            chat_replyNamePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            chat_replyTextPaint = new TextPaint(1);
            chat_instantViewPaint = new TextPaint(1);
            chat_instantViewPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            chat_instantViewRectPaint = new Paint(1);
            chat_instantViewRectPaint.setStyle(Style.STROKE);
            chat_replyLinePaint = new Paint();
            chat_msgErrorPaint = new Paint(1);
            chat_statusPaint = new Paint(1);
            chat_statusRecordPaint = new Paint(1);
            chat_statusRecordPaint.setStyle(Style.STROKE);
            chat_statusRecordPaint.setStrokeCap(Cap.ROUND);
            chat_actionTextPaint = new TextPaint(1);
            chat_actionTextPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            chat_actionBackgroundPaint = new Paint(1);
            chat_timeBackgroundPaint = new Paint(1);
            chat_contextResult_titleTextPaint = new TextPaint(1);
            chat_contextResult_titleTextPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            chat_contextResult_descriptionTextPaint = new TextPaint(1);
            chat_composeBackgroundPaint = new Paint();
            Resources resources = context.getResources();
            chat_msgInDrawable = resources.getDrawable(R.drawable.msg_in).mutate();
            chat_msgInSelectedDrawable = resources.getDrawable(R.drawable.msg_in).mutate();
            chat_msgOutDrawable = resources.getDrawable(R.drawable.msg_out).mutate();
            chat_msgOutSelectedDrawable = resources.getDrawable(R.drawable.msg_out).mutate();
            chat_msgInMediaDrawable = resources.getDrawable(R.drawable.msg_photo).mutate();
            chat_msgInMediaSelectedDrawable = resources.getDrawable(R.drawable.msg_photo).mutate();
            chat_msgOutMediaDrawable = resources.getDrawable(R.drawable.msg_photo).mutate();
            chat_msgOutMediaSelectedDrawable = resources.getDrawable(R.drawable.msg_photo).mutate();
            chat_msgOutCheckDrawable = resources.getDrawable(R.drawable.msg_check).mutate();
            chat_msgOutCheckSelectedDrawable = resources.getDrawable(R.drawable.msg_check).mutate();
            chat_msgMediaCheckDrawable = resources.getDrawable(R.drawable.msg_check).mutate();
            chat_msgStickerCheckDrawable = resources.getDrawable(R.drawable.msg_check).mutate();
            chat_msgOutHalfCheckDrawable = resources.getDrawable(R.drawable.msg_halfcheck).mutate();
            chat_msgOutHalfCheckSelectedDrawable = resources.getDrawable(R.drawable.msg_halfcheck).mutate();
            chat_msgMediaHalfCheckDrawable = resources.getDrawable(R.drawable.msg_halfcheck).mutate();
            chat_msgStickerHalfCheckDrawable = resources.getDrawable(R.drawable.msg_halfcheck).mutate();
            chat_msgOutClockDrawable = resources.getDrawable(R.drawable.msg_clock).mutate();
            chat_msgOutSelectedClockDrawable = resources.getDrawable(R.drawable.msg_clock).mutate();
            chat_msgInClockDrawable = resources.getDrawable(R.drawable.msg_clock).mutate();
            chat_msgInSelectedClockDrawable = resources.getDrawable(R.drawable.msg_clock).mutate();
            chat_msgMediaClockDrawable = resources.getDrawable(R.drawable.msg_clock).mutate();
            chat_msgStickerClockDrawable = resources.getDrawable(R.drawable.msg_clock).mutate();
            chat_msgInViewsDrawable = resources.getDrawable(R.drawable.msg_views).mutate();
            chat_msgInViewsSelectedDrawable = resources.getDrawable(R.drawable.msg_views).mutate();
            chat_msgOutViewsDrawable = resources.getDrawable(R.drawable.msg_views).mutate();
            chat_msgOutViewsSelectedDrawable = resources.getDrawable(R.drawable.msg_views).mutate();
            chat_msgMediaViewsDrawable = resources.getDrawable(R.drawable.msg_views).mutate();
            chat_msgStickerViewsDrawable = resources.getDrawable(R.drawable.msg_views).mutate();
            chat_msgInMenuDrawable = resources.getDrawable(R.drawable.msg_actions).mutate();
            chat_msgInMenuSelectedDrawable = resources.getDrawable(R.drawable.msg_actions).mutate();
            chat_msgOutMenuDrawable = resources.getDrawable(R.drawable.msg_actions).mutate();
            chat_msgOutMenuSelectedDrawable = resources.getDrawable(R.drawable.msg_actions).mutate();
            chat_msgMediaMenuDrawable = resources.getDrawable(R.drawable.video_actions);
            chat_msgInInstantDrawable = resources.getDrawable(R.drawable.msg_instant).mutate();
            chat_msgOutInstantDrawable = resources.getDrawable(R.drawable.msg_instant).mutate();
            chat_msgErrorDrawable = resources.getDrawable(R.drawable.msg_warning);
            chat_muteIconDrawable = resources.getDrawable(R.drawable.list_mute).mutate();
            chat_lockIconDrawable = resources.getDrawable(R.drawable.ic_lock_header);
            chat_msgBroadcastDrawable = resources.getDrawable(R.drawable.broadcast3).mutate();
            chat_msgBroadcastMediaDrawable = resources.getDrawable(R.drawable.broadcast3).mutate();
            chat_msgInCallDrawable = resources.getDrawable(R.drawable.ic_call_white_24dp).mutate();
            chat_msgInCallSelectedDrawable = resources.getDrawable(R.drawable.ic_call_white_24dp).mutate();
            chat_msgOutCallDrawable = resources.getDrawable(R.drawable.ic_call_white_24dp).mutate();
            chat_msgOutCallSelectedDrawable = resources.getDrawable(R.drawable.ic_call_white_24dp).mutate();
            chat_msgCallUpRedDrawable = resources.getDrawable(R.drawable.ic_call_made_green_18dp).mutate();
            chat_msgCallUpGreenDrawable = resources.getDrawable(R.drawable.ic_call_made_green_18dp).mutate();
            chat_msgCallDownRedDrawable = resources.getDrawable(R.drawable.ic_call_received_green_18dp).mutate();
            chat_msgCallDownGreenDrawable = resources.getDrawable(R.drawable.ic_call_received_green_18dp).mutate();
            chat_msgAvatarLiveLocationDrawable = resources.getDrawable(R.drawable.livepin).mutate();
            chat_inlineResultFile = resources.getDrawable(R.drawable.bot_file);
            chat_inlineResultAudio = resources.getDrawable(R.drawable.bot_music);
            chat_inlineResultLocation = resources.getDrawable(R.drawable.bot_location);
            chat_msgInShadowDrawable = resources.getDrawable(R.drawable.msg_in_shadow);
            chat_msgOutShadowDrawable = resources.getDrawable(R.drawable.msg_out_shadow);
            chat_msgInMediaShadowDrawable = resources.getDrawable(R.drawable.msg_photo_shadow);
            chat_msgOutMediaShadowDrawable = resources.getDrawable(R.drawable.msg_photo_shadow);
            chat_botLinkDrawalbe = resources.getDrawable(R.drawable.bot_link);
            chat_botInlineDrawable = resources.getDrawable(R.drawable.bot_lines);
            chat_systemDrawable = resources.getDrawable(R.drawable.system);
            chat_contextResult_shadowUnderSwitchDrawable = resources.getDrawable(R.drawable.header_shadow).mutate();
            chat_attachButtonDrawables[0] = resources.getDrawable(R.drawable.attach_camera_states);
            chat_attachButtonDrawables[1] = resources.getDrawable(R.drawable.attach_gallery_states);
            chat_attachButtonDrawables[2] = resources.getDrawable(R.drawable.attach_video_states);
            chat_attachButtonDrawables[3] = resources.getDrawable(R.drawable.attach_audio_states);
            chat_attachButtonDrawables[4] = resources.getDrawable(R.drawable.attach_file_states);
            chat_attachButtonDrawables[5] = resources.getDrawable(R.drawable.attach_contact_states);
            chat_attachButtonDrawables[6] = resources.getDrawable(R.drawable.attach_location_states);
            chat_attachButtonDrawables[7] = resources.getDrawable(R.drawable.attach_hide_states);
            chat_cornerOuter[0] = resources.getDrawable(R.drawable.corner_out_tl);
            chat_cornerOuter[1] = resources.getDrawable(R.drawable.corner_out_tr);
            chat_cornerOuter[2] = resources.getDrawable(R.drawable.corner_out_br);
            chat_cornerOuter[3] = resources.getDrawable(R.drawable.corner_out_bl);
            chat_cornerInner[0] = resources.getDrawable(R.drawable.corner_in_tr);
            chat_cornerInner[1] = resources.getDrawable(R.drawable.corner_in_tl);
            chat_cornerInner[2] = resources.getDrawable(R.drawable.corner_in_br);
            chat_cornerInner[3] = resources.getDrawable(R.drawable.corner_in_bl);
            chat_shareDrawable = resources.getDrawable(R.drawable.share_round);
            chat_shareIconDrawable = resources.getDrawable(R.drawable.share_arrow);
            chat_replyIconDrawable = resources.getDrawable(R.drawable.fast_reply);
            chat_goIconDrawable = resources.getDrawable(R.drawable.message_arrow);
            chat_ivStatesDrawable[0][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(40.0f), (int) R.drawable.msg_round_play_m, 1);
            chat_ivStatesDrawable[0][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(40.0f), (int) R.drawable.msg_round_play_m, 1);
            chat_ivStatesDrawable[1][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(40.0f), (int) R.drawable.msg_round_pause_m, 1);
            chat_ivStatesDrawable[1][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(40.0f), (int) R.drawable.msg_round_pause_m, 1);
            chat_ivStatesDrawable[2][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(40.0f), (int) R.drawable.msg_round_load_m, 1);
            chat_ivStatesDrawable[2][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(40.0f), (int) R.drawable.msg_round_load_m, 1);
            chat_ivStatesDrawable[3][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(40.0f), (int) R.drawable.msg_round_cancel_m, 2);
            chat_ivStatesDrawable[3][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(40.0f), (int) R.drawable.msg_round_cancel_m, 2);
            chat_fileMiniStatesDrawable[0][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(22.0f), R.drawable.audio_mini_arrow);
            chat_fileMiniStatesDrawable[0][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(22.0f), R.drawable.audio_mini_arrow);
            chat_fileMiniStatesDrawable[1][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(22.0f), R.drawable.audio_mini_cancel);
            chat_fileMiniStatesDrawable[1][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(22.0f), R.drawable.audio_mini_cancel);
            chat_fileMiniStatesDrawable[2][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(22.0f), R.drawable.audio_mini_arrow);
            chat_fileMiniStatesDrawable[2][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(22.0f), R.drawable.audio_mini_arrow);
            chat_fileMiniStatesDrawable[3][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(22.0f), R.drawable.audio_mini_cancel);
            chat_fileMiniStatesDrawable[3][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(22.0f), R.drawable.audio_mini_cancel);
            chat_fileMiniStatesDrawable[4][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(22.0f), R.drawable.video_mini_arrow);
            chat_fileMiniStatesDrawable[4][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(22.0f), R.drawable.video_mini_arrow);
            chat_fileMiniStatesDrawable[5][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(22.0f), R.drawable.video_mini_cancel);
            chat_fileMiniStatesDrawable[5][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(22.0f), R.drawable.video_mini_cancel);
            chat_fileStatesDrawable[0][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0f), R.drawable.msg_round_play_m);
            chat_fileStatesDrawable[0][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0f), R.drawable.msg_round_play_m);
            chat_fileStatesDrawable[1][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0f), R.drawable.msg_round_pause_m);
            chat_fileStatesDrawable[1][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0f), R.drawable.msg_round_pause_m);
            chat_fileStatesDrawable[2][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0f), R.drawable.msg_round_load_m);
            chat_fileStatesDrawable[2][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0f), R.drawable.msg_round_load_m);
            chat_fileStatesDrawable[3][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0f), R.drawable.msg_round_file_s);
            chat_fileStatesDrawable[3][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0f), R.drawable.msg_round_file_s);
            chat_fileStatesDrawable[4][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0f), R.drawable.msg_round_cancel_m);
            chat_fileStatesDrawable[4][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0f), R.drawable.msg_round_cancel_m);
            chat_fileStatesDrawable[5][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0f), R.drawable.msg_round_play_m);
            chat_fileStatesDrawable[5][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0f), R.drawable.msg_round_play_m);
            chat_fileStatesDrawable[6][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0f), R.drawable.msg_round_pause_m);
            chat_fileStatesDrawable[6][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0f), R.drawable.msg_round_pause_m);
            chat_fileStatesDrawable[7][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0f), R.drawable.msg_round_load_m);
            chat_fileStatesDrawable[7][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0f), R.drawable.msg_round_load_m);
            chat_fileStatesDrawable[8][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0f), R.drawable.msg_round_file_s);
            chat_fileStatesDrawable[8][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0f), R.drawable.msg_round_file_s);
            chat_fileStatesDrawable[9][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0f), R.drawable.msg_round_cancel_m);
            chat_fileStatesDrawable[9][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0f), R.drawable.msg_round_cancel_m);
            chat_photoStatesDrawables[0][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(48.0f), R.drawable.msg_round_load_m);
            chat_photoStatesDrawables[0][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(48.0f), R.drawable.msg_round_load_m);
            chat_photoStatesDrawables[1][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(48.0f), R.drawable.msg_round_cancel_m);
            chat_photoStatesDrawables[1][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(48.0f), R.drawable.msg_round_cancel_m);
            chat_photoStatesDrawables[2][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(48.0f), R.drawable.msg_round_gif_m);
            chat_photoStatesDrawables[2][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(48.0f), R.drawable.msg_round_gif_m);
            chat_photoStatesDrawables[3][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(48.0f), R.drawable.msg_round_play_m);
            chat_photoStatesDrawables[3][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(48.0f), R.drawable.msg_round_play_m);
            Drawable[] drawableArr = chat_photoStatesDrawables[4];
            Drawable[] drawableArr2 = chat_photoStatesDrawables[4];
            Drawable drawable = resources.getDrawable(R.drawable.burn);
            drawableArr2[1] = drawable;
            drawableArr[0] = drawable;
            drawableArr = chat_photoStatesDrawables[5];
            drawableArr2 = chat_photoStatesDrawables[5];
            drawable = resources.getDrawable(R.drawable.circle);
            drawableArr2[1] = drawable;
            drawableArr[0] = drawable;
            drawableArr = chat_photoStatesDrawables[6];
            drawableArr2 = chat_photoStatesDrawables[6];
            drawable = resources.getDrawable(R.drawable.photocheck);
            drawableArr2[1] = drawable;
            drawableArr[0] = drawable;
            chat_photoStatesDrawables[7][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(48.0f), R.drawable.msg_round_load_m);
            chat_photoStatesDrawables[7][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(48.0f), R.drawable.msg_round_load_m);
            chat_photoStatesDrawables[8][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(48.0f), R.drawable.msg_round_cancel_m);
            chat_photoStatesDrawables[8][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(48.0f), R.drawable.msg_round_cancel_m);
            chat_photoStatesDrawables[9][0] = resources.getDrawable(R.drawable.doc_big).mutate();
            chat_photoStatesDrawables[9][1] = resources.getDrawable(R.drawable.doc_big).mutate();
            chat_photoStatesDrawables[10][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(48.0f), R.drawable.msg_round_load_m);
            chat_photoStatesDrawables[10][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(48.0f), R.drawable.msg_round_load_m);
            chat_photoStatesDrawables[11][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(48.0f), R.drawable.msg_round_cancel_m);
            chat_photoStatesDrawables[11][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(48.0f), R.drawable.msg_round_cancel_m);
            chat_photoStatesDrawables[12][0] = resources.getDrawable(R.drawable.doc_big).mutate();
            chat_photoStatesDrawables[12][1] = resources.getDrawable(R.drawable.doc_big).mutate();
            chat_contactDrawable[0] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0f), R.drawable.msg_contact);
            chat_contactDrawable[1] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0f), R.drawable.msg_contact);
            chat_locationDrawable[0] = createRoundRectDrawableWithIcon(AndroidUtilities.dp(2.0f), R.drawable.msg_location);
            chat_locationDrawable[1] = createRoundRectDrawableWithIcon(AndroidUtilities.dp(2.0f), R.drawable.msg_location);
            chat_composeShadowDrawable = context.getResources().getDrawable(R.drawable.compose_panel_shadow);
            try {
                int bitmapSize = AndroidUtilities.roundMessageSize + AndroidUtilities.dp(6.0f);
                Bitmap bitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                Paint paint = new Paint(1);
                try {
                    canvas.setBitmap(null);
                } catch (Exception e) {
                }
                chat_roundVideoShadow = new BitmapDrawable(bitmap);
            } catch (Throwable th) {
            }
            applyChatTheme(fontsOnly);
        }
        chat_msgTextPaintOneEmoji.setTextSize((float) AndroidUtilities.dp(28.0f));
        chat_msgTextPaintTwoEmoji.setTextSize((float) AndroidUtilities.dp(24.0f));
        chat_msgTextPaintThreeEmoji.setTextSize((float) AndroidUtilities.dp(20.0f));
        chat_msgTextPaint.setTextSize((float) AndroidUtilities.dp((float) SharedConfig.fontSize));
        chat_msgGameTextPaint.setTextSize((float) AndroidUtilities.dp(14.0f));
        chat_msgBotButtonPaint.setTextSize((float) AndroidUtilities.dp(15.0f));
        if (!fontsOnly && chat_botProgressPaint != null) {
            chat_botProgressPaint.setStrokeWidth((float) AndroidUtilities.dp(2.0f));
            chat_infoPaint.setTextSize((float) AndroidUtilities.dp(12.0f));
            chat_docNamePaint.setTextSize((float) AndroidUtilities.dp(15.0f));
            chat_locationTitlePaint.setTextSize((float) AndroidUtilities.dp(15.0f));
            chat_locationAddressPaint.setTextSize((float) AndroidUtilities.dp(13.0f));
            chat_audioTimePaint.setTextSize((float) AndroidUtilities.dp(12.0f));
            chat_livePaint.setTextSize((float) AndroidUtilities.dp(12.0f));
            chat_audioTitlePaint.setTextSize((float) AndroidUtilities.dp(16.0f));
            chat_audioPerformerPaint.setTextSize((float) AndroidUtilities.dp(15.0f));
            chat_botButtonPaint.setTextSize((float) AndroidUtilities.dp(15.0f));
            chat_contactNamePaint.setTextSize((float) AndroidUtilities.dp(15.0f));
            chat_contactPhonePaint.setTextSize((float) AndroidUtilities.dp(13.0f));
            chat_durationPaint.setTextSize((float) AndroidUtilities.dp(12.0f));
            chat_timePaint.setTextSize((float) AndroidUtilities.dp(12.0f));
            chat_adminPaint.setTextSize((float) AndroidUtilities.dp(13.0f));
            chat_namePaint.setTextSize((float) AndroidUtilities.dp(14.0f));
            chat_forwardNamePaint.setTextSize((float) AndroidUtilities.dp(14.0f));
            chat_replyNamePaint.setTextSize((float) AndroidUtilities.dp(14.0f));
            chat_replyTextPaint.setTextSize((float) AndroidUtilities.dp(14.0f));
            chat_gamePaint.setTextSize((float) AndroidUtilities.dp(13.0f));
            chat_shipmentPaint.setTextSize((float) AndroidUtilities.dp(13.0f));
            chat_instantViewPaint.setTextSize((float) AndroidUtilities.dp(13.0f));
            chat_instantViewRectPaint.setStrokeWidth((float) AndroidUtilities.dp(1.0f));
            chat_statusRecordPaint.setStrokeWidth((float) AndroidUtilities.dp(2.0f));
            chat_actionTextPaint.setTextSize((float) AndroidUtilities.dp((float) (Math.max(16, SharedConfig.fontSize) - i)));
            chat_contextResult_titleTextPaint.setTextSize((float) AndroidUtilities.dp(15.0f));
            chat_contextResult_descriptionTextPaint.setTextSize((float) AndroidUtilities.dp(13.0f));
            chat_radialProgressPaint.setStrokeWidth((float) AndroidUtilities.dp(3.0f));
            chat_radialProgress2Paint.setStrokeWidth((float) AndroidUtilities.dp(2.0f));
        }
    }

    public static void applyChatTheme(boolean fontsOnly) {
        if (!(chat_msgTextPaint == null || chat_msgInDrawable == null || fontsOnly)) {
            int a;
            chat_gamePaint.setColor(getColor(key_chat_previewGameText));
            chat_durationPaint.setColor(getColor(key_chat_previewDurationText));
            chat_botButtonPaint.setColor(getColor(key_chat_botButtonText));
            chat_urlPaint.setColor(getColor(key_chat_linkSelectBackground));
            chat_botProgressPaint.setColor(getColor(key_chat_botProgress));
            chat_deleteProgressPaint.setColor(getColor(key_chat_secretTimeText));
            chat_textSearchSelectionPaint.setColor(getColor(key_chat_textSelectBackground));
            chat_msgErrorPaint.setColor(getColor(key_chat_sentError));
            chat_statusPaint.setColor(getColor(key_actionBarDefaultSubtitle));
            chat_statusRecordPaint.setColor(getColor(key_actionBarDefaultSubtitle));
            chat_actionTextPaint.setColor(getColor(key_chat_serviceText));
            chat_actionTextPaint.linkColor = getColor(key_chat_serviceLink);
            chat_contextResult_titleTextPaint.setColor(getColor(key_windowBackgroundWhiteBlackText));
            chat_composeBackgroundPaint.setColor(getColor(key_chat_messagePanelBackground));
            chat_timeBackgroundPaint.setColor(getColor(key_chat_mediaTimeBackground));
            setDrawableColorByKey(chat_msgInDrawable, key_chat_inBubble);
            setDrawableColorByKey(chat_msgInSelectedDrawable, key_chat_inBubbleSelected);
            setDrawableColorByKey(chat_msgInShadowDrawable, key_chat_inBubbleShadow);
            setDrawableColorByKey(chat_msgOutDrawable, key_chat_outBubble);
            setDrawableColorByKey(chat_msgOutSelectedDrawable, key_chat_outBubbleSelected);
            setDrawableColorByKey(chat_msgOutShadowDrawable, key_chat_outBubbleShadow);
            setDrawableColorByKey(chat_msgInMediaDrawable, key_chat_inBubble);
            setDrawableColorByKey(chat_msgInMediaSelectedDrawable, key_chat_inBubbleSelected);
            setDrawableColorByKey(chat_msgInMediaShadowDrawable, key_chat_inBubbleShadow);
            setDrawableColorByKey(chat_msgOutMediaDrawable, key_chat_outBubble);
            setDrawableColorByKey(chat_msgOutMediaSelectedDrawable, key_chat_outBubbleSelected);
            setDrawableColorByKey(chat_msgOutMediaShadowDrawable, key_chat_outBubbleShadow);
            setDrawableColorByKey(chat_msgOutCheckDrawable, key_chat_outSentCheck);
            setDrawableColorByKey(chat_msgOutCheckSelectedDrawable, key_chat_outSentCheckSelected);
            setDrawableColorByKey(chat_msgOutHalfCheckDrawable, key_chat_outSentCheck);
            setDrawableColorByKey(chat_msgOutHalfCheckSelectedDrawable, key_chat_outSentCheckSelected);
            setDrawableColorByKey(chat_msgOutClockDrawable, key_chat_outSentClock);
            setDrawableColorByKey(chat_msgOutSelectedClockDrawable, key_chat_outSentClockSelected);
            setDrawableColorByKey(chat_msgInClockDrawable, key_chat_inSentClock);
            setDrawableColorByKey(chat_msgInSelectedClockDrawable, key_chat_inSentClockSelected);
            setDrawableColorByKey(chat_msgMediaCheckDrawable, key_chat_mediaSentCheck);
            setDrawableColorByKey(chat_msgMediaHalfCheckDrawable, key_chat_mediaSentCheck);
            setDrawableColorByKey(chat_msgMediaClockDrawable, key_chat_mediaSentClock);
            setDrawableColorByKey(chat_msgStickerCheckDrawable, key_chat_serviceText);
            setDrawableColorByKey(chat_msgStickerHalfCheckDrawable, key_chat_serviceText);
            setDrawableColorByKey(chat_msgStickerClockDrawable, key_chat_serviceText);
            setDrawableColorByKey(chat_msgStickerViewsDrawable, key_chat_serviceText);
            setDrawableColorByKey(chat_shareIconDrawable, key_chat_serviceIcon);
            setDrawableColorByKey(chat_replyIconDrawable, key_chat_serviceIcon);
            setDrawableColorByKey(chat_goIconDrawable, key_chat_serviceIcon);
            setDrawableColorByKey(chat_botInlineDrawable, key_chat_serviceIcon);
            setDrawableColorByKey(chat_botLinkDrawalbe, key_chat_serviceIcon);
            setDrawableColorByKey(chat_msgInViewsDrawable, key_chat_inViews);
            setDrawableColorByKey(chat_msgInViewsSelectedDrawable, key_chat_inViewsSelected);
            setDrawableColorByKey(chat_msgOutViewsDrawable, key_chat_outViews);
            setDrawableColorByKey(chat_msgOutViewsSelectedDrawable, key_chat_outViewsSelected);
            setDrawableColorByKey(chat_msgMediaViewsDrawable, key_chat_mediaViews);
            setDrawableColorByKey(chat_msgInMenuDrawable, key_chat_inMenu);
            setDrawableColorByKey(chat_msgInMenuSelectedDrawable, key_chat_inMenuSelected);
            setDrawableColorByKey(chat_msgOutMenuDrawable, key_chat_outMenu);
            setDrawableColorByKey(chat_msgOutMenuSelectedDrawable, key_chat_outMenuSelected);
            setDrawableColorByKey(chat_msgMediaMenuDrawable, key_chat_mediaMenu);
            setDrawableColorByKey(chat_msgOutInstantDrawable, key_chat_outInstant);
            setDrawableColorByKey(chat_msgInInstantDrawable, key_chat_inInstant);
            setDrawableColorByKey(chat_msgErrorDrawable, key_chat_sentErrorIcon);
            setDrawableColorByKey(chat_muteIconDrawable, key_chat_muteIcon);
            setDrawableColorByKey(chat_lockIconDrawable, key_chat_lockIcon);
            setDrawableColorByKey(chat_msgBroadcastDrawable, key_chat_outBroadcast);
            setDrawableColorByKey(chat_msgBroadcastMediaDrawable, key_chat_mediaBroadcast);
            setDrawableColorByKey(chat_inlineResultFile, key_chat_inlineResultIcon);
            setDrawableColorByKey(chat_inlineResultAudio, key_chat_inlineResultIcon);
            setDrawableColorByKey(chat_inlineResultLocation, key_chat_inlineResultIcon);
            setDrawableColorByKey(chat_msgInCallDrawable, key_chat_inInstant);
            setDrawableColorByKey(chat_msgInCallSelectedDrawable, key_chat_inInstantSelected);
            setDrawableColorByKey(chat_msgOutCallDrawable, key_chat_outInstant);
            setDrawableColorByKey(chat_msgOutCallSelectedDrawable, key_chat_outInstantSelected);
            setDrawableColorByKey(chat_msgCallUpRedDrawable, key_calls_callReceivedRedIcon);
            setDrawableColorByKey(chat_msgCallUpGreenDrawable, key_calls_callReceivedGreenIcon);
            setDrawableColorByKey(chat_msgCallDownRedDrawable, key_calls_callReceivedRedIcon);
            setDrawableColorByKey(chat_msgCallDownGreenDrawable, key_calls_callReceivedGreenIcon);
            for (a = 0; a < 2; a++) {
                setCombinedDrawableColor(chat_fileMiniStatesDrawable[a][0], getColor(key_chat_outLoader), false);
                setCombinedDrawableColor(chat_fileMiniStatesDrawable[a][0], getColor(key_chat_outBubble), true);
                setCombinedDrawableColor(chat_fileMiniStatesDrawable[a][1], getColor(key_chat_outLoaderSelected), false);
                setCombinedDrawableColor(chat_fileMiniStatesDrawable[a][1], getColor(key_chat_outBubbleSelected), true);
                setCombinedDrawableColor(chat_fileMiniStatesDrawable[2 + a][0], getColor(key_chat_inLoader), false);
                setCombinedDrawableColor(chat_fileMiniStatesDrawable[2 + a][0], getColor(key_chat_inBubble), true);
                setCombinedDrawableColor(chat_fileMiniStatesDrawable[2 + a][1], getColor(key_chat_inLoaderSelected), false);
                setCombinedDrawableColor(chat_fileMiniStatesDrawable[2 + a][1], getColor(key_chat_inBubbleSelected), true);
                setCombinedDrawableColor(chat_fileMiniStatesDrawable[4 + a][0], getColor(key_chat_mediaLoaderPhoto), false);
                setCombinedDrawableColor(chat_fileMiniStatesDrawable[4 + a][0], getColor(key_chat_mediaLoaderPhotoIcon), true);
                setCombinedDrawableColor(chat_fileMiniStatesDrawable[4 + a][1], getColor(key_chat_mediaLoaderPhotoSelected), false);
                setCombinedDrawableColor(chat_fileMiniStatesDrawable[4 + a][1], getColor(key_chat_mediaLoaderPhotoIconSelected), true);
            }
            for (a = 0; a < 5; a++) {
                setCombinedDrawableColor(chat_fileStatesDrawable[a][0], getColor(key_chat_outLoader), false);
                setCombinedDrawableColor(chat_fileStatesDrawable[a][0], getColor(key_chat_outBubble), true);
                setCombinedDrawableColor(chat_fileStatesDrawable[a][1], getColor(key_chat_outLoaderSelected), false);
                setCombinedDrawableColor(chat_fileStatesDrawable[a][1], getColor(key_chat_outBubbleSelected), true);
                setCombinedDrawableColor(chat_fileStatesDrawable[5 + a][0], getColor(key_chat_inLoader), false);
                setCombinedDrawableColor(chat_fileStatesDrawable[5 + a][0], getColor(key_chat_inBubble), true);
                setCombinedDrawableColor(chat_fileStatesDrawable[5 + a][1], getColor(key_chat_inLoaderSelected), false);
                setCombinedDrawableColor(chat_fileStatesDrawable[5 + a][1], getColor(key_chat_inBubbleSelected), true);
            }
            for (a = 0; a < 4; a++) {
                setCombinedDrawableColor(chat_photoStatesDrawables[a][0], getColor(key_chat_mediaLoaderPhoto), false);
                setCombinedDrawableColor(chat_photoStatesDrawables[a][0], getColor(key_chat_mediaLoaderPhotoIcon), true);
                setCombinedDrawableColor(chat_photoStatesDrawables[a][1], getColor(key_chat_mediaLoaderPhotoSelected), false);
                setCombinedDrawableColor(chat_photoStatesDrawables[a][1], getColor(key_chat_mediaLoaderPhotoIconSelected), true);
            }
            for (a = 0; a < 2; a++) {
                setCombinedDrawableColor(chat_photoStatesDrawables[7 + a][0], getColor(key_chat_outLoaderPhoto), false);
                setCombinedDrawableColor(chat_photoStatesDrawables[7 + a][0], getColor(key_chat_outLoaderPhotoIcon), true);
                setCombinedDrawableColor(chat_photoStatesDrawables[7 + a][1], getColor(key_chat_outLoaderPhotoSelected), false);
                setCombinedDrawableColor(chat_photoStatesDrawables[7 + a][1], getColor(key_chat_outLoaderPhotoIconSelected), true);
                setCombinedDrawableColor(chat_photoStatesDrawables[10 + a][0], getColor(key_chat_inLoaderPhoto), false);
                setCombinedDrawableColor(chat_photoStatesDrawables[10 + a][0], getColor(key_chat_inLoaderPhotoIcon), true);
                setCombinedDrawableColor(chat_photoStatesDrawables[10 + a][1], getColor(key_chat_inLoaderPhotoSelected), false);
                setCombinedDrawableColor(chat_photoStatesDrawables[10 + a][1], getColor(key_chat_inLoaderPhotoIconSelected), true);
            }
            setDrawableColorByKey(chat_photoStatesDrawables[9][0], key_chat_outFileIcon);
            setDrawableColorByKey(chat_photoStatesDrawables[9][1], key_chat_outFileSelectedIcon);
            setDrawableColorByKey(chat_photoStatesDrawables[12][0], key_chat_inFileIcon);
            setDrawableColorByKey(chat_photoStatesDrawables[12][1], key_chat_inFileSelectedIcon);
            setCombinedDrawableColor(chat_contactDrawable[0], getColor(key_chat_inContactBackground), false);
            setCombinedDrawableColor(chat_contactDrawable[0], getColor(key_chat_inContactIcon), true);
            setCombinedDrawableColor(chat_contactDrawable[1], getColor(key_chat_outContactBackground), false);
            setCombinedDrawableColor(chat_contactDrawable[1], getColor(key_chat_outContactIcon), true);
            setCombinedDrawableColor(chat_locationDrawable[0], getColor(key_chat_inLocationBackground), false);
            setCombinedDrawableColor(chat_locationDrawable[0], getColor(key_chat_inLocationIcon), true);
            setCombinedDrawableColor(chat_locationDrawable[1], getColor(key_chat_outLocationBackground), false);
            setCombinedDrawableColor(chat_locationDrawable[1], getColor(key_chat_outLocationIcon), true);
            setDrawableColorByKey(chat_composeShadowDrawable, key_chat_messagePanelShadow);
            applyChatServiceMessageColor();
        }
    }

    public static void applyChatServiceMessageColor() {
        if (chat_actionBackgroundPaint != null) {
            Integer serviceColor = (Integer) currentColors.get(key_chat_serviceBackground);
            Integer servicePressedColor = (Integer) currentColors.get(key_chat_serviceBackgroundSelected);
            if (serviceColor == null) {
                serviceColor = Integer.valueOf(serviceMessageColor);
            }
            if (servicePressedColor == null) {
                servicePressedColor = Integer.valueOf(serviceSelectedMessageColor);
            }
            if (currentColor != serviceColor.intValue()) {
                chat_actionBackgroundPaint.setColor(serviceColor.intValue());
                colorFilter = new PorterDuffColorFilter(serviceColor.intValue(), Mode.MULTIPLY);
                currentColor = serviceColor.intValue();
                int a = 0;
                if (chat_cornerOuter[0] != null) {
                    while (true) {
                        int a2 = a;
                        if (a2 >= 4) {
                            break;
                        }
                        chat_cornerOuter[a2].setColorFilter(colorFilter);
                        chat_cornerInner[a2].setColorFilter(colorFilter);
                        a = a2 + 1;
                    }
                }
            }
            if (currentSelectedColor != servicePressedColor.intValue()) {
                currentSelectedColor = servicePressedColor.intValue();
                colorPressedFilter = new PorterDuffColorFilter(servicePressedColor.intValue(), Mode.MULTIPLY);
            }
        }
    }

    public static void createProfileResources(Context context) {
        if (profile_verifiedDrawable == null) {
            profile_aboutTextPaint = new TextPaint(1);
            Resources resources = context.getResources();
            profile_verifiedDrawable = resources.getDrawable(R.drawable.verified_area).mutate();
            profile_verifiedCheckDrawable = resources.getDrawable(R.drawable.verified_check).mutate();
            applyProfileTheme();
        }
        profile_aboutTextPaint.setTextSize((float) AndroidUtilities.dp(16.0f));
    }

    public static void applyProfileTheme() {
        if (profile_verifiedDrawable != null) {
            profile_aboutTextPaint.setColor(getColor(key_windowBackgroundWhiteBlackText));
            profile_aboutTextPaint.linkColor = getColor(key_windowBackgroundWhiteLinkText);
            setDrawableColorByKey(profile_verifiedDrawable, key_profile_verifiedBackground);
            setDrawableColorByKey(profile_verifiedCheckDrawable, key_profile_verifiedCheck);
        }
    }

    public static Drawable getThemedDrawable(Context context, int resId, String key) {
        Drawable drawable = context.getResources().getDrawable(resId).mutate();
        drawable.setColorFilter(new PorterDuffColorFilter(getColor(key), Mode.MULTIPLY));
        return drawable;
    }

    public static int getDefaultColor(String key) {
        Integer value = (Integer) defaultColors.get(key);
        if (value != null) {
            return value.intValue();
        }
        if (key.equals(key_chats_menuTopShadow)) {
            return 0;
        }
        return -65536;
    }

    public static boolean hasThemeKey(String key) {
        return currentColors.containsKey(key);
    }

    public static Integer getColorOrNull(String key) {
        Integer color = (Integer) currentColors.get(key);
        if (color != null) {
            return color;
        }
        if (((String) fallbackKeys.get(key)) != null) {
            color = (Integer) currentColors.get(key);
        }
        if (color == null) {
            return (Integer) defaultColors.get(key);
        }
        return color;
    }

    public static int getColor(String key) {
        return getColor(key, null);
    }

    public static int getColor(String key, boolean[] isDefault) {
        Integer color = (Integer) currentColors.get(key);
        if (color == null) {
            String fallbackKey = (String) fallbackKeys.get(key);
            if (fallbackKey != null) {
                color = (Integer) currentColors.get(fallbackKey);
            }
            if (color == null) {
                if (isDefault != null) {
                    isDefault[0] = true;
                }
                if (key.equals(key_chat_serviceBackground)) {
                    return serviceMessageColor;
                }
                if (key.equals(key_chat_serviceBackgroundSelected)) {
                    return serviceSelectedMessageColor;
                }
                return getDefaultColor(key);
            }
        }
        return color.intValue();
    }

    public static void setColor(String key, int color, boolean useDefault) {
        if (key.equals(key_chat_wallpaper)) {
            color |= ACTION_BAR_VIDEO_EDIT_COLOR;
        }
        if (useDefault) {
            currentColors.remove(key);
        } else {
            currentColors.put(key, Integer.valueOf(color));
        }
        if (!key.equals(key_chat_serviceBackground)) {
            if (!key.equals(key_chat_serviceBackgroundSelected)) {
                if (key.equals(key_chat_wallpaper)) {
                    reloadWallpaper();
                    return;
                }
                return;
            }
        }
        applyChatServiceMessageColor();
    }

    public static void setThemeWallpaper(String themeName, Bitmap bitmap, File path) {
        currentColors.remove(key_chat_wallpaper);
        MessagesController.getGlobalMainSettings().edit().remove("overrideThemeWallpaper").commit();
        if (bitmap != null) {
            themedWallpaper = new BitmapDrawable(bitmap);
            saveCurrentTheme(themeName, false);
            calcBackgroundColor(themedWallpaper, 0);
            applyChatServiceMessageColor();
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didSetNewWallpapper, new Object[0]);
            return;
        }
        themedWallpaper = null;
        wallpaper = null;
        saveCurrentTheme(themeName, false);
        reloadWallpaper();
    }

    public static void setDrawableColor(Drawable drawable, int color) {
        if (drawable != null) {
            if (drawable instanceof ShapeDrawable) {
                ((ShapeDrawable) drawable).getPaint().setColor(color);
            } else {
                drawable.setColorFilter(new PorterDuffColorFilter(color, Mode.MULTIPLY));
            }
        }
    }

    public static void setDrawableColorByKey(Drawable drawable, String key) {
        if (key != null) {
            setDrawableColor(drawable, getColor(key));
        }
    }

    public static boolean hasWallpaperFromTheme() {
        if (!currentColors.containsKey(key_chat_wallpaper)) {
            if (themedWallpaperFileOffset <= 0) {
                return false;
            }
        }
        return true;
    }

    public static boolean isCustomTheme() {
        return isCustomTheme;
    }

    public static int getSelectedColor() {
        return selectedColor;
    }

    public static void reloadWallpaper() {
        wallpaper = null;
        themedWallpaper = null;
        loadWallpaper();
    }

    private static void calcBackgroundColor(Drawable drawable, int save) {
        if (save != 2) {
            int[] result = AndroidUtilities.calcDrawableColor(drawable);
            serviceMessageColor = result[0];
            serviceSelectedMessageColor = result[1];
        }
    }

    public static int getServiceMessageColor() {
        Integer serviceColor = (Integer) currentColors.get(key_chat_serviceBackground);
        return serviceColor == null ? serviceMessageColor : serviceColor.intValue();
    }

    public static void loadWallpaper() {
        if (wallpaper == null) {
            Utilities.searchQueue.postRunnable(new Runnable() {
                public void run() {
                    /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.telegram.ui.ActionBar.Theme.11.run():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: java.lang.NullPointerException
*/
                    /*
                    r0 = this;
                    r0 = org.telegram.ui.ActionBar.Theme.wallpaperSync;
                    monitor-enter(r0);
                    r1 = org.telegram.messenger.MessagesController.getGlobalMainSettings();	 Catch:{ all -> 0x0140 }
                    r2 = "overrideThemeWallpaper";	 Catch:{ all -> 0x0140 }
                    r3 = 0;	 Catch:{ all -> 0x0140 }
                    r2 = r1.getBoolean(r2, r3);	 Catch:{ all -> 0x0140 }
                    r4 = 1;	 Catch:{ all -> 0x0140 }
                    if (r2 != 0) goto L_0x00b6;	 Catch:{ all -> 0x0140 }
                L_0x0013:
                    r5 = org.telegram.ui.ActionBar.Theme.currentColors;	 Catch:{ all -> 0x0140 }
                    r6 = "chat_wallpaper";	 Catch:{ all -> 0x0140 }
                    r5 = r5.get(r6);	 Catch:{ all -> 0x0140 }
                    r5 = (java.lang.Integer) r5;	 Catch:{ all -> 0x0140 }
                    if (r5 == 0) goto L_0x0032;	 Catch:{ all -> 0x0140 }
                L_0x0021:
                    r6 = new android.graphics.drawable.ColorDrawable;	 Catch:{ all -> 0x0140 }
                    r7 = r5.intValue();	 Catch:{ all -> 0x0140 }
                    r6.<init>(r7);	 Catch:{ all -> 0x0140 }
                    org.telegram.ui.ActionBar.Theme.wallpaper = r6;	 Catch:{ all -> 0x0140 }
                    org.telegram.ui.ActionBar.Theme.isCustomTheme = r4;	 Catch:{ all -> 0x0140 }
                    goto L_0x00b6;	 Catch:{ all -> 0x0140 }
                L_0x0032:
                    r6 = org.telegram.ui.ActionBar.Theme.themedWallpaperFileOffset;	 Catch:{ all -> 0x0140 }
                    if (r6 <= 0) goto L_0x00b6;	 Catch:{ all -> 0x0140 }
                L_0x0038:
                    r6 = org.telegram.ui.ActionBar.Theme.currentTheme;	 Catch:{ all -> 0x0140 }
                    r6 = r6.pathToFile;	 Catch:{ all -> 0x0140 }
                    if (r6 != 0) goto L_0x0048;	 Catch:{ all -> 0x0140 }
                L_0x0040:
                    r6 = org.telegram.ui.ActionBar.Theme.currentTheme;	 Catch:{ all -> 0x0140 }
                    r6 = r6.assetName;	 Catch:{ all -> 0x0140 }
                    if (r6 == 0) goto L_0x00b6;
                L_0x0048:
                    r6 = 0;
                    r7 = 0;
                    r8 = org.telegram.ui.ActionBar.Theme.currentTheme;	 Catch:{ Throwable -> 0x0099 }
                    r8 = r8.assetName;	 Catch:{ Throwable -> 0x0099 }
                    if (r8 == 0) goto L_0x005d;	 Catch:{ Throwable -> 0x0099 }
                L_0x0052:
                    r8 = org.telegram.ui.ActionBar.Theme.currentTheme;	 Catch:{ Throwable -> 0x0099 }
                    r8 = r8.assetName;	 Catch:{ Throwable -> 0x0099 }
                    r8 = org.telegram.ui.ActionBar.Theme.getAssetFile(r8);	 Catch:{ Throwable -> 0x0099 }
                    goto L_0x0068;	 Catch:{ Throwable -> 0x0099 }
                L_0x005d:
                    r8 = new java.io.File;	 Catch:{ Throwable -> 0x0099 }
                    r9 = org.telegram.ui.ActionBar.Theme.currentTheme;	 Catch:{ Throwable -> 0x0099 }
                    r9 = r9.pathToFile;	 Catch:{ Throwable -> 0x0099 }
                    r8.<init>(r9);	 Catch:{ Throwable -> 0x0099 }
                L_0x0068:
                    r9 = new java.io.FileInputStream;	 Catch:{ Throwable -> 0x0099 }
                    r9.<init>(r8);	 Catch:{ Throwable -> 0x0099 }
                    r6 = r9;	 Catch:{ Throwable -> 0x0099 }
                    r9 = r6.getChannel();	 Catch:{ Throwable -> 0x0099 }
                    r10 = org.telegram.ui.ActionBar.Theme.themedWallpaperFileOffset;	 Catch:{ Throwable -> 0x0099 }
                    r10 = (long) r10;	 Catch:{ Throwable -> 0x0099 }
                    r9.position(r10);	 Catch:{ Throwable -> 0x0099 }
                    r9 = android.graphics.BitmapFactory.decodeStream(r6);	 Catch:{ Throwable -> 0x0099 }
                    if (r9 == 0) goto L_0x008f;	 Catch:{ Throwable -> 0x0099 }
                L_0x0080:
                    r10 = new android.graphics.drawable.BitmapDrawable;	 Catch:{ Throwable -> 0x0099 }
                    r10.<init>(r9);	 Catch:{ Throwable -> 0x0099 }
                    r10 = org.telegram.ui.ActionBar.Theme.wallpaper = r10;	 Catch:{ Throwable -> 0x0099 }
                    org.telegram.ui.ActionBar.Theme.themedWallpaper = r10;	 Catch:{ Throwable -> 0x0099 }
                    org.telegram.ui.ActionBar.Theme.isCustomTheme = r4;	 Catch:{ Throwable -> 0x0099 }
                L_0x008f:
                    if (r6 == 0) goto L_0x00a8;
                L_0x0091:
                    r6.close();	 Catch:{ Exception -> 0x0095 }
                    goto L_0x00a8;
                L_0x0095:
                    r7 = move-exception;
                    goto L_0x00a4;
                L_0x0097:
                    r3 = move-exception;
                    goto L_0x00a9;
                L_0x0099:
                    r7 = move-exception;
                    org.telegram.messenger.FileLog.e(r7);	 Catch:{ all -> 0x0097 }
                    if (r6 == 0) goto L_0x00a8;
                    r6.close();	 Catch:{ Exception -> 0x00a3 }
                    goto L_0x00a8;
                L_0x00a3:
                    r7 = move-exception;
                L_0x00a4:
                    org.telegram.messenger.FileLog.e(r7);	 Catch:{ all -> 0x0140 }
                    goto L_0x00b6;
                L_0x00a8:
                    goto L_0x00b6;
                    if (r6 == 0) goto L_0x00b5;
                    r6.close();	 Catch:{ Exception -> 0x00b0 }
                    goto L_0x00b5;
                L_0x00b0:
                    r4 = move-exception;
                    org.telegram.messenger.FileLog.e(r4);	 Catch:{ all -> 0x0140 }
                    throw r3;	 Catch:{ all -> 0x0140 }
                L_0x00b6:
                    r5 = org.telegram.ui.ActionBar.Theme.wallpaper;	 Catch:{ all -> 0x0140 }
                    if (r5 != 0) goto L_0x012f;
                    r5 = r3;
                    r6 = org.telegram.messenger.MessagesController.getGlobalMainSettings();	 Catch:{ Throwable -> 0x011b }
                    r1 = r6;	 Catch:{ Throwable -> 0x011b }
                    r6 = "selectedBackground";	 Catch:{ Throwable -> 0x011b }
                    r7 = 1000001; // 0xf4241 float:1.4013E-39 double:4.94066E-318;	 Catch:{ Throwable -> 0x011b }
                    r6 = r1.getInt(r6, r7);	 Catch:{ Throwable -> 0x011b }
                    r8 = "selectedColor";	 Catch:{ Throwable -> 0x011b }
                    r8 = r1.getInt(r8, r3);	 Catch:{ Throwable -> 0x011b }
                    r5 = r8;	 Catch:{ Throwable -> 0x011b }
                    if (r5 != 0) goto L_0x011a;	 Catch:{ Throwable -> 0x011b }
                    r8 = 2131165226; // 0x7f07002a float:1.7944663E38 double:1.052935524E-314;	 Catch:{ Throwable -> 0x011b }
                    if (r6 != r7) goto L_0x00ea;	 Catch:{ Throwable -> 0x011b }
                    r7 = org.telegram.messenger.ApplicationLoader.applicationContext;	 Catch:{ Throwable -> 0x011b }
                    r7 = r7.getResources();	 Catch:{ Throwable -> 0x011b }
                    r7 = r7.getDrawable(r8);	 Catch:{ Throwable -> 0x011b }
                    org.telegram.ui.ActionBar.Theme.wallpaper = r7;	 Catch:{ Throwable -> 0x011b }
                    org.telegram.ui.ActionBar.Theme.isCustomTheme = r3;	 Catch:{ Throwable -> 0x011b }
                    goto L_0x011a;	 Catch:{ Throwable -> 0x011b }
                    r7 = new java.io.File;	 Catch:{ Throwable -> 0x011b }
                    r9 = org.telegram.messenger.ApplicationLoader.getFilesDirFixed();	 Catch:{ Throwable -> 0x011b }
                    r10 = "wallpaper.jpg";	 Catch:{ Throwable -> 0x011b }
                    r7.<init>(r9, r10);	 Catch:{ Throwable -> 0x011b }
                    r9 = r7.exists();	 Catch:{ Throwable -> 0x011b }
                    if (r9 == 0) goto L_0x010a;	 Catch:{ Throwable -> 0x011b }
                    r3 = r7.getAbsolutePath();	 Catch:{ Throwable -> 0x011b }
                    r3 = android.graphics.drawable.Drawable.createFromPath(r3);	 Catch:{ Throwable -> 0x011b }
                    org.telegram.ui.ActionBar.Theme.wallpaper = r3;	 Catch:{ Throwable -> 0x011b }
                    org.telegram.ui.ActionBar.Theme.isCustomTheme = r4;	 Catch:{ Throwable -> 0x011b }
                    goto L_0x011a;	 Catch:{ Throwable -> 0x011b }
                    r9 = org.telegram.messenger.ApplicationLoader.applicationContext;	 Catch:{ Throwable -> 0x011b }
                    r9 = r9.getResources();	 Catch:{ Throwable -> 0x011b }
                    r8 = r9.getDrawable(r8);	 Catch:{ Throwable -> 0x011b }
                    org.telegram.ui.ActionBar.Theme.wallpaper = r8;	 Catch:{ Throwable -> 0x011b }
                    org.telegram.ui.ActionBar.Theme.isCustomTheme = r3;	 Catch:{ Throwable -> 0x011b }
                    goto L_0x011c;
                L_0x011b:
                    r3 = move-exception;
                    r3 = org.telegram.ui.ActionBar.Theme.wallpaper;	 Catch:{ all -> 0x0140 }
                    if (r3 != 0) goto L_0x012f;	 Catch:{ all -> 0x0140 }
                    if (r5 != 0) goto L_0x0127;	 Catch:{ all -> 0x0140 }
                    r5 = -2693905; // 0xffffffffffd6e4ef float:NaN double:NaN;	 Catch:{ all -> 0x0140 }
                    r3 = new android.graphics.drawable.ColorDrawable;	 Catch:{ all -> 0x0140 }
                    r3.<init>(r5);	 Catch:{ all -> 0x0140 }
                    org.telegram.ui.ActionBar.Theme.wallpaper = r3;	 Catch:{ all -> 0x0140 }
                    r3 = org.telegram.ui.ActionBar.Theme.wallpaper;	 Catch:{ all -> 0x0140 }
                    org.telegram.ui.ActionBar.Theme.calcBackgroundColor(r3, r4);	 Catch:{ all -> 0x0140 }
                    r3 = new org.telegram.ui.ActionBar.Theme$11$1;	 Catch:{ all -> 0x0140 }
                    r3.<init>();	 Catch:{ all -> 0x0140 }
                    org.telegram.messenger.AndroidUtilities.runOnUIThread(r3);	 Catch:{ all -> 0x0140 }
                    monitor-exit(r0);	 Catch:{ all -> 0x0140 }
                    return;	 Catch:{ all -> 0x0140 }
                L_0x0140:
                    r1 = move-exception;	 Catch:{ all -> 0x0140 }
                    monitor-exit(r0);	 Catch:{ all -> 0x0140 }
                    throw r1;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ActionBar.Theme.11.run():void");
                }
            });
        }
    }

    public static Drawable getCachedWallpaper() {
        synchronized (wallpaperSync) {
            if (themedWallpaper != null) {
                Drawable drawable = themedWallpaper;
                return drawable;
            }
            drawable = wallpaper;
            return drawable;
        }
    }
}
