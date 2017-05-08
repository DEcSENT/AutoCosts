package dvinc.autocosts.activities;

/**
 * Created by DV on Space 5
 * 28.04.2017
 */

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import dvinc.autocosts.R;
import dvinc.autocosts.database.Contract.*;

/**
 * TODO: Класс для создания записи по расходам на авто
 * TODO: Сделать форму, выбор категорий, вставку фото и комментариев. Так же дополнительно подумать что нужно, а что не нужно знать при записи данных и внести правки в шаблон
 */
public class CreateActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** Идентификатор для загрузчика.*/
    private static final int LOADER = 0;

    /** Текущий URI для существующей записи (нуль если запись новая). */
    private Uri mCurrentEntryUri;

    /* Объявление полей для ввода пользовательской информации.*/
    private Spinner mCostTypeSpinner;
    private EditText mDateEditText;
    private EditText mMileageEditText;
    private EditText mCostValueEditText;
    private EditText mCostVolumeEditText;
    private EditText mCommentEditText;
    private ImageView mPhotoImageView;
    
    /** Переменная для хранения выранной категории.*/
    private String mCostType;

    /** Переменная для хранения строки с фото в формате басе64.*/
    private String image64;

    /** Кнопка для загрузки фотографии. */
    private Button mButtonLoadPhoto;

    /** Кнопка для сохранения записи. */
    private Button mButtonSaveEntry;

    /** Флаг, который отслеживает была ли запись отредактирована. */
    private boolean mEntryHasChanged = false;

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
        setContentView(R.layout.activity_create);

        CostTypeDefault = getResources().getString(R.string.costType_none);

        /* Инициализация полей для ввода пользовательской информации. */
        mCostTypeSpinner = (Spinner) findViewById(R.id.spinnerCostType);
        mDateEditText = (EditText) findViewById(R.id.editDate);
        mMileageEditText = (EditText) findViewById(R.id.editMileage);
        mCostValueEditText = (EditText) findViewById(R.id.editCostValue);
        mCostVolumeEditText = (EditText)findViewById(R.id.editCostVolume);
        mCommentEditText = (EditText) findViewById(R.id.editComment);
        mPhotoImageView = (ImageView) findViewById(R.id.imageViewPhotoLoad);

        /* По умолчанию прячем поле ввода для объема.*/
        mCostVolumeEditText.setVisibility(View.INVISIBLE);
        mCostVolumeEditText.setVisibility(View.GONE);

        /*Инициализация кнопок.*/
        mButtonLoadPhoto = (Button) findViewById(R.id.buttonLoadPhoto);
        mButtonSaveEntry = (Button) findViewById(R.id.buttonSaveEntry);


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
        // прикасался ли к ним или изменял ли их пользователь. Это нужно, чтобы нельзя было покинуть активность, если
        // имеются несохраненные данные.
        mCostTypeSpinner.setOnTouchListener(mTouchListener);
        mDateEditText.setOnTouchListener(mTouchListener);
        mMileageEditText.setOnTouchListener(mTouchListener);
        mCostValueEditText.setOnTouchListener(mTouchListener);
        mCostVolumeEditText.setOnTouchListener(mTouchListener);
        mCommentEditText.setOnTouchListener(mTouchListener);
        mPhotoImageView.setOnTouchListener(mTouchListener);

        //TODO: datepicker and fokuslistener here

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
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mCostType = CostTypeDefault;
            }
        });
    }

    /**
     * Метод для охранения пользовательской информации в базу данных.
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
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

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
     * @param image - изображение
     * @param maxSize - новый размер
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
