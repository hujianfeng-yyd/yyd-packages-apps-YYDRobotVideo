/*
 *  Copyright 2014 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.yongyida.robot.video.apprtc;

import com.yongyida.robot.video.R;
import com.yongyida.robot.video.comm.log;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Settings fragment for AppRTC.
 */
public class SettingsFragment extends PreferenceFragment {
	private static final String TAG = SettingsFragment.class.getSimpleName();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
	}
}
