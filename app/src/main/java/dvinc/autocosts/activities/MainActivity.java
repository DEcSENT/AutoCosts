package dvinc.autocosts.activities;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import dvinc.autocosts.R;
import dvinc.autocosts.database.Contract.*;
import dvinc.autocosts.database.HistoryCursorAdapter;

/*
 * Created by DV on Space 5
 * 28.04.2017
 */

/**
 * Класс главной активности. Содержит в себе меню навигации, фаб кнопку, список со всеми записями.
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER = 0;
    HistoryCursorAdapter historyCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.nav_main_title);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateActivity.class);
                startActivity(intent);

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ListView listView = (ListView) findViewById(R.id.list);
        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);
        historyCursorAdapter = new HistoryCursorAdapter(this, null);
        listView.setAdapter(historyCursorAdapter);

        /* Установка слушателя для нажатия на выбранную запись с последующим вызовом диалога выбора действия.*/
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                showSelectionDialog(id);
            }
        });

        getLoaderManager().initLoader(LOADER, null, this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_all_history:
                showDeleteConfirmationDialog();
                break;
        }
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_main) {

        } else if (id == R.id.nav_stat) {
            Intent intent = new Intent(MainActivity.this, StatisticActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_info) {
            Intent intent = new Intent(MainActivity.this, InfoActivity.class);
            startActivity(intent);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Зададим нужные колонки
        String[] projection = {
                CostEntry._ID,
                CostEntry.COLUMN_COST_TYPE,
                CostEntry.COLUMN_DATE,
                CostEntry.COLUMN_COST_VALUE
                };

        // Загрузчик запускает запрос ContentProvider в фоновом потоке
        return new CursorLoader(this,
                CostEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Обновляем CursorAdapter новым курсором, которые содержит обновленные данные
        historyCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Освобождаем ресурсы
        historyCursorAdapter.swapCursor(null);
    }

    /**
     * Предупреждающий диалог для удаления всех записей из истории.
     */
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete_history_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteFromHistory(0);
            }
        });
        builder.setNegativeButton(R.string.delete_history_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Общий метод для удаления всей истории или одной выбранной записи.
     * @param elementID - номер записи в истории. Если равен 0, то вся история будет удалена.
     * Если elementID не равен 0, то будет удалена одна запись с этим id.
     */
    private void deleteFromHistory(final long elementID){
        String message;
        if(elementID == 0){
            int rowsDeleted = getContentResolver().delete(CostEntry.CONTENT_URI, null, null);
            message = "Удалено записей из истории: " + rowsDeleted;
        } else{
            Uri currentCostUri = ContentUris.withAppendedId(CostEntry.CONTENT_URI, elementID);
            getContentResolver().delete(currentCostUri, null, null);
            message = "Запись удалена";
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Диалог для выбора действия по клику на запись в истории.
     * @param elementID - номер записи для удаления.
     */
    private void showSelectionDialog(final long elementID){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.choose_action).setIcon(R.drawable.car_other);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteFromHistory(elementID);
            }
        });
        builder.setNegativeButton(R.string.change_entry, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(MainActivity.this, CreateActivity.class);
                Uri currentCostUri = ContentUris.withAppendedId(CostEntry.CONTENT_URI, elementID);
                intent.setData(currentCostUri);
                startActivity(intent);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
