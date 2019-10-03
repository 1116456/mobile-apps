package com.mycheckins;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;

import static android.app.Activity.RESULT_OK;

public class ItemUIFragment extends Fragment {

    private View fragmentView;
    private Bitmap imageBitmap;

    // Show the layout for an item user interface
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        fragmentView = inflater.inflate(R.layout.fragment_item_ui, container, false);

        // Check if we're doing a create or a view
        if(getArguments() != null && getArguments().containsKey("item_id"))
            initializeViewItemUi();
        else
            initializeCreateItemUi();

        // Make the button that shows the map work
        fragmentView.findViewById(R.id.button_showmap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MapsActivity.class);

                // Send the recorded location
                intent.putExtra("location", ((TextView)fragmentView.findViewById(R.id.textview_location)).getText().toString());
                startActivity(intent);
            }
        });

        return fragmentView;
    }

    // Set the controls for viewing an item
    private void initializeViewItemUi() {
        final String itemId = getArguments().getString("item_id");

        // Query the item details
        String[] neededColumns = { "_id", "title", "place", "details", "checkin_date", "location" };
        Cursor cursor = getActivity().getContentResolver().query(
                Uri.parse(ItemsContentProvider.URL + "/items"),
                neededColumns,
                "_id = ?",
                new String[]{ itemId }, null);

        cursor.moveToFirst();

        // Extract the details
        String title = cursor.getString(cursor.getColumnIndex("title"));
        String place = cursor.getString(cursor.getColumnIndexOrThrow("place"));
        String details = cursor.getString(cursor.getColumnIndex("details"));
        String checkinDate = cursor.getString(cursor.getColumnIndex("checkin_date"));
        String location = cursor.getString(cursor.getColumnIndex("location"));

        try {
            FileInputStream fis = getActivity().openFileInput(String.valueOf(itemId));
            imageBitmap = BitmapFactory.decodeStream(fis);
            fis.close();
        } catch(Exception e) {
            Log.e("Image Load", e.getMessage());
        }

        ((EditText) fragmentView.findViewById(R.id.edittext_title)).setText(title);
        ((EditText) fragmentView.findViewById(R.id.edittext_place)).setText(place);
        ((EditText) fragmentView.findViewById(R.id.edittext_details)).setText(details);
        ((Button) fragmentView.findViewById(R.id.button_date)).setText(checkinDate);
        ((TextView) fragmentView.findViewById(R.id.textview_location)).setText(location);
        ((ImageView) fragmentView.findViewById(R.id.imageview_receipt)).setImageBitmap(imageBitmap);

        // Make the share button become a delete button
        Button buttonDelete = fragmentView.findViewById(R.id.button_share);
        buttonDelete.setText("Delete");
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getContentResolver().delete(
                        Uri.parse(ItemsContentProvider.URL + "/items"),
                        "_id = ?",
                        new String[] { itemId }
                );

                MainActivity.getInstance().showListUIFragment();
                getActivity().deleteFile(itemId);
            }
        });

        // Hide the button for taking picture
        fragmentView.findViewById(R.id.button_take_picture).setVisibility(View.GONE);
    }

    // Set the controls for creating an item
    private void initializeCreateItemUi() {
        // Pre-populate the location
        TextView textViewLocation = fragmentView.findViewById(R.id.textview_location);

        if (LocationService.lastLocationRecorded != null)
            textViewLocation.setText(LocationService.lastLocationRecorded.getLatitude() + "," + LocationService.lastLocationRecorded.getLongitude());

        // Create a function for the date button to select a date
        final Button buttonDate = fragmentView.findViewById(R.id.button_date);
        buttonDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int currentYear = calendar.get(Calendar.YEAR);
                int currentMonth = calendar.get(Calendar.MONTH);
                int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

                // Show a date picker dialog when the date button is selected
                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.getInstance(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay) {
                        buttonDate.setText(selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay);
                    }
                }, currentYear, currentMonth, currentDay);

                datePickerDialog.show();
            }
        });

        // Make the share button work, which will save the details to the database
        fragmentView.findViewById(R.id.button_share).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String title = ((EditText) fragmentView.findViewById(R.id.edittext_title)).getText().toString().trim();
                String place = ((EditText) fragmentView.findViewById(R.id.edittext_place)).getText().toString().trim();
                String details = ((EditText) fragmentView.findViewById(R.id.edittext_details)).getText().toString().trim();
                String date = buttonDate.getText().toString();
                String location = ((TextView) fragmentView.findViewById(R.id.textview_location)).getText().toString();

                // Validate that all fields are required
                if (title.isEmpty() || place.isEmpty() || details.isEmpty() || date.equalsIgnoreCase("Select a Date") || location.isEmpty() || imageBitmap == null) {
                    Toast.makeText(MainActivity.getInstance(), "All fields are required.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Insert to database
                ContentValues values = new ContentValues();
                values.put("title", title);
                values.put("place", place);
                values.put("details", details);
                values.put("checkin_date", date);
                values.put("location", location);

                Uri uri = getActivity().getContentResolver().insert(
                        Uri.parse(ItemsContentProvider.URL + "/items"),
                        values);

                long itemId = ContentUris.parseId(uri);

                // Save the image as a file in internal storage
                try {
                    FileOutputStream fos =getActivity().openFileOutput(String.valueOf(itemId), Context.MODE_PRIVATE);
                    imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.close();
                } catch(Exception e) {
                    Log.e("Image Save", e.getMessage());
                }

                // Back to the list
                MainActivity.getInstance().showListUIFragment();
            }
        });

        // Initialize the button to allow taking pictures
        fragmentView.findViewById(R.id.button_take_picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 1);
            }
        });
    }

    // Get the captured image and show it
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_OK)
            return;

        imageBitmap = (Bitmap) data.getExtras().get("data");
        ((ImageView)fragmentView.findViewById(R.id.imageview_receipt)).setImageBitmap(imageBitmap);
    }
}
