package xyz.janficko.movienator.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FavouriteDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "favourite.db";
    public static final int DATABASE_VERSION = 1;

    public FavouriteDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_FAVOURITE_TABLE =
                "CREATE TABLE " + FavouriteContract.FavouriteEntry.TABLE_NAME + " (" +
                        FavouriteContract.FavouriteEntry._ID                +   " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        FavouriteContract.FavouriteEntry.COLUMN_MEDIA_ID    +   " TEXT UNIQUE"                         +
                ");";
        db.execSQL(SQL_CREATE_FAVOURITE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FavouriteContract.FavouriteEntry.TABLE_NAME);
        onCreate(db);
    }
}
