package com.freezinghipster.youtubealgodeceiver.cli;

import com.freezinghipster.youtubealgodeceiver.DeceiverOptions;

import java.util.Map;

public class DeceiverOptionsImpl implements DeceiverOptions {
    private Map<String, Object> typedOptions;

    public DeceiverOptionsImpl(Map<String, Object> typedOptions) {
        this.typedOptions = typedOptions;
    }

    @Override
    public String getGeckoDriverPath() {
        return (String) this.typedOptions.get(CLIDeceiverOptionsKeys.GECKO_DRIVER_PATH.toString());
    }

    @Override
    public String getFirefoxDriverPath() {
        return (String) this.typedOptions.get(CLIDeceiverOptionsKeys.FIREFOX_PATH.toString());
    }

    @Override
    public String getProfileFolderPath() {
        return String.valueOf(this.typedOptions.get(CLIDeceiverOptionsKeys.PROFILE_FOLDER_PATH.toString()));
    }

    @Override
    public Long getMaxPlayingTime() {
        return Long.parseLong(String.valueOf(this.typedOptions.get(CLIDeceiverOptionsKeys.DURATION.toString())));
    }

    @Override
    public Long getQueueCycleTime() {
        return Long.parseLong(String.valueOf(this.typedOptions.get(CLIDeceiverOptionsKeys.QUEUE_CYCLE_TIME.toString())));
    }

    @Override
    public boolean isRunningHeadless() {
        return Boolean.valueOf(String.valueOf(this.typedOptions.get(CLIDeceiverOptionsKeys.RUNNING_HEADLESS.toString())));
    }

    public enum CLIDeceiverOptionsKeys {
        GECKO_DRIVER_PATH,
        FIREFOX_PATH,
        PROFILE_FOLDER_PATH,
        DURATION,
        QUEUE_CYCLE_TIME,
        RUNNING_HEADLESS
    }


    @Override
    public String toString() {
        return "DeceiverOptionsImpl{" +
                "typedOptions=" + typedOptions +
                '}';
    }
}
