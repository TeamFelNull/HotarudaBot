package bot.voice;

import bot.HotarudaBot;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

public class VoiceAudioScheduler extends AudioEventAdapter {
    private static final Logger log = LoggerFactory.getLogger(VoiceAudioScheduler.class);
    private AudioManager audioManager;
    private VoiceAudioManager voiceAudioManager;
    public AudioPlayer audioPlayer;
    private final long guildId;

    /**
     * コンストラクタ
     *
     * @param audioManager      オーディオマネージャー
     * @param voiceAudioManager ボイスオーディオマネージャー
     */
    public VoiceAudioScheduler(AudioManager audioManager, VoiceAudioManager voiceAudioManager) {
        this.audioManager = audioManager;
        this.voiceAudioManager = voiceAudioManager;
        audioPlayer = voiceAudioManager.getAudioPlayerManager().createPlayer();
        guildId = HotarudaBot.guildId;
        audioPlayer.addListener(this);
        audioManager.setSendingHandler(new VoiceAudioHandler(audioPlayer));

        AudioChannel channel = HotarudaBot.guild.getChannelById(AudioChannel.class, HotarudaBot.voiceChannelId);
        if(channel != null){
            audioManager.openAudioConnection(channel);
        }else {
            log.error("チャンネルが取得できなかったよ～...");
            return;
        }

    }


    /**
     * 破棄
     */
    public void dispose() {
        stop();
        audioManager.setSendingHandler(null);
    }

    public void stop() {
        audioPlayer.stopTrack();
    }

    public void play() {
        AudioTrack hotaruNoHikari;
        AtomicReference<AudioTrack> retTrack = new AtomicReference<>();
        try {
            File file = extractResourceToTemp("/assets/hotaru.mp3");
            voiceAudioManager.getAudioPlayerManager().loadItem(file.getAbsolutePath(), new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    retTrack.set(track);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                }

                @Override
                public void noMatches() {
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                }
            }).get();
        } catch (InterruptedException | ExecutionException | IOException e) {
            throw new RuntimeException(e);
        }

        hotaruNoHikari = retTrack.get();
        if (hotaruNoHikari == null) {
            throw new RuntimeException("Failed to load track");
        }
        audioPlayer.startTrack(hotaruNoHikari, true);
    }

    public File extractResourceToTemp(String resourcePath) throws IOException {
        InputStream is = getClass().getResourceAsStream(resourcePath);

        if (is == null) {
            throw new FileNotFoundException("リソースが見つからない: " + resourcePath);
        }

        File tempFile = File.createTempFile("audio_", ".mp3");
        tempFile.deleteOnExit();

        Files.copy(is, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        return tempFile;
    }


}