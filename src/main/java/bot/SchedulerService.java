package bot;

import bot.voice.VoiceAudioScheduler;
import net.dv8tion.jda.api.JDA;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SchedulerService {
    private final JDA jda;
    private final BotConfig config;
    private final ConfigStore store;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private VoiceAudioScheduler voiceAudioScheduler = HotarudaBot.voiceAudioScheduler;

    public SchedulerService(JDA jda, BotConfig config, ConfigStore store) {
        this.jda = jda;
        this.config = config;
        this.store = store;
    }

    public void start() {
        executor.scheduleAtFixedRate(this::tick, 0, 1, TimeUnit.SECONDS);
    }

    private void tick() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDate today = now.toLocalDate();
            LocalTime target = config.getEffectiveTime();

            if (!config.isSkipToday()
                    && (config.getLastPlayedDate() == null || !config.getLastPlayedDate().equals(today))) {

                if (now.getHour() == target.getHour() && now.getMinute() == target.getMinute()) {
                    var guild = jda.getGuildById(config.getGuildId());
                    if (guild != null && (voiceAudioScheduler.audioPlayer.getPlayingTrack() == null)) {
                        voiceAudioScheduler.play();
                        config.setLastPlayedDate(today);
                        store.save(config);
                    }
                }
            }

            if (now.getHour() == 0 && now.getMinute() == 0) {
                boolean changed = false;

                if (config.getTodayOverride() != null) {
                    config.setTodayOverride(null);
                    changed = true;
                }

                if (config.isSkipToday()) {
                    config.setSkipToday(false);
                    changed = true;
                }

                if (changed) {
                    store.save(config);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}