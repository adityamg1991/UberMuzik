package muzic.coffeemug.com.muzic.MusicPlayback;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import muzic.coffeemug.com.muzic.Activities.PlayTrackActivity;
import muzic.coffeemug.com.muzic.Data.SharedPrefs;
import muzic.coffeemug.com.muzic.Data.Track;
import muzic.coffeemug.com.muzic.R;
import muzic.coffeemug.com.muzic.Store.TrackStore;
import muzic.coffeemug.com.muzic.Utilities.Constants;
import muzic.coffeemug.com.muzic.Utilities.MuzicApplication;

public class MusicPlaybackService extends Service implements MusicPlaybackController.TrackPlayingObserver {

    private MediaPlayer mediaPlayer;
    private static MuzicApplication muzicApplication;
    private static SharedPrefs prefs;
    private static TrackStore mTrackStore;
    private PrefTrackUpdater prefTrackUpdater;

    private ScheduledExecutorService service;

    public MusicPlaybackService() {
    }


    @Override
    public void onCreate() {
        super.onCreate();

        mediaPlayer = new MediaPlayer();
        service = Executors.newScheduledThreadPool(1);

        prefTrackUpdater = PrefTrackUpdater.getInstance(mediaPlayer);

        muzicApplication = MuzicApplication.getInstance();
        prefs = SharedPrefs.getInstance(this);
        mTrackStore = TrackStore.getInstance();

        mediaPlayer.setOnErrorListener(new MediaPlayerErrorListener());
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(null != intent) {

            try {

                Bundle args = intent.getExtras();
                if(null != args) {

                    int action = args.getInt(MusicPlaybackConstants.ACTION);
                    stopForeground(true);
                    Track trackToBePlayed = SharedPrefs.getInstance(this).getStoredTrack();

                    if(null != trackToBePlayed) {

                        if(MusicPlaybackConstants.ACTION_PLAY == action) {

                            mediaPlayer.reset();
                            mediaPlayer.setDataSource(trackToBePlayed.getData());
                            mediaPlayer.prepareAsync();

                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    mp.start();
                                }
                            });

                            startForeground(MusicPlaybackConstants.NOTI_ID, getNotification(trackToBePlayed));
                            publishTrackProgress();
                            prefTrackUpdater.store();

                        } else if(MusicPlaybackConstants.ACTION_PAUSE == action) {

                            mediaPlayer.stop();
                            mediaPlayer.reset();
                            pausePublishingTrackProgress();
                            prefTrackUpdater.stopStoring();
                            prefTrackUpdater = null;
                            stopSelf();

                        } else if(MusicPlaybackConstants.ACTION_CONTINUE == action) {

                            // Start from beginning if no track is present in SharedPrefs
                            Track track = prefs.getStoredTrack();
                            if(null == track) {
                                Track firstTrack = mTrackStore.getFirstTrack();
                                if(null != firstTrack) {
                                    prefs.storeTrack(firstTrack);

                                    // Start playing the song
                                    Intent serviceIntent = new Intent(this, MusicPlaybackService.class);
                                    serviceIntent.putExtra(MusicPlaybackConstants.ACTION, MusicPlaybackConstants.ACTION_PLAY);
                                    startService(serviceIntent);

                                    // Tell PlayTrackActivity that new song is stored in SharedPrefs now
                                    Intent i = new Intent(Constants.TRACK_UPDATE_FROM_SERVICE);
                                    i.putExtra(Constants.UPDATE_TRACK, true);
                                    LocalBroadcastManager.getInstance(MusicPlaybackService.this).sendBroadcast(i);

                                } else {
                                    // Nothing can help you now.
                                }
                            } else {

                                final int progress = prefs.getTrackCurrentPosition();

                                mediaPlayer.reset();
                                mediaPlayer.setDataSource(trackToBePlayed.getData());
                                mediaPlayer.prepareAsync();

                                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                    @Override
                                    public void onPrepared(MediaPlayer mp) {
                                        mp.start();
                                        mp.seekTo(progress * 1000);
                                    }
                                });

                                startForeground(MusicPlaybackConstants.NOTI_ID, getNotification(trackToBePlayed));
                                publishTrackProgress();
                                prefTrackUpdater.store();
                            }
                        }

                    }
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }


        return START_NOT_STICKY;
    }




    /**
     * Publishes Track progress update ( in milliseconds) to any body who is listening.
     */
    private void publishTrackProgress() {

        if(null == mediaPlayer || !mediaPlayer.isPlaying()) {
            return;
        }
        service.scheduleWithFixedDelay(new Runnable() {
            public void run() {

                Intent i = new Intent(Constants.TRACK_UPDATE_FROM_SERVICE);
                i.putExtra(Constants.TRACK_PROGRESSION_UPDATE_KEY, mediaPlayer.getCurrentPosition());
                LocalBroadcastManager.getInstance(MusicPlaybackService.this).sendBroadcast(i);
            }
        }, 1, 1, TimeUnit.MICROSECONDS);
    }


    private void pausePublishingTrackProgress() {

        if(null != service) {
            service.shutdownNow();
        }
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

        Notification notification = new NotificationCompat.Builder(this)
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


    @Override
    public boolean isTrackPlaying() {

        if(null != mediaPlayer) {
            return mediaPlayer.isPlaying();
        } else {
            return false;
        }
    }


    /**
     * Handles the error thrown by Media Player
     * For now : If error found, restarting the track
     */
    private class MediaPlayerErrorListener implements MediaPlayer.OnErrorListener {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {

            Log.d("Aditya", "Error : " + what);
            return false;
        }
    }


    private static class PrefTrackUpdater {

        private static PrefTrackUpdater instance;
        private static ScheduledExecutorService innerService;
        private static MediaPlayer mpGlobal;

        private PrefTrackUpdater(){};

        private static PrefTrackUpdater getInstance(MediaPlayer mp) {

            innerService = Executors.newScheduledThreadPool(1);

            if(null == instance) {
                instance = new PrefTrackUpdater();
                mpGlobal = mp;
            }
            return instance;
        }

        /**
         * Stores track progress in Shared Preferences every 3 seconds.
         */
        private void store() {

            if(null != innerService) {

                innerService.scheduleWithFixedDelay(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("Aditya", "Saving track progress to Shared prefs : " + mpGlobal.getCurrentPosition()/1000);
                        prefs.setTrackCurrentPosition(mpGlobal.getCurrentPosition()/1000);
                    }
                }, 3, 3, TimeUnit.SECONDS);
            }
        }

        private void stopStoring() {
            if(null != innerService) {
                innerService.shutdownNow();
            }
        }

    }
}

