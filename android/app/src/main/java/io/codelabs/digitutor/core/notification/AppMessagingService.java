package io.codelabs.digitutor.core.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import io.codelabs.digitutor.R;
import io.codelabs.digitutor.view.HomeActivity;
import io.codelabs.digitutor.view.RequestDetailsActivity;
import io.codelabs.digitutor.view.kotlin.FeedbackActivity;
import io.codelabs.sdk.util.ExtensionUtils;

import java.util.Map;
import java.util.Objects;

/**
 * Send and receive push notifications from the firebase messaging service
 */
public class AppMessagingService extends FirebaseMessagingService {

    public static final String CHANNEL_ID = "home_tutor_default_channel_id";
    public static final int RC_NOTIFICATION = 133;
    public static final int NOTIFICATION_ICON = R.drawable.shr_logo;
    public static final int NOTIFICATION_ID = (int) System.currentTimeMillis();

    public static final String TYPE_REQUEST = "tutor-request";
    public static final String TYPE_FEEDBACK = "tutor-feedback";
    public static final String TYPE_ASSIGNMENT = "ward-assignment";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage == null) return;
        Map<String, String> data = remoteMessage.getData();
        createNotificationChannel(getString(R.string.default_notification_channel_id));
        if (data.containsKey("type")) {

            switch (Objects.requireNonNull(data.get("type"))) {
                case TYPE_REQUEST:
                    // Add data to intent
                    Intent i = new Intent(getApplicationContext(), RequestDetailsActivity.class);
                    i.putExtra(RequestDetailsActivity.EXTRA_REQUEST_ID, data.get("id"));
                    i.putExtra(RequestDetailsActivity.EXTRA_REQUEST_PARENT, data.get("parent"));

                    // Send notification to device
                    pushNotification(data.get("title"), data.get("message"), i);
                    break;

                case TYPE_FEEDBACK:
                    Intent feedbackIntent = new Intent(getApplicationContext(), FeedbackActivity.class);
                    ExtensionUtils.debugLog(getApplicationContext(), "Feedback received as: " + data.get("key"));
                    feedbackIntent.putExtra(FeedbackActivity.EXTRA_FEEDBACK_ID, data.get("key"));

                    // Send notification to device
                    pushNotification(data.get("title"), data.get("message"), feedbackIntent);
                    break;

                case TYPE_ASSIGNMENT:
                    Intent assignmentIntent = new Intent(getApplicationContext(), HomeActivity.class);

                    // Send notification to device
                    pushNotification(data.get("title"), data.get("message"), assignmentIntent);
                    break;
            }

        }
    }

    private void pushNotification(String title, String content, Intent intent) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), RC_NOTIFICATION, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Customize the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(NOTIFICATION_ICON)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setVibrate(new long[]{0, 200, 0, 300})
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // Push notification
        if (manager != null) manager.notify(NOTIFICATION_ID, builder.build());

    }

    private void createNotificationChannel(String channelName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String descriptionText = "app__description_channel";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(descriptionText);

            // Register the channel with the system
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
