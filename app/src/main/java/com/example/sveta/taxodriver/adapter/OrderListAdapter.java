package com.example.sveta.taxodriver.adapter;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sveta.taxodriver.R;
import com.example.sveta.taxodriver.data.Coords;
import com.example.sveta.taxodriver.data.Order;

import java.util.List;
import java.util.Locale;

/**
 * Created by bohdan on 07.02.17.
 */

public class OrderListAdapter extends RecyclerView.Adapter<OrderListAdapter.OrdersViewHolder> {

    private List<Order> orders;
    private Context context;

    public OrderListAdapter(Context context, List<Order> orders) {
        this.orders = orders;
        this.context = context;
    }


    @Override
    public OrdersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        OrdersViewHolder holder = null;
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_order_list_item, parent, false);
        holder = new OrdersViewHolder(layoutView);
        return holder;
    }


    @Override
    public void onBindViewHolder(OrdersViewHolder holder, int position) {
        holder.stopCountTextView.setText(orders.get(position).getToCoords().size());
        holder.priceTextView.setText(Double.toString(orders.get(position).getPrice()));
        holder.fromTextView.setText(getCompleteAddressString(orders.get(position).getFromCoords().getLatitude(),
                orders.get(position).getFromCoords().getLongitude()));
        Coords toCoords = orders.get(position).getToCoords().get(orders.get(position).getToCoords().size() - 1);
        holder.toTextView.setText(getCompleteAddressString(toCoords.getLatitude(), toCoords.getLongitude()));
    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strAdd;
    }


    @Override
    public int getItemCount() {
        if (orders != null) {
            return orders.size();
        }
        return 0;
    }

    class OrdersViewHolder extends RecyclerView.ViewHolder {

        TextView fromTextView;
        TextView toTextView;
        TextView stopCountTextView;
        TextView priceTextView;

        public OrdersViewHolder(View itemView) {
            super(itemView);

            fromTextView = (TextView) itemView.findViewById(R.id.listitem_from_textview);
            toTextView = (TextView) itemView.findViewById(R.id.listitem_to_textview);
            stopCountTextView = (TextView) itemView.findViewById(R.id.listitem_stopcount_textview);
            priceTextView = (TextView) itemView.findViewById(R.id.listitem_price_textview);
        }
    }
}
