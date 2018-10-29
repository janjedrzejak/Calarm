package calarm.calarm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import android.telephony.SmsManager;


public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {

    private TextView readText, saveText, answerText;
    private Sensor mySensor;
    private SensorManager SM;

    private double X,Y,Z; //actual coordinates
    private double saveX, saveY, saveZ; //saved coordinates
    private boolean onLock=false;
    private boolean Alarm=false;

    private static final String PREFERENCES_NAME = "Calarm";
    private static final String PREFERENCES_PHONENUMBER = "phonenumber";
    private static final String PREFERENCES_MAIL = "mail";
    private static final String PREFERENCES_PIN = "pin";
    private static final String PREFERENCES_TIMEAWAKE = "timeawake";

    private SharedPreferences preferences;



    ImageView image;
    SmsManager smsManager = SmsManager.getDefault();
    String phoneNo;
    String sms = "Test alarmu samochodowego. Zignoruj wiadomość pozdrawiam";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences(PREFERENCES_NAME, AppCompatActivity.MODE_PRIVATE);
        readData();

        SM = (SensorManager)getSystemService(SENSOR_SERVICE);
        mySensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        SM.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_NORMAL);
        readText = (TextView)findViewById(R.id.readText);
        saveText = (TextView)findViewById(R.id.saveText);
        answerText = (TextView)findViewById(R.id.sText);

        ImageView btnDisable = (ImageView)findViewById(R.id.btnDisable);
        ImageView btnPreferencje = (ImageView)findViewById(R.id.imageView3);

        btnDisable.setOnClickListener(this);
        btnPreferencje.setOnClickListener(this);



    }

    private boolean btnOn=false;
    public void onClick(View v) {
        image = (ImageView) findViewById(R.id.btnDisable);
        switch (v.getId()) {
            case R.id.btnDisable:

                saveX = X;
                saveY = Y;
                saveZ = Z;

                if(btnOn==false) {
                    image.setImageResource(R.drawable.odblokuj);
                    btnOn=true;
                    onLock = true;
                } else {
                    image.setImageResource(R.drawable.zablokuj);
                    btnOn=false;
                    onLock = false;
                }
                break;
            case R.id.imageView3:
                Intent intent = new Intent(this, PreferencesActivity.class);
                startActivity(intent);
                break;
        }

    }

    private void readData() {
        String phonenumberFrompreferences = preferences.getString(PREFERENCES_PHONENUMBER, "");
        String mailFrompreferences = preferences.getString(PREFERENCES_MAIL, "");
        String pinFrompreferences = preferences.getString(PREFERENCES_PIN, "");
        String timeawakeFrompreferences = preferences.getString(PREFERENCES_TIMEAWAKE, "");

        phoneNo = phonenumberFrompreferences;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        X = event.values[0]; X = Math.round(X);
        Y = event.values[1]; Y = Math.round(Y);
        Z = event.values[2]; Z = Math.round(Z);

        //readText.setText("X: " +  X);
        //saveText.setText("X: " +  saveX);

        if(onLock) {
            if(X!=saveX || Y!=saveY) {
                answerText.setText("ALARM!");
                Alarm=true;
            }
        } else {
            answerText.setText("");
            Alarm=false;
        }

        if(Alarm==true) {
            smsManager.sendTextMessage(phoneNo, null, sms, null, null);
            onLock=false;
            Alarm=false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
