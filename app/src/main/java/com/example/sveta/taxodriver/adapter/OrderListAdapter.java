package com.example.sveta.taxodriver.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sveta.taxodriver.R;
import com.example.sveta.taxodriver.data.Coords;
import com.example.sveta.taxodriver.data.Order;
import com.example.sveta.taxodriver.tools.LocationConverter;

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
        Order order;
        TextView priceTextView;

        public OrdersViewHolder(View itemView, ItemClickListener clickListener) {
            super(itemView);

            fromTextView = (TextView) itemView.findViewById(R.id.listitem_from_textview);
            toTextView = (TextView) itemView.findViewById(R.id.listitem_to_textview);
            stopCountTextView = (TextView) itemView.findViewById(R.id.listitem_stopcount_textview);
            priceTextView = (TextView) itemView.findViewById(R.id.listitem_price_textview);
            this.clickListener = clickListener;
            itemView.setOnClickListener(this);
        }

        void bind(Order order) {
            this.order = order;
            stopCountTextView.setText(Integer.toString(order.getToCoords().size() - 1));
            priceTextView.setText(Double.toString(order.getPrice()) + " " + context.getResources().getString(R.string.currency_uah));
            fromTextView.setText(LocationConverter.getCompleteAddressString(context, order.getFromCoords().getLatitude(),
                    order.getFromCoords().getLongitude()));
            Coords toCoords = order.getToCoords().get(order.getToCoords().size() - 1);
            toTextView.setText(LocationConverter.getCompleteAddressString(context, toCoords.getLatitude(), toCoords.getLongitude()));
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                clickListener.onItemClick(order, getAdapterPosition());
            }
        }

    }
}
