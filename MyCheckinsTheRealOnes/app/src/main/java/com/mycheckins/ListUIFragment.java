package com.mycheckins;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class ListUIFragment extends Fragment {

    private View fragmentView;

    // Show the layout for the list user interface
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        fragmentView = inflater.inflate(R.layout.fragment_list_ui, container, false);

        // Display all the items
        String[] neededColumns = { "_id", "title", "checkin_date", "place" };

        Cursor cursor = getActivity().getContentResolver().query(
                Uri.parse(ItemsContentProvider.URL + "/items"),
                neededColumns, null, null, null);

        ItemsCursorAdapter cursorAdapter = new ItemsCursorAdapter(getActivity(), cursor, 0);
        ListView listViewItems = fragmentView.findViewById(R.id.listview_items);
        listViewItems.setAdapter(cursorAdapter);

        // Make the "new" button work by showing the fragment for adding a new item
        fragmentView.findViewById(R.id.button_new).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                MainActivity.getInstance().showItemUIFragment(null);
            }
        });

        // Make the "help" button work by showing an activity for viewing the info
        fragmentView.findViewById(R.id.button_help).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), HelpActivity.class);
                startActivity(intent);
            }
        });

        // Make the list view to allow to select an item and then it gets sent to the listui item for viewing
        listViewItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                String itemId = cursor.getString(cursor.getColumnIndex("_id"));

                // Pass item's id to the item UI fragment for loading
                MainActivity.getInstance().showItemUIFragment(itemId);
            }
        });

        return fragmentView;
    }
}
