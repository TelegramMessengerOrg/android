package org.telegram.ui;

import android.app.Activity;
import android.app.ActivityManager.TaskDescription;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.StatFs;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.ContactsController.Contact;
import org.telegram.messenger.DataQuery;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.LocaleController.LocaleInfo;
import org.telegram.messenger.LocationController.SharingLocationInfo;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.SendMessagesHelper.SendingMediaInfo;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.beta.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.camera.CameraController;
import org.telegram.messenger.support.widget.helper.ItemTouchHelper.Callback;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatInvite;
import org.telegram.tgnet.TLRPC.LangPackString;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.TL_account_authorizationForm;
import org.telegram.tgnet.TLRPC.TL_account_getAuthorizationForm;
import org.telegram.tgnet.TLRPC.TL_account_getPassword;
import org.telegram.tgnet.TLRPC.TL_account_password;
import org.telegram.tgnet.TLRPC.TL_contacts_resolveUsername;
import org.telegram.tgnet.TLRPC.TL_contacts_resolvedPeer;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_help_appUpdate;
import org.telegram.tgnet.TLRPC.TL_help_deepLinkInfo;
import org.telegram.tgnet.TLRPC.TL_help_getAppUpdate;
import org.telegram.tgnet.TLRPC.TL_help_getDeepLinkInfo;
import org.telegram.tgnet.TLRPC.TL_help_termsOfService;
import org.telegram.tgnet.TLRPC.TL_inputGameShortName;
import org.telegram.tgnet.TLRPC.TL_inputMediaGame;
import org.telegram.tgnet.TLRPC.TL_inputStickerSetShortName;
import org.telegram.tgnet.TLRPC.TL_langpack_getStrings;
import org.telegram.tgnet.TLRPC.TL_messages_checkChatInvite;
import org.telegram.tgnet.TLRPC.TL_messages_importChatInvite;
import org.telegram.tgnet.TLRPC.TL_webPage;
import org.telegram.tgnet.TLRPC.Updates;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.Vector;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.ActionBarLayout.ActionBarLayoutDelegate;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.DrawerLayoutContainer;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.Theme.ThemeInfo;
import org.telegram.ui.Adapters.DrawerLayoutAdapter;
import org.telegram.ui.Cells.DrawerAddCell;
import org.telegram.ui.Cells.DrawerUserCell;
import org.telegram.ui.Cells.LanguageCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.BlockingUpdateView;
import org.telegram.ui.Components.EmbedBottomSheet;
import org.telegram.ui.Components.JoinGroupAlert;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.PasscodeView;
import org.telegram.ui.Components.PipRoundVideoView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.StickersAlert;
import org.telegram.ui.Components.TermsOfServiceView;
import org.telegram.ui.Components.TermsOfServiceView.TermsOfServiceViewDelegate;
import org.telegram.ui.Components.ThemeEditorView;
import org.telegram.ui.Components.UpdateAppAlertDialog;
import org.telegram.ui.DialogsActivity.DialogsActivityDelegate;

public class LaunchActivity extends Activity implements NotificationCenterDelegate, ActionBarLayoutDelegate, DialogsActivityDelegate {
    private static ArrayList<BaseFragment> layerFragmentsStack = new ArrayList();
    private static ArrayList<BaseFragment> mainFragmentsStack = new ArrayList();
    private static ArrayList<BaseFragment> rightFragmentsStack = new ArrayList();
    private ActionBarLayout actionBarLayout;
    private View backgroundTablet;
    private BlockingUpdateView blockingUpdateView;
    private ArrayList<User> contactsToSend;
    private Uri contactsToSendUri;
    private int currentAccount;
    private int currentConnectionState;
    private String documentsMimeType;
    private ArrayList<String> documentsOriginalPathsArray;
    private ArrayList<String> documentsPathsArray;
    private ArrayList<Uri> documentsUrisArray;
    private DrawerLayoutAdapter drawerLayoutAdapter;
    protected DrawerLayoutContainer drawerLayoutContainer;
    private HashMap<String, String> englishLocaleStrings;
    private boolean finished;
    private ActionBarLayout layersActionBarLayout;
    private boolean loadingLocaleDialog;
    private AlertDialog localeDialog;
    private Runnable lockRunnable;
    private OnGlobalLayoutListener onGlobalLayoutListener;
    private Intent passcodeSaveIntent;
    private boolean passcodeSaveIntentIsNew;
    private boolean passcodeSaveIntentIsRestore;
    private PasscodeView passcodeView;
    private ArrayList<SendingMediaInfo> photoPathsArray;
    private AlertDialog proxyErrorDialog;
    private ActionBarLayout rightActionBarLayout;
    private String sendingText;
    private FrameLayout shadowTablet;
    private FrameLayout shadowTabletSide;
    private RecyclerListView sideMenu;
    private HashMap<String, String> systemLocaleStrings;
    private boolean tabletFullSize;
    private TermsOfServiceView termsOfServiceView;
    private String videoPath;
    private AlertDialog visibleDialog;

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void onCreate(android.os.Bundle r41) {
        /*
        r40 = this;
        org.telegram.messenger.ApplicationLoader.postInitApplication();
        r35 = r40.getResources();
        r35 = r35.getConfiguration();
        r0 = r40;
        r1 = r35;
        org.telegram.messenger.AndroidUtilities.checkDisplaySize(r0, r1);
        r35 = org.telegram.messenger.UserConfig.selectedAccount;
        r0 = r35;
        r1 = r40;
        r1.currentAccount = r0;
        r0 = r40;
        r0 = r0.currentAccount;
        r35 = r0;
        r35 = org.telegram.messenger.UserConfig.getInstance(r35);
        r35 = r35.isClientActivated();
        if (r35 != 0) goto L_0x013b;
    L_0x002a:
        r19 = r40.getIntent();
        r21 = 0;
        if (r19 == 0) goto L_0x00aa;
    L_0x0032:
        r35 = r19.getAction();
        if (r35 == 0) goto L_0x00aa;
    L_0x0038:
        r35 = "android.intent.action.SEND";
        r36 = r19.getAction();
        r35 = r35.equals(r36);
        if (r35 != 0) goto L_0x0052;
    L_0x0045:
        r35 = "android.intent.action.SEND_MULTIPLE";
        r36 = r19.getAction();
        r35 = r35.equals(r36);
        if (r35 == 0) goto L_0x0059;
    L_0x0052:
        super.onCreate(r41);
        r40.finish();
    L_0x0058:
        return;
    L_0x0059:
        r35 = "android.intent.action.VIEW";
        r36 = r19.getAction();
        r35 = r35.equals(r36);
        if (r35 == 0) goto L_0x00aa;
    L_0x0066:
        r32 = r19.getData();
        if (r32 == 0) goto L_0x00aa;
    L_0x006c:
        r35 = r32.toString();
        r33 = r35.toLowerCase();
        r35 = "tg:proxy";
        r0 = r33;
        r1 = r35;
        r35 = r0.startsWith(r1);
        if (r35 != 0) goto L_0x00a8;
    L_0x0081:
        r35 = "tg://proxy";
        r0 = r33;
        r1 = r35;
        r35 = r0.startsWith(r1);
        if (r35 != 0) goto L_0x00a8;
    L_0x008e:
        r35 = "tg:socks";
        r0 = r33;
        r1 = r35;
        r35 = r0.startsWith(r1);
        if (r35 != 0) goto L_0x00a8;
    L_0x009b:
        r35 = "tg://socks";
        r0 = r33;
        r1 = r35;
        r35 = r0.startsWith(r1);
        if (r35 == 0) goto L_0x0137;
    L_0x00a8:
        r21 = 1;
    L_0x00aa:
        r26 = org.telegram.messenger.MessagesController.getGlobalMainSettings();
        r35 = "intro_crashed_time";
        r36 = 0;
        r0 = r26;
        r1 = r35;
        r2 = r36;
        r10 = r0.getLong(r1, r2);
        r35 = "fromIntro";
        r36 = 0;
        r0 = r19;
        r1 = r35;
        r2 = r36;
        r17 = r0.getBooleanExtra(r1, r2);
        if (r17 == 0) goto L_0x00e4;
    L_0x00ce:
        r35 = r26.edit();
        r36 = "intro_crashed_time";
        r38 = 0;
        r0 = r35;
        r1 = r36;
        r2 = r38;
        r35 = r0.putLong(r1, r2);
        r35.commit();
    L_0x00e4:
        if (r21 != 0) goto L_0x013b;
    L_0x00e6:
        r36 = java.lang.System.currentTimeMillis();
        r36 = r10 - r36;
        r36 = java.lang.Math.abs(r36);
        r38 = 120000; // 0x1d4c0 float:1.68156E-40 double:5.9288E-319;
        r35 = (r36 > r38 ? 1 : (r36 == r38 ? 0 : -1));
        if (r35 < 0) goto L_0x013b;
    L_0x00f7:
        if (r19 == 0) goto L_0x013b;
    L_0x00f9:
        if (r17 != 0) goto L_0x013b;
    L_0x00fb:
        r35 = org.telegram.messenger.ApplicationLoader.applicationContext;
        r36 = "logininfo2";
        r37 = 0;
        r26 = r35.getSharedPreferences(r36, r37);
        r31 = r26.getAll();
        r35 = r31.isEmpty();
        if (r35 == 0) goto L_0x013b;
    L_0x0110:
        r20 = new android.content.Intent;
        r35 = org.telegram.ui.IntroActivity.class;
        r0 = r20;
        r1 = r40;
        r2 = r35;
        r0.<init>(r1, r2);
        r35 = r19.getData();
        r0 = r20;
        r1 = r35;
        r0.setData(r1);
        r0 = r40;
        r1 = r20;
        r0.startActivity(r1);
        super.onCreate(r41);
        r40.finish();
        goto L_0x0058;
    L_0x0137:
        r21 = 0;
        goto L_0x00aa;
    L_0x013b:
        r35 = 1;
        r0 = r40;
        r1 = r35;
        r0.requestWindowFeature(r1);
        r35 = 2131558412; // 0x7f0d000c float:1.874214E38 double:1.0531297835E-314;
        r0 = r40;
        r1 = r35;
        r0.setTheme(r1);
        r35 = android.os.Build.VERSION.SDK_INT;
        r36 = 21;
        r0 = r35;
        r1 = r36;
        if (r0 < r1) goto L_0x0173;
    L_0x0158:
        r35 = new android.app.ActivityManager$TaskDescription;	 Catch:{ Exception -> 0x0943 }
        r36 = 0;
        r37 = 0;
        r38 = "actionBarDefault";
        r38 = org.telegram.ui.ActionBar.Theme.getColor(r38);	 Catch:{ Exception -> 0x0943 }
        r39 = -16777216; // 0xffffffffff000000 float:-1.7014118E38 double:NaN;
        r38 = r38 | r39;
        r35.<init>(r36, r37, r38);	 Catch:{ Exception -> 0x0943 }
        r0 = r40;
        r1 = r35;
        r0.setTaskDescription(r1);	 Catch:{ Exception -> 0x0943 }
    L_0x0173:
        r35 = r40.getWindow();
        r36 = 2131165669; // 0x7f0701e5 float:1.7945562E38 double:1.0529357426E-314;
        r35.setBackgroundDrawableResource(r36);
        r35 = org.telegram.messenger.SharedConfig.passcodeHash;
        r35 = r35.length();
        if (r35 <= 0) goto L_0x0194;
    L_0x0185:
        r35 = org.telegram.messenger.SharedConfig.allowScreenCapture;
        if (r35 != 0) goto L_0x0194;
    L_0x0189:
        r35 = r40.getWindow();	 Catch:{ Exception -> 0x0698 }
        r36 = 8192; // 0x2000 float:1.14794E-41 double:4.0474E-320;
        r37 = 8192; // 0x2000 float:1.14794E-41 double:4.0474E-320;
        r35.setFlags(r36, r37);	 Catch:{ Exception -> 0x0698 }
    L_0x0194:
        super.onCreate(r41);
        r35 = android.os.Build.VERSION.SDK_INT;
        r36 = 24;
        r0 = r35;
        r1 = r36;
        if (r0 < r1) goto L_0x01a7;
    L_0x01a1:
        r35 = r40.isInMultiWindowMode();
        org.telegram.messenger.AndroidUtilities.isInMultiwindow = r35;
    L_0x01a7:
        r35 = 0;
        r0 = r40;
        r1 = r35;
        org.telegram.ui.ActionBar.Theme.createChatResources(r0, r1);
        r35 = org.telegram.messenger.SharedConfig.passcodeHash;
        r35 = r35.length();
        if (r35 == 0) goto L_0x01cc;
    L_0x01b8:
        r35 = org.telegram.messenger.SharedConfig.appLocked;
        if (r35 == 0) goto L_0x01cc;
    L_0x01bc:
        r0 = r40;
        r0 = r0.currentAccount;
        r35 = r0;
        r35 = org.telegram.tgnet.ConnectionsManager.getInstance(r35);
        r35 = r35.getCurrentTime();
        org.telegram.messenger.SharedConfig.lastPauseTime = r35;
    L_0x01cc:
        r35 = r40.getResources();
        r36 = "status_bar_height";
        r37 = "dimen";
        r38 = "android";
        r28 = r35.getIdentifier(r36, r37, r38);
        if (r28 <= 0) goto L_0x01ed;
    L_0x01df:
        r35 = r40.getResources();
        r0 = r35;
        r1 = r28;
        r35 = r0.getDimensionPixelSize(r1);
        org.telegram.messenger.AndroidUtilities.statusBarHeight = r35;
    L_0x01ed:
        r35 = new org.telegram.ui.ActionBar.ActionBarLayout;
        r0 = r35;
        r1 = r40;
        r0.<init>(r1);
        r0 = r35;
        r1 = r40;
        r1.actionBarLayout = r0;
        r35 = new org.telegram.ui.ActionBar.DrawerLayoutContainer;
        r0 = r35;
        r1 = r40;
        r0.<init>(r1);
        r0 = r35;
        r1 = r40;
        r1.drawerLayoutContainer = r0;
        r0 = r40;
        r0 = r0.drawerLayoutContainer;
        r35 = r0;
        r36 = new android.view.ViewGroup$LayoutParams;
        r37 = -1;
        r38 = -1;
        r36.<init>(r37, r38);
        r0 = r40;
        r1 = r35;
        r2 = r36;
        r0.setContentView(r1, r2);
        r35 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r35 == 0) goto L_0x06a6;
    L_0x0229:
        r35 = r40.getWindow();
        r36 = 16;
        r35.setSoftInputMode(r36);
        r22 = new org.telegram.ui.LaunchActivity$1;
        r0 = r22;
        r1 = r40;
        r2 = r40;
        r0.<init>(r2);
        r0 = r40;
        r0 = r0.drawerLayoutContainer;
        r35 = r0;
        r36 = -1;
        r37 = -1082130432; // 0xffffffffbf800000 float:-1.0 double:NaN;
        r36 = org.telegram.ui.Components.LayoutHelper.createFrame(r36, r37);
        r0 = r35;
        r1 = r22;
        r2 = r36;
        r0.addView(r1, r2);
        r35 = new android.view.View;
        r0 = r35;
        r1 = r40;
        r0.<init>(r1);
        r0 = r35;
        r1 = r40;
        r1.backgroundTablet = r0;
        r35 = r40.getResources();
        r36 = 2131165246; // 0x7f07003e float:1.7944704E38 double:1.0529355337E-314;
        r13 = r35.getDrawable(r36);
        r13 = (android.graphics.drawable.BitmapDrawable) r13;
        r35 = android.graphics.Shader.TileMode.REPEAT;
        r36 = android.graphics.Shader.TileMode.REPEAT;
        r0 = r35;
        r1 = r36;
        r13.setTileModeXY(r0, r1);
        r0 = r40;
        r0 = r0.backgroundTablet;
        r35 = r0;
        r0 = r35;
        r0.setBackgroundDrawable(r13);
        r0 = r40;
        r0 = r0.backgroundTablet;
        r35 = r0;
        r36 = -1;
        r37 = -1;
        r36 = org.telegram.ui.Components.LayoutHelper.createRelative(r36, r37);
        r0 = r22;
        r1 = r35;
        r2 = r36;
        r0.addView(r1, r2);
        r0 = r40;
        r0 = r0.actionBarLayout;
        r35 = r0;
        r0 = r22;
        r1 = r35;
        r0.addView(r1);
        r35 = new org.telegram.ui.ActionBar.ActionBarLayout;
        r0 = r35;
        r1 = r40;
        r0.<init>(r1);
        r0 = r35;
        r1 = r40;
        r1.rightActionBarLayout = r0;
        r0 = r40;
        r0 = r0.rightActionBarLayout;
        r35 = r0;
        r36 = rightFragmentsStack;
        r35.init(r36);
        r0 = r40;
        r0 = r0.rightActionBarLayout;
        r35 = r0;
        r0 = r35;
        r1 = r40;
        r0.setDelegate(r1);
        r0 = r40;
        r0 = r0.rightActionBarLayout;
        r35 = r0;
        r0 = r22;
        r1 = r35;
        r0.addView(r1);
        r35 = new android.widget.FrameLayout;
        r0 = r35;
        r1 = r40;
        r0.<init>(r1);
        r0 = r35;
        r1 = r40;
        r1.shadowTabletSide = r0;
        r0 = r40;
        r0 = r0.shadowTabletSide;
        r35 = r0;
        r36 = 1076449908; // 0x40295274 float:2.6456575 double:5.31836919E-315;
        r35.setBackgroundColor(r36);
        r0 = r40;
        r0 = r0.shadowTabletSide;
        r35 = r0;
        r0 = r22;
        r1 = r35;
        r0.addView(r1);
        r35 = new android.widget.FrameLayout;
        r0 = r35;
        r1 = r40;
        r0.<init>(r1);
        r0 = r35;
        r1 = r40;
        r1.shadowTablet = r0;
        r0 = r40;
        r0 = r0.shadowTablet;
        r36 = r0;
        r35 = layerFragmentsStack;
        r35 = r35.isEmpty();
        if (r35 == 0) goto L_0x069e;
    L_0x0323:
        r35 = 8;
    L_0x0325:
        r0 = r36;
        r1 = r35;
        r0.setVisibility(r1);
        r0 = r40;
        r0 = r0.shadowTablet;
        r35 = r0;
        r36 = 2130706432; // 0x7f000000 float:1.7014118E38 double:1.0527088494E-314;
        r35.setBackgroundColor(r36);
        r0 = r40;
        r0 = r0.shadowTablet;
        r35 = r0;
        r0 = r22;
        r1 = r35;
        r0.addView(r1);
        r0 = r40;
        r0 = r0.shadowTablet;
        r35 = r0;
        r36 = new org.telegram.ui.LaunchActivity$$Lambda$0;
        r0 = r36;
        r1 = r40;
        r0.<init>(r1);
        r35.setOnTouchListener(r36);
        r0 = r40;
        r0 = r0.shadowTablet;
        r35 = r0;
        r36 = org.telegram.ui.LaunchActivity$$Lambda$1.$instance;
        r35.setOnClickListener(r36);
        r35 = new org.telegram.ui.ActionBar.ActionBarLayout;
        r0 = r35;
        r1 = r40;
        r0.<init>(r1);
        r0 = r35;
        r1 = r40;
        r1.layersActionBarLayout = r0;
        r0 = r40;
        r0 = r0.layersActionBarLayout;
        r35 = r0;
        r36 = 1;
        r35.setRemoveActionBarExtraHeight(r36);
        r0 = r40;
        r0 = r0.layersActionBarLayout;
        r35 = r0;
        r0 = r40;
        r0 = r0.shadowTablet;
        r36 = r0;
        r35.setBackgroundView(r36);
        r0 = r40;
        r0 = r0.layersActionBarLayout;
        r35 = r0;
        r36 = 1;
        r35.setUseAlphaAnimations(r36);
        r0 = r40;
        r0 = r0.layersActionBarLayout;
        r35 = r0;
        r36 = 2131165231; // 0x7f07002f float:1.7944673E38 double:1.0529355262E-314;
        r35.setBackgroundResource(r36);
        r0 = r40;
        r0 = r0.layersActionBarLayout;
        r35 = r0;
        r36 = layerFragmentsStack;
        r35.init(r36);
        r0 = r40;
        r0 = r0.layersActionBarLayout;
        r35 = r0;
        r0 = r35;
        r1 = r40;
        r0.setDelegate(r1);
        r0 = r40;
        r0 = r0.layersActionBarLayout;
        r35 = r0;
        r0 = r40;
        r0 = r0.drawerLayoutContainer;
        r36 = r0;
        r35.setDrawerLayoutContainer(r36);
        r0 = r40;
        r0 = r0.layersActionBarLayout;
        r36 = r0;
        r35 = layerFragmentsStack;
        r35 = r35.isEmpty();
        if (r35 == 0) goto L_0x06a2;
    L_0x03d6:
        r35 = 8;
    L_0x03d8:
        r0 = r36;
        r1 = r35;
        r0.setVisibility(r1);
        r0 = r40;
        r0 = r0.layersActionBarLayout;
        r35 = r0;
        r0 = r22;
        r1 = r35;
        r0.addView(r1);
    L_0x03ec:
        r35 = new org.telegram.ui.Components.RecyclerListView;
        r0 = r35;
        r1 = r40;
        r0.<init>(r1);
        r0 = r35;
        r1 = r40;
        r1.sideMenu = r0;
        r0 = r40;
        r0 = r0.sideMenu;
        r35 = r0;
        r35 = r35.getItemAnimator();
        r35 = (org.telegram.messenger.support.widget.DefaultItemAnimator) r35;
        r36 = 0;
        r35.setDelayAnimations(r36);
        r0 = r40;
        r0 = r0.sideMenu;
        r35 = r0;
        r36 = "chats_menuBackground";
        r36 = org.telegram.ui.ActionBar.Theme.getColor(r36);
        r35.setBackgroundColor(r36);
        r0 = r40;
        r0 = r0.sideMenu;
        r35 = r0;
        r36 = new org.telegram.messenger.support.widget.LinearLayoutManager;
        r37 = 1;
        r38 = 0;
        r0 = r36;
        r1 = r40;
        r2 = r37;
        r3 = r38;
        r0.<init>(r1, r2, r3);
        r35.setLayoutManager(r36);
        r0 = r40;
        r0 = r0.sideMenu;
        r35 = r0;
        r36 = new org.telegram.ui.Adapters.DrawerLayoutAdapter;
        r0 = r36;
        r1 = r40;
        r0.<init>(r1);
        r0 = r36;
        r1 = r40;
        r1.drawerLayoutAdapter = r0;
        r35.setAdapter(r36);
        r0 = r40;
        r0 = r0.drawerLayoutContainer;
        r35 = r0;
        r0 = r40;
        r0 = r0.sideMenu;
        r36 = r0;
        r35.setDrawerLayout(r36);
        r0 = r40;
        r0 = r0.sideMenu;
        r35 = r0;
        r23 = r35.getLayoutParams();
        r23 = (android.widget.FrameLayout.LayoutParams) r23;
        r29 = org.telegram.messenger.AndroidUtilities.getRealScreenSize();
        r35 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r35 == 0) goto L_0x06c0;
    L_0x0473:
        r35 = 1134559232; // 0x43a00000 float:320.0 double:5.605467397E-315;
        r35 = org.telegram.messenger.AndroidUtilities.dp(r35);
    L_0x0479:
        r0 = r35;
        r1 = r23;
        r1.width = r0;
        r35 = -1;
        r0 = r35;
        r1 = r23;
        r1.height = r0;
        r0 = r40;
        r0 = r0.sideMenu;
        r35 = r0;
        r0 = r35;
        r1 = r23;
        r0.setLayoutParams(r1);
        r0 = r40;
        r0 = r0.sideMenu;
        r35 = r0;
        r36 = new org.telegram.ui.LaunchActivity$$Lambda$2;
        r0 = r36;
        r1 = r40;
        r0.<init>(r1);
        r35.setOnItemClickListener(r36);
        r0 = r40;
        r0 = r0.drawerLayoutContainer;
        r35 = r0;
        r0 = r40;
        r0 = r0.actionBarLayout;
        r36 = r0;
        r35.setParentActionBarLayout(r36);
        r0 = r40;
        r0 = r0.actionBarLayout;
        r35 = r0;
        r0 = r40;
        r0 = r0.drawerLayoutContainer;
        r36 = r0;
        r35.setDrawerLayoutContainer(r36);
        r0 = r40;
        r0 = r0.actionBarLayout;
        r35 = r0;
        r36 = mainFragmentsStack;
        r35.init(r36);
        r0 = r40;
        r0 = r0.actionBarLayout;
        r35 = r0;
        r0 = r35;
        r1 = r40;
        r0.setDelegate(r1);
        org.telegram.ui.ActionBar.Theme.loadWallpaper();
        r35 = new org.telegram.ui.Components.PasscodeView;
        r0 = r35;
        r1 = r40;
        r0.<init>(r1);
        r0 = r35;
        r1 = r40;
        r1.passcodeView = r0;
        r0 = r40;
        r0 = r0.drawerLayoutContainer;
        r35 = r0;
        r0 = r40;
        r0 = r0.passcodeView;
        r36 = r0;
        r37 = -1;
        r38 = -1082130432; // 0xffffffffbf800000 float:-1.0 double:NaN;
        r37 = org.telegram.ui.Components.LayoutHelper.createFrame(r37, r38);
        r35.addView(r36, r37);
        r40.checkCurrentAccount();
        r35 = org.telegram.messenger.NotificationCenter.getGlobalInstance();
        r36 = org.telegram.messenger.NotificationCenter.closeOtherAppActivities;
        r37 = 1;
        r0 = r37;
        r0 = new java.lang.Object[r0];
        r37 = r0;
        r38 = 0;
        r37[r38] = r40;
        r35.postNotificationName(r36, r37);
        r0 = r40;
        r0 = r0.currentAccount;
        r35 = r0;
        r35 = org.telegram.tgnet.ConnectionsManager.getInstance(r35);
        r35 = r35.getConnectionState();
        r0 = r35;
        r1 = r40;
        r1.currentConnectionState = r0;
        r35 = org.telegram.messenger.NotificationCenter.getGlobalInstance();
        r36 = org.telegram.messenger.NotificationCenter.needShowAlert;
        r0 = r35;
        r1 = r40;
        r2 = r36;
        r0.addObserver(r1, r2);
        r35 = org.telegram.messenger.NotificationCenter.getGlobalInstance();
        r36 = org.telegram.messenger.NotificationCenter.reloadInterface;
        r0 = r35;
        r1 = r40;
        r2 = r36;
        r0.addObserver(r1, r2);
        r35 = org.telegram.messenger.NotificationCenter.getGlobalInstance();
        r36 = org.telegram.messenger.NotificationCenter.suggestedLangpack;
        r0 = r35;
        r1 = r40;
        r2 = r36;
        r0.addObserver(r1, r2);
        r35 = org.telegram.messenger.NotificationCenter.getGlobalInstance();
        r36 = org.telegram.messenger.NotificationCenter.didSetNewTheme;
        r0 = r35;
        r1 = r40;
        r2 = r36;
        r0.addObserver(r1, r2);
        r35 = org.telegram.messenger.NotificationCenter.getGlobalInstance();
        r36 = org.telegram.messenger.NotificationCenter.needSetDayNightTheme;
        r0 = r35;
        r1 = r40;
        r2 = r36;
        r0.addObserver(r1, r2);
        r35 = org.telegram.messenger.NotificationCenter.getGlobalInstance();
        r36 = org.telegram.messenger.NotificationCenter.closeOtherAppActivities;
        r0 = r35;
        r1 = r40;
        r2 = r36;
        r0.addObserver(r1, r2);
        r35 = org.telegram.messenger.NotificationCenter.getGlobalInstance();
        r36 = org.telegram.messenger.NotificationCenter.didSetPasscode;
        r0 = r35;
        r1 = r40;
        r2 = r36;
        r0.addObserver(r1, r2);
        r35 = org.telegram.messenger.NotificationCenter.getGlobalInstance();
        r36 = org.telegram.messenger.NotificationCenter.didSetNewWallpapper;
        r0 = r35;
        r1 = r40;
        r2 = r36;
        r0.addObserver(r1, r2);
        r35 = org.telegram.messenger.NotificationCenter.getGlobalInstance();
        r36 = org.telegram.messenger.NotificationCenter.notificationsCountUpdated;
        r0 = r35;
        r1 = r40;
        r2 = r36;
        r0.addObserver(r1, r2);
        r0 = r40;
        r0 = r0.actionBarLayout;
        r35 = r0;
        r0 = r35;
        r0 = r0.fragmentsStack;
        r35 = r0;
        r35 = r35.isEmpty();
        if (r35 == 0) goto L_0x085d;
    L_0x05ca:
        r0 = r40;
        r0 = r0.currentAccount;
        r35 = r0;
        r35 = org.telegram.messenger.UserConfig.getInstance(r35);
        r35 = r35.isClientActivated();
        if (r35 != 0) goto L_0x06e4;
    L_0x05da:
        r0 = r40;
        r0 = r0.actionBarLayout;
        r35 = r0;
        r36 = new org.telegram.ui.LoginActivity;
        r36.<init>();
        r35.addFragmentToStack(r36);
        r0 = r40;
        r0 = r0.drawerLayoutContainer;
        r35 = r0;
        r36 = 0;
        r37 = 0;
        r35.setAllowOpenDrawer(r36, r37);
    L_0x05f5:
        if (r41 == 0) goto L_0x061b;
    L_0x05f7:
        r35 = "fragment";
        r0 = r41;
        r1 = r35;
        r16 = r0.getString(r1);	 Catch:{ Exception -> 0x07a5 }
        if (r16 == 0) goto L_0x061b;
    L_0x0604:
        r35 = "args";
        r0 = r41;
        r1 = r35;
        r7 = r0.getBundle(r1);	 Catch:{ Exception -> 0x07a5 }
        r35 = -1;
        r36 = r16.hashCode();	 Catch:{ Exception -> 0x07a5 }
        switch(r36) {
            case -1529105743: goto L_0x0778;
            case -1349522494: goto L_0x0767;
            case 3052376: goto L_0x0712;
            case 3108362: goto L_0x0756;
            case 98629247: goto L_0x0734;
            case 738950403: goto L_0x0745;
            case 1434631203: goto L_0x0723;
            default: goto L_0x0618;
        };
    L_0x0618:
        switch(r35) {
            case 0: goto L_0x0789;
            case 1: goto L_0x07ab;
            case 2: goto L_0x07c6;
            case 3: goto L_0x07e8;
            case 4: goto L_0x0804;
            case 5: goto L_0x0820;
            case 6: goto L_0x0842;
            default: goto L_0x061b;
        };
    L_0x061b:
        r40.checkLayout();
        r36 = r40.getIntent();
        r37 = 0;
        if (r41 == 0) goto L_0x092f;
    L_0x0626:
        r35 = 1;
    L_0x0628:
        r38 = 0;
        r0 = r40;
        r1 = r36;
        r2 = r37;
        r3 = r35;
        r4 = r38;
        r0.handleIntent(r1, r2, r3, r4);
        r24 = android.os.Build.DISPLAY;	 Catch:{ Exception -> 0x093d }
        r25 = android.os.Build.USER;	 Catch:{ Exception -> 0x093d }
        if (r24 == 0) goto L_0x0933;
    L_0x063d:
        r24 = r24.toLowerCase();	 Catch:{ Exception -> 0x093d }
    L_0x0641:
        if (r25 == 0) goto L_0x0938;
    L_0x0643:
        r25 = r24.toLowerCase();	 Catch:{ Exception -> 0x093d }
    L_0x0647:
        r35 = "flyme";
        r0 = r24;
        r1 = r35;
        r35 = r0.contains(r1);	 Catch:{ Exception -> 0x093d }
        if (r35 != 0) goto L_0x0661;
    L_0x0654:
        r35 = "flyme";
        r0 = r25;
        r1 = r35;
        r35 = r0.contains(r1);	 Catch:{ Exception -> 0x093d }
        if (r35 == 0) goto L_0x0687;
    L_0x0661:
        r35 = 1;
        org.telegram.messenger.AndroidUtilities.incorrectDisplaySizeFix = r35;	 Catch:{ Exception -> 0x093d }
        r35 = r40.getWindow();	 Catch:{ Exception -> 0x093d }
        r35 = r35.getDecorView();	 Catch:{ Exception -> 0x093d }
        r34 = r35.getRootView();	 Catch:{ Exception -> 0x093d }
        r35 = r34.getViewTreeObserver();	 Catch:{ Exception -> 0x093d }
        r36 = new org.telegram.ui.LaunchActivity$$Lambda$3;	 Catch:{ Exception -> 0x093d }
        r0 = r36;
        r1 = r34;
        r0.<init>(r1);	 Catch:{ Exception -> 0x093d }
        r0 = r36;
        r1 = r40;
        r1.onGlobalLayoutListener = r0;	 Catch:{ Exception -> 0x093d }
        r35.addOnGlobalLayoutListener(r36);	 Catch:{ Exception -> 0x093d }
    L_0x0687:
        r35 = org.telegram.messenger.MediaController.getInstance();
        r36 = 1;
        r0 = r35;
        r1 = r40;
        r2 = r36;
        r0.setBaseActivity(r1, r2);
        goto L_0x0058;
    L_0x0698:
        r14 = move-exception;
        org.telegram.messenger.FileLog.e(r14);
        goto L_0x0194;
    L_0x069e:
        r35 = 0;
        goto L_0x0325;
    L_0x06a2:
        r35 = 0;
        goto L_0x03d8;
    L_0x06a6:
        r0 = r40;
        r0 = r0.drawerLayoutContainer;
        r35 = r0;
        r0 = r40;
        r0 = r0.actionBarLayout;
        r36 = r0;
        r37 = new android.view.ViewGroup$LayoutParams;
        r38 = -1;
        r39 = -1;
        r37.<init>(r38, r39);
        r35.addView(r36, r37);
        goto L_0x03ec;
    L_0x06c0:
        r35 = 1134559232; // 0x43a00000 float:320.0 double:5.605467397E-315;
        r35 = org.telegram.messenger.AndroidUtilities.dp(r35);
        r0 = r29;
        r0 = r0.x;
        r36 = r0;
        r0 = r29;
        r0 = r0.y;
        r37 = r0;
        r36 = java.lang.Math.min(r36, r37);
        r37 = 1113587712; // 0x42600000 float:56.0 double:5.50185432E-315;
        r37 = org.telegram.messenger.AndroidUtilities.dp(r37);
        r36 = r36 - r37;
        r35 = java.lang.Math.min(r35, r36);
        goto L_0x0479;
    L_0x06e4:
        r12 = new org.telegram.ui.DialogsActivity;
        r35 = 0;
        r0 = r35;
        r12.<init>(r0);
        r0 = r40;
        r0 = r0.sideMenu;
        r35 = r0;
        r0 = r35;
        r12.setSideMenu(r0);
        r0 = r40;
        r0 = r0.actionBarLayout;
        r35 = r0;
        r0 = r35;
        r0.addFragmentToStack(r12);
        r0 = r40;
        r0 = r0.drawerLayoutContainer;
        r35 = r0;
        r36 = 1;
        r37 = 0;
        r35.setAllowOpenDrawer(r36, r37);
        goto L_0x05f5;
    L_0x0712:
        r36 = "chat";
        r0 = r16;
        r1 = r36;
        r36 = r0.equals(r1);	 Catch:{ Exception -> 0x07a5 }
        if (r36 == 0) goto L_0x0618;
    L_0x071f:
        r35 = 0;
        goto L_0x0618;
    L_0x0723:
        r36 = "settings";
        r0 = r16;
        r1 = r36;
        r36 = r0.equals(r1);	 Catch:{ Exception -> 0x07a5 }
        if (r36 == 0) goto L_0x0618;
    L_0x0730:
        r35 = 1;
        goto L_0x0618;
    L_0x0734:
        r36 = "group";
        r0 = r16;
        r1 = r36;
        r36 = r0.equals(r1);	 Catch:{ Exception -> 0x07a5 }
        if (r36 == 0) goto L_0x0618;
    L_0x0741:
        r35 = 2;
        goto L_0x0618;
    L_0x0745:
        r36 = "channel";
        r0 = r16;
        r1 = r36;
        r36 = r0.equals(r1);	 Catch:{ Exception -> 0x07a5 }
        if (r36 == 0) goto L_0x0618;
    L_0x0752:
        r35 = 3;
        goto L_0x0618;
    L_0x0756:
        r36 = "edit";
        r0 = r16;
        r1 = r36;
        r36 = r0.equals(r1);	 Catch:{ Exception -> 0x07a5 }
        if (r36 == 0) goto L_0x0618;
    L_0x0763:
        r35 = 4;
        goto L_0x0618;
    L_0x0767:
        r36 = "chat_profile";
        r0 = r16;
        r1 = r36;
        r36 = r0.equals(r1);	 Catch:{ Exception -> 0x07a5 }
        if (r36 == 0) goto L_0x0618;
    L_0x0774:
        r35 = 5;
        goto L_0x0618;
    L_0x0778:
        r36 = "wallpapers";
        r0 = r16;
        r1 = r36;
        r36 = r0.equals(r1);	 Catch:{ Exception -> 0x07a5 }
        if (r36 == 0) goto L_0x0618;
    L_0x0785:
        r35 = 6;
        goto L_0x0618;
    L_0x0789:
        if (r7 == 0) goto L_0x061b;
    L_0x078b:
        r9 = new org.telegram.ui.ChatActivity;	 Catch:{ Exception -> 0x07a5 }
        r9.<init>(r7);	 Catch:{ Exception -> 0x07a5 }
        r0 = r40;
        r0 = r0.actionBarLayout;	 Catch:{ Exception -> 0x07a5 }
        r35 = r0;
        r0 = r35;
        r35 = r0.addFragmentToStack(r9);	 Catch:{ Exception -> 0x07a5 }
        if (r35 == 0) goto L_0x061b;
    L_0x079e:
        r0 = r41;
        r9.restoreSelfArgs(r0);	 Catch:{ Exception -> 0x07a5 }
        goto L_0x061b;
    L_0x07a5:
        r14 = move-exception;
        org.telegram.messenger.FileLog.e(r14);
        goto L_0x061b;
    L_0x07ab:
        r30 = new org.telegram.ui.SettingsActivity;	 Catch:{ Exception -> 0x07a5 }
        r30.<init>();	 Catch:{ Exception -> 0x07a5 }
        r0 = r40;
        r0 = r0.actionBarLayout;	 Catch:{ Exception -> 0x07a5 }
        r35 = r0;
        r0 = r35;
        r1 = r30;
        r0.addFragmentToStack(r1);	 Catch:{ Exception -> 0x07a5 }
        r0 = r30;
        r1 = r41;
        r0.restoreSelfArgs(r1);	 Catch:{ Exception -> 0x07a5 }
        goto L_0x061b;
    L_0x07c6:
        if (r7 == 0) goto L_0x061b;
    L_0x07c8:
        r18 = new org.telegram.ui.GroupCreateFinalActivity;	 Catch:{ Exception -> 0x07a5 }
        r0 = r18;
        r0.<init>(r7);	 Catch:{ Exception -> 0x07a5 }
        r0 = r40;
        r0 = r0.actionBarLayout;	 Catch:{ Exception -> 0x07a5 }
        r35 = r0;
        r0 = r35;
        r1 = r18;
        r35 = r0.addFragmentToStack(r1);	 Catch:{ Exception -> 0x07a5 }
        if (r35 == 0) goto L_0x061b;
    L_0x07df:
        r0 = r18;
        r1 = r41;
        r0.restoreSelfArgs(r1);	 Catch:{ Exception -> 0x07a5 }
        goto L_0x061b;
    L_0x07e8:
        if (r7 == 0) goto L_0x061b;
    L_0x07ea:
        r8 = new org.telegram.ui.ChannelCreateActivity;	 Catch:{ Exception -> 0x07a5 }
        r8.<init>(r7);	 Catch:{ Exception -> 0x07a5 }
        r0 = r40;
        r0 = r0.actionBarLayout;	 Catch:{ Exception -> 0x07a5 }
        r35 = r0;
        r0 = r35;
        r35 = r0.addFragmentToStack(r8);	 Catch:{ Exception -> 0x07a5 }
        if (r35 == 0) goto L_0x061b;
    L_0x07fd:
        r0 = r41;
        r8.restoreSelfArgs(r0);	 Catch:{ Exception -> 0x07a5 }
        goto L_0x061b;
    L_0x0804:
        if (r7 == 0) goto L_0x061b;
    L_0x0806:
        r8 = new org.telegram.ui.ChannelEditActivity;	 Catch:{ Exception -> 0x07a5 }
        r8.<init>(r7);	 Catch:{ Exception -> 0x07a5 }
        r0 = r40;
        r0 = r0.actionBarLayout;	 Catch:{ Exception -> 0x07a5 }
        r35 = r0;
        r0 = r35;
        r35 = r0.addFragmentToStack(r8);	 Catch:{ Exception -> 0x07a5 }
        if (r35 == 0) goto L_0x061b;
    L_0x0819:
        r0 = r41;
        r8.restoreSelfArgs(r0);	 Catch:{ Exception -> 0x07a5 }
        goto L_0x061b;
    L_0x0820:
        if (r7 == 0) goto L_0x061b;
    L_0x0822:
        r27 = new org.telegram.ui.ProfileActivity;	 Catch:{ Exception -> 0x07a5 }
        r0 = r27;
        r0.<init>(r7);	 Catch:{ Exception -> 0x07a5 }
        r0 = r40;
        r0 = r0.actionBarLayout;	 Catch:{ Exception -> 0x07a5 }
        r35 = r0;
        r0 = r35;
        r1 = r27;
        r35 = r0.addFragmentToStack(r1);	 Catch:{ Exception -> 0x07a5 }
        if (r35 == 0) goto L_0x061b;
    L_0x0839:
        r0 = r27;
        r1 = r41;
        r0.restoreSelfArgs(r1);	 Catch:{ Exception -> 0x07a5 }
        goto L_0x061b;
    L_0x0842:
        r30 = new org.telegram.ui.WallpapersActivity;	 Catch:{ Exception -> 0x07a5 }
        r30.<init>();	 Catch:{ Exception -> 0x07a5 }
        r0 = r40;
        r0 = r0.actionBarLayout;	 Catch:{ Exception -> 0x07a5 }
        r35 = r0;
        r0 = r35;
        r1 = r30;
        r0.addFragmentToStack(r1);	 Catch:{ Exception -> 0x07a5 }
        r0 = r30;
        r1 = r41;
        r0.restoreSelfArgs(r1);	 Catch:{ Exception -> 0x07a5 }
        goto L_0x061b;
    L_0x085d:
        r0 = r40;
        r0 = r0.actionBarLayout;
        r35 = r0;
        r0 = r35;
        r0 = r0.fragmentsStack;
        r35 = r0;
        r36 = 0;
        r15 = r35.get(r36);
        r15 = (org.telegram.ui.ActionBar.BaseFragment) r15;
        r0 = r15 instanceof org.telegram.ui.DialogsActivity;
        r35 = r0;
        if (r35 == 0) goto L_0x0884;
    L_0x0877:
        r15 = (org.telegram.ui.DialogsActivity) r15;
        r0 = r40;
        r0 = r0.sideMenu;
        r35 = r0;
        r0 = r35;
        r15.setSideMenu(r0);
    L_0x0884:
        r6 = 1;
        r35 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r35 == 0) goto L_0x08e9;
    L_0x088b:
        r0 = r40;
        r0 = r0.actionBarLayout;
        r35 = r0;
        r0 = r35;
        r0 = r0.fragmentsStack;
        r35 = r0;
        r35 = r35.size();
        r36 = 1;
        r0 = r35;
        r1 = r36;
        if (r0 > r1) goto L_0x092d;
    L_0x08a3:
        r0 = r40;
        r0 = r0.layersActionBarLayout;
        r35 = r0;
        r0 = r35;
        r0 = r0.fragmentsStack;
        r35 = r0;
        r35 = r35.isEmpty();
        if (r35 == 0) goto L_0x092d;
    L_0x08b5:
        r6 = 1;
    L_0x08b6:
        r0 = r40;
        r0 = r0.layersActionBarLayout;
        r35 = r0;
        r0 = r35;
        r0 = r0.fragmentsStack;
        r35 = r0;
        r35 = r35.size();
        r36 = 1;
        r0 = r35;
        r1 = r36;
        if (r0 != r1) goto L_0x08e9;
    L_0x08ce:
        r0 = r40;
        r0 = r0.layersActionBarLayout;
        r35 = r0;
        r0 = r35;
        r0 = r0.fragmentsStack;
        r35 = r0;
        r36 = 0;
        r35 = r35.get(r36);
        r0 = r35;
        r0 = r0 instanceof org.telegram.ui.LoginActivity;
        r35 = r0;
        if (r35 == 0) goto L_0x08e9;
    L_0x08e8:
        r6 = 0;
    L_0x08e9:
        r0 = r40;
        r0 = r0.actionBarLayout;
        r35 = r0;
        r0 = r35;
        r0 = r0.fragmentsStack;
        r35 = r0;
        r35 = r35.size();
        r36 = 1;
        r0 = r35;
        r1 = r36;
        if (r0 != r1) goto L_0x091c;
    L_0x0901:
        r0 = r40;
        r0 = r0.actionBarLayout;
        r35 = r0;
        r0 = r35;
        r0 = r0.fragmentsStack;
        r35 = r0;
        r36 = 0;
        r35 = r35.get(r36);
        r0 = r35;
        r0 = r0 instanceof org.telegram.ui.LoginActivity;
        r35 = r0;
        if (r35 == 0) goto L_0x091c;
    L_0x091b:
        r6 = 0;
    L_0x091c:
        r0 = r40;
        r0 = r0.drawerLayoutContainer;
        r35 = r0;
        r36 = 0;
        r0 = r35;
        r1 = r36;
        r0.setAllowOpenDrawer(r6, r1);
        goto L_0x061b;
    L_0x092d:
        r6 = 0;
        goto L_0x08b6;
    L_0x092f:
        r35 = 0;
        goto L_0x0628;
    L_0x0933:
        r24 = "";
        goto L_0x0641;
    L_0x0938:
        r25 = "";
        goto L_0x0647;
    L_0x093d:
        r14 = move-exception;
        org.telegram.messenger.FileLog.e(r14);
        goto L_0x0687;
    L_0x0943:
        r35 = move-exception;
        goto L_0x0173;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.LaunchActivity.onCreate(android.os.Bundle):void");
    }

    final /* synthetic */ boolean lambda$onCreate$0$LaunchActivity(View v, MotionEvent event) {
        if (this.actionBarLayout.fragmentsStack.isEmpty() || event.getAction() != 1) {
            return false;
        }
        float x = event.getX();
        float y = event.getY();
        int[] location = new int[2];
        this.layersActionBarLayout.getLocationOnScreen(location);
        int viewX = location[0];
        int viewY = location[1];
        if (this.layersActionBarLayout.checkTransitionAnimation() || (x > ((float) viewX) && x < ((float) (this.layersActionBarLayout.getWidth() + viewX)) && y > ((float) viewY) && y < ((float) (this.layersActionBarLayout.getHeight() + viewY)))) {
            return false;
        }
        if (!this.layersActionBarLayout.fragmentsStack.isEmpty()) {
            int a = 0;
            while (this.layersActionBarLayout.fragmentsStack.size() - 1 > 0) {
                this.layersActionBarLayout.removeFragmentFromStack((BaseFragment) this.layersActionBarLayout.fragmentsStack.get(0));
                a = (a - 1) + 1;
            }
            this.layersActionBarLayout.closeLastFragment(true);
        }
        return true;
    }

    static final /* synthetic */ void lambda$onCreate$1$LaunchActivity(View v) {
    }

    final /* synthetic */ void lambda$onCreate$2$LaunchActivity(View view, int position) {
        boolean z = false;
        if (position == 0) {
            DrawerLayoutAdapter drawerLayoutAdapter = this.drawerLayoutAdapter;
            if (!this.drawerLayoutAdapter.isAccountsShowed()) {
                z = true;
            }
            drawerLayoutAdapter.setAccountsShowed(z, true);
        } else if (view instanceof DrawerUserCell) {
            switchToAccount(((DrawerUserCell) view).getAccountNumber(), true);
            this.drawerLayoutContainer.closeDrawer(false);
        } else if (view instanceof DrawerAddCell) {
            int freeAccount = -1;
            for (int a = 0; a < 3; a++) {
                if (!UserConfig.getInstance(a).isClientActivated()) {
                    freeAccount = a;
                    break;
                }
            }
            if (freeAccount >= 0) {
                presentFragment(new LoginActivity(freeAccount));
            }
            this.drawerLayoutContainer.closeDrawer(false);
        } else {
            int id = this.drawerLayoutAdapter.getId(position);
            if (id == 2) {
                presentFragment(new GroupCreateActivity());
                this.drawerLayoutContainer.closeDrawer(false);
            } else if (id == 3) {
                args = new Bundle();
                args.putBoolean("onlyUsers", true);
                args.putBoolean("destroyAfterSelect", true);
                args.putBoolean("createSecretChat", true);
                args.putBoolean("allowBots", false);
                presentFragment(new ContactsActivity(args));
                this.drawerLayoutContainer.closeDrawer(false);
            } else if (id == 4) {
                SharedPreferences preferences = MessagesController.getGlobalMainSettings();
                if (BuildVars.DEBUG_VERSION || !preferences.getBoolean("channel_intro", false)) {
                    presentFragment(new ChannelIntroActivity());
                    preferences.edit().putBoolean("channel_intro", true).commit();
                } else {
                    args = new Bundle();
                    args.putInt("step", 0);
                    presentFragment(new ChannelCreateActivity(args));
                }
                this.drawerLayoutContainer.closeDrawer(false);
            } else if (id == 6) {
                presentFragment(new ContactsActivity(null));
                this.drawerLayoutContainer.closeDrawer(false);
            } else if (id == 7) {
                presentFragment(new InviteContactsActivity());
                this.drawerLayoutContainer.closeDrawer(false);
            } else if (id == 8) {
                presentFragment(new SettingsActivity());
                this.drawerLayoutContainer.closeDrawer(false);
            } else if (id == 9) {
                Browser.openUrl((Context) this, LocaleController.getString("TelegramFaqUrl", R.string.TelegramFaqUrl));
                this.drawerLayoutContainer.closeDrawer(false);
            } else if (id == 10) {
                presentFragment(new CallLogActivity());
                this.drawerLayoutContainer.closeDrawer(false);
            } else if (id == 11) {
                args = new Bundle();
                args.putInt("user_id", UserConfig.getInstance(this.currentAccount).getClientUserId());
                presentFragment(new ChatActivity(args));
                this.drawerLayoutContainer.closeDrawer(false);
            }
        }
    }

    static final /* synthetic */ void lambda$onCreate$3$LaunchActivity(View view) {
        int height = view.getMeasuredHeight();
        if (VERSION.SDK_INT >= 21) {
            height -= AndroidUtilities.statusBarHeight;
        }
        if (height > AndroidUtilities.dp(100.0f) && height < AndroidUtilities.displaySize.y && AndroidUtilities.dp(100.0f) + height > AndroidUtilities.displaySize.y) {
            AndroidUtilities.displaySize.y = height;
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("fix display size y to " + AndroidUtilities.displaySize.y);
            }
        }
    }

