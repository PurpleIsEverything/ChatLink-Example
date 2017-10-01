package ca.shadownode.chatlink.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class CoreConfig {

    @Setting("Redis")
    public Redis redis = new Redis();
    
    @ConfigSerializable
    public static class Redis {
        
        @Setting("Hostname")
        public String hostname = "127.0.0.1";

        @Setting("Port")
        public Integer port = 6379;

        @Setting("Password")
        public String password = "";

    }
}