package io.flutter.plugin.editing;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class InputConnectionDemuxer {

    Map<Integer, View> mViewIds;
    Map<Integer, InputConnection> mTargets;
    final View mDefaultView;
    InputConnection mDefaultInputConnection;
    Integer mFocusedView;
    final DynamicInvocationHandler mInputConnectionHandler;
    final InputConnection mInputConnectionProxy;

    EditorInfo lastOutAttrs;
    CreateInputConnection defaultCreator;

    InputMethodManager mImm;

    public InputConnectionDemuxer(View defaultView) {
        mTargets = new HashMap<>();
        mViewIds = new HashMap<>();
        mDefaultView = defaultView;
        mInputConnectionHandler = new DynamicInvocationHandler();
        mInputConnectionProxy = (InputConnection) Proxy.newProxyInstance(
                InputConnection.class.getClassLoader(),
                new Class[] { InputConnection.class },
                mInputConnectionHandler
        );
        mImm = (InputMethodManager) defaultView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    }
    // TODO next:
    // pass an input connection creator for flutter view.
    // onCreateInputConnection for this class invoke create input connectio for the focused view or for default and return it.

    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        if (mFocusedView == null) {
            Log.d("AMIR", "creating default input connection");
            return defaultCreator.create();
        }
        InputConnection ic =  mViewIds.get(mFocusedView).onCreateInputConnection(outAttrs);
        Log.d("AMIR", "non default connection is: " + ic);
        return ic;
        // Log.d("AMIR", "onCreateInputConnection");
        // for (int id : mViewIds.keySet()) {
        //     View view = mViewIds.get(id);
        //     InputConnection con = view.onCreateInputConnection(outAttrs);
        //     if (con != null)
        //         mTargets.put(id, con);
        //     Log.d("AMIR", "created: " + mTargets.get(id));
        //     Log.d("AMIR", "con: " + con);
        // }
        // lastOutAttrs = outAttrs;
    }

    public InputConnection getInputConnection() {
        return mInputConnectionProxy;
    }

    public void setDefaultInputConnection(InputConnection inputConnection) {
        Log.d("AMIR", "setting default connection to: " + inputConnection);
        mDefaultInputConnection = inputConnection;
    }

    public void setDefaultInputConnectionCreator(CreateInputConnection create) {
        defaultCreator = create;
    }

    boolean restarting = false;
    public boolean checkInputConnectionProxy(final View view) {
        Integer id = viewIdFor(view);
        // if (id == null || !mTargets.containsKey(id)) {
        //     Log.d("AMIR", "not proxying for: " + view.getClass());
        //     return false;
        // }
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        boolean showInput = false;
        for (StackTraceElement s : stackTrace) {
            if (s.getClassName().equals("android.view.inputmethod.InputMethodManager") && s.getMethodName().equals("showSoftInput")) {
                showInput = true;
                break;
            }
        }
        if (showInput) {
            Log.d("AMIR", "setting focus on: " + view.getClass());
            mFocusedView = viewIdFor(view);
            if (!restarting) {
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("AMIR", "restarting imm");
                        restarting = true;
                        mImm.restartInput(view);
                    }
                });
            } else {
                restarting = false;
            }
        }
        return true;
    }

    Integer viewIdFor(View v) {
        for (int id : mViewIds.keySet()) {
            if (mViewIds.get(id) == v)
                return id;
        }
        return null;
    }

    public void addTargetView(int id, View view) {
        Log.d("AMIR", "add view");
        //InputConnection viewConnection = null;
        // if (lastOutAttrs != null) {
        //     viewConnection = view.onCreateInputConnection(lastOutAttrs);
        // }
        mViewIds.put(id, view);
    }

    public InputConnection getCurrentTarget() {
        if (mFocusedView == null) {
            Log.d("AMIR", "default connection");
            return mDefaultInputConnection;
        }
        Log.d("AMIR", "non default connection");
        InputConnection connection = mTargets.get(mFocusedView);
        if (connection == null) {
            Log.d("AMIR", "connection for target is null returning default");
            View view = mViewIds.get(mFocusedView);
            Log.d("AMIR", "trying to lazy create ic");
            InputConnection ic = view.onCreateInputConnection(lastOutAttrs);
            if (ic == null) {
                Log.d("AMIR", "couldn't create ic, routing to default target: " + mDefaultInputConnection);
                return mDefaultInputConnection;
            }
            mTargets.put(mFocusedView, ic);
            Log.d("AMIR", "lazily created ic");
            return ic;
        }
        return mTargets.get(mFocusedView);
    }

    class DynamicInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (getCurrentTarget() == null) {
                Log.d("AMIR", "target is null!!!");
                return null;
            }
            return method.invoke(getCurrentTarget(), args);
        }
    }

    public interface CreateInputConnection {
        InputConnection create();
    }
}
