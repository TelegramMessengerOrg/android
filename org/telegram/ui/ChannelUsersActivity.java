package org.telegram.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.SparseArray;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.exoplayer2.DefaultLoadControl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.beta.R;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.Adapter;
import org.telegram.messenger.support.widget.RecyclerView.LayoutParams;
import org.telegram.messenger.support.widget.RecyclerView.OnScrollListener;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.messenger.support.widget.helper.ItemTouchHelper.Callback;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.ChannelParticipant;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatFull;
import org.telegram.tgnet.TLRPC.TL_channelAdminRights;
import org.telegram.tgnet.TLRPC.TL_channelBannedRights;
import org.telegram.tgnet.TLRPC.TL_channelParticipant;
import org.telegram.tgnet.TLRPC.TL_channelParticipantAdmin;
import org.telegram.tgnet.TLRPC.TL_channelParticipantBanned;
import org.telegram.tgnet.TLRPC.TL_channelParticipantCreator;
import org.telegram.tgnet.TLRPC.TL_channelParticipantSelf;
import org.telegram.tgnet.TLRPC.TL_channelParticipantsAdmins;
import org.telegram.tgnet.TLRPC.TL_channelParticipantsBanned;
import org.telegram.tgnet.TLRPC.TL_channelParticipantsKicked;
import org.telegram.tgnet.TLRPC.TL_channelParticipantsRecent;
import org.telegram.tgnet.TLRPC.TL_channels_channelParticipants;
import org.telegram.tgnet.TLRPC.TL_channels_editBanned;
import org.telegram.tgnet.TLRPC.TL_channels_getParticipants;
import org.telegram.tgnet.TLRPC.TL_contact;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.Updates;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuItem.ActionBarMenuItemSearchListener;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.ActionBar.ThemeDescription.ThemeDescriptionDelegate;
import org.telegram.ui.Adapters.SearchAdapterHelper;
import org.telegram.ui.Adapters.SearchAdapterHelper.HashtagObject;
import org.telegram.ui.Adapters.SearchAdapterHelper.SearchAdapterHelperDelegate;
import org.telegram.ui.Cells.GraySectionCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ManageChatTextCell;
import org.telegram.ui.Cells.ManageChatUserCell;
import org.telegram.ui.Cells.RadioCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;

