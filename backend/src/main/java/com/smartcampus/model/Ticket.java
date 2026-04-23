package com.smartcampus.model;

import com.smartcampus.enums.TicketPriority;
import com.smartcampus.enums.TicketStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "tickets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Ticket {
    @Id
    private String id;
    private String userId;
    private String userName;
    private String resourceId;
    private String resourceName;
    private String category;
    private String description;
    private TicketPriority priority;
    private String location;
    private String contactDetails;
    @Builder.Default
    private TicketStatus status = TicketStatus.OPEN;
    private String assignedToId;
    private String assignedToName;
    private String resolutionNotes;
    private String rejectionReason;
    @Builder.Default
    private List<TicketAttachment> attachments = new ArrayList<>();
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
