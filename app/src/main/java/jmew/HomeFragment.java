package jmew;

import android.animation.ArgbEvaluator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.echonest.api.v4.EchoNestException;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;

import java.util.ArrayList;
import java.util.Collections;

public class HomeFragment extends Fragment {

    private Context mContext;
    private SensorEventListener heartRateSensor;
    private DecoView mDecoView;
    private SeriesItem backSeriesItem;

    private ValueAnimator colorAnimation;

    private int mHeartRateSeriesIndex;
    private int mBackIndex;
    private final float mSeriesMax = 150f;
    private ArrayList<Integer> lastHeartRates;

    private Sensor mHeartRateSensor;
    private SensorManager mSensorManager;

    private TextView mHeartRateText;

    private RelativeLayout creatingPlaylistView;

    private MusicPlayer musicPlayer;

    private static CircularProgressView progressView;
    private ImageView plusButton;

    public HomeFragment() {
        mContext = getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.home_fragment, container, false);

        musicPlayer = MusicPlayer.getPlayer();
        musicPlayer.setRecyclerView((RecyclerView) rootView.findViewById(R.id.pulse_playlist));

        plusButton = (ImageView) rootView.findViewById(R.id.plus_icon);

        mSensorManager = (SensorManager) rootView.getContext().getSystemService(mContext.SENSOR_SERVICE);
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        heartRateSensor = new HeartRateSensor();

        progressView = (CircularProgressView) rootView.findViewById(R.id.progress_view);
        creatingPlaylistView = (RelativeLayout) rootView.findViewById(R.id.playlist_loading);
        musicPlayer.setLoadingSpinner(creatingPlaylistView);

        mHeartRateText = (TextView) rootView.findViewById(R.id.heartRateText);
        mDecoView = (DecoView) rootView.findViewById(R.id.dynamicArcView);
        createBackSeries();
        createBackSeriesEvent();

        mDecoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                plusButton.setVisibility(View.GONE);
                flashCircleEvent(1000);
                lastHeartRates = new ArrayList<>();
                heartRateSensor = new HeartRateSensor();
                mHeartRateText.setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.heartRateLabel).setVisibility(View.GONE);
                mSensorManager.registerListener(heartRateSensor, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(heartRateSensor);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void createBackSeries() {
        backSeriesItem = new SeriesItem.Builder(Color.parseColor("#FFFFFFFF"))
                .setRange(0, mSeriesMax, mSeriesMax)
                .setInitialVisibility(true)
                .build();

        mBackIndex = mDecoView.addSeries(backSeriesItem);
    }

    private void createAnimationSeries(Integer color) {
        backSeriesItem.setColor(color);
        mDecoView.executeReset();
        mBackIndex = mDecoView.addSeries(backSeriesItem);
        mDecoView.addEvent(new DecoEvent.Builder(mSeriesMax)
                .setIndex(mBackIndex)
                .build());
    }

    private void createDataSeries() {
        final SeriesItem seriesItem = new SeriesItem.Builder(Color.parseColor("#FFE91E63"))
                .setRange(0, mSeriesMax, 0)
                .setInitialVisibility(false)
                .build();

        seriesItem.addArcSeriesItemListener(new SeriesItem.SeriesItemListener() {
            @Override
            public void onSeriesItemAnimationProgress(float percentComplete, float currentPosition) {
                int BPM = (int) currentPosition;
                if (BPM >= 100) {
                    mHeartRateText.setText(Html.fromHtml("<b>" + String.format("%03d", BPM) + "</b>"));
                } else {
                    mHeartRateText.setText(Html.fromHtml("0<b>" + String.format("%02d", BPM) + "</b>"));
                }
            }

            @Override
            public void onSeriesItemDisplayProgress(float percentComplete) {

            }
        });

        mHeartRateSeriesIndex = mDecoView.addSeries(seriesItem);
    }

    private void createBackSeriesEvent() {
        mDecoView.addEvent(new DecoEvent.Builder(mSeriesMax)
                .setIndex(mBackIndex)
                .build());
    }

    private void showHeartRateCircleEvent(float heartRate) {
        mDecoView.executeReset();
        createBackSeriesEvent();

        mDecoView.addEvent(new DecoEvent.Builder(heartRate)
                .setIndex(mHeartRateSeriesIndex)
                .setDuration(1750)
                .build());
    }

    private void flashCircleEvent(int heartBeat) {
//        int animationTime = heartBeat / 60;
        Integer colorFrom = Color.parseColor("#FFFFFFFF");
        Integer colorTo = Color.parseColor("#FFE91E63");
        colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(800);
        colorAnimation.setRepeatCount(99); //TODO change from 99
        colorAnimation.setRepeatMode(2);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                createAnimationSeries((Integer) animator.getAnimatedValue());
            }

        });
        colorAnimation.start();
    }

    private void countUp(int start, int end) {
        ValueAnimator animator = new ValueAnimator();
        animator.setObjectValues(start, end);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                mHeartRateText.setText(String.valueOf(animation.getAnimatedValue()) + "%");
            }
        });
        animator.setEvaluator(new TypeEvaluator<Integer>() {
            public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
                return Math.round(startValue + (endValue - startValue) * fraction);
            }
        });
        animator.setDuration(500);
        animator.start();
    }

    class HeartRateSensor implements SensorEventListener {

        private int dataPoints = 0;

        @Override
        public void onAccuracyChanged(Sensor s, int i) {
        }

        @Override
        public void onSensorChanged(SensorEvent se) {
            if (dataPoints == 6) {
                mSensorManager.unregisterListener(heartRateSensor);
                lastHeartRates.remove(0);
                lastHeartRates.remove(1);
                Collections.sort(lastHeartRates);
                float averageHeartRate = (lastHeartRates.get(1) + lastHeartRates.get(2)) / 2;
                try {
                    musicPlayer.createPulsePlaylist(averageHeartRate);
                } catch (EchoNestException e) {
                    Log.e("EchoNest", e.getMessage());
                }
                creatingPlaylistView.setVisibility(View.VISIBLE);
                progressView.startAnimation();

                colorAnimation.end();
                mHeartRateText.setTextSize(70);
                mHeartRateText.setTypeface(null, Typeface.NORMAL);
                mHeartRateText.setText(String.valueOf(averageHeartRate));
                getActivity().findViewById(R.id.heartRateLabel).setVisibility(View.VISIBLE);
                createBackSeries();
                createDataSeries();
                showHeartRateCircleEvent(averageHeartRate);
            } else {
                if (se.sensor.getType() == Sensor.TYPE_HEART_RATE) {
                    if ((int) se.values[0] > 0) {
                        if (dataPoints == 1) {
                            mHeartRateText.setTextSize(60);
                            countUp(0, 20);
                        } else if (dataPoints == 2) {
                            countUp(20, 40);
                        } else if (dataPoints == 3) {
                            countUp(40, 60);
                        } else if (dataPoints == 4) {
                            countUp(60, 80);
                        } else if (dataPoints == 5) {
                            countUp(80, 100);
                        }
                        lastHeartRates.add((int) se.values[0]);
                        Log.d("Heart Rate", String.valueOf(se.values[0]));
                        dataPoints++;
                    }
                }
            }
        }
    }
}