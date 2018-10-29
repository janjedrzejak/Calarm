package calarm.calarm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class PreferencesActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        ImageView btnClose = (ImageView) findViewById(R.id.imageView2);
        Button btnZatwiedz = (Button) findViewById(R.id.btnZatwierdz);
        btnZatwiedz.setOnClickListener(this);
        btnClose.setOnClickListener(this);
    }

    public void onClick(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        switch (v.getId()) {
            case R.id.btnZatwierdz:
                startActivity(intent);
            case R.id.imageView2:
                startActivity(intent);
        }
    }

}
