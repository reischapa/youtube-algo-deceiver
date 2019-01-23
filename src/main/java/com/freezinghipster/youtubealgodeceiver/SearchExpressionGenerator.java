package com.freezinghipster.youtubealgodeceiver;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public class SearchExpressionGenerator {
    private String dbUrl;

    public SearchExpressionGenerator(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public String getSearchExpression() {
        String result = null;

        try (Connection connection = DriverManager.getConnection(this.dbUrl)) {
            connection.setAutoCommit(false);
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM words ORDER BY RANDOM() LIMIT 2");

            ps.execute();

            ResultSet resultSet = ps.getResultSet();

            List<String> words = new LinkedList<>();

            while (resultSet.next()) {
                words.add(resultSet.getString(1));
            }

            result = URLEncoder.encode(
                    words.stream().reduce("", (acc, val) -> acc + (acc.length() > 0 ? " " : "") + val),
                    "utf-8");

            ps = connection.prepareStatement("INSERT INTO search_expressions VALUES (?)");
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
