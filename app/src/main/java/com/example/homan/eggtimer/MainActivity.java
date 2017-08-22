package com.example.homan.eggtimer;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.homan.eggtimer.explosionfield.ExplosionField;

import static android.R.color.holo_green_light;
import static android.R.color.holo_red_light;
import static com.example.homan.eggtimer.R.drawable.boiling_egg;
import static com.example.homan.eggtimer.R.drawable.pause;
import static com.example.homan.eggtimer.R.drawable.playing;
import static com.example.homan.eggtimer.R.drawable.stop;
import static com.example.homan.eggtimer.R.id;
import static com.example.homan.eggtimer.R.layout;

public class MainActivity extends AppCompatActivity {

    SeekBar timerSeekBar;
    TextView seekBarStatusText;
    ImageView seekBarStatusRadio;

    //see bar status color
    int green = holo_green_light;
    int red = holo_red_light;

    //timer resume tracker
    long mmsLeft;

    TextView timerView;
    TextView ctrlMsg;
    MediaPlayer onGoing = new MediaPlayer();
    ImageView goButton;

    Boolean counterIsActive = false;
    boolean started;
    CountDownTimer countDownTimer;

    //global var: explosion field
    private ExplosionField explosionEgg;
    ImageButton btnEgg;

    //==================================================== END OF VARs

    //program reset
    public void resetTimer() {
        timerView.setText("0:05");
        timerSeekBar.setProgress(5);
        countDownTimer.cancel();
        ctrlMsg.setText("Again?");
        timerSeekBar.setEnabled(true);
        seekBarStatusRadio.setImageResource(android.R.drawable.presence_online);
        seekBarStatusText.setText("ON");
        seekBarStatusText.setTextColor(this.getResources().getColor(green));

        goButton.setImageResource(stop);
        started = false;

        Toast.makeText(MainActivity.this, "Reset", Toast.LENGTH_LONG).show();

        //reset to original graphic
        explosionEgg.clear();
        btnEgg.setScaleX(1f);
        btnEgg.setScaleY(1f);
        btnEgg.setAlpha(1f);
        btnEgg.setVisibility(View.VISIBLE);

    }


    //Timer update
    public void updateTimer(int secondsLeft) {
        int minutes = (int) secondsLeft / 60;
        int seconds = secondsLeft - minutes * 60;
        timerView.setText(Integer.toString(minutes)+":"+String.format("%02d", seconds));
        timerSeekBar.setProgress(secondsLeft);
    }

