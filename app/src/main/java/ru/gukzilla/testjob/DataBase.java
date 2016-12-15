package ru.gukzilla.testjob;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import ru.gukzilla.testjob.models.Note;

public class DataBase {

    private static final String DB_NAME = "notesDatabase";
    private static final int DB_VERSION = 1;
    private static final String DB_TABLE = "notes";

    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_PREV = "prev";
    private static final String COLUMN_NEXT = "next";
    private static final String COLUMN_NAME = "name";

    private static final String DB_CREATE =
            "create table " + DB_TABLE + "(" +
                    COLUMN_ID + " TEXT PRIMARY KEY NOT NULL DEFAULT '', " +
                    COLUMN_PREV + " TEXT NOT NULL DEFAULT '', " +
                    COLUMN_NEXT + " TEXT NOT NULL DEFAULT '', " +
                    COLUMN_NAME + " TEXT NOT NULL DEFAULT ''" +
                    ");";

    private final Context mCtx;


    private DBHelper mDBHelper;
    private SQLiteDatabase mDB;
    private boolean isCreated;

    public DataBase(Context ctx) {
        mCtx = ctx;
        isCreated = false;
    }

    public boolean isCreated() {
        return isCreated;
    }

    public void open() {
        mDBHelper = new DBHelper(mCtx, DB_NAME, null, DB_VERSION);
        mDB = mDBHelper.getWritableDatabase();
    }

    public void close() {
        if (mDBHelper!=null) mDBHelper.close();
    }

    public List<Note> getAllNotes() {
        Note note = getFirstNote();

        List<Note> notes = new ArrayList<>();
        notes.add(note);

        HashMap<String, Note> hashMap = new HashMap<>();
        Cursor cursor = mDB.query(DB_TABLE, null, null, null, null, null, null);
        try {
            while (cursor.moveToNext()) {
                String _id = cursor.getString(cursor.getColumnIndex(COLUMN_ID));
                String prev = cursor.getString(cursor.getColumnIndex(COLUMN_PREV));
                String next = cursor.getString(cursor.getColumnIndex(COLUMN_NEXT));
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                hashMap.put(_id, new Note(_id, prev, next, name));
            }
        } finally {
            cursor.close();
        }

        String nextId = note.getNext();
        while(!TextUtils.isEmpty(nextId)) {
            Note next = hashMap.get(nextId);

            if(next == null) {
                nextId = null;
            } else {
                notes.add(next);
                nextId = next.getNext();
            }
        }

        return notes;
    }

    public void addNote(Note note) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ID, note.get_id());
        cv.put(COLUMN_PREV, note.getPrev());
        cv.put(COLUMN_NEXT, note.getNext());
        cv.put(COLUMN_NAME, note.getName());
        mDB.insert(DB_TABLE, null, cv);
    }

    public void updateNote(Note note) {
        if(note == null) {
            return;
        }

        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PREV, note.getPrev());
        cv.put(COLUMN_NEXT, note.getNext());
        cv.put(COLUMN_NAME, note.getName());
        mDB.update(DB_TABLE, cv, COLUMN_ID + "=?", new String[]{note.get_id()});
    }

    public void updateNotes(Note... notes) {
        mDB.beginTransaction();
        for(Note note : notes) {
            updateNote(note);
        }
        mDB.setTransactionSuccessful();
        mDB.endTransaction();
    }

    public Note getFirstNote() {
        Cursor cursor =  mDB.rawQuery(
                "select * from " + DB_TABLE + " where " + COLUMN_PREV + "=''", null);
        try {
            while (cursor.moveToNext()) {
                String _id = cursor.getString(cursor.getColumnIndex(COLUMN_ID));
                String prev = cursor.getString(cursor.getColumnIndex(COLUMN_PREV));
                String next = cursor.getString(cursor.getColumnIndex(COLUMN_NEXT));
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                return new Note(_id, prev, next, name);
            }
        } finally {
            cursor.close();
        }

        return null;
    }

    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, CursorFactory factory,
                        int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE);
            isCreated = true;
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

    public String generateId() {
        return UUID.randomUUID().toString();
    }
}