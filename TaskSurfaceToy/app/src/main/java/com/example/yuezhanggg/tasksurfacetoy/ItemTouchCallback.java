package com.example.yuezhanggg.tasksurfacetoy;

import android.content.ClipData;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class ItemTouchCallback extends ItemTouchHelper.SimpleCallback {

    private ItemAdapter mItemAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private RecyclerView mRecyclerView;

    private ItemAdapter mOtherItemAdapter;
    private ItemTouchHelper mOtherItemTouchHelper;
    private RecyclerView mOtherRecyclerView;
    private ItemTouchCallback mOtherItemTouchCallback;

    private View mDummyView;
    private View mParentView;
    private int mStatusbarHeight;

    private int mCurrentItemIndex = -1;

    public ItemTouchCallback(int dragDirs, int swipeDirs) {
        super(dragDirs, swipeDirs);
    }

    public void initialize(RecyclerView rv, ItemAdapter adapter, ItemTouchHelper itemTouchHelper,
                           View dummyView, View parentView) {
        mItemAdapter = adapter;
        mItemTouchHelper = itemTouchHelper;
        mRecyclerView = rv;
        mDummyView = dummyView;
        mParentView = parentView;
    }

    public void initializeOther(ItemAdapter adapter, ItemTouchHelper itemTouchHelper,
                                RecyclerView recyclerView, ItemTouchCallback itemTouchCallback) {
        // Get the reference of components of the other child recyclerView.
        mOtherItemAdapter = adapter;
        mOtherItemTouchHelper = itemTouchHelper;
        mOtherRecyclerView = recyclerView;
        mOtherItemTouchCallback = itemTouchCallback;
    }

    @Override
    public int getSwipeDirs(@NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder) {
        final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT
                | ItemTouchHelper.RIGHT;
        final int swipeFlags = 0;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder from,
                          @NonNull RecyclerView.ViewHolder to) {
        mItemAdapter.onItemMoved(from.getAdapterPosition(), to.getAdapterPosition());
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {}

    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            mDummyView.setVisibility(View.VISIBLE);
            // Make the dummy view looks exactly like the item being dragged.
            updateDummyView(viewHolder.itemView);
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull final RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        mDummyView.setX(viewHolder.itemView.getX());
        mDummyView.setY(viewHolder.itemView.getY() + recyclerView.getTop());
    }

    private void updateDummyView(View itemView){
        String text = ((TextView) itemView.findViewById(R.id.text)).getText().toString();
        ((TextView) mDummyView).setText(text);
    }


    public void handleDragEvent(DragEvent event) {
        float x = event.getX();
        float y = event.getY();
        Rect parentRect = new Rect();
        mParentView.getGlobalVisibleRect(parentRect);

        if (event.getAction() == DragEvent.ACTION_DRAG_STARTED) {
            if (y < mRecyclerView.getTop() || y > mRecyclerView.getBottom() ||
                    x < mRecyclerView.getLeft() || x > mRecyclerView.getRight()) return;
            for (int i = 0; i < mRecyclerView.getAdapter().getItemCount(); i++) {
                RecyclerView.ViewHolder viewHolder = mRecyclerView.findViewHolderForAdapterPosition(i);
                if (viewHolder == null) continue;
                View child = viewHolder.itemView;

                Rect rect = new Rect();
                child.getGlobalVisibleRect(rect);
                rect.offset(0, -parentRect.top);
                if (isOverlap(rect.centerX(), rect.centerY(), x, y,
                        rect.width()/2)) {
                    mCurrentItemIndex = i;
                    View currentItemView = viewHolder.itemView;
                    currentItemView.setVisibility(View.GONE);
                    startDraggingDummyView();

                }
            }

        } else if (event.getAction() == DragEvent.ACTION_DRAG_ENDED) {
            if (mCurrentItemIndex == -1) return;

            mRecyclerView.findViewHolderForAdapterPosition(mCurrentItemIndex).itemView.setVisibility(View.VISIBLE);
            mCurrentItemIndex = -1;

        } else if (event.getAction() == DragEvent.ACTION_DRAG_LOCATION) {
            if (isOnBorder(y) && mCurrentItemIndex != -1) {
                for (int i = 0; i < mOtherRecyclerView.getAdapter().getItemCount(); i++) {
                    RecyclerView.ViewHolder viewHolder = mOtherRecyclerView.findViewHolderForAdapterPosition(i);
                    if (viewHolder == null) continue;
                    View child = viewHolder.itemView;
                    Rect rect = new Rect();
                    child.getGlobalVisibleRect(rect);
                    rect.offset(0, -parentRect.top);
                    if (isOverlap(rect.centerX(), rect.centerY(), x, y,
                            rect.width()/2)) {
                        final int otherIndex = i;

                        Integer data = mItemAdapter.onItemRemoved(mCurrentItemIndex);
                        mOtherItemAdapter.onItemAdded(data, otherIndex);

                        mOtherItemTouchCallback.setCurrentItem(i);
                        mOtherRecyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                RecyclerView.ViewHolder otherViewHolder = mOtherRecyclerView.findViewHolderForAdapterPosition(otherIndex);
                                if (otherViewHolder == null) {
                                    if (mOtherRecyclerView.getLayoutManager() instanceof GridLayoutManager) return;
//                                    LinearLayoutManager manager = (LinearLayoutManager) mOtherRecyclerView.getLayoutManager();
//                                    manager.scrollToPosition(otherIndex);
                                    mOtherRecyclerView.smoothScrollToPosition(otherIndex);
                                    return;
                                }
                                mOtherRecyclerView.findViewHolderForAdapterPosition(otherIndex).itemView.setVisibility(View.GONE);
                            }
                        });
                        mCurrentItemIndex = -1;
                        return;
                    }
                }

            }

            if (y < mRecyclerView.getTop() || y > mRecyclerView.getBottom() ||
                    x < mRecyclerView.getLeft() || x > mRecyclerView.getRight()) return;
            if (mCurrentItemIndex == -1 || mRecyclerView.isAnimating()) return;

            for (int i = 0; i < mRecyclerView.getAdapter().getItemCount(); i++) {
                RecyclerView.ViewHolder toViewHolder = mRecyclerView.findViewHolderForAdapterPosition(i);
                RecyclerView.ViewHolder fromViewHolder = mRecyclerView.findViewHolderForAdapterPosition(mCurrentItemIndex);
                if (toViewHolder == null || fromViewHolder == null || i == mCurrentItemIndex) continue;
                View child = toViewHolder.itemView;
                Rect rect = new Rect();
                child.getGlobalVisibleRect(rect);
                rect.offset(0, -parentRect.top);
                if (isOverlap(rect.centerX(), rect.centerY(), x, y,
                        rect.width()/2)) {
                    onMove(mRecyclerView, fromViewHolder, toViewHolder);
                    mCurrentItemIndex = i;
                    return;
                }
            }
        }
    }

    private static boolean isOverlap(
            float left1, float top1, float left2, float top2, float threshold) {
        return Math.abs(left1 - left2) < threshold && Math.abs(top1 - top2) < threshold;
    }

    private void startDraggingDummyView() {
        ClipData data = ClipData.newPlainText("", "");
        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                mDummyView);
        mParentView.startDrag(data, shadowBuilder, shadowBuilder, 0);
    }

    private boolean isOnBorder(float y) {
        return mRecyclerView.getTop() - y > 100 || y - mRecyclerView.getBottom() > 100;
    }

    public void setCurrentItem(int currentItemIndex) {
        mCurrentItemIndex = currentItemIndex;
    }
}
