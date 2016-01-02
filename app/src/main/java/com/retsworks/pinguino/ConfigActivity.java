package com.retsworks.pinguino;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;


public class ConfigActivity extends Activity {
    PhotoEntry spSettings;
    String TAG = "PINGUINO-ConfigActivity";
    private static final int SELECT_PHOTO = 100;


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        Log.d(TAG, "onActivityResult: " + requestCode);
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
                    startActivity(intent);
                }
            default:
                Log.d(TAG, "default onActivityResult:" + requestCode);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        this.onCreate(null);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        SettingsContract sc = new SettingsContract();
        spSettings = sc.loadSettings(this);

        ImageView imageDisplay = (ImageView)findViewById(R.id.imageView);
        TextView textInstructions = (TextView)findViewById(R.id.textViewInstructions);
        Button buttonSelectPhoto = (Button)findViewById(R.id.buttonSelectPhoto);

        String fileToDecode = HelperFunctions.getAppDir(this) +
                "/" + spSettings.getPhotoPathName();
        Bitmap bitmapEdit = BitmapFactory.decodeFile(fileToDecode);
        imageDisplay.setImageBitmap(bitmapEdit);

        if (spSettings.getPhotoPathName() == "default.png") {
            buttonSelectPhoto.setText(getString(R.string.ButtonFirstTime));
            textInstructions.setText(getString(R.string.InstructionsFirstTime));
        }
        else {
            buttonSelectPhoto.setText(getString(R.string.ButtonNextTime));
            textInstructions.setText(getString(R.string.InstructionsNextTime));
        }

        buttonSelectPhoto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, SELECT_PHOTO);
            }
        });

    }
}
