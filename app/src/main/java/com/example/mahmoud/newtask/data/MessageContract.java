package com.example.mahmoud.newtask.data;


import android.net.Uri;
import android.provider.BaseColumns;

public class MessageContract {
    public static final String CONTENT_AUTHORITY = "com.example.mahmoud.newtask.data";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    public static final String PATH_MESAGE = "message";



        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MESAGE)
                .build();
        public static class MessageEntry implements BaseColumns {
            public static final String TABLE_NAME = "name";
            public static final String COLUMN_MESSAGE = "message";
        }
    }


