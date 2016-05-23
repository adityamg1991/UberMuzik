package muzic.coffeemug.com.muzic.MusicPlaybackV2;

import android.app.ActivityManager;
import android.content.Context;

/**
 * Created by PAVILION on 5/23/2016.
 */
public class MasterPlaybackUtils {

    private static MasterPlaybackUtils instance;

    private MasterPlaybackUtils(){}

    public static MasterPlaybackUtils getInstance() {
        if (null == instance) {
            instance = new MasterPlaybackUtils();
        }
        return instance;
    }

    public  boolean isMasterPlaybackServiceRunning(Context context) {
        final Class<?> serviceClass = MasterPlaybackService.class;
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
