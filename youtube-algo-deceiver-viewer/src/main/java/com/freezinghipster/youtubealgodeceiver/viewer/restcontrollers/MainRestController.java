package com.freezinghipster.youtubealgodeceiver.viewer.restcontrollers;

import com.freezinghipster.youtubealgodeceiver.viewer.services.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class MainRestController {

    @Autowired
    private VideoService videoService;

    @GetMapping("/")
    public String index(Model model) {
        List<String> results  = this.videoService.getVideoIds();

        model.addAttribute("videoIds", results);

        return "index";
    }
}
