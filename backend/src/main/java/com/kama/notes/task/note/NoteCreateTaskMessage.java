package com.kama.notes.task.note;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteCreateTaskMessage {
    private Long userId;
    private Integer questionId;
    private String content;
}
