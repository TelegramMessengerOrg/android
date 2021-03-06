package org.telegram.messenger;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.ImageDecoder;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Path.FillType;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioAttributes.Builder;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.provider.Settings.System;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Action;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.support.v4.app.NotificationCompat.CarExtender;
import android.support.v4.app.NotificationCompat.CarExtender.UnreadConversation;
import android.support.v4.app.NotificationCompat.InboxStyle;
import android.support.v4.app.NotificationCompat.MessagingStyle;
import android.support.v4.app.NotificationCompat.Style;
import android.support.v4.app.NotificationCompat.WearableExtender;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.Person;
import android.support.v4.app.RemoteInput;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.drawable.IconCompat;
import android.text.TextUtils;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.upstream.DataSchemeDataSource;
import com.google.android.exoplayer2.util.MimeTypes;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.beta.R;
import org.telegram.messenger.support.SparseLongArray;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.KeyboardButton;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.PhoneCallDiscardReason;
import org.telegram.tgnet.TLRPC.TL_account_updateNotifySettings;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_inputNotifyPeer;
import org.telegram.tgnet.TLRPC.TL_inputPeerNotifySettings;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonCallback;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonRow;
import org.telegram.tgnet.TLRPC.TL_messageActionChannelCreate;
import org.telegram.tgnet.TLRPC.TL_messageActionChannelMigrateFrom;
import org.telegram.tgnet.TLRPC.TL_messageActionChatAddUser;
import org.telegram.tgnet.TLRPC.TL_messageActionChatCreate;
import org.telegram.tgnet.TLRPC.TL_messageActionChatDeletePhoto;
import org.telegram.tgnet.TLRPC.TL_messageActionChatDeleteUser;
import org.telegram.tgnet.TLRPC.TL_messageActionChatEditPhoto;
import org.telegram.tgnet.TLRPC.TL_messageActionChatEditTitle;
import org.telegram.tgnet.TLRPC.TL_messageActionChatJoinedByLink;
import org.telegram.tgnet.TLRPC.TL_messageActionChatMigrateTo;
import org.telegram.tgnet.TLRPC.TL_messageActionEmpty;
import org.telegram.tgnet.TLRPC.TL_messageActionGameScore;
import org.telegram.tgnet.TLRPC.TL_messageActionLoginUnknownLocation;
import org.telegram.tgnet.TLRPC.TL_messageActionPaymentSent;
import org.telegram.tgnet.TLRPC.TL_messageActionPhoneCall;
import org.telegram.tgnet.TLRPC.TL_messageActionPinMessage;
import org.telegram.tgnet.TLRPC.TL_messageActionScreenshotTaken;
import org.telegram.tgnet.TLRPC.TL_messageActionUserJoined;
import org.telegram.tgnet.TLRPC.TL_messageActionUserUpdatedPhoto;
import org.telegram.tgnet.TLRPC.TL_messageMediaContact;
import org.telegram.tgnet.TLRPC.TL_messageMediaDocument;
import org.telegram.tgnet.TLRPC.TL_messageMediaGame;
import org.telegram.tgnet.TLRPC.TL_messageMediaGeo;
import org.telegram.tgnet.TLRPC.TL_messageMediaGeoLive;
import org.telegram.tgnet.TLRPC.TL_messageMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_messageMediaVenue;
import org.telegram.tgnet.TLRPC.TL_messageService;
import org.telegram.tgnet.TLRPC.TL_phoneCallDiscardReasonMissed;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.PopupNotificationActivity;

public class NotificationsController {
    public static final String EXTRA_VOICE_REPLY = "extra_voice_reply";
    private static volatile NotificationsController[] Instance = new NotificationsController[3];
    public static String OTHER_NOTIFICATIONS_CHANNEL = null;
    protected static AudioManager audioManager = ((AudioManager) ApplicationLoader.applicationContext.getSystemService(MimeTypes.BASE_TYPE_AUDIO));
    public static long globalSecretChatId = -4294967296L;
    public static long lastNoDataNotificationTime;
    private static NotificationManagerCompat notificationManager;
    private static DispatchQueue notificationsQueue = new DispatchQueue("notificationsQueue");
    private static NotificationManager systemNotificationManager;
    private AlarmManager alarmManager;
    private int currentAccount;
    private ArrayList<MessageObject> delayedPushMessages = new ArrayList();
    private LongSparseArray<MessageObject> fcmRandomMessagesDict = new LongSparseArray();
    private boolean inChatSoundEnabled;
    private int lastBadgeCount = -1;
    private int lastButtonId = DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS;
    private boolean lastNotificationIsNoData;
    private int lastOnlineFromOtherDevice = 0;
    private long lastSoundOutPlay;
    private long lastSoundPlay;
    private LongSparseArray<Integer> lastWearNotifiedMessageId = new LongSparseArray();
    private String launcherClassName;
    private Runnable notificationDelayRunnable;
    private WakeLock notificationDelayWakelock;
    private String notificationGroup;
    private int notificationId;
    private boolean notifyCheck = false;
    private long opened_dialog_id = 0;
    private int personal_count = 0;
    public ArrayList<MessageObject> popupMessages = new ArrayList();
    public ArrayList<MessageObject> popupReplyMessages = new ArrayList();
    private LongSparseArray<Integer> pushDialogs = new LongSparseArray();
    private LongSparseArray<Integer> pushDialogsOverrideMention = new LongSparseArray();
    private ArrayList<MessageObject> pushMessages = new ArrayList();
    private LongSparseArray<MessageObject> pushMessagesDict = new LongSparseArray();
    public boolean showBadgeNumber;
    private LongSparseArray<Point> smartNotificationsDialogs = new LongSparseArray();
    private int soundIn;
    private boolean soundInLoaded;
    private int soundOut;
    private boolean soundOutLoaded;
    private SoundPool soundPool;
    private int soundRecord;
    private boolean soundRecordLoaded;
    private int total_unread_count = 0;
    private LongSparseArray<Integer> wearNotificationsIds = new LongSparseArray();

    class AnonymousClass1NotificationHolder {
        int id;
        Notification notification;

        AnonymousClass1NotificationHolder(int i, Notification n) {
            this.id = i;
            this.notification = n;
        }

        void call() {
            if (BuildVars.LOGS_ENABLED) {
                FileLog.w("show dialog notification with id " + this.id);
            }
            NotificationsController.notificationManager.notify(this.id, this.notification);
        }
    }

    static {
        notificationManager = null;
        systemNotificationManager = null;
        if (VERSION.SDK_INT >= 26 && ApplicationLoader.applicationContext != null) {
            notificationManager = NotificationManagerCompat.from(ApplicationLoader.applicationContext);
            systemNotificationManager = (NotificationManager) ApplicationLoader.applicationContext.getSystemService("notification");
            checkOtherNotificationsChannel();
        }
    }

    public static NotificationsController getInstance(int num) {
        NotificationsController localInstance = Instance[num];
        if (localInstance == null) {
            synchronized (NotificationsController.class) {
                try {
                    localInstance = Instance[num];
                    if (localInstance == null) {
                        NotificationsController[] notificationsControllerArr = Instance;
                        NotificationsController localInstance2 = new NotificationsController(num);
                        try {
                            notificationsControllerArr[num] = localInstance2;
                            localInstance = localInstance2;
                        } catch (Throwable th) {
                            Throwable th2 = th;
                            localInstance = localInstance2;
                            throw th2;
                        }
                    }
                } catch (Throwable th3) {
                    th2 = th3;
                    throw th2;
                }
            }
        }
        return localInstance;
    }

    public NotificationsController(int instance) {
        this.currentAccount = instance;
        this.notificationId = this.currentAccount + 1;
        this.notificationGroup = "messages" + (this.currentAccount == 0 ? TtmlNode.ANONYMOUS_REGION_ID : Integer.valueOf(this.currentAccount));
        SharedPreferences preferences = MessagesController.getNotificationsSettings(this.currentAccount);
        this.inChatSoundEnabled = preferences.getBoolean("EnableInChatSound", true);
        this.showBadgeNumber = preferences.getBoolean("badgeNumber", true);
        notificationManager = NotificationManagerCompat.from(ApplicationLoader.applicationContext);
        systemNotificationManager = (NotificationManager) ApplicationLoader.applicationContext.getSystemService("notification");
        try {
            audioManager = (AudioManager) ApplicationLoader.applicationContext.getSystemService(MimeTypes.BASE_TYPE_AUDIO);
        } catch (Throwable e) {
            FileLog.e(e);
        }
        try {
            this.alarmManager = (AlarmManager) ApplicationLoader.applicationContext.getSystemService("alarm");
        } catch (Throwable e2) {
            FileLog.e(e2);
        }
        try {
            this.notificationDelayWakelock = ((PowerManager) ApplicationLoader.applicationContext.getSystemService("power")).newWakeLock(1, "lock");
            this.notificationDelayWakelock.setReferenceCounted(false);
        } catch (Throwable e22) {
            FileLog.e(e22);
        }
        this.notificationDelayRunnable = new NotificationsController$$Lambda$0(this);
    }

