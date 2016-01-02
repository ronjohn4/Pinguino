package com.retsworks.pinguino;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class HelperFunctions {
    //http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    public static String getRealPathFromURI(Context context, Uri contentURI) {
        String TAG = "PINGUINO-getRealPathFromURI";
        String result;
        Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            Log.d(TAG, "cursor1:" + cursor);
            cursor.moveToFirst();
            Log.d(TAG, "cursor2:" + cursor);
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            Log.d(TAG,"idx:" + idx);
            result = cursor.getString(idx);
            Log.d(TAG, "result:" + result);
            cursor.close();
        }
        return result;
    }


    public static String filenamefromPath(String path) {
        Uri u = Uri.parse(path);
        File f = new File("" + u);
        return f.getName();
    }


    public static Uri getUriFromPath(String FilePath) {
        File f = new File(FilePath);
        f.setReadable(true, false);
        Uri returnUri = Uri.fromFile(f);
        return returnUri;
    }


    public static String getAppDir(Context context)
    {
        return context.getFilesDir().toString();
    }


    public static String getAppTempDir(Context context) {
        return context.getCacheDir().toString();
    }


    public static String loadTempNewPhoto(Context context, Uri selectedImageUri) throws IOException {
        String TAG = "loadTempNewPhoto";
        Log.d(TAG, "loadTempNewPhoto()");
        String appDir = getAppTempDir(context);

        String workingfileName = filenamefromPath(selectedImageUri.toString()) + ".png";

        Log.d(TAG,"workingfileName:" + workingfileName);

        // Create Bitmap of selected file scaled down for UI work
        String originalfilePath = getRealPathFromURI(context, selectedImageUri);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(originalfilePath, options);

        int samplesize = calculateInSampleSize(options, 200, 200);
        options.inSampleSize = samplesize;
        options.inJustDecodeBounds = false;
        Bitmap originalfileBitmap =  BitmapFactory.decodeFile(originalfilePath, options);

        // Save working copy
        File outFile = new File(appDir + "/" + workingfileName);
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(outFile);
            originalfileBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
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

        return workingfileName;
    }


    public static String loadDefaultPhoto(Context context) {
        String TAG = "loadDefaultPhoto";
        Uri defaultUri = Uri.parse("android.resource://" + context.getPackageName() +
                "/" + R.drawable.pinguinoleft);
        String appDir = getAppDir(context);
        String defaultfile = "default.png";

        try {
            InputStream in = context.getContentResolver().openInputStream(defaultUri);

            File outFile = new File(appDir + "/" + defaultfile);
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

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return defaultfile;
    }

}
