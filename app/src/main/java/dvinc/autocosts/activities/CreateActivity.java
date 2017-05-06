package dvinc.autocosts.activities;

/**
 * Created by DV on Space 5
 * 28.04.2017
 */

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import dvinc.autocosts.R;

/**
 * TODO: Класс для создания записи по расходам на авто
 * TODO: Сделать форму, выбор категорий, вставку фото и комментариев. Так же дополнительно подумать что нужно, а что не нужно знать при записи данных и внести правки в шаблон
 */
public class CreateActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
    }

    public void loadImageClick(View view) {
    }

    public void onClickSafe(View view) {
    }
}
