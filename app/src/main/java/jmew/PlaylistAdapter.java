package jmew;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.Track;

import java.util.Collections;
import java.util.List;

/**
 * Created by Mew on 2015-12-13.
 */
public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    List<Track> tracks;

    public PlaylistAdapter(List<Track> tracks) {
        this.tracks = tracks;
    }

    public static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        static TextView trackName;
        static TextView trackArtist;

        PlaylistViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            trackName = (TextView)itemView.findViewById(R.id.playlist_track_name);
            trackArtist = (TextView)itemView.findViewById(R.id.playlist_track_artist);
        }
    }

    public List<Track> onItemDismiss(int position) {
        tracks.remove(position);
        notifyItemRemoved(position);
        return tracks;
    }

    public List<Track> onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(tracks, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(tracks, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        return tracks;
    }

    @Override
    public PlaylistViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.playlist_item, viewGroup, false);
        PlaylistViewHolder pvh = new PlaylistViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(PlaylistViewHolder personViewHolder, int i) {
        try {
            PlaylistViewHolder.trackName.setText(tracks.get(i).getTitle());
            PlaylistViewHolder.trackArtist.setText(tracks.get(i).getArtistName());
        } catch (EchoNestException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
