package dvinc.autocosts.activities;

/*
 * Created by DV on Space 5
 * 28.04.2017
 */

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import dvinc.autocosts.R;
import dvinc.autocosts.database.Contract.*;
import dvinc.autocosts.utility.DateDialog;

import static dvinc.autocosts.database.Contract.CostEntry.*;

/**
 * Класс для создания записи по расходам на авто
 * TODO: Дополнительно подумать что нужно, а что не нужно знать при записи данных и внести правки в шаблон
 */
public class CreateActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** Идентификатор для загрузчика.*/
    private static final int LOADER = 0;

    /** Текущий URI для существующей записи (нуль если запись новая). */
    private Uri mCurrentEntryUri;

    /* Объявление полей для ввода пользовательской информации.*/
    private Spinner mCostTypeSpinner;
    public  EditText mDateEditText;
    private EditText mMileageEditText;
    private EditText mCostValueEditText;
    private EditText mCostVolumeEditText;
    private EditText mCommentEditText;
    private ImageView mPhotoImageView;

    /** Макет со значением объема.*/
    private LinearLayout mLLcostVolume;
    
    /** Переменная для хранения выбранной категории.*/
    private String mCostType;

    /** Переменная для хранения строки с фото в формате басе64.*/
    private String image64;

    /** Кнопка для загрузки фотографии. */
    private Button mButtonLoadPhoto;

    /** Кнопка для сохранения записи. */
    private Button mButtonSaveEntry;

    /** Флаг, который отслеживает была ли запись отредактирована. */
    private boolean mEntryHasChanged = false;

    /** Значение типа рахода по умолчанию. */
    private String CostTypeDefault;

    /**
     * OnTouchListener, который отслеживает прикасался ли пользователь к каким-либо View.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mEntryHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_2nd);

        CostTypeDefault = getResources().getString(R.string.costType_none);

        /* Инициализация полей для ввода пользовательской информации. */
        mCostTypeSpinner = (Spinner) findViewById(R.id.spinnerCostType);
        mDateEditText = (EditText) findViewById(R.id.editDate);
        mMileageEditText = (EditText) findViewById(R.id.editMileage);
        mCostValueEditText = (EditText) findViewById(R.id.editCostValue);
        mCostVolumeEditText = (EditText)findViewById(R.id.editCostVolume);
        mCommentEditText = (EditText) findViewById(R.id.editComment);
        mPhotoImageView = (ImageView) findViewById(R.id.imageViewPhotoLoad);

        mLLcostVolume = (LinearLayout) findViewById(R.id.LL_cost_volume);

        /* По умолчанию прячем поле ввода для объема.*/
        mLLcostVolume.setVisibility(View.INVISIBLE);
        mLLcostVolume.setVisibility(View.GONE);

        /* Инициализация кнопок.*/
        mButtonLoadPhoto = (Button) findViewById(R.id.buttonLoadPhoto);
        mButtonSaveEntry = (Button) findViewById(R.id.buttonSaveEntry);

        /* Скрываем клавиатуру при старте приложения. */
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // Получаем интент из места, откуда открыли эту активность.
        Intent intent = getIntent();
        mCurrentEntryUri = intent.getData();

        // Если интент не содержит в себе URI, то создаем новую запись с соответсвующим заголовком.
        // Если интенте содержит в себе URI, то редактируем запись по текущему URI.
        if (mCurrentEntryUri == null) {
            setTitle(getString(R.string.editor_title_new));
            // Прячем меню для новой записи (возможность удаления новой записи не нужна).
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_title_edit_current));
            // Инициализация загрузчика для чтения информации из базы данных
            getLoaderManager().initLoader(LOADER, null, this);
        }

        // Устанавливаем OnTouchListener для всех полей ввода пользовательской информации, чтобы опеределить
        // прикасался ли к ним или изменял ли их пользователь. Это нужно для того, чтобы нельзя было покинуть активность, если
        // имеются несохраненные данные.
        mCostTypeSpinner.setOnTouchListener(mTouchListener);
        mDateEditText.setOnTouchListener(mTouchListener);
        mMileageEditText.setOnTouchListener(mTouchListener);
        mCostValueEditText.setOnTouchListener(mTouchListener);
        mCostVolumeEditText.setOnTouchListener(mTouchListener);
        mCommentEditText.setOnTouchListener(mTouchListener);
        mPhotoImageView.setOnTouchListener(mTouchListener);

        mDateEditText.setKeyListener(null);
        mDateEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View view, boolean hasfocus) {
                if (hasfocus) {
                    DateDialog dialog = new DateDialog(view);
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    dialog.show(ft, "DatePicker");
                }
            }
        });

        // Устанавливаем спиннер для выбора типа расхода.
        setupCostTypeSpinner();
    }

    /**
     * Спиннер для выбора типа расхода.
     */
    private void setupCostTypeSpinner(){
        ArrayAdapter costTypeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_cost_type, android.R.layout.simple_spinner_item);
        costTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mCostTypeSpinner.setAdapter(costTypeSpinnerAdapter);
        mCostTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                        mCostType = selection;
                    if (selection.equals(COST_TYPE_FUEL)) {
                        mLLcostVolume.setVisibility(View.VISIBLE);
                    } else {
                        mLLcostVolume.setVisibility(View.INVISIBLE);
                        mLLcostVolume.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mCostType = CostTypeDefault;
            }
        });
    }

    /**
     * Метод для сохранения пользовательской информации в базу данных.
     */
    private void saveEntry(){
        /* Получаем данные пользоваеля.*/
        String date = mDateEditText.getText().toString().trim();
        String mileage = mMileageEditText.getText().toString().trim();
        String costValue = mCostValueEditText.getText().toString().trim();
        String costVolume = mCostVolumeEditText.getText().toString().trim();
        String comment = mCommentEditText.getText().toString().trim();

        if (mPhotoImageView.getDrawable() != null) {
            Bitmap b = ((BitmapDrawable) mPhotoImageView.getDrawable()).getBitmap();
            Bitmap converetdImage = getResizedBitmap(b, 600);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            converetdImage.compress(Bitmap.CompressFormat.JPEG, 50, bos);
            byte[] img = bos.toByteArray();
            image64 = Base64.encodeToString(img, Base64.DEFAULT);
        }

        // Если никакие поля не редактировались, можно вернуться без создания новой записи.
        if (mCurrentEntryUri == null &&
                TextUtils.isEmpty(date) && TextUtils.isEmpty(mileage) &&
                TextUtils.isEmpty(costValue) && TextUtils.isEmpty(costVolume)&& TextUtils.isEmpty(comment) && mCostType.equals(CostTypeDefault)) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(CostEntry.COLUMN_COST_TYPE, mCostType);
        values.put(CostEntry.COLUMN_DATE, date);
        values.put(CostEntry.COLUMN_MILEAGE, mileage);
        values.put(CostEntry.COLUMN_COST_VALUE, costValue);
        values.put(CostEntry.COLUMN_COST_VOLUME, costVolume);
        values.put(CostEntry.COLUMN_COMMENT, comment);
        values.put(CostEntry.COLUMN_PHOTO, image64);

        if (mCurrentEntryUri == null) {
            // Если запись новая, то сохраняем запись через контент-провайдер и показываем всплывающее сообщение.
            Uri newUri = getContentResolver().insert(CostEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_entry_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_entry_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Если редактируется уже существующая запись, то сохраняем отредактированную запись по текущему URI и показываем соотвествующее всплывающее сообщение.
            int rowsAffected = getContentResolver().update(mCurrentEntryUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_entry_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_entry_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        mPhotoImageView.setImageBitmap(null);
    }

    /**
     * Метод для кнопки сохранения записи в базу данных.
     */
    public void onClickSafe(View view) {
        saveEntry();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentEntryUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mEntryHasChanged) {
                    NavUtils.navigateUpFromSameTask(CreateActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(CreateActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Предупреждающий диалог для удаления записи из истории.
     */
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg2);
        builder.setPositiveButton(R.string.delete_history_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteCurrentEntry();
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
     * Удаление текущей записи из истории.
     */
    private void deleteCurrentEntry() {
        if (mCurrentEntryUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentEntryUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    /**
     * Предупреждающий о несохраненных изменениях диалог.
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.delete_history_yes, discardButtonClickListener);
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                CostEntry.COLUMN_ID,
                CostEntry.COLUMN_COST_TYPE,
                CostEntry.COLUMN_DATE,
                CostEntry.COLUMN_MILEAGE,
                CostEntry.COLUMN_COST_VALUE,
                CostEntry.COLUMN_COST_VOLUME,
                CostEntry.COLUMN_COMMENT,
                CostEntry.COLUMN_PHOTO};

        return new CursorLoader(this,
                mCurrentEntryUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int costTypeIndex = cursor.getColumnIndex(COLUMN_COST_TYPE);
            int dateIndex = cursor.getColumnIndex(COLUMN_DATE);
            int mileageIndex = cursor.getColumnIndex(COLUMN_MILEAGE);
            int valueIndex = cursor.getColumnIndex(COLUMN_COST_VALUE);
            int volumeIndex = cursor.getColumnIndex(COLUMN_COST_VOLUME);
            int commentIndex = cursor.getColumnIndex(COLUMN_COMMENT);
            int photoIndex = cursor.getColumnIndex(COLUMN_PHOTO);

            String costType = cursor.getString(costTypeIndex);
            String date = cursor.getString(dateIndex);
            String mileage = cursor.getString(mileageIndex);
            String value = cursor.getString(valueIndex);
            String volume = cursor.getString(volumeIndex);
            String comment = cursor.getString(commentIndex);
            String photo = cursor.getString(photoIndex);

            switch (costType){
                case COST_TYPE_FUEL:
                    mCostTypeSpinner.setSelection(1);
                    break;
                case COST_TYPE_SERVICE:
                    mCostTypeSpinner.setSelection(2);
                    break;
                case COST_TYPE_TO:
                    mCostTypeSpinner.setSelection(3);
                    break;
                case COST_TYPE_OTHER:
                    mCostTypeSpinner.setSelection(4);
                    break;
                default: mCostTypeSpinner.setSelection(0);
            }
            mDateEditText.setText(date);
            mMileageEditText.setText(mileage);
            mCostValueEditText.setText(value);
            mCostVolumeEditText.setText(volume);
            mCommentEditText.setText(comment);

            if (photo != null) {
                byte[] decodedString = Base64.decode(photo, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                mPhotoImageView.setImageBitmap(decodedByte);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /**
     * Метод для кнопки загрузки фотографии.
     */
    public void loadImageClick(View view) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
    }

    static final int GALLERY_REQUEST = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        Bitmap bitmap = null;
        ImageView imageViewLoad = (ImageView) findViewById(R.id.imageViewPhotoLoad);
        switch (requestCode) {
            case GALLERY_REQUEST:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    imageViewLoad.setImageBitmap(bitmap);
                }
        }
    }

    /**
     * Метод для изменения размера изображения.
     * @param image - изображение;
     * @param maxSize - новый размер;
     * @return - возвращает сжатое до нужных размеров изображение.
     */
    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
}
