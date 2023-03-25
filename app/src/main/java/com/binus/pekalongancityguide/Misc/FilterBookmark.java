package com.binus.pekalongancityguide.Misc;

import android.widget.Filter;

import com.binus.pekalongancityguide.Adapter.AdminDestinationAdapter;
import com.binus.pekalongancityguide.Adapter.BookmarkAdapter;
import com.binus.pekalongancityguide.ItemTemplate.Destination;

import java.util.ArrayList;

public class FilterBookmark extends Filter {
    ArrayList<Destination> filter;
    private BookmarkAdapter bookmarkAdapter;

    public FilterBookmark(ArrayList<Destination> filter, BookmarkAdapter bookmarkAdapter) {
        this.filter = filter;
        this.bookmarkAdapter = bookmarkAdapter;
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
        bookmarkAdapter.destiArray = (ArrayList<Destination>)results.values;
        bookmarkAdapter.notifyDataSetChanged();
    }
}
