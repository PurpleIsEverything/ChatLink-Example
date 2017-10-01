package ca.shadownode.chatlink.config;

import ca.shadownode.chatlink.ChatLink;
import com.google.common.reflect.TypeToken;
import java.io.File;
import java.io.IOException;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public final class Configuration {

    private final ChatLink plugin;
    private final File pluginFolder;
    private CoreConfig coreConfig;

    public Configuration(ChatLink plugin) {
        this.plugin = plugin;
        this.pluginFolder = plugin.getPluginFolder();
        if (!this.pluginFolder.exists()) {
            this.pluginFolder.mkdirs();
        }
    }

    public boolean loadCore() {
        try {
            File file = new File(this.plugin.getPluginFolder(), "core.conf");
            if (!file.exists()) {
                file.createNewFile();
            }
            ConfigurationLoader<CommentedConfigurationNode> loader = ((HoconConfigurationLoader.Builder) HoconConfigurationLoader.builder().setFile(file)).build();
            CommentedConfigurationNode config = (CommentedConfigurationNode) loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));
            this.coreConfig = config.getValue(TypeToken.of(CoreConfig.class), new CoreConfig());
            loader.save(config);
            return true;
        } catch (IOException | ObjectMappingException ex) {
            this.plugin.getLogger().error("[Core] Failed to load core configuration.", ex);
        }
        return false;
    }
    
    public CoreConfig getCore() {
        return this.coreConfig;
    }
}