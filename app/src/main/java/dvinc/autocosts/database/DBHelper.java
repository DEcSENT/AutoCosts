package dvinc.autocosts.database;

/*
 * Created by DV on Space 5
 * 28.04.2017
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import dvinc.autocosts.database.Contract.*;

/**
 * Класс для создания базы данных.
 */
public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "autocosts.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;
    
    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_PETS_TABLE =  "CREATE TABLE " + CostEntry.TABLE_NAME + " ("
                + CostEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CostEntry.COLUMN_COST_TYPE + " TEXT, "
                + CostEntry.COLUMN_DATE + " TEXT, "
                + CostEntry.COLUMN_MILEAGE + " INTEGER NOT NULL DEFAULT 0, "
                + CostEntry.COLUMN_COST_VALUE + " INTEGER NOT NULL DEFAULT 0, "
                + CostEntry.COLUMN_COST_VOLUME + " INTEGER NOT NULL DEFAULT 0, "
                + CostEntry.COLUMN_COMMENT + " TEXT, "
                + CostEntry.COLUMN_PHOTO + " TEXT);";

        db.execSQL(SQL_CREATE_PETS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}
