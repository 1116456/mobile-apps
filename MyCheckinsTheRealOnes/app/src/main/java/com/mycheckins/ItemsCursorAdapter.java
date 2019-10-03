package com.mycheckins;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

// A cursor adapter is a bridge between the list UI and the data from the database
public class ItemsCursorAdapter extends CursorAdapter {

    private LayoutInflater layoutInflater;

    // Create a curs
    public ItemsCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);

        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // Bind the data from the database to the list user interface
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String title = cursor.getString(cursor.getColumnIndex("title"));
        String checkinDate = cursor.getString(cursor.getColumnIndex("checkin_date"));
        String place = cursor.getString(cursor.getColumnIndexOrThrow("place"));

        ((TextView)view.findViewById(R.id.textview_title)).setText(title);
        ((TextView)view.findViewById(R.id.textview_date)).setText("Date: " + checkinDate);
        ((TextView)view.findViewById(R.id.textview_place)).setText("Place: " + place);
    }

    // Inflate the layout to be used for displaying
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return layoutInflater.inflate(R.layout.listui_row, parent, false);
    }
}
