package game.mmu.ac.snake;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;
import java.util.Random;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

class SnakeEngine extends SurfaceView implements Runnable {

    //Game thread for the main game loop
    private Thread thread = null;

    //Holds a reference to the activity
    private Context context;

    //For playing sound effects


    //For tracking movement heading
    public enum Heading {UP, RIGHT, DOWN, LEFT}

    //Start by heading to the right
    private Heading heading = Heading.RIGHT;

    //Hold the screen size in pixels
    private int screenX;
    private int screenY;

    //Length of the snake
    private int snakeLength;

    //Where is Bob object hiding?
    private int bobX;
    private int bobY;

    //The size in pixels of a snake segment
    private int blockSize;

    //The size in segments of the playable area
    private final int NUM_BLOCKS_WIDE = 40;
    private int numBlocksHigh;

    //Control pausing between updates
    private long nextFrameTime;

    //Update the game 10 times per second
    private final long FPS = 10;

    //There are 1000 ms in a second
    private final long MILLIS_PER_SECOND = 1000;

    //How many points each player has
    private int score;

    //The location in the grid of all the segments
    private int[] snakeXs;
    private int[] snakeYs;

    //Everything we need for drawing

    //Is the game currently playing?
    private volatile boolean isPlaying;

    //A canvas for our paint
    private Canvas canvas;

    //Require to use canvas
    private SurfaceHolder surfaceHolder;

    //Some paint for our canvas
    private Paint paint;

    public SnakeEngine(Context context, Point size) {
        super(context);

        context = context;

        screenX = size.x;
        screenY = size.y;

        //Work out how many pixels each block is
        blockSize = screenX / NUM_BLOCKS_WIDE;

        //How many block of the same size will fit into the height
        numBlocksHigh = screenY / blockSize;

        //Set the sound up HERE

        //Initialise the drawing objects
        surfaceHolder = getHolder();
        paint = new Paint();

        //If you score 2-- you are rewarded with a crash achievement
        snakeXs = new int[200];
        snakeYs = new int[200];

        //start the game
        newGame();
    }

    @Override
    public void run() {

        while (isPlaying) {

            //Update 10 times a second
            if (updateRequired()) {
                update();
                draw();
            }
        }
    }

    public void pause() {
        isPlaying = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            //Error
        }
    }

    public void resume() {
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }

    public void newGame() {

        //Start with a single snake segment.
        snakeLength = 1;
        snakeXs[0] = NUM_BLOCKS_WIDE / 2;
        snakeYs[0] = numBlocksHigh / 2;

        //Get Bob object ready to be consumed by snake.
        spawnBob();

        //Reset the score
        score = 0;

        //setup the nextFrameTime so an update is triggered.
        //This will call update and draw methods.
        nextFrameTime = System.currentTimeMillis();
    }

    public void spawnBob() {
        Random random = new Random();
        bobX = random.nextInt(NUM_BLOCKS_WIDE - 1) + 1;
        bobY = random.nextInt(numBlocksHigh - 1) + 1;
    }

    private void eatBob() {
        //When Bob is caught, increase the size of the snake.
        snakeLength++;
        //replace Bob
        spawnBob();
        //add to the score
        score = score + 1;
        //add more sound HERE
    }

    private void moveSnake() {
        //Move the body
        for (int i = snakeLength; i >0; i--) {
            //Start at the back and move it to the position of the segment in front of it.
            snakeXs[i] = snakeXs[i-1];
            snakeYs[i] = snakeYs[i-1];

            //Exclude the ehad because the head has nothing infront of it.

            //Move the head in the appropriate heading.
            switch(heading) {
                case UP:
                snakeYs[0]--;
                break;

                case RIGHT:
                    snakeXs[0]++;

                case DOWN:
                    snakeYs[0]++;
                    break;

                case LEFT:
                    snakeXs[0]--;
                    break;
            }
        }
    }
    private boolean detectDeath() {

        //Has the snake died?
        boolean dead = false;

        //Hit the screen edge
        if (snakeXs[0] == -1) dead = true;
        if (snakeXs[0] >= NUM_BLOCKS_WIDE) dead = true;
        if (snakeYs[0] == -1) dead = true;
        if (snakeYs[0] == numBlocksHigh) dead = true;

        // Eaten itself?
        for (int i = snakeLength - 1; i > 0; i--) {
            if ((i > 4) && (snakeXs[0] == snakeXs[i]) && (snakeYs[0] == snakeYs[i])) {
                dead = true;
            }
        }
        return dead;
    }
    public void update() {

        //Did snake eat Bob?
        if(snakeXs[0] == bobX && snakeYs[0] == bobY) {
            eatBob();
        }

        moveSnake();

        if (detectDeath()) {
            //start again
            //ADD Music here

            newGame();
        }
    }
    public void draw() {

        //Geta lock on the canvas
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();

            //Fill the screen with Black
            canvas.drawColor(Color.argb(0, 0, 0, 0));

            // Set the color of the paint to draw the snake white
            paint.setColor(Color.argb(255, 255, 255, 255));

            // Scale the HUD text
            paint.setTextSize(90);
            canvas.drawText("Score:" + score, 10, 70, paint);

            // Draw the snake one block at a time
            for (int i = 0; i < snakeLength; i++) {
                canvas.drawRect(snakeXs[i] * blockSize,
                        (snakeYs[i] * blockSize),
                        (snakeXs[i] * blockSize) + blockSize,
                        (snakeYs[i] * blockSize) + blockSize,
                        paint);

                // Set the color of Bob as red
                paint.setColor(Color.argb(255, 255, 0, 0));

                // Draw Bob
                canvas.drawRect(bobX * blockSize,
                        (bobY * blockSize),
                        (bobX * blockSize) + blockSize,
                        (bobY * blockSize) + blockSize,
                        paint);

                // Unlock the canvas and reveal the graphics for this frame
                surfaceHolder.unlockCanvasAndPost(canvas);
            }

        }
    }
    public boolean updateRequired() {

        //Do we need to update the frame?
        if(nextFrameTime <= System.currentTimeMillis()) {
            //Tenth of a second has passed

            //When the update is triggered, this will be set up.
            nextFrameTime = System.currentTimeMillis()+ MILLIS_PER_SECOND/FPS;

            //Return true so that the update and draw functions are executed.
            return true;
        }
        return false;
    }
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                if (motionEvent.getX() >= screenX / 2) {
                    switch (heading) {
                        case UP:
                            heading = Heading.RIGHT;
                            break;
                        case RIGHT:
                            heading = Heading.DOWN;
                            break;
                        case DOWN:
                            heading = Heading.LEFT;
                            break;
                        case LEFT:
                            heading = Heading.UP;
                            break;

                    }
                } else {
                    switch (heading) {
                        case UP:
                            heading = Heading.LEFT;
                            break;
                        case LEFT:
                            heading = Heading.DOWN;
                        case DOWN:
                            heading = Heading.RIGHT;
                        case RIGHT:
                            heading = Heading.UP;
                            break;
                    }
                }
        }
        return true;
    }
}


