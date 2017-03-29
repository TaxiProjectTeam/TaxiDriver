package com.ck.taxoteam.taxodriver.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ck.taxoteam.taxodriver.R;
import com.ck.taxoteam.taxodriver.data.Order;

import java.util.List;

/**
 * Created by bohdan on 07.02.17.
 */

public class OrderListAdapter extends RecyclerView.Adapter<OrderListAdapter.OrdersViewHolder> {

    private List<Order> orders;
    private Context context;
    private ItemClickListener clickListener;

    public OrderListAdapter(Context context, List<Order> orders, ItemClickListener clickListener) {
        this.orders = orders;
        this.context = context;
        this.clickListener = clickListener;
    }


    @Override
    public OrdersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        OrdersViewHolder holder;
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_order_list_item, parent, false);
        holder = new OrdersViewHolder(layoutView, clickListener);
        return holder;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(OrdersViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }




    @Override
    public int getItemCount() {
        if (orders != null) {
            return orders.size();
        }
        return 0;
    }

    public interface ItemClickListener {
        void onItemClick(Order order, int position);
    }

    class OrdersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ItemClickListener clickListener;
        TextView fromTextView;
        TextView toTextView;
        TextView stopCountTextView;
        TextView statusTextView;
        Order order;
        TextView priceTextView;

        public OrdersViewHolder(View itemView, ItemClickListener clickListener) {
            super(itemView);

            fromTextView = (TextView) itemView.findViewById(R.id.listitem_from_textview);
            toTextView = (TextView) itemView.findViewById(R.id.listitem_to_textview);
            stopCountTextView = (TextView) itemView.findViewById(R.id.listitem_stopcount_textview);
            priceTextView = (TextView) itemView.findViewById(R.id.listitem_price_textview);
            statusTextView = (TextView) itemView.findViewById(R.id.listitem_status_textview);
            this.clickListener = clickListener;
            itemView.setOnClickListener(this);
        }

        void bind(Order order) {
            this.order = order;
            stopCountTextView.setText(String.valueOf(order.getToCoords().size() - 1));
            priceTextView.setText(Double.toString(order.getPrice()) + " " + context.getResources().getString(R.string.currency_uah));
            fromTextView.setText(order.getFromAddress());
            switch (order.getStatus()){
                case "free":
                    statusTextView.setText(context.getResources().getString(R.string.status_free_text));
                    break;
                case "accepted":
                    statusTextView.setText(context.getResources().getString(R.string.status_accepted_text));
                    break;
                case "arrived":
                    statusTextView.setText(context.getResources().getString(R.string.status_accepted_text));
                    break;
                case "completed":
                    statusTextView.setText(context.getResources().getString(R.string.status_completed_text));
                    break;
            }
            String toAddress = "";
            if (order.getToAdress().size() != 0) {
                toAddress = order.getToAdress().get(order.getToAdress().size() - 1);
            }
            toTextView.setText(toAddress);
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                clickListener.onItemClick(order, getAdapterPosition());
            }
        }

    }
}
