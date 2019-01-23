package com.freezinghipster.youtubealgodeceiver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class YoutubeAlgoDeceiver {
    private final Object lock = new Object();
    private Deque<String> videoIds;

    private FirefoxOptions options;
    private FirefoxDriver driver;

    private ExecutorService executorService;

    private boolean autoStopIfQueueIsEmpty;

    public YoutubeAlgoDeceiver(String geckoDriverPath, String firefoxBinPath) throws FileNotFoundException {
        this.videoIds = new LinkedList<>();

        this.executorService = Executors.newScheduledThreadPool(2);

        this.options = new FirefoxOptions();

        System.setProperty("webdriver.gecko.driver", geckoDriverPath);

        FirefoxBinary binary = new FirefoxBinary(new File(firefoxBinPath));

        options.setBinary(binary);

        System.out.println("Loading firefox profile...");

        File profileDir = null;

        try {
            profileDir = new File(YoutubeAlgoDeceiver.class.getClassLoader().getResource("firefox-profile-dir").toURI());
        } catch (Exception e) {
            throw new FileNotFoundException("Could not find firefox profile dir");
        }

        System.out.println("Loaded firefox profile in firefox-profile-dir.");

        FirefoxProfile firefoxProfile = new FirefoxProfile(profileDir);

        options.setProfile(firefoxProfile);

    }

    public void start() {
        if (this.driver == null) {
            this.driver = new FirefoxDriver(this.options);
        }

        Runnable runnable = () -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    continue;
                }

                synchronized (lock) {
                    if (executorService.isShutdown()) {
                        return;
                    }

                    if (videoIds.size() == 0) {
                        if (isAutoStopIfQueueIsEmpty()) {
                            System.out.println("No video urls in queue... Terminating deceiver...");
                            stop();
                            return;
                        } else {
                            System.out.println("No video urls in queue... Waiting");
                            continue;
                        }
                    }

                    String videoId = videoIds.removeLast();
                    System.out.println("Video id found! id = " + videoId);

                    try {
                        playVideo(videoId);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        };

        executorService.submit(runnable);
    }

    public void stop() {
        executorService.shutdownNow();

        if (this.driver != null) {
            this.driver.quit();
        }
    }

    private void playVideo(String videoId) throws InterruptedException {
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

        while (true) {
            Thread.sleep(2000);

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

            System.out.println("Playing back video with id " + videoId);

        }

    }

    public void pushUrl(String s) {
        synchronized (lock) {
            this.videoIds.addFirst(s);
        }
    }

    public void pushUrls(Collection<String> urls) {
        synchronized (lock) {
            for (String url : urls) {
                this.videoIds.addFirst(url);
            }
        }
    }

    public boolean isAutoStopIfQueueIsEmpty() {
        synchronized (lock) {
            return autoStopIfQueueIsEmpty;
        }
    }

    public void setAutoStopIfQueueIsEmpty(boolean autoStopIfQueueIsEmpty) {
        synchronized (lock) {
            this.autoStopIfQueueIsEmpty = autoStopIfQueueIsEmpty;
        }
    }
}
