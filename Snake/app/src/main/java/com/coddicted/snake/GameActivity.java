package com.coddicted.snake;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

public class GameActivity extends Activity {

    private static String TAG = "GameActivity";
    Canvas canvas;
    SnakeView snakeView;

    // Bitmaps for current view
    Bitmap currentHeadBitmap;
    Bitmap currentBodyBitmap;
    Bitmap currentTailBitmap;

    // Overall Bitmaps
    Bitmap headRightBitmap;
    Bitmap headLeftBitmap;
    Bitmap headUpBitmap;
    Bitmap headDownBitmap;
    Bitmap bodyHorizontalBitmap;
    Bitmap bodyVerticalBitmap;
    Bitmap tailRightBitmap;
    Bitmap tailLeftBitmap;
    Bitmap tailUpBitmap;
    Bitmap tailDownBitmap;
    Bitmap bodyCorner1Bitmap;
    Bitmap bodyCorner2Bitmap;
    Bitmap bodyCorner3Bitmap;
    Bitmap bodyCorner4Bitmap;

    Bitmap appleBitmap;

    // Variables for saving the High Score
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    String dataName = "SnakeGameData";
    String stringHiScoreKey = "SnakeHiScore";
    int defaultHiScore = 0;
    int currentHiScore = 0;

    //Sound
    //initialize sound variables
    private SoundPool soundPool;
    int sample1 = -1;
    int sample2 = -1;
    int sample3 = -1;
    int sample4 = -1;

    //for snake movement
    int previousDirection=0;
    int directionOfTravel=0;
    //0 = up, 1 = right, 2 = down, 3= left


    int screenWidth;
    int screenHeight;
    int topGap;

    //stats
    long lastFrameTime;
    int fps;
    int score;

    //Game objects
    int [] snakeX;
    int [] snakeY;
    // to hold direction of each body part
    int [] snakeDirections;
    int snakeLength;
    int appleX;
    int appleY;

