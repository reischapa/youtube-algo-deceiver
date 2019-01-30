package com.freezinghipster.youtubealgodeceiver;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
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
    private Map<String, YoutubeAlgoDeceiverRunner> runners;

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

    private YoutubeAlgoDeceiverRunner getRunner() {
        FirefoxDriver driver = this.getFirefoxDriverInstance();

        if (driver == null) {
            return null;
        }

        return new YoutubeAlgoDeceiverRunner(driver);
    }

    public String initRunner() {
        YoutubeAlgoDeceiverRunner runner = this.getRunner();

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

    public void setShutdownOnEmptyQueue(String runnerId, boolean value) {
        if (this.runners.containsKey(runnerId)) {
            this.runners.get(runnerId).setShuttingDownIfQueueIsEmpty(value);
        }
    }

    public void setMaxPlayTime(String runnerId, long value) {
        if (this.runners.containsKey(runnerId)) {
            this.runners.get(runnerId).setMaxPlayTime(value);
        }
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

    private class YoutubeAlgoDeceiverRunner implements Runnable {
        private final FirefoxDriver driver;
        private final String identifier;
        private boolean shuttingDownIfQueueIsEmpty;
        private long maxPlayTime = 10 * 1000;

        public YoutubeAlgoDeceiverRunner(FirefoxDriver driver) {
            this.driver = driver;
            this.identifier = UUID.randomUUID().toString();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);

                    String videoId = videoIds.pollLast();


                    if (videoId == null) {
                        if (this.isShuttingDownIfQueueIsEmpty()) {
                            System.out.println("No video urls in queue... Deceiver Runner with id " + this.identifier + " exiting...");
                            this.cleanup();
                            return;
                        } else {
                            System.out.println("No video urls in queue... Deceiver Runner with id " + this.identifier + " waiting...");
                            continue;
                        }
                    }

                    System.out.println("Video id found! id = " + videoId);

                    driver.get("http://youtube.com/watch?v=" + videoId);

                    Thread.sleep(3000);

                    List<WebElement> autoplayToggleCandidates = driver.findElements(By.id("improved-toggle"));

                    if (autoplayToggleCandidates.size() > 0) {
                        WebElement autoplayToggle = autoplayToggleCandidates.get(0);

                        if (autoplayToggle.getAttribute("aria-pressed").equals("true")) {
                            autoplayToggle.click();
                            System.out.println("Disabled toggle");
                        }

                    }

                    Thread.sleep(1000);

                    driver.findElement(By.tagName("body")).sendKeys("k");
                    System.out.println("Playback started");


                    List<WebElement> candidates;

                    long initialTimestamp = System.currentTimeMillis();

                    while (true) {
                        if (System.currentTimeMillis() - initialTimestamp > this.maxPlayTime) {
                            System.out.println("Deceiver runner with id " + this.identifier + " has reached max play time. Exiting out of play loop...");
                            driver.get("http://example.com");
                            break;
                        }

                        Thread.sleep(5000);

                        candidates = driver.findElements(By.className("html5-endscreen"));

                        if (candidates.size() == 0) {
                            continue;
                        }

                        WebElement endScreen = candidates.get(0);

                        String displayValue = endScreen.getCssValue("display");

                        if (!displayValue.equals("none")) {
                            System.out.println("Finished playing back video with id " + videoId);
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    System.out.println("Deceiver runner with id " + this.identifier + " was interrupted. Exiting...");
                    e.printStackTrace();
                    this.cleanup();
                    return;
                }

            }
        }

        private void cleanup() {
            this.driver.quit();
        }


        public boolean isShuttingDownIfQueueIsEmpty() {
            return shuttingDownIfQueueIsEmpty;
        }

        public void setShuttingDownIfQueueIsEmpty(boolean shuttingDownIfQueueIsEmpty) {
            this.shuttingDownIfQueueIsEmpty = shuttingDownIfQueueIsEmpty;
        }

        public long getMaxPlayTime() {
            return maxPlayTime;
        }

        public void setMaxPlayTime(long maxPlayTime) {
            this.maxPlayTime = maxPlayTime;
        }

        public String getIdentifier() {
            return identifier;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            YoutubeAlgoDeceiverRunner that = (YoutubeAlgoDeceiverRunner) o;
            return Objects.equals(identifier, that.identifier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(identifier);
        }
    }
}
