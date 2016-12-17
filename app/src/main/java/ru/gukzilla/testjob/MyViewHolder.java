package ru.gukzilla.testjob;

import android.view.View;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;

/**
 * Created by Evgeniy on 17.12.2016.
 */
class MyViewHolder extends AbstractDraggableItemViewHolder {
    TextView textView;

    public MyViewHolder(View itemView) {
        super(itemView);
        textView = (TextView) itemView.findViewById(android.R.id.text1);
    }
}
