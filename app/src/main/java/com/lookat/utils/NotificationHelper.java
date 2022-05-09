package com.lookat.utils;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;

import com.lookat.R;
import com.lookat.activities.MainActivity;
import com.lookat.models.ReminderItem;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class NotificationHelper extends ContextWrapper {
	public static final String channelID = "StudyPartnerChannelId";
	public static final String channelName = "Reminders";
	private NotificationManager mManager;
	
	public NotificationHelper(Context base) {
		super(base);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			createChannel();
		}
	}
	
	@TargetApi(Build.VERSION_CODES.O)
	private void createChannel() {
		NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);
		channel.setLightColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));
		channel.enableLights(true);
		AudioAttributes audioAttributes = new AudioAttributes.Builder()
				.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
				.setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
				.build();
		channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), audioAttributes);
		getManager().createNotificationChannel(channel);
	}
	
	public NotificationManager getManager() {
		if (mManager == null) {
			mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		}
		return mManager;
	}
	
	public NotificationCompat.Builder getChannelNotification(Context context, ReminderItem item) {
		
		Bundle bundle = new Bundle();
		bundle.putParcelable("BUNDLE_REMINDER_ITEM", item);
		
//		PendingIntent pendingIntent = new NavDeepLinkBuilder(context)
//				.setComponentName(MainActivity.class)
//				.setGraph(R.navigation.main_nav_graph)
//				.setDestination(R.id.nav_reminder)
//				.createPendingIntent();
		
		Intent openIntent = new Intent(context, MainActivity.class);
		openIntent.putExtra("EXTRA_REMINDER_ITEM", bundle);
		openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent openPendingIntent = PendingIntent.getActivity(context,1,openIntent,PendingIntent.FLAG_UPDATE_CURRENT);
		
		Intent dismissIntent = new Intent(context, ReminderAlertReceiver.class);
		dismissIntent.putExtra("EXTRA_REMINDER_ITEM", bundle);
		dismissIntent.putExtra("CANCEL", true);
		PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(context, 1, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		return new NotificationCompat.Builder(context, channelID)
				.setAutoCancel(true)
				.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
				.setPriority(NotificationCompat.PRIORITY_MAX)
				.setContentTitle(item.getTitle())
				.setContentText(item.getDescription())
				.setContentIntent(openPendingIntent)
				.addAction(android.R.drawable.ic_menu_view, "OPEN IN APP", openPendingIntent)
				.addAction(android.R.drawable.ic_delete, "DISMISS", dismissPendingIntent)
				.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.app_logo_round))
				.setSmallIcon(R.drawable.app_logo_transparent)
				.setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
				.setLights(ContextCompat.getColor(this, R.color.colorPrimaryDark), 1000, 1000)
				.setDefaults(NotificationCompat.DEFAULT_VIBRATE);
	}
}
