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
import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.ListCategoryBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.binus.pekalongancityguide.Misc.Constants.FIREBASE_DATABASE_URL;

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
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
            builder.setTitle(R.string.delete_opt)

                    .setMessage(context.getString(R.string.remove_confirm) + " " + category + "?")

                    .setPositiveButton(R.string.yes_txt, (dialog, which) -> {
                        deleteCat(model);
                        Toast.makeText(context,R.string.deleting_item, Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(R.string.no_txt, (dialog, which) -> dialog.dismiss())
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
        String categoryId = model.getId();
        DatabaseReference categoryRef = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL).getReference("Categories");
        categoryRef.child(categoryId)
                .removeValue()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(context,R.string.deleted_category, Toast.LENGTH_SHORT).show();
                    deleteDestinations(categoryId);
                })
                .addOnFailureListener(e -> Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deleteDestinations(String categoryId) {
        DatabaseReference destinationRef = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL).getReference("Destination");
        destinationRef.orderByChild("categoryId").equalTo(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                            dataSnapshot.getRef().removeValue();
                        }
                        Toast.makeText(context,R.string.cat_destination_deleted, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Failed to delete destinations: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
