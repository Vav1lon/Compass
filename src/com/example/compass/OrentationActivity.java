package com.example.compass;

import android.app.Activity;
import android.hardware.*;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class OrentationActivity extends Activity implements SensorEventListener {

    private static final String CAMERA_FOCAL_LENGTH = "focal-length";
    private static final String CAMERA_VERTICAL_VIEW_ANGLE = "vertical-view-angle";
    private static final String CAMERA_HORIZONTAL_VIEW_ANGLE = "horizontal-view-angle";

    private float a = 0.001f;
    private float lastX;
    private int count;

    private SensorManager sensorManager;
    private Sensor rotationVector;

    private float[] matrixValues;
    private float[] mRotationMatrix;
    private float[] valuesRotation;
    private Camera camera;

    private Double focalLength;
    private Double verticalViewAngle;
    private Double horizontalViewAngle;

    private TextView readingAzimuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        readingAzimuth = (TextView) findViewById(R.id.azimutText);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        initCamera();
        focalLength = getCameraParam(camera, CAMERA_FOCAL_LENGTH);
        verticalViewAngle = getCameraParam(camera, CAMERA_VERTICAL_VIEW_ANGLE);
        horizontalViewAngle = getCameraParam(camera, CAMERA_HORIZONTAL_VIEW_ANGLE);
        releaseCamera();

        matrixValues = new float[3];
        mRotationMatrix = new float[16];
        valuesRotation = new float[16];

        lastX = 0f;
    }

    @Override
    protected void onResume() {
        sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_FASTEST);
        super.onResume();
    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
        if (arg1 != SensorManager.SENSOR_STATUS_ACCURACY_HIGH) {
            System.out.println("sdf");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ROTATION_VECTOR:
                valuesRotation = event.values.clone();
                count++;
                if (count == 17) {
                    update();
                    count = 0;
                }
                break;
        }
    }

    private void update() {
        SensorManager.getRotationMatrixFromVector(mRotationMatrix, valuesRotation);
        SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, mRotationMatrix);
        SensorManager.getOrientation(mRotationMatrix, matrixValues);

        matrixValues[0] = (float) Math.toDegrees(matrixValues[0]);

        readingAzimuth.setText(""+normalizeDataX(lowPass(matrixValues[0], lastX) + 22));

        lastX = matrixValues[0];
    }

    private float lowPass(float current, float last) {
        return last * (1.0f - a) + current * a;
    }

    private float highPass(float current, float last, float filtered) {
        return a * (filtered + current - last);
    }

    private int normalizeDataX(float data) {

        if (data < 0) {
            data += 360;
        }
        return (int) data;
    }

    public Double getCameraParam(Camera camera, String nameParam) {
        assert camera != null;
        return Double.parseDouble(camera.getParameters().get(nameParam));
    }

    private void releaseCamera() {
        camera.release();
    }

    private void initCamera() {
        camera = Camera.open();
    }
}