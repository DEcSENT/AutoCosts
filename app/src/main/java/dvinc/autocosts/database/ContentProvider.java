package dvinc.autocosts.database;

/**
 * Created by DV on Space 5
 * 28.04.2017
 */

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import dvinc.autocosts.database.Contract.*;

/**
 * Класс для контент-провайдера
 */
public class ContentProvider extends android.content.ContentProvider {

    public static final String TAG = ContentProvider.class.getSimpleName();

    private static final int ALL_HISTORY = 100;

    private static final int HISTORY_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(Contract.CONTENT_AUTHORITY, Contract.PATH_HISTORY, ALL_HISTORY);
        sUriMatcher.addURI(Contract.CONTENT_AUTHORITY, Contract.PATH_HISTORY + "/#", HISTORY_ID);
    }

    public DbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new DbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case ALL_HISTORY:
                /* Примечение: здесь курсор для всей истории сортируется в обратном порядке, для отображения последних записей вверху истории. */
                cursor = database.query(CostEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, CostEntry._ID + " DESC");
                break;
            case HISTORY_ID:
                selection = CostEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(CostEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        if(getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return cursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ALL_HISTORY:
                return CostEntry.CONTENT_LIST_TYPE;
            case HISTORY_ID:
                return CostEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ALL_HISTORY:
                return insertHistory(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ALL_HISTORY:
                rowsDeleted = database.delete(CostEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case HISTORY_ID:
                selection = CostEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(CostEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (rowsDeleted != 0 & getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Возвращает количество удаленных строк
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ALL_HISTORY:
                return updateHistory(uri, values, selection, selectionArgs);
            case HISTORY_ID:
                selection = CostEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateHistory(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private Uri insertHistory(Uri uri, ContentValues values) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(CostEntry.TABLE_NAME, null, values);
        // Если ID равен -1, значит запись в историю не получилась
        if (id == -1) {
            Log.e(TAG, "Failed to insert row for " + uri);
            return null;
        }

        if(getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return ContentUris.withAppendedId(uri, id);
    }

    private int updateHistory(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Если нет значений для обновления, возвращаем 0
        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Обновляем запись в базе данных и возвращаем количество обновленных строк
        int rowsUpdated = database.update(CostEntry.TABLE_NAME, values, selection, selectionArgs);

        Log.v("updateHistory", "Rows updated: " + rowsUpdated);

        // Если хотя бы одна строка была обновлена, оповещаем об изменении
        if (rowsUpdated != 0 & getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Возвращает количество обновленных строк
        return rowsUpdated;
    }
}

