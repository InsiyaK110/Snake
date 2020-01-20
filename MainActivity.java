package game.mmu.ac.snake;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;

public class MainActivity extends Activity {

    //Declare an instance of SnakeEngine
    SnakeEngine snakeEngine;

    @Override
    //onCreate uses the Display class of an object of type Point to get the resolution of the device the game is running on.
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Gets the pixel dimensions of the screen
        Display display = getWindowManager().getDefaultDisplay();

        //Initialise the result into a point object
        Point size = new Point();
        display.getSize(size);

        //Create a new instance of the SnakeEngine class
        snakeEngine = new SnakeEngine(this, size);

        //Make snakeEngine the view of the Activity
        setContentView(snakeEngine);
    }
    //Start the thread in snakeEngine
    @Override
    protected void onResume() {
        super.onResume();
        snakeEngine.resume();
        }

        //Stop the thread in snakeEngine
    @Override
    protected void onPause() {
        super.onPause();
        snakeEngine.pause();


    }

}


