package de.afarber.googleauth;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import static de.afarber.googleauth.User.GOOGLE;

public class DatabaseHelper extends SQLiteAssetHelper {
    private static final String TAG = DatabaseHelper.class.getSimpleName();

    private static final int DATABASE_VERSION   = 3;
    private static final String DATABASE_NAME   = "social.db";
    private static final String TABLE_SOCIAL    = "social";

    public static final String COLUMN_SID       = "sid";
    public static final String COLUMN_NET       = "net";
    public static final String COLUMN_GIVEN     = "given";
    public static final String COLUMN_FAMILY    = "family";
    public static final String COLUMN_PHOTO     = "photo";
    public static final String COLUMN_LAT       = "lat";
    public static final String COLUMN_LNG       = "lng";
    public static final String COLUMN_STAMP     = "stamp";

    private static final String[] COLUMNS_SOCIAL = new String[] {
            COLUMN_SID,
            COLUMN_NET,
            COLUMN_GIVEN,
            COLUMN_FAMILY,
            COLUMN_PHOTO,
            COLUMN_LAT,
            COLUMN_LNG,
            COLUMN_STAMP
    };

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        setForcedUpgrade();
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("pragma foreign_keys = on;");
        }
    }

    private boolean findUser(int net) {
        SQLiteDatabase db = getReadableDatabase();
        int rows = 0;
        Cursor cursor = db.query(TABLE_SOCIAL,
            COLUMNS_SOCIAL,
            // OR: "net=" + net,
            "net=?",
            new String[]{String.valueOf(net)},
            null,
            null,
            null
        );

        rows = cursor.getCount();
        cursor.close();
        db.close();
        return rows > 0;
    }

    public boolean findGoogleUser() {
        return findUser(GOOGLE);
    }

    public User findNewestUser() {
        SQLiteDatabase db = getReadableDatabase();
        User user = null;
        Cursor cursor = db.query(TABLE_SOCIAL,
            COLUMNS_SOCIAL,
            null,
            null,
            null,
            null,
            "stamp desc",
            "1");

        if (cursor.moveToNext()) {
            user = new User(cursor);
        }

        cursor.close();
        db.close();
        return user;
    }

    public void updateUser(User user) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_SOCIAL, "net=?", new String[]{ String.valueOf(user.net) });

        ContentValues cv = new ContentValues();
        cv.put(COLUMN_SID, user.sid);
        cv.put(COLUMN_NET, user.net);
        cv.put(COLUMN_GIVEN, user.given);
        cv.put(COLUMN_FAMILY, user.family);
        cv.put(COLUMN_PHOTO, user.photo);
        cv.put(COLUMN_LAT, user.lat);
        cv.put(COLUMN_LNG, user.lng);
        //cv.put(COLUMN_STAMP, (int) (System.currentTimeMillis() / 1000));
        db.insert(TABLE_SOCIAL, null, cv);
        db.close();
    }

    public void deleteAll() {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE_SOCIAL, "1", null);
        Log.d(TAG, "deleted rows: " + rows);
        db.close();
    }

    public void printAll() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_SOCIAL,
                COLUMNS_SOCIAL,
                null,
                null,
                null,
                null,
                "stamp desc",
                null);

        while (cursor.moveToNext()) {
            User user = new User(cursor);
            Log.d(TAG, user.toString());
        }

        cursor.close();
        db.close();
    }

}
