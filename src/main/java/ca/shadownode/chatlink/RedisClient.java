package ca.shadownode.chatlink;

import java.util.Optional;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public final class RedisClient {

    private final ChatLink plugin;

    private String hostname;
    private Integer port;
    private String password;

    private Optional<JedisPool> jedisPool = Optional.empty();

    public RedisClient(ChatLink plugin) {
        this.plugin = plugin;
    }

    public boolean load() {
        plugin.getLogger().info("Connecting to redis.");
        this.hostname = plugin.getConfig().getCore().redis.hostname;
        this.port = plugin.getConfig().getCore().redis.port;
        this.password = plugin.getConfig().getCore().redis.password;
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxWaitMillis(10*1000);
        if (this.password == null || this.password.equals("")) {
            jedisPool = Optional.ofNullable(new JedisPool(poolConfig, hostname, port, 0));
        } else {
            jedisPool = Optional.ofNullable(new JedisPool(poolConfig, hostname, port, 0, password));
        }
        return jedisPool.isPresent();
    }

    public boolean close() {
        if (jedisPool.isPresent()) {
            jedisPool.get().destroy();
            return true;
        }
        return false;
    }

    public Optional<JedisPool> getPool() {
        if (!jedisPool.isPresent() || jedisPool.get().isClosed()) {
            load();
        }
        return jedisPool;
    }
}