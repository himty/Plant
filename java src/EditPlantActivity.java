package com.plant.plant;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EditPlantActivity extends AppCompatActivity{
    final String TAG = "EditPlantActivity";

    //components to save
    private static EditText plantName;
    private static EditText plantSpecies;
    private static ImageView plantImage;
    private static EditText wateringIntervalDays;
    private static TextView location; //TODO: implement location (maybe not TextView)
    private static EditText notesText;
    private static EditText startDateText;

    //other componenets
    private static Button rotatePlantButton;

    private static String mCurrentPhotoPath = null;
    boolean flag_stop_saveplant;

    private static final int REQUEST_TAKE_PHOTO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_plant);

        plantName = (EditText) findViewById(R.id.nameText);
        plantSpecies = (EditText) findViewById(R.id.speciesText);
        plantImage = (ImageView) findViewById(R.id.plantImage);
        wateringIntervalDays = (EditText) findViewById(R.id.wateringIntervalDays);
        notesText = (EditText) findViewById(R.id.notesText);
        rotatePlantButton = (Button) findViewById(R.id.btnRotatePlant);
        startDateText = (EditText) findViewById(R.id.startDateText);

        initPlantImage();
        initStartDate();
    }

    public void initPlantImage() {
        //TODO: if there is no loaded picture and if it's not a new entry
        if (mCurrentPhotoPath != null) {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath,bmOptions);
            plantImage.setImageBitmap(bitmap);
            rotatePlantButton.setEnabled(true);
        } else {
            plantImage.setImageResource(R.drawable.no_picture);
            rotatePlantButton.setEnabled(false);
        }
    }

    public void initStartDate() {
        final TextWatcher textWatcher = new TextWatcher() {
            boolean shouldIgnore = false;
            String prevValue = "";

            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (shouldIgnore)
                    return;

                shouldIgnore = true;
                //parse text into MM/DD/YY
                String result = "";
                String[] input = editable.toString().split("/");

                for (int i = 0; i < input.length; i++ ) {
                    //while deleting number before a slash on the last section or deleting
                    // a slash, don't create a new slash
                    if (prevValue.length() > editable.length() && i == input.length - 1
                            && (editable.length() == 0 || (editable.charAt(editable.length() - 1)) == '/')) {
                            result += input[i];
                    } else {
                        if (i < 2) { //not on year section
                            if (input[i].length() == 1) {
                                result += input[i];
                            }
                            if (input[i].length() == 2) {
                                if (prevValue.length() > editable.length() && prevValue.charAt(prevValue.length()-1) == '/'
                                        && i == input.length - 1) { //just deleted a slash
                                    result += input[i]; //no slash
                                } else {
                                    result += input[i] + "/"; //yes slash
                                }
                            }
                            if (input[i].length() == 3) {
                                result += input[i].substring(0,2) + "/" + input[i].charAt(2);
                            }
                        } else {
                            result += input[i];
                        }
                    }
                }

                startDateText.setText(result);
                startDateText.setSelection(startDateText.length());
                shouldIgnore = false;
                prevValue = result;

            }
        };

        startDateText.addTextChangedListener(textWatcher);
    }

    public void setLocation(View v) {
        //TODO: record location of phone
    }

    public void takePicture(View v) {
        if (hasCameraHardware(this)) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            //Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    //TODO: How does one error-catch
                    Log.e(TAG, "Error: image file cannot be created");
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    Uri photoURI = Uri.fromFile(photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            }
        } else {
            AlertDialog.Builder alertDialogBuilder =
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setTitle("Error")
                            .setMessage("No camera detected on this device.")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    public void rotatePlant(View v) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath,bmOptions);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = (int)(width * 1.0 / bitmap.getWidth() * bitmap.getHeight());

        bitmap = Bitmap.createScaledBitmap(bitmap,width,height,true);
        bitmap = getRotatedImage(bitmap, 90);

        saveImage(new File(mCurrentPhotoPath), bitmap);

        plantImage.setImageBitmap(bitmap);
    }

    /*
        Note: this overwrites previous files with the same name
        How to read plant afterwards ("_plt_" distinguishes it from other files):
        try {
            FileInputStream fis = openFileInput("_plt_" + "name_of_plant" + ".txt");
            ObjectInputStream is = new ObjectInputStream(fis);
            Plant testplant = (Plant) is.readObject();
            is.close();
            fis.close();
            Log.i(TAG, testplant.species); //or any other characteristic in Plant class
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error: Cannot read from plant file");
        }
     */
    public void savePlant(View v) {
        //needs a name to be saved
        if (plantName.getText().toString().equals("")) {
            AlertDialog.Builder alertDialogBuilder =
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setTitle("Error")
                            .setMessage("Please enter a plant name to save")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return;
        }

        formatPlantName();

        //TODO: And the user is not trying to edit an existing plant entry
        if(new File(getFilesDir()+"/_plt_"+plantName.getText().toString().replace(" ", "_").toLowerCase() + ".txt").exists()) {
            AlertDialog.Builder alertDialogBuilder =
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setTitle("Warning")
                            .setMessage("A plant is already named "+plantName.getText()+". Saving "
                                +"will overwrite the previous plant entry.")
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel(); //do nothing
                                }
                            })
                            .setPositiveButton("Save Anyways", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    doSavingAction();
                                    dialog.cancel();
                                }
                            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } else {
            doSavingAction();
        }
    }

    private void doSavingAction() {
        //create plant object from data on this activity
        Integer wateringIntervalTemp;
        String wateringIntervalTemp2 = wateringIntervalDays.getText().toString();
        if (wateringIntervalTemp2.equals("")) {
            wateringIntervalTemp = null;
        } else {
            wateringIntervalTemp = Integer.valueOf(wateringIntervalTemp2);
        }

        Plant plantObj = new Plant(plantName.getText().toString(), plantSpecies.getText().toString(),
                mCurrentPhotoPath, startDateText.getText().toString(), wateringIntervalTemp,
                notesText.getText().toString());

        try {
            FileOutputStream fos = openFileOutput("_plt_"+plantName.getText().toString().replace(" ", "_").toLowerCase() + ".txt", Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(plantObj);
            os.close();
            fos.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error: Plant save file cannot be written into.");
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getFilesDir();
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_TAKE_PHOTO) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                //enable the rotate button b/c it's not the default image anymore
                rotatePlantButton.setEnabled(true);

                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath,bmOptions);

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int width = displayMetrics.widthPixels;
                int height = (int)(width * 1.0 / bitmap.getWidth() * bitmap.getHeight());
                bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);

                //rotates an image if the phone gets it wrong apparently
                try {
                    ExifInterface ei = new ExifInterface(mCurrentPhotoPath);
                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED);

                    switch (orientation) {

                        case ExifInterface.ORIENTATION_ROTATE_90:
                            bitmap = getRotatedImage(bitmap, 90);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_180:
                            bitmap = getRotatedImage(bitmap, 180);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_270:
                            bitmap = getRotatedImage(bitmap, 270);
                            break;

                        case ExifInterface.ORIENTATION_NORMAL:

                        default:
                            break;
                    }
                }
                catch (Exception e) {
                    //TODO: how does one error-catch 2.0
                    e.printStackTrace();
                    Log.e(TAG, "Error: Cannot find image (for rotaion)");
                }

                saveImage(new File(mCurrentPhotoPath), bitmap); //save the scaled version
                plantImage.setImageBitmap(bitmap);
            }
        }
    }

    private boolean hasCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public static Bitmap getRotatedImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    private void saveImage(File file, Bitmap b) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);

            if (fos != null) {
                b.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error: Cannot save plant image");
        }
    }

    private void formatPlantName() {
        String plantNameString = plantName.getText().toString().trim();

        //delete mutiple spaces
        char prevChar = plantNameString.charAt(0);
        int i = 1;
        while (i < plantNameString.length() - 1) {
            if (prevChar == ' ' && plantNameString.charAt(i) == ' ') {
                plantNameString = plantNameString.substring(0, i) + plantNameString.substring(i+1);
            } else {
                prevChar = plantNameString.charAt(i);
                i++;
            }
        }

        plantName.setText(plantNameString); //fix the displayed name while you're at it
    }

    private void setSavePlantStopFlag(boolean stop) {
        flag_stop_saveplant = stop;
    }
}


