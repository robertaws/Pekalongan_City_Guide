package com.binus.pekalongancityguide.Misc;

import android.widget.Filter;

import com.binus.pekalongancityguide.Adapter.DestinationAdapter;
import com.binus.pekalongancityguide.Adapter.IterAdapter;
import com.binus.pekalongancityguide.ItemTemplate.Destination;

import java.util.ArrayList;

public class FilterIterUser extends Filter {
    ArrayList<Destination> filter;
    private final IterAdapter iterAdapter;

    public FilterIterUser(ArrayList<Destination> filter, IterAdapter iterAdapter){
        this.filter = filter;
        this.iterAdapter = iterAdapter;
    }
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        if(constraint!=null && constraint.length()>0){
            constraint = constraint.toString().toUpperCase();
            ArrayList<Destination> filtered = new ArrayList<>();
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
        iterAdapter.destinations = (ArrayList<Destination>)results.values;
        iterAdapter.notifyDataSetChanged();
    }
}
