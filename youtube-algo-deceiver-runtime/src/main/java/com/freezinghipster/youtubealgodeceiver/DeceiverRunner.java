package com.freezinghipster.youtubealgodeceiver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class DeceiverRunner implements Runnable {
    private final FirefoxDriver driver;
    private final String identifier;

    private final Deque<String> videoIds;

    private DeceiverRunnerOptions options;

    public DeceiverRunner(FirefoxDriver driver, Deque<String> videoIds) {
        this.driver = driver;
        this.videoIds = videoIds;
        this.identifier = UUID.randomUUID().toString();
        this.options = new DeceiverRunnerOptions();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(this.options.getQueueCycleTime());

                String videoId = videoIds.pollLast();


                if (videoId == null) {
                    if (this.options.isShuttingDownIfQueueIsEmpty()) {
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
                    if (System.currentTimeMillis() - initialTimestamp > this.options.getMaxPlayTime()) {
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


    public DeceiverRunnerOptions getOptions() {
        return options;
    }

    public void setOptions(DeceiverRunnerOptions options) {
        this.options = options;
    }


    public String getIdentifier() {
        return identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeceiverRunner that = (DeceiverRunner) o;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }

}
