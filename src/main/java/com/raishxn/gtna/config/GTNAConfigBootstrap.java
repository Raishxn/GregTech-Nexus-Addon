package com.raishxn.gtna.config;

public final class GTNAConfigBootstrap {

    private GTNAConfigBootstrap() {}

    public static void init() {
        ConfigHolder.init();
        GTNABalance.init();
    }
}