public class ChannelUsersActivity extends BaseFragment implements NotificationCenterDelegate {
    private static final int search_button = 0;
    private int addNew2Row;
    private int addNewRow;
    private int addNewSectionRow;
    private int blockedEmptyRow;
    private int changeAddHeaderRow;
    private int changeAddRadio1Row;
    private int changeAddRadio2Row;
    private int changeAddSectionRow;
    private int chatId = this.arguments.getInt("chat_id");
    private Chat currentChat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.chatId));
    private EmptyTextProgressView emptyView;
    private boolean firstEndReached;
    private boolean firstLoaded;
    private RecyclerListView listView;
    private ListAdapter listViewAdapter;
    private boolean loadingUsers;
    private boolean needOpenSearch = this.arguments.getBoolean("open_search");
    private ArrayList<ChannelParticipant> participants = new ArrayList();
    private ArrayList<ChannelParticipant> participants2 = new ArrayList();
    private int participants2EndRow;
    private int participants2StartRow;
    private int participantsDividerRow;
    private int participantsEndRow;
    private int participantsInfoRow;
    private SparseArray<ChannelParticipant> participantsMap = new SparseArray();
    private int participantsStartRow;
    private int restricted1SectionRow;
    private int restricted2SectionRow;
    private int rowCount;
    private ActionBarMenuItem searchItem;
    private SearchAdapter searchListViewAdapter;
    private boolean searchWas;
    private boolean searching;
    private int selectType = this.arguments.getInt("selectType");
    private int type = this.arguments.getInt("type");

    private class ListAdapter extends SelectionAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            this.mContext = context;
        }

        public boolean isEnabled(ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == 0 || type == 2 || type == 6;
        }

        public int getItemCount() {
            if (!ChannelUsersActivity.this.loadingUsers || ChannelUsersActivity.this.firstLoaded) {
                return ChannelUsersActivity.this.rowCount;
            }
            return 0;
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0:
                    view = new ManageChatUserCell(this.mContext, ChannelUsersActivity.this.type == 0 ? 8 : 1, ChannelUsersActivity.this.selectType == 0);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    ((ManageChatUserCell) view).setDelegate(new ChannelUsersActivity$ListAdapter$$Lambda$0(this));
                    break;
                case 1:
                    view = new TextInfoPrivacyCell(this.mContext);
                    break;
                case 2:
                    view = new ManageChatTextCell(this.mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 3:
                    view = new ShadowSectionCell(this.mContext);
                    break;
                case 4:
                    view = new FrameLayout(this.mContext) {
                        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec) - AndroidUtilities.dp(56.0f), 1073741824));
                        }
                    };
                    FrameLayout frameLayout = (FrameLayout) view;
                    frameLayout.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
                    LinearLayout linearLayout = new LinearLayout(this.mContext);
                    linearLayout.setOrientation(1);
                    frameLayout.addView(linearLayout, LayoutHelper.createFrame(-2, -2.0f, 17, 20.0f, 0.0f, 20.0f, 0.0f));
                    ImageView imageView = new ImageView(this.mContext);
                    imageView.setImageResource(R.drawable.group_ban_empty);
                    imageView.setScaleType(ScaleType.CENTER);
                    imageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_emptyListPlaceholder), Mode.MULTIPLY));
                    linearLayout.addView(imageView, LayoutHelper.createLinear(-2, -2, 1));
                    TextView textView = new TextView(this.mContext);
                    textView.setText(LocaleController.getString("NoBlockedUsers", R.string.NoBlockedUsers));
                    textView.setTextColor(Theme.getColor(Theme.key_emptyListPlaceholder));
                    textView.setTextSize(1, 16.0f);
                    textView.setGravity(1);
                    textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                    linearLayout.addView(textView, LayoutHelper.createLinear(-2, -2, 1, 0, 10, 0, 0));
                    textView = new TextView(this.mContext);
                    if (ChannelUsersActivity.this.currentChat.megagroup) {
                        textView.setText(LocaleController.getString("NoBlockedGroup", R.string.NoBlockedGroup));
                    } else {
                        textView.setText(LocaleController.getString("NoBlockedChannel", R.string.NoBlockedChannel));
                    }
                    textView.setTextColor(Theme.getColor(Theme.key_emptyListPlaceholder));
                    textView.setTextSize(1, 15.0f);
                    textView.setGravity(1);
                    linearLayout.addView(textView, LayoutHelper.createLinear(-2, -2, 1, 0, 10, 0, 0));
                    view.setLayoutParams(new LayoutParams(-1, -1));
                    break;
                case 5:
                    view = new HeaderCell(this.mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                default:
                    view = new RadioCell(this.mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
            }
            return new Holder(view);
        }

        final /* synthetic */ boolean lambda$onCreateViewHolder$0$ChannelUsersActivity$ListAdapter(ManageChatUserCell cell, boolean click) {
            return ChannelUsersActivity.this.createMenuForParticipant(ChannelUsersActivity.this.listViewAdapter.getItem(((Integer) cell.getTag()).intValue()), !click);
        }

        public void onBindViewHolder(ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 0:
                    ManageChatUserCell userCell = holder.itemView;
                    userCell.setTag(Integer.valueOf(position));
                    ChannelParticipant participant = getItem(position);
                    User user = MessagesController.getInstance(ChannelUsersActivity.this.currentAccount).getUser(Integer.valueOf(participant.user_id));
                    if (user == null) {
                        return;
                    }
                    String role;
                    if (ChannelUsersActivity.this.type == 0) {
                        role = null;
                        if ((participant instanceof TL_channelParticipantBanned) && MessagesController.getInstance(ChannelUsersActivity.this.currentAccount).getUser(Integer.valueOf(participant.kicked_by)) != null) {
                            role = LocaleController.formatString("UserRestrictionsBy", R.string.UserRestrictionsBy, ContactsController.formatName(MessagesController.getInstance(ChannelUsersActivity.this.currentAccount).getUser(Integer.valueOf(participant.kicked_by)).first_name, MessagesController.getInstance(ChannelUsersActivity.this.currentAccount).getUser(Integer.valueOf(participant.kicked_by)).last_name));
                        }
                        userCell.setData(user, null, role);
                        return;
                    } else if (ChannelUsersActivity.this.type == 1) {
                        role = null;
                        if ((participant instanceof TL_channelParticipantCreator) || (participant instanceof TL_channelParticipantSelf)) {
                            role = LocaleController.getString("ChannelCreator", R.string.ChannelCreator);
                        } else if ((participant instanceof TL_channelParticipantAdmin) && MessagesController.getInstance(ChannelUsersActivity.this.currentAccount).getUser(Integer.valueOf(participant.promoted_by)) != null) {
                            role = LocaleController.formatString("EditAdminPromotedBy", R.string.EditAdminPromotedBy, ContactsController.formatName(MessagesController.getInstance(ChannelUsersActivity.this.currentAccount).getUser(Integer.valueOf(participant.promoted_by)).first_name, MessagesController.getInstance(ChannelUsersActivity.this.currentAccount).getUser(Integer.valueOf(participant.promoted_by)).last_name));
                        }
                        userCell.setData(user, null, role);
                        return;
                    } else if (ChannelUsersActivity.this.type == 2) {
                        userCell.setData(user, null, null);
                        return;
                    } else {
                        return;
                    }
                case 1:
                    TextInfoPrivacyCell privacyCell = holder.itemView;
                    if (position != ChannelUsersActivity.this.participantsInfoRow) {
                        return;
                    }
                    if (ChannelUsersActivity.this.type == 0) {
                        if (ChatObject.canBlockUsers(ChannelUsersActivity.this.currentChat)) {
                            if (ChannelUsersActivity.this.currentChat.megagroup) {
                                privacyCell.setText(String.format("%1$s\n\n%2$s", new Object[]{LocaleController.getString("NoBlockedGroup", R.string.NoBlockedGroup), LocaleController.getString("UnbanText", R.string.UnbanText)}));
                            } else {
                                privacyCell.setText(String.format("%1$s\n\n%2$s", new Object[]{LocaleController.getString("NoBlockedChannel", R.string.NoBlockedChannel), LocaleController.getString("UnbanText", R.string.UnbanText)}));
                            }
                        } else if (ChannelUsersActivity.this.currentChat.megagroup) {
                            privacyCell.setText(LocaleController.getString("NoBlockedGroup", R.string.NoBlockedGroup));
                        } else {
                            privacyCell.setText(LocaleController.getString("NoBlockedChannel", R.string.NoBlockedChannel));
                        }
                        privacyCell.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
                        return;
                    } else if (ChannelUsersActivity.this.type == 1) {
                        if (ChannelUsersActivity.this.addNewRow != -1) {
                            if (ChannelUsersActivity.this.currentChat.megagroup) {
                                privacyCell.setText(LocaleController.getString("MegaAdminsInfo", R.string.MegaAdminsInfo));
                            } else {
                                privacyCell.setText(LocaleController.getString("ChannelAdminsInfo", R.string.ChannelAdminsInfo));
                            }
                            privacyCell.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
                            return;
                        }
                        privacyCell.setText(TtmlNode.ANONYMOUS_REGION_ID);
                        privacyCell.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
                        return;
                    } else if (ChannelUsersActivity.this.type == 2) {
                        if (ChannelUsersActivity.this.currentChat.megagroup || ChannelUsersActivity.this.selectType != 0) {
                            privacyCell.setText(TtmlNode.ANONYMOUS_REGION_ID);
                        } else {
                            privacyCell.setText(LocaleController.getString("ChannelMembersInfo", R.string.ChannelMembersInfo));
                        }
                        privacyCell.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
                        return;
                    } else {
                        return;
                    }
                case 2:
                    ManageChatTextCell actionCell = holder.itemView;
                    if (position == ChannelUsersActivity.this.addNewRow) {
                        if (ChannelUsersActivity.this.type == 0) {
                            actionCell.setText(LocaleController.getString("ChannelBlockUser", R.string.ChannelBlockUser), null, R.drawable.group_ban_new, false);
                            return;
                        } else if (ChannelUsersActivity.this.type == 1) {
                            actionCell.setText(LocaleController.getString("ChannelAddAdmin", R.string.ChannelAddAdmin), null, R.drawable.group_admin_new, false);
                            return;
                        } else if (ChannelUsersActivity.this.type != 2) {
                            return;
                        } else {
                            if (!ChatObject.isChannel(ChannelUsersActivity.this.currentChat) || ChannelUsersActivity.this.currentChat.megagroup) {
                                actionCell.setText(LocaleController.getString("AddMember", R.string.AddMember), null, R.drawable.menu_invite, true);
                                return;
                            } else {
                                actionCell.setText(LocaleController.getString("AddSubscriber", R.string.AddSubscriber), null, R.drawable.menu_invite, true);
                                return;
                            }
                        }
                    } else if (position == ChannelUsersActivity.this.addNew2Row) {
                        actionCell.setText(LocaleController.getString("ChannelInviteViaLink", R.string.ChannelInviteViaLink), null, R.drawable.msg_panel_link, false);
                        return;
                    } else {
                        return;
                    }
                case 5:
                    HeaderCell headerCell = holder.itemView;
                    if (position == ChannelUsersActivity.this.restricted1SectionRow) {
                        headerCell.setText(LocaleController.getString("ChannelRestrictedUsers", R.string.ChannelRestrictedUsers));
                        return;
                    } else if (position == ChannelUsersActivity.this.restricted2SectionRow) {
                        headerCell.setText(LocaleController.getString("ChannelBlockedUsers", R.string.ChannelBlockedUsers));
                        return;
                    } else if (position == ChannelUsersActivity.this.changeAddHeaderRow) {
                        headerCell.setText(LocaleController.getString("WhoCanAddMembers", R.string.WhoCanAddMembers));
                        return;
                    } else {
                        return;
                    }
                case 6:
                    RadioCell radioCell = holder.itemView;
                    Chat chat = MessagesController.getInstance(ChannelUsersActivity.this.currentAccount).getChat(Integer.valueOf(ChannelUsersActivity.this.chatId));
                    String string;
                    boolean z;
                    if (position == ChannelUsersActivity.this.changeAddRadio1Row) {
                        radioCell.setTag(Integer.valueOf(0));
                        string = LocaleController.getString("WhoCanAddMembersAllMembers", R.string.WhoCanAddMembersAllMembers);
                        z = chat != null && chat.democracy;
                        radioCell.setText(string, z, true);
                        return;
                    } else if (position == ChannelUsersActivity.this.changeAddRadio2Row) {
                        radioCell.setTag(Integer.valueOf(1));
                        string = LocaleController.getString("WhoCanAddMembersAdmins", R.string.WhoCanAddMembersAdmins);
                        z = (chat == null || chat.democracy) ? false : true;
                        radioCell.setText(string, z, false);
                        return;
                    } else {
                        return;
                    }
                default:
                    return;
            }
        }

        public void onViewRecycled(ViewHolder holder) {
            if (holder.itemView instanceof ManageChatUserCell) {
                ((ManageChatUserCell) holder.itemView).recycle();
            }
        }

        public int getItemViewType(int position) {
            if (position == ChannelUsersActivity.this.addNewRow || position == ChannelUsersActivity.this.addNew2Row) {
                return 2;
            }
            if (position >= ChannelUsersActivity.this.participantsStartRow && position < ChannelUsersActivity.this.participantsEndRow) {
                return 0;
            }
            if (position >= ChannelUsersActivity.this.participants2StartRow && position < ChannelUsersActivity.this.participants2EndRow) {
                return 0;
            }
            if (position == ChannelUsersActivity.this.addNewSectionRow || position == ChannelUsersActivity.this.changeAddSectionRow || position == ChannelUsersActivity.this.participantsDividerRow) {
                return 3;
            }
            if (position == ChannelUsersActivity.this.participantsInfoRow) {
                return 1;
            }
            if (position == ChannelUsersActivity.this.changeAddHeaderRow || position == ChannelUsersActivity.this.restricted1SectionRow || position == ChannelUsersActivity.this.restricted2SectionRow) {
                return 5;
            }
            if (position == ChannelUsersActivity.this.changeAddRadio1Row || position == ChannelUsersActivity.this.changeAddRadio2Row) {
                return 6;
            }
            if (position == ChannelUsersActivity.this.blockedEmptyRow) {
                return 4;
            }
            return 0;
        }

        public ChannelParticipant getItem(int position) {
            if (ChannelUsersActivity.this.participantsStartRow != -1 && position >= ChannelUsersActivity.this.participantsStartRow && position < ChannelUsersActivity.this.participantsEndRow) {
                return (ChannelParticipant) ChannelUsersActivity.this.participants.get(position - ChannelUsersActivity.this.participantsStartRow);
            }
            if (ChannelUsersActivity.this.participants2StartRow == -1 || position < ChannelUsersActivity.this.participants2StartRow || position >= ChannelUsersActivity.this.participants2EndRow) {
                return null;
            }
            return (ChannelParticipant) ChannelUsersActivity.this.participants2.get(position - ChannelUsersActivity.this.participants2StartRow);
        }
    }

    private class SearchAdapter extends SelectionAdapter {
        private int contactsStartRow;
        private int globalStartRow;
        private int group2StartRow;
        private int groupStartRow;
        private Context mContext;
        private SearchAdapterHelper searchAdapterHelper;
        private ArrayList<User> searchResult = new ArrayList();
        private ArrayList<CharSequence> searchResultNames = new ArrayList();
        private Timer searchTimer;
        private int totalCount;

        public SearchAdapter(Context context) {
            this.mContext = context;
            this.searchAdapterHelper = new SearchAdapterHelper(true);
            this.searchAdapterHelper.setDelegate(new SearchAdapterHelperDelegate(ChannelUsersActivity.this) {
                public void onDataSetChanged() {
                    SearchAdapter.this.notifyDataSetChanged();
                }

                public void onSetHashtags(ArrayList<HashtagObject> arrayList, HashMap<String, HashtagObject> hashMap) {
                }
            });
        }

        public void searchDialogs(final String query) {
            try {
                if (this.searchTimer != null) {
                    this.searchTimer.cancel();
                }
            } catch (Throwable e) {
                FileLog.e(e);
            }
            if (query == null) {
                boolean z;
                this.searchResult.clear();
                this.searchResultNames.clear();
                SearchAdapterHelper searchAdapterHelper = this.searchAdapterHelper;
                if (ChannelUsersActivity.this.type != 0) {
                    z = true;
                } else {
                    z = false;
                }
                searchAdapterHelper.queryServerSearch(null, z, false, true, true, ChannelUsersActivity.this.chatId, ChannelUsersActivity.this.type == 0);
                notifyDataSetChanged();
                return;
            }
            this.searchTimer = new Timer();
            this.searchTimer.schedule(new TimerTask() {
                public void run() {
                    try {
                        SearchAdapter.this.searchTimer.cancel();
                        SearchAdapter.this.searchTimer = null;
                    } catch (Throwable e) {
                        FileLog.e(e);
                    }
                    SearchAdapter.this.processSearch(query);
                }
            }, 200, 300);
        }

        private void processSearch(String query) {
            AndroidUtilities.runOnUIThread(new ChannelUsersActivity$SearchAdapter$$Lambda$0(this, query));
        }

        final /* synthetic */ void lambda$processSearch$1$ChannelUsersActivity$SearchAdapter(String query) {
            this.searchAdapterHelper.queryServerSearch(query, ChannelUsersActivity.this.selectType != 0, false, true, true, ChannelUsersActivity.this.chatId, ChannelUsersActivity.this.type == 0);
            if (ChannelUsersActivity.this.selectType == 1) {
                Utilities.searchQueue.postRunnable(new ChannelUsersActivity$SearchAdapter$$Lambda$3(this, query, new ArrayList(ContactsController.getInstance(ChannelUsersActivity.this.currentAccount).contacts)));
            }
        }

        final /* synthetic */ void lambda$null$0$ChannelUsersActivity$SearchAdapter(String query, ArrayList contactsCopy) {
            String search1 = query.trim().toLowerCase();
            if (search1.length() == 0) {
                updateSearchResults(new ArrayList(), new ArrayList());
                return;
            }
            String search2 = LocaleController.getInstance().getTranslitString(search1);
            if (search1.equals(search2) || search2.length() == 0) {
                search2 = null;
            }
            String[] search = new String[((search2 != null ? 1 : 0) + 1)];
            search[0] = search1;
            if (search2 != null) {
                search[1] = search2;
            }
            ArrayList<User> resultArray = new ArrayList();
            ArrayList<CharSequence> resultArrayNames = new ArrayList();
            for (int a = 0; a < contactsCopy.size(); a++) {
                User user = MessagesController.getInstance(ChannelUsersActivity.this.currentAccount).getUser(Integer.valueOf(((TL_contact) contactsCopy.get(a)).user_id));
                if (user.id != UserConfig.getInstance(ChannelUsersActivity.this.currentAccount).getClientUserId()) {
                    String name = ContactsController.formatName(user.first_name, user.last_name).toLowerCase();
                    String tName = LocaleController.getInstance().getTranslitString(name);
                    if (name.equals(tName)) {
                        tName = null;
                    }
                    int found = 0;
                    int length = search.length;
                    int i = 0;
                    while (i < length) {
                        String q = search[i];
                        if (name.startsWith(q) || name.contains(" " + q) || (tName != null && (tName.startsWith(q) || tName.contains(" " + q)))) {
                            found = 1;
                        } else if (user.username != null && user.username.startsWith(q)) {
                            found = 2;
                        }
                        if (found != 0) {
                            if (found == 1) {
                                resultArrayNames.add(AndroidUtilities.generateSearchName(user.first_name, user.last_name, q));
                            } else {
                                resultArrayNames.add(AndroidUtilities.generateSearchName("@" + user.username, null, "@" + q));
                            }
                            resultArray.add(user);
                        } else {
                            i++;
                        }
                    }
                }
            }
            updateSearchResults(resultArray, resultArrayNames);
        }

        private void updateSearchResults(ArrayList<User> users, ArrayList<CharSequence> names) {
            AndroidUtilities.runOnUIThread(new ChannelUsersActivity$SearchAdapter$$Lambda$1(this, users, names));
        }

        final /* synthetic */ void lambda$updateSearchResults$2$ChannelUsersActivity$SearchAdapter(ArrayList users, ArrayList names) {
            this.searchResult = users;
            this.searchResultNames = names;
            notifyDataSetChanged();
        }

        public boolean isEnabled(ViewHolder holder) {
            return holder.getItemViewType() != 1;
        }

        public int getItemCount() {
            int contactsCount = this.searchResult.size();
            int globalCount = this.searchAdapterHelper.getGlobalSearch().size();
            int groupsCount = this.searchAdapterHelper.getGroupSearch().size();
            int groupsCount2 = this.searchAdapterHelper.getGroupSearch2().size();
            int count = 0;
            if (contactsCount != 0) {
                count = 0 + (contactsCount + 1);
            }
            if (globalCount != 0) {
                count += globalCount + 1;
            }
            if (groupsCount != 0) {
                count += groupsCount + 1;
            }
            if (groupsCount2 != 0) {
                return count + (groupsCount2 + 1);
            }
            return count;
        }

        public void notifyDataSetChanged() {
            this.totalCount = 0;
            int count = this.searchAdapterHelper.getGroupSearch().size();
            if (count != 0) {
                this.groupStartRow = 0;
                this.totalCount += count + 1;
            } else {
                this.groupStartRow = -1;
            }
            count = this.searchAdapterHelper.getGroupSearch2().size();
            if (count != 0) {
                this.group2StartRow = this.totalCount;
                this.totalCount += count + 1;
            } else {
                this.group2StartRow = -1;
            }
            count = this.searchResult.size();
            if (count != 0) {
                this.contactsStartRow = this.totalCount;
                this.totalCount += count + 1;
            } else {
                this.contactsStartRow = -1;
            }
            count = this.searchAdapterHelper.getGlobalSearch().size();
            if (count != 0) {
                this.globalStartRow = this.totalCount;
                this.totalCount += count + 1;
            } else {
                this.globalStartRow = -1;
            }
            super.notifyDataSetChanged();
        }

        public TLObject getItem(int i) {
            int count = this.searchAdapterHelper.getGroupSearch().size();
            if (count != 0) {
                if (count + 1 <= i) {
                    i -= count + 1;
                } else if (i == 0) {
                    return null;
                } else {
                    return (TLObject) this.searchAdapterHelper.getGroupSearch().get(i - 1);
                }
            }
            count = this.searchAdapterHelper.getGroupSearch2().size();
            if (count != 0) {
                if (count + 1 <= i) {
                    i -= count + 1;
                } else if (i != 0) {
                    return (TLObject) this.searchAdapterHelper.getGroupSearch2().get(i - 1);
                } else {
                    return null;
                }
            }
            count = this.searchResult.size();
            if (count != 0) {
                if (count + 1 <= i) {
                    i -= count + 1;
                } else if (i != 0) {
                    return (TLObject) this.searchResult.get(i - 1);
                } else {
                    return null;
                }
            }
            count = this.searchAdapterHelper.getGlobalSearch().size();
            if (count == 0 || count + 1 <= i || i == 0) {
                return null;
            }
            return (TLObject) this.searchAdapterHelper.getGlobalSearch().get(i - 1);
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0:
                    view = new ManageChatUserCell(this.mContext, 2, ChannelUsersActivity.this.selectType == 0);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    ((ManageChatUserCell) view).setDelegate(new ChannelUsersActivity$SearchAdapter$$Lambda$2(this));
                    break;
                default:
                    view = new GraySectionCell(this.mContext);
                    break;
            }
            return new Holder(view);
        }

        final /* synthetic */ boolean lambda$onCreateViewHolder$3$ChannelUsersActivity$SearchAdapter(ManageChatUserCell cell, boolean click) {
            if (!(getItem(((Integer) cell.getTag()).intValue()) instanceof ChannelParticipant)) {
                return false;
            }
            boolean z;
            ChannelParticipant participant = (ChannelParticipant) getItem(((Integer) cell.getTag()).intValue());
            ChannelUsersActivity channelUsersActivity = ChannelUsersActivity.this;
            if (click) {
                z = false;
            } else {
                z = true;
            }
            return channelUsersActivity.createMenuForParticipant(participant, z);
        }

        public void onBindViewHolder(ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 0:
                    User user;
                    TLObject object = getItem(position);
                    if (object instanceof User) {
                        user = (User) object;
                    } else {
                        user = MessagesController.getInstance(ChannelUsersActivity.this.currentAccount).getUser(Integer.valueOf(((ChannelParticipant) object).user_id));
                    }
                    String un = user.username;
                    CharSequence username = null;
                    CharSequence charSequence = null;
                    int count = this.searchAdapterHelper.getGroupSearch().size();
                    boolean ok = false;
                    String nameSearch = null;
                    if (count != 0) {
                        if (count + 1 > position) {
                            nameSearch = this.searchAdapterHelper.getLastFoundChannel();
                            ok = true;
                        } else {
                            position -= count + 1;
                        }
                    }
                    if (!ok) {
                        count = this.searchAdapterHelper.getGroupSearch2().size();
                        if (count != 0) {
                            if (count + 1 > position) {
                                nameSearch = this.searchAdapterHelper.getLastFoundChannel2();
                            } else {
                                position -= count + 1;
                            }
                        }
                    }
                    if (!ok) {
                        count = this.searchResult.size();
                        if (count != 0) {
                            if (count + 1 > position) {
                                ok = true;
                                charSequence = (CharSequence) this.searchResultNames.get(position - 1);
                                if (charSequence != null && un != null && un.length() > 0 && charSequence.toString().startsWith("@" + un)) {
                                    username = charSequence;
                                    charSequence = null;
                                }
                            } else {
                                position -= count + 1;
                            }
                        }
                    }
                    if (!ok) {
                        count = this.searchAdapterHelper.getGlobalSearch().size();
                        if (count != 0 && count + 1 > position) {
                            String foundUserName = this.searchAdapterHelper.getLastFoundUsername();
                            if (foundUserName.startsWith("@")) {
                                foundUserName = foundUserName.substring(1);
                            }
                            try {
                                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                                spannableStringBuilder.append("@");
                                spannableStringBuilder.append(un);
                                int index = un.toLowerCase().indexOf(foundUserName);
                                if (index != -1) {
                                    int len = foundUserName.length();
                                    if (index == 0) {
                                        len++;
                                    } else {
                                        index++;
                                    }
                                    spannableStringBuilder.setSpan(new ForegroundColorSpan(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4)), index, index + len, 33);
                                }
                                username = spannableStringBuilder;
                            } catch (Throwable e) {
                                Object username2 = un;
                                FileLog.e(e);
                            }
                        }
                    }
                    if (nameSearch != null) {
                        String u = UserObject.getUserName(user);
                        charSequence = new SpannableStringBuilder(u);
                        int idx = u.toLowerCase().indexOf(nameSearch);
                        if (idx != -1) {
                            ((SpannableStringBuilder) charSequence).setSpan(new ForegroundColorSpan(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4)), idx, nameSearch.length() + idx, 33);
                        }
                    }
                    ManageChatUserCell userCell = (ManageChatUserCell) holder.itemView;
                    userCell.setTag(Integer.valueOf(position));
                    userCell.setData(user, charSequence, username);
                    return;
                case 1:
                    GraySectionCell sectionCell = holder.itemView;
                    if (position != this.groupStartRow) {
                        if (position == this.group2StartRow) {
                            sectionCell.setText(LocaleController.getString("ChannelBlockedUsers", R.string.ChannelBlockedUsers).toUpperCase());
                            return;
                        }
                        if (position == this.globalStartRow) {
                            sectionCell.setText(LocaleController.getString("GlobalSearch", R.string.GlobalSearch).toUpperCase());
                            return;
                        }
                        if (position == this.contactsStartRow) {
                            sectionCell.setText(LocaleController.getString("Contacts", R.string.Contacts).toUpperCase());
                            return;
                        }
                        return;
                    } else if (ChannelUsersActivity.this.type == 0) {
                        sectionCell.setText(LocaleController.getString("ChannelRestrictedUsers", R.string.ChannelRestrictedUsers).toUpperCase());
                        return;
                    } else if (!ChatObject.isChannel(ChannelUsersActivity.this.currentChat) || ChannelUsersActivity.this.currentChat.megagroup) {
                        sectionCell.setText(LocaleController.getString("ChannelMembers", R.string.ChannelMembers).toUpperCase());
                        return;
                    } else {
                        ChannelUsersActivity.this.actionBar.setTitle(LocaleController.getString("ChannelSubscribers", R.string.ChannelSubscribers));
                        return;
                    }
                default:
                    return;
            }
        }

        public void onViewRecycled(ViewHolder holder) {
            if (holder.itemView instanceof ManageChatUserCell) {
                ((ManageChatUserCell) holder.itemView).recycle();
            }
        }

        public int getItemViewType(int i) {
            if (i == this.globalStartRow || i == this.groupStartRow || i == this.contactsStartRow || i == this.group2StartRow) {
                return 1;
            }
            return 0;
        }
    }

    public ChannelUsersActivity(Bundle args) {
        super(args);
    }

    private void updateRows() {
        this.currentChat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.chatId));
        if (this.currentChat != null) {
            this.changeAddHeaderRow = -1;
            this.changeAddRadio1Row = -1;
            this.changeAddRadio2Row = -1;
            this.changeAddSectionRow = -1;
            this.addNewRow = -1;
            this.addNew2Row = -1;
            this.addNewSectionRow = -1;
            this.restricted1SectionRow = -1;
            this.participantsStartRow = -1;
            this.participantsDividerRow = -1;
            this.participantsEndRow = -1;
            this.restricted2SectionRow = -1;
            this.participants2StartRow = -1;
            this.participants2EndRow = -1;
            this.participantsInfoRow = -1;
            this.blockedEmptyRow = -1;
            this.rowCount = 0;
            int i;
            if (this.type == 0) {
                if (ChatObject.canBlockUsers(this.currentChat)) {
                    i = this.rowCount;
                    this.rowCount = i + 1;
                    this.addNewRow = i;
                    if (!(this.participants.isEmpty() && this.participants2.isEmpty())) {
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.addNewSectionRow = i;
                    }
                } else {
                    this.addNewRow = -1;
                    this.addNewSectionRow = -1;
                }
                if (!this.participants.isEmpty()) {
                    i = this.rowCount;
                    this.rowCount = i + 1;
                    this.restricted1SectionRow = i;
                    this.participantsStartRow = this.rowCount;
                    this.rowCount += this.participants.size();
                    this.participantsEndRow = this.rowCount;
                }
                if (!this.participants2.isEmpty()) {
                    if (this.restricted1SectionRow != -1) {
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.participantsDividerRow = i;
                    }
                    i = this.rowCount;
                    this.rowCount = i + 1;
                    this.restricted2SectionRow = i;
                    this.participants2StartRow = this.rowCount;
                    this.rowCount += this.participants2.size();
                    this.participants2EndRow = this.rowCount;
                }
                if (this.participantsStartRow == -1 && this.participants2StartRow == -1) {
                    if (this.searchItem != null) {
                        this.searchItem.setVisibility(4);
                    }
                    i = this.rowCount;
                    this.rowCount = i + 1;
                    this.blockedEmptyRow = i;
                    return;
                }
                if (this.searchItem != null) {
                    this.searchItem.setVisibility(0);
                }
                i = this.rowCount;
                this.rowCount = i + 1;
                this.participantsInfoRow = i;
            } else if (this.type == 1) {
                if ((this.currentChat.creator || (this.currentChat.admin_rights != null && this.currentChat.admin_rights.change_info)) && this.currentChat.megagroup) {
                    i = this.rowCount;
                    this.rowCount = i + 1;
                    this.changeAddHeaderRow = i;
                    i = this.rowCount;
                    this.rowCount = i + 1;
                    this.changeAddRadio1Row = i;
                    i = this.rowCount;
                    this.rowCount = i + 1;
                    this.changeAddRadio2Row = i;
                    i = this.rowCount;
                    this.rowCount = i + 1;
                    this.changeAddSectionRow = i;
                }
                if (ChatObject.canAddAdmins(this.currentChat)) {
                    i = this.rowCount;
                    this.rowCount = i + 1;
                    this.addNewRow = i;
                    i = this.rowCount;
                    this.rowCount = i + 1;
                    this.addNewSectionRow = i;
                } else {
                    this.addNewRow = -1;
                    this.addNewSectionRow = -1;
                }
                if (this.participants.isEmpty()) {
                    this.participantsStartRow = -1;
                    this.participantsEndRow = -1;
                } else {
                    this.participantsStartRow = this.rowCount;
                    this.rowCount += this.participants.size();
                    this.participantsEndRow = this.rowCount;
                }
                i = this.rowCount;
                this.rowCount = i + 1;
                this.participantsInfoRow = i;
            } else if (this.type == 2) {
                if (this.selectType == 0 && !this.currentChat.megagroup && ChatObject.canAddUsers(this.currentChat)) {
                    i = this.rowCount;
                    this.rowCount = i + 1;
                    this.addNewRow = i;
                    if ((this.currentChat.flags & 64) == 0 && ChatObject.canAddViaLink(this.currentChat)) {
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.addNew2Row = i;
                    }
                    i = this.rowCount;
                    this.rowCount = i + 1;
                    this.addNewSectionRow = i;
                }
                if (this.participants.isEmpty()) {
                    this.participantsStartRow = -1;
                    this.participantsEndRow = -1;
                } else {
                    this.participantsStartRow = this.rowCount;
                    this.rowCount += this.participants.size();
                    this.participantsEndRow = this.rowCount;
                }
                if (this.rowCount != 0) {
                    i = this.rowCount;
                    this.rowCount = i + 1;
                    this.participantsInfoRow = i;
                }
            }
        }
    }

    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.chatInfoDidLoaded);
        getChannelParticipants(0, Callback.DEFAULT_DRAG_ANIMATION_DURATION);
        return true;
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.chatInfoDidLoaded);
    }

    public View createView(Context context) {
        int i = 1;
        this.searching = false;
        this.searchWas = false;
        this.actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        this.actionBar.setAllowOverlayTitle(true);
        if (this.type == 0) {
            this.actionBar.setTitle(LocaleController.getString("ChannelBlacklist", R.string.ChannelBlacklist));
        } else if (this.type == 1) {
            this.actionBar.setTitle(LocaleController.getString("ChannelAdministrators", R.string.ChannelAdministrators));
        } else if (this.type == 2) {
            if (this.selectType == 0) {
                if (!ChatObject.isChannel(this.currentChat) || this.currentChat.megagroup) {
                    this.actionBar.setTitle(LocaleController.getString("ChannelMembers", R.string.ChannelMembers));
                } else {
                    this.actionBar.setTitle(LocaleController.getString("ChannelSubscribers", R.string.ChannelSubscribers));
                }
            } else if (this.selectType == 1) {
                this.actionBar.setTitle(LocaleController.getString("ChannelAddAdmin", R.string.ChannelAddAdmin));
            } else if (this.selectType == 2) {
                this.actionBar.setTitle(LocaleController.getString("ChannelBlockUser", R.string.ChannelBlockUser));
            }
        }
        this.actionBar.setActionBarMenuOnItemClick(new ActionBarMenuOnItemClick() {
            public void onItemClick(int id) {
                if (id == -1) {
                    ChannelUsersActivity.this.finishFragment();
                }
            }
        });
        if (this.selectType != 0 || this.type == 2 || this.type == 0) {
            this.searchListViewAdapter = new SearchAdapter(context);
            this.searchItem = this.actionBar.createMenu().addItem(0, (int) R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItemSearchListener() {
                public void onSearchExpand() {
                    ChannelUsersActivity.this.searching = true;
                    ChannelUsersActivity.this.emptyView.setShowAtCenter(true);
                }

                public void onSearchCollapse() {
                    ChannelUsersActivity.this.searchListViewAdapter.searchDialogs(null);
                    ChannelUsersActivity.this.searching = false;
                    ChannelUsersActivity.this.searchWas = false;
                    ChannelUsersActivity.this.listView.setAdapter(ChannelUsersActivity.this.listViewAdapter);
                    ChannelUsersActivity.this.listViewAdapter.notifyDataSetChanged();
                    ChannelUsersActivity.this.listView.setFastScrollVisible(true);
                    ChannelUsersActivity.this.listView.setVerticalScrollBarEnabled(false);
                    ChannelUsersActivity.this.emptyView.setShowAtCenter(false);
                }

                public void onTextChanged(EditText editText) {
                    if (ChannelUsersActivity.this.searchListViewAdapter != null) {
                        String text = editText.getText().toString();
                        if (text.length() != 0) {
                            ChannelUsersActivity.this.searchWas = true;
                            if (ChannelUsersActivity.this.listView != null) {
                                ChannelUsersActivity.this.listView.setAdapter(ChannelUsersActivity.this.searchListViewAdapter);
                                ChannelUsersActivity.this.searchListViewAdapter.notifyDataSetChanged();
                                ChannelUsersActivity.this.listView.setFastScrollVisible(false);
                                ChannelUsersActivity.this.listView.setVerticalScrollBarEnabled(true);
                            }
                        }
                        ChannelUsersActivity.this.searchListViewAdapter.searchDialogs(text);
                    }
                }
            });
            this.searchItem.getSearchField().setHint(LocaleController.getString("Search", R.string.Search));
        }
        this.fragmentView = new FrameLayout(context);
        this.fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = this.fragmentView;
        this.emptyView = new EmptyTextProgressView(context);
        if (this.type == 0 || this.type == 2) {
            this.emptyView.setText(LocaleController.getString("NoResult", R.string.NoResult));
        }
        frameLayout.addView(this.emptyView, LayoutHelper.createFrame(-1, -1.0f));
        this.listView = new RecyclerListView(context);
        this.listView.setEmptyView(this.emptyView);
        this.listView.setLayoutManager(new LinearLayoutManager(context, 1, false));
        RecyclerListView recyclerListView = this.listView;
        Adapter listAdapter = new ListAdapter(context);
        this.listViewAdapter = listAdapter;
        recyclerListView.setAdapter(listAdapter);
        recyclerListView = this.listView;
        if (!LocaleController.isRTL) {
            i = 2;
        }
        recyclerListView.setVerticalScrollbarPosition(i);
        frameLayout.addView(this.listView, LayoutHelper.createFrame(-1, -1.0f));
        this.listView.setOnItemClickListener(new ChannelUsersActivity$$Lambda$0(this));
        this.listView.setOnItemLongClickListener(new ChannelUsersActivity$$Lambda$1(this));
        if (this.searchItem != null) {
            this.listView.setOnScrollListener(new OnScrollListener() {
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    if (newState == 1 && ChannelUsersActivity.this.searching && ChannelUsersActivity.this.searchWas) {
                        AndroidUtilities.hideKeyboard(ChannelUsersActivity.this.getParentActivity().getCurrentFocus());
                    }
                }

                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                }
            });
        }
        if (this.loadingUsers) {
            this.emptyView.showProgress();
        } else {
            this.emptyView.showTextView();
        }
        updateRows();
        return this.fragmentView;
    }

    final /* synthetic */ void lambda$createView$3$ChannelUsersActivity(View view, int position) {
        Bundle args;
        if (position == this.addNewRow) {
            Bundle bundle;
            if (this.type == 0) {
                bundle = new Bundle();
                bundle.putInt("chat_id", this.chatId);
                bundle.putInt("type", 2);
                bundle.putInt("selectType", 2);
                presentFragment(new ChannelUsersActivity(bundle));
            } else if (this.type == 1) {
                bundle = new Bundle();
                bundle.putInt("chat_id", this.chatId);
                bundle.putInt("type", 2);
                bundle.putInt("selectType", 1);
                presentFragment(new ChannelUsersActivity(bundle));
            } else if (this.type == 2) {
                args = new Bundle();
                args.putBoolean("onlyUsers", true);
                args.putBoolean("destroyAfterSelect", true);
                args.putBoolean("returnAsResult", true);
                args.putBoolean("needForwardCount", false);
                args.putString("selectAlertString", LocaleController.getString("ChannelAddTo", R.string.ChannelAddTo));
                ContactsActivity fragment = new ContactsActivity(args);
                fragment.setDelegate(new ChannelUsersActivity$$Lambda$13(this));
                presentFragment(fragment);
            }
        } else if (position == this.addNew2Row) {
            presentFragment(new GroupInviteActivity(this.chatId));
        } else if (position == this.changeAddRadio1Row || position == this.changeAddRadio2Row) {
            Chat chat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.chatId));
            if (chat != null) {
                boolean changed = false;
                if (position == 1 && !chat.democracy) {
                    chat.democracy = true;
                    changed = true;
                } else if (position == 2 && chat.democracy) {
                    chat.democracy = false;
                    changed = true;
                }
                if (changed) {
                    MessagesController.getInstance(this.currentAccount).toogleChannelInvites(this.chatId, chat.democracy);
                    int count = this.listView.getChildCount();
                    for (int a = 0; a < count; a++) {
                        View child = this.listView.getChildAt(a);
                        if (child instanceof RadioCell) {
                            int num = ((Integer) child.getTag()).intValue();
                            RadioCell radioCell = (RadioCell) child;
                            boolean z = (num == 0 && chat.democracy) || (num == 1 && !chat.democracy);
                            radioCell.setChecked(z, true);
                        }
                    }
                }
            }
        } else {
            ChannelParticipant participant;
            TL_channelBannedRights banned_rights = null;
            TL_channelAdminRights admin_rights = null;
            int user_id = 0;
            boolean canEditAdmin = false;
            if (this.listView.getAdapter() == this.listViewAdapter) {
                participant = this.listViewAdapter.getItem(position);
                if (participant != null) {
                    user_id = participant.user_id;
                    banned_rights = participant.banned_rights;
                    admin_rights = participant.admin_rights;
                    canEditAdmin = !((participant instanceof TL_channelParticipantAdmin) || (participant instanceof TL_channelParticipantCreator)) || participant.can_edit;
                    if (participant instanceof TL_channelParticipantCreator) {
                        admin_rights = new TL_channelAdminRights();
                        admin_rights.add_admins = true;
                        admin_rights.pin_messages = true;
                        admin_rights.invite_link = true;
                        admin_rights.invite_users = true;
                        admin_rights.ban_users = true;
                        admin_rights.delete_messages = true;
                        admin_rights.edit_messages = true;
                        admin_rights.post_messages = true;
                        admin_rights.change_info = true;
                    }
                }
            } else {
                TLObject object = this.searchListViewAdapter.getItem(position);
                if (object instanceof User) {
                    User user = (User) object;
                    MessagesController.getInstance(this.currentAccount).putUser(user, false);
                    SparseArray sparseArray = this.participantsMap;
                    user_id = user.id;
                    participant = (ChannelParticipant) sparseArray.get(user_id);
                } else if (object instanceof ChannelParticipant) {
                    participant = (ChannelParticipant) object;
                } else {
                    participant = null;
                }
                if (participant != null) {
                    user_id = participant.user_id;
                    canEditAdmin = !((participant instanceof TL_channelParticipantAdmin) || (participant instanceof TL_channelParticipantCreator)) || participant.can_edit;
                    banned_rights = participant.banned_rights;
                    admin_rights = participant.admin_rights;
                } else {
                    canEditAdmin = true;
                }
            }
            if (user_id == 0) {
                return;
            }
            if (this.selectType == 0) {
                boolean canEdit = false;
                if (this.type == 1) {
                    canEdit = user_id != UserConfig.getInstance(this.currentAccount).getClientUserId() && (this.currentChat.creator || canEditAdmin);
                } else if (this.type == 0) {
                    canEdit = ChatObject.canBlockUsers(this.currentChat);
                }
                if ((this.type == 1 || this.currentChat.megagroup) && !(this.type == 2 && this.selectType == 0)) {
                    if (banned_rights == null) {
                        banned_rights = new TL_channelBannedRights();
                        banned_rights.view_messages = true;
                        banned_rights.send_stickers = true;
                        banned_rights.send_media = true;
                        banned_rights.embed_links = true;
                        banned_rights.send_messages = true;
                        banned_rights.send_games = true;
                        banned_rights.send_inline = true;
                        banned_rights.send_gifs = true;
                    }
                    ChannelRightsEditActivity channelRightsEditActivity = new ChannelRightsEditActivity(user_id, this.chatId, admin_rights, banned_rights, this.type == 1 ? 0 : 1, canEdit);
                    channelRightsEditActivity.setDelegate(new ChannelUsersActivity$$Lambda$15(this, participant));
                    presentFragment(channelRightsEditActivity);
                    return;
                }
                args = new Bundle();
                args.putInt("user_id", user_id);
                presentFragment(new ProfileActivity(args));
            } else if (this.currentChat.megagroup || this.selectType == 1) {
                ChannelRightsEditActivity fragment2 = new ChannelRightsEditActivity(user_id, this.chatId, admin_rights, banned_rights, this.selectType == 1 ? 0 : 1, canEditAdmin);
                fragment2.setDelegate(new ChannelUsersActivity$$Lambda$14(this, participant));
                presentFragment(fragment2);
            } else {
                MessagesController.getInstance(this.currentAccount).deleteUserFromChat(this.chatId, MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(user_id)), null);
                finishFragment();
            }
        }
    }

    final /* synthetic */ void lambda$null$0$ChannelUsersActivity(User user, String param, ContactsActivity activity) {
        MessagesController.getInstance(this.currentAccount).addUserToChat(this.chatId, user, null, param != null ? Utilities.parseInt(param).intValue() : 0, null, this);
    }

    final /* synthetic */ void lambda$null$1$ChannelUsersActivity(ChannelParticipant participant, int rights, TL_channelAdminRights rightsAdmin, TL_channelBannedRights rightsBanned) {
        if (participant != null) {
            participant.admin_rights = rightsAdmin;
            participant.banned_rights = rightsBanned;
            ChannelParticipant p = (ChannelParticipant) this.participantsMap.get(participant.user_id);
            if (p != null) {
                p.admin_rights = rightsAdmin;
                p.banned_rights = rightsBanned;
            }
        }
        removeSelfFromStack();
    }

    final /* synthetic */ void lambda$null$2$ChannelUsersActivity(ChannelParticipant participant, int rights, TL_channelAdminRights rightsAdmin, TL_channelBannedRights rightsBanned) {
        if (participant != null) {
            participant.admin_rights = rightsAdmin;
            participant.banned_rights = rightsBanned;
            ChannelParticipant p = (ChannelParticipant) this.participantsMap.get(participant.user_id);
            if (p != null) {
                p.admin_rights = rightsAdmin;
                p.banned_rights = rightsBanned;
            }
        }
    }

    final /* synthetic */ boolean lambda$createView$4$ChannelUsersActivity(View view, int position) {
        return getParentActivity() != null && this.listView.getAdapter() == this.listViewAdapter && createMenuForParticipant(this.listViewAdapter.getItem(position), false);
    }

    private boolean createMenuForParticipant(ChannelParticipant participant, boolean resultOnly) {
        if (participant == null || this.selectType != 0) {
            return false;
        }
        if (participant.user_id == UserConfig.getInstance(this.currentAccount).getClientUserId()) {
            return false;
        }
        if (this.type == 2) {
            boolean allowSetAdmin;
            ArrayList<String> items;
            ArrayList<Integer> actions;
            User user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(participant.user_id));
            if ((participant instanceof TL_channelParticipant) || (participant instanceof TL_channelParticipantBanned)) {
                allowSetAdmin = true;
            } else {
                allowSetAdmin = false;
            }
            boolean canEditAdmin;
            if (((participant instanceof TL_channelParticipantAdmin) || (participant instanceof TL_channelParticipantCreator)) && !participant.can_edit) {
                canEditAdmin = false;
            } else {
                canEditAdmin = true;
            }
            if (resultOnly) {
                items = null;
                actions = null;
            } else {
                items = new ArrayList();
                actions = new ArrayList();
            }
            if (allowSetAdmin && ChatObject.canAddAdmins(this.currentChat)) {
                if (resultOnly) {
                    return true;
                }
                items.add(LocaleController.getString("SetAsAdmin", R.string.SetAsAdmin));
                actions.add(Integer.valueOf(0));
            }
            if (ChatObject.canBlockUsers(this.currentChat) && canEditAdmin) {
                if (resultOnly) {
                    return true;
                }
                if (this.currentChat.megagroup) {
                    items.add(LocaleController.getString("KickFromSupergroup", R.string.KickFromSupergroup));
                    actions.add(Integer.valueOf(1));
                    items.add(LocaleController.getString("KickFromGroup", R.string.KickFromGroup));
                    actions.add(Integer.valueOf(2));
                } else {
                    items.add(LocaleController.getString("ChannelRemoveUser", R.string.ChannelRemoveUser));
                    actions.add(Integer.valueOf(2));
                }
            }
            if (actions == null || actions.isEmpty()) {
                return false;
            }
            Builder builder = new Builder(getParentActivity());
            builder.setItems((CharSequence[]) items.toArray(new CharSequence[actions.size()]), new ChannelUsersActivity$$Lambda$2(this, actions, user, participant));
            showDialog(builder.create());
            return true;
        }
        CharSequence[] items2 = null;
        if (this.type == 0 && ChatObject.canBlockUsers(this.currentChat)) {
            if (resultOnly) {
                return true;
            }
            items2 = new CharSequence[]{LocaleController.getString("Unban", R.string.Unban)};
        } else if (this.type == 1 && ChatObject.canAddAdmins(this.currentChat) && participant.can_edit) {
            if (resultOnly) {
                return true;
            }
            items2 = new CharSequence[]{LocaleController.getString("ChannelRemoveUserAdmin", R.string.ChannelRemoveUserAdmin)};
        }
        if (items2 == null) {
            return false;
        }
        builder = new Builder(getParentActivity());
        builder.setItems(items2, new ChannelUsersActivity$$Lambda$3(this, participant));
        showDialog(builder.create());
        return true;
    }

    final /* synthetic */ void lambda$createMenuForParticipant$6$ChannelUsersActivity(ArrayList actions, User user, ChannelParticipant participant, DialogInterface dialogInterface, int i) {
        if (((Integer) actions.get(i)).intValue() == 2) {
            MessagesController.getInstance(this.currentAccount).deleteUserFromChat(this.chatId, user, null);
            for (int a = 0; a < this.participants.size(); a++) {
                if (((ChannelParticipant) this.participants.get(a)).user_id == participant.user_id) {
                    this.participants.remove(a);
                    updateRows();
                    this.listViewAdapter.notifyDataSetChanged();
                    return;
                }
            }
            return;
        }
        ChannelRightsEditActivity fragment = new ChannelRightsEditActivity(user.id, this.chatId, participant.admin_rights, participant.banned_rights, ((Integer) actions.get(i)).intValue(), true);
        fragment.setDelegate(new ChannelUsersActivity$$Lambda$12(this, actions, i, participant));
        presentFragment(fragment);
    }

    final /* synthetic */ void lambda$null$5$ChannelUsersActivity(ArrayList actions, int i, ChannelParticipant participant, int rights, TL_channelAdminRights rightsAdmin, TL_channelBannedRights rightsBanned) {
        int a;
        if (((Integer) actions.get(i)).intValue() == 0) {
            for (a = 0; a < this.participants.size(); a++) {
                if (((ChannelParticipant) this.participants.get(a)).user_id == participant.user_id) {
                    ChannelParticipant newPart;
                    if (rights == 1) {
                        newPart = new TL_channelParticipantAdmin();
                    } else {
                        newPart = new TL_channelParticipant();
                    }
                    newPart.admin_rights = rightsAdmin;
                    newPart.banned_rights = rightsBanned;
                    newPart.inviter_id = UserConfig.getInstance(this.currentAccount).getClientUserId();
                    newPart.user_id = participant.user_id;
                    newPart.date = participant.date;
                    this.participants.set(a, newPart);
                    return;
                }
            }
        } else if (((Integer) actions.get(i)).intValue() == 1 && rights == 0) {
            for (a = 0; a < this.participants.size(); a++) {
                if (((ChannelParticipant) this.participants.get(a)).user_id == participant.user_id) {
                    this.participants.remove(a);
                    updateRows();
                    this.listViewAdapter.notifyDataSetChanged();
                    return;
                }
            }
        }
    }

    final /* synthetic */ void lambda$createMenuForParticipant$9$ChannelUsersActivity(ChannelParticipant participant, DialogInterface dialogInterface, int i) {
        if (i != 0) {
            return;
        }
        if (this.type == 0) {
            this.participants.remove(participant);
            updateRows();
            this.listViewAdapter.notifyDataSetChanged();
            TL_channels_editBanned req = new TL_channels_editBanned();
            req.user_id = MessagesController.getInstance(this.currentAccount).getInputUser(participant.user_id);
            req.channel = MessagesController.getInstance(this.currentAccount).getInputChannel(this.chatId);
            req.banned_rights = new TL_channelBannedRights();
            ConnectionsManager.getInstance(this.currentAccount).sendRequest(req, new ChannelUsersActivity$$Lambda$10(this));
        } else if (this.type == 1) {
            MessagesController.getInstance(this.currentAccount).setUserAdminRole(this.chatId, MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(participant.user_id)), new TL_channelAdminRights(), this.currentChat.megagroup, this);
        } else if (this.type == 2) {
            MessagesController.getInstance(this.currentAccount).deleteUserFromChat(this.chatId, MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(participant.user_id)), null);
        }
    }

    final /* synthetic */ void lambda$null$8$ChannelUsersActivity(TLObject response, TL_error error) {
        if (response != null) {
            Updates updates = (Updates) response;
            MessagesController.getInstance(this.currentAccount).processUpdates(updates, false);
            if (!updates.chats.isEmpty()) {
                AndroidUtilities.runOnUIThread(new ChannelUsersActivity$$Lambda$11(this, updates), 1000);
            }
        }
    }

    final /* synthetic */ void lambda$null$7$ChannelUsersActivity(Updates updates) {
        MessagesController.getInstance(this.currentAccount).loadFullChat(((Chat) updates.chats.get(0)).id, 0, true);
    }

    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.chatInfoDidLoaded) {
            ChatFull chatFull = args[0];
            boolean byChannelUsers = ((Boolean) args[2]).booleanValue();
            if (chatFull.id == this.chatId && !byChannelUsers) {
                AndroidUtilities.runOnUIThread(new ChannelUsersActivity$$Lambda$4(this));
            }
        }
    }

    final /* synthetic */ void lambda$didReceivedNotification$10$ChannelUsersActivity() {
        this.firstEndReached = false;
        getChannelParticipants(0, Callback.DEFAULT_DRAG_ANIMATION_DURATION);
    }

    private int getChannelAdminParticipantType(ChannelParticipant participant) {
        if ((participant instanceof TL_channelParticipantCreator) || (participant instanceof TL_channelParticipantSelf)) {
            return 0;
        }
        if (participant instanceof TL_channelParticipantAdmin) {
            return 1;
        }
        return 2;
    }

    private void getChannelParticipants(int offset, int count) {
        if (!this.loadingUsers) {
            this.loadingUsers = true;
            if (!(this.emptyView == null || this.firstLoaded)) {
                this.emptyView.showProgress();
            }
            if (this.listViewAdapter != null) {
                this.listViewAdapter.notifyDataSetChanged();
            }
            TL_channels_getParticipants req = new TL_channels_getParticipants();
            req.channel = MessagesController.getInstance(this.currentAccount).getInputChannel(this.chatId);
            boolean byEndReached = this.firstEndReached;
            if (this.type == 0) {
                if (byEndReached) {
                    req.filter = new TL_channelParticipantsKicked();
                } else {
                    req.filter = new TL_channelParticipantsBanned();
                }
            } else if (this.type == 1) {
                req.filter = new TL_channelParticipantsAdmins();
            } else if (this.type == 2) {
                req.filter = new TL_channelParticipantsRecent();
            }
            req.filter.q = TtmlNode.ANONYMOUS_REGION_ID;
            req.offset = offset;
            req.limit = count;
            ConnectionsManager.getInstance(this.currentAccount).bindRequestToGuid(ConnectionsManager.getInstance(this.currentAccount).sendRequest(req, new ChannelUsersActivity$$Lambda$5(this, byEndReached)), this.classGuid);
        }
    }

    final /* synthetic */ void lambda$getChannelParticipants$14$ChannelUsersActivity(boolean byEndReached, TLObject response, TL_error error) {
        AndroidUtilities.runOnUIThread(new ChannelUsersActivity$$Lambda$7(this, error, response, byEndReached));
    }

    final /* synthetic */ void lambda$null$13$ChannelUsersActivity(TL_error error, TLObject response, boolean byEndReached) {
        boolean changeFirst;
        if (this.firstLoaded) {
            changeFirst = false;
        } else {
            changeFirst = true;
        }
        this.loadingUsers = false;
        this.firstLoaded = true;
        if (this.emptyView != null) {
            this.emptyView.showTextView();
        }
        if (error == null) {
            int a;
            TL_channels_channelParticipants res = (TL_channels_channelParticipants) response;
            MessagesController.getInstance(this.currentAccount).putUsers(res.users, false);
            int selfId = UserConfig.getInstance(this.currentAccount).getClientUserId();
            if (this.selectType != 0) {
                for (a = 0; a < res.participants.size(); a++) {
                    if (((ChannelParticipant) res.participants.get(a)).user_id == selfId) {
                        res.participants.remove(a);
                        break;
                    }
                }
            }
            if (this.type != 0) {
                this.participantsMap.clear();
                this.participants = res.participants;
            } else if (byEndReached) {
                this.participants2 = res.participants;
            } else {
                this.participants2 = new ArrayList();
                this.participantsMap.clear();
                this.participants = res.participants;
                if (changeFirst) {
                    this.firstLoaded = false;
                }
                this.firstEndReached = true;
                getChannelParticipants(0, Callback.DEFAULT_DRAG_ANIMATION_DURATION);
            }
            for (a = 0; a < res.participants.size(); a++) {
                ChannelParticipant participant = (ChannelParticipant) res.participants.get(a);
                this.participantsMap.put(participant.user_id, participant);
            }
            try {
                if (this.type == 0 || this.type == 2) {
                    Collections.sort(res.participants, new ChannelUsersActivity$$Lambda$8(this));
                } else if (this.type == 1) {
                    Collections.sort(res.participants, new ChannelUsersActivity$$Lambda$9(this));
                }
            } catch (Throwable e) {
                FileLog.e(e);
            }
        }
        updateRows();
        if (this.listViewAdapter != null) {
            this.listViewAdapter.notifyDataSetChanged();
        }
    }

    final /* synthetic */ int lambda$null$11$ChannelUsersActivity(ChannelParticipant lhs, ChannelParticipant rhs) {
        User user1 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(rhs.user_id));
        User user2 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(lhs.user_id));
        int status1 = 0;
        int status2 = 0;
        if (!(user1 == null || user1.status == null)) {
            status1 = user1.id == UserConfig.getInstance(this.currentAccount).getClientUserId() ? ConnectionsManager.getInstance(this.currentAccount).getCurrentTime() + DefaultLoadControl.DEFAULT_MAX_BUFFER_MS : user1.status.expires;
        }
        if (!(user2 == null || user2.status == null)) {
            status2 = user2.id == UserConfig.getInstance(this.currentAccount).getClientUserId() ? ConnectionsManager.getInstance(this.currentAccount).getCurrentTime() + DefaultLoadControl.DEFAULT_MAX_BUFFER_MS : user2.status.expires;
        }
        if (status1 <= 0 || status2 <= 0) {
            if (status1 >= 0 || status2 >= 0) {
                if ((status1 < 0 && status2 > 0) || (status1 == 0 && status2 != 0)) {
                    return -1;
                }
                if (status2 < 0 && status1 > 0) {
                    return 1;
                }
                if (status2 != 0 || status1 == 0) {
                    return 0;
                }
                return 1;
            } else if (status1 > status2) {
                return 1;
            } else {
                if (status1 < status2) {
                    return -1;
                }
                return 0;
            }
        } else if (status1 > status2) {
            return 1;
        } else {
            if (status1 < status2) {
                return -1;
            }
            return 0;
        }
    }

    final /* synthetic */ int lambda$null$12$ChannelUsersActivity(ChannelParticipant lhs, ChannelParticipant rhs) {
        int type1 = getChannelAdminParticipantType(lhs);
        int type2 = getChannelAdminParticipantType(rhs);
        if (type1 > type2) {
            return 1;
        }
        if (type1 < type2) {
            return -1;
        }
        return 0;
    }

    public void onResume() {
        super.onResume();
        if (this.listViewAdapter != null) {
            this.listViewAdapter.notifyDataSetChanged();
        }
    }

    protected void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
        if (isOpen && !backward && this.needOpenSearch) {
            this.searchItem.openSearch(true);
        }
    }

    public ThemeDescription[] getThemeDescriptions() {
        ThemeDescriptionDelegate cellDelegate = new ChannelUsersActivity$$Lambda$6(this);
        ThemeDescription[] themeDescriptionArr = new ThemeDescription[34];
        themeDescriptionArr[0] = new ThemeDescription(this.listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{ManageChatUserCell.class, TextSettingsCell.class, ManageChatTextCell.class, RadioCell.class}, null, null, null, Theme.key_windowBackgroundWhite);
        themeDescriptionArr[1] = new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray);
        themeDescriptionArr[2] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault);
        themeDescriptionArr[3] = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefault);
        themeDescriptionArr[4] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon);
        themeDescriptionArr[5] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle);
        themeDescriptionArr[6] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector);
        themeDescriptionArr[7] = new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector);
        themeDescriptionArr[8] = new ThemeDescription(this.listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider);
        themeDescriptionArr[9] = new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow);
        themeDescriptionArr[10] = new ThemeDescription(this.listView, 0, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText4);
        themeDescriptionArr[11] = new ThemeDescription(this.listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText);
        themeDescriptionArr[12] = new ThemeDescription(this.listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteValueText);
        themeDescriptionArr[13] = new ThemeDescription(this.listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"valueImageView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayIcon);
        themeDescriptionArr[14] = new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow);
        themeDescriptionArr[15] = new ThemeDescription(this.listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader);
        themeDescriptionArr[16] = new ThemeDescription(this.listView, 0, new Class[]{RadioCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText);
        themeDescriptionArr[17] = new ThemeDescription(this.listView, ThemeDescription.FLAG_CHECKBOX, new Class[]{RadioCell.class}, new String[]{"radioButton"}, null, null, null, Theme.key_radioBackground);
        themeDescriptionArr[18] = new ThemeDescription(this.listView, ThemeDescription.FLAG_CHECKBOXCHECK, new Class[]{RadioCell.class}, new String[]{"radioButton"}, null, null, null, Theme.key_radioBackgroundChecked);
        themeDescriptionArr[19] = new ThemeDescription(this.listView, 0, new Class[]{GraySectionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_graySectionText);
        themeDescriptionArr[20] = new ThemeDescription(this.listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{GraySectionCell.class}, null, null, null, Theme.key_graySection);
        themeDescriptionArr[21] = new ThemeDescription(this.listView, 0, new Class[]{ManageChatUserCell.class}, new String[]{"nameTextView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText);
        themeDescriptionArr[22] = new ThemeDescription(this.listView, 0, new Class[]{ManageChatUserCell.class}, new String[]{"statusColor"}, null, null, cellDelegate, Theme.key_windowBackgroundWhiteGrayText);
        themeDescriptionArr[23] = new ThemeDescription(this.listView, 0, new Class[]{ManageChatUserCell.class}, new String[]{"statusOnlineColor"}, null, null, cellDelegate, Theme.key_windowBackgroundWhiteBlueText);
        themeDescriptionArr[24] = new ThemeDescription(this.listView, 0, new Class[]{ManageChatUserCell.class}, null, new Drawable[]{Theme.avatar_photoDrawable, Theme.avatar_broadcastDrawable, Theme.avatar_savedDrawable}, null, Theme.key_avatar_text);
        themeDescriptionArr[25] = new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundRed);
        themeDescriptionArr[26] = new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundOrange);
        themeDescriptionArr[27] = new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundViolet);
        themeDescriptionArr[28] = new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundGreen);
        themeDescriptionArr[29] = new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundCyan);
        themeDescriptionArr[30] = new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundBlue);
        themeDescriptionArr[31] = new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundPink);
        themeDescriptionArr[32] = new ThemeDescription(this.listView, 0, new Class[]{ManageChatTextCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText);
        themeDescriptionArr[33] = new ThemeDescription(this.listView, 0, new Class[]{ManageChatTextCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayIcon);
        return themeDescriptionArr;
    }

    final /* synthetic */ void lambda$getThemeDescriptions$15$ChannelUsersActivity() {
        if (this.listView != null) {
            int count = this.listView.getChildCount();
            for (int a = 0; a < count; a++) {
                View child = this.listView.getChildAt(a);
                if (child instanceof ManageChatUserCell) {
                    ((ManageChatUserCell) child).update(0);
                }
            }
        }
    }
}
