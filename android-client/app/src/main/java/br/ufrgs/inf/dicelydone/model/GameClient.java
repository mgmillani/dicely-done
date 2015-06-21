package br.ufrgs.inf.dicelydone.model;

import android.app.Activity;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class GameClient extends GameControl {

    private static final String TAG = "GameClient";
    private static final String TAG_RECV = TAG + "#RecvThread";
    private static final String TAG_SEND = TAG + "#SendThread";

    private LineReader mRecvStream;
    private PrintStream mSendStream;

    private volatile boolean mStop = false;

    private final Object mStreamsMon = new Object();

    public interface ConnectHandler {
        void onConnected();
        void onError(Exception e);
    }

    private Thread mRecvThread = new Thread() {
        @Override
        public void run() {
            while (!mStop) {
                try {
                    recvMsg();
                } catch (IOException e) {
                    Log.e(TAG_RECV, e.toString());
                }
            }
        }
    };

    private Thread mSendThread = new Thread() {
        @Override
        public void run() {
            while (!mStop) {
                try {
                    sendMsg();
                } catch (InterruptedException e) {
                    Log.e(TAG_SEND, e.toString());
                }
            }
        }
    };

    private BlockingQueue<String> mSendQ = new ArrayBlockingQueue<>(10);
    private final HashSet<String> mExpectedMsgs = new HashSet<>();
    private int mRound;

    public GameClient(Activity context) {
        super(context);

    }

    public void connect(final String address, final int port, final ConnectHandler handler) {
        new Thread() {
            @Override
            public void run() {
                synchronized (mStreamsMon) {
                    try {
                        Socket sock = new Socket(address, port);

                        mRecvStream = new LineReader(sock.getInputStream());
                        mSendStream = new PrintStream(new BufferedOutputStream(sock.getOutputStream()));

                    } catch (final IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                handler.onError(e);
                            }
                        });
                        return;
                    }

                    mSendThread.start();
                    mRecvThread.start();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handler.onConnected();
                        }
                    });
                }
            }
        }.start();
    }

    public void stop() {
        mStop = true;
    }

    private void sendMsg() throws InterruptedException {
        String line = mSendQ.take();
        mSendStream.append(line).append('\n').flush();
        Log.d(TAG_SEND, "sent message: " + line);

        if (line.equals("quit")) {
            stop();
        }
    }

    private void recvMsg() throws IOException {
        String line = mRecvStream.readLine();
        final InMessage message;

        if (line == null) {
            Log.d(TAG_RECV, "connection from server lost");
            message = new CloseMessage();
            stop();

        } else {
            String[] msg = line.split("\\s+");

            Log.d(TAG_RECV, "received message: " + line);

            synchronized (mExpectedMsgs) {
                if (!mExpectedMsgs.contains(msg[0])) {
                    Log.e(TAG_RECV, "received unexpected message of type " + msg[0]);
                }
            }

            switch (msg[0]) {
                case "joined": {
                    setExpected("startgame");
                    message = new JoinedMessage();
                }
                break;

                case "startgame": {
                    mRound = 0;
                    int bet = Integer.parseInt(msg[1]);

                    setExpected("dice", "startturn");
                    message = new StartGameMessage(bet);
                }
                break;

                case "startturn": {
                    mRound = Integer.parseInt(msg[1]);

                    if (mRound == 1 || mRound == 4) {

                        setExpected("dice");
                        message = new StartTurnMessage(mRound);

                    } else if (mRound == 2 || mRound == 3) {
                        final int minBet = Integer.parseInt(msg[2]);

                        setExpected();
                        message = new StartTurnMessage(mRound, minBet);

                    } else {
                        // INVALID ROUND
                        Log.e(TAG_RECV, "Invalid round " + mRound);
                        message = null;
                    }
                }
                break;

                case "dice": {
                    String player = msg[1];
                    Die[] vals = new Die[5];
                    for (int i=0; i<5; i++) {
                        vals[i] = Die.byVal(Integer.parseInt(msg[i+2]));
                    }
                    Hand dice = new Hand(vals);

                    message = new DiceMessage(player, dice);
                }
                break;

                case "betplaced": {
                    String player = msg[1];
                    int singleBet = Integer.parseInt(msg[2]);
                    int totalBet = Integer.parseInt(msg[3]);

                    message = new BetPlacedMessage(player, singleBet, totalBet);
                }
                break;

                case "folded": {
                    String player = msg[1];
                    message = new FoldedMessage(player);
                }
                break;

                case "endgame": {
                    String winner = msg[1];
                    int amtWon = Integer.parseInt(msg[2]);

                    setExpected();
                    message = new EndGameMessage(winner, amtWon);
                }
                break;

                default:
                    // INVALID MESSAGE
                    message = null;
            }
        }

        if (message != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fireMessage(message);
                }
            });
        }

    }

    @Override
    public void join(String playerName) {
        mSendQ.add("join " + playerName);
        setExpected("joined");
    }

    @Override
    public void roll() {
        mSendQ.add("roll");
        setExpected("dice", "betplaced", "folded", "endgame", "startturn");
    }

    @Override
    public void bet(ChipSet bet) {
        mSendQ.add("bet " + Integer.toString(bet.getValue()));
        setExpected("dice", "betplaced", "folded", "endgame", "startturn");
    }

    @Override
    public void fold() {
        mSendQ.add("fold");
        setExpected("dice", "betplaced", "folded", "endgame", "startturn");
    }

    @Override
    public void reroll(Hand kept) {
        mSendQ.add("reroll " + kept);
        setExpected("dice", "endgame");
    }

    @Override
    public void restart() {
        mSendQ.add("restart");
        setExpected("startgame");
    }

    @Override
    public void quit() {
        mSendQ.add("quit");
    }

    private void setExpected(String... types) {
        synchronized (mExpectedMsgs) {
            mExpectedMsgs.clear();
            Collections.addAll(mExpectedMsgs, types);
        }
    }

}