    final /* synthetic */ void lambda$new$0$NotificationsController() {
        if (BuildVars.LOGS_ENABLED) {
            FileLog.d("delay reached");
        }
        if (!this.delayedPushMessages.isEmpty()) {
            showOrUpdateNotification(true);
            this.delayedPushMessages.clear();
        } else if (this.lastNotificationIsNoData) {
            notificationManager.cancel(this.notificationId);
        }
        try {
            if (this.notificationDelayWakelock.isHeld()) {
                this.notificationDelayWakelock.release();
            }
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }

    public static void checkOtherNotificationsChannel() {
        if (VERSION.SDK_INT >= 26) {
            SharedPreferences preferences = null;
            if (OTHER_NOTIFICATIONS_CHANNEL == null) {
                preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0);
                OTHER_NOTIFICATIONS_CHANNEL = preferences.getString("OtherKey", "Other3");
            }
            NotificationChannel notificationChannel = systemNotificationManager.getNotificationChannel(OTHER_NOTIFICATIONS_CHANNEL);
            if (notificationChannel != null && notificationChannel.getImportance() == 0) {
                systemNotificationManager.deleteNotificationChannel(OTHER_NOTIFICATIONS_CHANNEL);
                OTHER_NOTIFICATIONS_CHANNEL = null;
                notificationChannel = null;
            }
            if (OTHER_NOTIFICATIONS_CHANNEL == null) {
                if (preferences == null) {
                    preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0);
                }
                OTHER_NOTIFICATIONS_CHANNEL = "Other" + Utilities.random.nextLong();
                preferences.edit().putString("OtherKey", OTHER_NOTIFICATIONS_CHANNEL).commit();
            }
            if (notificationChannel == null) {
                notificationChannel = new NotificationChannel(OTHER_NOTIFICATIONS_CHANNEL, "Other", 3);
                notificationChannel.enableLights(false);
                notificationChannel.enableVibration(false);
                notificationChannel.setSound(null, null);
                systemNotificationManager.createNotificationChannel(notificationChannel);
            }
        }
    }

    public void cleanup() {
        this.popupMessages.clear();
        this.popupReplyMessages.clear();
        notificationsQueue.postRunnable(new NotificationsController$$Lambda$1(this));
    }

    final /* synthetic */ void lambda$cleanup$1$NotificationsController() {
        this.opened_dialog_id = 0;
        this.total_unread_count = 0;
        this.personal_count = 0;
        this.pushMessages.clear();
        this.pushMessagesDict.clear();
        this.fcmRandomMessagesDict.clear();
        this.pushDialogs.clear();
        this.wearNotificationsIds.clear();
        this.lastWearNotifiedMessageId.clear();
        this.delayedPushMessages.clear();
        this.notifyCheck = false;
        this.lastBadgeCount = 0;
        try {
            if (this.notificationDelayWakelock.isHeld()) {
                this.notificationDelayWakelock.release();
            }
        } catch (Throwable e) {
            FileLog.e(e);
        }
        setBadge(getTotalAllUnreadCount());
        Editor editor = MessagesController.getNotificationsSettings(this.currentAccount).edit();
        editor.clear();
        editor.commit();
        if (VERSION.SDK_INT >= 26) {
            try {
                String keyStart = this.currentAccount + "channel";
                List<NotificationChannel> list = systemNotificationManager.getNotificationChannels();
                int count = list.size();
                for (int a = 0; a < count; a++) {
                    String id = ((NotificationChannel) list.get(a)).getId();
                    if (id.startsWith(keyStart)) {
                        systemNotificationManager.deleteNotificationChannel(id);
                    }
                }
            } catch (Throwable e2) {
                FileLog.e(e2);
            }
        }
    }

    public void setInChatSoundEnabled(boolean value) {
        this.inChatSoundEnabled = value;
    }

    final /* synthetic */ void lambda$setOpenedDialogId$2$NotificationsController(long dialog_id) {
        this.opened_dialog_id = dialog_id;
    }

    public void setOpenedDialogId(long dialog_id) {
        notificationsQueue.postRunnable(new NotificationsController$$Lambda$2(this, dialog_id));
    }

    public void setLastOnlineFromOtherDevice(int time) {
        notificationsQueue.postRunnable(new NotificationsController$$Lambda$3(this, time));
    }

    final /* synthetic */ void lambda$setLastOnlineFromOtherDevice$3$NotificationsController(int time) {
        if (BuildVars.LOGS_ENABLED) {
            FileLog.d("set last online from other device = " + time);
        }
        this.lastOnlineFromOtherDevice = time;
    }

    public void removeNotificationsForDialog(long did) {
        getInstance(this.currentAccount).processReadMessages(null, did, 0, ConnectionsManager.DEFAULT_DATACENTER_ID, false);
        LongSparseArray<Integer> dialogsToUpdate = new LongSparseArray();
        dialogsToUpdate.put(did, Integer.valueOf(0));
        getInstance(this.currentAccount).processDialogsUpdateRead(dialogsToUpdate);
    }

    public boolean hasMessagesToReply() {
        for (int a = 0; a < this.pushMessages.size(); a++) {
            MessageObject messageObject = (MessageObject) this.pushMessages.get(a);
            long dialog_id = messageObject.getDialogId();
            if ((!messageObject.messageOwner.mentioned || !(messageObject.messageOwner.action instanceof TL_messageActionPinMessage)) && ((int) dialog_id) != 0 && (messageObject.messageOwner.to_id.channel_id == 0 || messageObject.isMegagroup())) {
                return true;
            }
        }
        return false;
    }

    protected void forceShowPopupForReply() {
        notificationsQueue.postRunnable(new NotificationsController$$Lambda$4(this));
    }

    final /* synthetic */ void lambda$forceShowPopupForReply$5$NotificationsController() {
        ArrayList<MessageObject> popupArray = new ArrayList();
        for (int a = 0; a < this.pushMessages.size(); a++) {
            MessageObject messageObject = (MessageObject) this.pushMessages.get(a);
            long dialog_id = messageObject.getDialogId();
            if (!((messageObject.messageOwner.mentioned && (messageObject.messageOwner.action instanceof TL_messageActionPinMessage)) || ((int) dialog_id) == 0 || (messageObject.messageOwner.to_id.channel_id != 0 && !messageObject.isMegagroup()))) {
                popupArray.add(0, messageObject);
            }
        }
        if (!popupArray.isEmpty() && !AndroidUtilities.needShowPasscode(false)) {
            AndroidUtilities.runOnUIThread(new NotificationsController$$Lambda$31(this, popupArray));
        }
    }

    final /* synthetic */ void lambda$null$4$NotificationsController(ArrayList popupArray) {
        this.popupReplyMessages = popupArray;
        Intent popupIntent = new Intent(ApplicationLoader.applicationContext, PopupNotificationActivity.class);
        popupIntent.putExtra("force", true);
        popupIntent.putExtra("currentAccount", this.currentAccount);
        popupIntent.setFlags(268763140);
        ApplicationLoader.applicationContext.startActivity(popupIntent);
        ApplicationLoader.applicationContext.sendBroadcast(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
    }

    public void removeDeletedMessagesFromNotifications(SparseArray<ArrayList<Integer>> deletedMessages) {
        notificationsQueue.postRunnable(new NotificationsController$$Lambda$5(this, deletedMessages, new ArrayList(0)));
    }

    final /* synthetic */ void lambda$removeDeletedMessagesFromNotifications$8$NotificationsController(SparseArray deletedMessages, ArrayList popupArrayRemove) {
        int old_unread_count = this.total_unread_count;
        SharedPreferences preferences = MessagesController.getNotificationsSettings(this.currentAccount);
        for (int a = 0; a < deletedMessages.size(); a++) {
            int key = deletedMessages.keyAt(a);
            long dialog_id = (long) (-key);
            ArrayList<Integer> mids = (ArrayList) deletedMessages.get(key);
            Integer currentCount = (Integer) this.pushDialogs.get(dialog_id);
            if (currentCount == null) {
                currentCount = Integer.valueOf(0);
            }
            Integer newCount = currentCount;
            for (int b = 0; b < mids.size(); b++) {
                long mid = ((long) ((Integer) mids.get(b)).intValue()) | (((long) key) << 32);
                MessageObject messageObject = (MessageObject) this.pushMessagesDict.get(mid);
                if (messageObject != null) {
                    this.pushMessagesDict.remove(mid);
                    this.delayedPushMessages.remove(messageObject);
                    this.pushMessages.remove(messageObject);
                    if (isPersonalMessage(messageObject)) {
                        this.personal_count--;
                    }
                    popupArrayRemove.add(messageObject);
                    newCount = Integer.valueOf(newCount.intValue() - 1);
                }
            }
            if (newCount.intValue() <= 0) {
                newCount = Integer.valueOf(0);
                this.smartNotificationsDialogs.remove(dialog_id);
            }
            if (!newCount.equals(currentCount)) {
                this.total_unread_count -= currentCount.intValue();
                this.total_unread_count += newCount.intValue();
                this.pushDialogs.put(dialog_id, newCount);
            }
            if (newCount.intValue() == 0) {
                this.pushDialogs.remove(dialog_id);
                this.pushDialogsOverrideMention.remove(dialog_id);
            }
        }
        if (!popupArrayRemove.isEmpty()) {
            AndroidUtilities.runOnUIThread(new NotificationsController$$Lambda$29(this, popupArrayRemove));
        }
        if (old_unread_count != this.total_unread_count) {
            if (this.notifyCheck) {
                scheduleNotificationDelay(this.lastOnlineFromOtherDevice > ConnectionsManager.getInstance(this.currentAccount).getCurrentTime());
            } else {
                this.delayedPushMessages.clear();
                showOrUpdateNotification(this.notifyCheck);
            }
            AndroidUtilities.runOnUIThread(new NotificationsController$$Lambda$30(this, this.pushDialogs.size()));
        }
        this.notifyCheck = false;
        if (this.showBadgeNumber) {
            setBadge(getTotalAllUnreadCount());
        }
    }

    final /* synthetic */ void lambda$null$6$NotificationsController(ArrayList popupArrayRemove) {
        int size = popupArrayRemove.size();
        for (int a = 0; a < size; a++) {
            this.popupMessages.remove(popupArrayRemove.get(a));
        }
    }

    final /* synthetic */ void lambda$null$7$NotificationsController(int pushDialogsCount) {
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.notificationsCountUpdated, Integer.valueOf(this.currentAccount));
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.dialogsUnreadCounterChanged, Integer.valueOf(pushDialogsCount));
    }

    public void removeDeletedHisoryFromNotifications(SparseIntArray deletedMessages) {
        notificationsQueue.postRunnable(new NotificationsController$$Lambda$6(this, deletedMessages, new ArrayList(0)));
    }

    final /* synthetic */ void lambda$removeDeletedHisoryFromNotifications$11$NotificationsController(SparseIntArray deletedMessages, ArrayList popupArrayRemove) {
        int old_unread_count = this.total_unread_count;
        SharedPreferences preferences = MessagesController.getNotificationsSettings(this.currentAccount);
        for (int a = 0; a < deletedMessages.size(); a++) {
            int key = deletedMessages.keyAt(a);
            long dialog_id = (long) (-key);
            int id = deletedMessages.get(key);
            Integer currentCount = (Integer) this.pushDialogs.get(dialog_id);
            if (currentCount == null) {
                currentCount = Integer.valueOf(0);
            }
            Integer newCount = currentCount;
            int c = 0;
            while (c < this.pushMessages.size()) {
                MessageObject messageObject = (MessageObject) this.pushMessages.get(c);
                if (messageObject.getDialogId() == dialog_id && messageObject.getId() <= id) {
                    this.pushMessagesDict.remove(messageObject.getIdWithChannel());
                    this.delayedPushMessages.remove(messageObject);
                    this.pushMessages.remove(messageObject);
                    c--;
                    if (isPersonalMessage(messageObject)) {
                        this.personal_count--;
                    }
                    popupArrayRemove.add(messageObject);
                    newCount = Integer.valueOf(newCount.intValue() - 1);
                }
                c++;
            }
            if (newCount.intValue() <= 0) {
                newCount = Integer.valueOf(0);
                this.smartNotificationsDialogs.remove(dialog_id);
            }
            if (!newCount.equals(currentCount)) {
                this.total_unread_count -= currentCount.intValue();
                this.total_unread_count += newCount.intValue();
                this.pushDialogs.put(dialog_id, newCount);
            }
            if (newCount.intValue() == 0) {
                this.pushDialogs.remove(dialog_id);
                this.pushDialogsOverrideMention.remove(dialog_id);
            }
        }
        if (popupArrayRemove.isEmpty()) {
            AndroidUtilities.runOnUIThread(new NotificationsController$$Lambda$27(this, popupArrayRemove));
        }
        if (old_unread_count != this.total_unread_count) {
            if (this.notifyCheck) {
                scheduleNotificationDelay(this.lastOnlineFromOtherDevice > ConnectionsManager.getInstance(this.currentAccount).getCurrentTime());
            } else {
                this.delayedPushMessages.clear();
                showOrUpdateNotification(this.notifyCheck);
            }
            AndroidUtilities.runOnUIThread(new NotificationsController$$Lambda$28(this, this.pushDialogs.size()));
        }
        this.notifyCheck = false;
        if (this.showBadgeNumber) {
            setBadge(getTotalAllUnreadCount());
        }
    }

    final /* synthetic */ void lambda$null$9$NotificationsController(ArrayList popupArrayRemove) {
        int size = popupArrayRemove.size();
        for (int a = 0; a < size; a++) {
            this.popupMessages.remove(popupArrayRemove.get(a));
        }
    }

    final /* synthetic */ void lambda$null$10$NotificationsController(int pushDialogsCount) {
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.notificationsCountUpdated, Integer.valueOf(this.currentAccount));
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.dialogsUnreadCounterChanged, Integer.valueOf(pushDialogsCount));
    }

    public void processReadMessages(SparseLongArray inbox, long dialog_id, int max_date, int max_id, boolean isPopup) {
        notificationsQueue.postRunnable(new NotificationsController$$Lambda$7(this, inbox, new ArrayList(0), dialog_id, max_id, max_date, isPopup));
    }

    final /* synthetic */ void lambda$processReadMessages$13$NotificationsController(SparseLongArray inbox, ArrayList popupArrayRemove, long dialog_id, int max_id, int max_date, boolean isPopup) {
        int a;
        MessageObject messageObject;
        long mid;
        if (inbox != null) {
            for (int b = 0; b < inbox.size(); b++) {
                int key = inbox.keyAt(b);
                long messageId = inbox.get(key);
                a = 0;
                while (a < this.pushMessages.size()) {
                    messageObject = (MessageObject) this.pushMessages.get(a);
                    if (messageObject.getDialogId() == ((long) key) && messageObject.getId() <= ((int) messageId)) {
                        if (isPersonalMessage(messageObject)) {
                            this.personal_count--;
                        }
                        popupArrayRemove.add(messageObject);
                        mid = (long) messageObject.getId();
                        if (messageObject.messageOwner.to_id.channel_id != 0) {
                            mid |= ((long) messageObject.messageOwner.to_id.channel_id) << 32;
                        }
                        this.pushMessagesDict.remove(mid);
                        this.delayedPushMessages.remove(messageObject);
                        this.pushMessages.remove(a);
                        a--;
                    }
                    a++;
                }
            }
        }
        if (!(dialog_id == 0 || (max_id == 0 && max_date == 0))) {
            a = 0;
            while (a < this.pushMessages.size()) {
                messageObject = (MessageObject) this.pushMessages.get(a);
                if (messageObject.getDialogId() == dialog_id) {
                    boolean remove = false;
                    if (max_date != 0) {
                        if (messageObject.messageOwner.date <= max_date) {
                            remove = true;
                        }
                    } else if (isPopup) {
                        if (messageObject.getId() == max_id || max_id < 0) {
                            remove = true;
                        }
                    } else if (messageObject.getId() <= max_id || max_id < 0) {
                        remove = true;
                    }
                    if (remove) {
                        if (isPersonalMessage(messageObject)) {
                            this.personal_count--;
                        }
                        this.pushMessages.remove(a);
                        this.delayedPushMessages.remove(messageObject);
                        popupArrayRemove.add(messageObject);
                        mid = (long) messageObject.getId();
                        if (messageObject.messageOwner.to_id.channel_id != 0) {
                            mid |= ((long) messageObject.messageOwner.to_id.channel_id) << 32;
                        }
                        this.pushMessagesDict.remove(mid);
                        a--;
                    }
                }
                a++;
            }
        }
        if (!popupArrayRemove.isEmpty()) {
            AndroidUtilities.runOnUIThread(new NotificationsController$$Lambda$26(this, popupArrayRemove));
        }
    }

    final /* synthetic */ void lambda$null$12$NotificationsController(ArrayList popupArrayRemove) {
        int size = popupArrayRemove.size();
        for (int a = 0; a < size; a++) {
            this.popupMessages.remove(popupArrayRemove.get(a));
        }
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.pushMessagesUpdated, new Object[0]);
    }

    public void processNewMessages(ArrayList<MessageObject> messageObjects, boolean isLast, boolean isFcm) {
        if (!messageObjects.isEmpty()) {
            notificationsQueue.postRunnable(new NotificationsController$$Lambda$8(this, messageObjects, isFcm, new ArrayList(0), isLast));
        }
    }

    final /* synthetic */ void lambda$processNewMessages$16$NotificationsController(ArrayList messageObjects, boolean isFcm, ArrayList popupArrayAdd, boolean isLast) {
        long dialog_id;
        boolean added = false;
        LongSparseArray<Boolean> settingsCache = new LongSparseArray();
        SharedPreferences preferences = MessagesController.getNotificationsSettings(this.currentAccount);
        boolean allowPinned = preferences.getBoolean("PinnedMessages", true);
        int popup = 0;
        for (int a = 0; a < messageObjects.size(); a++) {
            MessageObject messageObject = (MessageObject) messageObjects.get(a);
            long mid = (long) messageObject.getId();
            long random_id = messageObject.isFcmMessage() ? messageObject.messageOwner.random_id : 0;
            if (messageObject.messageOwner.to_id.channel_id != 0) {
                mid |= ((long) messageObject.messageOwner.to_id.channel_id) << 32;
            }
            MessageObject oldMessageObject = (MessageObject) this.pushMessagesDict.get(mid);
            if (oldMessageObject == null && messageObject.messageOwner.random_id != 0) {
                oldMessageObject = (MessageObject) this.fcmRandomMessagesDict.get(messageObject.messageOwner.random_id);
                if (oldMessageObject != null) {
                    this.fcmRandomMessagesDict.remove(messageObject.messageOwner.random_id);
                }
            }
            if (oldMessageObject == null) {
                dialog_id = messageObject.getDialogId();
                long original_dialog_id = dialog_id;
                if (dialog_id != this.opened_dialog_id || !ApplicationLoader.isScreenOn) {
                    boolean value;
                    if (messageObject.messageOwner.mentioned) {
                        if (allowPinned || !(messageObject.messageOwner.action instanceof TL_messageActionPinMessage)) {
                            dialog_id = (long) messageObject.messageOwner.from_id;
                        }
                    }
                    if (isPersonalMessage(messageObject)) {
                        this.personal_count++;
                    }
                    added = true;
                    int lower_id = (int) dialog_id;
                    if (lower_id < 0) {
                    }
                    int index = settingsCache.indexOfKey(dialog_id);
                    if (index >= 0) {
                        value = ((Boolean) settingsCache.valueAt(index)).booleanValue();
                    } else {
                        int notifyOverride = getNotifyOverride(preferences, dialog_id);
                        value = notifyOverride == -1 ? ((int) dialog_id) < 0 ? preferences.getBoolean("EnableGroup", true) : preferences.getBoolean("EnableAll", true) : notifyOverride != 2;
                        settingsCache.put(dialog_id, Boolean.valueOf(value));
                    }
                    if (lower_id != 0) {
                        if (preferences.getBoolean("custom_" + dialog_id, false)) {
                            popup = preferences.getInt("popup_" + dialog_id, 0);
                        } else {
                            popup = 0;
                        }
                        if (popup == 0) {
                            popup = preferences.getInt(((int) dialog_id) < 0 ? "popupGroup" : "popupAll", 0);
                        } else if (popup == 1) {
                            popup = 3;
                        } else if (popup == 2) {
                            popup = 0;
                        }
                    }
                    if (!(popup == 0 || messageObject.messageOwner.to_id.channel_id == 0 || messageObject.isMegagroup())) {
                        popup = 0;
                    }
                    if (value) {
                        if (popup != 0) {
                            popupArrayAdd.add(0, messageObject);
                        }
                        this.delayedPushMessages.add(messageObject);
                        this.pushMessages.add(0, messageObject);
                        if (mid != 0) {
                            this.pushMessagesDict.put(mid, messageObject);
                        } else if (random_id != 0) {
                            this.fcmRandomMessagesDict.put(random_id, messageObject);
                        }
                        if (original_dialog_id != dialog_id) {
                            this.pushDialogsOverrideMention.put(original_dialog_id, Integer.valueOf(1));
                        }
                    }
                } else if (!isFcm) {
                    playInChatSound();
                }
            } else if (oldMessageObject.isFcmMessage()) {
                this.pushMessagesDict.put(mid, messageObject);
                int idxOld = this.pushMessages.indexOf(oldMessageObject);
                if (idxOld >= 0) {
                    this.pushMessages.set(idxOld, messageObject);
                }
            }
        }
        if (added) {
            this.notifyCheck = isLast;
        }
        if (!(popupArrayAdd.isEmpty() || AndroidUtilities.needShowPasscode(false))) {
            AndroidUtilities.runOnUIThread(new NotificationsController$$Lambda$24(this, popupArrayAdd, popup));
        }
        if (added && isFcm) {
            dialog_id = ((MessageObject) messageObjects.get(0)).getDialogId();
            int old_unread_count = this.total_unread_count;
            notifyOverride = getNotifyOverride(preferences, dialog_id);
            if (this.notifyCheck) {
                Integer override = (Integer) this.pushDialogsOverrideMention.get(dialog_id);
                if (override != null && override.intValue() == 1) {
                    this.pushDialogsOverrideMention.put(dialog_id, Integer.valueOf(0));
                    notifyOverride = 1;
                }
            }
            boolean canAddValue = notifyOverride == -1 ? ((int) dialog_id) < 0 ? preferences.getBoolean("EnableGroup", true) : preferences.getBoolean("EnableAll", true) : notifyOverride != 2;
            Integer currentCount = (Integer) this.pushDialogs.get(dialog_id);
            Integer newCount = Integer.valueOf(currentCount != null ? currentCount.intValue() + 1 : 1);
            if (canAddValue) {
                if (currentCount != null) {
                    this.total_unread_count -= currentCount.intValue();
                }
                this.total_unread_count += newCount.intValue();
                this.pushDialogs.put(dialog_id, newCount);
            }
            if (old_unread_count != this.total_unread_count) {
                if (this.notifyCheck) {
                    scheduleNotificationDelay(this.lastOnlineFromOtherDevice > ConnectionsManager.getInstance(this.currentAccount).getCurrentTime());
                } else {
                    this.delayedPushMessages.clear();
                    showOrUpdateNotification(this.notifyCheck);
                }
                AndroidUtilities.runOnUIThread(new NotificationsController$$Lambda$25(this, this.pushDialogs.size()));
            }
            this.notifyCheck = false;
            if (this.showBadgeNumber) {
                setBadge(getTotalAllUnreadCount());
            }
        }
    }

    final /* synthetic */ void lambda$null$14$NotificationsController(ArrayList popupArrayAdd, int popupFinal) {
        this.popupMessages.addAll(0, popupArrayAdd);
        if (!ApplicationLoader.mainInterfacePaused && (ApplicationLoader.isScreenOn || SharedConfig.isWaitingForPasscodeEnter)) {
            return;
        }
        if (popupFinal == 3 || ((popupFinal == 1 && ApplicationLoader.isScreenOn) || (popupFinal == 2 && !ApplicationLoader.isScreenOn))) {
            Intent popupIntent = new Intent(ApplicationLoader.applicationContext, PopupNotificationActivity.class);
            popupIntent.setFlags(268763140);
            ApplicationLoader.applicationContext.startActivity(popupIntent);
        }
    }

    final /* synthetic */ void lambda$null$15$NotificationsController(int pushDialogsCount) {
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.notificationsCountUpdated, Integer.valueOf(this.currentAccount));
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.dialogsUnreadCounterChanged, Integer.valueOf(pushDialogsCount));
    }

    public int getTotalUnreadCount() {
        return this.total_unread_count;
    }

    public void processDialogsUpdateRead(LongSparseArray<Integer> dialogsToUpdate) {
        notificationsQueue.postRunnable(new NotificationsController$$Lambda$9(this, dialogsToUpdate, new ArrayList()));
    }

    final /* synthetic */ void lambda$processDialogsUpdateRead$19$NotificationsController(LongSparseArray dialogsToUpdate, ArrayList popupArrayToRemove) {
        int old_unread_count = this.total_unread_count;
        SharedPreferences preferences = MessagesController.getNotificationsSettings(this.currentAccount);
        for (int b = 0; b < dialogsToUpdate.size(); b++) {
            long dialog_id = dialogsToUpdate.keyAt(b);
            int notifyOverride = getNotifyOverride(preferences, dialog_id);
            if (this.notifyCheck) {
                Integer override = (Integer) this.pushDialogsOverrideMention.get(dialog_id);
                if (override != null && override.intValue() == 1) {
                    this.pushDialogsOverrideMention.put(dialog_id, Integer.valueOf(0));
                    notifyOverride = 1;
                }
            }
            boolean canAddValue = notifyOverride == -1 ? ((int) dialog_id) < 0 ? preferences.getBoolean("EnableGroup", true) : preferences.getBoolean("EnableAll", true) : notifyOverride != 2;
            Integer currentCount = (Integer) this.pushDialogs.get(dialog_id);
            Integer newCount = (Integer) dialogsToUpdate.get(dialog_id);
            if (newCount.intValue() == 0) {
                this.smartNotificationsDialogs.remove(dialog_id);
            }
            if (newCount.intValue() < 0) {
                if (currentCount == null) {
                } else {
                    newCount = Integer.valueOf(currentCount.intValue() + newCount.intValue());
                }
            }
            if ((canAddValue || newCount.intValue() == 0) && currentCount != null) {
                this.total_unread_count -= currentCount.intValue();
            }
            if (newCount.intValue() == 0) {
                this.pushDialogs.remove(dialog_id);
                this.pushDialogsOverrideMention.remove(dialog_id);
                int a = 0;
                while (a < this.pushMessages.size()) {
                    MessageObject messageObject = (MessageObject) this.pushMessages.get(a);
                    if (messageObject.getDialogId() == dialog_id) {
                        if (isPersonalMessage(messageObject)) {
                            this.personal_count--;
                        }
                        this.pushMessages.remove(a);
                        a--;
                        this.delayedPushMessages.remove(messageObject);
                        long mid = (long) messageObject.getId();
                        if (messageObject.messageOwner.to_id.channel_id != 0) {
                            mid |= ((long) messageObject.messageOwner.to_id.channel_id) << 32;
                        }
                        this.pushMessagesDict.remove(mid);
                        popupArrayToRemove.add(messageObject);
                    }
                    a++;
                }
            } else if (canAddValue) {
                this.total_unread_count += newCount.intValue();
                this.pushDialogs.put(dialog_id, newCount);
            }
        }
        if (!popupArrayToRemove.isEmpty()) {
            AndroidUtilities.runOnUIThread(new NotificationsController$$Lambda$22(this, popupArrayToRemove));
        }
        if (old_unread_count != this.total_unread_count) {
            if (this.notifyCheck) {
                scheduleNotificationDelay(this.lastOnlineFromOtherDevice > ConnectionsManager.getInstance(this.currentAccount).getCurrentTime());
            } else {
                this.delayedPushMessages.clear();
                showOrUpdateNotification(this.notifyCheck);
            }
            AndroidUtilities.runOnUIThread(new NotificationsController$$Lambda$23(this, this.pushDialogs.size()));
        }
        this.notifyCheck = false;
        if (this.showBadgeNumber) {
            setBadge(getTotalAllUnreadCount());
        }
    }

    final /* synthetic */ void lambda$null$17$NotificationsController(ArrayList popupArrayToRemove) {
        int size = popupArrayToRemove.size();
        for (int a = 0; a < size; a++) {
            this.popupMessages.remove(popupArrayToRemove.get(a));
        }
    }

    final /* synthetic */ void lambda$null$18$NotificationsController(int pushDialogsCount) {
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.notificationsCountUpdated, Integer.valueOf(this.currentAccount));
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.dialogsUnreadCounterChanged, Integer.valueOf(pushDialogsCount));
    }

    public void processLoadedUnreadMessages(LongSparseArray<Integer> dialogs, ArrayList<Message> messages, ArrayList<User> users, ArrayList<Chat> chats, ArrayList<EncryptedChat> encryptedChats) {
        MessagesController.getInstance(this.currentAccount).putUsers(users, true);
        MessagesController.getInstance(this.currentAccount).putChats(chats, true);
        MessagesController.getInstance(this.currentAccount).putEncryptedChats(encryptedChats, true);
        notificationsQueue.postRunnable(new NotificationsController$$Lambda$10(this, messages, dialogs));
    }

    final /* synthetic */ void lambda$processLoadedUnreadMessages$21$NotificationsController(ArrayList messages, LongSparseArray dialogs) {
        int a;
        long dialog_id;
        int index;
        boolean value;
        this.pushDialogs.clear();
        this.pushMessages.clear();
        this.pushMessagesDict.clear();
        this.total_unread_count = 0;
        this.personal_count = 0;
        SharedPreferences preferences = MessagesController.getNotificationsSettings(this.currentAccount);
        LongSparseArray<Boolean> settingsCache = new LongSparseArray();
        if (messages != null) {
            for (a = 0; a < messages.size(); a++) {
                Message message = (Message) messages.get(a);
                long mid = (long) message.id;
                if (message.to_id.channel_id != 0) {
                    mid |= ((long) message.to_id.channel_id) << 32;
                }
                if (this.pushMessagesDict.indexOfKey(mid) < 0) {
                    MessageObject messageObject = new MessageObject(this.currentAccount, message, false);
                    if (isPersonalMessage(messageObject)) {
                        this.personal_count++;
                    }
                    dialog_id = messageObject.getDialogId();
                    long original_dialog_id = dialog_id;
                    if (messageObject.messageOwner.mentioned) {
                        dialog_id = (long) messageObject.messageOwner.from_id;
                    }
                    index = settingsCache.indexOfKey(dialog_id);
                    if (index >= 0) {
                        value = ((Boolean) settingsCache.valueAt(index)).booleanValue();
                    } else {
                        int notifyOverride = getNotifyOverride(preferences, dialog_id);
                        value = notifyOverride == -1 ? ((int) dialog_id) < 0 ? preferences.getBoolean("EnableGroup", true) : preferences.getBoolean("EnableAll", true) : notifyOverride != 2;
                        settingsCache.put(dialog_id, Boolean.valueOf(value));
                    }
                    if (value && !(dialog_id == this.opened_dialog_id && ApplicationLoader.isScreenOn)) {
                        this.pushMessagesDict.put(mid, messageObject);
                        this.pushMessages.add(0, messageObject);
                        if (original_dialog_id != dialog_id) {
                            this.pushDialogsOverrideMention.put(original_dialog_id, Integer.valueOf(1));
                        }
                    }
                }
            }
        }
        for (a = 0; a < dialogs.size(); a++) {
            dialog_id = dialogs.keyAt(a);
            index = settingsCache.indexOfKey(dialog_id);
            if (index >= 0) {
                value = ((Boolean) settingsCache.valueAt(index)).booleanValue();
            } else {
                notifyOverride = getNotifyOverride(preferences, dialog_id);
                Integer override = (Integer) this.pushDialogsOverrideMention.get(dialog_id);
                if (override != null && override.intValue() == 1) {
                    this.pushDialogsOverrideMention.put(dialog_id, Integer.valueOf(0));
                    notifyOverride = 1;
                }
                value = notifyOverride == -1 ? ((int) dialog_id) < 0 ? preferences.getBoolean("EnableGroup", true) : preferences.getBoolean("EnableAll", true) : notifyOverride != 2;
                settingsCache.put(dialog_id, Boolean.valueOf(value));
            }
            if (value) {
                int count = ((Integer) dialogs.valueAt(a)).intValue();
                this.pushDialogs.put(dialog_id, Integer.valueOf(count));
                this.total_unread_count += count;
            }
        }
        AndroidUtilities.runOnUIThread(new NotificationsController$$Lambda$21(this, this.pushDialogs.size()));
        showOrUpdateNotification(SystemClock.elapsedRealtime() / 1000 < 60);
        if (this.showBadgeNumber) {
            setBadge(getTotalAllUnreadCount());
        }
    }

    final /* synthetic */ void lambda$null$20$NotificationsController(int pushDialogsCount) {
        if (this.total_unread_count == 0) {
            this.popupMessages.clear();
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.pushMessagesUpdated, new Object[0]);
        }
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.notificationsCountUpdated, Integer.valueOf(this.currentAccount));
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.dialogsUnreadCounterChanged, Integer.valueOf(pushDialogsCount));
    }

    private int getTotalAllUnreadCount() {
        int count = 0;
        for (int a = 0; a < 3; a++) {
            if (UserConfig.getInstance(a).isClientActivated()) {
                NotificationsController controller = getInstance(a);
                if (controller.showBadgeNumber) {
                    count += controller.total_unread_count;
                }
            }
        }
        return count;
    }

    public void setBadgeEnabled(boolean enabled) {
        this.showBadgeNumber = enabled;
        notificationsQueue.postRunnable(new NotificationsController$$Lambda$11(this));
    }

    final /* synthetic */ void lambda$setBadgeEnabled$22$NotificationsController() {
        setBadge(getTotalAllUnreadCount());
    }

    private void setBadge(int count) {
        if (this.lastBadgeCount != count) {
            this.lastBadgeCount = count;
            NotificationBadge.applyCount(count);
        }
    }

    private String getShortStringForMessage(MessageObject messageObject, String[] userName) {
        if (AndroidUtilities.needShowPasscode(false) || SharedConfig.isWaitingForPasscodeEnter) {
            return LocaleController.getString("YouHaveNewMessage", R.string.YouHaveNewMessage);
        }
        long dialog_id = messageObject.messageOwner.dialog_id;
        int chat_id = messageObject.messageOwner.to_id.chat_id != 0 ? messageObject.messageOwner.to_id.chat_id : messageObject.messageOwner.to_id.channel_id;
        int from_id = messageObject.messageOwner.to_id.user_id;
        if (messageObject.isFcmMessage()) {
            if (chat_id == 0 && from_id != 0) {
                if (!MessagesController.getNotificationsSettings(this.currentAccount).getBoolean("EnablePreviewAll", true)) {
                    return LocaleController.formatString("NotificationMessageNoText", R.string.NotificationMessageNoText, messageObject.localName);
                } else if (VERSION.SDK_INT > 27) {
                    userName[0] = messageObject.localName;
                }
            } else if (chat_id != 0) {
                if (MessagesController.getNotificationsSettings(this.currentAccount).getBoolean("EnablePreviewGroup", true)) {
                    if (messageObject.messageOwner.to_id.channel_id == 0 || messageObject.isMegagroup()) {
                        userName[0] = messageObject.localUserName;
                    } else if (VERSION.SDK_INT > 27) {
                        userName[0] = messageObject.localName;
                    }
                } else if (messageObject.isMegagroup() || messageObject.messageOwner.to_id.channel_id == 0) {
                    return LocaleController.formatString("NotificationMessageGroupNoText", R.string.NotificationMessageGroupNoText, messageObject.localUserName, messageObject.localName);
                } else {
                    return LocaleController.formatString("ChannelMessageNoText", R.string.ChannelMessageNoText, messageObject.localName);
                }
            }
            return messageObject.messageOwner.message;
        }
        User user;
        Chat chat;
        if (from_id == 0) {
            if (messageObject.isFromUser() || messageObject.getId() < 0) {
                from_id = messageObject.messageOwner.from_id;
            } else {
                from_id = -chat_id;
            }
        } else if (from_id == UserConfig.getInstance(this.currentAccount).getClientUserId()) {
            from_id = messageObject.messageOwner.from_id;
        }
        if (dialog_id == 0) {
            if (chat_id != 0) {
                dialog_id = (long) (-chat_id);
            } else if (from_id != 0) {
                dialog_id = (long) from_id;
            }
        }
        String name = null;
        if (from_id > 0) {
            user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(from_id));
            if (user != null) {
                name = UserObject.getUserName(user);
                if (chat_id != 0) {
                    userName[0] = name;
                } else if (VERSION.SDK_INT > 27) {
                    userName[0] = name;
                } else {
                    userName[0] = null;
                }
            }
        } else {
            chat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(-from_id));
            if (chat != null) {
                name = chat.title;
                userName[0] = name;
            }
        }
        if (name == null) {
            return null;
        }
        chat = null;
        if (chat_id != 0) {
            chat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(chat_id));
            if (chat == null) {
                return null;
            }
            if (ChatObject.isChannel(chat) && !chat.megagroup && VERSION.SDK_INT <= 27) {
                userName[0] = null;
            }
        }
        if (((int) dialog_id) == 0) {
            return LocaleController.getString("YouHaveNewMessage", R.string.YouHaveNewMessage);
        }
        SharedPreferences preferences = MessagesController.getNotificationsSettings(this.currentAccount);
        if ((chat_id != 0 || from_id == 0 || !preferences.getBoolean("EnablePreviewAll", true)) && (chat_id == 0 || !preferences.getBoolean("EnablePreviewGroup", true))) {
            return LocaleController.getString("Message", R.string.Message);
        }
        if (messageObject.messageOwner instanceof TL_messageService) {
            userName[0] = null;
            if (messageObject.messageOwner.action instanceof TL_messageActionUserJoined) {
                return LocaleController.formatString("NotificationContactJoined", R.string.NotificationContactJoined, name);
            } else if (messageObject.messageOwner.action instanceof TL_messageActionUserUpdatedPhoto) {
                return LocaleController.formatString("NotificationContactNewPhoto", R.string.NotificationContactNewPhoto, name);
            } else if (messageObject.messageOwner.action instanceof TL_messageActionLoginUnknownLocation) {
                String date = LocaleController.formatString("formatDateAtTime", R.string.formatDateAtTime, LocaleController.getInstance().formatterYear.format(((long) messageObject.messageOwner.date) * 1000), LocaleController.getInstance().formatterDay.format(((long) messageObject.messageOwner.date) * 1000));
                return LocaleController.formatString("NotificationUnrecognizedDevice", R.string.NotificationUnrecognizedDevice, UserConfig.getInstance(this.currentAccount).getCurrentUser().first_name, date, messageObject.messageOwner.action.title, messageObject.messageOwner.action.address);
            } else if ((messageObject.messageOwner.action instanceof TL_messageActionGameScore) || (messageObject.messageOwner.action instanceof TL_messageActionPaymentSent)) {
                return messageObject.messageText.toString();
            } else {
                if (messageObject.messageOwner.action instanceof TL_messageActionPhoneCall) {
                    PhoneCallDiscardReason reason = messageObject.messageOwner.action.reason;
                    if (!messageObject.isOut() && (reason instanceof TL_phoneCallDiscardReasonMissed)) {
                        return LocaleController.getString("CallMessageIncomingMissed", R.string.CallMessageIncomingMissed);
                    }
                } else if (messageObject.messageOwner.action instanceof TL_messageActionChatAddUser) {
                    int singleUserId = messageObject.messageOwner.action.user_id;
                    if (singleUserId == 0 && messageObject.messageOwner.action.users.size() == 1) {
                        singleUserId = ((Integer) messageObject.messageOwner.action.users.get(0)).intValue();
                    }
                    if (singleUserId == 0) {
                        StringBuilder stringBuilder = new StringBuilder(TtmlNode.ANONYMOUS_REGION_ID);
                        for (int a = 0; a < messageObject.messageOwner.action.users.size(); a++) {
                            user = MessagesController.getInstance(this.currentAccount).getUser((Integer) messageObject.messageOwner.action.users.get(a));
                            if (user != null) {
                                String name2 = UserObject.getUserName(user);
                                if (stringBuilder.length() != 0) {
                                    stringBuilder.append(", ");
                                }
                                stringBuilder.append(name2);
                            }
                        }
                        return LocaleController.formatString("NotificationGroupAddMember", R.string.NotificationGroupAddMember, name, chat.title, stringBuilder.toString());
                    } else if (messageObject.messageOwner.to_id.channel_id != 0 && !chat.megagroup) {
                        return LocaleController.formatString("ChannelAddedByNotification", R.string.ChannelAddedByNotification, name, chat.title);
                    } else if (singleUserId == UserConfig.getInstance(this.currentAccount).getClientUserId()) {
                        return LocaleController.formatString("NotificationInvitedToGroup", R.string.NotificationInvitedToGroup, name, chat.title);
                    } else {
                        User u2 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(singleUserId));
                        if (u2 == null) {
                            return null;
                        }
                        if (from_id != u2.id) {
                            return LocaleController.formatString("NotificationGroupAddMember", R.string.NotificationGroupAddMember, name, chat.title, UserObject.getUserName(u2));
                        } else if (chat.megagroup) {
                            return LocaleController.formatString("NotificationGroupAddSelfMega", R.string.NotificationGroupAddSelfMega, name, chat.title);
                        } else {
                            return LocaleController.formatString("NotificationGroupAddSelf", R.string.NotificationGroupAddSelf, name, chat.title);
                        }
                    }
                } else if (messageObject.messageOwner.action instanceof TL_messageActionChatJoinedByLink) {
                    return LocaleController.formatString("NotificationInvitedToGroupByLink", R.string.NotificationInvitedToGroupByLink, name, chat.title);
                } else if (messageObject.messageOwner.action instanceof TL_messageActionChatEditTitle) {
                    return LocaleController.formatString("NotificationEditedGroupName", R.string.NotificationEditedGroupName, name, messageObject.messageOwner.action.title);
                } else if ((messageObject.messageOwner.action instanceof TL_messageActionChatEditPhoto) || (messageObject.messageOwner.action instanceof TL_messageActionChatDeletePhoto)) {
                    if (messageObject.messageOwner.to_id.channel_id == 0 || chat.megagroup) {
                        return LocaleController.formatString("NotificationEditedGroupPhoto", R.string.NotificationEditedGroupPhoto, name, chat.title);
                    }
                    return LocaleController.formatString("ChannelPhotoEditNotification", R.string.ChannelPhotoEditNotification, chat.title);
                } else if (messageObject.messageOwner.action instanceof TL_messageActionChatDeleteUser) {
                    if (messageObject.messageOwner.action.user_id == UserConfig.getInstance(this.currentAccount).getClientUserId()) {
                        return LocaleController.formatString("NotificationGroupKickYou", R.string.NotificationGroupKickYou, name, chat.title);
                    } else if (messageObject.messageOwner.action.user_id == from_id) {
                        return LocaleController.formatString("NotificationGroupLeftMember", R.string.NotificationGroupLeftMember, name, chat.title);
                    } else {
                        if (MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(messageObject.messageOwner.action.user_id)) == null) {
                            return null;
                        }
                        return LocaleController.formatString("NotificationGroupKickMember", R.string.NotificationGroupKickMember, name, chat.title, UserObject.getUserName(MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(messageObject.messageOwner.action.user_id))));
                    }
                } else if (messageObject.messageOwner.action instanceof TL_messageActionChatCreate) {
                    return messageObject.messageText.toString();
                } else {
                    if (messageObject.messageOwner.action instanceof TL_messageActionChannelCreate) {
                        return messageObject.messageText.toString();
                    }
                    if (messageObject.messageOwner.action instanceof TL_messageActionChatMigrateTo) {
                        return LocaleController.formatString("ActionMigrateFromGroupNotify", R.string.ActionMigrateFromGroupNotify, chat.title);
                    } else if (messageObject.messageOwner.action instanceof TL_messageActionChannelMigrateFrom) {
                        return LocaleController.formatString("ActionMigrateFromGroupNotify", R.string.ActionMigrateFromGroupNotify, messageObject.messageOwner.action.title);
                    } else if (messageObject.messageOwner.action instanceof TL_messageActionScreenshotTaken) {
                        return messageObject.messageText.toString();
                    } else {
                        if (messageObject.messageOwner.action instanceof TL_messageActionPinMessage) {
                            MessageObject object;
                            String message;
                            CharSequence message2;
                            if (chat == null || !chat.megagroup) {
                                if (messageObject.replyMessageObject == null) {
                                    return LocaleController.formatString("NotificationActionPinnedNoTextChannel", R.string.NotificationActionPinnedNoTextChannel, chat.title);
                                }
                                object = messageObject.replyMessageObject;
                                if (object.isMusic()) {
                                    return LocaleController.formatString("NotificationActionPinnedMusicChannel", R.string.NotificationActionPinnedMusicChannel, chat.title);
                                } else if (object.isVideo()) {
                                    if (VERSION.SDK_INT < 19 || TextUtils.isEmpty(object.messageOwner.message)) {
                                        return LocaleController.formatString("NotificationActionPinnedVideoChannel", R.string.NotificationActionPinnedVideoChannel, chat.title);
                                    }
                                    message = "📹 " + object.messageOwner.message;
                                    return LocaleController.formatString("NotificationActionPinnedTextChannel", R.string.NotificationActionPinnedTextChannel, chat.title, message);
                                } else if (object.isGif()) {
                                    if (VERSION.SDK_INT < 19 || TextUtils.isEmpty(object.messageOwner.message)) {
                                        return LocaleController.formatString("NotificationActionPinnedGifChannel", R.string.NotificationActionPinnedGifChannel, chat.title);
                                    }
                                    message = "🎬 " + object.messageOwner.message;
                                    return LocaleController.formatString("NotificationActionPinnedTextChannel", R.string.NotificationActionPinnedTextChannel, chat.title, message);
                                } else if (object.isVoice()) {
                                    return LocaleController.formatString("NotificationActionPinnedVoiceChannel", R.string.NotificationActionPinnedVoiceChannel, chat.title);
                                } else if (object.isRoundVideo()) {
                                    return LocaleController.formatString("NotificationActionPinnedRoundChannel", R.string.NotificationActionPinnedRoundChannel, chat.title);
                                } else if (object.isSticker()) {
                                    if (object.getStickerEmoji() != null) {
                                        return LocaleController.formatString("NotificationActionPinnedStickerEmojiChannel", R.string.NotificationActionPinnedStickerEmojiChannel, chat.title, object.getStickerEmoji());
                                    }
                                    return LocaleController.formatString("NotificationActionPinnedStickerChannel", R.string.NotificationActionPinnedStickerChannel, chat.title);
                                } else if (object.messageOwner.media instanceof TL_messageMediaDocument) {
                                    if (VERSION.SDK_INT < 19 || TextUtils.isEmpty(object.messageOwner.message)) {
                                        return LocaleController.formatString("NotificationActionPinnedFileChannel", R.string.NotificationActionPinnedFileChannel, chat.title);
                                    }
                                    message = "📎 " + object.messageOwner.message;
                                    return LocaleController.formatString("NotificationActionPinnedTextChannel", R.string.NotificationActionPinnedTextChannel, chat.title, message);
                                } else if ((object.messageOwner.media instanceof TL_messageMediaGeo) || (object.messageOwner.media instanceof TL_messageMediaVenue)) {
                                    return LocaleController.formatString("NotificationActionPinnedGeoChannel", R.string.NotificationActionPinnedGeoChannel, chat.title);
                                } else if (object.messageOwner.media instanceof TL_messageMediaGeoLive) {
                                    return LocaleController.formatString("NotificationActionPinnedGeoLiveChannel", R.string.NotificationActionPinnedGeoLiveChannel, chat.title);
                                } else if (object.messageOwner.media instanceof TL_messageMediaContact) {
                                    return LocaleController.formatString("NotificationActionPinnedContactChannel", R.string.NotificationActionPinnedContactChannel, chat.title);
                                } else if (object.messageOwner.media instanceof TL_messageMediaPhoto) {
                                    if (VERSION.SDK_INT < 19 || TextUtils.isEmpty(object.messageOwner.message)) {
                                        return LocaleController.formatString("NotificationActionPinnedPhotoChannel", R.string.NotificationActionPinnedPhotoChannel, chat.title);
                                    }
                                    message = "🖼 " + object.messageOwner.message;
                                    return LocaleController.formatString("NotificationActionPinnedTextChannel", R.string.NotificationActionPinnedTextChannel, chat.title, message);
                                } else if (object.messageOwner.media instanceof TL_messageMediaGame) {
                                    return LocaleController.formatString("NotificationActionPinnedGameChannel", R.string.NotificationActionPinnedGameChannel, chat.title);
                                } else if (object.messageText == null || object.messageText.length() <= 0) {
                                    return LocaleController.formatString("NotificationActionPinnedNoTextChannel", R.string.NotificationActionPinnedNoTextChannel, chat.title);
                                } else {
                                    message2 = object.messageText;
                                    if (message2.length() > 20) {
                                        message2 = message2.subSequence(0, 20) + "...";
                                    }
                                    return LocaleController.formatString("NotificationActionPinnedTextChannel", R.string.NotificationActionPinnedTextChannel, chat.title, message2);
                                }
                            } else if (messageObject.replyMessageObject == null) {
                                return LocaleController.formatString("NotificationActionPinnedNoText", R.string.NotificationActionPinnedNoText, name, chat.title);
                            } else {
                                object = messageObject.replyMessageObject;
                                if (object.isMusic()) {
                                    return LocaleController.formatString("NotificationActionPinnedMusic", R.string.NotificationActionPinnedMusic, name, chat.title);
                                } else if (object.isVideo()) {
                                    if (VERSION.SDK_INT < 19 || TextUtils.isEmpty(object.messageOwner.message)) {
                                        return LocaleController.formatString("NotificationActionPinnedVideo", R.string.NotificationActionPinnedVideo, name, chat.title);
                                    }
                                    message = "📹 " + object.messageOwner.message;
                                    return LocaleController.formatString("NotificationActionPinnedText", R.string.NotificationActionPinnedText, name, message, chat.title);
                                } else if (object.isGif()) {
                                    if (VERSION.SDK_INT < 19 || TextUtils.isEmpty(object.messageOwner.message)) {
                                        return LocaleController.formatString("NotificationActionPinnedGif", R.string.NotificationActionPinnedGif, name, chat.title);
                                    }
                                    message = "🎬 " + object.messageOwner.message;
                                    return LocaleController.formatString("NotificationActionPinnedText", R.string.NotificationActionPinnedText, name, message, chat.title);
                                } else if (object.isVoice()) {
                                    return LocaleController.formatString("NotificationActionPinnedVoice", R.string.NotificationActionPinnedVoice, name, chat.title);
                                } else if (object.isRoundVideo()) {
                                    return LocaleController.formatString("NotificationActionPinnedRound", R.string.NotificationActionPinnedRound, name, chat.title);
                                } else if (object.isSticker()) {
                                    if (object.getStickerEmoji() != null) {
                                        return LocaleController.formatString("NotificationActionPinnedStickerEmoji", R.string.NotificationActionPinnedStickerEmoji, name, chat.title, object.getStickerEmoji());
                                    }
                                    return LocaleController.formatString("NotificationActionPinnedSticker", R.string.NotificationActionPinnedSticker, name, chat.title);
                                } else if (object.messageOwner.media instanceof TL_messageMediaDocument) {
                                    if (VERSION.SDK_INT < 19 || TextUtils.isEmpty(object.messageOwner.message)) {
                                        return LocaleController.formatString("NotificationActionPinnedFile", R.string.NotificationActionPinnedFile, name, chat.title);
                                    }
                                    message = "📎 " + object.messageOwner.message;
                                    return LocaleController.formatString("NotificationActionPinnedText", R.string.NotificationActionPinnedText, name, message, chat.title);
                                } else if ((object.messageOwner.media instanceof TL_messageMediaGeo) || (object.messageOwner.media instanceof TL_messageMediaVenue)) {
                                    return LocaleController.formatString("NotificationActionPinnedGeo", R.string.NotificationActionPinnedGeo, name, chat.title);
                                } else if (object.messageOwner.media instanceof TL_messageMediaGeoLive) {
                                    return LocaleController.formatString("NotificationActionPinnedGeoLive", R.string.NotificationActionPinnedGeoLive, name, chat.title);
                                } else if (object.messageOwner.media instanceof TL_messageMediaContact) {
                                    return LocaleController.formatString("NotificationActionPinnedContact", R.string.NotificationActionPinnedContact, name, chat.title);
                                } else if (object.messageOwner.media instanceof TL_messageMediaPhoto) {
                                    if (VERSION.SDK_INT < 19 || TextUtils.isEmpty(object.messageOwner.message)) {
                                        return LocaleController.formatString("NotificationActionPinnedPhoto", R.string.NotificationActionPinnedPhoto, name, chat.title);
                                    }
                                    message = "🖼 " + object.messageOwner.message;
                                    return LocaleController.formatString("NotificationActionPinnedText", R.string.NotificationActionPinnedText, name, message, chat.title);
                                } else if (object.messageOwner.media instanceof TL_messageMediaGame) {
                                    return LocaleController.formatString("NotificationActionPinnedGame", R.string.NotificationActionPinnedGame, name, chat.title);
                                } else if (object.messageText == null || object.messageText.length() <= 0) {
                                    return LocaleController.formatString("NotificationActionPinnedNoText", R.string.NotificationActionPinnedNoText, name, chat.title);
                                } else {
                                    message2 = object.messageText;
                                    if (message2.length() > 20) {
                                        message2 = message2.subSequence(0, 20) + "...";
                                    }
                                    return LocaleController.formatString("NotificationActionPinnedText", R.string.NotificationActionPinnedText, name, message2, chat.title);
                                }
                            }
                        } else if (messageObject.messageOwner.action instanceof TL_messageActionGameScore) {
                            return messageObject.messageText.toString();
                        }
                    }
                }
            }
        } else if (messageObject.isMediaEmpty()) {
            if (TextUtils.isEmpty(messageObject.messageOwner.message)) {
                return LocaleController.getString("Message", R.string.Message);
            }
            return messageObject.messageOwner.message;
        } else if (messageObject.messageOwner.media instanceof TL_messageMediaPhoto) {
            if (VERSION.SDK_INT >= 19 && !TextUtils.isEmpty(messageObject.messageOwner.message)) {
                return "🖼 " + messageObject.messageOwner.message;
            }
            if (messageObject.messageOwner.media.ttl_seconds != 0) {
                return LocaleController.getString("AttachDestructingPhoto", R.string.AttachDestructingPhoto);
            }
            return LocaleController.getString("AttachPhoto", R.string.AttachPhoto);
        } else if (messageObject.isVideo()) {
            if (VERSION.SDK_INT >= 19 && !TextUtils.isEmpty(messageObject.messageOwner.message)) {
                return "📹 " + messageObject.messageOwner.message;
            }
            if (messageObject.messageOwner.media.ttl_seconds != 0) {
                return LocaleController.getString("AttachDestructingVideo", R.string.AttachDestructingVideo);
            }
            return LocaleController.getString("AttachVideo", R.string.AttachVideo);
        } else if (messageObject.isGame()) {
            return LocaleController.getString("AttachGame", R.string.AttachGame);
        } else {
            if (messageObject.isVoice()) {
                return LocaleController.getString("AttachAudio", R.string.AttachAudio);
            }
            if (messageObject.isRoundVideo()) {
                return LocaleController.getString("AttachRound", R.string.AttachRound);
            }
            if (messageObject.isMusic()) {
                return LocaleController.getString("AttachMusic", R.string.AttachMusic);
            }
            if (messageObject.messageOwner.media instanceof TL_messageMediaContact) {
                return LocaleController.getString("AttachContact", R.string.AttachContact);
            }
            if ((messageObject.messageOwner.media instanceof TL_messageMediaGeo) || (messageObject.messageOwner.media instanceof TL_messageMediaVenue)) {
                return LocaleController.getString("AttachLocation", R.string.AttachLocation);
            }
            if (messageObject.messageOwner.media instanceof TL_messageMediaGeoLive) {
                return LocaleController.getString("AttachLiveLocation", R.string.AttachLiveLocation);
            }
            if (messageObject.messageOwner.media instanceof TL_messageMediaDocument) {
                if (messageObject.isSticker()) {
                    String emoji = messageObject.getStickerEmoji();
                    if (emoji != null) {
                        return emoji + " " + LocaleController.getString("AttachSticker", R.string.AttachSticker);
                    }
                    return LocaleController.getString("AttachSticker", R.string.AttachSticker);
                } else if (messageObject.isGif()) {
                    if (VERSION.SDK_INT < 19 || TextUtils.isEmpty(messageObject.messageOwner.message)) {
                        return LocaleController.getString("AttachGif", R.string.AttachGif);
                    }
                    return "🎬 " + messageObject.messageOwner.message;
                } else if (VERSION.SDK_INT < 19 || TextUtils.isEmpty(messageObject.messageOwner.message)) {
                    return LocaleController.getString("AttachDocument", R.string.AttachDocument);
                } else {
                    return "📎 " + messageObject.messageOwner.message;
                }
            }
        }
        return null;
    }

    private String getStringForMessage(MessageObject messageObject, boolean shortMessage, boolean[] text) {
        if (AndroidUtilities.needShowPasscode(false) || SharedConfig.isWaitingForPasscodeEnter) {
            return LocaleController.getString("YouHaveNewMessage", R.string.YouHaveNewMessage);
        }
        long dialog_id = messageObject.messageOwner.dialog_id;
        int chat_id = messageObject.messageOwner.to_id.chat_id != 0 ? messageObject.messageOwner.to_id.chat_id : messageObject.messageOwner.to_id.channel_id;
        int from_id = messageObject.messageOwner.to_id.user_id;
        if (messageObject.isFcmMessage()) {
            if (chat_id == 0 && from_id != 0) {
                if (!MessagesController.getNotificationsSettings(this.currentAccount).getBoolean("EnablePreviewAll", true)) {
                    return LocaleController.formatString("NotificationMessageNoText", R.string.NotificationMessageNoText, messageObject.localName);
                }
            } else if (chat_id != 0) {
                if (!MessagesController.getNotificationsSettings(this.currentAccount).getBoolean("EnablePreviewGroup", true)) {
                    if (messageObject.isMegagroup() || messageObject.messageOwner.to_id.channel_id == 0) {
                        return LocaleController.formatString("NotificationMessageGroupNoText", R.string.NotificationMessageGroupNoText, messageObject.localUserName, messageObject.localName);
                    }
                    return LocaleController.formatString("ChannelMessageNoText", R.string.ChannelMessageNoText, messageObject.localName);
                }
            }
            text[0] = true;
            return (String) messageObject.messageText;
        }
        User user;
        Chat chat;
        if (from_id == 0) {
            if (messageObject.isFromUser() || messageObject.getId() < 0) {
                from_id = messageObject.messageOwner.from_id;
            } else {
                from_id = -chat_id;
            }
        } else if (from_id == UserConfig.getInstance(this.currentAccount).getClientUserId()) {
            from_id = messageObject.messageOwner.from_id;
        }
        if (dialog_id == 0) {
            if (chat_id != 0) {
                dialog_id = (long) (-chat_id);
            } else if (from_id != 0) {
                dialog_id = (long) from_id;
            }
        }
        String name = null;
        if (from_id > 0) {
            user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(from_id));
            if (user != null) {
                name = UserObject.getUserName(user);
            }
        } else {
            chat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(-from_id));
            if (chat != null) {
                name = chat.title;
            }
        }
        if (name == null) {
            return null;
        }
        chat = null;
        if (chat_id != 0) {
            chat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(chat_id));
            if (chat == null) {
                return null;
            }
        }
        String msg = null;
        if (((int) dialog_id) == 0) {
            msg = LocaleController.getString("YouHaveNewMessage", R.string.YouHaveNewMessage);
        } else if (chat_id == 0 && from_id != 0) {
            if (!MessagesController.getNotificationsSettings(this.currentAccount).getBoolean("EnablePreviewAll", true)) {
                msg = LocaleController.formatString("NotificationMessageNoText", R.string.NotificationMessageNoText, name);
            } else if (messageObject.messageOwner instanceof TL_messageService) {
                if (messageObject.messageOwner.action instanceof TL_messageActionUserJoined) {
                    msg = LocaleController.formatString("NotificationContactJoined", R.string.NotificationContactJoined, name);
                } else if (messageObject.messageOwner.action instanceof TL_messageActionUserUpdatedPhoto) {
                    msg = LocaleController.formatString("NotificationContactNewPhoto", R.string.NotificationContactNewPhoto, name);
                } else if (messageObject.messageOwner.action instanceof TL_messageActionLoginUnknownLocation) {
                    String date = LocaleController.formatString("formatDateAtTime", R.string.formatDateAtTime, LocaleController.getInstance().formatterYear.format(((long) messageObject.messageOwner.date) * 1000), LocaleController.getInstance().formatterDay.format(((long) messageObject.messageOwner.date) * 1000));
                    msg = LocaleController.formatString("NotificationUnrecognizedDevice", R.string.NotificationUnrecognizedDevice, UserConfig.getInstance(this.currentAccount).getCurrentUser().first_name, date, messageObject.messageOwner.action.title, messageObject.messageOwner.action.address);
                } else if ((messageObject.messageOwner.action instanceof TL_messageActionGameScore) || (messageObject.messageOwner.action instanceof TL_messageActionPaymentSent)) {
                    msg = messageObject.messageText.toString();
                } else if (messageObject.messageOwner.action instanceof TL_messageActionPhoneCall) {
                    PhoneCallDiscardReason reason = messageObject.messageOwner.action.reason;
                    if (!messageObject.isOut() && (reason instanceof TL_phoneCallDiscardReasonMissed)) {
                        msg = LocaleController.getString("CallMessageIncomingMissed", R.string.CallMessageIncomingMissed);
                    }
                }
            } else if (messageObject.isMediaEmpty()) {
                if (shortMessage) {
                    msg = LocaleController.formatString("NotificationMessageNoText", R.string.NotificationMessageNoText, name);
                } else if (TextUtils.isEmpty(messageObject.messageOwner.message)) {
                    msg = LocaleController.formatString("NotificationMessageNoText", R.string.NotificationMessageNoText, name);
                } else {
                    msg = LocaleController.formatString("NotificationMessageText", R.string.NotificationMessageText, name, messageObject.messageOwner.message);
                    text[0] = true;
                }
            } else if (messageObject.messageOwner.media instanceof TL_messageMediaPhoto) {
                if (shortMessage || VERSION.SDK_INT < 19 || TextUtils.isEmpty(messageObject.messageOwner.message)) {
                    msg = messageObject.messageOwner.media.ttl_seconds != 0 ? LocaleController.formatString("NotificationMessageSDPhoto", R.string.NotificationMessageSDPhoto, name) : LocaleController.formatString("NotificationMessagePhoto", R.string.NotificationMessagePhoto, name);
                } else {
                    msg = LocaleController.formatString("NotificationMessageText", R.string.NotificationMessageText, name, "🖼 " + messageObject.messageOwner.message);
                    text[0] = true;
                }
            } else if (messageObject.isVideo()) {
                if (shortMessage || VERSION.SDK_INT < 19 || TextUtils.isEmpty(messageObject.messageOwner.message)) {
                    msg = messageObject.messageOwner.media.ttl_seconds != 0 ? LocaleController.formatString("NotificationMessageSDVideo", R.string.NotificationMessageSDVideo, name) : LocaleController.formatString("NotificationMessageVideo", R.string.NotificationMessageVideo, name);
                } else {
                    msg = LocaleController.formatString("NotificationMessageText", R.string.NotificationMessageText, name, "📹 " + messageObject.messageOwner.message);
                    text[0] = true;
                }
            } else if (messageObject.isGame()) {
                msg = LocaleController.formatString("NotificationMessageGame", R.string.NotificationMessageGame, name, messageObject.messageOwner.media.game.title);
            } else if (messageObject.isVoice()) {
                msg = LocaleController.formatString("NotificationMessageAudio", R.string.NotificationMessageAudio, name);
            } else if (messageObject.isRoundVideo()) {
                msg = LocaleController.formatString("NotificationMessageRound", R.string.NotificationMessageRound, name);
            } else if (messageObject.isMusic()) {
                msg = LocaleController.formatString("NotificationMessageMusic", R.string.NotificationMessageMusic, name);
            } else if (messageObject.messageOwner.media instanceof TL_messageMediaContact) {
                msg = LocaleController.formatString("NotificationMessageContact", R.string.NotificationMessageContact, name);
            } else if ((messageObject.messageOwner.media instanceof TL_messageMediaGeo) || (messageObject.messageOwner.media instanceof TL_messageMediaVenue)) {
                msg = LocaleController.formatString("NotificationMessageMap", R.string.NotificationMessageMap, name);
            } else if (messageObject.messageOwner.media instanceof TL_messageMediaGeoLive) {
                msg = LocaleController.formatString("NotificationMessageLiveLocation", R.string.NotificationMessageLiveLocation, name);
            } else if (messageObject.messageOwner.media instanceof TL_messageMediaDocument) {
                if (messageObject.isSticker()) {
                    msg = messageObject.getStickerEmoji() != null ? LocaleController.formatString("NotificationMessageStickerEmoji", R.string.NotificationMessageStickerEmoji, name, messageObject.getStickerEmoji()) : LocaleController.formatString("NotificationMessageSticker", R.string.NotificationMessageSticker, name);
                } else if (messageObject.isGif()) {
                    if (shortMessage || VERSION.SDK_INT < 19 || TextUtils.isEmpty(messageObject.messageOwner.message)) {
                        msg = LocaleController.formatString("NotificationMessageGif", R.string.NotificationMessageGif, name);
                    } else {
                        msg = LocaleController.formatString("NotificationMessageText", R.string.NotificationMessageText, name, "🎬 " + messageObject.messageOwner.message);
                        text[0] = true;
                    }
                } else if (shortMessage || VERSION.SDK_INT < 19 || TextUtils.isEmpty(messageObject.messageOwner.message)) {
                    msg = LocaleController.formatString("NotificationMessageDocument", R.string.NotificationMessageDocument, name);
                } else {
                    msg = LocaleController.formatString("NotificationMessageText", R.string.NotificationMessageText, name, "📎 " + messageObject.messageOwner.message);
                    text[0] = true;
                }
            }
        } else if (chat_id != 0) {
            if (!MessagesController.getNotificationsSettings(this.currentAccount).getBoolean("EnablePreviewGroup", true)) {
                msg = (!ChatObject.isChannel(chat) || chat.megagroup) ? LocaleController.formatString("NotificationMessageGroupNoText", R.string.NotificationMessageGroupNoText, name, chat.title) : LocaleController.formatString("ChannelMessageNoText", R.string.ChannelMessageNoText, name);
            } else if (messageObject.messageOwner instanceof TL_messageService) {
                if (messageObject.messageOwner.action instanceof TL_messageActionChatAddUser) {
                    int singleUserId = messageObject.messageOwner.action.user_id;
                    if (singleUserId == 0 && messageObject.messageOwner.action.users.size() == 1) {
                        singleUserId = ((Integer) messageObject.messageOwner.action.users.get(0)).intValue();
                    }
                    if (singleUserId == 0) {
                        StringBuilder stringBuilder = new StringBuilder(TtmlNode.ANONYMOUS_REGION_ID);
                        for (int a = 0; a < messageObject.messageOwner.action.users.size(); a++) {
                            user = MessagesController.getInstance(this.currentAccount).getUser((Integer) messageObject.messageOwner.action.users.get(a));
                            if (user != null) {
                                String name2 = UserObject.getUserName(user);
                                if (stringBuilder.length() != 0) {
                                    stringBuilder.append(", ");
                                }
                                stringBuilder.append(name2);
                            }
                        }
                        msg = LocaleController.formatString("NotificationGroupAddMember", R.string.NotificationGroupAddMember, name, chat.title, stringBuilder.toString());
                    } else if (messageObject.messageOwner.to_id.channel_id != 0 && !chat.megagroup) {
                        msg = LocaleController.formatString("ChannelAddedByNotification", R.string.ChannelAddedByNotification, name, chat.title);
                    } else if (singleUserId == UserConfig.getInstance(this.currentAccount).getClientUserId()) {
                        msg = LocaleController.formatString("NotificationInvitedToGroup", R.string.NotificationInvitedToGroup, name, chat.title);
                    } else {
                        User u2 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(singleUserId));
                        if (u2 == null) {
                            return null;
                        }
                        msg = from_id == u2.id ? chat.megagroup ? LocaleController.formatString("NotificationGroupAddSelfMega", R.string.NotificationGroupAddSelfMega, name, chat.title) : LocaleController.formatString("NotificationGroupAddSelf", R.string.NotificationGroupAddSelf, name, chat.title) : LocaleController.formatString("NotificationGroupAddMember", R.string.NotificationGroupAddMember, name, chat.title, UserObject.getUserName(u2));
                    }
                } else if (messageObject.messageOwner.action instanceof TL_messageActionChatJoinedByLink) {
                    msg = LocaleController.formatString("NotificationInvitedToGroupByLink", R.string.NotificationInvitedToGroupByLink, name, chat.title);
                } else if (messageObject.messageOwner.action instanceof TL_messageActionChatEditTitle) {
                    msg = LocaleController.formatString("NotificationEditedGroupName", R.string.NotificationEditedGroupName, name, messageObject.messageOwner.action.title);
                } else if ((messageObject.messageOwner.action instanceof TL_messageActionChatEditPhoto) || (messageObject.messageOwner.action instanceof TL_messageActionChatDeletePhoto)) {
                    msg = (messageObject.messageOwner.to_id.channel_id == 0 || chat.megagroup) ? LocaleController.formatString("NotificationEditedGroupPhoto", R.string.NotificationEditedGroupPhoto, name, chat.title) : LocaleController.formatString("ChannelPhotoEditNotification", R.string.ChannelPhotoEditNotification, chat.title);
                } else if (messageObject.messageOwner.action instanceof TL_messageActionChatDeleteUser) {
                    if (messageObject.messageOwner.action.user_id == UserConfig.getInstance(this.currentAccount).getClientUserId()) {
                        msg = LocaleController.formatString("NotificationGroupKickYou", R.string.NotificationGroupKickYou, name, chat.title);
                    } else if (messageObject.messageOwner.action.user_id == from_id) {
                        msg = LocaleController.formatString("NotificationGroupLeftMember", R.string.NotificationGroupLeftMember, name, chat.title);
                    } else {
                        if (MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(messageObject.messageOwner.action.user_id)) == null) {
                            return null;
                        }
                        msg = LocaleController.formatString("NotificationGroupKickMember", R.string.NotificationGroupKickMember, name, chat.title, UserObject.getUserName(MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(messageObject.messageOwner.action.user_id))));
                    }
                } else if (messageObject.messageOwner.action instanceof TL_messageActionChatCreate) {
                    msg = messageObject.messageText.toString();
                } else if (messageObject.messageOwner.action instanceof TL_messageActionChannelCreate) {
                    msg = messageObject.messageText.toString();
                } else if (messageObject.messageOwner.action instanceof TL_messageActionChatMigrateTo) {
                    msg = LocaleController.formatString("ActionMigrateFromGroupNotify", R.string.ActionMigrateFromGroupNotify, chat.title);
                } else if (messageObject.messageOwner.action instanceof TL_messageActionChannelMigrateFrom) {
                    msg = LocaleController.formatString("ActionMigrateFromGroupNotify", R.string.ActionMigrateFromGroupNotify, messageObject.messageOwner.action.title);
                } else if (messageObject.messageOwner.action instanceof TL_messageActionScreenshotTaken) {
                    msg = messageObject.messageText.toString();
                } else if (messageObject.messageOwner.action instanceof TL_messageActionPinMessage) {
                    MessageObject object;
                    String message;
                    CharSequence message2;
                    if (chat == null || !chat.megagroup) {
                        if (messageObject.replyMessageObject == null) {
                            msg = LocaleController.formatString("NotificationActionPinnedNoTextChannel", R.string.NotificationActionPinnedNoTextChannel, chat.title);
                        } else {
                            object = messageObject.replyMessageObject;
                            if (object.isMusic()) {
                                msg = LocaleController.formatString("NotificationActionPinnedMusicChannel", R.string.NotificationActionPinnedMusicChannel, chat.title);
                            } else if (object.isVideo()) {
                                if (VERSION.SDK_INT < 19 || TextUtils.isEmpty(object.messageOwner.message)) {
                                    msg = LocaleController.formatString("NotificationActionPinnedVideoChannel", R.string.NotificationActionPinnedVideoChannel, chat.title);
                                } else {
                                    message = "📹 " + object.messageOwner.message;
                                    msg = LocaleController.formatString("NotificationActionPinnedTextChannel", R.string.NotificationActionPinnedTextChannel, chat.title, message);
                                }
                            } else if (object.isGif()) {
                                if (VERSION.SDK_INT < 19 || TextUtils.isEmpty(object.messageOwner.message)) {
                                    msg = LocaleController.formatString("NotificationActionPinnedGifChannel", R.string.NotificationActionPinnedGifChannel, chat.title);
                                } else {
                                    message = "🎬 " + object.messageOwner.message;
                                    msg = LocaleController.formatString("NotificationActionPinnedTextChannel", R.string.NotificationActionPinnedTextChannel, chat.title, message);
                                }
                            } else if (object.isVoice()) {
                                msg = LocaleController.formatString("NotificationActionPinnedVoiceChannel", R.string.NotificationActionPinnedVoiceChannel, chat.title);
                            } else if (object.isRoundVideo()) {
                                msg = LocaleController.formatString("NotificationActionPinnedRoundChannel", R.string.NotificationActionPinnedRoundChannel, chat.title);
                            } else if (object.isSticker()) {
                                msg = object.getStickerEmoji() != null ? LocaleController.formatString("NotificationActionPinnedStickerEmojiChannel", R.string.NotificationActionPinnedStickerEmojiChannel, chat.title, object.getStickerEmoji()) : LocaleController.formatString("NotificationActionPinnedStickerChannel", R.string.NotificationActionPinnedStickerChannel, chat.title);
                            } else if (object.messageOwner.media instanceof TL_messageMediaDocument) {
                                if (VERSION.SDK_INT < 19 || TextUtils.isEmpty(object.messageOwner.message)) {
                                    msg = LocaleController.formatString("NotificationActionPinnedFileChannel", R.string.NotificationActionPinnedFileChannel, chat.title);
                                } else {
                                    message = "📎 " + object.messageOwner.message;
                                    msg = LocaleController.formatString("NotificationActionPinnedTextChannel", R.string.NotificationActionPinnedTextChannel, chat.title, message);
                                }
                            } else if ((object.messageOwner.media instanceof TL_messageMediaGeo) || (object.messageOwner.media instanceof TL_messageMediaVenue)) {
                                msg = LocaleController.formatString("NotificationActionPinnedGeoChannel", R.string.NotificationActionPinnedGeoChannel, chat.title);
                            } else if (object.messageOwner.media instanceof TL_messageMediaGeoLive) {
                                msg = LocaleController.formatString("NotificationActionPinnedGeoLiveChannel", R.string.NotificationActionPinnedGeoLiveChannel, chat.title);
                            } else if (object.messageOwner.media instanceof TL_messageMediaContact) {
                                msg = LocaleController.formatString("NotificationActionPinnedContactChannel", R.string.NotificationActionPinnedContactChannel, chat.title);
                            } else if (object.messageOwner.media instanceof TL_messageMediaPhoto) {
                                if (VERSION.SDK_INT < 19 || TextUtils.isEmpty(object.messageOwner.message)) {
                                    msg = LocaleController.formatString("NotificationActionPinnedPhotoChannel", R.string.NotificationActionPinnedPhotoChannel, chat.title);
                                } else {
                                    message = "🖼 " + object.messageOwner.message;
                                    msg = LocaleController.formatString("NotificationActionPinnedTextChannel", R.string.NotificationActionPinnedTextChannel, chat.title, message);
                                }
                            } else if (object.messageOwner.media instanceof TL_messageMediaGame) {
                                msg = LocaleController.formatString("NotificationActionPinnedGameChannel", R.string.NotificationActionPinnedGameChannel, chat.title);
                            } else if (object.messageText == null || object.messageText.length() <= 0) {
                                msg = LocaleController.formatString("NotificationActionPinnedNoTextChannel", R.string.NotificationActionPinnedNoTextChannel, chat.title);
                            } else {
                                message2 = object.messageText;
                                if (message2.length() > 20) {
                                    message2 = message2.subSequence(0, 20) + "...";
                                }
                                msg = LocaleController.formatString("NotificationActionPinnedTextChannel", R.string.NotificationActionPinnedTextChannel, chat.title, message2);
                            }
                        }
                    } else if (messageObject.replyMessageObject == null) {
                        msg = LocaleController.formatString("NotificationActionPinnedNoText", R.string.NotificationActionPinnedNoText, name, chat.title);
                    } else {
                        object = messageObject.replyMessageObject;
                        if (object.isMusic()) {
                            msg = LocaleController.formatString("NotificationActionPinnedMusic", R.string.NotificationActionPinnedMusic, name, chat.title);
                        } else if (object.isVideo()) {
                            if (VERSION.SDK_INT < 19 || TextUtils.isEmpty(object.messageOwner.message)) {
                                msg = LocaleController.formatString("NotificationActionPinnedVideo", R.string.NotificationActionPinnedVideo, name, chat.title);
                            } else {
                                message = "📹 " + object.messageOwner.message;
                                msg = LocaleController.formatString("NotificationActionPinnedText", R.string.NotificationActionPinnedText, name, message, chat.title);
                            }
                        } else if (object.isGif()) {
                            if (VERSION.SDK_INT < 19 || TextUtils.isEmpty(object.messageOwner.message)) {
                                msg = LocaleController.formatString("NotificationActionPinnedGif", R.string.NotificationActionPinnedGif, name, chat.title);
                            } else {
                                message = "🎬 " + object.messageOwner.message;
                                msg = LocaleController.formatString("NotificationActionPinnedText", R.string.NotificationActionPinnedText, name, message, chat.title);
                            }
                        } else if (object.isVoice()) {
                            msg = LocaleController.formatString("NotificationActionPinnedVoice", R.string.NotificationActionPinnedVoice, name, chat.title);
                        } else if (object.isRoundVideo()) {
                            msg = LocaleController.formatString("NotificationActionPinnedRound", R.string.NotificationActionPinnedRound, name, chat.title);
                        } else if (object.isSticker()) {
                            msg = object.getStickerEmoji() != null ? LocaleController.formatString("NotificationActionPinnedStickerEmoji", R.string.NotificationActionPinnedStickerEmoji, name, chat.title, object.getStickerEmoji()) : LocaleController.formatString("NotificationActionPinnedSticker", R.string.NotificationActionPinnedSticker, name, chat.title);
                        } else if (object.messageOwner.media instanceof TL_messageMediaDocument) {
                            if (VERSION.SDK_INT < 19 || TextUtils.isEmpty(object.messageOwner.message)) {
                                msg = LocaleController.formatString("NotificationActionPinnedFile", R.string.NotificationActionPinnedFile, name, chat.title);
                            } else {
                                message = "📎 " + object.messageOwner.message;
                                msg = LocaleController.formatString("NotificationActionPinnedText", R.string.NotificationActionPinnedText, name, message, chat.title);
                            }
                        } else if ((object.messageOwner.media instanceof TL_messageMediaGeo) || (object.messageOwner.media instanceof TL_messageMediaVenue)) {
                            msg = LocaleController.formatString("NotificationActionPinnedGeo", R.string.NotificationActionPinnedGeo, name, chat.title);
                        } else if (object.messageOwner.media instanceof TL_messageMediaGeoLive) {
                            msg = LocaleController.formatString("NotificationActionPinnedGeoLive", R.string.NotificationActionPinnedGeoLive, name, chat.title);
                        } else if (object.messageOwner.media instanceof TL_messageMediaContact) {
                            msg = LocaleController.formatString("NotificationActionPinnedContact", R.string.NotificationActionPinnedContact, name, chat.title);
                        } else if (object.messageOwner.media instanceof TL_messageMediaPhoto) {
                            if (VERSION.SDK_INT < 19 || TextUtils.isEmpty(object.messageOwner.message)) {
                                msg = LocaleController.formatString("NotificationActionPinnedPhoto", R.string.NotificationActionPinnedPhoto, name, chat.title);
                            } else {
                                message = "🖼 " + object.messageOwner.message;
                                msg = LocaleController.formatString("NotificationActionPinnedText", R.string.NotificationActionPinnedText, name, message, chat.title);
                            }
                        } else if (object.messageOwner.media instanceof TL_messageMediaGame) {
                            msg = LocaleController.formatString("NotificationActionPinnedGame", R.string.NotificationActionPinnedGame, name, chat.title);
                        } else if (object.messageText == null || object.messageText.length() <= 0) {
                            msg = LocaleController.formatString("NotificationActionPinnedNoText", R.string.NotificationActionPinnedNoText, name, chat.title);
                        } else {
                            message2 = object.messageText;
                            if (message2.length() > 20) {
                                message2 = message2.subSequence(0, 20) + "...";
                            }
                            msg = LocaleController.formatString("NotificationActionPinnedText", R.string.NotificationActionPinnedText, name, message2, chat.title);
                        }
                    }
                } else if (messageObject.messageOwner.action instanceof TL_messageActionGameScore) {
                    msg = messageObject.messageText.toString();
                }
            } else if (!ChatObject.isChannel(chat) || chat.megagroup) {
                if (messageObject.isMediaEmpty()) {
                    msg = (shortMessage || messageObject.messageOwner.message == null || messageObject.messageOwner.message.length() == 0) ? LocaleController.formatString("NotificationMessageGroupNoText", R.string.NotificationMessageGroupNoText, name, chat.title) : LocaleController.formatString("NotificationMessageGroupText", R.string.NotificationMessageGroupText, name, chat.title, messageObject.messageOwner.message);
                } else if (messageObject.messageOwner.media instanceof TL_messageMediaPhoto) {
                    msg = (shortMessage || VERSION.SDK_INT < 19 || TextUtils.isEmpty(messageObject.messageOwner.message)) ? LocaleController.formatString("NotificationMessageGroupPhoto", R.string.NotificationMessageGroupPhoto, name, chat.title) : LocaleController.formatString("NotificationMessageGroupText", R.string.NotificationMessageGroupText, name, chat.title, "🖼 " + messageObject.messageOwner.message);
                } else if (messageObject.isVideo()) {
                    msg = (shortMessage || VERSION.SDK_INT < 19 || TextUtils.isEmpty(messageObject.messageOwner.message)) ? LocaleController.formatString("NotificationMessageGroupVideo", R.string.NotificationMessageGroupVideo, name, chat.title) : LocaleController.formatString("NotificationMessageGroupText", R.string.NotificationMessageGroupText, name, chat.title, "📹 " + messageObject.messageOwner.message);
                } else if (messageObject.isVoice()) {
                    msg = LocaleController.formatString("NotificationMessageGroupAudio", R.string.NotificationMessageGroupAudio, name, chat.title);
                } else if (messageObject.isRoundVideo()) {
                    msg = LocaleController.formatString("NotificationMessageGroupRound", R.string.NotificationMessageGroupRound, name, chat.title);
                } else if (messageObject.isMusic()) {
                    msg = LocaleController.formatString("NotificationMessageGroupMusic", R.string.NotificationMessageGroupMusic, name, chat.title);
                } else if (messageObject.messageOwner.media instanceof TL_messageMediaContact) {
                    msg = LocaleController.formatString("NotificationMessageGroupContact", R.string.NotificationMessageGroupContact, name, chat.title);
                } else if (messageObject.messageOwner.media instanceof TL_messageMediaGame) {
                    msg = LocaleController.formatString("NotificationMessageGroupGame", R.string.NotificationMessageGroupGame, name, chat.title, messageObject.messageOwner.media.game.title);
                } else if ((messageObject.messageOwner.media instanceof TL_messageMediaGeo) || (messageObject.messageOwner.media instanceof TL_messageMediaVenue)) {
                    msg = LocaleController.formatString("NotificationMessageGroupMap", R.string.NotificationMessageGroupMap, name, chat.title);
                } else if (messageObject.messageOwner.media instanceof TL_messageMediaGeoLive) {
                    msg = LocaleController.formatString("NotificationMessageGroupLiveLocation", R.string.NotificationMessageGroupLiveLocation, name, chat.title);
                } else if (messageObject.messageOwner.media instanceof TL_messageMediaDocument) {
                    if (messageObject.isSticker()) {
                        msg = messageObject.getStickerEmoji() != null ? LocaleController.formatString("NotificationMessageGroupStickerEmoji", R.string.NotificationMessageGroupStickerEmoji, name, chat.title, messageObject.getStickerEmoji()) : LocaleController.formatString("NotificationMessageGroupSticker", R.string.NotificationMessageGroupSticker, name, chat.title);
                    } else {
                        msg = messageObject.isGif() ? (shortMessage || VERSION.SDK_INT < 19 || TextUtils.isEmpty(messageObject.messageOwner.message)) ? LocaleController.formatString("NotificationMessageGroupGif", R.string.NotificationMessageGroupGif, name, chat.title) : LocaleController.formatString("NotificationMessageGroupText", R.string.NotificationMessageGroupText, name, chat.title, "🎬 " + messageObject.messageOwner.message) : (shortMessage || VERSION.SDK_INT < 19 || TextUtils.isEmpty(messageObject.messageOwner.message)) ? LocaleController.formatString("NotificationMessageGroupDocument", R.string.NotificationMessageGroupDocument, name, chat.title) : LocaleController.formatString("NotificationMessageGroupText", R.string.NotificationMessageGroupText, name, chat.title, "📎 " + messageObject.messageOwner.message);
                    }
                }
            } else if (messageObject.isMediaEmpty()) {
                if (shortMessage || messageObject.messageOwner.message == null || messageObject.messageOwner.message.length() == 0) {
                    msg = LocaleController.formatString("ChannelMessageNoText", R.string.ChannelMessageNoText, name);
                } else {
                    msg = LocaleController.formatString("NotificationMessageText", R.string.NotificationMessageText, name, messageObject.messageOwner.message);
                    text[0] = true;
                }
            } else if (messageObject.messageOwner.media instanceof TL_messageMediaPhoto) {
                if (shortMessage || VERSION.SDK_INT < 19 || TextUtils.isEmpty(messageObject.messageOwner.message)) {
                    msg = LocaleController.formatString("ChannelMessagePhoto", R.string.ChannelMessagePhoto, name);
                } else {
                    msg = LocaleController.formatString("NotificationMessageText", R.string.NotificationMessageText, name, "🖼 " + messageObject.messageOwner.message);
                    text[0] = true;
                }
            } else if (messageObject.isVideo()) {
                if (shortMessage || VERSION.SDK_INT < 19 || TextUtils.isEmpty(messageObject.messageOwner.message)) {
                    msg = LocaleController.formatString("ChannelMessageVideo", R.string.ChannelMessageVideo, name);
                } else {
                    msg = LocaleController.formatString("NotificationMessageText", R.string.NotificationMessageText, name, "📹 " + messageObject.messageOwner.message);
                    text[0] = true;
                }
            } else if (messageObject.isVoice()) {
                msg = LocaleController.formatString("ChannelMessageAudio", R.string.ChannelMessageAudio, name);
            } else if (messageObject.isRoundVideo()) {
                msg = LocaleController.formatString("ChannelMessageRound", R.string.ChannelMessageRound, name);
            } else if (messageObject.isMusic()) {
                msg = LocaleController.formatString("ChannelMessageMusic", R.string.ChannelMessageMusic, name);
            } else if (messageObject.messageOwner.media instanceof TL_messageMediaContact) {
                msg = LocaleController.formatString("ChannelMessageContact", R.string.ChannelMessageContact, name);
            } else if ((messageObject.messageOwner.media instanceof TL_messageMediaGeo) || (messageObject.messageOwner.media instanceof TL_messageMediaVenue)) {
                msg = LocaleController.formatString("ChannelMessageMap", R.string.ChannelMessageMap, name);
            } else if (messageObject.messageOwner.media instanceof TL_messageMediaGeoLive) {
                msg = LocaleController.formatString("ChannelMessageLiveLocation", R.string.ChannelMessageLiveLocation, name);
            } else if (messageObject.messageOwner.media instanceof TL_messageMediaDocument) {
                if (messageObject.isSticker()) {
                    msg = messageObject.getStickerEmoji() != null ? LocaleController.formatString("ChannelMessageStickerEmoji", R.string.ChannelMessageStickerEmoji, name, messageObject.getStickerEmoji()) : LocaleController.formatString("ChannelMessageSticker", R.string.ChannelMessageSticker, name);
                } else if (messageObject.isGif()) {
                    if (shortMessage || VERSION.SDK_INT < 19 || TextUtils.isEmpty(messageObject.messageOwner.message)) {
                        msg = LocaleController.formatString("ChannelMessageGIF", R.string.ChannelMessageGIF, name);
                    } else {
                        msg = LocaleController.formatString("NotificationMessageText", R.string.NotificationMessageText, name, "🎬 " + messageObject.messageOwner.message);
                        text[0] = true;
                    }
                } else if (shortMessage || VERSION.SDK_INT < 19 || TextUtils.isEmpty(messageObject.messageOwner.message)) {
                    msg = LocaleController.formatString("ChannelMessageDocument", R.string.ChannelMessageDocument, name);
                } else {
                    msg = LocaleController.formatString("NotificationMessageText", R.string.NotificationMessageText, name, "📎 " + messageObject.messageOwner.message);
                    text[0] = true;
                }
            }
        }
        return msg;
    }

    private void scheduleNotificationRepeat() {
        try {
            Intent intent = new Intent(ApplicationLoader.applicationContext, NotificationRepeat.class);
            intent.putExtra("currentAccount", this.currentAccount);
            PendingIntent pintent = PendingIntent.getService(ApplicationLoader.applicationContext, 0, intent, 0);
            int minutes = MessagesController.getNotificationsSettings(this.currentAccount).getInt("repeat_messages", 60);
            if (minutes <= 0 || this.personal_count <= 0) {
                this.alarmManager.cancel(pintent);
            } else {
                this.alarmManager.set(2, SystemClock.elapsedRealtime() + ((long) ((minutes * 60) * 1000)), pintent);
            }
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }

    private boolean isPersonalMessage(MessageObject messageObject) {
        return messageObject.messageOwner.to_id != null && messageObject.messageOwner.to_id.chat_id == 0 && messageObject.messageOwner.to_id.channel_id == 0 && (messageObject.messageOwner.action == null || (messageObject.messageOwner.action instanceof TL_messageActionEmpty));
    }

    private int getNotifyOverride(SharedPreferences preferences, long dialog_id) {
        int notifyOverride = preferences.getInt("notify2_" + dialog_id, -1);
        if (notifyOverride != 3 || preferences.getInt("notifyuntil_" + dialog_id, 0) < ConnectionsManager.getInstance(this.currentAccount).getCurrentTime()) {
            return notifyOverride;
        }
        return 2;
    }

    private void dismissNotification() {
        try {
            this.lastNotificationIsNoData = false;
            notificationManager.cancel(this.notificationId);
            this.pushMessages.clear();
            this.pushMessagesDict.clear();
            this.lastWearNotifiedMessageId.clear();
            for (int a = 0; a < this.wearNotificationsIds.size(); a++) {
                notificationManager.cancel(((Integer) this.wearNotificationsIds.valueAt(a)).intValue());
            }
            this.wearNotificationsIds.clear();
            AndroidUtilities.runOnUIThread(NotificationsController$$Lambda$12.$instance);
            if (WearDataLayerListenerService.isWatchConnected()) {
                try {
                    JSONObject o = new JSONObject();
                    o.put(TtmlNode.ATTR_ID, UserConfig.getInstance(this.currentAccount).getClientUserId());
                    o.put("cancel_all", true);
                    WearDataLayerListenerService.sendMessageToWatch("/notify", o.toString().getBytes(C.UTF8_NAME), "remote_notifications");
                } catch (JSONException e) {
                }
            }
        } catch (Throwable e2) {
            FileLog.e(e2);
        }
    }

    private void playInChatSound() {
        if (this.inChatSoundEnabled && !MediaController.getInstance().isRecordingAudio()) {
            try {
                if (audioManager.getRingerMode() == 0) {
                    return;
                }
            } catch (Throwable e) {
                FileLog.e(e);
            }
            try {
                if (getNotifyOverride(MessagesController.getNotificationsSettings(this.currentAccount), this.opened_dialog_id) != 2) {
                    notificationsQueue.postRunnable(new NotificationsController$$Lambda$13(this));
                }
            } catch (Throwable e2) {
                FileLog.e(e2);
            }
        }
    }

    final /* synthetic */ void lambda$playInChatSound$25$NotificationsController() {
        if (Math.abs(System.currentTimeMillis() - this.lastSoundPlay) > 500) {
            try {
                if (this.soundPool == null) {
                    this.soundPool = new SoundPool(3, 1, 0);
                    this.soundPool.setOnLoadCompleteListener(NotificationsController$$Lambda$20.$instance);
                }
                if (this.soundIn == 0 && !this.soundInLoaded) {
                    this.soundInLoaded = true;
                    this.soundIn = this.soundPool.load(ApplicationLoader.applicationContext, R.raw.sound_in, 1);
                }
                if (this.soundIn != 0) {
                    try {
                        this.soundPool.play(this.soundIn, 1.0f, 1.0f, 1, 0, 1.0f);
                    } catch (Throwable e) {
                        FileLog.e(e);
                    }
                }
            } catch (Throwable e2) {
                FileLog.e(e2);
            }
        }
    }

    static final /* synthetic */ void lambda$null$24$NotificationsController(SoundPool soundPool, int sampleId, int status) {
        if (status == 0) {
            try {
                soundPool.play(sampleId, 1.0f, 1.0f, 1, 0, 1.0f);
            } catch (Throwable e) {
                FileLog.e(e);
            }
        }
    }

    private void scheduleNotificationDelay(boolean onlineReason) {
        try {
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("delay notification start, onlineReason = " + onlineReason);
            }
            this.notificationDelayWakelock.acquire(10000);
            notificationsQueue.cancelRunnable(this.notificationDelayRunnable);
            notificationsQueue.postRunnable(this.notificationDelayRunnable, (long) (onlineReason ? 3000 : 1000));
        } catch (Throwable e) {
            FileLog.e(e);
            showOrUpdateNotification(this.notifyCheck);
        }
    }

    protected void repeatNotificationMaybe() {
        notificationsQueue.postRunnable(new NotificationsController$$Lambda$14(this));
    }

    final /* synthetic */ void lambda$repeatNotificationMaybe$26$NotificationsController() {
        int hour = Calendar.getInstance().get(11);
        if (hour < 11 || hour > 22) {
            scheduleNotificationRepeat();
            return;
        }
        notificationManager.cancel(this.notificationId);
        showOrUpdateNotification(true);
    }

    private boolean isEmptyVibration(long[] pattern) {
        if (pattern == null || pattern.length == 0) {
            return false;
        }
        for (int a = 0; a < pattern.length; a++) {
            if (pattern[0] != 0) {
                return false;
            }
        }
        return true;
    }

    @TargetApi(26)
    private String validateChannelId(long dialogId, String name, long[] vibrationPattern, int ledColor, Uri sound, int importance, long[] configVibrationPattern, Uri configSound, int configImportance) {
        SharedPreferences preferences = MessagesController.getNotificationsSettings(this.currentAccount);
        String key = "org.telegram.key" + dialogId;
        String channelId = preferences.getString(key, null);
        String settings = preferences.getString(key + "_s", null);
        StringBuilder newSettings = new StringBuilder();
        for (long append : vibrationPattern) {
            newSettings.append(append);
        }
        newSettings.append(ledColor);
        if (sound != null) {
            newSettings.append(sound.toString());
        }
        newSettings.append(importance);
        String newSettingsHash = Utilities.MD5(newSettings.toString());
        if (!(channelId == null || settings.equals(newSettingsHash))) {
            if (false) {
                preferences.edit().putString(key, channelId).putString(key + "_s", newSettingsHash).commit();
            } else {
                systemNotificationManager.deleteNotificationChannel(channelId);
                channelId = null;
            }
        }
        if (channelId == null) {
            channelId = this.currentAccount + "channel" + dialogId + "_" + Utilities.random.nextLong();
            NotificationChannel notificationChannel = new NotificationChannel(channelId, name, importance);
            if (ledColor != 0) {
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(ledColor);
            }
            if (isEmptyVibration(vibrationPattern)) {
                notificationChannel.enableVibration(false);
            } else {
                notificationChannel.enableVibration(true);
                if (vibrationPattern != null && vibrationPattern.length > 0) {
                    notificationChannel.setVibrationPattern(vibrationPattern);
                }
            }
            Builder builder = new Builder();
            builder.setContentType(4);
            builder.setUsage(5);
            if (sound != null) {
                notificationChannel.setSound(sound, builder.build());
            } else {
                notificationChannel.setSound(null, builder.build());
            }
            systemNotificationManager.createNotificationChannel(notificationChannel);
            preferences.edit().putString(key, channelId).putString(key + "_s", newSettingsHash).commit();
        }
        return channelId;
    }

    private void showOrUpdateNotification(boolean notifyAboutLast) {
        if (!UserConfig.getInstance(this.currentAccount).isClientActivated() || this.pushMessages.isEmpty()) {
            dismissNotification();
            return;
        }
        try {
            ConnectionsManager.getInstance(this.currentAccount).resumeNetworkMaybe();
            MessageObject lastMessageObject = (MessageObject) this.pushMessages.get(0);
            SharedPreferences preferences = MessagesController.getNotificationsSettings(this.currentAccount);
            int dismissDate = preferences.getInt("dismissDate", 0);
            if (lastMessageObject.messageOwner.date <= dismissDate) {
                dismissNotification();
                return;
            }
            int count;
            int vibrateOverride;
            int priorityOverride;
            String choosenSoundPath;
            String chatName;
            String name;
            String detailText;
            long dialog_id = lastMessageObject.getDialogId();
            long override_dialog_id = dialog_id;
            if (lastMessageObject.messageOwner.mentioned) {
                override_dialog_id = (long) lastMessageObject.messageOwner.from_id;
            }
            int mid = lastMessageObject.getId();
            int chat_id = lastMessageObject.messageOwner.to_id.chat_id != 0 ? lastMessageObject.messageOwner.to_id.chat_id : lastMessageObject.messageOwner.to_id.channel_id;
            int user_id = lastMessageObject.messageOwner.to_id.user_id;
            if (user_id == 0) {
                user_id = lastMessageObject.messageOwner.from_id;
            } else if (user_id == UserConfig.getInstance(this.currentAccount).getClientUserId()) {
                user_id = lastMessageObject.messageOwner.from_id;
            }
            User user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(user_id));
            Chat chat = null;
            if (chat_id != 0) {
                chat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(chat_id));
            }
            TLObject photoPath = null;
            boolean notifyDisabled = false;
            int needVibrate = 0;
            int ledColor = -16776961;
            int priority = 0;
            int notifyOverride = getNotifyOverride(preferences, override_dialog_id);
            boolean value = notifyOverride == -1 ? ((int) dialog_id) < 0 ? preferences.getBoolean("EnableGroup", true) : preferences.getBoolean("EnableAll", true) : notifyOverride != 2;
            if (!(notifyAboutLast && value)) {
                notifyDisabled = true;
            }
            if (!(notifyDisabled || dialog_id != override_dialog_id || chat == null)) {
                int notifyMaxCount;
                int notifyDelay;
                if (preferences.getBoolean("custom_" + dialog_id, false)) {
                    notifyMaxCount = preferences.getInt("smart_max_count_" + dialog_id, 2);
                    notifyDelay = preferences.getInt("smart_delay_" + dialog_id, 180);
                } else {
                    notifyMaxCount = 2;
                    notifyDelay = 180;
                }
                if (notifyMaxCount != 0) {
                    Point dialogInfo = (Point) this.smartNotificationsDialogs.get(dialog_id);
                    if (dialogInfo == null) {
                        this.smartNotificationsDialogs.put(dialog_id, new Point(1, (int) (System.currentTimeMillis() / 1000)));
                    } else if (((long) (dialogInfo.y + notifyDelay)) < System.currentTimeMillis() / 1000) {
                        dialogInfo.set(1, (int) (System.currentTimeMillis() / 1000));
                    } else {
                        count = dialogInfo.x;
                        if (count < notifyMaxCount) {
                            dialogInfo.set(count + 1, (int) (System.currentTimeMillis() / 1000));
                        } else {
                            notifyDisabled = true;
                        }
                    }
                }
            }
            String defaultPath = System.DEFAULT_NOTIFICATION_URI.getPath();
            boolean inAppSounds = preferences.getBoolean("EnableInAppSounds", true);
            boolean inAppVibrate = preferences.getBoolean("EnableInAppVibrate", true);
            boolean inAppPreview = preferences.getBoolean("EnableInAppPreview", true);
            boolean inAppPriority = preferences.getBoolean("EnableInAppPriority", false);
            boolean custom = preferences.getBoolean("custom_" + dialog_id, false);
            if (custom) {
                vibrateOverride = preferences.getInt("vibrate_" + dialog_id, 0);
                priorityOverride = preferences.getInt("priority_" + dialog_id, 3);
                choosenSoundPath = preferences.getString("sound_path_" + dialog_id, null);
            } else {
                vibrateOverride = 0;
                priorityOverride = 3;
                choosenSoundPath = null;
            }
            boolean vibrateOnlyIfSilent = false;
            if (chat_id != 0) {
                if (choosenSoundPath != null && choosenSoundPath.equals(defaultPath)) {
                    choosenSoundPath = null;
                } else if (choosenSoundPath == null) {
                    choosenSoundPath = preferences.getString("GroupSoundPath", defaultPath);
                }
                needVibrate = preferences.getInt("vibrate_group", 0);
                priority = preferences.getInt("priority_group", 1);
                ledColor = preferences.getInt("GroupLed", -16776961);
            } else if (user_id != 0) {
                if (choosenSoundPath != null && choosenSoundPath.equals(defaultPath)) {
                    choosenSoundPath = null;
                } else if (choosenSoundPath == null) {
                    choosenSoundPath = preferences.getString("GlobalSoundPath", defaultPath);
                }
                needVibrate = preferences.getInt("vibrate_messages", 0);
                priority = preferences.getInt("priority_messages", 1);
                ledColor = preferences.getInt("MessagesLed", -16776961);
            }
            if (custom) {
                if (preferences.contains("color_" + dialog_id)) {
                    ledColor = preferences.getInt("color_" + dialog_id, 0);
                }
            }
            if (priorityOverride != 3) {
                priority = priorityOverride;
            }
            if (needVibrate == 4) {
                vibrateOnlyIfSilent = true;
                needVibrate = 0;
            }
            if ((needVibrate == 2 && (vibrateOverride == 1 || vibrateOverride == 3)) || ((needVibrate != 2 && vibrateOverride == 2) || !(vibrateOverride == 0 || vibrateOverride == 4))) {
                needVibrate = vibrateOverride;
            }
            if (!ApplicationLoader.mainInterfacePaused) {
                if (!inAppSounds) {
                    choosenSoundPath = null;
                }
                if (!inAppVibrate) {
                    needVibrate = 2;
                }
                if (!inAppPriority) {
                    priority = 0;
                } else if (priority == 2) {
                    priority = 1;
                }
            }
            if (vibrateOnlyIfSilent && needVibrate != 2) {
                try {
                    int mode = audioManager.getRingerMode();
                    if (!(mode == 0 || mode == 1)) {
                        needVibrate = 2;
                    }
                } catch (Throwable e) {
                    FileLog.e(e);
                }
            }
            Uri configSound = null;
            long[] configVibrationPattern = null;
            int configImportance = 0;
            if (VERSION.SDK_INT >= 26) {
                if (needVibrate == 2) {
                    configVibrationPattern = new long[]{0, 0};
                } else if (needVibrate == 1) {
                    configVibrationPattern = new long[]{0, 100, 0, 100};
                } else if (needVibrate == 0 || needVibrate == 4) {
                    configVibrationPattern = new long[0];
                } else if (needVibrate == 3) {
                    configVibrationPattern = new long[]{0, 1000};
                }
                if (choosenSoundPath != null) {
                    if (!choosenSoundPath.equals("NoSound")) {
                        configSound = choosenSoundPath.equals(defaultPath) ? System.DEFAULT_NOTIFICATION_URI : Uri.parse(choosenSoundPath);
                    }
                }
                if (priority == 0) {
                    configImportance = 3;
                } else if (priority == 1 || priority == 2) {
                    configImportance = 4;
                } else if (priority == 4) {
                    configImportance = 1;
                } else if (priority == 5) {
                    configImportance = 2;
                }
            }
            if (notifyDisabled) {
                needVibrate = 0;
                priority = 0;
                ledColor = 0;
                choosenSoundPath = null;
            }
            Intent intent = new Intent(ApplicationLoader.applicationContext, LaunchActivity.class);
            intent.setAction("com.tmessages.openchat" + Math.random() + ConnectionsManager.DEFAULT_DATACENTER_ID);
            intent.setFlags(32768);
            if (((int) dialog_id) != 0) {
                if (this.pushDialogs.size() == 1) {
                    if (chat_id != 0) {
                        intent.putExtra("chatId", chat_id);
                    } else if (user_id != 0) {
                        intent.putExtra("userId", user_id);
                    }
                }
                if (AndroidUtilities.needShowPasscode(false) || SharedConfig.isWaitingForPasscodeEnter) {
                    photoPath = null;
                } else if (this.pushDialogs.size() == 1 && VERSION.SDK_INT < 28) {
                    if (chat != null) {
                        if (!(chat.photo == null || chat.photo.photo_small == null || chat.photo.photo_small.volume_id == 0 || chat.photo.photo_small.local_id == 0)) {
                            photoPath = chat.photo.photo_small;
                        }
                    } else if (!(user == null || user.photo == null || user.photo.photo_small == null || user.photo.photo_small.volume_id == 0 || user.photo.photo_small.local_id == 0)) {
                        photoPath = user.photo.photo_small;
                    }
                }
            } else if (this.pushDialogs.size() == 1 && dialog_id != globalSecretChatId) {
                intent.putExtra("encId", (int) (dialog_id >> 32));
            }
            intent.putExtra("currentAccount", this.currentAccount);
            PendingIntent contentIntent = PendingIntent.getActivity(ApplicationLoader.applicationContext, 0, intent, 1073741824);
            boolean replace = true;
            if (((chat_id != 0 && chat == null) || user == null) && lastMessageObject.isFcmMessage()) {
                chatName = lastMessageObject.localName;
            } else if (chat != null) {
                chatName = chat.title;
            } else {
                chatName = UserObject.getUserName(user);
            }
            if (((int) dialog_id) == 0 || this.pushDialogs.size() > 1 || AndroidUtilities.needShowPasscode(false) || SharedConfig.isWaitingForPasscodeEnter) {
                name = LocaleController.getString("AppName", R.string.AppName);
                replace = false;
            } else {
                name = chatName;
            }
            if (UserConfig.getActivatedAccountsCount() <= 1) {
                detailText = TtmlNode.ANONYMOUS_REGION_ID;
            } else if (this.pushDialogs.size() == 1) {
                detailText = UserObject.getFirstName(UserConfig.getInstance(this.currentAccount).getCurrentUser());
            } else {
                detailText = UserObject.getFirstName(UserConfig.getInstance(this.currentAccount).getCurrentUser()) + "・";
            }
            if (this.pushDialogs.size() != 1 || VERSION.SDK_INT < 23) {
                if (this.pushDialogs.size() == 1) {
                    detailText = detailText + LocaleController.formatPluralString("NewMessages", this.total_unread_count);
                } else {
                    detailText = detailText + LocaleController.formatString("NotificationMessagesPeopleDisplayOrder", R.string.NotificationMessagesPeopleDisplayOrder, LocaleController.formatPluralString("NewMessages", this.total_unread_count), LocaleController.formatPluralString("FromChats", this.pushDialogs.size()));
                }
            }
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ApplicationLoader.applicationContext).setContentTitle(name).setSmallIcon(R.drawable.notification).setAutoCancel(true).setNumber(this.total_unread_count).setContentIntent(contentIntent).setGroup(this.notificationGroup).setGroupSummary(true).setShowWhen(true).setWhen(((long) lastMessageObject.messageOwner.date) * 1000).setColor(-13851168);
            long[] vibrationPattern = null;
            int importance = 0;
            Uri sound = null;
            mBuilder.setCategory("msg");
            if (chat == null && user != null && user.phone != null && user.phone.length() > 0) {
                mBuilder.addPerson("tel:+" + user.phone);
            }
            int silent = 2;
            String lastMessage = null;
            MessageObject messageObject;
            boolean[] text;
            String message;
            if (this.pushMessages.size() == 1) {
                messageObject = (MessageObject) this.pushMessages.get(0);
                text = new boolean[1];
                lastMessage = getStringForMessage(messageObject, false, text);
                message = lastMessage;
                silent = messageObject.messageOwner.silent ? 1 : 0;
                if (message != null) {
                    if (replace) {
                        if (chat != null) {
                            message = message.replace(" @ " + name, TtmlNode.ANONYMOUS_REGION_ID);
                        } else if (text[0]) {
                            message = message.replace(name + ": ", TtmlNode.ANONYMOUS_REGION_ID);
                        } else {
                            message = message.replace(name + " ", TtmlNode.ANONYMOUS_REGION_ID);
                        }
                    }
                    mBuilder.setContentText(message);
                    mBuilder.setStyle(new BigTextStyle().bigText(message));
                } else {
                    return;
                }
            }
            mBuilder.setContentText(detailText);
            Style inboxStyle = new InboxStyle();
            inboxStyle.setBigContentTitle(name);
            count = Math.min(10, this.pushMessages.size());
            text = new boolean[1];
            for (int i = 0; i < count; i++) {
                messageObject = (MessageObject) this.pushMessages.get(i);
                message = getStringForMessage(messageObject, false, text);
                if (message != null && messageObject.messageOwner.date > dismissDate) {
                    if (silent == 2) {
                        lastMessage = message;
                        silent = messageObject.messageOwner.silent ? 1 : 0;
                    }
                    if (this.pushDialogs.size() == 1 && replace) {
                        message = chat != null ? message.replace(" @ " + name, TtmlNode.ANONYMOUS_REGION_ID) : text[0] ? message.replace(name + ": ", TtmlNode.ANONYMOUS_REGION_ID) : message.replace(name + " ", TtmlNode.ANONYMOUS_REGION_ID);
                    }
                    inboxStyle.addLine(message);
                }
            }
            inboxStyle.setSummaryText(detailText);
            mBuilder.setStyle(inboxStyle);
            intent = new Intent(ApplicationLoader.applicationContext, NotificationDismissReceiver.class);
            intent.putExtra("messageDate", lastMessageObject.messageOwner.date);
            intent.putExtra("currentAccount", this.currentAccount);
            mBuilder.setDeleteIntent(PendingIntent.getBroadcast(ApplicationLoader.applicationContext, 1, intent, 134217728));
            if (photoPath != null) {
                BitmapDrawable img = ImageLoader.getInstance().getImageFromMemory(photoPath, null, "50_50");
                if (img != null) {
                    mBuilder.setLargeIcon(img.getBitmap());
                } else {
                    try {
                        File file = FileLoader.getPathToAttach(photoPath, true);
                        if (file.exists()) {
                            int i2;
                            float scaleFactor = 160.0f / ((float) AndroidUtilities.dp(50.0f));
                            Options options = new Options();
                            if (scaleFactor < 1.0f) {
                                i2 = 1;
                            } else {
                                i2 = (int) scaleFactor;
                            }
                            options.inSampleSize = i2;
                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                            if (bitmap != null) {
                                mBuilder.setLargeIcon(bitmap);
                            }
                        }
                    } catch (Throwable th) {
                    }
                }
            }
            if (!notifyAboutLast || silent == 1) {
                mBuilder.setPriority(-1);
                if (VERSION.SDK_INT >= 26) {
                    importance = 2;
                }
            } else if (priority == 0) {
                mBuilder.setPriority(0);
                if (VERSION.SDK_INT >= 26) {
                    importance = 3;
                }
            } else if (priority == 1 || priority == 2) {
                mBuilder.setPriority(1);
                if (VERSION.SDK_INT >= 26) {
                    importance = 4;
                }
            } else if (priority == 4) {
                mBuilder.setPriority(-2);
                if (VERSION.SDK_INT >= 26) {
                    importance = 1;
                }
            } else if (priority == 5) {
                mBuilder.setPriority(-1);
                if (VERSION.SDK_INT >= 26) {
                    importance = 2;
                }
            }
            if (silent == 1 || notifyDisabled) {
                vibrationPattern = new long[]{0, 0};
                mBuilder.setVibrate(vibrationPattern);
            } else {
                if (ApplicationLoader.mainInterfacePaused || inAppPreview) {
                    if (lastMessage.length() > 100) {
                        lastMessage = lastMessage.substring(0, 100).replace('\n', ' ').trim() + "...";
                    }
                    mBuilder.setTicker(lastMessage);
                }
                if (!(MediaController.getInstance().isRecordingAudio() || choosenSoundPath == null)) {
                    if (!choosenSoundPath.equals("NoSound")) {
                        if (VERSION.SDK_INT >= 26) {
                            sound = choosenSoundPath.equals(defaultPath) ? System.DEFAULT_NOTIFICATION_URI : Uri.parse(choosenSoundPath);
                        } else if (choosenSoundPath.equals(defaultPath)) {
                            mBuilder.setSound(System.DEFAULT_NOTIFICATION_URI, 5);
                        } else {
                            mBuilder.setSound(Uri.parse(choosenSoundPath), 5);
                        }
                    }
                }
                if (ledColor != 0) {
                    mBuilder.setLights(ledColor, 1000, 1000);
                }
                if (needVibrate == 2 || MediaController.getInstance().isRecordingAudio()) {
                    vibrationPattern = new long[]{0, 0};
                    mBuilder.setVibrate(vibrationPattern);
                } else if (needVibrate == 1) {
                    vibrationPattern = new long[]{0, 100, 0, 100};
                    mBuilder.setVibrate(vibrationPattern);
                } else if (needVibrate == 0 || needVibrate == 4) {
                    mBuilder.setDefaults(2);
                    vibrationPattern = new long[0];
                } else if (needVibrate == 3) {
                    vibrationPattern = new long[]{0, 1000};
                    mBuilder.setVibrate(vibrationPattern);
                }
            }
            boolean hasCallback = false;
            if (!(AndroidUtilities.needShowPasscode(false) || SharedConfig.isWaitingForPasscodeEnter || lastMessageObject.getDialogId() != 777000 || lastMessageObject.messageOwner.reply_markup == null)) {
                ArrayList<TL_keyboardButtonRow> rows = lastMessageObject.messageOwner.reply_markup.rows;
                int size = rows.size();
                for (int a = 0; a < size; a++) {
                    TL_keyboardButtonRow row = (TL_keyboardButtonRow) rows.get(a);
                    int size2 = row.buttons.size();
                    for (int b = 0; b < size2; b++) {
                        KeyboardButton button = (KeyboardButton) row.buttons.get(b);
                        if (button instanceof TL_keyboardButtonCallback) {
                            intent = new Intent(ApplicationLoader.applicationContext, NotificationCallbackReceiver.class);
                            intent.putExtra("currentAccount", this.currentAccount);
                            intent.putExtra("did", dialog_id);
                            if (button.data != null) {
                                intent.putExtra(DataSchemeDataSource.SCHEME_DATA, button.data);
                            }
                            intent.putExtra("mid", lastMessageObject.getId());
                            String str = button.text;
                            Context context = ApplicationLoader.applicationContext;
                            int i3 = this.lastButtonId;
                            this.lastButtonId = i3 + 1;
                            mBuilder.addAction(0, str, PendingIntent.getBroadcast(context, i3, intent, 134217728));
                            hasCallback = true;
                        }
                    }
                }
            }
            if (!hasCallback && VERSION.SDK_INT < 24 && SharedConfig.passcodeHash.length() == 0 && hasMessagesToReply()) {
                intent = new Intent(ApplicationLoader.applicationContext, PopupReplyReceiver.class);
                intent.putExtra("currentAccount", this.currentAccount);
                if (VERSION.SDK_INT <= 19) {
                    mBuilder.addAction(R.drawable.ic_ab_reply2, LocaleController.getString("Reply", R.string.Reply), PendingIntent.getBroadcast(ApplicationLoader.applicationContext, 2, intent, 134217728));
                } else {
                    mBuilder.addAction(R.drawable.ic_ab_reply, LocaleController.getString("Reply", R.string.Reply), PendingIntent.getBroadcast(ApplicationLoader.applicationContext, 2, intent, 134217728));
                }
            }
            if (VERSION.SDK_INT >= 26) {
                mBuilder.setChannelId(validateChannelId(dialog_id, chatName, vibrationPattern, ledColor, sound, importance, configVibrationPattern, configSound, configImportance));
            }
            showExtraNotifications(mBuilder, notifyAboutLast, detailText);
            this.lastNotificationIsNoData = false;
            scheduleNotificationRepeat();
        } catch (Throwable e2) {
            FileLog.e(e2);
        }
    }

    @SuppressLint({"InlinedApi"})
    private void showExtraNotifications(NotificationCompat.Builder notificationBuilder, boolean notifyAboutLast, String summary) {
        Notification mainNotification = notificationBuilder.build();
        if (VERSION.SDK_INT < 18) {
            notificationManager.notify(this.notificationId, mainNotification);
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("show summary notification by SDK check");
                return;
            }
            return;
        }
        int a;
        ArrayList<Long> sortedDialogs = new ArrayList();
        LongSparseArray<ArrayList<MessageObject>> messagesByDialogs = new LongSparseArray();
        for (a = 0; a < this.pushMessages.size(); a++) {
            MessageObject messageObject = (MessageObject) this.pushMessages.get(a);
            long dialog_id = messageObject.getDialogId();
            ArrayList<MessageObject> arrayList = (ArrayList) messagesByDialogs.get(dialog_id);
            if (arrayList == null) {
                arrayList = new ArrayList();
                messagesByDialogs.put(dialog_id, arrayList);
                sortedDialogs.add(0, Long.valueOf(dialog_id));
            }
            arrayList.add(messageObject);
        }
        LongSparseArray<Integer> oldIdsWear = this.wearNotificationsIds.clone();
        this.wearNotificationsIds.clear();
        ArrayList<AnonymousClass1NotificationHolder> holders = new ArrayList();
        JSONArray serializedNotifications = null;
        if (WearDataLayerListenerService.isWatchConnected()) {
            serializedNotifications = new JSONArray();
        }
        boolean useSummaryNotification = VERSION.SDK_INT <= 27 || (VERSION.SDK_INT > 27 && sortedDialogs.size() > 1);
        if (useSummaryNotification && VERSION.SDK_INT >= 26) {
            checkOtherNotificationsChannel();
        }
        int size = sortedDialogs.size();
        for (int b = 0; b < size; b++) {
            boolean canReply;
            String name;
            String dismissalID;
            dialog_id = ((Long) sortedDialogs.get(b)).longValue();
            ArrayList<MessageObject> messageObjects = (ArrayList) messagesByDialogs.get(dialog_id);
            int max_id = ((MessageObject) messageObjects.get(0)).getId();
            int lowerId = (int) dialog_id;
            int highId = (int) (dialog_id >> 32);
            Integer internalId = (Integer) oldIdsWear.get(dialog_id);
            if (internalId != null) {
                oldIdsWear.remove(dialog_id);
            } else if (lowerId != 0) {
                internalId = Integer.valueOf(lowerId);
            } else {
                internalId = Integer.valueOf(highId);
            }
            JSONObject jSONObject = null;
            if (serializedNotifications != null) {
                jSONObject = new JSONObject();
            }
            MessageObject lastMessageObject = (MessageObject) messageObjects.get(0);
            int max_date = lastMessageObject.messageOwner.date;
            Chat chat = null;
            User user = null;
            boolean isChannel = false;
            boolean isSupergroup = false;
            TLObject photoPath = null;
            Bitmap avatarBitmap = null;
            File avatalFile = null;
            LongSparseArray<Person> personCache = new LongSparseArray();
            if (lowerId != 0) {
                canReply = lowerId != 777000;
                if (lowerId > 0) {
                    user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(lowerId));
                    if (user != null) {
                        name = UserObject.getUserName(user);
                        if (!(user.photo == null || user.photo.photo_small == null || user.photo.photo_small.volume_id == 0 || user.photo.photo_small.local_id == 0)) {
                            photoPath = user.photo.photo_small;
                        }
                    } else if (lastMessageObject.isFcmMessage()) {
                        name = lastMessageObject.localName;
                    } else {
                        if (BuildVars.LOGS_ENABLED) {
                            FileLog.w("not found user to show dialog notification " + lowerId);
                        }
                    }
                } else {
                    chat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(-lowerId));
                    if (chat != null) {
                        isSupergroup = chat.megagroup;
                        isChannel = ChatObject.isChannel(chat) && !chat.megagroup;
                        name = chat.title;
                        if (!(chat.photo == null || chat.photo.photo_small == null || chat.photo.photo_small.volume_id == 0 || chat.photo.photo_small.local_id == 0)) {
                            photoPath = chat.photo.photo_small;
                        }
                    } else if (lastMessageObject.isFcmMessage()) {
                        isSupergroup = lastMessageObject.isMegagroup();
                        name = lastMessageObject.localName;
                        isChannel = lastMessageObject.localChannel;
                    } else {
                        if (BuildVars.LOGS_ENABLED) {
                            FileLog.w("not found chat to show dialog notification " + lowerId);
                        }
                    }
                }
            } else {
                canReply = false;
                if (dialog_id != globalSecretChatId) {
                    EncryptedChat encryptedChat = MessagesController.getInstance(this.currentAccount).getEncryptedChat(Integer.valueOf(highId));
                    if (encryptedChat != null) {
                        user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(encryptedChat.user_id));
                        if (user == null) {
                            if (BuildVars.LOGS_ENABLED) {
                                FileLog.w("not found secret chat user to show dialog notification " + encryptedChat.user_id);
                            }
                        }
                    } else if (BuildVars.LOGS_ENABLED) {
                        FileLog.w("not found secret chat to show dialog notification " + highId);
                    }
                }
                name = LocaleController.getString("SecretChatName", R.string.SecretChatName);
                photoPath = null;
                jSONObject = null;
            }
            if (AndroidUtilities.needShowPasscode(false) || SharedConfig.isWaitingForPasscodeEnter) {
                name = LocaleController.getString("AppName", R.string.AppName);
                photoPath = null;
                canReply = false;
            }
            if (photoPath != null) {
                avatalFile = FileLoader.getPathToAttach(photoPath, true);
                BitmapDrawable img = ImageLoader.getInstance().getImageFromMemory(photoPath, null, "50_50");
                if (img != null) {
                    avatarBitmap = img.getBitmap();
                } else if (VERSION.SDK_INT < 28) {
                    try {
                        if (avatalFile.exists()) {
                            int i;
                            float scaleFactor = 160.0f / ((float) AndroidUtilities.dp(50.0f));
                            Options options = new Options();
                            if (scaleFactor < 1.0f) {
                                i = 1;
                            } else {
                                i = (int) scaleFactor;
                            }
                            options.inSampleSize = i;
                            avatarBitmap = BitmapFactory.decodeFile(avatalFile.getAbsolutePath(), options);
                        }
                    } catch (Throwable th) {
                    }
                }
            }
            UnreadConversation.Builder unreadConvBuilder = new UnreadConversation.Builder(name).setLatestTimestamp(((long) max_date) * 1000);
            Intent intent = new Intent(ApplicationLoader.applicationContext, AutoMessageHeardReceiver.class);
            intent.addFlags(32);
            intent.setAction("org.telegram.messenger.ACTION_MESSAGE_HEARD");
            intent.putExtra("dialog_id", dialog_id);
            intent.putExtra("max_id", max_id);
            intent.putExtra("currentAccount", this.currentAccount);
            unreadConvBuilder.setReadPendingIntent(PendingIntent.getBroadcast(ApplicationLoader.applicationContext, internalId.intValue(), intent, 134217728));
            Action wearReplyAction = null;
            if ((!isChannel || isSupergroup) && canReply && !SharedConfig.isWaitingForPasscodeEnter) {
                String replyToString;
                intent = new Intent(ApplicationLoader.applicationContext, AutoMessageReplyReceiver.class);
                intent.addFlags(32);
                intent.setAction("org.telegram.messenger.ACTION_MESSAGE_REPLY");
                intent.putExtra("dialog_id", dialog_id);
                intent.putExtra("max_id", max_id);
                intent.putExtra("currentAccount", this.currentAccount);
                unreadConvBuilder.setReplyAction(PendingIntent.getBroadcast(ApplicationLoader.applicationContext, internalId.intValue(), intent, 134217728), new RemoteInput.Builder(EXTRA_VOICE_REPLY).setLabel(LocaleController.getString("Reply", R.string.Reply)).build());
                intent = new Intent(ApplicationLoader.applicationContext, WearReplyReceiver.class);
                intent.putExtra("dialog_id", dialog_id);
                intent.putExtra("max_id", max_id);
                intent.putExtra("currentAccount", this.currentAccount);
                PendingIntent replyPendingIntent = PendingIntent.getBroadcast(ApplicationLoader.applicationContext, internalId.intValue(), intent, 134217728);
                RemoteInput remoteInputWear = new RemoteInput.Builder(EXTRA_VOICE_REPLY).setLabel(LocaleController.getString("Reply", R.string.Reply)).build();
                if (lowerId < 0) {
                    replyToString = LocaleController.formatString("ReplyToGroup", R.string.ReplyToGroup, name);
                } else {
                    replyToString = LocaleController.formatString("ReplyToUser", R.string.ReplyToUser, name);
                }
                wearReplyAction = new Action.Builder(R.drawable.ic_reply_icon, replyToString, replyPendingIntent).setAllowGeneratedReplies(true).addRemoteInput(remoteInputWear).build();
            }
            Integer count = (Integer) this.pushDialogs.get(dialog_id);
            if (count == null) {
                count = Integer.valueOf(0);
            }
            String conversationName = String.format("%1$s (%2$s)", new Object[]{name, LocaleController.formatPluralString("NewMessages", Math.max(count.intValue(), messageObjects.size()))});
            Style messagingStyle = new MessagingStyle(TtmlNode.ANONYMOUS_REGION_ID);
            messagingStyle.setConversationTitle(conversationName);
            if (!isChannel && lowerId < 0) {
                messagingStyle.setGroupConversation(true);
            }
            StringBuilder text = new StringBuilder();
            String[] senderName = new String[1];
            ArrayList<TL_keyboardButtonRow> rows = null;
            int rowsMid = 0;
            JSONArray serializedMsgs = null;
            if (jSONObject != null) {
                serializedMsgs = new JSONArray();
            }
            for (a = messageObjects.size() - 1; a >= 0; a--) {
                messageObject = (MessageObject) messageObjects.get(a);
                String message = getShortStringForMessage(messageObject, senderName);
                if (message != null) {
                    long uid;
                    User sender;
                    if (text.length() > 0) {
                        text.append("\n\n");
                    }
                    if (senderName[0] != null) {
                        text.append(String.format("%1$s: %2$s", new Object[]{senderName[0], message}));
                    } else {
                        text.append(message);
                    }
                    unreadConvBuilder.addMessage(message);
                    if (lowerId > 0) {
                        uid = (long) lowerId;
                    } else if (isChannel) {
                        uid = (long) (-lowerId);
                    } else if (lowerId < 0) {
                        uid = (long) messageObject.getFromId();
                    } else {
                        uid = dialog_id;
                    }
                    Person person = (Person) personCache.get(uid);
                    if (person == null) {
                        Person.Builder personBuilder = new Person.Builder().setName(senderName[0] == null ? TtmlNode.ANONYMOUS_REGION_ID : senderName[0]);
                        if (VERSION.SDK_INT >= 28) {
                            File avatar = null;
                            if (lowerId > 0 || isChannel) {
                                avatar = avatalFile;
                            } else if (lowerId < 0) {
                                int fromId = messageObject.getFromId();
                                sender = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(fromId));
                                if (sender == null) {
                                    sender = MessagesStorage.getInstance(this.currentAccount).getUserSync(fromId);
                                    if (sender != null) {
                                        MessagesController.getInstance(this.currentAccount).putUser(sender, true);
                                    }
                                }
                                if (!(sender == null || sender.photo == null || sender.photo.photo_small == null || sender.photo.photo_small.volume_id == 0 || sender.photo.photo_small.local_id == 0)) {
                                    avatar = FileLoader.getPathToAttach(sender.photo.photo_small, true);
                                }
                            }
                            if (avatar != null) {
                                try {
                                    personBuilder.setIcon(IconCompat.createWithBitmap(ImageDecoder.decodeBitmap(ImageDecoder.createSource(avatar), NotificationsController$$Lambda$15.$instance)));
                                } catch (Throwable th2) {
                                }
                            }
                        }
                        person = personBuilder.build();
                        personCache.put(uid, person);
                    }
                    messagingStyle.addMessage(message, ((long) messageObject.messageOwner.date) * 1000, person);
                    if (messageObject.isVoice()) {
                        List<MessagingStyle.Message> messages = messagingStyle.getMessages();
                        if (!messages.isEmpty()) {
                            Uri uri;
                            File f = FileLoader.getPathToMessage(messageObject.messageOwner);
                            if (VERSION.SDK_INT >= 24) {
                                try {
                                    uri = FileProvider.getUriForFile(ApplicationLoader.applicationContext, "org.telegram.messenger.beta.provider", f);
                                } catch (Exception e) {
                                    uri = null;
                                }
                            } else {
                                uri = Uri.fromFile(f);
                            }
                            if (uri != null) {
                                ((MessagingStyle.Message) messages.get(messages.size() - 1)).setData("audio/ogg", uri);
                            }
                        }
                    }
                    if (serializedMsgs != null) {
                        try {
                            JSONObject jmsg = new JSONObject();
                            jmsg.put("text", message);
                            jmsg.put("date", messageObject.messageOwner.date);
                            if (messageObject.isFromUser() && lowerId < 0) {
                                sender = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(messageObject.getFromId()));
                                if (sender != null) {
                                    jmsg.put("fname", sender.first_name);
                                    jmsg.put("lname", sender.last_name);
                                }
                            }
                            serializedMsgs.put(jmsg);
                        } catch (JSONException e2) {
                        }
                    }
                    if (dialog_id == 777000 && messageObject.messageOwner.reply_markup != null) {
                        rows = messageObject.messageOwner.reply_markup.rows;
                        rowsMid = messageObject.getId();
                    }
                } else if (BuildVars.LOGS_ENABLED) {
                    FileLog.w("message text is null for " + messageObject.getId() + " did = " + messageObject.getDialogId());
                }
            }
            intent = new Intent(ApplicationLoader.applicationContext, LaunchActivity.class);
            intent.setAction("com.tmessages.openchat" + Math.random() + ConnectionsManager.DEFAULT_DATACENTER_ID);
            intent.setFlags(32768);
            if (lowerId == 0) {
                intent.putExtra("encId", highId);
            } else if (lowerId > 0) {
                intent.putExtra("userId", lowerId);
            } else {
                intent.putExtra("chatId", -lowerId);
            }
            intent.putExtra("currentAccount", this.currentAccount);
            PendingIntent contentIntent = PendingIntent.getActivity(ApplicationLoader.applicationContext, 0, intent, 1073741824);
            WearableExtender wearableExtender = new WearableExtender();
            if (wearReplyAction != null) {
                wearableExtender.addAction(wearReplyAction);
            }
            Action readAction = new Action.Builder(R.drawable.menu_read, LocaleController.getString("MarkAsRead", R.string.MarkAsRead), PendingIntent.getBroadcast(ApplicationLoader.applicationContext, internalId.intValue(), intent, 134217728)).build();
            if (lowerId != 0) {
                if (lowerId > 0) {
                    dismissalID = "tguser" + lowerId + "_" + max_id;
                } else {
                    dismissalID = "tgchat" + (-lowerId) + "_" + max_id;
                }
            } else if (dialog_id != globalSecretChatId) {
                dismissalID = "tgenc" + highId + "_" + max_id;
            } else {
                dismissalID = null;
            }
            if (dismissalID != null) {
                wearableExtender.setDismissalId(dismissalID);
                WearableExtender summaryExtender = new WearableExtender();
                summaryExtender.setDismissalId("summary_" + dismissalID);
                notificationBuilder.extend(summaryExtender);
            }
            wearableExtender.setBridgeTag("tgaccount" + UserConfig.getInstance(this.currentAccount).getClientUserId());
            long date = ((long) ((MessageObject) messageObjects.get(0)).messageOwner.date) * 1000;
            NotificationCompat.Builder builder = new NotificationCompat.Builder(ApplicationLoader.applicationContext).setContentTitle(name).setSmallIcon(R.drawable.notification).setContentText(text.toString()).setAutoCancel(true).setNumber(messageObjects.size()).setColor(-13851168).setGroupSummary(false).setWhen(date).setShowWhen(true).setShortcutId("sdid_" + dialog_id).setStyle(messagingStyle).setContentIntent(contentIntent).extend(wearableExtender).setSortKey(TtmlNode.ANONYMOUS_REGION_ID + (Long.MAX_VALUE - date)).extend(new CarExtender().setUnreadConversation(unreadConvBuilder.build())).setCategory("msg");
            if (useSummaryNotification) {
                builder.setGroup(this.notificationGroup);
                builder.setGroupAlertBehavior(1);
            }
            if (wearReplyAction != null) {
                builder.addAction(wearReplyAction);
            }
            builder.addAction(readAction);
            if (this.pushDialogs.size() == 1 && !TextUtils.isEmpty(summary)) {
                builder.setSubText(summary);
            }
            if (lowerId == 0) {
                builder.setLocalOnly(true);
            }
            if (avatarBitmap != null && VERSION.SDK_INT < 28) {
                builder.setLargeIcon(avatarBitmap);
            }
            if (!(AndroidUtilities.needShowPasscode(false) || SharedConfig.isWaitingForPasscodeEnter || rows == null)) {
                int rc = rows.size();
                for (int r = 0; r < rc; r++) {
                    TL_keyboardButtonRow row = (TL_keyboardButtonRow) rows.get(r);
                    int cc = row.buttons.size();
                    for (int c = 0; c < cc; c++) {
                        KeyboardButton button = (KeyboardButton) row.buttons.get(c);
                        if (button instanceof TL_keyboardButtonCallback) {
                            intent = new Intent(ApplicationLoader.applicationContext, NotificationCallbackReceiver.class);
                            intent.putExtra("currentAccount", this.currentAccount);
                            intent.putExtra("did", dialog_id);
                            if (button.data != null) {
                                intent.putExtra(DataSchemeDataSource.SCHEME_DATA, button.data);
                            }
                            intent.putExtra("mid", rowsMid);
                            String str = button.text;
                            Context context = ApplicationLoader.applicationContext;
                            int i2 = this.lastButtonId;
                            this.lastButtonId = i2 + 1;
                            builder.addAction(0, str, PendingIntent.getBroadcast(context, i2, intent, 134217728));
                        }
                    }
                }
            }
            if (chat == null && user != null && user.phone != null && user.phone.length() > 0) {
                builder.addPerson("tel:+" + user.phone);
            }
            if (VERSION.SDK_INT >= 26) {
                if (useSummaryNotification) {
                    builder.setChannelId(OTHER_NOTIFICATIONS_CHANNEL);
                } else {
                    builder.setChannelId(mainNotification.getChannelId());
                }
            }
            holders.add(new AnonymousClass1NotificationHolder(internalId.intValue(), builder.build()));
            this.wearNotificationsIds.put(dialog_id, internalId);
            if (!(lowerId == 0 || jSONObject == null)) {
                try {
                    jSONObject.put("reply", canReply);
                    jSONObject.put("name", name);
                    jSONObject.put("max_id", max_id);
                    jSONObject.put("max_date", max_date);
                    jSONObject.put(TtmlNode.ATTR_ID, Math.abs(lowerId));
                    if (photoPath != null) {
                        jSONObject.put("photo", photoPath.dc_id + "_" + photoPath.volume_id + "_" + photoPath.secret);
                    }
                    if (serializedMsgs != null) {
                        jSONObject.put("msgs", serializedMsgs);
                    }
                    if (lowerId > 0) {
                        jSONObject.put("type", "user");
                    } else if (lowerId < 0) {
                        if (isChannel || isSupergroup) {
                            jSONObject.put("type", "channel");
                        } else {
                            jSONObject.put("type", "group");
                        }
                    }
                    serializedNotifications.put(jSONObject);
                } catch (JSONException e3) {
                }
            }
        }
        if (useSummaryNotification) {
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("show summary with id " + this.notificationId);
            }
            notificationManager.notify(this.notificationId, mainNotification);
        } else {
            notificationManager.cancel(this.notificationId);
        }
        size = holders.size();
        for (a = 0; a < size; a++) {
            ((AnonymousClass1NotificationHolder) holders.get(a)).call();
        }
        for (a = 0; a < oldIdsWear.size(); a++) {
            Integer id = (Integer) oldIdsWear.valueAt(a);
            if (BuildVars.LOGS_ENABLED) {
                FileLog.w("cancel notification id " + id);
            }
            notificationManager.cancel(id.intValue());
        }
        if (serializedNotifications != null) {
            try {
                JSONObject s = new JSONObject();
                s.put(TtmlNode.ATTR_ID, UserConfig.getInstance(this.currentAccount).getClientUserId());
                s.put("n", serializedNotifications);
                WearDataLayerListenerService.sendMessageToWatch("/notify", s.toString().getBytes(C.UTF8_NAME), "remote_notifications");
            } catch (Exception e4) {
            }
        }
    }

    static final /* synthetic */ int lambda$null$27$NotificationsController(Canvas canvas) {
        Path path = new Path();
        path.setFillType(FillType.INVERSE_EVEN_ODD);
        int width = canvas.getWidth();
        path.addRoundRect(0.0f, 0.0f, (float) width, (float) canvas.getHeight(), (float) (width / 2), (float) (width / 2), Direction.CW);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(0);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC));
        canvas.drawPath(path, paint);
        return -3;
    }

    public void playOutChatSound() {
        if (this.inChatSoundEnabled && !MediaController.getInstance().isRecordingAudio()) {
            try {
                if (audioManager.getRingerMode() == 0) {
                    return;
                }
            } catch (Throwable e) {
                FileLog.e(e);
            }
            notificationsQueue.postRunnable(new NotificationsController$$Lambda$16(this));
        }
    }

    final /* synthetic */ void lambda$playOutChatSound$30$NotificationsController() {
        try {
            if (Math.abs(System.currentTimeMillis() - this.lastSoundOutPlay) > 100) {
                this.lastSoundOutPlay = System.currentTimeMillis();
                if (this.soundPool == null) {
                    this.soundPool = new SoundPool(3, 1, 0);
                    this.soundPool.setOnLoadCompleteListener(NotificationsController$$Lambda$18.$instance);
                }
                if (this.soundOut == 0 && !this.soundOutLoaded) {
                    this.soundOutLoaded = true;
                    this.soundOut = this.soundPool.load(ApplicationLoader.applicationContext, R.raw.sound_out, 1);
                }
                if (this.soundOut != 0) {
                    try {
                        this.soundPool.play(this.soundOut, 1.0f, 1.0f, 1, 0, 1.0f);
                    } catch (Throwable e) {
                        FileLog.e(e);
                    }
                }
            }
        } catch (Throwable e2) {
            FileLog.e(e2);
        }
    }

    static final /* synthetic */ void lambda$null$29$NotificationsController(SoundPool soundPool, int sampleId, int status) {
        if (status == 0) {
            try {
                soundPool.play(sampleId, 1.0f, 1.0f, 1, 0, 1.0f);
            } catch (Throwable e) {
                FileLog.e(e);
            }
        }
    }

    public void updateServerNotificationsSettings(long dialog_id) {
        int i = 0;
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.notificationsSettingsUpdated, new Object[0]);
        if (((int) dialog_id) != 0) {
            SharedPreferences preferences = MessagesController.getNotificationsSettings(this.currentAccount);
            TL_account_updateNotifySettings req = new TL_account_updateNotifySettings();
            req.settings = new TL_inputPeerNotifySettings();
            TL_inputPeerNotifySettings tL_inputPeerNotifySettings = req.settings;
            tL_inputPeerNotifySettings.flags |= 1;
            req.settings.show_previews = preferences.getBoolean("preview_" + dialog_id, true);
            tL_inputPeerNotifySettings = req.settings;
            tL_inputPeerNotifySettings.flags |= 2;
            req.settings.silent = preferences.getBoolean("silent_" + dialog_id, false);
            int mute_type = preferences.getInt("notify2_" + dialog_id, -1);
            if (mute_type != -1) {
                tL_inputPeerNotifySettings = req.settings;
                tL_inputPeerNotifySettings.flags |= 4;
                if (mute_type == 3) {
                    req.settings.mute_until = preferences.getInt("notifyuntil_" + dialog_id, 0);
                } else {
                    tL_inputPeerNotifySettings = req.settings;
                    if (mute_type == 2) {
                        i = ConnectionsManager.DEFAULT_DATACENTER_ID;
                    }
                    tL_inputPeerNotifySettings.mute_until = i;
                }
            }
            req.peer = new TL_inputNotifyPeer();
            ((TL_inputNotifyPeer) req.peer).peer = MessagesController.getInstance(this.currentAccount).getInputPeer((int) dialog_id);
            ConnectionsManager.getInstance(this.currentAccount).sendRequest(req, NotificationsController$$Lambda$17.$instance);
        }
    }

    static final /* synthetic */ void lambda$updateServerNotificationsSettings$31$NotificationsController(TLObject response, TL_error error) {
    }
}
