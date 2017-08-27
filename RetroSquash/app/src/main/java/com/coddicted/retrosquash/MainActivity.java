package com.coddicted.retrosquash;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.Random;

public class MainActivity extends Activity {

    Canvas canvas;
    SquashCourtView squashCourtView;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    String dataName = "RetroSquashData";
    String stringHiScoreKey = "RetroSquashHiScore";
    int defaultHiScore = 0;
    int currentHiScore = 0;

    /// Sound variables
    SoundPool soundPool;
    int sample1 = -1;
    int sample2 = -1;
    int sample3 = -1;
    int sample4 = -1;

    // Display details
    Display display;
    Point size;
    int screenWidth;
    int screenHeight;

    // Game Objects
    int racketWidth;
    int racketHeight;
    Point racketPosition;

    Point ballPosition;
    int ballWidth;

    // Ball movement
    boolean ballIsMovingLeft;
    boolean ballIsMovingRight;
    boolean ballIsMovingUp;
    boolean ballIsMovingDown;

    // For racket movement
    boolean racketIsMovingLeft;
    boolean racketIsMovingRight;

    // stats
    long lastFrameTime;
    int fps;
    int score;
    int lives;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        squashCourtView = new SquashCourtView(this);
        setContentView(squashCourtView);

        // Load sounds
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
            Log.e("ERROR ", "Unable to load sound files");
            e.printStackTrace();
        }

        // Get the screen size in pixels
        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        // game objects
        racketPosition = new Point();
        racketPosition.x = screenWidth / 2;
        racketPosition.y = screenHeight - 20;
        racketWidth = screenWidth / 8;
        racketHeight = 10;

        ballWidth = screenWidth / 35;
        ballPosition = new Point();
        ballPosition.x = screenWidth / 2;
        ballPosition.y = ballWidth + 1;

        lives = 3;

        preferences = getSharedPreferences(dataName, MODE_PRIVATE);
        editor = preferences.edit();

        // Load the high score
        currentHiScore = preferences.getInt(stringHiScoreKey, defaultHiScore);

    }

    @Override
    protected void onStop() {
        super.onStop();

        while (true){
            squashCourtView.pause();
            break;
        }
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        squashCourtView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        squashCourtView.resume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            squashCourtView.pause();
            finish();
            return true;
        }
        return false;
    }

    class SquashCourtView extends SurfaceView implements Runnable {

        Thread ourThread = null;
        SurfaceHolder ourHolder;
        volatile boolean playingSquash;
        Paint paint;

        public SquashCourtView(Context context) {
            super(context);

            ourHolder = getHolder();
            paint = new Paint();
            ballIsMovingDown = true;
            setRandomXDirectionOfBall();


        }

        private void setRandomXDirectionOfBall() {
            // send the ball in random direction
            Random randomNumber = new Random();
            int ballDirection = randomNumber.nextInt(3);
            switch (ballDirection){
                case 0:
                    ballIsMovingLeft = true;
                    ballIsMovingRight = false;
                    break;
                case 1:
                    ballIsMovingLeft = false;
                    ballIsMovingRight = true;
                    break;
                case 2:
                    ballIsMovingLeft = false;
                    ballIsMovingRight = false;
                    break;
            }
        }

        @Override
        public void run() {

            while (playingSquash){
                updateCourt();
                drawCourt();
                controlFPS();
            }
        }

        private void updateCourt() {
            if(racketIsMovingRight){
                if ((racketPosition.x + racketWidth / 2) < screenWidth) {
                    // improvisation on original code
                    // to stop the racket from moving beyond screen
                    racketPosition.x = racketPosition.x + 12;
                }
            }
            if(racketIsMovingLeft){
                if ((racketPosition.x - racketWidth / 2) > 0) {
                    racketPosition.x = racketPosition.x - 12;
                }
            }

            // detect collisions

            // ball hits the right of screen
            if(ballPosition.x + ballWidth > screenWidth) {
                ballIsMovingLeft = true;
                ballIsMovingRight = false;
                soundPool.play(sample1, 1, 1, 0, 0, 1);
            }

            // ball hits the left of screen
            if(ballPosition.x < 0){
                ballIsMovingLeft = false;
                ballIsMovingRight = true;
                soundPool.play(sample1, 1, 1, 0, 0, 1);
            }

            // Edge of ball has hit bottom of screen
            if (ballPosition.y > screenHeight - ballWidth){
                // decrease 1 life
                lives--;
                if(lives == 0){
                    // All lives exhausted. Reset the game
                    lives = 3;
                    score = 0;
                    soundPool.play(sample4, 1, 1, 0, 0, 1);
                }
                // send the ball back to top of screen
                ballPosition.y = 1 + ballWidth;

                //what horizontal direction should we use
                //for the next falling ball
                Random randomNumber = new Random();
                int startX = randomNumber.nextInt(screenWidth - ballWidth) + 1;
                ballPosition.x = startX + ballWidth;
                setRandomXDirectionOfBall();

            }

            // ball hits the top of the screen
            if (ballPosition.y <= 0){
                ballIsMovingDown = true;
                ballIsMovingUp = false;
                ballPosition.y = 1;
                soundPool.play(sample2, 1, 1, 0, 0, 1);
            }

            // adjust the X and Y positions based on
            // the two directions we should be moving
            if(ballIsMovingDown){
                ballPosition.y += 6;
            }
            if (ballIsMovingUp){
                ballPosition.y -= 10;
            }
            if (ballIsMovingLeft){
                ballPosition.x -= 12;
            }
            if (ballIsMovingRight){
                ballPosition.x += 12;
            }

            // has ball hit the racket
            if (ballPosition.y + ballWidth >= (racketPosition.y - racketHeight / 2)) {
                int halfRacket = racketWidth / 2;
                if (ballPosition.x + ballWidth > (racketPosition.x - halfRacket) &&
                        (ballPosition.x - ballWidth < (racketPosition.x + halfRacket))){
                    // rebound the ball vertically and play a sound
                    soundPool.play(sample3, 1, 1, 0, 0, 1);
                    score++;
                    ballIsMovingUp = true;
                    ballIsMovingDown = false;

                    // deciding how to rebound the ball horizontally
                    if(ballPosition.x > racketPosition.x){
                        ballIsMovingRight = true;
                        ballIsMovingLeft = false;
                    } else {
                        ballIsMovingRight = false;
                        ballIsMovingLeft = true;
                    }
                    if (score > currentHiScore){
                        currentHiScore = score;
                        editor.putInt(stringHiScoreKey, currentHiScore);
                        editor.commit();
                    }
                }
            }
        }

        private void drawCourt() {
            if (ourHolder.getSurface().isValid()){
                canvas = ourHolder.lockCanvas();
                canvas.drawColor(Color.BLACK); // background color
                paint.setColor(Color.argb(255, 255, 255, 255));
                paint.setTextSize(45);
                canvas.drawText("Score:" + score + " Lives:" + lives +
                        " High Score:" + currentHiScore + " fps:" + fps, 20, 40, paint);

                // draw the squash racket
                canvas.drawRect(racketPosition.x - (racketWidth/2), // left
                        racketPosition.y - (racketHeight/2),    // top
                        racketPosition.x + (racketWidth/2), // right
                        racketPosition.y + racketHeight,    // bottom
                        paint);

                // draw the ball
                canvas.drawRect(ballPosition.x, ballPosition.y,
                        ballPosition.x + ballWidth, ballPosition.y + ballWidth,
                        paint);

                ourHolder.unlockCanvasAndPost(canvas);
            }
        }

        private void controlFPS() {
            long timeThisFrame = System.currentTimeMillis() - lastFrameTime;
            long timeToSleep = 15 - timeThisFrame;
            if (timeThisFrame > 0){
                fps = (int) (1000 / timeThisFrame);
            }
            if (timeToSleep > 0){
                try {
                    ourThread.sleep(timeToSleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            lastFrameTime = System.currentTimeMillis();
        }

        public void pause() {
            playingSquash = false;
            try {
                ourThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void resume() {
            playingSquash = true;
            ourThread = new Thread(this);
            ourThread.start();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    if (event.getX() >= (screenWidth / 2)) {
                        racketIsMovingRight = true;
                        racketIsMovingLeft = false;
                    } else {
                        racketIsMovingLeft = true;
                        racketIsMovingRight = false;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    racketIsMovingRight = false;
                    racketIsMovingLeft = false;
                    break;
            }
            return true;
        }
    }

}
