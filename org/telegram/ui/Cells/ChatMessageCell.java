package org.telegram.ui.Cells;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build.VERSION;
import android.text.Layout.Alignment;
import android.text.Layout.Directions;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.StateSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewStructure;
import java.util.ArrayList;
import java.util.HashMap;
import net.hockeyapp.android.UpdateFragment;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DownloadController;
import org.telegram.messenger.DownloadController.FileDownloadProgressListener;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.ImageReceiver.ImageReceiverDelegate;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessageObject.GroupedMessagePosition;
import org.telegram.messenger.MessageObject.GroupedMessages;
import org.telegram.messenger.MessageObject.TextLayoutBlock;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.WebFile;
import org.telegram.messenger.beta.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.DocumentAttribute;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.KeyboardButton;
import org.telegram.tgnet.TLRPC.MessageFwdHeader;
import org.telegram.tgnet.TLRPC.PhoneCallDiscardReason;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_documentAttributeAudio;
import org.telegram.tgnet.TLRPC.TL_documentAttributeVideo;
import org.telegram.tgnet.TLRPC.TL_fileLocationUnavailable;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonBuy;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonCallback;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonGame;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonRequestGeoLocation;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonSwitchInline;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonUrl;
import org.telegram.tgnet.TLRPC.TL_messageMediaContact;
import org.telegram.tgnet.TLRPC.TL_messageMediaEmpty;
import org.telegram.tgnet.TLRPC.TL_messageMediaGame;
import org.telegram.tgnet.TLRPC.TL_messageMediaGeo;
import org.telegram.tgnet.TLRPC.TL_messageMediaGeoLive;
import org.telegram.tgnet.TLRPC.TL_messageMediaInvoice;
import org.telegram.tgnet.TLRPC.TL_messageMediaWebPage;
import org.telegram.tgnet.TLRPC.TL_phoneCallDiscardReasonBusy;
import org.telegram.tgnet.TLRPC.TL_phoneCallDiscardReasonMissed;
import org.telegram.tgnet.TLRPC.TL_photoSizeEmpty;
import org.telegram.tgnet.TLRPC.TL_webPage;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.WebPage;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.LinkPath;
import org.telegram.ui.Components.RadialProgress;
import org.telegram.ui.Components.RoundVideoPlayingDrawable;
import org.telegram.ui.Components.SeekBar;
import org.telegram.ui.Components.SeekBar.SeekBarDelegate;
import org.telegram.ui.Components.SeekBarWaveform;
import org.telegram.ui.Components.StaticLayoutEx;
import org.telegram.ui.Components.TypefaceSpan;
import org.telegram.ui.Components.URLSpanBotCommand;
import org.telegram.ui.Components.URLSpanMono;
import org.telegram.ui.Components.URLSpanNoUnderline;
import org.telegram.ui.PhotoViewer;
import org.telegram.ui.SecretMediaViewer;

public class ChatMessageCell extends BaseCell implements FileDownloadProgressListener, ImageReceiverDelegate, SeekBarDelegate {
    private static final int DOCUMENT_ATTACH_TYPE_AUDIO = 3;
    private static final int DOCUMENT_ATTACH_TYPE_DOCUMENT = 1;
    private static final int DOCUMENT_ATTACH_TYPE_GIF = 2;
    private static final int DOCUMENT_ATTACH_TYPE_MUSIC = 5;
    private static final int DOCUMENT_ATTACH_TYPE_NONE = 0;
    private static final int DOCUMENT_ATTACH_TYPE_ROUND = 7;
    private static final int DOCUMENT_ATTACH_TYPE_STICKER = 6;
    private static final int DOCUMENT_ATTACH_TYPE_VIDEO = 4;
    private int TAG;
    private int addedCaptionHeight;
    private boolean addedForTest;
    private StaticLayout adminLayout;
    private boolean allowAssistant;
    private StaticLayout authorLayout;
    private int authorX;
    private int availableTimeWidth;
    private AvatarDrawable avatarDrawable;
    private ImageReceiver avatarImage = new ImageReceiver();
    private boolean avatarPressed;
    private int backgroundDrawableLeft;
    private int backgroundDrawableRight;
    private int backgroundWidth = 100;
    private ArrayList<BotButton> botButtons = new ArrayList();
    private HashMap<String, BotButton> botButtonsByData = new HashMap();
    private HashMap<String, BotButton> botButtonsByPosition = new HashMap();
    private String botButtonsLayout;
    private int buttonPressed;
    private int buttonState;
    private int buttonX;
    private int buttonY;
    private boolean cancelLoading;
    private int captionHeight;
    private StaticLayout captionLayout;
    private int captionOffsetX;
    private int captionWidth;
    private int captionX;
    private int captionY;
    private AvatarDrawable contactAvatarDrawable;
    private float controlsAlpha = 1.0f;
    private int currentAccount = UserConfig.selectedAccount;
    private Drawable currentBackgroundDrawable;
    private CharSequence currentCaption;
    private Chat currentChat;
    private Chat currentForwardChannel;
    private String currentForwardNameString;
    private User currentForwardUser;
    private int currentMapProvider;
    private MessageObject currentMessageObject;
    private GroupedMessages currentMessagesGroup;
    private String currentNameString;
    private FileLocation currentPhoto;
    private String currentPhotoFilter;
    private String currentPhotoFilterThumb;
    private PhotoSize currentPhotoObject;
    private PhotoSize currentPhotoObjectThumb;
    private GroupedMessagePosition currentPosition;
    private FileLocation currentReplyPhoto;
    private String currentTimeString;
    private String currentUrl;
    private User currentUser;
    private User currentViaBotUser;
    private String currentViewsString;
    private WebFile currentWebFile;
    private ChatMessageCellDelegate delegate;
    private RectF deleteProgressRect = new RectF();
    private StaticLayout descriptionLayout;
    private int descriptionX;
    private int descriptionY;
    private boolean disallowLongPress;
    private StaticLayout docTitleLayout;
    private int docTitleOffsetX;
    private Document documentAttach;
    private int documentAttachType;
    private boolean drawBackground = true;
    private boolean drawForwardedName;
    private boolean drawImageButton;
    private boolean drawInstantView;
    private int drawInstantViewType;
    private boolean drawJoinChannelView;
    private boolean drawJoinGroupView;
    private boolean drawName;
    private boolean drawNameLayout;
    private boolean drawPhotoImage;
    private boolean drawPinnedBottom;
    private boolean drawPinnedTop;
    private boolean drawRadialCheckBackground;
    private boolean drawShareButton;
    private boolean drawTime = true;
    private boolean drwaShareGoIcon;
    private StaticLayout durationLayout;
    private int durationWidth;
    private int firstVisibleBlockNum;
    private boolean forceNotDrawTime;
    private boolean forwardBotPressed;
    private boolean forwardName;
    private float[] forwardNameOffsetX = new float[2];
    private boolean forwardNamePressed;
    private int forwardNameX;
    private int forwardNameY;
    private StaticLayout[] forwardedNameLayout = new StaticLayout[2];
    private int forwardedNameWidth;
    private boolean fullyDraw;
    private boolean gamePreviewPressed;
    private boolean groupPhotoInvisible;
    private boolean hasGamePreview;
    private boolean hasInvoicePreview;
    private boolean hasLinkPreview;
    private int hasMiniProgress;
    private boolean hasNewLineForTime;
    private boolean hasOldCaptionPreview;
    private int highlightProgress;
    private boolean imagePressed;
    private boolean inLayout;
    private StaticLayout infoLayout;
    private int infoWidth;
    private boolean instantButtonPressed;
    private RectF instantButtonRect = new RectF();
    private boolean instantPressed;
    private int instantTextLeftX;
    private int instantTextX;
    private StaticLayout instantViewLayout;
    private Drawable instantViewSelectorDrawable;
    private int instantWidth;
    private Runnable invalidateRunnable = new Runnable() {
        public void run() {
            ChatMessageCell.this.checkLocationExpired();
            if (ChatMessageCell.this.locationExpired) {
                ChatMessageCell.this.invalidate();
                ChatMessageCell.this.scheduledInvalidate = false;
                return;
            }
            ChatMessageCell.this.invalidate(((int) ChatMessageCell.this.rect.left) - 5, ((int) ChatMessageCell.this.rect.top) - 5, ((int) ChatMessageCell.this.rect.right) + 5, ((int) ChatMessageCell.this.rect.bottom) + 5);
            if (ChatMessageCell.this.scheduledInvalidate) {
                AndroidUtilities.runOnUIThread(ChatMessageCell.this.invalidateRunnable, 1000);
            }
        }
    };
    private boolean isAvatarVisible;
    public boolean isChat;
    private boolean isCheckPressed = true;
    private boolean isHighlighted;
    private boolean isHighlightedAnimated;
    private boolean isPressed;
    private boolean isSmallImage;
    private int keyboardHeight;
    private long lastControlsAlphaChangeTime;
    private int lastDeleteDate;
    private int lastHeight;
    private long lastHighlightProgressTime;
    private int lastSendState;
    private int lastTime;
    private int lastViewsCount;
    private int lastVisibleBlockNum;
    private int layoutHeight;
    private int layoutWidth;
    private int linkBlockNum;
    private int linkPreviewHeight;
    private boolean linkPreviewPressed;
    private int linkSelectionBlockNum;
    private boolean locationExpired;
    private ImageReceiver locationImageReceiver;
    private boolean mediaBackground;
    private int mediaOffsetY;
    private boolean mediaWasInvisible;
    private int miniButtonPressed;
    private int miniButtonState;
    private StaticLayout nameLayout;
    private float nameOffsetX;
    private int nameWidth;
    private float nameX;
    private float nameY;
    private int namesOffset;
    private boolean needNewVisiblePart;
    private boolean needReplyImage;
    private boolean otherPressed;
    private int otherX;
    private int otherY;
    private StaticLayout performerLayout;
    private int performerX;
    private ImageReceiver photoImage;
    private boolean photoNotSet;
    private StaticLayout photosCountLayout;
    private int photosCountWidth;
    private boolean pinnedBottom;
    private boolean pinnedTop;
    private int pressedBotButton;
    private CharacterStyle pressedLink;
    private int pressedLinkType;
    private int[] pressedState = new int[]{16842910, 16842919};
    private RadialProgress radialProgress;
    private RectF rect = new RectF();
    private ImageReceiver replyImageReceiver;
    private StaticLayout replyNameLayout;
    private float replyNameOffset;
    private int replyNameWidth;
    private boolean replyPressed;
    private int replyStartX;
    private int replyStartY;
    private StaticLayout replyTextLayout;
    private float replyTextOffset;
    private int replyTextWidth;
    private RoundVideoPlayingDrawable roundVideoPlayingDrawable;
    private boolean scheduledInvalidate;
    private Rect scrollRect = new Rect();
    private SeekBar seekBar;
    private SeekBarWaveform seekBarWaveform;
    private int seekBarX;
    private int seekBarY;
    private boolean sharePressed;
    private int shareStartX;
    private int shareStartY;
    private StaticLayout siteNameLayout;
    private boolean siteNameRtl;
    private int siteNameWidth;
    private StaticLayout songLayout;
    private int songX;
    private int substractBackgroundHeight;
    private int textX;
    private int textY;
    private float timeAlpha = 1.0f;
    private int timeAudioX;
    private StaticLayout timeLayout;
    private int timeTextWidth;
    private boolean timeWasInvisible;
    private int timeWidth;
    private int timeWidthAudio;
    private int timeX;
    private StaticLayout titleLayout;
    private int titleX;
    private long totalChangeTime;
    private int totalHeight;
    private int totalVisibleBlocksCount;
    private int unmovedTextX;
    private ArrayList<LinkPath> urlPath = new ArrayList();
    private ArrayList<LinkPath> urlPathCache = new ArrayList();
    private ArrayList<LinkPath> urlPathSelection = new ArrayList();
    private boolean useSeekBarWaweform;
    private int viaNameWidth;
    private int viaWidth;
    private StaticLayout videoInfoLayout;
    private StaticLayout viewsLayout;
    private int viewsTextWidth;
    private boolean wasLayout;
    private int widthBeforeNewTimeLine;
    private int widthForButtons;

    private class BotButton {
        private int angle;
        private KeyboardButton button;
        private int height;
        private long lastUpdateTime;
        private float progressAlpha;
        private StaticLayout title;
        private int width;
        private int x;
        private int y;

        private BotButton() {
        }
    }

    public interface ChatMessageCellDelegate {
        boolean canPerformActions();

        void didLongPressed(ChatMessageCell chatMessageCell);

        void didPressedBotButton(ChatMessageCell chatMessageCell, KeyboardButton keyboardButton);

        void didPressedCancelSendButton(ChatMessageCell chatMessageCell);

        void didPressedChannelAvatar(ChatMessageCell chatMessageCell, Chat chat, int i);

        void didPressedImage(ChatMessageCell chatMessageCell);

        void didPressedInstantButton(ChatMessageCell chatMessageCell, int i);

        void didPressedOther(ChatMessageCell chatMessageCell);

        void didPressedReplyMessage(ChatMessageCell chatMessageCell, int i);

        void didPressedShare(ChatMessageCell chatMessageCell);

        void didPressedUrl(MessageObject messageObject, CharacterStyle characterStyle, boolean z);

        void didPressedUserAvatar(ChatMessageCell chatMessageCell, User user);

        void didPressedViaBot(ChatMessageCell chatMessageCell, String str);

        boolean isChatAdminCell(int i);

        void needOpenWebView(String str, String str2, String str3, String str4, int i, int i2);

        boolean needPlayMessage(MessageObject messageObject);
    }

    public ChatMessageCell(Context context) {
        super(context);
        this.avatarImage.setRoundRadius(AndroidUtilities.dp(21.0f));
        this.avatarDrawable = new AvatarDrawable();
        this.replyImageReceiver = new ImageReceiver(this);
        this.locationImageReceiver = new ImageReceiver(this);
        this.locationImageReceiver.setRoundRadius(AndroidUtilities.dp(26.1f));
        this.TAG = DownloadController.getInstance(this.currentAccount).generateObserverTag();
        this.contactAvatarDrawable = new AvatarDrawable();
        this.photoImage = new ImageReceiver(this);
        this.photoImage.setDelegate(this);
        this.radialProgress = new RadialProgress(this);
        this.seekBar = new SeekBar(context);
        this.seekBar.setDelegate(this);
        this.seekBarWaveform = new SeekBarWaveform(context);
        this.seekBarWaveform.setDelegate(this);
        this.seekBarWaveform.setParentView(this);
        this.roundVideoPlayingDrawable = new RoundVideoPlayingDrawable(this);
    }

    private void resetPressedLink(int type) {
        if (this.pressedLink == null) {
            return;
        }
        if (this.pressedLinkType == type || type == -1) {
            resetUrlPaths(false);
            this.pressedLink = null;
            this.pressedLinkType = -1;
            invalidate();
        }
    }

    private void resetUrlPaths(boolean text) {
        if (text) {
            if (!this.urlPathSelection.isEmpty()) {
                this.urlPathCache.addAll(this.urlPathSelection);
                this.urlPathSelection.clear();
            }
        } else if (!this.urlPath.isEmpty()) {
            this.urlPathCache.addAll(this.urlPath);
            this.urlPath.clear();
        }
    }

    private LinkPath obtainNewUrlPath(boolean text) {
        LinkPath linkPath;
        if (this.urlPathCache.isEmpty()) {
            linkPath = new LinkPath();
        } else {
            linkPath = (LinkPath) this.urlPathCache.get(0);
            this.urlPathCache.remove(0);
        }
        if (text) {
            this.urlPathSelection.add(linkPath);
        } else {
            this.urlPath.add(linkPath);
        }
        return linkPath;
    }

    private boolean checkTextBlockMotionEvent(MotionEvent event) {
        if (this.currentMessageObject.type != 0 || this.currentMessageObject.textLayoutBlocks == null || this.currentMessageObject.textLayoutBlocks.isEmpty() || !(this.currentMessageObject.messageText instanceof Spannable)) {
            return false;
        }
        if (event.getAction() == 0 || (event.getAction() == 1 && this.pressedLinkType == 1)) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            if (x < this.textX || y < this.textY || x > this.textX + this.currentMessageObject.textWidth || y > this.textY + this.currentMessageObject.textHeight) {
                resetPressedLink(1);
            } else {
                y -= this.textY;
                int blockNum = 0;
                int a = 0;
                while (a < this.currentMessageObject.textLayoutBlocks.size() && ((TextLayoutBlock) this.currentMessageObject.textLayoutBlocks.get(a)).textYOffset <= ((float) y)) {
                    blockNum = a;
                    a++;
                }
                try {
                    TextLayoutBlock block = (TextLayoutBlock) this.currentMessageObject.textLayoutBlocks.get(blockNum);
                    x = (int) (((float) x) - (((float) this.textX) - (block.isRtl() ? this.currentMessageObject.textXOffset : 0.0f)));
                    int line = block.textLayout.getLineForVertical((int) (((float) y) - block.textYOffset));
                    int off = block.textLayout.getOffsetForHorizontal(line, (float) x);
                    float left = block.textLayout.getLineLeft(line);
                    if (left <= ((float) x) && block.textLayout.getLineWidth(line) + left >= ((float) x)) {
                        Spannable buffer = this.currentMessageObject.messageText;
                        CharacterStyle[] link = (CharacterStyle[]) buffer.getSpans(off, off, ClickableSpan.class);
                        boolean isMono = false;
                        if (link == null || link.length == 0) {
                            link = (CharacterStyle[]) buffer.getSpans(off, off, URLSpanMono.class);
                            isMono = true;
                        }
                        boolean ignore = false;
                        if (link.length == 0 || !(link.length == 0 || !(link[0] instanceof URLSpanBotCommand) || URLSpanBotCommand.enabled)) {
                            ignore = true;
                        }
                        if (!ignore) {
                            if (event.getAction() == 0) {
                                this.pressedLink = link[0];
                                this.linkBlockNum = blockNum;
                                this.pressedLinkType = 1;
                                resetUrlPaths(false);
                                try {
                                    TextLayoutBlock nextBlock;
                                    CharacterStyle[] nextLink;
                                    Path path = obtainNewUrlPath(false);
                                    int start = buffer.getSpanStart(this.pressedLink);
                                    int end = buffer.getSpanEnd(this.pressedLink);
                                    path.setCurrentLayout(block.textLayout, start, 0.0f);
                                    block.textLayout.getSelectionPath(start, end, path);
                                    if (end >= block.charactersEnd) {
                                        a = blockNum + 1;
                                        while (a < this.currentMessageObject.textLayoutBlocks.size()) {
                                            nextBlock = (TextLayoutBlock) this.currentMessageObject.textLayoutBlocks.get(a);
                                            if (isMono) {
                                                nextLink = (CharacterStyle[]) buffer.getSpans(nextBlock.charactersOffset, nextBlock.charactersOffset, URLSpanMono.class);
                                            } else {
                                                nextLink = (CharacterStyle[]) buffer.getSpans(nextBlock.charactersOffset, nextBlock.charactersOffset, ClickableSpan.class);
                                            }
                                            if (nextLink != null && nextLink.length != 0 && nextLink[0] == this.pressedLink) {
                                                path = obtainNewUrlPath(false);
                                                path.setCurrentLayout(nextBlock.textLayout, 0, nextBlock.textYOffset - block.textYOffset);
                                                nextBlock.textLayout.getSelectionPath(0, end, path);
                                                if (end < nextBlock.charactersEnd - 1) {
                                                    break;
                                                }
                                                a++;
                                            } else {
                                                break;
                                            }
                                        }
                                    }
                                    if (start <= block.charactersOffset) {
                                        int offsetY = 0;
                                        a = blockNum - 1;
                                        while (a >= 0) {
                                            nextBlock = (TextLayoutBlock) this.currentMessageObject.textLayoutBlocks.get(a);
                                            if (isMono) {
                                                nextLink = (CharacterStyle[]) buffer.getSpans(nextBlock.charactersEnd - 1, nextBlock.charactersEnd - 1, URLSpanMono.class);
                                            } else {
                                                nextLink = (CharacterStyle[]) buffer.getSpans(nextBlock.charactersEnd - 1, nextBlock.charactersEnd - 1, ClickableSpan.class);
                                            }
                                            if (nextLink != null && nextLink.length != 0) {
                                                if (nextLink[0] == this.pressedLink) {
                                                    path = obtainNewUrlPath(false);
                                                    start = buffer.getSpanStart(this.pressedLink);
                                                    offsetY -= nextBlock.height;
                                                    path.setCurrentLayout(nextBlock.textLayout, start, (float) offsetY);
                                                    nextBlock.textLayout.getSelectionPath(start, buffer.getSpanEnd(this.pressedLink), path);
                                                    if (start <= nextBlock.charactersOffset) {
                                                        a--;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } catch (Throwable e) {
                                    FileLog.e(e);
                                }
                                invalidate();
                                return true;
                            }
                            if (link[0] == this.pressedLink) {
                                this.delegate.didPressedUrl(this.currentMessageObject, this.pressedLink, false);
                                resetPressedLink(1);
                                return true;
                            }
                        }
                    }
                } catch (Throwable e2) {
                    FileLog.e(e2);
                }
            }
        }
        return false;
    }

    private boolean checkCaptionMotionEvent(MotionEvent event) {
        if (!(this.currentCaption instanceof Spannable) || this.captionLayout == null) {
            return false;
        }
        if (event.getAction() == 0 || ((this.linkPreviewPressed || this.pressedLink != null) && event.getAction() == 1)) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            if (x < this.captionX || x > this.captionX + this.captionWidth || y < this.captionY || y > this.captionY + this.captionHeight) {
                resetPressedLink(3);
            } else if (event.getAction() == 0) {
                try {
                    x -= this.captionX;
                    int line = this.captionLayout.getLineForVertical(y - this.captionY);
                    int off = this.captionLayout.getOffsetForHorizontal(line, (float) x);
                    float left = this.captionLayout.getLineLeft(line);
                    if (left <= ((float) x) && this.captionLayout.getLineWidth(line) + left >= ((float) x)) {
                        Spannable buffer = this.currentCaption;
                        CharacterStyle[] link = (CharacterStyle[]) buffer.getSpans(off, off, ClickableSpan.class);
                        if (link == null || link.length == 0) {
                            link = (CharacterStyle[]) buffer.getSpans(off, off, URLSpanMono.class);
                        }
                        boolean ignore = false;
                        if (link.length == 0 || !(link.length == 0 || !(link[0] instanceof URLSpanBotCommand) || URLSpanBotCommand.enabled)) {
                            ignore = true;
                        }
                        if (!ignore) {
                            this.pressedLink = link[0];
                            this.pressedLinkType = 3;
                            resetUrlPaths(false);
                            try {
                                LinkPath path = obtainNewUrlPath(false);
                                int start = buffer.getSpanStart(this.pressedLink);
                                path.setCurrentLayout(this.captionLayout, start, 0.0f);
                                this.captionLayout.getSelectionPath(start, buffer.getSpanEnd(this.pressedLink), path);
                            } catch (Throwable e) {
                                FileLog.e(e);
                            }
                            if (!(this.currentMessagesGroup == null || getParent() == null)) {
                                ((ViewGroup) getParent()).invalidate();
                            }
                            invalidate();
                            return true;
                        }
                    }
                } catch (Throwable e2) {
                    FileLog.e(e2);
                }
            } else if (this.pressedLinkType == 3) {
                this.delegate.didPressedUrl(this.currentMessageObject, this.pressedLink, false);
                resetPressedLink(3);
                return true;
            }
        }
        return false;
    }

    private boolean checkGameMotionEvent(MotionEvent event) {
        if (!this.hasGamePreview) {
            return false;
        }
        int x = (int) event.getX();
        int y = (int) event.getY();
        if (event.getAction() == 0) {
            if (this.drawPhotoImage && this.photoImage.isInsideImage((float) x, (float) y)) {
                this.gamePreviewPressed = true;
                return true;
            } else if (this.descriptionLayout != null && y >= this.descriptionY) {
                try {
                    x -= (this.unmovedTextX + AndroidUtilities.dp(10.0f)) + this.descriptionX;
                    int line = this.descriptionLayout.getLineForVertical(y - this.descriptionY);
                    int off = this.descriptionLayout.getOffsetForHorizontal(line, (float) x);
                    float left = this.descriptionLayout.getLineLeft(line);
                    if (left <= ((float) x) && this.descriptionLayout.getLineWidth(line) + left >= ((float) x)) {
                        Spannable buffer = this.currentMessageObject.linkDescription;
                        ClickableSpan[] link = (ClickableSpan[]) buffer.getSpans(off, off, ClickableSpan.class);
                        boolean ignore = false;
                        if (link.length == 0 || !(link.length == 0 || !(link[0] instanceof URLSpanBotCommand) || URLSpanBotCommand.enabled)) {
                            ignore = true;
                        }
                        if (!ignore) {
                            this.pressedLink = link[0];
                            this.linkBlockNum = -10;
                            this.pressedLinkType = 2;
                            resetUrlPaths(false);
                            try {
                                LinkPath path = obtainNewUrlPath(false);
                                int start = buffer.getSpanStart(this.pressedLink);
                                path.setCurrentLayout(this.descriptionLayout, start, 0.0f);
                                this.descriptionLayout.getSelectionPath(start, buffer.getSpanEnd(this.pressedLink), path);
                            } catch (Throwable e) {
                                FileLog.e(e);
                            }
                            invalidate();
                            return true;
                        }
                    }
                } catch (Throwable e2) {
                    FileLog.e(e2);
                }
            }
        } else if (event.getAction() == 1) {
            if (this.pressedLinkType != 2 && !this.gamePreviewPressed) {
                resetPressedLink(2);
            } else if (this.pressedLink != null) {
                if (this.pressedLink instanceof URLSpan) {
                    Browser.openUrl(getContext(), ((URLSpan) this.pressedLink).getURL());
                } else if (this.pressedLink instanceof ClickableSpan) {
                    ((ClickableSpan) this.pressedLink).onClick(this);
                }
                resetPressedLink(2);
            } else {
                this.gamePreviewPressed = false;
                for (int a = 0; a < this.botButtons.size(); a++) {
                    BotButton button = (BotButton) this.botButtons.get(a);
                    if (button.button instanceof TL_keyboardButtonGame) {
                        playSoundEffect(0);
                        this.delegate.didPressedBotButton(this, button.button);
                        invalidate();
                        break;
                    }
                }
                resetPressedLink(2);
                return true;
            }
        }
        return false;
    }

    private boolean checkLinkPreviewMotionEvent(MotionEvent event) {
        if (this.currentMessageObject.type != 0 || !this.hasLinkPreview) {
            return false;
        }
        int x = (int) event.getX();
        int y = (int) event.getY();
        if (x >= this.unmovedTextX && x <= this.unmovedTextX + this.backgroundWidth && y >= this.textY + this.currentMessageObject.textHeight) {
            if (y <= AndroidUtilities.dp((float) ((this.drawInstantView ? 46 : 0) + 8)) + (this.linkPreviewHeight + (this.textY + this.currentMessageObject.textHeight))) {
                WebPage webPage;
                if (event.getAction() == 0) {
                    if (this.descriptionLayout != null && y >= this.descriptionY) {
                        try {
                            int checkX = x - ((this.unmovedTextX + AndroidUtilities.dp(10.0f)) + this.descriptionX);
                            int checkY = y - this.descriptionY;
                            if (checkY <= this.descriptionLayout.getHeight()) {
                                int line = this.descriptionLayout.getLineForVertical(checkY);
                                int off = this.descriptionLayout.getOffsetForHorizontal(line, (float) checkX);
                                float left = this.descriptionLayout.getLineLeft(line);
                                if (left <= ((float) checkX) && this.descriptionLayout.getLineWidth(line) + left >= ((float) checkX)) {
                                    Spannable buffer = this.currentMessageObject.linkDescription;
                                    ClickableSpan[] link = (ClickableSpan[]) buffer.getSpans(off, off, ClickableSpan.class);
                                    boolean ignore = false;
                                    if (link.length == 0 || !(link.length == 0 || !(link[0] instanceof URLSpanBotCommand) || URLSpanBotCommand.enabled)) {
                                        ignore = true;
                                    }
                                    if (!ignore) {
                                        this.pressedLink = link[0];
                                        this.linkBlockNum = -10;
                                        this.pressedLinkType = 2;
                                        resetUrlPaths(false);
                                        try {
                                            Path path = obtainNewUrlPath(false);
                                            int start = buffer.getSpanStart(this.pressedLink);
                                            path.setCurrentLayout(this.descriptionLayout, start, 0.0f);
                                            this.descriptionLayout.getSelectionPath(start, buffer.getSpanEnd(this.pressedLink), path);
                                        } catch (Throwable e) {
                                            FileLog.e(e);
                                        }
                                        invalidate();
                                        return true;
                                    }
                                }
                            }
                        } catch (Throwable e2) {
                            FileLog.e(e2);
                        }
                    }
                    if (this.pressedLink == null) {
                        int side = AndroidUtilities.dp(48.0f);
                        boolean area2 = false;
                        if (this.miniButtonState >= 0) {
                            int offset = AndroidUtilities.dp(27.0f);
                            area2 = x >= this.buttonX + offset && x <= (this.buttonX + offset) + side && y >= this.buttonY + offset && y <= (this.buttonY + offset) + side;
                        }
                        if (area2) {
                            this.miniButtonPressed = 1;
                            invalidate();
                            return true;
                        } else if (this.drawPhotoImage && this.drawImageButton && this.buttonState != -1 && (this.photoImage.isInsideImage((float) x, (float) y) || (x >= this.buttonX && x <= this.buttonX + AndroidUtilities.dp(48.0f) && y >= this.buttonY && y <= this.buttonY + AndroidUtilities.dp(48.0f)))) {
                            this.buttonPressed = 1;
                            return true;
                        } else if (this.drawInstantView) {
                            this.instantPressed = true;
                            if (VERSION.SDK_INT >= 21 && this.instantViewSelectorDrawable != null && this.instantViewSelectorDrawable.getBounds().contains(x, y)) {
                                this.instantViewSelectorDrawable.setState(this.pressedState);
                                this.instantViewSelectorDrawable.setHotspot((float) x, (float) y);
                                this.instantButtonPressed = true;
                            }
                            invalidate();
                            return true;
                        } else if (this.documentAttachType != 1 && this.drawPhotoImage && this.photoImage.isInsideImage((float) x, (float) y)) {
                            this.linkPreviewPressed = true;
                            webPage = this.currentMessageObject.messageOwner.media.webpage;
                            if (this.documentAttachType != 2 || this.buttonState != -1 || !SharedConfig.autoplayGifs || (this.photoImage.getAnimation() != null && TextUtils.isEmpty(webPage.embed_url))) {
                                return true;
                            }
                            this.linkPreviewPressed = false;
                            return false;
                        }
                    }
                } else if (event.getAction() == 1) {
                    if (this.instantPressed) {
                        if (this.delegate != null) {
                            this.delegate.didPressedInstantButton(this, this.drawInstantViewType);
                        }
                        playSoundEffect(0);
                        if (VERSION.SDK_INT >= 21 && this.instantViewSelectorDrawable != null) {
                            this.instantViewSelectorDrawable.setState(StateSet.NOTHING);
                        }
                        this.instantButtonPressed = false;
                        this.instantPressed = false;
                        invalidate();
                    } else if (this.pressedLinkType != 2 && this.buttonPressed == 0 && this.miniButtonPressed == 0 && !this.linkPreviewPressed) {
                        resetPressedLink(2);
                    } else if (this.buttonPressed != 0) {
                        this.buttonPressed = 0;
                        playSoundEffect(0);
                        didPressedButton(false);
                        invalidate();
                    } else if (this.miniButtonPressed != 0) {
                        this.miniButtonPressed = 0;
                        playSoundEffect(0);
                        didPressedMiniButton(false);
                        invalidate();
                    } else if (this.pressedLink != null) {
                        if (this.pressedLink instanceof URLSpan) {
                            Browser.openUrl(getContext(), ((URLSpan) this.pressedLink).getURL());
                        } else if (this.pressedLink instanceof ClickableSpan) {
                            ((ClickableSpan) this.pressedLink).onClick(this);
                        }
                        resetPressedLink(2);
                    } else {
                        if (this.documentAttachType == 7) {
                            if (!MediaController.getInstance().isPlayingMessage(this.currentMessageObject) || MediaController.getInstance().isMessagePaused()) {
                                this.delegate.needPlayMessage(this.currentMessageObject);
                            } else {
                                MediaController.getInstance().pauseMessage(this.currentMessageObject);
                            }
                        } else if (this.documentAttachType != 2 || !this.drawImageButton) {
                            webPage = this.currentMessageObject.messageOwner.media.webpage;
                            if (webPage != null && !TextUtils.isEmpty(webPage.embed_url)) {
                                this.delegate.needOpenWebView(webPage.embed_url, webPage.site_name, webPage.title, webPage.url, webPage.embed_width, webPage.embed_height);
                            } else if (this.buttonState == -1 || this.buttonState == 3) {
                                this.delegate.didPressedImage(this);
                                playSoundEffect(0);
                            } else if (webPage != null) {
                                Browser.openUrl(getContext(), webPage.url);
                            }
                        } else if (this.buttonState == -1) {
                            if (SharedConfig.autoplayGifs) {
                                this.delegate.didPressedImage(this);
                            } else {
                                this.buttonState = 2;
                                this.currentMessageObject.gifState = 1.0f;
                                this.photoImage.setAllowStartAnimation(false);
                                this.photoImage.stopAnimation();
                                this.radialProgress.setBackground(getDrawableForCurrentState(), false, false);
                                invalidate();
                                playSoundEffect(0);
                            }
                        } else if (this.buttonState == 2 || this.buttonState == 0) {
                            didPressedButton(false);
                            playSoundEffect(0);
                        }
                        resetPressedLink(2);
                        return true;
                    }
                } else if (event.getAction() == 2 && this.instantButtonPressed && VERSION.SDK_INT >= 21 && this.instantViewSelectorDrawable != null) {
                    this.instantViewSelectorDrawable.setHotspot((float) x, (float) y);
                }
            }
        }
        return false;
    }

    private boolean checkInstantButtonMotionEvent(MotionEvent event) {
        if (!this.drawInstantView || this.currentMessageObject.type == 0) {
            return false;
        }
        int x = (int) event.getX();
        int y = (int) event.getY();
        if (event.getAction() == 0) {
            if (this.drawInstantView && this.instantButtonRect.contains((float) x, (float) y)) {
                this.instantPressed = true;
                if (VERSION.SDK_INT >= 21 && this.instantViewSelectorDrawable != null && this.instantViewSelectorDrawable.getBounds().contains(x, y)) {
                    this.instantViewSelectorDrawable.setState(this.pressedState);
                    this.instantViewSelectorDrawable.setHotspot((float) x, (float) y);
                    this.instantButtonPressed = true;
                }
                invalidate();
                return true;
            }
        } else if (event.getAction() == 1) {
            if (this.instantPressed) {
                if (this.delegate != null) {
                    this.delegate.didPressedInstantButton(this, this.drawInstantViewType);
                }
                playSoundEffect(0);
                if (VERSION.SDK_INT >= 21 && this.instantViewSelectorDrawable != null) {
                    this.instantViewSelectorDrawable.setState(StateSet.NOTHING);
                }
                this.instantButtonPressed = false;
                this.instantPressed = false;
                invalidate();
            }
        } else if (event.getAction() == 2 && this.instantButtonPressed && VERSION.SDK_INT >= 21 && this.instantViewSelectorDrawable != null) {
            this.instantViewSelectorDrawable.setHotspot((float) x, (float) y);
        }
        return false;
    }

    private boolean checkOtherButtonMotionEvent(MotionEvent event) {
        boolean allow;
        if (this.currentMessageObject.type == 16) {
            allow = true;
        } else {
            allow = false;
        }
        if (!allow) {
            if ((this.documentAttachType != 1 && this.currentMessageObject.type != 12 && this.documentAttachType != 5 && this.documentAttachType != 4 && this.documentAttachType != 2 && this.currentMessageObject.type != 8) || this.hasGamePreview || this.hasInvoicePreview) {
                allow = false;
            } else {
                allow = true;
            }
        }
        if (!allow) {
            return false;
        }
        int x = (int) event.getX();
        int y = (int) event.getY();
        boolean result = false;
        if (event.getAction() == 0) {
            if (this.currentMessageObject.type == 16) {
                if (x >= this.otherX && x <= this.otherX + AndroidUtilities.dp(235.0f) && y >= this.otherY - AndroidUtilities.dp(14.0f) && y <= this.otherY + AndroidUtilities.dp(50.0f)) {
                    this.otherPressed = true;
                    result = true;
                    invalidate();
                }
            } else if (x >= this.otherX - AndroidUtilities.dp(20.0f) && x <= this.otherX + AndroidUtilities.dp(20.0f) && y >= this.otherY - AndroidUtilities.dp(4.0f) && y <= this.otherY + AndroidUtilities.dp(30.0f)) {
                this.otherPressed = true;
                result = true;
                invalidate();
            }
        } else if (event.getAction() == 1 && this.otherPressed) {
            this.otherPressed = false;
            playSoundEffect(0);
            this.delegate.didPressedOther(this);
            invalidate();
            result = true;
        }
        return result;
    }

    private boolean checkPhotoImageMotionEvent(MotionEvent event) {
        if (!this.drawPhotoImage && this.documentAttachType != 1) {
            return false;
        }
        int x = (int) event.getX();
        int y = (int) event.getY();
        boolean result = false;
        if (event.getAction() == 0) {
            boolean area2 = false;
            int side = AndroidUtilities.dp(48.0f);
            if (this.miniButtonState >= 0) {
                int offset = AndroidUtilities.dp(27.0f);
                if (x < this.buttonX + offset || x > (this.buttonX + offset) + side || y < this.buttonY + offset || y > (this.buttonY + offset) + side) {
                    area2 = false;
                } else {
                    area2 = true;
                }
            }
            if (area2) {
                this.miniButtonPressed = 1;
                invalidate();
                result = true;
            } else if (this.buttonState != -1 && x >= this.buttonX && x <= this.buttonX + side && y >= this.buttonY && y <= this.buttonY + side) {
                this.buttonPressed = 1;
                invalidate();
                result = true;
            } else if (this.documentAttachType == 1) {
                if (x >= this.photoImage.getImageX() && x <= (this.photoImage.getImageX() + this.backgroundWidth) - AndroidUtilities.dp(50.0f) && y >= this.photoImage.getImageY() && y <= this.photoImage.getImageY() + this.photoImage.getImageHeight()) {
                    this.imagePressed = true;
                    result = true;
                }
            } else if (!(this.currentMessageObject.type == 13 && this.currentMessageObject.getInputStickerSet() == null)) {
                if (x >= this.photoImage.getImageX() && x <= this.photoImage.getImageX() + this.backgroundWidth && y >= this.photoImage.getImageY() && y <= this.photoImage.getImageY() + this.photoImage.getImageHeight()) {
                    this.imagePressed = true;
                    result = true;
                }
                if (this.currentMessageObject.type == 12 && MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.currentMessageObject.messageOwner.media.user_id)) == null) {
                    this.imagePressed = false;
                    result = false;
                }
            }
            if (this.imagePressed) {
                if (this.currentMessageObject.isSendError()) {
                    this.imagePressed = false;
                    result = false;
                } else if (this.currentMessageObject.type == 8 && this.buttonState == -1 && SharedConfig.autoplayGifs && this.photoImage.getAnimation() == null) {
                    this.imagePressed = false;
                    result = false;
                } else if (this.currentMessageObject.type == 5 && this.buttonState != -1) {
                    this.imagePressed = false;
                    result = false;
                }
            }
        } else if (event.getAction() == 1) {
            if (this.buttonPressed == 1) {
                this.buttonPressed = 0;
                playSoundEffect(0);
                didPressedButton(false);
                updateRadialProgressBackground();
                invalidate();
            } else if (this.miniButtonPressed == 1) {
                this.miniButtonPressed = 0;
                playSoundEffect(0);
                didPressedMiniButton(false);
                invalidate();
            } else if (this.imagePressed) {
                this.imagePressed = false;
                if (this.buttonState == -1 || this.buttonState == 2 || this.buttonState == 3) {
                    playSoundEffect(0);
                    didClickedImage();
                } else if (this.buttonState == 0 && this.documentAttachType == 1) {
                    playSoundEffect(0);
                    didPressedButton(false);
                }
                invalidate();
            }
        }
        return result;
    }

    private boolean checkAudioMotionEvent(MotionEvent event) {
        if (this.documentAttachType != 3 && this.documentAttachType != 5) {
            return false;
        }
        boolean result;
        int x = (int) event.getX();
        int y = (int) event.getY();
        if (this.useSeekBarWaweform) {
            result = this.seekBarWaveform.onTouch(event.getAction(), (event.getX() - ((float) this.seekBarX)) - ((float) AndroidUtilities.dp(13.0f)), event.getY() - ((float) this.seekBarY));
        } else {
            result = this.seekBar.onTouch(event.getAction(), event.getX() - ((float) this.seekBarX), event.getY() - ((float) this.seekBarY));
        }
        if (result) {
            if (!this.useSeekBarWaweform && event.getAction() == 0) {
                getParent().requestDisallowInterceptTouchEvent(true);
            } else if (this.useSeekBarWaweform && !this.seekBarWaveform.isStartDraging() && event.getAction() == 1) {
                didPressedButton(true);
            }
            this.disallowLongPress = true;
            invalidate();
            return result;
        }
        int side = AndroidUtilities.dp(36.0f);
        boolean area = false;
        boolean area2 = false;
        if (this.miniButtonState >= 0) {
            int offset = AndroidUtilities.dp(27.0f);
            area2 = x >= this.buttonX + offset && x <= (this.buttonX + offset) + side && y >= this.buttonY + offset && y <= (this.buttonY + offset) + side;
        }
        if (!area2) {
            if (this.buttonState == 0 || this.buttonState == 1 || this.buttonState == 2) {
                if (x >= this.buttonX - AndroidUtilities.dp(12.0f) && x <= (this.buttonX - AndroidUtilities.dp(12.0f)) + this.backgroundWidth) {
                    if (y >= (this.drawInstantView ? this.buttonY : this.namesOffset + this.mediaOffsetY)) {
                        if (y <= (this.drawInstantView ? this.buttonY + side : (this.namesOffset + this.mediaOffsetY) + AndroidUtilities.dp(82.0f))) {
                            area = true;
                        }
                    }
                }
                area = false;
            } else {
                area = x >= this.buttonX && x <= this.buttonX + side && y >= this.buttonY && y <= this.buttonY + side;
            }
        }
        if (event.getAction() == 0) {
            if (!area && !area2) {
                return result;
            }
            if (area) {
                this.buttonPressed = 1;
            } else {
                this.miniButtonPressed = 1;
            }
            invalidate();
            updateRadialProgressBackground();
            return true;
        } else if (this.buttonPressed != 0) {
            if (event.getAction() == 1) {
                this.buttonPressed = 0;
                playSoundEffect(0);
                didPressedButton(true);
                invalidate();
            } else if (event.getAction() == 3) {
                this.buttonPressed = 0;
                invalidate();
            } else if (event.getAction() == 2 && !area) {
                this.buttonPressed = 0;
                invalidate();
            }
            updateRadialProgressBackground();
            return result;
        } else if (this.miniButtonPressed == 0) {
            return result;
        } else {
            if (event.getAction() == 1) {
                this.miniButtonPressed = 0;
                playSoundEffect(0);
                didPressedMiniButton(true);
                invalidate();
            } else if (event.getAction() == 3) {
                this.miniButtonPressed = 0;
                invalidate();
            } else if (event.getAction() == 2 && !area2) {
                this.miniButtonPressed = 0;
                invalidate();
            }
            updateRadialProgressBackground();
            return result;
        }
    }

    private boolean checkBotButtonMotionEvent(MotionEvent event) {
        if (this.botButtons.isEmpty() || this.currentMessageObject.eventId != 0) {
            return false;
        }
        int x = (int) event.getX();
        int y = (int) event.getY();
        if (event.getAction() == 0) {
            int addX;
            if (this.currentMessageObject.isOutOwner()) {
                addX = (getMeasuredWidth() - this.widthForButtons) - AndroidUtilities.dp(10.0f);
            } else {
                addX = this.backgroundDrawableLeft + AndroidUtilities.dp(this.mediaBackground ? 1.0f : 7.0f);
            }
            int a = 0;
            while (a < this.botButtons.size()) {
                BotButton button = (BotButton) this.botButtons.get(a);
                int y2 = (button.y + this.layoutHeight) - AndroidUtilities.dp(2.0f);
                if (x < button.x + addX || x > (button.x + addX) + button.width || y < y2 || y > button.height + y2) {
                    a++;
                } else {
                    this.pressedBotButton = a;
                    invalidate();
                    return true;
                }
            }
            return false;
        } else if (event.getAction() != 1 || this.pressedBotButton == -1) {
            return false;
        } else {
            playSoundEffect(0);
            this.delegate.didPressedBotButton(this, ((BotButton) this.botButtons.get(this.pressedBotButton)).button);
            this.pressedBotButton = -1;
            invalidate();
            return false;
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.currentMessageObject == null || !this.delegate.canPerformActions()) {
            return super.onTouchEvent(event);
        }
        this.disallowLongPress = false;
        boolean result = checkTextBlockMotionEvent(event);
        if (!result) {
            result = checkOtherButtonMotionEvent(event);
        }
        if (!result) {
            result = checkCaptionMotionEvent(event);
        }
        if (!result) {
            result = checkAudioMotionEvent(event);
        }
        if (!result) {
            result = checkLinkPreviewMotionEvent(event);
        }
        if (!result) {
            result = checkInstantButtonMotionEvent(event);
        }
        if (!result) {
            result = checkGameMotionEvent(event);
        }
        if (!result) {
            result = checkPhotoImageMotionEvent(event);
        }
        if (!result) {
            result = checkBotButtonMotionEvent(event);
        }
        if (event.getAction() == 3) {
            this.buttonPressed = 0;
            this.miniButtonPressed = 0;
            this.pressedBotButton = -1;
            this.linkPreviewPressed = false;
            this.otherPressed = false;
            this.imagePressed = false;
            this.gamePreviewPressed = false;
            this.instantButtonPressed = false;
            this.instantPressed = false;
            if (VERSION.SDK_INT >= 21 && this.instantViewSelectorDrawable != null) {
                this.instantViewSelectorDrawable.setState(StateSet.NOTHING);
            }
            result = false;
            resetPressedLink(-1);
        }
        if (!this.disallowLongPress && result && event.getAction() == 0) {
            startCheckLongPress();
        }
        if (!(event.getAction() == 0 || event.getAction() == 2)) {
            cancelCheckLongPress();
        }
        if (result) {
            return result;
        }
        float x = event.getX();
        float y = event.getY();
        int replyEnd;
        if (event.getAction() != 0) {
            if (event.getAction() != 2) {
                cancelCheckLongPress();
            }
            if (this.avatarPressed) {
                if (event.getAction() == 1) {
                    this.avatarPressed = false;
                    playSoundEffect(0);
                    if (this.delegate == null) {
                        return result;
                    }
                    if (this.currentUser != null) {
                        this.delegate.didPressedUserAvatar(this, this.currentUser);
                        return result;
                    } else if (this.currentChat == null) {
                        return result;
                    } else {
                        this.delegate.didPressedChannelAvatar(this, this.currentChat, 0);
                        return result;
                    }
                } else if (event.getAction() == 3) {
                    this.avatarPressed = false;
                    return result;
                } else if (event.getAction() != 2 || !this.isAvatarVisible || this.avatarImage.isInsideImage(x, ((float) getTop()) + y)) {
                    return result;
                } else {
                    this.avatarPressed = false;
                    return result;
                }
            } else if (this.forwardNamePressed) {
                if (event.getAction() == 1) {
                    this.forwardNamePressed = false;
                    playSoundEffect(0);
                    if (this.delegate == null) {
                        return result;
                    }
                    if (this.currentForwardChannel != null) {
                        this.delegate.didPressedChannelAvatar(this, this.currentForwardChannel, this.currentMessageObject.messageOwner.fwd_from.channel_post);
                        return result;
                    } else if (this.currentForwardUser == null) {
                        return result;
                    } else {
                        this.delegate.didPressedUserAvatar(this, this.currentForwardUser);
                        return result;
                    }
                } else if (event.getAction() == 3) {
                    this.forwardNamePressed = false;
                    return result;
                } else if (event.getAction() != 2) {
                    return result;
                } else {
                    if (x >= ((float) this.forwardNameX) && x <= ((float) (this.forwardNameX + this.forwardedNameWidth)) && y >= ((float) this.forwardNameY) && y <= ((float) (this.forwardNameY + AndroidUtilities.dp(32.0f)))) {
                        return result;
                    }
                    this.forwardNamePressed = false;
                    return result;
                }
            } else if (this.forwardBotPressed) {
                if (event.getAction() == 1) {
                    this.forwardBotPressed = false;
                    playSoundEffect(0);
                    if (this.delegate == null) {
                        return result;
                    }
                    this.delegate.didPressedViaBot(this, this.currentViaBotUser != null ? this.currentViaBotUser.username : this.currentMessageObject.messageOwner.via_bot_name);
                    return result;
                } else if (event.getAction() == 3) {
                    this.forwardBotPressed = false;
                    return result;
                } else if (event.getAction() != 2) {
                    return result;
                } else {
                    if (!this.drawForwardedName || this.forwardedNameLayout[0] == null) {
                        if (x >= this.nameX + ((float) this.viaNameWidth) && x <= (this.nameX + ((float) this.viaNameWidth)) + ((float) this.viaWidth) && y >= this.nameY - ((float) AndroidUtilities.dp(4.0f)) && y <= this.nameY + ((float) AndroidUtilities.dp(20.0f))) {
                            return result;
                        }
                        this.forwardBotPressed = false;
                        return result;
                    } else if (x >= ((float) this.forwardNameX) && x <= ((float) (this.forwardNameX + this.forwardedNameWidth)) && y >= ((float) this.forwardNameY) && y <= ((float) (this.forwardNameY + AndroidUtilities.dp(32.0f)))) {
                        return result;
                    } else {
                        this.forwardBotPressed = false;
                        return result;
                    }
                }
            } else if (this.replyPressed) {
                if (event.getAction() == 1) {
                    this.replyPressed = false;
                    playSoundEffect(0);
                    if (this.delegate == null) {
                        return result;
                    }
                    this.delegate.didPressedReplyMessage(this, this.currentMessageObject.messageOwner.reply_to_msg_id);
                    return result;
                } else if (event.getAction() == 3) {
                    this.replyPressed = false;
                    return result;
                } else if (event.getAction() != 2) {
                    return result;
                } else {
                    if (this.currentMessageObject.type == 13 || this.currentMessageObject.type == 5) {
                        replyEnd = this.replyStartX + Math.max(this.replyNameWidth, this.replyTextWidth);
                    } else {
                        replyEnd = this.replyStartX + this.backgroundDrawableRight;
                    }
                    if (x >= ((float) this.replyStartX) && x <= ((float) replyEnd) && y >= ((float) this.replyStartY) && y <= ((float) (this.replyStartY + AndroidUtilities.dp(35.0f)))) {
                        return result;
                    }
                    this.replyPressed = false;
                    return result;
                }
            } else if (!this.sharePressed) {
                return result;
            } else {
                if (event.getAction() == 1) {
                    this.sharePressed = false;
                    playSoundEffect(0);
                    if (this.delegate != null) {
                        this.delegate.didPressedShare(this);
                    }
                } else if (event.getAction() == 3) {
                    this.sharePressed = false;
                } else if (event.getAction() == 2 && (x < ((float) this.shareStartX) || x > ((float) (this.shareStartX + AndroidUtilities.dp(40.0f))) || y < ((float) this.shareStartY) || y > ((float) (this.shareStartY + AndroidUtilities.dp(32.0f))))) {
                    this.sharePressed = false;
                }
                invalidate();
                return result;
            }
        } else if (this.delegate != null && !this.delegate.canPerformActions()) {
            return result;
        } else {
            if (this.isAvatarVisible && this.avatarImage.isInsideImage(x, ((float) getTop()) + y)) {
                this.avatarPressed = true;
                result = true;
            } else if (this.drawForwardedName && this.forwardedNameLayout[0] != null && x >= ((float) this.forwardNameX) && x <= ((float) (this.forwardNameX + this.forwardedNameWidth)) && y >= ((float) this.forwardNameY) && y <= ((float) (this.forwardNameY + AndroidUtilities.dp(32.0f)))) {
                if (this.viaWidth == 0 || x < ((float) ((this.forwardNameX + this.viaNameWidth) + AndroidUtilities.dp(4.0f)))) {
                    this.forwardNamePressed = true;
                } else {
                    this.forwardBotPressed = true;
                }
                result = true;
            } else if (this.drawNameLayout && this.nameLayout != null && this.viaWidth != 0 && x >= this.nameX + ((float) this.viaNameWidth) && x <= (this.nameX + ((float) this.viaNameWidth)) + ((float) this.viaWidth) && y >= this.nameY - ((float) AndroidUtilities.dp(4.0f)) && y <= this.nameY + ((float) AndroidUtilities.dp(20.0f))) {
                this.forwardBotPressed = true;
                result = true;
            } else if (this.drawShareButton && x >= ((float) this.shareStartX) && x <= ((float) (this.shareStartX + AndroidUtilities.dp(40.0f))) && y >= ((float) this.shareStartY) && y <= ((float) (this.shareStartY + AndroidUtilities.dp(32.0f)))) {
                this.sharePressed = true;
                result = true;
                invalidate();
            } else if (this.replyNameLayout != null) {
                if (this.currentMessageObject.type == 13 || this.currentMessageObject.type == 5) {
                    replyEnd = this.replyStartX + Math.max(this.replyNameWidth, this.replyTextWidth);
                } else {
                    replyEnd = this.replyStartX + this.backgroundDrawableRight;
                }
                if (x >= ((float) this.replyStartX) && x <= ((float) replyEnd) && y >= ((float) this.replyStartY) && y <= ((float) (this.replyStartY + AndroidUtilities.dp(35.0f)))) {
                    this.replyPressed = true;
                    result = true;
                }
            }
            if (!result) {
                return result;
            }
            startCheckLongPress();
            return result;
        }
    }

    public void updatePlayingMessageProgress() {
        if (this.currentMessageObject != null) {
            int duration;
            int a;
            DocumentAttribute attribute;
            String timeString;
            if (this.currentMessageObject.isRoundVideo()) {
                duration = 0;
                Document document = this.currentMessageObject.getDocument();
                for (a = 0; a < document.attributes.size(); a++) {
                    attribute = (DocumentAttribute) document.attributes.get(a);
                    if (attribute instanceof TL_documentAttributeVideo) {
                        duration = attribute.duration;
                        break;
                    }
                }
                if (MediaController.getInstance().isPlayingMessage(this.currentMessageObject)) {
                    duration = Math.max(0, duration - this.currentMessageObject.audioProgressSec);
                    if (!(this.currentMessageObject.mediaExists || this.currentMessageObject.attachPathExists)) {
                        this.currentMessageObject.mediaExists = true;
                        updateButtonState(true, false);
                    }
                }
                if (this.lastTime != duration) {
                    this.lastTime = duration;
                    timeString = String.format("%02d:%02d", new Object[]{Integer.valueOf(duration / 60), Integer.valueOf(duration % 60)});
                    this.timeWidthAudio = (int) Math.ceil((double) Theme.chat_timePaint.measureText(timeString));
                    this.durationLayout = new StaticLayout(timeString, Theme.chat_timePaint, this.timeWidthAudio, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                    invalidate();
                }
            } else if (this.documentAttach != null) {
                if (this.useSeekBarWaweform) {
                    if (!this.seekBarWaveform.isDragging()) {
                        this.seekBarWaveform.setProgress(this.currentMessageObject.audioProgress);
                    }
                } else if (!this.seekBar.isDragging()) {
                    this.seekBar.setProgress(this.currentMessageObject.audioProgress);
                    this.seekBar.setBufferedProgress(this.currentMessageObject.bufferedProgress);
                }
                duration = 0;
                if (this.documentAttachType == 3) {
                    if (MediaController.getInstance().isPlayingMessage(this.currentMessageObject)) {
                        duration = this.currentMessageObject.audioProgressSec;
                    } else {
                        for (a = 0; a < this.documentAttach.attributes.size(); a++) {
                            attribute = (DocumentAttribute) this.documentAttach.attributes.get(a);
                            if (attribute instanceof TL_documentAttributeAudio) {
                                duration = attribute.duration;
                                break;
                            }
                        }
                    }
                    if (this.lastTime != duration) {
                        this.lastTime = duration;
                        timeString = String.format("%02d:%02d", new Object[]{Integer.valueOf(duration / 60), Integer.valueOf(duration % 60)});
                        this.timeWidthAudio = (int) Math.ceil((double) Theme.chat_audioTimePaint.measureText(timeString));
                        this.durationLayout = new StaticLayout(timeString, Theme.chat_audioTimePaint, this.timeWidthAudio, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                    }
                } else {
                    int currentProgress = 0;
                    duration = this.currentMessageObject.getDuration();
                    if (MediaController.getInstance().isPlayingMessage(this.currentMessageObject)) {
                        currentProgress = this.currentMessageObject.audioProgressSec;
                    }
                    if (this.lastTime != currentProgress) {
                        this.lastTime = currentProgress;
                        if (duration == 0) {
                            timeString = String.format("%d:%02d / -:--", new Object[]{Integer.valueOf(currentProgress / 60), Integer.valueOf(currentProgress % 60)});
                        } else {
                            timeString = String.format("%d:%02d / %d:%02d", new Object[]{Integer.valueOf(currentProgress / 60), Integer.valueOf(currentProgress % 60), Integer.valueOf(duration / 60), Integer.valueOf(duration % 60)});
                        }
                        this.durationLayout = new StaticLayout(timeString, Theme.chat_audioTimePaint, (int) Math.ceil((double) Theme.chat_audioTimePaint.measureText(timeString)), Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                    }
                }
                invalidate();
            }
        }
    }

    public void downloadAudioIfNeed() {
        if (this.documentAttachType == 3 && this.buttonState == 2) {
            FileLoader.getInstance(this.currentAccount).loadFile(this.documentAttach, true, 0);
            this.buttonState = 4;
            this.radialProgress.setBackground(getDrawableForCurrentState(), false, false);
        }
    }

    public void setFullyDraw(boolean draw) {
        this.fullyDraw = draw;
    }

    public void setVisiblePart(int position, int height) {
        if (this.currentMessageObject != null && this.currentMessageObject.textLayoutBlocks != null) {
            position -= this.textY;
            int newFirst = -1;
            int newLast = -1;
            int newCount = 0;
            int startBlock = 0;
            int a = 0;
            while (a < this.currentMessageObject.textLayoutBlocks.size() && ((TextLayoutBlock) this.currentMessageObject.textLayoutBlocks.get(a)).textYOffset <= ((float) position)) {
                startBlock = a;
                a++;
            }
            for (a = startBlock; a < this.currentMessageObject.textLayoutBlocks.size(); a++) {
                TextLayoutBlock block = (TextLayoutBlock) this.currentMessageObject.textLayoutBlocks.get(a);
                float y = block.textYOffset;
                if (intersect(y, ((float) block.height) + y, (float) position, (float) (position + height))) {
                    if (newFirst == -1) {
                        newFirst = a;
                    }
                    newLast = a;
                    newCount++;
                } else if (y > ((float) position)) {
                    break;
                }
            }
            if (this.lastVisibleBlockNum != newLast || this.firstVisibleBlockNum != newFirst || this.totalVisibleBlocksCount != newCount) {
                this.lastVisibleBlockNum = newLast;
                this.firstVisibleBlockNum = newFirst;
                this.totalVisibleBlocksCount = newCount;
                invalidate();
            }
        }
    }

    private boolean intersect(float left1, float right1, float left2, float right2) {
        if (left1 <= left2) {
            if (right1 >= left2) {
                return true;
            }
            return false;
        } else if (left1 > right2) {
            return false;
        } else {
            return true;
        }
    }

    public static StaticLayout generateStaticLayout(CharSequence text, TextPaint paint, int maxWidth, int smallWidth, int linesCount, int maxLines) {
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(text);
        int addedChars = 0;
        StaticLayout layout = new StaticLayout(text, paint, smallWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        int a = 0;
        while (a < linesCount) {
            Directions directions = layout.getLineDirections(a);
            if (layout.getLineLeft(a) != 0.0f || layout.isRtlCharAt(layout.getLineStart(a)) || layout.isRtlCharAt(layout.getLineEnd(a))) {
                maxWidth = smallWidth;
            }
            int pos = layout.getLineEnd(a);
            if (pos != text.length()) {
                pos--;
                if (stringBuilder.charAt(pos + addedChars) == ' ') {
                    stringBuilder.replace(pos + addedChars, (pos + addedChars) + 1, "\n");
                } else {
                    if (stringBuilder.charAt(pos + addedChars) != '\n') {
                        stringBuilder.insert(pos + addedChars, "\n");
                        addedChars++;
                    }
                }
                if (a == layout.getLineCount() - 1 || a == maxLines - 1) {
                    break;
                }
                a++;
            } else {
                break;
            }
        }
        return StaticLayoutEx.createStaticLayout(stringBuilder, paint, maxWidth, Alignment.ALIGN_NORMAL, 1.0f, (float) AndroidUtilities.dp(1.0f), false, TruncateAt.END, maxWidth, maxLines);
    }

    private void didClickedImage() {
        if (this.currentMessageObject.type == 1 || this.currentMessageObject.type == 13) {
            if (this.buttonState == -1) {
                this.delegate.didPressedImage(this);
            } else if (this.buttonState == 0) {
                didPressedButton(false);
            }
        } else if (this.currentMessageObject.type == 12) {
            this.delegate.didPressedUserAvatar(this, MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.currentMessageObject.messageOwner.media.user_id)));
        } else if (this.currentMessageObject.type == 5) {
            if (!MediaController.getInstance().isPlayingMessage(this.currentMessageObject) || MediaController.getInstance().isMessagePaused()) {
                this.delegate.needPlayMessage(this.currentMessageObject);
            } else {
                MediaController.getInstance().pauseMessage(this.currentMessageObject);
            }
        } else if (this.currentMessageObject.type == 8) {
            if (this.buttonState == -1) {
                if (SharedConfig.autoplayGifs) {
                    this.delegate.didPressedImage(this);
                    return;
                }
                this.buttonState = 2;
                this.currentMessageObject.gifState = 1.0f;
                this.photoImage.setAllowStartAnimation(false);
                this.photoImage.stopAnimation();
                this.radialProgress.setBackground(getDrawableForCurrentState(), false, false);
                invalidate();
            } else if (this.buttonState == 2 || this.buttonState == 0) {
                didPressedButton(false);
            }
        } else if (this.documentAttachType == 4) {
            if (this.buttonState == -1) {
                this.delegate.didPressedImage(this);
            } else if (this.buttonState == 0 || this.buttonState == 3) {
                didPressedButton(false);
            }
        } else if (this.currentMessageObject.type == 4) {
            this.delegate.didPressedImage(this);
        } else if (this.documentAttachType == 1) {
            if (this.buttonState == -1) {
                this.delegate.didPressedImage(this);
            }
        } else if (this.documentAttachType == 2) {
            if (this.buttonState == -1) {
                WebPage webPage = this.currentMessageObject.messageOwner.media.webpage;
                if (webPage == null) {
                    return;
                }
                if (webPage.embed_url == null || webPage.embed_url.length() == 0) {
                    Browser.openUrl(getContext(), webPage.url);
                } else {
                    this.delegate.needOpenWebView(webPage.embed_url, webPage.site_name, webPage.description, webPage.url, webPage.embed_width, webPage.embed_height);
                }
            }
        } else if (this.hasInvoicePreview && this.buttonState == -1) {
            this.delegate.didPressedImage(this);
        }
    }

    private void updateSecretTimeText(MessageObject messageObject) {
        if (messageObject != null && messageObject.needDrawBluredPreview()) {
            String str = messageObject.getSecretTimeString();
            if (str != null) {
                this.infoWidth = (int) Math.ceil((double) Theme.chat_infoPaint.measureText(str));
                this.infoLayout = new StaticLayout(TextUtils.ellipsize(str, Theme.chat_infoPaint, (float) this.infoWidth, TruncateAt.END), Theme.chat_infoPaint, this.infoWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                invalidate();
            }
        }
    }

    private boolean isPhotoDataChanged(MessageObject object) {
        if (object.type == 0 || object.type == 14) {
            return false;
        }
        if (object.type == 4) {
            if (this.currentUrl == null) {
                return true;
            }
            String url;
            double lat = object.messageOwner.media.geo.lat;
            double lon = object.messageOwner.media.geo._long;
            if (object.messageOwner.media instanceof TL_messageMediaGeoLive) {
                double rad = ((double) 268435456) / 3.141592653589793d;
                url = AndroidUtilities.formapMapUrl(this.currentAccount, ((1.5707963267948966d - (2.0d * Math.atan(Math.exp((((double) (Math.round(((double) 268435456) - ((Math.log((1.0d + Math.sin((3.141592653589793d * lat) / 180.0d)) / (1.0d - Math.sin((3.141592653589793d * lat) / 180.0d))) * rad) / 2.0d)) - ((long) (AndroidUtilities.dp(10.3f) << 6)))) - ((double) 268435456)) / rad)))) * 180.0d) / 3.141592653589793d, lon, (int) (((float) (this.backgroundWidth - AndroidUtilities.dp(21.0f))) / AndroidUtilities.density), (int) (((float) AndroidUtilities.dp(195.0f)) / AndroidUtilities.density), false, 15);
            } else if (TextUtils.isEmpty(object.messageOwner.media.title)) {
                url = AndroidUtilities.formapMapUrl(this.currentAccount, lat, lon, (int) (((float) (this.backgroundWidth - AndroidUtilities.dp(12.0f))) / AndroidUtilities.density), (int) (((float) AndroidUtilities.dp(195.0f)) / AndroidUtilities.density), true, 15);
            } else {
                url = AndroidUtilities.formapMapUrl(this.currentAccount, lat, lon, (int) (((float) (this.backgroundWidth - AndroidUtilities.dp(21.0f))) / AndroidUtilities.density), (int) (((float) AndroidUtilities.dp(195.0f)) / AndroidUtilities.density), true, 15);
            }
            if (url.equals(this.currentUrl)) {
                return false;
            }
            return true;
        } else if (this.currentPhotoObject == null || (this.currentPhotoObject.location instanceof TL_fileLocationUnavailable)) {
            return object.type == 1 || object.type == 5 || object.type == 3 || object.type == 8 || object.type == 13;
        } else {
            if (this.currentMessageObject == null || !this.photoNotSet) {
                return false;
            }
            return FileLoader.getPathToMessage(this.currentMessageObject.messageOwner).exists();
        }
    }

    private boolean isUserDataChanged() {
        boolean z = false;
        if (this.currentMessageObject != null && !this.hasLinkPreview && this.currentMessageObject.messageOwner.media != null && (this.currentMessageObject.messageOwner.media.webpage instanceof TL_webPage)) {
            return true;
        }
        if (this.currentMessageObject == null || (this.currentUser == null && this.currentChat == null)) {
            return false;
        }
        if (this.lastSendState != this.currentMessageObject.messageOwner.send_state || this.lastDeleteDate != this.currentMessageObject.messageOwner.destroyTime || this.lastViewsCount != this.currentMessageObject.messageOwner.views) {
            return true;
        }
        updateCurrentUserAndChat();
        FileLocation newPhoto = null;
        if (this.isAvatarVisible) {
            if (this.currentUser != null && this.currentUser.photo != null) {
                newPhoto = this.currentUser.photo.photo_small;
            } else if (!(this.currentChat == null || this.currentChat.photo == null)) {
                newPhoto = this.currentChat.photo.photo_small;
            }
        }
        if (this.replyTextLayout == null && this.currentMessageObject.replyMessageObject != null) {
            return true;
        }
        if (this.currentPhoto == null && newPhoto != null) {
            return true;
        }
        if (this.currentPhoto != null && newPhoto == null) {
            return true;
        }
        if (this.currentPhoto != null && newPhoto != null && (this.currentPhoto.local_id != newPhoto.local_id || this.currentPhoto.volume_id != newPhoto.volume_id)) {
            return true;
        }
        FileLocation newReplyPhoto = null;
        if (this.replyNameLayout != null) {
            PhotoSize photoSize = FileLoader.getClosestPhotoSizeWithSize(this.currentMessageObject.replyMessageObject.photoThumbs, 80);
            if (!(photoSize == null || this.currentMessageObject.replyMessageObject.type == 13)) {
                newReplyPhoto = photoSize.location;
            }
        }
        if (this.currentReplyPhoto == null && newReplyPhoto != null) {
            return true;
        }
        String newNameString = null;
        if (this.drawName && this.isChat && !this.currentMessageObject.isOutOwner()) {
            if (this.currentUser != null) {
                newNameString = UserObject.getUserName(this.currentUser);
            } else if (this.currentChat != null) {
                newNameString = this.currentChat.title;
            }
        }
        if (this.currentNameString == null && newNameString != null) {
            return true;
        }
        if (this.currentNameString != null && newNameString == null) {
            return true;
        }
        if (this.currentNameString != null && newNameString != null && !this.currentNameString.equals(newNameString)) {
            return true;
        }
        if (!this.drawForwardedName) {
            return false;
        }
        newNameString = this.currentMessageObject.getForwardedName();
        if ((this.currentForwardNameString == null && newNameString != null) || ((this.currentForwardNameString != null && newNameString == null) || !(this.currentForwardNameString == null || newNameString == null || this.currentForwardNameString.equals(newNameString)))) {
            z = true;
        }
        return z;
    }

    public ImageReceiver getPhotoImage() {
        return this.photoImage;
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.avatarImage.onDetachedFromWindow();
        this.replyImageReceiver.onDetachedFromWindow();
        this.locationImageReceiver.onDetachedFromWindow();
        this.photoImage.onDetachedFromWindow();
        if (!(!this.addedForTest || this.currentUrl == null || this.currentWebFile == null)) {
            ImageLoader.getInstance().removeTestWebFile(this.currentUrl);
            this.addedForTest = false;
        }
        DownloadController.getInstance(this.currentAccount).removeLoadingFileObserver(this);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setTranslationX(0.0f);
        this.avatarImage.onAttachedToWindow();
        this.avatarImage.setParentView((View) getParent());
        this.replyImageReceiver.onAttachedToWindow();
        this.locationImageReceiver.onAttachedToWindow();
        if (!this.photoImage.onAttachedToWindow()) {
            updateButtonState(false, false);
        } else if (this.drawPhotoImage) {
            updateButtonState(false, false);
        }
        if (this.currentMessageObject != null && this.currentMessageObject.isRoundVideo()) {
            checkRoundVideoPlayback(true);
        }
    }

    public void checkRoundVideoPlayback(boolean allowStart) {
        if (allowStart) {
            allowStart = MediaController.getInstance().getPlayingMessageObject() == null;
        }
        this.photoImage.setAllowStartAnimation(allowStart);
        if (allowStart) {
            this.photoImage.startAnimation();
        } else {
            this.photoImage.stopAnimation();
        }
    }

    protected void onLongPress() {
        if (this.pressedLink instanceof URLSpanMono) {
            this.delegate.didPressedUrl(this.currentMessageObject, this.pressedLink, true);
            return;
        }
        if (this.pressedLink instanceof URLSpanNoUnderline) {
            if (this.pressedLink.getURL().startsWith("/")) {
                this.delegate.didPressedUrl(this.currentMessageObject, this.pressedLink, true);
                return;
            }
        } else if (this.pressedLink instanceof URLSpan) {
            this.delegate.didPressedUrl(this.currentMessageObject, this.pressedLink, true);
            return;
        }
        resetPressedLink(-1);
        if (!(this.buttonPressed == 0 && this.miniButtonPressed == 0 && this.pressedBotButton == -1)) {
            this.buttonPressed = 0;
            this.miniButtonState = 0;
            this.pressedBotButton = -1;
            invalidate();
        }
        if (this.instantPressed) {
            this.instantButtonPressed = false;
            this.instantPressed = false;
            if (VERSION.SDK_INT >= 21 && this.instantViewSelectorDrawable != null) {
                this.instantViewSelectorDrawable.setState(StateSet.NOTHING);
            }
            invalidate();
        }
        if (this.delegate != null) {
            this.delegate.didLongPressed(this);
        }
    }

    public void setCheckPressed(boolean value, boolean pressed) {
        this.isCheckPressed = value;
        this.isPressed = pressed;
        updateRadialProgressBackground();
        if (this.useSeekBarWaweform) {
            this.seekBarWaveform.setSelected(isDrawSelectedBackground());
        } else {
            this.seekBar.setSelected(isDrawSelectedBackground());
        }
        invalidate();
    }

    public void setHighlightedAnimated() {
        this.isHighlightedAnimated = true;
        this.highlightProgress = 1000;
        this.lastHighlightProgressTime = System.currentTimeMillis();
        invalidate();
    }

    public boolean isHighlighted() {
        return this.isHighlighted;
    }

    public void setHighlighted(boolean value) {
        if (this.isHighlighted != value) {
            this.isHighlighted = value;
            if (this.isHighlighted) {
                this.isHighlightedAnimated = false;
                this.highlightProgress = 0;
            } else {
                this.lastHighlightProgressTime = System.currentTimeMillis();
                this.isHighlightedAnimated = true;
                this.highlightProgress = 300;
            }
            updateRadialProgressBackground();
            if (this.useSeekBarWaweform) {
                this.seekBarWaveform.setSelected(isDrawSelectedBackground());
            } else {
                this.seekBar.setSelected(isDrawSelectedBackground());
            }
            invalidate();
        }
    }

    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        updateRadialProgressBackground();
        if (this.useSeekBarWaweform) {
            this.seekBarWaveform.setSelected(isDrawSelectedBackground());
        } else {
            this.seekBar.setSelected(isDrawSelectedBackground());
        }
        invalidate();
    }

    private void updateRadialProgressBackground() {
        if (!this.drawRadialCheckBackground) {
            this.radialProgress.swapBackground(getDrawableForCurrentState());
            if (this.hasMiniProgress != 0) {
                this.radialProgress.swapMiniBackground(getMiniDrawableForCurrentState());
            }
        }
    }

    public void onSeekBarDrag(float progress) {
        if (this.currentMessageObject != null) {
            this.currentMessageObject.audioProgress = progress;
            MediaController.getInstance().seekToProgress(this.currentMessageObject, progress);
        }
    }

    private void updateWaveform() {
        if (this.currentMessageObject != null && this.documentAttachType == 3) {
            for (int a = 0; a < this.documentAttach.attributes.size(); a++) {
                DocumentAttribute attribute = (DocumentAttribute) this.documentAttach.attributes.get(a);
                if (attribute instanceof TL_documentAttributeAudio) {
                    if (attribute.waveform == null || attribute.waveform.length == 0) {
                        MediaController.getInstance().generateWaveform(this.currentMessageObject);
                    }
                    this.useSeekBarWaweform = attribute.waveform != null;
                    this.seekBarWaveform.setWaveform(attribute.waveform);
                    return;
                }
            }
        }
    }

    private int createDocumentLayout(int maxWidth, MessageObject messageObject) {
        if (messageObject.type == 0) {
            this.documentAttach = messageObject.messageOwner.media.webpage.document;
        } else {
            this.documentAttach = messageObject.messageOwner.media.document;
        }
        if (this.documentAttach == null) {
            return 0;
        }
        int duration;
        int a;
        DocumentAttribute attribute;
        if (MessageObject.isVoiceDocument(this.documentAttach)) {
            this.documentAttachType = 3;
            duration = 0;
            for (a = 0; a < this.documentAttach.attributes.size(); a++) {
                attribute = (DocumentAttribute) this.documentAttach.attributes.get(a);
                if (attribute instanceof TL_documentAttributeAudio) {
                    duration = attribute.duration;
                    break;
                }
            }
            this.widthBeforeNewTimeLine = (maxWidth - AndroidUtilities.dp(94.0f)) - ((int) Math.ceil((double) Theme.chat_audioTimePaint.measureText("00:00")));
            this.availableTimeWidth = maxWidth - AndroidUtilities.dp(18.0f);
            measureTime(messageObject);
            int minSize = AndroidUtilities.dp(174.0f) + this.timeWidth;
            if (!this.hasLinkPreview) {
                this.backgroundWidth = Math.min(maxWidth, (AndroidUtilities.dp(10.0f) * duration) + minSize);
            }
            this.seekBarWaveform.setMessageObject(messageObject);
            return 0;
        } else if (MessageObject.isMusicDocument(this.documentAttach)) {
            this.documentAttachType = 5;
            maxWidth -= AndroidUtilities.dp(86.0f);
            this.songLayout = new StaticLayout(TextUtils.ellipsize(messageObject.getMusicTitle().replace('\n', ' '), Theme.chat_audioTitlePaint, (float) (maxWidth - AndroidUtilities.dp(12.0f)), TruncateAt.END), Theme.chat_audioTitlePaint, maxWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            if (this.songLayout.getLineCount() > 0) {
                this.songX = -((int) Math.ceil((double) this.songLayout.getLineLeft(0)));
            }
            this.performerLayout = new StaticLayout(TextUtils.ellipsize(messageObject.getMusicAuthor().replace('\n', ' '), Theme.chat_audioPerformerPaint, (float) maxWidth, TruncateAt.END), Theme.chat_audioPerformerPaint, maxWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            if (this.performerLayout.getLineCount() > 0) {
                this.performerX = -((int) Math.ceil((double) this.performerLayout.getLineLeft(0)));
            }
            duration = 0;
            for (a = 0; a < this.documentAttach.attributes.size(); a++) {
                attribute = (DocumentAttribute) this.documentAttach.attributes.get(a);
                if (attribute instanceof TL_documentAttributeAudio) {
                    duration = attribute.duration;
                    break;
                }
            }
            int durationWidth = (int) Math.ceil((double) Theme.chat_audioTimePaint.measureText(String.format("%d:%02d / %d:%02d", new Object[]{Integer.valueOf(duration / 60), Integer.valueOf(duration % 60), Integer.valueOf(duration / 60), Integer.valueOf(duration % 60)})));
            this.widthBeforeNewTimeLine = (this.backgroundWidth - AndroidUtilities.dp(86.0f)) - durationWidth;
            this.availableTimeWidth = this.backgroundWidth - AndroidUtilities.dp(28.0f);
            return durationWidth;
        } else if (MessageObject.isVideoDocument(this.documentAttach)) {
            this.documentAttachType = 4;
            if (!messageObject.needDrawBluredPreview()) {
                duration = 0;
                for (a = 0; a < this.documentAttach.attributes.size(); a++) {
                    attribute = (DocumentAttribute) this.documentAttach.attributes.get(a);
                    if (attribute instanceof TL_documentAttributeVideo) {
                        duration = attribute.duration;
                        break;
                    }
                }
                int seconds = duration - ((duration / 60) * 60);
                str = String.format("%d:%02d, %s", new Object[]{Integer.valueOf(duration / 60), Integer.valueOf(seconds), AndroidUtilities.formatFileSize((long) this.documentAttach.size)});
                this.infoWidth = (int) Math.ceil((double) Theme.chat_infoPaint.measureText(str));
                this.infoLayout = new StaticLayout(str, Theme.chat_infoPaint, this.infoWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            }
            return 0;
        } else {
            int width;
            boolean z = (this.documentAttach.mime_type != null && this.documentAttach.mime_type.toLowerCase().startsWith("image/")) || !(this.documentAttach.thumb == null || (this.documentAttach.thumb instanceof TL_photoSizeEmpty) || (this.documentAttach.thumb.location instanceof TL_fileLocationUnavailable));
            this.drawPhotoImage = z;
            if (!this.drawPhotoImage) {
                maxWidth += AndroidUtilities.dp(30.0f);
            }
            this.documentAttachType = 1;
            String name = FileLoader.getDocumentFileName(this.documentAttach);
            if (name == null || name.length() == 0) {
                name = LocaleController.getString("AttachDocument", R.string.AttachDocument);
            }
            this.docTitleLayout = StaticLayoutEx.createStaticLayout(name, Theme.chat_docNamePaint, maxWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false, TruncateAt.MIDDLE, maxWidth, this.drawPhotoImage ? 2 : 1);
            this.docTitleOffsetX = Integer.MIN_VALUE;
            if (this.docTitleLayout == null || this.docTitleLayout.getLineCount() <= 0) {
                width = maxWidth;
                this.docTitleOffsetX = 0;
            } else {
                int maxLineWidth = 0;
                for (a = 0; a < this.docTitleLayout.getLineCount(); a++) {
                    maxLineWidth = Math.max(maxLineWidth, (int) Math.ceil((double) this.docTitleLayout.getLineWidth(a)));
                    this.docTitleOffsetX = Math.max(this.docTitleOffsetX, (int) Math.ceil((double) (-this.docTitleLayout.getLineLeft(a))));
                }
                width = Math.min(maxWidth, maxLineWidth);
            }
            str = AndroidUtilities.formatFileSize((long) this.documentAttach.size) + " " + FileLoader.getDocumentExtension(this.documentAttach);
            this.infoWidth = Math.min(maxWidth - AndroidUtilities.dp(30.0f), (int) Math.ceil((double) Theme.chat_infoPaint.measureText(str)));
            CharSequence str2 = TextUtils.ellipsize(str, Theme.chat_infoPaint, (float) this.infoWidth, TruncateAt.END);
            try {
                if (this.infoWidth < 0) {
                    this.infoWidth = AndroidUtilities.dp(10.0f);
                }
                this.infoLayout = new StaticLayout(str2, Theme.chat_infoPaint, this.infoWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            } catch (Throwable e) {
                FileLog.e(e);
            }
            if (this.drawPhotoImage) {
                this.currentPhotoObject = FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, AndroidUtilities.getPhotoSize());
                this.photoImage.setNeedsQualityThumb(true);
                this.photoImage.setShouldGenerateQualityThumb(true);
                this.photoImage.setParentMessageObject(messageObject);
                if (this.currentPhotoObject != null) {
                    this.currentPhotoFilter = "86_86_b";
                    this.photoImage.setImage(null, null, null, null, this.currentPhotoObject.location, this.currentPhotoFilter, 0, null, 1);
                } else {
                    this.photoImage.setImageBitmap((BitmapDrawable) null);
                }
            }
            return width;
        }
    }

    private void calcBackgroundWidth(int maxWidth, int timeMore, int maxChildWidth) {
        if (this.hasLinkPreview || this.hasOldCaptionPreview || this.hasGamePreview || this.hasInvoicePreview || maxWidth - this.currentMessageObject.lastLineWidth < timeMore || this.currentMessageObject.hasRtl) {
            this.totalHeight += AndroidUtilities.dp(14.0f);
            this.hasNewLineForTime = true;
            this.backgroundWidth = Math.max(maxChildWidth, this.currentMessageObject.lastLineWidth) + AndroidUtilities.dp(31.0f);
            this.backgroundWidth = Math.max(this.backgroundWidth, (this.currentMessageObject.isOutOwner() ? this.timeWidth + AndroidUtilities.dp(17.0f) : this.timeWidth) + AndroidUtilities.dp(31.0f));
            return;
        }
        int diff = maxChildWidth - this.currentMessageObject.lastLineWidth;
        if (diff < 0 || diff > timeMore) {
            this.backgroundWidth = Math.max(maxChildWidth, this.currentMessageObject.lastLineWidth + timeMore) + AndroidUtilities.dp(31.0f);
        } else {
            this.backgroundWidth = ((maxChildWidth + timeMore) - diff) + AndroidUtilities.dp(31.0f);
        }
    }

    public void setHighlightedText(String text) {
        if (this.currentMessageObject.messageOwner.message != null && this.currentMessageObject != null && this.currentMessageObject.type == 0 && !TextUtils.isEmpty(this.currentMessageObject.messageText) && text != null) {
            int start = TextUtils.indexOf(this.currentMessageObject.messageOwner.message.toLowerCase(), text.toLowerCase());
            if (start != -1) {
                int end = start + text.length();
                int c = 0;
                while (c < this.currentMessageObject.textLayoutBlocks.size()) {
                    TextLayoutBlock block = (TextLayoutBlock) this.currentMessageObject.textLayoutBlocks.get(c);
                    if (start < block.charactersOffset || start >= block.charactersOffset + block.textLayout.getText().length()) {
                        c++;
                    } else {
                        this.linkSelectionBlockNum = c;
                        resetUrlPaths(true);
                        try {
                            LinkPath path = obtainNewUrlPath(true);
                            int length = block.textLayout.getText().length();
                            path.setCurrentLayout(block.textLayout, start, 0.0f);
                            block.textLayout.getSelectionPath(start, end - block.charactersOffset, path);
                            if (end >= block.charactersOffset + length) {
                                for (int a = c + 1; a < this.currentMessageObject.textLayoutBlocks.size(); a++) {
                                    TextLayoutBlock nextBlock = (TextLayoutBlock) this.currentMessageObject.textLayoutBlocks.get(a);
                                    length = nextBlock.textLayout.getText().length();
                                    path = obtainNewUrlPath(true);
                                    path.setCurrentLayout(nextBlock.textLayout, 0, (float) nextBlock.height);
                                    nextBlock.textLayout.getSelectionPath(0, end - nextBlock.charactersOffset, path);
                                    if (end < (block.charactersOffset + length) - 1) {
                                        break;
                                    }
                                }
                            }
                        } catch (Throwable e) {
                            FileLog.e(e);
                        }
                        invalidate();
                        return;
                    }
                }
            } else if (!this.urlPathSelection.isEmpty()) {
                this.linkSelectionBlockNum = -1;
                resetUrlPaths(true);
                invalidate();
            }
        } else if (!this.urlPathSelection.isEmpty()) {
            this.linkSelectionBlockNum = -1;
            resetUrlPaths(true);
            invalidate();
        }
    }

    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == this.instantViewSelectorDrawable;
    }

    private boolean isCurrentLocationTimeExpired(MessageObject messageObject) {
        if (this.currentMessageObject.messageOwner.media.period % 60 == 0) {
            if (Math.abs(ConnectionsManager.getInstance(this.currentAccount).getCurrentTime() - messageObject.messageOwner.date) > messageObject.messageOwner.media.period) {
                return true;
            }
            return false;
        } else if (Math.abs(ConnectionsManager.getInstance(this.currentAccount).getCurrentTime() - messageObject.messageOwner.date) <= messageObject.messageOwner.media.period - 5) {
            return false;
        } else {
            return true;
        }
    }

    private void checkLocationExpired() {
        if (this.currentMessageObject != null) {
            boolean newExpired = isCurrentLocationTimeExpired(this.currentMessageObject);
            if (newExpired != this.locationExpired) {
                this.locationExpired = newExpired;
                if (this.locationExpired) {
                    MessageObject messageObject = this.currentMessageObject;
                    this.currentMessageObject = null;
                    setMessageObject(messageObject, this.currentMessagesGroup, this.pinnedBottom, this.pinnedTop);
                    return;
                }
                AndroidUtilities.runOnUIThread(this.invalidateRunnable, 1000);
                this.scheduledInvalidate = true;
                this.docTitleLayout = new StaticLayout(LocaleController.getString("AttachLiveLocation", R.string.AttachLiveLocation), Theme.chat_locationTitlePaint, this.backgroundWidth - AndroidUtilities.dp(91.0f), Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setMessageObject(org.telegram.messenger.MessageObject r145, org.telegram.messenger.MessageObject.GroupedMessages r146, boolean r147, boolean r148) {
        /*
        r144 = this;
        r4 = r145.checkLayout();
        if (r4 != 0) goto L_0x0016;
    L_0x0006:
        r0 = r144;
        r4 = r0.currentPosition;
        if (r4 == 0) goto L_0x001b;
    L_0x000c:
        r0 = r144;
        r4 = r0.lastHeight;
        r6 = org.telegram.messenger.AndroidUtilities.displaySize;
        r6 = r6.y;
        if (r4 == r6) goto L_0x001b;
    L_0x0016:
        r4 = 0;
        r0 = r144;
        r0.currentMessageObject = r4;
    L_0x001b:
        r0 = r144;
        r4 = r0.currentMessageObject;
        if (r4 == 0) goto L_0x002f;
    L_0x0021:
        r0 = r144;
        r4 = r0.currentMessageObject;
        r4 = r4.getId();
        r6 = r145.getId();
        if (r4 == r6) goto L_0x0858;
    L_0x002f:
        r97 = 1;
    L_0x0031:
        r0 = r144;
        r4 = r0.currentMessageObject;
        r0 = r145;
        if (r4 != r0) goto L_0x003f;
    L_0x0039:
        r0 = r145;
        r4 = r0.forceUpdate;
        if (r4 == 0) goto L_0x085c;
    L_0x003f:
        r96 = 1;
    L_0x0041:
        r0 = r144;
        r4 = r0.currentMessageObject;
        if (r4 == 0) goto L_0x0062;
    L_0x0047:
        r0 = r144;
        r4 = r0.currentMessageObject;
        r4 = r4.getId();
        r6 = r145.getId();
        if (r4 != r6) goto L_0x0062;
    L_0x0055:
        r0 = r144;
        r4 = r0.lastSendState;
        r6 = 3;
        if (r4 != r6) goto L_0x0062;
    L_0x005c:
        r4 = r145.isSent();
        if (r4 != 0) goto L_0x0076;
    L_0x0062:
        r0 = r144;
        r4 = r0.currentMessageObject;
        r0 = r145;
        if (r4 != r0) goto L_0x0860;
    L_0x006a:
        r4 = r144.isUserDataChanged();
        if (r4 != 0) goto L_0x0076;
    L_0x0070:
        r0 = r144;
        r4 = r0.photoNotSet;
        if (r4 == 0) goto L_0x0860;
    L_0x0076:
        r63 = 1;
    L_0x0078:
        r0 = r144;
        r4 = r0.currentMessagesGroup;
        r0 = r146;
        if (r0 == r4) goto L_0x0864;
    L_0x0080:
        r74 = 1;
    L_0x0082:
        if (r74 != 0) goto L_0x00ab;
    L_0x0084:
        if (r146 == 0) goto L_0x00ab;
    L_0x0086:
        r0 = r146;
        r4 = r0.messages;
        r4 = r4.size();
        r6 = 1;
        if (r4 <= r6) goto L_0x0868;
    L_0x0091:
        r0 = r144;
        r4 = r0.currentMessagesGroup;
        r4 = r4.positions;
        r0 = r144;
        r6 = r0.currentMessageObject;
        r99 = r4.get(r6);
        r99 = (org.telegram.messenger.MessageObject.GroupedMessagePosition) r99;
    L_0x00a1:
        r0 = r144;
        r4 = r0.currentPosition;
        r0 = r99;
        if (r0 == r4) goto L_0x086c;
    L_0x00a9:
        r74 = 1;
    L_0x00ab:
        if (r96 != 0) goto L_0x00c7;
    L_0x00ad:
        if (r63 != 0) goto L_0x00c7;
    L_0x00af:
        if (r74 != 0) goto L_0x00c7;
    L_0x00b1:
        r4 = r144.isPhotoDataChanged(r145);
        if (r4 != 0) goto L_0x00c7;
    L_0x00b7:
        r0 = r144;
        r4 = r0.pinnedBottom;
        r0 = r147;
        if (r4 != r0) goto L_0x00c7;
    L_0x00bf:
        r0 = r144;
        r4 = r0.pinnedTop;
        r0 = r148;
        if (r4 == r0) goto L_0x3c70;
    L_0x00c7:
        r0 = r147;
        r1 = r144;
        r1.pinnedBottom = r0;
        r0 = r148;
        r1 = r144;
        r1.pinnedTop = r0;
        r4 = -2;
        r0 = r144;
        r0.lastTime = r4;
        r4 = 0;
        r0 = r144;
        r0.isHighlightedAnimated = r4;
        r4 = -1;
        r0 = r144;
        r0.widthBeforeNewTimeLine = r4;
        r0 = r145;
        r1 = r144;
        r1.currentMessageObject = r0;
        r0 = r146;
        r1 = r144;
        r1.currentMessagesGroup = r0;
        r0 = r144;
        r4 = r0.currentMessagesGroup;
        if (r4 == 0) goto L_0x0870;
    L_0x00f4:
        r0 = r144;
        r4 = r0.currentMessagesGroup;
        r4 = r4.posArray;
        r4 = r4.size();
        r6 = 1;
        if (r4 <= r6) goto L_0x0870;
    L_0x0101:
        r0 = r144;
        r4 = r0.currentMessagesGroup;
        r4 = r4.positions;
        r0 = r144;
        r6 = r0.currentMessageObject;
        r4 = r4.get(r6);
        r4 = (org.telegram.messenger.MessageObject.GroupedMessagePosition) r4;
        r0 = r144;
        r0.currentPosition = r4;
        r0 = r144;
        r4 = r0.currentPosition;
        if (r4 != 0) goto L_0x0120;
    L_0x011b:
        r4 = 0;
        r0 = r144;
        r0.currentMessagesGroup = r4;
    L_0x0120:
        r0 = r144;
        r4 = r0.pinnedTop;
        if (r4 == 0) goto L_0x087c;
    L_0x0126:
        r0 = r144;
        r4 = r0.currentPosition;
        if (r4 == 0) goto L_0x0136;
    L_0x012c:
        r0 = r144;
        r4 = r0.currentPosition;
        r4 = r4.flags;
        r4 = r4 & 4;
        if (r4 == 0) goto L_0x087c;
    L_0x0136:
        r4 = 1;
    L_0x0137:
        r0 = r144;
        r0.drawPinnedTop = r4;
        r0 = r144;
        r4 = r0.pinnedBottom;
        if (r4 == 0) goto L_0x087f;
    L_0x0141:
        r0 = r144;
        r4 = r0.currentPosition;
        if (r4 == 0) goto L_0x0151;
    L_0x0147:
        r0 = r144;
        r4 = r0.currentPosition;
        r4 = r4.flags;
        r4 = r4 & 8;
        if (r4 == 0) goto L_0x087f;
    L_0x0151:
        r4 = 1;
    L_0x0152:
        r0 = r144;
        r0.drawPinnedBottom = r4;
        r0 = r144;
        r4 = r0.photoImage;
        r6 = 0;
        r4.setCrossfadeWithOldImage(r6);
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.send_state;
        r0 = r144;
        r0.lastSendState = r4;
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.destroyTime;
        r0 = r144;
        r0.lastDeleteDate = r4;
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.views;
        r0 = r144;
        r0.lastViewsCount = r4;
        r4 = 0;
        r0 = r144;
        r0.isPressed = r4;
        r4 = 0;
        r0 = r144;
        r0.gamePreviewPressed = r4;
        r4 = 1;
        r0 = r144;
        r0.isCheckPressed = r4;
        r4 = 0;
        r0 = r144;
        r0.hasNewLineForTime = r4;
        r0 = r144;
        r4 = r0.isChat;
        if (r4 == 0) goto L_0x0882;
    L_0x0196:
        r4 = r145.isOutOwner();
        if (r4 != 0) goto L_0x0882;
    L_0x019c:
        r4 = r145.needDrawAvatar();
        if (r4 == 0) goto L_0x0882;
    L_0x01a2:
        r0 = r144;
        r4 = r0.currentPosition;
        if (r4 == 0) goto L_0x01b0;
    L_0x01a8:
        r0 = r144;
        r4 = r0.currentPosition;
        r4 = r4.edge;
        if (r4 == 0) goto L_0x0882;
    L_0x01b0:
        r4 = 1;
    L_0x01b1:
        r0 = r144;
        r0.isAvatarVisible = r4;
        r4 = 0;
        r0 = r144;
        r0.wasLayout = r4;
        r4 = 0;
        r0 = r144;
        r0.drwaShareGoIcon = r4;
        r4 = 0;
        r0 = r144;
        r0.groupPhotoInvisible = r4;
        r4 = r144.checkNeedDrawShareButton(r145);
        r0 = r144;
        r0.drawShareButton = r4;
        r4 = 0;
        r0 = r144;
        r0.replyNameLayout = r4;
        r4 = 0;
        r0 = r144;
        r0.adminLayout = r4;
        r4 = 0;
        r0 = r144;
        r0.replyTextLayout = r4;
        r4 = 0;
        r0 = r144;
        r0.replyNameWidth = r4;
        r4 = 0;
        r0 = r144;
        r0.replyTextWidth = r4;
        r4 = 0;
        r0 = r144;
        r0.viaWidth = r4;
        r4 = 0;
        r0 = r144;
        r0.viaNameWidth = r4;
        r4 = 0;
        r0 = r144;
        r0.addedCaptionHeight = r4;
        r4 = 0;
        r0 = r144;
        r0.currentReplyPhoto = r4;
        r4 = 0;
        r0 = r144;
        r0.currentUser = r4;
        r4 = 0;
        r0 = r144;
        r0.currentChat = r4;
        r4 = 0;
        r0 = r144;
        r0.currentViaBotUser = r4;
        r4 = 0;
        r0 = r144;
        r0.instantViewLayout = r4;
        r4 = 0;
        r0 = r144;
        r0.drawNameLayout = r4;
        r0 = r144;
        r4 = r0.scheduledInvalidate;
        if (r4 == 0) goto L_0x0224;
    L_0x0218:
        r0 = r144;
        r4 = r0.invalidateRunnable;
        org.telegram.messenger.AndroidUtilities.cancelRunOnUIThread(r4);
        r4 = 0;
        r0 = r144;
        r0.scheduledInvalidate = r4;
    L_0x0224:
        r4 = -1;
        r0 = r144;
        r0.resetPressedLink(r4);
        r4 = 0;
        r0 = r145;
        r0.forceUpdate = r4;
        r4 = 0;
        r0 = r144;
        r0.drawPhotoImage = r4;
        r4 = 0;
        r0 = r144;
        r0.hasLinkPreview = r4;
        r4 = 0;
        r0 = r144;
        r0.hasOldCaptionPreview = r4;
        r4 = 0;
        r0 = r144;
        r0.hasGamePreview = r4;
        r4 = 0;
        r0 = r144;
        r0.hasInvoicePreview = r4;
        r4 = 0;
        r0 = r144;
        r0.instantButtonPressed = r4;
        r0 = r144;
        r0.instantPressed = r4;
        r4 = android.os.Build.VERSION.SDK_INT;
        r6 = 21;
        if (r4 < r6) goto L_0x026f;
    L_0x0257:
        r0 = r144;
        r4 = r0.instantViewSelectorDrawable;
        if (r4 == 0) goto L_0x026f;
    L_0x025d:
        r0 = r144;
        r4 = r0.instantViewSelectorDrawable;
        r6 = 0;
        r8 = 0;
        r4.setVisible(r6, r8);
        r0 = r144;
        r4 = r0.instantViewSelectorDrawable;
        r6 = android.util.StateSet.NOTHING;
        r4.setState(r6);
    L_0x026f:
        r4 = 0;
        r0 = r144;
        r0.linkPreviewPressed = r4;
        r4 = 0;
        r0 = r144;
        r0.buttonPressed = r4;
        r4 = 0;
        r0 = r144;
        r0.miniButtonPressed = r4;
        r4 = -1;
        r0 = r144;
        r0.pressedBotButton = r4;
        r4 = 0;
        r0 = r144;
        r0.linkPreviewHeight = r4;
        r4 = 0;
        r0 = r144;
        r0.mediaOffsetY = r4;
        r4 = 0;
        r0 = r144;
        r0.documentAttachType = r4;
        r4 = 0;
        r0 = r144;
        r0.documentAttach = r4;
        r4 = 0;
        r0 = r144;
        r0.descriptionLayout = r4;
        r4 = 0;
        r0 = r144;
        r0.titleLayout = r4;
        r4 = 0;
        r0 = r144;
        r0.videoInfoLayout = r4;
        r4 = 0;
        r0 = r144;
        r0.photosCountLayout = r4;
        r4 = 0;
        r0 = r144;
        r0.siteNameLayout = r4;
        r4 = 0;
        r0 = r144;
        r0.authorLayout = r4;
        r4 = 0;
        r0 = r144;
        r0.captionLayout = r4;
        r4 = 0;
        r0 = r144;
        r0.captionOffsetX = r4;
        r4 = 0;
        r0 = r144;
        r0.currentCaption = r4;
        r4 = 0;
        r0 = r144;
        r0.docTitleLayout = r4;
        r4 = 0;
        r0 = r144;
        r0.drawImageButton = r4;
        r4 = 0;
        r0 = r144;
        r0.currentPhotoObject = r4;
        r4 = 0;
        r0 = r144;
        r0.currentPhotoObjectThumb = r4;
        r4 = 0;
        r0 = r144;
        r0.currentPhotoFilter = r4;
        r4 = 0;
        r0 = r144;
        r0.infoLayout = r4;
        r4 = 0;
        r0 = r144;
        r0.cancelLoading = r4;
        r4 = -1;
        r0 = r144;
        r0.buttonState = r4;
        r4 = -1;
        r0 = r144;
        r0.miniButtonState = r4;
        r4 = 0;
        r0 = r144;
        r0.hasMiniProgress = r4;
        r0 = r144;
        r4 = r0.addedForTest;
        if (r4 == 0) goto L_0x0313;
    L_0x02fc:
        r0 = r144;
        r4 = r0.currentUrl;
        if (r4 == 0) goto L_0x0313;
    L_0x0302:
        r0 = r144;
        r4 = r0.currentWebFile;
        if (r4 == 0) goto L_0x0313;
    L_0x0308:
        r4 = org.telegram.messenger.ImageLoader.getInstance();
        r0 = r144;
        r6 = r0.currentUrl;
        r4.removeTestWebFile(r6);
    L_0x0313:
        r4 = 0;
        r0 = r144;
        r0.addedForTest = r4;
        r4 = 0;
        r0 = r144;
        r0.currentUrl = r4;
        r4 = 0;
        r0 = r144;
        r0.currentWebFile = r4;
        r4 = 0;
        r0 = r144;
        r0.photoNotSet = r4;
        r4 = 1;
        r0 = r144;
        r0.drawBackground = r4;
        r4 = 0;
        r0 = r144;
        r0.drawName = r4;
        r4 = 0;
        r0 = r144;
        r0.useSeekBarWaweform = r4;
        r4 = 0;
        r0 = r144;
        r0.drawInstantView = r4;
        r4 = 0;
        r0 = r144;
        r0.drawInstantViewType = r4;
        r4 = 0;
        r0 = r144;
        r0.drawForwardedName = r4;
        r4 = 0;
        r0 = r144;
        r0.mediaBackground = r4;
        r57 = 0;
        r4 = 0;
        r0 = r144;
        r0.availableTimeWidth = r4;
        r0 = r144;
        r4 = r0.photoImage;
        r6 = 0;
        r4.setForceLoading(r6);
        r0 = r144;
        r4 = r0.photoImage;
        r6 = 0;
        r4.setNeedsQualityThumb(r6);
        r0 = r144;
        r4 = r0.photoImage;
        r6 = 0;
        r4.setShouldGenerateQualityThumb(r6);
        r0 = r144;
        r4 = r0.photoImage;
        r6 = 0;
        r4.setAllowDecodeSingleFrame(r6);
        r0 = r144;
        r4 = r0.photoImage;
        r6 = 0;
        r4.setParentMessageObject(r6);
        r0 = r144;
        r4 = r0.photoImage;
        r6 = 1077936128; // 0x40400000 float:3.0 double:5.325712093E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4.setRoundRadius(r6);
        if (r96 == 0) goto L_0x0397;
    L_0x0388:
        r4 = 0;
        r0 = r144;
        r0.firstVisibleBlockNum = r4;
        r4 = 0;
        r0 = r144;
        r0.lastVisibleBlockNum = r4;
        r4 = 1;
        r0 = r144;
        r0.needNewVisiblePart = r4;
    L_0x0397:
        r0 = r145;
        r4 = r0.type;
        if (r4 != 0) goto L_0x1c84;
    L_0x039d:
        r4 = 1;
        r0 = r144;
        r0.drawForwardedName = r4;
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 == 0) goto L_0x08aa;
    L_0x03a8:
        r0 = r144;
        r4 = r0.isChat;
        if (r4 == 0) goto L_0x0885;
    L_0x03ae:
        r4 = r145.isOutOwner();
        if (r4 != 0) goto L_0x0885;
    L_0x03b4:
        r4 = r145.needDrawAvatar();
        if (r4 == 0) goto L_0x0885;
    L_0x03ba:
        r4 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        r6 = 1123287040; // 0x42f40000 float:122.0 double:5.54977537E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r95 = r4 - r6;
        r4 = 1;
        r0 = r144;
        r0.drawName = r4;
    L_0x03cb:
        r0 = r95;
        r1 = r144;
        r1.availableTimeWidth = r0;
        r4 = r145.isRoundVideo();
        if (r4 == 0) goto L_0x03ff;
    L_0x03d7:
        r0 = r144;
        r4 = r0.availableTimeWidth;
        r8 = (double) r4;
        r4 = org.telegram.ui.ActionBar.Theme.chat_audioTimePaint;
        r6 = "00:00";
        r4 = r4.measureText(r6);
        r0 = (double) r4;
        r20 = r0;
        r20 = java.lang.Math.ceil(r20);
        r4 = r145.isOutOwner();
        if (r4 == 0) goto L_0x0904;
    L_0x03f2:
        r4 = 0;
    L_0x03f3:
        r0 = (double) r4;
        r26 = r0;
        r20 = r20 + r26;
        r8 = r8 - r20;
        r4 = (int) r8;
        r0 = r144;
        r0.availableTimeWidth = r4;
    L_0x03ff:
        r144.measureTime(r145);
        r0 = r144;
        r4 = r0.timeWidth;
        r6 = 1086324736; // 0x40c00000 float:6.0 double:5.367157323E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r130 = r4 + r6;
        r4 = r145.isOutOwner();
        if (r4 == 0) goto L_0x041c;
    L_0x0414:
        r4 = 1101266944; // 0x41a40000 float:20.5 double:5.44098164E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r130 = r130 + r4;
    L_0x041c:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r4 = r4 instanceof org.telegram.tgnet.TLRPC.TL_messageMediaGame;
        if (r4 == 0) goto L_0x090c;
    L_0x0426:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r4 = r4.game;
        r4 = r4 instanceof org.telegram.tgnet.TLRPC.TL_game;
        if (r4 == 0) goto L_0x090c;
    L_0x0432:
        r4 = 1;
    L_0x0433:
        r0 = r144;
        r0.hasGamePreview = r4;
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r4 = r4 instanceof org.telegram.tgnet.TLRPC.TL_messageMediaInvoice;
        r0 = r144;
        r0.hasInvoicePreview = r4;
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r4 = r4 instanceof org.telegram.tgnet.TLRPC.TL_messageMediaWebPage;
        if (r4 == 0) goto L_0x090f;
    L_0x044d:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r4 = r4.webpage;
        r4 = r4 instanceof org.telegram.tgnet.TLRPC.TL_webPage;
        if (r4 == 0) goto L_0x090f;
    L_0x0459:
        r4 = 1;
    L_0x045a:
        r0 = r144;
        r0.hasLinkPreview = r4;
        r0 = r144;
        r4 = r0.hasLinkPreview;
        if (r4 == 0) goto L_0x0912;
    L_0x0464:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r4 = r4.webpage;
        r4 = r4.cached_page;
        if (r4 == 0) goto L_0x0912;
    L_0x0470:
        r4 = 1;
    L_0x0471:
        r0 = r144;
        r0.drawInstantView = r4;
        r124 = 0;
        r0 = r144;
        r4 = r0.hasLinkPreview;
        if (r4 == 0) goto L_0x0915;
    L_0x047d:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r4 = r4.webpage;
        r0 = r4.site_name;
        r123 = r0;
    L_0x0489:
        r0 = r144;
        r4 = r0.hasLinkPreview;
        if (r4 == 0) goto L_0x0919;
    L_0x048f:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r4 = r4.webpage;
        r0 = r4.type;
        r140 = r0;
    L_0x049b:
        r0 = r144;
        r4 = r0.drawInstantView;
        if (r4 != 0) goto L_0x094b;
    L_0x04a1:
        r4 = "telegram_channel";
        r0 = r140;
        r4 = r4.equals(r0);
        if (r4 == 0) goto L_0x091d;
    L_0x04ac:
        r4 = 1;
        r0 = r144;
        r0.drawInstantView = r4;
        r4 = 1;
        r0 = r144;
        r0.drawInstantViewType = r4;
    L_0x04b6:
        r0 = r95;
        r1 = r144;
        r1.backgroundWidth = r0;
        r0 = r144;
        r4 = r0.hasLinkPreview;
        if (r4 != 0) goto L_0x04d8;
    L_0x04c2:
        r0 = r144;
        r4 = r0.hasGamePreview;
        if (r4 != 0) goto L_0x04d8;
    L_0x04c8:
        r0 = r144;
        r4 = r0.hasInvoicePreview;
        if (r4 != 0) goto L_0x04d8;
    L_0x04ce:
        r0 = r145;
        r4 = r0.lastLineWidth;
        r4 = r95 - r4;
        r0 = r130;
        if (r4 >= r0) goto L_0x0a34;
    L_0x04d8:
        r0 = r144;
        r4 = r0.backgroundWidth;
        r0 = r145;
        r6 = r0.lastLineWidth;
        r4 = java.lang.Math.max(r4, r6);
        r6 = 1106771968; // 0x41f80000 float:31.0 double:5.46818007E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r0 = r144;
        r0.backgroundWidth = r4;
        r0 = r144;
        r4 = r0.backgroundWidth;
        r0 = r144;
        r6 = r0.timeWidth;
        r8 = 1106771968; // 0x41f80000 float:31.0 double:5.46818007E-315;
        r8 = org.telegram.messenger.AndroidUtilities.dp(r8);
        r6 = r6 + r8;
        r4 = java.lang.Math.max(r4, r6);
        r0 = r144;
        r0.backgroundWidth = r4;
    L_0x0506:
        r0 = r144;
        r4 = r0.backgroundWidth;
        r6 = 1106771968; // 0x41f80000 float:31.0 double:5.46818007E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r0 = r144;
        r0.availableTimeWidth = r4;
        r4 = r145.isRoundVideo();
        if (r4 == 0) goto L_0x0543;
    L_0x051b:
        r0 = r144;
        r4 = r0.availableTimeWidth;
        r8 = (double) r4;
        r4 = org.telegram.ui.ActionBar.Theme.chat_audioTimePaint;
        r6 = "00:00";
        r4 = r4.measureText(r6);
        r0 = (double) r4;
        r20 = r0;
        r20 = java.lang.Math.ceil(r20);
        r4 = r145.isOutOwner();
        if (r4 == 0) goto L_0x0a76;
    L_0x0536:
        r4 = 0;
    L_0x0537:
        r0 = (double) r4;
        r26 = r0;
        r20 = r20 + r26;
        r8 = r8 - r20;
        r4 = (int) r8;
        r0 = r144;
        r0.availableTimeWidth = r4;
    L_0x0543:
        r144.setMessageObjectInternal(r145);
        r0 = r145;
        r6 = r0.textWidth;
        r0 = r144;
        r4 = r0.hasGamePreview;
        if (r4 != 0) goto L_0x0556;
    L_0x0550:
        r0 = r144;
        r4 = r0.hasInvoicePreview;
        if (r4 == 0) goto L_0x0a7e;
    L_0x0556:
        r4 = 1092616192; // 0x41200000 float:10.0 double:5.398241246E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
    L_0x055c:
        r4 = r4 + r6;
        r0 = r144;
        r0.backgroundWidth = r4;
        r0 = r145;
        r4 = r0.textHeight;
        r6 = 1100742656; // 0x419c0000 float:19.5 double:5.43839131E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r0 = r144;
        r6 = r0.namesOffset;
        r4 = r4 + r6;
        r0 = r144;
        r0.totalHeight = r4;
        r0 = r144;
        r4 = r0.drawPinnedTop;
        if (r4 == 0) goto L_0x058a;
    L_0x057b:
        r0 = r144;
        r4 = r0.namesOffset;
        r6 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r0 = r144;
        r0.namesOffset = r4;
    L_0x058a:
        r0 = r144;
        r4 = r0.backgroundWidth;
        r0 = r144;
        r6 = r0.nameWidth;
        r91 = java.lang.Math.max(r4, r6);
        r0 = r144;
        r4 = r0.forwardedNameWidth;
        r0 = r91;
        r91 = java.lang.Math.max(r0, r4);
        r0 = r144;
        r4 = r0.replyNameWidth;
        r0 = r91;
        r91 = java.lang.Math.max(r0, r4);
        r0 = r144;
        r4 = r0.replyTextWidth;
        r0 = r91;
        r91 = java.lang.Math.max(r0, r4);
        r94 = 0;
        r0 = r144;
        r4 = r0.hasLinkPreview;
        if (r4 != 0) goto L_0x05c8;
    L_0x05bc:
        r0 = r144;
        r4 = r0.hasGamePreview;
        if (r4 != 0) goto L_0x05c8;
    L_0x05c2:
        r0 = r144;
        r4 = r0.hasInvoicePreview;
        if (r4 == 0) goto L_0x1c6d;
    L_0x05c8:
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 == 0) goto L_0x0a8f;
    L_0x05ce:
        r0 = r144;
        r4 = r0.isChat;
        if (r4 == 0) goto L_0x0a81;
    L_0x05d4:
        r4 = r145.needDrawAvatar();
        if (r4 == 0) goto L_0x0a81;
    L_0x05da:
        r0 = r144;
        r4 = r0.currentMessageObject;
        r4 = r4.isOut();
        if (r4 != 0) goto L_0x0a81;
    L_0x05e4:
        r4 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        r6 = 1124335616; // 0x43040000 float:132.0 double:5.554956023E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r11 = r4 - r6;
    L_0x05f0:
        r0 = r144;
        r4 = r0.drawShareButton;
        if (r4 == 0) goto L_0x05fd;
    L_0x05f6:
        r4 = 1101004800; // 0x41a00000 float:20.0 double:5.439686476E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r11 = r11 - r4;
    L_0x05fd:
        r0 = r144;
        r4 = r0.hasLinkPreview;
        if (r4 == 0) goto L_0x0ad8;
    L_0x0603:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r0 = r4.webpage;
        r139 = r0;
        r139 = (org.telegram.tgnet.TLRPC.TL_webPage) r139;
        r0 = r139;
        r7 = r0.site_name;
        r0 = r139;
        r0 = r0.title;
        r132 = r0;
        r0 = r139;
        r0 = r0.author;
        r47 = r0;
        r0 = r139;
        r0 = r0.description;
        r64 = r0;
        r0 = r139;
        r0 = r0.photo;
        r106 = r0;
        r15 = 0;
        r0 = r139;
        r0 = r0.document;
        r66 = r0;
        r0 = r139;
        r0 = r0.type;
        r134 = r0;
        r0 = r139;
        r0 = r0.duration;
        r67 = r0;
        if (r7 == 0) goto L_0x065f;
    L_0x0640:
        if (r106 == 0) goto L_0x065f;
    L_0x0642:
        r4 = r7.toLowerCase();
        r6 = "instagram";
        r4 = r4.equals(r6);
        if (r4 == 0) goto L_0x065f;
    L_0x064f:
        r4 = org.telegram.messenger.AndroidUtilities.displaySize;
        r4 = r4.y;
        r4 = r4 / 3;
        r0 = r144;
        r6 = r0.currentMessageObject;
        r6 = r6.textWidth;
        r11 = java.lang.Math.max(r4, r6);
    L_0x065f:
        if (r124 != 0) goto L_0x0ad1;
    L_0x0661:
        r0 = r144;
        r4 = r0.drawInstantView;
        if (r4 != 0) goto L_0x0ad1;
    L_0x0667:
        if (r66 != 0) goto L_0x0ad1;
    L_0x0669:
        if (r134 == 0) goto L_0x0ad1;
    L_0x066b:
        r4 = "app";
        r0 = r134;
        r4 = r0.equals(r4);
        if (r4 != 0) goto L_0x068c;
    L_0x0676:
        r4 = "profile";
        r0 = r134;
        r4 = r0.equals(r4);
        if (r4 != 0) goto L_0x068c;
    L_0x0681:
        r4 = "article";
        r0 = r134;
        r4 = r0.equals(r4);
        if (r4 == 0) goto L_0x0ad1;
    L_0x068c:
        r125 = 1;
    L_0x068e:
        if (r124 != 0) goto L_0x0ad5;
    L_0x0690:
        r0 = r144;
        r4 = r0.drawInstantView;
        if (r4 != 0) goto L_0x0ad5;
    L_0x0696:
        if (r66 != 0) goto L_0x0ad5;
    L_0x0698:
        if (r64 == 0) goto L_0x0ad5;
    L_0x069a:
        if (r134 == 0) goto L_0x0ad5;
    L_0x069c:
        r4 = "app";
        r0 = r134;
        r4 = r0.equals(r4);
        if (r4 != 0) goto L_0x06bd;
    L_0x06a7:
        r4 = "profile";
        r0 = r134;
        r4 = r0.equals(r4);
        if (r4 != 0) goto L_0x06bd;
    L_0x06b2:
        r4 = "article";
        r0 = r134;
        r4 = r0.equals(r4);
        if (r4 == 0) goto L_0x0ad5;
    L_0x06bd:
        r0 = r144;
        r4 = r0.currentMessageObject;
        r4 = r4.photoThumbs;
        if (r4 == 0) goto L_0x0ad5;
    L_0x06c5:
        r4 = 1;
    L_0x06c6:
        r0 = r144;
        r0.isSmallImage = r4;
        r138 = r15;
    L_0x06cc:
        r0 = r144;
        r4 = r0.hasInvoicePreview;
        if (r4 == 0) goto L_0x0b5e;
    L_0x06d2:
        r41 = 0;
    L_0x06d4:
        r116 = 3;
        r43 = 0;
        r86 = r11 - r41;
        r0 = r144;
        r4 = r0.currentMessageObject;
        r4 = r4.photoThumbs;
        if (r4 != 0) goto L_0x06ec;
    L_0x06e2:
        if (r106 == 0) goto L_0x06ec;
    L_0x06e4:
        r0 = r144;
        r4 = r0.currentMessageObject;
        r6 = 1;
        r4.generateThumbs(r6);
    L_0x06ec:
        if (r7 == 0) goto L_0x0771;
    L_0x06ee:
        r4 = org.telegram.ui.ActionBar.Theme.chat_replyNamePaint;	 Catch:{ Exception -> 0x0b69 }
        r4 = r4.measureText(r7);	 Catch:{ Exception -> 0x0b69 }
        r6 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r4 = r4 + r6;
        r8 = (double) r4;	 Catch:{ Exception -> 0x0b69 }
        r8 = java.lang.Math.ceil(r8);	 Catch:{ Exception -> 0x0b69 }
        r0 = (int) r8;	 Catch:{ Exception -> 0x0b69 }
        r141 = r0;
        r6 = new android.text.StaticLayout;	 Catch:{ Exception -> 0x0b69 }
        r8 = org.telegram.ui.ActionBar.Theme.chat_replyNamePaint;	 Catch:{ Exception -> 0x0b69 }
        r0 = r141;
        r1 = r86;
        r9 = java.lang.Math.min(r0, r1);	 Catch:{ Exception -> 0x0b69 }
        r10 = android.text.Layout.Alignment.ALIGN_NORMAL;	 Catch:{ Exception -> 0x0b69 }
        r11 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r12 = 0;
        r13 = 0;
        r6.<init>(r7, r8, r9, r10, r11, r12, r13);	 Catch:{ Exception -> 0x0b69 }
        r0 = r144;
        r0.siteNameLayout = r6;	 Catch:{ Exception -> 0x0b69 }
        r0 = r144;
        r4 = r0.siteNameLayout;	 Catch:{ Exception -> 0x0b69 }
        r6 = 0;
        r4 = r4.getLineLeft(r6);	 Catch:{ Exception -> 0x0b69 }
        r6 = 0;
        r4 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1));
        if (r4 == 0) goto L_0x0b66;
    L_0x0726:
        r4 = 1;
    L_0x0727:
        r0 = r144;
        r0.siteNameRtl = r4;	 Catch:{ Exception -> 0x0b69 }
        r0 = r144;
        r4 = r0.siteNameLayout;	 Catch:{ Exception -> 0x0b69 }
        r0 = r144;
        r6 = r0.siteNameLayout;	 Catch:{ Exception -> 0x0b69 }
        r6 = r6.getLineCount();	 Catch:{ Exception -> 0x0b69 }
        r6 = r6 + -1;
        r78 = r4.getLineBottom(r6);	 Catch:{ Exception -> 0x0b69 }
        r0 = r144;
        r4 = r0.linkPreviewHeight;	 Catch:{ Exception -> 0x0b69 }
        r4 = r4 + r78;
        r0 = r144;
        r0.linkPreviewHeight = r4;	 Catch:{ Exception -> 0x0b69 }
        r0 = r144;
        r4 = r0.totalHeight;	 Catch:{ Exception -> 0x0b69 }
        r4 = r4 + r78;
        r0 = r144;
        r0.totalHeight = r4;	 Catch:{ Exception -> 0x0b69 }
        r43 = r43 + r78;
        r0 = r144;
        r4 = r0.siteNameLayout;	 Catch:{ Exception -> 0x0b69 }
        r141 = r4.getWidth();	 Catch:{ Exception -> 0x0b69 }
        r0 = r141;
        r1 = r144;
        r1.siteNameWidth = r0;	 Catch:{ Exception -> 0x0b69 }
        r4 = r141 + r41;
        r0 = r91;
        r91 = java.lang.Math.max(r0, r4);	 Catch:{ Exception -> 0x0b69 }
        r4 = r141 + r41;
        r0 = r94;
        r94 = java.lang.Math.max(r0, r4);	 Catch:{ Exception -> 0x0b69 }
    L_0x0771:
        r133 = 0;
        if (r132 == 0) goto L_0x3ceb;
    L_0x0775:
        r4 = 2147483647; // 0x7fffffff float:NaN double:1.060997895E-314;
        r0 = r144;
        r0.titleX = r4;	 Catch:{ Exception -> 0x3cd4 }
        r0 = r144;
        r4 = r0.linkPreviewHeight;	 Catch:{ Exception -> 0x3cd4 }
        if (r4 == 0) goto L_0x07a0;
    L_0x0782:
        r0 = r144;
        r4 = r0.linkPreviewHeight;	 Catch:{ Exception -> 0x3cd4 }
        r6 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);	 Catch:{ Exception -> 0x3cd4 }
        r4 = r4 + r6;
        r0 = r144;
        r0.linkPreviewHeight = r4;	 Catch:{ Exception -> 0x3cd4 }
        r0 = r144;
        r4 = r0.totalHeight;	 Catch:{ Exception -> 0x3cd4 }
        r6 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);	 Catch:{ Exception -> 0x3cd4 }
        r4 = r4 + r6;
        r0 = r144;
        r0.totalHeight = r4;	 Catch:{ Exception -> 0x3cd4 }
    L_0x07a0:
        r113 = 0;
        r0 = r144;
        r4 = r0.isSmallImage;	 Catch:{ Exception -> 0x3cd4 }
        if (r4 == 0) goto L_0x07aa;
    L_0x07a8:
        if (r64 != 0) goto L_0x0b6f;
    L_0x07aa:
        r9 = org.telegram.ui.ActionBar.Theme.chat_replyNamePaint;	 Catch:{ Exception -> 0x3cd4 }
        r11 = android.text.Layout.Alignment.ALIGN_NORMAL;	 Catch:{ Exception -> 0x3cd4 }
        r12 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r4 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);	 Catch:{ Exception -> 0x3cd4 }
        r13 = (float) r4;	 Catch:{ Exception -> 0x3cd4 }
        r14 = 0;
        r15 = android.text.TextUtils.TruncateAt.END;	 Catch:{ Exception -> 0x3cd4 }
        r17 = 4;
        r8 = r132;
        r10 = r86;
        r16 = r86;
        r4 = org.telegram.ui.Components.StaticLayoutEx.createStaticLayout(r8, r9, r10, r11, r12, r13, r14, r15, r16, r17);	 Catch:{ Exception -> 0x3cd4 }
        r0 = r144;
        r0.titleLayout = r4;	 Catch:{ Exception -> 0x3cd4 }
        r13 = r116;
    L_0x07cc:
        r0 = r144;
        r4 = r0.titleLayout;	 Catch:{ Exception -> 0x0ba7 }
        r0 = r144;
        r6 = r0.titleLayout;	 Catch:{ Exception -> 0x0ba7 }
        r6 = r6.getLineCount();	 Catch:{ Exception -> 0x0ba7 }
        r6 = r6 + -1;
        r78 = r4.getLineBottom(r6);	 Catch:{ Exception -> 0x0ba7 }
        r0 = r144;
        r4 = r0.linkPreviewHeight;	 Catch:{ Exception -> 0x0ba7 }
        r4 = r4 + r78;
        r0 = r144;
        r0.linkPreviewHeight = r4;	 Catch:{ Exception -> 0x0ba7 }
        r0 = r144;
        r4 = r0.totalHeight;	 Catch:{ Exception -> 0x0ba7 }
        r4 = r4 + r78;
        r0 = r144;
        r0.totalHeight = r4;	 Catch:{ Exception -> 0x0ba7 }
        r58 = 1;
        r40 = 0;
    L_0x07f6:
        r0 = r144;
        r4 = r0.titleLayout;	 Catch:{ Exception -> 0x0ba7 }
        r4 = r4.getLineCount();	 Catch:{ Exception -> 0x0ba7 }
        r0 = r40;
        if (r0 >= r4) goto L_0x0bab;
    L_0x0802:
        r0 = r144;
        r4 = r0.titleLayout;	 Catch:{ Exception -> 0x0ba7 }
        r0 = r40;
        r4 = r4.getLineLeft(r0);	 Catch:{ Exception -> 0x0ba7 }
        r0 = (int) r4;	 Catch:{ Exception -> 0x0ba7 }
        r85 = r0;
        if (r85 == 0) goto L_0x0813;
    L_0x0811:
        r133 = 1;
    L_0x0813:
        r0 = r144;
        r4 = r0.titleX;	 Catch:{ Exception -> 0x0ba7 }
        r6 = 2147483647; // 0x7fffffff float:NaN double:1.060997895E-314;
        if (r4 != r6) goto L_0x0b96;
    L_0x081c:
        r0 = r85;
        r4 = -r0;
        r0 = r144;
        r0.titleX = r4;	 Catch:{ Exception -> 0x0ba7 }
    L_0x0823:
        if (r85 == 0) goto L_0x0d26;
    L_0x0825:
        r0 = r144;
        r4 = r0.titleLayout;	 Catch:{ Exception -> 0x0ba7 }
        r4 = r4.getWidth();	 Catch:{ Exception -> 0x0ba7 }
        r141 = r4 - r85;
    L_0x082f:
        r0 = r40;
        r1 = r113;
        if (r0 < r1) goto L_0x083d;
    L_0x0835:
        if (r85 == 0) goto L_0x0845;
    L_0x0837:
        r0 = r144;
        r4 = r0.isSmallImage;	 Catch:{ Exception -> 0x0ba7 }
        if (r4 == 0) goto L_0x0845;
    L_0x083d:
        r4 = 1112539136; // 0x42500000 float:52.0 double:5.496673668E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);	 Catch:{ Exception -> 0x0ba7 }
        r141 = r141 + r4;
    L_0x0845:
        r4 = r141 + r41;
        r0 = r91;
        r91 = java.lang.Math.max(r0, r4);	 Catch:{ Exception -> 0x0ba7 }
        r4 = r141 + r41;
        r0 = r94;
        r94 = java.lang.Math.max(r0, r4);	 Catch:{ Exception -> 0x0ba7 }
        r40 = r40 + 1;
        goto L_0x07f6;
    L_0x0858:
        r97 = 0;
        goto L_0x0031;
    L_0x085c:
        r96 = 0;
        goto L_0x0041;
    L_0x0860:
        r63 = 0;
        goto L_0x0078;
    L_0x0864:
        r74 = 0;
        goto L_0x0082;
    L_0x0868:
        r99 = 0;
        goto L_0x00a1;
    L_0x086c:
        r74 = 0;
        goto L_0x00ab;
    L_0x0870:
        r4 = 0;
        r0 = r144;
        r0.currentMessagesGroup = r4;
        r4 = 0;
        r0 = r144;
        r0.currentPosition = r4;
        goto L_0x0120;
    L_0x087c:
        r4 = 0;
        goto L_0x0137;
    L_0x087f:
        r4 = 0;
        goto L_0x0152;
    L_0x0882:
        r4 = 0;
        goto L_0x01b1;
    L_0x0885:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.to_id;
        r4 = r4.channel_id;
        if (r4 == 0) goto L_0x08a8;
    L_0x088f:
        r4 = r145.isOutOwner();
        if (r4 != 0) goto L_0x08a8;
    L_0x0895:
        r4 = 1;
    L_0x0896:
        r0 = r144;
        r0.drawName = r4;
        r4 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        r6 = 1117782016; // 0x42a00000 float:80.0 double:5.522576936E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r95 = r4 - r6;
        goto L_0x03cb;
    L_0x08a8:
        r4 = 0;
        goto L_0x0896;
    L_0x08aa:
        r0 = r144;
        r4 = r0.isChat;
        if (r4 == 0) goto L_0x08d7;
    L_0x08b0:
        r4 = r145.isOutOwner();
        if (r4 != 0) goto L_0x08d7;
    L_0x08b6:
        r4 = r145.needDrawAvatar();
        if (r4 == 0) goto L_0x08d7;
    L_0x08bc:
        r4 = org.telegram.messenger.AndroidUtilities.displaySize;
        r4 = r4.x;
        r6 = org.telegram.messenger.AndroidUtilities.displaySize;
        r6 = r6.y;
        r4 = java.lang.Math.min(r4, r6);
        r6 = 1123287040; // 0x42f40000 float:122.0 double:5.54977537E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r95 = r4 - r6;
        r4 = 1;
        r0 = r144;
        r0.drawName = r4;
        goto L_0x03cb;
    L_0x08d7:
        r4 = org.telegram.messenger.AndroidUtilities.displaySize;
        r4 = r4.x;
        r6 = org.telegram.messenger.AndroidUtilities.displaySize;
        r6 = r6.y;
        r4 = java.lang.Math.min(r4, r6);
        r6 = 1117782016; // 0x42a00000 float:80.0 double:5.522576936E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r95 = r4 - r6;
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.to_id;
        r4 = r4.channel_id;
        if (r4 == 0) goto L_0x0902;
    L_0x08f5:
        r4 = r145.isOutOwner();
        if (r4 != 0) goto L_0x0902;
    L_0x08fb:
        r4 = 1;
    L_0x08fc:
        r0 = r144;
        r0.drawName = r4;
        goto L_0x03cb;
    L_0x0902:
        r4 = 0;
        goto L_0x08fc;
    L_0x0904:
        r4 = 1115684864; // 0x42800000 float:64.0 double:5.51221563E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        goto L_0x03f3;
    L_0x090c:
        r4 = 0;
        goto L_0x0433;
    L_0x090f:
        r4 = 0;
        goto L_0x045a;
    L_0x0912:
        r4 = 0;
        goto L_0x0471;
    L_0x0915:
        r123 = 0;
        goto L_0x0489;
    L_0x0919:
        r140 = 0;
        goto L_0x049b;
    L_0x091d:
        r4 = "telegram_megagroup";
        r0 = r140;
        r4 = r4.equals(r0);
        if (r4 == 0) goto L_0x0934;
    L_0x0928:
        r4 = 1;
        r0 = r144;
        r0.drawInstantView = r4;
        r4 = 2;
        r0 = r144;
        r0.drawInstantViewType = r4;
        goto L_0x04b6;
    L_0x0934:
        r4 = "telegram_message";
        r0 = r140;
        r4 = r4.equals(r0);
        if (r4 == 0) goto L_0x04b6;
    L_0x093f:
        r4 = 1;
        r0 = r144;
        r0.drawInstantView = r4;
        r4 = 3;
        r0 = r144;
        r0.drawInstantViewType = r4;
        goto L_0x04b6;
    L_0x094b:
        if (r123 == 0) goto L_0x04b6;
    L_0x094d:
        r123 = r123.toLowerCase();
        r4 = "instagram";
        r0 = r123;
        r4 = r0.equals(r4);
        if (r4 != 0) goto L_0x0972;
    L_0x095c:
        r4 = "twitter";
        r0 = r123;
        r4 = r0.equals(r4);
        if (r4 != 0) goto L_0x0972;
    L_0x0967:
        r4 = "telegram_album";
        r0 = r140;
        r4 = r4.equals(r0);
        if (r4 == 0) goto L_0x04b6;
    L_0x0972:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r4 = r4.webpage;
        r4 = r4.cached_page;
        r4 = r4 instanceof org.telegram.tgnet.TLRPC.TL_pageFull;
        if (r4 == 0) goto L_0x04b6;
    L_0x0980:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r4 = r4.webpage;
        r4 = r4.photo;
        r4 = r4 instanceof org.telegram.tgnet.TLRPC.TL_photo;
        if (r4 != 0) goto L_0x099e;
    L_0x098e:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r4 = r4.webpage;
        r4 = r4.document;
        r4 = org.telegram.messenger.MessageObject.isVideoDocument(r4);
        if (r4 == 0) goto L_0x04b6;
    L_0x099e:
        r4 = 0;
        r0 = r144;
        r0.drawInstantView = r4;
        r124 = 1;
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r4 = r4.webpage;
        r4 = r4.cached_page;
        r0 = r4.blocks;
        r52 = r0;
        r59 = 1;
        r40 = 0;
    L_0x09b7:
        r4 = r52.size();
        r0 = r40;
        if (r0 >= r4) goto L_0x09f1;
    L_0x09bf:
        r0 = r52;
        r1 = r40;
        r51 = r0.get(r1);
        r51 = (org.telegram.tgnet.TLRPC.PageBlock) r51;
        r0 = r51;
        r4 = r0 instanceof org.telegram.tgnet.TLRPC.TL_pageBlockSlideshow;
        if (r4 == 0) goto L_0x09de;
    L_0x09cf:
        r50 = r51;
        r50 = (org.telegram.tgnet.TLRPC.TL_pageBlockSlideshow) r50;
        r0 = r50;
        r4 = r0.items;
        r59 = r4.size();
    L_0x09db:
        r40 = r40 + 1;
        goto L_0x09b7;
    L_0x09de:
        r0 = r51;
        r4 = r0 instanceof org.telegram.tgnet.TLRPC.TL_pageBlockCollage;
        if (r4 == 0) goto L_0x09db;
    L_0x09e4:
        r50 = r51;
        r50 = (org.telegram.tgnet.TLRPC.TL_pageBlockCollage) r50;
        r0 = r50;
        r4 = r0.items;
        r59 = r4.size();
        goto L_0x09db;
    L_0x09f1:
        r4 = "Of";
        r6 = 2131494094; // 0x7f0c04ce float:1.8611687E38 double:1.053098006E-314;
        r8 = 2;
        r8 = new java.lang.Object[r8];
        r9 = 0;
        r10 = 1;
        r10 = java.lang.Integer.valueOf(r10);
        r8[r9] = r10;
        r9 = 1;
        r10 = java.lang.Integer.valueOf(r59);
        r8[r9] = r10;
        r5 = org.telegram.messenger.LocaleController.formatString(r4, r6, r8);
        r4 = org.telegram.ui.ActionBar.Theme.chat_durationPaint;
        r4 = r4.measureText(r5);
        r8 = (double) r4;
        r8 = java.lang.Math.ceil(r8);
        r4 = (int) r8;
        r0 = r144;
        r0.photosCountWidth = r4;
        r4 = new android.text.StaticLayout;
        r6 = org.telegram.ui.ActionBar.Theme.chat_durationPaint;
        r0 = r144;
        r7 = r0.photosCountWidth;
        r8 = android.text.Layout.Alignment.ALIGN_NORMAL;
        r9 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r10 = 0;
        r11 = 0;
        r4.<init>(r5, r6, r7, r8, r9, r10, r11);
        r0 = r144;
        r0.photosCountLayout = r4;
        goto L_0x04b6;
    L_0x0a34:
        r0 = r144;
        r4 = r0.backgroundWidth;
        r0 = r145;
        r6 = r0.lastLineWidth;
        r65 = r4 - r6;
        if (r65 < 0) goto L_0x0a5b;
    L_0x0a40:
        r0 = r65;
        r1 = r130;
        if (r0 > r1) goto L_0x0a5b;
    L_0x0a46:
        r0 = r144;
        r4 = r0.backgroundWidth;
        r4 = r4 + r130;
        r4 = r4 - r65;
        r6 = 1106771968; // 0x41f80000 float:31.0 double:5.46818007E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r0 = r144;
        r0.backgroundWidth = r4;
        goto L_0x0506;
    L_0x0a5b:
        r0 = r144;
        r4 = r0.backgroundWidth;
        r0 = r145;
        r6 = r0.lastLineWidth;
        r6 = r6 + r130;
        r4 = java.lang.Math.max(r4, r6);
        r6 = 1106771968; // 0x41f80000 float:31.0 double:5.46818007E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r0 = r144;
        r0.backgroundWidth = r4;
        goto L_0x0506;
    L_0x0a76:
        r4 = 1115684864; // 0x42800000 float:64.0 double:5.51221563E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        goto L_0x0537;
    L_0x0a7e:
        r4 = 0;
        goto L_0x055c;
    L_0x0a81:
        r4 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        r6 = 1117782016; // 0x42a00000 float:80.0 double:5.522576936E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r11 = r4 - r6;
        goto L_0x05f0;
    L_0x0a8f:
        r0 = r144;
        r4 = r0.isChat;
        if (r4 == 0) goto L_0x0abb;
    L_0x0a95:
        r4 = r145.needDrawAvatar();
        if (r4 == 0) goto L_0x0abb;
    L_0x0a9b:
        r0 = r144;
        r4 = r0.currentMessageObject;
        r4 = r4.isOutOwner();
        if (r4 != 0) goto L_0x0abb;
    L_0x0aa5:
        r4 = org.telegram.messenger.AndroidUtilities.displaySize;
        r4 = r4.x;
        r6 = org.telegram.messenger.AndroidUtilities.displaySize;
        r6 = r6.y;
        r4 = java.lang.Math.min(r4, r6);
        r6 = 1124335616; // 0x43040000 float:132.0 double:5.554956023E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r11 = r4 - r6;
        goto L_0x05f0;
    L_0x0abb:
        r4 = org.telegram.messenger.AndroidUtilities.displaySize;
        r4 = r4.x;
        r6 = org.telegram.messenger.AndroidUtilities.displaySize;
        r6 = r6.y;
        r4 = java.lang.Math.min(r4, r6);
        r6 = 1117782016; // 0x42a00000 float:80.0 double:5.522576936E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r11 = r4 - r6;
        goto L_0x05f0;
    L_0x0ad1:
        r125 = 0;
        goto L_0x068e;
    L_0x0ad5:
        r4 = 0;
        goto L_0x06c6;
    L_0x0ad8:
        r0 = r144;
        r4 = r0.hasInvoicePreview;
        if (r4 == 0) goto L_0x0b1c;
    L_0x0ade:
        r0 = r145;
        r4 = r0.messageOwner;
        r0 = r4.media;
        r80 = r0;
        r80 = (org.telegram.tgnet.TLRPC.TL_messageMediaInvoice) r80;
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r7 = r4.title;
        r132 = 0;
        r64 = 0;
        r106 = 0;
        r47 = 0;
        r66 = 0;
        r0 = r80;
        r4 = r0.photo;
        r4 = r4 instanceof org.telegram.tgnet.TLRPC.TL_webDocument;
        if (r4 == 0) goto L_0x0b1a;
    L_0x0b02:
        r0 = r80;
        r4 = r0.photo;
        r15 = org.telegram.messenger.WebFile.createWithWebDocument(r4);
    L_0x0b0a:
        r67 = 0;
        r134 = "invoice";
        r4 = 0;
        r0 = r144;
        r0.isSmallImage = r4;
        r125 = 0;
        r138 = r15;
        goto L_0x06cc;
    L_0x0b1a:
        r15 = 0;
        goto L_0x0b0a;
    L_0x0b1c:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r0 = r4.game;
        r73 = r0;
        r0 = r73;
        r7 = r0.title;
        r132 = 0;
        r15 = 0;
        r0 = r145;
        r4 = r0.messageText;
        r4 = android.text.TextUtils.isEmpty(r4);
        if (r4 == 0) goto L_0x0b5b;
    L_0x0b37:
        r0 = r73;
        r0 = r0.description;
        r64 = r0;
    L_0x0b3d:
        r0 = r73;
        r0 = r0.photo;
        r106 = r0;
        r47 = 0;
        r0 = r73;
        r0 = r0.document;
        r66 = r0;
        r67 = 0;
        r134 = "game";
        r4 = 0;
        r0 = r144;
        r0.isSmallImage = r4;
        r125 = 0;
        r138 = r15;
        goto L_0x06cc;
    L_0x0b5b:
        r64 = 0;
        goto L_0x0b3d;
    L_0x0b5e:
        r4 = 1092616192; // 0x41200000 float:10.0 double:5.398241246E-315;
        r41 = org.telegram.messenger.AndroidUtilities.dp(r4);
        goto L_0x06d4;
    L_0x0b66:
        r4 = 0;
        goto L_0x0727;
    L_0x0b69:
        r69 = move-exception;
        org.telegram.messenger.FileLog.e(r69);
        goto L_0x0771;
    L_0x0b6f:
        r113 = r116;
        r9 = org.telegram.ui.ActionBar.Theme.chat_replyNamePaint;	 Catch:{ Exception -> 0x3cd4 }
        r4 = 1112539136; // 0x42500000 float:52.0 double:5.496673668E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);	 Catch:{ Exception -> 0x3cd4 }
        r11 = r86 - r4;
        r13 = 4;
        r8 = r132;
        r10 = r86;
        r12 = r116;
        r4 = generateStaticLayout(r8, r9, r10, r11, r12, r13);	 Catch:{ Exception -> 0x3cd4 }
        r0 = r144;
        r0.titleLayout = r4;	 Catch:{ Exception -> 0x3cd4 }
        r0 = r144;
        r4 = r0.titleLayout;	 Catch:{ Exception -> 0x3cd4 }
        r4 = r4.getLineCount();	 Catch:{ Exception -> 0x3cd4 }
        r13 = r116 - r4;
        goto L_0x07cc;
    L_0x0b96:
        r0 = r144;
        r4 = r0.titleX;	 Catch:{ Exception -> 0x0ba7 }
        r0 = r85;
        r6 = -r0;
        r4 = java.lang.Math.max(r4, r6);	 Catch:{ Exception -> 0x0ba7 }
        r0 = r144;
        r0.titleX = r4;	 Catch:{ Exception -> 0x0ba7 }
        goto L_0x0823;
    L_0x0ba7:
        r69 = move-exception;
    L_0x0ba8:
        org.telegram.messenger.FileLog.e(r69);
    L_0x0bab:
        if (r133 == 0) goto L_0x3ce5;
    L_0x0bad:
        r0 = r144;
        r4 = r0.isSmallImage;
        if (r4 == 0) goto L_0x3ce5;
    L_0x0bb3:
        r4 = 1111490560; // 0x42400000 float:48.0 double:5.491493014E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r11 = r86 - r4;
        r116 = r13;
    L_0x0bbd:
        r48 = 0;
        if (r47 == 0) goto L_0x3ce1;
    L_0x0bc1:
        if (r132 != 0) goto L_0x3ce1;
    L_0x0bc3:
        r0 = r144;
        r4 = r0.linkPreviewHeight;	 Catch:{ Exception -> 0x0d70 }
        if (r4 == 0) goto L_0x0be7;
    L_0x0bc9:
        r0 = r144;
        r4 = r0.linkPreviewHeight;	 Catch:{ Exception -> 0x0d70 }
        r6 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);	 Catch:{ Exception -> 0x0d70 }
        r4 = r4 + r6;
        r0 = r144;
        r0.linkPreviewHeight = r4;	 Catch:{ Exception -> 0x0d70 }
        r0 = r144;
        r4 = r0.totalHeight;	 Catch:{ Exception -> 0x0d70 }
        r6 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);	 Catch:{ Exception -> 0x0d70 }
        r4 = r4 + r6;
        r0 = r144;
        r0.totalHeight = r4;	 Catch:{ Exception -> 0x0d70 }
    L_0x0be7:
        r4 = 3;
        r0 = r116;
        if (r0 != r4) goto L_0x0d3a;
    L_0x0bec:
        r0 = r144;
        r4 = r0.isSmallImage;	 Catch:{ Exception -> 0x0d70 }
        if (r4 == 0) goto L_0x0bf4;
    L_0x0bf2:
        if (r64 != 0) goto L_0x0d3a;
    L_0x0bf4:
        r8 = new android.text.StaticLayout;	 Catch:{ Exception -> 0x0d70 }
        r10 = org.telegram.ui.ActionBar.Theme.chat_replyNamePaint;	 Catch:{ Exception -> 0x0d70 }
        r12 = android.text.Layout.Alignment.ALIGN_NORMAL;	 Catch:{ Exception -> 0x0d70 }
        r13 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r14 = 0;
        r15 = 0;
        r9 = r47;
        r8.<init>(r9, r10, r11, r12, r13, r14, r15);	 Catch:{ Exception -> 0x0d70 }
        r0 = r144;
        r0.authorLayout = r8;	 Catch:{ Exception -> 0x0d70 }
        r13 = r116;
    L_0x0c09:
        r0 = r144;
        r4 = r0.authorLayout;	 Catch:{ Exception -> 0x3cd1 }
        r0 = r144;
        r6 = r0.authorLayout;	 Catch:{ Exception -> 0x3cd1 }
        r6 = r6.getLineCount();	 Catch:{ Exception -> 0x3cd1 }
        r6 = r6 + -1;
        r78 = r4.getLineBottom(r6);	 Catch:{ Exception -> 0x3cd1 }
        r0 = r144;
        r4 = r0.linkPreviewHeight;	 Catch:{ Exception -> 0x3cd1 }
        r4 = r4 + r78;
        r0 = r144;
        r0.linkPreviewHeight = r4;	 Catch:{ Exception -> 0x3cd1 }
        r0 = r144;
        r4 = r0.totalHeight;	 Catch:{ Exception -> 0x3cd1 }
        r4 = r4 + r78;
        r0 = r144;
        r0.totalHeight = r4;	 Catch:{ Exception -> 0x3cd1 }
        r0 = r144;
        r4 = r0.authorLayout;	 Catch:{ Exception -> 0x3cd1 }
        r6 = 0;
        r4 = r4.getLineLeft(r6);	 Catch:{ Exception -> 0x3cd1 }
        r0 = (int) r4;	 Catch:{ Exception -> 0x3cd1 }
        r85 = r0;
        r0 = r85;
        r4 = -r0;
        r0 = r144;
        r0.authorX = r4;	 Catch:{ Exception -> 0x3cd1 }
        if (r85 == 0) goto L_0x0d5d;
    L_0x0c44:
        r0 = r144;
        r4 = r0.authorLayout;	 Catch:{ Exception -> 0x3cd1 }
        r4 = r4.getWidth();	 Catch:{ Exception -> 0x3cd1 }
        r141 = r4 - r85;
        r48 = 1;
    L_0x0c50:
        r4 = r141 + r41;
        r0 = r91;
        r91 = java.lang.Math.max(r0, r4);	 Catch:{ Exception -> 0x3cd1 }
        r4 = r141 + r41;
        r0 = r94;
        r94 = java.lang.Math.max(r0, r4);	 Catch:{ Exception -> 0x3cd1 }
    L_0x0c60:
        if (r64 == 0) goto L_0x0d9e;
    L_0x0c62:
        r4 = 0;
        r0 = r144;
        r0.descriptionX = r4;	 Catch:{ Exception -> 0x0d9a }
        r0 = r144;
        r4 = r0.currentMessageObject;	 Catch:{ Exception -> 0x0d9a }
        r4.generateLinkDescription();	 Catch:{ Exception -> 0x0d9a }
        r0 = r144;
        r4 = r0.linkPreviewHeight;	 Catch:{ Exception -> 0x0d9a }
        if (r4 == 0) goto L_0x0c92;
    L_0x0c74:
        r0 = r144;
        r4 = r0.linkPreviewHeight;	 Catch:{ Exception -> 0x0d9a }
        r6 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);	 Catch:{ Exception -> 0x0d9a }
        r4 = r4 + r6;
        r0 = r144;
        r0.linkPreviewHeight = r4;	 Catch:{ Exception -> 0x0d9a }
        r0 = r144;
        r4 = r0.totalHeight;	 Catch:{ Exception -> 0x0d9a }
        r6 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);	 Catch:{ Exception -> 0x0d9a }
        r4 = r4 + r6;
        r0 = r144;
        r0.totalHeight = r4;	 Catch:{ Exception -> 0x0d9a }
    L_0x0c92:
        r113 = 0;
        r4 = r7.toLowerCase();	 Catch:{ Exception -> 0x0d9a }
        r6 = "twitter";
        r45 = r4.equals(r6);	 Catch:{ Exception -> 0x0d9a }
        r4 = 3;
        if (r13 != r4) goto L_0x0d7c;
    L_0x0ca2:
        r0 = r144;
        r4 = r0.isSmallImage;	 Catch:{ Exception -> 0x0d9a }
        if (r4 != 0) goto L_0x0d7c;
    L_0x0ca8:
        r0 = r145;
        r9 = r0.linkDescription;	 Catch:{ Exception -> 0x0d9a }
        r10 = org.telegram.ui.ActionBar.Theme.chat_replyTextPaint;	 Catch:{ Exception -> 0x0d9a }
        r12 = android.text.Layout.Alignment.ALIGN_NORMAL;	 Catch:{ Exception -> 0x0d9a }
        r13 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r4 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);	 Catch:{ Exception -> 0x0d9a }
        r14 = (float) r4;	 Catch:{ Exception -> 0x0d9a }
        r15 = 0;
        r16 = android.text.TextUtils.TruncateAt.END;	 Catch:{ Exception -> 0x0d9a }
        if (r45 == 0) goto L_0x0d78;
    L_0x0cbe:
        r18 = 100;
    L_0x0cc0:
        r17 = r11;
        r4 = org.telegram.ui.Components.StaticLayoutEx.createStaticLayout(r9, r10, r11, r12, r13, r14, r15, r16, r17, r18);	 Catch:{ Exception -> 0x0d9a }
        r0 = r144;
        r0.descriptionLayout = r4;	 Catch:{ Exception -> 0x0d9a }
    L_0x0cca:
        r0 = r144;
        r4 = r0.descriptionLayout;	 Catch:{ Exception -> 0x0d9a }
        r0 = r144;
        r6 = r0.descriptionLayout;	 Catch:{ Exception -> 0x0d9a }
        r6 = r6.getLineCount();	 Catch:{ Exception -> 0x0d9a }
        r6 = r6 + -1;
        r78 = r4.getLineBottom(r6);	 Catch:{ Exception -> 0x0d9a }
        r0 = r144;
        r4 = r0.linkPreviewHeight;	 Catch:{ Exception -> 0x0d9a }
        r4 = r4 + r78;
        r0 = r144;
        r0.linkPreviewHeight = r4;	 Catch:{ Exception -> 0x0d9a }
        r0 = r144;
        r4 = r0.totalHeight;	 Catch:{ Exception -> 0x0d9a }
        r4 = r4 + r78;
        r0 = r144;
        r0.totalHeight = r4;	 Catch:{ Exception -> 0x0d9a }
        r77 = 0;
        r40 = 0;
    L_0x0cf4:
        r0 = r144;
        r4 = r0.descriptionLayout;	 Catch:{ Exception -> 0x0d9a }
        r4 = r4.getLineCount();	 Catch:{ Exception -> 0x0d9a }
        r0 = r40;
        if (r0 >= r4) goto L_0x135f;
    L_0x0d00:
        r0 = r144;
        r4 = r0.descriptionLayout;	 Catch:{ Exception -> 0x0d9a }
        r0 = r40;
        r4 = r4.getLineLeft(r0);	 Catch:{ Exception -> 0x0d9a }
        r8 = (double) r4;	 Catch:{ Exception -> 0x0d9a }
        r8 = java.lang.Math.ceil(r8);	 Catch:{ Exception -> 0x0d9a }
        r0 = (int) r8;	 Catch:{ Exception -> 0x0d9a }
        r85 = r0;
        if (r85 == 0) goto L_0x0d23;
    L_0x0d14:
        r77 = 1;
        r0 = r144;
        r4 = r0.descriptionX;	 Catch:{ Exception -> 0x0d9a }
        if (r4 != 0) goto L_0x134e;
    L_0x0d1c:
        r0 = r85;
        r4 = -r0;
        r0 = r144;
        r0.descriptionX = r4;	 Catch:{ Exception -> 0x0d9a }
    L_0x0d23:
        r40 = r40 + 1;
        goto L_0x0cf4;
    L_0x0d26:
        r0 = r144;
        r4 = r0.titleLayout;	 Catch:{ Exception -> 0x0ba7 }
        r0 = r40;
        r4 = r4.getLineWidth(r0);	 Catch:{ Exception -> 0x0ba7 }
        r8 = (double) r4;	 Catch:{ Exception -> 0x0ba7 }
        r8 = java.lang.Math.ceil(r8);	 Catch:{ Exception -> 0x0ba7 }
        r0 = (int) r8;
        r141 = r0;
        goto L_0x082f;
    L_0x0d3a:
        r10 = org.telegram.ui.ActionBar.Theme.chat_replyNamePaint;	 Catch:{ Exception -> 0x0d70 }
        r4 = 1112539136; // 0x42500000 float:52.0 double:5.496673668E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);	 Catch:{ Exception -> 0x0d70 }
        r12 = r11 - r4;
        r14 = 1;
        r9 = r47;
        r13 = r116;
        r4 = generateStaticLayout(r9, r10, r11, r12, r13, r14);	 Catch:{ Exception -> 0x0d70 }
        r0 = r144;
        r0.authorLayout = r4;	 Catch:{ Exception -> 0x0d70 }
        r0 = r144;
        r4 = r0.authorLayout;	 Catch:{ Exception -> 0x0d70 }
        r4 = r4.getLineCount();	 Catch:{ Exception -> 0x0d70 }
        r13 = r116 - r4;
        goto L_0x0c09;
    L_0x0d5d:
        r0 = r144;
        r4 = r0.authorLayout;	 Catch:{ Exception -> 0x3cd1 }
        r6 = 0;
        r4 = r4.getLineWidth(r6);	 Catch:{ Exception -> 0x3cd1 }
        r8 = (double) r4;	 Catch:{ Exception -> 0x3cd1 }
        r8 = java.lang.Math.ceil(r8);	 Catch:{ Exception -> 0x3cd1 }
        r0 = (int) r8;
        r141 = r0;
        goto L_0x0c50;
    L_0x0d70:
        r69 = move-exception;
        r13 = r116;
    L_0x0d73:
        org.telegram.messenger.FileLog.e(r69);
        goto L_0x0c60;
    L_0x0d78:
        r18 = 6;
        goto L_0x0cc0;
    L_0x0d7c:
        r113 = r13;
        r0 = r145;
        r9 = r0.linkDescription;	 Catch:{ Exception -> 0x0d9a }
        r10 = org.telegram.ui.ActionBar.Theme.chat_replyTextPaint;	 Catch:{ Exception -> 0x0d9a }
        r4 = 1112539136; // 0x42500000 float:52.0 double:5.496673668E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);	 Catch:{ Exception -> 0x0d9a }
        r12 = r11 - r4;
        if (r45 == 0) goto L_0x134b;
    L_0x0d8e:
        r14 = 100;
    L_0x0d90:
        r4 = generateStaticLayout(r9, r10, r11, r12, r13, r14);	 Catch:{ Exception -> 0x0d9a }
        r0 = r144;
        r0.descriptionLayout = r4;	 Catch:{ Exception -> 0x0d9a }
        goto L_0x0cca;
    L_0x0d9a:
        r69 = move-exception;
        org.telegram.messenger.FileLog.e(r69);
    L_0x0d9e:
        if (r125 == 0) goto L_0x0dbe;
    L_0x0da0:
        r0 = r144;
        r4 = r0.descriptionLayout;
        if (r4 == 0) goto L_0x0db7;
    L_0x0da6:
        r0 = r144;
        r4 = r0.descriptionLayout;
        if (r4 == 0) goto L_0x0dbe;
    L_0x0dac:
        r0 = r144;
        r4 = r0.descriptionLayout;
        r4 = r4.getLineCount();
        r6 = 1;
        if (r4 != r6) goto L_0x0dbe;
    L_0x0db7:
        r125 = 0;
        r4 = 0;
        r0 = r144;
        r0.isSmallImage = r4;
    L_0x0dbe:
        if (r125 == 0) goto L_0x13fd;
    L_0x0dc0:
        r4 = 1111490560; // 0x42400000 float:48.0 double:5.491493014E-315;
        r93 = org.telegram.messenger.AndroidUtilities.dp(r4);
    L_0x0dc6:
        if (r66 == 0) goto L_0x1857;
    L_0x0dc8:
        r4 = org.telegram.messenger.MessageObject.isRoundVideoDocument(r66);
        if (r4 == 0) goto L_0x1401;
    L_0x0dce:
        r0 = r66;
        r4 = r0.thumb;
        r0 = r144;
        r0.currentPhotoObject = r4;
        r0 = r66;
        r1 = r144;
        r1.documentAttach = r0;
        r4 = 7;
        r0 = r144;
        r0.documentAttachType = r4;
        r15 = r138;
    L_0x0de3:
        r0 = r144;
        r4 = r0.documentAttachType;
        r6 = 5;
        if (r4 == r6) goto L_0x10f3;
    L_0x0dea:
        r0 = r144;
        r4 = r0.documentAttachType;
        r6 = 3;
        if (r4 == r6) goto L_0x10f3;
    L_0x0df1:
        r0 = r144;
        r4 = r0.documentAttachType;
        r6 = 1;
        if (r4 == r6) goto L_0x10f3;
    L_0x0df8:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        if (r4 != 0) goto L_0x0e00;
    L_0x0dfe:
        if (r15 == 0) goto L_0x1c08;
    L_0x0e00:
        if (r134 == 0) goto L_0x18c7;
    L_0x0e02:
        r4 = "photo";
        r0 = r134;
        r4 = r0.equals(r4);
        if (r4 != 0) goto L_0x0e31;
    L_0x0e0d:
        r4 = "document";
        r0 = r134;
        r4 = r0.equals(r4);
        if (r4 == 0) goto L_0x0e1f;
    L_0x0e18:
        r0 = r144;
        r4 = r0.documentAttachType;
        r6 = 6;
        if (r4 != r6) goto L_0x0e31;
    L_0x0e1f:
        r4 = "gif";
        r0 = r134;
        r4 = r0.equals(r4);
        if (r4 != 0) goto L_0x0e31;
    L_0x0e2a:
        r0 = r144;
        r4 = r0.documentAttachType;
        r6 = 4;
        if (r4 != r6) goto L_0x18c7;
    L_0x0e31:
        r4 = 1;
    L_0x0e32:
        r0 = r144;
        r0.drawImageButton = r4;
        r0 = r144;
        r4 = r0.linkPreviewHeight;
        if (r4 == 0) goto L_0x0e5a;
    L_0x0e3c:
        r0 = r144;
        r4 = r0.linkPreviewHeight;
        r6 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r0 = r144;
        r0.linkPreviewHeight = r4;
        r0 = r144;
        r4 = r0.totalHeight;
        r6 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r0 = r144;
        r0.totalHeight = r4;
    L_0x0e5a:
        r0 = r144;
        r4 = r0.documentAttachType;
        r6 = 6;
        if (r4 != r6) goto L_0x18d7;
    L_0x0e61:
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 == 0) goto L_0x18ca;
    L_0x0e67:
        r4 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        r4 = (float) r4;
        r6 = 1056964608; // 0x3f000000 float:0.5 double:5.222099017E-315;
        r4 = r4 * r6;
        r0 = (int) r4;
        r93 = r0;
    L_0x0e72:
        r0 = r144;
        r4 = r0.hasInvoicePreview;
        if (r4 == 0) goto L_0x18ea;
    L_0x0e78:
        r4 = 1094713344; // 0x41400000 float:12.0 double:5.408602553E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
    L_0x0e7e:
        r4 = r93 - r4;
        r4 = r4 + r41;
        r0 = r91;
        r91 = java.lang.Math.max(r0, r4);
        r0 = r144;
        r4 = r0.currentPhotoObject;
        if (r4 == 0) goto L_0x18ed;
    L_0x0e8e:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r6 = -1;
        r4.size = r6;
        r0 = r144;
        r4 = r0.currentPhotoObjectThumb;
        if (r4 == 0) goto L_0x0ea2;
    L_0x0e9b:
        r0 = r144;
        r4 = r0.currentPhotoObjectThumb;
        r6 = -1;
        r4.size = r6;
    L_0x0ea2:
        if (r125 != 0) goto L_0x0eab;
    L_0x0ea4:
        r0 = r144;
        r4 = r0.documentAttachType;
        r6 = 7;
        if (r4 != r6) goto L_0x18f2;
    L_0x0eab:
        r78 = r93;
        r141 = r93;
    L_0x0eaf:
        r0 = r144;
        r4 = r0.isSmallImage;
        if (r4 == 0) goto L_0x198b;
    L_0x0eb5:
        r4 = 1112014848; // 0x42480000 float:50.0 double:5.49408334E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r4 + r43;
        r0 = r144;
        r6 = r0.linkPreviewHeight;
        if (r4 <= r6) goto L_0x0eec;
    L_0x0ec3:
        r0 = r144;
        r4 = r0.totalHeight;
        r6 = 1112014848; // 0x42480000 float:50.0 double:5.49408334E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r6 = r6 + r43;
        r0 = r144;
        r8 = r0.linkPreviewHeight;
        r6 = r6 - r8;
        r8 = 1090519040; // 0x41000000 float:8.0 double:5.38787994E-315;
        r8 = org.telegram.messenger.AndroidUtilities.dp(r8);
        r6 = r6 + r8;
        r4 = r4 + r6;
        r0 = r144;
        r0.totalHeight = r4;
        r4 = 1112014848; // 0x42480000 float:50.0 double:5.49408334E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r4 + r43;
        r0 = r144;
        r0.linkPreviewHeight = r4;
    L_0x0eec:
        r0 = r144;
        r4 = r0.linkPreviewHeight;
        r6 = 1090519040; // 0x41000000 float:8.0 double:5.38787994E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r0 = r144;
        r0.linkPreviewHeight = r4;
    L_0x0efb:
        r0 = r144;
        r4 = r0.photoImage;
        r6 = 0;
        r8 = 0;
        r0 = r141;
        r1 = r78;
        r4.setImageCoords(r6, r8, r0, r1);
        r4 = java.util.Locale.US;
        r6 = "%d_%d";
        r8 = 2;
        r8 = new java.lang.Object[r8];
        r9 = 0;
        r10 = java.lang.Integer.valueOf(r141);
        r8[r9] = r10;
        r9 = 1;
        r10 = java.lang.Integer.valueOf(r78);
        r8[r9] = r10;
        r4 = java.lang.String.format(r4, r6, r8);
        r0 = r144;
        r0.currentPhotoFilter = r4;
        r4 = java.util.Locale.US;
        r6 = "%d_%d_b";
        r8 = 2;
        r8 = new java.lang.Object[r8];
        r9 = 0;
        r10 = java.lang.Integer.valueOf(r141);
        r8[r9] = r10;
        r9 = 1;
        r10 = java.lang.Integer.valueOf(r78);
        r8[r9] = r10;
        r4 = java.lang.String.format(r4, r6, r8);
        r0 = r144;
        r0.currentPhotoFilterThumb = r4;
        if (r15 == 0) goto L_0x19a8;
    L_0x0f46:
        r0 = r144;
        r14 = r0.photoImage;
        r16 = 0;
        r0 = r144;
        r0 = r0.currentPhotoFilter;
        r17 = r0;
        r18 = 0;
        r19 = 0;
        r20 = "b1";
        r0 = r15.size;
        r21 = r0;
        r22 = 0;
        r23 = 1;
        r14.setImage(r15, r16, r17, r18, r19, r20, r21, r22, r23);
    L_0x0f64:
        r4 = 1;
        r0 = r144;
        r0.drawPhotoImage = r4;
        if (r134 == 0) goto L_0x1bc5;
    L_0x0f6b:
        r4 = "video";
        r0 = r134;
        r4 = r0.equals(r4);
        if (r4 == 0) goto L_0x1bc5;
    L_0x0f76:
        if (r67 == 0) goto L_0x1bc5;
    L_0x0f78:
        r98 = r67 / 60;
        r4 = r98 * 60;
        r122 = r67 - r4;
        r4 = "%d:%02d";
        r6 = 2;
        r6 = new java.lang.Object[r6];
        r8 = 0;
        r9 = java.lang.Integer.valueOf(r98);
        r6[r8] = r9;
        r8 = 1;
        r9 = java.lang.Integer.valueOf(r122);
        r6[r8] = r9;
        r5 = java.lang.String.format(r4, r6);
        r4 = org.telegram.ui.ActionBar.Theme.chat_durationPaint;
        r4 = r4.measureText(r5);
        r8 = (double) r4;
        r8 = java.lang.Math.ceil(r8);
        r4 = (int) r8;
        r0 = r144;
        r0.durationWidth = r4;
        r16 = new android.text.StaticLayout;
        r18 = org.telegram.ui.ActionBar.Theme.chat_durationPaint;
        r0 = r144;
        r0 = r0.durationWidth;
        r19 = r0;
        r20 = android.text.Layout.Alignment.ALIGN_NORMAL;
        r21 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r22 = 0;
        r23 = 0;
        r17 = r5;
        r16.<init>(r17, r18, r19, r20, r21, r22, r23);
        r0 = r16;
        r1 = r144;
        r1.videoInfoLayout = r0;
    L_0x0fc3:
        r0 = r144;
        r4 = r0.hasInvoicePreview;
        if (r4 == 0) goto L_0x10b9;
    L_0x0fc9:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r4 = r4.flags;
        r4 = r4 & 4;
        if (r4 == 0) goto L_0x1c32;
    L_0x0fd5:
        r4 = "PaymentReceipt";
        r6 = 2131494359; // 0x7f0c05d7 float:1.8612224E38 double:1.053098137E-314;
        r4 = org.telegram.messenger.LocaleController.getString(r4, r6);
        r5 = r4.toUpperCase();
    L_0x0fe3:
        r4 = org.telegram.messenger.LocaleController.getInstance();
        r0 = r145;
        r6 = r0.messageOwner;
        r6 = r6.media;
        r8 = r6.total_amount;
        r0 = r145;
        r6 = r0.messageOwner;
        r6 = r6.media;
        r6 = r6.currency;
        r112 = r4.formatCurrencyString(r8, r6);
        r17 = new android.text.SpannableStringBuilder;
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r0 = r112;
        r4 = r4.append(r0);
        r6 = " ";
        r4 = r4.append(r6);
        r4 = r4.append(r5);
        r4 = r4.toString();
        r0 = r17;
        r0.<init>(r4);
        r4 = new org.telegram.ui.Components.TypefaceSpan;
        r6 = "fonts/rmedium.ttf";
        r6 = org.telegram.messenger.AndroidUtilities.getTypeface(r6);
        r4.<init>(r6);
        r6 = 0;
        r8 = r112.length();
        r9 = 33;
        r0 = r17;
        r0.setSpan(r4, r6, r8, r9);
        r4 = org.telegram.ui.ActionBar.Theme.chat_shipmentPaint;
        r6 = 0;
        r8 = r17.length();
        r0 = r17;
        r4 = r4.measureText(r0, r6, r8);
        r8 = (double) r4;
        r8 = java.lang.Math.ceil(r8);
        r4 = (int) r8;
        r0 = r144;
        r0.durationWidth = r4;
        r16 = new android.text.StaticLayout;
        r18 = org.telegram.ui.ActionBar.Theme.chat_shipmentPaint;
        r0 = r144;
        r4 = r0.durationWidth;
        r6 = 1092616192; // 0x41200000 float:10.0 double:5.398241246E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r19 = r4 + r6;
        r20 = android.text.Layout.Alignment.ALIGN_NORMAL;
        r21 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r22 = 0;
        r23 = 0;
        r16.<init>(r17, r18, r19, r20, r21, r22, r23);
        r0 = r16;
        r1 = r144;
        r1.videoInfoLayout = r0;
        r0 = r144;
        r4 = r0.drawPhotoImage;
        if (r4 != 0) goto L_0x10b9;
    L_0x1072:
        r0 = r144;
        r4 = r0.totalHeight;
        r6 = 1086324736; // 0x40c00000 float:6.0 double:5.367157323E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r0 = r144;
        r0.totalHeight = r4;
        r0 = r144;
        r6 = r0.timeWidth;
        r4 = r145.isOutOwner();
        if (r4 == 0) goto L_0x1c5c;
    L_0x108b:
        r4 = 20;
    L_0x108d:
        r4 = r4 + 14;
        r4 = (float) r4;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r131 = r6 + r4;
        r0 = r144;
        r4 = r0.durationWidth;
        r4 = r4 + r131;
        r0 = r95;
        if (r4 <= r0) goto L_0x1c5f;
    L_0x10a0:
        r0 = r144;
        r4 = r0.durationWidth;
        r0 = r91;
        r91 = java.lang.Math.max(r4, r0);
        r0 = r144;
        r4 = r0.totalHeight;
        r6 = 1094713344; // 0x41400000 float:12.0 double:5.408602553E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r0 = r144;
        r0.totalHeight = r4;
    L_0x10b9:
        r0 = r144;
        r4 = r0.hasGamePreview;
        if (r4 == 0) goto L_0x10e8;
    L_0x10bf:
        r0 = r145;
        r4 = r0.textHeight;
        if (r4 == 0) goto L_0x10e8;
    L_0x10c5:
        r0 = r144;
        r4 = r0.linkPreviewHeight;
        r0 = r145;
        r6 = r0.textHeight;
        r8 = 1086324736; // 0x40c00000 float:6.0 double:5.367157323E-315;
        r8 = org.telegram.messenger.AndroidUtilities.dp(r8);
        r6 = r6 + r8;
        r4 = r4 + r6;
        r0 = r144;
        r0.linkPreviewHeight = r4;
        r0 = r144;
        r4 = r0.totalHeight;
        r6 = 1082130432; // 0x40800000 float:4.0 double:5.34643471E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r0 = r144;
        r0.totalHeight = r4;
    L_0x10e8:
        r0 = r144;
        r1 = r95;
        r2 = r130;
        r3 = r91;
        r0.calcBackgroundWidth(r1, r2, r3);
    L_0x10f3:
        r144.createInstantViewButton();
    L_0x10f6:
        r0 = r144;
        r4 = r0.currentPosition;
        if (r4 != 0) goto L_0x38e6;
    L_0x10fc:
        r0 = r144;
        r4 = r0.captionLayout;
        if (r4 != 0) goto L_0x38e6;
    L_0x1102:
        r0 = r145;
        r4 = r0.caption;
        if (r4 == 0) goto L_0x38e6;
    L_0x1108:
        r0 = r145;
        r4 = r0.type;
        r6 = 13;
        if (r4 == r6) goto L_0x38e6;
    L_0x1110:
        r0 = r145;
        r4 = r0.caption;	 Catch:{ Exception -> 0x38dd }
        r0 = r144;
        r0.currentCaption = r4;	 Catch:{ Exception -> 0x38dd }
        r0 = r144;
        r4 = r0.backgroundWidth;	 Catch:{ Exception -> 0x38dd }
        r6 = 1106771968; // 0x41f80000 float:31.0 double:5.46818007E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);	 Catch:{ Exception -> 0x38dd }
        r141 = r4 - r6;
        r4 = 1092616192; // 0x41200000 float:10.0 double:5.398241246E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);	 Catch:{ Exception -> 0x38dd }
        r29 = r141 - r4;
        r4 = android.os.Build.VERSION.SDK_INT;	 Catch:{ Exception -> 0x38dd }
        r6 = 24;
        if (r4 < r6) goto L_0x38c0;
    L_0x1132:
        r0 = r145;
        r4 = r0.caption;	 Catch:{ Exception -> 0x38dd }
        r6 = 0;
        r0 = r145;
        r8 = r0.caption;	 Catch:{ Exception -> 0x38dd }
        r8 = r8.length();	 Catch:{ Exception -> 0x38dd }
        r9 = org.telegram.ui.ActionBar.Theme.chat_msgTextPaint;	 Catch:{ Exception -> 0x38dd }
        r0 = r29;
        r4 = android.text.StaticLayout.Builder.obtain(r4, r6, r8, r9, r0);	 Catch:{ Exception -> 0x38dd }
        r6 = 1;
        r4 = r4.setBreakStrategy(r6);	 Catch:{ Exception -> 0x38dd }
        r6 = 0;
        r4 = r4.setHyphenationFrequency(r6);	 Catch:{ Exception -> 0x38dd }
        r6 = android.text.Layout.Alignment.ALIGN_NORMAL;	 Catch:{ Exception -> 0x38dd }
        r4 = r4.setAlignment(r6);	 Catch:{ Exception -> 0x38dd }
        r4 = r4.build();	 Catch:{ Exception -> 0x38dd }
        r0 = r144;
        r0.captionLayout = r4;	 Catch:{ Exception -> 0x38dd }
    L_0x115f:
        r0 = r144;
        r4 = r0.captionLayout;	 Catch:{ Exception -> 0x38dd }
        r4 = r4.getLineCount();	 Catch:{ Exception -> 0x38dd }
        if (r4 <= 0) goto L_0x11f9;
    L_0x1169:
        r0 = r29;
        r1 = r144;
        r1.captionWidth = r0;	 Catch:{ Exception -> 0x38dd }
        r0 = r144;
        r6 = r0.timeWidth;	 Catch:{ Exception -> 0x38dd }
        r4 = r145.isOutOwner();	 Catch:{ Exception -> 0x38dd }
        if (r4 == 0) goto L_0x38e3;
    L_0x1179:
        r4 = 1101004800; // 0x41a00000 float:20.0 double:5.439686476E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);	 Catch:{ Exception -> 0x38dd }
    L_0x117f:
        r131 = r6 + r4;
        r0 = r144;
        r4 = r0.captionLayout;	 Catch:{ Exception -> 0x38dd }
        r4 = r4.getHeight();	 Catch:{ Exception -> 0x38dd }
        r0 = r144;
        r0.captionHeight = r4;	 Catch:{ Exception -> 0x38dd }
        r0 = r144;
        r4 = r0.totalHeight;	 Catch:{ Exception -> 0x38dd }
        r0 = r144;
        r6 = r0.captionHeight;	 Catch:{ Exception -> 0x38dd }
        r8 = 1091567616; // 0x41100000 float:9.0 double:5.39306059E-315;
        r8 = org.telegram.messenger.AndroidUtilities.dp(r8);	 Catch:{ Exception -> 0x38dd }
        r6 = r6 + r8;
        r4 = r4 + r6;
        r0 = r144;
        r0.totalHeight = r4;	 Catch:{ Exception -> 0x38dd }
        r0 = r144;
        r4 = r0.captionLayout;	 Catch:{ Exception -> 0x38dd }
        r0 = r144;
        r6 = r0.captionLayout;	 Catch:{ Exception -> 0x38dd }
        r6 = r6.getLineCount();	 Catch:{ Exception -> 0x38dd }
        r6 = r6 + -1;
        r4 = r4.getLineWidth(r6);	 Catch:{ Exception -> 0x38dd }
        r0 = r144;
        r6 = r0.captionLayout;	 Catch:{ Exception -> 0x38dd }
        r0 = r144;
        r8 = r0.captionLayout;	 Catch:{ Exception -> 0x38dd }
        r8 = r8.getLineCount();	 Catch:{ Exception -> 0x38dd }
        r8 = r8 + -1;
        r6 = r6.getLineLeft(r8);	 Catch:{ Exception -> 0x38dd }
        r83 = r4 + r6;
        r4 = 1090519040; // 0x41000000 float:8.0 double:5.38787994E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);	 Catch:{ Exception -> 0x38dd }
        r4 = r141 - r4;
        r4 = (float) r4;	 Catch:{ Exception -> 0x38dd }
        r4 = r4 - r83;
        r0 = r131;
        r6 = (float) r0;	 Catch:{ Exception -> 0x38dd }
        r4 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1));
        if (r4 >= 0) goto L_0x11f9;
    L_0x11d9:
        r0 = r144;
        r4 = r0.totalHeight;	 Catch:{ Exception -> 0x38dd }
        r6 = 1096810496; // 0x41600000 float:14.0 double:5.41896386E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);	 Catch:{ Exception -> 0x38dd }
        r4 = r4 + r6;
        r0 = r144;
        r0.totalHeight = r4;	 Catch:{ Exception -> 0x38dd }
        r0 = r144;
        r4 = r0.captionHeight;	 Catch:{ Exception -> 0x38dd }
        r6 = 1096810496; // 0x41600000 float:14.0 double:5.41896386E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);	 Catch:{ Exception -> 0x38dd }
        r4 = r4 + r6;
        r0 = r144;
        r0.captionHeight = r4;	 Catch:{ Exception -> 0x38dd }
        r57 = 2;
    L_0x11f9:
        r0 = r144;
        r4 = r0.currentMessageObject;
        r8 = r4.eventId;
        r20 = 0;
        r4 = (r8 > r20 ? 1 : (r8 == r20 ? 0 : -1));
        if (r4 == 0) goto L_0x395f;
    L_0x1205:
        r0 = r144;
        r4 = r0.currentMessageObject;
        r4 = r4.isMediaEmpty();
        if (r4 != 0) goto L_0x395f;
    L_0x120f:
        r0 = r144;
        r4 = r0.currentMessageObject;
        r4 = r4.messageOwner;
        r4 = r4.media;
        r4 = r4.webpage;
        if (r4 == 0) goto L_0x395f;
    L_0x121b:
        r0 = r144;
        r4 = r0.backgroundWidth;
        r6 = 1109655552; // 0x42240000 float:41.0 double:5.48242687E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r11 = r4 - r6;
        r4 = 1;
        r0 = r144;
        r0.hasOldCaptionPreview = r4;
        r4 = 0;
        r0 = r144;
        r0.linkPreviewHeight = r4;
        r0 = r144;
        r4 = r0.currentMessageObject;
        r4 = r4.messageOwner;
        r4 = r4.media;
        r0 = r4.webpage;
        r139 = r0;
        r4 = org.telegram.ui.ActionBar.Theme.chat_replyNamePaint;	 Catch:{ Exception -> 0x3910 }
        r0 = r139;
        r6 = r0.site_name;	 Catch:{ Exception -> 0x3910 }
        r4 = r4.measureText(r6);	 Catch:{ Exception -> 0x3910 }
        r6 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r4 = r4 + r6;
        r8 = (double) r4;	 Catch:{ Exception -> 0x3910 }
        r8 = java.lang.Math.ceil(r8);	 Catch:{ Exception -> 0x3910 }
        r0 = (int) r8;	 Catch:{ Exception -> 0x3910 }
        r141 = r0;
        r0 = r141;
        r1 = r144;
        r1.siteNameWidth = r0;	 Catch:{ Exception -> 0x3910 }
        r30 = new android.text.StaticLayout;	 Catch:{ Exception -> 0x3910 }
        r0 = r139;
        r0 = r0.site_name;	 Catch:{ Exception -> 0x3910 }
        r31 = r0;
        r32 = org.telegram.ui.ActionBar.Theme.chat_replyNamePaint;	 Catch:{ Exception -> 0x3910 }
        r0 = r141;
        r33 = java.lang.Math.min(r0, r11);	 Catch:{ Exception -> 0x3910 }
        r34 = android.text.Layout.Alignment.ALIGN_NORMAL;	 Catch:{ Exception -> 0x3910 }
        r35 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r36 = 0;
        r37 = 0;
        r30.<init>(r31, r32, r33, r34, r35, r36, r37);	 Catch:{ Exception -> 0x3910 }
        r0 = r30;
        r1 = r144;
        r1.siteNameLayout = r0;	 Catch:{ Exception -> 0x3910 }
        r0 = r144;
        r4 = r0.siteNameLayout;	 Catch:{ Exception -> 0x3910 }
        r6 = 0;
        r4 = r4.getLineLeft(r6);	 Catch:{ Exception -> 0x3910 }
        r6 = 0;
        r4 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1));
        if (r4 == 0) goto L_0x390d;
    L_0x1287:
        r4 = 1;
    L_0x1288:
        r0 = r144;
        r0.siteNameRtl = r4;	 Catch:{ Exception -> 0x3910 }
        r0 = r144;
        r4 = r0.siteNameLayout;	 Catch:{ Exception -> 0x3910 }
        r0 = r144;
        r6 = r0.siteNameLayout;	 Catch:{ Exception -> 0x3910 }
        r6 = r6.getLineCount();	 Catch:{ Exception -> 0x3910 }
        r6 = r6 + -1;
        r78 = r4.getLineBottom(r6);	 Catch:{ Exception -> 0x3910 }
        r0 = r144;
        r4 = r0.linkPreviewHeight;	 Catch:{ Exception -> 0x3910 }
        r4 = r4 + r78;
        r0 = r144;
        r0.linkPreviewHeight = r4;	 Catch:{ Exception -> 0x3910 }
        r0 = r144;
        r4 = r0.totalHeight;	 Catch:{ Exception -> 0x3910 }
        r4 = r4 + r78;
        r0 = r144;
        r0.totalHeight = r4;	 Catch:{ Exception -> 0x3910 }
    L_0x12b2:
        r4 = 0;
        r0 = r144;
        r0.descriptionX = r4;	 Catch:{ Exception -> 0x3927 }
        r0 = r144;
        r4 = r0.linkPreviewHeight;	 Catch:{ Exception -> 0x3927 }
        if (r4 == 0) goto L_0x12cc;
    L_0x12bd:
        r0 = r144;
        r4 = r0.totalHeight;	 Catch:{ Exception -> 0x3927 }
        r6 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);	 Catch:{ Exception -> 0x3927 }
        r4 = r4 + r6;
        r0 = r144;
        r0.totalHeight = r4;	 Catch:{ Exception -> 0x3927 }
    L_0x12cc:
        r0 = r139;
        r0 = r0.description;	 Catch:{ Exception -> 0x3927 }
        r30 = r0;
        r31 = org.telegram.ui.ActionBar.Theme.chat_replyTextPaint;	 Catch:{ Exception -> 0x3927 }
        r33 = android.text.Layout.Alignment.ALIGN_NORMAL;	 Catch:{ Exception -> 0x3927 }
        r34 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r4 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);	 Catch:{ Exception -> 0x3927 }
        r0 = (float) r4;	 Catch:{ Exception -> 0x3927 }
        r35 = r0;
        r36 = 0;
        r37 = android.text.TextUtils.TruncateAt.END;	 Catch:{ Exception -> 0x3927 }
        r39 = 6;
        r32 = r11;
        r38 = r11;
        r4 = org.telegram.ui.Components.StaticLayoutEx.createStaticLayout(r30, r31, r32, r33, r34, r35, r36, r37, r38, r39);	 Catch:{ Exception -> 0x3927 }
        r0 = r144;
        r0.descriptionLayout = r4;	 Catch:{ Exception -> 0x3927 }
        r0 = r144;
        r4 = r0.descriptionLayout;	 Catch:{ Exception -> 0x3927 }
        r0 = r144;
        r6 = r0.descriptionLayout;	 Catch:{ Exception -> 0x3927 }
        r6 = r6.getLineCount();	 Catch:{ Exception -> 0x3927 }
        r6 = r6 + -1;
        r78 = r4.getLineBottom(r6);	 Catch:{ Exception -> 0x3927 }
        r0 = r144;
        r4 = r0.linkPreviewHeight;	 Catch:{ Exception -> 0x3927 }
        r4 = r4 + r78;
        r0 = r144;
        r0.linkPreviewHeight = r4;	 Catch:{ Exception -> 0x3927 }
        r0 = r144;
        r4 = r0.totalHeight;	 Catch:{ Exception -> 0x3927 }
        r4 = r4 + r78;
        r0 = r144;
        r0.totalHeight = r4;	 Catch:{ Exception -> 0x3927 }
        r40 = 0;
    L_0x131b:
        r0 = r144;
        r4 = r0.descriptionLayout;	 Catch:{ Exception -> 0x3927 }
        r4 = r4.getLineCount();	 Catch:{ Exception -> 0x3927 }
        r0 = r40;
        if (r0 >= r4) goto L_0x392b;
    L_0x1327:
        r0 = r144;
        r4 = r0.descriptionLayout;	 Catch:{ Exception -> 0x3927 }
        r0 = r40;
        r4 = r4.getLineLeft(r0);	 Catch:{ Exception -> 0x3927 }
        r8 = (double) r4;	 Catch:{ Exception -> 0x3927 }
        r8 = java.lang.Math.ceil(r8);	 Catch:{ Exception -> 0x3927 }
        r0 = (int) r8;	 Catch:{ Exception -> 0x3927 }
        r85 = r0;
        if (r85 == 0) goto L_0x1348;
    L_0x133b:
        r0 = r144;
        r4 = r0.descriptionX;	 Catch:{ Exception -> 0x3927 }
        if (r4 != 0) goto L_0x3916;
    L_0x1341:
        r0 = r85;
        r4 = -r0;
        r0 = r144;
        r0.descriptionX = r4;	 Catch:{ Exception -> 0x3927 }
    L_0x1348:
        r40 = r40 + 1;
        goto L_0x131b;
    L_0x134b:
        r14 = 6;
        goto L_0x0d90;
    L_0x134e:
        r0 = r144;
        r4 = r0.descriptionX;	 Catch:{ Exception -> 0x0d9a }
        r0 = r85;
        r6 = -r0;
        r4 = java.lang.Math.max(r4, r6);	 Catch:{ Exception -> 0x0d9a }
        r0 = r144;
        r0.descriptionX = r4;	 Catch:{ Exception -> 0x0d9a }
        goto L_0x0d23;
    L_0x135f:
        r0 = r144;
        r4 = r0.descriptionLayout;	 Catch:{ Exception -> 0x0d9a }
        r127 = r4.getWidth();	 Catch:{ Exception -> 0x0d9a }
        r40 = 0;
    L_0x1369:
        r0 = r144;
        r4 = r0.descriptionLayout;	 Catch:{ Exception -> 0x0d9a }
        r4 = r4.getLineCount();	 Catch:{ Exception -> 0x0d9a }
        r0 = r40;
        if (r0 >= r4) goto L_0x0d9e;
    L_0x1375:
        r0 = r144;
        r4 = r0.descriptionLayout;	 Catch:{ Exception -> 0x0d9a }
        r0 = r40;
        r4 = r4.getLineLeft(r0);	 Catch:{ Exception -> 0x0d9a }
        r8 = (double) r4;	 Catch:{ Exception -> 0x0d9a }
        r8 = java.lang.Math.ceil(r8);	 Catch:{ Exception -> 0x0d9a }
        r0 = (int) r8;	 Catch:{ Exception -> 0x0d9a }
        r85 = r0;
        if (r85 != 0) goto L_0x1394;
    L_0x1389:
        r0 = r144;
        r4 = r0.descriptionX;	 Catch:{ Exception -> 0x0d9a }
        if (r4 == 0) goto L_0x1394;
    L_0x138f:
        r4 = 0;
        r0 = r144;
        r0.descriptionX = r4;	 Catch:{ Exception -> 0x0d9a }
    L_0x1394:
        if (r85 == 0) goto L_0x13e1;
    L_0x1396:
        r141 = r127 - r85;
    L_0x1398:
        r0 = r40;
        r1 = r113;
        if (r0 < r1) goto L_0x13a8;
    L_0x139e:
        if (r113 == 0) goto L_0x13b0;
    L_0x13a0:
        if (r85 == 0) goto L_0x13b0;
    L_0x13a2:
        r0 = r144;
        r4 = r0.isSmallImage;	 Catch:{ Exception -> 0x0d9a }
        if (r4 == 0) goto L_0x13b0;
    L_0x13a8:
        r4 = 1112539136; // 0x42500000 float:52.0 double:5.496673668E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);	 Catch:{ Exception -> 0x0d9a }
        r141 = r141 + r4;
    L_0x13b0:
        r4 = r141 + r41;
        r0 = r94;
        if (r0 >= r4) goto L_0x13d6;
    L_0x13b6:
        if (r133 == 0) goto L_0x13c5;
    L_0x13b8:
        r0 = r144;
        r4 = r0.titleX;	 Catch:{ Exception -> 0x0d9a }
        r6 = r141 + r41;
        r6 = r6 - r94;
        r4 = r4 + r6;
        r0 = r144;
        r0.titleX = r4;	 Catch:{ Exception -> 0x0d9a }
    L_0x13c5:
        if (r48 == 0) goto L_0x13d4;
    L_0x13c7:
        r0 = r144;
        r4 = r0.authorX;	 Catch:{ Exception -> 0x0d9a }
        r6 = r141 + r41;
        r6 = r6 - r94;
        r4 = r4 + r6;
        r0 = r144;
        r0.authorX = r4;	 Catch:{ Exception -> 0x0d9a }
    L_0x13d4:
        r94 = r141 + r41;
    L_0x13d6:
        r4 = r141 + r41;
        r0 = r91;
        r91 = java.lang.Math.max(r0, r4);	 Catch:{ Exception -> 0x0d9a }
        r40 = r40 + 1;
        goto L_0x1369;
    L_0x13e1:
        if (r77 == 0) goto L_0x13e6;
    L_0x13e3:
        r141 = r127;
        goto L_0x1398;
    L_0x13e6:
        r0 = r144;
        r4 = r0.descriptionLayout;	 Catch:{ Exception -> 0x0d9a }
        r0 = r40;
        r4 = r4.getLineWidth(r0);	 Catch:{ Exception -> 0x0d9a }
        r8 = (double) r4;	 Catch:{ Exception -> 0x0d9a }
        r8 = java.lang.Math.ceil(r8);	 Catch:{ Exception -> 0x0d9a }
        r4 = (int) r8;	 Catch:{ Exception -> 0x0d9a }
        r0 = r127;
        r141 = java.lang.Math.min(r4, r0);	 Catch:{ Exception -> 0x0d9a }
        goto L_0x1398;
    L_0x13fd:
        r93 = r11;
        goto L_0x0dc6;
    L_0x1401:
        r4 = org.telegram.messenger.MessageObject.isGifDocument(r66);
        if (r4 == 0) goto L_0x14b2;
    L_0x1407:
        r4 = org.telegram.messenger.SharedConfig.autoplayGifs;
        if (r4 != 0) goto L_0x1411;
    L_0x140b:
        r4 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r0 = r145;
        r0.gifState = r4;
    L_0x1411:
        r0 = r144;
        r6 = r0.photoImage;
        r0 = r145;
        r4 = r0.gifState;
        r8 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r4 = (r4 > r8 ? 1 : (r4 == r8 ? 0 : -1));
        if (r4 == 0) goto L_0x14ac;
    L_0x141f:
        r4 = 1;
    L_0x1420:
        r6.setAllowStartAnimation(r4);
        r0 = r66;
        r4 = r0.thumb;
        r0 = r144;
        r0.currentPhotoObject = r4;
        r0 = r144;
        r4 = r0.currentPhotoObject;
        if (r4 == 0) goto L_0x149d;
    L_0x1431:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r4 = r4.w;
        if (r4 == 0) goto L_0x1441;
    L_0x1439:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r4 = r4.h;
        if (r4 != 0) goto L_0x149d;
    L_0x1441:
        r40 = 0;
    L_0x1443:
        r0 = r66;
        r4 = r0.attributes;
        r4 = r4.size();
        r0 = r40;
        if (r0 >= r4) goto L_0x147b;
    L_0x144f:
        r0 = r66;
        r4 = r0.attributes;
        r0 = r40;
        r46 = r4.get(r0);
        r46 = (org.telegram.tgnet.TLRPC.DocumentAttribute) r46;
        r0 = r46;
        r4 = r0 instanceof org.telegram.tgnet.TLRPC.TL_documentAttributeImageSize;
        if (r4 != 0) goto L_0x1467;
    L_0x1461:
        r0 = r46;
        r4 = r0 instanceof org.telegram.tgnet.TLRPC.TL_documentAttributeVideo;
        if (r4 == 0) goto L_0x14af;
    L_0x1467:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r0 = r46;
        r6 = r0.w;
        r4.w = r6;
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r0 = r46;
        r6 = r0.h;
        r4.h = r6;
    L_0x147b:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r4 = r4.w;
        if (r4 == 0) goto L_0x148b;
    L_0x1483:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r4 = r4.h;
        if (r4 != 0) goto L_0x149d;
    L_0x148b:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r0 = r144;
        r6 = r0.currentPhotoObject;
        r8 = 1125515264; // 0x43160000 float:150.0 double:5.56078426E-315;
        r8 = org.telegram.messenger.AndroidUtilities.dp(r8);
        r6.h = r8;
        r4.w = r8;
    L_0x149d:
        r0 = r66;
        r1 = r144;
        r1.documentAttach = r0;
        r4 = 2;
        r0 = r144;
        r0.documentAttachType = r4;
        r15 = r138;
        goto L_0x0de3;
    L_0x14ac:
        r4 = 0;
        goto L_0x1420;
    L_0x14af:
        r40 = r40 + 1;
        goto L_0x1443;
    L_0x14b2:
        r4 = org.telegram.messenger.MessageObject.isVideoDocument(r66);
        if (r4 == 0) goto L_0x153b;
    L_0x14b8:
        r0 = r66;
        r4 = r0.thumb;
        r0 = r144;
        r0.currentPhotoObject = r4;
        r0 = r144;
        r4 = r0.currentPhotoObject;
        if (r4 == 0) goto L_0x152c;
    L_0x14c6:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r4 = r4.w;
        if (r4 == 0) goto L_0x14d6;
    L_0x14ce:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r4 = r4.h;
        if (r4 != 0) goto L_0x152c;
    L_0x14d6:
        r40 = 0;
    L_0x14d8:
        r0 = r66;
        r4 = r0.attributes;
        r4 = r4.size();
        r0 = r40;
        if (r0 >= r4) goto L_0x150a;
    L_0x14e4:
        r0 = r66;
        r4 = r0.attributes;
        r0 = r40;
        r46 = r4.get(r0);
        r46 = (org.telegram.tgnet.TLRPC.DocumentAttribute) r46;
        r0 = r46;
        r4 = r0 instanceof org.telegram.tgnet.TLRPC.TL_documentAttributeVideo;
        if (r4 == 0) goto L_0x1538;
    L_0x14f6:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r0 = r46;
        r6 = r0.w;
        r4.w = r6;
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r0 = r46;
        r6 = r0.h;
        r4.h = r6;
    L_0x150a:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r4 = r4.w;
        if (r4 == 0) goto L_0x151a;
    L_0x1512:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r4 = r4.h;
        if (r4 != 0) goto L_0x152c;
    L_0x151a:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r0 = r144;
        r6 = r0.currentPhotoObject;
        r8 = 1125515264; // 0x43160000 float:150.0 double:5.56078426E-315;
        r8 = org.telegram.messenger.AndroidUtilities.dp(r8);
        r6.h = r8;
        r4.w = r8;
    L_0x152c:
        r4 = 0;
        r0 = r144;
        r1 = r145;
        r0.createDocumentLayout(r4, r1);
        r15 = r138;
        goto L_0x0de3;
    L_0x1538:
        r40 = r40 + 1;
        goto L_0x14d8;
    L_0x153b:
        r4 = org.telegram.messenger.MessageObject.isStickerDocument(r66);
        if (r4 == 0) goto L_0x15c7;
    L_0x1541:
        r0 = r66;
        r4 = r0.thumb;
        r0 = r144;
        r0.currentPhotoObject = r4;
        r0 = r144;
        r4 = r0.currentPhotoObject;
        if (r4 == 0) goto L_0x15b5;
    L_0x154f:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r4 = r4.w;
        if (r4 == 0) goto L_0x155f;
    L_0x1557:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r4 = r4.h;
        if (r4 != 0) goto L_0x15b5;
    L_0x155f:
        r40 = 0;
    L_0x1561:
        r0 = r66;
        r4 = r0.attributes;
        r4 = r4.size();
        r0 = r40;
        if (r0 >= r4) goto L_0x1593;
    L_0x156d:
        r0 = r66;
        r4 = r0.attributes;
        r0 = r40;
        r46 = r4.get(r0);
        r46 = (org.telegram.tgnet.TLRPC.DocumentAttribute) r46;
        r0 = r46;
        r4 = r0 instanceof org.telegram.tgnet.TLRPC.TL_documentAttributeImageSize;
        if (r4 == 0) goto L_0x15c4;
    L_0x157f:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r0 = r46;
        r6 = r0.w;
        r4.w = r6;
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r0 = r46;
        r6 = r0.h;
        r4.h = r6;
    L_0x1593:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r4 = r4.w;
        if (r4 == 0) goto L_0x15a3;
    L_0x159b:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r4 = r4.h;
        if (r4 != 0) goto L_0x15b5;
    L_0x15a3:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r0 = r144;
        r6 = r0.currentPhotoObject;
        r8 = 1125515264; // 0x43160000 float:150.0 double:5.56078426E-315;
        r8 = org.telegram.messenger.AndroidUtilities.dp(r8);
        r6.h = r8;
        r4.w = r8;
    L_0x15b5:
        r0 = r66;
        r1 = r144;
        r1.documentAttach = r0;
        r4 = 6;
        r0 = r144;
        r0.documentAttachType = r4;
        r15 = r138;
        goto L_0x0de3;
    L_0x15c4:
        r40 = r40 + 1;
        goto L_0x1561;
    L_0x15c7:
        r0 = r144;
        r1 = r95;
        r2 = r130;
        r3 = r91;
        r0.calcBackgroundWidth(r1, r2, r3);
        r4 = org.telegram.messenger.MessageObject.isStickerDocument(r66);
        if (r4 != 0) goto L_0x3cdd;
    L_0x15d8:
        r0 = r144;
        r4 = r0.backgroundWidth;
        r6 = 1101004800; // 0x41a00000 float:20.0 double:5.439686476E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r6 = r6 + r95;
        if (r4 >= r6) goto L_0x15f2;
    L_0x15e6:
        r4 = 1101004800; // 0x41a00000 float:20.0 double:5.439686476E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r4 + r95;
        r0 = r144;
        r0.backgroundWidth = r4;
    L_0x15f2:
        r4 = org.telegram.messenger.MessageObject.isVoiceDocument(r66);
        if (r4 == 0) goto L_0x16ce;
    L_0x15f8:
        r0 = r144;
        r4 = r0.backgroundWidth;
        r6 = 1092616192; // 0x41200000 float:10.0 double:5.398241246E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r0 = r144;
        r1 = r145;
        r0.createDocumentLayout(r4, r1);
        r0 = r144;
        r4 = r0.currentMessageObject;
        r4 = r4.textHeight;
        r6 = 1090519040; // 0x41000000 float:8.0 double:5.38787994E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r0 = r144;
        r6 = r0.linkPreviewHeight;
        r4 = r4 + r6;
        r0 = r144;
        r0.mediaOffsetY = r4;
        r0 = r144;
        r4 = r0.totalHeight;
        r6 = 1110441984; // 0x42300000 float:44.0 double:5.48631236E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r0 = r144;
        r0.totalHeight = r4;
        r0 = r144;
        r4 = r0.linkPreviewHeight;
        r6 = 1110441984; // 0x42300000 float:44.0 double:5.48631236E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r0 = r144;
        r0.linkPreviewHeight = r4;
        r4 = 1118568448; // 0x42ac0000 float:86.0 double:5.526462427E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r95 = r95 - r4;
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 == 0) goto L_0x1694;
    L_0x164c:
        r6 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        r0 = r144;
        r4 = r0.isChat;
        if (r4 == 0) goto L_0x1692;
    L_0x1656:
        r4 = r145.needDrawAvatar();
        if (r4 == 0) goto L_0x1692;
    L_0x165c:
        r4 = r145.isOutOwner();
        if (r4 != 0) goto L_0x1692;
    L_0x1662:
        r4 = 1112539136; // 0x42500000 float:52.0 double:5.496673668E-315;
    L_0x1664:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r6 - r4;
        r6 = 1130102784; // 0x435c0000 float:220.0 double:5.58344962E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = java.lang.Math.min(r4, r6);
        r6 = 1106247680; // 0x41f00000 float:30.0 double:5.465589745E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r4 = r4 + r41;
        r0 = r91;
        r91 = java.lang.Math.max(r0, r4);
    L_0x1683:
        r0 = r144;
        r1 = r95;
        r2 = r130;
        r3 = r91;
        r0.calcBackgroundWidth(r1, r2, r3);
        r15 = r138;
        goto L_0x0de3;
    L_0x1692:
        r4 = 0;
        goto L_0x1664;
    L_0x1694:
        r4 = org.telegram.messenger.AndroidUtilities.displaySize;
        r6 = r4.x;
        r0 = r144;
        r4 = r0.isChat;
        if (r4 == 0) goto L_0x16cc;
    L_0x169e:
        r4 = r145.needDrawAvatar();
        if (r4 == 0) goto L_0x16cc;
    L_0x16a4:
        r4 = r145.isOutOwner();
        if (r4 != 0) goto L_0x16cc;
    L_0x16aa:
        r4 = 1112539136; // 0x42500000 float:52.0 double:5.496673668E-315;
    L_0x16ac:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r6 - r4;
        r6 = 1130102784; // 0x435c0000 float:220.0 double:5.58344962E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = java.lang.Math.min(r4, r6);
        r6 = 1106247680; // 0x41f00000 float:30.0 double:5.465589745E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r4 = r4 + r41;
        r0 = r91;
        r91 = java.lang.Math.max(r0, r4);
        goto L_0x1683;
    L_0x16cc:
        r4 = 0;
        goto L_0x16ac;
    L_0x16ce:
        r4 = org.telegram.messenger.MessageObject.isMusicDocument(r66);
        if (r4 == 0) goto L_0x179f;
    L_0x16d4:
        r0 = r144;
        r4 = r0.backgroundWidth;
        r6 = 1092616192; // 0x41200000 float:10.0 double:5.398241246E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r0 = r144;
        r1 = r145;
        r68 = r0.createDocumentLayout(r4, r1);
        r0 = r144;
        r4 = r0.currentMessageObject;
        r4 = r4.textHeight;
        r6 = 1090519040; // 0x41000000 float:8.0 double:5.38787994E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r0 = r144;
        r6 = r0.linkPreviewHeight;
        r4 = r4 + r6;
        r0 = r144;
        r0.mediaOffsetY = r4;
        r0 = r144;
        r4 = r0.totalHeight;
        r6 = 1113587712; // 0x42600000 float:56.0 double:5.50185432E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r0 = r144;
        r0.totalHeight = r4;
        r0 = r144;
        r4 = r0.linkPreviewHeight;
        r6 = 1113587712; // 0x42600000 float:56.0 double:5.50185432E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r0 = r144;
        r0.linkPreviewHeight = r4;
        r4 = 1118568448; // 0x42ac0000 float:86.0 double:5.526462427E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r95 = r95 - r4;
        r4 = r68 + r41;
        r6 = 1119617024; // 0x42bc0000 float:94.0 double:5.53164308E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r0 = r91;
        r91 = java.lang.Math.max(r0, r4);
        r0 = r144;
        r4 = r0.songLayout;
        if (r4 == 0) goto L_0x1761;
    L_0x1738:
        r0 = r144;
        r4 = r0.songLayout;
        r4 = r4.getLineCount();
        if (r4 <= 0) goto L_0x1761;
    L_0x1742:
        r0 = r91;
        r4 = (float) r0;
        r0 = r144;
        r6 = r0.songLayout;
        r8 = 0;
        r6 = r6.getLineWidth(r8);
        r0 = r41;
        r8 = (float) r0;
        r6 = r6 + r8;
        r8 = 1118568448; // 0x42ac0000 float:86.0 double:5.526462427E-315;
        r8 = org.telegram.messenger.AndroidUtilities.dp(r8);
        r8 = (float) r8;
        r6 = r6 + r8;
        r4 = java.lang.Math.max(r4, r6);
        r0 = (int) r4;
        r91 = r0;
    L_0x1761:
        r0 = r144;
        r4 = r0.performerLayout;
        if (r4 == 0) goto L_0x1790;
    L_0x1767:
        r0 = r144;
        r4 = r0.performerLayout;
        r4 = r4.getLineCount();
        if (r4 <= 0) goto L_0x1790;
    L_0x1771:
        r0 = r91;
        r4 = (float) r0;
        r0 = r144;
        r6 = r0.performerLayout;
        r8 = 0;
        r6 = r6.getLineWidth(r8);
        r0 = r41;
        r8 = (float) r0;
        r6 = r6 + r8;
        r8 = 1118568448; // 0x42ac0000 float:86.0 double:5.526462427E-315;
        r8 = org.telegram.messenger.AndroidUtilities.dp(r8);
        r8 = (float) r8;
        r6 = r6 + r8;
        r4 = java.lang.Math.max(r4, r6);
        r0 = (int) r4;
        r91 = r0;
    L_0x1790:
        r0 = r144;
        r1 = r95;
        r2 = r130;
        r3 = r91;
        r0.calcBackgroundWidth(r1, r2, r3);
        r15 = r138;
        goto L_0x0de3;
    L_0x179f:
        r0 = r144;
        r4 = r0.backgroundWidth;
        r6 = 1126694912; // 0x43280000 float:168.0 double:5.566612494E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r0 = r144;
        r1 = r145;
        r0.createDocumentLayout(r4, r1);
        r4 = 1;
        r0 = r144;
        r0.drawImageButton = r4;
        r0 = r144;
        r4 = r0.drawPhotoImage;
        if (r4 == 0) goto L_0x17fb;
    L_0x17bc:
        r0 = r144;
        r4 = r0.totalHeight;
        r6 = 1120403456; // 0x42c80000 float:100.0 double:5.53552857E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r0 = r144;
        r0.totalHeight = r4;
        r0 = r144;
        r4 = r0.linkPreviewHeight;
        r6 = 1118568448; // 0x42ac0000 float:86.0 double:5.526462427E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r0 = r144;
        r0.linkPreviewHeight = r4;
        r0 = r144;
        r4 = r0.photoImage;
        r6 = 0;
        r0 = r144;
        r8 = r0.totalHeight;
        r0 = r144;
        r9 = r0.namesOffset;
        r8 = r8 + r9;
        r9 = 1118568448; // 0x42ac0000 float:86.0 double:5.526462427E-315;
        r9 = org.telegram.messenger.AndroidUtilities.dp(r9);
        r10 = 1118568448; // 0x42ac0000 float:86.0 double:5.526462427E-315;
        r10 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r4.setImageCoords(r6, r8, r9, r10);
        r15 = r138;
        goto L_0x0de3;
    L_0x17fb:
        r0 = r144;
        r4 = r0.currentMessageObject;
        r4 = r4.textHeight;
        r6 = 1090519040; // 0x41000000 float:8.0 double:5.38787994E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r0 = r144;
        r6 = r0.linkPreviewHeight;
        r4 = r4 + r6;
        r0 = r144;
        r0.mediaOffsetY = r4;
        r0 = r144;
        r4 = r0.photoImage;
        r6 = 0;
        r0 = r144;
        r8 = r0.totalHeight;
        r0 = r144;
        r9 = r0.namesOffset;
        r8 = r8 + r9;
        r9 = 1096810496; // 0x41600000 float:14.0 double:5.41896386E-315;
        r9 = org.telegram.messenger.AndroidUtilities.dp(r9);
        r8 = r8 - r9;
        r9 = 1113587712; // 0x42600000 float:56.0 double:5.50185432E-315;
        r9 = org.telegram.messenger.AndroidUtilities.dp(r9);
        r10 = 1113587712; // 0x42600000 float:56.0 double:5.50185432E-315;
        r10 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r4.setImageCoords(r6, r8, r9, r10);
        r0 = r144;
        r4 = r0.totalHeight;
        r6 = 1115684864; // 0x42800000 float:64.0 double:5.51221563E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r0 = r144;
        r0.totalHeight = r4;
        r0 = r144;
        r4 = r0.linkPreviewHeight;
        r6 = 1112014848; // 0x42480000 float:50.0 double:5.49408334E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r0 = r144;
        r0.linkPreviewHeight = r4;
        r15 = r138;
        goto L_0x0de3;
    L_0x1857:
        if (r106 == 0) goto L_0x18b0;
    L_0x1859:
        if (r134 == 0) goto L_0x18a9;
    L_0x185b:
        r4 = "photo";
        r0 = r134;
        r4 = r0.equals(r4);
        if (r4 == 0) goto L_0x18a9;
    L_0x1866:
        r4 = 1;
    L_0x1867:
        r0 = r144;
        r0.drawImageButton = r4;
        r0 = r145;
        r8 = r0.photoThumbs;
        r0 = r144;
        r4 = r0.drawImageButton;
        if (r4 == 0) goto L_0x18ab;
    L_0x1875:
        r4 = org.telegram.messenger.AndroidUtilities.getPhotoSize();
    L_0x1879:
        r0 = r144;
        r6 = r0.drawImageButton;
        if (r6 != 0) goto L_0x18ae;
    L_0x187f:
        r6 = 1;
    L_0x1880:
        r4 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r8, r4, r6);
        r0 = r144;
        r0.currentPhotoObject = r4;
        r0 = r145;
        r4 = r0.photoThumbs;
        r6 = 80;
        r4 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r4, r6);
        r0 = r144;
        r0.currentPhotoObjectThumb = r4;
        r0 = r144;
        r4 = r0.currentPhotoObjectThumb;
        r0 = r144;
        r6 = r0.currentPhotoObject;
        if (r4 != r6) goto L_0x3cdd;
    L_0x18a0:
        r4 = 0;
        r0 = r144;
        r0.currentPhotoObjectThumb = r4;
        r15 = r138;
        goto L_0x0de3;
    L_0x18a9:
        r4 = 0;
        goto L_0x1867;
    L_0x18ab:
        r4 = r93;
        goto L_0x1879;
    L_0x18ae:
        r6 = 0;
        goto L_0x1880;
    L_0x18b0:
        if (r138 == 0) goto L_0x3cdd;
    L_0x18b2:
        r0 = r138;
        r4 = r0.mime_type;
        r6 = "image/";
        r4 = r4.startsWith(r6);
        if (r4 != 0) goto L_0x3cd9;
    L_0x18bf:
        r15 = 0;
    L_0x18c0:
        r4 = 0;
        r0 = r144;
        r0.drawImageButton = r4;
        goto L_0x0de3;
    L_0x18c7:
        r4 = 0;
        goto L_0x0e32;
    L_0x18ca:
        r4 = org.telegram.messenger.AndroidUtilities.displaySize;
        r4 = r4.x;
        r4 = (float) r4;
        r6 = 1056964608; // 0x3f000000 float:0.5 double:5.222099017E-315;
        r4 = r4 * r6;
        r0 = (int) r4;
        r93 = r0;
        goto L_0x0e72;
    L_0x18d7:
        r0 = r144;
        r4 = r0.documentAttachType;
        r6 = 7;
        if (r4 != r6) goto L_0x0e72;
    L_0x18de:
        r93 = org.telegram.messenger.AndroidUtilities.roundMessageSize;
        r0 = r144;
        r4 = r0.photoImage;
        r6 = 1;
        r4.setAllowDecodeSingleFrame(r6);
        goto L_0x0e72;
    L_0x18ea:
        r4 = 0;
        goto L_0x0e7e;
    L_0x18ed:
        r4 = -1;
        r15.size = r4;
        goto L_0x0ea2;
    L_0x18f2:
        r0 = r144;
        r4 = r0.hasGamePreview;
        if (r4 != 0) goto L_0x18fe;
    L_0x18f8:
        r0 = r144;
        r4 = r0.hasInvoicePreview;
        if (r4 == 0) goto L_0x1922;
    L_0x18fe:
        r141 = 640; // 0x280 float:8.97E-43 double:3.16E-321;
        r78 = 360; // 0x168 float:5.04E-43 double:1.78E-321;
        r0 = r141;
        r4 = (float) r0;
        r6 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r6 = r93 - r6;
        r6 = (float) r6;
        r120 = r4 / r6;
        r0 = r141;
        r4 = (float) r0;
        r4 = r4 / r120;
        r0 = (int) r4;
        r141 = r0;
        r0 = r78;
        r4 = (float) r0;
        r4 = r4 / r120;
        r0 = (int) r4;
        r78 = r0;
        goto L_0x0eaf;
    L_0x1922:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r0 = r4.w;
        r141 = r0;
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r0 = r4.h;
        r78 = r0;
        r0 = r141;
        r4 = (float) r0;
        r6 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r6 = r93 - r6;
        r6 = (float) r6;
        r120 = r4 / r6;
        r0 = r141;
        r4 = (float) r0;
        r4 = r4 / r120;
        r0 = (int) r4;
        r141 = r0;
        r0 = r78;
        r4 = (float) r0;
        r4 = r4 / r120;
        r0 = (int) r4;
        r78 = r0;
        if (r7 == 0) goto L_0x1967;
    L_0x1952:
        if (r7 == 0) goto L_0x1979;
    L_0x1954:
        r4 = r7.toLowerCase();
        r6 = "instagram";
        r4 = r4.equals(r6);
        if (r4 != 0) goto L_0x1979;
    L_0x1961:
        r0 = r144;
        r4 = r0.documentAttachType;
        if (r4 != 0) goto L_0x1979;
    L_0x1967:
        r4 = org.telegram.messenger.AndroidUtilities.displaySize;
        r4 = r4.y;
        r4 = r4 / 3;
        r0 = r78;
        if (r0 <= r4) goto L_0x0eaf;
    L_0x1971:
        r4 = org.telegram.messenger.AndroidUtilities.displaySize;
        r4 = r4.y;
        r78 = r4 / 3;
        goto L_0x0eaf;
    L_0x1979:
        r4 = org.telegram.messenger.AndroidUtilities.displaySize;
        r4 = r4.y;
        r4 = r4 / 2;
        r0 = r78;
        if (r0 <= r4) goto L_0x0eaf;
    L_0x1983:
        r4 = org.telegram.messenger.AndroidUtilities.displaySize;
        r4 = r4.y;
        r78 = r4 / 2;
        goto L_0x0eaf;
    L_0x198b:
        r0 = r144;
        r4 = r0.totalHeight;
        r6 = 1094713344; // 0x41400000 float:12.0 double:5.408602553E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r6 = r6 + r78;
        r4 = r4 + r6;
        r0 = r144;
        r0.totalHeight = r4;
        r0 = r144;
        r4 = r0.linkPreviewHeight;
        r4 = r4 + r78;
        r0 = r144;
        r0.linkPreviewHeight = r4;
        goto L_0x0efb;
    L_0x19a8:
        r0 = r144;
        r4 = r0.documentAttachType;
        r6 = 6;
        if (r4 != r6) goto L_0x19eb;
    L_0x19af:
        r0 = r144;
        r0 = r0.photoImage;
        r16 = r0;
        r0 = r144;
        r0 = r0.documentAttach;
        r17 = r0;
        r18 = 0;
        r0 = r144;
        r0 = r0.currentPhotoFilter;
        r19 = r0;
        r20 = 0;
        r0 = r144;
        r4 = r0.currentPhotoObject;
        if (r4 == 0) goto L_0x19e8;
    L_0x19cb:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r0 = r4.location;
        r21 = r0;
    L_0x19d3:
        r22 = "b1";
        r0 = r144;
        r4 = r0.documentAttach;
        r0 = r4.size;
        r23 = r0;
        r24 = "webp";
        r25 = 1;
        r16.setImage(r17, r18, r19, r20, r21, r22, r23, r24, r25);
        goto L_0x0f64;
    L_0x19e8:
        r21 = 0;
        goto L_0x19d3;
    L_0x19eb:
        r0 = r144;
        r4 = r0.documentAttachType;
        r6 = 4;
        if (r4 != r6) goto L_0x1a2e;
    L_0x19f2:
        r0 = r144;
        r4 = r0.photoImage;
        r6 = 1;
        r4.setNeedsQualityThumb(r6);
        r0 = r144;
        r4 = r0.photoImage;
        r6 = 1;
        r4.setShouldGenerateQualityThumb(r6);
        r0 = r144;
        r4 = r0.photoImage;
        r0 = r145;
        r4.setParentMessageObject(r0);
        r0 = r144;
        r0 = r0.photoImage;
        r16 = r0;
        r17 = 0;
        r18 = 0;
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r0 = r4.location;
        r19 = r0;
        r0 = r144;
        r0 = r0.currentPhotoFilter;
        r20 = r0;
        r21 = 0;
        r22 = 0;
        r23 = 0;
        r16.setImage(r17, r18, r19, r20, r21, r22, r23);
        goto L_0x0f64;
    L_0x1a2e:
        r0 = r144;
        r4 = r0.documentAttachType;
        r6 = 2;
        if (r4 == r6) goto L_0x1a3c;
    L_0x1a35:
        r0 = r144;
        r4 = r0.documentAttachType;
        r6 = 7;
        if (r4 != r6) goto L_0x1b04;
    L_0x1a3c:
        r70 = org.telegram.messenger.FileLoader.getAttachFileName(r66);
        r49 = 0;
        r4 = org.telegram.messenger.MessageObject.isRoundVideoDocument(r66);
        if (r4 == 0) goto L_0x1ab9;
    L_0x1a48:
        r0 = r144;
        r4 = r0.photoImage;
        r6 = org.telegram.messenger.AndroidUtilities.roundMessageSize;
        r6 = r6 / 2;
        r4.setRoundRadius(r6);
        r0 = r144;
        r4 = r0.currentAccount;
        r4 = org.telegram.messenger.DownloadController.getInstance(r4);
        r0 = r144;
        r6 = r0.currentMessageObject;
        r49 = r4.canDownloadMedia(r6);
    L_0x1a63:
        r4 = r145.isSending();
        if (r4 != 0) goto L_0x1ad3;
    L_0x1a69:
        r4 = r145.isEditing();
        if (r4 != 0) goto L_0x1ad3;
    L_0x1a6f:
        r0 = r145;
        r4 = r0.mediaExists;
        if (r4 != 0) goto L_0x1a87;
    L_0x1a75:
        r0 = r144;
        r4 = r0.currentAccount;
        r4 = org.telegram.messenger.FileLoader.getInstance(r4);
        r0 = r70;
        r4 = r4.isLoadingFile(r0);
        if (r4 != 0) goto L_0x1a87;
    L_0x1a85:
        if (r49 == 0) goto L_0x1ad3;
    L_0x1a87:
        r4 = 0;
        r0 = r144;
        r0.photoNotSet = r4;
        r0 = r144;
        r0 = r0.photoImage;
        r16 = r0;
        r18 = 0;
        r0 = r144;
        r4 = r0.currentPhotoObject;
        if (r4 == 0) goto L_0x1ad0;
    L_0x1a9a:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r0 = r4.location;
        r19 = r0;
    L_0x1aa2:
        r0 = r144;
        r0 = r0.currentPhotoFilterThumb;
        r20 = r0;
        r0 = r66;
        r0 = r0.size;
        r21 = r0;
        r22 = 0;
        r23 = 0;
        r17 = r66;
        r16.setImage(r17, r18, r19, r20, r21, r22, r23);
        goto L_0x0f64;
    L_0x1ab9:
        r4 = org.telegram.messenger.MessageObject.isNewGifDocument(r66);
        if (r4 == 0) goto L_0x1a63;
    L_0x1abf:
        r0 = r144;
        r4 = r0.currentAccount;
        r4 = org.telegram.messenger.DownloadController.getInstance(r4);
        r0 = r144;
        r6 = r0.currentMessageObject;
        r49 = r4.canDownloadMedia(r6);
        goto L_0x1a63;
    L_0x1ad0:
        r19 = 0;
        goto L_0x1aa2;
    L_0x1ad3:
        r4 = 1;
        r0 = r144;
        r0.photoNotSet = r4;
        r0 = r144;
        r0 = r0.photoImage;
        r16 = r0;
        r17 = 0;
        r18 = 0;
        r0 = r144;
        r4 = r0.currentPhotoObject;
        if (r4 == 0) goto L_0x1b01;
    L_0x1ae8:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r0 = r4.location;
        r19 = r0;
    L_0x1af0:
        r0 = r144;
        r0 = r0.currentPhotoFilterThumb;
        r20 = r0;
        r21 = 0;
        r22 = 0;
        r23 = 0;
        r16.setImage(r17, r18, r19, r20, r21, r22, r23);
        goto L_0x0f64;
    L_0x1b01:
        r19 = 0;
        goto L_0x1af0;
    L_0x1b04:
        r0 = r145;
        r0 = r0.mediaExists;
        r107 = r0;
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r70 = org.telegram.messenger.FileLoader.getAttachFileName(r4);
        r0 = r144;
        r4 = r0.hasGamePreview;
        if (r4 != 0) goto L_0x1b3c;
    L_0x1b18:
        if (r107 != 0) goto L_0x1b3c;
    L_0x1b1a:
        r0 = r144;
        r4 = r0.currentAccount;
        r4 = org.telegram.messenger.DownloadController.getInstance(r4);
        r0 = r144;
        r6 = r0.currentMessageObject;
        r4 = r4.canDownloadMedia(r6);
        if (r4 != 0) goto L_0x1b3c;
    L_0x1b2c:
        r0 = r144;
        r4 = r0.currentAccount;
        r4 = org.telegram.messenger.FileLoader.getInstance(r4);
        r0 = r70;
        r4 = r4.isLoadingFile(r0);
        if (r4 == 0) goto L_0x1b77;
    L_0x1b3c:
        r4 = 0;
        r0 = r144;
        r0.photoNotSet = r4;
        r0 = r144;
        r0 = r0.photoImage;
        r16 = r0;
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r0 = r4.location;
        r17 = r0;
        r0 = r144;
        r0 = r0.currentPhotoFilter;
        r18 = r0;
        r0 = r144;
        r4 = r0.currentPhotoObjectThumb;
        if (r4 == 0) goto L_0x1b74;
    L_0x1b5b:
        r0 = r144;
        r4 = r0.currentPhotoObjectThumb;
        r0 = r4.location;
        r19 = r0;
    L_0x1b63:
        r0 = r144;
        r0 = r0.currentPhotoFilterThumb;
        r20 = r0;
        r21 = 0;
        r22 = 0;
        r23 = 0;
        r16.setImage(r17, r18, r19, r20, r21, r22, r23);
        goto L_0x0f64;
    L_0x1b74:
        r19 = 0;
        goto L_0x1b63;
    L_0x1b77:
        r4 = 1;
        r0 = r144;
        r0.photoNotSet = r4;
        r0 = r144;
        r4 = r0.currentPhotoObjectThumb;
        if (r4 == 0) goto L_0x1bb9;
    L_0x1b82:
        r0 = r144;
        r0 = r0.photoImage;
        r16 = r0;
        r17 = 0;
        r18 = 0;
        r0 = r144;
        r4 = r0.currentPhotoObjectThumb;
        r0 = r4.location;
        r19 = r0;
        r4 = java.util.Locale.US;
        r6 = "%d_%d_b";
        r8 = 2;
        r8 = new java.lang.Object[r8];
        r9 = 0;
        r10 = java.lang.Integer.valueOf(r141);
        r8[r9] = r10;
        r9 = 1;
        r10 = java.lang.Integer.valueOf(r78);
        r8[r9] = r10;
        r20 = java.lang.String.format(r4, r6, r8);
        r21 = 0;
        r22 = 0;
        r23 = 0;
        r16.setImage(r17, r18, r19, r20, r21, r22, r23);
        goto L_0x0f64;
    L_0x1bb9:
        r0 = r144;
        r6 = r0.photoImage;
        r4 = 0;
        r4 = (android.graphics.drawable.Drawable) r4;
        r6.setImageBitmap(r4);
        goto L_0x0f64;
    L_0x1bc5:
        r0 = r144;
        r4 = r0.hasGamePreview;
        if (r4 == 0) goto L_0x0fc3;
    L_0x1bcb:
        r4 = "AttachGame";
        r6 = 2131493048; // 0x7f0c00b8 float:1.8609565E38 double:1.0530974894E-314;
        r4 = org.telegram.messenger.LocaleController.getString(r4, r6);
        r5 = r4.toUpperCase();
        r4 = org.telegram.ui.ActionBar.Theme.chat_gamePaint;
        r4 = r4.measureText(r5);
        r8 = (double) r4;
        r8 = java.lang.Math.ceil(r8);
        r4 = (int) r8;
        r0 = r144;
        r0.durationWidth = r4;
        r16 = new android.text.StaticLayout;
        r18 = org.telegram.ui.ActionBar.Theme.chat_gamePaint;
        r0 = r144;
        r0 = r0.durationWidth;
        r19 = r0;
        r20 = android.text.Layout.Alignment.ALIGN_NORMAL;
        r21 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r22 = 0;
        r23 = 0;
        r17 = r5;
        r16.<init>(r17, r18, r19, r20, r21, r22, r23);
        r0 = r16;
        r1 = r144;
        r1.videoInfoLayout = r0;
        goto L_0x0fc3;
    L_0x1c08:
        r0 = r144;
        r6 = r0.photoImage;
        r4 = 0;
        r4 = (android.graphics.drawable.Drawable) r4;
        r6.setImageBitmap(r4);
        r0 = r144;
        r4 = r0.linkPreviewHeight;
        r6 = 1086324736; // 0x40c00000 float:6.0 double:5.367157323E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r0 = r144;
        r0.linkPreviewHeight = r4;
        r0 = r144;
        r4 = r0.totalHeight;
        r6 = 1082130432; // 0x40800000 float:4.0 double:5.34643471E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r0 = r144;
        r0.totalHeight = r4;
        goto L_0x0fc3;
    L_0x1c32:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r4 = r4.test;
        if (r4 == 0) goto L_0x1c4c;
    L_0x1c3c:
        r4 = "PaymentTestInvoice";
        r6 = 2131494377; // 0x7f0c05e9 float:1.861226E38 double:1.053098146E-314;
        r4 = org.telegram.messenger.LocaleController.getString(r4, r6);
        r5 = r4.toUpperCase();
        goto L_0x0fe3;
    L_0x1c4c:
        r4 = "PaymentInvoice";
        r6 = 2131494346; // 0x7f0c05ca float:1.8612198E38 double:1.0530981307E-314;
        r4 = org.telegram.messenger.LocaleController.getString(r4, r6);
        r5 = r4.toUpperCase();
        goto L_0x0fe3;
    L_0x1c5c:
        r4 = 0;
        goto L_0x108d;
    L_0x1c5f:
        r0 = r144;
        r4 = r0.durationWidth;
        r4 = r4 + r131;
        r0 = r91;
        r91 = java.lang.Math.max(r4, r0);
        goto L_0x10b9;
    L_0x1c6d:
        r0 = r144;
        r6 = r0.photoImage;
        r4 = 0;
        r4 = (android.graphics.drawable.Drawable) r4;
        r6.setImageBitmap(r4);
        r0 = r144;
        r1 = r95;
        r2 = r130;
        r3 = r91;
        r0.calcBackgroundWidth(r1, r2, r3);
        goto L_0x10f6;
    L_0x1c84:
        r0 = r145;
        r4 = r0.type;
        r6 = 16;
        if (r4 != r6) goto L_0x1e3d;
    L_0x1c8c:
        r4 = 0;
        r0 = r144;
        r0.drawName = r4;
        r4 = 0;
        r0 = r144;
        r0.drawForwardedName = r4;
        r4 = 0;
        r0 = r144;
        r0.drawPhotoImage = r4;
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 == 0) goto L_0x1dd2;
    L_0x1ca1:
        r6 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        r0 = r144;
        r4 = r0.isChat;
        if (r4 == 0) goto L_0x1dce;
    L_0x1cab:
        r4 = r145.needDrawAvatar();
        if (r4 == 0) goto L_0x1dce;
    L_0x1cb1:
        r4 = r145.isOutOwner();
        if (r4 != 0) goto L_0x1dce;
    L_0x1cb7:
        r4 = 1120665600; // 0x42cc0000 float:102.0 double:5.536823734E-315;
    L_0x1cb9:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r6 - r4;
        r6 = 1132920832; // 0x43870000 float:270.0 double:5.597372625E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = java.lang.Math.min(r4, r6);
        r0 = r144;
        r0.backgroundWidth = r4;
    L_0x1ccd:
        r0 = r144;
        r4 = r0.backgroundWidth;
        r6 = 1106771968; // 0x41f80000 float:31.0 double:5.46818007E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r0 = r144;
        r0.availableTimeWidth = r4;
        r4 = r144.getMaxNameWidth();
        r6 = 1112014848; // 0x42480000 float:50.0 double:5.49408334E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r95 = r4 - r6;
        if (r95 >= 0) goto L_0x1cf0;
    L_0x1cea:
        r4 = 1092616192; // 0x41200000 float:10.0 double:5.398241246E-315;
        r95 = org.telegram.messenger.AndroidUtilities.dp(r4);
    L_0x1cf0:
        r4 = org.telegram.messenger.LocaleController.getInstance();
        r4 = r4.formatterDay;
        r0 = r145;
        r6 = r0.messageOwner;
        r6 = r6.date;
        r8 = (long) r6;
        r20 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
        r8 = r8 * r20;
        r128 = r4.format(r8);
        r0 = r145;
        r4 = r0.messageOwner;
        r0 = r4.action;
        r56 = r0;
        r56 = (org.telegram.tgnet.TLRPC.TL_messageActionPhoneCall) r56;
        r0 = r56;
        r4 = r0.reason;
        r0 = r4 instanceof org.telegram.tgnet.TLRPC.TL_phoneCallDiscardReasonMissed;
        r81 = r0;
        r4 = r145.isOutOwner();
        if (r4 == 0) goto L_0x1e0f;
    L_0x1d1d:
        if (r81 == 0) goto L_0x1e03;
    L_0x1d1f:
        r4 = "CallMessageOutgoingMissed";
        r6 = 2131493136; // 0x7f0c0110 float:1.8609744E38 double:1.053097533E-314;
        r126 = org.telegram.messenger.LocaleController.getString(r4, r6);
    L_0x1d29:
        r0 = r56;
        r4 = r0.duration;
        if (r4 <= 0) goto L_0x1d51;
    L_0x1d2f:
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r0 = r128;
        r4 = r4.append(r0);
        r6 = ", ";
        r4 = r4.append(r6);
        r0 = r56;
        r6 = r0.duration;
        r6 = org.telegram.messenger.LocaleController.formatCallDuration(r6);
        r4 = r4.append(r6);
        r128 = r4.toString();
    L_0x1d51:
        r18 = new android.text.StaticLayout;
        r4 = org.telegram.ui.ActionBar.Theme.chat_audioTitlePaint;
        r0 = r95;
        r6 = (float) r0;
        r8 = android.text.TextUtils.TruncateAt.END;
        r0 = r126;
        r19 = android.text.TextUtils.ellipsize(r0, r4, r6, r8);
        r20 = org.telegram.ui.ActionBar.Theme.chat_audioTitlePaint;
        r4 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r21 = r95 + r4;
        r22 = android.text.Layout.Alignment.ALIGN_NORMAL;
        r23 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r24 = 0;
        r25 = 0;
        r18.<init>(r19, r20, r21, r22, r23, r24, r25);
        r0 = r18;
        r1 = r144;
        r1.titleLayout = r0;
        r18 = new android.text.StaticLayout;
        r4 = org.telegram.ui.ActionBar.Theme.chat_contactPhonePaint;
        r0 = r95;
        r6 = (float) r0;
        r8 = android.text.TextUtils.TruncateAt.END;
        r0 = r128;
        r19 = android.text.TextUtils.ellipsize(r0, r4, r6, r8);
        r20 = org.telegram.ui.ActionBar.Theme.chat_contactPhonePaint;
        r4 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r21 = r95 + r4;
        r22 = android.text.Layout.Alignment.ALIGN_NORMAL;
        r23 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r24 = 0;
        r25 = 0;
        r18.<init>(r19, r20, r21, r22, r23, r24, r25);
        r0 = r18;
        r1 = r144;
        r1.docTitleLayout = r0;
        r144.setMessageObjectInternal(r145);
        r4 = 1115815936; // 0x42820000 float:65.0 double:5.51286321E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r0 = r144;
        r6 = r0.namesOffset;
        r4 = r4 + r6;
        r0 = r144;
        r0.totalHeight = r4;
        r0 = r144;
        r4 = r0.drawPinnedTop;
        if (r4 == 0) goto L_0x10f6;
    L_0x1dbd:
        r0 = r144;
        r4 = r0.namesOffset;
        r6 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r0 = r144;
        r0.namesOffset = r4;
        goto L_0x10f6;
    L_0x1dce:
        r4 = 1112014848; // 0x42480000 float:50.0 double:5.49408334E-315;
        goto L_0x1cb9;
    L_0x1dd2:
        r4 = org.telegram.messenger.AndroidUtilities.displaySize;
        r6 = r4.x;
        r0 = r144;
        r4 = r0.isChat;
        if (r4 == 0) goto L_0x1e00;
    L_0x1ddc:
        r4 = r145.needDrawAvatar();
        if (r4 == 0) goto L_0x1e00;
    L_0x1de2:
        r4 = r145.isOutOwner();
        if (r4 != 0) goto L_0x1e00;
    L_0x1de8:
        r4 = 1120665600; // 0x42cc0000 float:102.0 double:5.536823734E-315;
    L_0x1dea:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r6 - r4;
        r6 = 1132920832; // 0x43870000 float:270.0 double:5.597372625E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = java.lang.Math.min(r4, r6);
        r0 = r144;
        r0.backgroundWidth = r4;
        goto L_0x1ccd;
    L_0x1e00:
        r4 = 1112014848; // 0x42480000 float:50.0 double:5.49408334E-315;
        goto L_0x1dea;
    L_0x1e03:
        r4 = "CallMessageOutgoing";
        r6 = 2131493135; // 0x7f0c010f float:1.8609742E38 double:1.0530975323E-314;
        r126 = org.telegram.messenger.LocaleController.getString(r4, r6);
        goto L_0x1d29;
    L_0x1e0f:
        if (r81 == 0) goto L_0x1e1d;
    L_0x1e11:
        r4 = "CallMessageIncomingMissed";
        r6 = 2131493134; // 0x7f0c010e float:1.860974E38 double:1.053097532E-314;
        r126 = org.telegram.messenger.LocaleController.getString(r4, r6);
        goto L_0x1d29;
    L_0x1e1d:
        r0 = r56;
        r4 = r0.reason;
        r4 = r4 instanceof org.telegram.tgnet.TLRPC.TL_phoneCallDiscardReasonBusy;
        if (r4 == 0) goto L_0x1e31;
    L_0x1e25:
        r4 = "CallMessageIncomingDeclined";
        r6 = 2131493133; // 0x7f0c010d float:1.8609738E38 double:1.0530975314E-314;
        r126 = org.telegram.messenger.LocaleController.getString(r4, r6);
        goto L_0x1d29;
    L_0x1e31:
        r4 = "CallMessageIncoming";
        r6 = 2131493132; // 0x7f0c010c float:1.8609736E38 double:1.053097531E-314;
        r126 = org.telegram.messenger.LocaleController.getString(r4, r6);
        goto L_0x1d29;
    L_0x1e3d:
        r0 = r145;
        r4 = r0.type;
        r6 = 12;
        if (r4 != r6) goto L_0x20d7;
    L_0x1e45:
        r4 = 0;
        r0 = r144;
        r0.drawName = r4;
        r4 = 1;
        r0 = r144;
        r0.drawForwardedName = r4;
        r4 = 1;
        r0 = r144;
        r0.drawPhotoImage = r4;
        r0 = r144;
        r4 = r0.photoImage;
        r6 = 1102053376; // 0x41b00000 float:22.0 double:5.44486713E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4.setRoundRadius(r6);
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 == 0) goto L_0x2006;
    L_0x1e67:
        r6 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        r0 = r144;
        r4 = r0.isChat;
        if (r4 == 0) goto L_0x2002;
    L_0x1e71:
        r4 = r145.needDrawAvatar();
        if (r4 == 0) goto L_0x2002;
    L_0x1e77:
        r4 = r145.isOutOwner();
        if (r4 != 0) goto L_0x2002;
    L_0x1e7d:
        r4 = 1120665600; // 0x42cc0000 float:102.0 double:5.536823734E-315;
    L_0x1e7f:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r6 - r4;
        r6 = 1132920832; // 0x43870000 float:270.0 double:5.597372625E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = java.lang.Math.min(r4, r6);
        r0 = r144;
        r0.backgroundWidth = r4;
    L_0x1e93:
        r0 = r144;
        r4 = r0.backgroundWidth;
        r6 = 1106771968; // 0x41f80000 float:31.0 double:5.46818007E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r0 = r144;
        r0.availableTimeWidth = r4;
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r0 = r4.user_id;
        r135 = r0;
        r0 = r144;
        r4 = r0.currentAccount;
        r4 = org.telegram.messenger.MessagesController.getInstance(r4);
        r6 = java.lang.Integer.valueOf(r135);
        r136 = r4.getUser(r6);
        r4 = r144.getMaxNameWidth();
        r6 = 1117782016; // 0x42a00000 float:80.0 double:5.522576936E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r95 = r4 - r6;
        if (r95 >= 0) goto L_0x1ed0;
    L_0x1eca:
        r4 = 1092616192; // 0x41200000 float:10.0 double:5.398241246E-315;
        r95 = org.telegram.messenger.AndroidUtilities.dp(r4);
    L_0x1ed0:
        r19 = 0;
        if (r136 == 0) goto L_0x1eeb;
    L_0x1ed4:
        r0 = r136;
        r4 = r0.photo;
        if (r4 == 0) goto L_0x1ee2;
    L_0x1eda:
        r0 = r136;
        r4 = r0.photo;
        r0 = r4.photo_small;
        r19 = r0;
    L_0x1ee2:
        r0 = r144;
        r4 = r0.contactAvatarDrawable;
        r0 = r136;
        r4.setInfo(r0);
    L_0x1eeb:
        r0 = r144;
        r0 = r0.photoImage;
        r18 = r0;
        r20 = "50_50";
        if (r136 == 0) goto L_0x2037;
    L_0x1ef6:
        r0 = r144;
        r0 = r0.contactAvatarDrawable;
        r21 = r0;
    L_0x1efc:
        r22 = 0;
        r23 = 0;
        r18.setImage(r19, r20, r21, r22, r23);
        r0 = r145;
        r4 = r0.vCardData;
        r4 = android.text.TextUtils.isEmpty(r4);
        if (r4 != 0) goto L_0x2046;
    L_0x1f0d:
        r0 = r145;
        r0 = r0.vCardData;
        r105 = r0;
        r4 = 1;
        r0 = r144;
        r0.drawInstantView = r4;
        r4 = 5;
        r0 = r144;
        r0.drawInstantViewType = r4;
    L_0x1f1d:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r4 = r4.first_name;
        r0 = r145;
        r6 = r0.messageOwner;
        r6 = r6.media;
        r6 = r6.last_name;
        r4 = org.telegram.messenger.ContactsController.formatName(r4, r6);
        r6 = 10;
        r8 = 32;
        r61 = r4.replace(r6, r8);
        r4 = r61.length();
        if (r4 != 0) goto L_0x1f4e;
    L_0x1f3f:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r0 = r4.phone_number;
        r61 = r0;
        if (r61 != 0) goto L_0x1f4e;
    L_0x1f4b:
        r61 = "";
    L_0x1f4e:
        r20 = new android.text.StaticLayout;
        r4 = org.telegram.ui.ActionBar.Theme.chat_contactNamePaint;
        r0 = r95;
        r6 = (float) r0;
        r8 = android.text.TextUtils.TruncateAt.END;
        r0 = r61;
        r21 = android.text.TextUtils.ellipsize(r0, r4, r6, r8);
        r22 = org.telegram.ui.ActionBar.Theme.chat_contactNamePaint;
        r4 = 1082130432; // 0x40800000 float:4.0 double:5.34643471E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r23 = r95 + r4;
        r24 = android.text.Layout.Alignment.ALIGN_NORMAL;
        r25 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r26 = 0;
        r27 = 0;
        r20.<init>(r21, r22, r23, r24, r25, r26, r27);
        r0 = r20;
        r1 = r144;
        r1.titleLayout = r0;
        r20 = new android.text.StaticLayout;
        r22 = org.telegram.ui.ActionBar.Theme.chat_contactPhonePaint;
        r4 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r23 = r95 + r4;
        r24 = android.text.Layout.Alignment.ALIGN_NORMAL;
        r25 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r4 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r0 = (float) r4;
        r26 = r0;
        r27 = 0;
        r21 = r105;
        r20.<init>(r21, r22, r23, r24, r25, r26, r27);
        r0 = r20;
        r1 = r144;
        r1.docTitleLayout = r0;
        r144.setMessageObjectInternal(r145);
        r0 = r144;
        r4 = r0.drawForwardedName;
        if (r4 == 0) goto L_0x2070;
    L_0x1fa7:
        r4 = r145.needDrawForwarded();
        if (r4 == 0) goto L_0x2070;
    L_0x1fad:
        r0 = r144;
        r4 = r0.currentPosition;
        if (r4 == 0) goto L_0x1fbb;
    L_0x1fb3:
        r0 = r144;
        r4 = r0.currentPosition;
        r4 = r4.minY;
        if (r4 != 0) goto L_0x2070;
    L_0x1fbb:
        r0 = r144;
        r4 = r0.namesOffset;
        r6 = 1084227584; // 0x40a00000 float:5.0 double:5.356796015E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r0 = r144;
        r0.namesOffset = r4;
    L_0x1fca:
        r4 = 1113325568; // 0x425c0000 float:55.0 double:5.50055916E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r0 = r144;
        r6 = r0.namesOffset;
        r4 = r4 + r6;
        r0 = r144;
        r6 = r0.docTitleLayout;
        r6 = r6.getHeight();
        r4 = r4 + r6;
        r0 = r144;
        r0.totalHeight = r4;
        r0 = r144;
        r4 = r0.drawPinnedTop;
        if (r4 == 0) goto L_0x1ff7;
    L_0x1fe8:
        r0 = r144;
        r4 = r0.namesOffset;
        r6 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r0 = r144;
        r0.namesOffset = r4;
    L_0x1ff7:
        r0 = r144;
        r4 = r0.drawInstantView;
        if (r4 == 0) goto L_0x208f;
    L_0x1ffd:
        r144.createInstantViewButton();
        goto L_0x10f6;
    L_0x2002:
        r4 = 1112014848; // 0x42480000 float:50.0 double:5.49408334E-315;
        goto L_0x1e7f;
    L_0x2006:
        r4 = org.telegram.messenger.AndroidUtilities.displaySize;
        r6 = r4.x;
        r0 = r144;
        r4 = r0.isChat;
        if (r4 == 0) goto L_0x2034;
    L_0x2010:
        r4 = r145.needDrawAvatar();
        if (r4 == 0) goto L_0x2034;
    L_0x2016:
        r4 = r145.isOutOwner();
        if (r4 != 0) goto L_0x2034;
    L_0x201c:
        r4 = 1120665600; // 0x42cc0000 float:102.0 double:5.536823734E-315;
    L_0x201e:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r6 - r4;
        r6 = 1132920832; // 0x43870000 float:270.0 double:5.597372625E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = java.lang.Math.min(r4, r6);
        r0 = r144;
        r0.backgroundWidth = r4;
        goto L_0x1e93;
    L_0x2034:
        r4 = 1112014848; // 0x42480000 float:50.0 double:5.49408334E-315;
        goto L_0x201e;
    L_0x2037:
        r6 = org.telegram.ui.ActionBar.Theme.chat_contactDrawable;
        r4 = r145.isOutOwner();
        if (r4 == 0) goto L_0x2044;
    L_0x203f:
        r4 = 1;
    L_0x2040:
        r21 = r6[r4];
        goto L_0x1efc;
    L_0x2044:
        r4 = 0;
        goto L_0x2040;
    L_0x2046:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r0 = r4.phone_number;
        r105 = r0;
        r4 = android.text.TextUtils.isEmpty(r105);
        if (r4 != 0) goto L_0x2064;
    L_0x2056:
        r4 = org.telegram.PhoneFormat.PhoneFormat.getInstance();
        r105 = (java.lang.String) r105;
        r0 = r105;
        r105 = r4.format(r0);
        goto L_0x1f1d;
    L_0x2064:
        r4 = "NumberUnknown";
        r6 = 2131494092; // 0x7f0c04cc float:1.8611683E38 double:1.053098005E-314;
        r105 = org.telegram.messenger.LocaleController.getString(r4, r6);
        goto L_0x1f1d;
    L_0x2070:
        r0 = r144;
        r4 = r0.drawNameLayout;
        if (r4 == 0) goto L_0x1fca;
    L_0x2076:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.reply_to_msg_id;
        if (r4 != 0) goto L_0x1fca;
    L_0x207e:
        r0 = r144;
        r4 = r0.namesOffset;
        r6 = 1088421888; // 0x40e00000 float:7.0 double:5.37751863E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r0 = r144;
        r0.namesOffset = r4;
        goto L_0x1fca;
    L_0x208f:
        r0 = r144;
        r4 = r0.docTitleLayout;
        r4 = r4.getLineCount();
        if (r4 <= 0) goto L_0x10f6;
    L_0x2099:
        r0 = r144;
        r4 = r0.backgroundWidth;
        r6 = 1121714176; // 0x42dc0000 float:110.0 double:5.54200439E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r0 = r144;
        r6 = r0.docTitleLayout;
        r0 = r144;
        r8 = r0.docTitleLayout;
        r8 = r8.getLineCount();
        r8 = r8 + -1;
        r6 = r6.getLineWidth(r8);
        r8 = (double) r6;
        r8 = java.lang.Math.ceil(r8);
        r6 = (int) r8;
        r129 = r4 - r6;
        r0 = r144;
        r4 = r0.timeWidth;
        r0 = r129;
        if (r0 >= r4) goto L_0x10f6;
    L_0x20c6:
        r0 = r144;
        r4 = r0.totalHeight;
        r6 = 1090519040; // 0x41000000 float:8.0 double:5.38787994E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r0 = r144;
        r0.totalHeight = r4;
        goto L_0x10f6;
    L_0x20d7:
        r0 = r145;
        r4 = r0.type;
        r6 = 2;
        if (r4 != r6) goto L_0x217c;
    L_0x20de:
        r4 = 1;
        r0 = r144;
        r0.drawForwardedName = r4;
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 == 0) goto L_0x214c;
    L_0x20e9:
        r6 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        r0 = r144;
        r4 = r0.isChat;
        if (r4 == 0) goto L_0x2149;
    L_0x20f3:
        r4 = r145.needDrawAvatar();
        if (r4 == 0) goto L_0x2149;
    L_0x20f9:
        r4 = r145.isOutOwner();
        if (r4 != 0) goto L_0x2149;
    L_0x20ff:
        r4 = 1120665600; // 0x42cc0000 float:102.0 double:5.536823734E-315;
    L_0x2101:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r6 - r4;
        r6 = 1132920832; // 0x43870000 float:270.0 double:5.597372625E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = java.lang.Math.min(r4, r6);
        r0 = r144;
        r0.backgroundWidth = r4;
    L_0x2115:
        r0 = r144;
        r4 = r0.backgroundWidth;
        r0 = r144;
        r1 = r145;
        r0.createDocumentLayout(r4, r1);
        r144.setMessageObjectInternal(r145);
        r4 = 1116471296; // 0x428c0000 float:70.0 double:5.51610112E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r0 = r144;
        r6 = r0.namesOffset;
        r4 = r4 + r6;
        r0 = r144;
        r0.totalHeight = r4;
        r0 = r144;
        r4 = r0.drawPinnedTop;
        if (r4 == 0) goto L_0x10f6;
    L_0x2138:
        r0 = r144;
        r4 = r0.namesOffset;
        r6 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r0 = r144;
        r0.namesOffset = r4;
        goto L_0x10f6;
    L_0x2149:
        r4 = 1112014848; // 0x42480000 float:50.0 double:5.49408334E-315;
        goto L_0x2101;
    L_0x214c:
        r4 = org.telegram.messenger.AndroidUtilities.displaySize;
        r6 = r4.x;
        r0 = r144;
        r4 = r0.isChat;
        if (r4 == 0) goto L_0x2179;
    L_0x2156:
        r4 = r145.needDrawAvatar();
        if (r4 == 0) goto L_0x2179;
    L_0x215c:
        r4 = r145.isOutOwner();
        if (r4 != 0) goto L_0x2179;
    L_0x2162:
        r4 = 1120665600; // 0x42cc0000 float:102.0 double:5.536823734E-315;
    L_0x2164:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r6 - r4;
        r6 = 1132920832; // 0x43870000 float:270.0 double:5.597372625E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = java.lang.Math.min(r4, r6);
        r0 = r144;
        r0.backgroundWidth = r4;
        goto L_0x2115;
    L_0x2179:
        r4 = 1112014848; // 0x42480000 float:50.0 double:5.49408334E-315;
        goto L_0x2164;
    L_0x217c:
        r0 = r145;
        r4 = r0.type;
        r6 = 14;
        if (r4 != r6) goto L_0x221d;
    L_0x2184:
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 == 0) goto L_0x21ed;
    L_0x218a:
        r6 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        r0 = r144;
        r4 = r0.isChat;
        if (r4 == 0) goto L_0x21ea;
    L_0x2194:
        r4 = r145.needDrawAvatar();
        if (r4 == 0) goto L_0x21ea;
    L_0x219a:
        r4 = r145.isOutOwner();
        if (r4 != 0) goto L_0x21ea;
    L_0x21a0:
        r4 = 1120665600; // 0x42cc0000 float:102.0 double:5.536823734E-315;
    L_0x21a2:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r6 - r4;
        r6 = 1132920832; // 0x43870000 float:270.0 double:5.597372625E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = java.lang.Math.min(r4, r6);
        r0 = r144;
        r0.backgroundWidth = r4;
    L_0x21b6:
        r0 = r144;
        r4 = r0.backgroundWidth;
        r0 = r144;
        r1 = r145;
        r0.createDocumentLayout(r4, r1);
        r144.setMessageObjectInternal(r145);
        r4 = 1118044160; // 0x42a40000 float:82.0 double:5.5238721E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r0 = r144;
        r6 = r0.namesOffset;
        r4 = r4 + r6;
        r0 = r144;
        r0.totalHeight = r4;
        r0 = r144;
        r4 = r0.drawPinnedTop;
        if (r4 == 0) goto L_0x10f6;
    L_0x21d9:
        r0 = r144;
        r4 = r0.namesOffset;
        r6 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r0 = r144;
        r0.namesOffset = r4;
        goto L_0x10f6;
    L_0x21ea:
        r4 = 1112014848; // 0x42480000 float:50.0 double:5.49408334E-315;
        goto L_0x21a2;
    L_0x21ed:
        r4 = org.telegram.messenger.AndroidUtilities.displaySize;
        r6 = r4.x;
        r0 = r144;
        r4 = r0.isChat;
        if (r4 == 0) goto L_0x221a;
    L_0x21f7:
        r4 = r145.needDrawAvatar();
        if (r4 == 0) goto L_0x221a;
    L_0x21fd:
        r4 = r145.isOutOwner();
        if (r4 != 0) goto L_0x221a;
    L_0x2203:
        r4 = 1120665600; // 0x42cc0000 float:102.0 double:5.536823734E-315;
    L_0x2205:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r6 - r4;
        r6 = 1132920832; // 0x43870000 float:270.0 double:5.597372625E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = java.lang.Math.min(r4, r6);
        r0 = r144;
        r0.backgroundWidth = r4;
        goto L_0x21b6;
    L_0x221a:
        r4 = 1112014848; // 0x42480000 float:50.0 double:5.49408334E-315;
        goto L_0x2205;
    L_0x221d:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.fwd_from;
        if (r4 == 0) goto L_0x244c;
    L_0x2225:
        r0 = r145;
        r4 = r0.type;
        r6 = 13;
        if (r4 == r6) goto L_0x244c;
    L_0x222d:
        r4 = 1;
    L_0x222e:
        r0 = r144;
        r0.drawForwardedName = r4;
        r0 = r145;
        r4 = r0.type;
        r6 = 9;
        if (r4 == r6) goto L_0x244f;
    L_0x223a:
        r4 = 1;
    L_0x223b:
        r0 = r144;
        r0.mediaBackground = r4;
        r4 = 1;
        r0 = r144;
        r0.drawImageButton = r4;
        r4 = 1;
        r0 = r144;
        r0.drawPhotoImage = r4;
        r109 = 0;
        r108 = 0;
        r42 = 0;
        r0 = r145;
        r4 = r0.gifState;
        r6 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r4 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1));
        if (r4 == 0) goto L_0x2272;
    L_0x2259:
        r4 = org.telegram.messenger.SharedConfig.autoplayGifs;
        if (r4 != 0) goto L_0x2272;
    L_0x225d:
        r0 = r145;
        r4 = r0.type;
        r6 = 8;
        if (r4 == r6) goto L_0x226c;
    L_0x2265:
        r0 = r145;
        r4 = r0.type;
        r6 = 5;
        if (r4 != r6) goto L_0x2272;
    L_0x226c:
        r4 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r0 = r145;
        r0.gifState = r4;
    L_0x2272:
        r4 = r145.isRoundVideo();
        if (r4 == 0) goto L_0x2455;
    L_0x2278:
        r0 = r144;
        r4 = r0.photoImage;
        r6 = 1;
        r4.setAllowDecodeSingleFrame(r6);
        r0 = r144;
        r6 = r0.photoImage;
        r4 = org.telegram.messenger.MediaController.getInstance();
        r4 = r4.getPlayingMessageObject();
        if (r4 != 0) goto L_0x2452;
    L_0x228e:
        r4 = 1;
    L_0x228f:
        r6.setAllowStartAnimation(r4);
    L_0x2292:
        r0 = r144;
        r4 = r0.photoImage;
        r6 = r145.needDrawBluredPreview();
        r4.setForcePreview(r6);
        r0 = r145;
        r4 = r0.type;
        r6 = 9;
        if (r4 != r6) goto L_0x24c2;
    L_0x22a5:
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 == 0) goto L_0x246e;
    L_0x22ab:
        r6 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        r0 = r144;
        r4 = r0.isChat;
        if (r4 == 0) goto L_0x246a;
    L_0x22b5:
        r4 = r145.needDrawAvatar();
        if (r4 == 0) goto L_0x246a;
    L_0x22bb:
        r4 = r145.isOutOwner();
        if (r4 != 0) goto L_0x246a;
    L_0x22c1:
        r4 = 1120665600; // 0x42cc0000 float:102.0 double:5.536823734E-315;
    L_0x22c3:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r6 - r4;
        r6 = 1132920832; // 0x43870000 float:270.0 double:5.597372625E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = java.lang.Math.min(r4, r6);
        r0 = r144;
        r0.backgroundWidth = r4;
    L_0x22d7:
        r4 = r144.checkNeedDrawShareButton(r145);
        if (r4 == 0) goto L_0x22ec;
    L_0x22dd:
        r0 = r144;
        r4 = r0.backgroundWidth;
        r6 = 1101004800; // 0x41a00000 float:20.0 double:5.439686476E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r0 = r144;
        r0.backgroundWidth = r4;
    L_0x22ec:
        r0 = r144;
        r4 = r0.backgroundWidth;
        r6 = 1124728832; // 0x430a0000 float:138.0 double:5.55689877E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r95 = r4 - r6;
        r0 = r144;
        r1 = r95;
        r2 = r145;
        r0.createDocumentLayout(r1, r2);
        r0 = r145;
        r4 = r0.caption;
        r4 = android.text.TextUtils.isEmpty(r4);
        if (r4 != 0) goto L_0x2313;
    L_0x230b:
        r4 = 1118568448; // 0x42ac0000 float:86.0 double:5.526462427E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r95 = r95 + r4;
    L_0x2313:
        r0 = r144;
        r4 = r0.drawPhotoImage;
        if (r4 == 0) goto L_0x249f;
    L_0x2319:
        r4 = 1118568448; // 0x42ac0000 float:86.0 double:5.526462427E-315;
        r109 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = 1118568448; // 0x42ac0000 float:86.0 double:5.526462427E-315;
        r108 = org.telegram.messenger.AndroidUtilities.dp(r4);
    L_0x2325:
        r0 = r95;
        r1 = r144;
        r1.availableTimeWidth = r0;
        r0 = r144;
        r4 = r0.drawPhotoImage;
        if (r4 != 0) goto L_0x2374;
    L_0x2331:
        r0 = r145;
        r4 = r0.caption;
        r4 = android.text.TextUtils.isEmpty(r4);
        if (r4 == 0) goto L_0x2374;
    L_0x233b:
        r0 = r144;
        r4 = r0.infoLayout;
        r4 = r4.getLineCount();
        if (r4 <= 0) goto L_0x2374;
    L_0x2345:
        r144.measureTime(r145);
        r0 = r144;
        r4 = r0.backgroundWidth;
        r6 = 1123287040; // 0x42f40000 float:122.0 double:5.54977537E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r0 = r144;
        r6 = r0.infoLayout;
        r8 = 0;
        r6 = r6.getLineWidth(r8);
        r8 = (double) r6;
        r8 = java.lang.Math.ceil(r8);
        r6 = (int) r8;
        r129 = r4 - r6;
        r0 = r144;
        r4 = r0.timeWidth;
        r0 = r129;
        if (r0 >= r4) goto L_0x2374;
    L_0x236c:
        r4 = 1090519040; // 0x41000000 float:8.0 double:5.38787994E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r108 = r108 + r4;
    L_0x2374:
        r144.setMessageObjectInternal(r145);
        r0 = r144;
        r4 = r0.drawForwardedName;
        if (r4 == 0) goto L_0x38a1;
    L_0x237d:
        r4 = r145.needDrawForwarded();
        if (r4 == 0) goto L_0x38a1;
    L_0x2383:
        r0 = r144;
        r4 = r0.currentPosition;
        if (r4 == 0) goto L_0x2391;
    L_0x2389:
        r0 = r144;
        r4 = r0.currentPosition;
        r4 = r4.minY;
        if (r4 != 0) goto L_0x38a1;
    L_0x2391:
        r0 = r145;
        r4 = r0.type;
        r6 = 5;
        if (r4 == r6) goto L_0x23a7;
    L_0x2398:
        r0 = r144;
        r4 = r0.namesOffset;
        r6 = 1084227584; // 0x40a00000 float:5.0 double:5.356796015E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r0 = r144;
        r0.namesOffset = r4;
    L_0x23a7:
        r4 = 1096810496; // 0x41600000 float:14.0 double:5.41896386E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r4 + r108;
        r0 = r144;
        r6 = r0.namesOffset;
        r4 = r4 + r6;
        r4 = r4 + r42;
        r0 = r144;
        r0.totalHeight = r4;
        r0 = r144;
        r4 = r0.currentPosition;
        if (r4 == 0) goto L_0x23d9;
    L_0x23c0:
        r0 = r144;
        r4 = r0.currentPosition;
        r4 = r4.flags;
        r4 = r4 & 8;
        if (r4 != 0) goto L_0x23d9;
    L_0x23ca:
        r0 = r144;
        r4 = r0.totalHeight;
        r6 = 1077936128; // 0x40400000 float:3.0 double:5.325712093E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r0 = r144;
        r0.totalHeight = r4;
    L_0x23d9:
        r44 = 0;
        r0 = r144;
        r4 = r0.currentPosition;
        if (r4 == 0) goto L_0x2419;
    L_0x23e1:
        r0 = r144;
        r4 = r0.currentPosition;
        r0 = r144;
        r4 = r0.getAdditionalWidthForPosition(r4);
        r109 = r109 + r4;
        r0 = r144;
        r4 = r0.currentPosition;
        r4 = r4.flags;
        r4 = r4 & 4;
        if (r4 != 0) goto L_0x2407;
    L_0x23f7:
        r4 = 1082130432; // 0x40800000 float:4.0 double:5.34643471E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r108 = r108 + r4;
        r4 = 1082130432; // 0x40800000 float:4.0 double:5.34643471E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r44 = r44 - r4;
    L_0x2407:
        r0 = r144;
        r4 = r0.currentPosition;
        r4 = r4.flags;
        r4 = r4 & 8;
        if (r4 != 0) goto L_0x2419;
    L_0x2411:
        r4 = 1082130432; // 0x40800000 float:4.0 double:5.34643471E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r108 = r108 + r4;
    L_0x2419:
        r0 = r144;
        r4 = r0.drawPinnedTop;
        if (r4 == 0) goto L_0x242e;
    L_0x241f:
        r0 = r144;
        r4 = r0.namesOffset;
        r6 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r0 = r144;
        r0.namesOffset = r4;
    L_0x242e:
        r0 = r144;
        r4 = r0.photoImage;
        r6 = 0;
        r8 = 1088421888; // 0x40e00000 float:7.0 double:5.37751863E-315;
        r8 = org.telegram.messenger.AndroidUtilities.dp(r8);
        r0 = r144;
        r9 = r0.namesOffset;
        r8 = r8 + r9;
        r8 = r8 + r44;
        r0 = r109;
        r1 = r108;
        r4.setImageCoords(r6, r8, r0, r1);
        r144.invalidate();
        goto L_0x10f6;
    L_0x244c:
        r4 = 0;
        goto L_0x222e;
    L_0x244f:
        r4 = 0;
        goto L_0x223b;
    L_0x2452:
        r4 = 0;
        goto L_0x228f;
    L_0x2455:
        r0 = r144;
        r6 = r0.photoImage;
        r0 = r145;
        r4 = r0.gifState;
        r8 = 0;
        r4 = (r4 > r8 ? 1 : (r4 == r8 ? 0 : -1));
        if (r4 != 0) goto L_0x2468;
    L_0x2462:
        r4 = 1;
    L_0x2463:
        r6.setAllowStartAnimation(r4);
        goto L_0x2292;
    L_0x2468:
        r4 = 0;
        goto L_0x2463;
    L_0x246a:
        r4 = 1112014848; // 0x42480000 float:50.0 double:5.49408334E-315;
        goto L_0x22c3;
    L_0x246e:
        r4 = org.telegram.messenger.AndroidUtilities.displaySize;
        r6 = r4.x;
        r0 = r144;
        r4 = r0.isChat;
        if (r4 == 0) goto L_0x249c;
    L_0x2478:
        r4 = r145.needDrawAvatar();
        if (r4 == 0) goto L_0x249c;
    L_0x247e:
        r4 = r145.isOutOwner();
        if (r4 != 0) goto L_0x249c;
    L_0x2484:
        r4 = 1120665600; // 0x42cc0000 float:102.0 double:5.536823734E-315;
    L_0x2486:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r6 - r4;
        r6 = 1132920832; // 0x43870000 float:270.0 double:5.597372625E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = java.lang.Math.min(r4, r6);
        r0 = r144;
        r0.backgroundWidth = r4;
        goto L_0x22d7;
    L_0x249c:
        r4 = 1112014848; // 0x42480000 float:50.0 double:5.49408334E-315;
        goto L_0x2486;
    L_0x249f:
        r4 = 1113587712; // 0x42600000 float:56.0 double:5.50185432E-315;
        r109 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = 1113587712; // 0x42600000 float:56.0 double:5.50185432E-315;
        r108 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r0 = r145;
        r4 = r0.caption;
        r4 = android.text.TextUtils.isEmpty(r4);
        if (r4 == 0) goto L_0x24bf;
    L_0x24b5:
        r4 = 1112276992; // 0x424c0000 float:51.0 double:5.495378504E-315;
    L_0x24b7:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r95 = r95 + r4;
        goto L_0x2325;
    L_0x24bf:
        r4 = 1101529088; // 0x41a80000 float:21.0 double:5.442276803E-315;
        goto L_0x24b7;
    L_0x24c2:
        r0 = r145;
        r4 = r0.type;
        r6 = 4;
        if (r4 != r6) goto L_0x2ada;
    L_0x24c9:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r0 = r4.geo;
        r110 = r0;
        r0 = r110;
        r0 = r0.lat;
        r22 = r0;
        r0 = r110;
        r0 = r0._long;
        r24 = r0;
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r4 = r4 instanceof org.telegram.tgnet.TLRPC.TL_messageMediaGeoLive;
        if (r4 == 0) goto L_0x279a;
    L_0x24e9:
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 == 0) goto L_0x2729;
    L_0x24ef:
        r6 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        r0 = r144;
        r4 = r0.isChat;
        if (r4 == 0) goto L_0x2725;
    L_0x24f9:
        r4 = r145.needDrawAvatar();
        if (r4 == 0) goto L_0x2725;
    L_0x24ff:
        r4 = r145.isOutOwner();
        if (r4 != 0) goto L_0x2725;
    L_0x2505:
        r4 = 1120665600; // 0x42cc0000 float:102.0 double:5.536823734E-315;
    L_0x2507:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r6 - r4;
        r6 = 1133543424; // 0x43908000 float:289.0 double:5.60044864E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = java.lang.Math.min(r4, r6);
        r0 = r144;
        r0.backgroundWidth = r4;
    L_0x251c:
        r4 = r144.checkNeedDrawShareButton(r145);
        if (r4 == 0) goto L_0x2531;
    L_0x2522:
        r0 = r144;
        r4 = r0.backgroundWidth;
        r6 = 1101004800; // 0x41a00000 float:20.0 double:5.439686476E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r0 = r144;
        r0.backgroundWidth = r4;
    L_0x2531:
        r0 = r144;
        r4 = r0.backgroundWidth;
        r6 = 1108606976; // 0x42140000 float:37.0 double:5.477246216E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r95 = r4 - r6;
        r0 = r95;
        r1 = r144;
        r1.availableTimeWidth = r0;
        r4 = 1113063424; // 0x42580000 float:54.0 double:5.499263994E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r95 = r95 - r4;
        r0 = r144;
        r4 = r0.backgroundWidth;
        r6 = 1101529088; // 0x41a80000 float:21.0 double:5.442276803E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r109 = r4 - r6;
        r4 = 1128464384; // 0x43430000 float:195.0 double:5.575354847E-315;
        r108 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r101 = 268435456; // 0x10000000 float:2.5243549E-29 double:1.32624737E-315;
        r0 = r101;
        r8 = (double) r0;
        r20 = 4614256656552045848; // 0x400921fb54442d18 float:3.37028055E12 double:3.141592653589793;
        r114 = r8 / r20;
        r0 = r101;
        r8 = (double) r0;
        r20 = 4607182418800017408; // 0x3ff0000000000000 float:0.0 double:1.0;
        r26 = 4614256656552045848; // 0x400921fb54442d18 float:3.37028055E12 double:3.141592653589793;
        r26 = r26 * r22;
        r32 = 4640537203540230144; // 0x4066800000000000 float:0.0 double:180.0;
        r26 = r26 / r32;
        r26 = java.lang.Math.sin(r26);
        r20 = r20 + r26;
        r26 = 4607182418800017408; // 0x3ff0000000000000 float:0.0 double:1.0;
        r32 = 4614256656552045848; // 0x400921fb54442d18 float:3.37028055E12 double:3.141592653589793;
        r32 = r32 * r22;
        r34 = 4640537203540230144; // 0x4066800000000000 float:0.0 double:180.0;
        r32 = r32 / r34;
        r32 = java.lang.Math.sin(r32);
        r26 = r26 - r32;
        r20 = r20 / r26;
        r20 = java.lang.Math.log(r20);
        r20 = r20 * r114;
        r26 = 4611686018427387904; // 0x4000000000000000 float:0.0 double:2.0;
        r20 = r20 / r26;
        r8 = r8 - r20;
        r8 = java.lang.Math.round(r8);
        r4 = 1092930765; // 0x4124cccd float:10.3 double:5.399795443E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r4 << 6;
        r0 = (long) r4;
        r20 = r0;
        r8 = r8 - r20;
        r0 = (double) r8;
        r142 = r0;
        r8 = 4609753056924675352; // 0x3ff921fb54442d18 float:3.37028055E12 double:1.5707963267948966;
        r20 = 4611686018427387904; // 0x4000000000000000 float:0.0 double:2.0;
        r0 = r101;
        r0 = (double) r0;
        r26 = r0;
        r26 = r142 - r26;
        r26 = r26 / r114;
        r26 = java.lang.Math.exp(r26);
        r26 = java.lang.Math.atan(r26);
        r20 = r20 * r26;
        r8 = r8 - r20;
        r20 = 4640537203540230144; // 0x4066800000000000 float:0.0 double:180.0;
        r8 = r8 * r20;
        r20 = 4614256656552045848; // 0x400921fb54442d18 float:3.37028055E12 double:3.141592653589793;
        r22 = r8 / r20;
        r0 = r144;
        r0 = r0.currentAccount;
        r21 = r0;
        r0 = r109;
        r4 = (float) r0;
        r6 = org.telegram.messenger.AndroidUtilities.density;
        r4 = r4 / r6;
        r0 = (int) r4;
        r26 = r0;
        r0 = r108;
        r4 = (float) r0;
        r6 = org.telegram.messenger.AndroidUtilities.density;
        r4 = r4 / r6;
        r0 = (int) r4;
        r27 = r0;
        r28 = 0;
        r29 = 15;
        r4 = org.telegram.messenger.AndroidUtilities.formapMapUrl(r21, r22, r24, r26, r27, r28, r29);
        r0 = r144;
        r0.currentUrl = r4;
        r0 = r110;
        r0 = r0.access_hash;
        r26 = r0;
        r0 = r109;
        r4 = (float) r0;
        r6 = org.telegram.messenger.AndroidUtilities.density;
        r4 = r4 / r6;
        r0 = (int) r4;
        r28 = r0;
        r0 = r108;
        r4 = (float) r0;
        r6 = org.telegram.messenger.AndroidUtilities.density;
        r4 = r4 / r6;
        r0 = (int) r4;
        r29 = r0;
        r30 = 15;
        r4 = 2;
        r6 = org.telegram.messenger.AndroidUtilities.density;
        r8 = (double) r6;
        r8 = java.lang.Math.ceil(r8);
        r6 = (int) r8;
        r31 = java.lang.Math.min(r4, r6);
        r4 = org.telegram.messenger.WebFile.createWithGeoPoint(r22, r24, r26, r28, r29, r30, r31);
        r0 = r144;
        r0.currentWebFile = r4;
        r4 = r144.isCurrentLocationTimeExpired(r145);
        r0 = r144;
        r0.locationExpired = r4;
        if (r4 != 0) goto L_0x275b;
    L_0x2642:
        r0 = r144;
        r4 = r0.photoImage;
        r6 = 1;
        r4.setCrossfadeWithOldImage(r6);
        r4 = 0;
        r0 = r144;
        r0.mediaBackground = r4;
        r4 = 1113587712; // 0x42600000 float:56.0 double:5.50185432E-315;
        r42 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r0 = r144;
        r4 = r0.invalidateRunnable;
        r8 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
        org.telegram.messenger.AndroidUtilities.runOnUIThread(r4, r8);
        r4 = 1;
        r0 = r144;
        r0.scheduledInvalidate = r4;
    L_0x2663:
        r26 = new android.text.StaticLayout;
        r4 = "AttachLiveLocation";
        r6 = 2131493052; // 0x7f0c00bc float:1.8609573E38 double:1.0530974913E-314;
        r27 = org.telegram.messenger.LocaleController.getString(r4, r6);
        r28 = org.telegram.ui.ActionBar.Theme.chat_locationTitlePaint;
        r30 = android.text.Layout.Alignment.ALIGN_NORMAL;
        r31 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r32 = 0;
        r33 = 0;
        r29 = r95;
        r26.<init>(r27, r28, r29, r30, r31, r32, r33);
        r0 = r26;
        r1 = r144;
        r1.docTitleLayout = r0;
        r19 = 0;
        r144.updateCurrentUserAndChat();
        r0 = r144;
        r4 = r0.currentUser;
        if (r4 == 0) goto L_0x276c;
    L_0x268f:
        r0 = r144;
        r4 = r0.currentUser;
        r4 = r4.photo;
        if (r4 == 0) goto L_0x26a1;
    L_0x2697:
        r0 = r144;
        r4 = r0.currentUser;
        r4 = r4.photo;
        r0 = r4.photo_small;
        r19 = r0;
    L_0x26a1:
        r0 = r144;
        r4 = r0.contactAvatarDrawable;
        r0 = r144;
        r6 = r0.currentUser;
        r4.setInfo(r6);
    L_0x26ac:
        r0 = r144;
        r0 = r0.locationImageReceiver;
        r26 = r0;
        r28 = "50_50";
        r0 = r144;
        r0 = r0.contactAvatarDrawable;
        r29 = r0;
        r30 = 0;
        r31 = 0;
        r27 = r19;
        r26.setImage(r27, r28, r29, r30, r31);
        r26 = new android.text.StaticLayout;
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.edit_date;
        if (r4 == 0) goto L_0x2791;
    L_0x26ce:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.edit_date;
        r8 = (long) r4;
    L_0x26d5:
        r27 = org.telegram.messenger.LocaleController.formatLocationUpdateDate(r8);
        r28 = org.telegram.ui.ActionBar.Theme.chat_locationAddressPaint;
        r30 = android.text.Layout.Alignment.ALIGN_NORMAL;
        r31 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r32 = 0;
        r33 = 0;
        r29 = r95;
        r26.<init>(r27, r28, r29, r30, r31, r32, r33);
        r0 = r26;
        r1 = r144;
        r1.infoLayout = r0;
    L_0x26ee:
        r8 = r145.getDialogId();
        r4 = (int) r8;
        if (r4 != 0) goto L_0x2a4a;
    L_0x26f5:
        r4 = org.telegram.messenger.SharedConfig.mapPreviewType;
        if (r4 != 0) goto L_0x2a37;
    L_0x26f9:
        r4 = 2;
        r0 = r144;
        r0.currentMapProvider = r4;
    L_0x26fe:
        r0 = r144;
        r4 = r0.currentMapProvider;
        r6 = -1;
        if (r4 != r6) goto L_0x2a5d;
    L_0x2705:
        r0 = r144;
        r0 = r0.photoImage;
        r26 = r0;
        r27 = 0;
        r27 = (org.telegram.tgnet.TLObject) r27;
        r28 = 0;
        r6 = org.telegram.ui.ActionBar.Theme.chat_locationDrawable;
        r4 = r145.isOutOwner();
        if (r4 == 0) goto L_0x2a5a;
    L_0x2719:
        r4 = 1;
    L_0x271a:
        r29 = r6[r4];
        r30 = 0;
        r31 = 0;
        r26.setImage(r27, r28, r29, r30, r31);
        goto L_0x2374;
    L_0x2725:
        r4 = 1112014848; // 0x42480000 float:50.0 double:5.49408334E-315;
        goto L_0x2507;
    L_0x2729:
        r4 = org.telegram.messenger.AndroidUtilities.displaySize;
        r6 = r4.x;
        r0 = r144;
        r4 = r0.isChat;
        if (r4 == 0) goto L_0x2758;
    L_0x2733:
        r4 = r145.needDrawAvatar();
        if (r4 == 0) goto L_0x2758;
    L_0x2739:
        r4 = r145.isOutOwner();
        if (r4 != 0) goto L_0x2758;
    L_0x273f:
        r4 = 1120665600; // 0x42cc0000 float:102.0 double:5.536823734E-315;
    L_0x2741:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r6 - r4;
        r6 = 1133543424; // 0x43908000 float:289.0 double:5.60044864E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = java.lang.Math.min(r4, r6);
        r0 = r144;
        r0.backgroundWidth = r4;
        goto L_0x251c;
    L_0x2758:
        r4 = 1112014848; // 0x42480000 float:50.0 double:5.49408334E-315;
        goto L_0x2741;
    L_0x275b:
        r0 = r144;
        r4 = r0.backgroundWidth;
        r6 = 1091567616; // 0x41100000 float:9.0 double:5.39306059E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r0 = r144;
        r0.backgroundWidth = r4;
        goto L_0x2663;
    L_0x276c:
        r0 = r144;
        r4 = r0.currentChat;
        if (r4 == 0) goto L_0x26ac;
    L_0x2772:
        r0 = r144;
        r4 = r0.currentChat;
        r4 = r4.photo;
        if (r4 == 0) goto L_0x2784;
    L_0x277a:
        r0 = r144;
        r4 = r0.currentChat;
        r4 = r4.photo;
        r0 = r4.photo_small;
        r19 = r0;
    L_0x2784:
        r0 = r144;
        r4 = r0.contactAvatarDrawable;
        r0 = r144;
        r6 = r0.currentChat;
        r4.setInfo(r6);
        goto L_0x26ac;
    L_0x2791:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.date;
        r8 = (long) r4;
        goto L_0x26d5;
    L_0x279a:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r4 = r4.title;
        r4 = android.text.TextUtils.isEmpty(r4);
        if (r4 != 0) goto L_0x2947;
    L_0x27a8:
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 == 0) goto L_0x290c;
    L_0x27ae:
        r6 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        r0 = r144;
        r4 = r0.isChat;
        if (r4 == 0) goto L_0x2908;
    L_0x27b8:
        r4 = r145.needDrawAvatar();
        if (r4 == 0) goto L_0x2908;
    L_0x27be:
        r4 = r145.isOutOwner();
        if (r4 != 0) goto L_0x2908;
    L_0x27c4:
        r4 = 1120665600; // 0x42cc0000 float:102.0 double:5.536823734E-315;
    L_0x27c6:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r6 - r4;
        r6 = 1133543424; // 0x43908000 float:289.0 double:5.60044864E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = java.lang.Math.min(r4, r6);
        r0 = r144;
        r0.backgroundWidth = r4;
    L_0x27db:
        r4 = r144.checkNeedDrawShareButton(r145);
        if (r4 == 0) goto L_0x27f0;
    L_0x27e1:
        r0 = r144;
        r4 = r0.backgroundWidth;
        r6 = 1101004800; // 0x41a00000 float:20.0 double:5.439686476E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r0 = r144;
        r0.backgroundWidth = r4;
    L_0x27f0:
        r0 = r144;
        r4 = r0.backgroundWidth;
        r6 = 1107820544; // 0x42080000 float:34.0 double:5.473360725E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r95 = r4 - r6;
        r0 = r95;
        r1 = r144;
        r1.availableTimeWidth = r0;
        r0 = r144;
        r4 = r0.backgroundWidth;
        r6 = 1101529088; // 0x41a80000 float:21.0 double:5.442276803E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r109 = r4 - r6;
        r4 = 1128464384; // 0x43430000 float:195.0 double:5.575354847E-315;
        r108 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = 0;
        r0 = r144;
        r0.mediaBackground = r4;
        r0 = r144;
        r0 = r0.currentAccount;
        r21 = r0;
        r0 = r109;
        r4 = (float) r0;
        r6 = org.telegram.messenger.AndroidUtilities.density;
        r4 = r4 / r6;
        r0 = (int) r4;
        r26 = r0;
        r0 = r108;
        r4 = (float) r0;
        r6 = org.telegram.messenger.AndroidUtilities.density;
        r4 = r4 / r6;
        r0 = (int) r4;
        r27 = r0;
        r28 = 1;
        r29 = 15;
        r4 = org.telegram.messenger.AndroidUtilities.formapMapUrl(r21, r22, r24, r26, r27, r28, r29);
        r0 = r144;
        r0.currentUrl = r4;
        r0 = r109;
        r4 = (float) r0;
        r6 = org.telegram.messenger.AndroidUtilities.density;
        r4 = r4 / r6;
        r4 = (int) r4;
        r0 = r108;
        r6 = (float) r0;
        r8 = org.telegram.messenger.AndroidUtilities.density;
        r6 = r6 / r8;
        r6 = (int) r6;
        r8 = 15;
        r9 = 2;
        r10 = org.telegram.messenger.AndroidUtilities.density;
        r0 = (double) r10;
        r20 = r0;
        r20 = java.lang.Math.ceil(r20);
        r0 = r20;
        r10 = (int) r0;
        r9 = java.lang.Math.min(r9, r10);
        r0 = r110;
        r4 = org.telegram.messenger.WebFile.createWithGeoPoint(r0, r4, r6, r8, r9);
        r0 = r144;
        r0.currentWebFile = r4;
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r0 = r4.title;
        r26 = r0;
        r27 = org.telegram.ui.ActionBar.Theme.chat_locationTitlePaint;
        r29 = android.text.Layout.Alignment.ALIGN_NORMAL;
        r30 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r31 = 0;
        r32 = 0;
        r33 = android.text.TextUtils.TruncateAt.END;
        r35 = 1;
        r28 = r95;
        r34 = r95;
        r4 = org.telegram.ui.Components.StaticLayoutEx.createStaticLayout(r26, r27, r28, r29, r30, r31, r32, r33, r34, r35);
        r0 = r144;
        r0.docTitleLayout = r4;
        r4 = 1112014848; // 0x42480000 float:50.0 double:5.49408334E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r42 = r42 + r4;
        r0 = r144;
        r4 = r0.docTitleLayout;
        r84 = r4.getLineCount();
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r4 = r4.address;
        r4 = android.text.TextUtils.isEmpty(r4);
        if (r4 != 0) goto L_0x2940;
    L_0x28aa:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r0 = r4.address;
        r26 = r0;
        r27 = org.telegram.ui.ActionBar.Theme.chat_locationAddressPaint;
        r29 = android.text.Layout.Alignment.ALIGN_NORMAL;
        r30 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r31 = 0;
        r32 = 0;
        r33 = android.text.TextUtils.TruncateAt.END;
        r35 = 1;
        r28 = r95;
        r34 = r95;
        r4 = org.telegram.ui.Components.StaticLayoutEx.createStaticLayout(r26, r27, r28, r29, r30, r31, r32, r33, r34, r35);
        r0 = r144;
        r0.infoLayout = r4;
        r144.measureTime(r145);
        r0 = r144;
        r4 = r0.backgroundWidth;
        r0 = r144;
        r6 = r0.infoLayout;
        r8 = 0;
        r6 = r6.getLineWidth(r8);
        r8 = (double) r6;
        r8 = java.lang.Math.ceil(r8);
        r6 = (int) r8;
        r129 = r4 - r6;
        r0 = r144;
        r6 = r0.timeWidth;
        r4 = r145.isOutOwner();
        if (r4 == 0) goto L_0x293e;
    L_0x28f0:
        r4 = 20;
    L_0x28f2:
        r4 = r4 + 14;
        r4 = (float) r4;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r4 + r6;
        r0 = r129;
        if (r0 >= r4) goto L_0x26ee;
    L_0x28fe:
        r4 = 1090519040; // 0x41000000 float:8.0 double:5.38787994E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r42 = r42 + r4;
        goto L_0x26ee;
    L_0x2908:
        r4 = 1112014848; // 0x42480000 float:50.0 double:5.49408334E-315;
        goto L_0x27c6;
    L_0x290c:
        r4 = org.telegram.messenger.AndroidUtilities.displaySize;
        r6 = r4.x;
        r0 = r144;
        r4 = r0.isChat;
        if (r4 == 0) goto L_0x293b;
    L_0x2916:
        r4 = r145.needDrawAvatar();
        if (r4 == 0) goto L_0x293b;
    L_0x291c:
        r4 = r145.isOutOwner();
        if (r4 != 0) goto L_0x293b;
    L_0x2922:
        r4 = 1120665600; // 0x42cc0000 float:102.0 double:5.536823734E-315;
    L_0x2924:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r6 - r4;
        r6 = 1133543424; // 0x43908000 float:289.0 double:5.60044864E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = java.lang.Math.min(r4, r6);
        r0 = r144;
        r0.backgroundWidth = r4;
        goto L_0x27db;
    L_0x293b:
        r4 = 1112014848; // 0x42480000 float:50.0 double:5.49408334E-315;
        goto L_0x2924;
    L_0x293e:
        r4 = 0;
        goto L_0x28f2;
    L_0x2940:
        r4 = 0;
        r0 = r144;
        r0.infoLayout = r4;
        goto L_0x26ee;
    L_0x2947:
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 == 0) goto L_0x2a05;
    L_0x294d:
        r6 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        r0 = r144;
        r4 = r0.isChat;
        if (r4 == 0) goto L_0x2a01;
    L_0x2957:
        r4 = r145.needDrawAvatar();
        if (r4 == 0) goto L_0x2a01;
    L_0x295d:
        r4 = r145.isOutOwner();
        if (r4 != 0) goto L_0x2a01;
    L_0x2963:
        r4 = 1120665600; // 0x42cc0000 float:102.0 double:5.536823734E-315;
    L_0x2965:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r6 - r4;
        r6 = 1133543424; // 0x43908000 float:289.0 double:5.60044864E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = java.lang.Math.min(r4, r6);
        r0 = r144;
        r0.backgroundWidth = r4;
    L_0x297a:
        r4 = r144.checkNeedDrawShareButton(r145);
        if (r4 == 0) goto L_0x298f;
    L_0x2980:
        r0 = r144;
        r4 = r0.backgroundWidth;
        r6 = 1101004800; // 0x41a00000 float:20.0 double:5.439686476E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r0 = r144;
        r0.backgroundWidth = r4;
    L_0x298f:
        r0 = r144;
        r4 = r0.backgroundWidth;
        r6 = 1107820544; // 0x42080000 float:34.0 double:5.473360725E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r0 = r144;
        r0.availableTimeWidth = r4;
        r0 = r144;
        r4 = r0.backgroundWidth;
        r6 = 1094713344; // 0x41400000 float:12.0 double:5.408602553E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r109 = r4 - r6;
        r4 = 1128464384; // 0x43430000 float:195.0 double:5.575354847E-315;
        r108 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r0 = r144;
        r0 = r0.currentAccount;
        r21 = r0;
        r0 = r109;
        r4 = (float) r0;
        r6 = org.telegram.messenger.AndroidUtilities.density;
        r4 = r4 / r6;
        r0 = (int) r4;
        r26 = r0;
        r0 = r108;
        r4 = (float) r0;
        r6 = org.telegram.messenger.AndroidUtilities.density;
        r4 = r4 / r6;
        r0 = (int) r4;
        r27 = r0;
        r28 = 1;
        r29 = 15;
        r4 = org.telegram.messenger.AndroidUtilities.formapMapUrl(r21, r22, r24, r26, r27, r28, r29);
        r0 = r144;
        r0.currentUrl = r4;
        r0 = r109;
        r4 = (float) r0;
        r6 = org.telegram.messenger.AndroidUtilities.density;
        r4 = r4 / r6;
        r4 = (int) r4;
        r0 = r108;
        r6 = (float) r0;
        r8 = org.telegram.messenger.AndroidUtilities.density;
        r6 = r6 / r8;
        r6 = (int) r6;
        r8 = 15;
        r9 = 2;
        r10 = org.telegram.messenger.AndroidUtilities.density;
        r0 = (double) r10;
        r20 = r0;
        r20 = java.lang.Math.ceil(r20);
        r0 = r20;
        r10 = (int) r0;
        r9 = java.lang.Math.min(r9, r10);
        r0 = r110;
        r4 = org.telegram.messenger.WebFile.createWithGeoPoint(r0, r4, r6, r8, r9);
        r0 = r144;
        r0.currentWebFile = r4;
        goto L_0x26ee;
    L_0x2a01:
        r4 = 1112014848; // 0x42480000 float:50.0 double:5.49408334E-315;
        goto L_0x2965;
    L_0x2a05:
        r4 = org.telegram.messenger.AndroidUtilities.displaySize;
        r6 = r4.x;
        r0 = r144;
        r4 = r0.isChat;
        if (r4 == 0) goto L_0x2a34;
    L_0x2a0f:
        r4 = r145.needDrawAvatar();
        if (r4 == 0) goto L_0x2a34;
    L_0x2a15:
        r4 = r145.isOutOwner();
        if (r4 != 0) goto L_0x2a34;
    L_0x2a1b:
        r4 = 1120665600; // 0x42cc0000 float:102.0 double:5.536823734E-315;
    L_0x2a1d:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r6 - r4;
        r6 = 1133543424; // 0x43908000 float:289.0 double:5.60044864E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = java.lang.Math.min(r4, r6);
        r0 = r144;
        r0.backgroundWidth = r4;
        goto L_0x297a;
    L_0x2a34:
        r4 = 1112014848; // 0x42480000 float:50.0 double:5.49408334E-315;
        goto L_0x2a1d;
    L_0x2a37:
        r4 = org.telegram.messenger.SharedConfig.mapPreviewType;
        r6 = 1;
        if (r4 != r6) goto L_0x2a43;
    L_0x2a3c:
        r4 = 1;
        r0 = r144;
        r0.currentMapProvider = r4;
        goto L_0x26fe;
    L_0x2a43:
        r4 = -1;
        r0 = r144;
        r0.currentMapProvider = r4;
        goto L_0x26fe;
    L_0x2a4a:
        r0 = r145;
        r4 = r0.currentAccount;
        r4 = org.telegram.messenger.MessagesController.getInstance(r4);
        r4 = r4.mapProvider;
        r0 = r144;
        r0.currentMapProvider = r4;
        goto L_0x26fe;
    L_0x2a5a:
        r4 = 0;
        goto L_0x271a;
    L_0x2a5d:
        r0 = r144;
        r4 = r0.currentMapProvider;
        r6 = 2;
        if (r4 != r6) goto L_0x2a8e;
    L_0x2a64:
        r0 = r144;
        r4 = r0.currentWebFile;
        if (r4 == 0) goto L_0x2374;
    L_0x2a6a:
        r0 = r144;
        r0 = r0.photoImage;
        r26 = r0;
        r0 = r144;
        r0 = r0.currentWebFile;
        r27 = r0;
        r28 = 0;
        r6 = org.telegram.ui.ActionBar.Theme.chat_locationDrawable;
        r4 = r145.isOutOwner();
        if (r4 == 0) goto L_0x2a8c;
    L_0x2a80:
        r4 = 1;
    L_0x2a81:
        r29 = r6[r4];
        r30 = 0;
        r31 = 0;
        r26.setImage(r27, r28, r29, r30, r31);
        goto L_0x2374;
    L_0x2a8c:
        r4 = 0;
        goto L_0x2a81;
    L_0x2a8e:
        r0 = r144;
        r4 = r0.currentMapProvider;
        r6 = 3;
        if (r4 == r6) goto L_0x2a9c;
    L_0x2a95:
        r0 = r144;
        r4 = r0.currentMapProvider;
        r6 = 4;
        if (r4 != r6) goto L_0x2ab0;
    L_0x2a9c:
        r4 = org.telegram.messenger.ImageLoader.getInstance();
        r0 = r144;
        r6 = r0.currentUrl;
        r0 = r144;
        r8 = r0.currentWebFile;
        r4.addTestWebFile(r6, r8);
        r4 = 1;
        r0 = r144;
        r0.addedForTest = r4;
    L_0x2ab0:
        r0 = r144;
        r4 = r0.currentUrl;
        if (r4 == 0) goto L_0x2374;
    L_0x2ab6:
        r0 = r144;
        r0 = r0.photoImage;
        r26 = r0;
        r0 = r144;
        r0 = r0.currentUrl;
        r27 = r0;
        r28 = 0;
        r6 = org.telegram.ui.ActionBar.Theme.chat_locationDrawable;
        r4 = r145.isOutOwner();
        if (r4 == 0) goto L_0x2ad8;
    L_0x2acc:
        r4 = 1;
    L_0x2acd:
        r29 = r6[r4];
        r30 = 0;
        r31 = 0;
        r26.setImage(r27, r28, r29, r30, r31);
        goto L_0x2374;
    L_0x2ad8:
        r4 = 0;
        goto L_0x2acd;
    L_0x2ada:
        r0 = r145;
        r4 = r0.type;
        r6 = 13;
        if (r4 != r6) goto L_0x2c7c;
    L_0x2ae2:
        r4 = 0;
        r0 = r144;
        r0.drawBackground = r4;
        r40 = 0;
    L_0x2ae9:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r4 = r4.document;
        r4 = r4.attributes;
        r4 = r4.size();
        r0 = r40;
        if (r0 >= r4) goto L_0x2b1f;
    L_0x2afb:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r4 = r4.document;
        r4 = r4.attributes;
        r0 = r40;
        r46 = r4.get(r0);
        r46 = (org.telegram.tgnet.TLRPC.DocumentAttribute) r46;
        r0 = r46;
        r4 = r0 instanceof org.telegram.tgnet.TLRPC.TL_documentAttributeImageSize;
        if (r4 == 0) goto L_0x2bf8;
    L_0x2b13:
        r0 = r46;
        r0 = r0.w;
        r109 = r0;
        r0 = r46;
        r0 = r0.h;
        r108 = r0;
    L_0x2b1f:
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 == 0) goto L_0x2bfc;
    L_0x2b25:
        r4 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        r4 = (float) r4;
        r6 = 1053609165; // 0x3ecccccd float:0.4 double:5.205520926E-315;
        r95 = r4 * r6;
        r92 = r95;
    L_0x2b31:
        if (r109 != 0) goto L_0x2b40;
    L_0x2b33:
        r0 = r92;
        r0 = (int) r0;
        r108 = r0;
        r4 = 1120403456; // 0x42c80000 float:100.0 double:5.53552857E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r109 = r108 + r4;
    L_0x2b40:
        r0 = r108;
        r4 = (float) r0;
        r0 = r109;
        r6 = (float) r0;
        r6 = r95 / r6;
        r4 = r4 * r6;
        r0 = (int) r4;
        r108 = r0;
        r0 = r95;
        r0 = (int) r0;
        r109 = r0;
        r0 = r108;
        r4 = (float) r0;
        r4 = (r4 > r92 ? 1 : (r4 == r92 ? 0 : -1));
        if (r4 <= 0) goto L_0x2b69;
    L_0x2b58:
        r0 = r109;
        r4 = (float) r0;
        r0 = r108;
        r6 = (float) r0;
        r6 = r92 / r6;
        r4 = r4 * r6;
        r0 = (int) r4;
        r109 = r0;
        r0 = r92;
        r0 = (int) r0;
        r108 = r0;
    L_0x2b69:
        r4 = 6;
        r0 = r144;
        r0.documentAttachType = r4;
        r4 = org.telegram.messenger.AndroidUtilities.displaySize;
        r4 = r4.x;
        r6 = org.telegram.messenger.AndroidUtilities.displaySize;
        r6 = r6.y;
        r4 = java.lang.Math.min(r4, r6);
        r6 = 1096810496; // 0x41600000 float:14.0 double:5.41896386E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r0 = r144;
        r0.availableTimeWidth = r4;
        r4 = 1094713344; // 0x41400000 float:12.0 double:5.408602553E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r4 + r109;
        r0 = r144;
        r0.backgroundWidth = r4;
        r0 = r145;
        r4 = r0.photoThumbs;
        r6 = 80;
        r4 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r4, r6);
        r0 = r144;
        r0.currentPhotoObjectThumb = r4;
        r0 = r145;
        r4 = r0.attachPathExists;
        if (r4 == 0) goto L_0x2c14;
    L_0x2ba5:
        r0 = r144;
        r0 = r0.photoImage;
        r26 = r0;
        r27 = 0;
        r0 = r145;
        r4 = r0.messageOwner;
        r0 = r4.attachPath;
        r28 = r0;
        r4 = java.util.Locale.US;
        r6 = "%d_%d";
        r8 = 2;
        r8 = new java.lang.Object[r8];
        r9 = 0;
        r10 = java.lang.Integer.valueOf(r109);
        r8[r9] = r10;
        r9 = 1;
        r10 = java.lang.Integer.valueOf(r108);
        r8[r9] = r10;
        r29 = java.lang.String.format(r4, r6, r8);
        r30 = 0;
        r0 = r144;
        r4 = r0.currentPhotoObjectThumb;
        if (r4 == 0) goto L_0x2c11;
    L_0x2bd7:
        r0 = r144;
        r4 = r0.currentPhotoObjectThumb;
        r0 = r4.location;
        r31 = r0;
    L_0x2bdf:
        r32 = "b1";
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r4 = r4.document;
        r0 = r4.size;
        r33 = r0;
        r34 = "webp";
        r35 = 1;
        r26.setImage(r27, r28, r29, r30, r31, r32, r33, r34, r35);
        goto L_0x2374;
    L_0x2bf8:
        r40 = r40 + 1;
        goto L_0x2ae9;
    L_0x2bfc:
        r4 = org.telegram.messenger.AndroidUtilities.displaySize;
        r4 = r4.x;
        r6 = org.telegram.messenger.AndroidUtilities.displaySize;
        r6 = r6.y;
        r4 = java.lang.Math.min(r4, r6);
        r4 = (float) r4;
        r6 = 1056964608; // 0x3f000000 float:0.5 double:5.222099017E-315;
        r95 = r4 * r6;
        r92 = r95;
        goto L_0x2b31;
    L_0x2c11:
        r31 = 0;
        goto L_0x2bdf;
    L_0x2c14:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r4 = r4.document;
        r8 = r4.id;
        r20 = 0;
        r4 = (r8 > r20 ? 1 : (r8 == r20 ? 0 : -1));
        if (r4 == 0) goto L_0x2374;
    L_0x2c24:
        r0 = r144;
        r0 = r0.photoImage;
        r26 = r0;
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r0 = r4.document;
        r27 = r0;
        r28 = 0;
        r4 = java.util.Locale.US;
        r6 = "%d_%d";
        r8 = 2;
        r8 = new java.lang.Object[r8];
        r9 = 0;
        r10 = java.lang.Integer.valueOf(r109);
        r8[r9] = r10;
        r9 = 1;
        r10 = java.lang.Integer.valueOf(r108);
        r8[r9] = r10;
        r29 = java.lang.String.format(r4, r6, r8);
        r30 = 0;
        r0 = r144;
        r4 = r0.currentPhotoObjectThumb;
        if (r4 == 0) goto L_0x2c79;
    L_0x2c58:
        r0 = r144;
        r4 = r0.currentPhotoObjectThumb;
        r0 = r4.location;
        r31 = r0;
    L_0x2c60:
        r32 = "b1";
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r4 = r4.document;
        r0 = r4.size;
        r33 = r0;
        r34 = "webp";
        r35 = 1;
        r26.setImage(r27, r28, r29, r30, r31, r32, r33, r34, r35);
        goto L_0x2374;
    L_0x2c79:
        r31 = 0;
        goto L_0x2c60;
    L_0x2c7c:
        r0 = r145;
        r4 = r0.type;
        r6 = 5;
        if (r4 != r6) goto L_0x2e6c;
    L_0x2c83:
        r109 = org.telegram.messenger.AndroidUtilities.roundMessageSize;
        r93 = r109;
    L_0x2c87:
        r4 = 1120403456; // 0x42c80000 float:100.0 double:5.53552857E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r108 = r109 + r4;
        r0 = r145;
        r4 = r0.type;
        r6 = 5;
        if (r4 == r6) goto L_0x2cac;
    L_0x2c96:
        r4 = r144.checkNeedDrawShareButton(r145);
        if (r4 == 0) goto L_0x2cac;
    L_0x2c9c:
        r4 = 1101004800; // 0x41a00000 float:20.0 double:5.439686476E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r93 = r93 - r4;
        r4 = 1101004800; // 0x41a00000 float:20.0 double:5.439686476E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r109 = r109 - r4;
    L_0x2cac:
        r4 = org.telegram.messenger.AndroidUtilities.getPhotoSize();
        r0 = r109;
        if (r0 <= r4) goto L_0x2cb8;
    L_0x2cb4:
        r109 = org.telegram.messenger.AndroidUtilities.getPhotoSize();
    L_0x2cb8:
        r4 = org.telegram.messenger.AndroidUtilities.getPhotoSize();
        r0 = r108;
        if (r0 <= r4) goto L_0x2cc4;
    L_0x2cc0:
        r108 = org.telegram.messenger.AndroidUtilities.getPhotoSize();
    L_0x2cc4:
        r0 = r145;
        r4 = r0.type;
        r6 = 1;
        if (r4 != r6) goto L_0x2e9a;
    L_0x2ccb:
        r144.updateSecretTimeText(r145);
        r0 = r145;
        r4 = r0.photoThumbs;
        r6 = 80;
        r4 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r4, r6);
        r0 = r144;
        r0.currentPhotoObjectThumb = r4;
    L_0x2cdc:
        r0 = r144;
        r4 = r0.currentMessagesGroup;
        if (r4 != 0) goto L_0x2ced;
    L_0x2ce2:
        r0 = r145;
        r4 = r0.caption;
        if (r4 == 0) goto L_0x2ced;
    L_0x2ce8:
        r4 = 0;
        r0 = r144;
        r0.mediaBackground = r4;
    L_0x2ced:
        r0 = r145;
        r4 = r0.photoThumbs;
        r6 = org.telegram.messenger.AndroidUtilities.getPhotoSize();
        r4 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r4, r6);
        r0 = r144;
        r0.currentPhotoObject = r4;
        r137 = 0;
        r75 = 0;
        r0 = r144;
        r4 = r0.currentPhotoObject;
        if (r4 == 0) goto L_0x2d16;
    L_0x2d07:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r0 = r144;
        r6 = r0.currentPhotoObjectThumb;
        if (r4 != r6) goto L_0x2d16;
    L_0x2d11:
        r4 = 0;
        r0 = r144;
        r0.currentPhotoObjectThumb = r4;
    L_0x2d16:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        if (r4 == 0) goto L_0x2d6a;
    L_0x2d1c:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r4 = r4.w;
        r4 = (float) r4;
        r0 = r109;
        r6 = (float) r0;
        r120 = r4 / r6;
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r4 = r4.w;
        r4 = (float) r4;
        r4 = r4 / r120;
        r0 = (int) r4;
        r137 = r0;
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r4 = r4.h;
        r4 = (float) r4;
        r4 = r4 / r120;
        r0 = (int) r4;
        r75 = r0;
        if (r137 != 0) goto L_0x2d48;
    L_0x2d42:
        r4 = 1125515264; // 0x43160000 float:150.0 double:5.56078426E-315;
        r137 = org.telegram.messenger.AndroidUtilities.dp(r4);
    L_0x2d48:
        if (r75 != 0) goto L_0x2d50;
    L_0x2d4a:
        r4 = 1125515264; // 0x43160000 float:150.0 double:5.56078426E-315;
        r75 = org.telegram.messenger.AndroidUtilities.dp(r4);
    L_0x2d50:
        r0 = r75;
        r1 = r108;
        if (r0 <= r1) goto L_0x2f5a;
    L_0x2d56:
        r0 = r75;
        r0 = (float) r0;
        r121 = r0;
        r75 = r108;
        r0 = r75;
        r4 = (float) r0;
        r121 = r121 / r4;
        r0 = r137;
        r4 = (float) r0;
        r4 = r4 / r121;
        r0 = (int) r4;
        r137 = r0;
    L_0x2d6a:
        r0 = r145;
        r4 = r0.type;
        r6 = 5;
        if (r4 != r6) goto L_0x2d75;
    L_0x2d71:
        r75 = org.telegram.messenger.AndroidUtilities.roundMessageSize;
        r137 = r75;
    L_0x2d75:
        if (r137 == 0) goto L_0x2d79;
    L_0x2d77:
        if (r75 != 0) goto L_0x2deb;
    L_0x2d79:
        r0 = r145;
        r4 = r0.type;
        r6 = 8;
        if (r4 != r6) goto L_0x2deb;
    L_0x2d81:
        r40 = 0;
    L_0x2d83:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r4 = r4.document;
        r4 = r4.attributes;
        r4 = r4.size();
        r0 = r40;
        if (r0 >= r4) goto L_0x2deb;
    L_0x2d95:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r4 = r4.document;
        r4 = r4.attributes;
        r0 = r40;
        r46 = r4.get(r0);
        r46 = (org.telegram.tgnet.TLRPC.DocumentAttribute) r46;
        r0 = r46;
        r4 = r0 instanceof org.telegram.tgnet.TLRPC.TL_documentAttributeImageSize;
        if (r4 != 0) goto L_0x2db3;
    L_0x2dad:
        r0 = r46;
        r4 = r0 instanceof org.telegram.tgnet.TLRPC.TL_documentAttributeVideo;
        if (r4 == 0) goto L_0x2fc8;
    L_0x2db3:
        r0 = r46;
        r4 = r0.w;
        r4 = (float) r4;
        r0 = r109;
        r6 = (float) r0;
        r120 = r4 / r6;
        r0 = r46;
        r4 = r0.w;
        r4 = (float) r4;
        r4 = r4 / r120;
        r0 = (int) r4;
        r137 = r0;
        r0 = r46;
        r4 = r0.h;
        r4 = (float) r4;
        r4 = r4 / r120;
        r0 = (int) r4;
        r75 = r0;
        r0 = r75;
        r1 = r108;
        if (r0 <= r1) goto L_0x2f94;
    L_0x2dd7:
        r0 = r75;
        r0 = (float) r0;
        r121 = r0;
        r75 = r108;
        r0 = r75;
        r4 = (float) r0;
        r121 = r121 / r4;
        r0 = r137;
        r4 = (float) r0;
        r4 = r4 / r121;
        r0 = (int) r4;
        r137 = r0;
    L_0x2deb:
        if (r137 == 0) goto L_0x2def;
    L_0x2ded:
        if (r75 != 0) goto L_0x2df7;
    L_0x2def:
        r4 = 1125515264; // 0x43160000 float:150.0 double:5.56078426E-315;
        r75 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r137 = r75;
    L_0x2df7:
        r0 = r145;
        r4 = r0.type;
        r6 = 3;
        if (r4 != r6) goto L_0x2e19;
    L_0x2dfe:
        r0 = r144;
        r4 = r0.infoWidth;
        r6 = 1109393408; // 0x42200000 float:40.0 double:5.481131706E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r0 = r137;
        if (r0 >= r4) goto L_0x2e19;
    L_0x2e0d:
        r0 = r144;
        r4 = r0.infoWidth;
        r6 = 1109393408; // 0x42200000 float:40.0 double:5.481131706E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r137 = r4 + r6;
    L_0x2e19:
        r0 = r144;
        r4 = r0.currentMessagesGroup;
        if (r4 == 0) goto L_0x30fe;
    L_0x2e1f:
        r71 = 0;
        r62 = r144.getGroupPhotosWidth();
        r40 = 0;
    L_0x2e27:
        r0 = r144;
        r4 = r0.currentMessagesGroup;
        r4 = r4.posArray;
        r4 = r4.size();
        r0 = r40;
        if (r0 >= r4) goto L_0x2fcc;
    L_0x2e35:
        r0 = r144;
        r4 = r0.currentMessagesGroup;
        r4 = r4.posArray;
        r0 = r40;
        r111 = r4.get(r0);
        r111 = (org.telegram.messenger.MessageObject.GroupedMessagePosition) r111;
        r0 = r111;
        r4 = r0.minY;
        if (r4 != 0) goto L_0x2fcc;
    L_0x2e49:
        r0 = r71;
        r8 = (double) r0;
        r0 = r111;
        r4 = r0.pw;
        r0 = r111;
        r6 = r0.leftSpanOffset;
        r4 = r4 + r6;
        r4 = (float) r4;
        r6 = 1148846080; // 0x447a0000 float:1000.0 double:5.676053805E-315;
        r4 = r4 / r6;
        r0 = r62;
        r6 = (float) r0;
        r4 = r4 * r6;
        r0 = (double) r4;
        r20 = r0;
        r20 = java.lang.Math.ceil(r20);
        r8 = r8 + r20;
        r0 = (int) r8;
        r71 = r0;
        r40 = r40 + 1;
        goto L_0x2e27;
    L_0x2e6c:
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 == 0) goto L_0x2e82;
    L_0x2e72:
        r4 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        r4 = (float) r4;
        r6 = 1060320051; // 0x3f333333 float:0.7 double:5.23867711E-315;
        r4 = r4 * r6;
        r0 = (int) r4;
        r109 = r0;
        r93 = r109;
        goto L_0x2c87;
    L_0x2e82:
        r4 = org.telegram.messenger.AndroidUtilities.displaySize;
        r4 = r4.x;
        r6 = org.telegram.messenger.AndroidUtilities.displaySize;
        r6 = r6.y;
        r4 = java.lang.Math.min(r4, r6);
        r4 = (float) r4;
        r6 = 1060320051; // 0x3f333333 float:0.7 double:5.23867711E-315;
        r4 = r4 * r6;
        r0 = (int) r4;
        r109 = r0;
        r93 = r109;
        goto L_0x2c87;
    L_0x2e9a:
        r0 = r145;
        r4 = r0.type;
        r6 = 3;
        if (r4 != r6) goto L_0x2ecd;
    L_0x2ea1:
        r4 = 0;
        r0 = r144;
        r1 = r145;
        r0.createDocumentLayout(r4, r1);
        r144.updateSecretTimeText(r145);
        r4 = r145.needDrawBluredPreview();
        if (r4 != 0) goto L_0x2ec2;
    L_0x2eb2:
        r0 = r144;
        r4 = r0.photoImage;
        r6 = 1;
        r4.setNeedsQualityThumb(r6);
        r0 = r144;
        r4 = r0.photoImage;
        r6 = 1;
        r4.setShouldGenerateQualityThumb(r6);
    L_0x2ec2:
        r0 = r144;
        r4 = r0.photoImage;
        r0 = r145;
        r4.setParentMessageObject(r0);
        goto L_0x2cdc;
    L_0x2ecd:
        r0 = r145;
        r4 = r0.type;
        r6 = 5;
        if (r4 != r6) goto L_0x2ef5;
    L_0x2ed4:
        r4 = r145.needDrawBluredPreview();
        if (r4 != 0) goto L_0x2eea;
    L_0x2eda:
        r0 = r144;
        r4 = r0.photoImage;
        r6 = 1;
        r4.setNeedsQualityThumb(r6);
        r0 = r144;
        r4 = r0.photoImage;
        r6 = 1;
        r4.setShouldGenerateQualityThumb(r6);
    L_0x2eea:
        r0 = r144;
        r4 = r0.photoImage;
        r0 = r145;
        r4.setParentMessageObject(r0);
        goto L_0x2cdc;
    L_0x2ef5:
        r0 = r145;
        r4 = r0.type;
        r6 = 8;
        if (r4 != r6) goto L_0x2cdc;
    L_0x2efd:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r4 = r4.document;
        r4 = r4.size;
        r8 = (long) r4;
        r5 = org.telegram.messenger.AndroidUtilities.formatFileSize(r8);
        r4 = org.telegram.ui.ActionBar.Theme.chat_infoPaint;
        r4 = r4.measureText(r5);
        r8 = (double) r4;
        r8 = java.lang.Math.ceil(r8);
        r4 = (int) r8;
        r0 = r144;
        r0.infoWidth = r4;
        r26 = new android.text.StaticLayout;
        r28 = org.telegram.ui.ActionBar.Theme.chat_infoPaint;
        r0 = r144;
        r0 = r0.infoWidth;
        r29 = r0;
        r30 = android.text.Layout.Alignment.ALIGN_NORMAL;
        r31 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r32 = 0;
        r33 = 0;
        r27 = r5;
        r26.<init>(r27, r28, r29, r30, r31, r32, r33);
        r0 = r26;
        r1 = r144;
        r1.infoLayout = r0;
        r4 = r145.needDrawBluredPreview();
        if (r4 != 0) goto L_0x2f4f;
    L_0x2f3f:
        r0 = r144;
        r4 = r0.photoImage;
        r6 = 1;
        r4.setNeedsQualityThumb(r6);
        r0 = r144;
        r4 = r0.photoImage;
        r6 = 1;
        r4.setShouldGenerateQualityThumb(r6);
    L_0x2f4f:
        r0 = r144;
        r4 = r0.photoImage;
        r0 = r145;
        r4.setParentMessageObject(r0);
        goto L_0x2cdc;
    L_0x2f5a:
        r4 = 1123024896; // 0x42f00000 float:120.0 double:5.548480205E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r0 = r75;
        if (r0 >= r4) goto L_0x2d6a;
    L_0x2f64:
        r4 = 1123024896; // 0x42f00000 float:120.0 double:5.548480205E-315;
        r75 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r4 = r4.h;
        r4 = (float) r4;
        r0 = r75;
        r6 = (float) r0;
        r76 = r4 / r6;
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r4 = r4.w;
        r4 = (float) r4;
        r4 = r4 / r76;
        r0 = r109;
        r6 = (float) r0;
        r4 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1));
        if (r4 >= 0) goto L_0x2d6a;
    L_0x2f86:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r4 = r4.w;
        r4 = (float) r4;
        r4 = r4 / r76;
        r0 = (int) r4;
        r137 = r0;
        goto L_0x2d6a;
    L_0x2f94:
        r4 = 1123024896; // 0x42f00000 float:120.0 double:5.548480205E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r0 = r75;
        if (r0 >= r4) goto L_0x2deb;
    L_0x2f9e:
        r4 = 1123024896; // 0x42f00000 float:120.0 double:5.548480205E-315;
        r75 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r0 = r46;
        r4 = r0.h;
        r4 = (float) r4;
        r0 = r75;
        r6 = (float) r0;
        r76 = r4 / r6;
        r0 = r46;
        r4 = r0.w;
        r4 = (float) r4;
        r4 = r4 / r76;
        r0 = r109;
        r6 = (float) r0;
        r4 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1));
        if (r4 >= 0) goto L_0x2deb;
    L_0x2fbc:
        r0 = r46;
        r4 = r0.w;
        r4 = (float) r4;
        r4 = r4 / r76;
        r0 = (int) r4;
        r137 = r0;
        goto L_0x2deb;
    L_0x2fc8:
        r40 = r40 + 1;
        goto L_0x2d83;
    L_0x2fcc:
        r4 = 1108082688; // 0x420c0000 float:35.0 double:5.47465589E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r71 - r4;
        r0 = r144;
        r0.availableTimeWidth = r4;
    L_0x2fd8:
        r0 = r145;
        r4 = r0.type;
        r6 = 5;
        if (r4 != r6) goto L_0x3006;
    L_0x2fdf:
        r0 = r144;
        r4 = r0.availableTimeWidth;
        r8 = (double) r4;
        r4 = org.telegram.ui.ActionBar.Theme.chat_audioTimePaint;
        r6 = "00:00";
        r4 = r4.measureText(r6);
        r0 = (double) r4;
        r20 = r0;
        r20 = java.lang.Math.ceil(r20);
        r4 = 1104150528; // 0x41d00000 float:26.0 double:5.455228437E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r0 = (double) r4;
        r26 = r0;
        r20 = r20 + r26;
        r8 = r8 - r20;
        r4 = (int) r8;
        r0 = r144;
        r0.availableTimeWidth = r4;
    L_0x3006:
        r144.measureTime(r145);
        r0 = r144;
        r6 = r0.timeWidth;
        r4 = r145.isOutOwner();
        if (r4 == 0) goto L_0x310c;
    L_0x3013:
        r4 = 20;
    L_0x3015:
        r4 = r4 + 14;
        r4 = (float) r4;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r131 = r6 + r4;
        r0 = r137;
        r1 = r131;
        if (r0 >= r1) goto L_0x3026;
    L_0x3024:
        r137 = r131;
    L_0x3026:
        r4 = r145.isRoundVideo();
        if (r4 == 0) goto L_0x310f;
    L_0x302c:
        r0 = r137;
        r1 = r75;
        r75 = java.lang.Math.min(r0, r1);
        r137 = r75;
        r4 = 0;
        r0 = r144;
        r0.drawBackground = r4;
        r0 = r144;
        r4 = r0.photoImage;
        r6 = r137 / 2;
        r4.setRoundRadius(r6);
    L_0x3044:
        r29 = 0;
        r0 = r144;
        r4 = r0.currentMessagesGroup;
        if (r4 == 0) goto L_0x35b8;
    L_0x304c:
        r4 = org.telegram.messenger.AndroidUtilities.displaySize;
        r4 = r4.x;
        r6 = org.telegram.messenger.AndroidUtilities.displaySize;
        r6 = r6.y;
        r4 = java.lang.Math.max(r4, r6);
        r4 = (float) r4;
        r6 = 1056964608; // 0x3f000000 float:0.5 double:5.222099017E-315;
        r92 = r4 * r6;
        r62 = r144.getGroupPhotosWidth();
        r0 = r144;
        r4 = r0.currentPosition;
        r4 = r4.pw;
        r4 = (float) r4;
        r6 = 1148846080; // 0x447a0000 float:1000.0 double:5.676053805E-315;
        r4 = r4 / r6;
        r0 = r62;
        r6 = (float) r0;
        r4 = r4 * r6;
        r8 = (double) r4;
        r8 = java.lang.Math.ceil(r8);
        r0 = (int) r8;
        r137 = r0;
        r0 = r144;
        r4 = r0.currentPosition;
        r4 = r4.minY;
        if (r4 == 0) goto L_0x3199;
    L_0x307f:
        r4 = r145.isOutOwner();
        if (r4 == 0) goto L_0x308f;
    L_0x3085:
        r0 = r144;
        r4 = r0.currentPosition;
        r4 = r4.flags;
        r4 = r4 & 1;
        if (r4 != 0) goto L_0x309f;
    L_0x308f:
        r4 = r145.isOutOwner();
        if (r4 != 0) goto L_0x3199;
    L_0x3095:
        r0 = r144;
        r4 = r0.currentPosition;
        r4 = r4.flags;
        r4 = r4 & 2;
        if (r4 == 0) goto L_0x3199;
    L_0x309f:
        r71 = 0;
        r60 = 0;
        r40 = 0;
    L_0x30a5:
        r0 = r144;
        r4 = r0.currentMessagesGroup;
        r4 = r4.posArray;
        r4 = r4.size();
        r0 = r40;
        if (r0 >= r4) goto L_0x3195;
    L_0x30b3:
        r0 = r144;
        r4 = r0.currentMessagesGroup;
        r4 = r4.posArray;
        r0 = r40;
        r111 = r4.get(r0);
        r111 = (org.telegram.messenger.MessageObject.GroupedMessagePosition) r111;
        r0 = r111;
        r4 = r0.minY;
        if (r4 != 0) goto L_0x3144;
    L_0x30c7:
        r0 = r71;
        r0 = (double) r0;
        r20 = r0;
        r0 = r111;
        r4 = r0.pw;
        r4 = (float) r4;
        r6 = 1148846080; // 0x447a0000 float:1000.0 double:5.676053805E-315;
        r4 = r4 / r6;
        r0 = r62;
        r6 = (float) r0;
        r4 = r4 * r6;
        r8 = (double) r4;
        r26 = java.lang.Math.ceil(r8);
        r0 = r111;
        r4 = r0.leftSpanOffset;
        if (r4 == 0) goto L_0x3141;
    L_0x30e3:
        r0 = r111;
        r4 = r0.leftSpanOffset;
        r4 = (float) r4;
        r6 = 1148846080; // 0x447a0000 float:1000.0 double:5.676053805E-315;
        r4 = r4 / r6;
        r0 = r62;
        r6 = (float) r0;
        r4 = r4 * r6;
        r8 = (double) r4;
        r8 = java.lang.Math.ceil(r8);
    L_0x30f4:
        r8 = r8 + r26;
        r8 = r8 + r20;
        r0 = (int) r8;
        r71 = r0;
    L_0x30fb:
        r40 = r40 + 1;
        goto L_0x30a5;
    L_0x30fe:
        r4 = 1096810496; // 0x41600000 float:14.0 double:5.41896386E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r93 - r4;
        r0 = r144;
        r0.availableTimeWidth = r4;
        goto L_0x2fd8;
    L_0x310c:
        r4 = 0;
        goto L_0x3015;
    L_0x310f:
        r4 = r145.needDrawBluredPreview();
        if (r4 == 0) goto L_0x3044;
    L_0x3115:
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 == 0) goto L_0x312a;
    L_0x311b:
        r4 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        r4 = (float) r4;
        r6 = 1056964608; // 0x3f000000 float:0.5 double:5.222099017E-315;
        r4 = r4 * r6;
        r0 = (int) r4;
        r75 = r0;
        r137 = r75;
        goto L_0x3044;
    L_0x312a:
        r4 = org.telegram.messenger.AndroidUtilities.displaySize;
        r4 = r4.x;
        r6 = org.telegram.messenger.AndroidUtilities.displaySize;
        r6 = r6.y;
        r4 = java.lang.Math.min(r4, r6);
        r4 = (float) r4;
        r6 = 1056964608; // 0x3f000000 float:0.5 double:5.222099017E-315;
        r4 = r4 * r6;
        r0 = (int) r4;
        r75 = r0;
        r137 = r75;
        goto L_0x3044;
    L_0x3141:
        r8 = 0;
        goto L_0x30f4;
    L_0x3144:
        r0 = r111;
        r4 = r0.minY;
        r0 = r144;
        r6 = r0.currentPosition;
        r6 = r6.minY;
        if (r4 != r6) goto L_0x3189;
    L_0x3150:
        r0 = r60;
        r0 = (double) r0;
        r20 = r0;
        r0 = r111;
        r4 = r0.pw;
        r4 = (float) r4;
        r6 = 1148846080; // 0x447a0000 float:1000.0 double:5.676053805E-315;
        r4 = r4 / r6;
        r0 = r62;
        r6 = (float) r0;
        r4 = r4 * r6;
        r8 = (double) r4;
        r26 = java.lang.Math.ceil(r8);
        r0 = r111;
        r4 = r0.leftSpanOffset;
        if (r4 == 0) goto L_0x3186;
    L_0x316c:
        r0 = r111;
        r4 = r0.leftSpanOffset;
        r4 = (float) r4;
        r6 = 1148846080; // 0x447a0000 float:1000.0 double:5.676053805E-315;
        r4 = r4 / r6;
        r0 = r62;
        r6 = (float) r0;
        r4 = r4 * r6;
        r8 = (double) r4;
        r8 = java.lang.Math.ceil(r8);
    L_0x317d:
        r8 = r8 + r26;
        r8 = r8 + r20;
        r0 = (int) r8;
        r60 = r0;
        goto L_0x30fb;
    L_0x3186:
        r8 = 0;
        goto L_0x317d;
    L_0x3189:
        r0 = r111;
        r4 = r0.minY;
        r0 = r144;
        r6 = r0.currentPosition;
        r6 = r6.minY;
        if (r4 <= r6) goto L_0x30fb;
    L_0x3195:
        r4 = r71 - r60;
        r137 = r137 + r4;
    L_0x3199:
        r4 = 1091567616; // 0x41100000 float:9.0 double:5.39306059E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r137 = r137 - r4;
        r0 = r144;
        r4 = r0.isAvatarVisible;
        if (r4 == 0) goto L_0x31af;
    L_0x31a7:
        r4 = 1111490560; // 0x42400000 float:48.0 double:5.491493014E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r137 = r137 - r4;
    L_0x31af:
        r0 = r144;
        r4 = r0.currentPosition;
        r4 = r4.siblingHeights;
        if (r4 == 0) goto L_0x3318;
    L_0x31b7:
        r75 = 0;
        r40 = 0;
    L_0x31bb:
        r0 = r144;
        r4 = r0.currentPosition;
        r4 = r4.siblingHeights;
        r4 = r4.length;
        r0 = r40;
        if (r0 >= r4) goto L_0x31db;
    L_0x31c6:
        r0 = r144;
        r4 = r0.currentPosition;
        r4 = r4.siblingHeights;
        r4 = r4[r40];
        r4 = r4 * r92;
        r8 = (double) r4;
        r8 = java.lang.Math.ceil(r8);
        r4 = (int) r8;
        r75 = r75 + r4;
        r40 = r40 + 1;
        goto L_0x31bb;
    L_0x31db:
        r0 = r144;
        r4 = r0.currentPosition;
        r4 = r4.maxY;
        r0 = r144;
        r6 = r0.currentPosition;
        r6 = r6.minY;
        r4 = r4 - r6;
        r6 = 1093664768; // 0x41300000 float:11.0 double:5.4034219E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 * r6;
        r75 = r75 + r4;
    L_0x31f1:
        r0 = r137;
        r1 = r144;
        r1.backgroundWidth = r0;
        r4 = 1094713344; // 0x41400000 float:12.0 double:5.408602553E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r137 = r137 - r4;
        r109 = r137;
        r0 = r144;
        r4 = r0.currentPosition;
        r4 = r4.edge;
        if (r4 != 0) goto L_0x3211;
    L_0x3209:
        r4 = 1092616192; // 0x41200000 float:10.0 double:5.398241246E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r109 = r109 + r4;
    L_0x3211:
        r108 = r75;
        r4 = 1092616192; // 0x41200000 float:10.0 double:5.398241246E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r109 - r4;
        r29 = r29 + r4;
        r0 = r144;
        r4 = r0.currentPosition;
        r4 = r4.flags;
        r4 = r4 & 8;
        if (r4 != 0) goto L_0x3239;
    L_0x3227:
        r0 = r144;
        r4 = r0.currentMessagesGroup;
        r4 = r4.hasSibling;
        if (r4 == 0) goto L_0x33fa;
    L_0x322f:
        r0 = r144;
        r4 = r0.currentPosition;
        r4 = r4.flags;
        r4 = r4 & 4;
        if (r4 != 0) goto L_0x33fa;
    L_0x3239:
        r0 = r144;
        r4 = r0.currentPosition;
        r0 = r144;
        r4 = r0.getAdditionalWidthForPosition(r4);
        r29 = r29 + r4;
        r0 = r144;
        r4 = r0.currentMessagesGroup;
        r4 = r4.messages;
        r59 = r4.size();
        r79 = 0;
    L_0x3251:
        r0 = r79;
        r1 = r59;
        if (r0 >= r1) goto L_0x33fa;
    L_0x3257:
        r0 = r144;
        r4 = r0.currentMessagesGroup;
        r4 = r4.messages;
        r0 = r79;
        r88 = r4.get(r0);
        r88 = (org.telegram.messenger.MessageObject) r88;
        r0 = r144;
        r4 = r0.currentMessagesGroup;
        r4 = r4.posArray;
        r0 = r79;
        r118 = r4.get(r0);
        r118 = (org.telegram.messenger.MessageObject.GroupedMessagePosition) r118;
        r0 = r144;
        r4 = r0.currentPosition;
        r0 = r118;
        if (r0 == r4) goto L_0x33e9;
    L_0x327b:
        r0 = r118;
        r4 = r0.flags;
        r4 = r4 & 8;
        if (r4 == 0) goto L_0x33e9;
    L_0x3283:
        r0 = r118;
        r4 = r0.pw;
        r4 = (float) r4;
        r6 = 1148846080; // 0x447a0000 float:1000.0 double:5.676053805E-315;
        r4 = r4 / r6;
        r0 = r62;
        r6 = (float) r0;
        r4 = r4 * r6;
        r8 = (double) r4;
        r8 = java.lang.Math.ceil(r8);
        r0 = (int) r8;
        r137 = r0;
        r0 = r118;
        r4 = r0.minY;
        if (r4 == 0) goto L_0x337d;
    L_0x329d:
        r4 = r145.isOutOwner();
        if (r4 == 0) goto L_0x32ab;
    L_0x32a3:
        r0 = r118;
        r4 = r0.flags;
        r4 = r4 & 1;
        if (r4 != 0) goto L_0x32b9;
    L_0x32ab:
        r4 = r145.isOutOwner();
        if (r4 != 0) goto L_0x337d;
    L_0x32b1:
        r0 = r118;
        r4 = r0.flags;
        r4 = r4 & 2;
        if (r4 == 0) goto L_0x337d;
    L_0x32b9:
        r71 = 0;
        r60 = 0;
        r40 = 0;
    L_0x32bf:
        r0 = r144;
        r4 = r0.currentMessagesGroup;
        r4 = r4.posArray;
        r4 = r4.size();
        r0 = r40;
        if (r0 >= r4) goto L_0x3379;
    L_0x32cd:
        r0 = r144;
        r4 = r0.currentMessagesGroup;
        r4 = r4.posArray;
        r0 = r40;
        r111 = r4.get(r0);
        r111 = (org.telegram.messenger.MessageObject.GroupedMessagePosition) r111;
        r0 = r111;
        r4 = r0.minY;
        if (r4 != 0) goto L_0x332d;
    L_0x32e1:
        r0 = r71;
        r0 = (double) r0;
        r20 = r0;
        r0 = r111;
        r4 = r0.pw;
        r4 = (float) r4;
        r6 = 1148846080; // 0x447a0000 float:1000.0 double:5.676053805E-315;
        r4 = r4 / r6;
        r0 = r62;
        r6 = (float) r0;
        r4 = r4 * r6;
        r8 = (double) r4;
        r26 = java.lang.Math.ceil(r8);
        r0 = r111;
        r4 = r0.leftSpanOffset;
        if (r4 == 0) goto L_0x332a;
    L_0x32fd:
        r0 = r111;
        r4 = r0.leftSpanOffset;
        r4 = (float) r4;
        r6 = 1148846080; // 0x447a0000 float:1000.0 double:5.676053805E-315;
        r4 = r4 / r6;
        r0 = r62;
        r6 = (float) r0;
        r4 = r4 * r6;
        r8 = (double) r4;
        r8 = java.lang.Math.ceil(r8);
    L_0x330e:
        r8 = r8 + r26;
        r8 = r8 + r20;
        r0 = (int) r8;
        r71 = r0;
    L_0x3315:
        r40 = r40 + 1;
        goto L_0x32bf;
    L_0x3318:
        r0 = r144;
        r4 = r0.currentPosition;
        r4 = r4.ph;
        r4 = r4 * r92;
        r8 = (double) r4;
        r8 = java.lang.Math.ceil(r8);
        r0 = (int) r8;
        r75 = r0;
        goto L_0x31f1;
    L_0x332a:
        r8 = 0;
        goto L_0x330e;
    L_0x332d:
        r0 = r111;
        r4 = r0.minY;
        r0 = r118;
        r6 = r0.minY;
        if (r4 != r6) goto L_0x336f;
    L_0x3337:
        r0 = r60;
        r0 = (double) r0;
        r20 = r0;
        r0 = r111;
        r4 = r0.pw;
        r4 = (float) r4;
        r6 = 1148846080; // 0x447a0000 float:1000.0 double:5.676053805E-315;
        r4 = r4 / r6;
        r0 = r62;
        r6 = (float) r0;
        r4 = r4 * r6;
        r8 = (double) r4;
        r26 = java.lang.Math.ceil(r8);
        r0 = r111;
        r4 = r0.leftSpanOffset;
        if (r4 == 0) goto L_0x336c;
    L_0x3353:
        r0 = r111;
        r4 = r0.leftSpanOffset;
        r4 = (float) r4;
        r6 = 1148846080; // 0x447a0000 float:1000.0 double:5.676053805E-315;
        r4 = r4 / r6;
        r0 = r62;
        r6 = (float) r0;
        r4 = r4 * r6;
        r8 = (double) r4;
        r8 = java.lang.Math.ceil(r8);
    L_0x3364:
        r8 = r8 + r26;
        r8 = r8 + r20;
        r0 = (int) r8;
        r60 = r0;
        goto L_0x3315;
    L_0x336c:
        r8 = 0;
        goto L_0x3364;
    L_0x336f:
        r0 = r111;
        r4 = r0.minY;
        r0 = r118;
        r6 = r0.minY;
        if (r4 <= r6) goto L_0x3315;
    L_0x3379:
        r4 = r71 - r60;
        r137 = r137 + r4;
    L_0x337d:
        r4 = 1099956224; // 0x41900000 float:18.0 double:5.43450582E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r137 = r137 - r4;
        r0 = r144;
        r4 = r0.isChat;
        if (r4 == 0) goto L_0x33a7;
    L_0x338b:
        r4 = r88.isOutOwner();
        if (r4 != 0) goto L_0x33a7;
    L_0x3391:
        r4 = r88.needDrawAvatar();
        if (r4 == 0) goto L_0x33a7;
    L_0x3397:
        if (r118 == 0) goto L_0x339f;
    L_0x3399:
        r0 = r118;
        r4 = r0.edge;
        if (r4 == 0) goto L_0x33a7;
    L_0x339f:
        r4 = 1111490560; // 0x42400000 float:48.0 double:5.491493014E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r137 = r137 - r4;
    L_0x33a7:
        r0 = r144;
        r1 = r118;
        r4 = r0.getAdditionalWidthForPosition(r1);
        r137 = r137 + r4;
        r0 = r118;
        r4 = r0.edge;
        if (r4 != 0) goto L_0x33bf;
    L_0x33b7:
        r4 = 1092616192; // 0x41200000 float:10.0 double:5.398241246E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r137 = r137 + r4;
    L_0x33bf:
        r29 = r29 + r137;
        r0 = r118;
        r4 = r0.minX;
        r0 = r144;
        r6 = r0.currentPosition;
        r6 = r6.minX;
        if (r4 < r6) goto L_0x33df;
    L_0x33cd:
        r0 = r144;
        r4 = r0.currentMessagesGroup;
        r4 = r4.hasSibling;
        if (r4 == 0) goto L_0x33e9;
    L_0x33d5:
        r0 = r118;
        r4 = r0.minY;
        r0 = r118;
        r6 = r0.maxY;
        if (r4 == r6) goto L_0x33e9;
    L_0x33df:
        r0 = r144;
        r4 = r0.captionOffsetX;
        r4 = r4 - r137;
        r0 = r144;
        r0.captionOffsetX = r4;
    L_0x33e9:
        r0 = r88;
        r4 = r0.caption;
        if (r4 == 0) goto L_0x35b4;
    L_0x33ef:
        r0 = r144;
        r4 = r0.currentCaption;
        if (r4 == 0) goto L_0x35ac;
    L_0x33f5:
        r4 = 0;
        r0 = r144;
        r0.currentCaption = r4;
    L_0x33fa:
        r0 = r144;
        r4 = r0.currentCaption;
        if (r4 == 0) goto L_0x34c5;
    L_0x3400:
        r4 = android.os.Build.VERSION.SDK_INT;	 Catch:{ Exception -> 0x360c }
        r6 = 24;
        if (r4 < r6) goto L_0x35ef;
    L_0x3406:
        r0 = r144;
        r4 = r0.currentCaption;	 Catch:{ Exception -> 0x360c }
        r6 = 0;
        r0 = r144;
        r8 = r0.currentCaption;	 Catch:{ Exception -> 0x360c }
        r8 = r8.length();	 Catch:{ Exception -> 0x360c }
        r9 = org.telegram.ui.ActionBar.Theme.chat_msgTextPaint;	 Catch:{ Exception -> 0x360c }
        r0 = r29;
        r4 = android.text.StaticLayout.Builder.obtain(r4, r6, r8, r9, r0);	 Catch:{ Exception -> 0x360c }
        r6 = 1;
        r4 = r4.setBreakStrategy(r6);	 Catch:{ Exception -> 0x360c }
        r6 = 0;
        r4 = r4.setHyphenationFrequency(r6);	 Catch:{ Exception -> 0x360c }
        r6 = android.text.Layout.Alignment.ALIGN_NORMAL;	 Catch:{ Exception -> 0x360c }
        r4 = r4.setAlignment(r6);	 Catch:{ Exception -> 0x360c }
        r4 = r4.build();	 Catch:{ Exception -> 0x360c }
        r0 = r144;
        r0.captionLayout = r4;	 Catch:{ Exception -> 0x360c }
    L_0x3433:
        r0 = r144;
        r4 = r0.captionLayout;	 Catch:{ Exception -> 0x360c }
        r4 = r4.getLineCount();	 Catch:{ Exception -> 0x360c }
        if (r4 <= 0) goto L_0x34c5;
    L_0x343d:
        r0 = r29;
        r1 = r144;
        r1.captionWidth = r0;	 Catch:{ Exception -> 0x360c }
        r0 = r144;
        r4 = r0.captionLayout;	 Catch:{ Exception -> 0x360c }
        r4 = r4.getHeight();	 Catch:{ Exception -> 0x360c }
        r0 = r144;
        r0.captionHeight = r4;	 Catch:{ Exception -> 0x360c }
        r0 = r144;
        r4 = r0.captionHeight;	 Catch:{ Exception -> 0x360c }
        r6 = 1091567616; // 0x41100000 float:9.0 double:5.39306059E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);	 Catch:{ Exception -> 0x360c }
        r4 = r4 + r6;
        r0 = r144;
        r0.addedCaptionHeight = r4;	 Catch:{ Exception -> 0x360c }
        r0 = r144;
        r4 = r0.currentPosition;	 Catch:{ Exception -> 0x360c }
        if (r4 == 0) goto L_0x346e;
    L_0x3464:
        r0 = r144;
        r4 = r0.currentPosition;	 Catch:{ Exception -> 0x360c }
        r4 = r4.flags;	 Catch:{ Exception -> 0x360c }
        r4 = r4 & 8;
        if (r4 == 0) goto L_0x3612;
    L_0x346e:
        r0 = r144;
        r4 = r0.addedCaptionHeight;	 Catch:{ Exception -> 0x360c }
        r42 = r42 + r4;
        r0 = r144;
        r4 = r0.captionLayout;	 Catch:{ Exception -> 0x360c }
        r0 = r144;
        r6 = r0.captionLayout;	 Catch:{ Exception -> 0x360c }
        r6 = r6.getLineCount();	 Catch:{ Exception -> 0x360c }
        r6 = r6 + -1;
        r4 = r4.getLineWidth(r6);	 Catch:{ Exception -> 0x360c }
        r0 = r144;
        r6 = r0.captionLayout;	 Catch:{ Exception -> 0x360c }
        r0 = r144;
        r8 = r0.captionLayout;	 Catch:{ Exception -> 0x360c }
        r8 = r8.getLineCount();	 Catch:{ Exception -> 0x360c }
        r8 = r8 + -1;
        r6 = r6.getLineLeft(r8);	 Catch:{ Exception -> 0x360c }
        r83 = r4 + r6;
        r4 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);	 Catch:{ Exception -> 0x360c }
        r4 = r4 + r29;
        r4 = (float) r4;	 Catch:{ Exception -> 0x360c }
        r4 = r4 - r83;
        r0 = r131;
        r6 = (float) r0;	 Catch:{ Exception -> 0x360c }
        r4 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1));
        if (r4 >= 0) goto L_0x34c5;
    L_0x34ac:
        r4 = 1096810496; // 0x41600000 float:14.0 double:5.41896386E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);	 Catch:{ Exception -> 0x360c }
        r42 = r42 + r4;
        r0 = r144;
        r4 = r0.addedCaptionHeight;	 Catch:{ Exception -> 0x360c }
        r6 = 1096810496; // 0x41600000 float:14.0 double:5.41896386E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);	 Catch:{ Exception -> 0x360c }
        r4 = r4 + r6;
        r0 = r144;
        r0.addedCaptionHeight = r4;	 Catch:{ Exception -> 0x360c }
        r57 = 1;
    L_0x34c5:
        r4 = java.util.Locale.US;
        r6 = "%d_%d";
        r8 = 2;
        r8 = new java.lang.Object[r8];
        r9 = 0;
        r0 = r137;
        r10 = (float) r0;
        r12 = org.telegram.messenger.AndroidUtilities.density;
        r10 = r10 / r12;
        r10 = (int) r10;
        r10 = java.lang.Integer.valueOf(r10);
        r8[r9] = r10;
        r9 = 1;
        r0 = r75;
        r10 = (float) r0;
        r12 = org.telegram.messenger.AndroidUtilities.density;
        r10 = r10 / r12;
        r10 = (int) r10;
        r10 = java.lang.Integer.valueOf(r10);
        r8[r9] = r10;
        r4 = java.lang.String.format(r4, r6, r8);
        r0 = r144;
        r0.currentPhotoFilterThumb = r4;
        r0 = r144;
        r0.currentPhotoFilter = r4;
        r0 = r145;
        r4 = r0.photoThumbs;
        if (r4 == 0) goto L_0x3506;
    L_0x34fb:
        r0 = r145;
        r4 = r0.photoThumbs;
        r4 = r4.size();
        r6 = 1;
        if (r4 > r6) goto L_0x351c;
    L_0x3506:
        r0 = r145;
        r4 = r0.type;
        r6 = 3;
        if (r4 == r6) goto L_0x351c;
    L_0x350d:
        r0 = r145;
        r4 = r0.type;
        r6 = 8;
        if (r4 == r6) goto L_0x351c;
    L_0x3515:
        r0 = r145;
        r4 = r0.type;
        r6 = 5;
        if (r4 != r6) goto L_0x355a;
    L_0x351c:
        r4 = r145.needDrawBluredPreview();
        if (r4 == 0) goto L_0x3619;
    L_0x3522:
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r0 = r144;
        r6 = r0.currentPhotoFilter;
        r4 = r4.append(r6);
        r6 = "_b2";
        r4 = r4.append(r6);
        r4 = r4.toString();
        r0 = r144;
        r0.currentPhotoFilter = r4;
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r0 = r144;
        r6 = r0.currentPhotoFilterThumb;
        r4 = r4.append(r6);
        r6 = "_b2";
        r4 = r4.append(r6);
        r4 = r4.toString();
        r0 = r144;
        r0.currentPhotoFilterThumb = r4;
    L_0x355a:
        r100 = 0;
        r0 = r145;
        r4 = r0.type;
        r6 = 3;
        if (r4 == r6) goto L_0x3572;
    L_0x3563:
        r0 = r145;
        r4 = r0.type;
        r6 = 8;
        if (r4 == r6) goto L_0x3572;
    L_0x356b:
        r0 = r145;
        r4 = r0.type;
        r6 = 5;
        if (r4 != r6) goto L_0x3574;
    L_0x3572:
        r100 = 1;
    L_0x3574:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        if (r4 == 0) goto L_0x358b;
    L_0x357a:
        if (r100 != 0) goto L_0x358b;
    L_0x357c:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r4 = r4.size;
        if (r4 != 0) goto L_0x358b;
    L_0x3584:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r6 = -1;
        r4.size = r6;
    L_0x358b:
        r0 = r145;
        r4 = r0.type;
        r6 = 1;
        if (r4 != r6) goto L_0x3722;
    L_0x3592:
        r0 = r145;
        r4 = r0.useCustomPhoto;
        if (r4 == 0) goto L_0x3637;
    L_0x3598:
        r0 = r144;
        r4 = r0.photoImage;
        r6 = r144.getResources();
        r8 = 2131165659; // 0x7f0701db float:1.7945541E38 double:1.0529357377E-314;
        r6 = r6.getDrawable(r8);
        r4.setImageBitmap(r6);
        goto L_0x2374;
    L_0x35ac:
        r0 = r88;
        r4 = r0.caption;
        r0 = r144;
        r0.currentCaption = r4;
    L_0x35b4:
        r79 = r79 + 1;
        goto L_0x3251;
    L_0x35b8:
        r109 = r137;
        r108 = r75;
        r4 = 1094713344; // 0x41400000 float:12.0 double:5.408602553E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r4 + r137;
        r0 = r144;
        r0.backgroundWidth = r4;
        r0 = r144;
        r4 = r0.mediaBackground;
        if (r4 != 0) goto L_0x35dd;
    L_0x35ce:
        r0 = r144;
        r4 = r0.backgroundWidth;
        r6 = 1091567616; // 0x41100000 float:9.0 double:5.39306059E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r0 = r144;
        r0.backgroundWidth = r4;
    L_0x35dd:
        r0 = r145;
        r4 = r0.caption;
        r0 = r144;
        r0.currentCaption = r4;
        r4 = 1092616192; // 0x41200000 float:10.0 double:5.398241246E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r29 = r109 - r4;
        goto L_0x33fa;
    L_0x35ef:
        r26 = new android.text.StaticLayout;	 Catch:{ Exception -> 0x360c }
        r0 = r144;
        r0 = r0.currentCaption;	 Catch:{ Exception -> 0x360c }
        r27 = r0;
        r28 = org.telegram.ui.ActionBar.Theme.chat_msgTextPaint;	 Catch:{ Exception -> 0x360c }
        r30 = android.text.Layout.Alignment.ALIGN_NORMAL;	 Catch:{ Exception -> 0x360c }
        r31 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r32 = 0;
        r33 = 0;
        r26.<init>(r27, r28, r29, r30, r31, r32, r33);	 Catch:{ Exception -> 0x360c }
        r0 = r26;
        r1 = r144;
        r1.captionLayout = r0;	 Catch:{ Exception -> 0x360c }
        goto L_0x3433;
    L_0x360c:
        r69 = move-exception;
        org.telegram.messenger.FileLog.e(r69);
        goto L_0x34c5;
    L_0x3612:
        r4 = 0;
        r0 = r144;
        r0.captionLayout = r4;	 Catch:{ Exception -> 0x360c }
        goto L_0x34c5;
    L_0x3619:
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r0 = r144;
        r6 = r0.currentPhotoFilterThumb;
        r4 = r4.append(r6);
        r6 = "_b";
        r4 = r4.append(r6);
        r4 = r4.toString();
        r0 = r144;
        r0.currentPhotoFilterThumb = r4;
        goto L_0x355a;
    L_0x3637:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        if (r4 == 0) goto L_0x3716;
    L_0x363d:
        r107 = 1;
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r70 = org.telegram.messenger.FileLoader.getAttachFileName(r4);
        r0 = r145;
        r4 = r0.mediaExists;
        if (r4 == 0) goto L_0x36bd;
    L_0x364d:
        r0 = r144;
        r4 = r0.currentAccount;
        r4 = org.telegram.messenger.DownloadController.getInstance(r4);
        r0 = r144;
        r4.removeLoadingFileObserver(r0);
    L_0x365a:
        if (r107 != 0) goto L_0x367e;
    L_0x365c:
        r0 = r144;
        r4 = r0.currentAccount;
        r4 = org.telegram.messenger.DownloadController.getInstance(r4);
        r0 = r144;
        r6 = r0.currentMessageObject;
        r4 = r4.canDownloadMedia(r6);
        if (r4 != 0) goto L_0x367e;
    L_0x366e:
        r0 = r144;
        r4 = r0.currentAccount;
        r4 = org.telegram.messenger.FileLoader.getInstance(r4);
        r0 = r70;
        r4 = r4.isLoadingFile(r0);
        if (r4 == 0) goto L_0x36cf;
    L_0x367e:
        r0 = r144;
        r0 = r0.photoImage;
        r30 = r0;
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r0 = r4.location;
        r31 = r0;
        r0 = r144;
        r0 = r0.currentPhotoFilter;
        r32 = r0;
        r0 = r144;
        r4 = r0.currentPhotoObjectThumb;
        if (r4 == 0) goto L_0x36c0;
    L_0x3698:
        r0 = r144;
        r4 = r0.currentPhotoObjectThumb;
        r0 = r4.location;
        r33 = r0;
    L_0x36a0:
        r0 = r144;
        r0 = r0.currentPhotoFilterThumb;
        r34 = r0;
        if (r100 == 0) goto L_0x36c3;
    L_0x36a8:
        r35 = 0;
    L_0x36aa:
        r36 = 0;
        r0 = r144;
        r4 = r0.currentMessageObject;
        r4 = r4.shouldEncryptPhotoOrVideo();
        if (r4 == 0) goto L_0x36cc;
    L_0x36b6:
        r37 = 2;
    L_0x36b8:
        r30.setImage(r31, r32, r33, r34, r35, r36, r37);
        goto L_0x2374;
    L_0x36bd:
        r107 = 0;
        goto L_0x365a;
    L_0x36c0:
        r33 = 0;
        goto L_0x36a0;
    L_0x36c3:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r0 = r4.size;
        r35 = r0;
        goto L_0x36aa;
    L_0x36cc:
        r37 = 0;
        goto L_0x36b8;
    L_0x36cf:
        r4 = 1;
        r0 = r144;
        r0.photoNotSet = r4;
        r0 = r144;
        r4 = r0.currentPhotoObjectThumb;
        if (r4 == 0) goto L_0x370a;
    L_0x36da:
        r0 = r144;
        r0 = r0.photoImage;
        r30 = r0;
        r31 = 0;
        r32 = 0;
        r0 = r144;
        r4 = r0.currentPhotoObjectThumb;
        r0 = r4.location;
        r33 = r0;
        r0 = r144;
        r0 = r0.currentPhotoFilterThumb;
        r34 = r0;
        r35 = 0;
        r36 = 0;
        r0 = r144;
        r4 = r0.currentMessageObject;
        r4 = r4.shouldEncryptPhotoOrVideo();
        if (r4 == 0) goto L_0x3707;
    L_0x3700:
        r37 = 2;
    L_0x3702:
        r30.setImage(r31, r32, r33, r34, r35, r36, r37);
        goto L_0x2374;
    L_0x3707:
        r37 = 0;
        goto L_0x3702;
    L_0x370a:
        r0 = r144;
        r6 = r0.photoImage;
        r4 = 0;
        r4 = (android.graphics.drawable.Drawable) r4;
        r6.setImageBitmap(r4);
        goto L_0x2374;
    L_0x3716:
        r0 = r144;
        r6 = r0.photoImage;
        r4 = 0;
        r4 = (android.graphics.drawable.Drawable) r4;
        r6.setImageBitmap(r4);
        goto L_0x2374;
    L_0x3722:
        r0 = r145;
        r4 = r0.type;
        r6 = 8;
        if (r4 == r6) goto L_0x3731;
    L_0x372a:
        r0 = r145;
        r4 = r0.type;
        r6 = 5;
        if (r4 != r6) goto L_0x3868;
    L_0x3731:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r4 = r4.document;
        r70 = org.telegram.messenger.FileLoader.getAttachFileName(r4);
        r87 = 0;
        r0 = r145;
        r4 = r0.attachPathExists;
        if (r4 == 0) goto L_0x37cc;
    L_0x3745:
        r0 = r144;
        r4 = r0.currentAccount;
        r4 = org.telegram.messenger.DownloadController.getInstance(r4);
        r0 = r144;
        r4.removeLoadingFileObserver(r0);
        r87 = 1;
    L_0x3754:
        r49 = 0;
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r4 = r4.document;
        r4 = org.telegram.messenger.MessageObject.isNewGifDocument(r4);
        if (r4 == 0) goto L_0x37d5;
    L_0x3764:
        r0 = r144;
        r4 = r0.currentAccount;
        r4 = org.telegram.messenger.DownloadController.getInstance(r4);
        r0 = r144;
        r6 = r0.currentMessageObject;
        r49 = r4.canDownloadMedia(r6);
    L_0x3774:
        r4 = r145.isSending();
        if (r4 != 0) goto L_0x3837;
    L_0x377a:
        r4 = r145.isEditing();
        if (r4 != 0) goto L_0x3837;
    L_0x3780:
        if (r87 != 0) goto L_0x3794;
    L_0x3782:
        r0 = r144;
        r4 = r0.currentAccount;
        r4 = org.telegram.messenger.FileLoader.getInstance(r4);
        r0 = r70;
        r4 = r4.isLoadingFile(r0);
        if (r4 != 0) goto L_0x3794;
    L_0x3792:
        if (r49 == 0) goto L_0x3837;
    L_0x3794:
        r4 = 1;
        r0 = r87;
        if (r0 != r4) goto L_0x37f9;
    L_0x3799:
        r0 = r144;
        r0 = r0.photoImage;
        r30 = r0;
        r31 = 0;
        r4 = r145.isSendError();
        if (r4 == 0) goto L_0x37ed;
    L_0x37a7:
        r32 = 0;
    L_0x37a9:
        r33 = 0;
        r34 = 0;
        r0 = r144;
        r4 = r0.currentPhotoObject;
        if (r4 == 0) goto L_0x37f6;
    L_0x37b3:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r0 = r4.location;
        r35 = r0;
    L_0x37bb:
        r0 = r144;
        r0 = r0.currentPhotoFilterThumb;
        r36 = r0;
        r37 = 0;
        r38 = 0;
        r39 = 0;
        r30.setImage(r31, r32, r33, r34, r35, r36, r37, r38, r39);
        goto L_0x2374;
    L_0x37cc:
        r0 = r145;
        r4 = r0.mediaExists;
        if (r4 == 0) goto L_0x3754;
    L_0x37d2:
        r87 = 2;
        goto L_0x3754;
    L_0x37d5:
        r0 = r145;
        r4 = r0.type;
        r6 = 5;
        if (r4 != r6) goto L_0x3774;
    L_0x37dc:
        r0 = r144;
        r4 = r0.currentAccount;
        r4 = org.telegram.messenger.DownloadController.getInstance(r4);
        r0 = r144;
        r6 = r0.currentMessageObject;
        r49 = r4.canDownloadMedia(r6);
        goto L_0x3774;
    L_0x37ed:
        r0 = r145;
        r4 = r0.messageOwner;
        r0 = r4.attachPath;
        r32 = r0;
        goto L_0x37a9;
    L_0x37f6:
        r35 = 0;
        goto L_0x37bb;
    L_0x37f9:
        r0 = r144;
        r0 = r0.photoImage;
        r30 = r0;
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r0 = r4.document;
        r31 = r0;
        r32 = 0;
        r0 = r144;
        r4 = r0.currentPhotoObject;
        if (r4 == 0) goto L_0x3834;
    L_0x3811:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r0 = r4.location;
        r33 = r0;
    L_0x3819:
        r0 = r144;
        r0 = r0.currentPhotoFilterThumb;
        r34 = r0;
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r4 = r4.document;
        r0 = r4.size;
        r35 = r0;
        r36 = 0;
        r37 = 0;
        r30.setImage(r31, r32, r33, r34, r35, r36, r37);
        goto L_0x2374;
    L_0x3834:
        r33 = 0;
        goto L_0x3819;
    L_0x3837:
        r4 = 1;
        r0 = r144;
        r0.photoNotSet = r4;
        r0 = r144;
        r0 = r0.photoImage;
        r30 = r0;
        r31 = 0;
        r32 = 0;
        r0 = r144;
        r4 = r0.currentPhotoObject;
        if (r4 == 0) goto L_0x3865;
    L_0x384c:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r0 = r4.location;
        r33 = r0;
    L_0x3854:
        r0 = r144;
        r0 = r0.currentPhotoFilterThumb;
        r34 = r0;
        r35 = 0;
        r36 = 0;
        r37 = 0;
        r30.setImage(r31, r32, r33, r34, r35, r36, r37);
        goto L_0x2374;
    L_0x3865:
        r33 = 0;
        goto L_0x3854;
    L_0x3868:
        r0 = r144;
        r0 = r0.photoImage;
        r30 = r0;
        r31 = 0;
        r32 = 0;
        r0 = r144;
        r4 = r0.currentPhotoObject;
        if (r4 == 0) goto L_0x389b;
    L_0x3878:
        r0 = r144;
        r4 = r0.currentPhotoObject;
        r0 = r4.location;
        r33 = r0;
    L_0x3880:
        r0 = r144;
        r0 = r0.currentPhotoFilterThumb;
        r34 = r0;
        r35 = 0;
        r36 = 0;
        r0 = r144;
        r4 = r0.currentMessageObject;
        r4 = r4.shouldEncryptPhotoOrVideo();
        if (r4 == 0) goto L_0x389e;
    L_0x3894:
        r37 = 2;
    L_0x3896:
        r30.setImage(r31, r32, r33, r34, r35, r36, r37);
        goto L_0x2374;
    L_0x389b:
        r33 = 0;
        goto L_0x3880;
    L_0x389e:
        r37 = 0;
        goto L_0x3896;
    L_0x38a1:
        r0 = r144;
        r4 = r0.drawNameLayout;
        if (r4 == 0) goto L_0x23a7;
    L_0x38a7:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.reply_to_msg_id;
        if (r4 != 0) goto L_0x23a7;
    L_0x38af:
        r0 = r144;
        r4 = r0.namesOffset;
        r6 = 1088421888; // 0x40e00000 float:7.0 double:5.37751863E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r0 = r144;
        r0.namesOffset = r4;
        goto L_0x23a7;
    L_0x38c0:
        r26 = new android.text.StaticLayout;	 Catch:{ Exception -> 0x38dd }
        r0 = r145;
        r0 = r0.caption;	 Catch:{ Exception -> 0x38dd }
        r27 = r0;
        r28 = org.telegram.ui.ActionBar.Theme.chat_msgTextPaint;	 Catch:{ Exception -> 0x38dd }
        r30 = android.text.Layout.Alignment.ALIGN_NORMAL;	 Catch:{ Exception -> 0x38dd }
        r31 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r32 = 0;
        r33 = 0;
        r26.<init>(r27, r28, r29, r30, r31, r32, r33);	 Catch:{ Exception -> 0x38dd }
        r0 = r26;
        r1 = r144;
        r1.captionLayout = r0;	 Catch:{ Exception -> 0x38dd }
        goto L_0x115f;
    L_0x38dd:
        r69 = move-exception;
        org.telegram.messenger.FileLog.e(r69);
        goto L_0x11f9;
    L_0x38e3:
        r4 = 0;
        goto L_0x117f;
    L_0x38e6:
        r0 = r144;
        r4 = r0.widthBeforeNewTimeLine;
        r6 = -1;
        if (r4 == r6) goto L_0x11f9;
    L_0x38ed:
        r0 = r144;
        r4 = r0.availableTimeWidth;
        r0 = r144;
        r6 = r0.widthBeforeNewTimeLine;
        r4 = r4 - r6;
        r0 = r144;
        r6 = r0.timeWidth;
        if (r4 >= r6) goto L_0x11f9;
    L_0x38fc:
        r0 = r144;
        r4 = r0.totalHeight;
        r6 = 1096810496; // 0x41600000 float:14.0 double:5.41896386E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r0 = r144;
        r0.totalHeight = r4;
        goto L_0x11f9;
    L_0x390d:
        r4 = 0;
        goto L_0x1288;
    L_0x3910:
        r69 = move-exception;
        org.telegram.messenger.FileLog.e(r69);
        goto L_0x12b2;
    L_0x3916:
        r0 = r144;
        r4 = r0.descriptionX;	 Catch:{ Exception -> 0x3927 }
        r0 = r85;
        r6 = -r0;
        r4 = java.lang.Math.max(r4, r6);	 Catch:{ Exception -> 0x3927 }
        r0 = r144;
        r0.descriptionX = r4;	 Catch:{ Exception -> 0x3927 }
        goto L_0x1348;
    L_0x3927:
        r69 = move-exception;
        org.telegram.messenger.FileLog.e(r69);
    L_0x392b:
        r0 = r144;
        r4 = r0.totalHeight;
        r6 = 1099431936; // 0x41880000 float:17.0 double:5.431915495E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r0 = r144;
        r0.totalHeight = r4;
        if (r57 == 0) goto L_0x395f;
    L_0x393c:
        r0 = r144;
        r4 = r0.totalHeight;
        r6 = 1096810496; // 0x41600000 float:14.0 double:5.41896386E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r0 = r144;
        r0.totalHeight = r4;
        r4 = 2;
        r0 = r57;
        if (r0 != r4) goto L_0x395f;
    L_0x3950:
        r0 = r144;
        r4 = r0.captionHeight;
        r6 = 1096810496; // 0x41600000 float:14.0 double:5.41896386E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r0 = r144;
        r0.captionHeight = r4;
    L_0x395f:
        r0 = r144;
        r4 = r0.botButtons;
        r4.clear();
        if (r97 == 0) goto L_0x397b;
    L_0x3968:
        r0 = r144;
        r4 = r0.botButtonsByData;
        r4.clear();
        r0 = r144;
        r4 = r0.botButtonsByPosition;
        r4.clear();
        r4 = 0;
        r0 = r144;
        r0.botButtonsLayout = r4;
    L_0x397b:
        r0 = r144;
        r4 = r0.currentPosition;
        if (r4 != 0) goto L_0x3c83;
    L_0x3981:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.reply_markup;
        r4 = r4 instanceof org.telegram.tgnet.TLRPC.TL_replyInlineMarkup;
        if (r4 == 0) goto L_0x3c83;
    L_0x398b:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.reply_markup;
        r4 = r4.rows;
        r119 = r4.size();
        r4 = 1111490560; // 0x42400000 float:48.0 double:5.491493014E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r4 * r119;
        r6 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r0 = r144;
        r0.keyboardHeight = r4;
        r0 = r144;
        r0.substractBackgroundHeight = r4;
        r0 = r144;
        r6 = r0.backgroundWidth;
        r0 = r144;
        r4 = r0.mediaBackground;
        if (r4 == 0) goto L_0x3a6e;
    L_0x39b8:
        r4 = 0;
    L_0x39b9:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r6 - r4;
        r0 = r144;
        r0.widthForButtons = r4;
        r72 = 0;
        r0 = r145;
        r4 = r0.wantedBotKeyboardWidth;
        r0 = r144;
        r6 = r0.widthForButtons;
        if (r4 <= r6) goto L_0x3a0c;
    L_0x39cf:
        r0 = r144;
        r4 = r0.isChat;
        if (r4 == 0) goto L_0x3a72;
    L_0x39d5:
        r4 = r145.needDrawAvatar();
        if (r4 == 0) goto L_0x3a72;
    L_0x39db:
        r4 = r145.isOutOwner();
        if (r4 != 0) goto L_0x3a72;
    L_0x39e1:
        r4 = 1115160576; // 0x42780000 float:62.0 double:5.5096253E-315;
    L_0x39e3:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r0 = -r4;
        r89 = r0;
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 == 0) goto L_0x3a76;
    L_0x39f0:
        r4 = org.telegram.messenger.AndroidUtilities.getMinTabletSide();
        r89 = r89 + r4;
    L_0x39f6:
        r0 = r144;
        r4 = r0.backgroundWidth;
        r0 = r145;
        r6 = r0.wantedBotKeyboardWidth;
        r0 = r89;
        r6 = java.lang.Math.min(r6, r0);
        r4 = java.lang.Math.max(r4, r6);
        r0 = r144;
        r0.widthForButtons = r4;
    L_0x3a0c:
        r90 = 0;
        r103 = new java.util.HashMap;
        r0 = r144;
        r4 = r0.botButtonsByData;
        r0 = r103;
        r0.<init>(r4);
        r0 = r145;
        r4 = r0.botButtonsLayout;
        if (r4 == 0) goto L_0x3a8d;
    L_0x3a1f:
        r0 = r144;
        r4 = r0.botButtonsLayout;
        if (r4 == 0) goto L_0x3a8d;
    L_0x3a25:
        r0 = r144;
        r4 = r0.botButtonsLayout;
        r0 = r145;
        r6 = r0.botButtonsLayout;
        r6 = r6.toString();
        r4 = r4.equals(r6);
        if (r4 == 0) goto L_0x3a8d;
    L_0x3a37:
        r104 = new java.util.HashMap;
        r0 = r144;
        r4 = r0.botButtonsByPosition;
        r0 = r104;
        r0.<init>(r4);
    L_0x3a42:
        r0 = r144;
        r4 = r0.botButtonsByData;
        r4.clear();
        r40 = 0;
    L_0x3a4b:
        r0 = r40;
        r1 = r119;
        if (r0 >= r1) goto L_0x3c21;
    L_0x3a51:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.reply_markup;
        r4 = r4.rows;
        r0 = r40;
        r117 = r4.get(r0);
        r117 = (org.telegram.tgnet.TLRPC.TL_keyboardButtonRow) r117;
        r0 = r117;
        r4 = r0.buttons;
        r55 = r4.size();
        if (r55 != 0) goto L_0x3aa2;
    L_0x3a6b:
        r40 = r40 + 1;
        goto L_0x3a4b;
    L_0x3a6e:
        r4 = 1091567616; // 0x41100000 float:9.0 double:5.39306059E-315;
        goto L_0x39b9;
    L_0x3a72:
        r4 = 1092616192; // 0x41200000 float:10.0 double:5.398241246E-315;
        goto L_0x39e3;
    L_0x3a76:
        r4 = org.telegram.messenger.AndroidUtilities.displaySize;
        r4 = r4.x;
        r6 = org.telegram.messenger.AndroidUtilities.displaySize;
        r6 = r6.y;
        r4 = java.lang.Math.min(r4, r6);
        r6 = 1084227584; // 0x40a00000 float:5.0 double:5.356796015E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r89 = r89 + r4;
        goto L_0x39f6;
    L_0x3a8d:
        r0 = r145;
        r4 = r0.botButtonsLayout;
        if (r4 == 0) goto L_0x3a9f;
    L_0x3a93:
        r0 = r145;
        r4 = r0.botButtonsLayout;
        r4 = r4.toString();
        r0 = r144;
        r0.botButtonsLayout = r4;
    L_0x3a9f:
        r104 = 0;
        goto L_0x3a42;
    L_0x3aa2:
        r0 = r144;
        r4 = r0.widthForButtons;
        r6 = 1084227584; // 0x40a00000 float:5.0 double:5.356796015E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r8 = r55 + -1;
        r6 = r6 * r8;
        r4 = r4 - r6;
        r6 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r54 = r4 / r55;
        r50 = 0;
    L_0x3abb:
        r0 = r117;
        r4 = r0.buttons;
        r4 = r4.size();
        r0 = r50;
        if (r0 >= r4) goto L_0x3a6b;
    L_0x3ac7:
        r53 = new org.telegram.ui.Cells.ChatMessageCell$BotButton;
        r4 = 0;
        r0 = r53;
        r1 = r144;
        r0.<init>();
        r0 = r117;
        r4 = r0.buttons;
        r0 = r50;
        r4 = r4.get(r0);
        r4 = (org.telegram.tgnet.TLRPC.KeyboardButton) r4;
        r0 = r53;
        r0.button = r4;
        r4 = r53.button;
        r4 = r4.data;
        r82 = org.telegram.messenger.Utilities.bytesToHex(r4);
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r0 = r40;
        r4 = r4.append(r0);
        r6 = "";
        r4 = r4.append(r6);
        r0 = r50;
        r4 = r4.append(r0);
        r111 = r4.toString();
        if (r104 == 0) goto L_0x3bde;
    L_0x3b0a:
        r0 = r104;
        r1 = r111;
        r102 = r0.get(r1);
        r102 = (org.telegram.ui.Cells.ChatMessageCell.BotButton) r102;
    L_0x3b14:
        if (r102 == 0) goto L_0x3bea;
    L_0x3b16:
        r4 = r102.progressAlpha;
        r0 = r53;
        r0.progressAlpha = r4;
        r4 = r102.angle;
        r0 = r53;
        r0.angle = r4;
        r8 = r102.lastUpdateTime;
        r0 = r53;
        r0.lastUpdateTime = r8;
    L_0x3b31:
        r0 = r144;
        r4 = r0.botButtonsByData;
        r0 = r82;
        r1 = r53;
        r4.put(r0, r1);
        r0 = r144;
        r4 = r0.botButtonsByPosition;
        r0 = r111;
        r1 = r53;
        r4.put(r0, r1);
        r4 = 1084227584; // 0x40a00000 float:5.0 double:5.356796015E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r4 + r54;
        r4 = r4 * r50;
        r0 = r53;
        r0.x = r4;
        r4 = 1111490560; // 0x42400000 float:48.0 double:5.491493014E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = r4 * r40;
        r6 = 1084227584; // 0x40a00000 float:5.0 double:5.356796015E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 + r6;
        r0 = r53;
        r0.y = r4;
        r53.width = r54;
        r4 = 1110441984; // 0x42300000 float:44.0 double:5.48631236E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r0 = r53;
        r0.height = r4;
        r4 = r53.button;
        r4 = r4 instanceof org.telegram.tgnet.TLRPC.TL_keyboardButtonBuy;
        if (r4 == 0) goto L_0x3bf5;
    L_0x3b80:
        r0 = r145;
        r4 = r0.messageOwner;
        r4 = r4.media;
        r4 = r4.flags;
        r4 = r4 & 4;
        if (r4 == 0) goto L_0x3bf5;
    L_0x3b8c:
        r4 = "PaymentReceipt";
        r6 = 2131494359; // 0x7f0c05d7 float:1.8612224E38 double:1.053098137E-314;
        r31 = org.telegram.messenger.LocaleController.getString(r4, r6);
    L_0x3b96:
        r30 = new android.text.StaticLayout;
        r32 = org.telegram.ui.ActionBar.Theme.chat_botButtonPaint;
        r4 = 1092616192; // 0x41200000 float:10.0 double:5.398241246E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r33 = r54 - r4;
        r34 = android.text.Layout.Alignment.ALIGN_CENTER;
        r35 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r36 = 0;
        r37 = 0;
        r30.<init>(r31, r32, r33, r34, r35, r36, r37);
        r0 = r53;
        r1 = r30;
        r0.title = r1;
        r0 = r144;
        r4 = r0.botButtons;
        r0 = r53;
        r4.add(r0);
        r0 = r117;
        r4 = r0.buttons;
        r4 = r4.size();
        r4 = r4 + -1;
        r0 = r50;
        if (r0 != r4) goto L_0x3bda;
    L_0x3bcb:
        r4 = r53.x;
        r6 = r53.width;
        r4 = r4 + r6;
        r0 = r90;
        r90 = java.lang.Math.max(r0, r4);
    L_0x3bda:
        r50 = r50 + 1;
        goto L_0x3abb;
    L_0x3bde:
        r0 = r103;
        r1 = r82;
        r102 = r0.get(r1);
        r102 = (org.telegram.ui.Cells.ChatMessageCell.BotButton) r102;
        goto L_0x3b14;
    L_0x3bea:
        r8 = java.lang.System.currentTimeMillis();
        r0 = r53;
        r0.lastUpdateTime = r8;
        goto L_0x3b31;
    L_0x3bf5:
        r4 = r53.button;
        r4 = r4.text;
        r6 = org.telegram.ui.ActionBar.Theme.chat_botButtonPaint;
        r6 = r6.getFontMetricsInt();
        r8 = 1097859072; // 0x41700000 float:15.0 double:5.424144515E-315;
        r8 = org.telegram.messenger.AndroidUtilities.dp(r8);
        r9 = 0;
        r31 = org.telegram.messenger.Emoji.replaceEmoji(r4, r6, r8, r9);
        r4 = org.telegram.ui.ActionBar.Theme.chat_botButtonPaint;
        r6 = 1092616192; // 0x41200000 float:10.0 double:5.398241246E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r6 = r54 - r6;
        r6 = (float) r6;
        r8 = android.text.TextUtils.TruncateAt.END;
        r0 = r31;
        r31 = android.text.TextUtils.ellipsize(r0, r4, r6, r8);
        goto L_0x3b96;
    L_0x3c21:
        r0 = r90;
        r1 = r144;
        r1.widthForButtons = r0;
    L_0x3c27:
        r0 = r144;
        r4 = r0.drawPinnedBottom;
        if (r4 == 0) goto L_0x3c8e;
    L_0x3c2d:
        r0 = r144;
        r4 = r0.drawPinnedTop;
        if (r4 == 0) goto L_0x3c8e;
    L_0x3c33:
        r0 = r144;
        r4 = r0.totalHeight;
        r6 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r0 = r144;
        r0.totalHeight = r4;
    L_0x3c42:
        r0 = r145;
        r4 = r0.type;
        r6 = 13;
        if (r4 != r6) goto L_0x3c60;
    L_0x3c4a:
        r0 = r144;
        r4 = r0.totalHeight;
        r6 = 1116471296; // 0x428c0000 float:70.0 double:5.51610112E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        if (r4 >= r6) goto L_0x3c60;
    L_0x3c56:
        r4 = 1116471296; // 0x428c0000 float:70.0 double:5.51610112E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r0 = r144;
        r0.totalHeight = r4;
    L_0x3c60:
        r0 = r144;
        r4 = r0.drawPhotoImage;
        if (r4 != 0) goto L_0x3c70;
    L_0x3c66:
        r0 = r144;
        r6 = r0.photoImage;
        r4 = 0;
        r4 = (android.graphics.drawable.Drawable) r4;
        r6.setImageBitmap(r4);
    L_0x3c70:
        r144.updateWaveform();
        if (r63 == 0) goto L_0x3ccf;
    L_0x3c75:
        r0 = r145;
        r4 = r0.cancelEditing;
        if (r4 != 0) goto L_0x3ccf;
    L_0x3c7b:
        r4 = 1;
    L_0x3c7c:
        r6 = 1;
        r0 = r144;
        r0.updateButtonState(r4, r6);
        return;
    L_0x3c83:
        r4 = 0;
        r0 = r144;
        r0.substractBackgroundHeight = r4;
        r4 = 0;
        r0 = r144;
        r0.keyboardHeight = r4;
        goto L_0x3c27;
    L_0x3c8e:
        r0 = r144;
        r4 = r0.drawPinnedBottom;
        if (r4 == 0) goto L_0x3ca4;
    L_0x3c94:
        r0 = r144;
        r4 = r0.totalHeight;
        r6 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r0 = r144;
        r0.totalHeight = r4;
        goto L_0x3c42;
    L_0x3ca4:
        r0 = r144;
        r4 = r0.drawPinnedTop;
        if (r4 == 0) goto L_0x3c42;
    L_0x3caa:
        r0 = r144;
        r4 = r0.pinnedBottom;
        if (r4 == 0) goto L_0x3c42;
    L_0x3cb0:
        r0 = r144;
        r4 = r0.currentPosition;
        if (r4 == 0) goto L_0x3c42;
    L_0x3cb6:
        r0 = r144;
        r4 = r0.currentPosition;
        r4 = r4.siblingHeights;
        if (r4 != 0) goto L_0x3c42;
    L_0x3cbe:
        r0 = r144;
        r4 = r0.totalHeight;
        r6 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = r4 - r6;
        r0 = r144;
        r0.totalHeight = r4;
        goto L_0x3c42;
    L_0x3ccf:
        r4 = 0;
        goto L_0x3c7c;
    L_0x3cd1:
        r69 = move-exception;
        goto L_0x0d73;
    L_0x3cd4:
        r69 = move-exception;
        r13 = r116;
        goto L_0x0ba8;
    L_0x3cd9:
        r15 = r138;
        goto L_0x18c0;
    L_0x3cdd:
        r15 = r138;
        goto L_0x0de3;
    L_0x3ce1:
        r13 = r116;
        goto L_0x0c60;
    L_0x3ce5:
        r116 = r13;
        r11 = r86;
        goto L_0x0bbd;
    L_0x3ceb:
        r11 = r86;
        goto L_0x0bbd;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Cells.ChatMessageCell.setMessageObject(org.telegram.messenger.MessageObject, org.telegram.messenger.MessageObject$GroupedMessages, boolean, boolean):void");
    }

    private int getAdditionalWidthForPosition(GroupedMessagePosition position) {
        int w = 0;
        if (position == null) {
            return 0;
        }
        if ((position.flags & 2) == 0) {
            w = 0 + AndroidUtilities.dp(4.0f);
        }
        if ((position.flags & 1) == 0) {
            return w + AndroidUtilities.dp(4.0f);
        }
        return w;
    }

    private void createInstantViewButton() {
        if (VERSION.SDK_INT >= 21 && this.drawInstantView) {
            if (this.instantViewSelectorDrawable == null) {
                final Paint maskPaint = new Paint(1);
                maskPaint.setColor(-1);
                Drawable maskDrawable = new Drawable() {
                    RectF rect = new RectF();

                    public void draw(Canvas canvas) {
                        Rect bounds = getBounds();
                        this.rect.set((float) bounds.left, (float) bounds.top, (float) bounds.right, (float) bounds.bottom);
                        canvas.drawRoundRect(this.rect, (float) AndroidUtilities.dp(6.0f), (float) AndroidUtilities.dp(6.0f), maskPaint);
                    }

                    public void setAlpha(int alpha) {
                    }

                    public void setColorFilter(ColorFilter colorFilter) {
                    }

                    public int getOpacity() {
                        return -1;
                    }
                };
                int[][] iArr = new int[][]{StateSet.WILD_CARD};
                int[] iArr2 = new int[1];
                iArr2[0] = Theme.getColor(this.currentMessageObject.isOutOwner() ? Theme.key_chat_outPreviewInstantText : Theme.key_chat_inPreviewInstantText) & 1610612735;
                this.instantViewSelectorDrawable = new RippleDrawable(new ColorStateList(iArr, iArr2), null, maskDrawable);
                this.instantViewSelectorDrawable.setCallback(this);
            } else {
                Theme.setSelectorDrawableColor(this.instantViewSelectorDrawable, Theme.getColor(this.currentMessageObject.isOutOwner() ? Theme.key_chat_outPreviewInstantText : Theme.key_chat_inPreviewInstantText) & 1610612735, true);
            }
            this.instantViewSelectorDrawable.setVisible(true, false);
        }
        if (this.drawInstantView && this.instantViewLayout == null) {
            String str;
            this.instantWidth = AndroidUtilities.dp(33.0f);
            if (this.drawInstantViewType == 1) {
                str = LocaleController.getString("OpenChannel", R.string.OpenChannel);
            } else if (this.drawInstantViewType == 2) {
                str = LocaleController.getString("OpenGroup", R.string.OpenGroup);
            } else if (this.drawInstantViewType == 3) {
                str = LocaleController.getString("OpenMessage", R.string.OpenMessage);
            } else if (this.drawInstantViewType == 5) {
                str = LocaleController.getString("ViewContact", R.string.ViewContact);
            } else {
                str = LocaleController.getString("InstantView", R.string.InstantView);
            }
            int mWidth = this.backgroundWidth - AndroidUtilities.dp(75.0f);
            this.instantViewLayout = new StaticLayout(TextUtils.ellipsize(str, Theme.chat_instantViewPaint, (float) mWidth, TruncateAt.END), Theme.chat_instantViewPaint, mWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            this.instantWidth = this.backgroundWidth - AndroidUtilities.dp(34.0f);
            this.totalHeight += AndroidUtilities.dp(46.0f);
            if (this.currentMessageObject.type == 12) {
                this.totalHeight += AndroidUtilities.dp(14.0f);
            }
            if (this.instantViewLayout != null && this.instantViewLayout.getLineCount() > 0) {
                int dp;
                int ceil = ((int) (((double) this.instantWidth) - Math.ceil((double) this.instantViewLayout.getLineWidth(0)))) / 2;
                if (this.drawInstantViewType == 0) {
                    dp = AndroidUtilities.dp(8.0f);
                } else {
                    dp = 0;
                }
                this.instantTextX = dp + ceil;
                this.instantTextLeftX = (int) this.instantViewLayout.getLineLeft(0);
                this.instantTextX += -this.instantTextLeftX;
            }
        }
    }

    public void requestLayout() {
        if (!this.inLayout) {
            super.requestLayout();
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.currentMessageObject != null && (this.currentMessageObject.checkLayout() || !(this.currentPosition == null || this.lastHeight == AndroidUtilities.displaySize.y))) {
            this.inLayout = true;
            MessageObject messageObject = this.currentMessageObject;
            this.currentMessageObject = null;
            setMessageObject(messageObject, this.currentMessagesGroup, this.pinnedBottom, this.pinnedTop);
            this.inLayout = false;
        }
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), this.totalHeight + this.keyboardHeight);
        this.lastHeight = AndroidUtilities.displaySize.y;
    }

    public void forceResetMessageObject() {
        MessageObject messageObject = this.currentMessageObject;
        this.currentMessageObject = null;
        setMessageObject(messageObject, this.currentMessagesGroup, this.pinnedBottom, this.pinnedTop);
    }

    private int getGroupPhotosWidth() {
        if (AndroidUtilities.isInMultiwindow || !AndroidUtilities.isTablet() || (AndroidUtilities.isSmallTablet() && getResources().getConfiguration().orientation != 2)) {
            return AndroidUtilities.displaySize.x;
        }
        int leftWidth = (AndroidUtilities.displaySize.x / 100) * 35;
        if (leftWidth < AndroidUtilities.dp(320.0f)) {
            leftWidth = AndroidUtilities.dp(320.0f);
        }
        return AndroidUtilities.displaySize.x - leftWidth;
    }

    @SuppressLint({"DrawAllocation"})
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (this.currentMessageObject != null) {
            if (changed || !this.wasLayout) {
                this.layoutWidth = getMeasuredWidth();
                this.layoutHeight = getMeasuredHeight() - this.substractBackgroundHeight;
                if (this.timeTextWidth < 0) {
                    this.timeTextWidth = AndroidUtilities.dp(10.0f);
                }
                this.timeLayout = new StaticLayout(this.currentTimeString, Theme.chat_timePaint, this.timeTextWidth + AndroidUtilities.dp(100.0f), Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                if (this.mediaBackground) {
                    if (this.currentMessageObject.isOutOwner()) {
                        this.timeX = (this.layoutWidth - this.timeWidth) - AndroidUtilities.dp(42.0f);
                    } else {
                        this.timeX = (this.isAvatarVisible ? AndroidUtilities.dp(48.0f) : 0) + ((this.backgroundWidth - AndroidUtilities.dp(4.0f)) - this.timeWidth);
                        if (!(this.currentPosition == null || this.currentPosition.leftSpanOffset == 0)) {
                            this.timeX += (int) Math.ceil((double) ((((float) this.currentPosition.leftSpanOffset) / 1000.0f) * ((float) getGroupPhotosWidth())));
                        }
                    }
                } else if (this.currentMessageObject.isOutOwner()) {
                    this.timeX = (this.layoutWidth - this.timeWidth) - AndroidUtilities.dp(38.5f);
                } else {
                    int dp;
                    int dp2 = (this.backgroundWidth - AndroidUtilities.dp(9.0f)) - this.timeWidth;
                    if (this.isAvatarVisible) {
                        dp = AndroidUtilities.dp(48.0f);
                    } else {
                        dp = 0;
                    }
                    this.timeX = dp + dp2;
                }
                if ((this.currentMessageObject.messageOwner.flags & 1024) != 0) {
                    this.viewsLayout = new StaticLayout(this.currentViewsString, Theme.chat_timePaint, this.viewsTextWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                } else {
                    this.viewsLayout = null;
                }
                if (this.isAvatarVisible) {
                    this.avatarImage.setImageCoords(AndroidUtilities.dp(6.0f), this.avatarImage.getImageY(), AndroidUtilities.dp(42.0f), AndroidUtilities.dp(42.0f));
                }
                this.wasLayout = true;
            }
            if (this.currentMessageObject.type == 0) {
                this.textY = AndroidUtilities.dp(10.0f) + this.namesOffset;
            }
            if (this.currentMessageObject.isRoundVideo()) {
                updatePlayingMessageProgress();
            }
            if (this.documentAttachType == 3) {
                if (this.currentMessageObject.isOutOwner()) {
                    this.seekBarX = (this.layoutWidth - this.backgroundWidth) + AndroidUtilities.dp(57.0f);
                    this.buttonX = (this.layoutWidth - this.backgroundWidth) + AndroidUtilities.dp(14.0f);
                    this.timeAudioX = (this.layoutWidth - this.backgroundWidth) + AndroidUtilities.dp(67.0f);
                } else if (this.isChat && this.currentMessageObject.needDrawAvatar()) {
                    this.seekBarX = AndroidUtilities.dp(114.0f);
                    this.buttonX = AndroidUtilities.dp(71.0f);
                    this.timeAudioX = AndroidUtilities.dp(124.0f);
                } else {
                    this.seekBarX = AndroidUtilities.dp(66.0f);
                    this.buttonX = AndroidUtilities.dp(23.0f);
                    this.timeAudioX = AndroidUtilities.dp(76.0f);
                }
                if (this.hasLinkPreview) {
                    this.seekBarX += AndroidUtilities.dp(10.0f);
                    this.buttonX += AndroidUtilities.dp(10.0f);
                    this.timeAudioX += AndroidUtilities.dp(10.0f);
                }
                this.seekBarWaveform.setSize(this.backgroundWidth - AndroidUtilities.dp((float) ((this.hasLinkPreview ? 10 : 0) + 92)), AndroidUtilities.dp(30.0f));
                this.seekBar.setSize(this.backgroundWidth - AndroidUtilities.dp((float) ((this.hasLinkPreview ? 10 : 0) + 72)), AndroidUtilities.dp(30.0f));
                this.seekBarY = (AndroidUtilities.dp(13.0f) + this.namesOffset) + this.mediaOffsetY;
                this.buttonY = (AndroidUtilities.dp(13.0f) + this.namesOffset) + this.mediaOffsetY;
                this.radialProgress.setProgressRect(this.buttonX, this.buttonY, this.buttonX + AndroidUtilities.dp(44.0f), this.buttonY + AndroidUtilities.dp(44.0f));
                updatePlayingMessageProgress();
            } else if (this.documentAttachType == 5) {
                if (this.currentMessageObject.isOutOwner()) {
                    this.seekBarX = (this.layoutWidth - this.backgroundWidth) + AndroidUtilities.dp(56.0f);
                    this.buttonX = (this.layoutWidth - this.backgroundWidth) + AndroidUtilities.dp(14.0f);
                    this.timeAudioX = (this.layoutWidth - this.backgroundWidth) + AndroidUtilities.dp(67.0f);
                } else if (this.isChat && this.currentMessageObject.needDrawAvatar()) {
                    this.seekBarX = AndroidUtilities.dp(113.0f);
                    this.buttonX = AndroidUtilities.dp(71.0f);
                    this.timeAudioX = AndroidUtilities.dp(124.0f);
                } else {
                    this.seekBarX = AndroidUtilities.dp(65.0f);
                    this.buttonX = AndroidUtilities.dp(23.0f);
                    this.timeAudioX = AndroidUtilities.dp(76.0f);
                }
                if (this.hasLinkPreview) {
                    this.seekBarX += AndroidUtilities.dp(10.0f);
                    this.buttonX += AndroidUtilities.dp(10.0f);
                    this.timeAudioX += AndroidUtilities.dp(10.0f);
                }
                this.seekBar.setSize(this.backgroundWidth - AndroidUtilities.dp((float) ((this.hasLinkPreview ? 10 : 0) + 65)), AndroidUtilities.dp(30.0f));
                this.seekBarY = (AndroidUtilities.dp(29.0f) + this.namesOffset) + this.mediaOffsetY;
                this.buttonY = (AndroidUtilities.dp(13.0f) + this.namesOffset) + this.mediaOffsetY;
                this.radialProgress.setProgressRect(this.buttonX, this.buttonY, this.buttonX + AndroidUtilities.dp(44.0f), this.buttonY + AndroidUtilities.dp(44.0f));
                updatePlayingMessageProgress();
            } else if (this.documentAttachType == 1 && !this.drawPhotoImage) {
                if (this.currentMessageObject.isOutOwner()) {
                    this.buttonX = (this.layoutWidth - this.backgroundWidth) + AndroidUtilities.dp(14.0f);
                } else if (this.isChat && this.currentMessageObject.needDrawAvatar()) {
                    this.buttonX = AndroidUtilities.dp(71.0f);
                } else {
                    this.buttonX = AndroidUtilities.dp(23.0f);
                }
                if (this.hasLinkPreview) {
                    this.buttonX += AndroidUtilities.dp(10.0f);
                }
                this.buttonY = (AndroidUtilities.dp(13.0f) + this.namesOffset) + this.mediaOffsetY;
                this.radialProgress.setProgressRect(this.buttonX, this.buttonY, this.buttonX + AndroidUtilities.dp(44.0f), this.buttonY + AndroidUtilities.dp(44.0f));
                this.photoImage.setImageCoords(this.buttonX - AndroidUtilities.dp(10.0f), this.buttonY - AndroidUtilities.dp(10.0f), this.photoImage.getImageWidth(), this.photoImage.getImageHeight());
            } else if (this.currentMessageObject.type == 12) {
                if (this.currentMessageObject.isOutOwner()) {
                    x = (this.layoutWidth - this.backgroundWidth) + AndroidUtilities.dp(14.0f);
                } else if (this.isChat && this.currentMessageObject.needDrawAvatar()) {
                    x = AndroidUtilities.dp(72.0f);
                } else {
                    x = AndroidUtilities.dp(23.0f);
                }
                this.photoImage.setImageCoords(x, AndroidUtilities.dp(13.0f) + this.namesOffset, AndroidUtilities.dp(44.0f), AndroidUtilities.dp(44.0f));
            } else {
                if (this.currentMessageObject.type == 0 && (this.hasLinkPreview || this.hasGamePreview || this.hasInvoicePreview)) {
                    int linkX;
                    if (this.hasGamePreview) {
                        linkX = this.unmovedTextX - AndroidUtilities.dp(10.0f);
                    } else if (this.hasInvoicePreview) {
                        linkX = this.unmovedTextX + AndroidUtilities.dp(1.0f);
                    } else {
                        linkX = this.unmovedTextX + AndroidUtilities.dp(1.0f);
                    }
                    if (this.isSmallImage) {
                        x = (this.backgroundWidth + linkX) - AndroidUtilities.dp(81.0f);
                    } else {
                        x = linkX + (this.hasInvoicePreview ? -AndroidUtilities.dp(6.3f) : AndroidUtilities.dp(10.0f));
                    }
                } else if (!this.currentMessageObject.isOutOwner()) {
                    if (this.isChat && this.isAvatarVisible) {
                        x = AndroidUtilities.dp(63.0f);
                    } else {
                        x = AndroidUtilities.dp(15.0f);
                    }
                    if (!(this.currentPosition == null || this.currentPosition.edge)) {
                        x -= AndroidUtilities.dp(10.0f);
                    }
                } else if (this.mediaBackground) {
                    x = (this.layoutWidth - this.backgroundWidth) - AndroidUtilities.dp(3.0f);
                } else {
                    x = (this.layoutWidth - this.backgroundWidth) + AndroidUtilities.dp(6.0f);
                }
                if (this.currentPosition != null) {
                    if ((this.currentPosition.flags & 1) == 0) {
                        x -= AndroidUtilities.dp(4.0f);
                    }
                    if (this.currentPosition.leftSpanOffset != 0) {
                        x += (int) Math.ceil((double) ((((float) this.currentPosition.leftSpanOffset) / 1000.0f) * ((float) getGroupPhotosWidth())));
                    }
                }
                this.photoImage.setImageCoords(x, this.photoImage.getImageY(), this.photoImage.getImageWidth(), this.photoImage.getImageHeight());
                this.buttonX = (int) (((float) x) + (((float) (this.photoImage.getImageWidth() - AndroidUtilities.dp(48.0f))) / 2.0f));
                this.buttonY = this.photoImage.getImageY() + ((this.photoImage.getImageHeight() - AndroidUtilities.dp(48.0f)) / 2);
                this.radialProgress.setProgressRect(this.buttonX, this.buttonY, this.buttonX + AndroidUtilities.dp(48.0f), this.buttonY + AndroidUtilities.dp(48.0f));
                this.deleteProgressRect.set((float) (this.buttonX + AndroidUtilities.dp(3.0f)), (float) (this.buttonY + AndroidUtilities.dp(3.0f)), (float) (this.buttonX + AndroidUtilities.dp(45.0f)), (float) (this.buttonY + AndroidUtilities.dp(45.0f)));
            }
        }
    }

    public boolean needDelayRoundProgressDraw() {
        return this.documentAttachType == 7 && this.currentMessageObject.type != 5 && MediaController.getInstance().isPlayingMessage(this.currentMessageObject);
    }

    public void drawRoundProgress(Canvas canvas) {
        this.rect.set(((float) this.photoImage.getImageX()) + AndroidUtilities.dpf2(1.5f), ((float) this.photoImage.getImageY()) + AndroidUtilities.dpf2(1.5f), ((float) this.photoImage.getImageX2()) - AndroidUtilities.dpf2(1.5f), ((float) this.photoImage.getImageY2()) - AndroidUtilities.dpf2(1.5f));
        canvas.drawArc(this.rect, -90.0f, this.currentMessageObject.audioProgress * 360.0f, false, Theme.chat_radialProgressPaint);
    }

    private void drawContent(Canvas canvas) {
        int a;
        int startY;
        int linkX;
        int linkPreviewY;
        int x;
        int y;
        int instantY;
        Paint backPaint;
        float progress;
        int x1;
        int y1;
        RadialProgress radialProgress;
        String str;
        if (this.needNewVisiblePart && this.currentMessageObject.type == 0) {
            getLocalVisibleRect(this.scrollRect);
            setVisiblePart(this.scrollRect.top, this.scrollRect.bottom - this.scrollRect.top);
            this.needNewVisiblePart = false;
        }
        this.forceNotDrawTime = this.currentMessagesGroup != null;
        ImageReceiver imageReceiver = this.photoImage;
        int i = isDrawSelectedBackground() ? this.currentPosition != null ? 2 : 1 : 0;
        imageReceiver.setPressed(i);
        imageReceiver = this.photoImage;
        boolean z = (PhotoViewer.isShowingImage(this.currentMessageObject) || SecretMediaViewer.getInstance().isShowingImage(this.currentMessageObject)) ? false : true;
        imageReceiver.setVisible(z, false);
        if (!this.photoImage.getVisible()) {
            this.mediaWasInvisible = true;
            this.timeWasInvisible = true;
        } else if (this.groupPhotoInvisible) {
            this.timeWasInvisible = true;
        } else if (this.mediaWasInvisible || this.timeWasInvisible) {
            if (this.mediaWasInvisible) {
                this.controlsAlpha = 0.0f;
                this.mediaWasInvisible = false;
            }
            if (this.timeWasInvisible) {
                this.timeAlpha = 0.0f;
                this.timeWasInvisible = false;
            }
            this.lastControlsAlphaChangeTime = System.currentTimeMillis();
            this.totalChangeTime = 0;
        }
        this.radialProgress.setHideCurrentDrawable(false);
        this.radialProgress.setProgressColor(Theme.getColor(Theme.key_chat_mediaProgress));
        boolean imageDrawn = false;
        if (this.currentMessageObject.type == 0) {
            int b;
            if (this.currentMessageObject.isOutOwner()) {
                this.textX = this.currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(11.0f);
            } else {
                int i2 = this.currentBackgroundDrawable.getBounds().left;
                float f = (this.mediaBackground || !this.drawPinnedBottom) ? 17.0f : 11.0f;
                this.textX = AndroidUtilities.dp(f) + i2;
            }
            if (this.hasGamePreview) {
                this.textX += AndroidUtilities.dp(11.0f);
                this.textY = AndroidUtilities.dp(14.0f) + this.namesOffset;
                if (this.siteNameLayout != null) {
                    this.textY += this.siteNameLayout.getLineBottom(this.siteNameLayout.getLineCount() - 1);
                }
            } else if (this.hasInvoicePreview) {
                this.textY = AndroidUtilities.dp(14.0f) + this.namesOffset;
                if (this.siteNameLayout != null) {
                    this.textY += this.siteNameLayout.getLineBottom(this.siteNameLayout.getLineCount() - 1);
                }
            } else {
                this.textY = AndroidUtilities.dp(10.0f) + this.namesOffset;
            }
            this.unmovedTextX = this.textX;
            if (!(this.currentMessageObject.textXOffset == 0.0f || this.replyNameLayout == null)) {
                int diff = (this.backgroundWidth - AndroidUtilities.dp(31.0f)) - this.currentMessageObject.textWidth;
                if (!this.hasNewLineForTime) {
                    diff -= AndroidUtilities.dp((float) ((this.currentMessageObject.isOutOwner() ? 20 : 0) + 4)) + this.timeWidth;
                }
                if (diff > 0) {
                    this.textX += diff;
                }
            }
            if (!(this.currentMessageObject.textLayoutBlocks == null || this.currentMessageObject.textLayoutBlocks.isEmpty())) {
                if (this.fullyDraw) {
                    this.firstVisibleBlockNum = 0;
                    this.lastVisibleBlockNum = this.currentMessageObject.textLayoutBlocks.size();
                }
                if (this.firstVisibleBlockNum >= 0) {
                    a = this.firstVisibleBlockNum;
                    while (a <= this.lastVisibleBlockNum && a < this.currentMessageObject.textLayoutBlocks.size()) {
                        TextLayoutBlock block = (TextLayoutBlock) this.currentMessageObject.textLayoutBlocks.get(a);
                        canvas.save();
                        canvas.translate((float) (this.textX - (block.isRtl() ? (int) Math.ceil((double) this.currentMessageObject.textXOffset) : 0)), ((float) this.textY) + block.textYOffset);
                        if (this.pressedLink != null && a == this.linkBlockNum) {
                            for (b = 0; b < this.urlPath.size(); b++) {
                                canvas.drawPath((Path) this.urlPath.get(b), Theme.chat_urlPaint);
                            }
                        }
                        if (a == this.linkSelectionBlockNum && !this.urlPathSelection.isEmpty()) {
                            for (b = 0; b < this.urlPathSelection.size(); b++) {
                                canvas.drawPath((Path) this.urlPathSelection.get(b), Theme.chat_textSearchSelectionPaint);
                            }
                        }
                        try {
                            block.textLayout.draw(canvas);
                        } catch (Throwable e) {
                            FileLog.e(e);
                        }
                        canvas.restore();
                        a++;
                    }
                }
            }
            if (this.hasLinkPreview || this.hasGamePreview || this.hasInvoicePreview) {
                int size;
                if (this.hasGamePreview) {
                    startY = AndroidUtilities.dp(14.0f) + this.namesOffset;
                    linkX = this.unmovedTextX - AndroidUtilities.dp(10.0f);
                } else if (this.hasInvoicePreview) {
                    startY = AndroidUtilities.dp(14.0f) + this.namesOffset;
                    linkX = this.unmovedTextX + AndroidUtilities.dp(1.0f);
                } else {
                    startY = (this.textY + this.currentMessageObject.textHeight) + AndroidUtilities.dp(8.0f);
                    linkX = this.unmovedTextX + AndroidUtilities.dp(1.0f);
                }
                linkPreviewY = startY;
                int smallImageStartY = 0;
                if (!this.hasInvoicePreview) {
                    Theme.chat_replyLinePaint.setColor(Theme.getColor(this.currentMessageObject.isOutOwner() ? Theme.key_chat_outPreviewLine : Theme.key_chat_inPreviewLine));
                    canvas.drawRect((float) linkX, (float) (linkPreviewY - AndroidUtilities.dp(3.0f)), (float) (AndroidUtilities.dp(2.0f) + linkX), (float) ((this.linkPreviewHeight + linkPreviewY) + AndroidUtilities.dp(3.0f)), Theme.chat_replyLinePaint);
                }
                if (this.siteNameLayout != null) {
                    Theme.chat_replyNamePaint.setColor(Theme.getColor(this.currentMessageObject.isOutOwner() ? Theme.key_chat_outSiteNameText : Theme.key_chat_inSiteNameText));
                    canvas.save();
                    if (this.siteNameRtl) {
                        x = (this.backgroundWidth - this.siteNameWidth) - AndroidUtilities.dp(32.0f);
                    } else if (this.hasInvoicePreview) {
                        x = 0;
                    } else {
                        x = AndroidUtilities.dp(10.0f);
                    }
                    canvas.translate((float) (linkX + x), (float) (linkPreviewY - AndroidUtilities.dp(3.0f)));
                    this.siteNameLayout.draw(canvas);
                    canvas.restore();
                    linkPreviewY += this.siteNameLayout.getLineBottom(this.siteNameLayout.getLineCount() - 1);
                }
                if ((this.hasGamePreview || this.hasInvoicePreview) && this.currentMessageObject.textHeight != 0) {
                    startY += this.currentMessageObject.textHeight + AndroidUtilities.dp(4.0f);
                    linkPreviewY += this.currentMessageObject.textHeight + AndroidUtilities.dp(4.0f);
                }
                if (this.drawPhotoImage && this.drawInstantView) {
                    if (linkPreviewY != startY) {
                        linkPreviewY += AndroidUtilities.dp(2.0f);
                    }
                    this.photoImage.setImageCoords(AndroidUtilities.dp(10.0f) + linkX, linkPreviewY, this.photoImage.getImageWidth(), this.photoImage.getImageHeight());
                    if (this.drawImageButton) {
                        size = AndroidUtilities.dp(48.0f);
                        this.buttonX = (int) (((float) this.photoImage.getImageX()) + (((float) (this.photoImage.getImageWidth() - size)) / 2.0f));
                        this.buttonY = (int) (((float) this.photoImage.getImageY()) + (((float) (this.photoImage.getImageHeight() - size)) / 2.0f));
                        this.radialProgress.setProgressRect(this.buttonX, this.buttonY, this.buttonX + size, this.buttonY + size);
                    }
                    imageDrawn = this.photoImage.draw(canvas);
                    linkPreviewY += this.photoImage.getImageHeight() + AndroidUtilities.dp(6.0f);
                }
                if (this.currentMessageObject.isOutOwner()) {
                    Theme.chat_replyNamePaint.setColor(Theme.getColor(Theme.key_chat_messageTextOut));
                    Theme.chat_replyTextPaint.setColor(Theme.getColor(Theme.key_chat_messageTextOut));
                } else {
                    Theme.chat_replyNamePaint.setColor(Theme.getColor(Theme.key_chat_messageTextIn));
                    Theme.chat_replyTextPaint.setColor(Theme.getColor(Theme.key_chat_messageTextIn));
                }
                if (this.titleLayout != null) {
                    if (linkPreviewY != startY) {
                        linkPreviewY += AndroidUtilities.dp(2.0f);
                    }
                    smallImageStartY = linkPreviewY - AndroidUtilities.dp(1.0f);
                    canvas.save();
                    canvas.translate((float) ((AndroidUtilities.dp(10.0f) + linkX) + this.titleX), (float) (linkPreviewY - AndroidUtilities.dp(3.0f)));
                    this.titleLayout.draw(canvas);
                    canvas.restore();
                    linkPreviewY += this.titleLayout.getLineBottom(this.titleLayout.getLineCount() - 1);
                }
                if (this.authorLayout != null) {
                    if (linkPreviewY != startY) {
                        linkPreviewY += AndroidUtilities.dp(2.0f);
                    }
                    if (smallImageStartY == 0) {
                        smallImageStartY = linkPreviewY - AndroidUtilities.dp(1.0f);
                    }
                    canvas.save();
                    canvas.translate((float) ((AndroidUtilities.dp(10.0f) + linkX) + this.authorX), (float) (linkPreviewY - AndroidUtilities.dp(3.0f)));
                    this.authorLayout.draw(canvas);
                    canvas.restore();
                    linkPreviewY += this.authorLayout.getLineBottom(this.authorLayout.getLineCount() - 1);
                }
                if (this.descriptionLayout != null) {
                    if (linkPreviewY != startY) {
                        linkPreviewY += AndroidUtilities.dp(2.0f);
                    }
                    if (smallImageStartY == 0) {
                        smallImageStartY = linkPreviewY - AndroidUtilities.dp(1.0f);
                    }
                    this.descriptionY = linkPreviewY - AndroidUtilities.dp(3.0f);
                    canvas.save();
                    canvas.translate((float) (((this.hasInvoicePreview ? 0 : AndroidUtilities.dp(10.0f)) + linkX) + this.descriptionX), (float) this.descriptionY);
                    if (this.pressedLink != null && this.linkBlockNum == -10) {
                        for (b = 0; b < this.urlPath.size(); b++) {
                            canvas.drawPath((Path) this.urlPath.get(b), Theme.chat_urlPaint);
                        }
                    }
                    this.descriptionLayout.draw(canvas);
                    canvas.restore();
                    linkPreviewY += this.descriptionLayout.getLineBottom(this.descriptionLayout.getLineCount() - 1);
                }
                if (this.drawPhotoImage && !this.drawInstantView) {
                    if (linkPreviewY != startY) {
                        linkPreviewY += AndroidUtilities.dp(2.0f);
                    }
                    if (this.isSmallImage) {
                        this.photoImage.setImageCoords((this.backgroundWidth + linkX) - AndroidUtilities.dp(81.0f), smallImageStartY, this.photoImage.getImageWidth(), this.photoImage.getImageHeight());
                    } else {
                        imageReceiver = this.photoImage;
                        if (this.hasInvoicePreview) {
                            i = -AndroidUtilities.dp(6.3f);
                        } else {
                            i = AndroidUtilities.dp(10.0f);
                        }
                        imageReceiver.setImageCoords(i + linkX, linkPreviewY, this.photoImage.getImageWidth(), this.photoImage.getImageHeight());
                        if (this.drawImageButton) {
                            size = AndroidUtilities.dp(48.0f);
                            this.buttonX = (int) (((float) this.photoImage.getImageX()) + (((float) (this.photoImage.getImageWidth() - size)) / 2.0f));
                            this.buttonY = (int) (((float) this.photoImage.getImageY()) + (((float) (this.photoImage.getImageHeight() - size)) / 2.0f));
                            this.radialProgress.setProgressRect(this.buttonX, this.buttonY, this.buttonX + size, this.buttonY + size);
                        }
                    }
                    if (this.currentMessageObject.isRoundVideo() && MediaController.getInstance().isPlayingMessage(this.currentMessageObject) && MediaController.getInstance().isRoundVideoDrawingReady()) {
                        imageDrawn = true;
                        this.drawTime = true;
                    } else {
                        imageDrawn = this.photoImage.draw(canvas);
                    }
                }
                if (this.photosCountLayout != null && this.photoImage.getVisible()) {
                    x = ((this.photoImage.getImageX() + this.photoImage.getImageWidth()) - AndroidUtilities.dp(8.0f)) - this.photosCountWidth;
                    y = (this.photoImage.getImageY() + this.photoImage.getImageHeight()) - AndroidUtilities.dp(19.0f);
                    this.rect.set((float) (x - AndroidUtilities.dp(4.0f)), (float) (y - AndroidUtilities.dp(1.5f)), (float) ((this.photosCountWidth + x) + AndroidUtilities.dp(4.0f)), (float) (AndroidUtilities.dp(14.5f) + y));
                    int oldAlpha = Theme.chat_timeBackgroundPaint.getAlpha();
                    Theme.chat_timeBackgroundPaint.setAlpha((int) (((float) oldAlpha) * this.controlsAlpha));
                    Theme.chat_durationPaint.setAlpha((int) (255.0f * this.controlsAlpha));
                    canvas.drawRoundRect(this.rect, (float) AndroidUtilities.dp(4.0f), (float) AndroidUtilities.dp(4.0f), Theme.chat_timeBackgroundPaint);
                    Theme.chat_timeBackgroundPaint.setAlpha(oldAlpha);
                    canvas.save();
                    canvas.translate((float) x, (float) y);
                    this.photosCountLayout.draw(canvas);
                    canvas.restore();
                    Theme.chat_durationPaint.setAlpha(255);
                }
                if (this.videoInfoLayout != null && (!this.drawPhotoImage || this.photoImage.getVisible())) {
                    if (!this.hasGamePreview && !this.hasInvoicePreview) {
                        x = ((this.photoImage.getImageX() + this.photoImage.getImageWidth()) - AndroidUtilities.dp(8.0f)) - this.durationWidth;
                        y = (this.photoImage.getImageY() + this.photoImage.getImageHeight()) - AndroidUtilities.dp(19.0f);
                        this.rect.set((float) (x - AndroidUtilities.dp(4.0f)), (float) (y - AndroidUtilities.dp(1.5f)), (float) ((this.durationWidth + x) + AndroidUtilities.dp(4.0f)), (float) (AndroidUtilities.dp(14.5f) + y));
                        canvas.drawRoundRect(this.rect, (float) AndroidUtilities.dp(4.0f), (float) AndroidUtilities.dp(4.0f), Theme.chat_timeBackgroundPaint);
                    } else if (this.drawPhotoImage) {
                        x = this.photoImage.getImageX() + AndroidUtilities.dp(8.5f);
                        y = this.photoImage.getImageY() + AndroidUtilities.dp(6.0f);
                        this.rect.set((float) (x - AndroidUtilities.dp(4.0f)), (float) (y - AndroidUtilities.dp(1.5f)), (float) ((this.durationWidth + x) + AndroidUtilities.dp(4.0f)), (float) (AndroidUtilities.dp(16.5f) + y));
                        canvas.drawRoundRect(this.rect, (float) AndroidUtilities.dp(4.0f), (float) AndroidUtilities.dp(4.0f), Theme.chat_timeBackgroundPaint);
                    } else {
                        x = linkX;
                        y = linkPreviewY;
                    }
                    canvas.save();
                    canvas.translate((float) x, (float) y);
                    if (this.hasInvoicePreview) {
                        if (this.drawPhotoImage) {
                            Theme.chat_shipmentPaint.setColor(Theme.getColor(Theme.key_chat_previewGameText));
                        } else if (this.currentMessageObject.isOutOwner()) {
                            Theme.chat_shipmentPaint.setColor(Theme.getColor(Theme.key_chat_messageTextOut));
                        } else {
                            Theme.chat_shipmentPaint.setColor(Theme.getColor(Theme.key_chat_messageTextIn));
                        }
                    }
                    this.videoInfoLayout.draw(canvas);
                    canvas.restore();
                }
                if (this.drawInstantView) {
                    Drawable instantDrawable;
                    instantY = (this.linkPreviewHeight + startY) + AndroidUtilities.dp(10.0f);
                    backPaint = Theme.chat_instantViewRectPaint;
                    if (this.currentMessageObject.isOutOwner()) {
                        instantDrawable = Theme.chat_msgOutInstantDrawable;
                        Theme.chat_instantViewPaint.setColor(Theme.getColor(Theme.key_chat_outPreviewInstantText));
                        backPaint.setColor(Theme.getColor(Theme.key_chat_outPreviewInstantText));
                    } else {
                        instantDrawable = Theme.chat_msgInInstantDrawable;
                        Theme.chat_instantViewPaint.setColor(Theme.getColor(Theme.key_chat_inPreviewInstantText));
                        backPaint.setColor(Theme.getColor(Theme.key_chat_inPreviewInstantText));
                    }
                    if (VERSION.SDK_INT >= 21) {
                        this.instantViewSelectorDrawable.setBounds(linkX, instantY, this.instantWidth + linkX, AndroidUtilities.dp(36.0f) + instantY);
                        this.instantViewSelectorDrawable.draw(canvas);
                    }
                    this.rect.set((float) linkX, (float) instantY, (float) (this.instantWidth + linkX), (float) (AndroidUtilities.dp(36.0f) + instantY));
                    canvas.drawRoundRect(this.rect, (float) AndroidUtilities.dp(6.0f), (float) AndroidUtilities.dp(6.0f), backPaint);
                    if (this.drawInstantViewType == 0) {
                        BaseCell.setDrawableBounds(instantDrawable, ((this.instantTextLeftX + this.instantTextX) + linkX) - AndroidUtilities.dp(15.0f), AndroidUtilities.dp(11.5f) + instantY, AndroidUtilities.dp(9.0f), AndroidUtilities.dp(13.0f));
                        instantDrawable.draw(canvas);
                    }
                    if (this.instantViewLayout != null) {
                        canvas.save();
                        canvas.translate((float) (this.instantTextX + linkX), (float) (AndroidUtilities.dp(10.5f) + instantY));
                        this.instantViewLayout.draw(canvas);
                        canvas.restore();
                    }
                }
            }
            this.drawTime = true;
        } else if (this.drawPhotoImage) {
            if (this.currentMessageObject.isRoundVideo() && MediaController.getInstance().isPlayingMessage(this.currentMessageObject) && MediaController.getInstance().isRoundVideoDrawingReady()) {
                imageDrawn = true;
                this.drawTime = true;
            } else {
                if (this.currentMessageObject.type == 5 && Theme.chat_roundVideoShadow != null) {
                    x = this.photoImage.getImageX() - AndroidUtilities.dp(3.0f);
                    y = this.photoImage.getImageY() - AndroidUtilities.dp(2.0f);
                    Theme.chat_roundVideoShadow.setAlpha((int) (this.photoImage.getCurrentAlpha() * 255.0f));
                    Theme.chat_roundVideoShadow.setBounds(x, y, (AndroidUtilities.roundMessageSize + x) + AndroidUtilities.dp(6.0f), (AndroidUtilities.roundMessageSize + y) + AndroidUtilities.dp(6.0f));
                    Theme.chat_roundVideoShadow.draw(canvas);
                }
                imageDrawn = this.photoImage.draw(canvas);
                boolean drawTimeOld = this.drawTime;
                this.drawTime = this.photoImage.getVisible();
                if (!(this.currentPosition == null || drawTimeOld == this.drawTime)) {
                    ViewGroup viewGroup = (ViewGroup) getParent();
                    if (viewGroup != null) {
                        if (this.currentPosition.last) {
                            viewGroup.invalidate();
                        } else {
                            int count = viewGroup.getChildCount();
                            for (a = 0; a < count; a++) {
                                View child = viewGroup.getChildAt(a);
                                if (child != this && (child instanceof ChatMessageCell)) {
                                    ChatMessageCell cell = (ChatMessageCell) child;
                                    if (cell.getCurrentMessagesGroup() == this.currentMessagesGroup) {
                                        GroupedMessagePosition position = cell.getCurrentPosition();
                                        if (position.last && position.maxY == this.currentPosition.maxY && (cell.timeX - AndroidUtilities.dp(4.0f)) + cell.getLeft() < getRight()) {
                                            cell.groupPhotoInvisible = !this.drawTime;
                                            cell.invalidate();
                                            viewGroup.invalidate();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (this.buttonState == -1 && this.currentMessageObject.needDrawBluredPreview() && !MediaController.getInstance().isPlayingMessage(this.currentMessageObject) && this.photoImage.getVisible()) {
            int drawable = 4;
            if (this.currentMessageObject.messageOwner.destroyTime != 0) {
                if (this.currentMessageObject.isOutOwner()) {
                    drawable = 6;
                } else {
                    drawable = 5;
                }
            }
            BaseCell.setDrawableBounds(Theme.chat_photoStatesDrawables[drawable][this.buttonPressed], this.buttonX, this.buttonY);
            Theme.chat_photoStatesDrawables[drawable][this.buttonPressed].setAlpha((int) ((255.0f * (1.0f - this.radialProgress.getAlpha())) * this.controlsAlpha));
            Theme.chat_photoStatesDrawables[drawable][this.buttonPressed].draw(canvas);
            if (this.currentMessageObject.messageOwner.destroyTime != 0) {
                if (!this.currentMessageObject.isOutOwner()) {
                    progress = ((float) Math.max(0, (((long) this.currentMessageObject.messageOwner.destroyTime) * 1000) - (System.currentTimeMillis() + ((long) (ConnectionsManager.getInstance(this.currentAccount).getTimeDifference() * 1000))))) / (((float) this.currentMessageObject.messageOwner.ttl) * 1000.0f);
                    Theme.chat_deleteProgressPaint.setAlpha((int) (255.0f * this.controlsAlpha));
                    canvas.drawArc(this.deleteProgressRect, -90.0f, -360.0f * progress, true, Theme.chat_deleteProgressPaint);
                    if (progress != 0.0f) {
                        int offset = AndroidUtilities.dp(2.0f);
                        invalidate(((int) this.deleteProgressRect.left) - offset, ((int) this.deleteProgressRect.top) - offset, ((int) this.deleteProgressRect.right) + (offset * 2), ((int) this.deleteProgressRect.bottom) + (offset * 2));
                    }
                }
                updateSecretTimeText(this.currentMessageObject);
            }
        }
        if (this.documentAttachType == 2 || this.currentMessageObject.type == 8) {
            if (!(!this.photoImage.getVisible() || this.hasGamePreview || this.currentMessageObject.needDrawBluredPreview())) {
                oldAlpha = ((BitmapDrawable) Theme.chat_msgMediaMenuDrawable).getPaint().getAlpha();
                Theme.chat_msgMediaMenuDrawable.setAlpha((int) (((float) oldAlpha) * this.controlsAlpha));
                Drawable drawable2 = Theme.chat_msgMediaMenuDrawable;
                i2 = (this.photoImage.getImageX() + this.photoImage.getImageWidth()) - AndroidUtilities.dp(14.0f);
                this.otherX = i2;
                int imageY = this.photoImage.getImageY() + AndroidUtilities.dp(8.1f);
                this.otherY = imageY;
                BaseCell.setDrawableBounds(drawable2, i2, imageY);
                Theme.chat_msgMediaMenuDrawable.draw(canvas);
                Theme.chat_msgMediaMenuDrawable.setAlpha(oldAlpha);
            }
        } else if (this.documentAttachType == 7 || this.currentMessageObject.type == 5) {
            if (this.durationLayout != null) {
                boolean playing = MediaController.getInstance().isPlayingMessage(this.currentMessageObject);
                if (playing && this.currentMessageObject.type == 5) {
                    drawRoundProgress(canvas);
                }
                if (this.documentAttachType == 7) {
                    i2 = this.backgroundDrawableLeft;
                    f = (this.currentMessageObject.isOutOwner() || this.drawPinnedBottom) ? 12.0f : 18.0f;
                    x1 = i2 + AndroidUtilities.dp(f);
                    i2 = this.layoutHeight;
                    if (this.drawPinnedBottom) {
                        i = 2;
                    } else {
                        i = 0;
                    }
                    y1 = (i2 - AndroidUtilities.dp(6.3f - ((float) i))) - this.timeLayout.getHeight();
                } else {
                    x1 = this.backgroundDrawableLeft + AndroidUtilities.dp(8.0f);
                    y1 = this.layoutHeight - AndroidUtilities.dp((float) (28 - (this.drawPinnedBottom ? 2 : 0)));
                    this.rect.set((float) x1, (float) y1, (float) ((this.timeWidthAudio + x1) + AndroidUtilities.dp(22.0f)), (float) (AndroidUtilities.dp(17.0f) + y1));
                    canvas.drawRoundRect(this.rect, (float) AndroidUtilities.dp(4.0f), (float) AndroidUtilities.dp(4.0f), Theme.chat_actionBackgroundPaint);
                    if (playing || !this.currentMessageObject.isContentUnread()) {
                        if (!playing || MediaController.getInstance().isMessagePaused()) {
                            this.roundVideoPlayingDrawable.stop();
                        } else {
                            this.roundVideoPlayingDrawable.start();
                        }
                        BaseCell.setDrawableBounds(this.roundVideoPlayingDrawable, (this.timeWidthAudio + x1) + AndroidUtilities.dp(6.0f), AndroidUtilities.dp(2.3f) + y1);
                        this.roundVideoPlayingDrawable.draw(canvas);
                    } else {
                        Theme.chat_docBackPaint.setColor(Theme.getColor(Theme.key_chat_mediaTimeText));
                        canvas.drawCircle((float) ((this.timeWidthAudio + x1) + AndroidUtilities.dp(12.0f)), (float) (AndroidUtilities.dp(8.3f) + y1), (float) AndroidUtilities.dp(3.0f), Theme.chat_docBackPaint);
                    }
                    x1 += AndroidUtilities.dp(4.0f);
                    y1 += AndroidUtilities.dp(1.7f);
                }
                canvas.save();
                canvas.translate((float) x1, (float) y1);
                this.durationLayout.draw(canvas);
                canvas.restore();
            }
        } else if (this.documentAttachType == 5) {
            if (this.currentMessageObject.isOutOwner()) {
                Theme.chat_audioTitlePaint.setColor(Theme.getColor(Theme.key_chat_outAudioTitleText));
                Theme.chat_audioPerformerPaint.setColor(Theme.getColor(isDrawSelectedBackground() ? Theme.key_chat_outAudioPerfomerSelectedText : Theme.key_chat_outAudioPerfomerText));
                Theme.chat_audioTimePaint.setColor(Theme.getColor(isDrawSelectedBackground() ? Theme.key_chat_outAudioDurationSelectedText : Theme.key_chat_outAudioDurationText));
                radialProgress = this.radialProgress;
                str = (isDrawSelectedBackground() || this.buttonPressed != 0) ? Theme.key_chat_outAudioSelectedProgress : Theme.key_chat_outAudioProgress;
                radialProgress.setProgressColor(Theme.getColor(str));
            } else {
                Theme.chat_audioTitlePaint.setColor(Theme.getColor(Theme.key_chat_inAudioTitleText));
                Theme.chat_audioPerformerPaint.setColor(Theme.getColor(isDrawSelectedBackground() ? Theme.key_chat_inAudioPerfomerSelectedText : Theme.key_chat_inAudioPerfomerText));
                Theme.chat_audioTimePaint.setColor(Theme.getColor(isDrawSelectedBackground() ? Theme.key_chat_inAudioDurationSelectedText : Theme.key_chat_inAudioDurationText));
                radialProgress = this.radialProgress;
                str = (isDrawSelectedBackground() || this.buttonPressed != 0) ? Theme.key_chat_inAudioSelectedProgress : Theme.key_chat_inAudioProgress;
                radialProgress.setProgressColor(Theme.getColor(str));
            }
            this.radialProgress.draw(canvas);
            canvas.save();
            canvas.translate((float) (this.timeAudioX + this.songX), (float) ((AndroidUtilities.dp(13.0f) + this.namesOffset) + this.mediaOffsetY));
            this.songLayout.draw(canvas);
            canvas.restore();
            canvas.save();
            if (MediaController.getInstance().isPlayingMessage(this.currentMessageObject)) {
                canvas.translate((float) this.seekBarX, (float) this.seekBarY);
                this.seekBar.draw(canvas);
            } else {
                canvas.translate((float) (this.timeAudioX + this.performerX), (float) ((AndroidUtilities.dp(35.0f) + this.namesOffset) + this.mediaOffsetY));
                this.performerLayout.draw(canvas);
            }
            canvas.restore();
            canvas.save();
            canvas.translate((float) this.timeAudioX, (float) ((AndroidUtilities.dp(57.0f) + this.namesOffset) + this.mediaOffsetY));
            this.durationLayout.draw(canvas);
            canvas.restore();
            Drawable menuDrawable = this.currentMessageObject.isOutOwner() ? isDrawSelectedBackground() ? Theme.chat_msgOutMenuSelectedDrawable : Theme.chat_msgOutMenuDrawable : isDrawSelectedBackground() ? Theme.chat_msgInMenuSelectedDrawable : Theme.chat_msgInMenuDrawable;
            i = (this.backgroundWidth + this.buttonX) - AndroidUtilities.dp(this.currentMessageObject.type == 0 ? 58.0f : 48.0f);
            this.otherX = i;
            i2 = this.buttonY - AndroidUtilities.dp(5.0f);
            this.otherY = i2;
            BaseCell.setDrawableBounds(menuDrawable, i, i2);
            menuDrawable.draw(canvas);
        } else if (this.documentAttachType == 3) {
            if (this.currentMessageObject.isOutOwner()) {
                Theme.chat_audioTimePaint.setColor(Theme.getColor(isDrawSelectedBackground() ? Theme.key_chat_outAudioDurationSelectedText : Theme.key_chat_outAudioDurationText));
                radialProgress = this.radialProgress;
                str = (isDrawSelectedBackground() || this.buttonPressed != 0) ? Theme.key_chat_outAudioSelectedProgress : Theme.key_chat_outAudioProgress;
                radialProgress.setProgressColor(Theme.getColor(str));
            } else {
                Theme.chat_audioTimePaint.setColor(Theme.getColor(isDrawSelectedBackground() ? Theme.key_chat_inAudioDurationSelectedText : Theme.key_chat_inAudioDurationText));
                radialProgress = this.radialProgress;
                str = (isDrawSelectedBackground() || this.buttonPressed != 0) ? Theme.key_chat_inAudioSelectedProgress : Theme.key_chat_inAudioProgress;
                radialProgress.setProgressColor(Theme.getColor(str));
            }
            this.radialProgress.draw(canvas);
            canvas.save();
            if (this.useSeekBarWaweform) {
                canvas.translate((float) (this.seekBarX + AndroidUtilities.dp(13.0f)), (float) this.seekBarY);
                this.seekBarWaveform.draw(canvas);
            } else {
                canvas.translate((float) this.seekBarX, (float) this.seekBarY);
                this.seekBar.draw(canvas);
            }
            canvas.restore();
            canvas.save();
            canvas.translate((float) this.timeAudioX, (float) ((AndroidUtilities.dp(44.0f) + this.namesOffset) + this.mediaOffsetY));
            this.durationLayout.draw(canvas);
            canvas.restore();
            if (this.currentMessageObject.type != 0 && this.currentMessageObject.isContentUnread()) {
                Theme.chat_docBackPaint.setColor(Theme.getColor(this.currentMessageObject.isOutOwner() ? Theme.key_chat_outVoiceSeekbarFill : Theme.key_chat_inVoiceSeekbarFill));
                canvas.drawCircle((float) ((this.timeAudioX + this.timeWidthAudio) + AndroidUtilities.dp(6.0f)), (float) ((AndroidUtilities.dp(51.0f) + this.namesOffset) + this.mediaOffsetY), (float) AndroidUtilities.dp(3.0f), Theme.chat_docBackPaint);
            }
        }
        if (this.currentMessageObject.type == 1 || this.documentAttachType == 4) {
            if (this.photoImage.getVisible()) {
                if (!this.currentMessageObject.needDrawBluredPreview() && this.documentAttachType == 4) {
                    oldAlpha = ((BitmapDrawable) Theme.chat_msgMediaMenuDrawable).getPaint().getAlpha();
                    Theme.chat_msgMediaMenuDrawable.setAlpha((int) (((float) oldAlpha) * this.controlsAlpha));
                    drawable2 = Theme.chat_msgMediaMenuDrawable;
                    i2 = (this.photoImage.getImageX() + this.photoImage.getImageWidth()) - AndroidUtilities.dp(14.0f);
                    this.otherX = i2;
                    imageY = this.photoImage.getImageY() + AndroidUtilities.dp(8.1f);
                    this.otherY = imageY;
                    BaseCell.setDrawableBounds(drawable2, i2, imageY);
                    Theme.chat_msgMediaMenuDrawable.draw(canvas);
                    Theme.chat_msgMediaMenuDrawable.setAlpha(oldAlpha);
                }
                if (!(this.forceNotDrawTime || this.infoLayout == null || (this.buttonState != 1 && this.buttonState != 0 && this.buttonState != 3 && !this.currentMessageObject.needDrawBluredPreview()))) {
                    Theme.chat_infoPaint.setColor(Theme.getColor(Theme.key_chat_mediaInfoText));
                    x1 = this.photoImage.getImageX() + AndroidUtilities.dp(4.0f);
                    y1 = this.photoImage.getImageY() + AndroidUtilities.dp(4.0f);
                    this.rect.set((float) x1, (float) y1, (float) ((this.infoWidth + x1) + AndroidUtilities.dp(8.0f)), (float) (AndroidUtilities.dp(16.5f) + y1));
                    oldAlpha = Theme.chat_timeBackgroundPaint.getAlpha();
                    Theme.chat_timeBackgroundPaint.setAlpha((int) (((float) oldAlpha) * this.controlsAlpha));
                    canvas.drawRoundRect(this.rect, (float) AndroidUtilities.dp(4.0f), (float) AndroidUtilities.dp(4.0f), Theme.chat_timeBackgroundPaint);
                    Theme.chat_timeBackgroundPaint.setAlpha(oldAlpha);
                    canvas.save();
                    canvas.translate((float) (this.photoImage.getImageX() + AndroidUtilities.dp(8.0f)), (float) (this.photoImage.getImageY() + AndroidUtilities.dp(5.5f)));
                    Theme.chat_infoPaint.setAlpha((int) (255.0f * this.controlsAlpha));
                    this.infoLayout.draw(canvas);
                    canvas.restore();
                    Theme.chat_infoPaint.setAlpha(255);
                }
            }
        } else if (this.currentMessageObject.type == 4) {
            if (this.docTitleLayout != null) {
                if (this.currentMessageObject.isOutOwner()) {
                    Theme.chat_locationTitlePaint.setColor(Theme.getColor(Theme.key_chat_messageTextOut));
                    Theme.chat_locationAddressPaint.setColor(Theme.getColor(isDrawSelectedBackground() ? Theme.key_chat_outVenueInfoSelectedText : Theme.key_chat_outVenueInfoText));
                } else {
                    Theme.chat_locationTitlePaint.setColor(Theme.getColor(Theme.key_chat_messageTextIn));
                    Theme.chat_locationAddressPaint.setColor(Theme.getColor(isDrawSelectedBackground() ? Theme.key_chat_inVenueInfoSelectedText : Theme.key_chat_inVenueInfoText));
                }
                if (this.currentMessageObject.messageOwner.media instanceof TL_messageMediaGeoLive) {
                    int cy = this.photoImage.getImageY2() + AndroidUtilities.dp(30.0f);
                    if (!this.locationExpired) {
                        this.forceNotDrawTime = true;
                        progress = 1.0f - (((float) Math.abs(ConnectionsManager.getInstance(this.currentAccount).getCurrentTime() - this.currentMessageObject.messageOwner.date)) / ((float) this.currentMessageObject.messageOwner.media.period));
                        this.rect.set((float) (this.photoImage.getImageX2() - AndroidUtilities.dp(43.0f)), (float) (cy - AndroidUtilities.dp(15.0f)), (float) (this.photoImage.getImageX2() - AndroidUtilities.dp(13.0f)), (float) (AndroidUtilities.dp(15.0f) + cy));
                        if (this.currentMessageObject.isOutOwner()) {
                            Theme.chat_radialProgress2Paint.setColor(Theme.getColor(Theme.key_chat_outInstant));
                            Theme.chat_livePaint.setColor(Theme.getColor(Theme.key_chat_outInstant));
                        } else {
                            Theme.chat_radialProgress2Paint.setColor(Theme.getColor(Theme.key_chat_inInstant));
                            Theme.chat_livePaint.setColor(Theme.getColor(Theme.key_chat_inInstant));
                        }
                        Theme.chat_radialProgress2Paint.setAlpha(50);
                        canvas.drawCircle(this.rect.centerX(), this.rect.centerY(), (float) AndroidUtilities.dp(15.0f), Theme.chat_radialProgress2Paint);
                        Theme.chat_radialProgress2Paint.setAlpha(255);
                        canvas.drawArc(this.rect, -90.0f, -360.0f * progress, false, Theme.chat_radialProgress2Paint);
                        String text = LocaleController.formatLocationLeftTime(Math.abs(this.currentMessageObject.messageOwner.media.period - (ConnectionsManager.getInstance(this.currentAccount).getCurrentTime() - this.currentMessageObject.messageOwner.date)));
                        canvas.drawText(text, this.rect.centerX() - (Theme.chat_livePaint.measureText(text) / 2.0f), (float) (AndroidUtilities.dp(4.0f) + cy), Theme.chat_livePaint);
                        canvas.save();
                        canvas.translate((float) (this.photoImage.getImageX() + AndroidUtilities.dp(10.0f)), (float) (this.photoImage.getImageY2() + AndroidUtilities.dp(10.0f)));
                        this.docTitleLayout.draw(canvas);
                        canvas.translate(0.0f, (float) AndroidUtilities.dp(23.0f));
                        this.infoLayout.draw(canvas);
                        canvas.restore();
                    }
                    int cx = (this.photoImage.getImageX() + (this.photoImage.getImageWidth() / 2)) - AndroidUtilities.dp(31.0f);
                    cy = (this.photoImage.getImageY() + (this.photoImage.getImageHeight() / 2)) - AndroidUtilities.dp(38.0f);
                    BaseCell.setDrawableBounds(Theme.chat_msgAvatarLiveLocationDrawable, cx, cy);
                    Theme.chat_msgAvatarLiveLocationDrawable.draw(canvas);
                    this.locationImageReceiver.setImageCoords(AndroidUtilities.dp(5.0f) + cx, AndroidUtilities.dp(5.0f) + cy, AndroidUtilities.dp(52.0f), AndroidUtilities.dp(52.0f));
                    this.locationImageReceiver.draw(canvas);
                } else {
                    canvas.save();
                    canvas.translate((float) (this.photoImage.getImageX() + AndroidUtilities.dp(6.0f)), (float) (this.photoImage.getImageY2() + AndroidUtilities.dp(8.0f)));
                    this.docTitleLayout.draw(canvas);
                    if (this.infoLayout != null) {
                        canvas.translate(0.0f, (float) AndroidUtilities.dp(21.0f));
                        this.infoLayout.draw(canvas);
                    }
                    canvas.restore();
                }
            }
        } else if (this.currentMessageObject.type == 16) {
            Drawable icon;
            Drawable phone;
            if (this.currentMessageObject.isOutOwner()) {
                Theme.chat_audioTitlePaint.setColor(Theme.getColor(Theme.key_chat_messageTextOut));
                Theme.chat_contactPhonePaint.setColor(Theme.getColor(isDrawSelectedBackground() ? Theme.key_chat_outTimeSelectedText : Theme.key_chat_outTimeText));
            } else {
                Theme.chat_audioTitlePaint.setColor(Theme.getColor(Theme.key_chat_messageTextIn));
                Theme.chat_contactPhonePaint.setColor(Theme.getColor(isDrawSelectedBackground() ? Theme.key_chat_inTimeSelectedText : Theme.key_chat_inTimeText));
            }
            this.forceNotDrawTime = true;
            if (this.currentMessageObject.isOutOwner()) {
                x = (this.layoutWidth - this.backgroundWidth) + AndroidUtilities.dp(16.0f);
            } else if (this.isChat && this.currentMessageObject.needDrawAvatar()) {
                x = AndroidUtilities.dp(74.0f);
            } else {
                x = AndroidUtilities.dp(25.0f);
            }
            this.otherX = x;
            if (this.titleLayout != null) {
                canvas.save();
                canvas.translate((float) x, (float) (AndroidUtilities.dp(12.0f) + this.namesOffset));
                this.titleLayout.draw(canvas);
                canvas.restore();
            }
            if (this.docTitleLayout != null) {
                canvas.save();
                canvas.translate((float) (AndroidUtilities.dp(19.0f) + x), (float) (AndroidUtilities.dp(37.0f) + this.namesOffset));
                this.docTitleLayout.draw(canvas);
                canvas.restore();
            }
            if (this.currentMessageObject.isOutOwner()) {
                icon = Theme.chat_msgCallUpGreenDrawable;
                phone = (isDrawSelectedBackground() || this.otherPressed) ? Theme.chat_msgOutCallSelectedDrawable : Theme.chat_msgOutCallDrawable;
            } else {
                PhoneCallDiscardReason reason = this.currentMessageObject.messageOwner.action.reason;
                if ((reason instanceof TL_phoneCallDiscardReasonMissed) || (reason instanceof TL_phoneCallDiscardReasonBusy)) {
                    icon = Theme.chat_msgCallDownRedDrawable;
                } else {
                    icon = Theme.chat_msgCallDownGreenDrawable;
                }
                phone = (isDrawSelectedBackground() || this.otherPressed) ? Theme.chat_msgInCallSelectedDrawable : Theme.chat_msgInCallDrawable;
            }
            BaseCell.setDrawableBounds(icon, x - AndroidUtilities.dp(3.0f), AndroidUtilities.dp(36.0f) + this.namesOffset);
            icon.draw(canvas);
            i = AndroidUtilities.dp(205.0f) + x;
            i2 = AndroidUtilities.dp(22.0f);
            this.otherY = i2;
            BaseCell.setDrawableBounds(phone, i, i2);
            phone.draw(canvas);
        } else if (this.currentMessageObject.type == 12) {
            if (this.currentMessageObject.isOutOwner()) {
                Theme.chat_contactNamePaint.setColor(Theme.getColor(Theme.key_chat_outContactNameText));
                Theme.chat_contactPhonePaint.setColor(Theme.getColor(isDrawSelectedBackground() ? "chat_outContactPhoneText" : "chat_outContactPhoneText"));
            } else {
                Theme.chat_contactNamePaint.setColor(Theme.getColor(Theme.key_chat_inContactNameText));
                Theme.chat_contactPhonePaint.setColor(Theme.getColor(isDrawSelectedBackground() ? Theme.key_chat_inContactPhoneSelectedText : Theme.key_chat_inContactPhoneText));
            }
            if (this.titleLayout != null) {
                canvas.save();
                canvas.translate((float) ((this.photoImage.getImageX() + this.photoImage.getImageWidth()) + AndroidUtilities.dp(9.0f)), (float) (AndroidUtilities.dp(16.0f) + this.namesOffset));
                this.titleLayout.draw(canvas);
                canvas.restore();
            }
            if (this.docTitleLayout != null) {
                canvas.save();
                canvas.translate((float) ((this.photoImage.getImageX() + this.photoImage.getImageWidth()) + AndroidUtilities.dp(9.0f)), (float) (AndroidUtilities.dp(39.0f) + this.namesOffset));
                this.docTitleLayout.draw(canvas);
                canvas.restore();
            }
            menuDrawable = this.currentMessageObject.isOutOwner() ? isDrawSelectedBackground() ? Theme.chat_msgOutMenuSelectedDrawable : Theme.chat_msgOutMenuDrawable : isDrawSelectedBackground() ? Theme.chat_msgInMenuSelectedDrawable : Theme.chat_msgInMenuDrawable;
            i = (this.photoImage.getImageX() + this.backgroundWidth) - AndroidUtilities.dp(48.0f);
            this.otherX = i;
            i2 = this.photoImage.getImageY() - AndroidUtilities.dp(5.0f);
            this.otherY = i2;
            BaseCell.setDrawableBounds(menuDrawable, i, i2);
            menuDrawable.draw(canvas);
            if (this.drawInstantView) {
                int textX = this.photoImage.getImageX() - AndroidUtilities.dp(2.0f);
                instantY = getMeasuredHeight() - AndroidUtilities.dp(64.0f);
                backPaint = Theme.chat_instantViewRectPaint;
                if (this.currentMessageObject.isOutOwner()) {
                    Theme.chat_instantViewPaint.setColor(Theme.getColor(Theme.key_chat_outPreviewInstantText));
                    backPaint.setColor(Theme.getColor(Theme.key_chat_outPreviewInstantText));
                } else {
                    Theme.chat_instantViewPaint.setColor(Theme.getColor(Theme.key_chat_inPreviewInstantText));
                    backPaint.setColor(Theme.getColor(Theme.key_chat_inPreviewInstantText));
                }
                if (VERSION.SDK_INT >= 21) {
                    this.instantViewSelectorDrawable.setBounds(textX, instantY, this.instantWidth + textX, AndroidUtilities.dp(36.0f) + instantY);
                    this.instantViewSelectorDrawable.draw(canvas);
                }
                this.instantButtonRect.set((float) textX, (float) instantY, (float) (this.instantWidth + textX), (float) (AndroidUtilities.dp(36.0f) + instantY));
                canvas.drawRoundRect(this.instantButtonRect, (float) AndroidUtilities.dp(6.0f), (float) AndroidUtilities.dp(6.0f), backPaint);
                if (this.instantViewLayout != null) {
                    canvas.save();
                    canvas.translate((float) (this.instantTextX + textX), (float) (AndroidUtilities.dp(10.5f) + instantY));
                    this.instantViewLayout.draw(canvas);
                    canvas.restore();
                }
            }
        }
        if (this.captionLayout != null) {
            if (this.currentMessageObject.type == 1 || this.documentAttachType == 4 || this.currentMessageObject.type == 8) {
                this.captionX = (this.photoImage.getImageX() + AndroidUtilities.dp(5.0f)) + this.captionOffsetX;
                this.captionY = (this.photoImage.getImageY() + this.photoImage.getImageHeight()) + AndroidUtilities.dp(6.0f);
            } else if (this.hasOldCaptionPreview) {
                this.captionX = (AndroidUtilities.dp(this.currentMessageObject.isOutOwner() ? 11.0f : 17.0f) + this.backgroundDrawableLeft) + this.captionOffsetX;
                this.captionY = (((this.totalHeight - this.captionHeight) - AndroidUtilities.dp(this.drawPinnedTop ? 9.0f : 10.0f)) - this.linkPreviewHeight) - AndroidUtilities.dp(17.0f);
            } else {
                this.captionX = (AndroidUtilities.dp(this.currentMessageObject.isOutOwner() ? 11.0f : 17.0f) + this.backgroundDrawableLeft) + this.captionOffsetX;
                this.captionY = (this.totalHeight - this.captionHeight) - AndroidUtilities.dp(this.drawPinnedTop ? 9.0f : 10.0f);
            }
        }
        if (this.currentPosition == null) {
            drawCaptionLayout(canvas, false);
        }
        if (this.hasOldCaptionPreview) {
            if (this.currentMessageObject.type == 1 || this.documentAttachType == 4 || this.currentMessageObject.type == 8) {
                linkX = this.photoImage.getImageX() + AndroidUtilities.dp(5.0f);
            } else {
                linkX = this.backgroundDrawableLeft + AndroidUtilities.dp(this.currentMessageObject.isOutOwner() ? 11.0f : 17.0f);
            }
            startY = ((this.totalHeight - AndroidUtilities.dp(this.drawPinnedTop ? 9.0f : 10.0f)) - this.linkPreviewHeight) - AndroidUtilities.dp(8.0f);
            linkPreviewY = startY;
            Theme.chat_replyLinePaint.setColor(Theme.getColor(this.currentMessageObject.isOutOwner() ? Theme.key_chat_outPreviewLine : Theme.key_chat_inPreviewLine));
            canvas.drawRect((float) linkX, (float) (linkPreviewY - AndroidUtilities.dp(3.0f)), (float) (AndroidUtilities.dp(2.0f) + linkX), (float) (this.linkPreviewHeight + linkPreviewY), Theme.chat_replyLinePaint);
            if (this.siteNameLayout != null) {
                Theme.chat_replyNamePaint.setColor(Theme.getColor(this.currentMessageObject.isOutOwner() ? Theme.key_chat_outSiteNameText : Theme.key_chat_inSiteNameText));
                canvas.save();
                if (this.siteNameRtl) {
                    x = (this.backgroundWidth - this.siteNameWidth) - AndroidUtilities.dp(32.0f);
                } else if (this.hasInvoicePreview) {
                    x = 0;
                } else {
                    x = AndroidUtilities.dp(10.0f);
                }
                canvas.translate((float) (linkX + x), (float) (linkPreviewY - AndroidUtilities.dp(3.0f)));
                this.siteNameLayout.draw(canvas);
                canvas.restore();
                linkPreviewY += this.siteNameLayout.getLineBottom(this.siteNameLayout.getLineCount() - 1);
            }
            if (this.currentMessageObject.isOutOwner()) {
                Theme.chat_replyTextPaint.setColor(Theme.getColor(Theme.key_chat_messageTextOut));
            } else {
                Theme.chat_replyTextPaint.setColor(Theme.getColor(Theme.key_chat_messageTextIn));
            }
            if (this.descriptionLayout != null) {
                if (linkPreviewY != startY) {
                    linkPreviewY += AndroidUtilities.dp(2.0f);
                }
                this.descriptionY = linkPreviewY - AndroidUtilities.dp(3.0f);
                canvas.save();
                canvas.translate((float) ((AndroidUtilities.dp(10.0f) + linkX) + this.descriptionX), (float) this.descriptionY);
                this.descriptionLayout.draw(canvas);
                canvas.restore();
            }
            this.drawTime = true;
        }
        if (this.documentAttachType == 1) {
            int titleY;
            int subtitleY;
            if (this.currentMessageObject.isOutOwner()) {
                Theme.chat_docNamePaint.setColor(Theme.getColor(Theme.key_chat_outFileNameText));
                Theme.chat_infoPaint.setColor(Theme.getColor(isDrawSelectedBackground() ? Theme.key_chat_outFileInfoSelectedText : Theme.key_chat_outFileInfoText));
                Theme.chat_docBackPaint.setColor(Theme.getColor(isDrawSelectedBackground() ? Theme.key_chat_outFileBackgroundSelected : Theme.key_chat_outFileBackground));
                if (isDrawSelectedBackground()) {
                    menuDrawable = Theme.chat_msgOutMenuSelectedDrawable;
                } else {
                    menuDrawable = Theme.chat_msgOutMenuDrawable;
                }
            } else {
                Theme.chat_docNamePaint.setColor(Theme.getColor(Theme.key_chat_inFileNameText));
                Theme.chat_infoPaint.setColor(Theme.getColor(isDrawSelectedBackground() ? Theme.key_chat_inFileInfoSelectedText : Theme.key_chat_inFileInfoText));
                Theme.chat_docBackPaint.setColor(Theme.getColor(isDrawSelectedBackground() ? Theme.key_chat_inFileBackgroundSelected : Theme.key_chat_inFileBackground));
                menuDrawable = isDrawSelectedBackground() ? Theme.chat_msgInMenuSelectedDrawable : Theme.chat_msgInMenuDrawable;
            }
            if (this.drawPhotoImage) {
                if (this.currentMessageObject.type == 0) {
                    i = (this.photoImage.getImageX() + this.backgroundWidth) - AndroidUtilities.dp(56.0f);
                    this.otherX = i;
                    i2 = this.photoImage.getImageY() + AndroidUtilities.dp(1.0f);
                    this.otherY = i2;
                    BaseCell.setDrawableBounds(menuDrawable, i, i2);
                } else {
                    i = (this.photoImage.getImageX() + this.backgroundWidth) - AndroidUtilities.dp(40.0f);
                    this.otherX = i;
                    i2 = this.photoImage.getImageY() + AndroidUtilities.dp(1.0f);
                    this.otherY = i2;
                    BaseCell.setDrawableBounds(menuDrawable, i, i2);
                }
                x = (this.photoImage.getImageX() + this.photoImage.getImageWidth()) + AndroidUtilities.dp(10.0f);
                titleY = this.photoImage.getImageY() + AndroidUtilities.dp(8.0f);
                subtitleY = (this.photoImage.getImageY() + this.docTitleLayout.getLineBottom(this.docTitleLayout.getLineCount() - 1)) + AndroidUtilities.dp(13.0f);
                if (this.buttonState >= 0 && this.buttonState < 4) {
                    if (imageDrawn) {
                        this.radialProgress.swapBackground(Theme.chat_photoStatesDrawables[this.buttonState][this.buttonPressed]);
                    } else {
                        int image = this.buttonState;
                        if (this.buttonState == 0) {
                            image = this.currentMessageObject.isOutOwner() ? 7 : 10;
                        } else if (this.buttonState == 1) {
                            image = this.currentMessageObject.isOutOwner() ? 8 : 11;
                        }
                        radialProgress = this.radialProgress;
                        Drawable[] drawableArr = Theme.chat_photoStatesDrawables[image];
                        i = (isDrawSelectedBackground() || this.buttonPressed != 0) ? 1 : 0;
                        radialProgress.swapBackground(drawableArr[i]);
                    }
                }
                if (imageDrawn) {
                    if (this.buttonState == -1) {
                        this.radialProgress.setHideCurrentDrawable(true);
                    }
                    this.radialProgress.setProgressColor(Theme.getColor(Theme.key_chat_mediaProgress));
                } else {
                    this.rect.set((float) this.photoImage.getImageX(), (float) this.photoImage.getImageY(), (float) (this.photoImage.getImageX() + this.photoImage.getImageWidth()), (float) (this.photoImage.getImageY() + this.photoImage.getImageHeight()));
                    canvas.drawRoundRect(this.rect, (float) AndroidUtilities.dp(3.0f), (float) AndroidUtilities.dp(3.0f), Theme.chat_docBackPaint);
                    if (this.currentMessageObject.isOutOwner()) {
                        radialProgress = this.radialProgress;
                        if (isDrawSelectedBackground()) {
                            str = Theme.key_chat_outFileProgressSelected;
                        } else {
                            str = Theme.key_chat_outFileProgress;
                        }
                        radialProgress.setProgressColor(Theme.getColor(str));
                    } else {
                        this.radialProgress.setProgressColor(Theme.getColor(isDrawSelectedBackground() ? Theme.key_chat_inFileProgressSelected : Theme.key_chat_inFileProgress));
                    }
                }
            } else {
                i = (this.backgroundWidth + this.buttonX) - AndroidUtilities.dp(this.currentMessageObject.type == 0 ? 58.0f : 48.0f);
                this.otherX = i;
                i2 = this.buttonY - AndroidUtilities.dp(5.0f);
                this.otherY = i2;
                BaseCell.setDrawableBounds(menuDrawable, i, i2);
                x = this.buttonX + AndroidUtilities.dp(53.0f);
                titleY = this.buttonY + AndroidUtilities.dp(4.0f);
                subtitleY = this.buttonY + AndroidUtilities.dp(27.0f);
                if (this.currentMessageObject.isOutOwner()) {
                    radialProgress = this.radialProgress;
                    if (isDrawSelectedBackground() || this.buttonPressed != 0) {
                        str = Theme.key_chat_outAudioSelectedProgress;
                    } else {
                        str = Theme.key_chat_outAudioProgress;
                    }
                    radialProgress.setProgressColor(Theme.getColor(str));
                } else {
                    radialProgress = this.radialProgress;
                    str = (isDrawSelectedBackground() || this.buttonPressed != 0) ? Theme.key_chat_inAudioSelectedProgress : Theme.key_chat_inAudioProgress;
                    radialProgress.setProgressColor(Theme.getColor(str));
                }
            }
            menuDrawable.draw(canvas);
            try {
                if (this.docTitleLayout != null) {
                    canvas.save();
                    canvas.translate((float) (this.docTitleOffsetX + x), (float) titleY);
                    this.docTitleLayout.draw(canvas);
                    canvas.restore();
                }
            } catch (Throwable e2) {
                FileLog.e(e2);
            }
            try {
                if (this.infoLayout != null) {
                    canvas.save();
                    canvas.translate((float) x, (float) subtitleY);
                    this.infoLayout.draw(canvas);
                    canvas.restore();
                }
            } catch (Throwable e22) {
                FileLog.e(e22);
            }
        }
        if (this.drawImageButton && this.photoImage.getVisible()) {
            if (this.controlsAlpha != 1.0f) {
                this.radialProgress.setOverrideAlpha(this.controlsAlpha);
            }
            this.radialProgress.draw(canvas);
        }
        if (this.currentMessageObject.type == 4 && !(this.currentMessageObject.messageOwner.media instanceof TL_messageMediaGeoLive) && this.currentMapProvider == 2 && this.photoImage.hasNotThumb()) {
            int w = (int) (((float) Theme.chat_redLocationIcon.getIntrinsicWidth()) * 0.8f);
            int h = (int) (((float) Theme.chat_redLocationIcon.getIntrinsicHeight()) * 0.8f);
            x = this.photoImage.getImageX() + ((this.photoImage.getImageWidth() - w) / 2);
            y = this.photoImage.getImageY() + ((this.photoImage.getImageHeight() / 2) - h);
            Theme.chat_redLocationIcon.setAlpha((int) (255.0f * this.photoImage.getCurrentAlpha()));
            Theme.chat_redLocationIcon.setBounds(x, y, x + w, y + h);
            Theme.chat_redLocationIcon.draw(canvas);
        }
        if (!this.botButtons.isEmpty()) {
            int addX;
            if (this.currentMessageObject.isOutOwner()) {
                addX = (getMeasuredWidth() - this.widthForButtons) - AndroidUtilities.dp(10.0f);
            } else {
                addX = this.backgroundDrawableLeft + AndroidUtilities.dp(this.mediaBackground ? 1.0f : 7.0f);
            }
            a = 0;
            while (a < this.botButtons.size()) {
                BotButton button = (BotButton) this.botButtons.get(a);
                y = (button.y + this.layoutHeight) - AndroidUtilities.dp(2.0f);
                Theme.chat_systemDrawable.setColorFilter(a == this.pressedBotButton ? Theme.colorPressedFilter : Theme.colorFilter);
                Theme.chat_systemDrawable.setBounds(button.x + addX, y, (button.x + addX) + button.width, button.height + y);
                Theme.chat_systemDrawable.draw(canvas);
                canvas.save();
                canvas.translate((float) ((button.x + addX) + AndroidUtilities.dp(5.0f)), (float) (((AndroidUtilities.dp(44.0f) - button.title.getLineBottom(button.title.getLineCount() - 1)) / 2) + y));
                button.title.draw(canvas);
                canvas.restore();
                if (button.button instanceof TL_keyboardButtonUrl) {
                    BaseCell.setDrawableBounds(Theme.chat_botLinkDrawalbe, (((button.x + button.width) - AndroidUtilities.dp(3.0f)) - Theme.chat_botLinkDrawalbe.getIntrinsicWidth()) + addX, AndroidUtilities.dp(3.0f) + y);
                    Theme.chat_botLinkDrawalbe.draw(canvas);
                } else if (button.button instanceof TL_keyboardButtonSwitchInline) {
                    BaseCell.setDrawableBounds(Theme.chat_botInlineDrawable, (((button.x + button.width) - AndroidUtilities.dp(3.0f)) - Theme.chat_botInlineDrawable.getIntrinsicWidth()) + addX, AndroidUtilities.dp(3.0f) + y);
                    Theme.chat_botInlineDrawable.draw(canvas);
                } else if ((button.button instanceof TL_keyboardButtonCallback) || (button.button instanceof TL_keyboardButtonRequestGeoLocation) || (button.button instanceof TL_keyboardButtonGame) || (button.button instanceof TL_keyboardButtonBuy)) {
                    boolean drawProgress = (((button.button instanceof TL_keyboardButtonCallback) || (button.button instanceof TL_keyboardButtonGame) || (button.button instanceof TL_keyboardButtonBuy)) && SendMessagesHelper.getInstance(this.currentAccount).isSendingCallback(this.currentMessageObject, button.button)) || ((button.button instanceof TL_keyboardButtonRequestGeoLocation) && SendMessagesHelper.getInstance(this.currentAccount).isSendingCurrentLocation(this.currentMessageObject, button.button));
                    if (drawProgress || !(drawProgress || button.progressAlpha == 0.0f)) {
                        Theme.chat_botProgressPaint.setAlpha(Math.min(255, (int) (button.progressAlpha * 255.0f)));
                        x = ((button.x + button.width) - AndroidUtilities.dp(12.0f)) + addX;
                        this.rect.set((float) x, (float) (AndroidUtilities.dp(4.0f) + y), (float) (AndroidUtilities.dp(8.0f) + x), (float) (AndroidUtilities.dp(12.0f) + y));
                        canvas.drawArc(this.rect, (float) button.angle, 220.0f, false, Theme.chat_botProgressPaint);
                        invalidate(((int) this.rect.left) - AndroidUtilities.dp(2.0f), ((int) this.rect.top) - AndroidUtilities.dp(2.0f), ((int) this.rect.right) + AndroidUtilities.dp(2.0f), ((int) this.rect.bottom) + AndroidUtilities.dp(2.0f));
                        long newTime = System.currentTimeMillis();
                        if (Math.abs(button.lastUpdateTime - System.currentTimeMillis()) < 1000) {
                            long delta = newTime - button.lastUpdateTime;
                            button.angle = (int) (((float) button.angle) + (((float) (360 * delta)) / 2000.0f));
                            button.angle = button.angle - ((button.angle / 360) * 360);
                            if (drawProgress) {
                                if (button.progressAlpha < 1.0f) {
                                    button.progressAlpha = button.progressAlpha + (((float) delta) / 200.0f);
                                    if (button.progressAlpha > 1.0f) {
                                        button.progressAlpha = 1.0f;
                                    }
                                }
                            } else if (button.progressAlpha > 0.0f) {
                                button.progressAlpha = button.progressAlpha - (((float) delta) / 200.0f);
                                if (button.progressAlpha < 0.0f) {
                                    button.progressAlpha = 0.0f;
                                }
                            }
                        }
                        button.lastUpdateTime = newTime;
                    }
                }
                a++;
            }
        }
    }

    private Drawable getMiniDrawableForCurrentState() {
        int i = 1;
        if (this.miniButtonState < 0) {
            return null;
        }
        if (this.documentAttachType == 3 || this.documentAttachType == 5) {
            this.radialProgress.setAlphaForPrevious(false);
            CombinedDrawable[] combinedDrawableArr = Theme.chat_fileMiniStatesDrawable[this.currentMessageObject.isOutOwner() ? this.miniButtonState : this.miniButtonState + 2];
            int i2 = (isDrawSelectedBackground() || this.miniButtonPressed != 0) ? 1 : 0;
            return combinedDrawableArr[i2];
        } else if (this.documentAttachType != 4) {
            return null;
        } else {
            CombinedDrawable[] combinedDrawableArr2 = Theme.chat_fileMiniStatesDrawable[this.miniButtonState + 4];
            if (this.miniButtonPressed == 0) {
                i = 0;
            }
            return combinedDrawableArr2[i];
        }
    }

    private Drawable getDrawableForCurrentState() {
        int i = 3;
        int i2 = 0;
        int i3 = 1;
        if (this.documentAttachType != 3 && this.documentAttachType != 5) {
            Drawable[] drawableArr;
            if (this.documentAttachType != 1 || this.drawPhotoImage) {
                this.radialProgress.setAlphaForPrevious(true);
                if (this.buttonState < 0 || this.buttonState >= 4) {
                    if (this.buttonState == -1 && this.documentAttachType == 1) {
                        drawableArr = Theme.chat_photoStatesDrawables[this.currentMessageObject.isOutOwner() ? 9 : 12];
                        if (!isDrawSelectedBackground()) {
                            i3 = 0;
                        }
                        return drawableArr[i3];
                    }
                } else if (this.documentAttachType != 1) {
                    return Theme.chat_photoStatesDrawables[this.buttonState][this.buttonPressed];
                } else {
                    int image = this.buttonState;
                    if (this.buttonState == 0) {
                        image = this.currentMessageObject.isOutOwner() ? 7 : 10;
                    } else if (this.buttonState == 1) {
                        image = this.currentMessageObject.isOutOwner() ? 8 : 11;
                    }
                    drawableArr = Theme.chat_photoStatesDrawables[image];
                    if (isDrawSelectedBackground() || this.buttonPressed != 0) {
                        i2 = 1;
                    }
                    return drawableArr[i2];
                }
            }
            this.radialProgress.setAlphaForPrevious(false);
            if (this.buttonState == -1) {
                Drawable[][] drawableArr2 = Theme.chat_fileStatesDrawable;
                if (!this.currentMessageObject.isOutOwner()) {
                    i = 8;
                }
                drawableArr = drawableArr2[i];
                if (!isDrawSelectedBackground()) {
                    i3 = 0;
                }
                return drawableArr[i3];
            } else if (this.buttonState == 0) {
                drawableArr = Theme.chat_fileStatesDrawable[this.currentMessageObject.isOutOwner() ? 2 : 7];
                if (!isDrawSelectedBackground()) {
                    i3 = 0;
                }
                return drawableArr[i3];
            } else if (this.buttonState == 1) {
                drawableArr = Theme.chat_fileStatesDrawable[this.currentMessageObject.isOutOwner() ? 4 : 9];
                if (!isDrawSelectedBackground()) {
                    i3 = 0;
                }
                return drawableArr[i3];
            }
            return null;
        } else if (this.buttonState == -1) {
            return null;
        } else {
            this.radialProgress.setAlphaForPrevious(false);
            this.radialProgress.setAlphaForMiniPrevious(true);
            Drawable[] drawableArr3 = Theme.chat_fileStatesDrawable[this.currentMessageObject.isOutOwner() ? this.buttonState : this.buttonState + 5];
            i = (isDrawSelectedBackground() || this.buttonPressed != 0) ? 1 : 0;
            return drawableArr3[i];
        }
    }

    private int getMaxNameWidth() {
        if (this.documentAttachType == 6 || this.currentMessageObject.type == 5) {
            int maxWidth;
            if (AndroidUtilities.isTablet()) {
                if (this.isChat && !this.currentMessageObject.isOutOwner() && this.currentMessageObject.needDrawAvatar()) {
                    maxWidth = AndroidUtilities.getMinTabletSide() - AndroidUtilities.dp(42.0f);
                } else {
                    maxWidth = AndroidUtilities.getMinTabletSide();
                }
            } else if (this.isChat && !this.currentMessageObject.isOutOwner() && this.currentMessageObject.needDrawAvatar()) {
                maxWidth = Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) - AndroidUtilities.dp(42.0f);
            } else {
                maxWidth = Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y);
            }
            return (maxWidth - this.backgroundWidth) - AndroidUtilities.dp(57.0f);
        } else if (this.currentMessagesGroup != null) {
            int dWidth;
            if (AndroidUtilities.isTablet()) {
                dWidth = AndroidUtilities.getMinTabletSide();
            } else {
                dWidth = AndroidUtilities.displaySize.x;
            }
            int firstLineWidth = 0;
            for (int a = 0; a < this.currentMessagesGroup.posArray.size(); a++) {
                GroupedMessagePosition position = (GroupedMessagePosition) this.currentMessagesGroup.posArray.get(a);
                if (position.minY != (byte) 0) {
                    break;
                }
                firstLineWidth = (int) (((double) firstLineWidth) + Math.ceil((double) ((((float) (position.pw + position.leftSpanOffset)) / 1000.0f) * ((float) dWidth))));
            }
            return firstLineWidth - AndroidUtilities.dp((float) ((this.isAvatarVisible ? 48 : 0) + 31));
        } else {
            return this.backgroundWidth - AndroidUtilities.dp(this.mediaBackground ? 22.0f : 31.0f);
        }
    }

    public void updateButtonState(boolean animated, boolean fromSet) {
        this.drawRadialCheckBackground = false;
        String fileName = null;
        boolean fileExists = false;
        if (this.currentMessageObject.type == 1) {
            if (this.currentPhotoObject != null) {
                fileName = FileLoader.getAttachFileName(this.currentPhotoObject);
                fileExists = this.currentMessageObject.mediaExists;
            } else {
                return;
            }
        } else if (this.currentMessageObject.type == 8 || this.currentMessageObject.type == 5 || this.documentAttachType == 7 || this.documentAttachType == 4 || this.currentMessageObject.type == 9 || this.documentAttachType == 3 || this.documentAttachType == 5) {
            if (this.currentMessageObject.useCustomPhoto) {
                this.buttonState = 1;
                this.radialProgress.setBackground(getDrawableForCurrentState(), false, animated);
                return;
            } else if (this.currentMessageObject.attachPathExists) {
                fileName = this.currentMessageObject.messageOwner.attachPath;
                fileExists = true;
            } else if (!this.currentMessageObject.isSendError() || this.documentAttachType == 3 || this.documentAttachType == 5) {
                fileName = this.currentMessageObject.getFileName();
                fileExists = this.currentMessageObject.mediaExists;
            }
        } else if (this.documentAttachType != 0) {
            fileName = FileLoader.getAttachFileName(this.documentAttach);
            fileExists = this.currentMessageObject.mediaExists;
        } else if (this.currentPhotoObject != null) {
            fileName = FileLoader.getAttachFileName(this.currentPhotoObject);
            fileExists = this.currentMessageObject.mediaExists;
        }
        if (SharedConfig.streamMedia && ((int) this.currentMessageObject.getDialogId()) != 0 && !this.currentMessageObject.isSecretMedia() && (this.documentAttachType == 5 || (this.documentAttachType == 4 && this.currentMessageObject.canStreamVideo()))) {
            this.hasMiniProgress = fileExists ? 1 : 2;
            fileExists = true;
        }
        if (TextUtils.isEmpty(fileName)) {
            this.radialProgress.setBackground(null, false, false);
            this.radialProgress.setMiniBackground(null, false, false);
            return;
        }
        boolean fromBot = this.currentMessageObject.messageOwner.params != null && this.currentMessageObject.messageOwner.params.containsKey("query_id");
        Float progress;
        RadialProgress radialProgress;
        Drawable miniDrawableForCurrentState;
        boolean z;
        if (this.documentAttachType == 3 || this.documentAttachType == 5) {
            if ((this.currentMessageObject.isOut() && (this.currentMessageObject.isSending() || this.currentMessageObject.isEditing())) || (this.currentMessageObject.isSendError() && fromBot)) {
                DownloadController.getInstance(this.currentAccount).addLoadingFileObserver(this.currentMessageObject.messageOwner.attachPath, this.currentMessageObject, this);
                this.buttonState = 4;
                this.radialProgress.setBackground(getDrawableForCurrentState(), !fromBot, animated);
                if (fromBot) {
                    this.radialProgress.setProgress(0.0f, false);
                } else {
                    float floatValue;
                    progress = ImageLoader.getInstance().getFileProgress(this.currentMessageObject.messageOwner.attachPath);
                    if (progress == null && SendMessagesHelper.getInstance(this.currentAccount).isSendingMessage(this.currentMessageObject.getId())) {
                        progress = Float.valueOf(1.0f);
                    }
                    radialProgress = this.radialProgress;
                    if (progress != null) {
                        floatValue = progress.floatValue();
                    } else {
                        floatValue = 0.0f;
                    }
                    radialProgress.setProgress(floatValue, false);
                }
            } else if (this.hasMiniProgress != 0) {
                this.radialProgress.setMiniProgressBackgroundColor(Theme.getColor(this.currentMessageObject.isOutOwner() ? Theme.key_chat_outLoader : Theme.key_chat_inLoader));
                playing = MediaController.getInstance().isPlayingMessage(this.currentMessageObject);
                if (!playing || (playing && MediaController.getInstance().isMessagePaused())) {
                    this.buttonState = 0;
                } else {
                    this.buttonState = 1;
                }
                this.radialProgress.setBackground(getDrawableForCurrentState(), false, animated);
                if (this.hasMiniProgress == 1) {
                    DownloadController.getInstance(this.currentAccount).removeLoadingFileObserver(this);
                    this.miniButtonState = -1;
                } else {
                    DownloadController.getInstance(this.currentAccount).addLoadingFileObserver(fileName, this.currentMessageObject, this);
                    if (FileLoader.getInstance(this.currentAccount).isLoadingFile(fileName)) {
                        this.miniButtonState = 1;
                        progress = ImageLoader.getInstance().getFileProgress(fileName);
                        if (progress != null) {
                            this.radialProgress.setProgress(progress.floatValue(), animated);
                        } else {
                            this.radialProgress.setProgress(0.0f, animated);
                        }
                    } else {
                        this.radialProgress.setProgress(0.0f, animated);
                        this.miniButtonState = 0;
                    }
                }
                radialProgress = this.radialProgress;
                miniDrawableForCurrentState = getMiniDrawableForCurrentState();
                if (this.miniButtonState == 1) {
                    z = true;
                } else {
                    z = false;
                }
                radialProgress.setMiniBackground(miniDrawableForCurrentState, z, animated);
            } else if (fileExists) {
                DownloadController.getInstance(this.currentAccount).removeLoadingFileObserver(this);
                playing = MediaController.getInstance().isPlayingMessage(this.currentMessageObject);
                if (!playing || (playing && MediaController.getInstance().isMessagePaused())) {
                    this.buttonState = 0;
                } else {
                    this.buttonState = 1;
                }
                this.radialProgress.setBackground(getDrawableForCurrentState(), false, animated);
            } else {
                DownloadController.getInstance(this.currentAccount).addLoadingFileObserver(fileName, this.currentMessageObject, this);
                if (FileLoader.getInstance(this.currentAccount).isLoadingFile(fileName)) {
                    this.buttonState = 4;
                    progress = ImageLoader.getInstance().getFileProgress(fileName);
                    if (progress != null) {
                        this.radialProgress.setProgress(progress.floatValue(), animated);
                    } else {
                        this.radialProgress.setProgress(0.0f, animated);
                    }
                    this.radialProgress.setBackground(getDrawableForCurrentState(), true, animated);
                } else {
                    this.buttonState = 2;
                    this.radialProgress.setProgress(0.0f, animated);
                    this.radialProgress.setBackground(getDrawableForCurrentState(), false, animated);
                }
            }
            updatePlayingMessageProgress();
        } else if (this.currentMessageObject.type != 0 || this.documentAttachType == 1 || this.documentAttachType == 4) {
            if (!this.currentMessageObject.isOut() || (!this.currentMessageObject.isSending() && !this.currentMessageObject.isEditing())) {
                if (!(this.currentMessageObject.messageOwner.attachPath == null || this.currentMessageObject.messageOwner.attachPath.length() == 0)) {
                    DownloadController.getInstance(this.currentAccount).removeLoadingFileObserver(this);
                }
                if (this.hasMiniProgress != 0) {
                    this.radialProgress.setMiniProgressBackgroundColor(Theme.getColor(Theme.key_chat_inLoaderPhoto));
                    this.buttonState = 3;
                    this.radialProgress.setBackground(getDrawableForCurrentState(), false, animated);
                    if (this.hasMiniProgress == 1) {
                        DownloadController.getInstance(this.currentAccount).removeLoadingFileObserver(this);
                        this.miniButtonState = -1;
                    } else {
                        DownloadController.getInstance(this.currentAccount).addLoadingFileObserver(fileName, this.currentMessageObject, this);
                        if (FileLoader.getInstance(this.currentAccount).isLoadingFile(fileName)) {
                            this.miniButtonState = 1;
                            progress = ImageLoader.getInstance().getFileProgress(fileName);
                            if (progress != null) {
                                this.radialProgress.setProgress(progress.floatValue(), animated);
                            } else {
                                this.radialProgress.setProgress(0.0f, animated);
                            }
                        } else {
                            this.radialProgress.setProgress(0.0f, animated);
                            this.miniButtonState = 0;
                        }
                    }
                    radialProgress = this.radialProgress;
                    miniDrawableForCurrentState = getMiniDrawableForCurrentState();
                    if (this.miniButtonState == 1) {
                        z = true;
                    } else {
                        z = false;
                    }
                    radialProgress.setMiniBackground(miniDrawableForCurrentState, z, animated);
                } else if (fileExists) {
                    DownloadController.getInstance(this.currentAccount).removeLoadingFileObserver(this);
                    if (this.currentMessageObject.needDrawBluredPreview()) {
                        this.buttonState = -1;
                    } else if (this.currentMessageObject.type == 8 && !this.photoImage.isAllowStartAnimation()) {
                        this.buttonState = 2;
                    } else if (this.documentAttachType == 4) {
                        this.buttonState = 3;
                    } else {
                        this.buttonState = -1;
                    }
                    this.radialProgress.setBackground(getDrawableForCurrentState(), false, animated);
                    if (!fromSet && this.photoNotSet) {
                        setMessageObject(this.currentMessageObject, this.currentMessagesGroup, this.pinnedBottom, this.pinnedTop);
                    }
                    invalidate();
                } else {
                    DownloadController.getInstance(this.currentAccount).addLoadingFileObserver(fileName, this.currentMessageObject, this);
                    setProgress = 0.0f;
                    progressVisible = false;
                    if (FileLoader.getInstance(this.currentAccount).isLoadingFile(fileName)) {
                        progressVisible = true;
                        this.buttonState = 1;
                        progress = ImageLoader.getInstance().getFileProgress(fileName);
                        setProgress = progress != null ? progress.floatValue() : 0.0f;
                    } else {
                        boolean autoDownload = false;
                        if (this.currentMessageObject.type == 1) {
                            autoDownload = DownloadController.getInstance(this.currentAccount).canDownloadMedia(this.currentMessageObject);
                        } else if (this.currentMessageObject.type == 8 && MessageObject.isNewGifDocument(this.currentMessageObject.messageOwner.media.document)) {
                            autoDownload = DownloadController.getInstance(this.currentAccount).canDownloadMedia(this.currentMessageObject);
                        } else if (this.currentMessageObject.type == 5) {
                            autoDownload = DownloadController.getInstance(this.currentAccount).canDownloadMedia(this.currentMessageObject);
                        }
                        if (this.cancelLoading || !autoDownload) {
                            this.buttonState = 0;
                        } else {
                            progressVisible = true;
                            this.buttonState = 1;
                        }
                    }
                    this.radialProgress.setBackground(getDrawableForCurrentState(), progressVisible, animated);
                    this.radialProgress.setProgress(setProgress, false);
                    invalidate();
                }
            } else if (this.currentMessageObject.messageOwner.attachPath != null && this.currentMessageObject.messageOwner.attachPath.length() > 0) {
                DownloadController.getInstance(this.currentAccount).addLoadingFileObserver(this.currentMessageObject.messageOwner.attachPath, this.currentMessageObject, this);
                boolean needProgress = this.currentMessageObject.messageOwner.attachPath == null || !this.currentMessageObject.messageOwner.attachPath.startsWith("http");
                HashMap<String, String> params = this.currentMessageObject.messageOwner.params;
                if (this.currentMessageObject.messageOwner.message == null || params == null || !(params.containsKey(UpdateFragment.FRAGMENT_URL) || params.containsKey("bot"))) {
                    this.buttonState = 1;
                } else {
                    needProgress = false;
                    this.buttonState = -1;
                }
                boolean sending = SendMessagesHelper.getInstance(this.currentAccount).isSendingMessage(this.currentMessageObject.getId());
                if (this.currentPosition != null && sending && this.buttonState == 1) {
                    this.drawRadialCheckBackground = true;
                    this.radialProgress.setCheckBackground(false, animated);
                } else {
                    this.radialProgress.setBackground(getDrawableForCurrentState(), needProgress, animated);
                }
                if (needProgress) {
                    progress = ImageLoader.getInstance().getFileProgress(this.currentMessageObject.messageOwner.attachPath);
                    if (progress == null && sending) {
                        progress = Float.valueOf(1.0f);
                    }
                    this.radialProgress.setProgress(progress != null ? progress.floatValue() : 0.0f, false);
                } else {
                    this.radialProgress.setProgress(0.0f, false);
                }
                invalidate();
            }
        } else if (this.currentPhotoObject != null && this.drawImageButton) {
            if (fileExists) {
                DownloadController.getInstance(this.currentAccount).removeLoadingFileObserver(this);
                if (this.documentAttachType != 2 || this.photoImage.isAllowStartAnimation()) {
                    this.buttonState = -1;
                } else {
                    this.buttonState = 2;
                }
                this.radialProgress.setBackground(getDrawableForCurrentState(), false, animated);
                invalidate();
            } else {
                DownloadController.getInstance(this.currentAccount).addLoadingFileObserver(fileName, this.currentMessageObject, this);
                setProgress = 0.0f;
                progressVisible = false;
                if (FileLoader.getInstance(this.currentAccount).isLoadingFile(fileName)) {
                    progressVisible = true;
                    this.buttonState = 1;
                    progress = ImageLoader.getInstance().getFileProgress(fileName);
                    setProgress = progress != null ? progress.floatValue() : 0.0f;
                } else if (this.cancelLoading || !((this.documentAttachType == 0 && DownloadController.getInstance(this.currentAccount).canDownloadMedia(this.currentMessageObject)) || (this.documentAttachType == 2 && MessageObject.isNewGifDocument(this.documentAttach) && DownloadController.getInstance(this.currentAccount).canDownloadMedia(this.currentMessageObject)))) {
                    this.buttonState = 0;
                } else {
                    progressVisible = true;
                    this.buttonState = 1;
                }
                this.radialProgress.setProgress(setProgress, false);
                this.radialProgress.setBackground(getDrawableForCurrentState(), progressVisible, animated);
                invalidate();
            }
        } else {
            return;
        }
        if (this.hasMiniProgress == 0) {
            this.radialProgress.setMiniBackground(null, false, animated);
        }
    }

    private void didPressedMiniButton(boolean animated) {
        if (this.miniButtonState == 0) {
            this.miniButtonState = 1;
            this.radialProgress.setProgress(0.0f, false);
            if (this.documentAttachType == 3 || this.documentAttachType == 5) {
                FileLoader.getInstance(this.currentAccount).loadFile(this.documentAttach, true, 0);
            } else if (this.documentAttachType == 4) {
                FileLoader.getInstance(this.currentAccount).loadFile(this.documentAttach, true, this.currentMessageObject.shouldEncryptPhotoOrVideo() ? 2 : 0);
            }
            this.radialProgress.setMiniBackground(getMiniDrawableForCurrentState(), true, false);
            invalidate();
        } else if (this.miniButtonState == 1) {
            if ((this.documentAttachType == 3 || this.documentAttachType == 5) && MediaController.getInstance().isPlayingMessage(this.currentMessageObject)) {
                MediaController.getInstance().cleanupPlayer(true, true);
            }
            this.miniButtonState = 0;
            FileLoader.getInstance(this.currentAccount).cancelLoadFile(this.documentAttach);
            this.radialProgress.setMiniBackground(getMiniDrawableForCurrentState(), true, false);
            invalidate();
        }
    }

    private void didPressedButton(boolean animated) {
        if (this.buttonState == 0) {
            if (this.documentAttachType == 3 || this.documentAttachType == 5) {
                if (this.miniButtonState == 0) {
                    FileLoader.getInstance(this.currentAccount).loadFile(this.documentAttach, true, 0);
                }
                if (this.delegate.needPlayMessage(this.currentMessageObject)) {
                    if (this.hasMiniProgress == 2 && this.miniButtonState != 1) {
                        this.miniButtonState = 1;
                        this.radialProgress.setProgress(0.0f, false);
                        this.radialProgress.setMiniBackground(getMiniDrawableForCurrentState(), true, false);
                    }
                    updatePlayingMessageProgress();
                    this.buttonState = 1;
                    this.radialProgress.setBackground(getDrawableForCurrentState(), false, false);
                    invalidate();
                    return;
                }
                return;
            }
            this.cancelLoading = false;
            this.radialProgress.setProgress(0.0f, false);
            if (this.currentMessageObject.type == 1) {
                FileLocation fileLocation;
                int i;
                this.photoImage.setForceLoading(true);
                ImageReceiver imageReceiver = this.photoImage;
                TLObject tLObject = this.currentPhotoObject.location;
                String str = this.currentPhotoFilter;
                if (this.currentPhotoObjectThumb != null) {
                    fileLocation = this.currentPhotoObjectThumb.location;
                } else {
                    fileLocation = null;
                }
                String str2 = this.currentPhotoFilterThumb;
                int i2 = this.currentPhotoObject.size;
                if (this.currentMessageObject.shouldEncryptPhotoOrVideo()) {
                    i = 2;
                } else {
                    i = 0;
                }
                imageReceiver.setImage(tLObject, str, fileLocation, str2, i2, null, i);
            } else if (this.currentMessageObject.type == 8) {
                this.currentMessageObject.gifState = 2.0f;
                this.photoImage.setForceLoading(true);
                this.photoImage.setImage(this.currentMessageObject.messageOwner.media.document, null, this.currentPhotoObject != null ? this.currentPhotoObject.location : null, this.currentPhotoFilterThumb, this.currentMessageObject.messageOwner.media.document.size, null, 0);
            } else if (this.currentMessageObject.isRoundVideo()) {
                if (this.currentMessageObject.isSecretMedia()) {
                    FileLoader.getInstance(this.currentAccount).loadFile(this.currentMessageObject.getDocument(), true, 1);
                } else {
                    this.currentMessageObject.gifState = 2.0f;
                    Document document = this.currentMessageObject.getDocument();
                    this.photoImage.setForceLoading(true);
                    this.photoImage.setImage(document, null, this.currentPhotoObject != null ? this.currentPhotoObject.location : null, this.currentPhotoFilterThumb, document.size, null, 0);
                }
            } else if (this.currentMessageObject.type == 9) {
                FileLoader.getInstance(this.currentAccount).loadFile(this.currentMessageObject.messageOwner.media.document, false, 0);
            } else if (this.documentAttachType == 4) {
                FileLoader.getInstance(this.currentAccount).loadFile(this.documentAttach, true, this.currentMessageObject.shouldEncryptPhotoOrVideo() ? 2 : 0);
            } else if (this.currentMessageObject.type != 0 || this.documentAttachType == 0) {
                this.photoImage.setForceLoading(true);
                this.photoImage.setImage(this.currentPhotoObject.location, this.currentPhotoFilter, this.currentPhotoObjectThumb != null ? this.currentPhotoObjectThumb.location : null, this.currentPhotoFilterThumb, 0, null, 0);
            } else if (this.documentAttachType == 2) {
                this.photoImage.setForceLoading(true);
                this.photoImage.setImage(this.currentMessageObject.messageOwner.media.webpage.document, null, this.currentPhotoObject.location, this.currentPhotoFilterThumb, this.currentMessageObject.messageOwner.media.webpage.document.size, null, 0);
                this.currentMessageObject.gifState = 2.0f;
            } else if (this.documentAttachType == 1) {
                FileLoader.getInstance(this.currentAccount).loadFile(this.currentMessageObject.messageOwner.media.webpage.document, false, 0);
            }
            this.buttonState = 1;
            this.radialProgress.setBackground(getDrawableForCurrentState(), true, animated);
            invalidate();
        } else if (this.buttonState == 1) {
            if (this.documentAttachType == 3 || this.documentAttachType == 5) {
                if (MediaController.getInstance().pauseMessage(this.currentMessageObject)) {
                    this.buttonState = 0;
                    this.radialProgress.setBackground(getDrawableForCurrentState(), false, false);
                    invalidate();
                }
            } else if (!this.currentMessageObject.isOut() || (!this.currentMessageObject.isSending() && !this.currentMessageObject.isEditing())) {
                this.cancelLoading = true;
                if (this.documentAttachType == 4 || this.documentAttachType == 1) {
                    FileLoader.getInstance(this.currentAccount).cancelLoadFile(this.documentAttach);
                } else if (this.currentMessageObject.type == 0 || this.currentMessageObject.type == 1 || this.currentMessageObject.type == 8 || this.currentMessageObject.type == 5) {
                    ImageLoader.getInstance().cancelForceLoadingForImageReceiver(this.photoImage);
                    this.photoImage.cancelLoadImage();
                } else if (this.currentMessageObject.type == 9) {
                    FileLoader.getInstance(this.currentAccount).cancelLoadFile(this.currentMessageObject.messageOwner.media.document);
                }
                this.buttonState = 0;
                this.radialProgress.setBackground(getDrawableForCurrentState(), false, animated);
                invalidate();
            } else if (!this.radialProgress.isDrawCheckDrawable()) {
                this.delegate.didPressedCancelSendButton(this);
            }
        } else if (this.buttonState == 2) {
            if (this.documentAttachType == 3 || this.documentAttachType == 5) {
                this.radialProgress.setProgress(0.0f, false);
                FileLoader.getInstance(this.currentAccount).loadFile(this.documentAttach, true, 0);
                this.buttonState = 4;
                this.radialProgress.setBackground(getDrawableForCurrentState(), true, false);
                invalidate();
                return;
            }
            this.photoImage.setAllowStartAnimation(true);
            this.photoImage.startAnimation();
            this.currentMessageObject.gifState = 0.0f;
            this.buttonState = -1;
            this.radialProgress.setBackground(getDrawableForCurrentState(), false, animated);
        } else if (this.buttonState == 3) {
            if (this.hasMiniProgress == 2 && this.miniButtonState != 1) {
                this.miniButtonState = 1;
                this.radialProgress.setProgress(0.0f, false);
                this.radialProgress.setMiniBackground(getMiniDrawableForCurrentState(), true, false);
            }
            this.delegate.didPressedImage(this);
        } else if (this.buttonState != 4) {
        } else {
            if (this.documentAttachType != 3 && this.documentAttachType != 5) {
                return;
            }
            if ((!this.currentMessageObject.isOut() || (!this.currentMessageObject.isSending() && !this.currentMessageObject.isEditing())) && !this.currentMessageObject.isSendError()) {
                FileLoader.getInstance(this.currentAccount).cancelLoadFile(this.documentAttach);
                this.buttonState = 2;
                this.radialProgress.setBackground(getDrawableForCurrentState(), false, false);
                invalidate();
            } else if (this.delegate != null) {
                this.delegate.didPressedCancelSendButton(this);
            }
        }
    }

    public void onFailedDownload(String fileName) {
        boolean z;
        if (this.documentAttachType == 3 || this.documentAttachType == 5) {
            z = true;
        } else {
            z = false;
        }
        updateButtonState(z, false);
    }

    public void onSuccessDownload(String fileName) {
        if (this.documentAttachType == 3 || this.documentAttachType == 5) {
            updateButtonState(true, false);
            updateWaveform();
            return;
        }
        this.radialProgress.setProgress(1.0f, true);
        if (this.currentMessageObject.type != 0) {
            if (!this.photoNotSet || ((this.currentMessageObject.type == 8 || this.currentMessageObject.type == 5) && this.currentMessageObject.gifState != 1.0f)) {
                if ((this.currentMessageObject.type == 8 || this.currentMessageObject.type == 5) && this.currentMessageObject.gifState != 1.0f) {
                    this.photoNotSet = false;
                    this.buttonState = 2;
                    didPressedButton(true);
                } else {
                    updateButtonState(true, false);
                }
            }
            if (this.photoNotSet) {
                setMessageObject(this.currentMessageObject, this.currentMessagesGroup, this.pinnedBottom, this.pinnedTop);
            }
        } else if (this.documentAttachType == 2 && this.currentMessageObject.gifState != 1.0f) {
            this.buttonState = 2;
            didPressedButton(true);
        } else if (this.photoNotSet) {
            setMessageObject(this.currentMessageObject, this.currentMessagesGroup, this.pinnedBottom, this.pinnedTop);
        } else {
            updateButtonState(true, false);
        }
    }

    public void didSetImage(ImageReceiver imageReceiver, boolean set, boolean thumb) {
        if (this.currentMessageObject == null) {
            return;
        }
        if ((this.currentMessageObject.type == 0 || this.currentMessageObject.type == 1 || this.currentMessageObject.type == 5 || this.currentMessageObject.type == 8) && set && !thumb && !this.currentMessageObject.mediaExists && !this.currentMessageObject.attachPathExists) {
            this.currentMessageObject.mediaExists = true;
            updateButtonState(true, false);
        }
    }

    public void onProgressDownload(String fileName, float progress) {
        this.radialProgress.setProgress(progress, true);
        if (this.documentAttachType == 3 || this.documentAttachType == 5) {
            if (this.hasMiniProgress != 0) {
                if (this.miniButtonState != 1) {
                    updateButtonState(false, false);
                }
            } else if (this.buttonState != 4) {
                updateButtonState(false, false);
            }
        } else if (this.hasMiniProgress != 0) {
            if (this.miniButtonState != 1) {
                updateButtonState(false, false);
            }
        } else if (this.buttonState != 1) {
            updateButtonState(false, false);
        }
    }

    public void onProgressUpload(String fileName, float progress, boolean isEncrypted) {
        this.radialProgress.setProgress(progress, true);
        if (progress == 1.0f && this.currentPosition != null && SendMessagesHelper.getInstance(this.currentAccount).isSendingMessage(this.currentMessageObject.getId()) && this.buttonState == 1) {
            this.drawRadialCheckBackground = true;
            this.radialProgress.setCheckBackground(false, true);
        }
    }

    public void onProvideStructure(ViewStructure structure) {
        super.onProvideStructure(structure);
        if (this.allowAssistant && VERSION.SDK_INT >= 23) {
            if (this.currentMessageObject.messageText != null && this.currentMessageObject.messageText.length() > 0) {
                structure.setText(this.currentMessageObject.messageText);
            } else if (this.currentMessageObject.caption != null && this.currentMessageObject.caption.length() > 0) {
                structure.setText(this.currentMessageObject.caption);
            }
        }
    }

    public void setDelegate(ChatMessageCellDelegate chatMessageCellDelegate) {
        this.delegate = chatMessageCellDelegate;
    }

    public void setAllowAssistant(boolean value) {
        this.allowAssistant = value;
    }

    private void measureTime(MessageObject messageObject) {
        CharSequence signString;
        boolean edited;
        String timeString;
        if (messageObject.messageOwner.post_author != null) {
            signString = messageObject.messageOwner.post_author.replace("\n", TtmlNode.ANONYMOUS_REGION_ID);
        } else if (messageObject.messageOwner.fwd_from != null && messageObject.messageOwner.fwd_from.post_author != null) {
            signString = messageObject.messageOwner.fwd_from.post_author.replace("\n", TtmlNode.ANONYMOUS_REGION_ID);
        } else if (messageObject.isOutOwner() || messageObject.messageOwner.from_id <= 0 || !messageObject.messageOwner.post) {
            signString = null;
        } else {
            User signUser = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(messageObject.messageOwner.from_id));
            if (signUser != null) {
                signString = ContactsController.formatName(signUser.first_name, signUser.last_name).replace('\n', ' ');
            } else {
                signString = null;
            }
        }
        User author = null;
        if (this.currentMessageObject.isFromUser()) {
            author = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(messageObject.messageOwner.from_id));
        }
        if (messageObject.isLiveLocation() || messageObject.getDialogId() == 777000 || messageObject.messageOwner.via_bot_id != 0 || messageObject.messageOwner.via_bot_name != null || (author != null && author.bot)) {
            edited = false;
        } else if (this.currentPosition == null || this.currentMessagesGroup == null) {
            edited = (messageObject.messageOwner.flags & 32768) != 0 || messageObject.isEditing();
        } else {
            edited = false;
            int size = this.currentMessagesGroup.messages.size();
            for (int a = 0; a < size; a++) {
                MessageObject object = (MessageObject) this.currentMessagesGroup.messages.get(a);
                if ((object.messageOwner.flags & 32768) != 0 || object.isEditing()) {
                    edited = true;
                    break;
                }
            }
        }
        if (edited) {
            timeString = LocaleController.getString("EditedMessage", R.string.EditedMessage) + " " + LocaleController.getInstance().formatterDay.format(((long) messageObject.messageOwner.date) * 1000);
        } else {
            timeString = LocaleController.getInstance().formatterDay.format(((long) messageObject.messageOwner.date) * 1000);
        }
        if (signString != null) {
            this.currentTimeString = ", " + timeString;
        } else {
            this.currentTimeString = timeString;
        }
        int ceil = (int) Math.ceil((double) Theme.chat_timePaint.measureText(this.currentTimeString));
        this.timeWidth = ceil;
        this.timeTextWidth = ceil;
        if ((messageObject.messageOwner.flags & 1024) != 0) {
            this.currentViewsString = String.format("%s", new Object[]{LocaleController.formatShortNumber(Math.max(1, messageObject.messageOwner.views), null)});
            this.viewsTextWidth = (int) Math.ceil((double) Theme.chat_timePaint.measureText(this.currentViewsString));
            this.timeWidth += (this.viewsTextWidth + Theme.chat_msgInViewsDrawable.getIntrinsicWidth()) + AndroidUtilities.dp(10.0f);
        }
        if (signString != null) {
            if (this.availableTimeWidth == 0) {
                this.availableTimeWidth = AndroidUtilities.dp(1000.0f);
            }
            int widthForSign = this.availableTimeWidth - this.timeWidth;
            if (messageObject.isOutOwner()) {
                if (messageObject.type == 5) {
                    widthForSign -= AndroidUtilities.dp(20.0f);
                } else {
                    widthForSign -= AndroidUtilities.dp(96.0f);
                }
            }
            int width = (int) Math.ceil((double) Theme.chat_timePaint.measureText(signString, 0, signString.length()));
            if (width > widthForSign) {
                if (widthForSign <= 0) {
                    signString = TtmlNode.ANONYMOUS_REGION_ID;
                    width = 0;
                } else {
                    signString = TextUtils.ellipsize(signString, Theme.chat_timePaint, (float) widthForSign, TruncateAt.END);
                    width = widthForSign;
                }
            }
            this.currentTimeString = signString + this.currentTimeString;
            this.timeTextWidth += width;
            this.timeWidth += width;
        }
    }

    private boolean isDrawSelectedBackground() {
        return (isPressed() && this.isCheckPressed) || ((!this.isCheckPressed && this.isPressed) || this.isHighlighted);
    }

    private boolean isOpenChatByShare(MessageObject messageObject) {
        return (messageObject.messageOwner.fwd_from == null || messageObject.messageOwner.fwd_from.saved_from_peer == null) ? false : true;
    }

    private boolean checkNeedDrawShareButton(MessageObject messageObject) {
        if (this.currentPosition != null && !this.currentPosition.last) {
            return false;
        }
        if (messageObject.eventId != 0) {
            return false;
        }
        if (messageObject.messageOwner.fwd_from != null && !messageObject.isOutOwner() && messageObject.messageOwner.fwd_from.saved_from_peer != null && messageObject.getDialogId() == ((long) UserConfig.getInstance(this.currentAccount).getClientUserId())) {
            this.drwaShareGoIcon = true;
            return true;
        } else if (messageObject.type == 13) {
            return false;
        } else {
            if (messageObject.messageOwner.fwd_from != null && messageObject.messageOwner.fwd_from.channel_id != 0 && !messageObject.isOutOwner()) {
                return true;
            }
            if (messageObject.isFromUser()) {
                if ((messageObject.messageOwner.media instanceof TL_messageMediaEmpty) || messageObject.messageOwner.media == null || ((messageObject.messageOwner.media instanceof TL_messageMediaWebPage) && !(messageObject.messageOwner.media.webpage instanceof TL_webPage))) {
                    return false;
                }
                User user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(messageObject.messageOwner.from_id));
                if (user != null && user.bot) {
                    return true;
                }
                if (!messageObject.isOut()) {
                    if ((messageObject.messageOwner.media instanceof TL_messageMediaGame) || (messageObject.messageOwner.media instanceof TL_messageMediaInvoice)) {
                        return true;
                    }
                    if (messageObject.isMegagroup()) {
                        Chat chat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(messageObject.messageOwner.to_id.channel_id));
                        if (chat == null || chat.username == null || chat.username.length() <= 0 || (messageObject.messageOwner.media instanceof TL_messageMediaContact) || (messageObject.messageOwner.media instanceof TL_messageMediaGeo)) {
                            return false;
                        }
                        return true;
                    }
                }
            } else if ((messageObject.messageOwner.from_id < 0 || messageObject.messageOwner.post) && messageObject.messageOwner.to_id.channel_id != 0 && ((messageObject.messageOwner.via_bot_id == 0 && messageObject.messageOwner.reply_to_msg_id == 0) || messageObject.type != 13)) {
                return true;
            }
            return false;
        }
    }

    public boolean isInsideBackground(float x, float y) {
        return this.currentBackgroundDrawable != null && x >= ((float) (getLeft() + this.backgroundDrawableLeft)) && x <= ((float) ((getLeft() + this.backgroundDrawableLeft) + this.backgroundDrawableRight));
    }

    private void updateCurrentUserAndChat() {
        MessagesController messagesController = MessagesController.getInstance(this.currentAccount);
        MessageFwdHeader fwd_from = this.currentMessageObject.messageOwner.fwd_from;
        int currentUserId = UserConfig.getInstance(this.currentAccount).getClientUserId();
        if (fwd_from != null && fwd_from.channel_id != 0 && this.currentMessageObject.getDialogId() == ((long) currentUserId)) {
            this.currentChat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(fwd_from.channel_id));
        } else if (fwd_from == null || fwd_from.saved_from_peer == null) {
            if (fwd_from != null && fwd_from.from_id != 0 && fwd_from.channel_id == 0 && this.currentMessageObject.getDialogId() == ((long) currentUserId)) {
                this.currentUser = messagesController.getUser(Integer.valueOf(fwd_from.from_id));
            } else if (this.currentMessageObject.isFromUser()) {
                this.currentUser = messagesController.getUser(Integer.valueOf(this.currentMessageObject.messageOwner.from_id));
            } else if (this.currentMessageObject.messageOwner.from_id < 0) {
                this.currentChat = messagesController.getChat(Integer.valueOf(-this.currentMessageObject.messageOwner.from_id));
            } else if (this.currentMessageObject.messageOwner.post) {
                this.currentChat = messagesController.getChat(Integer.valueOf(this.currentMessageObject.messageOwner.to_id.channel_id));
            }
        } else if (fwd_from.saved_from_peer.user_id != 0) {
            if (fwd_from.from_id != 0) {
                this.currentUser = messagesController.getUser(Integer.valueOf(fwd_from.from_id));
            } else {
                this.currentUser = messagesController.getUser(Integer.valueOf(fwd_from.saved_from_peer.user_id));
            }
        } else if (fwd_from.saved_from_peer.channel_id != 0) {
            if (!this.currentMessageObject.isSavedFromMegagroup() || fwd_from.from_id == 0) {
                this.currentChat = messagesController.getChat(Integer.valueOf(fwd_from.saved_from_peer.channel_id));
            } else {
                this.currentUser = messagesController.getUser(Integer.valueOf(fwd_from.from_id));
            }
        } else if (fwd_from.saved_from_peer.chat_id == 0) {
        } else {
            if (fwd_from.from_id != 0) {
                this.currentUser = messagesController.getUser(Integer.valueOf(fwd_from.from_id));
            } else {
                this.currentChat = messagesController.getChat(Integer.valueOf(fwd_from.saved_from_peer.chat_id));
            }
        }
    }

    private void setMessageObjectInternal(MessageObject messageObject) {
        SpannableStringBuilder spannableStringBuilder;
        String name;
        if (!((messageObject.messageOwner.flags & 1024) == 0 || this.currentMessageObject.viewsReloaded)) {
            MessagesController.getInstance(this.currentAccount).addToViewsQueue(this.currentMessageObject.messageOwner);
            this.currentMessageObject.viewsReloaded = true;
        }
        updateCurrentUserAndChat();
        if (this.isAvatarVisible) {
            if (this.currentUser != null) {
                if (this.currentUser.photo != null) {
                    this.currentPhoto = this.currentUser.photo.photo_small;
                } else {
                    this.currentPhoto = null;
                }
                this.avatarDrawable.setInfo(this.currentUser);
            } else if (this.currentChat != null) {
                if (this.currentChat.photo != null) {
                    this.currentPhoto = this.currentChat.photo.photo_small;
                } else {
                    this.currentPhoto = null;
                }
                this.avatarDrawable.setInfo(this.currentChat);
            } else {
                this.currentPhoto = null;
                this.avatarDrawable.setInfo(messageObject.messageOwner.from_id, null, null, false);
            }
            this.avatarImage.setImage(this.currentPhoto, "50_50", this.avatarDrawable, null, 0);
        }
        measureTime(messageObject);
        this.namesOffset = 0;
        String viaUsername = null;
        CharSequence viaString = null;
        if (messageObject.messageOwner.via_bot_id != 0) {
            User botUser = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(messageObject.messageOwner.via_bot_id));
            if (!(botUser == null || botUser.username == null || botUser.username.length() <= 0)) {
                viaUsername = "@" + botUser.username;
                viaString = AndroidUtilities.replaceTags(String.format(" via <b>%s</b>", new Object[]{viaUsername}));
                this.viaWidth = (int) Math.ceil((double) Theme.chat_replyNamePaint.measureText(viaString, 0, viaString.length()));
                this.currentViaBotUser = botUser;
            }
        } else if (messageObject.messageOwner.via_bot_name != null && messageObject.messageOwner.via_bot_name.length() > 0) {
            viaUsername = "@" + messageObject.messageOwner.via_bot_name;
            viaString = AndroidUtilities.replaceTags(String.format(" via <b>%s</b>", new Object[]{viaUsername}));
            this.viaWidth = (int) Math.ceil((double) Theme.chat_replyNamePaint.measureText(viaString, 0, viaString.length()));
        }
        boolean authorName = this.drawName && this.isChat && !this.currentMessageObject.isOutOwner();
        boolean viaBot = (messageObject.messageOwner.fwd_from == null || messageObject.type == 14) && viaUsername != null;
        if (authorName || viaBot) {
            String adminString;
            int adminWidth;
            this.drawNameLayout = true;
            this.nameWidth = getMaxNameWidth();
            if (this.nameWidth < 0) {
                this.nameWidth = AndroidUtilities.dp(100.0f);
            }
            if (this.currentUser == null || this.currentMessageObject.isOutOwner() || this.currentMessageObject.type == 13 || this.currentMessageObject.type == 5 || !this.delegate.isChatAdminCell(this.currentUser.id)) {
                adminString = null;
                adminWidth = 0;
            } else {
                adminString = LocaleController.getString("ChatAdmin", R.string.ChatAdmin);
                adminWidth = (int) Math.ceil((double) Theme.chat_adminPaint.measureText(adminString));
                this.nameWidth -= adminWidth;
            }
            if (!authorName) {
                this.currentNameString = TtmlNode.ANONYMOUS_REGION_ID;
            } else if (this.currentUser != null) {
                this.currentNameString = UserObject.getUserName(this.currentUser);
            } else if (this.currentChat != null) {
                this.currentNameString = this.currentChat.title;
            } else {
                this.currentNameString = "DELETED";
            }
            CharSequence nameStringFinal = TextUtils.ellipsize(this.currentNameString.replace('\n', ' '), Theme.chat_namePaint, (float) (this.nameWidth - (viaBot ? this.viaWidth : 0)), TruncateAt.END);
            if (viaBot) {
                int color;
                this.viaNameWidth = (int) Math.ceil((double) Theme.chat_namePaint.measureText(nameStringFinal, 0, nameStringFinal.length()));
                if (this.viaNameWidth != 0) {
                    this.viaNameWidth += AndroidUtilities.dp(4.0f);
                }
                if (this.currentMessageObject.type == 13 || this.currentMessageObject.type == 5) {
                    color = Theme.getColor(Theme.key_chat_stickerViaBotNameText);
                } else {
                    color = Theme.getColor(this.currentMessageObject.isOutOwner() ? Theme.key_chat_outViaBotNameText : Theme.key_chat_inViaBotNameText);
                }
                if (this.currentNameString.length() > 0) {
                    spannableStringBuilder = new SpannableStringBuilder(String.format("%s via %s", new Object[]{nameStringFinal, viaUsername}));
                    spannableStringBuilder.setSpan(new TypefaceSpan(Typeface.DEFAULT, 0, color), nameStringFinal.length() + 1, nameStringFinal.length() + 4, 33);
                    spannableStringBuilder.setSpan(new TypefaceSpan(AndroidUtilities.getTypeface("fonts/rmedium.ttf"), 0, color), nameStringFinal.length() + 5, spannableStringBuilder.length(), 33);
                    nameStringFinal = spannableStringBuilder;
                } else {
                    spannableStringBuilder = new SpannableStringBuilder(String.format("via %s", new Object[]{viaUsername}));
                    spannableStringBuilder.setSpan(new TypefaceSpan(Typeface.DEFAULT, 0, color), 0, 4, 33);
                    spannableStringBuilder.setSpan(new TypefaceSpan(AndroidUtilities.getTypeface("fonts/rmedium.ttf"), 0, color), 4, spannableStringBuilder.length(), 33);
                    Object nameStringFinal2 = spannableStringBuilder;
                }
                nameStringFinal = TextUtils.ellipsize(nameStringFinal, Theme.chat_namePaint, (float) this.nameWidth, TruncateAt.END);
            }
            try {
                this.nameLayout = new StaticLayout(nameStringFinal, Theme.chat_namePaint, this.nameWidth + AndroidUtilities.dp(2.0f), Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                if (this.nameLayout == null || this.nameLayout.getLineCount() <= 0) {
                    this.nameWidth = 0;
                } else {
                    this.nameWidth = (int) Math.ceil((double) this.nameLayout.getLineWidth(0));
                    if (messageObject.type != 13) {
                        this.namesOffset += AndroidUtilities.dp(19.0f);
                    }
                    this.nameOffsetX = this.nameLayout.getLineLeft(0);
                }
                if (adminString != null) {
                    this.adminLayout = new StaticLayout(adminString, Theme.chat_adminPaint, adminWidth + AndroidUtilities.dp(2.0f), Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                    this.nameWidth = (int) (((float) this.nameWidth) + (this.adminLayout.getLineWidth(0) + ((float) AndroidUtilities.dp(8.0f))));
                } else {
                    this.adminLayout = null;
                }
            } catch (Throwable e) {
                FileLog.e(e);
            }
            if (this.currentNameString.length() == 0) {
                this.currentNameString = null;
            }
        } else {
            this.currentNameString = null;
            this.nameLayout = null;
            this.nameWidth = 0;
        }
        this.currentForwardUser = null;
        this.currentForwardNameString = null;
        this.currentForwardChannel = null;
        this.forwardedNameLayout[0] = null;
        this.forwardedNameLayout[1] = null;
        this.forwardedNameWidth = 0;
        if (this.drawForwardedName && messageObject.needDrawForwarded() && (this.currentPosition == null || this.currentPosition.minY == (byte) 0)) {
            if (messageObject.messageOwner.fwd_from.channel_id != 0) {
                this.currentForwardChannel = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(messageObject.messageOwner.fwd_from.channel_id));
            }
            if (messageObject.messageOwner.fwd_from.from_id != 0) {
                this.currentForwardUser = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(messageObject.messageOwner.fwd_from.from_id));
            }
            if (!(this.currentForwardUser == null && this.currentForwardChannel == null)) {
                String fromString;
                if (this.currentForwardChannel != null) {
                    if (this.currentForwardUser != null) {
                        this.currentForwardNameString = String.format("%s (%s)", new Object[]{this.currentForwardChannel.title, UserObject.getUserName(this.currentForwardUser)});
                    } else {
                        this.currentForwardNameString = this.currentForwardChannel.title;
                    }
                } else if (this.currentForwardUser != null) {
                    this.currentForwardNameString = UserObject.getUserName(this.currentForwardUser);
                }
                this.forwardedNameWidth = getMaxNameWidth();
                String from = LocaleController.getString("From", R.string.From);
                String fromFormattedString = LocaleController.getString("FromFormatted", R.string.FromFormatted);
                int idx = fromFormattedString.indexOf("%1$s");
                name = TextUtils.ellipsize(this.currentForwardNameString.replace('\n', ' '), Theme.chat_replyNamePaint, (float) ((this.forwardedNameWidth - ((int) Math.ceil((double) Theme.chat_forwardNamePaint.measureText(from + " ")))) - this.viaWidth), TruncateAt.END);
                try {
                    fromString = String.format(fromFormattedString, new Object[]{name});
                } catch (Exception e2) {
                    fromString = name.toString();
                }
                if (viaString != null) {
                    spannableStringBuilder = new SpannableStringBuilder(String.format("%s via %s", new Object[]{fromString, viaUsername}));
                    this.viaNameWidth = (int) Math.ceil((double) Theme.chat_forwardNamePaint.measureText(fromString));
                    spannableStringBuilder.setSpan(new TypefaceSpan(AndroidUtilities.getTypeface("fonts/rmedium.ttf")), (spannableStringBuilder.length() - viaUsername.length()) - 1, spannableStringBuilder.length(), 33);
                } else {
                    spannableStringBuilder = new SpannableStringBuilder(String.format(fromFormattedString, new Object[]{name}));
                }
                if (idx >= 0) {
                    stringBuilder.setSpan(new TypefaceSpan(AndroidUtilities.getTypeface("fonts/rmedium.ttf")), idx, name.length() + idx, 33);
                }
                try {
                    this.forwardedNameLayout[1] = new StaticLayout(TextUtils.ellipsize(stringBuilder, Theme.chat_forwardNamePaint, (float) this.forwardedNameWidth, TruncateAt.END), Theme.chat_forwardNamePaint, this.forwardedNameWidth + AndroidUtilities.dp(2.0f), Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                    this.forwardedNameLayout[0] = new StaticLayout(TextUtils.ellipsize(AndroidUtilities.replaceTags(LocaleController.getString("ForwardedMessage", R.string.ForwardedMessage)), Theme.chat_forwardNamePaint, (float) this.forwardedNameWidth, TruncateAt.END), Theme.chat_forwardNamePaint, this.forwardedNameWidth + AndroidUtilities.dp(2.0f), Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                    this.forwardedNameWidth = Math.max((int) Math.ceil((double) this.forwardedNameLayout[0].getLineWidth(0)), (int) Math.ceil((double) this.forwardedNameLayout[1].getLineWidth(0)));
                    this.forwardNameOffsetX[0] = this.forwardedNameLayout[0].getLineLeft(0);
                    this.forwardNameOffsetX[1] = this.forwardedNameLayout[1].getLineLeft(0);
                    if (messageObject.type != 5) {
                        this.namesOffset += AndroidUtilities.dp(36.0f);
                    }
                } catch (Throwable e3) {
                    FileLog.e(e3);
                }
            }
        }
        if (messageObject.hasValidReplyMessageObject() && (this.currentPosition == null || this.currentPosition.minY == (byte) 0)) {
            if (!(messageObject.type == 13 || messageObject.type == 5)) {
                this.namesOffset += AndroidUtilities.dp(42.0f);
                if (messageObject.type != 0) {
                    this.namesOffset += AndroidUtilities.dp(5.0f);
                }
            }
            int maxWidth = getMaxNameWidth();
            if (messageObject.type != 13 && messageObject.type != 5) {
                maxWidth -= AndroidUtilities.dp(10.0f);
            } else if (messageObject.type == 5) {
                maxWidth += AndroidUtilities.dp(13.0f);
            }
            CharSequence stringFinalText = null;
            PhotoSize photoSize = FileLoader.getClosestPhotoSizeWithSize(messageObject.replyMessageObject.photoThumbs2, 80);
            if (photoSize == null) {
                photoSize = FileLoader.getClosestPhotoSizeWithSize(messageObject.replyMessageObject.photoThumbs, 80);
            }
            if (photoSize == null || messageObject.replyMessageObject.type == 13 || ((messageObject.type == 13 && !AndroidUtilities.isTablet()) || messageObject.replyMessageObject.isSecretMedia())) {
                this.replyImageReceiver.setImageBitmap((Drawable) null);
                this.needReplyImage = false;
            } else {
                if (messageObject.replyMessageObject.isRoundVideo()) {
                    this.replyImageReceiver.setRoundRadius(AndroidUtilities.dp(22.0f));
                } else {
                    this.replyImageReceiver.setRoundRadius(0);
                }
                this.currentReplyPhoto = photoSize.location;
                this.replyImageReceiver.setImage(photoSize.location, "50_50", null, null, 1);
                this.needReplyImage = true;
                maxWidth -= AndroidUtilities.dp(44.0f);
            }
            name = null;
            if (messageObject.customReplyName != null) {
                name = messageObject.customReplyName;
            } else if (messageObject.replyMessageObject.isFromUser()) {
                User user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(messageObject.replyMessageObject.messageOwner.from_id));
                if (user != null) {
                    name = UserObject.getUserName(user);
                }
            } else if (messageObject.replyMessageObject.messageOwner.from_id < 0) {
                chat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(-messageObject.replyMessageObject.messageOwner.from_id));
                if (chat != null) {
                    name = chat.title;
                }
            } else {
                chat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(messageObject.replyMessageObject.messageOwner.to_id.channel_id));
                if (chat != null) {
                    name = chat.title;
                }
            }
            if (name == null) {
                name = LocaleController.getString("Loading", R.string.Loading);
            }
            CharSequence stringFinalName = TextUtils.ellipsize(name.replace('\n', ' '), Theme.chat_replyNamePaint, (float) maxWidth, TruncateAt.END);
            if (messageObject.replyMessageObject.messageOwner.media instanceof TL_messageMediaGame) {
                stringFinalText = TextUtils.ellipsize(Emoji.replaceEmoji(messageObject.replyMessageObject.messageOwner.media.game.title, Theme.chat_replyTextPaint.getFontMetricsInt(), AndroidUtilities.dp(14.0f), false), Theme.chat_replyTextPaint, (float) maxWidth, TruncateAt.END);
            } else if (messageObject.replyMessageObject.messageOwner.media instanceof TL_messageMediaInvoice) {
                stringFinalText = TextUtils.ellipsize(Emoji.replaceEmoji(messageObject.replyMessageObject.messageOwner.media.title, Theme.chat_replyTextPaint.getFontMetricsInt(), AndroidUtilities.dp(14.0f), false), Theme.chat_replyTextPaint, (float) maxWidth, TruncateAt.END);
            } else if (messageObject.replyMessageObject.messageText != null && messageObject.replyMessageObject.messageText.length() > 0) {
                String mess = messageObject.replyMessageObject.messageText.toString();
                if (mess.length() > 150) {
                    mess = mess.substring(0, 150);
                }
                stringFinalText = TextUtils.ellipsize(Emoji.replaceEmoji(mess.replace('\n', ' '), Theme.chat_replyTextPaint.getFontMetricsInt(), AndroidUtilities.dp(14.0f), false), Theme.chat_replyTextPaint, (float) maxWidth, TruncateAt.END);
            }
            try {
                this.replyNameWidth = AndroidUtilities.dp((float) ((this.needReplyImage ? 44 : 0) + 4));
                if (stringFinalName != null) {
                    this.replyNameLayout = new StaticLayout(stringFinalName, Theme.chat_replyNamePaint, maxWidth + AndroidUtilities.dp(6.0f), Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                    if (this.replyNameLayout.getLineCount() > 0) {
                        this.replyNameWidth += ((int) Math.ceil((double) this.replyNameLayout.getLineWidth(0))) + AndroidUtilities.dp(8.0f);
                        this.replyNameOffset = this.replyNameLayout.getLineLeft(0);
                    }
                }
            } catch (Throwable e32) {
                FileLog.e(e32);
            }
            try {
                this.replyTextWidth = AndroidUtilities.dp((float) ((this.needReplyImage ? 44 : 0) + 4));
                if (stringFinalText != null) {
                    this.replyTextLayout = new StaticLayout(stringFinalText, Theme.chat_replyTextPaint, maxWidth + AndroidUtilities.dp(6.0f), Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                    if (this.replyTextLayout.getLineCount() > 0) {
                        this.replyTextWidth += ((int) Math.ceil((double) this.replyTextLayout.getLineWidth(0))) + AndroidUtilities.dp(8.0f);
                        this.replyTextOffset = this.replyTextLayout.getLineLeft(0);
                    }
                }
            } catch (Throwable e322) {
                FileLog.e(e322);
            }
        }
        requestLayout();
    }

    public int getCaptionHeight() {
        return this.addedCaptionHeight;
    }

    public ImageReceiver getAvatarImage() {
        return this.isAvatarVisible ? this.avatarImage : null;
    }

    protected void onDraw(Canvas canvas) {
        if (this.currentMessageObject != null) {
            if (this.wasLayout) {
                Drawable currentBackgroundSelectedDrawable;
                Drawable currentBackgroundShadowDrawable;
                int i;
                long newTime;
                long dt;
                if (this.currentMessageObject.isOutOwner()) {
                    Theme.chat_msgTextPaint.setColor(Theme.getColor(Theme.key_chat_messageTextOut));
                    Theme.chat_msgTextPaint.linkColor = Theme.getColor(Theme.key_chat_messageLinkOut);
                    Theme.chat_msgGameTextPaint.setColor(Theme.getColor(Theme.key_chat_messageTextOut));
                    Theme.chat_msgGameTextPaint.linkColor = Theme.getColor(Theme.key_chat_messageLinkOut);
                    Theme.chat_replyTextPaint.linkColor = Theme.getColor(Theme.key_chat_messageLinkOut);
                } else {
                    Theme.chat_msgTextPaint.setColor(Theme.getColor(Theme.key_chat_messageTextIn));
                    Theme.chat_msgTextPaint.linkColor = Theme.getColor(Theme.key_chat_messageLinkIn);
                    Theme.chat_msgGameTextPaint.setColor(Theme.getColor(Theme.key_chat_messageTextIn));
                    Theme.chat_msgGameTextPaint.linkColor = Theme.getColor(Theme.key_chat_messageLinkIn);
                    Theme.chat_replyTextPaint.linkColor = Theme.getColor(Theme.key_chat_messageLinkIn);
                }
                if (this.documentAttach != null) {
                    if (this.documentAttachType == 3) {
                        if (this.currentMessageObject.isOutOwner()) {
                            this.seekBarWaveform.setColors(Theme.getColor(Theme.key_chat_outVoiceSeekbar), Theme.getColor(Theme.key_chat_outVoiceSeekbarFill), Theme.getColor(Theme.key_chat_outVoiceSeekbarSelected));
                            this.seekBar.setColors(Theme.getColor(Theme.key_chat_outAudioSeekbar), Theme.getColor(Theme.key_chat_outAudioCacheSeekbar), Theme.getColor(Theme.key_chat_outAudioSeekbarFill), Theme.getColor(Theme.key_chat_outAudioSeekbarFill), Theme.getColor(Theme.key_chat_outAudioSeekbarSelected));
                        } else {
                            this.seekBarWaveform.setColors(Theme.getColor(Theme.key_chat_inVoiceSeekbar), Theme.getColor(Theme.key_chat_inVoiceSeekbarFill), Theme.getColor(Theme.key_chat_inVoiceSeekbarSelected));
                            this.seekBar.setColors(Theme.getColor(Theme.key_chat_inAudioSeekbar), Theme.getColor(Theme.key_chat_inAudioCacheSeekbar), Theme.getColor(Theme.key_chat_inAudioSeekbarFill), Theme.getColor(Theme.key_chat_inAudioSeekbarFill), Theme.getColor(Theme.key_chat_inAudioSeekbarSelected));
                        }
                    } else if (this.documentAttachType == 5) {
                        this.documentAttachType = 5;
                        if (this.currentMessageObject.isOutOwner()) {
                            this.seekBar.setColors(Theme.getColor(Theme.key_chat_outAudioSeekbar), Theme.getColor(Theme.key_chat_outAudioCacheSeekbar), Theme.getColor(Theme.key_chat_outAudioSeekbarFill), Theme.getColor(Theme.key_chat_outAudioSeekbarFill), Theme.getColor(Theme.key_chat_outAudioSeekbarSelected));
                        } else {
                            this.seekBar.setColors(Theme.getColor(Theme.key_chat_inAudioSeekbar), Theme.getColor(Theme.key_chat_inAudioCacheSeekbar), Theme.getColor(Theme.key_chat_inAudioSeekbarFill), Theme.getColor(Theme.key_chat_inAudioSeekbarFill), Theme.getColor(Theme.key_chat_inAudioSeekbarSelected));
                        }
                    }
                }
                if (this.currentMessageObject.type == 5) {
                    Theme.chat_timePaint.setColor(Theme.getColor(Theme.key_chat_mediaTimeText));
                } else if (this.mediaBackground) {
                    if (this.currentMessageObject.type == 13 || this.currentMessageObject.type == 5) {
                        Theme.chat_timePaint.setColor(Theme.getColor(Theme.key_chat_serviceText));
                    } else {
                        Theme.chat_timePaint.setColor(Theme.getColor(Theme.key_chat_mediaTimeText));
                    }
                } else if (this.currentMessageObject.isOutOwner()) {
                    Theme.chat_timePaint.setColor(Theme.getColor(isDrawSelectedBackground() ? Theme.key_chat_outTimeSelectedText : Theme.key_chat_outTimeText));
                } else {
                    Theme.chat_timePaint.setColor(Theme.getColor(isDrawSelectedBackground() ? Theme.key_chat_inTimeSelectedText : Theme.key_chat_inTimeText));
                }
                int additionalTop = 0;
                int additionalBottom = 0;
                int i2;
                int offsetBottom;
                int backgroundTop;
                if (this.currentMessageObject.isOutOwner()) {
                    if (this.mediaBackground || this.drawPinnedBottom) {
                        this.currentBackgroundDrawable = Theme.chat_msgOutMediaDrawable;
                        currentBackgroundSelectedDrawable = Theme.chat_msgOutMediaSelectedDrawable;
                        currentBackgroundShadowDrawable = Theme.chat_msgOutMediaShadowDrawable;
                    } else {
                        this.currentBackgroundDrawable = Theme.chat_msgOutDrawable;
                        currentBackgroundSelectedDrawable = Theme.chat_msgOutSelectedDrawable;
                        currentBackgroundShadowDrawable = Theme.chat_msgOutShadowDrawable;
                    }
                    this.backgroundDrawableLeft = (this.layoutWidth - this.backgroundWidth) - (!this.mediaBackground ? 0 : AndroidUtilities.dp(9.0f));
                    i = this.backgroundWidth;
                    if (this.mediaBackground) {
                        i2 = 0;
                    } else {
                        i2 = AndroidUtilities.dp(3.0f);
                    }
                    this.backgroundDrawableRight = i - i2;
                    if (!(this.currentMessagesGroup == null || this.currentPosition.edge)) {
                        this.backgroundDrawableRight += AndroidUtilities.dp(10.0f);
                    }
                    int backgroundLeft = this.backgroundDrawableLeft;
                    if (!this.mediaBackground && this.drawPinnedBottom) {
                        this.backgroundDrawableRight -= AndroidUtilities.dp(6.0f);
                    }
                    if (this.currentPosition != null) {
                        if ((this.currentPosition.flags & 2) == 0) {
                            this.backgroundDrawableRight += AndroidUtilities.dp(8.0f);
                        }
                        if ((this.currentPosition.flags & 1) == 0) {
                            backgroundLeft -= AndroidUtilities.dp(8.0f);
                            this.backgroundDrawableRight += AndroidUtilities.dp(8.0f);
                        }
                        if ((this.currentPosition.flags & 4) == 0) {
                            additionalTop = 0 - AndroidUtilities.dp(9.0f);
                            additionalBottom = 0 + AndroidUtilities.dp(9.0f);
                        }
                        if ((this.currentPosition.flags & 8) == 0) {
                            additionalBottom += AndroidUtilities.dp(9.0f);
                        }
                    }
                    if (this.drawPinnedBottom && this.drawPinnedTop) {
                        offsetBottom = 0;
                    } else if (this.drawPinnedBottom) {
                        offsetBottom = AndroidUtilities.dp(1.0f);
                    } else {
                        offsetBottom = AndroidUtilities.dp(2.0f);
                    }
                    i2 = (this.drawPinnedTop || (this.drawPinnedTop && this.drawPinnedBottom)) ? 0 : AndroidUtilities.dp(1.0f);
                    backgroundTop = additionalTop + i2;
                    BaseCell.setDrawableBounds(this.currentBackgroundDrawable, backgroundLeft, backgroundTop, this.backgroundDrawableRight, (this.layoutHeight - offsetBottom) + additionalBottom);
                    BaseCell.setDrawableBounds(currentBackgroundSelectedDrawable, backgroundLeft, backgroundTop, this.backgroundDrawableRight, (this.layoutHeight - offsetBottom) + additionalBottom);
                    BaseCell.setDrawableBounds(currentBackgroundShadowDrawable, backgroundLeft, backgroundTop, this.backgroundDrawableRight, (this.layoutHeight - offsetBottom) + additionalBottom);
                } else {
                    if (this.mediaBackground || this.drawPinnedBottom) {
                        this.currentBackgroundDrawable = Theme.chat_msgInMediaDrawable;
                        currentBackgroundSelectedDrawable = Theme.chat_msgInMediaSelectedDrawable;
                        currentBackgroundShadowDrawable = Theme.chat_msgInMediaShadowDrawable;
                    } else {
                        this.currentBackgroundDrawable = Theme.chat_msgInDrawable;
                        currentBackgroundSelectedDrawable = Theme.chat_msgInSelectedDrawable;
                        currentBackgroundShadowDrawable = Theme.chat_msgInShadowDrawable;
                    }
                    i2 = (this.isChat && this.isAvatarVisible) ? 48 : 0;
                    this.backgroundDrawableLeft = AndroidUtilities.dp((float) (i2 + (!this.mediaBackground ? 3 : 9)));
                    i = this.backgroundWidth;
                    if (this.mediaBackground) {
                        i2 = 0;
                    } else {
                        i2 = AndroidUtilities.dp(3.0f);
                    }
                    this.backgroundDrawableRight = i - i2;
                    if (this.currentMessagesGroup != null) {
                        if (!this.currentPosition.edge) {
                            this.backgroundDrawableLeft -= AndroidUtilities.dp(10.0f);
                            this.backgroundDrawableRight += AndroidUtilities.dp(10.0f);
                        }
                        if (this.currentPosition.leftSpanOffset != 0) {
                            this.backgroundDrawableLeft += (int) Math.ceil((double) ((((float) this.currentPosition.leftSpanOffset) / 1000.0f) * ((float) getGroupPhotosWidth())));
                        }
                    }
                    if (!this.mediaBackground && this.drawPinnedBottom) {
                        this.backgroundDrawableRight -= AndroidUtilities.dp(6.0f);
                        this.backgroundDrawableLeft += AndroidUtilities.dp(6.0f);
                    }
                    if (this.currentPosition != null) {
                        if ((this.currentPosition.flags & 2) == 0) {
                            this.backgroundDrawableRight += AndroidUtilities.dp(8.0f);
                        }
                        if ((this.currentPosition.flags & 1) == 0) {
                            this.backgroundDrawableLeft -= AndroidUtilities.dp(8.0f);
                            this.backgroundDrawableRight += AndroidUtilities.dp(8.0f);
                        }
                        if ((this.currentPosition.flags & 4) == 0) {
                            additionalTop = 0 - AndroidUtilities.dp(9.0f);
                            additionalBottom = 0 + AndroidUtilities.dp(9.0f);
                        }
                        if ((this.currentPosition.flags & 8) == 0) {
                            additionalBottom += AndroidUtilities.dp(10.0f);
                        }
                    }
                    if (this.drawPinnedBottom && this.drawPinnedTop) {
                        offsetBottom = 0;
                    } else if (this.drawPinnedBottom) {
                        offsetBottom = AndroidUtilities.dp(1.0f);
                    } else {
                        offsetBottom = AndroidUtilities.dp(2.0f);
                    }
                    i2 = (this.drawPinnedTop || (this.drawPinnedTop && this.drawPinnedBottom)) ? 0 : AndroidUtilities.dp(1.0f);
                    backgroundTop = additionalTop + i2;
                    BaseCell.setDrawableBounds(this.currentBackgroundDrawable, this.backgroundDrawableLeft, backgroundTop, this.backgroundDrawableRight, (this.layoutHeight - offsetBottom) + additionalBottom);
                    BaseCell.setDrawableBounds(currentBackgroundSelectedDrawable, this.backgroundDrawableLeft, backgroundTop, this.backgroundDrawableRight, (this.layoutHeight - offsetBottom) + additionalBottom);
                    BaseCell.setDrawableBounds(currentBackgroundShadowDrawable, this.backgroundDrawableLeft, backgroundTop, this.backgroundDrawableRight, (this.layoutHeight - offsetBottom) + additionalBottom);
                }
                if (this.drawBackground && this.currentBackgroundDrawable != null) {
                    if (this.isHighlightedAnimated) {
                        float alpha;
                        this.currentBackgroundDrawable.draw(canvas);
                        if (this.highlightProgress >= 300) {
                            alpha = 1.0f;
                        } else {
                            alpha = ((float) this.highlightProgress) / 300.0f;
                        }
                        if (this.currentPosition == null) {
                            currentBackgroundSelectedDrawable.setAlpha((int) (255.0f * alpha));
                            currentBackgroundSelectedDrawable.draw(canvas);
                        }
                        newTime = System.currentTimeMillis();
                        dt = Math.abs(newTime - this.lastHighlightProgressTime);
                        if (dt > 17) {
                            dt = 17;
                        }
                        this.highlightProgress = (int) (((long) this.highlightProgress) - dt);
                        this.lastHighlightProgressTime = newTime;
                        if (this.highlightProgress <= 0) {
                            this.highlightProgress = 0;
                            this.isHighlightedAnimated = false;
                        }
                        invalidate();
                    } else if (!isDrawSelectedBackground() || (this.currentPosition != null && getBackground() == null)) {
                        this.currentBackgroundDrawable.draw(canvas);
                    } else {
                        currentBackgroundSelectedDrawable.setAlpha(255);
                        currentBackgroundSelectedDrawable.draw(canvas);
                    }
                    if (this.currentPosition == null || this.currentPosition.flags != 0) {
                        currentBackgroundShadowDrawable.draw(canvas);
                    }
                }
                drawContent(canvas);
                if (this.drawShareButton) {
                    Theme.chat_shareDrawable.setColorFilter(this.sharePressed ? Theme.colorPressedFilter : Theme.colorFilter);
                    if (this.currentMessageObject.isOutOwner()) {
                        this.shareStartX = (this.currentBackgroundDrawable.getBounds().left - AndroidUtilities.dp(8.0f)) - Theme.chat_shareDrawable.getIntrinsicWidth();
                    } else {
                        this.shareStartX = this.currentBackgroundDrawable.getBounds().right + AndroidUtilities.dp(8.0f);
                    }
                    Drawable drawable = Theme.chat_shareDrawable;
                    i = this.shareStartX;
                    int dp = this.layoutHeight - AndroidUtilities.dp(41.0f);
                    this.shareStartY = dp;
                    BaseCell.setDrawableBounds(drawable, i, dp);
                    Theme.chat_shareDrawable.draw(canvas);
                    if (this.drwaShareGoIcon) {
                        BaseCell.setDrawableBounds(Theme.chat_goIconDrawable, this.shareStartX + AndroidUtilities.dp(12.0f), this.shareStartY + AndroidUtilities.dp(9.0f));
                        Theme.chat_goIconDrawable.draw(canvas);
                    } else {
                        BaseCell.setDrawableBounds(Theme.chat_shareIconDrawable, this.shareStartX + AndroidUtilities.dp(9.0f), this.shareStartY + AndroidUtilities.dp(9.0f));
                        Theme.chat_shareIconDrawable.draw(canvas);
                    }
                }
                if (this.currentPosition == null) {
                    drawNamesLayout(canvas);
                }
                if ((this.drawTime || !this.mediaBackground) && !this.forceNotDrawTime) {
                    drawTimeLayout(canvas);
                }
                if (this.controlsAlpha != 1.0f || this.timeAlpha != 1.0f) {
                    newTime = System.currentTimeMillis();
                    dt = Math.abs(this.lastControlsAlphaChangeTime - newTime);
                    if (dt > 17) {
                        dt = 17;
                    }
                    this.totalChangeTime += dt;
                    if (this.totalChangeTime > 100) {
                        this.totalChangeTime = 100;
                    }
                    this.lastControlsAlphaChangeTime = newTime;
                    if (this.controlsAlpha != 1.0f) {
                        this.controlsAlpha = AndroidUtilities.decelerateInterpolator.getInterpolation(((float) this.totalChangeTime) / 100.0f);
                    }
                    if (this.timeAlpha != 1.0f) {
                        this.timeAlpha = AndroidUtilities.decelerateInterpolator.getInterpolation(((float) this.totalChangeTime) / 100.0f);
                    }
                    invalidate();
                    if (this.forceNotDrawTime && this.currentPosition != null && this.currentPosition.last && getParent() != null) {
                        ((View) getParent()).invalidate();
                        return;
                    }
                    return;
                }
                return;
            }
            requestLayout();
        }
    }

    public int getBackgroundDrawableLeft() {
        int i = 0;
        if (this.currentMessageObject.isOutOwner()) {
            int i2 = this.layoutWidth - this.backgroundWidth;
            if (this.mediaBackground) {
                i = AndroidUtilities.dp(9.0f);
            }
            return i2 - i;
        }
        if (this.isChat && this.isAvatarVisible) {
            i = 48;
        }
        return AndroidUtilities.dp((float) (i + (!this.mediaBackground ? 3 : 9)));
    }

    public boolean hasNameLayout() {
        return (this.drawNameLayout && this.nameLayout != null) || ((this.drawForwardedName && this.forwardedNameLayout[0] != null && this.forwardedNameLayout[1] != null && (this.currentPosition == null || (this.currentPosition.minY == (byte) 0 && this.currentPosition.minX == (byte) 0))) || this.replyNameLayout != null);
    }

    public void drawNamesLayout(Canvas canvas) {
        float f;
        int i;
        float f2 = 11.0f;
        int i2 = 0;
        if (this.drawNameLayout && this.nameLayout != null) {
            canvas.save();
            if (this.currentMessageObject.type == 13 || this.currentMessageObject.type == 5) {
                Theme.chat_namePaint.setColor(Theme.getColor(Theme.key_chat_stickerNameText));
                if (this.currentMessageObject.isOutOwner()) {
                    this.nameX = (float) AndroidUtilities.dp(28.0f);
                } else {
                    this.nameX = (float) ((this.backgroundDrawableLeft + this.backgroundDrawableRight) + AndroidUtilities.dp(22.0f));
                }
                this.nameY = (float) (this.layoutHeight - AndroidUtilities.dp(38.0f));
                Theme.chat_systemDrawable.setColorFilter(Theme.colorFilter);
                Theme.chat_systemDrawable.setBounds(((int) this.nameX) - AndroidUtilities.dp(12.0f), ((int) this.nameY) - AndroidUtilities.dp(5.0f), (((int) this.nameX) + AndroidUtilities.dp(12.0f)) + this.nameWidth, ((int) this.nameY) + AndroidUtilities.dp(22.0f));
                Theme.chat_systemDrawable.draw(canvas);
            } else {
                if (this.mediaBackground || this.currentMessageObject.isOutOwner()) {
                    this.nameX = ((float) (this.backgroundDrawableLeft + AndroidUtilities.dp(11.0f))) - this.nameOffsetX;
                } else {
                    int i3 = this.backgroundDrawableLeft;
                    f = (this.mediaBackground || !this.drawPinnedBottom) ? 17.0f : 11.0f;
                    this.nameX = ((float) (AndroidUtilities.dp(f) + i3)) - this.nameOffsetX;
                }
                if (this.currentUser != null) {
                    Theme.chat_namePaint.setColor(AvatarDrawable.getNameColorForId(this.currentUser.id));
                } else if (this.currentChat == null) {
                    Theme.chat_namePaint.setColor(AvatarDrawable.getNameColorForId(0));
                } else if (!ChatObject.isChannel(this.currentChat) || this.currentChat.megagroup) {
                    Theme.chat_namePaint.setColor(AvatarDrawable.getNameColorForId(this.currentChat.id));
                } else {
                    Theme.chat_namePaint.setColor(AvatarDrawable.getNameColorForId(5));
                }
                if (this.drawPinnedTop) {
                    f = 9.0f;
                } else {
                    f = 10.0f;
                }
                this.nameY = (float) AndroidUtilities.dp(f);
            }
            canvas.translate(this.nameX, this.nameY);
            this.nameLayout.draw(canvas);
            canvas.restore();
            if (this.adminLayout != null) {
                Theme.chat_adminPaint.setColor(Theme.getColor(isDrawSelectedBackground() ? Theme.key_chat_adminSelectedText : Theme.key_chat_adminText));
                canvas.save();
                canvas.translate(((float) ((this.backgroundDrawableLeft + this.backgroundDrawableRight) - AndroidUtilities.dp(11.0f))) - this.adminLayout.getLineWidth(0), this.nameY + ((float) AndroidUtilities.dp(0.5f)));
                this.adminLayout.draw(canvas);
                canvas.restore();
            }
        }
        if (this.drawForwardedName && this.forwardedNameLayout[0] != null && this.forwardedNameLayout[1] != null && (this.currentPosition == null || (this.currentPosition.minY == (byte) 0 && this.currentPosition.minX == (byte) 0))) {
            if (this.currentMessageObject.type == 5) {
                Theme.chat_forwardNamePaint.setColor(Theme.getColor(Theme.key_chat_stickerReplyNameText));
                if (this.currentMessageObject.isOutOwner()) {
                    this.forwardNameX = AndroidUtilities.dp(23.0f);
                } else {
                    this.forwardNameX = (this.backgroundDrawableLeft + this.backgroundDrawableRight) + AndroidUtilities.dp(17.0f);
                }
                this.forwardNameY = AndroidUtilities.dp(12.0f);
                int backWidth = this.forwardedNameWidth + AndroidUtilities.dp(14.0f);
                Theme.chat_systemDrawable.setColorFilter(Theme.colorFilter);
                Theme.chat_systemDrawable.setBounds(this.forwardNameX - AndroidUtilities.dp(7.0f), this.forwardNameY - AndroidUtilities.dp(6.0f), (this.forwardNameX - AndroidUtilities.dp(7.0f)) + backWidth, this.forwardNameY + AndroidUtilities.dp(38.0f));
                Theme.chat_systemDrawable.draw(canvas);
            } else {
                if (this.drawNameLayout) {
                    i = 19;
                } else {
                    i = 0;
                }
                this.forwardNameY = AndroidUtilities.dp((float) (i + 10));
                if (this.currentMessageObject.isOutOwner()) {
                    Theme.chat_forwardNamePaint.setColor(Theme.getColor(Theme.key_chat_outForwardedNameText));
                    this.forwardNameX = this.backgroundDrawableLeft + AndroidUtilities.dp(11.0f);
                } else {
                    Theme.chat_forwardNamePaint.setColor(Theme.getColor(Theme.key_chat_inForwardedNameText));
                    if (this.mediaBackground) {
                        this.forwardNameX = this.backgroundDrawableLeft + AndroidUtilities.dp(11.0f);
                    } else {
                        i = this.backgroundDrawableLeft;
                        if (this.mediaBackground || !this.drawPinnedBottom) {
                            f2 = 17.0f;
                        }
                        this.forwardNameX = i + AndroidUtilities.dp(f2);
                    }
                }
            }
            for (int a = 0; a < 2; a++) {
                canvas.save();
                canvas.translate(((float) this.forwardNameX) - this.forwardNameOffsetX[a], (float) (this.forwardNameY + (AndroidUtilities.dp(16.0f) * a)));
                this.forwardedNameLayout[a].draw(canvas);
                canvas.restore();
            }
        }
        if (this.replyNameLayout != null) {
            if (this.currentMessageObject.type == 13 || this.currentMessageObject.type == 5) {
                Theme.chat_replyLinePaint.setColor(Theme.getColor(Theme.key_chat_stickerReplyLine));
                Theme.chat_replyNamePaint.setColor(Theme.getColor(Theme.key_chat_stickerReplyNameText));
                Theme.chat_replyTextPaint.setColor(Theme.getColor(Theme.key_chat_stickerReplyMessageText));
                if (this.currentMessageObject.isOutOwner()) {
                    this.replyStartX = AndroidUtilities.dp(23.0f);
                } else if (this.currentMessageObject.type == 5) {
                    this.replyStartX = (this.backgroundDrawableLeft + this.backgroundDrawableRight) + AndroidUtilities.dp(4.0f);
                } else {
                    this.replyStartX = (this.backgroundDrawableLeft + this.backgroundDrawableRight) + AndroidUtilities.dp(17.0f);
                }
                this.replyStartY = AndroidUtilities.dp(12.0f);
                backWidth = Math.max(this.replyNameWidth, this.replyTextWidth) + AndroidUtilities.dp(14.0f);
                Theme.chat_systemDrawable.setColorFilter(Theme.colorFilter);
                Theme.chat_systemDrawable.setBounds(this.replyStartX - AndroidUtilities.dp(7.0f), this.replyStartY - AndroidUtilities.dp(6.0f), (this.replyStartX - AndroidUtilities.dp(7.0f)) + backWidth, this.replyStartY + AndroidUtilities.dp(41.0f));
                Theme.chat_systemDrawable.draw(canvas);
            } else {
                int i4;
                if (this.currentMessageObject.isOutOwner()) {
                    Theme.chat_replyLinePaint.setColor(Theme.getColor(Theme.key_chat_outReplyLine));
                    Theme.chat_replyNamePaint.setColor(Theme.getColor(Theme.key_chat_outReplyNameText));
                    if (!this.currentMessageObject.hasValidReplyMessageObject() || this.currentMessageObject.replyMessageObject.type != 0 || (this.currentMessageObject.replyMessageObject.messageOwner.media instanceof TL_messageMediaGame) || (this.currentMessageObject.replyMessageObject.messageOwner.media instanceof TL_messageMediaInvoice)) {
                        Theme.chat_replyTextPaint.setColor(Theme.getColor(isDrawSelectedBackground() ? Theme.key_chat_outReplyMediaMessageSelectedText : Theme.key_chat_outReplyMediaMessageText));
                    } else {
                        Theme.chat_replyTextPaint.setColor(Theme.getColor(Theme.key_chat_outReplyMessageText));
                    }
                    this.replyStartX = this.backgroundDrawableLeft + AndroidUtilities.dp(12.0f);
                } else {
                    Theme.chat_replyLinePaint.setColor(Theme.getColor(Theme.key_chat_inReplyLine));
                    Theme.chat_replyNamePaint.setColor(Theme.getColor(Theme.key_chat_inReplyNameText));
                    if (!this.currentMessageObject.hasValidReplyMessageObject() || this.currentMessageObject.replyMessageObject.type != 0 || (this.currentMessageObject.replyMessageObject.messageOwner.media instanceof TL_messageMediaGame) || (this.currentMessageObject.replyMessageObject.messageOwner.media instanceof TL_messageMediaInvoice)) {
                        Theme.chat_replyTextPaint.setColor(Theme.getColor(isDrawSelectedBackground() ? Theme.key_chat_inReplyMediaMessageSelectedText : Theme.key_chat_inReplyMediaMessageText));
                    } else {
                        Theme.chat_replyTextPaint.setColor(Theme.getColor(Theme.key_chat_inReplyMessageText));
                    }
                    if (this.mediaBackground) {
                        this.replyStartX = this.backgroundDrawableLeft + AndroidUtilities.dp(12.0f);
                    } else {
                        i4 = this.backgroundDrawableLeft;
                        f = (this.mediaBackground || !this.drawPinnedBottom) ? 18.0f : 12.0f;
                        this.replyStartX = AndroidUtilities.dp(f) + i4;
                    }
                }
                if (!this.drawForwardedName || this.forwardedNameLayout[0] == null) {
                    i = 0;
                } else {
                    i = 36;
                }
                i4 = i + 12;
                if (!this.drawNameLayout || this.nameLayout == null) {
                    i = 0;
                } else {
                    i = 20;
                }
                this.replyStartY = AndroidUtilities.dp((float) (i + i4));
            }
            if (this.currentPosition == null || (this.currentPosition.minY == (byte) 0 && this.currentPosition.minX == (byte) 0)) {
                canvas.drawRect((float) this.replyStartX, (float) this.replyStartY, (float) (this.replyStartX + AndroidUtilities.dp(2.0f)), (float) (this.replyStartY + AndroidUtilities.dp(35.0f)), Theme.chat_replyLinePaint);
                if (this.needReplyImage) {
                    this.replyImageReceiver.setImageCoords(this.replyStartX + AndroidUtilities.dp(10.0f), this.replyStartY, AndroidUtilities.dp(35.0f), AndroidUtilities.dp(35.0f));
                    this.replyImageReceiver.draw(canvas);
                }
                if (this.replyNameLayout != null) {
                    canvas.save();
                    canvas.translate(((float) AndroidUtilities.dp((float) ((this.needReplyImage ? 44 : 0) + 10))) + (((float) this.replyStartX) - this.replyNameOffset), (float) this.replyStartY);
                    this.replyNameLayout.draw(canvas);
                    canvas.restore();
                }
                if (this.replyTextLayout != null) {
                    canvas.save();
                    f = ((float) this.replyStartX) - this.replyTextOffset;
                    if (this.needReplyImage) {
                        i2 = 44;
                    }
                    canvas.translate(f + ((float) AndroidUtilities.dp((float) (i2 + 10))), (float) (this.replyStartY + AndroidUtilities.dp(19.0f)));
                    this.replyTextLayout.draw(canvas);
                    canvas.restore();
                }
            }
        }
    }

    public boolean hasCaptionLayout() {
        return this.captionLayout != null;
    }

    public void drawCaptionLayout(Canvas canvas, boolean selectionOnly) {
        if (this.captionLayout == null) {
            return;
        }
        if (!selectionOnly || this.pressedLink != null) {
            canvas.save();
            canvas.translate((float) this.captionX, (float) this.captionY);
            if (this.pressedLink != null) {
                for (int b = 0; b < this.urlPath.size(); b++) {
                    canvas.drawPath((Path) this.urlPath.get(b), Theme.chat_urlPaint);
                }
            }
            if (!selectionOnly) {
                try {
                    this.captionLayout.draw(canvas);
                } catch (Throwable e) {
                    FileLog.e(e);
                }
            }
            canvas.restore();
        }
    }

    public void drawTimeLayout(Canvas canvas) {
        if (((this.drawTime && !this.groupPhotoInvisible) || !this.mediaBackground || this.captionLayout != null) && this.timeLayout != null) {
            int x;
            int y;
            if (this.currentMessageObject.type == 5) {
                Theme.chat_timePaint.setColor(Theme.getColor(Theme.key_chat_mediaTimeText));
            } else if (this.mediaBackground && this.captionLayout == null) {
                if (this.currentMessageObject.type == 13 || this.currentMessageObject.type == 5) {
                    Theme.chat_timePaint.setColor(Theme.getColor(Theme.key_chat_serviceText));
                } else {
                    Theme.chat_timePaint.setColor(Theme.getColor(Theme.key_chat_mediaTimeText));
                }
            } else if (this.currentMessageObject.isOutOwner()) {
                Theme.chat_timePaint.setColor(Theme.getColor(isDrawSelectedBackground() ? Theme.key_chat_outTimeSelectedText : Theme.key_chat_outTimeText));
            } else {
                Theme.chat_timePaint.setColor(Theme.getColor(isDrawSelectedBackground() ? Theme.key_chat_inTimeSelectedText : Theme.key_chat_inTimeText));
            }
            if (this.drawPinnedBottom) {
                canvas.translate(0.0f, (float) AndroidUtilities.dp(2.0f));
            }
            int additionalX;
            Drawable viewsDrawable;
            if (this.mediaBackground && this.captionLayout == null) {
                Paint paint;
                if (this.currentMessageObject.type == 13 || this.currentMessageObject.type == 5) {
                    paint = Theme.chat_actionBackgroundPaint;
                } else {
                    paint = Theme.chat_timeBackgroundPaint;
                }
                int oldAlpha = paint.getAlpha();
                paint.setAlpha((int) (((float) oldAlpha) * this.timeAlpha));
                Theme.chat_timePaint.setAlpha((int) (255.0f * this.timeAlpha));
                int x1 = this.timeX - AndroidUtilities.dp(4.0f);
                int y1 = this.layoutHeight - AndroidUtilities.dp(28.0f);
                this.rect.set((float) x1, (float) y1, (float) (AndroidUtilities.dp((float) ((this.currentMessageObject.isOutOwner() ? 20 : 0) + 8)) + (x1 + this.timeWidth)), (float) (AndroidUtilities.dp(17.0f) + y1));
                canvas.drawRoundRect(this.rect, (float) AndroidUtilities.dp(4.0f), (float) AndroidUtilities.dp(4.0f), paint);
                paint.setAlpha(oldAlpha);
                additionalX = (int) (-this.timeLayout.getLineLeft(0));
                if ((this.currentMessageObject.messageOwner.flags & 1024) != 0) {
                    additionalX += (int) (((float) this.timeWidth) - this.timeLayout.getLineWidth(0));
                    if (this.currentMessageObject.isSending() || this.currentMessageObject.isEditing()) {
                        if (!this.currentMessageObject.isOutOwner()) {
                            BaseCell.setDrawableBounds(Theme.chat_msgMediaClockDrawable, this.timeX + AndroidUtilities.dp(11.0f), (this.layoutHeight - AndroidUtilities.dp(14.0f)) - Theme.chat_msgMediaClockDrawable.getIntrinsicHeight());
                            Theme.chat_msgMediaClockDrawable.draw(canvas);
                        }
                    } else if (!this.currentMessageObject.isSendError()) {
                        if (this.currentMessageObject.type == 13 || this.currentMessageObject.type == 5) {
                            viewsDrawable = Theme.chat_msgStickerViewsDrawable;
                        } else {
                            viewsDrawable = Theme.chat_msgMediaViewsDrawable;
                        }
                        oldAlpha = ((BitmapDrawable) viewsDrawable).getPaint().getAlpha();
                        viewsDrawable.setAlpha((int) (this.timeAlpha * ((float) oldAlpha)));
                        BaseCell.setDrawableBounds(viewsDrawable, this.timeX, (this.layoutHeight - AndroidUtilities.dp(10.5f)) - this.timeLayout.getHeight());
                        viewsDrawable.draw(canvas);
                        viewsDrawable.setAlpha(oldAlpha);
                        if (this.viewsLayout != null) {
                            canvas.save();
                            canvas.translate((float) ((this.timeX + viewsDrawable.getIntrinsicWidth()) + AndroidUtilities.dp(3.0f)), (float) ((this.layoutHeight - AndroidUtilities.dp(12.3f)) - this.timeLayout.getHeight()));
                            this.viewsLayout.draw(canvas);
                            canvas.restore();
                        }
                    } else if (!this.currentMessageObject.isOutOwner()) {
                        x = this.timeX + AndroidUtilities.dp(11.0f);
                        y = this.layoutHeight - AndroidUtilities.dp(27.5f);
                        this.rect.set((float) x, (float) y, (float) (AndroidUtilities.dp(14.0f) + x), (float) (AndroidUtilities.dp(14.0f) + y));
                        canvas.drawRoundRect(this.rect, (float) AndroidUtilities.dp(1.0f), (float) AndroidUtilities.dp(1.0f), Theme.chat_msgErrorPaint);
                        BaseCell.setDrawableBounds(Theme.chat_msgErrorDrawable, AndroidUtilities.dp(6.0f) + x, AndroidUtilities.dp(2.0f) + y);
                        Theme.chat_msgErrorDrawable.draw(canvas);
                    }
                }
                canvas.save();
                canvas.translate((float) (this.timeX + additionalX), (float) ((this.layoutHeight - AndroidUtilities.dp(12.3f)) - this.timeLayout.getHeight()));
                this.timeLayout.draw(canvas);
                canvas.restore();
                Theme.chat_timePaint.setAlpha(255);
            } else {
                additionalX = (int) (-this.timeLayout.getLineLeft(0));
                if ((this.currentMessageObject.messageOwner.flags & 1024) != 0) {
                    additionalX += (int) (((float) this.timeWidth) - this.timeLayout.getLineWidth(0));
                    if (this.currentMessageObject.isSending() || this.currentMessageObject.isEditing()) {
                        if (!this.currentMessageObject.isOutOwner()) {
                            Drawable clockDrawable = isDrawSelectedBackground() ? Theme.chat_msgInSelectedClockDrawable : Theme.chat_msgInClockDrawable;
                            BaseCell.setDrawableBounds(clockDrawable, this.timeX + AndroidUtilities.dp(11.0f), (this.layoutHeight - AndroidUtilities.dp(8.5f)) - clockDrawable.getIntrinsicHeight());
                            clockDrawable.draw(canvas);
                        }
                    } else if (!this.currentMessageObject.isSendError()) {
                        if (this.currentMessageObject.isOutOwner()) {
                            viewsDrawable = isDrawSelectedBackground() ? Theme.chat_msgOutViewsSelectedDrawable : Theme.chat_msgOutViewsDrawable;
                            BaseCell.setDrawableBounds(viewsDrawable, this.timeX, (this.layoutHeight - AndroidUtilities.dp(4.5f)) - this.timeLayout.getHeight());
                            viewsDrawable.draw(canvas);
                        } else {
                            viewsDrawable = isDrawSelectedBackground() ? Theme.chat_msgInViewsSelectedDrawable : Theme.chat_msgInViewsDrawable;
                            BaseCell.setDrawableBounds(viewsDrawable, this.timeX, (this.layoutHeight - AndroidUtilities.dp(4.5f)) - this.timeLayout.getHeight());
                            viewsDrawable.draw(canvas);
                        }
                        if (this.viewsLayout != null) {
                            canvas.save();
                            canvas.translate((float) ((this.timeX + Theme.chat_msgInViewsDrawable.getIntrinsicWidth()) + AndroidUtilities.dp(3.0f)), (float) ((this.layoutHeight - AndroidUtilities.dp(6.5f)) - this.timeLayout.getHeight()));
                            this.viewsLayout.draw(canvas);
                            canvas.restore();
                        }
                    } else if (!this.currentMessageObject.isOutOwner()) {
                        x = this.timeX + AndroidUtilities.dp(11.0f);
                        y = this.layoutHeight - AndroidUtilities.dp(20.5f);
                        this.rect.set((float) x, (float) y, (float) (AndroidUtilities.dp(14.0f) + x), (float) (AndroidUtilities.dp(14.0f) + y));
                        canvas.drawRoundRect(this.rect, (float) AndroidUtilities.dp(1.0f), (float) AndroidUtilities.dp(1.0f), Theme.chat_msgErrorPaint);
                        BaseCell.setDrawableBounds(Theme.chat_msgErrorDrawable, AndroidUtilities.dp(6.0f) + x, AndroidUtilities.dp(2.0f) + y);
                        Theme.chat_msgErrorDrawable.draw(canvas);
                    }
                }
                canvas.save();
                canvas.translate((float) (this.timeX + additionalX), (float) ((this.layoutHeight - AndroidUtilities.dp(6.5f)) - this.timeLayout.getHeight()));
                this.timeLayout.draw(canvas);
                canvas.restore();
            }
            if (this.currentMessageObject.isOutOwner()) {
                boolean drawCheck1 = false;
                boolean drawCheck2 = false;
                boolean drawClock = false;
                boolean drawError = false;
                boolean isBroadcast = ((int) (this.currentMessageObject.getDialogId() >> 32)) == 1;
                if (this.currentMessageObject.isSending() || this.currentMessageObject.isEditing()) {
                    drawCheck1 = false;
                    drawCheck2 = false;
                    drawClock = true;
                    drawError = false;
                } else if (this.currentMessageObject.isSendError()) {
                    drawCheck1 = false;
                    drawCheck2 = false;
                    drawClock = false;
                    drawError = true;
                } else if (this.currentMessageObject.isSent()) {
                    if (this.currentMessageObject.isUnread()) {
                        drawCheck1 = false;
                        drawCheck2 = true;
                    } else {
                        drawCheck1 = true;
                        drawCheck2 = true;
                    }
                    drawClock = false;
                    drawError = false;
                }
                if (drawClock) {
                    if (!this.mediaBackground || this.captionLayout != null) {
                        BaseCell.setDrawableBounds(Theme.chat_msgOutClockDrawable, (this.layoutWidth - AndroidUtilities.dp(18.5f)) - Theme.chat_msgOutClockDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(8.5f)) - Theme.chat_msgOutClockDrawable.getIntrinsicHeight());
                        Theme.chat_msgOutClockDrawable.draw(canvas);
                    } else if (this.currentMessageObject.type == 13 || this.currentMessageObject.type == 5) {
                        BaseCell.setDrawableBounds(Theme.chat_msgStickerClockDrawable, (this.layoutWidth - AndroidUtilities.dp(22.0f)) - Theme.chat_msgStickerClockDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(13.5f)) - Theme.chat_msgStickerClockDrawable.getIntrinsicHeight());
                        Theme.chat_msgStickerClockDrawable.draw(canvas);
                    } else {
                        BaseCell.setDrawableBounds(Theme.chat_msgMediaClockDrawable, (this.layoutWidth - AndroidUtilities.dp(22.0f)) - Theme.chat_msgMediaClockDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(13.5f)) - Theme.chat_msgMediaClockDrawable.getIntrinsicHeight());
                        Theme.chat_msgMediaClockDrawable.draw(canvas);
                    }
                }
                if (!isBroadcast) {
                    Drawable drawable;
                    if (drawCheck2) {
                        if (!this.mediaBackground || this.captionLayout != null) {
                            drawable = isDrawSelectedBackground() ? Theme.chat_msgOutCheckSelectedDrawable : Theme.chat_msgOutCheckDrawable;
                            if (drawCheck1) {
                                BaseCell.setDrawableBounds(drawable, (this.layoutWidth - AndroidUtilities.dp(22.5f)) - drawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(8.0f)) - drawable.getIntrinsicHeight());
                            } else {
                                BaseCell.setDrawableBounds(drawable, (this.layoutWidth - AndroidUtilities.dp(18.5f)) - drawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(8.0f)) - drawable.getIntrinsicHeight());
                            }
                            drawable.draw(canvas);
                        } else if (this.currentMessageObject.type == 13 || this.currentMessageObject.type == 5) {
                            if (drawCheck1) {
                                BaseCell.setDrawableBounds(Theme.chat_msgStickerCheckDrawable, (this.layoutWidth - AndroidUtilities.dp(26.3f)) - Theme.chat_msgStickerCheckDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(13.5f)) - Theme.chat_msgStickerCheckDrawable.getIntrinsicHeight());
                            } else {
                                BaseCell.setDrawableBounds(Theme.chat_msgStickerCheckDrawable, (this.layoutWidth - AndroidUtilities.dp(21.5f)) - Theme.chat_msgStickerCheckDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(13.5f)) - Theme.chat_msgStickerCheckDrawable.getIntrinsicHeight());
                            }
                            Theme.chat_msgStickerCheckDrawable.draw(canvas);
                        } else {
                            if (drawCheck1) {
                                BaseCell.setDrawableBounds(Theme.chat_msgMediaCheckDrawable, (this.layoutWidth - AndroidUtilities.dp(26.3f)) - Theme.chat_msgMediaCheckDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(13.5f)) - Theme.chat_msgMediaCheckDrawable.getIntrinsicHeight());
                            } else {
                                BaseCell.setDrawableBounds(Theme.chat_msgMediaCheckDrawable, (this.layoutWidth - AndroidUtilities.dp(21.5f)) - Theme.chat_msgMediaCheckDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(13.5f)) - Theme.chat_msgMediaCheckDrawable.getIntrinsicHeight());
                            }
                            Theme.chat_msgMediaCheckDrawable.setAlpha((int) (255.0f * this.timeAlpha));
                            Theme.chat_msgMediaCheckDrawable.draw(canvas);
                            Theme.chat_msgMediaCheckDrawable.setAlpha(255);
                        }
                    }
                    if (drawCheck1) {
                        if (!this.mediaBackground || this.captionLayout != null) {
                            drawable = isDrawSelectedBackground() ? Theme.chat_msgOutHalfCheckSelectedDrawable : Theme.chat_msgOutHalfCheckDrawable;
                            BaseCell.setDrawableBounds(drawable, (this.layoutWidth - AndroidUtilities.dp(18.0f)) - drawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(8.0f)) - drawable.getIntrinsicHeight());
                            drawable.draw(canvas);
                        } else if (this.currentMessageObject.type == 13 || this.currentMessageObject.type == 5) {
                            BaseCell.setDrawableBounds(Theme.chat_msgStickerHalfCheckDrawable, (this.layoutWidth - AndroidUtilities.dp(21.5f)) - Theme.chat_msgStickerHalfCheckDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(13.5f)) - Theme.chat_msgStickerHalfCheckDrawable.getIntrinsicHeight());
                            Theme.chat_msgStickerHalfCheckDrawable.draw(canvas);
                        } else {
                            BaseCell.setDrawableBounds(Theme.chat_msgMediaHalfCheckDrawable, (this.layoutWidth - AndroidUtilities.dp(21.5f)) - Theme.chat_msgMediaHalfCheckDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(13.5f)) - Theme.chat_msgMediaHalfCheckDrawable.getIntrinsicHeight());
                            Theme.chat_msgMediaHalfCheckDrawable.setAlpha((int) (255.0f * this.timeAlpha));
                            Theme.chat_msgMediaHalfCheckDrawable.draw(canvas);
                            Theme.chat_msgMediaHalfCheckDrawable.setAlpha(255);
                        }
                    }
                } else if (drawCheck1 || drawCheck2) {
                    if (this.mediaBackground && this.captionLayout == null) {
                        BaseCell.setDrawableBounds(Theme.chat_msgBroadcastMediaDrawable, (this.layoutWidth - AndroidUtilities.dp(24.0f)) - Theme.chat_msgBroadcastMediaDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(14.0f)) - Theme.chat_msgBroadcastMediaDrawable.getIntrinsicHeight());
                        Theme.chat_msgBroadcastMediaDrawable.draw(canvas);
                    } else {
                        BaseCell.setDrawableBounds(Theme.chat_msgBroadcastDrawable, (this.layoutWidth - AndroidUtilities.dp(20.5f)) - Theme.chat_msgBroadcastDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(8.0f)) - Theme.chat_msgBroadcastDrawable.getIntrinsicHeight());
                        Theme.chat_msgBroadcastDrawable.draw(canvas);
                    }
                }
                if (drawError) {
                    if (this.mediaBackground && this.captionLayout == null) {
                        x = this.layoutWidth - AndroidUtilities.dp(34.5f);
                        y = this.layoutHeight - AndroidUtilities.dp(26.5f);
                    } else {
                        x = this.layoutWidth - AndroidUtilities.dp(32.0f);
                        y = this.layoutHeight - AndroidUtilities.dp(21.0f);
                    }
                    this.rect.set((float) x, (float) y, (float) (AndroidUtilities.dp(14.0f) + x), (float) (AndroidUtilities.dp(14.0f) + y));
                    canvas.drawRoundRect(this.rect, (float) AndroidUtilities.dp(1.0f), (float) AndroidUtilities.dp(1.0f), Theme.chat_msgErrorPaint);
                    BaseCell.setDrawableBounds(Theme.chat_msgErrorDrawable, AndroidUtilities.dp(6.0f) + x, AndroidUtilities.dp(2.0f) + y);
                    Theme.chat_msgErrorDrawable.draw(canvas);
                }
            }
        }
    }

    public int getObserverTag() {
        return this.TAG;
    }

    public MessageObject getMessageObject() {
        return this.currentMessageObject;
    }

    public boolean isPinnedBottom() {
        return this.pinnedBottom;
    }

    public boolean isPinnedTop() {
        return this.pinnedTop;
    }

    public GroupedMessages getCurrentMessagesGroup() {
        return this.currentMessagesGroup;
    }

    public GroupedMessagePosition getCurrentPosition() {
        return this.currentPosition;
    }

    public int getLayoutHeight() {
        return this.layoutHeight;
    }
}
