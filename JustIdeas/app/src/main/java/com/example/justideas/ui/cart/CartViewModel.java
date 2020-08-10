package com.example.justideas.ui.cart;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.justideas.R;

public class CartViewModel extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView pTitle, pPrice, pColor, pSize, pQuantity, pCategory;
    public ImageView pImage;
    public ItemClickListener itemClickListener;

    public CartViewModel(View itemView) {
        super(itemView);

        pImage = (ImageView) itemView.findViewById(R.id.basket_product_image);
        pTitle = (TextView) itemView.findViewById(R.id.pTitleBskt);
        pPrice = (TextView) itemView.findViewById(R.id.pPriceBskt);
        pCategory = (TextView) itemView.findViewById(R.id.pCategoryBskt);
        pSize = (TextView) itemView.findViewById(R.id.pSizeBskt);
        pColor = (TextView) itemView.findViewById(R.id.pColorBskt);
        pQuantity = (TextView) itemView.findViewById(R.id.pQuantityBskt);
    }


    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v, getAdapterPosition(), false);
    }

    public void setItemClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }
}