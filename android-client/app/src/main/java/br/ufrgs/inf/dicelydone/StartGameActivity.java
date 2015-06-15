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

        mNickEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == R.id.start_game || actionId == EditorInfo.IME_NULL) {
                    startGame();
                    return true;
                }
                return false;
            }
        });

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

    private void startGame() {
        String nick = mNickEdit.getText().toString();
        if (nick.isEmpty()) {
            mNickEdit.setError(getString(R.string.error_nick_required));
            return;
        }

        Intent intent = new Intent(StartGameActivity.this, PokerGame.class);
        intent.putExtra(PokerGame.EXTRA_NICKNAME, nick);

        startActivity(intent);
    }

}
