//  pinguino
//  RETSworks.com, Copyright 2015

package com.retsworks.pinguino;

import android.util.Log;

public class PhotoEntry {
    private Boolean defaultPhoto;
    private String photoPathName;
    private String entryID;
    private String TAG = "PINGUINO-PhotoEntry";

    public void setDefaultPhoto(Boolean defaultPhoto) {
        Log.d(TAG,"setDefaultPhoto: " + defaultPhoto);
        this.defaultPhoto = defaultPhoto;
    }

    public void setPhotoPathName(String photoPathName) {
        Log.d(TAG,"setPhotoPathName: " + photoPathName);
        this.photoPathName = photoPathName;
    }

    public String getPhotoPathName() {
        Log.d(TAG, "getPhotoPathName: " + this.photoPathName);
        return this.photoPathName;
    }

    public Boolean getDefaultPhoto() {
        Log.d(TAG, "getDefaultPhoto: " + this.defaultPhoto);
        return this.defaultPhoto;
    }

    public String getEntryID() {
        Log.d(TAG,"getEntryID: " + this.entryID);
        return this.entryID;
    }

    public void setEntryID(String entryID) {
        Log.d(TAG,"setEntryID: " + entryID);
        this.entryID = entryID;
    }

}