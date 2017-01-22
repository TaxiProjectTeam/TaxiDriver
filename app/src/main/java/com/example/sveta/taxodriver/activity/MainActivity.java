package com.example.sveta.taxodriver.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.sveta.taxodriver.data.Order;
import com.example.sveta.taxodriver.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MainActivity extends AppCompatActivity {

    public static final String ORDERS_CHILD = "orders";
    public static final String TAG = "Taxo";
    private DatabaseReference databaseReference;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private ProgressBar progressBar;


    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView startingStreet;
        private TextView startingHouse;
        private TextView startingEntrance;
        private TextView destinationStreet;
        private TextView destinationHouse;

        public OrderViewHolder(View v) {
            super(v);
            startingStreet = (TextView) itemView.findViewById(R.id.address_from);
            startingHouse = (TextView) itemView.findViewById(R.id.house_from);
            startingEntrance = (TextView) itemView.findViewById(R.id.entrance);
            destinationStreet = (TextView) itemView.findViewById(R.id.address_to);
            destinationHouse = (TextView) itemView.findViewById(R.id.house_to);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseReference = FirebaseDatabase.getInstance().getReference("orders");

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        recyclerView = (RecyclerView) findViewById(R.id.orders_item);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Order, OrderViewHolder>(
                Order.class,
                R.layout.list_order,
                OrderViewHolder.class,
                databaseReference.child(ORDERS_CHILD)) {

            @Override
            protected Order parseSnapshot(DataSnapshot snapshot) {
                Order order = super.parseSnapshot(snapshot);
                if (order != null) {
                    order.setId(snapshot.getKey());
                }
                return order;
            }

            @Override
            protected void populateViewHolder(OrderViewHolder viewHolder, Order order, int position) {
                progressBar.setVisibility(ProgressBar.INVISIBLE);
                viewHolder.startingStreet.setText(order.getStartingStreet());
                viewHolder.startingHouse.setText(order.getStartingHouse());
                viewHolder.startingEntrance.setText(order.getStartingEntrance());
                viewHolder.destinationStreet.setText(order.getDestinationStreet());
                viewHolder.destinationHouse.setText(order.getDestinationHouse());

            }

        };

//        firebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
//            @Override
//            public void onItemRangeInserted(int positionStart, int itemCount) {
//                super.onItemRangeInserted(positionStart, itemCount);
//                int friendlyMessageCount = firebaseRecyclerAdapter.getItemCount();
//                int lastVisiblePosition =
//                        linearLayoutManager.findLastCompletelyVisibleItemPosition();
//                // If the recycler view is initially being loaded or the
//                // user is at the bottom of the list, scroll to the bottom
//                // of the list to show the newly added message.
//                if (lastVisiblePosition == -1 ||
//                        (positionStart >= (friendlyMessageCount - 1) &&
//                                lastVisiblePosition == (positionStart - 1))) {
//                    recyclerView.scrollToPosition(positionStart);
//                }
//            }
//        });

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }
}
