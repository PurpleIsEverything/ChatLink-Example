package ca.shadownode.chatlink.events;

import ca.shadownode.chatlink.ChatLink;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.UUID;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.serializer.TextSerializers;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class ChatListener extends JedisPubSub {

    private final ChatLink plugin;

    private final Gson gson = new Gson();

    /**
     * Just a random UUID as a placeholder for what could be a configurable
     * server ID which would allow other servers to identify where a message
     * came from. Currently this is just used to make sure messages are not
     * duped.
     *
     */
    private final UUID serverID = UUID.randomUUID();

    public ChatListener(ChatLink plugin) {
        this.plugin = plugin;
    }

    public void register() {
        Sponge.getEventManager().registerListeners(plugin, this);
        Task.builder().execute(() -> {
            plugin.getClient().getPool().ifPresent((pool) -> {
                try (Jedis jedis = pool.getResource()) {
                    jedis.subscribe(this, "chatlink:chat");
                } catch (Exception ex) {
                }
            });
        }).async().submit(plugin);
    }

    @Listener(order = Order.POST)
    public void onChat(MessageChannelEvent.Chat event, @First Player player) {
        if (event.isCancelled() || event.isMessageCancelled()) {
            return;
        }

        String message = TextSerializers.JSON.serialize(event.getMessage());

        Sponge.getScheduler().createAsyncExecutor(plugin).execute(() -> {
            JsonObject obj = new JsonObject();
            obj.addProperty("serverName", serverID.toString());
            obj.addProperty("message", message);
            plugin.getClient().getPool().ifPresent((pool) -> {
                try (Jedis jedis = pool.getResource()) {
                    jedis.publish("chatlink:chat", obj.toString());
                } catch (Exception ex) {
                }
            });
        });

    }

    @Override

    public void onMessage(String channel, String request) {
        if (channel.equalsIgnoreCase("chatlink:chat")) {
            JsonObject obj = gson.fromJson(request, JsonObject.class);
            if (obj != null) {
                String servername = obj.get("serverName").getAsString();
                if (!servername.equalsIgnoreCase(serverID.toString())) {
                    String message = obj.get("message").getAsString();

                    // Note, this shouldn't trigger the ServerChatEvent since it looks for a player chatting specifically
                    Sponge.getServer().getBroadcastChannel().send(TextSerializers.JSON.deserialize(message));
                }
            }
        }
    }
}
