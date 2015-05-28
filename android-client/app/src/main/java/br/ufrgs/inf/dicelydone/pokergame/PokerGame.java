package br.ufrgs.inf.dicelydone.pokergame;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import br.ufrgs.inf.dicelydone.R;
import br.ufrgs.inf.dicelydone.model.Hand;


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

        if (findViewById(R.id.pokergame_fragment_container) != null) {

            // Don't create fragments when being restored to avoid overlapping fragments
            if (savedInstanceState != null) {
                return;
            }

            WaitingScreen firstFragment = new WaitingScreen();
            firstFragment.setArguments(getIntent().getExtras()); // Intent isn't null since we have no savedInstanceState

            getFragmentManager().beginTransaction()
                    .add(R.id.pokergame_fragment_container, firstFragment).commit();

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_poker_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case R.id.action_test_round_1: {
            Round1 fragment = new Round1();
            fragment.setArguments(null);

            FragmentTransaction tr = getFragmentManager().beginTransaction();

            tr.replace(R.id.pokergame_fragment_container, fragment);
            tr.addToBackStack(null);

            tr.commit();

            Toast.makeText(this, "Test round 1", Toast.LENGTH_SHORT).show();
            return true;
        }

        case R.id.action_test_waiting_screen: {
            WaitingScreen fragment = new WaitingScreen();
            fragment.setArguments(null);

            FragmentTransaction tr = getFragmentManager().beginTransaction();

            tr.replace(R.id.pokergame_fragment_container, fragment);
            tr.addToBackStack(null);

            tr.commit();
            Toast.makeText(this, "Test waiting screen", Toast.LENGTH_SHORT).show();
            return true;
        }

        default:
            return super.onOptionsItemSelected(item);
        }

    }

}
