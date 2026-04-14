package com.mining.plugin.models;

import java.util.UUID;

public class PlayerData {
    public UUID uuid;
    public int level = 1;
    public long experience = 0;
    public double gold = 0;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public long getRequiredXP() {
        return level * 1000L;
    }
}