package com.example.yuezhanggg.tasksurfacetoy;

import android.content.ClipData;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
    List<Integer> mData;
    private View mDummyView;
    private View mParentView;

    public ItemAdapter(List<Integer> mData, View dummyView, View parentView) {
        this.mData = mData;
        mDummyView = dummyView;
        mParentView = parentView;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.single_item, viewGroup, false);
        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder viewHolder, int i) {
        TextView textView = viewHolder.itemView.findViewById(R.id.text);
        textView.setText(String.valueOf(mData.get(i)));

//        viewHolder.itemView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                ClipData data = ClipData.newPlainText("", "");
//                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
//                        mDummyView);
//                mParentView.startDrag(data, shadowBuilder, mDummyView, 0);
//                return false;
//            }
//        });

        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                        mDummyView);
                mParentView.startDrag(data, shadowBuilder, mDummyView, 0);
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public void onItemMoved(int from, int to) {
        Integer item = mData.remove(from);
        mData.add(to, item);
        notifyItemMoved(from, to);
    }

    public Integer onItemRemoved(int i) {
        Integer item = mData.remove(i);
        notifyItemRemoved(i);
        return item;
    }

    public void onItemAdded(int item, int index) {
        mData.add(index, item);
        notifyItemInserted(index);
    }
}
