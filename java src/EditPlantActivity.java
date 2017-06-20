package com.plant.plant;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EditPlantActivity extends AppCompatActivity {
    final String TAG = "EditPlantActivity";

    private static EditText wateringIntervalDays;
    private static SeekBar wateringIntervalSeek;
    private static ImageView plantImage;
    private static TextView notesText;
    private static Button rotatePlantButton;

    static String mCurrentPhotoPath;

    static final int REQUEST_TAKE_PHOTO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_plant);
        plantImage = (ImageView) findViewById(R.id.plantImage);
        rotatePlantButton = (Button) findViewById(R.id.btnRotatePlant);
        rotatePlantButton.setEnabled(false);
        initPlantImage();
    }

    public void initPlantImage() {
        //TODO: if there is no loaded picture and if it's not a new entry
        plantImage.setImageResource(R.drawable.no_picture);
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
//                Uri photoURI = FileProvider.getUriForFile(this,
//                        "com.plant.plant",
//                        photoFile);
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

    public void savePlant(View v) {
        //TODO: save plant in memory
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
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
                //enable the rotate button
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
                            saveImage(new File(mCurrentPhotoPath), bitmap);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_180:
                            bitmap = getRotatedImage(bitmap, 180);
                            saveImage(new File(mCurrentPhotoPath), bitmap);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_270:
                            bitmap = getRotatedImage(bitmap, 270);
                            saveImage(new File(mCurrentPhotoPath), bitmap);
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
}
