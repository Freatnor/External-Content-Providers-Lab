package com.example.freatnor.external_content_providers_lab;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.icu.util.Calendar;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String TAG = "MainActivity";
    private static final int CALENDAR_LOADER = 10;

    private ListView mListView;
    private CursorAdapter mCursorAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = (ListView) findViewById(R.id.calendar_list_view);
//        mCursorAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, null,
//                new String[]{CalendarContract.Events.TITLE, CalendarContract.Events.DTSTART},
//                new int[]{android.R.id.text1, android.R.id.text2}, 0);
        mCursorAdapter = new CursorAdapter(this, null, 0) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
                return LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, viewGroup, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                TextView titleText = (TextView) view.findViewById(android.R.id.text1);
                TextView dateText = (TextView) view.findViewById(android.R.id.text2);
                titleText.setText(cursor.getString(cursor.getColumnIndex(CalendarContract.Events.TITLE)));
                //get the date from the utc milliseconds
                Date date = new Date(cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTSTART)));
                Log.d(TAG, "bindView: date for " + titleText.getText().toString() + " is " + cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTSTART)));
                SimpleDateFormat sdf = new SimpleDateFormat();
                Log.d(TAG, "bindView: " + cursor.getString(cursor.getColumnIndex(CalendarContract.Events.EVENT_TIMEZONE)));
                sdf.setTimeZone(TimeZone.getTimeZone(cursor.getString(cursor.getColumnIndex(CalendarContract.Events.EVENT_TIMEZONE))));
                dateText.setText(sdf.format(date));
            }
        };

        mListView.setAdapter(mCursorAdapter);

        getSupportLoaderManager().initLoader(CALENDAR_LOADER, null, this);

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, final long l) {
                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Remove Event")
                        .setMessage("Do you want to remove the event " +
                                ((TextView)view.findViewById(android.R.id.text1)).getText().toString() + "?")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                getContentResolver().delete(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, l), null, null);
                            }
                        })
                        .create();
                dialog.show();
                return true;
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case CALENDAR_LOADER:
                return new CursorLoader(this, CalendarContract.Events.CONTENT_URI,
                        new String[]{CalendarContract.Events._ID, CalendarContract.Events.TITLE,
                                CalendarContract.Events.DTSTART, CalendarContract.Events.EVENT_TIMEZONE},
                        null, null, CalendarContract.Events.DTSTART + " ASC");
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.changeCursor(null);
    }

    public void onAddClick(View view){

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI);
        startActivity(intent);

    }
}
