package com.example.mahmoud.newtask.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MessagesDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "message.db";

    public MessagesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {


        final String SQL_CREATE_MESSAGE_TABLE = "CREATE TABLE " + MessageContract.MessageEntry.TABLE_NAME + " (" +
                MessageContract.MessageEntry.COLUMN_MESSAGE + " TEXT NOT NULL);";
        db.execSQL(SQL_CREATE_MESSAGE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MessageContract.MessageEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
