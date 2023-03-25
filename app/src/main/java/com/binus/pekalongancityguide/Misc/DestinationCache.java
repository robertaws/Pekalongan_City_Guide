package com.binus.pekalongancityguide.Misc;

import android.util.LruCache;

import com.binus.pekalongancityguide.ItemTemplate.Destination;

import java.util.ArrayList;

public class DestinationCache extends LruCache<String, ArrayList<Destination>> {

    public DestinationCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(String key, ArrayList<Destination> value) {
        return value.size() * 4;
    }
}