    //The size in pixels of a place on the game board
    int blockSize;
    int numBlocksWide;
    int numBlocksHigh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadSound();
        configureDisplay();
        snakeView = new SnakeView(this);
        setContentView(snakeView);
    }

    class SnakeView extends SurfaceView implements Runnable {
        Thread ourThread = null;
        SurfaceHolder ourHolder;
        volatile boolean playingSnake;
        Paint paint;

        public SnakeView(Context context) {
            super(context);
            ourHolder = getHolder();
            paint = new Paint();

            //Even my 9 year old play tester couldn't
            //get a snake this long
            snakeX = new int[200];
            snakeY = new int[200];
            snakeDirections = new int[200];

            //our starting snake
            getSnake();
            //get an apple to munch
            getApple();
            // get High score
            getHighScore();
        }

        public void getHighScore() {
            preferences = getSharedPreferences(dataName, MODE_PRIVATE);
            editor = preferences.edit();

            // Load the high score
            currentHiScore = preferences.getInt(stringHiScoreKey, defaultHiScore);
        }

        public void getSnake(){
            snakeLength = 3;
            //start snake head in the middle of screen
            snakeX[0] = numBlocksWide / 2;
            snakeY[0] = numBlocksHigh / 2;
            snakeDirections[0] = directionOfTravel;

            //Then the body
            snakeX[1] = snakeX[0]-1;
            snakeY[1] = snakeY[0];
            snakeDirections[1] = directionOfTravel;

            //And the tail
            snakeX[2] = snakeX[1]-1;
            snakeY[2] = snakeY[1];
            snakeDirections[2] = directionOfTravel;
        }

        public void getApple(){
            Random random = new Random();
            appleX = random.nextInt(numBlocksWide-1)+1;
            appleY = random.nextInt(numBlocksHigh-1)+1;
        }

        @Override
        public void run() {
            while (playingSnake) {
                updateGame();
                drawGame();
                controlFPS();

            }

        }

        public void updateGame() {

            //Did the player get the apple
            if(snakeX[0] == appleX && snakeY[0] == appleY){
                //grow the snake
                snakeLength++;
                //replace the apple
                getApple();
                //add to the score
                score = score + snakeLength;
                soundPool.play(sample1, 1, 1, 0, 0, 1);
            }

            //move the body - starting at the back
            for(int i=snakeLength; i >0 ; i--){
                snakeX[i] = snakeX[i-1];
                snakeY[i] = snakeY[i-1];
                snakeDirections[i] = snakeDirections[i-1];
            }

            //Move the head in the appropriate direction
            switch (directionOfTravel){
                case 0://up
                    snakeY[0]  --;
                    break;

                case 1://right
                    snakeX[0] ++;
                    break;

                case 2://down
                    snakeY[0] ++;
                    break;

                case 3://left
                    snakeX[0] --;
                    break;
            }

            //Have we had an accident
            boolean dead = false;
            //with a wall
            if(snakeX[0] == -1)dead=true;
            if(snakeX[0] >= numBlocksWide) dead = true;
            if(snakeY[0] == -1)dead=true;
            if(snakeY[0] >= numBlocksHigh) dead = true;
            //or eaten ourselves?
            for (int i = snakeLength-1; i > 0; i--) {
                if ((i > 4) && (snakeX[0] == snakeX[i]) && (snakeY[0] == snakeY[i])) {
                    dead = true;
                }
            }


            if(dead){
                //start again
                soundPool.play(sample4, 1, 1, 0, 0, 1);
                score = 0;
                getSnake();

            }

            if (score > currentHiScore){
                currentHiScore = score;
                editor.putInt(stringHiScoreKey, currentHiScore);
                editor.commit();
            }

        }

        public void drawGame() {

            if (ourHolder.getSurface().isValid()) {
                canvas = ourHolder.lockCanvas();
                //Paint paint = new Paint();
                canvas.drawColor(Color.BLACK);//the background
                paint.setColor(Color.argb(255, 255, 255, 255));
                paint.setTextSize(topGap/2);
                canvas.drawText("Score:" + score + "  Hi:" + currentHiScore, 10, topGap-6, paint);

                //draw a border - 4 lines, top right, bottom , left
                paint.setStrokeWidth(3);//3 pixel border
                // top line
                canvas.drawLine(1,topGap,screenWidth-1,topGap,paint);
                // Right edge
                //Log.i("INFO", "Right line end stopY " + (topGap+(numBlocksHigh*blockSize)));
                canvas.drawLine(screenWidth-1,topGap,screenWidth-1,topGap+(numBlocksHigh*blockSize),paint);
                // Bottom Line
                canvas.drawLine(1,topGap+(numBlocksHigh*blockSize),
                        screenWidth-1, topGap+(numBlocksHigh*blockSize),paint);
                // Left edge
                canvas.drawLine(1,topGap, 1,topGap+(numBlocksHigh*blockSize), paint);

                //Draw the snake
                determineHeadBitmap();
                canvas.drawBitmap(currentHeadBitmap, snakeX[0]*blockSize, (snakeY[0]*blockSize)+topGap, paint);
                //Draw the body
                for(int i = 1; i < snakeLength-1;i++){
                    determineBodyBitmap(i);
                    canvas.drawBitmap(currentBodyBitmap, snakeX[i]*blockSize, (snakeY[i]*blockSize)+topGap, paint);
                }
                //draw the tail
                determineTailBitmap();
                canvas.drawBitmap(currentTailBitmap, snakeX[snakeLength-1]*blockSize, (snakeY[snakeLength-1]*blockSize)+topGap, paint);

                //draw the apple
                canvas.drawBitmap(appleBitmap, appleX*blockSize, (appleY*blockSize)+topGap, paint);

                ourHolder.unlockCanvasAndPost(canvas);
            }

        }

        private void determineTailBitmap() {
            // Determine the current Tail bitmap to be drawn based on
            // direction of travel
            switch (snakeDirections[snakeLength]){
                case 0://up
                    currentTailBitmap = tailUpBitmap;
                    break;
                case 1://right
                    currentTailBitmap  = tailRightBitmap;
                    break;
                case 2://down
                    currentTailBitmap  = tailDownBitmap;
                    break;
                case 3://left
                    currentTailBitmap  = tailLeftBitmap;
                    break;
            }
        }

        private void determineHeadBitmap() {
            // Determine the current Head bitmap to be drawn based on
            // direction of travel
            switch (directionOfTravel){
                case 0://up
                    currentHeadBitmap = headUpBitmap;
                    break;
                case 1://right
                    currentHeadBitmap = headRightBitmap;
                    break;
                case 2://down
                    currentHeadBitmap = headDownBitmap;
                    break;
                case 3://left
                    currentHeadBitmap = headLeftBitmap;
                    break;
            }
        }

        private void determineBodyBitmap(int i) {
            // Determine the current body bitmap to be drawn based on
            // direction of travel
            if(i < snakeLength && snakeDirections[i] != snakeDirections[i+1]) {
                currentBodyBitmap = getCornerBodyBitmap(i);
            } else {
                switch (snakeDirections[i]){
                    case 0://up
                    case 2://down
                        currentBodyBitmap = bodyVerticalBitmap;
                        break;

                    case 1://right
                    case 3://left
                        currentBodyBitmap = bodyHorizontalBitmap;
                        break;
                }
            }
        }

        private Bitmap getCornerBodyBitmap(int i) {
            Bitmap cornerBitmap = null;
            Log.i(TAG, "i: " + i + " snakeDirection[i]:" + snakeDirections[i] +
                    " snakeDirections[i+1]" + snakeDirections[i+1]);
            int ix = i + 1;

            switch (snakeDirections[i]) {
                case 0: // current direction is up
                    if(snakeDirections[ix] == 1) {
                        // previous direction was right
                        cornerBitmap = bodyCorner1Bitmap;
                    } else if(snakeDirections[ix] == 3) {
                        // previous direction was left
                        cornerBitmap = bodyCorner3Bitmap;
                    }
                    break;
                case 1: // current direction is right
                    if(snakeDirections[ix] == 0) {
                        // previous direction was up
                        cornerBitmap = bodyCorner4Bitmap;
                    } else if(snakeDirections[ix] == 2) {
                        // previous direction was down
                        cornerBitmap = bodyCorner3Bitmap;
                    }
                    break;
                case 2: // current direction is down
                    if(snakeDirections[ix] == 1) {
                        // previous direction was right
                        cornerBitmap = bodyCorner2Bitmap;
                    } else if(snakeDirections[ix] == 3) {
                        // previous direction was left
                        cornerBitmap = bodyCorner4Bitmap;
                    }
                    break;
                case 3: // current direction is left
                    if(snakeDirections[ix] == 0) {
                        // previous direction was up
                        cornerBitmap = bodyCorner2Bitmap;
                    } else if(snakeDirections[ix] == 2) {
                        // previous direction was down
                        cornerBitmap = bodyCorner1Bitmap;
                    }
                    break;

            }
            return cornerBitmap;
        }

        public void controlFPS() {
            long timeThisFrame = (System.currentTimeMillis() - lastFrameTime);
            long timeToSleep = 500 - timeThisFrame;
            if (timeThisFrame > 0) {
                fps = (int) (1000 / timeThisFrame);
            }
            if (timeToSleep > 0) {

                try {
                    ourThread.sleep(timeToSleep);
                } catch (InterruptedException e) {
                }

            }

            lastFrameTime = System.currentTimeMillis();
        }

        public void pause() {
            playingSnake = false;
            try {
                ourThread.join();
            } catch (InterruptedException e) {
            }

        }

        public void resume() {
            playingSnake = true;
            ourThread = new Thread(this);
            ourThread.start();
        }

        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {

            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_UP:
                    previousDirection = directionOfTravel;
                    if (motionEvent.getX() >= screenWidth / 2) {
                        //turn right
                        directionOfTravel ++;
                        //no such direction
                        if(directionOfTravel == 4)
                            //loop back to 0(up)
                            directionOfTravel = 0;
                    }
                    else {
                        //turn left
                        directionOfTravel--;
                        if(directionOfTravel == -1) {//no such direction
                            //loop back to 3(left)
                            directionOfTravel = 3;
                        }
                    }
            }
            // Change snake direction for rest of body
            snakeDirections[0] = directionOfTravel;
            return true;
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        while (true) {
            snakeView.pause();
            break;
        }

        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        snakeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        snakeView.pause();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            snakeView.pause();

            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
            return true;
        }
        return false;
    }

    public void loadSound(){
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        try {
            //Create objects of the 2 required classes
            AssetManager assetManager = getAssets();
            AssetFileDescriptor descriptor;

            //create our three fx in memory ready for use
            descriptor = assetManager.openFd("sample1.ogg");
            sample1 = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("sample2.ogg");
            sample2 = soundPool.load(descriptor, 0);


            descriptor = assetManager.openFd("sample3.ogg");
            sample3 = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("sample4.ogg");
            sample4 = soundPool.load(descriptor, 0);


        } catch (IOException e) {
            //Print an error message to the console
            Log.e("error", "failed to load sound files", e);
        }
    }

    public void configureDisplay(){
        //find out the width and height of the screen
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        Log.i("INFO", "screenHeight " + screenHeight);
        topGap = screenHeight/14;
        Log.i("INFO", "topGap " + topGap);
        //Determine the size of each block/place on the game board
        blockSize = screenWidth/25;
        Log.i("INFO", "blockSize " + blockSize);

        //Determine how many game blocks will fit into the
        //height and width
        //Leave one block for the score at the top
        numBlocksWide = 25;
        numBlocksHigh = ((screenHeight - topGap - numBlocksWide ))/blockSize;
        Log.i("INFO", "numBlockHigh " + numBlocksHigh);

        /* Load the bitmaps */
        // head
        headRightBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.head_right);
        headLeftBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.head_left);
        headUpBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.head_up);
        headDownBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.head_down);
        // body
        bodyHorizontalBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.body_horizontal);
        bodyVerticalBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.body_vertical);
        bodyCorner1Bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.body_corner1);
        bodyCorner2Bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.body_corner2);
        bodyCorner3Bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.body_corner3);
        bodyCorner4Bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.body_corner4);
        // tail
        tailRightBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.tail_right);
        tailLeftBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.tail_left);
        tailUpBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.tail_up);
        tailDownBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.tail_down);
        // apple
        appleBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.apple);

        /* scale the bitmaps to match the block size */
        // head
        headRightBitmap = Bitmap.createScaledBitmap(headRightBitmap, blockSize, blockSize, false);
        headLeftBitmap = Bitmap.createScaledBitmap(headLeftBitmap, blockSize, blockSize, false);
        headUpBitmap = Bitmap.createScaledBitmap(headUpBitmap, blockSize, blockSize, false);
        headDownBitmap = Bitmap.createScaledBitmap(headDownBitmap, blockSize, blockSize, false);
        // body
        bodyHorizontalBitmap = Bitmap.createScaledBitmap(bodyHorizontalBitmap, blockSize, blockSize, false);
        bodyVerticalBitmap = Bitmap.createScaledBitmap(bodyVerticalBitmap, blockSize, blockSize, false);
        bodyCorner1Bitmap = Bitmap.createScaledBitmap(bodyCorner1Bitmap, blockSize, blockSize, false);
        bodyCorner2Bitmap = Bitmap.createScaledBitmap(bodyCorner2Bitmap, blockSize, blockSize, false);
        bodyCorner3Bitmap = Bitmap.createScaledBitmap(bodyCorner3Bitmap, blockSize, blockSize, false);
        bodyCorner4Bitmap = Bitmap.createScaledBitmap(bodyCorner4Bitmap, blockSize, blockSize, false);

        // tail
        tailRightBitmap = Bitmap.createScaledBitmap(tailRightBitmap, blockSize, blockSize, false);
        tailLeftBitmap = Bitmap.createScaledBitmap(tailLeftBitmap, blockSize, blockSize, false);
        tailUpBitmap = Bitmap.createScaledBitmap(tailUpBitmap, blockSize, blockSize, false);
        tailDownBitmap = Bitmap.createScaledBitmap(tailDownBitmap, blockSize, blockSize, false);
        // apple
        appleBitmap = Bitmap.createScaledBitmap(appleBitmap, blockSize, blockSize, false);

    }

}
