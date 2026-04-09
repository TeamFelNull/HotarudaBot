package bot;

import bot.voice.VoiceAudioManager;
import bot.voice.VoiceAudioScheduler;
import club.minnced.discord.jdave.interop.JDaveSessionFactory;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.audio.AudioModuleConfig;
import net.dv8tion.jda.api.audio.dave.DaveSessionFactory;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class HotarudaBot {
    private static final Logger log = LoggerFactory.getLogger(HotarudaBot.class);
    public static HotarudaBot INSTANCE;
    private static Dotenv dotenv;
    public static String discordToken;
    public static long guildId;
    public static long voiceChannelId;
    public static CommandListener listener;
    public static ConfigStore store;
    public static BotConfig config;
    public static VoiceAudioScheduler voiceAudioScheduler;
    public static VoiceAudioManager voiceAudioManager;
    public static Guild guild;

    private HotarudaBot() {
        INSTANCE = this;
    }

    public static void main(String[] args) throws Exception {
        init();

        var jda = JDABuilder.createDefault(discordToken)
                .setAudioModuleConfig(
                        new AudioModuleConfig()
                                .withDaveSessionFactory(new JDaveSessionFactory())
                )
                .addEventListeners(new CommandListener(config, store))
                .build()
                .awaitReady();

        guild = jda.getGuildById(guildId);
        var guild = jda.getGuildById(config.getGuildId());

        if (guild != null) {
            voiceAudioScheduler = new VoiceAudioScheduler(guild.getAudioManager(),voiceAudioManager);
        }else {
            log.error("サーバーの読み込みに失敗");
            return;
        }




//[Commands]============================================================================================================
        jda.updateCommands()
                .addCommands(
                        Commands.slash("ping", "疎通確認"),
                        Commands.slash("setdefault", "毎日の再生時刻を設定")
                                .addOption(OptionType.STRING, "time", "HH:mm", true),
                        Commands.slash("override", "今日だけ再生時刻を変更")
                                .addOption(OptionType.STRING, "time", "HH:mm", true),
                        Commands.slash("clearoverride", "overrideを解除"),
                        Commands.slash("skiptoday", "本日の定時再生をスキップ"),
                        Commands.slash("stop", "現在の再生を停止"),
                        Commands.slash("status", "現在の設定を表示"),
                        Commands.slash("playnow", "今すぐ再生")
                )
                .queue();
//======================================================================================================================


        SchedulerService scheduler = new SchedulerService(jda, config, store);
        scheduler.start();

        System.out.println("Bot起動した!");
    }

    private static String require(String key) {
        String value = dotenv.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(".env に " + key + " が見つからないよ~...");
        }
        return value;
    }

    public static void init() {
        try {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new InternalError("VM does not support mandatory encoding UTF-8");
        }

        dotenv = Dotenv.load();

        discordToken = require("DISCORD_TOKEN");
        guildId = Long.parseLong(require("GUILD_ID"));
        voiceChannelId = Long.parseLong(require("VOICE_CHANNEL_ID"));

        store = new ConfigStore();
        config = store.load(guildId, voiceChannelId);


        voiceAudioManager = new VoiceAudioManager();

    }

    public static HotarudaBot getInstance() {
        return INSTANCE;
    }
}
