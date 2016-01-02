package com.retsworks.pinguino;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    String TAG = "PINGUINO-Test";

    public ApplicationTest() {
        super(Application.class);
        Log.d(TAG, "Test1");
    }
}