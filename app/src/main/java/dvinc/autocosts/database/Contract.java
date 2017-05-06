package dvinc.autocosts.database;

/**
 * Created by DV on Space 5
 * 28.04.2017
 */

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Класс для хранения переменных, используемых в базе данных и контент-провайдере
 */
public final class Contract {

    private Contract() {
    }

    /**
     * Имя для всего контент-провайдера.
     */
    static final String CONTENT_AUTHORITY = "dvinc.autocosts";

    /**
     * Переменная для доступа к контент-провайдеру.
     */
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Путь к провайдеру (добавляется к URI базового содержимого для возможных URI).
     */
    static final String PATH_HISTORY = "costsHistory";

    public static final class CostEntry implements BaseColumns {

        /**
         * URI содержимого для доступа к данным в провайдере.
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_HISTORY);

        static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_HISTORY;

        static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_HISTORY;

        /* Переменные для создания базы данных.*/
        final static String TABLE_NAME = "costsHistory";
        final static String COLUMN_ID = BaseColumns._ID;
        public final static String COLUMN_COST_TYPE = "costType";
        public final static String COLUMN_DATE = "date";
        public final static String COLUMN_MILEAGE = "mileage";
        public final static String COLUMN_COST_VALUE = "costValue";
        public final static String COLUMN_COST_VOLUME = "costVolume";
        public final static String COLUMN_COMMENT = "comment";
        public final static String COLUMN_PHOTO = "photo";
    }
}
