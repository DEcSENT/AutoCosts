package dvinc.autocosts.activities;

/*
 * Created by DV on Space 5
 * 28.04.2017
 */

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import dvinc.autocosts.R;

/**
 * Класс для отображения информации о приложении.
 */
public class InfoActivity extends AppCompatActivity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
