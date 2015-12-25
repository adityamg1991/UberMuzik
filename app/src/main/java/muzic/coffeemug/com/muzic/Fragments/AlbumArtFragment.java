package muzic.coffeemug.com.muzic.Fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import muzic.coffeemug.com.muzic.Data.SharedPrefs;
import muzic.coffeemug.com.muzic.Data.Track;
import muzic.coffeemug.com.muzic.R;
import muzic.coffeemug.com.muzic.Store.TrackStore;
import muzic.coffeemug.com.muzic.Utilities.MuzicApplication;

/**
 * Created by Aditya on 9/20/2015.
 */
public class AlbumArtFragment extends BaseFragment implements View.OnClickListener{

    private ImageView ivAlbumArt;
    private boolean isControlPanelVisible;
    private RelativeLayout rlSettings;
    private int valueToAnimate = -1;
    private MuzicApplication muzicApplication;

    public static AlbumArtFragment getInstance() {
        AlbumArtFragment frag = new AlbumArtFragment();
        return frag;
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.album_art_fragment, container, false);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        muzicApplication = MuzicApplication.getInstance();
        isControlPanelVisible = false;
        ivAlbumArt = (ImageView) getActivity().findViewById(R.id.iv_album_art);
        rlSettings = (RelativeLayout) getActivity().findViewById(R.id.rl_settings);
        setAlbumArt();

        // Click on ImageView and shit
        ivAlbumArt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isControlPanelVisible) {
                    hideSettings();
                } else {
                    showSettings();
                }

            }
        });

        // Get width of Settings View
        ViewTreeObserver vto = rlSettings.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                valueToAnimate = rlSettings.getWidth();
            }
        });

        // Set up clicks
        getActivity().findViewById(R.id.iv_sound_settings).setOnClickListener(this);
        getActivity().findViewById(R.id.iv_share).setOnClickListener(this);
    }

    public void setAlbumArt() {

        Track track = SharedPrefs.getInstance(getActivity()).getStoredTrack();

        if(null != track) {
            try {
                Bitmap bmp = MuzicApplication.getInstance().getHighResAlbumArt(track.getAlbumID(), getActivity());
                if(null != bmp) {
                    ivAlbumArt.setImageBitmap(bmp);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ivAlbumArt.setImageResource(R.drawable.no_album_art);
    }


    private void showSettings() {
        if(-1 != valueToAnimate) {
            ivAlbumArt.animate().translationXBy(valueToAnimate).start();
            rlSettings.animate().alpha(1).start();
            isControlPanelVisible = true;
        }
    }


    private void hideSettings() {
        if(-1 != valueToAnimate) {
            ivAlbumArt.animate().translationXBy(-valueToAnimate).start();
            rlSettings.animate().alpha(0).start();
            isControlPanelVisible = false;
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.iv_sound_settings : {
                try {
                    Intent aa = new Intent(android.provider.Settings.ACTION_SOUND_SETTINGS);
                    startActivityForResult(aa,0);
                } catch(Exception e) {
                    Toast.makeText(getActivity(), "Oops, some error occurred", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.iv_share : {
                muzicApplication.shareTrack(getActivity());
                break;
            }
        }
    }
}
