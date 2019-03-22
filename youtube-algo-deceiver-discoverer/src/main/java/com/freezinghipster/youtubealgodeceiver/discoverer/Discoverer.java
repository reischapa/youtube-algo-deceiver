package com.freezinghipster.youtubealgodeceiver.discoverer;

import com.freezinghipster.youtubealgodeceiver.SearchExpressionGenerator;
import org.apache.commons.lang3.StringEscapeUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;

import java.io.File;
import java.sql.*;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Discoverer {

    public static void main(String[] args) {
        if (args.length < 6) {
            System.err.println("Needs args dburl, dbUser, dbPassword, geckoDriverPath, firefoxBinPath, waitTime");
            System.exit(-1);
        }

        String dbUrl = args[0];
        String dbUser = args[1];
        String dbPassword = args[2];
        String geckoDriverPath = args[3];
        String firefoxDriverPath = args[4];
        Long waitTime = Long.valueOf(args[5]);

        Discoverer discoverer = new Discoverer(dbUrl, dbUser, dbPassword, geckoDriverPath, firefoxDriverPath);
        SearchExpressionGenerator expressionGenerator = new SearchExpressionGenerator(dbUrl, dbUser, dbPassword);

        System.out.println("Starting discoverer...");

        try {
            while (true) {
                Thread.sleep(waitTime);

                String searchExpression = expressionGenerator.getSearchExpression();
                if (searchExpression == null) {
                    System.err.println("Null search expression. Exiting...");
                    break;
                }
                discoverer.start(searchExpression);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        discoverer.stop();
    }

    private FirefoxDriver driver;

    private String dbUrl;
    private String dbUser;
    private String dbPassword;

    private String titleExclusionRegex = "w\\s+to\\s+([pP]ronounce|[sS]ay|[wW]rite|[sS]pell)";

    public Discoverer(String dbUrl, String dbUser, String dbPassword, String geckoDriverPath, String firefoxBinPath) {
        FirefoxOptions options = new FirefoxOptions();

        options.addArguments("--headless"); // This will make Firefox run in headless mode.

        System.setProperty("webdriver.gecko.driver", geckoDriverPath);

        // this uses default profile because we dont need a session to search
        FirefoxProfile firefoxProfile = new FirefoxProfile();

        FirefoxBinary binary = new FirefoxBinary(new File(firefoxBinPath));

        options.setBinary(binary);
        options.setProfile(firefoxProfile);

        this.driver = new FirefoxDriver(options);

        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }


    public void start(String searchExpression) throws IllegalStateException {
        driver.get("http://youtube.com/results?search_query=" + searchExpression);

        try {
            System.out.println("Waiting for page load...");
            Thread.sleep(5000);

            WebElement body = driver.findElement(By.tagName("body"));

            if (body == null) {
                System.out.println("Could not find HTML body");
                return;
            }

            int nScrolls = 4;
            long baseScrollWait = 1500;

            List<WebElement> initialThumbnails = this.driver.findElements(By.id("thumbnail"));

            if (initialThumbnails.size() == 0) {
                System.out.println("No results found for search expression " + searchExpression);
                return;
            }

            System.out.println("Some results were found...");

            System.out.println("Sending 2 scrolls to make sure that we are in the results...");
            body.sendKeys(Keys.PAGE_DOWN);
            body.sendKeys(Keys.PAGE_DOWN);

            for (int i = 0; i < nScrolls; i++) {
                System.out.println(MessageFormat.format("Scrolling page, iteration {0} of {1}", i + 1, nScrolls));
                body.sendKeys(Keys.PAGE_DOWN);
                long waitTime = (long) (baseScrollWait + (Math.random() * 400 - 200));
                System.out.println(MessageFormat.format("Scroll wait of {0} millis", waitTime));
                Thread.sleep(waitTime);
            }

            System.out.println("Finding results...");

            List<WebElement> results = this.driver.findElements(By.tagName("ytd-video-renderer"));

            if (results.size() == 0) {
                System.out.println("No thumbnail elements found");
                return;
            }

            System.out.println(MessageFormat.format("{0} results found...", results.size()));


            List<Map<String, String>> extractedResults = results.parallelStream().map(result -> {
                WebElement thumbnailLink = null;
                WebElement textLink = null;


                try {
                    thumbnailLink = result.findElement(By.id("thumbnail"));
                    textLink = result.findElement(By.id("video-title"));
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    return null;
                }

                if (thumbnailLink == null || textLink == null) {
                    return null;
                }

                String href = thumbnailLink.getAttribute("href");

                href = StringEscapeUtils.escapeHtml4(href);

                href = href.replaceFirst("^.*v=", "");

                if (href.length() > 11) {
                    href = href.substring(0, 11);
                }

                String title = textLink.getAttribute("title");

                title = StringEscapeUtils.escapeHtml4(title);

                Map<String, String> r = new HashMap<>();
                r.put("id", href);
                r.put("title", title);

                return r;
            })
                .filter(r -> {
                    if (r == null) {
                        return false;
                    }

                    String id = r.get("id");
                    String title = r.get("title");

                    if (!id.matches("[A-Za-z0-9-_]{11}")) {
                        System.out.println("Filtered out result with invalid id " + id);
                        return false;
                    };

                    Pattern pattern = Pattern.compile(this.titleExclusionRegex);
                    Matcher matcher = pattern.matcher(title);

                    if (matcher.find()) {
                        System.out.println("Filtered out result with title " + title);
                        return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());


            if (extractedResults.size() == 0) {
                System.out.println("No valid results remain after filtering...");
                return;
            } else {
                System.out.println(MessageFormat.format("{0} valid results found.", extractedResults.size()));
            }

            try (Connection connection = DriverManager.getConnection(this.dbUrl, this.dbUser, this.dbPassword)) {
                connection.setAutoCommit(false);

                PreparedStatement ps = connection.prepareStatement("INSERT INTO videos values (DEFAULT, ?, ?, ?)");

                for (Map<String, String> r : extractedResults) {
                    String id = r.get("id");
                    String title = r.get("title");

                    ps.setString(1, r.get(id));
                    ps.setString(2, r.get(title));
                    ps.setTimestamp(3, Timestamp.from(Instant.now()));

                    System.out.println("Inserting video with id " + id + " and title \"" + title + "\"");
                    ps.addBatch();

                }

                ps.executeBatch();

                connection.commit();

                System.out.println("Completed discovering process for search expression: " + searchExpression);
            } catch (SQLException e) {
                e.printStackTrace();

                SQLException next = e.getNextException();

                do {
                    next.printStackTrace();
                    next = next.getNextException();
                } while (next != null);

                throw new IllegalStateException("Error performing result insert operation");
            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public List<String> getURLList(int limit) {
        List<String> result = new LinkedList<>();

        try (Connection connection = DriverManager.getConnection(this.dbUrl, this.dbUser, this.dbPassword)) {
            PreparedStatement ps = connection.prepareStatement("SELECT video_id FROM videos ORDER BY RANDOM() LIMIT (?)");
            ps.setInt(1, limit);

            ps.execute();

            ResultSet resultSet = ps.getResultSet();

            while (resultSet.next()) {
                result.add(resultSet.getString(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public List<String> getURLList() {
        return this.getURLList(100);
    }

    public void stop() {
        if (this.driver != null) {
            this.driver.quit();
        }
    }

}
