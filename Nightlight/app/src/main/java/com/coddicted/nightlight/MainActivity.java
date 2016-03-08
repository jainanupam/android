package com.coddicted.nightlight;

import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivityTAG";

    Camera cam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void toggleFlashlight(View v) throws CameraAccessException {
        if(getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Log.d(TAG, "Flashlight found");
        } else {
            Log.d(TAG, "Flashlight not found/ supported");
            return;
        }

        Camera cam = getCameraInstance();
        Camera.Parameters p = cam.getParameters();
        if(!p.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)){
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            cam.setParameters(p);
            cam.startPreview();
        } else {
            p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            cam.setParameters(p);
            cam.stopPreview();
        }
    }

    private Camera getCameraInstance(){
        if(cam == null){
            cam = Camera.open();
        }
        return cam;
    }
}
