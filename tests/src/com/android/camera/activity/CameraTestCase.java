/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.camera.activity;

import android.app.Activity;
import android.app.Instrumentation;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.android.camera.CameraHolder;
import com.android.camera.CameraManager.CameraProxy;
import com.android.camera.R;

import static com.google.testing.littlemock.LittleMock.mock;
import static com.google.testing.littlemock.LittleMock.doReturn;
import com.google.testing.littlemock.AppDataDirGuesser;

import java.io.File;

public class CameraTestCase<T extends Activity> extends ActivityInstrumentationTestCase2<T> {
    protected CameraInfo mCameraInfo[];
    protected CameraProxy mMockCamera[];
    protected CameraInfo mOneCameraInfo[];
    protected CameraProxy mOneMockCamera[];
    private static Parameters mParameters;

    public CameraTestCase(Class<T> activityClass) {
        super(activityClass);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        AppDataDirGuesser.setInstance(new AppDataDirGuesser() {
            @Override
            public File guessSuitableDirectoryForGeneratedClasses() {
                return getInstrumentation().getTargetContext().getCacheDir();
            }
        });
        AppDataDirGuesser.getsInstance().guessSuitableDirectoryForGeneratedClasses();
        mCameraInfo = new CameraInfo[2];
        mCameraInfo[0] = new CameraInfo();
        mCameraInfo[0].facing = CameraInfo.CAMERA_FACING_BACK;
        mCameraInfo[1] = new CameraInfo();
        mCameraInfo[1].facing = CameraInfo.CAMERA_FACING_FRONT;
        mMockCamera = new CameraProxy[2];
        mMockCamera[0] = mock(CameraProxy.class);
        mMockCamera[1] = mock(CameraProxy.class);
        doReturn(getParameters()).when(mMockCamera[0]).getParameters();
        doReturn(getParameters()).when(mMockCamera[1]).getParameters();

        mOneCameraInfo = new CameraInfo[1];
        mOneCameraInfo[0] = new CameraInfo();
        mOneCameraInfo[0].facing = CameraInfo.CAMERA_FACING_BACK;
        mOneMockCamera = new CameraProxy[1];
        mOneMockCamera[0] = mock(CameraProxy.class);
        doReturn(getParameters()).when(mOneMockCamera[0]).getParameters();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        CameraHolder.injectMockCamera(null,  null);
    }

    protected void internalTestFailToConnect() throws Exception {
        CameraHolder.injectMockCamera(mCameraInfo, null);

        getActivity();
        Instrumentation inst = getInstrumentation();
        inst.waitForIdleSync();
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER); // close dialog
    }

    protected void internalTestRestoreDefault() throws Exception {
        CameraHolder.injectMockCamera(mCameraInfo, mMockCamera);

        getActivity();
        getInstrumentation().waitForIdleSync();
        performClick(R.id.second_level_indicator);
        performClick(R.id.other_setting_indicator);
        performClick(R.id.restore_default);
        performClick(R.id.rotate_dialog_button1);
    }

    protected void internalTestOneCamera() throws Exception {
        CameraHolder.injectMockCamera(mOneCameraInfo, mOneMockCamera);

        getActivity();
        getInstrumentation().waitForIdleSync();
        assertViewNotExist(R.id.camera_picker);
    }

    protected void performClick(final int id) {
        Activity activity = getActivity();
        getInstrumentation().waitForIdleSync();
        assertNotNull(activity.findViewById(id));
        Instrumentation inst = getInstrumentation();
        inst.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                View v = getActivity().findViewById(id);
                float x = (v.getLeft() + v.getRight()) / 2;
                float y = (v.getTop() + v.getBottom()) / 2;
                MotionEvent down = MotionEvent.obtain(0, 0,
                        MotionEvent.ACTION_DOWN, x, y, 0, 0, 0, 0, 0, 0, 0);
                MotionEvent up = MotionEvent.obtain(0, 0,
                        MotionEvent.ACTION_UP, x, y, 0, 0, 0, 0, 0, 0, 0);
                View parent = (View) v.getParent();
                parent.dispatchTouchEvent(down);
                parent.dispatchTouchEvent(up);
            }
        });
        inst.waitForIdleSync();
    }

    protected void assertViewNotExist(int id) {
        Activity activity = getActivity();
        getInstrumentation().waitForIdleSync();
        assertNull(activity.findViewById(id));
    }

    protected static Parameters getParameters() {
        synchronized (CameraTestCase.class) {
            if (mParameters == null) {
                mParameters = android.hardware.Camera.getEmptyParameters();
                mParameters.unflatten("preview-format-values=yuv420sp,yuv420p,yuv422i-yuyv,yuv420p;" +
                        "preview-format=yuv420sp;" +
                        "preview-size-values=800x480;preview-size=800x480;" +
                        "picture-size-values=2592x1944;picture-size=2592x1944;" +
                        "jpeg-thumbnail-size-values=320x240,0x0;jpeg-thumbnail-width=320;jpeg-thumbnail-height=240;" +
                        "jpeg-thumbnail-quality=60;jpeg-quality=95;" +
                        "preview-frame-rate-values=30,15;preview-frame-rate=30;" +
                        "focus-mode-values=continuous-video,auto,macro,infinity,continuous-picture;focus-mode=auto;" +
                        "preview-fps-range-values=(15000,30000);preview-fps-range=15000,30000;" +
                        "scene-mode-values=auto,action,night;scene-mode=auto;" +
                        "flash-mode-values=off,on,auto,torch;flash-mode=off;" +
                        "whitebalance-values=auto,daylight,fluorescent,incandescent;whitebalance=auto;" +
                        "effect-values=none,mono,sepia;effect=none;" +
                        "zoom-supported=true;zoom-ratios=100,200,400;max-zoom=2;" +
                        "picture-format-values=jpeg;picture-format=jpeg;" +
                        "min-exposure-compensation=-30;max-exposure-compensation=30;" +
                        "exposure-compensation=0;exposure-compensation-step=0.1;" +
                        "horizontal-view-angle=40;vertical-view-angle=40;");
            }
        }
        return mParameters;
    }
}