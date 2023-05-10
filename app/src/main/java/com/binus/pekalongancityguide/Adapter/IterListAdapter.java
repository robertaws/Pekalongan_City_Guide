package com.binus.pekalongancityguide.Adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.ItemTemplate.Iter;
import com.binus.pekalongancityguide.Misc.AlphaTransformation;
import com.binus.pekalongancityguide.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.List;

public class IterListAdapter extends RecyclerView.Adapter<IterListAdapter.ItineraryViewHolder> {
    private final Context context;
    private final List<Iter> itineraryList;

    public IterListAdapter(Context context, List<Iter> itineraryList) {
        this.context = context;
        this.itineraryList = itineraryList;
    }

    @Override
    public ItineraryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_fragment_iter, parent, false);
        return new ItineraryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItineraryViewHolder holder, int position) {
        Iter itinerary = itineraryList.get(position);
        loadImage(itinerary, holder);
        holder.nameTv.setText(itinerary.getItineraryName());
        holder.dateTV.setText(itinerary.getDate());
        holder.itemView.setOnClickListener(v -> {

        });
    }

    @Override
    public int getItemCount() {
        return itineraryList.size();
    }

    private void loadImage(Iter itinerary, ItineraryViewHolder holder) {
        String imageUrl = itinerary.getUrl();
        RequestOptions requestOptions = new RequestOptions()
                .transforms(new CenterCrop(), new AlphaTransformation(1f));

        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .apply(requestOptions)
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        holder.iterImage.setBackground(resource);
                        holder.isImageLoaded = true;
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
    }

    public static class ItineraryViewHolder extends RecyclerView.ViewHolder {
        public ImageView iterImage;
        public TextView nameTv,dateTV;
        public boolean isImageLoaded;

        public ItineraryViewHolder(View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.iterName_tv);
            dateTV = itemView.findViewById(R.id.iterDate_tv);
            iterImage = itemView.findViewById(R.id.iter_bg);
            isImageLoaded = false;
        }
    }
}
