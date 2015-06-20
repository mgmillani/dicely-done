package br.ufrgs.inf.dicelydone.pokergame;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import br.ufrgs.inf.dicelydone.R;

/**
 * Fragment for the first round, where players roll their dice.
 *
 * <p>
 * Activities using this fragment MUST implement {@link RollingRound.EventHandler}
 *
 * <p>
 * Displays the instructions for the player.In the current prototype, waits
 * for two seconds then generates a random hand.
 *
 * <li> TODO obtain dice from the server on hybrid interaction
 * <li> TODO do we just wait for a timeout on the virtual version?
 */
public class RollingRound extends Fragment implements SensorEventListener {

    /**
     * Defines the events generated by the {@link RollingRound} fragment.
     */
    public interface EventHandler {
        /**
         * Called when the dice must be rolled
         */
        void rollDice();
    }

    public static final String ARG_ROUND = "br.ufrgs.inf.dicelydone.ROUND";
    public static final String ARG_SIMULATION = "br.ufrgs.inf.dicelydone.SIMULATION";

    private static final float LARGE_ACCEL = 20;

    private SensorManager mSensorManager;
    private Sensor mAccelSensor;

    private boolean mMustRandomize = false;
    private EventHandler mCallback;

    private boolean mCanShake = false;

    public RollingRound() {
        // Required empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mAccelSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (mAccelSensor == null) {
            mAccelSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rolling_round, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getArguments() != null) {
            readBundle(getArguments());
        }

        mMustRandomize = false;
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isResumed()) {
                            //mCallback.rollDice();
                        } else {
                            mMustRandomize = true;
                        }
                    }
                });
            }
        }, 2000);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mMustRandomize) {
            mCallback.rollDice();
        }

        if (mCanShake) {
            mSensorManager.registerListener(this, mAccelSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Obtain the callback from the activity
        try {
            mCallback = (EventHandler) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement RollingRound.EventHandler");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            boolean large = false;
            for (float val : event.values) {
                if (val <= -LARGE_ACCEL || LARGE_ACCEL <= val) {
                    large = true;
                    break;
                }
            }

            if (large) {
                mCanShake = false;
                mCallback.rollDice();
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Nothing to do
    }

    private void readBundle(Bundle bundle) {
        mCanShake = bundle.getBoolean(ARG_SIMULATION);

        int msgRoll = mCanShake ? R.string.instruction_roll_shake : R.string.instruction_roll_dice;
        int msgReRoll = mCanShake ? R.string.instruction_reroll_shake : R.string.instruction_reroll_dice;

        int round = bundle.getInt(ARG_ROUND);

        TextView view = (TextView) getActivity().findViewById(R.id.instructionsView);
        view.setText(round == 1 ? msgRoll : msgReRoll);

    }

}
