package com.freezinghipster.youtubealgodeceiver.cli;

import com.freezinghipster.youtubealgodeceiver.DeceiverOptions;

import java.util.*;

public class DeceiverOptionsImpl implements DeceiverOptions {
    public enum YoutubeAlgoDeceiverOptionKeys {
        GECKO_DRIVER_PATH,
        FIREFOX_PATH,
        PROFILE_FOLDER_PATH,
        DURATION,
        QUEUE_TIMEOUT,
        QUEUE_CYCLE_TIME
    }

    private Map<String, Object> typedOptions;

    public DeceiverOptionsImpl(Map<String, Object> typedOptions) {
        this.typedOptions = typedOptions;
    }

    @Override
    public String getGeckoDriverPath() {
        return (String) this.typedOptions.get(YoutubeAlgoDeceiverOptionKeys.GECKO_DRIVER_PATH.toString());
    }

    @Override
    public String getFirefoxDriverPath() {
        return (String) this.typedOptions.get(YoutubeAlgoDeceiverOptionKeys.FIREFOX_PATH.toString());
    }

    @Override
    public String getProfileFolderPath() {
        return (String) this.typedOptions.get(YoutubeAlgoDeceiverOptionKeys.PROFILE_FOLDER_PATH.toString());
    }


    @Override
    public Integer getInstancePlayDuration() {
        return Integer.parseInt((String) this.typedOptions.get(YoutubeAlgoDeceiverOptionKeys.DURATION.toString()));
    }

    @Override
    public Integer getQueueTimeout() {
        return Integer.parseInt((String) this.typedOptions.get(YoutubeAlgoDeceiverOptionKeys.QUEUE_TIMEOUT.toString()));
    }

    @Override
    public Integer getQueueCycleTime() {
        return Integer.parseInt((String) this.typedOptions.get(YoutubeAlgoDeceiverOptionKeys.QUEUE_CYCLE_TIME.toString()));
    }

}
