package com.example.yuezhanggg.tasksurfacetoy;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    List<List<Integer>> mData;
    private View mDummyView;

    public RecyclerViewAdapter(List<List<Integer>> mData, View dummyView) {
        this.mData = mData;
        this.mDummyView = dummyView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        RecyclerView rv = (RecyclerView) LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.child_recycler_view_layout, viewGroup, false);
        return new ViewHolder(rv);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        // Initialize child recyclerView.
        RecyclerView rv = (RecyclerView) viewHolder.itemView;
        ItemAdapter itemAdapter = new ItemAdapter(mData.get(i));
        rv.setAdapter(itemAdapter);
        rv.setLayoutManager(new LinearLayoutManager(viewHolder.itemView.getContext(),
                LinearLayoutManager.HORIZONTAL, false));

        ItemTouchCallback callback = new ItemTouchCallback(0,0);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        callback.initialize(rv, itemAdapter, itemTouchHelper, mDummyView);
        itemTouchHelper.attachToRecyclerView(rv);

        viewHolder.setItemAdapter(itemAdapter);
        viewHolder.setItemTouchHelper(itemTouchHelper);
        viewHolder.setItemTouchCallback(callback);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ItemAdapter mItemAdapter;
        ItemTouchHelper mItemTouchHelper;
        ItemTouchCallback mItemTouchCallback;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void setItemAdapter(ItemAdapter mItemAdapter) {
            this.mItemAdapter = mItemAdapter;
        }

        void setItemTouchHelper(ItemTouchHelper mItemTouchHelper) {
            this.mItemTouchHelper = mItemTouchHelper;
        }

        void setItemTouchCallback(ItemTouchCallback itemTouchCallback) {
            this.mItemTouchCallback = itemTouchCallback;
        }
    }
}
