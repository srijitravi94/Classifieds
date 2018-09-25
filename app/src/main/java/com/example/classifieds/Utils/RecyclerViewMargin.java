package com.example.classifieds.Utils;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import static com.nostra13.universalimageloader.core.ImageLoader.TAG;

public class RecyclerViewMargin extends RecyclerView.ItemDecoration {

    private int columns;
    private int margin;

    public RecyclerViewMargin(int columns, int margin) {
        this.columns = columns;
        this.margin = margin;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildLayoutPosition(view);
        outRect.right = margin;
        outRect.bottom = margin;

        if(position < columns) {
            outRect.top = margin;
        }

        if(position % columns == 0) {
            outRect.left = margin;
        }
    }
}
