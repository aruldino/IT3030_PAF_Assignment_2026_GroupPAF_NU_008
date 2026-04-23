package com.smartcampus.model;

import com.smartcampus.enums.UserRole;
import com.smartcampus.enums.UserStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    private String id;
    @Indexed(unique = true)
    private String email;
    private String name;
    private String profilePicture;
    @Builder.Default
    private UserRole role = UserRole.USER;
    @Builder.Default
    private UserStatus status = UserStatus.PENDING;
    @CreatedDate
    private LocalDateTime createdAt;
}
