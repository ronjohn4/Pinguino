//  pinguino
//  RETSworks.com, Copyright 2015
//
//  Takes any photo and turns it into a Selfie by adding You.

package com.retsworks.pinguino;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class NewPhotoActivity extends AppCompatActivity {
    String TAG = "PINGUINO-NewPhotoActivity";
    PhotoEntry spSettings;
    ImageView imageviewEdit;
    Bitmap bitmapEdit;
    float hueDistance = 5.0f;
    private int fieldImgXY[] = new int[2];
    String newTempFile = "";


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.new_photo_menu, menu);
        return true;
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // Controls haven't been placed on the canvas at onCreate() which then gives (0, 0).
        imageviewEdit.getLocationOnScreen(fieldImgXY);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "NewPhotoActivity onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_photo);

        //persistence is through the settings object not a bundle
        SettingsContract sc = new SettingsContract();
        spSettings = sc.loadSettings(this);
//        if (savedInstanceState != null) {
//              restore values saved in the Bundle
//        }

        Intent intent = getIntent();
        newTempFile = intent.getStringExtra("TempNewPhoto");

        Log.d(TAG,"NewPhotoActivity.onCreate() - newpath:" + newTempFile);

        imageviewEdit = (ImageView) findViewById(R.id.imageEdit);
        TextView textEditPhotoInstructions = (TextView) findViewById(R.id.textEditPhotoInstructions);

        textEditPhotoInstructions.setText(getString(R.string.InstructionsPhotoEdit));

        bitmapEdit = fetchBitmapFromTempFile(newTempFile);
        imageviewEdit.setImageBitmap(bitmapEdit);

        imageviewEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    ImageView imageView = (ImageView) findViewById(R.id.imageEdit);
                    Drawable drawable = imageView.getDrawable();
                    Rect imageBounds = drawable.getBounds();

                    //original height and width of the bitmap
                    int intrinsicHeight = drawable.getIntrinsicHeight();
                    int intrinsicWidth = drawable.getIntrinsicWidth();

                    //height and width of the visible (scaled) image
                    int scaledHeight = imageBounds.height();
                    int scaledWidth = imageBounds.width();

                    //Find the ratio of the original image to the scaled image
                    //Should normally be equal unless a disproportionate scaling
                    //(e.g. fitXY) is used.
                    float heightRatio = (float) intrinsicHeight / scaledHeight;
                    float widthRatio = (float) intrinsicWidth / scaledWidth;

                    //get the distance from the left and top of the image bounds
                    int scaledImageOffsetX = (int) (event.getX() - imageBounds.left);
                    int scaledImageOffsetY = (int) (event.getY() - imageBounds.top);

                    //scale these distances according to the ratio of your scaling
                    //For example, if the original image is 1.5x the size of the scaled
                    //image, and your offset is (10, 20), your original image offset
                    //values should be (15, 30).
                    int originalImageOffsetX = (int) (scaledImageOffsetX * widthRatio);
                    int originalImageOffsetY = (int) (scaledImageOffsetY * heightRatio);

                    Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                    int pixel = bitmap.getPixel(originalImageOffsetX, originalImageOffsetY);
                    int r = (pixel >> 16) & 0xff;
                    int g = (pixel >> 8) & 0xff;
                    int b = pixel & 0xff;
                    int thisColor = 0xFF000000 | r | g | b;

                    Log.d(TAG, "selected Color:" + thisColor);
                    Log.d(TAG, "r:" + r + " b:" + b + " g:" + g);

                    bitmapEdit = createTransparentBitmapFromBitmap(bitmap, pixel);
                    imageviewEdit.setImageBitmap(bitmapEdit);

                    saveWorkingImage();
                }
                return true;
            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.about:
                Intent browserIntentAbout = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.ronjohnsonconsulting.com/pinguino"));
                startActivity(browserIntentAbout);
                return true;
            case R.id.help:
                Intent browserIntentHelp = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.ronjohnsonconsulting.com/pinguino"));
                startActivity(browserIntentHelp);
                return true;
            case R.id.done:
                try {
                    Log.d(TAG, "saveTempFileToApp()");
                    saveTempFileToApp(newTempFile);
                    Log.d(TAG,"SettingsContract");
                    SettingsContract sc = new SettingsContract();
                    spSettings = sc.loadSettings(getApplicationContext());
                    Log.d(TAG, "setPhotoPathName()");
                    spSettings.setPhotoPathName(newTempFile);
                    sc.saveSettings(getApplicationContext(), spSettings);

                    Intent returnIntent = new Intent(); //calling activity will reload
                    setResult(Activity.RESULT_OK, returnIntent);

                    finish();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.cancel:
                finish();
                return true;
            case R.id.reset:
                try {
                    resetFiles(newTempFile);
                    bitmapEdit = fetchBitmapFromTempFile(newTempFile);
                    imageviewEdit.setImageBitmap(bitmapEdit);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    // Update when handling multiple photos
    private String uniqueFilename() {
        return "pinguino_working_image.png";
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SettingsContract sc = new SettingsContract();
        Log.d(TAG,"getDefaultPhoto()-" + spSettings.getDefaultPhoto());
        Log.d(TAG, "getPhotoPathName()-" + String.valueOf(spSettings.getPhotoPathName()));
        sc.saveSettings(this, spSettings);
        saveWorkingImage();

        Log.d(TAG,"onSaveInstanceState");
    }


// helper functions-------------------------------------------------------------------------
    public void saveWorkingImage() {
        Context context = getApplicationContext();
        String appDir = HelperFunctions.getAppTempDir(context);

        String workingfileName = uniqueFilename();
        File outFile = new File(appDir + "/" + workingfileName);
        FileOutputStream outStream = null;

        try {
            outStream = new FileOutputStream(outFile);
            bitmapEdit.compress(Bitmap.CompressFormat.PNG, 100, outStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (outStream != null) {
                    outStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void saveTempFileToApp(String fileToSave) throws IOException {
        Log.d(TAG,"saveTempFileToApp: " + fileToSave);

        Context context = getApplicationContext();
        String appTempDir = HelperFunctions.getAppTempDir(context);
        String appDir = HelperFunctions.getAppDir(context);

        InputStream in = getContentResolver().openInputStream(
                HelperFunctions.getUriFromPath(appTempDir + "/" + uniqueFilename()));
        File outFile = new File(appDir + "/" + fileToSave);
        FileOutputStream out = null;

        Log.d(TAG,"saveTempFileToApp-outfile:" + outFile.toString());
        Log.d(TAG,"saveTempFileToApp-in:" + in.toString());


        try {
            out = new FileOutputStream(outFile);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    public void resetFiles(String fileToReset) throws IOException {
        Log.d(TAG,"resetFiles: " + fileToReset);

        Context context = getApplicationContext();
        String appTempDir = HelperFunctions.getAppTempDir(context);

        InputStream in = getContentResolver().openInputStream(
                HelperFunctions.getUriFromPath(appTempDir + "/" + fileToReset));
        File outFile = new File(appTempDir + "/" + uniqueFilename());
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(outFile);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    public Bitmap createTransparentBitmapFromBitmap(Bitmap bitmap, int replaceThisColor) {
        float[] hsv = new float[3];
        Color.colorToHSV(replaceThisColor, hsv);
        float hueSelected = hsv[0];

        if (bitmap != null && replaceThisColor != Color.TRANSPARENT) {
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

            int picw = bitmap.getWidth();
            int pich = bitmap.getHeight();
            int[] pix = new int[picw * pich];
            bitmap.getPixels(pix, 0, picw, 0, 0, picw, pich);

            for (int i = 0; i < pix.length; i++) {
                hsv[0] = 0f;
                hsv[1] = 0f;
                hsv[2] = 0f;
                Color.colorToHSV(pix[i], hsv);

                if (Math.abs(hsv[0] - hueSelected) < hueDistance) {
                    pix[i] = Color.TRANSPARENT;
                }
            }
            bitmap = Bitmap.createBitmap(pix, bitmap.getWidth(), bitmap.getHeight(),
                    Bitmap.Config.ARGB_8888);
         }
        return bitmap;
    }


    private Bitmap fetchBitmapFromTempFile(String requestedFile) {
        Log.d(TAG, "fetchBitmapFromTempFile:" + requestedFile);
        String appDir =  HelperFunctions.getAppTempDir(getApplicationContext());
        String fname = new File(appDir + "/" + requestedFile).getAbsolutePath();

        Log.d(TAG,"fname:" + fname);
        Log.d(TAG, "appDir:" + appDir);
        Bitmap bitmapReturn =  BitmapFactory.decodeFile(fname);

        return bitmapReturn;
    }

}
