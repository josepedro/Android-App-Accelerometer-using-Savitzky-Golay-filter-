package com.example.raghupati.accelerometer;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {


    private TextView h, x, y, z, magbefore, xsg, ysg, zsg, magafter;
    private SensorManager sensorManager;
    private Sensor sensor;
    private Vibrator vibrator;
    private Button start, stop;
    private List<Double> arrayList;
    private Boolean record = false;
    private GraphView graphView;
    private double values[];
    private double well[];
    private double filt[];
    private LineGraphSeries<DataPoint> series, filtered;

    private double[] xvaluesg = new double[5];
    private double[] yvaluesg = new double[5];
    private double[] zvaluesg = new double[5];
    private double xval,yval,zval,magbeforevalue,xsgval,ysgval,zsgval,magaftervalue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        //initialize views
//        h = (TextView) findViewById(R.id.heading);
        x = (TextView) findViewById(R.id.xdata);
        y = (TextView) findViewById(R.id.ydata);
        z = (TextView) findViewById(R.id.zdata);
        magbefore = (TextView) findViewById(R.id.magnitude_data_before);
        xsg = (TextView) findViewById(R.id.xdatasg);
        ysg = (TextView) findViewById(R.id.ydatasg);
        zsg = (TextView) findViewById(R.id.zdatasg);
        magafter = (TextView) findViewById(R.id.magnitude_data_after);
        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);
        graphView = (GraphView) findViewById(R.id.graph);
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setScalable(true);
        graphView.getViewport().setScrollable(true);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(50);

        for (int i = 0; i < 5; i++) {
            xvaluesg[i] = 0;
            yvaluesg[i] = 0;
            zvaluesg[i] = 0;
        }

        //START BUTTON
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arrayList = new ArrayList<Double>();
                v.setClickable(false);
                record = true;
                graphView.removeAllSeries();
            }
        });

        //STOP BUTTON
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                record = false;
                start.setClickable(true);
                Double[] arr = arrayList.toArray(new Double[arrayList.size()]);
                values = new double[arr.length];
                for (int j = 0; j < arr.length; j++) {
                    values[j] = arr[j].doubleValue();
                }
                SGFilter sgFilter = new SGFilter(3, 3);
                filt = sgFilter.computeSGCoefficients(3, 3, 4);
                well = sgFilter.smooth(values, filt);
                series = new LineGraphSeries<DataPoint>();
                series.setColor(Color.RED);
                filtered = new LineGraphSeries<DataPoint>();
                filtered.setColor(Color.BLUE);
                for (int i = 0; i < values.length; i++) {
                    series.appendData(new DataPoint(i, values[i]), true, 100);
                }
                for (int i = 0; i < well.length; i++) {
                    filtered.appendData(new DataPoint(i, well[i]), true, 100);
                }
                graphView.addSeries(series);
                graphView.addSeries(filtered);
            }
        });


        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        xval = event.values[0];
        yval = event.values[1];
        zval = event.values[2];
        magaftervalue = Math.sqrt(event.values[0] * event.values[0] + event.values[0] * event.values[0]
                + event.values[0] * event.values[0]);
        xval = Double.parseDouble(String.format("%.4f",xval));
        yval = Double.parseDouble(String.format("%.4f",yval));
        zval = Double.parseDouble(String.format("%.4f",zval));
        magaftervalue = Double.parseDouble(String.format("%.4f",magaftervalue));
        x.setText("XForce - " +xval);
        y.setText("YForce - " +yval);
        z.setText("ZForce - " +zval);
        magbefore.setText("Magnitude - " + magaftervalue );

        for(int i=0;i<4;i++){
            xvaluesg[i] = xvaluesg[i+1];
            yvaluesg[i] = yvaluesg[i+1];
            zvaluesg[i] = zvaluesg[i+1];
        }
        xvaluesg[4] = event.values[0];
        yvaluesg[4] = event.values[1];
        zvaluesg[4] = event.values[2];
        xsgval = (-3*xvaluesg[0]+12*xvaluesg[1]+17*xvaluesg[2]+12*xvaluesg[3]-3*xvaluesg[4])/35;
        ysgval = (-3*yvaluesg[0]+12*yvaluesg[1]+17*yvaluesg[2]+12*yvaluesg[3]-3*yvaluesg[4])/35;
        zsgval = (-3*zvaluesg[0]+12*zvaluesg[1]+17*zvaluesg[2]+12*zvaluesg[3]-3*zvaluesg[4])/35;
        magaftervalue = Math.sqrt(xsgval*xsgval+xsgval*xsgval+xsgval*xsgval);
        xsgval = Double.parseDouble(String.format("%.4f",xsgval));
        ysgval = Double.parseDouble(String.format("%.4f",ysgval));
        zsgval = Double.parseDouble(String.format("%.4f",zsgval));
        magaftervalue = Double.parseDouble(String.format("%.4f",magaftervalue));
        xsg.setText("X-SG - " + xsgval);
        ysg.setText("Y-SG - " + ysgval);
        zsg.setText("Z-SG - " + zsgval);
        magafter.setText("Mag-SG - " + magaftervalue );


        if (record) {
            arrayList.add(Math.sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2]));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
