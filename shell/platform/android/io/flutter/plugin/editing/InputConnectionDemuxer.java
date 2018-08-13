package io.flutter.plugin.editing;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class InputConnectionDemuxer extends BaseInputConnection {

    Map<Integer, View> mViewIds;
    Map<Integer, InputConnection> mTargets;
    final View mDefaultView;
    InputConnection mDefaultInputConnection;
    Integer mFocusedView;

    EditorInfo lastOutAttrs;

    public InputConnectionDemuxer(View defaultView) {
        super(defaultView, true);
        mTargets = new HashMap<>();
        mViewIds = new HashMap<>();
        mDefaultView = defaultView;
    }

    public void onCreateInputConnection(EditorInfo outAttrs) {
        Log.d("AMIR", "onCreateInputConnection");
        for (int id : mViewIds.keySet()) {
            View view = mViewIds.get(id);
            InputConnection con = view.onCreateInputConnection(outAttrs);
            if (con != null)
                mTargets.put(id, con);
            Log.d("AMIR", "created: " + mTargets.get(id));
            Log.d("AMIR", "con: " + con);
        }
        lastOutAttrs = outAttrs;
    }

    public void setDefaultInputConnection(InputConnection inputConnection) {
        mDefaultInputConnection = inputConnection;
    }

    public boolean checkInputConnectionProxy(View view) {
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
        Log.d("AMIR", "checking proxy for: " + view.getClass());
        if (showInput) {
            Log.d("AMIR", "setting focus on: " + view.getClass());
            mFocusedView = viewIdFor(view);
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

    private InputConnection getCurrentTarget() {
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
            if (ic == null)
                return mDefaultInputConnection;
            Log.d("AMIR", "lazily created ic");
            mTargets.put(mFocusedView, ic);
            return ic;
        }
        return mTargets.get(mFocusedView);
    }

    @Override
    public CharSequence getTextBeforeCursor(int n, int flags) {
        return getCurrentTarget().getTextBeforeCursor(n, flags);
    }

    @Override
    public CharSequence getTextAfterCursor(int n, int flags) {
        return getCurrentTarget().getTextAfterCursor(n, flags);
    }

    @Override
    public CharSequence getSelectedText(int flags) {
        return getCurrentTarget().getSelectedText(flags);
    }

    @Override
    public int getCursorCapsMode(int reqModes) {
        return getCurrentTarget().getCursorCapsMode(reqModes);
    }

    @Override
    public ExtractedText getExtractedText(ExtractedTextRequest request, int flags) {
        return getCurrentTarget().getExtractedText(request, flags);
    }

    @Override
    public boolean deleteSurroundingText(int beforeLength, int afterLength) {
        return getCurrentTarget().deleteSurroundingText(beforeLength, afterLength);
    }

    @Override
    public boolean setComposingText(CharSequence text, int newCursorPosition) {
        return getCurrentTarget().setComposingText(text, newCursorPosition);
    }

    @Override
    public boolean setComposingRegion(int start, int end) {
        return getCurrentTarget().setComposingRegion(start, end);
    }

    @Override
    public boolean finishComposingText() {
        return getCurrentTarget().finishComposingText();
    }

    @Override
    public boolean commitText(CharSequence text, int newCursorPosition) {
        return getCurrentTarget().commitText(text, newCursorPosition);
    }

    @Override
    public boolean commitCompletion(CompletionInfo text) {
        return getCurrentTarget().commitCompletion(text);
    }

    @Override
    public boolean commitCorrection(CorrectionInfo correctionInfo) {
        return getCurrentTarget().commitCorrection(correctionInfo);
    }

    @Override
    public boolean setSelection(int start, int end) {
        return getCurrentTarget().setSelection(start, end);
    }

    @Override
    public boolean performEditorAction(int editorAction) {
        return getCurrentTarget().performEditorAction(editorAction);
    }

    @Override
    public boolean performContextMenuAction(int id) {
        return getCurrentTarget().performContextMenuAction(id);
    }

    @Override
    public boolean beginBatchEdit() {
        return getCurrentTarget().beginBatchEdit();
    }

    @Override
    public boolean endBatchEdit() {
        return getCurrentTarget().endBatchEdit();
    }

    @Override
    public boolean sendKeyEvent(KeyEvent event) {
        return getCurrentTarget().sendKeyEvent(event);
    }

    @Override
    public boolean clearMetaKeyStates(int states) {
        return getCurrentTarget().clearMetaKeyStates(states);
    }

    @Override
    public boolean reportFullscreenMode(boolean enabled) {
        return getCurrentTarget().reportFullscreenMode(enabled);
    }

    @Override
    public boolean performPrivateCommand(String action, Bundle data) {
        return getCurrentTarget().performPrivateCommand(action, data);
    }
}
