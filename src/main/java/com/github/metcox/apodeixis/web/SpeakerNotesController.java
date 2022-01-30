package com.github.metcox.apodeixis.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpeakerNotesController {

    @GetMapping("/speakernotes")
    public String speakernotes() {
        return "speakernotes";
    }
}
