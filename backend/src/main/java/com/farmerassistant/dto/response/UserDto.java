package com.farmerassistant.dto.response;

import com.farmerassistant.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private User.Role role;
    private String profileImageUrl;
    private boolean isEmailVerified;
    private boolean isActive;
}
