package io.flutter.view;

import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.accessibility.AccessibilityRecord;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class PlatformViewAccessibilityBridge {
    private final View embeddedView;
    private final View rootAccessibilityView;
    private final Map<Integer, Integer> flutterToEmbeddedId = new HashMap<>();
    private final Map<Integer, Integer> embeddedToFlutterId = new HashMap<>();
    private final Rect platformViewGlobalRect;

    private int nextFlutterId;

    public PlatformViewAccessibilityBridge(int seedId, View embeddedView, View rootAccessibilityView, Rect platformViewGlobalRect) {
        this.rootAccessibilityView = rootAccessibilityView;
        this.nextFlutterId = seedId;
        this.embeddedView = embeddedView;
        this.platformViewGlobalRect = platformViewGlobalRect;
    }

    public AccessibilityNodeInfo getNode(int flutterId) {
        AccessibilityNodeInfo sourceNode;
        if (flutterId < 5000) {
            sourceNode = embeddedView.createAccessibilityNodeInfo();
            int sourceNodeEmbeddedId = (int) getAccessibilityId(sourceNode);
            Log.d("AMIR", "source node id: " + sourceNodeEmbeddedId);
            flutterToEmbeddedId.put(flutterId, sourceNodeEmbeddedId);
            embeddedToFlutterId.put(sourceNodeEmbeddedId, flutterId);
            AccessibilityNodeProvider provider = embeddedView.getAccessibilityNodeProvider();
            if (sourceNode.getChildCount() > 0) {
                int myId = (int) getParentId(provider.createAccessibilityNodeInfo((int) getChildId(sourceNode, 0)));
                Log.d("AMIR", "myId: " + myId);
            } else {
                Log.d("AMIR", "no children");
            }
            //traverseTree(sourceNode);
        } else {
            int embeddedId = flutterToEmbeddedId.get(flutterId);
            AccessibilityNodeProvider provider = embeddedView.getAccessibilityNodeProvider();
            sourceNode = provider.createAccessibilityNodeInfo(embeddedId);
        }

        AccessibilityNodeInfo result = AccessibilityNodeInfo.obtain(rootAccessibilityView, flutterId);
        result.setPackageName(rootAccessibilityView.getContext().getPackageName());
        result.setClassName("android.view.View");
        result.setSource(rootAccessibilityView, flutterId);

        result.setClassName(sourceNode.getClassName());

    // result.setClassName(root.getClass().getName() + "#" + virtualNodeId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            result.setContextClickable(sourceNode.isContextClickable());
        }
        result.setAccessibilityFocused(sourceNode.isAccessibilityFocused());

    Rect boundsInParent = new Rect();
    sourceNode.getBoundsInParent(boundsInParent);
    result.setBoundsInParent(boundsInParent);

    Rect boundsInScreen = new Rect();
    sourceNode.getBoundsInScreen(boundsInScreen);
    if (flutterId >= 5000) {
        boundsInParent.offset(platformViewGlobalRect.left, platformViewGlobalRect.top);
    }
    result.setBoundsInScreen(boundsInScreen);

    result.setAvailableExtraData(sourceNode.getAvailableExtraData());
    result.setCanOpenPopup(sourceNode.canOpenPopup());
    result.setCheckable(sourceNode.isCheckable());
    result.setChecked(sourceNode.isChecked());
    result.setCollectionInfo(sourceNode.getCollectionInfo());
    result.setCollectionItemInfo(sourceNode.getCollectionItemInfo());
    result.setContentDescription(sourceNode.getContentDescription());
    result.setEditable(sourceNode.isEditable());
    result.setEnabled(sourceNode.isEnabled());
    result.setClickable(sourceNode.isClickable());
    result.setContentInvalid(sourceNode.isContentInvalid());
    result.setDismissable(sourceNode.isDismissable());
    result.setDrawingOrder(sourceNode.getDrawingOrder());
    result.setError(sourceNode.getError());
    result.setFocusable(sourceNode.isFocusable());
    result.setFocused(sourceNode.isFocused());
    //result.setHeading(sourceNode.isHeading()); // crashes
    result.setHintText(sourceNode.getHintText());
    result.setImportantForAccessibility(sourceNode.isImportantForAccessibility());
    result.setInputType(sourceNode.getInputType());
    result.setLiveRegion(sourceNode.getLiveRegion());
    result.setLongClickable(sourceNode.isLongClickable());
    result.setMaxTextLength(sourceNode.getMaxTextLength());
    result.setMovementGranularities(sourceNode.getMovementGranularities());
    result.setMultiLine(sourceNode.isMultiLine());
    //result.setPaneTitle(sourceNode.getPaneTitle()); // crashes
    result.setPassword(sourceNode.isPassword());
    result.setRangeInfo(sourceNode.getRangeInfo());
    //result.setScreenReaderFocusable(sourceNode.isScreenReaderFocusable()); // crashes
    result.setScrollable(sourceNode.isScrollable());
    result.setSelected(sourceNode.isSelected());
    result.setShowingHintText(sourceNode.isShowingHintText());
    result.setText(sourceNode.getText());
    // TODO: result.setTextSelection();
    //result.setTooltipText(sourceNode.getTooltipText()); // crashes
    result.setVisibleToUser(sourceNode.isVisibleToUser());

    int parentEmbeddedId = (int) getParentId(sourceNode);
    if (parentEmbeddedId == -1) {
    } else {
        if (!embeddedToFlutterId.containsKey(parentEmbeddedId)) {
            Log.d("AMIR", "Can't map embedded id: " + parentEmbeddedId);
            Log.d("AMIR", sourceNode.toString());
        } else {
            int parentFlutterId = embeddedToFlutterId.get(parentEmbeddedId);
            result.setParent(rootAccessibilityView, parentFlutterId);
        }
    }
    // int parentNodeVirtualId = apiWorkarounds.getParentNodeVirtualId(sourceNode);
    // if (parentNodeVirtualId != apiWorkarounds.getUndefinedNodeVirtualId()) {
    //   result.setParent(root, parentNodeVirtualId);
    // } else {
    //   result.setParent(root);
    // }

    result.getExtras().putAll(sourceNode.getExtras());

    for (AccessibilityNodeInfo.AccessibilityAction action : sourceNode.getActionList()) {
      result.addAction(action);
    }
    if (flutterId < 5000) {
        result.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_ACCESSIBILITY_FOCUS);
    }

