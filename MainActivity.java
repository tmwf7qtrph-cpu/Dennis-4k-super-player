package nl.dennis.superplayer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

/**
 * MainActivity: wordt gestart als launcher.
 * Als er een URI in de intent zit (bijv. van Stremio), stuur direct door naar PlayerActivity.
 * Anders toon het welkomstscherm.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        // Check of er een media URI meegegeven is
        Intent intent = getIntent();
        Uri uri = intent.getData();
        String action = intent.getAction();

        boolean hasMedia = (uri != null) ||
                           intent.hasExtra("uri") ||
                           intent.hasExtra("videoUri");

        if (hasMedia || Intent.ACTION_VIEW.equals(action)) {
            // Stuur door naar PlayerActivity
            Intent playerIntent = new Intent(this, PlayerActivity.class);
            playerIntent.setData(uri);
            if (intent.getExtras() != null) {
                playerIntent.putExtras(intent.getExtras());
            }
            playerIntent.setAction(action);
            startActivity(playerIntent);
            finish();
        } else {
            // Geen media - toon welkomstscherm
            setContentView(R.layout.activity_main);
        }
    }
}
