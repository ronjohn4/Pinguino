//  pinguino
//  RETSworks.com, Copyright 2015
//
//  Takes any photo and turns it into a Selfie by adding You.

/*
Dev Diary
TODO: add tracking
TODO: add purchase
TODO: send multiple SM photos
TODO: if messenger has photo attached already, overwrite with new composite?
TODO: move image loads to new thread
TODO: add timing to potential thread sections and get timing results, use logs
TODO: reset should go back to the last edit, not reload the file
TODO: automated tests
TODO: photo management should be a object

1/21 -  Support tablets
1/12 - config to resize based on screen and/or scroll
1/7 - handle portrait photos by trimming the source photo when making transparent
1/7 - handle portrait pinguino photos
1/1 - source control
12/13 - use file paths rather than uri when possible
12/07 - improve config page
12/07 - improve instructions
11/15 - rebranded selfieme to pinguino - updated images
10/27 - release alpha version
10/26 - add way for user to give feedback during beta
10/25 - fix call stack problem, returning from messenger without sending
10/25 - fix reset
10/25 - clean up HelperFunctions and duplicated functions
10/22 - fix large image memory issue
10/22 - convert normal photo into selfie photo with transparent background
10/21 - fix image click position
10/19 - improve icon
10/6 - save SM photo when edited
10/4 - display all photo sizes as full screen when editing
10/4 - save thumbnail (or create it from original when loading config)
10/4 - if target photo is too large, reduce when building composite
9/12 - save selfie images in app working directory
9/11 - selfie image display should be full width, like when sending an IM photo
9/11 - sqlite3
9/10 - load SelfieMe Config (preferences)
9/10 - persisting the state, onActivityResult() vs onResume() conflict
9/9 - King Scoopers (Abby is at Girl Scouts) - cleaning persist code
9/8 - Watched Udemy videos on SQLite
9/3 - temp files in cache directory so are removed when app is stopped
9/1 - install on device
8/31 - put SelfieMe photo over photo selected when sharing
8/30 - Display images from res as well as selected from the gallery
8/30 - File read/write permissions must be at manifest level, not activity
8/22 - improve the new icons, darker outlining, bigger images in bubble
8/22 - Use new icon set
8/20 - debug loading new testbackground settings field
8/19 - Store new greenscreen image in resources.  Load with settings.
8/18 - Created first green screen photo and made first prototype of me with Carla in Panama.
8/17 - Made a greenscreen out of a disposable tablecloth
*/

package com.retsworks.pinguino;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    String TAG = "PINGUINO-MainActivity";
    float PctSelfieSizeLandscrape = 0.5f;
    float PctSelfieSizePortrait = 1.0f;
    PhotoEntry spSettings;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        SettingsContract sc = new SettingsContract();
        spSettings = sc.loadSettings(this);

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendImage(intent);
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendMultipleImages(intent);
            }
        } else {
            // Handle other intents, such as being started from the home screen
            startActivity(intent);
        }
    }


    void handleSendImage(Intent intent) {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);

        // scan through list of intent activities and build a list with SMS only
        List<Intent> targetedShareIntents = new ArrayList<>();
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(shareIntent, 0);
        if (!resInfo.isEmpty()){
            for (ResolveInfo resolveInfo : resInfo) {
                String packageName = resolveInfo.activityInfo.packageName;

                //TODO: is this package name guaranteed on all installs?
                if (packageName.equals("com.android.mms")){
                    String appPath = HelperFunctions.getAppDir(getApplicationContext());
                    Uri compositeUri = MergeUris(imageUri,
                            HelperFunctions.getUriFromPath(appPath + "/" +
                                    spSettings.getPhotoPathName()));

                    Log.d(TAG, "After mergeUri");

                    Intent targetedShareIntent = new Intent(android.content.Intent.ACTION_SEND);
                    Log.d(TAG, "After mergeUri");

                    targetedShareIntent.setType("image/*");
                    targetedShareIntent.putExtra(Intent.EXTRA_STREAM, compositeUri);
                    targetedShareIntent.setPackage(packageName);
                    targetedShareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    targetedShareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    targetedShareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    Log.d(TAG, "After addFlags()");

                    targetedShareIntents.add(targetedShareIntent);
                    Log.d(TAG, "after .add()");
                }
            }

            //This chooser should never display - only 1 target intent added
            Intent chooserIntent = Intent.createChooser(targetedShareIntents.remove(0),
                    "Select app to share");
            Log.d(TAG, "after chooserIntent");

            startActivity(chooserIntent);
            Log.d(TAG, "after startActivity");
            finish();
            Log.d(TAG, "after finish()");
        }
    }


    void handleSendMultipleImages(Intent intent) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            Log.d(TAG, "handleSendMultipleImages() - imageUris:" + imageUris);
        }
    }


    private Uri MergeUris(Uri bottomImageUri, Uri topImageUri) {
        Uri returnUri = bottomImageUri;
        try {
            Context context = getApplicationContext();
            String outputDir = HelperFunctions.getAppTempDir(context);

            // Create Bitmap of selected file scaled down for UI work
            String originalfilePath = HelperFunctions.getRealPathFromURI(this, bottomImageUri);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(originalfilePath, options);

            options.inSampleSize = HelperFunctions.calculateInSampleSize(options, 200, 200);
            options.inJustDecodeBounds = false;
            Bitmap bottomImage = BitmapFactory.decodeFile(originalfilePath, options);

            //Load bottom image
            Bitmap bottomImagesmall = Bitmap.createScaledBitmap(bottomImage, bottomImage.getWidth(),
                    bottomImage.getHeight(), false);
            int bottomImageWidth = bottomImage.getWidth();
            Bitmap bottomImagemutable = bottomImagesmall.copy(Bitmap.Config.ARGB_8888, true);

            //Load top image
            Bitmap topImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(),
                    topImageUri);

            int topImageWidth = topImage.getWidth();
            int topImageHeight = topImage.getHeight();

            float PctSelfieSize;
            if (bottomImage.getWidth() > bottomImage.getHeight()) {
                PctSelfieSize = PctSelfieSizeLandscrape;
            }
            else {
                PctSelfieSize = PctSelfieSizePortrait;
            }
            float ratio1 = bottomImageWidth * PctSelfieSize;

            float ratio2 = ratio1 / topImageWidth;
            int finalheight = (int) (ratio2 * topImageHeight);

            Bitmap topImagemutable = Bitmap.createScaledBitmap(topImage,
                    (int)(bottomImageWidth * PctSelfieSize),
                    finalheight, false);

            //combine bottom image and top image
            Canvas comboImage = new Canvas(bottomImagemutable);
            comboImage.drawBitmap(topImagemutable,
                    bottomImagemutable.getWidth() - topImagemutable.getWidth(),
                    bottomImagemutable.getHeight() - topImagemutable.getHeight(),
                    null);

            Time t = new Time();
            t.setToNow();
            String newFileName = "selfiemecomposite-" + t.year + t.yearDay + t.minute +
                    t.second + ".png";

            OutputStream outStream = new FileOutputStream(outputDir + "/" + newFileName);

            //compress quality
            bottomImagemutable.compress(Bitmap.CompressFormat.PNG, 50, outStream);
            outStream.flush();
            outStream.close();

            returnUri = HelperFunctions.getUriFromPath(outputDir + "/" + newFileName);
            Log.d(TAG, "MergeUri: " + returnUri);
        } catch (IOException e) {
            Log.d(TAG, "IOException catch:" + returnUri);
        } catch (Throwable throwable) {
            Log.d(TAG, "Throwable catch:" + returnUri);
            throwable.printStackTrace();
        }
        return returnUri;
    }

}
