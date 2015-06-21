package br.ufrgs.inf.dicelydone;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import br.ufrgs.inf.dicelydone.pokergame.PokerGame;


public class StartGameActivity extends AppCompatActivity {

    private EditText mNickEdit;
    private CheckBox mCbSimulate;
    private EditText mSrvAddrEdit;
    private EditText mSrvPortEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_game);

        mNickEdit = (EditText) findViewById(R.id.nickEdit);
        mCbSimulate = (CheckBox) findViewById(R.id.cbSimulate);
        mSrvAddrEdit = (EditText) findViewById(R.id.serverAddrEdit);
        mSrvPortEdit = (EditText) findViewById(R.id.serverPortEdit);

        if (savedInstanceState != null) {
            mNickEdit.setText(savedInstanceState.getCharSequence("NICK"));
            mCbSimulate.setChecked(savedInstanceState.getBoolean("SIMULATE"));
            mSrvAddrEdit.setText(savedInstanceState.getCharSequence("SRV_ADDR"));
            mSrvPortEdit.setText(savedInstanceState.getCharSequence("SRV_PORT"));
        }


        TextView.OnEditorActionListener actionListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == R.id.start_game || actionId == EditorInfo.IME_NULL) {
                    startGame();
                    return true;
                }
                return false;
            }
        };
        mNickEdit.setOnEditorActionListener(actionListener);
        mSrvPortEdit.setOnEditorActionListener(actionListener);

        mCbSimulate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSrvAddrEdit.setEnabled(!isChecked);
                mSrvPortEdit.setEnabled(!isChecked);
            }
        });

        findViewById(R.id.btnStartGame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame();
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putCharSequence("NICK", mNickEdit.getText());
        outState.putBoolean("SIMULATE", mCbSimulate.isChecked());
        outState.putCharSequence("SRV_ADDR", mSrvAddrEdit.getText());
        outState.putCharSequence("SRV_PORT", mSrvPortEdit.getText());
    }

    private void startGame() {
        String nick = mNickEdit.getText().toString();
        if (nick.isEmpty()) {
            mNickEdit.setError(getString(R.string.error_nick_required));
            return;
        }

        if (nick.indexOf(' ') != -1 || nick.indexOf('\t') != -1) {
            mNickEdit.setError(getString(R.string.error_nick_whitespace));
            return;
        }

        Intent intent = new Intent(StartGameActivity.this, PokerGame.class);
        intent.putExtra(PokerGame.EXTRA_NICKNAME, nick);

        if (!mCbSimulate.isChecked()) {

            String addr = mSrvAddrEdit.getText().toString();

            int port;
            try {
                port = Integer.parseInt(mSrvPortEdit.getText().toString());
            } catch (NumberFormatException e) {
                mSrvPortEdit.setError(getString(R.string.error_parse_port));
                return;
            }

            intent.putExtra(PokerGame.EXTRA_SERVER_ADDR, addr);
            intent.putExtra(PokerGame.EXTRA_SERVER_PORT, port);
        }

        startActivity(intent);
    }

}
