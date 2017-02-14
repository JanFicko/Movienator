package xyz.janficko.movienator.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class FavouriteContract {

    public static final String CONTENT_AUTHORITY = "xyz.janficko.movienator";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_FAVOURITE = "favourite";

    public static final class FavouriteEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_FAVOURITE)
                .build();

        public static final String TABLE_NAME = "favourite";
        public static final String COLUMN_MEDIA_ID = "media_id";
        public static final String COLUMN_POSTER_PATH = "poster_path";

    }
}
