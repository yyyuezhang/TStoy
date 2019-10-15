package com.example.yuezhanggg.tasksurfacetoy;

import android.content.ClipData;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.DragEvent;
import android.view.View;

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

    /**
     * Called by the global onDragListener to handle the {@link DragEvent} if needed.
     * @return whether the drag event is handled by this callback.
     */
    public boolean handleDragEvent(DragEvent event) {
        float x = event.getX();
        float y = event.getY();
        Rect parentRect = new Rect();
        mParentView.getGlobalVisibleRect(parentRect);
        // When drag starts, figure out which item in which recyclerView is dragged. Make the
        // dragging item invisible since we have the drag shadow.
        if (event.getAction() == DragEvent.ACTION_DRAG_STARTED) {
            if (outsideRecyclerView(event, mRecyclerView)) return false;
            for (int i = 0; i < mRecyclerView.getAdapter().getItemCount(); i++) {
                RecyclerView.ViewHolder viewHolder = mRecyclerView.findViewHolderForAdapterPosition(i);
                if (viewHolder == null) continue;
                Rect rect = getChildPositionRect(viewHolder.itemView, parentRect.top);
                if (isOverlap(rect.centerX(), rect.centerY(), x, y, rect.width()/2)) {
                    mCurrentItemIndex = i;
                    View currentItemView = viewHolder.itemView;
                    currentItemView.setVisibility(View.GONE);
                    startDraggingDummyView();

                }
            }
        }
        // When drag ends, set the dragging item back to visible.
        else if (event.getAction() == DragEvent.ACTION_DRAG_ENDED) {
            RecyclerView.ViewHolder mCurrentViewHolder =
                    mRecyclerView.findViewHolderForAdapterPosition(mCurrentItemIndex);
            if (!ownDraggingItem() || mCurrentViewHolder == null) return false;

            mCurrentViewHolder.itemView.setVisibility(View.VISIBLE);
            mCurrentItemIndex = -1;

        }
        // During drag.
        else if (event.getAction() == DragEvent.ACTION_DRAG_LOCATION) {
            if (!ownDraggingItem()) return false;

            // Ignore the calls while recyclerView is still animating to get moved items to correct
            // position.
            if (mRecyclerView.isAnimating()) return true;

            // If a handover should happen.
            if (isOnBorder(y)) {
                for (int i = 0; i < mOtherRecyclerView.getAdapter().getItemCount(); i++) {
                    RecyclerView.ViewHolder viewHolder =
                            mOtherRecyclerView.findViewHolderForAdapterPosition(i);
                    if (viewHolder == null) continue;
                    Rect rect = getChildPositionRect(viewHolder.itemView, parentRect.top);
                    if (isOverlap(rect.centerX(), rect.centerY(), x, y, rect.width()/2)) {
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
                                    mOtherRecyclerView.smoothScrollToPosition(otherIndex);
                                    return;
                                }
                                mOtherRecyclerView.findViewHolderForAdapterPosition(otherIndex).itemView.setVisibility(View.GONE);
                            }
                        });
                        mCurrentItemIndex = -1;
                        return true;
                    }
                }
            }

            // If there is no handover, and the drag event is outside of this recyclerView, ignore
            // the drag event.
            if (outsideRecyclerView(event, mRecyclerView)) return false;

            // Decide whether a reorder should happen within the recyclerView.
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
                    return true;
                }
            }
        }
        return false;
    }

    private void startDraggingDummyView() {
        ClipData data = ClipData.newPlainText("", "");
        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                mDummyView);
        mParentView.startDrag(data, shadowBuilder, shadowBuilder, 0);
    }

    /**
     * Called by the other callback to specify the current dragging item when it hands over the
     * ownership of the dragging item to this callback.
     */
    void setCurrentItem(int currentItemIndex) {
        mCurrentItemIndex = currentItemIndex;
    }

    /**
     * @return Whether the touch point is overlapped with an itemView. This is used in multiple
     * places, e.g. figure out the current dragging item when drag starts, decide whether there
     * should be a reorder, etc.
     */
    private static boolean isOverlap(
            float left1, float top1, float left2, float top2, float threshold) {
        return Math.abs(left1 - left2) < threshold && Math.abs(top1 - top2) < threshold;
    }

    /**
     *
     * @param y The y of current drag event.
     * @return  Whether the dragging item has reached the point where the ownership of the dragging
     * item should be handover to the other callback.
     */
    private boolean isOnBorder(float y) {
        return (mRecyclerView.getTop() != 0 && mRecyclerView.getTop() - y > 100) ||
                (mRecyclerView.getTop() == 0 && y - mRecyclerView.getBottom() > 100);
    }

    /**
     * @return Whether the current callback owns the dragging item, i.e. whether this callback
     * should handle this event.
     */
    private boolean ownDraggingItem(){
        return mCurrentItemIndex != -1;
    }

    /**
     * @return Whether current drag event happens within recyclerView.
     */
    private boolean outsideRecyclerView(DragEvent dragEvent, RecyclerView recyclerView) {
        float x = dragEvent.getX();
        float y = dragEvent.getY();
        return y < mRecyclerView.getTop() || y > mRecyclerView.getBottom() ||
                x < mRecyclerView.getLeft() || x > mRecyclerView.getRight();
    }

    private Rect getChildPositionRect(View itemView, int offset) {
        Rect rect = new Rect();
        itemView.getGlobalVisibleRect(rect);
        rect.offset(0, -offset);
        return rect;
    }
}
