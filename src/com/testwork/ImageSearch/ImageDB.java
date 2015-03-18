package com.testwork.ImageSearch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class ImageDB extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "imageSeach";
    private static final String TABLE_NAME = "image";
    private static final String KEY_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_URL = "url";
    private static final String COLUMN_TBURL = "tburl";

    public ImageDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_IMAGES_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + COLUMN_TITLE + " TEXT,"
                + COLUMN_TBURL + " TEXT," + COLUMN_URL + " TEXT" + ")";
        sqLiteDatabase.execSQL(CREATE_IMAGES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS" + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public void addImageData(ImageData data) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, data.getTitle());
        values.put(COLUMN_TBURL, data.getThumbUrl());
        values.put(COLUMN_URL, data.getUrl());

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public void addListImageData(List<ImageData> data) {
        SQLiteDatabase db = this.getWritableDatabase();

        for (ImageData imageData : data ) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, imageData.getTitle());
            values.put(COLUMN_TBURL, imageData.getThumbUrl());
            values.put(COLUMN_URL, imageData.getUrl());

            db.insert(TABLE_NAME, null, values);
        }
        db.close();
    }

    public List<ImageData> getAllImageData() {
        List<ImageData> imageDataList = new ArrayList<ImageData>();

        String selectQuery = "SELECT  * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                ImageData data = new ImageData();
                data.setTitle(cursor.getString(1));
                data.setThumbUrl(cursor.getString(2));
                data.setUrl(cursor.getString(3));

                imageDataList.add(data);
            } while (cursor.moveToNext());
        }

        return imageDataList;
    }

    public int getImageDataCount() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        return cursor.getCount();
    }
}
