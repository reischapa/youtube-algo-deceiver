package com.freezinghipster.youtubealgodeceiver.viewer.daos;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;

@Component
public class VideoDAO {

    @Value("${dbUrl}")
    private String dbUrl;

    @Value("${dbUser}")
    private String user;

    @Value("${dbPassword}")
    private String password;

    public List<String> getStrings(int numStrings) throws DAOException {
        List<String> results = new LinkedList<>();

        try (Connection connection = DriverManager.getConnection(dbUrl, user, password)) {
            PreparedStatement ps = connection.prepareStatement("SELECT video_id FROM videos ORDER BY RANDOM() LIMIT ?");

            ps.setInt(1, numStrings);

            ps.execute();

            ResultSet resultSet = ps.getResultSet();

            while (resultSet.next()) {
                results.add(resultSet.getString(1));
            }
        } catch (Exception e) {
            throw new DAOException(e);
        }


        return results;
    }

}
