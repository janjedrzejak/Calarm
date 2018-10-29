package calarm.calarm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class PreferencesActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String PREFERENCES_NAME = "Calarm";
    private static final String PREFERENCES_PHONENUMBER = "phonenumber";
    private static final String PREFERENCES_MAIL = "mail";
    private static final String PREFERENCES_PIN = "pin";
    private static final String PREFERENCES_TIMEAWAKE = "timeawake";

    private EditText editTextPhonenumber;
    private EditText editTextMail;
    private EditText editTextPin;
    private EditText editTextTimeawake;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        preferences = getSharedPreferences(PREFERENCES_NAME, AppCompatActivity.MODE_PRIVATE);

        editTextPhonenumber = (EditText) findViewById(R.id.editText); //phone no
        editTextMail = (EditText) findViewById(R.id.editText2); //mail
        editTextPin = (EditText) findViewById(R.id.editText3); //pin
        editTextTimeawake = (EditText) findViewById(R.id.editText4); //awake time

        ImageView btnClose = (ImageView) findViewById(R.id.imageView2);
        Button btnZatwiedz = (Button) findViewById(R.id.btnZatwierdz);
        btnZatwiedz.setOnClickListener(this);
        btnClose.setOnClickListener(this);



    }

    private void saveData() {
        SharedPreferences.Editor preferencesEditor = preferences.edit();

        String phonenumber = editTextPhonenumber.getText().toString();
        String mail = editTextMail.getText().toString();
        String pin = editTextPin.getText().toString();
        String timeawake = editTextTimeawake.getText().toString();

        preferencesEditor.putString(PREFERENCES_PHONENUMBER, phonenumber);
        preferencesEditor.putString(PREFERENCES_MAIL, mail);
        preferencesEditor.putString(PREFERENCES_PIN, pin);
        preferencesEditor.putString(PREFERENCES_TIMEAWAKE, timeawake);

        preferencesEditor.commit();
    }


    /*
    private void restoreData() {
        String textFromPreferences = preferences.getString(PREFERENCES_TEXT_FIELD, "");
        etToSave.setText(textFromPreferences);
    }
    */

    public void onClick(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        switch (v.getId()) {
            case R.id.btnZatwierdz:
                saveData();
                startActivity(intent);
            case R.id.imageView2:
                startActivity(intent);
        }
    }

}
