package com.smartcampus.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TicketAttachment {
    private String fileName;
    private String filePath;
    private String fileType;
    private LocalDateTime uploadedAt;
}
