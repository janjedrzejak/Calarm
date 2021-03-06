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

    private TextView readText, saveText, answerText; //dev textViews
    private Sensor mySensor; //accelerometr
    private SensorManager SM; //accellerometer manager
    private LocationManager LM; //gps manager
    private SmsManager smsManager = SmsManager.getDefault(); //sms manager
    private double X,Y,Z; //actual coordinates
    private double saveX, saveY, saveZ; //saved coordinates
    private boolean detect=false; //alarm is not turn on
    private boolean Alarm=false; //alarm isnt detected move
    private static final String PREFERENCES_NAME = "Calarm";
    private static final String PREFERENCES_PHONENUMBER = "phonenumber";
    private static final String PREFERENCES_MAIL = "mail";
    private static final String PREFERENCES_PIN = "pin";
    private static final String PREFERENCES_TIMEAWAKE = "timeawake";
    private static final String PREFERENCES_PRECISION = "precision";
    private SharedPreferences preferences; //saved user preferences
    private Context context; //activity context
    private Activity activity;
    private View view;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private double latitude; //gps latitude
    private double longitude; //gps longitude
    private ImageView image; //button on center
    private String phoneNo; //phone number to sending alarm data
    private double prec=(1.00 - (30.00 / 100.00)); //precision of detected move
    private int timeawake=2000; //time evacuation
    private String sms = "ALARM!"; //first line of alarm message

    //if isnt data save or if is the first app lauch saving default data to preferences
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
    //checking sms permission
    private boolean checkSMSPermission(){
        int smsPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS);
        if (smsPermission == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }
    //checking gps permission
    public boolean checkLocationPermission()
    {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }
    //show the sms permission window
    private void requestSMSPermission(){
        ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.SEND_SMS},PERMISSION_REQUEST_CODE);
    }
    //show the location permission window
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext(); //initial variables
        activity = this;
        //checking permissions
        if(!checkSMSPermission()) {
            requestSMSPermission();
        }
        if(!checkLocationPermission()) {
            requestLocationPermission();
        }
        //get shared preferences access and saving default data if is the first app lauch
        preferences = getSharedPreferences(PREFERENCES_NAME, AppCompatActivity.MODE_PRIVATE);
        if(!preferences.contains(PREFERENCES_PHONENUMBER)) {
            saveDefaultData();
            Toast.makeText(getApplicationContext(),"Uzupełnij ustawienia, \n aby aplikacja była gotowa do użycia.", Toast.LENGTH_LONG).show();
            readData();
        } else {
            readData();
        }

        //initial app variables
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
        //gps access trying connection
        try {
        LM = (LocationManager)getSystemService(LOCATION_SERVICE); //gps
            LocationListener locationListener = new LocationListener() {
                boolean isGPS = false;

                @Override
                public void onLocationChanged(Location location) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    if (!isGPS) {
                        Toast.makeText(getApplicationContext(), "GPS gotowy do działania", Toast.LENGTH_SHORT).show();
                        isGPS = true;
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
            LM.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 5, locationListener);
        } catch(SecurityException e) {
            requestLocationPermission(); //show gps permission window if the trying is not working
        }

    }
    private boolean stop=false;
    private boolean btnOn=false; //button lock is not clicked
    public void onClick(View v) {
        image = (ImageView) findViewById(R.id.btnDisable);
        switch (v.getId()) {
            case R.id.btnDisable:
                //=======================================================================
                if(phoneNo.length()==9) {

                    if (btnOn == false) {
                        image.setImageResource(R.drawable.odblokuj);
                        Toast.makeText(getApplicationContext(), "Alarm uzbrojony! \nCzas na wyjście: " + (timeawake/1000) + " sekund", Toast.LENGTH_LONG).show();
                        stop=false;
                    } else {
                        image.setImageResource(R.drawable.zablokuj);
                        btnOn = false;
                        detect = false;
                        Toast.makeText(getApplicationContext(), "Alarm rozbrojony!", Toast.LENGTH_LONG).show();
                        stop=true;
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
                                detect = true;
                            } else {
                                image.setImageResource(R.drawable.zablokuj);
                                btnOn = false;
                                detect = false;
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
    //reading data from shared preferencess
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
    //up 2.1 version upgrade method
    @Override
    public void onSensorChanged(SensorEvent event) {
        X = event.values[0];
        X = Math.round(X * 10.0) / 10.0;
        Y = event.values[1];
        Y = Math.round(Y * 10.0) / 10.0;
        Z = event.values[2];
        Z = Math.round(Z * 10.0) / 10.0;

        //***************DEV textViews to reading datas********************************
        //readText.setText("X: " +  X + " Y: " + Y + " Z: " + Z);
        //saveText.setText("X: " +  saveX + " Y: " + saveY + " Z: " + saveZ);
        //readText.setText("Długość: " + longitude + " " + "\nSzerokość: " + latitude);
        //*****************************************************************************
        if (detect) { //turn on detecting
            if (    (X >= (saveX - prec) && X <= (saveX + prec)) &&
                    (Y >= (saveY - prec) && Y <= (saveY + prec)) &&
                    (Z >= (saveZ - prec) && Z <= (saveZ + prec)))
            {
                //NOT DETECTED
            } else {
                Alarm=true;
                silentAlarm();
            }
        } else {
            Alarm=false;
        }
    }

    public void silentAlarm() {
        detect=false;
        final Handler handler = new Handler();
        final int delay = 120000; //milliseconds
        Toast.makeText(context, "wysłano", Toast.LENGTH_SHORT).show();
        smsManager.sendTextMessage(phoneNo, null, sms, null, null);
            handler.postDelayed(new Runnable() {
                public void run() {
                    if(!stop) {
                        sms = "MAPA: " + "https://www.google.com/maps/search/?api=1&query="+latitude+","+longitude; //https://www.google.com/maps/search/?api=1&query=latitude,longitude
                        smsManager.sendTextMessage(phoneNo, null, sms, null, null);
                        Toast.makeText(context, "wysłano", Toast.LENGTH_SHORT).show();
                        handler.postDelayed(this, delay);
                    }
                }
            }, delay);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}
