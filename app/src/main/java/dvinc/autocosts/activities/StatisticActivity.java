package dvinc.autocosts.activities;

/*
 * Created by DV on Space 5
 * 28.04.2017
 */

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.Arrays;

import dvinc.autocosts.R;
import dvinc.autocosts.database.Contract.*;

import static dvinc.autocosts.database.Contract.CostEntry.*;

/**
 * Класс для получения и отображения всей статистики по расходам.
 * Дополнительно можно узнать расход топлива (если для этого есть данные).
 */
public class StatisticActivity extends AppCompatActivity {

    private TextView mTextViewSummary;
    private TextView mTextViewFuel;
    private TextView mTextViewServise;
    private TextView mTextViewTI;
    private TextView mTextViewOther;
    private TextView mTextViewAverageFuel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);

        mTextViewSummary = (TextView) findViewById(R.id.textViewSummary);
        mTextViewFuel = (TextView) findViewById(R.id.textViewFuel);
        mTextViewServise = (TextView) findViewById(R.id.textViewServise);
        mTextViewTI = (TextView) findViewById(R.id.textViewTI);
        mTextViewOther = (TextView) findViewById(R.id.textViewOther);
        mTextViewAverageFuel = (TextView) findViewById(R.id.textViewAverageFuel);

        try {
            calculateAllCost();
            calculateAverageFuelConsumption();
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка при расчетах. Нужно больше данных.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * Метод для подсчета трат в разных категориях.
     * Чтобы не городить отдельный метод для каждой категории была реализована идея с if и массивами для хранения результата.
     */
    private void calculateAllCost() {
        String[] costType = {null, "costType='" + "Заправка'", "costType='" + "Сервис'", "costType='" + "ТО'", "costType='" + "Разное'"};
        float[] allResult = new float[5];
        String[] projection = {
                CostEntry._ID,
                CostEntry.COLUMN_COST_VALUE,
        };
        /* Для каждого i получаем своё значение для запроса к базе данных. И потом каждый набор сначений суммируем и присваиваем в массив.*/
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
        String summary = allResult[0] + "";
        String fuel = allResult[1] + "";
        String servise = allResult[2] + "";
        String ti = allResult[3] + "";
        String other = allResult[4] + "";
        mTextViewSummary.setText(summary);
        mTextViewFuel.setText(fuel);
        mTextViewServise.setText(servise);
        mTextViewTI.setText(ti);
        mTextViewOther.setText(other);
    }

    /**
     * Метод для расчета примерного среднего расхода топлива на 100км.
     * Расчет довольно банальный и топорный, но работает...если есть данные.
     */
    private void calculateAverageFuelConsumption() {
        float summaryFuel = 0;
        float lastFuelValue = 0;
        int count = 0;
        int indexMileageArray = 1;
        float total;

        String[] projection = {
                CostEntry._ID,
                CostEntry.COLUMN_COST_TYPE,
                CostEntry.COLUMN_MILEAGE,
                CostEntry.COLUMN_COST_VOLUME,
        };
        /* Получаем курсор только с данными категории Заправка.*/
        Cursor cursor = this.getContentResolver().query(CostEntry.CONTENT_URI, projection,
                "costType='" + "Заправка'",
                null,
                null);

        if (cursor != null) {
            int mileageIndex = cursor.getColumnIndex(COLUMN_MILEAGE);
            int volumeIndex = cursor.getColumnIndex(COLUMN_COST_VOLUME);

            /* Считаем сумму всего залитого топлива и значение последней заправки.*/
            /* Последнее значение нужно, чтобы из суммы вычесть последнюю заправку т.к. она не нужна (ещё не потрачена в реальной жизни).*/
            while (cursor.moveToNext()) {
                summaryFuel += Float.parseFloat(cursor.getString(volumeIndex));
                lastFuelValue = Float.parseFloat(cursor.getString(volumeIndex));
                /* Заодно считаем сколько было заправок, чтобы сформировать массив для значений пробега.*/
                count++;
            }

            /* Ставим курсор вначало. Формируем массив со значениями пробега. И сразу присваиваем в массив первое значение.*/
            cursor.moveToFirst();
            float[] mileageArray = new float[count];
            mileageArray[0] = Float.parseFloat(cursor.getString(mileageIndex));

            /* Заполняем массив значениями с пробегом.*/
            while (cursor.moveToNext()) {
                mileageArray[indexMileageArray] = Float.parseFloat(cursor.getString(mileageIndex));
                indexMileageArray++;
            }
            /* Сортируем массив, чтобы потом получить самое маленькое и самое больше значение пробега.*/
            Arrays.sort(mileageArray);

            /* Считаем значение среднего расхода на 100км. пути.*/
            total = ((summaryFuel - lastFuelValue) * 100) / (mileageArray[count - 1] - mileageArray[0]);

            /* Формируем строку, отредактировав полученные данные (до двух знаков, запятую меняем на точку).*/
            String text = ((new DecimalFormat("#0.00").format(total)).replace(',', '.')) + getResources().getString(R.string.stat_average_fuel_km);
            mTextViewAverageFuel.setText(text);
            cursor.close();
        }
    }
}
