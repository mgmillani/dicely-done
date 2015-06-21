package br.ufrgs.inf.dicelydone.pokergame;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import br.ufrgs.inf.dicelydone.R;
import br.ufrgs.inf.dicelydone.model.ChipSet;
import br.ufrgs.inf.dicelydone.model.GameClient;
import br.ufrgs.inf.dicelydone.model.GameControl;
import br.ufrgs.inf.dicelydone.model.GameSimulation;
import br.ufrgs.inf.dicelydone.model.Hand;


public class PokerGame extends AppCompatActivity
    implements GameControl.Handler, RollingRound.EventHandler, BettingRound.EventHandler, ChipInfoFragment.EventHandler, EndGameFragment.Handler {

    public static final String EXTRA_NICKNAME = "br.ufrgs.inf.dicelydone.NICKNAME";
    public static final String EXTRA_SERVER_ADDR = "br.ufrgs.inf.dicelydone.SERVER_ADDR";
    public static final String EXTRA_SERVER_PORT = "br.ufrgs.inf.dicelydone.SERVER_PORT";

    private static final String TAG = "PokerGame";


    private String mPlayer = "Geralt";
    private int mRound = 0;
    private boolean mFolded = false;

    private GameControl mGameCtrl;

    private ChipInfoFragment mChipInfo;
    private HandInfoFragment mHandInfo;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poker_game);

        // TODO deal with savedInstanceState

        Bundle args = getIntent().getExtras();
        if (args == null) return;

        mProgress = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);

        mPlayer = args.getString(EXTRA_NICKNAME, mPlayer);

        mChipInfo = new ChipInfoFragment();
        //mChipInfo.setGameControl(mGameCtrl);
        mChipInfo.setPlayer(mPlayer);
        mChipInfo.initGame();

        mHandInfo = new HandInfoFragment();
        //mHandInfo.setGameControl(mGameCtrl);
        mHandInfo.setPlayer(mPlayer);

        if (args.containsKey(EXTRA_SERVER_ADDR)) {
            String addr = args.getString(EXTRA_SERVER_ADDR);
            int port = args.getInt(EXTRA_SERVER_PORT);

            GameClient cli = new GameClient(this);
            mGameCtrl = cli;

            mProgress.setTitle(R.string.wait_connecting);
            mProgress.show();

            mChipInfo.setGameControl(mGameCtrl);
            mHandInfo.setGameControl(mGameCtrl);

            cli.connect(addr, port, new GameClient.ConnectHandler() {
                @Override
                public void onConnected() {
                    mGameCtrl.join(mPlayer);
                }

                @Override
                public void onError(Exception e) {
                    mProgress.dismiss();
                    Toast.makeText(PokerGame.this, R.string.error_connect_failed, Toast.LENGTH_LONG).show();
                    Log.v(TAG, e.toString());
                    finish();
                }
            });

        } else {
            mGameCtrl = new GameSimulation(this);
        }

        mGameCtrl.addHandler(this);

        getFragmentManager().beginTransaction()
                .add(R.id.fragment_container, new WaitingScreen())
                .add(R.id.chipinfo_container, mChipInfo)
                .add(R.id.handinfo_container, mHandInfo)
                .hide(mHandInfo)
                .commit();

        if (mGameCtrl instanceof GameSimulation) {
            mGameCtrl.join(mPlayer);

            mChipInfo.setGameControl(mGameCtrl);
            mHandInfo.setGameControl(mGameCtrl);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_poker_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_quit:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        final AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(R.string.question_confirm_quit)
            .setNegativeButton(R.string.action_continue, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            })
            .setPositiveButton(R.string.action_quit, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mGameCtrl.quit();
                    dialog.dismiss();
                    finish();
                }
            })
            .create();

        dialog.show();
    }

    @Override
    public void handleMessage(GameControl.InMessage message) {
        switch (message.getType()) {
            case JOINED: {
                mProgress.setTitle(R.string.wait_gamestart);
                mProgress.show();
            }
            return;

            case STARTGAME: {
                mProgress.dismiss();

                mFolded = false;
                mChipInfo.setBetsVisible(true);

                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new WaitingScreen())
                        .hide(mHandInfo)
                        .show(mChipInfo)
                        .commit();
            }
            return;

            case STARTTURN: {
                GameControl.StartTurnMessage msg = (GameControl.StartTurnMessage) message;

                if (mFolded) return;
                mRound = msg.getRound();


                if (mRound == 1 || mRound == 4) {
                    Fragment fragment = new RollingRound();

                    Bundle args = new Bundle();
                    args.putInt(RollingRound.ARG_ROUND, mRound);
                    args.putBoolean(RollingRound.ARG_SIMULATION, true);
                    fragment.setArguments(args);

                    getFragmentManager().beginTransaction()
                            .hide(mChipInfo)
                            .replace(R.id.fragment_container, fragment)
                            .commit();
                }

                if (mRound == 2 || mRound == 3) {
                    int instruction = (mRound == 2) ? R.string.instruction_bet : R.string.instruction_call;
                    Fragment roundFragment = new BettingRound();

                    if (mRound == 3 && msg.getMinBet() == mChipInfo.getPlayerBet().getValue()) {
                        mGameCtrl.bet(new ChipSet());
                        return;
                    }

                    Bundle args = new Bundle();
                    args.putString(BettingRound.ARG_MESSAGE, getString(instruction));
                    roundFragment.setArguments(args);

                    getFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, roundFragment, "BettingRound")
                            .show(mChipInfo)
                            .commit();

                    mChipInfo.setReadOnly(mRound != 2);
                }

                soundAlert();
            }
            return;

            case DICE: {
                GameControl.DiceMessage msg = (GameControl.DiceMessage) message;

                if (msg.getPlayer().equals(mPlayer)) {
                    getFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new WaitingScreen())
                            .show(mChipInfo)
                            .show(mHandInfo)
                            .commit();
                }
            }
            return;

            case FOLDED: {
                GameControl.FoldedMessage msg = (GameControl.FoldedMessage) message;

                if (!msg.getPlayer().equals(mPlayer)) {
                    Toast.makeText(this, msg.getPlayer() + " folded.", Toast.LENGTH_SHORT).show();

                } else {
                    mFolded = true;

                    getFragmentManager().beginTransaction()
                            .hide(mChipInfo)
                            .hide(mHandInfo)
                            .replace(R.id.fragment_container, EndGameFragment.newInstance(false, true))
                            .commit();
                }
            }
            return;

            case BETPLACED: {
                GameControl.BetPlacedMessage msg = (GameControl.BetPlacedMessage) message;

                if (!msg.getPlayer().equals(mPlayer)) {
                    Toast.makeText(this, msg.getPlayer() + " placed a bet.", Toast.LENGTH_SHORT).show();
                }
            }
            return;

            case ENDGAME: {
                GameControl.EndGameMessage msg = (GameControl.EndGameMessage) message;

                soundAlert();

                boolean victory = msg.getWinner().equals(mPlayer);
                mChipInfo.setBetsVisible(false);

                getFragmentManager().beginTransaction()
                        .hide(mHandInfo)
                        .show(mChipInfo)
                        .replace(R.id.fragment_container, EndGameFragment.newInstance(victory, false))
                        .commit();

            }
            return;

            case CLOSE: {
                Toast.makeText(this, R.string.error_disconnect, Toast.LENGTH_LONG).show();
                finish();
            }
            return;

            case DISCONNECTED:
                break; // TODO?
        }

    }

    @Override
    public void rollDice() {
        if (mRound == 1) {
            mGameCtrl.roll();
        } else if (mRound == 4) {
            mGameCtrl.reroll(new Hand()); // TODO actually choose the dice
        }
    }

    @Override
    public void onBetPlaced() {
        mGameCtrl.bet(mChipInfo.getAddedBet());

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new WaitingScreen())
                .commit();
    }

    @Override
    public void onFolded() {
        mGameCtrl.fold();
    }

    @Override
    public void onBetAndStashChanged(ChipSet stash, ChipSet bet) {
        BettingRound fragment;
        try {
            fragment = (BettingRound) getFragmentManager().findFragmentById(R.id.fragment_container);
        } catch (ClassCastException e) {
            fragment = null;
        }

        if (fragment != null) {
            fragment.setBetEnabled(bet.getValue() >= mChipInfo.getIndividualBet());
        }
    }

    @Override
    public void onRestartGame() {
        mGameCtrl.restart();
        mProgress.setTitle(R.string.wait_gamestart);
        mProgress.show();
    }

    @Override
    public void onQuitGame() {
        mGameCtrl.quit();
        finish();
    }

    private void soundAlert() {
        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), alert);
        r.play();

        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(500);
    }
}
