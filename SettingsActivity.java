package nl.dennis.superplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private CheckBox cbHardwareDecoding;
    private CheckBox cbTunnelledPlayback;
    private CheckBox cbAutoPlay;
    private RadioGroup rgVideoZoom;
    private Spinner spAudioLanguage;
    private Spinner spSubtitleSize;
    private Spinner spSubtitlePosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        cbHardwareDecoding = findViewById(R.id.cb_hardware_decoding);
        cbTunnelledPlayback = findViewById(R.id.cb_tunnelled_playback);
        cbAutoPlay = findViewById(R.id.cb_auto_play);
        rgVideoZoom = findViewById(R.id.rg_video_zoom);
        spAudioLanguage = findViewById(R.id.sp_audio_language);
        spSubtitleSize = findViewById(R.id.sp_subtitle_size);
        spSubtitlePosition = findViewById(R.id.sp_subtitle_position);

        // Audio taal
        String[] languages = {"Nederlands (nl)", "Engels (en)", "Duits (de)", "Frans (fr)", "Spaans (es)"};
        ArrayAdapter<String> langAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, languages);
        langAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAudioLanguage.setAdapter(langAdapter);

        // Subtitle grootte
        String[] sizes = {"12pt", "14pt", "16pt", "18pt", "20pt", "24pt", "28pt", "32pt", "36pt"};
        ArrayAdapter<String> sizeAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, sizes);
        sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSubtitleSize.setAdapter(sizeAdapter);
        spSubtitleSize.setSelection(2); // 16pt standaard

        // Subtitle positie
        String[] positions = {"0", "10", "20", "30", "40", "50", "60", "70", "80", "90", "100"};
        ArrayAdapter<String> posAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, positions);
        posAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSubtitlePosition.setAdapter(posAdapter);
        spSubtitlePosition.setSelection(8); // 80 standaard

        // Standaardwaarden
        cbHardwareDecoding.setChecked(true);
        cbAutoPlay.setChecked(true);

        Button btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v -> saveAndClose());

        Button btnClose = findViewById(R.id.btn_close);
        btnClose.setOnClickListener(v -> finish());
    }

    private void saveAndClose() {
        Intent result = new Intent();
        result.putExtra("hardware_decoding", cbHardwareDecoding.isChecked());
        result.putExtra("tunnelled_playback", cbTunnelledPlayback.isChecked());
        result.putExtra("auto_play", cbAutoPlay.isChecked());

        // Audio taal code
        String[] langCodes = {"nl", "en", "de", "fr", "es"};
        int langPos = spAudioLanguage.getSelectedItemPosition();
        result.putExtra("audio_language", langCodes[langPos]);

        // Subtitle size
        String sizeStr = spSubtitleSize.getSelectedItem().toString().replace("pt", "");
        result.putExtra("subtitle_size", Integer.parseInt(sizeStr));

        // Subtitle position (0-100)
        int pos = Integer.parseInt(spSubtitlePosition.getSelectedItem().toString());
        result.putExtra("subtitle_position", pos);

        // Video zoom
        int zoomId = rgVideoZoom.getCheckedRadioButtonId();
        String zoom = "fit";
        if (zoomId == R.id.rb_fill) zoom = "fill";
        else if (zoomId == R.id.rb_stretch) zoom = "stretch";
        result.putExtra("video_zoom", zoom);

        setResult(RESULT_OK, result);
        finish();
    }
}
