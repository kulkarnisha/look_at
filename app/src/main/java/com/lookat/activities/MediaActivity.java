package com.lookat.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lookat.R;
import com.lookat.adapters.MediaAdapter;
import com.lookat.fragments.MediaFragment;
import com.lookat.models.FileItem;
import com.lookat.utils.FileType;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

public class MediaActivity extends AppCompatActivity {
	private static final String TAG = "Media ";
	
	private boolean inStarred = false;
	private ViewPager2 viewPager;
	private MediaAdapter mediaAdapter;
	
	private InterstitialAd mInterstitialAd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "Media Activity");
		setContentView(R.layout.activity_media);
		
		getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "NOTES_SEARCH", MODE_PRIVATE).edit().putBoolean("NotesSearchExists", false).apply();
		
		viewPager = findViewById(R.id.viewPager2);
		Intent intent = getIntent();
		String state = intent.getStringExtra("State");
		
		if (state.equals("Files")) {
			
			String path = intent.getStringExtra("Media");
			inStarred = intent.getBooleanExtra("InStarred", false);
			File file = new File(path);
			File parentFile = file.getParentFile();
			mediaData(parentFile, file);
			
		} else if (state.equals("Home")) {
			
			ArrayList<FileItem> homeMedia = intent.getParcelableArrayListExtra("HomeMedia");
			int position = intent.getIntExtra("Position", 0);
			homeMediaDisplay(homeMedia, position);
			
		}
		
		mInterstitialAd = new InterstitialAd(this);
		mInterstitialAd.setAdUnitId(getString(R.string.notes_interstitial_ad));
		mInterstitialAd.loadAd(new AdRequest.Builder().build());
	}
	
	private void homeMediaDisplay(ArrayList<FileItem> homeMedia, int value) {
		
		ArrayList<String> mediaFiles = new ArrayList<>();
		for (FileItem fileItem : homeMedia) {
			mediaFiles.add(fileItem.getPath());
		}
		mediaAdapter = new MediaAdapter(getSupportFragmentManager(), getLifecycle());
		for (String s : mediaFiles) {
			mediaAdapter.addFragment(MediaFragment.newInstance(s));
		}
		
		viewPager.setAdapter(mediaAdapter);
		viewPager.setCurrentItem(value, false);
		viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				super.onPageScrolled(position, positionOffset, positionOffsetPixels);
			}
			
			@Override
			public void onPageSelected(final int position) {
				super.onPageSelected(position);
				try {
					(mediaAdapter.createFragment(position - 1)).isHidden();
					if (position != 0 && position % 3 == 0 && mInterstitialAd.isLoaded()) {
						mInterstitialAd.show();
						mInterstitialAd.setAdListener(new AdListener() {
							@Override
							public void onAdClosed() {
								(mediaAdapter.createFragment(position)).isVisible();
								mInterstitialAd.loadAd(new AdRequest.Builder().build());
							}
						});
					} else {
						(mediaAdapter.createFragment(position)).isVisible();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				super.onPageScrollStateChanged(state);
			}
		});
		
	}
	
	public void mediaData(File parent, File child) {
		ArrayList<String> mediafiles = new ArrayList<>();
		
		int value = 0;
		if (!inStarred) {
			File[] files = parent.listFiles();
			assert files != null;
			for (File f : files) {
				FileItem newFile = new FileItem(f.getPath());
				
				if (newFile.getType() == FileType.FILE_TYPE_VIDEO || newFile.getType() == FileType.FILE_TYPE_AUDIO || newFile.getType() == FileType.FILE_TYPE_IMAGE) {
					mediafiles.add(newFile.getPath());
					if (f.getName().equals(child.getName()))
						value = mediafiles.size() - 1;
				}
			}
		} else {
			ArrayList<FileItem> starredFiles;
			
			SharedPreferences starredPreference = getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
			
			if (starredPreference.getBoolean("STARRED_ITEMS_EXISTS", false)) {
				Gson gson = new Gson();
				String json = starredPreference.getString("STARRED_ITEMS", "");
				Type type = new TypeToken<List<FileItem>>() {
				}.getType();
				starredFiles = gson.fromJson(json, type);
			} else {
				starredFiles = new ArrayList<>();
			}
			
			assert starredFiles != null;
			for (FileItem newFile : starredFiles) {
				if (newFile.getType() == FileType.FILE_TYPE_VIDEO || newFile.getType() == FileType.FILE_TYPE_AUDIO || newFile.getType() == FileType.FILE_TYPE_IMAGE) {
					mediafiles.add(newFile.getPath());
					if (newFile.getName().equals(child.getName()))
						value = mediafiles.size() - 1;
				}
			}
		}
		
		mediaAdapter = new MediaAdapter(getSupportFragmentManager(), getLifecycle());
		for (String s : mediafiles) {
			mediaAdapter.addFragment(MediaFragment.newInstance(s));
		}
		
		viewPager.setAdapter(mediaAdapter);
		viewPager.setCurrentItem(value, false);
		viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				super.onPageScrolled(position, positionOffset, positionOffsetPixels);
			}
			
			@Override
			public void onPageSelected(final int position) {
				super.onPageSelected(position);
				try {
					(mediaAdapter.createFragment(position - 1)).isHidden();
					if (position != 0 && position % 3 == 0 && mInterstitialAd.isLoaded()) {
						mInterstitialAd.show();
						mInterstitialAd.setAdListener(new AdListener() {
							@Override
							public void onAdClosed() {
								(mediaAdapter.createFragment(position)).isVisible();
								mInterstitialAd.loadAd(new AdRequest.Builder().build());
							}
						});
					} else {
						(mediaAdapter.createFragment(position)).isVisible();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				super.onPageScrollStateChanged(state);
			}
		});
		
	}
}