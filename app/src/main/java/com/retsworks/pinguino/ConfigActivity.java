package com.retsworks.pinguino;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;


public class ConfigActivity extends AppCompatActivity {
    PhotoEntry spSettings;
    String TAG = "PINGUINO-ConfigActivity";
    private static final int SELECT_PHOTO = 100;
    private static final int PREPARE_PHOTO = 200;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.config_menu, menu);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        Log.d(TAG, "onActivityResult: " + requestCode + " - " + resultCode);
        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    String newTempFile = null;
                    try {
                        newTempFile = HelperFunctions.loadTempNewPhoto(this,
                                imageReturnedIntent.getData());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Intent intent = new Intent();
                    intent.setClassName("com.retsworks.pinguino",
                            "com.retsworks.pinguino.NewPhotoActivity");
                    intent.putExtra("TempNewPhoto", newTempFile);
                    startActivityForResult(intent,PREPARE_PHOTO);
                }
                break;
            case PREPARE_PHOTO:
                //reload this activity so changes to the photo are redisplayed
                Intent refresh = new Intent();
                refresh.setClassName("com.retsworks.pinguino",
                        "com.retsworks.pinguino.ConfigActivity");
                startActivity(refresh);
                this.finish();
                break;
            default:
                Log.d(TAG, "default onActivityResult:" + requestCode);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate-setContentView()");
        setContentView(R.layout.activity_config);

        Log.d(TAG, "onCreate-SettingsContract");
        SettingsContract sc = new SettingsContract();
        Log.d(TAG, "onCreate-loadSettings()");
        spSettings = sc.loadSettings(this);

        ImageView imageDisplay = (ImageView)findViewById(R.id.imageView);
        TextView textInstructions = (TextView)findViewById(R.id.textViewInstructions);

        String fileToDecode = HelperFunctions.getAppDir(this) +
                "/" + spSettings.getPhotoPathName();
        Bitmap bitmapEdit = BitmapFactory.decodeFile(fileToDecode);
        imageDisplay.setImageBitmap(bitmapEdit);

        if (spSettings.getPhotoPathName() == "default.png") {
            textInstructions.setText(getString(R.string.InstructionsFirstTime));
        }
        else {
            textInstructions.setText(getString(R.string.InstructionsNextTime));
        }



    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.help:
                Intent helpActivity = new Intent();
                helpActivity.setClassName("com.retsworks.pinguino",
                        "com.retsworks.pinguino.HelpActivity");
                startActivity(helpActivity);
//                this.finish();
                return true;
            case R.id.photo:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, SELECT_PHOTO);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
