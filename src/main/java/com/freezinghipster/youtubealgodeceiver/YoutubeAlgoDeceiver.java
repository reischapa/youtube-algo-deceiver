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
import java.text.MessageFormat;
import java.util.*;

public class YoutubeAlgoDeceiver {
    private final Object lock = new Object();
    private Deque<String> videoIds;
    private File baseProfileFolderPath;
    private String geckoDriverPath;
    private String firefoxBinPath;
    private Map<String, Thread> threadStore;
    private Map<String, YoutubeAlgoDeceiverRunner> runners;
    public YoutubeAlgoDeceiver(String geckoDriverPath, String firefoxBinPath) {
        this.videoIds = new LinkedList<>();

        this.geckoDriverPath = geckoDriverPath;

        this.firefoxBinPath = firefoxBinPath;

        this.threadStore = new HashMap<>();

        this.runners = new HashMap<>();

        System.setProperty("webdriver.gecko.driver", this.geckoDriverPath);

        System.out.println("Loading base firefox profile...");

        try {
            baseProfileFolderPath = new File(YoutubeAlgoDeceiver.class.getClassLoader().getResource("firefox-profile-dir").toURI());
        } catch (Exception e) {
            throw new IllegalStateException("Could not find firefox profile dir");
        }

        System.out.println("Loaded base firefox profile present in firefox-profile-dir.");

    }

    public static void main(String[] args) {
        YoutubeAlgoDeceiver deceiver = new YoutubeAlgoDeceiver("/home/chapa/bin/geckodriver", "/home/chapa/bin/firefox");

        String s1 = deceiver.initRunner();

        deceiver.setShutdownOnEmptyQueue(s1, true);

        deceiver.pushVideoId("jdh_GjBsphg");
        deceiver.pushVideoId("lGHwQvhHvmU");

        deceiver.startAllRunners();

    }

    private File getBaseProfileFolderPathCopy() throws IOException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        File targetFile = new File(tmpDir + File.separator + UUID.randomUUID());

        FileUtils.copyDirectory(this.baseProfileFolderPath, targetFile);

        return targetFile;
    }

    private FirefoxDriver getFirefoxDriverInstance() {
        FirefoxOptions options = new FirefoxOptions();

        FirefoxBinary binary = new FirefoxBinary(new File(this.firefoxBinPath));

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
        synchronized (lock) {
            this.videoIds.addFirst(videoId);
        }
    }

    public void pushVideoIds(Collection<String> videoIds) {
        synchronized (lock) {
            for (String videoId : videoIds) {
                this.videoIds.addFirst(videoId);
            }
        }
    }

    private class YoutubeAlgoDeceiverRunner implements Runnable {
        private final FirefoxDriver driver;
        private final String identifier;
        private boolean pendingShutdown;
        private boolean shuttingDownIfQueueIsEmpty;

        public YoutubeAlgoDeceiverRunner(FirefoxDriver driver) {
            this.driver = driver;
            this.identifier = UUID.randomUUID().toString();
        }

        @Override
        public void run() {
            while (true) {
                if (this.isPendingShutdown()) {
                    System.out.println(MessageFormat.format("Youtube algo deceiver with id {0) has finished.", this.identifier));
                    this.cleanup();
                    return;
                }

                try {
                    Thread.sleep(1000);

                    String videoId;

                    synchronized (lock) {
                        if (videoIds.size() == 0) {
                            if (this.isShuttingDownIfQueueIsEmpty()) {
                                System.out.println("No video urls in queue... Deceiver Runner with id " + this.identifier + " exiting...");
                                this.cleanup();
                                return;
                            } else {
                                System.out.println("No video urls in queue... Deceiver Runner with id " + this.identifier + " waiting...");
                                continue;
                            }
                        }

                        videoId = videoIds.removeLast();
                        System.out.println("Video id found! id = " + videoId);
                    }


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

                    long maxAge = 10 * 1000;

                    long initialTimestamp = System.currentTimeMillis();

                    while (true) {
                        if (System.currentTimeMillis() - initialTimestamp > maxAge) {
                            System.out.println("Deceiver runner with id " + this.identifier + " has reached max play time. Exiting out of play loop...");
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

        public synchronized boolean isPendingShutdown() {
            return pendingShutdown;
        }

        public synchronized void setPendingShutdown(boolean pendingShutdown) {
            this.pendingShutdown = pendingShutdown;
        }


        public boolean isShuttingDownIfQueueIsEmpty() {
            return shuttingDownIfQueueIsEmpty;
        }

        public void setShuttingDownIfQueueIsEmpty(boolean shuttingDownIfQueueIsEmpty) {
            this.shuttingDownIfQueueIsEmpty = shuttingDownIfQueueIsEmpty;
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
