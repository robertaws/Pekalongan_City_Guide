package com.binus.pekalongancityguide.Misc;

import android.widget.Filter;

import com.binus.pekalongancityguide.Adapter.AdminDestinationAdapter;
import com.binus.pekalongancityguide.Adapter.CategoryAdapter;
import com.binus.pekalongancityguide.ItemTemplate.Categories;
import com.binus.pekalongancityguide.ItemTemplate.DestinationAdmin;

import java.util.ArrayList;

public class FilterDestiAdmin extends Filter {
    ArrayList<DestinationAdmin> filter;
    private
    AdminDestinationAdapter adminDestinationAdapter;

    public FilterDestiAdmin(ArrayList<DestinationAdmin> filter, AdminDestinationAdapter adminDestinationAdapter) {
        this.filter = filter;
        this.adminDestinationAdapter = adminDestinationAdapter;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        if(constraint!=null && constraint.length()>0){
            constraint = constraint.toString().toUpperCase();
            ArrayList<DestinationAdmin> filtered = new ArrayList<>();
            for(int i=0;i<filter.size();i++){
                if(filter.get(i).getTitle().toUpperCase().contains(constraint)){
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
        adminDestinationAdapter.destinationAdminArrayList = (ArrayList<DestinationAdmin>)results.values;
        adminDestinationAdapter.notifyDataSetChanged();
    }
}
