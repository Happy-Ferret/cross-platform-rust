/* -*- Mode: Java; c-basic-offset: 4; tab-width: 20; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.mozilla.toodle;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mozilla.toodle.rust.ItemUuidsCallback;
import com.mozilla.toodle.rust.ItemsChangedCallback;
import com.mozilla.toodle.rust.ListManager;
import com.mozilla.toodle.rust.Toodle;
import com.mozilla.toodle.rust.Uuid;
import com.mozilla.toodle.rust.UuidSet;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
    private String[] dataset;
    private final Context context;

    // We must keep reference to the callback around, otherwise it'll get GC'd and the native code
    // will call an empty stub instead of our callback.
    private final ItemsChangedCallback itemsChangedCallback = new ItemsChangedCallback() {
        @Override
        public void items() {
            Log.i("RustyToodleJava", "Got new items!");
            try (final Toodle toodle = new Toodle(context)) {
                try (final ListManager listManager = toodle.getListManager()) {
                    listManager.getAllUuids(new ItemUuidsCallback() {
                        @Override
                        public void uuids(UuidSet.ByReference uuidSet) {
                            final List<Uuid> uuids = uuidSet.getUuids();
                            final String[] uuidsArray = new String[uuids.size()];
                            for (int i = 0; i < uuids.size(); i++) {
                                uuidsArray[i] = uuids.get(i).uuid;
                            }
                            dataset = uuidsArray;
                            Log.i("RustyToodleJava", "Got uuids! " + uuids);
                        }
                    });
                }
            }
        }
    };

    ListAdapter(Context context) {
        this.context = context;
        this.dataset = new String[] {"One", "Two"};

        try (final Toodle toodle = new Toodle(context)) {
            try (final ListManager listManager = toodle.getListManager()) {
                listManager.registerChangedItemsCallback(itemsChangedCallback);
            }
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout itemView;

        ViewHolder(LinearLayout v) {
            super(v);
            itemView = v;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item, parent, false);

        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ((TextView) holder.itemView.findViewById(R.id.itemTitle)).setText(dataset[position]);
    }

    @Override
    public int getItemCount() {
        return dataset.length;
    }
}
