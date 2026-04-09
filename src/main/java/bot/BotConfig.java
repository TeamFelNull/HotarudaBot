package bot;

import java.time.LocalDate;
import java.time.LocalTime;

public class BotConfig {
    private long guildId;
    private long voiceChannelId;
    private LocalTime defaultTime = LocalTime.of(23, 55);
    private LocalTime todayOverride;
    private LocalDate lastPlayedDate;
    private boolean skipToday = false;

    public long getGuildId() {
        return guildId;
    }

    public void setGuildId(long guildId) {
        this.guildId = guildId;
    }

    public long getVoiceChannelId() {
        return voiceChannelId;
    }

    public void setVoiceChannelId(long voiceChannelId) {
        this.voiceChannelId = voiceChannelId;
    }

    public LocalTime getDefaultTime() {
        return defaultTime;
    }

    public void setDefaultTime(LocalTime defaultTime) {
        this.defaultTime = defaultTime;
    }

    public LocalTime getTodayOverride() {
        return todayOverride;
    }

    public void setTodayOverride(LocalTime todayOverride) {
        this.todayOverride = todayOverride;
    }

    public LocalDate getLastPlayedDate() {
        return lastPlayedDate;
    }

    public void setLastPlayedDate(LocalDate lastPlayedDate) {
        this.lastPlayedDate = lastPlayedDate;
    }

    public boolean isSkipToday() {
        return skipToday;
    }

    public void setSkipToday(boolean skipToday) {
        this.skipToday = skipToday;
    }

    public LocalTime getEffectiveTime() {
        return todayOverride != null ? todayOverride : defaultTime;
    }
}