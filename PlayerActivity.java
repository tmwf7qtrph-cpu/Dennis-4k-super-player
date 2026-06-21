package nl.dennis.superplayer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.Tracks;
import androidx.media3.common.VideoSize;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.ui.PlayerView;

import java.util.Locale;

public class PlayerActivity extends AppCompatActivity {

    private ExoPlayer player;
    private PlayerView playerView;
    private DefaultTrackSelector trackSelector;
    private Uri streamUri;
    private String videoTitle = "";

    private TextView tvTitle, tvResolution, tvCodec, tvHdr, tvAudioCodec, tvHwStatus, tvBuffer;
    private View overlayInfo;
    private Handler overlayHandler = new Handler();
    private Handler bufferHandler = new Handler();

    private boolean useHardwareDecoding = true;
    private boolean overlayVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fullscreen landscape
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        setContentView(R.layout.activity_player);

        playerView = findViewById(R.id.player_view);
        overlayInfo = findViewById(R.id.overlay_info);
        tvTitle = findViewById(R.id.tv_title);
        tvResolution = findViewById(R.id.tv_resolution);
        tvCodec = findViewById(R.id.tv_codec);
        tvHdr = findViewById(R.id.tv_hdr);
        tvAudioCodec = findViewById(R.id.tv_audio_codec);
        tvHwStatus = findViewById(R.id.tv_hw_status);
        tvBuffer = findViewById(R.id.tv_buffer);

        ImageButton btnSettings = findViewById(R.id.btn_settings);
        btnSettings.setOnClickListener(v -> openSettings());

        // Haal URI op uit intent
        Intent intent = getIntent();
        streamUri = intent.getData();
        if (streamUri == null && intent.hasExtra("uri")) {
            streamUri = Uri.parse(intent.getStringExtra("uri"));
        }
        if (streamUri == null && intent.hasExtra("videoUri")) {
            streamUri = Uri.parse(intent.getStringExtra("videoUri"));
        }

        // Haal titel op
        if (intent.hasExtra("title")) {
            videoTitle = intent.getStringExtra("title");
        }
        if (videoTitle == null || videoTitle.isEmpty()) {
            videoTitle = streamUri != null ? getFileNameFromUri(streamUri) : "Onbekend";
        }

