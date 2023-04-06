package com.binus.pekalongancityguide.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.ItemTemplate.Categories;
import com.binus.pekalongancityguide.Layout.ShowDestinationAdmin;
import com.binus.pekalongancityguide.Misc.FilterCategory;
import com.binus.pekalongancityguide.databinding.ListCategoryBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.HolderCategory> implements Filterable {
    private final Context context;
    public ArrayList<Categories> categoriesArrayList,filter;
    private ListCategoryBinding binding;
    private FilterCategory filterCategory;
    public CategoryAdapter(Context context, ArrayList<Categories> categoriesArrayList) {
        this.context = context;
        this.categoriesArrayList = categoriesArrayList;
        this.filter = categoriesArrayList;
    }

    @NonNull
    @Override
    public HolderCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = ListCategoryBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolderCategory(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCategory holder, int position) {
        Categories model = categoriesArrayList.get(position);
        String id = model.getId();
        String category = model.getCategory();
        holder.title.setText(category);
        holder.delete.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Delete")
                    .setMessage("Are you sure want to delete this item?")
                    .setPositiveButton("Confirm", (dialog, which) -> {
                        deleteCat(model);
                        Toast.makeText(context, "Deleting item...", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();

        });
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ShowDestinationAdmin.class);
            intent.putExtra("categoryId", id);
            intent.putExtra("categoryTitle", category);
            context.startActivity(intent);
        });
    }

    private void deleteCat(Categories model) {
        String id = model.getId();
        DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Categories");
        reference.child(id)
                .removeValue()
                .addOnSuccessListener(unused -> Toast.makeText(context, "Item deleted succesfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return categoriesArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filterCategory == null){
            filterCategory = new FilterCategory(filter,this);
        }
        return filterCategory;
    }

    class HolderCategory extends RecyclerView.ViewHolder{
        TextView title;
        ImageButton delete;
        public HolderCategory(@NonNull View itemView) {
            super(itemView);
            title = binding.catTitle;
            delete = binding.catDelete;
        }
    }
}
