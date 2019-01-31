package com.freezinghipster.youtubealgodeceiver;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Deceiver {
    private final Object lock = new Object();

    private Deque<String> videoIds;

    private Map<String, Thread> threadStore;
    private Map<String, DeceiverRunner> runners;

    private DeceiverOptions options;

    public Deceiver(DeceiverOptions options) {
        this.videoIds = new ConcurrentLinkedDeque<>();

        this.threadStore = new HashMap<>();

        this.runners = new HashMap<>();

        this.options = options;

        System.setProperty("webdriver.gecko.driver", this.options.getGeckoDriverPath());

    }

    private File getBaseProfileFolderPathCopy() throws IOException {
        System.out.println("Copying base firefox profile...");

        File baseProfileFolderPath;
        try {
            baseProfileFolderPath = new File(this.options.getProfileFolderPath());
        } catch (Exception e) {
            throw new IllegalStateException("Could not find firefox profile dir");
        }

        String tmpDir = System.getProperty("java.io.tmpdir");
        File targetFile = new File(tmpDir + File.separator + UUID.randomUUID());

        FileUtils.copyDirectory(baseProfileFolderPath, targetFile);

        System.out.println("Copied base firefox profile present onto " + targetFile);

        return targetFile;
    }

    private FirefoxDriver getFirefoxDriverInstance() {
        FirefoxOptions options = new FirefoxOptions();

        FirefoxBinary binary = new FirefoxBinary(new File(this.options.getFirefoxDriverPath()));

        options.setBinary(binary);

        if (this.options.isRunningHeadless()) {
            options.addArguments("-headless");
        }

        File profileFolder;

        try {
            profileFolder = this.getBaseProfileFolderPathCopy();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        FirefoxProfile firefoxProfile = new FirefoxProfile(profileFolder);

        options.setProfile(firefoxProfile);

        return new FirefoxDriver(options);

    }

    private DeceiverRunner getRunner() {
        FirefoxDriver driver = this.getFirefoxDriverInstance();

        if (driver == null) {
            return null;
        }

        return new DeceiverRunner(driver, this.videoIds);
    }

    public String initRunner() {
        DeceiverRunner runner = this.getRunner();

        if (runner == null) {
            return null;
        }

        this.runners.put(runner.getIdentifier(), runner);

        Thread thread = new Thread(runner);

        this.threadStore.put(runner.getIdentifier(), thread);

        return runner.getIdentifier();
    }

    public boolean startRunner(String runnerId) {
        if (!this.runners.containsKey(runnerId) || !this.threadStore.containsKey(runnerId)) {
            return false;
        }

        this.threadStore.get(runnerId).run();
        return true;
    }

    public void startAllRunners() {
        for (Map.Entry<String, Thread> entry : this.threadStore.entrySet()) {
            entry.getValue().start();
        }
    }

    public void setRunnerOptions(String runnerId, DeceiverRunnerOptions options) {
        if (this.runners.containsKey(runnerId)) {
            this.runners.get(runnerId).setOptions(options);
        }
    }

    public DeceiverRunnerOptions getRunnerOptions(String runnerId) {
        if (this.runners.containsKey(runnerId)) {
            return this.runners.get(runnerId).getOptions();
        }

        return null;
    }

    public boolean stopRunner(String runnerId) {
        if (!this.threadStore.containsKey(runnerId)) {
            return false;
        }

        this.threadStore.get(runnerId).interrupt();

        if (!this.runners.containsKey(runnerId)) {
            return false;
        }

        this.runners.remove(runnerId);
        return true;
    }

    public void stopAllRunners() {
        for (Map.Entry<String, Thread> entry : this.threadStore.entrySet()) {
            entry.getValue().interrupt();
        }

        this.runners.clear();
    }

    public void pushVideoId(String videoId) {
        this.videoIds.addFirst(videoId);
    }

    public void pushVideoIds(Collection<String> videoIds) {
        this.videoIds.addAll(videoIds);
    }
}
