package com.binus.pekalongancityguide.Misc;

import android.widget.Filter;

import com.binus.pekalongancityguide.Adapter.CategoryAdapter;
import com.binus.pekalongancityguide.ItemTemplate.Categories;

import java.util.ArrayList;

public class FilterCategory extends Filter {
    ArrayList<Categories> filter;
    private
    CategoryAdapter categoryAdapter;

    public FilterCategory(ArrayList<Categories> filter, CategoryAdapter categoryAdapter) {
        this.filter = filter;
        this.categoryAdapter = categoryAdapter;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        if(constraint!=null && constraint.length()>0){
            constraint = constraint.toString().toUpperCase();
            ArrayList<Categories> filtered = new ArrayList<>();
            for(int i=0;i<filter.size();i++){
                if(filter.get(i).getCategory().toUpperCase().contains(constraint)){
                    filtered.add(filter.get(i));
                }
            }
            results.count = filtered.size();
            results.values = filtered;
        }else{
            results.count = filter.size();
            results.values = filter;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        categoryAdapter.categoriesArrayList = (ArrayList<Categories>)results.values;
        categoryAdapter.notifyDataSetChanged();
    }
}
