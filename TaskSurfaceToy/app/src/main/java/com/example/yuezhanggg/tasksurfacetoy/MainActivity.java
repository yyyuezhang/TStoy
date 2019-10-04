package com.example.yuezhanggg.tasksurfacetoy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ViewGroup mParent;
    private RecyclerView mParentRecyclerView;
    private View mDummyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        List<List<Integer>> data = new ArrayList<>();
        data.add(new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5)));
        data.add(new ArrayList<>(Arrays.asList(6, 7, 8, 9, 10)));

        mParent = findViewById(R.id.parent_view);
        mDummyView = findViewById(R.id.dummy_view);

        mParentRecyclerView = findViewById(R.id.parent_recycler_view);
        mParentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(data, mDummyView);
        mParentRecyclerView.setAdapter(adapter);

        mParentRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                RecyclerViewAdapter.ViewHolder child1 = (RecyclerViewAdapter.ViewHolder)
                        mParentRecyclerView.findViewHolderForAdapterPosition(0);
                RecyclerViewAdapter.ViewHolder child2 = (RecyclerViewAdapter.ViewHolder)
                        mParentRecyclerView.findViewHolderForAdapterPosition(1);

                child1.mItemTouchCallback.initializeOther(child2.mItemAdapter,
                        child2.mItemTouchHelper, (RecyclerView)
                                mParentRecyclerView.findViewHolderForAdapterPosition(1).itemView);
                child2.mItemTouchCallback.initializeOther(child1.mItemAdapter,
                        child1.mItemTouchHelper, (RecyclerView)
                                mParentRecyclerView.findViewHolderForAdapterPosition(0).itemView);
            }
        });
    }
}
