package lkl.spring.ai.tutorial.hello.meeting;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record MeetingMinutes(
    String summary,
    List<String> keyDecisions,
    List<ActionItem> actionItems,
    List<String> keyPoints,
    String nextSteps
) {
    @Builder
    public record ActionItem(
        String task,
        String owner,
        LocalDate dueDate
    ) {}
}