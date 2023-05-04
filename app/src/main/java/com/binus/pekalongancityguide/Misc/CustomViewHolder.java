//package com.binus.pekalongancityguide.Misc;
//
//import android.content.ClipData;
//import android.graphics.Color;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.binus.pekalongancityguide.R;
//
//public class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
//    private ImageView imageView;
//    private TextView textView;
//    private AdapterView.OnItemLongClickListener onItemLongClickListener;
//
//    public CustomViewHolder(@NonNull View itemView, AdapterView.OnItemLongClickListener onItemLongClickListener) {
//        super(itemView);
//        this.imageView = itemView.findViewById(R.id.image_view);
//        this.textView = itemView.findViewById(R.id.text_view);
//        this.onItemLongClickListener = onItemLongClickListener;
//        itemView.setOnLongClickListener(this);
//    }
//
//    public void bind(ClipData.Item item) {
//        imageView.setImageResource(());
//        textView.setText(item.getItemName());
//        if (item.isSelected()) {
//            itemView.setBackgroundColor(Color.LTGRAY);
//        } else {
//            itemView.setBackgroundColor(Color.WHITE);
//        }
//    }
//
//    @Override
//    public boolean onLongClick(View v) {
//        onItemLongClickListener.onItemLongClick(getAdapterPosition());
//        return true;
//    }
//}
