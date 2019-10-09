package com.example.yuezhanggg.tasksurfacetoy;

import android.media.browse.MediaBrowser;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    List<List<Integer>> mData;
    List<ItemTouchCallback> mCallbacks = new ArrayList<>();
    private View mDummyView;
    private View mParentView;

    public RecyclerViewAdapter(List<List<Integer>> mData, View dummyView, View parentView) {
        this.mData = mData;
        this.mDummyView = dummyView;
        this.mParentView = parentView;
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
        ItemAdapter itemAdapter = new ItemAdapter(mData.get(i), mDummyView, mParentView);
        rv.setAdapter(itemAdapter);
        if (i == 0) {
            rv.setLayoutManager(new LinearLayoutManager(viewHolder.itemView.getContext(),
                    LinearLayoutManager.HORIZONTAL, false));
        } else {
            rv.setLayoutManager(new GridLayoutManager(viewHolder.itemView.getContext(), 2));
        }

        ItemTouchCallback callback = new ItemTouchCallback(0,0);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        callback.initialize(rv, itemAdapter, itemTouchHelper, mDummyView, mParentView);
        mCallbacks.add(callback);
//        itemTouchHelper.attachToRecyclerView(rv);

        viewHolder.setItemAdapter(itemAdapter);
        viewHolder.setItemTouchHelper(itemTouchHelper);
        viewHolder.setItemTouchCallback(callback);
        viewHolder.setIndex(i);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public List<ItemTouchCallback> getCallbacks() {
        return mCallbacks;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ItemAdapter mItemAdapter;
        ItemTouchHelper mItemTouchHelper;
        ItemTouchCallback mItemTouchCallback;
        int index;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
        void setIndex(int i) {this.index = i;}

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
