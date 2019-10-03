package com.mycheckins;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

// Handles database connectivity and operations
public class ItemsContentProvider extends ContentProvider {

    public static final String PROVIDER_NAME = "ItemsContentProvider";
    public static final String URL = "content://" + PROVIDER_NAME;
    public static final int ITEMS_URI_CODE = 1;

    public static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private SQLiteDatabase database;

    // Initialize the content provider
    public ItemsContentProvider() {
        uriMatcher.addURI(PROVIDER_NAME, "items", ITEMS_URI_CODE);
    }

    // Return the associated table name of a given URI
    private String getAssociatedTableName(Uri uri) {
        switch(uriMatcher.match(uri)) {
            case ITEMS_URI_CODE:
                return "items";
        }

        return null;
    }

    // Get the type of Uri
    @Override
    public String getType(Uri uri) {
        return getAssociatedTableName(uri);
    }

    // Create the database
    @Override
    public boolean onCreate() {
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        database  = dbHelper.getWritableDatabase();
        return database != null;
    }

    // Insert a new data to the database
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String tableName = getAssociatedTableName(uri);

        if(tableName == null)
            throw new IllegalArgumentException();

        long rowId = database.insert(tableName,"", values);

        if(rowId <= 0)
            throw new SQLException();

        uri = ContentUris.withAppendedId(Uri.parse(URL + "/" + tableName), rowId);
        getContext().getContentResolver().notifyChange(uri, null);
        return uri;
    }

    // Update a data to the database
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Set what table to update
        String tableName = getAssociatedTableName(uri);

        if(tableName == null)
            throw new IllegalArgumentException();

        // Do update
        int count = database.update(tableName, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    // Perform a delete
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Set what table to delete unto
        String tableName = getAssociatedTableName(uri);

        if(tableName == null)
            throw new IllegalArgumentException();

        int count = database.delete(tableName, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    // Query data from the database
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // Set what table to use
        String tableName = getAssociatedTableName(uri);

        if(tableName == null)
            throw new IllegalArgumentException();

        queryBuilder.setTables(tableName);

        // Set a default sort order
        if(sortOrder == null || sortOrder.isEmpty())
            sortOrder = "_id";

        // Do query and return
        Cursor cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    // Database helper for managing database creation and dropping
    private static class DatabaseHelper extends SQLiteOpenHelper {
        public static final int DB_VERSION = 1;
        public static final String DB_NAME = "MyCheckinsDB";

        // Initialize the database helper
        public DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        // Create the database table
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE items (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "title VARCHAR(50) NOT NULL, " +
                    "place VARCHAR(50) NOT NULL, " +
                    "details VARCHAR(255) NOT NULL, " +
                    "checkin_date VARCHAR(10) NOT NULL, " +
                    "location VARCHAR(30) NOT NULL " +
                    ")");
        }

        // Drop old table if version changed
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if(oldVersion == newVersion)
                return;

            db.execSQL("DROP TABLE items");
        }
    }
}
