package com.FairMatch.FairMatch.dto;

import com.FairMatch.FairMatch.model.User;
import com.FairMatch.FairMatch.model.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private String name;
    private String email;
    private String phone;

    private UUID id;

    private UserType userType;

    public UserDto(User user) {
      if (user == null) {
        return;
      }
      this.name = user.getName();
      this.email = user.getEmail();
      this.phone = user.getPhone();
      this.id = user.getId();
      this.userType = user.getUserType();
    }
}