//    applyIfSet(apiWorkarounds.getTravelsalBeforeVirtualId(input), result::setTraversalBefore);
//    applyIfSet(apiWorkarounds.getTravelsalAfterVirtualId(input), result::setTraversalAfter);
//    applyIfSet(apiWorkarounds.getLabeledByVirtualId(input), result::setLabeledBy);
//    applyIfSet(apiWorkarounds.getLabelForVirtualId(input), result::setLabelFor);

        addChildren(sourceNode, result);
        ReflectionAccessors r;
        try {
            r = new ReflectionAccessors();
            Long embeddedBefore = (Long) r.mTravelsalBefore.get(sourceNode);
            if (embeddedBefore != null) {
                Log.d("AMIR", "embedder before: " + embeddedBefore);
                Integer flutterBefore = embeddedToFlutterId.get(embeddedBefore);
                if(flutterBefore == null) {
                    Log.d("AMIR", "can't map");
                } else {
                    Log.d("AMIR", "mapped to: " + flutterBefore);
                    result.setTraversalBefore(rootAccessibilityView, flutterBefore);
                }
            }
            Long embeddedAfter = (Long) r.mTravelsalAfter.get(sourceNode);
            if (embeddedAfter != null) {
                Log.d("AMIR", "embedder after: " + embeddedAfter);
                Integer flutterAfter = embeddedToFlutterId.get(embeddedAfter);
                if(flutterAfter == null) {
                    Log.d("AMIR", "can't map");
                } else {
                    Log.d("AMIR", "mapped to: " + flutterAfter);
                    result.setTraversalAfter(rootAccessibilityView, flutterAfter);
                }
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new IllegalStateException();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            throw new IllegalStateException();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return result;
    }

    private void addChildren(AccessibilityNodeInfo sourceNode, AccessibilityNodeInfo resultNode) {
        for(int i = 0; i < sourceNode.getChildCount(); i++) {
            int embeddedId = (int) getChildId(sourceNode, i);
            int flutterId = nextFlutterId++;
            Log.d("AMIR", "mapping embeddedId: " + embeddedId + " -> " + flutterId);
            flutterToEmbeddedId.put(flutterId, embeddedId);
            embeddedToFlutterId.put(embeddedId, flutterId);
            resultNode.addChild(rootAccessibilityView, flutterId);
        }
    }

    private void traverseTree(AccessibilityNodeInfo node) {
        for(int i = 0; i < node.getChildCount(); i++) {
            int embeddedId = (int) getChildId(node, i);
            int flutterId = nextFlutterId++;
            flutterToEmbeddedId.put(flutterId, embeddedId);
            embeddedToFlutterId.put(embeddedId, flutterId);
            AccessibilityNodeProvider provider = embeddedView.getAccessibilityNodeProvider();
            AccessibilityNodeInfo child = provider.createAccessibilityNodeInfo(embeddedId);
            traverseTree(child);
        }
    }

    private static long getChildId(AccessibilityNodeInfo node, int i) {
        try {
            Class clazz = Class.forName("android.view.accessibility.AccessibilityNodeInfo");
            Method method = clazz.getMethod("getChildId", int.class);
            long id = (long) method.invoke(node, i);
            return id;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException();
    }

    private static long getParentId(AccessibilityNodeInfo node) {
        try {
            Class clazz = Class.forName("android.view.accessibility.AccessibilityNodeInfo");
            Method method = clazz.getMethod("getParentNodeId");
            long id = (long) method.invoke(node);
            return id;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException();
    }

    private static long getAccessibilityId(AccessibilityNodeInfo node) {
        try {
            Class clazz = Class.forName("android.view.accessibility.AccessibilityNodeInfo");
            Method method = clazz.getMethod("getSourceNodeId");
            long id = (long) method.invoke(node);
            return id;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException();
    }

    public boolean performAction(int virtualViewId, int accessibilityAction, Bundle arguments) {
        AccessibilityNodeProvider provider = embeddedView.getAccessibilityNodeProvider();
        int embeddedId = flutterToEmbeddedId.get(virtualViewId);
        Log.d("AMIR", "bridging action to " + embeddedId + "(" + accessibilityAction + ")");
        return provider.performAction(embeddedId, accessibilityAction, arguments);
    }

    public boolean delegateSendAccessibilityEvent(View child, AccessibilityEvent input) {
        AccessibilityEvent result = AccessibilityEvent.obtain(input);
        int embeddedId = (int) getRecordSourceId(input);
        int flutterId = embeddedToFlutterId.get(embeddedId);
    result.setSource(rootAccessibilityView, flutterId);
    result.setClassName(input.getClassName());
    result.setPackageName(input.getPackageName());
    for (int i = 0; i < result.getRecordCount(); i++) {
      AccessibilityRecord record = result.getRecord(i);
      int recordEmbeddedId = (int) getRecordSourceId(record);
      int recordFlutterId = embeddedToFlutterId.get(recordEmbeddedId);
      record.setSource(rootAccessibilityView, recordFlutterId);
    }
    // input.recycle(); // TODO: check
        Log.d("AMIR", "delegating a11y event: " + result);
        return rootAccessibilityView.getParent().requestSendAccessibilityEvent(child, result);
    }

    private static long getRecordSourceId(AccessibilityRecord record) {
        try {
            Class clazz = Class.forName("android.view.accessibility.AccessibilityRecord");
            Method method = clazz.getMethod("getSourceNodeId");
            long id = (long) method.invoke(record);
            return id;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException();
    }

      private static class ReflectionAccessors {
    private final Method getSourceNodeIdInRecord;
    private final Method getSourceNodeId;
    private final Field undefinedNodeId;
    private final Method getParentNodeId;
    private final Method getChildId;
    private final Field mTravelsalBefore;
    private final Field mTravelsalAfter;
    private final Field mLabelForId;
    private final Field mLabeledById;

    private ReflectionAccessors() throws NoSuchMethodException, NoSuchFieldException {
      getSourceNodeIdInRecord = AccessibilityRecord.class.getMethod("getSourceNodeId");
      getSourceNodeId = AccessibilityNodeInfo.class.getMethod("getSourceNodeId");
      undefinedNodeId = AccessibilityNodeInfo.class.getField("UNDEFINED_NODE_ID");
      getParentNodeId = AccessibilityNodeInfo.class.getMethod("getParentNodeId");
      getChildId = AccessibilityNodeInfo.class.getMethod("getChildId", int.class);
      mTravelsalBefore = AccessibilityNodeInfo.class.getDeclaredField("mTraversalBefore");
      mTravelsalBefore.setAccessible(true);
      mTravelsalAfter = AccessibilityNodeInfo.class.getDeclaredField("mTraversalAfter");
      mTravelsalAfter.setAccessible(true);
      mLabelForId = AccessibilityNodeInfo.class.getDeclaredField("mLabelForId");
      mLabelForId.setAccessible(true);
      mLabeledById = AccessibilityNodeInfo.class.getDeclaredField("mLabeledById");
      mLabeledById.setAccessible(true);
    }
  }
}
