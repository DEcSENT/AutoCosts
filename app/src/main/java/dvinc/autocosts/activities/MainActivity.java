package dvinc.autocosts.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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
        // Inflate the menu; this adds items to the action bar if it is present.
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

        } else if (id == R.id.nav_info) {

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
                deleteAllHistory();
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
     * Метод для удаления всех записей из истории.
     */
    private void deleteAllHistory() {
        int rowsDeleted = getContentResolver().delete(CostEntry.CONTENT_URI, null, null);
        Toast.makeText(this, "Удалено записей из истории: " +
                rowsDeleted, Toast.LENGTH_LONG)
                .show();
    }
}
