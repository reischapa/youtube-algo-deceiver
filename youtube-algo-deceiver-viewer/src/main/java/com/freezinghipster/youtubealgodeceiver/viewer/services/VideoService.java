package com.freezinghipster.youtubealgodeceiver.viewer.services;

import com.freezinghipster.youtubealgodeceiver.viewer.daos.DAOException;
import com.freezinghipster.youtubealgodeceiver.viewer.daos.VideoDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class VideoService {

    @Autowired
    private VideoDAO videoDAO;

    public List<String> getVideoIds() {
        try {
            return this.videoDAO.getStrings(20);
        } catch (DAOException e) {
            e.printStackTrace();
        }

        return new LinkedList<>();
    }
}
