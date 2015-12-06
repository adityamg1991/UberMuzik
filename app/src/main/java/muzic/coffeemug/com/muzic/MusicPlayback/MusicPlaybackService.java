package muzic.coffeemug.com.muzic.MusicPlayback;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;
import android.widget.TextView;

import java.io.IOException;

import muzic.coffeemug.com.muzic.Activities.PlayTrackActivity;
import muzic.coffeemug.com.muzic.Activities.TrackListActivity;
import muzic.coffeemug.com.muzic.Data.Track;
import muzic.coffeemug.com.muzic.R;
import muzic.coffeemug.com.muzic.Utilities.Constants;
import muzic.coffeemug.com.muzic.Utilities.MuzicApplication;

public class MusicPlaybackService extends Service {

    private MediaPlayer mediaPlayer;
    MuzicApplication muzicApplication;


    public MusicPlaybackService() {
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        muzicApplication = MuzicApplication.getInstance();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(null != intent) {

            Bundle bundle = intent.getExtras();
            if(null != bundle) {
                try {

                    Track trackToBePlayed = bundle.getParcelable(MusicPlaybackConstants.TRACK_TO_BE_PLAYED);
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(trackToBePlayed.getData());
                    mediaPlayer.prepare();
                    mediaPlayer.start();

                    startForeground(MusicPlaybackConstants.NOTI_ID, getNotification(trackToBePlayed));

                } catch(IOException e) {
                    e.printStackTrace();
                }

            }
        }


        return START_NOT_STICKY;
    }


    private Notification getNotification(Track track) {

        // Setting up the view of Notification
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_layout);

        // Adding relevant data to notification
        Bitmap bmp = muzicApplication.getSongCoverArt(this, Long.parseLong(track.getAlbumID()));
        if(null == bmp) {
            bmp = BitmapFactory.decodeResource(getResources(), R.drawable.no_album_art_black_small);
        }
        remoteViews.setImageViewBitmap(R.id.iv_noti_album_art, bmp);
        remoteViews.setTextViewText(R.id.noti_track_name, track.getTitle());
        remoteViews.setTextViewText(R.id.noti_artist_name, track.getArtist());

        // Setting up clicks on Notification (Next track)
        Intent nextTrackIntent = new Intent(this, NotificationButtonClickListener.class);
        nextTrackIntent.setAction(MusicPlaybackConstants.NOTIFICATION_NEXT_TRACK);
        PendingIntent piNextTrack = PendingIntent.getBroadcast(this, 0, nextTrackIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.noti_next, piNextTrack);

        // Setting up clicks on Notification (Play/Pause)
        Intent playIntent = new Intent(this, NotificationButtonClickListener.class);
        playIntent.setAction(MusicPlaybackConstants.NOTIFICATION_PLAY_PAUSE);
        PendingIntent piPlayPause = PendingIntent.getBroadcast(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.noti_play_pause, piPlayPause);

        Intent notificationIntent = new Intent(this, PlayTrackActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification=new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.play_icon)
                .setContent(remoteViews)
                .setContentIntent(pendingIntent).build();

        return notification;
    }





    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}