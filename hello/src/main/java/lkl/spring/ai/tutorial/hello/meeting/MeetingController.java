package lkl.spring.ai.tutorial.hello.meeting;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingMinutesService service;

    @PostMapping(value = "/generate", consumes = "multipart/form-data")
    public ResponseEntity<MeetingMinutes> generateMinutes(@RequestParam(required = false) MultipartFile audio, @RequestParam String meetingType, @RequestParam List<String> participants) throws IOException {

        var input = MeetingInput.builder().audioFile(audio != null ? audio.getResource() : null).metadata(new MeetingMetadata(meetingType, participants, LocalDateTime.now())).build();

        return ResponseEntity.ok(service.generateMinutes(input));
    }


    @GetMapping("/index")
    public String getHoroscope() {
        return "meeting.html";
    }
}