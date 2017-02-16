package com.example.sveta.taxodriver.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.sveta.taxodriver.R;
import com.example.sveta.taxodriver.data.Order;
import com.example.sveta.taxodriver.tools.LocationConverter;

public class OrderDetailsActivity extends AppCompatActivity {

    Order currentOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        //Get Extra
        Bundle data = getIntent().getExtras();
        currentOrder = (Order) data.getParcelable("order");

        showData(currentOrder);
    }

    private void showData(Order order) {
        TextView fromText = (TextView) findViewById(R.id.textview_details_text_from);
        TextView priceText = (TextView) findViewById(R.id.textview_details_text_price);
        TextView commentText = (TextView) findViewById(R.id.textview_details_text_comment);

        //Set data
        fromText.setText(LocationConverter.getCompleteAddressString(this, order.getFromCoords().getLatitude(), order.getFromCoords().getLongitude()));
        priceText.setText(String.valueOf(order.getPrice()) + " " + getResources().getString(R.string.currency_uah));
        if (order.getAdditionalComment() != null) {
            if (!order.getAdditionalComment().equals("")) {
                commentText.setText(order.getAdditionalComment());
            }
        }

    }
}
