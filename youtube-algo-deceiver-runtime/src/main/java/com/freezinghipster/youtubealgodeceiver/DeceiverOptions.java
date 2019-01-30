package com.freezinghipster.youtubealgodeceiver;

public interface DeceiverOptions {

    String getGeckoDriverPath();

    String getFirefoxDriverPath();

    String getProfileFolderPath();

    Integer getInstancePlayDuration();

    Integer getQueueTimeout();

    Integer getQueueCycleTime();

}
