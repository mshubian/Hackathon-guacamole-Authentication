package com.hackathon.guacamole;

public class DefaultTimeProvider implements TimeProviderInterface {
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}
