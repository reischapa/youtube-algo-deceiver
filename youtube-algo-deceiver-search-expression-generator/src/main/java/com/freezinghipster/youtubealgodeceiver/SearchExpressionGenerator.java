package com.freezinghipster.youtubealgodeceiver;

import org.apache.commons.math3.distribution.GeometricDistribution;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class SearchExpressionGenerator {
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private GeometricDistribution geometricDistribution;

    public SearchExpressionGenerator(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.geometricDistribution = new GeometricDistribution(0.375);
    }

    public String getSearchExpression() {
        Supplier<Integer> supplier = () -> this.geometricDistribution.sample() + 1;
        return this.getSearchExpression(supplier);
    }

    public String getSearchExpression(Supplier<Integer> numberOfWordsSupplier) {
        String result = null;

        try (Connection connection = DriverManager.getConnection(this.dbUrl, this.dbUser, this.dbPassword)) {
            connection.setAutoCommit(false);
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM words ORDER BY RANDOM() LIMIT ?");

            ps.setInt(1, numberOfWordsSupplier.get());

            ps.execute();

            ResultSet resultSet = ps.getResultSet();

            List<String> words = new LinkedList<>();

            while (resultSet.next()) {
                words.add(resultSet.getString(1));
            }

            result = URLEncoder.encode(
                    words.stream().reduce("", (acc, val) -> acc + (acc.length() > 0 ? " " : "") + val),
                    "utf-8");

            ps = connection.prepareStatement("INSERT INTO search_expressions VALUES (DEFAULT, ?)");
            ps.setString(1, result);

            ps.execute();
            connection.commit();

            System.out.println("Generated search expression: " + result);
        } catch (UnsupportedEncodingException | SQLException e) {
            e.printStackTrace();
        }


        return result;
    }
}
