package com.example.sampleblank.util;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sampleblank.R;

public class viewHolder extends RecyclerView.ViewHolder {

    public ConstraintLayout constraintLayout;

    public TextView storeTitle;
    public TextView storeOffer;
    public TextView storeOfferDescription;
    public TextView storeOfferExpiration;
    public ImageView storeLogo;

    public viewHolder(@NonNull View itemView) {
        super(itemView);

        constraintLayout = itemView.findViewById(R.id.root_layout);
        storeTitle = itemView.findViewById(R.id.storeTitle);
        storeOffer = itemView.findViewById(R.id.storeOffer);
        storeOfferDescription = itemView.findViewById(R.id.storeOfferDescription);
        storeOfferExpiration = itemView.findViewById(R.id.storeOfferExpiration);
        storeLogo = itemView.findViewById(R.id.storeLogo);
    }

    public void setStoreTitle(String string) {
        storeTitle.setText(string);
    }

    public void setStoreOffer(String string) {
        storeOffer.setText(string);
    }

    public void setStoreOfferDescription(String string) {
        storeOfferDescription.setText(string);
    }

    public void setStoreOfferExpiration(String string) {
        storeOfferExpiration.setText(string);
    }
}