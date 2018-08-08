// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugin.platform;

import android.annotation.TargetApi;
import android.app.Presentation;
import android.content.*;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.*;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import java.io.*;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
class SingleViewPresentation extends Presentation {
    private final PlatformViewFactory mViewFactory;

    private PlatformView mView;
    private int mViewId;

    // As the root view of a display cannot be detached, we use this mContainer
    // as the root, and attach mView to it. This allows us to detach mView.
    private FrameLayout mContainer;

    private final InputMethodManager mImm;
    /**
     * Creates a presentation that will use the view factory to create a new
     * platform view in the presentation's onCreate, and attach it.
     */
    public SingleViewPresentation(Context outerContext, Display display, PlatformViewFactory viewFactory, int viewId) {
        super(outerContext, display);
        mViewFactory = viewFactory;
        mViewId = viewId;
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        );

        mImm = (InputMethodManager) outerContext.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    /**
     * Creates a presentation that will attach an already existing view as
     * its root view.
     *
     * <p>The display's density must match the density of the context used
     * when the view was created.
     */
    public SingleViewPresentation(Context outerContext, Display display, PlatformView view) {
        super(outerContext, display);
        mViewFactory = null;
        mView = view;
        // getWindow().setFlags(
        //         WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        //         WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        // );
        mImm = (InputMethodManager) outerContext.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mView == null) {
            mView = mViewFactory.create(new MyContext(getContext(), mImm), mViewId);
        }
        mContainer = new FrameLayout(getContext());
        mContainer.addView(mView.getView());
        setContentView(mContainer);
    }

    public PlatformView detachView() {
        mContainer.removeView(mView.getView());
        return mView;
    }

    public View getView() {
        if (mView == null)
            return null;
        return mView.getView();
    }
}


class MyContext extends Context {

    MyContext(Context mDelegate, InputMethodManager imm) {
        this.mDelegate = mDelegate;
        this.mImm = imm;
    }

    private final Context mDelegate;

    private final InputMethodManager mImm;

    @Override
    public AssetManager getAssets() {
        return mDelegate.getAssets();
    }

    @Override
    public Resources getResources() {
        return mDelegate.getResources();
    }

    @Override
    public PackageManager getPackageManager() {
        return mDelegate.getPackageManager();
    }

    @Override
    public ContentResolver getContentResolver() {
        return mDelegate.getContentResolver();
    }

    @Override
    public Looper getMainLooper() {
        return mDelegate.getMainLooper();
    }

    @Override
    public Context getApplicationContext() {
        return mDelegate.getApplicationContext();
    }

    @Override
    public void setTheme(int resid) {
        mDelegate.setTheme(resid);
    }

    @Override
    public Resources.Theme getTheme() {
        return mDelegate.getTheme();
    }

    @Override
    public ClassLoader getClassLoader() {
        return mDelegate.getClassLoader();
    }

    @Override
    public String getPackageName() {
        return mDelegate.getPackageName();
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        return mDelegate.getApplicationInfo();
    }

    @Override
    public String getPackageResourcePath() {
        return mDelegate.getPackageResourcePath();
    }

    @Override
    public String getPackageCodePath() {
        return mDelegate.getPackageCodePath();
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return mDelegate.getSharedPreferences(name, mode);
    }

    @Override
    public FileInputStream openFileInput(String name) throws FileNotFoundException {
        return mDelegate.openFileInput(name);
    }

