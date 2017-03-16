package com.ck.taxoteam.taxodriver.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ck.taxoteam.taxodriver.R;
import com.ck.taxoteam.taxodriver.data.Coords;
import com.ck.taxoteam.taxodriver.tools.LocationConverter;

import java.util.ArrayList;

/**
 * Created by bogdan on 17.02.17.
 */

public class AddressListAdapter extends RecyclerView.Adapter<AddressListAdapter.AddressViewHolder> {

    private ArrayList<Coords> addressCoords;
    private Context context;

    public AddressListAdapter(Context context, ArrayList<Coords> addressCoords) {
        this.context = context;
        this.addressCoords = addressCoords;
    }

    @Override
    public AddressViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        AddressViewHolder holder;
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_address_list_item, parent, false);
        holder = new AddressViewHolder(layoutView);
        return holder;
    }

    @Override
    public void onBindViewHolder(AddressViewHolder holder, int position) {
        holder.bind(context, addressCoords.get(position));
    }


    @Override
    public int getItemCount() {
        return addressCoords.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class AddressViewHolder extends RecyclerView.ViewHolder {

        TextView addressText;

        public AddressViewHolder(View itemView) {
            super(itemView);
            addressText = (TextView) itemView.findViewById(R.id.address_list_item_address_text);
        }

        public void bind(Context context, Coords coords) {
            addressText.setText(LocationConverter.getCompleteAddressString(context, coords.getLatitude(), coords.getLongitude()));
        }
    }
}