        if (streamUri == null) {
            Toast.makeText(this, "Geen media URL ontvangen", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Subtitles ophalen
        String subtitleUri = null;
        if (intent.hasExtra("subtitleUri")) {
            subtitleUri = intent.getStringExtra("subtitleUri");
        }

        initPlayer(subtitleUri);
    }

    private void initPlayer(String subtitleUri) {
        DefaultTrackSelector.Parameters.Builder paramsBuilder =
            new DefaultTrackSelector.Parameters.Builder(this)
                .setPreferredAudioLanguage("nl")
                .setPreferredTextLanguage("nl")
                .setSelectUndeterminedTextLanguage(true);

        trackSelector = new DefaultTrackSelector(this);
        trackSelector.setParameters(paramsBuilder.build());

        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this);
        if (useHardwareDecoding) {
            renderersFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER);
        } else {
            renderersFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF);
        }

        player = new ExoPlayer.Builder(this)
                .setRenderersFactory(renderersFactory)
                .setTrackSelector(trackSelector)
                .build();

        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);
        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS);

        // Build media item (met optionele subtitle)
        MediaItem.Builder mediaBuilder = new MediaItem.Builder().setUri(streamUri);

        if (subtitleUri != null && !subtitleUri.isEmpty()) {
            MediaItem.SubtitleConfiguration subtitle = new MediaItem.SubtitleConfiguration.Builder(Uri.parse(subtitleUri))
                .setMimeType(getSubtitleMimeType(subtitleUri))
                .setLanguage("nl")
                .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                .build();
            mediaBuilder.setSubtitleConfigurations(java.util.Collections.singletonList(subtitle));
        }

        player.setMediaItem(mediaBuilder.build());
        player.prepare();
        player.setPlayWhenReady(true);

        // Listeners
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY) {
                    updateOverlayInfo();
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                Toast.makeText(PlayerActivity.this,
                    "Afspeelfout: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onVideoSizeChanged(@NonNull VideoSize videoSize) {
                updateResolutionInfo(videoSize.width, videoSize.height);
            }

            @Override
            public void onTracksChanged(@NonNull Tracks tracks) {
                updateTrackInfo(tracks);
            }
        });

        startBufferUpdates();
    }

    private void updateOverlayInfo() {
        tvTitle.setText(videoTitle);
        tvHwStatus.setText(useHardwareDecoding ? "HW ⚡" : "SW");
    }

    private void updateResolutionInfo(int width, int height) {
        String res = width + "x" + height;
        if (width >= 3840) res += " (4K)";
        else if (width >= 1920) res += " (1080p)";
        else if (width >= 1280) res += " (720p)";
        tvResolution.setText(res);
    }

    private void updateTrackInfo(Tracks tracks) {
        for (Tracks.Group group : tracks.getGroups()) {
            if (group.getType() == C.TRACK_TYPE_VIDEO && group.isSelected()) {
                for (int i = 0; i < group.length; i++) {
                    if (group.isTrackSelected(i)) {
                        Format format = group.getTrackFormat(i);
                        tvCodec.setText(format.sampleMimeType != null ? format.sampleMimeType : "Onbekend");
                        // HDR detectie
                        if (format.colorInfo != null) {
                            int colorTransfer = format.colorInfo.colorTransfer;
                            if (colorTransfer == C.COLOR_TRANSFER_ST2084) {
                                tvHdr.setText("HDR10");
                            } else if (colorTransfer == C.COLOR_TRANSFER_HLG) {
                                tvHdr.setText("HLG");
                            } else {
                                tvHdr.setText("SDR");
                            }
                        } else {
                            tvHdr.setText("SDR");
                        }
                    }
                }
            }
            if (group.getType() == C.TRACK_TYPE_AUDIO && group.isSelected()) {
                for (int i = 0; i < group.length; i++) {
                    if (group.isTrackSelected(i)) {
                        Format format = group.getTrackFormat(i);
                        tvAudioCodec.setText(format.sampleMimeType != null ? format.sampleMimeType : "AAC");
                    }
                }
            }
        }
    }

    private void startBufferUpdates() {
        bufferHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (player != null) {
                    int buffered = player.getBufferedPercentage();
                    tvBuffer.setText("Buffer: " + buffered + "%");
                }
                bufferHandler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private void showOverlay() {
        overlayInfo.setVisibility(View.VISIBLE);
        overlayVisible = true;
        overlayHandler.removeCallbacksAndMessages(null);
        overlayHandler.postDelayed(() -> {
            overlayInfo.setVisibility(View.GONE);
            overlayVisible = false;
        }, 5000);
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, 1001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            useHardwareDecoding = data.getBooleanExtra("hardware_decoding", true);
            String audioLang = data.getStringExtra("audio_language");
            String textSize = data.getStringExtra("subtitle_size");
            // Herstart player met nieuwe instellingen
            if (player != null) {
                long pos = player.getCurrentPosition();
                player.release();
                initPlayer(null);
                player.seekTo(pos);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (player == null) return super.onKeyDown(keyCode, event);

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                player.seekTo(player.getCurrentPosition() + (event.isLongPress() ? 30000 : 10000));
                showOverlay();
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                player.seekTo(Math.max(0, player.getCurrentPosition() - (event.isLongPress() ? 30000 : 10000)));
                showOverlay();
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                showOverlay();
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                overlayInfo.setVisibility(View.GONE);
                overlayVisible = false;
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                if (player.isPlaying()) player.pause();
                else player.play();
                return true;
            case KeyEvent.KEYCODE_MENU:
                openSettings();
                return true;
            case KeyEvent.KEYCODE_BACK:
                finish();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private String getFileNameFromUri(Uri uri) {
        String path = uri.getLastPathSegment();
        return path != null ? path : uri.toString();
    }

    private String getSubtitleMimeType(String uri) {
        if (uri.endsWith(".srt")) return MimeTypes.APPLICATION_SUBRIP;
        if (uri.endsWith(".vtt") || uri.endsWith(".webvtt")) return MimeTypes.TEXT_VTT;
        if (uri.endsWith(".ass") || uri.endsWith(".ssa")) return MimeTypes.TEXT_SSA;
        if (uri.endsWith(".ttml") || uri.endsWith(".xml")) return MimeTypes.APPLICATION_TTML;
        return MimeTypes.APPLICATION_SUBRIP;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) player.play();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) player.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bufferHandler.removeCallbacksAndMessages(null);
        overlayHandler.removeCallbacksAndMessages(null);
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
