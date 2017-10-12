package com.huntloc.eoc;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "EOCDB";

    private static final String TABLE_GROUPS = "Groups";
    private static final String KEY_GROUP_NAME = "group_name";
    private static final String KEY_GROUP_MESSAGE = "message";

    private static final String TABLE_CONTACTS = "Contacts";
    //private static final String KEY_GROUP_NAME = "group_name";
    private static final String KEY_CONTACT_NAME = "contact_name";
    private static final String KEY_CONTACT_NUMBER = "contact_number";

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        String CREATE_GROUP_TABLE = "CREATE TABLE "+ TABLE_GROUPS +" ( "+KEY_GROUP_NAME+" TEXT, "+KEY_GROUP_MESSAGE+" TEXT)";
        db.execSQL(CREATE_GROUP_TABLE);

        String CREATE_CONTACT_TABLE = "CREATE TABLE "+ TABLE_CONTACTS +" ( "+KEY_GROUP_NAME+" TEXT, "+KEY_CONTACT_NAME+" TEXT, "+KEY_CONTACT_NUMBER+" TEXT)";
        db.execSQL(CREATE_CONTACT_TABLE);

    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        this.onCreate(db);
    }

    public void addGroup(Group group) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_GROUP_NAME, group.getGroupName());
        values.put(KEY_GROUP_MESSAGE, group.getMessage());
        long result = db.insert(TABLE_GROUPS, null, values);
        db.close();
        Log.d("addGroup result",result+"");
    }

    public void deleteGroups() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_GROUPS);
        db.close();
    }

    public List<Group> getGroups() {
        List<Group> toReturn = new LinkedList<>();
        String query = "SELECT * FROM " + TABLE_GROUPS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        Group group;
        if (cursor.moveToFirst()) {
            do {
                group = new Group(cursor.getString(0), cursor.getString(1));
                toReturn.add(group);
            } while (cursor.moveToNext());
        }
        db.close();
        Log.d("getGroups count",toReturn.size()+"");
        return toReturn;
    }
    public void addContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_GROUP_NAME, contact.getGroupName());
        values.put(KEY_CONTACT_NAME, contact.getContactName());
        values.put(KEY_CONTACT_NUMBER, contact.getContactNumber());
        long result = db.insert(TABLE_CONTACTS, null, values);
        db.close();
        Log.d("addContact result",result+"");
    }

    public void deleteContacts() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_CONTACTS);
        db.close();
    }

    public List<Contact> getContacts(String group_name) {
        List<Contact> toReturn = new LinkedList<>();
        String query = "SELECT * FROM " + TABLE_CONTACTS+" where "+KEY_GROUP_NAME+"='"+group_name+"'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        Contact contact;
        if (cursor.moveToFirst()) {
            do {
                contact = new Contact(cursor.getString(0), cursor.getString(1), cursor.getString(2));
                toReturn.add(contact);
            } while (cursor.moveToNext());
        }
        db.close();
        Log.d("getContacts count",toReturn.size()+"");
        return toReturn;
    }
}
