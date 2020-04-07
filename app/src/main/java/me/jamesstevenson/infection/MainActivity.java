package me.jamesstevenson.infection;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.text.SimpleDateFormat;
import java.util.Date;

import me.jamesstevenson.infection.utils.InfectedStateManager;
import me.jamesstevenson.infection.utils.MultipleUse;
import me.jamesstevenson.infection.utils.TaskManager;
import me.jamesstevenson.infection.wifip2p.DiscoverWifiP2PService;

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
    public BroadcastReceiver updateUiBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Updates this variable back to 5 when the UI gets updated.
            clicksBeforeInfected = 5;

            if (BuildConfig.DEBUG) {
                Log.v(BuildConfig.APPLICATION_ID, "Received Local Broadcast Receiver");
            }

            updateUi();
        }
    };

    /**
     * Used to update the UI based on changes.
     *
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
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Notification channel used throughout the app.
        MultipleUse.createNotificationChannel(getApplicationContext());

        //Sets up local broadcast receiver used for updating the UI.
        LocalBroadcastManager.getInstance(this).registerReceiver(updateUiBroadcastReceiver, new IntentFilter(BuildConfig.APPLICATION_ID));

        //required before using discovery
        DiscoverWifiP2PService discoverWifiP2PService = new DiscoverWifiP2PService(getApplicationContext());
        discoverWifiP2PService.setupDiscovery();

        TaskManager taskManager = new TaskManager();
        taskManager.startPeriodicWork(getApplicationContext());
        taskManager.run(getApplicationContext());
    }

    @Override
    protected void onStop() {
        super.onStop();
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
    @SuppressLint("ClickableViewAccessibility")
    public void updateUi() {
        hideSystemUI();
        setContentView(R.layout.activity_main);

        // Gets current state which is used for updating the Ui elements
        final InfectedStateManager infectedStateManager = new InfectedStateManager(getApplicationContext());
        final String state = InfectedStateManager.getCurrentState(getApplicationContext());

        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(state.toUpperCase());

        // Uses state to update UI components
        if (state.equals(getString(R.string.clean_tag))) {
            textView.setTextColor(Color.GREEN);
            textView.setTextSize(80);
            getWindow().getDecorView().setBackgroundColor(Color.WHITE);
        } else if (state.equals(getString(R.string.infected_tag))) {
            textView.setTextColor(Color.RED);
            textView.setTextSize(60);
            getWindow().getDecorView().setBackgroundColor(Color.BLACK);
        }

        // If infected and the user holds the text for 10 seconds they get cured.
        textView.setOnTouchListener(new View.OnTouchListener() {

            final Handler handler = new Handler();
            Runnable mLongPressed = new Runnable() {
                public void run() {
                    if (InfectedStateManager.getCurrentState(getApplicationContext()).equals(getString(R.string.infected_tag))) {
                        Toast.makeText(getApplicationContext(), "Cure Expedited!", Toast.LENGTH_LONG).show();
                        infectedStateManager.setNewState(getString(R.string.clean_tag), true);
                    }
                }
            };

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction() == MotionEvent.ACTION_DOWN)
                    handler.postDelayed(mLongPressed, 10000);
                if((event.getAction() == MotionEvent.ACTION_UP)||(event.getAction() ==     MotionEvent.ACTION_UP))
                    handler.removeCallbacks(mLongPressed);
                return false;
            }
        });


        // Sets up an on click listener for the text view so that people can self-infect.
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Only runs the below if the user isn't already infected.
                if (!state.equals(getString(R.string.infected_tag))) {

                    // decreases a value, giving the user an option to stop self-infecting. If the value reaches ) the users state changes to infected.
                    if (clicksBeforeInfected > 1) {
                        clicksBeforeInfected = clicksBeforeInfected - 1;

                        if (clicksBeforeInfected == 1) {
                            Toast.makeText(getApplicationContext(), clicksBeforeInfected + getString(R.string.self_infect_one), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), clicksBeforeInfected + getString(R.string.self_infect_multiple), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        infectedStateManager.setNewState(getString(R.string.infected_tag), false);
                        Toast.makeText(getApplicationContext(), getString(R.string.now_infected), Toast.LENGTH_SHORT).show();
                    }
                } else {

                    // If cure is to happen in future let them know. Otherwise cure them.
                    if (InfectedStateManager.isCured(getApplicationContext())) {
                        infectedStateManager.setNewState(getString(R.string.clean_tag), true);
                        Toast.makeText(getApplicationContext(), getString(R.string.recoup_message), Toast.LENGTH_SHORT).show();
                    } else {

                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                        String curedDateAsString = formatter.format(new Date(InfectedStateManager.getCuredDate(getApplicationContext())));

                        Toast.makeText(getApplicationContext(), getString(R.string.wait_message) + " " + curedDateAsString + ".", Toast.LENGTH_SHORT).show();

                    }
                }
            }
        });

    }

}