    public void switchToAccount(int account, boolean removeAll) {
        if (account != UserConfig.selectedAccount) {
            ConnectionsManager.getInstance(this.currentAccount).setAppPaused(true, false);
            UserConfig.selectedAccount = account;
            UserConfig.getInstance(0).saveConfig(false);
            checkCurrentAccount();
            if (AndroidUtilities.isTablet()) {
                this.layersActionBarLayout.removeAllFragments();
                this.rightActionBarLayout.removeAllFragments();
                if (!this.tabletFullSize) {
                    this.shadowTabletSide.setVisibility(0);
                    if (this.rightActionBarLayout.fragmentsStack.isEmpty()) {
                        this.backgroundTablet.setVisibility(0);
                    }
                }
            }
            if (removeAll) {
                this.actionBarLayout.removeAllFragments();
            } else {
                this.actionBarLayout.removeFragmentFromStack(0);
            }
            DialogsActivity dialogsActivity = new DialogsActivity(null);
            dialogsActivity.setSideMenu(this.sideMenu);
            this.actionBarLayout.addFragmentToStack(dialogsActivity, 0);
            this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
            this.actionBarLayout.showLastFragment();
            if (AndroidUtilities.isTablet()) {
                this.layersActionBarLayout.showLastFragment();
                this.rightActionBarLayout.showLastFragment();
            }
            if (!ApplicationLoader.mainInterfacePaused) {
                ConnectionsManager.getInstance(this.currentAccount).setAppPaused(false, false);
            }
            if (UserConfig.getInstance(account).unacceptedTermsOfService != null) {
                showTosActivity(account, UserConfig.getInstance(account).unacceptedTermsOfService);
            }
        }
    }

    private void switchToAvailableAccountOrLogout() {
        int account = -1;
        for (int a = 0; a < 3; a++) {
            if (UserConfig.getInstance(a).isClientActivated()) {
                account = a;
                break;
            }
        }
        if (this.termsOfServiceView != null) {
            this.termsOfServiceView.setVisibility(8);
        }
        if (account != -1) {
            switchToAccount(account, true);
            return;
        }
        if (this.drawerLayoutAdapter != null) {
            this.drawerLayoutAdapter.notifyDataSetChanged();
        }
        Iterator it = this.actionBarLayout.fragmentsStack.iterator();
        while (it.hasNext()) {
            ((BaseFragment) it.next()).onFragmentDestroy();
        }
        this.actionBarLayout.fragmentsStack.clear();
        if (AndroidUtilities.isTablet()) {
            it = this.layersActionBarLayout.fragmentsStack.iterator();
            while (it.hasNext()) {
                ((BaseFragment) it.next()).onFragmentDestroy();
            }
            this.layersActionBarLayout.fragmentsStack.clear();
            it = this.rightActionBarLayout.fragmentsStack.iterator();
            while (it.hasNext()) {
                ((BaseFragment) it.next()).onFragmentDestroy();
            }
            this.rightActionBarLayout.fragmentsStack.clear();
        }
        startActivity(new Intent(this, IntroActivity.class));
        onFinish();
        finish();
    }

    public int getMainFragmentsCount() {
        return mainFragmentsStack.size();
    }

