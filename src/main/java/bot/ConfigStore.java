package bot;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;

public class ConfigStore {
    private static final Path CONFIG_PATH = Path.of("config.json");
    private final ObjectMapper mapper;

    public ConfigStore() {
        mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter();
    }

    public BotConfig load(long guildId, long voiceChannelId) {
        if (!Files.exists(CONFIG_PATH)) {
            BotConfig config = new BotConfig();
            config.setGuildId(guildId);
            config.setVoiceChannelId(voiceChannelId);
            save(config);
            return config;
        }

        JsonNode root = mapper.readTree(CONFIG_PATH.toFile());

        BotConfig config = new BotConfig();
        config.setGuildId(root.path("guildId").asLong(guildId));
        config.setVoiceChannelId(root.path("voiceChannelId").asLong(voiceChannelId));

        String defaultTimeStr = root.path("defaultTime").asText("23:55");
        config.setDefaultTime(LocalTime.parse(defaultTimeStr));

        if (!root.path("todayOverride").isMissingNode() && !root.path("todayOverride").isNull()) {
            config.setTodayOverride(LocalTime.parse(root.path("todayOverride").asText()));
        }

        if (!root.path("lastPlayedDate").isMissingNode() && !root.path("lastPlayedDate").isNull()) {
            config.setLastPlayedDate(LocalDate.parse(root.path("lastPlayedDate").asText()));
        }

        config.setSkipToday(root.path("skipToday").asBoolean(false));

        return config;
    }

    public void save(BotConfig config) {
        JsonNode root = mapper.createObjectNode()
                .put("guildId", config.getGuildId())
                .put("voiceChannelId", config.getVoiceChannelId())
                .put("defaultTime", config.getDefaultTime().toString())
                .put("skipToday", config.isSkipToday());

        ((tools.jackson.databind.node.ObjectNode) root).putNull("todayOverride");
        ((tools.jackson.databind.node.ObjectNode) root).putNull("lastPlayedDate");

        if (config.getTodayOverride() != null) {
            ((tools.jackson.databind.node.ObjectNode) root)
                    .put("todayOverride", config.getTodayOverride().toString());
        }

        if (config.getLastPlayedDate() != null) {
            ((tools.jackson.databind.node.ObjectNode) root)
                    .put("lastPlayedDate", config.getLastPlayedDate().toString());
        }

        mapper.writeValue(CONFIG_PATH.toFile(), root);
    }
}
