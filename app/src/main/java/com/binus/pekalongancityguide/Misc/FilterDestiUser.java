package com.binus.pekalongancityguide.Misc;

import android.widget.Filter;

import com.binus.pekalongancityguide.Adapter.DestinationAdapter;
import com.binus.pekalongancityguide.Adapter.IterAdapter;
import com.binus.pekalongancityguide.ItemTemplate.Destination;

import java.util.ArrayList;

public class FilterDestiUser extends Filter {
    ArrayList<Destination> filter;
    private final DestinationAdapter destinationAdapter;

    public FilterDestiUser(ArrayList<Destination> filter, DestinationAdapter destinationAdapter) {
        this.filter = filter;
        this.destinationAdapter = destinationAdapter;
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
        destinationAdapter.destinations = (ArrayList<Destination>)results.values;
        destinationAdapter.notifyDataSetChanged();
    }
}
