package br.ufrgs.inf.dicelydone.model;

import android.app.Activity;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class GameClient extends GameControl {

    private static final String TAG = "GameClient";
    private static final String TAG_RECV = TAG + "#RecvThread";
    private static final String TAG_SEND = TAG + "#SendThread";

    private LineReader mRecvStream;
    private PrintStream mSendStream;

    private boolean mConnected = false;
    private volatile boolean mStop = false;
    private IOException mError = null;

    private Object mStreamsMon = new Object();

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

    private BlockingQueue<String> mSendQ = new ArrayBlockingQueue<String>(10);
    private HashSet<String> mExpectedMsgs = new HashSet<>();
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
    }

    private void recvMsg() throws IOException {
        String line = mRecvStream.readLine();
        if (line == null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fireDisconnected();
                }
            });
            stop();
            return;
        }

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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fireJoined();
                    }
                });
            }
            break;

            case "startgame": {
                mRound = 0;
                final int bet = Integer.parseInt(msg[1]);

                setExpected("dice", "startturn");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fireStartGame(bet);
                    }
                });
            }
            break;

            case "startturn": {
                mRound = Integer.parseInt(msg[1]);

                if (mRound == 1 || mRound == 4) {

                    setExpected("dice");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fireStartRollTurn(mRound);
                        }
                    });

                } else if (mRound == 2 || mRound == 3) {
                    final int minBet = Integer.parseInt(msg[2]);

                    setExpected();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fireStartBetTurn(mRound, minBet);
                        }
                    });
                }
            }
            break;

            case "dice": {
                final String player = msg[1];
                Die[] vals = new Die[5];
                for (int i=0; i<5; i++) {
                    vals[i] = Die.byVal(Integer.parseInt(msg[i+2]));
                }
                final Hand dice = new Hand(vals);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fireDiceRolled(player, dice);
                    }
                });
            }
            break;

            case "betplaced": {
                final String player = msg[1];
                final int singleBet = Integer.parseInt(msg[2]);
                final int totalBet = Integer.parseInt(msg[3]);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fireBetPlaced(player, totalBet, singleBet);
                    }
                });
            }
            break;

            case "folded": {
                final String player = msg[1];

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fireFolded(player);
                    }
                });
            }
            break;

            case "endgame": {
                final String winner = msg[1];
                final int amtWon = Integer.parseInt(msg[2]);

                setExpected();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fireGameEnded(winner, amtWon);
                    }
                });
            }
            break;

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

    private void setExpected(String... types) {
        synchronized (mExpectedMsgs) {
            mExpectedMsgs.clear();
            for (String t : types) {
                mExpectedMsgs.add(t);
            }
        }
    }

}
