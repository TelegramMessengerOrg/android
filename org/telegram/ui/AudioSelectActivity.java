package org.telegram.ui;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.Audio.Media;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MediaController.AudioEntry;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.beta.R;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.Adapter;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.Peer;
import org.telegram.tgnet.TLRPC.TL_document;
import org.telegram.tgnet.TLRPC.TL_documentAttributeAudio;
import org.telegram.tgnet.TLRPC.TL_documentAttributeFilename;
import org.telegram.tgnet.TLRPC.TL_message;
import org.telegram.tgnet.TLRPC.TL_messageMediaDocument;
import org.telegram.tgnet.TLRPC.TL_peerUser;
import org.telegram.tgnet.TLRPC.TL_photoSizeEmpty;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.AudioCell;
import org.telegram.ui.Cells.AudioCell.AudioCellDelegate;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.PickerBottomLayout;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;

public class AudioSelectActivity extends BaseFragment implements NotificationCenterDelegate {
    private ArrayList<AudioEntry> audioEntries = new ArrayList();
    private PickerBottomLayout bottomLayout;
    private AudioSelectActivityDelegate delegate;
    private RecyclerListView listView;
    private ListAdapter listViewAdapter;
    private boolean loadingAudio;
    private MessageObject playingAudio;
    private EmptyTextProgressView progressView;
    private HashMap<Long, AudioEntry> selectedAudios = new HashMap();
    private View shadow;

    public interface AudioSelectActivityDelegate {
        void didSelectAudio(ArrayList<MessageObject> arrayList);
    }

