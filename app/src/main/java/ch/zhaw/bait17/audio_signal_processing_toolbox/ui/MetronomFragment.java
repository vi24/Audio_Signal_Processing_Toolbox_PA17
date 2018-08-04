package ch.zhaw.bait17.audio_signal_processing_toolbox.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.lantouzi.wheelview.WheelView;

import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ch.zhaw.bait17.audio_signal_processing_toolbox.R;
import ch.zhaw.bait17.audio_signal_processing_toolbox.beat_detection.Algorithm9Double;
import ch.zhaw.bait17.audio_signal_processing_toolbox.beat_detection.MyObserver;
import ch.zhaw.bait17.audio_signal_processing_toolbox.metronome.AsyncMP3Generator;
import ch.zhaw.bait17.audio_signal_processing_toolbox.metronome.Metronome;



public class MetronomFragment extends Fragment implements MyObserver {

    private static final String TAG = "METRONOM_FRAGMENT";
    private static final String TEXT_VIEW_STATE_KEY = "textView";
    private static final HashMap<String, String> STATE = new HashMap<>();

    private View rootView;
    private Algorithm9Double algorithm9;
    private static final int WRITE_EXTERNAL_CALLBACK_SUCCESSFUL = 1;
    private OnSelectFileListener listener;
    private Metronome metronome;
    private WheelView bpmWheelView;
    private WheelView loopWheelView;
    private final List<String> bpms = new ArrayList<>();
    private final List<String> loops = new ArrayList<>();
    private Context context;
    private String uri;
    private Integer offset = 0, sampleRate;
    private boolean isDetecting = false;


    @Override
    public void updateOnResult(HashMap<String, Integer> value) {
        Integer bpm = value.get(Algorithm9Double.BPM_KEY);
        offset = value.get(Algorithm9Double.OFFSET_KEY);
        sampleRate = value.get(Algorithm9Double.SAMPLE_RATE_KEY);
        Integer channels = value.get(Algorithm9Double.CHANNELS_KEY);
        isDetecting = false;
        Log.d(TAG, "received result:  bpm: " + bpm + " offset: " + offset);
        TextView beatView = (TextView) rootView.findViewById(R.id.textViewBeat);
        String text= "";
        for(String key : value.keySet()){
            if(value.get(key) == null){
                text = "Error occured. Try again\n";
            } else {
                text = text + key + ": " + value.get(key) + "\n";

            }
        }

        beatView.setText(text);
        STATE.put(TEXT_VIEW_STATE_KEY, text);

        try {
            metronome.setBPM(bpm);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(bpm != null){
            Toast.makeText(getActivity(), "BPM: " + String.valueOf(value), Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(getActivity(), "Error occcured. Please try again.", Toast.LENGTH_LONG).show();

        }
        //metronome.playMetronomeToSong();
    }


    @Override
    public void updateOnProgress(String value) {
        String text = "status: " + value;
        Log.d(TAG, "textViewTextUpdate:  " + text);

        TextView beatView = (TextView) rootView.findViewById(R.id.textViewBeat);
        beatView.setText(text);
        STATE.put(TEXT_VIEW_STATE_KEY, text);
    }



    public interface OnSelectFileListener {
        void onSelectFile();

    }

    private void startBeatDetection(String uri) {
        if (!isDetecting) {
            isDetecting = true;
            algorithm9 = new Algorithm9Double();
            algorithm9.registerObserver(this);
            algorithm9.execute(uri);
        } else {
            Toast.makeText(getActivity(), "detection already running", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.i("info", "OnCreateViewOfMetronomGotCalled");
        rootView = inflater.inflate(R.layout.metronom_view, container, false);

        TextView beatView = (TextView) rootView.findViewById(R.id.textViewBeat);
        beatView.setText(STATE.get(TEXT_VIEW_STATE_KEY));


        bpmWheelView = (WheelView) rootView.findViewById(R.id.wheelview1);
        for (int i = 60; i <= 240; i++) {
            bpms.add(String.valueOf(i));
        }
        bpmWheelView.setItems(bpms);
        bpmWheelView.selectIndex(60);

        loopWheelView = (WheelView) rootView.findViewById(R.id.wheelview2);
        for (int i = 1; i <= 100; i++) {
            loops.add(String.valueOf(i));
        }
        loopWheelView.setItems(loops);
        loopWheelView.selectIndex(3);

        metronome = Metronome.getInstance(getActivity());
        metronome.loadSoundPool();
        Bundle arguments = getArguments();
        if (arguments != null) {
            uri = arguments.getString("URL");
        }


        attachListener();

        return rootView;
    }

    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        // This method avoid to call super.onAttach(context) if I'm not using api 23 or more
        if (Build.VERSION.SDK_INT >= 23) {
            super.onAttach(context);
            onAttachToContext(context);
        }
    }

    /*
         * Deprecated on API 23
         * Use onAttachToContext instead
         */
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnSelectFileListener) activity;
            algorithm9 = new Algorithm9Double();
            algorithm9.registerObserver(this);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFragmentSendText");
        }
    }

