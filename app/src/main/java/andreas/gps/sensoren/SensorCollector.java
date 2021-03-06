package andreas.gps.sensoren;

/**
 * Created by root on 11/10/15.
 */

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.Stack;



/**
 * Created by root on 11/9/15.
 */

public class SensorCollector implements SensorEventListener {
    private static SensorManager sensorManager;
    public void start(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        //lastUpdate = System.currentTimeMillis();
        //register this class as a listener for the orientation and
        // accelerometer sensors

        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY),
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
                SensorManager.SENSOR_DELAY_NORMAL);

    }

    public void stop(){
        sensorManager.unregisterListener(this);
    }


    private Stack<SensorActor> SensorActors;

    public SensorCollector(SensorActor sensact){
        SensorActors = new Stack<SensorActor>();
        this.set(sensact);
    }

    public void push(SensorActor SensorActor){
        SensorActors.push(SensorActor);
    }

    public void pop(){
        SensorActors.pop();
    }

    public void set(SensorActor SensorActor){
        SensorActors.pop();
        SensorActors.push(SensorActor);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        SensorActor currentSensorActor = SensorActors.peek();

        float[] values = event.values;
        float x;
        float y;
        float z;
        float time;
        time = event.timestamp;

        int eventtype = event.sensor.getType();
        switch (eventtype) {
            case Sensor.TYPE_GYROSCOPE:

                // Movement
                x = values[0];
                y = values[1];
                z = values[2];
                currentSensorActor.Verwerk_Gyroscoop(x, y, z, time);
                break;
            case Sensor.TYPE_ACCELEROMETER:
                // Movement
                x = values[0];
                y = values[1];
                z = values[2];

                currentSensorActor.Verwerk_Accelerometer(x, y, z, time);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                x = values[0];
                y = values[1];
                z = values[2];
                currentSensorActor.Verwerk_Magn(x, y, z, time);
                break;
            case Sensor.TYPE_PROXIMITY:
                x = values[0];
                currentSensorActor.verwerk_Prox(x, time);
                break;
            case Sensor.TYPE_GAME_ROTATION_VECTOR:
                x = values[0];
                y = values[1];
                z = values[2];
                currentSensorActor.Verwerk_rot(x, y, z, time);
            case Sensor.TYPE_LIGHT:
                x = values[0];
                currentSensorActor.Verwerk_licht(x, time);

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
