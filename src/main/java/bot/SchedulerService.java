package bot;

import bot.voice.VoiceAudioScheduler;
import net.dv8tion.jda.api.JDA;

import java.time.*;
import java.util.EnumSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SchedulerService {
    private final JDA jda;
    private final BotConfig config;
    private final ConfigStore store;
    private final VoiceAudioScheduler voiceAudioScheduler;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private LocalDate lastDailyResetDate;

    public SchedulerService(JDA jda, BotConfig config, ConfigStore store, VoiceAudioScheduler voiceAudioScheduler) {
        this.jda = jda;
        this.config = config;
        this.store = store;
        this.voiceAudioScheduler = voiceAudioScheduler;
    }

    public void start() {
        executor.scheduleAtFixedRate(this::tick, 0, 1, TimeUnit.SECONDS);
    }

    private void tick() {
        try {
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Tokyo"));
            LocalDate today = now.toLocalDate();
            LocalTime target = config.getEffectiveTime();
            DayOfWeek dayOfWeek = today.getDayOfWeek();

            resetDailyFlagsIfNeeded(now, today);

            boolean isFridayOrSaturday =
                    (dayOfWeek == DayOfWeek.FRIDAY || dayOfWeek == DayOfWeek.SATURDAY) && config.getTodayOverride() == null;

            boolean alreadyPlayedToday =
                    config.getLastPlayedDate() != null && config.getLastPlayedDate().equals(today);

            boolean isTargetTime =
                    now.getHour() == target.getHour() && now.getMinute() == target.getMinute();

            if (!config.isSkipToday()
                    && !isFridayOrSaturday
                    && !alreadyPlayedToday
                    && isTargetTime) {

                var guild = jda.getGuildById(config.getGuildId());
                if (guild != null && voiceAudioScheduler.audioPlayer.getPlayingTrack() == null) {
                    voiceAudioScheduler.play();
                    config.setLastPlayedDate(today);
                    store.save(config);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetDailyFlagsIfNeeded(LocalDateTime now, LocalDate today) {
        if (now.getHour() == 0 && now.getMinute() == 0) {
            if (!today.equals(lastDailyResetDate)) {
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

                lastDailyResetDate = today;
            }
        }
    }
}