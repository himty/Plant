package com.plant.plant;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

public class EditPlantActivity extends AppCompatActivity {
    final String TAG = "EditPlantActivity";

    private static TextView wateringIntervalText;
    private static SeekBar wateringIntervalSeek;
    private static ImageView plantImage;
    private static TextView notesText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_plant);
        initWateringInterval();
        initPlantImage();
        initNotesText();
    }

    public void savePlant() {
        //TODO: save plant in memory
    }

    public void initWateringInterval() {
        wateringIntervalText = (TextView) findViewById(R.id.wateringIntervalDays);
        wateringIntervalSeek = (SeekBar) findViewById(R.id.wateringIntervalSeek);
        wateringIntervalText.setText(wateringIntervalSeek.getProgress() + " Days");

        final int MAX = 20;
        final int MIN = 1;
        wateringIntervalSeek.setMax(MAX - MIN);
        wateringIntervalText.setText(MIN + " Days");

        wateringIntervalSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int value = wateringIntervalSeek.getProgress() + MIN;
                wateringIntervalText.setText(value + " Days");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void initPlantImage() {
        //TODO: if there is no loaded picture and if it's not a new entry
        plantImage = (ImageView) findViewById(R.id.plantImage);
        plantImage.setImageResource(R.drawable.no_picture);
    }

    public void initNotesText() {
//        ScrollView scroller = new ScrollView(this);
        notesText = (TextView) findViewById(R.id.notesText);
//        scroller.addView(notesText);
        notesText.setMovementMethod(new ScrollingMovementMethod());
    }

    public void setLocation() {
        //TODO: record location of phone
    }
}
