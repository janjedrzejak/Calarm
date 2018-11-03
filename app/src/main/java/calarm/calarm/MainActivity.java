package calarm.calarm;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import android.telephony.SmsManager;
import android.widget.Toast;


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
    private static final String PREFERENCES_PRECISION = "precision";

    private SharedPreferences preferences;

    private Context context;
    private Activity activity;
    private View view;
    private static final int PERMISSION_REQUEST_CODE = 1;

    private double latitude;
    private double longitude;

    ImageView image;
    SmsManager smsManager = SmsManager.getDefault();
    LocationManager locationManager;
    LocationListener locationListener;
    String phoneNo;
    double prec=(1.00 - (30.00 / 100.00));
    int timeawake=2000; //time evacuation
    String sms = "ALARM!";



    private void saveDefaultData() {
        SharedPreferences.Editor preferencesEditor = preferences.edit();

        String phonenumber = "";
        String mail = "";
        String pin = "";
        String timeawake = "30";

        preferencesEditor.putString(PREFERENCES_PHONENUMBER, phonenumber);
        preferencesEditor.putString(PREFERENCES_MAIL, mail);
        preferencesEditor.putString(PREFERENCES_PIN, pin);
        preferencesEditor.putString(PREFERENCES_TIMEAWAKE, timeawake);
        preferencesEditor.putInt(PREFERENCES_PRECISION, 30);

        preferencesEditor.commit();
    }

    private boolean checkPermission(){
        int smsPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS);
        int locationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        if (smsPermission == PackageManager.PERMISSION_GRANTED && locationPermission == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }
    private void requestPermission(){
        ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.SEND_SMS},PERMISSION_REQUEST_CODE);
        ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        activity = this;

        if(!checkPermission()) {
            requestPermission();
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };
        locationManager.requestLocationUpdates("gps", 1000,0,locationListener);
        longitude=longitude;

        preferences = getSharedPreferences(PREFERENCES_NAME, AppCompatActivity.MODE_PRIVATE);
        if(!preferences.contains(PREFERENCES_PHONENUMBER)) {
            saveDefaultData();
            Toast.makeText(getApplicationContext(),"Uzupełnij ustawienia, \n aby aplikacja była gotowa do użycia.", Toast.LENGTH_LONG).show();
            readData();
        } else {
            readData();
        }


        SM = (SensorManager)getSystemService(SENSOR_SERVICE);
        mySensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        SM.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_NORMAL);
        readText = (TextView)findViewById(R.id.readText);
        saveText = (TextView)findViewById(R.id.saveText);
        answerText = (TextView)findViewById(R.id.sText);

        ImageView btnDisable = (ImageView)findViewById(R.id.btnDisable);
        Button btnPreferencje = (Button)findViewById(R.id.btnPreferencje);

        btnDisable.setOnClickListener(this);
        btnPreferencje.setOnClickListener(this);



    }

    private boolean btnOn=false;
    public void onClick(View v) {
        image = (ImageView) findViewById(R.id.btnDisable);
        switch (v.getId()) {
            case R.id.btnDisable:
                //=======================================================================
                if(phoneNo.length()==9) {

                    if (btnOn == false) {
                        image.setImageResource(R.drawable.odblokuj);
                        Toast.makeText(getApplicationContext(), "Alarm uzbrojony! \nCzas na wyjście: " + (timeawake/1000) + " sekund", Toast.LENGTH_LONG).show();
                    } else {
                        image.setImageResource(R.drawable.zablokuj);
                        btnOn = false;
                        onLock = false;
                        Toast.makeText(getApplicationContext(), "Alarm rozbrojony!", Toast.LENGTH_LONG).show();

                        break;
                    }

                    Handler handler=new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Alarm gotowy do wykrywania intruzów", Toast.LENGTH_LONG).show();

                            saveX = X;
                            saveY = Y;
                            saveZ = Z;

                            if (btnOn == false) {
                                image.setImageResource(R.drawable.odblokuj);
                                btnOn = true;
                                onLock = true;
                            } else {
                                image.setImageResource(R.drawable.zablokuj);
                                btnOn = false;
                                onLock = false;
                            }
                        }
                    }, timeawake);
                //=======================================================================
                } else {
                    if(phoneNo.length()>=1 || phoneNo.length()<9 || phoneNo.length()>9) {
                        Toast.makeText(getApplicationContext(), "Podany numer ma zły format!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Nie podano numeru telefonu w ustawieniach", Toast.LENGTH_LONG).show();
                        }
                    }

                //=======================================================================
                break;
            case R.id.btnPreferencje:
                //=======================================================================
                Intent intent = new Intent(this, PreferencesActivity.class);
                startActivity(intent);
                //=======================================================================
                break;
        }

    }

    private void readData() {
        String phonenumberFrompreferences = preferences.getString(PREFERENCES_PHONENUMBER, "");
        String mailFrompreferences = preferences.getString(PREFERENCES_MAIL, "");
        String pinFrompreferences = preferences.getString(PREFERENCES_PIN, "");
        String timeawakeFrompreferences = preferences.getString(PREFERENCES_TIMEAWAKE, "");
        int precisionFrompreferences = preferences.getInt(PREFERENCES_PRECISION,0);

        if(phonenumberFrompreferences == "" || mailFrompreferences == "" || pinFrompreferences == "") {
            Toast.makeText(getApplicationContext(), "uzupełnij dane w ustawieniach!", Toast.LENGTH_LONG).show();
        }
        phoneNo = phonenumberFrompreferences;
        prec = (1.00 - (precisionFrompreferences / 100.00));
        timeawake = (Integer.parseInt(timeawakeFrompreferences)) * 1000;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        X = event.values[0]; X = Math.round(X*10.0) / 10.0;
        Y = event.values[1]; Y = Math.round(Y*10.0) / 10.0;
        Z = event.values[2]; Z = Math.round(Z*10.0) / 10.0;

        //readText.setText("X: " +  X + " Y: " + Y + " Z: " + Z);
        //saveText.setText("X: " +  saveX + " Y: " + saveY + " Z: " + saveZ);

        if(onLock) {
            if(
                    (X>=(saveX-prec) && X<=(saveX+prec)) &&
                    (Y>=(saveY-prec) && Y<=(saveY+prec)) &&
                    (Z>=(saveZ-prec) && Z<=(saveZ+prec))      ) {

                Alarm=false;

            } else {
                Alarm=true;
                answerText.setText("ALARM!");
            }

        } else {
            answerText.setText("");
            Alarm=false;
        }

        if(Alarm==true) {
            //smsManager.sendTextMessage(phoneNo, null, sms, null, null);
            onLock=false;
            Alarm=false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