    //Explosion Sound Effect
    public void explosionSoundEffect() {
        //play effect sound
        try {
            //stop on going one
            onGoing.stop();

            //file:  assets/sound/crack_ice.wav
            MediaPlayer m = new MediaPlayer();
            AssetFileDescriptor descriptor = getAssets().openFd("sound/crack_ice.wav");
            m.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();

            m.prepare();
            m.setVolume(1f, 1f);
            m.setLooping(false);
            m.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startNewTimer(long mms) {
        //+100 made up by machine delay
        countDownTimer = new CountDownTimer(mms, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                mmsLeft = millisUntilFinished;
                updateTimer((int) millisUntilFinished / 1000);
                //stop music in 3s
                if (millisUntilFinished < 3000) {
                    onGoing.stop();
                    ctrlMsg.setText("Oh! NO...");
                }
            }

            @Override
            public void onFinish() {
                //stop the music
                onGoing.pause();

                //update time
                timerView.setText("0:00");

                //Log.i("tms finish ", "timer done");

                //sound
                explosionSoundEffect();
                //explode the egg
                explosionEgg.explode(btnEgg);

                //delay for explosion duration
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        resetTimer();
                    }
                }, 1500);

            }
        };
    }

    //Music for Count Down Timer
    public void playProcessMusic() {
        //Toast.makeText(MainActivity.this, "Check @ mms: "+String.valueOf(mmsLeft), Toast.LENGTH_LONG).show();

        //load music file only > 3s
        if (mmsLeft > 3000) {
            try {
                onGoing.reset();
                //sound file:  asset/sound/drum.mp3
                AssetFileDescriptor descriptor = getAssets().openFd("sound/drum.mp3");
                onGoing.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                descriptor.close();
                //settings:
                onGoing.prepare();
                onGoing.setVolume(1f, 1f);
                onGoing.setLooping(true);
                onGoing.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //Control Timer, buttons
    public void controlTimer(View view) throws InterruptedException {
        if (!started) {
            //redraw the egg if exploded before
            btnEgg.setImageResource(boiling_egg);

            mmsLeft = (long) timerSeekBar.getProgress() * 1000;

            //update status: new or old timer
            started = true;

            //start a new timer
            startNewTimer(timerSeekBar.getProgress() * 1000 + 100);
            countDownTimer.start();

            //update button image
            goButton.setImageResource(playing);

            //state control message
            ctrlMsg.setText("Playing");

            //turn on music
            //Log.i("tms music ", "==================== process");
            playProcessMusic();

            //lock down the seekbar after timer is on
            timerSeekBar.setEnabled(false);
            seekBarStatusRadio.setImageResource(android.R.drawable.ic_delete);
            seekBarStatusText.setTextColor(this.getResources().getColor(red));
            seekBarStatusText.setText("OFF");


        } else {
            //Toast.makeText(MainActivity.this, "Click @ mms: "+String.valueOf(mmsLeft), Toast.LENGTH_LONG).show();

            if(onGoing.isPlaying() ) {

                //Log.i("tms status ", " pause");

                //open the seekbar
                timerSeekBar.setEnabled(true);
                seekBarStatusRadio.setImageResource(android.R.drawable.presence_online);
                seekBarStatusText.setTextColor(this.getResources().getColor(green));
                seekBarStatusText.setText("ON");

                //pause timer
                countDownTimer.cancel();
                //update button
                goButton.setImageResource(pause);
                //update timer seek bar
                timerSeekBar.setProgress((int)mmsLeft/1000);
                //pause music
                onGoing.pause();
                //display message
                ctrlMsg.setText("Paused");
            } else {
                //Timer resume
                //Log.i("tms status ", " resume");

                //lock down the seekbar
                timerSeekBar.setEnabled(false);
                seekBarStatusRadio.setImageResource(android.R.drawable.ic_delete);
                seekBarStatusText.setTextColor(this.getResources().getColor(red));
                seekBarStatusText.setText("OFF");

                //resume time and start a new count down
                startNewTimer(mmsLeft);
                countDownTimer.start();

                //update button
                goButton.setImageResource(playing);
                //continue progress
                onGoing.start();
                //display message
                ctrlMsg.setText("Playing");
            } //end if else isPlaying

        } //end if else started
    } //end controlTimer

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);

        //============================================================ ini vars
        //for go button display
        goButton = (ImageView) findViewById(id.goButton);
        goButton.setImageResource(stop);
        //new or not new
        started = false;
        //explosion effect
        explosionEgg = ExplosionField.attach2Window(this);

        btnEgg = (ImageButton) findViewById(R.id.btnEgg);
        btnEgg.setOnClickListener(new View.OnClickListener() {
            //boring shaking egg
            @Override
            public void onClick(View v) {
                Animation a = AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake);
                btnEgg.startAnimation(a);
            }
        });

            //initial message
        ctrlMsg = (TextView) findViewById(id.ctrlMsg);
        ctrlMsg.setText("Go ?");
        //seekbar control
        timerSeekBar = (SeekBar) findViewById(id.timerSeekBar);
        timerSeekBar.setMax(600);
        timerSeekBar.setProgress(5);
        timerSeekBar.setEnabled(true);

        //for radio button
        seekBarStatusRadio = (ImageView) findViewById(id.seekBarStatusRadio);
        seekBarStatusText = (TextView) findViewById(id.seekBarStatusText);

        //clock display
        timerView = (TextView) findViewById(id.timerView);
        timerView.setText("0:05");

        //============================================================

        //seekbar control
        timerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateTimer(progress);
                //user wants to change then change
                if(timerSeekBar.isEnabled()) {
                    mmsLeft = (long)progress*1000;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

}
