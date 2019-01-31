package com.freezinghipster.youtubealgodeceiver;

public interface DeceiverOptions {

    String getGeckoDriverPath();

    String getFirefoxDriverPath();

    String getProfileFolderPath();

    Long getMaxPlayingTime();

    Long getQueueCycleTime();

    boolean isRunningHeadless();

}
