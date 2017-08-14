package com.coddicted.memorygame;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.Random;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    private SoundPool soundPool;
    int sample1 = -1;
    int sample2 = -1;
    int sample3 = -1;
    int sample4 = -1;

    TextView textScore;
    TextView textDifficulty;
    TextView textWatchGo;

    Button button1;
    Button button2;
    Button button3;
    Button button4;
    Button buttonReplay;

    int difficultyLevel = 3;
    int[] sequenceToCopy = new int[100];


    private Handler myHandler;
    boolean playSequence = false;
    int elementToPlay = 0;

    int playerResponses;
    int playerScore;
    boolean isResponding;

    Animation wobble;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        wobble = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.wobble);

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        try {
            AssetManager assetManager = getAssets();
            AssetFileDescriptor descriptor;

            descriptor = assetManager.openFd("sample1.ogg");
            sample1 = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("sample2.ogg");
            sample2 = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("sample3.ogg");
            sample3 = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("sample4.ogg");
            sample4 = soundPool.load(descriptor, 0);

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Reference all elements on the UI
        textScore = (TextView) findViewById(R.id.textScore);
        textScore.setText("Score: " + playerScore);
        textDifficulty = (TextView) findViewById(R.id.textDifficulty);

        textDifficulty.setText("Difficulty: " + difficultyLevel);
        textWatchGo = (TextView) findViewById(R.id.textWatchGo);

        // The buttons
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);
        buttonReplay = (Button) findViewById(R.id.buttonReplay);

        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);
        buttonReplay.setOnClickListener(this);

        myHandler = new Handler() {
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                if(playSequence){

                    //button1.setVisibility(View.VISIBLE);
                    //button2.setVisibility(View.VISIBLE);
                    //button3.setVisibility(View.VISIBLE);
                    //button4.setVisibility(View.VISIBLE);

                    switch (sequenceToCopy[elementToPlay]){
                        case 1:
                            //hide a button
                            //button1.setVisibility(View.INVISIBLE);
                            button1.startAnimation(wobble);
                            //play a sound
                            soundPool.play(sample1, 1, 1, 0, 0, 1);
                            break;

                        case 2:
                            //hide a button
                            //button2.setVisibility(View.INVISIBLE);
                            button2.startAnimation(wobble);
                            //play a sound
                            soundPool.play(sample2, 1, 1, 0, 0, 1);
                            break;

                        case 3:
                            //hide a button
                            //button3.setVisibility(View.INVISIBLE);
                            button3.startAnimation(wobble);
                            //play a sound
                            soundPool.play(sample3, 1, 1, 0, 0, 1);
                            break;

                        case 4:
                            //hide a button
                            //button4.setVisibility(View.INVISIBLE);
                            button4.startAnimation(wobble);
                            //play a sound
                            soundPool.play(sample4, 1, 1, 0, 0, 1);
                            break;
                    }

                    elementToPlay++;
                    if(elementToPlay == difficultyLevel){
                        sequenceFinished();
                    }
                }
                myHandler.sendEmptyMessageDelayed(0, 1000);
            }
        };

        myHandler.sendEmptyMessage(0);
        playASequence();
    }

    public void createSequence(){
        //For choosing a random button
        Random randInt = new Random();
        int ourRandom;
        for(int i = 0; i < difficultyLevel; i++){
            //get a random number between 1 and 4
            ourRandom = randInt.nextInt(4);
            ourRandom ++;//make sure it is not zero
            //Save that number to our array
            sequenceToCopy[i] = ourRandom;
        }

    }

    public void playASequence(){
        createSequence();
        isResponding = false;
        elementToPlay = 0;
        playerResponses = 0;
        textWatchGo.setText("WATCH!");
        playSequence = true;
    }

    public void sequenceFinished(){
        playSequence = false;

        //make sure all the buttons are made visible
//        button1.setVisibility(View.VISIBLE);
//        button2.setVisibility(View.VISIBLE);
//        button3.setVisibility(View.VISIBLE);
//        button4.setVisibility(View.VISIBLE);
        textWatchGo.setText("GO!");
        isResponding = true;
    }

    @Override
    public void onClick(View v) {
        if(!playSequence){
            switch (v.getId()){
                case R.id.button1:
                    soundPool.play(sample1, 1,1,0,0,1);
                    checkElement(1);
                    break;
                case R.id.button2:
                    //play a sound
                    soundPool.play(sample2, 1, 1, 0, 0, 1);
                    checkElement(2);
                    break;

                case R.id.button3:
                    //play a sound
                    soundPool.play(sample3, 1, 1, 0, 0, 1);
                    checkElement(3);
                    break;

                case R.id.button4:
                    //play a sound
                    soundPool.play(sample4, 1, 1, 0, 0, 1);
                    checkElement(4);
                    break;
                case R.id.buttonReplay:
                    difficultyLevel = 3;
                    playerScore = 0;
                    textScore.setText("Score: " + playerScore);
                    playASequence();
                    break;
            }
        }
    }

    public void checkElement(int thisElement) {

        if (isResponding) {
            playerResponses++;
            if (sequenceToCopy[playerResponses - 1] == thisElement) { //Correct
                playerScore = playerScore + ((thisElement + 1) * 2);
                textScore.setText("Score: " + playerScore);
                if (playerResponses == difficultyLevel) {//got the whole sequence
                    //don't checkElement anymore
                    isResponding = false;
                    //now raise the difficulty
                    difficultyLevel++;
                    //and play another sequence
                    playASequence();
                }

            } else {//wrong answer
                textWatchGo.setText("FAILED!");
                //don't checkElement anymore
                isResponding = false;
            }
        }
    }
}
