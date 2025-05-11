package lkl.spring.ai.tutorial.hello.meeting;

import lombok.Builder;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record MeetingInput(
    @Nullable Resource audioFile,
    @Nullable Resource videoFile,
    MeetingMetadata metadata
) {}

record MeetingMetadata(
    String meetingType,
    List<String> participants,
    LocalDateTime startTime
) {}