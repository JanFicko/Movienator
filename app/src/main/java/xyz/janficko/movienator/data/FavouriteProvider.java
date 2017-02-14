package xyz.janficko.movienator.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompatBase;
import android.util.Log;


public class FavouriteProvider extends ContentProvider {

    private static final String TAG = FavouriteProvider.class.getSimpleName();

    public static final int CODE_FAVOURITE = 100;
    public static final int CODE_FAVOURITE_WITH_ID = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private FavouriteDbHelper mDbHelper;
    private Context mContext;

    @Override
    public boolean onCreate() {
        mContext = getContext();
        mDbHelper = new FavouriteDbHelper(mContext);
        return true;
    }

    public static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = FavouriteContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, FavouriteContract.PATH_FAVOURITE, CODE_FAVOURITE);
        matcher.addURI(authority, FavouriteContract.PATH_FAVOURITE + "/#", CODE_FAVOURITE_WITH_ID);

        return matcher;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        final SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor returnCursor;

        switch(sUriMatcher.match(uri)){
            case CODE_FAVOURITE:
                returnCursor = db.query(
                        FavouriteContract.FavouriteEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case CODE_FAVOURITE_WITH_ID:
                returnCursor = db.query(
                        FavouriteContract.FavouriteEntry.TABLE_NAME,
                        projection,
                        FavouriteContract.FavouriteEntry.COLUMN_MEDIA_ID + " = ? ",
                        new String[]{uri.getLastPathSegment()},
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        returnCursor.setNotificationUri(mContext.getContentResolver(), uri);

        return returnCursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();

        Uri returnUri = null;

        switch(sUriMatcher.match(uri)){
            case CODE_FAVOURITE:
                long rowId = db.insert(FavouriteContract.FavouriteEntry.TABLE_NAME, null, values);

                if (rowId > 0) {
                    returnUri = ContentUris.withAppendedId(FavouriteContract.FavouriteEntry.CONTENT_URI, rowId);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        mContext.getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int numRowsDeleted;

        if (null == selection) {
            selection = "1";
        }

        switch(sUriMatcher.match(uri)){
            case CODE_FAVOURITE:
                numRowsDeleted = db.delete(
                        FavouriteContract.FavouriteEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            case CODE_FAVOURITE_WITH_ID:
                numRowsDeleted = db.delete(
                        FavouriteContract.FavouriteEntry.TABLE_NAME,
                        FavouriteContract.FavouriteEntry.COLUMN_MEDIA_ID + " = ? ",
                        new String[]{uri.getLastPathSegment()}
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (numRowsDeleted != 0) {
            mContext.getContentResolver().notifyChange(uri, null);
        }

        return numRowsDeleted;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        throw new RuntimeException("getType method isn't implemented.");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new RuntimeException("update method isn't implemented.");
    }
}
