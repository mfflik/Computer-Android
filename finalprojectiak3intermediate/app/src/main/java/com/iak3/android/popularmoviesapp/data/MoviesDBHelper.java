package com.iak3.android.popularmoviesapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static android.provider.BaseColumns._ID;
import static com.iak3.android.popularmoviesapp.data.MovieContract.MovieEntry;
import static com.iak3.android.popularmoviesapp.data.MovieContract.MovieEntry.COLUMN_MOVIE_BACKDROP_PATH;
import static com.iak3.android.popularmoviesapp.data.MovieContract.MovieEntry.COLUMN_MOVIE_ID;
import static com.iak3.android.popularmoviesapp.data.MovieContract.MovieEntry.COLUMN_MOVIE_OVERVIEW;
import static com.iak3.android.popularmoviesapp.data.MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_PATH;
import static com.iak3.android.popularmoviesapp.data.MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE;
import static com.iak3.android.popularmoviesapp.data.MovieContract.MovieEntry.COLUMN_MOVIE_TITLE;
import static com.iak3.android.popularmoviesapp.data.MovieContract.MovieEntry.COLUMN_MOVIE_VOTE_AVERAGE;
import static com.iak3.android.popularmoviesapp.data.MovieContract.MovieEntry.TABLE_NAME;

/**
 * Manages a local database for movies data.
 */
public class MoviesDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "movies.db";

    public MoviesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + TABLE_NAME
                + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                COLUMN_MOVIE_TITLE + " TEXT NOT NULL, " +
                COLUMN_MOVIE_POSTER_PATH + " TEXT NOT NULL, " +
                COLUMN_MOVIE_OVERVIEW + " TEXT NOT NULL, " +
                COLUMN_MOVIE_VOTE_AVERAGE + " TEXT NOT NULL, " +
                COLUMN_MOVIE_RELEASE_DATE + " TEXT NOT NULL, " +
                COLUMN_MOVIE_BACKDROP_PATH + " TEXT NOT NULL " +
                " );";
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
