package org.tensorflow.lite.examples.objectdetection.locationandtextdetect;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.tensorflow.lite.examples.objectdetection.R;

public class MainActivity extends AppCompatActivity {
    Button btnLiveLocation, btnTextDec;
    private static final int PERMISSION_REQUEST_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color);

        checkPermission();
        requestPermission();

        btnTextDec = (Button) findViewById(R.id.btnTextDec);
        btnLiveLocation = (Button) findViewById(R.id.btnLiveLocation);

        btnLiveLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, LiveLocationActivity.class);
                startActivity(i);
            }
        });

        btnTextDec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, TextDetectorActivity.class);
                startActivity(i);
            }
        });
    }
    private boolean checkPermission() {

        int result5 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        int result6 = ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_FINE_LOCATION);
        return result5 == PackageManager.PERMISSION_GRANTED
                && result6 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_CODE);
    }
}