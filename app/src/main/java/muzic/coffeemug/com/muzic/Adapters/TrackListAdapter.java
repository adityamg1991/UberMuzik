package muzic.coffeemug.com.muzic.Adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import muzic.coffeemug.com.muzic.Streaming.Models.SoundCloudTrack;
import muzic.coffeemug.com.muzic.Utilities.AppConstants;
import muzic.coffeemug.com.muzic.Data.Track;
import muzic.coffeemug.com.muzic.Dialogs.TrackOptionsDialog;
import muzic.coffeemug.com.muzic.R;

/**
 * Created by aditya on 02/09/15.
 */
public class TrackListAdapter extends RecyclerView.Adapter<TrackListAdapter.ViewHolder> {

    private ArrayList<Track> dataSet;
    private Context mContext;
    private ResultReceiver resultReceiver;
    private boolean isLongClickEnabled = false;


    public TrackListAdapter(Context context, ArrayList<Track> dataSet,
                            ResultReceiver resultReceiver, boolean isLongClickEnabled) {
        this.dataSet = dataSet;
        this.mContext = context;
        this.resultReceiver = resultReceiver;
        this.isLongClickEnabled = isLongClickEnabled;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.track_list_item, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        Track track = dataSet.get(position);

        String strArtist = track.getArtist();
        String strTitle = track.getTitle();
        String strAlbumName = track.getAlbumName();

        if (!TextUtils.isEmpty(strTitle)) {
            holder.mTextView.setText(strTitle);
        }

        String strInfo = "";

        if (!TextUtils.isEmpty(strArtist)) {
            strInfo += strArtist;
            strInfo += "  |  ";
        }

        if (!TextUtils.isEmpty(strAlbumName)) {
            strInfo += strAlbumName;
        }

        holder.tvInfo.setText(strInfo);

        holder.llContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int pos = (int) holder.llContainer.getTag();
                Track selectedTrack = dataSet.get(pos);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppConstants.SELECTED_TRACK, selectedTrack);
                resultReceiver.send(Activity.RESULT_OK, bundle);
            }
        });

        holder.llContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                if (isLongClickEnabled) {
                    int pos = (int) holder.llContainer.getTag();
                    Track selectedTrack = dataSet.get(pos);
                    new TrackOptionsDialog(selectedTrack, mContext, resultReceiver).show();
                }
                return true;

            }
        });

        holder.llContainer.setTag(position);
    }


    @Override
    public int getItemCount() {
        return dataSet.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mTextView, tvSeparator;
        public TextView tvInfo;
        public LinearLayout llContainer;

        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.info_text);
            tvInfo = (TextView) v.findViewById(R.id.tv_info);
            llContainer = (LinearLayout) v.findViewById(R.id.ll_container);
            tvSeparator = (TextView) v.findViewById(R.id.tv_separator);
        }
    }

}
