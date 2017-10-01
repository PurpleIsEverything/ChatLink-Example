package ca.shadownode.chatlink;

import ca.shadownode.chatlink.config.Configuration;
import ca.shadownode.chatlink.events.ChatListener;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;

@Plugin(id = "chatlink")
public class ChatLink {

    @Inject
    private Logger logger;

    @Inject
    @ConfigDir(sharedRoot = false)
    private File configDir;

    private Configuration config;

    private RedisClient client;

    @Listener
    public void onStarted(GameStartedServerEvent event) throws IOException {
        config = new Configuration(this);
        client = new RedisClient(this);

        if (config.loadCore()) {
            if (client.load()) {
                new RedisClient(this).load();
                new ChatListener(this).register();
                getLogger().info("Plugin started.");
            } else {
                getLogger().error("Unable to connect to redis.");
            }
        } else {
            getLogger().error("Failed to load configuration.");
        }
    }

    @Listener
    public void onStopping(GameStoppingServerEvent event) throws IOException {

        getLogger().info("Shutting down...");
        client.close();
    }

    @Listener
    public void onReload(GameReloadEvent event) throws IOException {
        client.close();
        config.loadCore();
        client.load();
        getLogger().info("Reloaded and reconnected to redis.");
    }

    public Configuration getConfig() {
        return config;
    }
    
    public RedisClient getClient() {
        return client;
    }
   
    public File getPluginFolder() {
        return configDir;
    }
    
    public Logger getLogger() {
        return logger;
    }
}
