package org.telegram.ui.Cells;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.beta.R;
import org.telegram.ui.Components.LayoutHelper;

@SuppressLint({"NewApi"})
public class PhotoAttachCameraCell extends FrameLayout {
    public PhotoAttachCameraCell(Context context) {
        super(context);
        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ScaleType.CENTER);
        imageView.setImageResource(R.drawable.instant_camera);
        imageView.setBackgroundColor(-16777216);
        addView(imageView, LayoutHelper.createFrame(80, 80.0f));
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(86.0f), 1073741824), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(80.0f), 1073741824));
    }
}
