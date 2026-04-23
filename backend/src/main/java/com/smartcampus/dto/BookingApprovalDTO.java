package com.smartcampus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BookingApprovalDTO {

    // Optional remark for approve; required for reject
    private String adminRemarks;
}