package ru.gukzilla.testjob;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import ru.gukzilla.testjob.models.Note;

public class DataBase {


    private final String TAG = getClass().getSimpleName();
    private final String DB_NAME = "notesDatabase";
    private final int DB_VERSION = 1;
    private final String DB_TABLE = "notes";

    private final String COLUMN_ID = "_id";
    private final String COLUMN_PREV = "prev";
    private final String COLUMN_NEXT = "next";
    private final String COLUMN_NAME = "name";

    private final Context mCtx;
    private DBHelper mDBHelper;
    private SQLiteDatabase mDB;
    private boolean isCreated;
    private final Handler mainHandler;

    private final String DB_CREATE =
            "create table " + DB_TABLE + "(" +
                    COLUMN_ID + " TEXT PRIMARY KEY NOT NULL DEFAULT '', " +
                    COLUMN_PREV + " TEXT, " +
                    COLUMN_NEXT + " TEXT, " +
                    COLUMN_NAME + " TEXT NOT NULL DEFAULT ''" +
                    ");";

    interface ProgressListener {
        void preStart(int size);
        void onResult(int progress);
        void onComplete();
    }

    interface NotesListener {
        void onResult(List<Note> notes);
    }

    public DataBase(Context ctx) {
        mCtx = ctx;
        mainHandler = new Handler(Looper.getMainLooper());
        isCreated = false;
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

    public void getAllNotesAsync(final NotesListener notesListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Note> notes = getAllNotes();
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        notesListener.onResult(notes);
                    }
                });
            }
        }).start();
    }

    private List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();

        Note note = getFirstNote();
        if(note == null) {
            return notes;
        }

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
        while(nextId != null) {
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

    public void addNotes(List<Note> notes, ProgressListener noteCallback) {
        int size = notes.size();
        noteCallback.preStart(size);

        mDB.beginTransaction();
        for(int i = 0; i < size; i++) {
            Note note = notes.get(i);
            addNote(note);
            noteCallback.onResult(i + 1);
        }
        mDB.setTransactionSuccessful();
        mDB.endTransaction();

        noteCallback.onComplete();
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

        Log.i(TAG, "" + note.getPrev() + " -> " + note.getName() + "(" + note.get_id() + ")" + " -> " + note.getNext());
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
                "select * from " + DB_TABLE + " where " + COLUMN_PREV + " IS NULL", null);
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



    public String generateId() {
        return UUID.randomUUID().toString();
    }
}