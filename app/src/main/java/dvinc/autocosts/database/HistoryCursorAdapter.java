package dvinc.autocosts.database;

/*
 * Created by DV on Space 5
 * 28.04.2017
 */

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import dvinc.autocosts.R;

import static dvinc.autocosts.database.Contract.CostEntry.*;

/**
 * Класс для создания адаптера данных. Заполняет список из курсора.
 */
public class HistoryCursorAdapter extends CursorAdapter {

    private Context context;
    public HistoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        this.context = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * Метод для привязки всех данных к заданному виду, например, для установки текста в TextView.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Находим поля, в которые будем подставлять свои данные в списке
        TextView costTypeTextView = (TextView) view.findViewById(R.id.listCostType);
        TextView dateTextView = (TextView) view.findViewById(R.id.listDate);
        TextView costTextView = (TextView) view.findViewById(R.id.listCostValue);
        TextView colorCircle = (TextView) view.findViewById(R.id.colorCircle);
        GradientDrawable listCircle = (GradientDrawable) colorCircle.getBackground();
        ImageView imageViewType = (ImageView) view.findViewById(R.id.imageViewType);
        if (cursor != null) {
            // Находим индексы столбцов в курсоре
            int costTypeIndex = cursor.getColumnIndex(COLUMN_COST_TYPE);
            int dateIndex = cursor.getColumnIndex(COLUMN_DATE);
            int costIndex = cursor.getColumnIndex(COLUMN_COST_VALUE);

            // Читаем данные из курсора для текущей записи
            String costType = cursor.getString(costTypeIndex);
            String date = cursor.getString(dateIndex);
            String cost = cursor.getString(costIndex);

            // Обновляем текстовые поля, подставляя в них данные для текущей записи
            costTypeTextView.setText(costType);
            dateTextView.setText(date);
            costTextView.setText(cost);
            listCircle.setColor(setCircleColor(costType));

            //Устанавливаем иконку в соответствии с типом записи
            switch (costType){
                case COST_TYPE_FUEL: imageViewType.setBackgroundResource(R.drawable.car_fuel); break;
                case COST_TYPE_SERVICE: imageViewType.setBackgroundResource(R.drawable.car_service); break;
                case COST_TYPE_TO: imageViewType.setBackgroundResource(R.drawable.car_to); break;
                case COST_TYPE_OTHER: imageViewType.setBackgroundResource(R.drawable.car_other); break;
            }
        }
    }

    private int setCircleColor(String str){
        int color;
        switch (str){
            case COST_TYPE_FUEL: color = R.color.colorFuel; break;
            case COST_TYPE_SERVICE: color = R.color.colorService; break;
            case COST_TYPE_TO: color = R.color.colorTO; break;
            case COST_TYPE_OTHER: color = R.color.colorOther; break;
            default: color = R.color.colorDefault;
        }
        return ContextCompat.getColor(context, color);
    }
}
