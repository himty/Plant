package com.plant.plant;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class EditPlantActivity extends AppCompatActivity {
    final String TAG = "EditPlantActivity";

    private static EditText wateringIntervalDays;
    private static SeekBar wateringIntervalSeek;
    private static ImageView plantImage;
    private static TextView notesText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_plant);
        initPlantImage();
    }

    public void savePlant() {
        //TODO: save plant in memory
    }

    public void initPlantImage() {
        //TODO: if there is no loaded picture and if it's not a new entry
        plantImage = (ImageView) findViewById(R.id.plantImage);
        plantImage.setImageResource(R.drawable.no_picture);
    }

    public void setLocation() {
        //TODO: record location of phone
    }

    public void takePicture() {
        //TODO: start camera app
    }
}
