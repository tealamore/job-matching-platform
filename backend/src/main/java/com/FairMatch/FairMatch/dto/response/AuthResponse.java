package com.FairMatch.FairMatch.dto.response;

import com.FairMatch.FairMatch.model.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
  private UserType userType;
}