    private void onAttachToContext(Context context) {
        this.context = context;
        Activity activity;
        if (context instanceof Activity) {
            activity = (Activity) context;
            listener = (MetronomFragment.OnSelectFileListener) activity;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement MediaListFragment.OnItemSelectedListener");
        }
    }



    private void attachListener() {
        Button button_play = (Button) rootView.findViewById(R.id.play_simple_metronome);
        Button button_stop = (Button) rootView.findViewById(R.id.stop_simple_metronome);
        Button button_select = (Button) rootView.findViewById(R.id.select_file);
        Button button_analyze = (Button) rootView.findViewById(R.id.analyze);
        Button button_lamemp3 = (Button) rootView.findViewById(R.id.generate_mp3);
        Button button_playToSong = (Button) rootView.findViewById(R.id.play_to_song);
        Button button_stopToSong = (Button) rootView.findViewById(R.id.stop_to_song);
        Button button_stopWithSong = (Button) rootView.findViewById(R.id.stop_with_song);
        Button button_playWithSong = (Button) rootView.findViewById(R.id.play_with_song);

        if (uri != null) {

            try {
                URI file_uri = new URI(uri);
                String path = file_uri.getPath();
                String filename = path.substring(path.lastIndexOf('/') + 1);
                filename = FilenameUtils.removeExtension(filename);
                TextView beatView = (TextView) rootView.findViewById(R.id.textViewBeat);
                beatView.setText("Selected: " + filename);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

        }

        algorithm9.registerObserver(this);


        button_play.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                int bpmIndex = bpmWheelView.getSelectedPosition();
                int loopIndex = loopWheelView.getSelectedPosition();
                Toast.makeText(getActivity(), String.valueOf(bpms.get(bpmIndex)), Toast.LENGTH_LONG).show();
                try {
                    metronome.setBPM(Integer.parseInt(bpms.get(bpmIndex)));
                    metronome.setLoops(Integer.parseInt(loops.get(loopIndex)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                metronome.playMetronomeToSong();
            }
        });

        button_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                metronome.stopMetronome();
            }
        });

        button_select.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                OnSelectFileListener fc = (OnSelectFileListener) getActivity();
                fc.onSelectFile();
            }
        });




        button_analyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(uri != null) {
                    startBeatDetection(uri);
                }else{
                    Toast.makeText(context,"File not selected yet", Toast.LENGTH_LONG).show();
                }
            }
        });


        button_lamemp3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int loop;
                int bpm = metronome.getBPM();

                if(uri != null && bpm != 0){
                    loop = metronome.calculateLoop(uri);
                    TextView beatView = (TextView) rootView.findViewById(R.id.textViewBeat);
                    if (bpm == 0) {
                        Toast.makeText(context, "BPM not set yet, analyze first!", Toast.LENGTH_LONG).show();

                    } else {
                        try {
                            AsyncMP3Generator asyncMP3Generator = AsyncMP3Generator.getInstance();
                            asyncMP3Generator.generateMp3File(bpm,context,offset,beatView, loop,uri);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                    Log.i(TAG, "Generated!");
                }else{
                    if(bpm == 0){
                        Toast.makeText(context,"Song not analyzed yet", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(context,"Song not selected yet", Toast.LENGTH_LONG).show();
                    }

                }


            }
        });

        button_playToSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int bpm = metronome.getBPM();
                try {
                    metronome.playMetronomeToSong(uri,bpm);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context,"Song wasn't analyzed yet!", Toast.LENGTH_LONG).show();
                }
            }
        });

        button_stopToSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                metronome.stopMetronome();
            }
        });

        button_playWithSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    try {
                        metronome.playMetronomeWithSong(uri,offset,sampleRate);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context,"Song not selected/analyzed yet!",Toast.LENGTH_LONG).show();
                    }
            }
        });

        button_stopWithSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                metronome.stopMetronomeWithSong();
            }
        });

    }


    @TargetApi(23)
    public void askForPermission() {
        if (ContextCompat.checkSelfPermission(this.getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_CALLBACK_SUCCESSFUL);


        }
    }

    @Override
    public void onResume() {
        metronome.loadSoundPool();
        super.onResume();
    }

    public void onPause() {
        metronome.releaseSoundPool();
        metronome.releaseMediaPlayer();
        super.onPause();
    }





}
