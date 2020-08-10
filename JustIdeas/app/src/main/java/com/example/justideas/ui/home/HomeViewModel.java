package com.example.justideas.ui.home;

import android.media.Image;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.justideas.R;

public class HomeViewModel extends RecyclerView.ViewHolder implements View.OnClickListener {


    public TextView productName, productPrice, productCategory;
    public ImageView imageView;
    public ItemClickListener Listener;

    public HomeViewModel(View itemView)
    {
        super(itemView);

        imageView = (ImageView) itemView.findViewById(R.id.product_image);
        productName = (TextView) itemView.findViewById(R.id.product_name);
        productPrice = (TextView) itemView.findViewById(R.id.product_price);
        productCategory = (TextView) itemView.findViewById(R.id.product_category);
    }

    public void setItemClickListener(ItemClickListener Listener)
    {
        this.Listener = Listener;
    }

    @Override
    public void onClick(View v) {

        View view;
        Listener.onClick(v, getAdapterPosition(), false);
    }
}