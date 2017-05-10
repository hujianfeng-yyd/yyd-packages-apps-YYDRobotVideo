package com.yongyida.robot.video.setting;

import com.yongyida.robot.video.Config;
import com.yongyida.robot.video.R;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.view.Window;

/**
 * 合成设置界面
 */
public class VideoSettings extends PreferenceActivity implements OnPreferenceChangeListener {
	public static final String PREFER_NAME = Config.PREFERENCE_NAME;
	private EditTextPreference mVideoWidthPreference;
	private EditTextPreference mVideoHeightPreference;
	private EditTextPreference mVideoFpsPreference;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		
		// 指定保存文件名字
		getPreferenceManager().setSharedPreferencesName(PREFER_NAME);
		addPreferencesFromResource(R.xml.video_setting);
		mVideoWidthPreference = (EditTextPreference)findPreference("video_width_preference");
		mVideoWidthPreference.getEditText().addTextChangedListener(new SettingTextWatcher(VideoSettings.this, mVideoWidthPreference, 0, 1280));
		
		mVideoHeightPreference = (EditTextPreference)findPreference("video_heigit_preference");
		mVideoHeightPreference.getEditText().addTextChangedListener(new SettingTextWatcher(VideoSettings.this, mVideoHeightPreference, 0, 720));
		
		mVideoFpsPreference = (EditTextPreference)findPreference("video_fps_preference");
		mVideoFpsPreference.getEditText().addTextChangedListener(new SettingTextWatcher(VideoSettings.this, mVideoFpsPreference, 0, 30));
	}
	
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		return true;
	}
}