    @Override
    public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
        return mDelegate.openFileOutput(name, mode);
    }

    @Override
    public boolean deleteFile(String name) {
        return mDelegate.deleteFile(name);
    }

    @Override
    public File getFileStreamPath(String name) {
        return mDelegate.getFileStreamPath(name);
    }

    @Override
    public File getFilesDir() {
        return mDelegate.getFilesDir();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public File getNoBackupFilesDir() {
        return mDelegate.getNoBackupFilesDir();
    }

    @Override
    public File getExternalFilesDir(String type) {
        return mDelegate.getExternalFilesDir(type);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public File[] getExternalFilesDirs(String type) {
        return mDelegate.getExternalFilesDirs(type);
    }

    @Override
    public File getObbDir() {
        return mDelegate.getObbDir();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public File[] getObbDirs() {
        return mDelegate.getObbDirs();
    }

    @Override
    public File getCacheDir() {
        return mDelegate.getCacheDir();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public File getCodeCacheDir() {
        return mDelegate.getCodeCacheDir();
    }

    @Override
    public File getExternalCacheDir() {
        return mDelegate.getExternalCacheDir();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public File[] getExternalCacheDirs() {
        return mDelegate.getExternalCacheDirs();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public File[] getExternalMediaDirs() {
        return mDelegate.getExternalMediaDirs();
    }

    @Override
    public String[] fileList() {
        return mDelegate.fileList();
    }

    @Override
    public File getDir(String name, int mode) {
        return mDelegate.getDir(name, mode);
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
        return mDelegate.openOrCreateDatabase(name, mode, factory);
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
        return mDelegate.openOrCreateDatabase(name, mode, factory, errorHandler);
    }

    @Override
    public boolean deleteDatabase(String name) {
        return mDelegate.deleteDatabase(name);
    }

    @Override
    public File getDatabasePath(String name) {
        return mDelegate.getDatabasePath(name);
    }

    @Override
    public String[] databaseList() {
        return mDelegate.databaseList();
    }

    @Override
    public Drawable getWallpaper() {
        return mDelegate.getWallpaper();
    }

    @Override
    public Drawable peekWallpaper() {
        return mDelegate.peekWallpaper();
    }

    @Override
    public int getWallpaperDesiredMinimumWidth() {
        return mDelegate.getWallpaperDesiredMinimumWidth();
    }

    @Override
    public int getWallpaperDesiredMinimumHeight() {
        return mDelegate.getWallpaperDesiredMinimumHeight();
    }

    @Override
    public void setWallpaper(Bitmap bitmap) throws IOException {
        mDelegate.setWallpaper(bitmap);
    }

    @Override
    public void setWallpaper(InputStream data) throws IOException {
        mDelegate.setWallpaper(data);
    }

    @Override
    public void clearWallpaper() throws IOException {
        mDelegate.clearWallpaper();
    }

    @Override
    public void startActivity(Intent intent) {
        mDelegate.startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void startActivity(Intent intent, Bundle options) {
        mDelegate.startActivity(intent, options);
    }

    @Override
    public void startActivities(Intent[] intents) {
        mDelegate.startActivities(intents);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void startActivities(Intent[] intents, Bundle options) {
        mDelegate.startActivities(intents, options);
    }

    @Override
    public void startIntentSender(IntentSender intent, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws IntentSender.SendIntentException {
        mDelegate.startIntentSender(intent, fillInIntent, flagsMask, flagsValues, extraFlags);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void startIntentSender(IntentSender intent, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, Bundle options) throws IntentSender.SendIntentException {
        mDelegate.startIntentSender(intent, fillInIntent, flagsMask, flagsValues, extraFlags, options);
    }

    @Override
    public void sendBroadcast(Intent intent) {
        mDelegate.sendBroadcast(intent);
    }

    @Override
    public void sendBroadcast(Intent intent, String receiverPermission) {
        mDelegate.sendBroadcast(intent, receiverPermission);
    }

    @Override
    public void sendOrderedBroadcast(Intent intent, String receiverPermission) {
        mDelegate.sendOrderedBroadcast(intent, receiverPermission);
    }

    @Override
    public void sendOrderedBroadcast(Intent intent, String receiverPermission, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        mDelegate.sendOrderedBroadcast(intent, receiverPermission, resultReceiver, scheduler, initialCode, initialData, initialExtras);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void sendBroadcastAsUser(Intent intent, UserHandle user) {
        mDelegate.sendBroadcastAsUser(intent, user);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void sendBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission) {
        mDelegate.sendBroadcastAsUser(intent, user, receiverPermission);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        mDelegate.sendOrderedBroadcastAsUser(intent, user, receiverPermission, resultReceiver, scheduler, initialCode, initialData, initialExtras);
    }

    @Override
    public void sendStickyBroadcast(Intent intent) {
        mDelegate.sendStickyBroadcast(intent);
    }

    @Override
    public void sendStickyOrderedBroadcast(Intent intent, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        mDelegate.sendStickyOrderedBroadcast(intent, resultReceiver, scheduler, initialCode, initialData, initialExtras);
    }

    @Override
    public void removeStickyBroadcast(Intent intent) {
        mDelegate.removeStickyBroadcast(intent);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void sendStickyBroadcastAsUser(Intent intent, UserHandle user) {
        mDelegate.sendStickyBroadcastAsUser(intent, user);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void sendStickyOrderedBroadcastAsUser(Intent intent, UserHandle user, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        mDelegate.sendStickyOrderedBroadcastAsUser(intent, user, resultReceiver, scheduler, initialCode, initialData, initialExtras);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void removeStickyBroadcastAsUser(Intent intent, UserHandle user) {
        mDelegate.removeStickyBroadcastAsUser(intent, user);
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        return mDelegate.registerReceiver(receiver, filter);
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler) {
        return mDelegate.registerReceiver(receiver, filter, broadcastPermission, scheduler);
    }

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {
        mDelegate.unregisterReceiver(receiver);
    }

    @Override
    public ComponentName startService(Intent service) {
        return mDelegate.startService(service);
    }

    @Override
    public boolean stopService(Intent service) {
        return mDelegate.stopService(service);
    }

    @Override
    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        return mDelegate.bindService(service, conn, flags);
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        mDelegate.unbindService(conn);
    }

    @Override
    public boolean startInstrumentation(ComponentName className, String profileFile, Bundle arguments) {
        return mDelegate.startInstrumentation(className, profileFile, arguments);
    }

    @Override
    public Object getSystemService(String name) {
        Log.d("AMIR", "getSystemService(\"" + name + "\")");
        if (name.equals(Context.INPUT_METHOD_SERVICE)) {
            Log.d("AMIR", "intercepted!");
            Exception e = new Exception();
            e.printStackTrace();
            return mImm;
        }
        return mDelegate.getSystemService(name);
    }

    @Override
    public int checkPermission(String permission, int pid, int uid) {
        return mDelegate.checkPermission(permission, pid, uid);
    }

    @Override
    public int checkCallingPermission(String permission) {
        return mDelegate.checkCallingPermission(permission);
    }

    @Override
    public int checkCallingOrSelfPermission(String permission) {
        return mDelegate.checkCallingOrSelfPermission(permission);
    }

    @Override
    public void enforcePermission(String permission, int pid, int uid, String message) {
        mDelegate.enforcePermission(permission, pid, uid, message);
    }

    @Override
    public void enforceCallingPermission(String permission, String message) {
        mDelegate.enforceCallingPermission(permission, message);
    }

    @Override
    public void enforceCallingOrSelfPermission(String permission, String message) {
        mDelegate.enforceCallingOrSelfPermission(permission, message);
    }

    @Override
    public void grantUriPermission(String toPackage, Uri uri, int modeFlags) {
        mDelegate.grantUriPermission(toPackage, uri, modeFlags);
    }

    @Override
    public void revokeUriPermission(Uri uri, int modeFlags) {
        mDelegate.revokeUriPermission(uri, modeFlags);
    }

    @Override
    public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags) {
        return mDelegate.checkUriPermission(uri, pid, uid, modeFlags);
    }

    @Override
    public int checkCallingUriPermission(Uri uri, int modeFlags) {
        return mDelegate.checkCallingUriPermission(uri, modeFlags);
    }

    @Override
    public int checkCallingOrSelfUriPermission(Uri uri, int modeFlags) {
        return mDelegate.checkCallingOrSelfUriPermission(uri, modeFlags);
    }

    @Override
    public int checkUriPermission(Uri uri, String readPermission, String writePermission, int pid, int uid, int modeFlags) {
        return mDelegate.checkUriPermission(uri, readPermission, writePermission, pid, uid, modeFlags);
    }

    @Override
    public void enforceUriPermission(Uri uri, int pid, int uid, int modeFlags, String message) {
        mDelegate.enforceUriPermission(uri, pid, uid, modeFlags, message);
    }

    @Override
    public void enforceCallingUriPermission(Uri uri, int modeFlags, String message) {
        mDelegate.enforceCallingUriPermission(uri, modeFlags, message);
    }

    @Override
    public void enforceCallingOrSelfUriPermission(Uri uri, int modeFlags, String message) {
        mDelegate.enforceCallingOrSelfUriPermission(uri, modeFlags, message);
    }

    @Override
    public void enforceUriPermission(Uri uri, String readPermission, String writePermission, int pid, int uid, int modeFlags, String message) {
        mDelegate.enforceUriPermission(uri, readPermission, writePermission, pid, uid, modeFlags, message);
    }

    @Override
    public Context createPackageContext(String packageName, int flags) throws PackageManager.NameNotFoundException {
        return mDelegate.createPackageContext(packageName, flags);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public Context createConfigurationContext(Configuration overrideConfiguration) {
        return mDelegate.createConfigurationContext(overrideConfiguration);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public Context createDisplayContext(Display display) {
        return mDelegate.createDisplayContext(display);
    }
}