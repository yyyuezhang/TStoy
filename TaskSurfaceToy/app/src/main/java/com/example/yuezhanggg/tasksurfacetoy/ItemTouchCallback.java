package com.example.yuezhanggg.tasksurfacetoy;

import android.graphics.Canvas;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
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

    private View mDummyView;

    public ItemTouchCallback(int dragDirs, int swipeDirs) {
        super(dragDirs, swipeDirs);
    }

    public void initialize(RecyclerView rv, ItemAdapter adapter, ItemTouchHelper itemTouchHelper,
                           View dummyView) {
        mItemAdapter = adapter;
        mItemTouchHelper = itemTouchHelper;
        mRecyclerView = rv;
        mDummyView = dummyView;
    }

    public void initializeOther(ItemAdapter adapter, ItemTouchHelper itemTouchHelper,
                                RecyclerView recyclerView) {
        // Get the reference of components of the other child recyclerView.
        mOtherItemAdapter = adapter;
        mOtherItemTouchHelper = itemTouchHelper;
        mOtherRecyclerView = recyclerView;
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
        // The thing I am trying to do here is that when you drag an item down to reach item.getY()
        // > 1000, we then:
        //       1. Stop the first ItemTouchHelper from handling this drag event by dispatching
        //          a ACTION_CANCEL.
        //       2. Let the second ItemTouchHelper start handling this drag event by dispatching
        //          a ACTION_DOWN.
        //       3. Call ItemTouchHelper.startDrag() of the second ItemTouchHelper to start
        //          dragging.
        if (mDummyView.getY() > 1000) {
            long downTime = SystemClock.uptimeMillis();
            long eventTime = SystemClock.uptimeMillis();
            int action = MotionEvent.ACTION_CANCEL;
            int x = 20;
            int y = 20;
            int metaState = 0;
            MotionEvent e = MotionEvent.obtain(downTime, eventTime, action, x ,y, metaState);
            mRecyclerView.dispatchTouchEvent(e);

            View otherView = mOtherRecyclerView.findViewHolderForAdapterPosition(0).itemView;
            int action2 = MotionEvent.ACTION_DOWN;
            MotionEvent e2 = MotionEvent.obtain(downTime, eventTime, action2, otherView.getX(), otherView.getY() + mOtherRecyclerView.getTop(), metaState);
            mOtherRecyclerView.dispatchTouchEvent(e2);

            mOtherItemTouchHelper.startDrag(mOtherRecyclerView.findViewHolderForAdapterPosition(0));
        }
    }

    private void updateDummyView(View itemView){
        String text = ((TextView) itemView.findViewById(R.id.text)).getText().toString();
        ((TextView) mDummyView).setText(text);
    }
}
