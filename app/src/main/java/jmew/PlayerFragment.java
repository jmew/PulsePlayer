package jmew;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;

public class PlayerFragment extends Fragment {

    private ImageView smallPlayerAlbumArt;
    private ImageView fullPlayerAlbumArt;
    private TextView trackNameSmallPlayer;
    private TextView trackNameFullPlayer;
    private TextView artistNameSmallPlayer;
    private TextView artistNameFullPlayer;
    private RoundCornerProgressBar trackProgressBarSmallPlayer;
    private RoundCornerProgressBar trackProgressBarFullPlayer;

    private MusicPlayer musicPlayer;

    public PlayerFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.player_fragment, container, false);

        musicPlayer = MusicPlayer.getPlayer();

        smallPlayerAlbumArt = (ImageView) rootView.findViewById(R.id.small_player_album_art);
        fullPlayerAlbumArt = (ImageView) rootView.findViewById(R.id.full_player_album_art);
        trackNameSmallPlayer = (TextView) rootView.findViewById(R.id.track_title_small_player);
        trackNameSmallPlayer.setSelected(true);
        trackNameFullPlayer = (TextView) rootView.findViewById(R.id.track_title_full_player);
        trackNameFullPlayer.setSelected(true);
        artistNameSmallPlayer = (TextView) rootView.findViewById(R.id.artist_name_small_player);
        artistNameSmallPlayer.setSelected(true);
        artistNameFullPlayer = (TextView) rootView.findViewById(R.id.artist_name_full_player);
        artistNameFullPlayer.setSelected(true);
        trackProgressBarSmallPlayer = (RoundCornerProgressBar) rootView.findViewById(R.id.track_progress_small_player);
        trackProgressBarFullPlayer = (RoundCornerProgressBar) rootView.findViewById(R.id.track_progress_full_player);

        ImageView nextButton = (ImageView) rootView.findViewById(R.id.fast_forward_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicPlayer.next();
            }
        });

        ImageView previousButton = (ImageView) rootView.findViewById(R.id.rewind_button);
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicPlayer.previous();
            }
        });

        final ImageView mPlayButtonSmall = (ImageView) rootView.findViewById(R.id.play_button);
        final ImageView mPlayButtonFull = (ImageView) rootView.findViewById(R.id.play_button_full);
        mPlayButtonSmall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicPlayer.playPause((ImageView) view, mPlayButtonFull);
            }
        });

        mPlayButtonFull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicPlayer.playPause((ImageView) view, mPlayButtonSmall);
            }
        });

        return rootView;
    }

    public void setTrackProgressBar(int duration, int position) {
        trackProgressBarSmallPlayer.setMax(duration);
        trackProgressBarSmallPlayer.setProgress(position);
        trackProgressBarFullPlayer.setMax(duration);
        trackProgressBarFullPlayer.setProgress(position);
    }

    public void setAlbumArt(Bitmap image) {
        smallPlayerAlbumArt.setImageBitmap(image);
        fullPlayerAlbumArt.setImageBitmap(image);
    }

    public void trackTextConfig(String trackName, String artistName) {
        trackNameSmallPlayer.setText(trackName);
        artistNameSmallPlayer.setText(artistName);

        trackNameFullPlayer.setText(trackName);
        artistNameFullPlayer.setText(artistName);
    }
}
