package bot;

import bot.voice.VoiceAudioScheduler;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class CommandListener extends ListenerAdapter {
    private final BotConfig config;
    private final ConfigStore store;

    public CommandListener(BotConfig config, ConfigStore store) {
        this.config = config;
        this.store = store;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        VoiceAudioScheduler voiceAudioScheduler = HotarudaBot.voiceAudioScheduler;
        switch (event.getName()) {
            case "ping" -> event.reply("Pong!").setEphemeral(true).queue();

            case "setdefault" -> {
                String timeStr = event.getOption("time").getAsString();
                try {
                    LocalTime time = LocalTime.parse(timeStr);
                    config.setDefaultTime(time);
                    store.save(config);
                    event.reply("毎日の時刻を " + time + " に更新した").queue();
                } catch (DateTimeParseException e) {
                    event.reply("時刻は HH:mm 形式で入れて").setEphemeral(true).queue();
                }
            }

            case "override" -> {
                String timeStr = event.getOption("time").getAsString();
                try {
                    LocalTime time = LocalTime.parse(timeStr);
                    config.setTodayOverride(time);
                    config.setSkipToday(false);
                    store.save(config);
                    event.reply("今日だけ " + time + " に変更した").queue();
                } catch (DateTimeParseException e) {
                    event.reply("時刻は HH:mm 形式で入れて").setEphemeral(true).queue();
                }
            }

            case "clearoverride" -> {
                config.setTodayOverride(null);
                store.save(config);
                event.reply("今日だけ設定を解除した").queue();
            }

            case "skiptoday" -> {
                config.setSkipToday(true);
                config.setTodayOverride(null);
                store.save(config);
                event.reply("本日の再生をスキップする").queue();
            }

            case "stop" -> {
                if (voiceAudioScheduler.audioPlayer.getPlayingTrack() == null) {
                    event.reply("いまは再生していない").setEphemeral(true).queue();
                    return;
                }

                voiceAudioScheduler.stop();
                event.reply("再生を停止した").queue();
            }

            case "status" -> {
                String message =
                        "guildId = " + config.getGuildId() + "\n" +
                                "voiceChannelId = " + config.getVoiceChannelId() + "\n" +
                                "defaultTime = " + config.getDefaultTime() + "\n" +
                                "todayOverride = " + config.getTodayOverride() + "\n" +
                                "skipToday = " + config.isSkipToday() + "\n" +
                                "effectiveTime = " + config.getEffectiveTime() + "\n" +
                                "lastPlayedDate = " + config.getLastPlayedDate() + "\n" +
                                "playing = " + (voiceAudioScheduler.audioPlayer.getPlayingTrack() != null);

                event.reply("```text\n" + message + "\n```").setEphemeral(true).queue();
            }

            case "playnow" -> {
                var guild = event.getJDA().getGuildById(config.getGuildId());
                if (guild == null) {
                    event.reply("Guildが見つからない").setEphemeral(true).queue();
                    return;
                }

                AudioTrack nowtrack = voiceAudioScheduler.audioPlayer.getPlayingTrack();

                if (nowtrack != null) {
                    event.reply("いま再生中: " + nowtrack.getInfo().title).setEphemeral(true).queue();
                    return;
                }

                try {
                    voiceAudioScheduler.play();
                    event.reply("再生開始").queue();
                } catch (Exception e) {
                    event.reply("再生失敗: " + e.getMessage()).setEphemeral(true).queue();
                }
            }

            default -> event.reply("未対応コマンド").setEphemeral(true).queue();
        }
    }
}