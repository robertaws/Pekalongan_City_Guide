package com.binus.pekalongancityguide.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.ItemTemplate.Destination;
import com.binus.pekalongancityguide.Layout.AddItinerary;
import com.binus.pekalongancityguide.Layout.DestinationDetails;
import com.binus.pekalongancityguide.Layout.ItineraryPager;
import com.binus.pekalongancityguide.Misc.FilterIterUser;
import com.binus.pekalongancityguide.Misc.ToastUtils;
import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.ListIterBinding;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class IterAdapter extends RecyclerView.Adapter<IterAdapter.HolderDestination> implements Filterable {
    private final Context context;
    private AddItinerary addItinerary;
    private ItineraryPager itineraryPager;
    public ArrayList<Destination> destinations, filterList;
    private ArrayList<Destination> selectedItems = new ArrayList<>();
    private OnItemLongClickListener onItemLongClickListener;
    private ListIterBinding binding;
    private FilterIterUser filterIterUser;
    private static final String TAG = "ADAPTER_USER_TAG";

    public IterAdapter(Context context, ArrayList<Destination> destinations, OnItemLongClickListener onItemLongClickListener, AddItinerary addItinerary, ItineraryPager itineraryPager) {
        this.context = context;
        this.destinations = destinations;
        this.filterList = destinations;
        this.onItemLongClickListener = onItemLongClickListener;
        this.addItinerary = addItinerary;
        this.itineraryPager = itineraryPager;
    }


    @NonNull
    @Override
    public HolderDestination onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = ListIterBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolderDestination(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderDestination holder, int position) {
        Destination destination = destinations.get(position);
        String destiId = destination.getId();
        String title = destination.getTitle();
        holder.title.setText(title);
        loadImage(destination, holder);
        if (selectedItems.contains(destination) && destination.isOpen()) {
            holder.selectButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.checked));
            holder.selectButton.setVisibility(View.VISIBLE);
            holder.layoutImage.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.grayishTint));
        } else {
            holder.selectButton.setVisibility(View.INVISIBLE);
            holder.layoutImage.setBackgroundTintList(null);
        }
        holder.itemView.setOnClickListener(v -> {
            if (selectedItems.isEmpty() && holder.isImageLoaded) {
                Drawable drawable = holder.layoutImage.getBackground();
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 25, stream);
                byte[] byteArray = stream.toByteArray();

                String filePath = context.getFilesDir().getPath() + "/image.png";
                FileOutputStream fos;
                try {
                    fos = new FileOutputStream(filePath);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                try {
                    fos.write(byteArray);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    fos.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Intent intent = new Intent(context, DestinationDetails.class);
                intent.putExtra("destiId", destiId);
                intent.putExtra("imageFilePath", filePath);
                context.startActivity(intent);
            } else {
                if (selectedItems.contains(destination)) {
                    selectedItems.remove(destination);
                } else {
                    if (destination.isOpen()) {
                        selectedItems.add(destination);
                    } else {
                        ToastUtils.showToast(context, "This place is closed", Toast.LENGTH_SHORT);
                    }
                }
                notifyItemChanged(position);
                addItinerary.checkSelect();
            }
        });
    }

    private void loadImage(Destination destination, HolderDestination holder) {
        String imageUrl = destination.getUrl();
        Glide.with(holder.itemView.getContext())
                .asBitmap()
                .load(imageUrl)
                .placeholder(R.drawable.logo)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//                        Log.d(TAG, "on Success: " + destination.getTitle() + "successfully got the file");
                        holder.isImageLoaded = true;
                        BitmapDrawable drawable = new BitmapDrawable(holder.itemView.getResources(), resource);
                        drawable.setGravity(Gravity.FILL);
                        holder.layoutImage.setBackground(drawable);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        Log.d(TAG, "on Failure: failed to getting file from url due to");
                    }
                });
    }

    public void exitSelectMode() {
        for (Destination destination : destinations) {
            destination.setSelected(false);
        }
        selectedItems.clear();
        addItinerary.checkSelect();
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return destinations.size();
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Destination destination);
    }

    @Override
    public Filter getFilter() {
        if (filterIterUser == null) {
            filterIterUser = new FilterIterUser(filterList, this);
        }
        return filterIterUser;
    }

    public ArrayList<Destination> getSelectedItems() {
        return selectedItems;
    }

    class HolderDestination extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        ImageView layoutImage;
        TextView title;
        ImageButton selectButton;
        boolean isImageLoaded;
        RelativeLayout iterLayout;

        public HolderDestination(@NonNull View itemView) {
            super(itemView);
            layoutImage = binding.iterImage;
            title = binding.iterTitle;
            isImageLoaded = false;
            iterLayout = binding.iterLayout;
            selectButton = binding.selectBtn;
            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            ToastUtils.setToastEnabled(true);
            int adapterPosition = getAdapterPosition();
            Destination destination = destinations.get(adapterPosition);
            if (!destination.isSelected()) {
                destination.setSelected(true);
            }
            if (selectedItems.contains(destination)) {
                selectedItems.remove(destination);
                destination.setSelected(false);
            } else if (!destination.isOpen()) {
                selectedItems.remove(destination);
                destination.setSelected(false);
                ToastUtils.showToast(context, "This place is closed", Toast.LENGTH_SHORT);
            } else {
                selectedItems.add(destination);
            }
            notifyItemChanged(adapterPosition);
            onItemLongClickListener.onItemLongClick(destination);
            if (addItinerary != null) {
                addItinerary.checkSelect();
            }
            return true;
        }
    }
}
