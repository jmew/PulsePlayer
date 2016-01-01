package jmew;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.echonest.api.v4.DynamicPlaylistParams;
import com.echonest.api.v4.DynamicPlaylistSession;
import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.Playlist;
import com.echonest.api.v4.PlaylistParams;
import com.echonest.api.v4.Song;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.PlayerStateCallback;
import com.spotify.sdk.android.player.Spotify;

import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MusicPlayer implements PlayerNotificationCallback, ConnectionStateCallback, Serializable {

    protected SpotifyApi api = new SpotifyApi();
    protected SpotifyService spotify;

    private RelativeLayout progressView;

    private List<com.echonest.api.v4.Track> tracks = new ArrayList<>();

    private Handler trackProgressHandler = new Handler();
    private EchoNestAPI en;

    private RecyclerView mRecyclerView;

    private Context mContext;

    private PlayerFragment playerFragment;
    private Player mPlayer;

    private static MusicPlayer player;

    private boolean login = false;
    private boolean songsQueued = false;

    public static MusicPlayer getPlayer() {
        if (player != null) {
            return player;
        } else {
            player = new MusicPlayer();
            return player;
        }
    }

    public void logout() {
        mPlayer = null;
    }

    public boolean isLogin() {
        return login;
    }

    public void setLogin(boolean login) {
        this.login = login;
    }

    public void configPlayer(Player player, Context context, PlayerFragment playerFragment) {
        this.playerFragment = playerFragment;
        mContext = context;
        mPlayer = player;
        mPlayer.addConnectionStateCallback(MusicPlayer.this);
        mPlayer.addPlayerNotificationCallback(MusicPlayer.this);

        api.setAccessToken(SpotifyHelper.getAuthToken());
        spotify = api.getService();
        spotify.getMe(new Callback<UserPrivate>() {
            @Override
            public void success(UserPrivate userPrivate, Response response) {
                SpotifyHelper.setUserId(userPrivate.id);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("Playlist Fragment", "Could not get userId: " + error.getMessage());
            }
        });
    }

    private MusicPlayer() {}

    public void setLoadingSpinner(RelativeLayout progressView) {
        this.progressView = progressView;
    }

    private class EchoNestPlayer extends AsyncTask<Integer, Integer, List<com.echonest.api.v4.Track>> {

        @Override
        protected List<com.echonest.api.v4.Track> doInBackground(Integer... integers) {
            en = new EchoNestAPI("TMYLDGXSQAL7JAPZQ");
            DynamicPlaylistParams params = new DynamicPlaylistParams();

            params.addIDSpace("spotify-CA");
            params.setType(PlaylistParams.PlaylistType.GENRE_RADIO);
            params.addGenre("pop");
            params.addGenre("dance pop");
            params.addGenre("hip hop");
            params.addGenre("edm");
//            params.addGenre("rock");
//            params.addGenre("country");
            params.addSongType(Song.SongType.studio, Song.SongTypeFlag.True);
            params.setArtistMinFamiliarity(0.85f);
            params.setMinDuration(120);
            params.setMaxTempo(integers[0] + 5);
            params.setMinTempo(integers[0] - 5);
            params.includeTracks();
            params.setLimit(true);
            try {
                DynamicPlaylistSession session = en.createDynamicPlaylist(params);
                Playlist playlist = session.next();
                tracks.add(playlist.getSongs().get(0).getTrack("spotify-CA"));
                mPlayer.play(playlist.getSongs().get(0).getTrack("spotify-CA").getForeignID().replace("-CA", "").replace("-AD", ""));
                playlist = session.next(10, 1);
                for (Song song : playlist.getSongs().subList(1, playlist.getSongs().size())) {
                    com.echonest.api.v4.Track track = song.getTrack("spotify-CA");
                    tracks.add(track);
                    mPlayer.queue(track.getForeignID().replace("-CA", "").replace("-AD", ""));
                    Log.e("EchoNest", track.getForeignID() + " " + song.getTitle() + " by " + song.getArtistName());
                }
            } catch (EchoNestException e) {
                Log.e("EchoNestError", e.getMessage());
            }
            return tracks;
        }

        @Override
        protected void onPostExecute(List<com.echonest.api.v4.Track> tracks) {
            progressView.setVisibility(View.GONE);

            LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
            mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setHasFixedSize(true);
            RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL);
            mRecyclerView.addItemDecoration(itemDecoration);

            final PlaylistAdapter adapter = new PlaylistAdapter(tracks);
            mRecyclerView.setAdapter(adapter);

            ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override
                public boolean isLongPressDragEnabled() {
                    return true;
                }

                @Override
                public boolean isItemViewSwipeEnabled() {
                    return true;
                }

                @Override
                public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                    int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                    int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                    return makeMovementFlags(dragFlags, swipeFlags);
                }

                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    //TODO PREVENT REMOVING FIRST TRACK AND MOVING BEFORE FIRST TRACK
                    try {
                        updateQueue(adapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition()));
                    } catch (EchoNestException e) {
                        e.printStackTrace();
                    }
                    return true;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                    try {
                        updateQueue(adapter.onItemDismiss(viewHolder.getAdapterPosition()));
                    } catch (EchoNestException e) {
                        e.printStackTrace();
                    }
                    //TODO REMOVE FROM QUEUE
                }
            };
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
            itemTouchHelper.attachToRecyclerView(mRecyclerView);
        }
    }

    public void setRecyclerView(RecyclerView mRecyclerView) {
        this.mRecyclerView = mRecyclerView;
    }

    public void createPulsePlaylist(float heartRate) throws EchoNestException {
        new EchoNestPlayer().execute((int) heartRate);
    }

    public void playTrack(String uri) {
    }

    public void playPlaylist(List<String> uris, boolean shuffle) {
        if (shuffle) {
            mPlayer.play(uris);
            mPlayer.setShuffle(true);
        } else {
            mPlayer.play(uris);
        }
    }

    public void next() {
        mPlayer.skipToNext();
    }

    public void previous() { mPlayer.skipToPrevious(); }

    public void clearTracks() {
        mPlayer.clearQueue();
    }

    public void playPause(final ImageView button1, final ImageView button2) {
        mPlayer.getPlayerState(new PlayerStateCallback() {
            @Override
            public void onPlayerState(PlayerState playerState) {
                if (playerState.playing) {
                    mPlayer.pause();
                    button1.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    button2.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                } else {
                    //TODO check if there are songs in queue
                    mPlayer.resume();
                    button1.setImageResource(R.drawable.ic_pause_black_24dp);
                    button2.setImageResource(R.drawable.ic_pause_black_24dp);
                }
            }
        });
    }

    public void updateQueue(List<com.echonest.api.v4.Track> tracks) throws EchoNestException {
        mPlayer.clearQueue();
        for (int i = 0; i < tracks.size(); i++){
            Log.e("QUEUE", tracks.get(i).getForeignID().replace("-CA", "").replace("-AD", ""));
            mPlayer.queue(tracks.get(i).getForeignID().replace("-CA", "").replace("-AD", ""));
        }
    }

    public void setTrackProgressBar() {
        mPlayer.getPlayerState(new PlayerStateCallback() {
            @Override
            public void onPlayerState(PlayerState playerState) {
                playerFragment.setTrackProgressBar(playerState.durationInMs, playerState.positionInMs);
            }
        });
        trackProgressHandler.postDelayed(run, 1000);
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.d("MainActivity", "Playback event received: " + eventType.name());
        switch (eventType) {
            case TRACK_CHANGED:
                Log.d("track", "Track Changed");
                spotify.getTrack(playerState.trackUri.replace("spotify:track:", ""), new Callback<Track>() {
                    @Override
                    public void success(Track track, Response response) {
                        Log.d("IMAGE URL", track.album.images.get(0).url);
                        new LoadImage().execute(track.album.images.get(0).url);
                        playerFragment.trackTextConfig(track.name, track.artists.get(0).name);
                        setTrackProgressBar();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e("Image Error", error.getMessage());
                    }
                });
                break;
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Throwable error) {
        Toast.makeText(mContext, "Login failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String errorDetails) {
        Log.d("MainActivity", "Playback error received: " + errorType.name());
        switch (errorType) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    private class LoadImage extends AsyncTask<String, String, Bitmap> {
        Bitmap bitmap;

        protected Bitmap doInBackground(String... args) {
            try {
                bitmap = BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap image) {

            if (image != null) {
                playerFragment.setAlbumArt(image);

            } else {

                Toast.makeText(mContext, "Image Does Not exist or Network Error", Toast.LENGTH_SHORT).show();

            }
        }
    }

    Runnable run = new Runnable() {
        @Override
        public void run() {
            setTrackProgressBar();
        }
    };
}
