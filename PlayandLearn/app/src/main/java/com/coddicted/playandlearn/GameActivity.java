package com.coddicted.playandlearn;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Random;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    int correctAnswer;
    int currentScore = 0;
    int currentLevel = 1;

    // variables for sound ids
    int successSound = -1;
    int failureSound = -1;
    int sample3 = -1;

    TextView textObjectPartA;
    TextView textObjectPartB;
    TextView textObjectScore;
    TextView textObjectLevel;

    Button buttonObjectChoice1;
    Button buttonObjectChoice2;
    Button buttonObjectChoice3;

    private SoundPool soundPool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Find all the views to interact with
        textObjectPartA = (TextView) findViewById(R.id.textPartA);
        textObjectPartB = (TextView) findViewById(R.id.textPartB);
        textObjectScore = (TextView) findViewById(R.id.textScore);
        textObjectLevel = (TextView) findViewById(R.id.textLevel);

        buttonObjectChoice1 = (Button) findViewById(R.id.buttonChoice1);
        buttonObjectChoice2 = (Button) findViewById(R.id.buttonChoice2);
        buttonObjectChoice3 = (Button) findViewById(R.id.buttonChoice3);

        buttonObjectChoice1.setOnClickListener(this);
        buttonObjectChoice2.setOnClickListener(this);
        buttonObjectChoice3.setOnClickListener(this);

        textObjectScore.setText("Score: " + currentScore);
        textObjectLevel.setText("Level: " + currentLevel);

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        try {
            AssetManager assetManager = getAssets();
            AssetFileDescriptor assetFileDescriptor;

            assetFileDescriptor = assetManager.openFd("success.ogg");
            successSound = soundPool.load(assetFileDescriptor, 0);

            assetFileDescriptor = assetManager.openFd("failure.ogg");
            failureSound = soundPool.load(assetFileDescriptor, 0);

            assetFileDescriptor = assetManager.openFd("sample3.ogg");
            sample3 = soundPool.load(assetFileDescriptor, 0);

        } catch (IOException e) {
            e.printStackTrace();
        }

        setQuestion();

    }

    @Override
    public void onClick(View v) {
        // Decide on the answer's correctness
        int answerGiven = 0;
        switch (v.getId()) {
            case R.id.buttonChoice1:
                answerGiven = Integer.parseInt(buttonObjectChoice1.getText() + "");
                break;
            case R.id.buttonChoice2:
                answerGiven = Integer.parseInt(buttonObjectChoice2.getText() + "");
                break;
            case R.id.buttonChoice3:
                answerGiven = Integer.parseInt(buttonObjectChoice3.getText() + "");
                break;
        }
        updateScoreAndLevel(answerGiven);
        setQuestion();
    }

    private void setQuestion(){
        // generate the parts of the question
        int numberRange = currentLevel * 3;
        Random randInt = new Random();

        int partA = randInt.nextInt(numberRange);
        partA++;    // Avoiding a 0 value

        int partB = randInt.nextInt(numberRange);
        partB++;

        correctAnswer = partA * partB;
        int wrongAnswer1 = correctAnswer - 2;
        int wrongAnswer2 = correctAnswer + 2;

        textObjectPartA.setText(partA + "");
        textObjectPartB.setText(partB + "");

        // set the mutli-choice question
        int buttonLayout = randInt.nextInt(3);

        switch (buttonLayout){
            case 0:
                buttonObjectChoice1.setText(correctAnswer + "");
                buttonObjectChoice2.setText(wrongAnswer1 + "");
                buttonObjectChoice3.setText(wrongAnswer2 + "");
                break;
            case 1:
                buttonObjectChoice2.setText(correctAnswer + "");
                buttonObjectChoice3.setText(wrongAnswer1 + "");
                buttonObjectChoice1.setText(wrongAnswer2 + "");
                break;
            case 2:
                buttonObjectChoice3.setText(correctAnswer + "");
                buttonObjectChoice1.setText(wrongAnswer1 + "");
                buttonObjectChoice2.setText(wrongAnswer2 + "");
                break;
        }
    }

    private void updateScoreAndLevel(int answerGiven){

        if(isCorrect(answerGiven)){
            for (int i = 1; i<=currentLevel; i++){
                currentScore += i;
            }
            currentLevel++;
        } else {
            currentScore = 0;
            currentLevel = 1;
        }
        // update the score and level views
        textObjectScore.setText("Score: " + currentScore);
        textObjectLevel.setText("Level: " + currentLevel);
    }

    private void toastFailure() {
        Log.d("FAILURE_TAG", "toastFailure being called");
        soundPool.play(failureSound, 1, 1, 0, 0, 1);
        Toast.makeText(getApplicationContext(), "Sorry!! That's wrong", Toast.LENGTH_LONG);
    }

    private void toastSuccess() {
        Log.d("SUCCESS_TAG", "toastSuccess being called");
        soundPool.play(successSound, 1, 1, 0, 0, 1);
        Toast.makeText(getApplicationContext(), "Nice!! That's correct", Toast.LENGTH_LONG);
    }

    private boolean isCorrect(int answerGiven) {
        boolean response = false;
        if(answerGiven == correctAnswer){
            toastSuccess();
            response = true;
        } else {
            toastFailure();
        }
        return response;
    }
}
