package br.ufrgs.inf.dicelydone.pokergame;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import br.ufrgs.inf.dicelydone.R;


public class PokerGame extends AppCompatActivity {
    /**
     * This argument must be an instance of {@link Hand} containing the player's dice.
     */
    public static final String ARG_HAND = "br.ufrgs.inf.dicelydone.HAND";

    /**
     * This argument must contain the player's chips.
     */
    public static final String ARG_CHIPS = "br.ufrgs.inf.dicelydone.CHIPS";

    /**
     * This argument must contain the player's current bet.
     */
    public static final String ARG_PLAYER_BET = "br.ufrgs.inf.dicelydone.PLAYER_BET";

    /**
     * This argument must contain the added bet of all players.
     */
    public static final String ARG_TOTAL_BET = "br.ufrgs.inf.dicelydone.TOTAL_BET";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poker_game);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_poker_game, menu);
        return false; // False because there is no menu to show
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