    private void checkCurrentAccount() {
        if (this.currentAccount != UserConfig.selectedAccount) {
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.appDidLogout);
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.mainUserInfoChanged);
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.didUpdatedConnectionState);
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.needShowAlert);
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.wasUnableToFindCurrentLocation);
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.openArticle);
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.hasNewContactsToImport);
        }
        this.currentAccount = UserConfig.selectedAccount;
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.appDidLogout);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.mainUserInfoChanged);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.didUpdatedConnectionState);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.needShowAlert);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.wasUnableToFindCurrentLocation);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.openArticle);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.hasNewContactsToImport);
        updateCurrentConnectionState(this.currentAccount);
    }

    private void checkLayout() {
        int i = 0;
        int i2 = 8;
        if (AndroidUtilities.isTablet() && this.rightActionBarLayout != null) {
            int a;
            BaseFragment chatFragment;
            if (AndroidUtilities.isInMultiwindow || (AndroidUtilities.isSmallTablet() && getResources().getConfiguration().orientation != 2)) {
                this.tabletFullSize = true;
                if (!this.rightActionBarLayout.fragmentsStack.isEmpty()) {
                    a = 0;
                    while (this.rightActionBarLayout.fragmentsStack.size() > 0) {
                        chatFragment = (BaseFragment) this.rightActionBarLayout.fragmentsStack.get(a);
                        if (chatFragment instanceof ChatActivity) {
                            ((ChatActivity) chatFragment).setIgnoreAttachOnPause(true);
                        }
                        chatFragment.onPause();
                        this.rightActionBarLayout.fragmentsStack.remove(a);
                        this.actionBarLayout.fragmentsStack.add(chatFragment);
                        a = (a - 1) + 1;
                    }
                    if (this.passcodeView.getVisibility() != 0) {
                        this.actionBarLayout.showLastFragment();
                    }
                }
                this.shadowTabletSide.setVisibility(8);
                this.rightActionBarLayout.setVisibility(8);
                View view = this.backgroundTablet;
                if (this.actionBarLayout.fragmentsStack.isEmpty()) {
                    i2 = 0;
                }
                view.setVisibility(i2);
                return;
            }
            int i3;
            this.tabletFullSize = false;
            if (this.actionBarLayout.fragmentsStack.size() >= 2) {
                for (a = 1; a < this.actionBarLayout.fragmentsStack.size(); a = (a - 1) + 1) {
                    chatFragment = (BaseFragment) this.actionBarLayout.fragmentsStack.get(a);
                    if (chatFragment instanceof ChatActivity) {
                        ((ChatActivity) chatFragment).setIgnoreAttachOnPause(true);
                    }
                    chatFragment.onPause();
                    this.actionBarLayout.fragmentsStack.remove(a);
                    this.rightActionBarLayout.fragmentsStack.add(chatFragment);
                }
                if (this.passcodeView.getVisibility() != 0) {
                    this.actionBarLayout.showLastFragment();
                    this.rightActionBarLayout.showLastFragment();
                }
            }
            ActionBarLayout actionBarLayout = this.rightActionBarLayout;
            if (this.rightActionBarLayout.fragmentsStack.isEmpty()) {
                i3 = 8;
            } else {
                i3 = 0;
            }
            actionBarLayout.setVisibility(i3);
            View view2 = this.backgroundTablet;
            if (this.rightActionBarLayout.fragmentsStack.isEmpty()) {
                i3 = 0;
            } else {
                i3 = 8;
            }
            view2.setVisibility(i3);
            FrameLayout frameLayout = this.shadowTabletSide;
            if (this.actionBarLayout.fragmentsStack.isEmpty()) {
                i = 8;
            }
            frameLayout.setVisibility(i);
        }
    }

    private void showUpdateActivity(int account, TL_help_appUpdate update) {
        if (this.blockingUpdateView == null) {
            this.blockingUpdateView = new BlockingUpdateView(this) {
                public void setVisibility(int visibility) {
                    super.setVisibility(visibility);
                    if (visibility == 8) {
                        LaunchActivity.this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
                    }
                }
            };
            this.drawerLayoutContainer.addView(this.blockingUpdateView, LayoutHelper.createFrame(-1, -1.0f));
        }
        this.blockingUpdateView.show(account, update);
        this.drawerLayoutContainer.setAllowOpenDrawer(false, false);
    }

    private void showTosActivity(int account, TL_help_termsOfService tos) {
        if (this.termsOfServiceView == null) {
            this.termsOfServiceView = new TermsOfServiceView(this);
            this.drawerLayoutContainer.addView(this.termsOfServiceView, LayoutHelper.createFrame(-1, -1.0f));
            this.termsOfServiceView.setDelegate(new TermsOfServiceViewDelegate() {
                public void onAcceptTerms(int account) {
                    UserConfig.getInstance(account).unacceptedTermsOfService = null;
                    UserConfig.getInstance(account).saveConfig(false);
                    LaunchActivity.this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
                    LaunchActivity.this.termsOfServiceView.setVisibility(8);
                }

                public void onDeclineTerms(int account) {
                    LaunchActivity.this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
                    LaunchActivity.this.termsOfServiceView.setVisibility(8);
                }
            });
        }
        TL_help_termsOfService currentTos = UserConfig.getInstance(account).unacceptedTermsOfService;
        if (currentTos != tos && (currentTos == null || !currentTos.id.data.equals(tos.id.data))) {
            UserConfig.getInstance(account).unacceptedTermsOfService = tos;
            UserConfig.getInstance(account).saveConfig(false);
        }
        this.termsOfServiceView.show(account, tos);
        this.drawerLayoutContainer.setAllowOpenDrawer(false, false);
    }

    private void showPasscodeActivity() {
        if (this.passcodeView != null) {
            SharedConfig.appLocked = true;
            if (SecretMediaViewer.hasInstance() && SecretMediaViewer.getInstance().isVisible()) {
                SecretMediaViewer.getInstance().closePhoto(false, false);
            } else if (PhotoViewer.hasInstance() && PhotoViewer.getInstance().isVisible()) {
                PhotoViewer.getInstance().closePhoto(false, true);
            } else if (ArticleViewer.hasInstance() && ArticleViewer.getInstance().isVisible()) {
                ArticleViewer.getInstance().close(false, true);
            }
            this.passcodeView.onShow();
            SharedConfig.isWaitingForPasscodeEnter = true;
            this.drawerLayoutContainer.setAllowOpenDrawer(false, false);
            this.passcodeView.setDelegate(new LaunchActivity$$Lambda$4(this));
        }
    }

    final /* synthetic */ void lambda$showPasscodeActivity$4$LaunchActivity() {
        SharedConfig.isWaitingForPasscodeEnter = false;
        if (this.passcodeSaveIntent != null) {
            handleIntent(this.passcodeSaveIntent, this.passcodeSaveIntentIsNew, this.passcodeSaveIntentIsRestore, true);
            this.passcodeSaveIntent = null;
        }
        this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
        this.actionBarLayout.showLastFragment();
        if (AndroidUtilities.isTablet()) {
            this.layersActionBarLayout.showLastFragment();
            this.rightActionBarLayout.showLastFragment();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean handleIntent(android.content.Intent r75, boolean r76, boolean r77, boolean r78) {
        /*
        r74 = this;
        r4 = org.telegram.messenger.AndroidUtilities.handleProxyIntent(r74, r75);
        if (r4 == 0) goto L_0x0024;
    L_0x0006:
        r0 = r74;
        r4 = r0.actionBarLayout;
        r4.showLastFragment();
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 == 0) goto L_0x0021;
    L_0x0013:
        r0 = r74;
        r4 = r0.layersActionBarLayout;
        r4.showLastFragment();
        r0 = r74;
        r4 = r0.rightActionBarLayout;
        r4.showLastFragment();
    L_0x0021:
        r54 = 1;
    L_0x0023:
        return r54;
    L_0x0024:
        r4 = org.telegram.ui.PhotoViewer.hasInstance();
        if (r4 == 0) goto L_0x004f;
    L_0x002a:
        r4 = org.telegram.ui.PhotoViewer.getInstance();
        r4 = r4.isVisible();
        if (r4 == 0) goto L_0x004f;
    L_0x0034:
        if (r75 == 0) goto L_0x0043;
    L_0x0036:
        r4 = "android.intent.action.MAIN";
        r5 = r75.getAction();
        r4 = r4.equals(r5);
        if (r4 != 0) goto L_0x004f;
    L_0x0043:
        r4 = org.telegram.ui.PhotoViewer.getInstance();
        r5 = 0;
        r18 = 1;
        r0 = r18;
        r4.closePhoto(r5, r0);
    L_0x004f:
        r41 = r75.getFlags();
        r4 = 1;
        r0 = new int[r4];
        r45 = r0;
        r4 = 0;
        r5 = "currentAccount";
        r18 = org.telegram.messenger.UserConfig.selectedAccount;
        r0 = r75;
        r1 = r18;
        r5 = r0.getIntExtra(r5, r1);
        r45[r4] = r5;
        r4 = 0;
        r4 = r45[r4];
        r5 = 1;
        r0 = r74;
        r0.switchToAccount(r4, r5);
        if (r78 != 0) goto L_0x00a2;
    L_0x0073:
        r4 = 1;
        r4 = org.telegram.messenger.AndroidUtilities.needShowPasscode(r4);
        if (r4 != 0) goto L_0x007e;
    L_0x007a:
        r4 = org.telegram.messenger.SharedConfig.isWaitingForPasscodeEnter;
        if (r4 == 0) goto L_0x00a2;
    L_0x007e:
        r74.showPasscodeActivity();
        r0 = r75;
        r1 = r74;
        r1.passcodeSaveIntent = r0;
        r0 = r76;
        r1 = r74;
        r1.passcodeSaveIntentIsNew = r0;
        r0 = r77;
        r1 = r74;
        r1.passcodeSaveIntentIsRestore = r0;
        r0 = r74;
        r4 = r0.currentAccount;
        r4 = org.telegram.messenger.UserConfig.getInstance(r4);
        r5 = 0;
        r4.saveConfig(r5);
        r54 = 0;
        goto L_0x0023;
    L_0x00a2:
        r54 = 0;
        r4 = 0;
        r58 = java.lang.Integer.valueOf(r4);
        r4 = 0;
        r55 = java.lang.Integer.valueOf(r4);
        r4 = 0;
        r56 = java.lang.Integer.valueOf(r4);
        r4 = 0;
        r57 = java.lang.Integer.valueOf(r4);
        r4 = 0;
        r48 = java.lang.Integer.valueOf(r4);
        r4 = 0;
        r47 = java.lang.Integer.valueOf(r4);
        r34 = 0;
        r4 = org.telegram.messenger.SharedConfig.directShare;
        if (r4 == 0) goto L_0x00df;
    L_0x00c8:
        if (r75 == 0) goto L_0x025b;
    L_0x00ca:
        r4 = r75.getExtras();
        if (r4 == 0) goto L_0x025b;
    L_0x00d0:
        r4 = r75.getExtras();
        r5 = "dialogId";
        r22 = 0;
        r0 = r22;
        r34 = r4.getLong(r5, r0);
    L_0x00df:
        r62 = 0;
        r64 = 0;
        r63 = 0;
        r4 = 0;
        r0 = r74;
        r0.photoPathsArray = r4;
        r4 = 0;
        r0 = r74;
        r0.videoPath = r4;
        r4 = 0;
        r0 = r74;
        r0.sendingText = r4;
        r4 = 0;
        r0 = r74;
        r0.documentsPathsArray = r4;
        r4 = 0;
        r0 = r74;
        r0.documentsOriginalPathsArray = r4;
        r4 = 0;
        r0 = r74;
        r0.documentsMimeType = r4;
        r4 = 0;
        r0 = r74;
        r0.documentsUrisArray = r4;
        r4 = 0;
        r0 = r74;
        r0.contactsToSend = r4;
        r4 = 0;
        r0 = r74;
        r0.contactsToSendUri = r4;
        r0 = r74;
        r4 = r0.currentAccount;
        r4 = org.telegram.messenger.UserConfig.getInstance(r4);
        r4 = r4.isClientActivated();
        if (r4 == 0) goto L_0x018b;
    L_0x0120:
        r4 = 1048576; // 0x100000 float:1.469368E-39 double:5.180654E-318;
        r4 = r4 & r41;
        if (r4 != 0) goto L_0x018b;
    L_0x0126:
        if (r75 == 0) goto L_0x018b;
    L_0x0128:
        r4 = r75.getAction();
        if (r4 == 0) goto L_0x018b;
    L_0x012e:
        if (r77 != 0) goto L_0x018b;
    L_0x0130:
        r4 = "android.intent.action.SEND";
        r5 = r75.getAction();
        r4 = r4.equals(r5);
        if (r4 == 0) goto L_0x03dd;
    L_0x013d:
        r40 = 0;
        r68 = r75.getType();
        if (r68 == 0) goto L_0x026b;
    L_0x0145:
        r4 = "text/x-vcard";
        r0 = r68;
        r4 = r0.equals(r4);
        if (r4 == 0) goto L_0x026b;
    L_0x0150:
        r4 = r75.getExtras();	 Catch:{ Exception -> 0x0263 }
        r5 = "android.intent.extra.STREAM";
        r69 = r4.get(r5);	 Catch:{ Exception -> 0x0263 }
        r69 = (android.net.Uri) r69;	 Catch:{ Exception -> 0x0263 }
        if (r69 == 0) goto L_0x025f;
    L_0x015f:
        r0 = r74;
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0263 }
        r5 = 0;
        r18 = 0;
        r21 = 0;
        r0 = r69;
        r1 = r18;
        r2 = r21;
        r4 = org.telegram.messenger.AndroidUtilities.loadVCardFromStream(r0, r4, r5, r1, r2);	 Catch:{ Exception -> 0x0263 }
        r0 = r74;
        r0.contactsToSend = r4;	 Catch:{ Exception -> 0x0263 }
        r0 = r69;
        r1 = r74;
        r1.contactsToSendUri = r0;	 Catch:{ Exception -> 0x0263 }
    L_0x017c:
        if (r40 == 0) goto L_0x018b;
    L_0x017e:
        r4 = "Unsupported content";
        r5 = 0;
        r0 = r74;
        r4 = android.widget.Toast.makeText(r0, r4, r5);
        r4.show();
    L_0x018b:
        r4 = r58.intValue();
        if (r4 == 0) goto L_0x0d1d;
    L_0x0191:
        r29 = new android.os.Bundle;
        r29.<init>();
        r4 = "user_id";
        r5 = r58.intValue();
        r0 = r29;
        r0.putInt(r4, r5);
        r4 = r57.intValue();
        if (r4 == 0) goto L_0x01b4;
    L_0x01a8:
        r4 = "message_id";
        r5 = r57.intValue();
        r0 = r29;
        r0.putInt(r4, r5);
    L_0x01b4:
        r4 = mainFragmentsStack;
        r4 = r4.isEmpty();
        if (r4 != 0) goto L_0x01dd;
    L_0x01bc:
        r4 = 0;
        r4 = r45[r4];
        r5 = org.telegram.messenger.MessagesController.getInstance(r4);
        r4 = mainFragmentsStack;
        r18 = mainFragmentsStack;
        r18 = r18.size();
        r18 = r18 + -1;
        r0 = r18;
        r4 = r4.get(r0);
        r4 = (org.telegram.ui.ActionBar.BaseFragment) r4;
        r0 = r29;
        r4 = r5.checkCanOpenChat(r0, r4);
        if (r4 == 0) goto L_0x01fc;
    L_0x01dd:
        r19 = new org.telegram.ui.ChatActivity;
        r0 = r19;
        r1 = r29;
        r0.<init>(r1);
        r0 = r74;
        r0 = r0.actionBarLayout;
        r18 = r0;
        r20 = 0;
        r21 = 1;
        r22 = 1;
        r23 = 0;
        r4 = r18.presentFragment(r19, r20, r21, r22, r23);
        if (r4 == 0) goto L_0x01fc;
    L_0x01fa:
        r54 = 1;
    L_0x01fc:
        if (r54 != 0) goto L_0x0253;
    L_0x01fe:
        if (r76 != 0) goto L_0x0253;
    L_0x0200:
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 == 0) goto L_0x114b;
    L_0x0206:
        r0 = r74;
        r4 = r0.currentAccount;
        r4 = org.telegram.messenger.UserConfig.getInstance(r4);
        r4 = r4.isClientActivated();
        if (r4 != 0) goto L_0x1117;
    L_0x0214:
        r0 = r74;
        r4 = r0.layersActionBarLayout;
        r4 = r4.fragmentsStack;
        r4 = r4.isEmpty();
        if (r4 == 0) goto L_0x0238;
    L_0x0220:
        r0 = r74;
        r4 = r0.layersActionBarLayout;
        r5 = new org.telegram.ui.LoginActivity;
        r5.<init>();
        r4.addFragmentToStack(r5);
        r0 = r74;
        r4 = r0.drawerLayoutContainer;
        r5 = 0;
        r18 = 0;
        r0 = r18;
        r4.setAllowOpenDrawer(r5, r0);
    L_0x0238:
        r0 = r74;
        r4 = r0.actionBarLayout;
        r4.showLastFragment();
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 == 0) goto L_0x0253;
    L_0x0245:
        r0 = r74;
        r4 = r0.layersActionBarLayout;
        r4.showLastFragment();
        r0 = r74;
        r4 = r0.rightActionBarLayout;
        r4.showLastFragment();
    L_0x0253:
        r4 = 0;
        r0 = r75;
        r0.setAction(r4);
        goto L_0x0023;
    L_0x025b:
        r34 = 0;
        goto L_0x00df;
    L_0x025f:
        r40 = 1;
        goto L_0x017c;
    L_0x0263:
        r38 = move-exception;
        org.telegram.messenger.FileLog.e(r38);
        r40 = 1;
        goto L_0x017c;
    L_0x026b:
        r4 = "android.intent.extra.TEXT";
        r0 = r75;
        r66 = r0.getStringExtra(r4);
        if (r66 != 0) goto L_0x0285;
    L_0x0276:
        r4 = "android.intent.extra.TEXT";
        r0 = r75;
        r67 = r0.getCharSequenceExtra(r4);
        if (r67 == 0) goto L_0x0285;
    L_0x0281:
        r66 = r67.toString();
    L_0x0285:
        r4 = "android.intent.extra.SUBJECT";
        r0 = r75;
        r65 = r0.getStringExtra(r4);
        if (r66 == 0) goto L_0x0344;
    L_0x0290:
        r4 = r66.length();
        if (r4 == 0) goto L_0x0344;
    L_0x0296:
        r4 = "http://";
        r0 = r66;
        r4 = r0.startsWith(r4);
        if (r4 != 0) goto L_0x02ac;
    L_0x02a1:
        r4 = "https://";
        r0 = r66;
        r4 = r0.startsWith(r4);
        if (r4 == 0) goto L_0x02d0;
    L_0x02ac:
        if (r65 == 0) goto L_0x02d0;
    L_0x02ae:
        r4 = r65.length();
        if (r4 == 0) goto L_0x02d0;
    L_0x02b4:
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r0 = r65;
        r4 = r4.append(r0);
        r5 = "\n";
        r4 = r4.append(r5);
        r0 = r66;
        r4 = r4.append(r0);
        r66 = r4.toString();
    L_0x02d0:
        r0 = r66;
        r1 = r74;
        r1.sendingText = r0;
    L_0x02d6:
        r4 = "android.intent.extra.STREAM";
        r0 = r75;
        r50 = r0.getParcelableExtra(r4);
        if (r50 == 0) goto L_0x03d3;
    L_0x02e1:
        r0 = r50;
        r4 = r0 instanceof android.net.Uri;
        if (r4 != 0) goto L_0x02ef;
    L_0x02e7:
        r4 = r50.toString();
        r50 = android.net.Uri.parse(r4);
    L_0x02ef:
        r69 = r50;
        r69 = (android.net.Uri) r69;
        if (r69 == 0) goto L_0x02fd;
    L_0x02f5:
        r4 = org.telegram.messenger.AndroidUtilities.isInternalUri(r69);
        if (r4 == 0) goto L_0x02fd;
    L_0x02fb:
        r40 = 1;
    L_0x02fd:
        if (r40 != 0) goto L_0x017c;
    L_0x02ff:
        if (r69 == 0) goto L_0x0353;
    L_0x0301:
        if (r68 == 0) goto L_0x030e;
    L_0x0303:
        r4 = "image/";
        r0 = r68;
        r4 = r0.startsWith(r4);
        if (r4 != 0) goto L_0x031f;
    L_0x030e:
        r4 = r69.toString();
        r4 = r4.toLowerCase();
        r5 = ".jpg";
        r4 = r4.endsWith(r5);
        if (r4 == 0) goto L_0x0353;
    L_0x031f:
        r0 = r74;
        r4 = r0.photoPathsArray;
        if (r4 != 0) goto L_0x032e;
    L_0x0325:
        r4 = new java.util.ArrayList;
        r4.<init>();
        r0 = r74;
        r0.photoPathsArray = r4;
    L_0x032e:
        r44 = new org.telegram.messenger.SendMessagesHelper$SendingMediaInfo;
        r44.<init>();
        r0 = r69;
        r1 = r44;
        r1.uri = r0;
        r0 = r74;
        r4 = r0.photoPathsArray;
        r0 = r44;
        r4.add(r0);
        goto L_0x017c;
    L_0x0344:
        if (r65 == 0) goto L_0x02d6;
    L_0x0346:
        r4 = r65.length();
        if (r4 <= 0) goto L_0x02d6;
    L_0x034c:
        r0 = r65;
        r1 = r74;
        r1.sendingText = r0;
        goto L_0x02d6;
    L_0x0353:
        r51 = org.telegram.messenger.AndroidUtilities.getPath(r69);
        if (r51 == 0) goto L_0x03b3;
    L_0x0359:
        r4 = "file:";
        r0 = r51;
        r4 = r0.startsWith(r4);
        if (r4 == 0) goto L_0x0370;
    L_0x0364:
        r4 = "file://";
        r5 = "";
        r0 = r51;
        r51 = r0.replace(r4, r5);
    L_0x0370:
        if (r68 == 0) goto L_0x0385;
    L_0x0372:
        r4 = "video/";
        r0 = r68;
        r4 = r0.startsWith(r4);
        if (r4 == 0) goto L_0x0385;
    L_0x037d:
        r0 = r51;
        r1 = r74;
        r1.videoPath = r0;
        goto L_0x017c;
    L_0x0385:
        r0 = r74;
        r4 = r0.documentsPathsArray;
        if (r4 != 0) goto L_0x039d;
    L_0x038b:
        r4 = new java.util.ArrayList;
        r4.<init>();
        r0 = r74;
        r0.documentsPathsArray = r4;
        r4 = new java.util.ArrayList;
        r4.<init>();
        r0 = r74;
        r0.documentsOriginalPathsArray = r4;
    L_0x039d:
        r0 = r74;
        r4 = r0.documentsPathsArray;
        r0 = r51;
        r4.add(r0);
        r0 = r74;
        r4 = r0.documentsOriginalPathsArray;
        r5 = r69.toString();
        r4.add(r5);
        goto L_0x017c;
    L_0x03b3:
        r0 = r74;
        r4 = r0.documentsUrisArray;
        if (r4 != 0) goto L_0x03c2;
    L_0x03b9:
        r4 = new java.util.ArrayList;
        r4.<init>();
        r0 = r74;
        r0.documentsUrisArray = r4;
    L_0x03c2:
        r0 = r74;
        r4 = r0.documentsUrisArray;
        r0 = r69;
        r4.add(r0);
        r0 = r68;
        r1 = r74;
        r1.documentsMimeType = r0;
        goto L_0x017c;
    L_0x03d3:
        r0 = r74;
        r4 = r0.sendingText;
        if (r4 != 0) goto L_0x017c;
    L_0x03d9:
        r40 = 1;
        goto L_0x017c;
    L_0x03dd:
        r4 = "android.intent.action.SEND_MULTIPLE";
        r5 = r75.getAction();
        r4 = r4.equals(r5);
        if (r4 == 0) goto L_0x054f;
    L_0x03ea:
        r40 = 0;
        r4 = "android.intent.extra.STREAM";
        r0 = r75;
        r70 = r0.getParcelableArrayListExtra(r4);	 Catch:{ Exception -> 0x0535 }
        r68 = r75.getType();	 Catch:{ Exception -> 0x0535 }
        if (r70 == 0) goto L_0x043f;
    L_0x03fb:
        r27 = 0;
    L_0x03fd:
        r4 = r70.size();	 Catch:{ Exception -> 0x0535 }
        r0 = r27;
        if (r0 >= r4) goto L_0x0437;
    L_0x0405:
        r0 = r70;
        r1 = r27;
        r50 = r0.get(r1);	 Catch:{ Exception -> 0x0535 }
        r50 = (android.os.Parcelable) r50;	 Catch:{ Exception -> 0x0535 }
        r0 = r50;
        r4 = r0 instanceof android.net.Uri;	 Catch:{ Exception -> 0x0535 }
        if (r4 != 0) goto L_0x041d;
    L_0x0415:
        r4 = r50.toString();	 Catch:{ Exception -> 0x0535 }
        r50 = android.net.Uri.parse(r4);	 Catch:{ Exception -> 0x0535 }
    L_0x041d:
        r0 = r50;
        r0 = (android.net.Uri) r0;	 Catch:{ Exception -> 0x0535 }
        r69 = r0;
        if (r69 == 0) goto L_0x0434;
    L_0x0425:
        r4 = org.telegram.messenger.AndroidUtilities.isInternalUri(r69);	 Catch:{ Exception -> 0x0535 }
        if (r4 == 0) goto L_0x0434;
    L_0x042b:
        r0 = r70;
        r1 = r27;
        r0.remove(r1);	 Catch:{ Exception -> 0x0535 }
        r27 = r27 + -1;
    L_0x0434:
        r27 = r27 + 1;
        goto L_0x03fd;
    L_0x0437:
        r4 = r70.isEmpty();	 Catch:{ Exception -> 0x0535 }
        if (r4 == 0) goto L_0x043f;
    L_0x043d:
        r70 = 0;
    L_0x043f:
        if (r70 == 0) goto L_0x054c;
    L_0x0441:
        if (r68 == 0) goto L_0x049c;
    L_0x0443:
        r4 = "image/";
        r0 = r68;
        r4 = r0.startsWith(r4);	 Catch:{ Exception -> 0x0535 }
        if (r4 == 0) goto L_0x049c;
    L_0x044e:
        r27 = 0;
    L_0x0450:
        r4 = r70.size();	 Catch:{ Exception -> 0x0535 }
        r0 = r27;
        if (r0 >= r4) goto L_0x053b;
    L_0x0458:
        r0 = r70;
        r1 = r27;
        r50 = r0.get(r1);	 Catch:{ Exception -> 0x0535 }
        r50 = (android.os.Parcelable) r50;	 Catch:{ Exception -> 0x0535 }
        r0 = r50;
        r4 = r0 instanceof android.net.Uri;	 Catch:{ Exception -> 0x0535 }
        if (r4 != 0) goto L_0x0470;
    L_0x0468:
        r4 = r50.toString();	 Catch:{ Exception -> 0x0535 }
        r50 = android.net.Uri.parse(r4);	 Catch:{ Exception -> 0x0535 }
    L_0x0470:
        r0 = r50;
        r0 = (android.net.Uri) r0;	 Catch:{ Exception -> 0x0535 }
        r69 = r0;
        r0 = r74;
        r4 = r0.photoPathsArray;	 Catch:{ Exception -> 0x0535 }
        if (r4 != 0) goto L_0x0485;
    L_0x047c:
        r4 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0535 }
        r4.<init>();	 Catch:{ Exception -> 0x0535 }
        r0 = r74;
        r0.photoPathsArray = r4;	 Catch:{ Exception -> 0x0535 }
    L_0x0485:
        r44 = new org.telegram.messenger.SendMessagesHelper$SendingMediaInfo;	 Catch:{ Exception -> 0x0535 }
        r44.<init>();	 Catch:{ Exception -> 0x0535 }
        r0 = r69;
        r1 = r44;
        r1.uri = r0;	 Catch:{ Exception -> 0x0535 }
        r0 = r74;
        r4 = r0.photoPathsArray;	 Catch:{ Exception -> 0x0535 }
        r0 = r44;
        r4.add(r0);	 Catch:{ Exception -> 0x0535 }
        r27 = r27 + 1;
        goto L_0x0450;
    L_0x049c:
        r27 = 0;
    L_0x049e:
        r4 = r70.size();	 Catch:{ Exception -> 0x0535 }
        r0 = r27;
        if (r0 >= r4) goto L_0x053b;
    L_0x04a6:
        r0 = r70;
        r1 = r27;
        r50 = r0.get(r1);	 Catch:{ Exception -> 0x0535 }
        r50 = (android.os.Parcelable) r50;	 Catch:{ Exception -> 0x0535 }
        r0 = r50;
        r4 = r0 instanceof android.net.Uri;	 Catch:{ Exception -> 0x0535 }
        if (r4 != 0) goto L_0x04be;
    L_0x04b6:
        r4 = r50.toString();	 Catch:{ Exception -> 0x0535 }
        r50 = android.net.Uri.parse(r4);	 Catch:{ Exception -> 0x0535 }
    L_0x04be:
        r0 = r50;
        r0 = (android.net.Uri) r0;	 Catch:{ Exception -> 0x0535 }
        r69 = r0;
        r51 = org.telegram.messenger.AndroidUtilities.getPath(r69);	 Catch:{ Exception -> 0x0535 }
        r49 = r50.toString();	 Catch:{ Exception -> 0x0535 }
        if (r49 != 0) goto L_0x04d0;
    L_0x04ce:
        r49 = r51;
    L_0x04d0:
        if (r51 == 0) goto L_0x0516;
    L_0x04d2:
        r4 = "file:";
        r0 = r51;
        r4 = r0.startsWith(r4);	 Catch:{ Exception -> 0x0535 }
        if (r4 == 0) goto L_0x04e9;
    L_0x04dd:
        r4 = "file://";
        r5 = "";
        r0 = r51;
        r51 = r0.replace(r4, r5);	 Catch:{ Exception -> 0x0535 }
    L_0x04e9:
        r0 = r74;
        r4 = r0.documentsPathsArray;	 Catch:{ Exception -> 0x0535 }
        if (r4 != 0) goto L_0x0501;
    L_0x04ef:
        r4 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0535 }
        r4.<init>();	 Catch:{ Exception -> 0x0535 }
        r0 = r74;
        r0.documentsPathsArray = r4;	 Catch:{ Exception -> 0x0535 }
        r4 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0535 }
        r4.<init>();	 Catch:{ Exception -> 0x0535 }
        r0 = r74;
        r0.documentsOriginalPathsArray = r4;	 Catch:{ Exception -> 0x0535 }
    L_0x0501:
        r0 = r74;
        r4 = r0.documentsPathsArray;	 Catch:{ Exception -> 0x0535 }
        r0 = r51;
        r4.add(r0);	 Catch:{ Exception -> 0x0535 }
        r0 = r74;
        r4 = r0.documentsOriginalPathsArray;	 Catch:{ Exception -> 0x0535 }
        r0 = r49;
        r4.add(r0);	 Catch:{ Exception -> 0x0535 }
    L_0x0513:
        r27 = r27 + 1;
        goto L_0x049e;
    L_0x0516:
        r0 = r74;
        r4 = r0.documentsUrisArray;	 Catch:{ Exception -> 0x0535 }
        if (r4 != 0) goto L_0x0525;
    L_0x051c:
        r4 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0535 }
        r4.<init>();	 Catch:{ Exception -> 0x0535 }
        r0 = r74;
        r0.documentsUrisArray = r4;	 Catch:{ Exception -> 0x0535 }
    L_0x0525:
        r0 = r74;
        r4 = r0.documentsUrisArray;	 Catch:{ Exception -> 0x0535 }
        r0 = r69;
        r4.add(r0);	 Catch:{ Exception -> 0x0535 }
        r0 = r68;
        r1 = r74;
        r1.documentsMimeType = r0;	 Catch:{ Exception -> 0x0535 }
        goto L_0x0513;
    L_0x0535:
        r38 = move-exception;
        org.telegram.messenger.FileLog.e(r38);
        r40 = 1;
    L_0x053b:
        if (r40 == 0) goto L_0x018b;
    L_0x053d:
        r4 = "Unsupported content";
        r5 = 0;
        r0 = r74;
        r4 = android.widget.Toast.makeText(r0, r4, r5);
        r4.show();
        goto L_0x018b;
    L_0x054c:
        r40 = 1;
        goto L_0x053b;
    L_0x054f:
        r4 = "android.intent.action.VIEW";
        r5 = r75.getAction();
        r4 = r4.equals(r5);
        if (r4 == 0) goto L_0x0c4a;
    L_0x055c:
        r33 = r75.getData();
        if (r33 == 0) goto L_0x018b;
    L_0x0562:
        r6 = 0;
        r7 = 0;
        r8 = 0;
        r15 = 0;
        r16 = 0;
        r17 = 0;
        r9 = 0;
        r10 = 0;
        r11 = 0;
        r52 = 0;
        r14 = 0;
        r53 = 0;
        r13 = 0;
        r12 = 0;
        r59 = r33.getScheme();
        if (r59 == 0) goto L_0x05ef;
    L_0x057a:
        r4 = "http";
        r0 = r59;
        r4 = r0.equals(r4);
        if (r4 != 0) goto L_0x0590;
    L_0x0585:
        r4 = "https";
        r0 = r59;
        r4 = r0.equals(r4);
        if (r4 == 0) goto L_0x0782;
    L_0x0590:
        r4 = r33.getHost();
        r42 = r4.toLowerCase();
        r4 = "telegram.me";
        r0 = r42;
        r4 = r0.equals(r4);
        if (r4 != 0) goto L_0x05c4;
    L_0x05a3:
        r4 = "t.me";
        r0 = r42;
        r4 = r0.equals(r4);
        if (r4 != 0) goto L_0x05c4;
    L_0x05ae:
        r4 = "telegram.dog";
        r0 = r42;
        r4 = r0.equals(r4);
        if (r4 != 0) goto L_0x05c4;
    L_0x05b9:
        r4 = "telesco.pe";
        r0 = r42;
        r4 = r0.equals(r4);
        if (r4 == 0) goto L_0x05ef;
    L_0x05c4:
        r51 = r33.getPath();
        if (r51 == 0) goto L_0x05ef;
    L_0x05ca:
        r4 = r51.length();
        r5 = 1;
        if (r4 <= r5) goto L_0x05ef;
    L_0x05d1:
        r4 = 1;
        r0 = r51;
        r51 = r0.substring(r4);
        r4 = "joinchat/";
        r0 = r51;
        r4 = r0.startsWith(r4);
        if (r4 == 0) goto L_0x0639;
    L_0x05e3:
        r4 = "joinchat/";
        r5 = "";
        r0 = r51;
        r7 = r0.replace(r4, r5);
    L_0x05ef:
        if (r11 == 0) goto L_0x060e;
    L_0x05f1:
        r4 = "@";
        r4 = r11.startsWith(r4);
        if (r4 == 0) goto L_0x060e;
    L_0x05fa:
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r5 = " ";
        r4 = r4.append(r5);
        r4 = r4.append(r11);
        r11 = r4.toString();
    L_0x060e:
        if (r52 != 0) goto L_0x0612;
    L_0x0610:
        if (r53 == 0) goto L_0x0b95;
    L_0x0612:
        r29 = new android.os.Bundle;
        r29.<init>();
        r4 = "phone";
        r0 = r29;
        r1 = r52;
        r0.putString(r4, r1);
        r4 = "hash";
        r0 = r29;
        r1 = r53;
        r0.putString(r4, r1);
        r4 = new org.telegram.ui.LaunchActivity$$Lambda$5;
        r0 = r74;
        r1 = r29;
        r4.<init>(r0, r1);
        org.telegram.messenger.AndroidUtilities.runOnUIThread(r4);
        goto L_0x018b;
    L_0x0639:
        r4 = "addstickers/";
        r0 = r51;
        r4 = r0.startsWith(r4);
        if (r4 == 0) goto L_0x0651;
    L_0x0644:
        r4 = "addstickers/";
        r5 = "";
        r0 = r51;
        r8 = r0.replace(r4, r5);
        goto L_0x05ef;
    L_0x0651:
        r4 = "iv/";
        r0 = r51;
        r4 = r0.startsWith(r4);
        if (r4 == 0) goto L_0x0689;
    L_0x065c:
        r4 = 0;
        r5 = "url";
        r0 = r33;
        r5 = r0.getQueryParameter(r5);
        r15[r4] = r5;
        r4 = 1;
        r5 = "rhash";
        r0 = r33;
        r5 = r0.getQueryParameter(r5);
        r15[r4] = r5;
        r4 = 0;
        r4 = r15[r4];
        r4 = android.text.TextUtils.isEmpty(r4);
        if (r4 != 0) goto L_0x0686;
    L_0x067d:
        r4 = 1;
        r4 = r15[r4];
        r4 = android.text.TextUtils.isEmpty(r4);
        if (r4 == 0) goto L_0x05ef;
    L_0x0686:
        r15 = 0;
        goto L_0x05ef;
    L_0x0689:
        r4 = "msg/";
        r0 = r51;
        r4 = r0.startsWith(r4);
        if (r4 != 0) goto L_0x069f;
    L_0x0694:
        r4 = "share/";
        r0 = r51;
        r4 = r0.startsWith(r4);
        if (r4 == 0) goto L_0x0711;
    L_0x069f:
        r4 = "url";
        r0 = r33;
        r11 = r0.getQueryParameter(r4);
        if (r11 != 0) goto L_0x06ad;
    L_0x06aa:
        r11 = "";
    L_0x06ad:
        r4 = "text";
        r0 = r33;
        r4 = r0.getQueryParameter(r4);
        if (r4 == 0) goto L_0x06ed;
    L_0x06b8:
        r4 = r11.length();
        if (r4 <= 0) goto L_0x06d3;
    L_0x06be:
        r12 = 1;
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r4 = r4.append(r11);
        r5 = "\n";
        r4 = r4.append(r5);
        r11 = r4.toString();
    L_0x06d3:
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r4 = r4.append(r11);
        r5 = "text";
        r0 = r33;
        r5 = r0.getQueryParameter(r5);
        r4 = r4.append(r5);
        r11 = r4.toString();
    L_0x06ed:
        r4 = r11.length();
        r5 = 16384; // 0x4000 float:2.2959E-41 double:8.0948E-320;
        if (r4 <= r5) goto L_0x06fc;
    L_0x06f5:
        r4 = 0;
        r5 = 16384; // 0x4000 float:2.2959E-41 double:8.0948E-320;
        r11 = r11.substring(r4, r5);
    L_0x06fc:
        r4 = "\n";
        r4 = r11.endsWith(r4);
        if (r4 == 0) goto L_0x05ef;
    L_0x0705:
        r4 = 0;
        r5 = r11.length();
        r5 = r5 + -1;
        r11 = r11.substring(r4, r5);
        goto L_0x06fc;
    L_0x0711:
        r4 = "confirmphone";
        r0 = r51;
        r4 = r0.startsWith(r4);
        if (r4 == 0) goto L_0x0730;
    L_0x071c:
        r4 = "phone";
        r0 = r33;
        r52 = r0.getQueryParameter(r4);
        r4 = "hash";
        r0 = r33;
        r53 = r0.getQueryParameter(r4);
        goto L_0x05ef;
    L_0x0730:
        r4 = r51.length();
        r5 = 1;
        if (r4 < r5) goto L_0x05ef;
    L_0x0737:
        r61 = r33.getPathSegments();
        r4 = r61.size();
        if (r4 <= 0) goto L_0x0765;
    L_0x0741:
        r4 = 0;
        r0 = r61;
        r6 = r0.get(r4);
        r6 = (java.lang.String) r6;
        r4 = r61.size();
        r5 = 1;
        if (r4 <= r5) goto L_0x0765;
    L_0x0751:
        r4 = 1;
        r0 = r61;
        r4 = r0.get(r4);
        r4 = (java.lang.String) r4;
        r13 = org.telegram.messenger.Utilities.parseInt(r4);
        r4 = r13.intValue();
        if (r4 != 0) goto L_0x0765;
    L_0x0764:
        r13 = 0;
    L_0x0765:
        r4 = "start";
        r0 = r33;
        r9 = r0.getQueryParameter(r4);
        r4 = "startgroup";
        r0 = r33;
        r10 = r0.getQueryParameter(r4);
        r4 = "game";
        r0 = r33;
        r14 = r0.getQueryParameter(r4);
        goto L_0x05ef;
    L_0x0782:
        r4 = "tg";
        r0 = r59;
        r4 = r0.equals(r4);
        if (r4 == 0) goto L_0x05ef;
    L_0x078d:
        r71 = r33.toString();
        r4 = "tg:resolve";
        r0 = r71;
        r4 = r0.startsWith(r4);
        if (r4 != 0) goto L_0x07a7;
    L_0x079c:
        r4 = "tg://resolve";
        r0 = r71;
        r4 = r0.startsWith(r4);
        if (r4 == 0) goto L_0x0893;
    L_0x07a7:
        r4 = "tg:resolve";
        r5 = "tg://telegram.org";
        r0 = r71;
        r4 = r0.replace(r4, r5);
        r5 = "tg://resolve";
        r18 = "tg://telegram.org";
        r0 = r18;
        r71 = r4.replace(r5, r0);
        r33 = android.net.Uri.parse(r71);
        r4 = "domain";
        r0 = r33;
        r6 = r0.getQueryParameter(r4);
        r4 = "telegrampassport";
        r4 = r4.equals(r6);
        if (r4 == 0) goto L_0x0862;
    L_0x07d5:
        r6 = 0;
        r16 = new java.util.HashMap;
        r16.<init>();
        r4 = "scope";
        r0 = r33;
        r60 = r0.getQueryParameter(r4);
        r4 = android.text.TextUtils.isEmpty(r60);
        if (r4 != 0) goto L_0x0850;
    L_0x07ea:
        r4 = "{";
        r0 = r60;
        r4 = r0.startsWith(r4);
        if (r4 == 0) goto L_0x0850;
    L_0x07f5:
        r4 = "}";
        r0 = r60;
        r4 = r0.endsWith(r4);
        if (r4 == 0) goto L_0x0850;
    L_0x0800:
        r4 = "nonce";
        r5 = "nonce";
        r0 = r33;
        r5 = r0.getQueryParameter(r5);
        r0 = r16;
        r0.put(r4, r5);
    L_0x0811:
        r4 = "bot_id";
        r5 = "bot_id";
        r0 = r33;
        r5 = r0.getQueryParameter(r5);
        r0 = r16;
        r0.put(r4, r5);
        r4 = "scope";
        r0 = r16;
        r1 = r60;
        r0.put(r4, r1);
        r4 = "public_key";
        r5 = "public_key";
        r0 = r33;
        r5 = r0.getQueryParameter(r5);
        r0 = r16;
        r0.put(r4, r5);
        r4 = "callback_url";
        r5 = "callback_url";
        r0 = r33;
        r5 = r0.getQueryParameter(r5);
        r0 = r16;
        r0.put(r4, r5);
        goto L_0x05ef;
    L_0x0850:
        r4 = "payload";
        r5 = "payload";
        r0 = r33;
        r5 = r0.getQueryParameter(r5);
        r0 = r16;
        r0.put(r4, r5);
        goto L_0x0811;
    L_0x0862:
        r4 = "start";
        r0 = r33;
        r9 = r0.getQueryParameter(r4);
        r4 = "startgroup";
        r0 = r33;
        r10 = r0.getQueryParameter(r4);
        r4 = "game";
        r0 = r33;
        r14 = r0.getQueryParameter(r4);
        r4 = "post";
        r0 = r33;
        r4 = r0.getQueryParameter(r4);
        r13 = org.telegram.messenger.Utilities.parseInt(r4);
        r4 = r13.intValue();
        if (r4 != 0) goto L_0x05ef;
    L_0x0890:
        r13 = 0;
        goto L_0x05ef;
    L_0x0893:
        r4 = "tg:join";
        r0 = r71;
        r4 = r0.startsWith(r4);
        if (r4 != 0) goto L_0x08a9;
    L_0x089e:
        r4 = "tg://join";
        r0 = r71;
        r4 = r0.startsWith(r4);
        if (r4 == 0) goto L_0x08d0;
    L_0x08a9:
        r4 = "tg:join";
        r5 = "tg://telegram.org";
        r0 = r71;
        r4 = r0.replace(r4, r5);
        r5 = "tg://join";
        r18 = "tg://telegram.org";
        r0 = r18;
        r71 = r4.replace(r5, r0);
        r33 = android.net.Uri.parse(r71);
        r4 = "invite";
        r0 = r33;
        r7 = r0.getQueryParameter(r4);
        goto L_0x05ef;
    L_0x08d0:
        r4 = "tg:addstickers";
        r0 = r71;
        r4 = r0.startsWith(r4);
        if (r4 != 0) goto L_0x08e6;
    L_0x08db:
        r4 = "tg://addstickers";
        r0 = r71;
        r4 = r0.startsWith(r4);
        if (r4 == 0) goto L_0x090d;
    L_0x08e6:
        r4 = "tg:addstickers";
        r5 = "tg://telegram.org";
        r0 = r71;
        r4 = r0.replace(r4, r5);
        r5 = "tg://addstickers";
        r18 = "tg://telegram.org";
        r0 = r18;
        r71 = r4.replace(r5, r0);
        r33 = android.net.Uri.parse(r71);
        r4 = "set";
        r0 = r33;
        r8 = r0.getQueryParameter(r4);
        goto L_0x05ef;
    L_0x090d:
        r4 = "tg:msg";
        r0 = r71;
        r4 = r0.startsWith(r4);
        if (r4 != 0) goto L_0x0939;
    L_0x0918:
        r4 = "tg://msg";
        r0 = r71;
        r4 = r0.startsWith(r4);
        if (r4 != 0) goto L_0x0939;
    L_0x0923:
        r4 = "tg://share";
        r0 = r71;
        r4 = r0.startsWith(r4);
        if (r4 != 0) goto L_0x0939;
    L_0x092e:
        r4 = "tg:share";
        r0 = r71;
        r4 = r0.startsWith(r4);
        if (r4 == 0) goto L_0x09df;
    L_0x0939:
        r4 = "tg:msg";
        r5 = "tg://telegram.org";
        r0 = r71;
        r4 = r0.replace(r4, r5);
        r5 = "tg://msg";
        r18 = "tg://telegram.org";
        r0 = r18;
        r4 = r4.replace(r5, r0);
        r5 = "tg://share";
        r18 = "tg://telegram.org";
        r0 = r18;
        r4 = r4.replace(r5, r0);
        r5 = "tg:share";
        r18 = "tg://telegram.org";
        r0 = r18;
        r71 = r4.replace(r5, r0);
        r33 = android.net.Uri.parse(r71);
        r4 = "url";
        r0 = r33;
        r11 = r0.getQueryParameter(r4);
        if (r11 != 0) goto L_0x097b;
    L_0x0978:
        r11 = "";
    L_0x097b:
        r4 = "text";
        r0 = r33;
        r4 = r0.getQueryParameter(r4);
        if (r4 == 0) goto L_0x09bb;
    L_0x0986:
        r4 = r11.length();
        if (r4 <= 0) goto L_0x09a1;
    L_0x098c:
        r12 = 1;
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r4 = r4.append(r11);
        r5 = "\n";
        r4 = r4.append(r5);
        r11 = r4.toString();
    L_0x09a1:
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r4 = r4.append(r11);
        r5 = "text";
        r0 = r33;
        r5 = r0.getQueryParameter(r5);
        r4 = r4.append(r5);
        r11 = r4.toString();
    L_0x09bb:
        r4 = r11.length();
        r5 = 16384; // 0x4000 float:2.2959E-41 double:8.0948E-320;
        if (r4 <= r5) goto L_0x09ca;
    L_0x09c3:
        r4 = 0;
        r5 = 16384; // 0x4000 float:2.2959E-41 double:8.0948E-320;
        r11 = r11.substring(r4, r5);
    L_0x09ca:
        r4 = "\n";
        r4 = r11.endsWith(r4);
        if (r4 == 0) goto L_0x05ef;
    L_0x09d3:
        r4 = 0;
        r5 = r11.length();
        r5 = r5 + -1;
        r11 = r11.substring(r4, r5);
        goto L_0x09ca;
    L_0x09df:
        r4 = "tg:confirmphone";
        r0 = r71;
        r4 = r0.startsWith(r4);
        if (r4 != 0) goto L_0x09f5;
    L_0x09ea:
        r4 = "tg://confirmphone";
        r0 = r71;
        r4 = r0.startsWith(r4);
        if (r4 == 0) goto L_0x0a25;
    L_0x09f5:
        r4 = "tg:confirmphone";
        r5 = "tg://telegram.org";
        r0 = r71;
        r4 = r0.replace(r4, r5);
        r5 = "tg://confirmphone";
        r18 = "tg://telegram.org";
        r0 = r18;
        r71 = r4.replace(r5, r0);
        r33 = android.net.Uri.parse(r71);
        r4 = "phone";
        r0 = r33;
        r52 = r0.getQueryParameter(r4);
        r4 = "hash";
        r0 = r33;
        r53 = r0.getQueryParameter(r4);
        goto L_0x05ef;
    L_0x0a25:
        r4 = "tg:openmessage";
        r0 = r71;
        r4 = r0.startsWith(r4);
        if (r4 != 0) goto L_0x0a3b;
    L_0x0a30:
        r4 = "tg://openmessage";
        r0 = r71;
        r4 = r0.startsWith(r4);
        if (r4 == 0) goto L_0x0a93;
    L_0x0a3b:
        r4 = "tg:openmessage";
        r5 = "tg://telegram.org";
        r0 = r71;
        r4 = r0.replace(r4, r5);
        r5 = "tg://openmessage";
        r18 = "tg://telegram.org";
        r0 = r18;
        r71 = r4.replace(r5, r0);
        r33 = android.net.Uri.parse(r71);
        r4 = "user_id";
        r0 = r33;
        r72 = r0.getQueryParameter(r4);
        r4 = "chat_id";
        r0 = r33;
        r30 = r0.getQueryParameter(r4);
        r4 = "message_id";
        r0 = r33;
        r46 = r0.getQueryParameter(r4);
        if (r72 == 0) goto L_0x0a88;
    L_0x0a74:
        r4 = java.lang.Integer.parseInt(r72);	 Catch:{ NumberFormatException -> 0x11ad }
        r58 = java.lang.Integer.valueOf(r4);	 Catch:{ NumberFormatException -> 0x11ad }
    L_0x0a7c:
        if (r46 == 0) goto L_0x05ef;
    L_0x0a7e:
        r4 = java.lang.Integer.parseInt(r46);	 Catch:{ NumberFormatException -> 0x11a7 }
        r57 = java.lang.Integer.valueOf(r4);	 Catch:{ NumberFormatException -> 0x11a7 }
        goto L_0x05ef;
    L_0x0a88:
        if (r30 == 0) goto L_0x0a7c;
    L_0x0a8a:
        r4 = java.lang.Integer.parseInt(r30);	 Catch:{ NumberFormatException -> 0x11aa }
        r55 = java.lang.Integer.valueOf(r4);	 Catch:{ NumberFormatException -> 0x11aa }
        goto L_0x0a7c;
    L_0x0a93:
        r4 = "tg:passport";
        r0 = r71;
        r4 = r0.startsWith(r4);
        if (r4 != 0) goto L_0x0ab4;
    L_0x0a9e:
        r4 = "tg://passport";
        r0 = r71;
        r4 = r0.startsWith(r4);
        if (r4 != 0) goto L_0x0ab4;
    L_0x0aa9:
        r4 = "tg:secureid";
        r0 = r71;
        r4 = r0.startsWith(r4);
        if (r4 == 0) goto L_0x0b68;
    L_0x0ab4:
        r4 = "tg:passport";
        r5 = "tg://telegram.org";
        r0 = r71;
        r4 = r0.replace(r4, r5);
        r5 = "tg://passport";
        r18 = "tg://telegram.org";
        r0 = r18;
        r4 = r4.replace(r5, r0);
        r5 = "tg:secureid";
        r18 = "tg://telegram.org";
        r0 = r18;
        r71 = r4.replace(r5, r0);
        r33 = android.net.Uri.parse(r71);
        r16 = new java.util.HashMap;
        r16.<init>();
        r4 = "scope";
        r0 = r33;
        r60 = r0.getQueryParameter(r4);
        r4 = android.text.TextUtils.isEmpty(r60);
        if (r4 != 0) goto L_0x0b56;
    L_0x0af0:
        r4 = "{";
        r0 = r60;
        r4 = r0.startsWith(r4);
        if (r4 == 0) goto L_0x0b56;
    L_0x0afb:
        r4 = "}";
        r0 = r60;
        r4 = r0.endsWith(r4);
        if (r4 == 0) goto L_0x0b56;
    L_0x0b06:
        r4 = "nonce";
        r5 = "nonce";
        r0 = r33;
        r5 = r0.getQueryParameter(r5);
        r0 = r16;
        r0.put(r4, r5);
    L_0x0b17:
        r4 = "bot_id";
        r5 = "bot_id";
        r0 = r33;
        r5 = r0.getQueryParameter(r5);
        r0 = r16;
        r0.put(r4, r5);
        r4 = "scope";
        r0 = r16;
        r1 = r60;
        r0.put(r4, r1);
        r4 = "public_key";
        r5 = "public_key";
        r0 = r33;
        r5 = r0.getQueryParameter(r5);
        r0 = r16;
        r0.put(r4, r5);
        r4 = "callback_url";
        r5 = "callback_url";
        r0 = r33;
        r5 = r0.getQueryParameter(r5);
        r0 = r16;
        r0.put(r4, r5);
        goto L_0x05ef;
    L_0x0b56:
        r4 = "payload";
        r5 = "payload";
        r0 = r33;
        r5 = r0.getQueryParameter(r5);
        r0 = r16;
        r0.put(r4, r5);
        goto L_0x0b17;
    L_0x0b68:
        r4 = "tg://";
        r5 = "";
        r0 = r71;
        r4 = r0.replace(r4, r5);
        r5 = "tg:";
        r18 = "";
        r0 = r18;
        r17 = r4.replace(r5, r0);
        r4 = 63;
        r0 = r17;
        r43 = r0.indexOf(r4);
        if (r43 < 0) goto L_0x05ef;
    L_0x0b8a:
        r4 = 0;
        r0 = r17;
        r1 = r43;
        r17 = r0.substring(r4, r1);
        goto L_0x05ef;
    L_0x0b95:
        if (r6 != 0) goto L_0x0ba5;
    L_0x0b97:
        if (r7 != 0) goto L_0x0ba5;
    L_0x0b99:
        if (r8 != 0) goto L_0x0ba5;
    L_0x0b9b:
        if (r11 != 0) goto L_0x0ba5;
    L_0x0b9d:
        if (r14 != 0) goto L_0x0ba5;
    L_0x0b9f:
        if (r15 != 0) goto L_0x0ba5;
    L_0x0ba1:
        if (r16 != 0) goto L_0x0ba5;
    L_0x0ba3:
        if (r17 == 0) goto L_0x0bb1;
    L_0x0ba5:
        r4 = 0;
        r5 = r45[r4];
        r18 = 0;
        r4 = r74;
        r4.runLinkRequest(r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17, r18);
        goto L_0x018b;
    L_0x0bb1:
        r32 = 0;
        r18 = r74.getContentResolver();	 Catch:{ Exception -> 0x0c38 }
        r19 = r75.getData();	 Catch:{ Exception -> 0x0c38 }
        r20 = 0;
        r21 = 0;
        r22 = 0;
        r23 = 0;
        r32 = r18.query(r19, r20, r21, r22, r23);	 Catch:{ Exception -> 0x0c38 }
        if (r32 == 0) goto L_0x0c2e;
    L_0x0bc9:
        r4 = r32.moveToFirst();	 Catch:{ Exception -> 0x0c38 }
        if (r4 == 0) goto L_0x0c2e;
    L_0x0bcf:
        r4 = "account_name";
        r0 = r32;
        r4 = r0.getColumnIndex(r4);	 Catch:{ Exception -> 0x0c38 }
        r0 = r32;
        r4 = r0.getString(r4);	 Catch:{ Exception -> 0x0c38 }
        r4 = org.telegram.messenger.Utilities.parseInt(r4);	 Catch:{ Exception -> 0x0c38 }
        r28 = r4.intValue();	 Catch:{ Exception -> 0x0c38 }
        r27 = 0;
    L_0x0be8:
        r4 = 3;
        r0 = r27;
        if (r0 >= r4) goto L_0x0c05;
    L_0x0bed:
        r4 = org.telegram.messenger.UserConfig.getInstance(r27);	 Catch:{ Exception -> 0x0c38 }
        r4 = r4.getClientUserId();	 Catch:{ Exception -> 0x0c38 }
        r0 = r28;
        if (r4 != r0) goto L_0x0c35;
    L_0x0bf9:
        r4 = 0;
        r45[r4] = r27;	 Catch:{ Exception -> 0x0c38 }
        r4 = 0;
        r4 = r45[r4];	 Catch:{ Exception -> 0x0c38 }
        r5 = 1;
        r0 = r74;
        r0.switchToAccount(r4, r5);	 Catch:{ Exception -> 0x0c38 }
    L_0x0c05:
        r4 = "DATA4";
        r0 = r32;
        r4 = r0.getColumnIndex(r4);	 Catch:{ Exception -> 0x0c38 }
        r0 = r32;
        r73 = r0.getInt(r4);	 Catch:{ Exception -> 0x0c38 }
        r4 = 0;
        r4 = r45[r4];	 Catch:{ Exception -> 0x0c38 }
        r4 = org.telegram.messenger.NotificationCenter.getInstance(r4);	 Catch:{ Exception -> 0x0c38 }
        r5 = org.telegram.messenger.NotificationCenter.closeChats;	 Catch:{ Exception -> 0x0c38 }
        r18 = 0;
        r0 = r18;
        r0 = new java.lang.Object[r0];	 Catch:{ Exception -> 0x0c38 }
        r18 = r0;
        r0 = r18;
        r4.postNotificationName(r5, r0);	 Catch:{ Exception -> 0x0c38 }
        r58 = java.lang.Integer.valueOf(r73);	 Catch:{ Exception -> 0x0c38 }
    L_0x0c2e:
        if (r32 == 0) goto L_0x018b;
    L_0x0c30:
        r32.close();
        goto L_0x018b;
    L_0x0c35:
        r27 = r27 + 1;
        goto L_0x0be8;
    L_0x0c38:
        r38 = move-exception;
        org.telegram.messenger.FileLog.e(r38);	 Catch:{ all -> 0x0c43 }
        if (r32 == 0) goto L_0x018b;
    L_0x0c3e:
        r32.close();
        goto L_0x018b;
    L_0x0c43:
        r4 = move-exception;
        if (r32 == 0) goto L_0x0c49;
    L_0x0c46:
        r32.close();
    L_0x0c49:
        throw r4;
    L_0x0c4a:
        r4 = r75.getAction();
        r5 = "org.telegram.messenger.OPEN_ACCOUNT";
        r4 = r4.equals(r5);
        if (r4 == 0) goto L_0x0c5e;
    L_0x0c57:
        r4 = 1;
        r48 = java.lang.Integer.valueOf(r4);
        goto L_0x018b;
    L_0x0c5e:
        r4 = r75.getAction();
        r5 = "new_dialog";
        r4 = r4.equals(r5);
        if (r4 == 0) goto L_0x0c72;
    L_0x0c6b:
        r4 = 1;
        r47 = java.lang.Integer.valueOf(r4);
        goto L_0x018b;
    L_0x0c72:
        r4 = r75.getAction();
        r5 = "com.tmessages.openchat";
        r4 = r4.startsWith(r5);
        if (r4 == 0) goto L_0x0cfb;
    L_0x0c7f:
        r4 = "chatId";
        r5 = 0;
        r0 = r75;
        r31 = r0.getIntExtra(r4, r5);
        r4 = "userId";
        r5 = 0;
        r0 = r75;
        r73 = r0.getIntExtra(r4, r5);
        r4 = "encId";
        r5 = 0;
        r0 = r75;
        r39 = r0.getIntExtra(r4, r5);
        if (r31 == 0) goto L_0x0cbb;
    L_0x0c9f:
        r4 = 0;
        r4 = r45[r4];
        r4 = org.telegram.messenger.NotificationCenter.getInstance(r4);
        r5 = org.telegram.messenger.NotificationCenter.closeChats;
        r18 = 0;
        r0 = r18;
        r0 = new java.lang.Object[r0];
        r18 = r0;
        r0 = r18;
        r4.postNotificationName(r5, r0);
        r55 = java.lang.Integer.valueOf(r31);
        goto L_0x018b;
    L_0x0cbb:
        if (r73 == 0) goto L_0x0cd9;
    L_0x0cbd:
        r4 = 0;
        r4 = r45[r4];
        r4 = org.telegram.messenger.NotificationCenter.getInstance(r4);
        r5 = org.telegram.messenger.NotificationCenter.closeChats;
        r18 = 0;
        r0 = r18;
        r0 = new java.lang.Object[r0];
        r18 = r0;
        r0 = r18;
        r4.postNotificationName(r5, r0);
        r58 = java.lang.Integer.valueOf(r73);
        goto L_0x018b;
    L_0x0cd9:
        if (r39 == 0) goto L_0x0cf7;
    L_0x0cdb:
        r4 = 0;
        r4 = r45[r4];
        r4 = org.telegram.messenger.NotificationCenter.getInstance(r4);
        r5 = org.telegram.messenger.NotificationCenter.closeChats;
        r18 = 0;
        r0 = r18;
        r0 = new java.lang.Object[r0];
        r18 = r0;
        r0 = r18;
        r4.postNotificationName(r5, r0);
        r56 = java.lang.Integer.valueOf(r39);
        goto L_0x018b;
    L_0x0cf7:
        r62 = 1;
        goto L_0x018b;
    L_0x0cfb:
        r4 = r75.getAction();
        r5 = "com.tmessages.openplayer";
        r4 = r4.equals(r5);
        if (r4 == 0) goto L_0x0d0c;
    L_0x0d08:
        r64 = 1;
        goto L_0x018b;
    L_0x0d0c:
        r4 = r75.getAction();
        r5 = "org.tmessages.openlocations";
        r4 = r4.equals(r5);
        if (r4 == 0) goto L_0x018b;
    L_0x0d19:
        r63 = 1;
        goto L_0x018b;
    L_0x0d1d:
        r4 = r55.intValue();
        if (r4 == 0) goto L_0x0d90;
    L_0x0d23:
        r29 = new android.os.Bundle;
        r29.<init>();
        r4 = "chat_id";
        r5 = r55.intValue();
        r0 = r29;
        r0.putInt(r4, r5);
        r4 = r57.intValue();
        if (r4 == 0) goto L_0x0d46;
    L_0x0d3a:
        r4 = "message_id";
        r5 = r57.intValue();
        r0 = r29;
        r0.putInt(r4, r5);
    L_0x0d46:
        r4 = mainFragmentsStack;
        r4 = r4.isEmpty();
        if (r4 != 0) goto L_0x0d6f;
    L_0x0d4e:
        r4 = 0;
        r4 = r45[r4];
        r5 = org.telegram.messenger.MessagesController.getInstance(r4);
        r4 = mainFragmentsStack;
        r18 = mainFragmentsStack;
        r18 = r18.size();
        r18 = r18 + -1;
        r0 = r18;
        r4 = r4.get(r0);
        r4 = (org.telegram.ui.ActionBar.BaseFragment) r4;
        r0 = r29;
        r4 = r5.checkCanOpenChat(r0, r4);
        if (r4 == 0) goto L_0x01fc;
    L_0x0d6f:
        r19 = new org.telegram.ui.ChatActivity;
        r0 = r19;
        r1 = r29;
        r0.<init>(r1);
        r0 = r74;
        r0 = r0.actionBarLayout;
        r18 = r0;
        r20 = 0;
        r21 = 1;
        r22 = 1;
        r23 = 0;
        r4 = r18.presentFragment(r19, r20, r21, r22, r23);
        if (r4 == 0) goto L_0x01fc;
    L_0x0d8c:
        r54 = 1;
        goto L_0x01fc;
    L_0x0d90:
        r4 = r56.intValue();
        if (r4 == 0) goto L_0x0dc8;
    L_0x0d96:
        r29 = new android.os.Bundle;
        r29.<init>();
        r4 = "enc_id";
        r5 = r56.intValue();
        r0 = r29;
        r0.putInt(r4, r5);
        r19 = new org.telegram.ui.ChatActivity;
        r0 = r19;
        r1 = r29;
        r0.<init>(r1);
        r0 = r74;
        r0 = r0.actionBarLayout;
        r18 = r0;
        r20 = 0;
        r21 = 1;
        r22 = 1;
        r23 = 0;
        r4 = r18.presentFragment(r19, r20, r21, r22, r23);
        if (r4 == 0) goto L_0x01fc;
    L_0x0dc4:
        r54 = 1;
        goto L_0x01fc;
    L_0x0dc8:
        if (r62 == 0) goto L_0x0e1e;
    L_0x0dca:
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 != 0) goto L_0x0ddd;
    L_0x0dd0:
        r0 = r74;
        r4 = r0.actionBarLayout;
        r4.removeAllFragments();
    L_0x0dd7:
        r54 = 0;
        r76 = 0;
        goto L_0x01fc;
    L_0x0ddd:
        r0 = r74;
        r4 = r0.layersActionBarLayout;
        r4 = r4.fragmentsStack;
        r4 = r4.isEmpty();
        if (r4 != 0) goto L_0x0dd7;
    L_0x0de9:
        r27 = 0;
    L_0x0deb:
        r0 = r74;
        r4 = r0.layersActionBarLayout;
        r4 = r4.fragmentsStack;
        r4 = r4.size();
        r4 = r4 + -1;
        if (r4 <= 0) goto L_0x0e15;
    L_0x0df9:
        r0 = r74;
        r5 = r0.layersActionBarLayout;
        r0 = r74;
        r4 = r0.layersActionBarLayout;
        r4 = r4.fragmentsStack;
        r18 = 0;
        r0 = r18;
        r4 = r4.get(r0);
        r4 = (org.telegram.ui.ActionBar.BaseFragment) r4;
        r5.removeFragmentFromStack(r4);
        r27 = r27 + -1;
        r27 = r27 + 1;
        goto L_0x0deb;
    L_0x0e15:
        r0 = r74;
        r4 = r0.layersActionBarLayout;
        r5 = 0;
        r4.closeLastFragment(r5);
        goto L_0x0dd7;
    L_0x0e1e:
        if (r64 == 0) goto L_0x0e49;
    L_0x0e20:
        r0 = r74;
        r4 = r0.actionBarLayout;
        r4 = r4.fragmentsStack;
        r4 = r4.isEmpty();
        if (r4 != 0) goto L_0x0e45;
    L_0x0e2c:
        r0 = r74;
        r4 = r0.actionBarLayout;
        r4 = r4.fragmentsStack;
        r5 = 0;
        r19 = r4.get(r5);
        r19 = (org.telegram.ui.ActionBar.BaseFragment) r19;
        r4 = new org.telegram.ui.Components.AudioPlayerAlert;
        r0 = r74;
        r4.<init>(r0);
        r0 = r19;
        r0.showDialog(r4);
    L_0x0e45:
        r54 = 0;
        goto L_0x01fc;
    L_0x0e49:
        if (r63 == 0) goto L_0x0e7d;
    L_0x0e4b:
        r0 = r74;
        r4 = r0.actionBarLayout;
        r4 = r4.fragmentsStack;
        r4 = r4.isEmpty();
        if (r4 != 0) goto L_0x0e79;
    L_0x0e57:
        r0 = r74;
        r4 = r0.actionBarLayout;
        r4 = r4.fragmentsStack;
        r5 = 0;
        r19 = r4.get(r5);
        r19 = (org.telegram.ui.ActionBar.BaseFragment) r19;
        r4 = new org.telegram.ui.Components.SharingLocationsAlert;
        r5 = new org.telegram.ui.LaunchActivity$$Lambda$6;
        r0 = r74;
        r1 = r45;
        r5.<init>(r0, r1);
        r0 = r74;
        r4.<init>(r0, r5);
        r0 = r19;
        r0.showDialog(r4);
    L_0x0e79:
        r54 = 0;
        goto L_0x01fc;
    L_0x0e7d:
        r0 = r74;
        r4 = r0.videoPath;
        if (r4 != 0) goto L_0x0ea1;
    L_0x0e83:
        r0 = r74;
        r4 = r0.photoPathsArray;
        if (r4 != 0) goto L_0x0ea1;
    L_0x0e89:
        r0 = r74;
        r4 = r0.sendingText;
        if (r4 != 0) goto L_0x0ea1;
    L_0x0e8f:
        r0 = r74;
        r4 = r0.documentsPathsArray;
        if (r4 != 0) goto L_0x0ea1;
    L_0x0e95:
        r0 = r74;
        r4 = r0.contactsToSend;
        if (r4 != 0) goto L_0x0ea1;
    L_0x0e9b:
        r0 = r74;
        r4 = r0.documentsUrisArray;
        if (r4 == 0) goto L_0x106b;
    L_0x0ea1:
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 != 0) goto L_0x0ebd;
    L_0x0ea7:
        r4 = 0;
        r4 = r45[r4];
        r4 = org.telegram.messenger.NotificationCenter.getInstance(r4);
        r5 = org.telegram.messenger.NotificationCenter.closeChats;
        r18 = 0;
        r0 = r18;
        r0 = new java.lang.Object[r0];
        r18 = r0;
        r0 = r18;
        r4.postNotificationName(r5, r0);
    L_0x0ebd:
        r4 = 0;
        r4 = (r34 > r4 ? 1 : (r34 == r4 ? 0 : -1));
        if (r4 != 0) goto L_0x104e;
    L_0x0ec3:
        r29 = new android.os.Bundle;
        r29.<init>();
        r4 = "onlySelect";
        r5 = 1;
        r0 = r29;
        r0.putBoolean(r4, r5);
        r4 = "dialogsType";
        r5 = 3;
        r0 = r29;
        r0.putInt(r4, r5);
        r4 = "allowSwitchAccount";
        r5 = 1;
        r0 = r29;
        r0.putBoolean(r4, r5);
        r0 = r74;
        r4 = r0.contactsToSend;
        if (r4 == 0) goto L_0x0fa9;
    L_0x0ee9:
        r0 = r74;
        r4 = r0.contactsToSend;
        r4 = r4.size();
        r5 = 1;
        if (r4 == r5) goto L_0x0f1c;
    L_0x0ef4:
        r4 = "selectAlertString";
        r5 = "SendContactTo";
        r18 = 2131494605; // 0x7f0c06cd float:1.8612723E38 double:1.0530982586E-314;
        r0 = r18;
        r5 = org.telegram.messenger.LocaleController.getString(r5, r0);
        r0 = r29;
        r0.putString(r4, r5);
        r4 = "selectAlertStringGroup";
        r5 = "SendContactToGroup";
        r18 = 2131494592; // 0x7f0c06c0 float:1.8612697E38 double:1.053098252E-314;
        r0 = r18;
        r5 = org.telegram.messenger.LocaleController.getString(r5, r0);
        r0 = r29;
        r0.putString(r4, r5);
    L_0x0f1c:
        r19 = new org.telegram.ui.DialogsActivity;
        r0 = r19;
        r1 = r29;
        r0.<init>(r1);
        r0 = r19;
        r1 = r74;
        r0.setDelegate(r1);
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 == 0) goto L_0x0fd6;
    L_0x0f32:
        r0 = r74;
        r4 = r0.layersActionBarLayout;
        r4 = r4.fragmentsStack;
        r4 = r4.size();
        if (r4 <= 0) goto L_0x0fd3;
    L_0x0f3e:
        r0 = r74;
        r4 = r0.layersActionBarLayout;
        r4 = r4.fragmentsStack;
        r0 = r74;
        r5 = r0.layersActionBarLayout;
        r5 = r5.fragmentsStack;
        r5 = r5.size();
        r5 = r5 + -1;
        r4 = r4.get(r5);
        r4 = r4 instanceof org.telegram.ui.DialogsActivity;
        if (r4 == 0) goto L_0x0fd3;
    L_0x0f58:
        r20 = 1;
    L_0x0f5a:
        r0 = r74;
        r0 = r0.actionBarLayout;
        r18 = r0;
        r21 = 1;
        r22 = 1;
        r23 = 0;
        r18.presentFragment(r19, r20, r21, r22, r23);
        r54 = 1;
        r4 = org.telegram.ui.SecretMediaViewer.hasInstance();
        if (r4 == 0) goto L_0x1004;
    L_0x0f71:
        r4 = org.telegram.ui.SecretMediaViewer.getInstance();
        r4 = r4.isVisible();
        if (r4 == 0) goto L_0x1004;
    L_0x0f7b:
        r4 = org.telegram.ui.SecretMediaViewer.getInstance();
        r5 = 0;
        r18 = 0;
        r0 = r18;
        r4.closePhoto(r5, r0);
    L_0x0f87:
        r0 = r74;
        r4 = r0.drawerLayoutContainer;
        r5 = 0;
        r18 = 0;
        r0 = r18;
        r4.setAllowOpenDrawer(r5, r0);
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 == 0) goto L_0x1040;
    L_0x0f99:
        r0 = r74;
        r4 = r0.actionBarLayout;
        r4.showLastFragment();
        r0 = r74;
        r4 = r0.rightActionBarLayout;
        r4.showLastFragment();
        goto L_0x01fc;
    L_0x0fa9:
        r4 = "selectAlertString";
        r5 = "SendMessagesTo";
        r18 = 2131494605; // 0x7f0c06cd float:1.8612723E38 double:1.0530982586E-314;
        r0 = r18;
        r5 = org.telegram.messenger.LocaleController.getString(r5, r0);
        r0 = r29;
        r0.putString(r4, r5);
        r4 = "selectAlertStringGroup";
        r5 = "SendMessagesToGroup";
        r18 = 2131494606; // 0x7f0c06ce float:1.8612725E38 double:1.053098259E-314;
        r0 = r18;
        r5 = org.telegram.messenger.LocaleController.getString(r5, r0);
        r0 = r29;
        r0.putString(r4, r5);
        goto L_0x0f1c;
    L_0x0fd3:
        r20 = 0;
        goto L_0x0f5a;
    L_0x0fd6:
        r0 = r74;
        r4 = r0.actionBarLayout;
        r4 = r4.fragmentsStack;
        r4 = r4.size();
        r5 = 1;
        if (r4 <= r5) goto L_0x1001;
    L_0x0fe3:
        r0 = r74;
        r4 = r0.actionBarLayout;
        r4 = r4.fragmentsStack;
        r0 = r74;
        r5 = r0.actionBarLayout;
        r5 = r5.fragmentsStack;
        r5 = r5.size();
        r5 = r5 + -1;
        r4 = r4.get(r5);
        r4 = r4 instanceof org.telegram.ui.DialogsActivity;
        if (r4 == 0) goto L_0x1001;
    L_0x0ffd:
        r20 = 1;
    L_0x0fff:
        goto L_0x0f5a;
    L_0x1001:
        r20 = 0;
        goto L_0x0fff;
    L_0x1004:
        r4 = org.telegram.ui.PhotoViewer.hasInstance();
        if (r4 == 0) goto L_0x1022;
    L_0x100a:
        r4 = org.telegram.ui.PhotoViewer.getInstance();
        r4 = r4.isVisible();
        if (r4 == 0) goto L_0x1022;
    L_0x1014:
        r4 = org.telegram.ui.PhotoViewer.getInstance();
        r5 = 0;
        r18 = 1;
        r0 = r18;
        r4.closePhoto(r5, r0);
        goto L_0x0f87;
    L_0x1022:
        r4 = org.telegram.ui.ArticleViewer.hasInstance();
        if (r4 == 0) goto L_0x0f87;
    L_0x1028:
        r4 = org.telegram.ui.ArticleViewer.getInstance();
        r4 = r4.isVisible();
        if (r4 == 0) goto L_0x0f87;
    L_0x1032:
        r4 = org.telegram.ui.ArticleViewer.getInstance();
        r5 = 0;
        r18 = 1;
        r0 = r18;
        r4.close(r5, r0);
        goto L_0x0f87;
    L_0x1040:
        r0 = r74;
        r4 = r0.drawerLayoutContainer;
        r5 = 1;
        r18 = 0;
        r0 = r18;
        r4.setAllowOpenDrawer(r5, r0);
        goto L_0x01fc;
    L_0x104e:
        r37 = new java.util.ArrayList;
        r37.<init>();
        r4 = java.lang.Long.valueOf(r34);
        r0 = r37;
        r0.add(r4);
        r4 = 0;
        r5 = 0;
        r18 = 0;
        r0 = r74;
        r1 = r37;
        r2 = r18;
        r0.didSelectDialogs(r4, r1, r5, r2);
        goto L_0x01fc;
    L_0x106b:
        r4 = r48.intValue();
        if (r4 == 0) goto L_0x10b8;
    L_0x1071:
        r0 = r74;
        r0 = r0.actionBarLayout;
        r21 = r0;
        r22 = new org.telegram.ui.SettingsActivity;
        r22.<init>();
        r23 = 0;
        r24 = 1;
        r25 = 1;
        r26 = 0;
        r21.presentFragment(r22, r23, r24, r25, r26);
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 == 0) goto L_0x10ab;
    L_0x108d:
        r0 = r74;
        r4 = r0.actionBarLayout;
        r4.showLastFragment();
        r0 = r74;
        r4 = r0.rightActionBarLayout;
        r4.showLastFragment();
        r0 = r74;
        r4 = r0.drawerLayoutContainer;
        r5 = 0;
        r18 = 0;
        r0 = r18;
        r4.setAllowOpenDrawer(r5, r0);
    L_0x10a7:
        r54 = 1;
        goto L_0x01fc;
    L_0x10ab:
        r0 = r74;
        r4 = r0.drawerLayoutContainer;
        r5 = 1;
        r18 = 0;
        r0 = r18;
        r4.setAllowOpenDrawer(r5, r0);
        goto L_0x10a7;
    L_0x10b8:
        r4 = r47.intValue();
        if (r4 == 0) goto L_0x01fc;
    L_0x10be:
        r29 = new android.os.Bundle;
        r29.<init>();
        r4 = "destroyAfterSelect";
        r5 = 1;
        r0 = r29;
        r0.putBoolean(r4, r5);
        r0 = r74;
        r0 = r0.actionBarLayout;
        r21 = r0;
        r22 = new org.telegram.ui.ContactsActivity;
        r0 = r22;
        r1 = r29;
        r0.<init>(r1);
        r23 = 0;
        r24 = 1;
        r25 = 1;
        r26 = 0;
        r21.presentFragment(r22, r23, r24, r25, r26);
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 == 0) goto L_0x110a;
    L_0x10ec:
        r0 = r74;
        r4 = r0.actionBarLayout;
        r4.showLastFragment();
        r0 = r74;
        r4 = r0.rightActionBarLayout;
        r4.showLastFragment();
        r0 = r74;
        r4 = r0.drawerLayoutContainer;
        r5 = 0;
        r18 = 0;
        r0 = r18;
        r4.setAllowOpenDrawer(r5, r0);
    L_0x1106:
        r54 = 1;
        goto L_0x01fc;
    L_0x110a:
        r0 = r74;
        r4 = r0.drawerLayoutContainer;
        r5 = 1;
        r18 = 0;
        r0 = r18;
        r4.setAllowOpenDrawer(r5, r0);
        goto L_0x1106;
    L_0x1117:
        r0 = r74;
        r4 = r0.actionBarLayout;
        r4 = r4.fragmentsStack;
        r4 = r4.isEmpty();
        if (r4 == 0) goto L_0x0238;
    L_0x1123:
        r36 = new org.telegram.ui.DialogsActivity;
        r4 = 0;
        r0 = r36;
        r0.<init>(r4);
        r0 = r74;
        r4 = r0.sideMenu;
        r0 = r36;
        r0.setSideMenu(r4);
        r0 = r74;
        r4 = r0.actionBarLayout;
        r0 = r36;
        r4.addFragmentToStack(r0);
        r0 = r74;
        r4 = r0.drawerLayoutContainer;
        r5 = 1;
        r18 = 0;
        r0 = r18;
        r4.setAllowOpenDrawer(r5, r0);
        goto L_0x0238;
    L_0x114b:
        r0 = r74;
        r4 = r0.actionBarLayout;
        r4 = r4.fragmentsStack;
        r4 = r4.isEmpty();
        if (r4 == 0) goto L_0x0238;
    L_0x1157:
        r0 = r74;
        r4 = r0.currentAccount;
        r4 = org.telegram.messenger.UserConfig.getInstance(r4);
        r4 = r4.isClientActivated();
        if (r4 != 0) goto L_0x117f;
    L_0x1165:
        r0 = r74;
        r4 = r0.actionBarLayout;
        r5 = new org.telegram.ui.LoginActivity;
        r5.<init>();
        r4.addFragmentToStack(r5);
        r0 = r74;
        r4 = r0.drawerLayoutContainer;
        r5 = 0;
        r18 = 0;
        r0 = r18;
        r4.setAllowOpenDrawer(r5, r0);
        goto L_0x0238;
    L_0x117f:
        r36 = new org.telegram.ui.DialogsActivity;
        r4 = 0;
        r0 = r36;
        r0.<init>(r4);
        r0 = r74;
        r4 = r0.sideMenu;
        r0 = r36;
        r0.setSideMenu(r4);
        r0 = r74;
        r4 = r0.actionBarLayout;
        r0 = r36;
        r4.addFragmentToStack(r0);
        r0 = r74;
        r4 = r0.drawerLayoutContainer;
        r5 = 1;
        r18 = 0;
        r0 = r18;
        r4.setAllowOpenDrawer(r5, r0);
        goto L_0x0238;
    L_0x11a7:
        r4 = move-exception;
        goto L_0x05ef;
    L_0x11aa:
        r4 = move-exception;
        goto L_0x0a7c;
    L_0x11ad:
        r4 = move-exception;
        goto L_0x0a7c;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.LaunchActivity.handleIntent(android.content.Intent, boolean, boolean, boolean):boolean");
    }

    final /* synthetic */ void lambda$handleIntent$5$LaunchActivity(Bundle args) {
        presentFragment(new CancelAccountDeletionActivity(args));
    }

    final /* synthetic */ void lambda$handleIntent$7$LaunchActivity(int[] intentAccount, SharingLocationInfo info) {
        intentAccount[0] = info.messageObject.currentAccount;
        switchToAccount(intentAccount[0], true);
        LocationActivity locationActivity = new LocationActivity(2);
        locationActivity.setMessageObject(info.messageObject);
        locationActivity.setDelegate(new LaunchActivity$$Lambda$48(intentAccount, info.messageObject.getDialogId()));
        presentFragment(locationActivity);
    }

    private void runLinkRequest(int intentAccount, String username, String group, String sticker, String botUser, String botChat, String message, boolean hasUrl, Integer messageId, String game, String[] instantView, HashMap<String, String> auth, String unsupportedUrl, int state) {
        if (state != 0 || UserConfig.getActivatedAccountsCount() < 2 || auth == null) {
            AlertDialog progressDialog = new AlertDialog(this, 1);
            progressDialog.setMessage(LocaleController.getString("Loading", R.string.Loading));
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            int[] requestId = new int[]{0};
            TLObject req;
            if (username != null) {
                req = new TL_contacts_resolveUsername();
                req.username = username;
                requestId[0] = ConnectionsManager.getInstance(intentAccount).sendRequest(req, new LaunchActivity$$Lambda$8(this, progressDialog, game, intentAccount, botChat, botUser, messageId));
            } else if (group != null) {
                if (state == 0) {
                    TLObject req2 = new TL_messages_checkChatInvite();
                    req2.hash = group;
                    requestId[0] = ConnectionsManager.getInstance(intentAccount).sendRequest(req2, new LaunchActivity$$Lambda$9(this, progressDialog, intentAccount, group, username, sticker, botUser, botChat, message, hasUrl, messageId, game, instantView, auth, unsupportedUrl), 2);
                } else if (state == 1) {
                    req = new TL_messages_importChatInvite();
                    req.hash = group;
                    ConnectionsManager.getInstance(intentAccount).sendRequest(req, new LaunchActivity$$Lambda$10(this, intentAccount, progressDialog), 2);
                }
            } else if (sticker != null) {
                if (!mainFragmentsStack.isEmpty()) {
                    TL_inputStickerSetShortName stickerset = new TL_inputStickerSetShortName();
                    stickerset.short_name = sticker;
                    BaseFragment fragment = (BaseFragment) mainFragmentsStack.get(mainFragmentsStack.size() - 1);
                    fragment.showDialog(new StickersAlert(this, fragment, stickerset, null, null));
                    return;
                }
                return;
            } else if (message != null) {
                Bundle args = new Bundle();
                args.putBoolean("onlySelect", true);
                DialogsActivity fragment2 = new DialogsActivity(args);
                fragment2.setDelegate(new LaunchActivity$$Lambda$11(this, hasUrl, intentAccount, message));
                presentFragment(fragment2, false, true);
            } else if (instantView == null) {
                if (auth != null) {
                    int bot_id = Utilities.parseInt((String) auth.get("bot_id")).intValue();
                    if (bot_id != 0) {
                        String payload = (String) auth.get("payload");
                        String nonce = (String) auth.get("nonce");
                        String callbackUrl = (String) auth.get("callback_url");
                        req = new TL_account_getAuthorizationForm();
                        req.bot_id = bot_id;
                        req.scope = (String) auth.get("scope");
                        req.public_key = (String) auth.get("public_key");
                        requestId[0] = ConnectionsManager.getInstance(intentAccount).sendRequest(req, new LaunchActivity$$Lambda$12(this, requestId, intentAccount, progressDialog, req, payload, nonce, callbackUrl));
                    } else {
                        return;
                    }
                } else if (unsupportedUrl != null) {
                    req = new TL_help_getDeepLinkInfo();
                    req.path = unsupportedUrl;
                    requestId[0] = ConnectionsManager.getInstance(this.currentAccount).sendRequest(req, new LaunchActivity$$Lambda$13(this, progressDialog));
                }
            }
            if (requestId[0] != 0) {
                progressDialog.setButton(-2, LocaleController.getString("Cancel", R.string.Cancel), new LaunchActivity$$Lambda$14(intentAccount, requestId));
                try {
                    progressDialog.show();
                    return;
                } catch (Exception e) {
                    return;
                }
            }
            return;
        }
        AlertsCreator.createAccountSelectDialog(this, new LaunchActivity$$Lambda$7(this, intentAccount, username, group, sticker, botUser, botChat, message, hasUrl, messageId, game, instantView, auth, unsupportedUrl)).show();
    }

    final /* synthetic */ void lambda$runLinkRequest$8$LaunchActivity(int intentAccount, String username, String group, String sticker, String botUser, String botChat, String message, boolean hasUrl, Integer messageId, String game, String[] instantView, HashMap auth, String unsupportedUrl, int account) {
        if (account != intentAccount) {
            switchToAccount(account, true);
        }
        runLinkRequest(account, username, group, sticker, botUser, botChat, message, hasUrl, messageId, game, instantView, auth, unsupportedUrl, 1);
    }

    final /* synthetic */ void lambda$runLinkRequest$12$LaunchActivity(AlertDialog progressDialog, String game, int intentAccount, String botChat, String botUser, Integer messageId, TLObject response, TL_error error) {
        AndroidUtilities.runOnUIThread(new LaunchActivity$$Lambda$45(this, progressDialog, response, error, game, intentAccount, botChat, botUser, messageId));
    }

    final /* synthetic */ void lambda$null$11$LaunchActivity(AlertDialog progressDialog, TLObject response, TL_error error, String game, int intentAccount, String botChat, String botUser, Integer messageId) {
        if (!isFinishing()) {
            try {
                progressDialog.dismiss();
            } catch (Throwable e) {
                FileLog.e(e);
            }
            TL_contacts_resolvedPeer res = (TL_contacts_resolvedPeer) response;
            if (error != null || this.actionBarLayout == null || (game != null && (game == null || res.users.isEmpty()))) {
                try {
                    Toast.makeText(this, LocaleController.getString("NoUsernameFound", R.string.NoUsernameFound), 0).show();
                    return;
                } catch (Throwable e2) {
                    FileLog.e(e2);
                    return;
                }
            }
            MessagesController.getInstance(intentAccount).putUsers(res.users, false);
            MessagesController.getInstance(intentAccount).putChats(res.chats, false);
            MessagesStorage.getInstance(intentAccount).putUsersAndChats(res.users, res.chats, false, true);
            Bundle args;
            DialogsActivity fragment;
            if (game != null) {
                args = new Bundle();
                args.putBoolean("onlySelect", true);
                args.putBoolean("cantSendToChannels", true);
                args.putInt("dialogsType", 1);
                args.putString("selectAlertString", LocaleController.getString("SendGameTo", R.string.SendGameTo));
                args.putString("selectAlertStringGroup", LocaleController.getString("SendGameToGroup", R.string.SendGameToGroup));
                fragment = new DialogsActivity(args);
                fragment.setDelegate(new LaunchActivity$$Lambda$46(this, game, intentAccount, res));
                boolean removeLast = AndroidUtilities.isTablet() ? this.layersActionBarLayout.fragmentsStack.size() > 0 && (this.layersActionBarLayout.fragmentsStack.get(this.layersActionBarLayout.fragmentsStack.size() - 1) instanceof DialogsActivity) : this.actionBarLayout.fragmentsStack.size() > 1 && (this.actionBarLayout.fragmentsStack.get(this.actionBarLayout.fragmentsStack.size() - 1) instanceof DialogsActivity);
                this.actionBarLayout.presentFragment(fragment, removeLast, true, true, false);
                if (SecretMediaViewer.hasInstance() && SecretMediaViewer.getInstance().isVisible()) {
                    SecretMediaViewer.getInstance().closePhoto(false, false);
                } else if (PhotoViewer.hasInstance() && PhotoViewer.getInstance().isVisible()) {
                    PhotoViewer.getInstance().closePhoto(false, true);
                } else if (ArticleViewer.hasInstance() && ArticleViewer.getInstance().isVisible()) {
                    ArticleViewer.getInstance().close(false, true);
                }
                this.drawerLayoutContainer.setAllowOpenDrawer(false, false);
                if (AndroidUtilities.isTablet()) {
                    this.actionBarLayout.showLastFragment();
                    this.rightActionBarLayout.showLastFragment();
                    return;
                }
                this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
            } else if (botChat != null) {
                User user = !res.users.isEmpty() ? (User) res.users.get(0) : null;
                if (user == null || (user.bot && user.bot_nochats)) {
                    try {
                        Toast.makeText(this, LocaleController.getString("BotCantJoinGroups", R.string.BotCantJoinGroups), 0).show();
                        return;
                    } catch (Throwable e22) {
                        FileLog.e(e22);
                        return;
                    }
                }
                args = new Bundle();
                args.putBoolean("onlySelect", true);
                args.putInt("dialogsType", 2);
                args.putString("addToGroupAlertString", LocaleController.formatString("AddToTheGroupTitle", R.string.AddToTheGroupTitle, UserObject.getUserName(user), "%1$s"));
                fragment = new DialogsActivity(args);
                fragment.setDelegate(new LaunchActivity$$Lambda$47(this, intentAccount, user, botChat));
                presentFragment(fragment);
            } else {
                boolean isBot = false;
                args = new Bundle();
                long dialog_id;
                if (res.chats.isEmpty()) {
                    args.putInt("user_id", ((User) res.users.get(0)).id);
                    dialog_id = (long) ((User) res.users.get(0)).id;
                } else {
                    args.putInt("chat_id", ((Chat) res.chats.get(0)).id);
                    dialog_id = (long) (-((Chat) res.chats.get(0)).id);
                }
                if (botUser != null && res.users.size() > 0 && ((User) res.users.get(0)).bot) {
                    args.putString("botUser", botUser);
                    isBot = true;
                }
                if (messageId != null) {
                    args.putInt("message_id", messageId.intValue());
                }
                BaseFragment lastFragment = !mainFragmentsStack.isEmpty() ? (BaseFragment) mainFragmentsStack.get(mainFragmentsStack.size() - 1) : null;
                if (lastFragment != null && !MessagesController.getInstance(intentAccount).checkCanOpenChat(args, lastFragment)) {
                    return;
                }
                if (isBot && lastFragment != null && (lastFragment instanceof ChatActivity) && ((ChatActivity) lastFragment).getDialogId() == dialog_id) {
                    ((ChatActivity) lastFragment).setBotUser(botUser);
                    return;
                }
                BaseFragment fragment2 = new ChatActivity(args);
                NotificationCenter.getInstance(intentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
                this.actionBarLayout.presentFragment(fragment2, false, true, true, false);
            }
        }
    }

    final /* synthetic */ void lambda$null$9$LaunchActivity(String game, int intentAccount, TL_contacts_resolvedPeer res, DialogsActivity fragment1, ArrayList dids, CharSequence message1, boolean param) {
        long did = ((Long) dids.get(0)).longValue();
        TL_inputMediaGame inputMediaGame = new TL_inputMediaGame();
        inputMediaGame.id = new TL_inputGameShortName();
        inputMediaGame.id.short_name = game;
        inputMediaGame.id.bot_id = MessagesController.getInstance(intentAccount).getInputUser((User) res.users.get(0));
        SendMessagesHelper.getInstance(intentAccount).sendGame(MessagesController.getInstance(intentAccount).getInputPeer((int) did), inputMediaGame, 0, 0);
        Bundle args1 = new Bundle();
        args1.putBoolean("scrollToTopOnResume", true);
        int lower_part = (int) did;
        int high_id = (int) (did >> 32);
        if (lower_part == 0) {
            args1.putInt("enc_id", high_id);
        } else if (high_id == 1) {
            args1.putInt("chat_id", lower_part);
        } else if (lower_part > 0) {
            args1.putInt("user_id", lower_part);
        } else if (lower_part < 0) {
            args1.putInt("chat_id", -lower_part);
        }
        if (MessagesController.getInstance(intentAccount).checkCanOpenChat(args1, fragment1)) {
            NotificationCenter.getInstance(intentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
            this.actionBarLayout.presentFragment(new ChatActivity(args1), true, false, true, false);
        }
    }

    final /* synthetic */ void lambda$null$10$LaunchActivity(int intentAccount, User user, String botChat, DialogsActivity fragment12, ArrayList dids, CharSequence message1, boolean param) {
        long did = ((Long) dids.get(0)).longValue();
        Bundle args12 = new Bundle();
        args12.putBoolean("scrollToTopOnResume", true);
        args12.putInt("chat_id", -((int) did));
        if (mainFragmentsStack.isEmpty() || MessagesController.getInstance(intentAccount).checkCanOpenChat(args12, (BaseFragment) mainFragmentsStack.get(mainFragmentsStack.size() - 1))) {
            NotificationCenter.getInstance(intentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
            MessagesController.getInstance(intentAccount).addUserToChat(-((int) did), user, null, 0, botChat, null);
            this.actionBarLayout.presentFragment(new ChatActivity(args12), true, false, true, false);
        }
    }

    final /* synthetic */ void lambda$runLinkRequest$15$LaunchActivity(AlertDialog progressDialog, int intentAccount, String group, String username, String sticker, String botUser, String botChat, String message, boolean hasUrl, Integer messageId, String game, String[] instantView, HashMap auth, String unsupportedUrl, TLObject response, TL_error error) {
        AndroidUtilities.runOnUIThread(new LaunchActivity$$Lambda$43(this, progressDialog, error, response, intentAccount, group, username, sticker, botUser, botChat, message, hasUrl, messageId, game, instantView, auth, unsupportedUrl));
    }

    final /* synthetic */ void lambda$null$14$LaunchActivity(AlertDialog progressDialog, TL_error error, TLObject response, int intentAccount, String group, String username, String sticker, String botUser, String botChat, String message, boolean hasUrl, Integer messageId, String game, String[] instantView, HashMap auth, String unsupportedUrl) {
        if (!isFinishing()) {
            try {
                progressDialog.dismiss();
            } catch (Throwable e) {
                FileLog.e(e);
            }
            Builder builder;
            if (error != null || this.actionBarLayout == null) {
                builder = new Builder((Context) this);
                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                if (error.text.startsWith("FLOOD_WAIT")) {
                    builder.setMessage(LocaleController.getString("FloodWait", R.string.FloodWait));
                } else {
                    builder.setMessage(LocaleController.getString("JoinToGroupErrorNotExist", R.string.JoinToGroupErrorNotExist));
                }
                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                showAlertDialog(builder);
                return;
            }
            ChatInvite invite = (ChatInvite) response;
            if (invite.chat != null && !ChatObject.isLeftFromChat(invite.chat)) {
                MessagesController.getInstance(intentAccount).putChat(invite.chat, false);
                ArrayList<Chat> chats = new ArrayList();
                chats.add(invite.chat);
                MessagesStorage.getInstance(intentAccount).putUsersAndChats(null, chats, false, true);
                Bundle args = new Bundle();
                args.putInt("chat_id", invite.chat.id);
                if (!mainFragmentsStack.isEmpty()) {
                    if (!MessagesController.getInstance(intentAccount).checkCanOpenChat(args, (BaseFragment) mainFragmentsStack.get(mainFragmentsStack.size() - 1))) {
                        return;
                    }
                }
                ChatActivity fragment = new ChatActivity(args);
                NotificationCenter.getInstance(intentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
                this.actionBarLayout.presentFragment(fragment, false, true, true, false);
            } else if (((invite.chat != null || (invite.channel && !invite.megagroup)) && (invite.chat == null || (ChatObject.isChannel(invite.chat) && !invite.chat.megagroup))) || mainFragmentsStack.isEmpty()) {
                builder = new Builder((Context) this);
                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                String str = "ChannelJoinTo";
                Object[] objArr = new Object[1];
                objArr[0] = invite.chat != null ? invite.chat.title : invite.title;
                builder.setMessage(LocaleController.formatString(str, R.string.ChannelJoinTo, objArr));
                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new LaunchActivity$$Lambda$44(this, intentAccount, username, group, sticker, botUser, botChat, message, hasUrl, messageId, game, instantView, auth, unsupportedUrl));
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                showAlertDialog(builder);
            } else {
                BaseFragment fragment2 = (BaseFragment) mainFragmentsStack.get(mainFragmentsStack.size() - 1);
                fragment2.showDialog(new JoinGroupAlert(this, invite, group, fragment2));
            }
        }
    }

    final /* synthetic */ void lambda$null$13$LaunchActivity(int intentAccount, String username, String group, String sticker, String botUser, String botChat, String message, boolean hasUrl, Integer messageId, String game, String[] instantView, HashMap auth, String unsupportedUrl, DialogInterface dialogInterface, int i) {
        runLinkRequest(intentAccount, username, group, sticker, botUser, botChat, message, hasUrl, messageId, game, instantView, auth, unsupportedUrl, 1);
    }

    final /* synthetic */ void lambda$runLinkRequest$17$LaunchActivity(int intentAccount, AlertDialog progressDialog, TLObject response, TL_error error) {
        if (error == null) {
            MessagesController.getInstance(intentAccount).processUpdates((Updates) response, false);
        }
        AndroidUtilities.runOnUIThread(new LaunchActivity$$Lambda$42(this, progressDialog, error, response, intentAccount));
    }

    final /* synthetic */ void lambda$null$16$LaunchActivity(AlertDialog progressDialog, TL_error error, TLObject response, int intentAccount) {
        if (!isFinishing()) {
            try {
                progressDialog.dismiss();
            } catch (Throwable e) {
                FileLog.e(e);
            }
            if (error != null) {
                Builder builder = new Builder((Context) this);
                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                if (error.text.startsWith("FLOOD_WAIT")) {
                    builder.setMessage(LocaleController.getString("FloodWait", R.string.FloodWait));
                } else if (error.text.equals("USERS_TOO_MUCH")) {
                    builder.setMessage(LocaleController.getString("JoinToGroupErrorFull", R.string.JoinToGroupErrorFull));
                } else {
                    builder.setMessage(LocaleController.getString("JoinToGroupErrorNotExist", R.string.JoinToGroupErrorNotExist));
                }
                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                showAlertDialog(builder);
            } else if (this.actionBarLayout != null) {
                Updates updates = (Updates) response;
                if (!updates.chats.isEmpty()) {
                    Chat chat = (Chat) updates.chats.get(0);
                    chat.left = false;
                    chat.kicked = false;
                    MessagesController.getInstance(intentAccount).putUsers(updates.users, false);
                    MessagesController.getInstance(intentAccount).putChats(updates.chats, false);
                    Bundle args = new Bundle();
                    args.putInt("chat_id", chat.id);
                    if (mainFragmentsStack.isEmpty() || MessagesController.getInstance(intentAccount).checkCanOpenChat(args, (BaseFragment) mainFragmentsStack.get(mainFragmentsStack.size() - 1))) {
                        ChatActivity fragment = new ChatActivity(args);
                        NotificationCenter.getInstance(intentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
                        this.actionBarLayout.presentFragment(fragment, false, true, true, false);
                    }
                }
            }
        }
    }

    final /* synthetic */ void lambda$runLinkRequest$18$LaunchActivity(boolean hasUrl, int intentAccount, String message, DialogsActivity fragment13, ArrayList dids, CharSequence m, boolean param) {
        long did = ((Long) dids.get(0)).longValue();
        Bundle args13 = new Bundle();
        args13.putBoolean("scrollToTopOnResume", true);
        args13.putBoolean("hasUrl", hasUrl);
        int lower_part = (int) did;
        int high_id = (int) (did >> 32);
        if (lower_part == 0) {
            args13.putInt("enc_id", high_id);
        } else if (high_id == 1) {
            args13.putInt("chat_id", lower_part);
        } else if (lower_part > 0) {
            args13.putInt("user_id", lower_part);
        } else if (lower_part < 0) {
            args13.putInt("chat_id", -lower_part);
        }
        if (MessagesController.getInstance(intentAccount).checkCanOpenChat(args13, fragment13)) {
            NotificationCenter.getInstance(intentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
            DataQuery.getInstance(intentAccount).saveDraft(did, message, null, null, false);
            this.actionBarLayout.presentFragment(new ChatActivity(args13), true, false, true, false);
        }
    }

    final /* synthetic */ void lambda$runLinkRequest$22$LaunchActivity(int[] requestId, int intentAccount, AlertDialog progressDialog, TL_account_getAuthorizationForm req, String payload, String nonce, String callbackUrl, TLObject response, TL_error error) {
        TL_account_authorizationForm authorizationForm = (TL_account_authorizationForm) response;
        if (authorizationForm != null) {
            requestId[0] = ConnectionsManager.getInstance(intentAccount).sendRequest(new TL_account_getPassword(), new LaunchActivity$$Lambda$39(this, progressDialog, intentAccount, authorizationForm, req, payload, nonce, callbackUrl));
            return;
        }
        AndroidUtilities.runOnUIThread(new LaunchActivity$$Lambda$40(this, progressDialog, error));
    }

    final /* synthetic */ void lambda$null$20$LaunchActivity(AlertDialog progressDialog, int intentAccount, TL_account_authorizationForm authorizationForm, TL_account_getAuthorizationForm req, String payload, String nonce, String callbackUrl, TLObject response1, TL_error error1) {
        AndroidUtilities.runOnUIThread(new LaunchActivity$$Lambda$41(this, progressDialog, response1, intentAccount, authorizationForm, req, payload, nonce, callbackUrl));
    }

    final /* synthetic */ void lambda$null$19$LaunchActivity(AlertDialog progressDialog, TLObject response1, int intentAccount, TL_account_authorizationForm authorizationForm, TL_account_getAuthorizationForm req, String payload, String nonce, String callbackUrl) {
        try {
            progressDialog.dismiss();
        } catch (Throwable e) {
            FileLog.e(e);
        }
        if (response1 != null) {
            TL_account_password accountPassword = (TL_account_password) response1;
            MessagesController.getInstance(intentAccount).putUsers(authorizationForm.users, false);
            presentFragment(new PassportActivity(5, req.bot_id, req.scope, req.public_key, payload, nonce, callbackUrl, authorizationForm, accountPassword));
        }
    }

    final /* synthetic */ void lambda$null$21$LaunchActivity(AlertDialog progressDialog, TL_error error) {
        try {
            progressDialog.dismiss();
            if ("APP_VERSION_OUTDATED".equals(error.text)) {
                AlertsCreator.showUpdateAppAlert(this, LocaleController.getString("UpdateAppAlert", R.string.UpdateAppAlert), true);
            } else {
                showAlertDialog(AlertsCreator.createSimpleAlert(this, LocaleController.getString("ErrorOccurred", R.string.ErrorOccurred) + "\n" + error.text));
            }
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }

    final /* synthetic */ void lambda$runLinkRequest$24$LaunchActivity(AlertDialog progressDialog, TLObject response, TL_error error) {
        AndroidUtilities.runOnUIThread(new LaunchActivity$$Lambda$38(this, progressDialog, response));
    }

    final /* synthetic */ void lambda$null$23$LaunchActivity(AlertDialog progressDialog, TLObject response) {
        try {
            progressDialog.dismiss();
        } catch (Throwable e) {
            FileLog.e(e);
        }
        if (response instanceof TL_help_deepLinkInfo) {
            TL_help_deepLinkInfo res = (TL_help_deepLinkInfo) response;
            AlertsCreator.showUpdateAppAlert(this, res.message, res.update_app);
        }
    }

    static final /* synthetic */ void lambda$runLinkRequest$25$LaunchActivity(int intentAccount, int[] requestId, DialogInterface dialog, int which) {
        ConnectionsManager.getInstance(intentAccount).cancelRequest(requestId[0], true);
        try {
            dialog.dismiss();
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }

    public void checkAppUpdate(boolean force) {
        if (!force && BuildVars.DEBUG_VERSION) {
            return;
        }
        if (!force && !BuildVars.CHECK_UPDATES) {
            return;
        }
        if (force || Math.abs(System.currentTimeMillis() - UserConfig.getInstance(0).lastUpdateCheckTime) >= 86400000) {
            TL_help_getAppUpdate req = new TL_help_getAppUpdate();
            try {
                req.source = ApplicationLoader.applicationContext.getPackageManager().getInstallerPackageName(ApplicationLoader.applicationContext.getPackageName());
            } catch (Exception e) {
            }
            if (req.source == null) {
                req.source = TtmlNode.ANONYMOUS_REGION_ID;
            }
            ConnectionsManager.getInstance(this.currentAccount).sendRequest(req, new LaunchActivity$$Lambda$15(this, this.currentAccount));
        }
    }

    final /* synthetic */ void lambda$checkAppUpdate$27$LaunchActivity(int accountNum, TLObject response, TL_error error) {
        UserConfig.getInstance(0).lastUpdateCheckTime = System.currentTimeMillis();
        UserConfig.getInstance(0).saveConfig(false);
        if (response instanceof TL_help_appUpdate) {
            AndroidUtilities.runOnUIThread(new LaunchActivity$$Lambda$37(this, (TL_help_appUpdate) response, accountNum));
        }
    }

    final /* synthetic */ void lambda$null$26$LaunchActivity(TL_help_appUpdate res, int accountNum) {
        if (BuildVars.DEBUG_PRIVATE_VERSION) {
            res.popup = Utilities.random.nextBoolean();
        }
        if (res.popup) {
            UserConfig.getInstance(0).pendingAppUpdate = res;
            UserConfig.getInstance(0).pendingAppUpdateBuildVersion = BuildVars.BUILD_VERSION;
            try {
                PackageInfo packageInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
                UserConfig.getInstance(0).pendingAppUpdateInstallTime = Math.max(packageInfo.lastUpdateTime, packageInfo.firstInstallTime);
            } catch (Throwable e) {
                FileLog.e(e);
                UserConfig.getInstance(0).pendingAppUpdateInstallTime = 0;
            }
            UserConfig.getInstance(0).saveConfig(false);
            showUpdateActivity(accountNum, res);
            return;
        }
        new UpdateAppAlertDialog(this, res, accountNum).show();
    }

    public AlertDialog showAlertDialog(Builder builder) {
        AlertDialog alertDialog = null;
        try {
            if (this.visibleDialog != null) {
                this.visibleDialog.dismiss();
                this.visibleDialog = null;
            }
        } catch (Throwable e) {
            FileLog.e(e);
        }
        try {
            this.visibleDialog = builder.show();
            this.visibleDialog.setCanceledOnTouchOutside(true);
            this.visibleDialog.setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    if (LaunchActivity.this.visibleDialog != null) {
                        if (LaunchActivity.this.visibleDialog == LaunchActivity.this.localeDialog) {
                            try {
                                Toast.makeText(LaunchActivity.this, LaunchActivity.this.getStringForLanguageAlert(LocaleController.getInstance().getCurrentLocaleInfo().shortName.equals("en") ? LaunchActivity.this.englishLocaleStrings : LaunchActivity.this.systemLocaleStrings, "ChangeLanguageLater", R.string.ChangeLanguageLater), 1).show();
                            } catch (Throwable e) {
                                FileLog.e(e);
                            }
                            LaunchActivity.this.localeDialog = null;
                        } else if (LaunchActivity.this.visibleDialog == LaunchActivity.this.proxyErrorDialog) {
                            SharedPreferences preferences = MessagesController.getGlobalMainSettings();
                            Editor editor = MessagesController.getGlobalMainSettings().edit();
                            editor.putBoolean("proxy_enabled", false);
                            editor.putBoolean("proxy_enabled_calls", false);
                            editor.commit();
                            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.proxySettingsChanged);
                            ConnectionsManager.setProxySettings(false, TtmlNode.ANONYMOUS_REGION_ID, 1080, TtmlNode.ANONYMOUS_REGION_ID, TtmlNode.ANONYMOUS_REGION_ID, TtmlNode.ANONYMOUS_REGION_ID);
                            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.proxySettingsChanged, new Object[0]);
                            LaunchActivity.this.proxyErrorDialog = null;
                        }
                    }
                    LaunchActivity.this.visibleDialog = null;
                }
            });
            return this.visibleDialog;
        } catch (Throwable e2) {
            FileLog.e(e2);
            return alertDialog;
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent, true, false, false);
    }

    public void didSelectDialogs(DialogsActivity dialogsFragment, ArrayList<Long> dids, CharSequence message, boolean param) {
        long did = ((Long) dids.get(0)).longValue();
        int lower_part = (int) did;
        int high_id = (int) (did >> 32);
        Bundle args = new Bundle();
        int account = dialogsFragment != null ? dialogsFragment.getCurrentAccount() : this.currentAccount;
        args.putBoolean("scrollToTopOnResume", true);
        if (!AndroidUtilities.isTablet()) {
            NotificationCenter.getInstance(account).postNotificationName(NotificationCenter.closeChats, new Object[0]);
        }
        if (lower_part == 0) {
            args.putInt("enc_id", high_id);
        } else if (high_id == 1) {
            args.putInt("chat_id", lower_part);
        } else if (lower_part > 0) {
            args.putInt("user_id", lower_part);
        } else if (lower_part < 0) {
            args.putInt("chat_id", -lower_part);
        }
        if (MessagesController.getInstance(account).checkCanOpenChat(args, dialogsFragment)) {
            BaseFragment fragment = new ChatActivity(args);
            if (this.contactsToSend == null || this.contactsToSend.size() != 1) {
                this.actionBarLayout.presentFragment(fragment, dialogsFragment != null, dialogsFragment == null, true, false);
                if (this.videoPath != null) {
                    fragment.openVideoEditor(this.videoPath, this.sendingText);
                    this.sendingText = null;
                }
                if (this.photoPathsArray != null) {
                    if (this.sendingText != null && this.sendingText.length() <= Callback.DEFAULT_DRAG_ANIMATION_DURATION && this.photoPathsArray.size() == 1) {
                        ((SendingMediaInfo) this.photoPathsArray.get(0)).caption = this.sendingText;
                        this.sendingText = null;
                    }
                    SendMessagesHelper.prepareSendingMedia(this.photoPathsArray, did, null, null, false, false, null);
                }
                if (this.sendingText != null) {
                    SendMessagesHelper.prepareSendingText(this.sendingText, did);
                }
                if (!(this.documentsPathsArray == null && this.documentsUrisArray == null)) {
                    SendMessagesHelper.prepareSendingDocuments(this.documentsPathsArray, this.documentsOriginalPathsArray, this.documentsUrisArray, this.documentsMimeType, did, null, null, null);
                }
                if (!(this.contactsToSend == null || this.contactsToSend.isEmpty())) {
                    for (int a = 0; a < this.contactsToSend.size(); a++) {
                        SendMessagesHelper.getInstance(account).sendMessage((User) this.contactsToSend.get(a), did, null, null, null);
                    }
                }
            } else if (this.contactsToSend.size() == 1) {
                boolean z;
                boolean z2;
                PhonebookShareActivity contactFragment = new PhonebookShareActivity(null, this.contactsToSendUri, null, null);
                contactFragment.setDelegate(new LaunchActivity$$Lambda$16(this, fragment, account, did));
                ActionBarLayout actionBarLayout = this.actionBarLayout;
                if (dialogsFragment != null) {
                    z = true;
                } else {
                    z = false;
                }
                if (dialogsFragment == null) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                actionBarLayout.presentFragment(contactFragment, z, z2, true, false);
            }
            this.photoPathsArray = null;
            this.videoPath = null;
            this.sendingText = null;
            this.documentsPathsArray = null;
            this.documentsOriginalPathsArray = null;
            this.contactsToSend = null;
            this.contactsToSendUri = null;
        }
    }

    final /* synthetic */ void lambda$didSelectDialogs$28$LaunchActivity(ChatActivity fragment, int account, long did, User user) {
        this.actionBarLayout.presentFragment(fragment, true, false, true, false);
        SendMessagesHelper.getInstance(account).sendMessage(user, did, null, null, null);
    }

    private void onFinish() {
        if (!this.finished) {
            this.finished = true;
            if (this.lockRunnable != null) {
                AndroidUtilities.cancelRunOnUIThread(this.lockRunnable);
                this.lockRunnable = null;
            }
            if (this.currentAccount != -1) {
                NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.appDidLogout);
                NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.mainUserInfoChanged);
                NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.didUpdatedConnectionState);
                NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.needShowAlert);
                NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.wasUnableToFindCurrentLocation);
                NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.openArticle);
                NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.hasNewContactsToImport);
            }
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.needShowAlert);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didSetNewWallpapper);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.suggestedLangpack);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.reloadInterface);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didSetNewTheme);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.needSetDayNightTheme);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.closeOtherAppActivities);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didSetPasscode);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.notificationsCountUpdated);
        }
    }

    public void presentFragment(BaseFragment fragment) {
        this.actionBarLayout.presentFragment(fragment);
    }

    public boolean presentFragment(BaseFragment fragment, boolean removeLast, boolean forceWithoutAnimation) {
        return this.actionBarLayout.presentFragment(fragment, removeLast, forceWithoutAnimation, true, false);
    }

    public ActionBarLayout getActionBarLayout() {
        return this.actionBarLayout;
    }

    public ActionBarLayout getLayersActionBarLayout() {
        return this.layersActionBarLayout;
    }

    public ActionBarLayout getRightActionBarLayout() {
        return this.rightActionBarLayout;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!(SharedConfig.passcodeHash.length() == 0 || SharedConfig.lastPauseTime == 0)) {
            SharedConfig.lastPauseTime = 0;
            UserConfig.getInstance(this.currentAccount).saveConfig(false);
        }
        super.onActivityResult(requestCode, resultCode, data);
        ThemeEditorView editorView = ThemeEditorView.getInstance();
        if (editorView != null) {
            editorView.onActivityResult(requestCode, resultCode, data);
        }
        if (this.actionBarLayout.fragmentsStack.size() != 0) {
            ((BaseFragment) this.actionBarLayout.fragmentsStack.get(this.actionBarLayout.fragmentsStack.size() - 1)).onActivityResultFragment(requestCode, resultCode, data);
        }
        if (AndroidUtilities.isTablet()) {
            if (this.rightActionBarLayout.fragmentsStack.size() != 0) {
                ((BaseFragment) this.rightActionBarLayout.fragmentsStack.get(this.rightActionBarLayout.fragmentsStack.size() - 1)).onActivityResultFragment(requestCode, resultCode, data);
            }
            if (this.layersActionBarLayout.fragmentsStack.size() != 0) {
                ((BaseFragment) this.layersActionBarLayout.fragmentsStack.get(this.layersActionBarLayout.fragmentsStack.size() - 1)).onActivityResultFragment(requestCode, resultCode, data);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 3 || requestCode == 4 || requestCode == 5 || requestCode == 19 || requestCode == 20 || requestCode == 22) {
            boolean showAlert = true;
            if (grantResults.length > 0 && grantResults[0] == 0) {
                if (requestCode == 4) {
                    ImageLoader.getInstance().checkMediaPaths();
                    return;
                } else if (requestCode == 5) {
                    ContactsController.getInstance(this.currentAccount).forceImportContacts();
                    return;
                } else if (requestCode == 3) {
                    if (SharedConfig.inappCamera) {
                        CameraController.getInstance().initCamera(null);
                        return;
                    }
                    return;
                } else if (requestCode == 19 || requestCode == 20 || requestCode == 22) {
                    showAlert = false;
                }
            }
            if (showAlert) {
                Builder builder = new Builder((Context) this);
                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                if (requestCode == 3) {
                    builder.setMessage(LocaleController.getString("PermissionNoAudio", R.string.PermissionNoAudio));
                } else if (requestCode == 4) {
                    builder.setMessage(LocaleController.getString("PermissionStorage", R.string.PermissionStorage));
                } else if (requestCode == 5) {
                    builder.setMessage(LocaleController.getString("PermissionContacts", R.string.PermissionContacts));
                } else if (requestCode == 19 || requestCode == 20 || requestCode == 22) {
                    builder.setMessage(LocaleController.getString("PermissionNoCamera", R.string.PermissionNoCamera));
                }
                builder.setNegativeButton(LocaleController.getString("PermissionOpenSettings", R.string.PermissionOpenSettings), new LaunchActivity$$Lambda$17(this));
                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                builder.show();
                return;
            }
        } else if (requestCode == 2 && grantResults.length > 0 && grantResults[0] == 0) {
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.locationPermissionGranted, new Object[0]);
        }
        if (this.actionBarLayout.fragmentsStack.size() != 0) {
            ((BaseFragment) this.actionBarLayout.fragmentsStack.get(this.actionBarLayout.fragmentsStack.size() - 1)).onRequestPermissionsResultFragment(requestCode, permissions, grantResults);
        }
        if (AndroidUtilities.isTablet()) {
            if (this.rightActionBarLayout.fragmentsStack.size() != 0) {
                ((BaseFragment) this.rightActionBarLayout.fragmentsStack.get(this.rightActionBarLayout.fragmentsStack.size() - 1)).onRequestPermissionsResultFragment(requestCode, permissions, grantResults);
            }
            if (this.layersActionBarLayout.fragmentsStack.size() != 0) {
                ((BaseFragment) this.layersActionBarLayout.fragmentsStack.get(this.layersActionBarLayout.fragmentsStack.size() - 1)).onRequestPermissionsResultFragment(requestCode, permissions, grantResults);
            }
        }
    }

    final /* synthetic */ void lambda$onRequestPermissionsResult$29$LaunchActivity(DialogInterface dialog, int which) {
        try {
            Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.parse("package:" + ApplicationLoader.applicationContext.getPackageName()));
            startActivity(intent);
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }

    protected void onPause() {
        super.onPause();
        SharedConfig.lastAppPauseTime = System.currentTimeMillis();
        ApplicationLoader.mainInterfacePaused = true;
        Utilities.stageQueue.postRunnable(LaunchActivity$$Lambda$18.$instance);
        onPasscodePause();
        this.actionBarLayout.onPause();
        if (AndroidUtilities.isTablet()) {
            this.rightActionBarLayout.onPause();
            this.layersActionBarLayout.onPause();
        }
        if (this.passcodeView != null) {
            this.passcodeView.onPause();
        }
        ConnectionsManager.getInstance(this.currentAccount).setAppPaused(true, false);
        AndroidUtilities.unregisterUpdates();
        if (PhotoViewer.hasInstance() && PhotoViewer.getInstance().isVisible()) {
            PhotoViewer.getInstance().onPause();
        }
    }

    static final /* synthetic */ void lambda$onPause$30$LaunchActivity() {
        ApplicationLoader.mainInterfacePausedStageQueue = true;
        ApplicationLoader.mainInterfacePausedStageQueueTime = 0;
    }

    protected void onStart() {
        super.onStart();
        Browser.bindCustomTabsService(this);
    }

    protected void onStop() {
        super.onStop();
        Browser.unbindCustomTabsService(this);
    }

    protected void onDestroy() {
        if (PhotoViewer.getPipInstance() != null) {
            PhotoViewer.getPipInstance().destroyPhotoViewer();
        }
        if (PhotoViewer.hasInstance()) {
            PhotoViewer.getInstance().destroyPhotoViewer();
        }
        if (SecretMediaViewer.hasInstance()) {
            SecretMediaViewer.getInstance().destroyPhotoViewer();
        }
        if (ArticleViewer.hasInstance()) {
            ArticleViewer.getInstance().destroyArticleViewer();
        }
        if (StickerPreviewViewer.hasInstance()) {
            StickerPreviewViewer.getInstance().destroy();
        }
        PipRoundVideoView pipRoundVideoView = PipRoundVideoView.getInstance();
        MediaController.getInstance().setBaseActivity(this, false);
        MediaController.getInstance().setFeedbackView(this.actionBarLayout, false);
        if (pipRoundVideoView != null) {
            pipRoundVideoView.close(false);
        }
        Theme.destroyResources();
        EmbedBottomSheet embedBottomSheet = EmbedBottomSheet.getInstance();
        if (embedBottomSheet != null) {
            embedBottomSheet.destroy();
        }
        ThemeEditorView editorView = ThemeEditorView.getInstance();
        if (editorView != null) {
            editorView.destroy();
        }
        try {
            if (this.visibleDialog != null) {
                this.visibleDialog.dismiss();
                this.visibleDialog = null;
            }
        } catch (Throwable e) {
            FileLog.e(e);
        }
        try {
            if (this.onGlobalLayoutListener != null) {
                getWindow().getDecorView().getRootView().getViewTreeObserver().removeOnGlobalLayoutListener(this.onGlobalLayoutListener);
            }
        } catch (Throwable e2) {
            FileLog.e(e2);
        }
        super.onDestroy();
        onFinish();
    }

    protected void onResume() {
        super.onResume();
        MediaController.getInstance().setFeedbackView(this.actionBarLayout, true);
        showLanguageAlert(false);
        ApplicationLoader.mainInterfacePaused = false;
        NotificationsController.lastNoDataNotificationTime = 0;
        Utilities.stageQueue.postRunnable(LaunchActivity$$Lambda$19.$instance);
        checkFreeDiscSpace();
        MediaController.checkGallery();
        onPasscodeResume();
        if (this.passcodeView.getVisibility() != 0) {
            this.actionBarLayout.onResume();
            if (AndroidUtilities.isTablet()) {
                this.rightActionBarLayout.onResume();
                this.layersActionBarLayout.onResume();
            }
        } else {
            this.actionBarLayout.dismissDialogs();
            if (AndroidUtilities.isTablet()) {
                this.rightActionBarLayout.dismissDialogs();
                this.layersActionBarLayout.dismissDialogs();
            }
            this.passcodeView.onResume();
        }
        AndroidUtilities.checkForCrashes(this);
        AndroidUtilities.checkForUpdates(this);
        ConnectionsManager.getInstance(this.currentAccount).setAppPaused(false, false);
        updateCurrentConnectionState(this.currentAccount);
        if (PhotoViewer.hasInstance() && PhotoViewer.getInstance().isVisible()) {
            PhotoViewer.getInstance().onResume();
        }
        if (PipRoundVideoView.getInstance() != null && MediaController.getInstance().isMessagePaused()) {
            MessageObject messageObject = MediaController.getInstance().getPlayingMessageObject();
            if (messageObject != null) {
                MediaController.getInstance().seekToProgress(messageObject, messageObject.audioProgress);
            }
        }
        if (UserConfig.getInstance(UserConfig.selectedAccount).unacceptedTermsOfService != null) {
            showTosActivity(UserConfig.selectedAccount, UserConfig.getInstance(UserConfig.selectedAccount).unacceptedTermsOfService);
        } else if (UserConfig.getInstance(0).pendingAppUpdate != null) {
            showUpdateActivity(UserConfig.selectedAccount, UserConfig.getInstance(0).pendingAppUpdate);
        }
        checkAppUpdate(false);
    }

    static final /* synthetic */ void lambda$onResume$31$LaunchActivity() {
        ApplicationLoader.mainInterfacePausedStageQueue = false;
        ApplicationLoader.mainInterfacePausedStageQueueTime = System.currentTimeMillis();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        AndroidUtilities.checkDisplaySize(this, newConfig);
        super.onConfigurationChanged(newConfig);
        checkLayout();
        PipRoundVideoView pipRoundVideoView = PipRoundVideoView.getInstance();
        if (pipRoundVideoView != null) {
            pipRoundVideoView.onConfigurationChanged();
        }
        EmbedBottomSheet embedBottomSheet = EmbedBottomSheet.getInstance();
        if (embedBottomSheet != null) {
            embedBottomSheet.onConfigurationChanged(newConfig);
        }
        PhotoViewer photoViewer = PhotoViewer.getPipInstance();
        if (photoViewer != null) {
            photoViewer.onConfigurationChanged(newConfig);
        }
        ThemeEditorView editorView = ThemeEditorView.getInstance();
        if (editorView != null) {
            editorView.onConfigurationChanged();
        }
    }

    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        AndroidUtilities.isInMultiwindow = isInMultiWindowMode;
        checkLayout();
    }

    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.appDidLogout) {
            switchToAvailableAccountOrLogout();
        } else if (id == NotificationCenter.closeOtherAppActivities) {
            if (args[0] != this) {
                onFinish();
                finish();
            }
        } else if (id == NotificationCenter.didUpdatedConnectionState) {
            int state = ConnectionsManager.getInstance(account).getConnectionState();
            if (this.currentConnectionState != state) {
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("switch to state " + state);
                }
                this.currentConnectionState = state;
                updateCurrentConnectionState(account);
            }
        } else if (id == NotificationCenter.mainUserInfoChanged) {
            this.drawerLayoutAdapter.notifyDataSetChanged();
        } else if (id == NotificationCenter.needShowAlert) {
            Integer reason = args[0];
            if (reason.intValue() == 3 && this.proxyErrorDialog != null) {
                return;
            }
            if (reason.intValue() == 4) {
                showTosActivity(account, (TL_help_termsOfService) args[1]);
                return;
            }
            builder = new Builder((Context) this);
            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
            if (!(reason.intValue() == 2 || reason.intValue() == 3)) {
                builder.setNegativeButton(LocaleController.getString("MoreInfo", R.string.MoreInfo), new LaunchActivity$$Lambda$20(account));
            }
            if (reason.intValue() == 0) {
                builder.setMessage(LocaleController.getString("NobodyLikesSpam1", R.string.NobodyLikesSpam1));
                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
            } else if (reason.intValue() == 1) {
                builder.setMessage(LocaleController.getString("NobodyLikesSpam2", R.string.NobodyLikesSpam2));
                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
            } else if (reason.intValue() == 2) {
                builder.setMessage((String) args[1]);
                if (args[2].startsWith("AUTH_KEY_DROP_")) {
                    builder.setPositiveButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                    builder.setNegativeButton(LocaleController.getString("LogOut", R.string.LogOut), new LaunchActivity$$Lambda$21(this));
                } else {
                    builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                }
            } else if (reason.intValue() == 3) {
                builder.setMessage(LocaleController.getString("UseProxyTelegramError", R.string.UseProxyTelegramError));
                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                this.proxyErrorDialog = showAlertDialog(builder);
                return;
            }
            if (!mainFragmentsStack.isEmpty()) {
                ((BaseFragment) mainFragmentsStack.get(mainFragmentsStack.size() - 1)).showDialog(builder.create());
            }
        } else if (id == NotificationCenter.wasUnableToFindCurrentLocation) {
            HashMap<String, MessageObject> waitingForLocation = args[0];
            builder = new Builder((Context) this);
            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
            builder.setNegativeButton(LocaleController.getString("ShareYouLocationUnableManually", R.string.ShareYouLocationUnableManually), new LaunchActivity$$Lambda$22(this, waitingForLocation, account));
            builder.setMessage(LocaleController.getString("ShareYouLocationUnable", R.string.ShareYouLocationUnable));
            if (!mainFragmentsStack.isEmpty()) {
                ((BaseFragment) mainFragmentsStack.get(mainFragmentsStack.size() - 1)).showDialog(builder.create());
            }
        } else if (id == NotificationCenter.didSetNewWallpapper) {
            if (this.sideMenu != null) {
                child = this.sideMenu.getChildAt(0);
                if (child != null) {
                    child.invalidate();
                }
            }
        } else if (id == NotificationCenter.didSetPasscode) {
            if (SharedConfig.passcodeHash.length() <= 0 || SharedConfig.allowScreenCapture) {
                try {
                    getWindow().clearFlags(MessagesController.UPDATE_MASK_CHANNEL);
                    return;
                } catch (Throwable e) {
                    FileLog.e(e);
                    return;
                }
            }
            try {
                getWindow().setFlags(MessagesController.UPDATE_MASK_CHANNEL, MessagesController.UPDATE_MASK_CHANNEL);
            } catch (Throwable e2) {
                FileLog.e(e2);
            }
        } else if (id == NotificationCenter.reloadInterface) {
            rebuildAllFragments(false);
        } else if (id == NotificationCenter.suggestedLangpack) {
            showLanguageAlert(false);
        } else if (id == NotificationCenter.openArticle) {
            if (!mainFragmentsStack.isEmpty()) {
                ArticleViewer.getInstance().setParentActivity(this, (BaseFragment) mainFragmentsStack.get(mainFragmentsStack.size() - 1));
                ArticleViewer.getInstance().open((TL_webPage) args[0], (String) args[1]);
            }
        } else if (id == NotificationCenter.hasNewContactsToImport) {
            if (this.actionBarLayout != null && !this.actionBarLayout.fragmentsStack.isEmpty()) {
                int type = ((Integer) args[0]).intValue();
                HashMap<String, Contact> contactHashMap = args[1];
                boolean first = ((Boolean) args[2]).booleanValue();
                boolean schedule = ((Boolean) args[3]).booleanValue();
                BaseFragment fragment = (BaseFragment) this.actionBarLayout.fragmentsStack.get(this.actionBarLayout.fragmentsStack.size() - 1);
                builder = new Builder((Context) this);
                builder.setTitle(LocaleController.getString("UpdateContactsTitle", R.string.UpdateContactsTitle));
                builder.setMessage(LocaleController.getString("UpdateContactsMessage", R.string.UpdateContactsMessage));
                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new LaunchActivity$$Lambda$23(account, contactHashMap, first, schedule));
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), new LaunchActivity$$Lambda$24(account, contactHashMap, first, schedule));
                builder.setOnBackButtonListener(new LaunchActivity$$Lambda$25(account, contactHashMap, first, schedule));
                AlertDialog dialog = builder.create();
                fragment.showDialog(dialog);
                dialog.setCanceledOnTouchOutside(false);
            }
        } else if (id == NotificationCenter.didSetNewTheme) {
            if (!args[0].booleanValue()) {
                if (this.sideMenu != null) {
                    this.sideMenu.setBackgroundColor(Theme.getColor(Theme.key_chats_menuBackground));
                    this.sideMenu.setGlowColor(Theme.getColor(Theme.key_chats_menuBackground));
                    this.sideMenu.getAdapter().notifyDataSetChanged();
                }
                if (VERSION.SDK_INT >= 21) {
                    try {
                        setTaskDescription(new TaskDescription(null, null, Theme.getColor(Theme.key_actionBarDefault) | Theme.ACTION_BAR_VIDEO_EDIT_COLOR));
                    } catch (Exception e3) {
                    }
                }
            }
        } else if (id == NotificationCenter.needSetDayNightTheme) {
            ThemeInfo theme = args[0];
            this.actionBarLayout.animateThemedValues(theme);
            if (AndroidUtilities.isTablet()) {
                this.layersActionBarLayout.animateThemedValues(theme);
                this.rightActionBarLayout.animateThemedValues(theme);
            }
        } else if (id == NotificationCenter.notificationsCountUpdated && this.sideMenu != null) {
            Integer accountNum = args[0];
            int count = this.sideMenu.getChildCount();
            for (int a = 0; a < count; a++) {
                child = this.sideMenu.getChildAt(a);
                if ((child instanceof DrawerUserCell) && ((DrawerUserCell) child).getAccountNumber() == accountNum.intValue()) {
                    child.invalidate();
                    return;
                }
            }
        }
    }

    static final /* synthetic */ void lambda$didReceivedNotification$32$LaunchActivity(int account, DialogInterface dialogInterface, int i) {
        if (!mainFragmentsStack.isEmpty()) {
            MessagesController.getInstance(account).openByUserName("spambot", (BaseFragment) mainFragmentsStack.get(mainFragmentsStack.size() - 1), 1);
        }
    }

    final /* synthetic */ void lambda$didReceivedNotification$33$LaunchActivity(DialogInterface dialog, int which) {
        MessagesController.getInstance(this.currentAccount).performLogout(2);
    }

    final /* synthetic */ void lambda$didReceivedNotification$35$LaunchActivity(HashMap waitingForLocation, int account, DialogInterface dialogInterface, int i) {
        if (!mainFragmentsStack.isEmpty() && AndroidUtilities.isGoogleMapsInstalled((BaseFragment) mainFragmentsStack.get(mainFragmentsStack.size() - 1))) {
            LocationActivity fragment = new LocationActivity(0);
            fragment.setDelegate(new LaunchActivity$$Lambda$36(waitingForLocation, account));
            presentFragment(fragment);
        }
    }

    static final /* synthetic */ void lambda$null$34$LaunchActivity(HashMap waitingForLocation, int account, MessageMedia location, int live) {
        for (Entry<String, MessageObject> entry : waitingForLocation.entrySet()) {
            MessageObject messageObject = (MessageObject) entry.getValue();
            SendMessagesHelper.getInstance(account).sendMessage(location, messageObject.getDialogId(), messageObject, null, null);
        }
    }

    private String getStringForLanguageAlert(HashMap<String, String> map, String key, int intKey) {
        String value = (String) map.get(key);
        if (value == null) {
            return LocaleController.getString(key, intKey);
        }
        return value;
    }

    private void checkFreeDiscSpace() {
        if (VERSION.SDK_INT < 26) {
            Utilities.globalQueue.postRunnable(new LaunchActivity$$Lambda$26(this), AdaptiveTrackSelection.DEFAULT_MIN_TIME_BETWEEN_BUFFER_REEVALUTATION_MS);
        }
    }

    final /* synthetic */ void lambda$checkFreeDiscSpace$40$LaunchActivity() {
        if (UserConfig.getInstance(this.currentAccount).isClientActivated()) {
            try {
                SharedPreferences preferences = MessagesController.getGlobalMainSettings();
                if (Math.abs(preferences.getLong("last_space_check", 0) - System.currentTimeMillis()) >= 259200000) {
                    File path = FileLoader.getDirectory(4);
                    if (path != null) {
                        long freeSpace;
                        StatFs statFs = new StatFs(path.getAbsolutePath());
                        if (VERSION.SDK_INT < 18) {
                            freeSpace = (long) Math.abs(statFs.getAvailableBlocks() * statFs.getBlockSize());
                        } else {
                            freeSpace = statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong();
                        }
                        preferences.edit().putLong("last_space_check", System.currentTimeMillis()).commit();
                        if (freeSpace < 104857600) {
                            AndroidUtilities.runOnUIThread(new LaunchActivity$$Lambda$35(this));
                        }
                    }
                }
            } catch (Throwable th) {
            }
        }
    }

    final /* synthetic */ void lambda$null$39$LaunchActivity() {
        try {
            AlertsCreator.createFreeSpaceDialog(this).show();
        } catch (Throwable th) {
        }
    }

    private void showLanguageAlertInternal(LocaleInfo systemInfo, LocaleInfo englishInfo, String systemLang) {
        try {
            LocaleInfo localeInfo;
            this.loadingLocaleDialog = false;
            boolean firstSystem = systemInfo.builtIn || LocaleController.getInstance().isCurrentLocalLocale();
            Builder builder = new Builder((Context) this);
            builder.setTitle(getStringForLanguageAlert(this.systemLocaleStrings, "ChooseYourLanguage", R.string.ChooseYourLanguage));
            builder.setSubtitle(getStringForLanguageAlert(this.englishLocaleStrings, "ChooseYourLanguage", R.string.ChooseYourLanguage));
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(1);
            LanguageCell[] cells = new LanguageCell[2];
            LocaleInfo[] selectedLanguage = new LocaleInfo[1];
            LocaleInfo[] locales = new LocaleInfo[2];
            String englishName = getStringForLanguageAlert(this.systemLocaleStrings, "English", R.string.English);
            if (firstSystem) {
                localeInfo = systemInfo;
            } else {
                localeInfo = englishInfo;
            }
            locales[0] = localeInfo;
            if (firstSystem) {
                localeInfo = englishInfo;
            } else {
                localeInfo = systemInfo;
            }
            locales[1] = localeInfo;
            if (!firstSystem) {
                systemInfo = englishInfo;
            }
            selectedLanguage[0] = systemInfo;
            int a = 0;
            while (a < 2) {
                cells[a] = new LanguageCell(this, true);
                cells[a].setLanguage(locales[a], locales[a] == englishInfo ? englishName : null, true);
                cells[a].setTag(Integer.valueOf(a));
                cells[a].setBackgroundDrawable(Theme.createSelectorDrawable(Theme.getColor(Theme.key_dialogButtonSelector), 2));
                cells[a].setLanguageSelected(a == 0);
                linearLayout.addView(cells[a], LayoutHelper.createLinear(-1, 48));
                cells[a].setOnClickListener(new LaunchActivity$$Lambda$27(selectedLanguage, cells));
                a++;
            }
            LanguageCell cell = new LanguageCell(this, true);
            cell.setValue(getStringForLanguageAlert(this.systemLocaleStrings, "ChooseYourLanguageOther", R.string.ChooseYourLanguageOther), getStringForLanguageAlert(this.englishLocaleStrings, "ChooseYourLanguageOther", R.string.ChooseYourLanguageOther));
            cell.setOnClickListener(new LaunchActivity$$Lambda$28(this));
            linearLayout.addView(cell, LayoutHelper.createLinear(-1, 48));
            builder.setView(linearLayout);
            builder.setNegativeButton(LocaleController.getString("OK", R.string.OK), new LaunchActivity$$Lambda$29(this, selectedLanguage));
            this.localeDialog = showAlertDialog(builder);
            MessagesController.getGlobalMainSettings().edit().putString("language_showed2", systemLang).commit();
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }

    static final /* synthetic */ void lambda$showLanguageAlertInternal$41$LaunchActivity(LocaleInfo[] selectedLanguage, LanguageCell[] cells, View v) {
        Integer tag = (Integer) v.getTag();
        selectedLanguage[0] = ((LanguageCell) v).getCurrentLocale();
        for (int a1 = 0; a1 < cells.length; a1++) {
            boolean z;
            LanguageCell languageCell = cells[a1];
            if (a1 == tag.intValue()) {
                z = true;
            } else {
                z = false;
            }
            languageCell.setLanguageSelected(z);
        }
    }

    final /* synthetic */ void lambda$showLanguageAlertInternal$42$LaunchActivity(View v) {
        this.localeDialog = null;
        this.drawerLayoutContainer.closeDrawer(true);
        presentFragment(new LanguageSelectActivity());
        if (this.visibleDialog != null) {
            this.visibleDialog.dismiss();
            this.visibleDialog = null;
        }
    }

    final /* synthetic */ void lambda$showLanguageAlertInternal$43$LaunchActivity(LocaleInfo[] selectedLanguage, DialogInterface dialog, int which) {
        LocaleController.getInstance().applyLanguage(selectedLanguage[0], true, false, this.currentAccount);
        rebuildAllFragments(true);
    }

    private void showLanguageAlert(boolean force) {
        try {
            if (!this.loadingLocaleDialog) {
                String showedLang = MessagesController.getGlobalMainSettings().getString("language_showed2", TtmlNode.ANONYMOUS_REGION_ID);
                String systemLang = LocaleController.getSystemLocaleStringIso639().toLowerCase();
                if (force || !showedLang.equals(systemLang)) {
                    String arg;
                    LocaleInfo[] infos = new LocaleInfo[2];
                    if (systemLang.contains("-")) {
                        arg = systemLang.split("-")[0];
                    } else {
                        arg = systemLang;
                    }
                    String alias;
                    if ("in".equals(arg)) {
                        alias = TtmlNode.ATTR_ID;
                    } else if ("iw".equals(arg)) {
                        alias = "he";
                    } else if ("jw".equals(arg)) {
                        alias = "jv";
                    } else {
                        alias = null;
                    }
                    for (int a = 0; a < LocaleController.getInstance().languages.size(); a++) {
                        LocaleInfo info = (LocaleInfo) LocaleController.getInstance().languages.get(a);
                        if (info.shortName.equals("en")) {
                            infos[0] = info;
                        }
                        if (info.shortName.replace("_", "-").equals(systemLang) || info.shortName.equals(arg) || (alias != null && info.shortName.equals(alias))) {
                            infos[1] = info;
                        }
                        if (infos[0] != null && infos[1] != null) {
                            break;
                        }
                    }
                    if (infos[0] != null && infos[1] != null && infos[0] != infos[1]) {
                        if (BuildVars.LOGS_ENABLED) {
                            FileLog.d("show lang alert for " + infos[0].getKey() + " and " + infos[1].getKey());
                        }
                        this.systemLocaleStrings = null;
                        this.englishLocaleStrings = null;
                        this.loadingLocaleDialog = true;
                        TL_langpack_getStrings req = new TL_langpack_getStrings();
                        req.lang_code = infos[1].shortName.replace("_", "-");
                        req.keys.add("English");
                        req.keys.add("ChooseYourLanguage");
                        req.keys.add("ChooseYourLanguageOther");
                        req.keys.add("ChangeLanguageLater");
                        ConnectionsManager.getInstance(this.currentAccount).sendRequest(req, new LaunchActivity$$Lambda$30(this, infos, systemLang), 8);
                        req = new TL_langpack_getStrings();
                        req.lang_code = infos[0].shortName.replace("_", "-");
                        req.keys.add("English");
                        req.keys.add("ChooseYourLanguage");
                        req.keys.add("ChooseYourLanguageOther");
                        req.keys.add("ChangeLanguageLater");
                        ConnectionsManager.getInstance(this.currentAccount).sendRequest(req, new LaunchActivity$$Lambda$31(this, infos, systemLang), 8);
                    }
                } else if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("alert already showed for " + showedLang);
                }
            }
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }

    final /* synthetic */ void lambda$showLanguageAlert$45$LaunchActivity(LocaleInfo[] infos, String systemLang, TLObject response, TL_error error) {
        HashMap<String, String> keys = new HashMap();
        if (response != null) {
            Vector vector = (Vector) response;
            for (int a = 0; a < vector.objects.size(); a++) {
                LangPackString string = (LangPackString) vector.objects.get(a);
                keys.put(string.key, string.value);
            }
        }
        AndroidUtilities.runOnUIThread(new LaunchActivity$$Lambda$34(this, keys, infos, systemLang));
    }

    final /* synthetic */ void lambda$null$44$LaunchActivity(HashMap keys, LocaleInfo[] infos, String systemLang) {
        this.systemLocaleStrings = keys;
        if (this.englishLocaleStrings != null && this.systemLocaleStrings != null) {
            showLanguageAlertInternal(infos[1], infos[0], systemLang);
        }
    }

    final /* synthetic */ void lambda$showLanguageAlert$47$LaunchActivity(LocaleInfo[] infos, String systemLang, TLObject response, TL_error error) {
        HashMap<String, String> keys = new HashMap();
        if (response != null) {
            Vector vector = (Vector) response;
            for (int a = 0; a < vector.objects.size(); a++) {
                LangPackString string = (LangPackString) vector.objects.get(a);
                keys.put(string.key, string.value);
            }
        }
        AndroidUtilities.runOnUIThread(new LaunchActivity$$Lambda$33(this, keys, infos, systemLang));
    }

    final /* synthetic */ void lambda$null$46$LaunchActivity(HashMap keys, LocaleInfo[] infos, String systemLang) {
        this.englishLocaleStrings = keys;
        if (this.englishLocaleStrings != null && this.systemLocaleStrings != null) {
            showLanguageAlertInternal(infos[1], infos[0], systemLang);
        }
    }

    private void onPasscodePause() {
        if (this.lockRunnable != null) {
            AndroidUtilities.cancelRunOnUIThread(this.lockRunnable);
            this.lockRunnable = null;
        }
        if (SharedConfig.passcodeHash.length() != 0) {
            SharedConfig.lastPauseTime = ConnectionsManager.getInstance(this.currentAccount).getCurrentTime();
            this.lockRunnable = new Runnable() {
                public void run() {
                    if (LaunchActivity.this.lockRunnable == this) {
                        if (AndroidUtilities.needShowPasscode(true)) {
                            if (BuildVars.LOGS_ENABLED) {
                                FileLog.d("lock app");
                            }
                            LaunchActivity.this.showPasscodeActivity();
                        } else if (BuildVars.LOGS_ENABLED) {
                            FileLog.d("didn't pass lock check");
                        }
                        LaunchActivity.this.lockRunnable = null;
                    }
                }
            };
            if (SharedConfig.appLocked) {
                AndroidUtilities.runOnUIThread(this.lockRunnable, 1000);
            } else if (SharedConfig.autoLockIn != 0) {
                AndroidUtilities.runOnUIThread(this.lockRunnable, (((long) SharedConfig.autoLockIn) * 1000) + 1000);
            }
        } else {
            SharedConfig.lastPauseTime = 0;
        }
        SharedConfig.saveConfig();
    }

    private void onPasscodeResume() {
        if (this.lockRunnable != null) {
            AndroidUtilities.cancelRunOnUIThread(this.lockRunnable);
            this.lockRunnable = null;
        }
        if (AndroidUtilities.needShowPasscode(true)) {
            showPasscodeActivity();
        }
        if (SharedConfig.lastPauseTime != 0) {
            SharedConfig.lastPauseTime = 0;
            SharedConfig.saveConfig();
        }
    }

    private void updateCurrentConnectionState(int account) {
        if (this.actionBarLayout != null) {
            String title = null;
            Runnable action = null;
            this.currentConnectionState = ConnectionsManager.getInstance(this.currentAccount).getConnectionState();
            if (this.currentConnectionState == 2) {
                title = LocaleController.getString("WaitingForNetwork", R.string.WaitingForNetwork);
            } else if (this.currentConnectionState == 5) {
                title = LocaleController.getString("Updating", R.string.Updating);
            } else if (this.currentConnectionState == 4) {
                title = LocaleController.getString("ConnectingToProxy", R.string.ConnectingToProxy);
            } else if (this.currentConnectionState == 1) {
                title = LocaleController.getString("Connecting", R.string.Connecting);
            }
            if (this.currentConnectionState == 1 || this.currentConnectionState == 4) {
                action = new LaunchActivity$$Lambda$32(this);
            }
            this.actionBarLayout.setTitleOverlayText(title, null, action);
        }
    }

    final /* synthetic */ void lambda$updateCurrentConnectionState$48$LaunchActivity() {
        BaseFragment lastFragment = null;
        if (AndroidUtilities.isTablet()) {
            if (!layerFragmentsStack.isEmpty()) {
                lastFragment = (BaseFragment) layerFragmentsStack.get(layerFragmentsStack.size() - 1);
            }
        } else if (!mainFragmentsStack.isEmpty()) {
            lastFragment = (BaseFragment) mainFragmentsStack.get(mainFragmentsStack.size() - 1);
        }
        if (!(lastFragment instanceof ProxyListActivity) && !(lastFragment instanceof ProxySettingsActivity)) {
            presentFragment(new ProxyListActivity());
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        try {
            super.onSaveInstanceState(outState);
            BaseFragment lastFragment = null;
            if (AndroidUtilities.isTablet()) {
                if (!this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                    lastFragment = (BaseFragment) this.layersActionBarLayout.fragmentsStack.get(this.layersActionBarLayout.fragmentsStack.size() - 1);
                } else if (!this.rightActionBarLayout.fragmentsStack.isEmpty()) {
                    lastFragment = (BaseFragment) this.rightActionBarLayout.fragmentsStack.get(this.rightActionBarLayout.fragmentsStack.size() - 1);
                } else if (!this.actionBarLayout.fragmentsStack.isEmpty()) {
                    lastFragment = (BaseFragment) this.actionBarLayout.fragmentsStack.get(this.actionBarLayout.fragmentsStack.size() - 1);
                }
            } else if (!this.actionBarLayout.fragmentsStack.isEmpty()) {
                lastFragment = (BaseFragment) this.actionBarLayout.fragmentsStack.get(this.actionBarLayout.fragmentsStack.size() - 1);
            }
            if (lastFragment != null) {
                Bundle args = lastFragment.getArguments();
                if ((lastFragment instanceof ChatActivity) && args != null) {
                    outState.putBundle("args", args);
                    outState.putString("fragment", "chat");
                } else if (lastFragment instanceof SettingsActivity) {
                    outState.putString("fragment", "settings");
                } else if ((lastFragment instanceof GroupCreateFinalActivity) && args != null) {
                    outState.putBundle("args", args);
                    outState.putString("fragment", "group");
                } else if (lastFragment instanceof WallpapersActivity) {
                    outState.putString("fragment", "wallpapers");
                } else if ((lastFragment instanceof ProfileActivity) && ((ProfileActivity) lastFragment).isChat() && args != null) {
                    outState.putBundle("args", args);
                    outState.putString("fragment", "chat_profile");
                } else if ((lastFragment instanceof ChannelCreateActivity) && args != null && args.getInt("step") == 0) {
                    outState.putBundle("args", args);
                    outState.putString("fragment", "channel");
                } else if ((lastFragment instanceof ChannelEditActivity) && args != null) {
                    outState.putBundle("args", args);
                    outState.putString("fragment", "edit");
                }
                lastFragment.saveSelfArgs(outState);
            }
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }

    public void onBackPressed() {
        if (this.passcodeView.getVisibility() == 0) {
            finish();
        } else if (SecretMediaViewer.hasInstance() && SecretMediaViewer.getInstance().isVisible()) {
            SecretMediaViewer.getInstance().closePhoto(true, false);
        } else if (PhotoViewer.hasInstance() && PhotoViewer.getInstance().isVisible()) {
            PhotoViewer.getInstance().closePhoto(true, false);
        } else if (ArticleViewer.hasInstance() && ArticleViewer.getInstance().isVisible()) {
            ArticleViewer.getInstance().close(true, false);
        } else if (this.drawerLayoutContainer.isDrawerOpened()) {
            this.drawerLayoutContainer.closeDrawer(false);
        } else if (!AndroidUtilities.isTablet()) {
            this.actionBarLayout.onBackPressed();
        } else if (this.layersActionBarLayout.getVisibility() == 0) {
            this.layersActionBarLayout.onBackPressed();
        } else {
            boolean cancel = false;
            if (this.rightActionBarLayout.getVisibility() == 0 && !this.rightActionBarLayout.fragmentsStack.isEmpty()) {
                cancel = !((BaseFragment) this.rightActionBarLayout.fragmentsStack.get(this.rightActionBarLayout.fragmentsStack.size() + -1)).onBackPressed();
            }
            if (!cancel) {
                this.actionBarLayout.onBackPressed();
            }
        }
    }

    public void onLowMemory() {
        super.onLowMemory();
        this.actionBarLayout.onLowMemory();
        if (AndroidUtilities.isTablet()) {
            this.rightActionBarLayout.onLowMemory();
            this.layersActionBarLayout.onLowMemory();
        }
    }

    public void onActionModeStarted(ActionMode mode) {
        super.onActionModeStarted(mode);
        try {
            Menu menu = mode.getMenu();
            if (!(menu == null || this.actionBarLayout.extendActionMode(menu) || !AndroidUtilities.isTablet() || this.rightActionBarLayout.extendActionMode(menu))) {
                this.layersActionBarLayout.extendActionMode(menu);
            }
        } catch (Throwable e) {
            FileLog.e(e);
        }
        if (VERSION.SDK_INT < 23 || mode.getType() != 1) {
            this.actionBarLayout.onActionModeStarted(mode);
            if (AndroidUtilities.isTablet()) {
                this.rightActionBarLayout.onActionModeStarted(mode);
                this.layersActionBarLayout.onActionModeStarted(mode);
            }
        }
    }

    public void onActionModeFinished(ActionMode mode) {
        super.onActionModeFinished(mode);
        if (VERSION.SDK_INT < 23 || mode.getType() != 1) {
            this.actionBarLayout.onActionModeFinished(mode);
            if (AndroidUtilities.isTablet()) {
                this.rightActionBarLayout.onActionModeFinished(mode);
                this.layersActionBarLayout.onActionModeFinished(mode);
            }
        }
    }

    public boolean onPreIme() {
        if (SecretMediaViewer.hasInstance() && SecretMediaViewer.getInstance().isVisible()) {
            SecretMediaViewer.getInstance().closePhoto(true, false);
            return true;
        } else if (PhotoViewer.hasInstance() && PhotoViewer.getInstance().isVisible()) {
            PhotoViewer.getInstance().closePhoto(true, false);
            return true;
        } else if (!ArticleViewer.hasInstance() || !ArticleViewer.getInstance().isVisible()) {
            return false;
        } else {
            ArticleViewer.getInstance().close(true, false);
            return true;
        }
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == 82 && !SharedConfig.isWaitingForPasscodeEnter) {
            if (PhotoViewer.hasInstance() && PhotoViewer.getInstance().isVisible()) {
                return super.onKeyUp(keyCode, event);
            }
            if (ArticleViewer.hasInstance() && ArticleViewer.getInstance().isVisible()) {
                return super.onKeyUp(keyCode, event);
            }
            if (AndroidUtilities.isTablet()) {
                if (this.layersActionBarLayout.getVisibility() == 0 && !this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                    this.layersActionBarLayout.onKeyUp(keyCode, event);
                } else if (this.rightActionBarLayout.getVisibility() != 0 || this.rightActionBarLayout.fragmentsStack.isEmpty()) {
                    this.actionBarLayout.onKeyUp(keyCode, event);
                } else {
                    this.rightActionBarLayout.onKeyUp(keyCode, event);
                }
            } else if (this.actionBarLayout.fragmentsStack.size() != 1) {
                this.actionBarLayout.onKeyUp(keyCode, event);
            } else if (this.drawerLayoutContainer.isDrawerOpened()) {
                this.drawerLayoutContainer.closeDrawer(false);
            } else {
                if (getCurrentFocus() != null) {
                    AndroidUtilities.hideKeyboard(getCurrentFocus());
                }
                this.drawerLayoutContainer.openDrawer(false);
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    public boolean needPresentFragment(BaseFragment fragment, boolean removeLast, boolean forceWithoutAnimation, ActionBarLayout layout) {
        if (ArticleViewer.hasInstance() && ArticleViewer.getInstance().isVisible()) {
            ArticleViewer.getInstance().close(false, true);
        }
        if (AndroidUtilities.isTablet()) {
            DrawerLayoutContainer drawerLayoutContainer = this.drawerLayoutContainer;
            boolean z = ((fragment instanceof LoginActivity) || (fragment instanceof CountrySelectActivity) || this.layersActionBarLayout.getVisibility() == 0) ? false : true;
            drawerLayoutContainer.setAllowOpenDrawer(z, true);
            if ((fragment instanceof DialogsActivity) && ((DialogsActivity) fragment).isMainDialogList() && layout != this.actionBarLayout) {
                this.actionBarLayout.removeAllFragments();
                this.actionBarLayout.presentFragment(fragment, removeLast, forceWithoutAnimation, false, false);
                this.layersActionBarLayout.removeAllFragments();
                this.layersActionBarLayout.setVisibility(8);
                this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
                if (!this.tabletFullSize) {
                    this.shadowTabletSide.setVisibility(0);
                    if (this.rightActionBarLayout.fragmentsStack.isEmpty()) {
                        this.backgroundTablet.setVisibility(0);
                    }
                }
                return false;
            } else if (fragment instanceof ChatActivity) {
                int a;
                if ((!this.tabletFullSize && layout == this.rightActionBarLayout) || (this.tabletFullSize && layout == this.actionBarLayout)) {
                    boolean result = (this.tabletFullSize && layout == this.actionBarLayout && this.actionBarLayout.fragmentsStack.size() == 1) ? false : true;
                    if (!this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                        a = 0;
                        while (this.layersActionBarLayout.fragmentsStack.size() - 1 > 0) {
                            this.layersActionBarLayout.removeFragmentFromStack((BaseFragment) this.layersActionBarLayout.fragmentsStack.get(0));
                            a = (a - 1) + 1;
                        }
                        this.layersActionBarLayout.closeLastFragment(!forceWithoutAnimation);
                    }
                    if (result) {
                        return result;
                    }
                    this.actionBarLayout.presentFragment(fragment, false, forceWithoutAnimation, false, false);
                    return result;
                } else if (!this.tabletFullSize && layout != this.rightActionBarLayout) {
                    this.rightActionBarLayout.setVisibility(0);
                    this.backgroundTablet.setVisibility(8);
                    this.rightActionBarLayout.removeAllFragments();
                    this.rightActionBarLayout.presentFragment(fragment, removeLast, true, false, false);
                    if (!this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                        a = 0;
                        while (this.layersActionBarLayout.fragmentsStack.size() - 1 > 0) {
                            this.layersActionBarLayout.removeFragmentFromStack((BaseFragment) this.layersActionBarLayout.fragmentsStack.get(0));
                            a = (a - 1) + 1;
                        }
                        this.layersActionBarLayout.closeLastFragment(!forceWithoutAnimation);
                    }
                    return false;
                } else if (!this.tabletFullSize || layout == this.actionBarLayout) {
                    if (!this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                        a = 0;
                        while (this.layersActionBarLayout.fragmentsStack.size() - 1 > 0) {
                            this.layersActionBarLayout.removeFragmentFromStack((BaseFragment) this.layersActionBarLayout.fragmentsStack.get(0));
                            a = (a - 1) + 1;
                        }
                        this.layersActionBarLayout.closeLastFragment(!forceWithoutAnimation);
                    }
                    this.actionBarLayout.presentFragment(fragment, this.actionBarLayout.fragmentsStack.size() > 1, forceWithoutAnimation, false, false);
                    return false;
                } else {
                    this.actionBarLayout.presentFragment(fragment, this.actionBarLayout.fragmentsStack.size() > 1, forceWithoutAnimation, false, false);
                    if (!this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                        a = 0;
                        while (this.layersActionBarLayout.fragmentsStack.size() - 1 > 0) {
                            this.layersActionBarLayout.removeFragmentFromStack((BaseFragment) this.layersActionBarLayout.fragmentsStack.get(0));
                            a = (a - 1) + 1;
                        }
                        this.layersActionBarLayout.closeLastFragment(!forceWithoutAnimation);
                    }
                    return false;
                }
            } else if (layout == this.layersActionBarLayout) {
                return true;
            } else {
                this.layersActionBarLayout.setVisibility(0);
                this.drawerLayoutContainer.setAllowOpenDrawer(false, true);
                if (fragment instanceof LoginActivity) {
                    this.backgroundTablet.setVisibility(0);
                    this.shadowTabletSide.setVisibility(8);
                    this.shadowTablet.setBackgroundColor(0);
                } else {
                    this.shadowTablet.setBackgroundColor(Theme.ACTION_BAR_PHOTO_VIEWER_COLOR);
                }
                this.layersActionBarLayout.presentFragment(fragment, removeLast, forceWithoutAnimation, false, false);
                return false;
            }
        }
        boolean allow = true;
        if (fragment instanceof LoginActivity) {
            if (mainFragmentsStack.size() == 0) {
                allow = false;
            }
        } else if ((fragment instanceof CountrySelectActivity) && mainFragmentsStack.size() == 1) {
            allow = false;
        }
        this.drawerLayoutContainer.setAllowOpenDrawer(allow, false);
        return true;
    }

    public boolean needAddFragmentToStack(BaseFragment fragment, ActionBarLayout layout) {
        if (AndroidUtilities.isTablet()) {
            DrawerLayoutContainer drawerLayoutContainer = this.drawerLayoutContainer;
            boolean z = ((fragment instanceof LoginActivity) || (fragment instanceof CountrySelectActivity) || this.layersActionBarLayout.getVisibility() == 0) ? false : true;
            drawerLayoutContainer.setAllowOpenDrawer(z, true);
            if (fragment instanceof DialogsActivity) {
                if (((DialogsActivity) fragment).isMainDialogList() && layout != this.actionBarLayout) {
                    this.actionBarLayout.removeAllFragments();
                    this.actionBarLayout.addFragmentToStack(fragment);
                    this.layersActionBarLayout.removeAllFragments();
                    this.layersActionBarLayout.setVisibility(8);
                    this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
                    if (this.tabletFullSize) {
                        return false;
                    }
                    this.shadowTabletSide.setVisibility(0);
                    if (!this.rightActionBarLayout.fragmentsStack.isEmpty()) {
                        return false;
                    }
                    this.backgroundTablet.setVisibility(0);
                    return false;
                }
            } else if (fragment instanceof ChatActivity) {
                int a;
                if (!this.tabletFullSize && layout != this.rightActionBarLayout) {
                    this.rightActionBarLayout.setVisibility(0);
                    this.backgroundTablet.setVisibility(8);
                    this.rightActionBarLayout.removeAllFragments();
                    this.rightActionBarLayout.addFragmentToStack(fragment);
                    if (this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                        return false;
                    }
                    a = 0;
                    while (this.layersActionBarLayout.fragmentsStack.size() - 1 > 0) {
                        this.layersActionBarLayout.removeFragmentFromStack((BaseFragment) this.layersActionBarLayout.fragmentsStack.get(0));
                        a = (a - 1) + 1;
                    }
                    this.layersActionBarLayout.closeLastFragment(true);
                    return false;
                } else if (this.tabletFullSize && layout != this.actionBarLayout) {
                    this.actionBarLayout.addFragmentToStack(fragment);
                    if (this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                        return false;
                    }
                    a = 0;
                    while (this.layersActionBarLayout.fragmentsStack.size() - 1 > 0) {
                        this.layersActionBarLayout.removeFragmentFromStack((BaseFragment) this.layersActionBarLayout.fragmentsStack.get(0));
                        a = (a - 1) + 1;
                    }
                    this.layersActionBarLayout.closeLastFragment(true);
                    return false;
                }
            } else if (layout != this.layersActionBarLayout) {
                this.layersActionBarLayout.setVisibility(0);
                this.drawerLayoutContainer.setAllowOpenDrawer(false, true);
                if (fragment instanceof LoginActivity) {
                    this.backgroundTablet.setVisibility(0);
                    this.shadowTabletSide.setVisibility(8);
                    this.shadowTablet.setBackgroundColor(0);
                } else {
                    this.shadowTablet.setBackgroundColor(Theme.ACTION_BAR_PHOTO_VIEWER_COLOR);
                }
                this.layersActionBarLayout.addFragmentToStack(fragment);
                return false;
            }
            return true;
        }
        boolean allow = true;
        if (fragment instanceof LoginActivity) {
            if (mainFragmentsStack.size() == 0) {
                allow = false;
            }
        } else if ((fragment instanceof CountrySelectActivity) && mainFragmentsStack.size() == 1) {
            allow = false;
        }
        this.drawerLayoutContainer.setAllowOpenDrawer(allow, false);
        return true;
    }

    public boolean needCloseLastFragment(ActionBarLayout layout) {
        if (AndroidUtilities.isTablet()) {
            if (layout == this.actionBarLayout && layout.fragmentsStack.size() <= 1) {
                onFinish();
                finish();
                return false;
            } else if (layout == this.rightActionBarLayout) {
                if (!this.tabletFullSize) {
                    this.backgroundTablet.setVisibility(0);
                }
            } else if (layout == this.layersActionBarLayout && this.actionBarLayout.fragmentsStack.isEmpty() && this.layersActionBarLayout.fragmentsStack.size() == 1) {
                onFinish();
                finish();
                return false;
            }
        } else if (layout.fragmentsStack.size() <= 1) {
            onFinish();
            finish();
            return false;
        } else if (layout.fragmentsStack.size() >= 2 && !(layout.fragmentsStack.get(0) instanceof LoginActivity)) {
            this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
        }
        return true;
    }

    public void rebuildAllFragments(boolean last) {
        if (this.layersActionBarLayout != null) {
            this.layersActionBarLayout.rebuildAllFragmentViews(last, last);
        } else {
            this.actionBarLayout.rebuildAllFragmentViews(last, last);
        }
    }

    public void onRebuildAllFragments(ActionBarLayout layout, boolean last) {
        if (AndroidUtilities.isTablet() && layout == this.layersActionBarLayout) {
            this.rightActionBarLayout.rebuildAllFragmentViews(last, last);
            this.actionBarLayout.rebuildAllFragmentViews(last, last);
        }
        this.drawerLayoutAdapter.notifyDataSetChanged();
    }
}
