package muzic.coffeemug.com.muzic.Streaming.Activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import muzic.coffeemug.com.muzic.Activities.BaseActivity;
import muzic.coffeemug.com.muzic.Fragments.BaseFragment;
import muzic.coffeemug.com.muzic.R;
import muzic.coffeemug.com.muzic.Streaming.Fragments.FragmentRecomsHome;
import muzic.coffeemug.com.muzic.Streaming.Playback.StreamingController;
import muzic.coffeemug.com.muzic.Utilities.MasterPlaybackUtils;

public class RecomsHomeActivity extends BaseActivity {


    private FragmentManager managerFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recoms_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Recommendations");

        managerFragment = getSupportFragmentManager();
        if (null == managerFragment.findFragmentByTag(FRAG_TAGS.HOME)) {
            FragmentRecomsHome fragmentRecomsHome = FragmentRecomsHome.newInstance();
            managerFragment.beginTransaction().add(R.id.ll_container,
                    fragmentRecomsHome, FRAG_TAGS.HOME).commit();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    public void loadFragment(BaseFragment fragment) {
        managerFragment.beginTransaction().replace(R.id.ll_container,
                fragment, FRAG_TAGS.ONLINE).addToBackStack(null).commit();
    }


    public interface FRAG_TAGS {
        String HOME = "HOME";
        String ONLINE = "ONLINE";
    }


    @Override
    public void onBackPressed() {

        if (managerFragment.getBackStackEntryCount() == 0) {

            if (MasterPlaybackUtils.getInstance().isMasterStreamingServiceRunning(this)){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Warning");
                builder.setMessage("Continue back and stop streaming Music?");
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        StreamingController.getInstance(RecomsHomeActivity.this).pauseTrack();
                        RecomsHomeActivity.super.onBackPressed();
                    }
                });
                builder.create().show();
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }
}