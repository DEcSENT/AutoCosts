package dvinc.autocosts.activities;

/*
 * Created by DV on Space 5
 * 28.04.2017
 */

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import dvinc.autocosts.R;
import dvinc.autocosts.database.Contract.*;

import static dvinc.autocosts.database.Contract.CostEntry.COLUMN_COST_VALUE;

/**
 * Класс для получения и отображения всей статистики по расходам.
 * TODO: Попробовать сделать расчет расхода бензина на 100км.
 */
public class StatisticActivity extends AppCompatActivity{

    private TextView mTextViewSummary;
    private TextView mTextViewFuel;
    private TextView mTextViewServise;
    private TextView mTextViewTI;
    private TextView mTextViewOther;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);

        mTextViewSummary = (TextView) findViewById(R.id.textViewSummary);
        mTextViewFuel = (TextView) findViewById(R.id.textViewFuel);
        mTextViewServise = (TextView) findViewById(R.id.textViewServise);
        mTextViewTI = (TextView) findViewById(R.id.textViewTI);
        mTextViewOther = (TextView) findViewById(R.id.textViewOther);

        calculateAllCost();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * Метод для подсчета трат в разных категориях.
     * Чтобы не городить отдельный метод для каждой категории была реализована идея с if и массивами для хранения результата.
     */
    private void calculateAllCost(){
        String[] costType = {null, "costType='"+"Заправка'", "costType='"+"Сервис'", "costType='"+"ТО'", "costType='"+"Разное'"};
        float[] allResult = new float[5];

        String[] projection = {
                CostEntry._ID,
                CostEntry.COLUMN_COST_VALUE,
        };
        for (int i = 0; i < costType.length; i++) {
            Cursor cursor = this.getContentResolver().query(CostEntry.CONTENT_URI, projection,
                    costType[i],
                    null,
                    null);

            if (cursor != null) {
                int costIndex = cursor.getColumnIndex(COLUMN_COST_VALUE);
                while (cursor.moveToNext()) {
                    allResult[i] += Float.parseFloat(cursor.getString(costIndex));
                }
                cursor.close();
            }
        }
        mTextViewSummary.setText(allResult[0]+"");
        mTextViewFuel.setText(allResult[1]+"");
        mTextViewServise.setText(allResult[2]+"");
        mTextViewTI.setText(allResult[3]+"");
        mTextViewOther.setText(allResult[4]+"");
    }
}
