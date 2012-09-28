/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.systemui.statusbar.policy;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.BatteryManager;
import android.os.Handler;
import android.provider.Settings;
import android.util.Slog;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.systemui.R;

public class BatteryController extends BroadcastReceiver {
    private static final String TAG = "StatusBar.BatteryController";

    private Context mContext;
    private ArrayList<ImageView> mIconViews = new ArrayList<ImageView>();
    private ArrayList<TextView> mLabelViews = new ArrayList<TextView>();

    private static final int BATTERY_STYLE_NORMAL  = 0;
    private static final int BATTERY_STYLE_TEXT    = 1;
    private static final int BATTERY_STYLE_CIRCLE  = 2;
    private static final int BATTERY_STYLE_BAR     = 3;
    private static final int BATTERY_STYLE_FATTY   = 4;
    private static final int BATTERY_STYLE_DIGITAL = 5;
    private static final int BATTERY_STYLE_GONE    = 6;

    private static final int BATTERY_ICON_STYLE_NORMAL         = R.drawable.stat_sys_battery;
    private static final int BATTERY_ICON_STYLE_CHARGE         = R.drawable.stat_sys_battery_charge;
    private static final int BATTERY_ICON_STYLE_NORMAL_MIN     = R.drawable.stat_sys_battery_min;
    private static final int BATTERY_ICON_STYLE_CHARGE_MIN     = R.drawable.stat_sys_battery_charge_min;
    private static final int BATTERY_ICON_STYLE_NORMAL_BAR     = R.drawable.stat_sys_battery_bar;
    private static final int BATTERY_ICON_STYLE_CHARGE_BAR     = R.drawable.stat_sys_battery_charge_bar;
    private static final int BATTERY_ICON_STYLE_NORMAL_CIRCLE  = R.drawable.stat_sys_battery_circle;
    private static final int BATTERY_ICON_STYLE_CHARGE_CIRCLE  = R.drawable.stat_sys_battery_charge_circle;
    private static final int BATTERY_ICON_STYLE_NORMAL_FATTY   = R.drawable.stat_sys_battery_fatty;
    private static final int BATTERY_ICON_STYLE_CHARGE_FATTY   = R.drawable.stat_sys_battery_charge_fatty;
    private static final int BATTERY_ICON_STYLE_NORMAL_DIGITAL = R.drawable.stat_sys_battery_digital;
    private static final int BATTERY_ICON_STYLE_CHARGE_DIGITAL = R.drawable.stat_sys_battery_charge_digital;

    private static final int BATTERY_TEXT_STYLE_NORMAL  = R.string.status_bar_settings_battery_meter_format;
    private static final int BATTERY_TEXT_STYLE_MIN     = R.string.status_bar_settings_battery_meter_min_format;

    private boolean mBatteryPlugged = false;
    private int mBatteryStyle;
    private int mBatteryIcon = BATTERY_ICON_STYLE_NORMAL;

    Handler mHandler;

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.STATUS_BAR_BATTERY), false, this);
        }

        @Override public void onChange(boolean selfChange) {
            updateSettings();
        }
    }

    public BatteryController(Context context) {
        mContext = context;

        mHandler = new Handler();

        SettingsObserver settingsObserver = new SettingsObserver(mHandler);
        settingsObserver.observe();
        updateSettings();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        context.registerReceiver(this, filter);
    }

    public void addIconView(ImageView v) {
        mIconViews.add(v);
    }

    public void addLabelView(TextView v) {
        mLabelViews.add(v);
    }

    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
            final int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            mBatteryPlugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0;
            int N = mIconViews.size();
            for (int i=0; i<N; i++) {
                ImageView v = mIconViews.get(i);
                v.setImageLevel(level);
                v.setContentDescription(mContext.getString(R.string.accessibility_battery_level,
                        level));
            }
            N = mLabelViews.size();
            for (int i=0; i<N; i++) {
                TextView v = mLabelViews.get(i);
                v.setText(mContext.getString(BATTERY_TEXT_STYLE_MIN,
                        level));
            }
            updateBattery();
        }
    }

    private void updateBattery() {
        int mIcon = View.GONE;
        int mText = View.GONE;
        int mIconStyle = BATTERY_ICON_STYLE_NORMAL;

        if (mBatteryStyle == 0) {
            mIcon = (View.VISIBLE);
            mIconStyle = mBatteryPlugged ? BATTERY_ICON_STYLE_CHARGE
                    : BATTERY_ICON_STYLE_NORMAL;
        } else if (mBatteryStyle == 1) {
            mIcon = (View.VISIBLE);
            mText = (View.VISIBLE);
            mIconStyle = mBatteryPlugged ? BATTERY_ICON_STYLE_CHARGE_MIN
                    : BATTERY_ICON_STYLE_NORMAL_MIN;
        } else if (mBatteryStyle == 2) {
            mIcon = (View.VISIBLE);
            mIconStyle = mBatteryPlugged ? BATTERY_ICON_STYLE_CHARGE_CIRCLE
                    : BATTERY_ICON_STYLE_NORMAL_CIRCLE;
        } else if (mBatteryStyle == 3) {
            mIcon = (View.VISIBLE);
            mIconStyle = mBatteryPlugged ? BATTERY_ICON_STYLE_CHARGE_BAR
                    : BATTERY_ICON_STYLE_NORMAL_BAR;
        } else if (mBatteryStyle == 4) {
            mIcon = (View.VISIBLE);
            mIconStyle = mBatteryPlugged ? BATTERY_ICON_STYLE_CHARGE_FATTY
                    : BATTERY_ICON_STYLE_NORMAL_FATTY;
        } else if (mBatteryStyle == 5) {
 	    mIcon = (View.VISIBLE);
 	    mIconStyle = mBatteryPlugged ? BATTERY_ICON_STYLE_CHARGE_DIGITAL	
                    : BATTERY_ICON_STYLE_NORMAL_DIGITAL;
        }

        int N = mIconViews.size();
        for (int i=0; i<N; i++) {
            ImageView v = mIconViews.get(i);
            v.setVisibility(mIcon);
            v.setImageResource(mIconStyle);
        }
        N = mLabelViews.size();
        for (int i=0; i<N; i++) {
            TextView v = mLabelViews.get(i);
            v.setVisibility(mText);
        }
    }

    private void updateSettings() {
        ContentResolver resolver = mContext.getContentResolver();

        mBatteryStyle = (Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_BATTERY, 0));
        updateBattery();
    }
}