    private class ListAdapter extends SelectionAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            this.mContext = context;
        }

        public int getItemCount() {
            return AudioSelectActivity.this.audioEntries.size();
        }

        public Object getItem(int i) {
            return AudioSelectActivity.this.audioEntries.get(i);
        }

        public long getItemId(int i) {
            return (long) i;
        }

        public boolean isEnabled(ViewHolder holder) {
            return true;
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            AudioCell view = new AudioCell(this.mContext);
            view.setDelegate(new AudioCellDelegate() {
                public void startedPlayingAudio(MessageObject messageObject) {
                    AudioSelectActivity.this.playingAudio = messageObject;
                }
            });
            return new Holder(view);
        }

        public void onBindViewHolder(ViewHolder holder, int position) {
            ((AudioCell) holder.itemView).setAudio((AudioEntry) AudioSelectActivity.this.audioEntries.get(position), position != AudioSelectActivity.this.audioEntries.size() + -1, AudioSelectActivity.this.selectedAudios.containsKey(Long.valueOf(((AudioEntry) AudioSelectActivity.this.audioEntries.get(position)).id)));
        }

        public int getItemViewType(int i) {
            return 0;
        }
    }

    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.closeChats);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.messagePlayingDidReset);
        loadAudio();
        return true;
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.closeChats);
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.messagePlayingDidReset);
        if (this.playingAudio != null && MediaController.getInstance(this.currentAccount).isPlayingMessage(this.playingAudio)) {
            MediaController.getInstance(this.currentAccount).cleanupPlayer(true, true);
        }
    }

    public View createView(Context context) {
        int i = 1;
        this.actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        this.actionBar.setAllowOverlayTitle(true);
        this.actionBar.setTitle(LocaleController.getString("AttachMusic", R.string.AttachMusic));
        this.actionBar.setActionBarMenuOnItemClick(new ActionBarMenuOnItemClick() {
            public void onItemClick(int id) {
                if (id == -1) {
                    AudioSelectActivity.this.finishFragment();
                }
            }
        });
        this.fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = this.fragmentView;
        this.progressView = new EmptyTextProgressView(context);
        this.progressView.setText(LocaleController.getString("NoAudio", R.string.NoAudio));
        frameLayout.addView(this.progressView, LayoutHelper.createFrame(-1, -1.0f));
        this.listView = new RecyclerListView(context);
        this.listView.setEmptyView(this.progressView);
        this.listView.setVerticalScrollBarEnabled(false);
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
        frameLayout.addView(this.listView, LayoutHelper.createFrame(-1, -1.0f, 51, 0.0f, 0.0f, 0.0f, 48.0f));
        this.listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(View view, int position) {
                AudioCell audioCell = (AudioCell) view;
                AudioEntry audioEntry = audioCell.getAudioEntry();
                if (AudioSelectActivity.this.selectedAudios.containsKey(Long.valueOf(audioEntry.id))) {
                    AudioSelectActivity.this.selectedAudios.remove(Long.valueOf(audioEntry.id));
                    audioCell.setChecked(false);
                } else {
                    AudioSelectActivity.this.selectedAudios.put(Long.valueOf(audioEntry.id), audioEntry);
                    audioCell.setChecked(true);
                }
                AudioSelectActivity.this.updateBottomLayoutCount();
            }
        });
        this.bottomLayout = new PickerBottomLayout(context, false);
        frameLayout.addView(this.bottomLayout, LayoutHelper.createFrame(-1, 48, 80));
        this.bottomLayout.cancelButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                AudioSelectActivity.this.finishFragment();
            }
        });
        this.bottomLayout.doneButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (AudioSelectActivity.this.delegate != null) {
                    ArrayList<MessageObject> audios = new ArrayList();
                    for (Entry<Long, AudioEntry> entry : AudioSelectActivity.this.selectedAudios.entrySet()) {
                        audios.add(((AudioEntry) entry.getValue()).messageObject);
                    }
                    AudioSelectActivity.this.delegate.didSelectAudio(audios);
                }
                AudioSelectActivity.this.finishFragment();
            }
        });
        View shadow = new View(context);
        shadow.setBackgroundResource(R.drawable.header_shadow_reverse);
        frameLayout.addView(shadow, LayoutHelper.createFrame(-1, 3.0f, 83, 0.0f, 0.0f, 0.0f, 48.0f));
        if (this.loadingAudio) {
            this.progressView.showProgress();
        } else {
            this.progressView.showTextView();
        }
        updateBottomLayoutCount();
        return this.fragmentView;
    }

    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.closeChats) {
            removeSelfFromStack();
        } else if (id == NotificationCenter.messagePlayingDidReset && this.listViewAdapter != null) {
            this.listViewAdapter.notifyDataSetChanged();
        }
    }

    private void updateBottomLayoutCount() {
        this.bottomLayout.updateSelectedCount(this.selectedAudios.size(), true);
    }

    public void setDelegate(AudioSelectActivityDelegate audioSelectActivityDelegate) {
        this.delegate = audioSelectActivityDelegate;
    }

    private void loadAudio() {
        this.loadingAudio = true;
        if (this.progressView != null) {
            this.progressView.showProgress();
        }
        Utilities.globalQueue.postRunnable(new Runnable() {
            public void run() {
                String[] projection = new String[]{"_id", "artist", "title", "_data", "duration", "album"};
                ArrayList<AudioEntry> newAudioEntries = new ArrayList();
                Cursor cursor = null;
                try {
                    cursor = ApplicationLoader.applicationContext.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, projection, "is_music != 0", null, "title");
                    int id = -2000000000;
                    while (cursor.moveToNext()) {
                        AudioEntry audioEntry = new AudioEntry();
                        audioEntry.id = (long) cursor.getInt(0);
                        audioEntry.author = cursor.getString(1);
                        audioEntry.title = cursor.getString(2);
                        audioEntry.path = cursor.getString(3);
                        audioEntry.duration = (int) (cursor.getLong(4) / 1000);
                        audioEntry.genre = cursor.getString(5);
                        File file = new File(audioEntry.path);
                        Message message = new TL_message();
                        message.out = true;
                        message.id = id;
                        message.to_id = new TL_peerUser();
                        Peer peer = message.to_id;
                        int clientUserId = UserConfig.getInstance(AudioSelectActivity.this.currentAccount).getClientUserId();
                        message.from_id = clientUserId;
                        peer.user_id = clientUserId;
                        message.date = (int) (System.currentTimeMillis() / 1000);
                        message.message = "-1";
                        message.attachPath = audioEntry.path;
                        message.media = new TL_messageMediaDocument();
                        MessageMedia messageMedia = message.media;
                        messageMedia.flags |= 3;
                        message.media.document = new TL_document();
                        message.flags |= 768;
                        String ext = FileLoader.getFileExtension(file);
                        message.media.document.id = 0;
                        message.media.document.access_hash = 0;
                        message.media.document.date = message.date;
                        Document document = message.media.document;
                        StringBuilder append = new StringBuilder().append("audio/");
                        if (ext.length() <= 0) {
                            ext = "mp3";
                        }
                        document.mime_type = append.append(ext).toString();
                        message.media.document.size = (int) file.length();
                        message.media.document.thumb = new TL_photoSizeEmpty();
                        message.media.document.thumb.type = "s";
                        message.media.document.dc_id = 0;
                        TL_documentAttributeAudio attributeAudio = new TL_documentAttributeAudio();
                        attributeAudio.duration = audioEntry.duration;
                        attributeAudio.title = audioEntry.title;
                        attributeAudio.performer = audioEntry.author;
                        attributeAudio.flags |= 3;
                        message.media.document.attributes.add(attributeAudio);
                        TL_documentAttributeFilename fileName = new TL_documentAttributeFilename();
                        fileName.file_name = file.getName();
                        message.media.document.attributes.add(fileName);
                        audioEntry.messageObject = new MessageObject(AudioSelectActivity.this.currentAccount, message, null, false);
                        newAudioEntries.add(audioEntry);
                        id--;
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Throwable e) {
                    FileLog.e(e);
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
                final ArrayList<AudioEntry> arrayList = newAudioEntries;
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public void run() {
                        AudioSelectActivity.this.audioEntries = arrayList;
                        AudioSelectActivity.this.progressView.showTextView();
                        AudioSelectActivity.this.listViewAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    public ThemeDescription[] getThemeDescriptions() {
        r9 = new ThemeDescription[24];
        r9[7] = new ThemeDescription(this.listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider);
        r9[8] = new ThemeDescription(this.progressView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_emptyListPlaceholder);
        r9[9] = new ThemeDescription(this.progressView, ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, Theme.key_progressCircle);
        r9[10] = new ThemeDescription(this.listView, 0, new Class[]{AudioCell.class}, new String[]{"titleTextView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText);
        r9[11] = new ThemeDescription(this.listView, 0, new Class[]{AudioCell.class}, new String[]{"genreTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2);
        r9[12] = new ThemeDescription(this.listView, 0, new Class[]{AudioCell.class}, new String[]{"authorTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2);
        r9[13] = new ThemeDescription(this.listView, 0, new Class[]{AudioCell.class}, new String[]{"timeTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText3);
        r9[14] = new ThemeDescription(this.listView, ThemeDescription.FLAG_CHECKBOX, new Class[]{AudioCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_musicPicker_checkbox);
        r9[15] = new ThemeDescription(this.listView, ThemeDescription.FLAG_CHECKBOXCHECK, new Class[]{AudioCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_musicPicker_checkboxCheck);
        r9[16] = new ThemeDescription(this.listView, ThemeDescription.FLAG_USEBACKGROUNDDRAWABLE, new Class[]{AudioCell.class}, new String[]{"playButton"}, null, null, null, Theme.key_musicPicker_buttonIcon);
        r9[17] = new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_USEBACKGROUNDDRAWABLE, new Class[]{AudioCell.class}, new String[]{"playButton"}, null, null, null, Theme.key_musicPicker_buttonBackground);
        r9[18] = new ThemeDescription(this.bottomLayout, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite);
        r9[19] = new ThemeDescription(this.bottomLayout, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{PickerBottomLayout.class}, new String[]{"cancelButton"}, null, null, null, Theme.key_picker_enabledButton);
        r9[20] = new ThemeDescription(this.bottomLayout, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{PickerBottomLayout.class}, new String[]{"doneButtonTextView"}, null, null, null, Theme.key_picker_enabledButton);
        r9[21] = new ThemeDescription(this.bottomLayout, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{PickerBottomLayout.class}, new String[]{"doneButtonTextView"}, null, null, null, Theme.key_picker_disabledButton);
        r9[22] = new ThemeDescription(this.bottomLayout, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{PickerBottomLayout.class}, new String[]{"doneButtonBadgeTextView"}, null, null, null, Theme.key_picker_badgeText);
        r9[23] = new ThemeDescription(this.bottomLayout, ThemeDescription.FLAG_USEBACKGROUNDDRAWABLE, new Class[]{PickerBottomLayout.class}, new String[]{"doneButtonBadgeTextView"}, null, null, null, Theme.key_picker_badge);
        return r9;
    }
}