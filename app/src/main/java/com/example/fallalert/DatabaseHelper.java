package com.example.fallalert;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "contacts.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "contacts";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PHONE = "phone";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_PHONE + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }


    public String addOrUpdateContact(String name, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if any contact exists
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            // Check if the required columns exist
            int phoneIndex = cursor.getColumnIndex(COLUMN_PHONE);
            int idIndex = cursor.getColumnIndex(COLUMN_ID);

            if (phoneIndex != -1 && idIndex != -1) {
                String existingPhone = cursor.getString(phoneIndex);
                if (existingPhone.equals(phone)) {
                    // The existing contact has the same phone number
                    cursor.close();
                    db.close();
                    return "Contact already exists with the same number.";
                } else {
                    // The phone number is different, update the existing contact
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_NAME, name);
                    values.put(COLUMN_PHONE, phone);
                    int id = cursor.getInt(idIndex);
                    db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
                    cursor.close();
                    db.close();
                    return "Contact updated with new number.";
                }
            } else {
                cursor.close();
                db.close();
                return "Error: Column not found in database.";
            }
        } else {
            // No contact exists, insert a new contact
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, name);
            values.put(COLUMN_PHONE, phone);
            long result = db.insert(TABLE_NAME, null, values);
            cursor.close();
            db.close();
            return result != -1 ? "Contact added successfully." : "Error adding contact.";
        }
    }

    public Cursor getContact() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_ID + " DESC LIMIT 1", null);
    }

}
