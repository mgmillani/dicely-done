package br.ufrgs.inf.dicelydone.pokergame;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import br.ufrgs.inf.dicelydone.R;

public class EndGameFragment extends Fragment {
    private static final String ARG_VICTORY = "br.ufrgs.inf.dicelydone.OUTCOME";
    private static final String ARG_WAIT = "br.ufrgs.inf.dicelydone.WAIT";

    private boolean mVictory;
    private boolean mWait;

    private TextView mLblOutcome;
    private TextView mLblQuestion;
    private Button mBtnPlay;
    private Button mBtnQuit;
    private ProgressBar mProgress;

    private Handler mListener;

    public static EndGameFragment newInstance(boolean victory, boolean wait) {
        EndGameFragment fragment = new EndGameFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_VICTORY, victory);
        args.putBoolean(ARG_WAIT, wait);
        fragment.setArguments(args);
        return fragment;
    }

    public EndGameFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mVictory = getArguments().getBoolean(ARG_VICTORY);
            mWait = getArguments().getBoolean(ARG_WAIT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View result = inflater.inflate(R.layout.fragment_end_game, container, false);

        mProgress = (ProgressBar) result.findViewById(R.id.progressBar);
        mLblOutcome = (TextView) result.findViewById(R.id.lblOutcome);
        mLblQuestion = (TextView) result.findViewById(R.id.lblQuestion);
        mBtnPlay = (Button) result.findViewById(R.id.btnPlay);
        mBtnQuit = (Button) result.findViewById(R.id.btnQuit);

        mBtnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onRestartGame();
            }
        });

        mBtnQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onQuitGame();
            }
        });

        setWaiting(mWait);
        setVictory(mVictory);

        return result;
    }

    public void setWaiting(boolean waiting) {
        mWait = waiting;

        mProgress.setVisibility(mWait? View.VISIBLE : View.INVISIBLE);
        mLblQuestion.setVisibility(mWait? View.INVISIBLE : View.VISIBLE);
        mBtnPlay.setVisibility(mWait? View.INVISIBLE : View.VISIBLE);
        mBtnQuit.setVisibility(mWait ? View.INVISIBLE : View.VISIBLE);
    }

    public void setVictory(boolean victory) {
        mVictory = victory;
        mLblOutcome.setText(victory? R.string.result_victory : R.string.result_defeat);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (Handler) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement EndGameFragment.Handler");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface Handler {
        void onRestartGame();
        void onQuitGame();
    }

}
