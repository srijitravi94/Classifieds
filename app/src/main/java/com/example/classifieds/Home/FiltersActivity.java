package com.example.classifieds.Home;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.classifieds.R;

public class FiltersActivity extends AppCompatActivity {

    private Button mSaveBtn;
    private EditText mCountry, mState, mCity;
    private ImageView mBackArrow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filters);

        initWidgets();
        init();
    }

    private void hideSoftKeyboard(){
        mCountry.onEditorAction(EditorInfo.IME_ACTION_DONE);
        mState.onEditorAction(EditorInfo.IME_ACTION_DONE);
        mCity.onEditorAction(EditorInfo.IME_ACTION_DONE);
    }

    private void toastService(String message) {
        Toast.makeText(FiltersActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void initWidgets() {
        mSaveBtn = (Button) findViewById(R.id.btnSaveFilters);
        mCountry = (EditText) findViewById(R.id.country);
        mState = (EditText) findViewById(R.id.state);
        mCity = (EditText) findViewById(R.id.city);
        mBackArrow = (ImageView) findViewById(R.id.backArrow);
    }

    private void init() {

        getFilterPreferences();

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard();

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FiltersActivity.this);
                SharedPreferences.Editor editor = preferences.edit();

                editor.putString(getString(R.string.preferences_country), mCountry.getText().toString());
                editor.commit();

                editor.putString(getString(R.string.preferences_state), mState.getText().toString());
                editor.commit();

                editor.putString(getString(R.string.preferences_city), mCity.getText().toString());
                editor.commit();

                toastService(getString(R.string.saved_preferences));
            }
        });

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getFilterPreferences(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String country = preferences.getString(getString(R.string.preferences_country), "");
        String state = preferences.getString(getString(R.string.preferences_state), "");
        String city = preferences.getString(getString(R.string.preferences_city), "");

        mCountry.setText(country);
        mState.setText(state);
        mCity.setText(city);
    }
}
