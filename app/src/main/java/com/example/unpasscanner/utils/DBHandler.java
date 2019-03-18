package com.example.unpasscanner.utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.unpasscanner.models.Ruangan;

import java.util.ArrayList;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "RuanganInfo";
    private static final String TABLE_RUANGAN = "Ruangan";

    private static final String ID = "idRuangan";
    private static final String KODERUANGAN = "kodeRuangan";
    private static final String NAMARUANGAN = "ruangan";

    public DBHandler(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_RUANGAN_TABLE = "CREATE TABLE " + TABLE_RUANGAN + "("
                + ID + " INTEGER PRIMARY KEY,"
                + KODERUANGAN + " TEXT,"
                + NAMARUANGAN + " TEXT" + ")";
        db.execSQL(CREATE_RUANGAN_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RUANGAN);
        onCreate(db);
    }

    public void addRuangan(Ruangan ruangan){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ID, ruangan.getIdRuangan());
        values.put(KODERUANGAN, ruangan.getKodeRuangan());
        values.put(NAMARUANGAN, ruangan.getRuangan());
        db.insert(TABLE_RUANGAN, null, values);
        db.close();
    }

    public List<Ruangan> getAllRuangan(){
        List<Ruangan> ruanganList = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_RUANGAN;
        SQLiteDatabase db = this.getReadableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()){
            do {
                Ruangan ruangan = new Ruangan();
                ruangan.setIdRuangan(Integer.parseInt(cursor.getString(0)));
                ruangan.setKodeRuangan(cursor.getString(1));
                ruangan.setRuangan(cursor.getString(2));
                ruanganList.add(ruangan);
            } while (cursor.moveToNext());
        }
        return ruanganList;
    }

    public void updateRuangan(Ruangan ruangan) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ID, ruangan.getIdRuangan());
        values.put(KODERUANGAN, ruangan.getKodeRuangan());
        values.put(NAMARUANGAN, ruangan.getRuangan());
        db.update(TABLE_RUANGAN, values, ID + " = ?",
                new String[]{String.valueOf(ruangan.getIdRuangan())});
    }

    public void deleteRuangan(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_RUANGAN, null, null);
        db.execSQL("DELETE FROM " + TABLE_RUANGAN);
        db.close();
    }
}
