package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.text.TextUtils.TruncateAt;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Emoji;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class StickerSetNameCell extends FrameLayout {
    private ImageView buttonView;
    private boolean empty;
    private TextView textView;

    public StickerSetNameCell(Context context) {
        super(context);
        this.textView = new TextView(context);
        this.textView.setTextColor(Theme.getColor(Theme.key_chat_emojiPanelStickerSetName));
        this.textView.setTextSize(1, 14.0f);
        this.textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        this.textView.setEllipsize(TruncateAt.END);
        this.textView.setSingleLine(true);
        addView(this.textView, LayoutHelper.createFrame(-2, -2.0f, 51, 17.0f, 4.0f, 57.0f, 0.0f));
        this.buttonView = new ImageView(context);
        this.buttonView.setScaleType(ScaleType.CENTER);
        this.buttonView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chat_emojiPanelStickerSetNameIcon), Mode.MULTIPLY));
        addView(this.buttonView, LayoutHelper.createFrame(24, 24.0f, 53, 0.0f, 0.0f, 16.0f, 0.0f));
    }

    public void setText(CharSequence text, int resId) {
        if (text == null) {
            this.empty = true;
            this.textView.setText(TtmlNode.ANONYMOUS_REGION_ID);
            this.buttonView.setVisibility(4);
            return;
        }
        this.textView.setText(Emoji.replaceEmoji(text, this.textView.getPaint().getFontMetricsInt(), AndroidUtilities.dp(14.0f), false));
        if (resId != 0) {
            this.buttonView.setImageResource(resId);
            this.buttonView.setVisibility(0);
            return;
        }
        this.buttonView.setVisibility(4);
    }

    public void setOnIconClickListener(OnClickListener onIconClickListener) {
        this.buttonView.setOnClickListener(onIconClickListener);
    }

    public void invalidate() {
        this.textView.invalidate();
        super.invalidate();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.empty) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), 1073741824), MeasureSpec.makeMeasureSpec(1, 1073741824));
        } else {
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), 1073741824), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(24.0f), 1073741824));
        }
    }
}
