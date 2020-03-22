package com.example.infection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.infection.utils.InfectedStateManager;
import com.example.infection.utils.TaskManager;

/**
 * The main activity of the application. The UI has one main view, of which is a textview, detailing the state (infected, or clean) of the player.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * A member variable used when the user clicks on the textview. If they click this amount of times they self infect.
     */
    private int clicksBeforeInfected = 5;

    /**
     * A dynamically defined local broadcast receiver, used for updating the UI.
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUi();
        }
    };

    /**
     * Used to update the UI based on changes.
     * @param hasFocus
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            updateUi();
        }
    }

    /**
     * Sets up a local broadcast receiver and starts the tasker.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Sets up local broadcast receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(BuildConfig.APPLICATION_ID));

        // Tasks work, this involves setting up a network service and polling for other services.
        TaskManager taskManager = new TaskManager();
        taskManager.initialiseWork(getApplicationContext());
        //taskManager.run(getApplicationContext());

    }

    // Makes the application view full screen.
    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    /**
     * Sets the UI to the respective colour for the specific state the device is in (Infected / or clean).
     */
    public void updateUi() {
        hideSystemUI();
        setContentView(R.layout.activity_main);

        // Gets current state
        //todo is the null here why.
        InfectedStateManager infectedStateManager = new InfectedStateManager(MainActivity.this, null, true);
        final String state = infectedStateManager.getCurrentState();

        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(state.toUpperCase());

        // Uses state to update UI components
        if (state.equals(getString(R.string.clean_tag))) {
            textView.setTextColor(Color.GREEN);
            textView.setTextSize(80);
            getWindow().getDecorView().setBackgroundColor(Color.WHITE);
        } else if (state.equals(getString(R.string.infected_tag))){
            textView.setTextColor(Color.RED);
            textView.setTextSize(60);
            getWindow().getDecorView().setBackgroundColor(Color.BLACK);
        }

        // Sets up an on click listener for the text view so that people can self-infect.
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Only runs the below if the user isn't already infected.
                if (!state.equals(getString(R.string.infected_tag))) {

                    // decreases a value, giving the user an option to stop self-infecting.
                    if (clicksBeforeInfected > 1) {
                        clicksBeforeInfected = clicksBeforeInfected - 1;

                        if (clicksBeforeInfected == 1) {
                            Toast.makeText(getApplicationContext(), clicksBeforeInfected + getString(R.string.self_infect_one), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), clicksBeforeInfected + getString(R.string.self_infect_multiple), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        new InfectedStateManager(MainActivity.this, getString(R.string.infected_tag), false);
                        Toast.makeText(getApplicationContext(), getString(R.string.now_infected), Toast.LENGTH_SHORT).show();
                    }
                } else {

                    // If cure is to happen in future let them know. Otherwise cure them.
                    if (InfectedStateManager.isCured(getApplicationContext())) {
                        new InfectedStateManager(MainActivity.this, getString(R.string.clean_tag), true);
                        Toast.makeText(getApplicationContext(), getString(R.string.recoup_message), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.wait_message) + " " + InfectedStateManager.getCuredDate(getApplicationContext()) + ".", Toast.LENGTH_SHORT).show();

                    }
                }
            }
        });

    }

}


