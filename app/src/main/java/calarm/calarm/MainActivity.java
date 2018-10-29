package calarm.calarm;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;



public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView xText,yText,zText;
    private Sensor mySensor;
    private SensorManager SM;

    private float X,Y,Z;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SM = (SensorManager)getSystemService(SENSOR_SERVICE);
        mySensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        SM.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_NORMAL);
        xText = (TextView)findViewById(R.id.xText);
        yText = (TextView)findViewById(R.id.yText);
        zText = (TextView)findViewById(R.id.zText);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        xText.setText("X: " +  event.values[0]);
        X = event.values[0];

        yText.setText("Y: " +  event.values[1]);
        Y = event.values[1];

        zText.setText("Z: " +  event.values[2]);
        Z = event.values[2];

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
