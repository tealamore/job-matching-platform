package com.FairMatch.FairMatch.dto;

import com.FairMatch.FairMatch.model.JobTitles;
import com.FairMatch.FairMatch.model.Skills;
import com.FairMatch.FairMatch.model.User;
import com.FairMatch.FairMatch.model.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
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

  @Builder.Default
  private List<TitleDTO> desiredTitles = null;

  @Builder.Default
  private List<SkillsDTO> skills = null;

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

  public UserDto(User user, List<JobTitles> jobTitles, List<Skills> skills) {
    if (user == null) {
      return;
    }
    this.name = user.getName();
    this.email = user.getEmail();
    this.phone = user.getPhone();
    this.id = user.getId();
    this.userType = user.getUserType();

    if (jobTitles == null) {
      this.desiredTitles = new ArrayList<>();
    } else {
      this.desiredTitles = jobTitles.stream()
        .map(TitleDTO::new)
        .toList();
    }

    if (skills == null) {
      this.skills = new ArrayList<>();
    } else {
      this.skills = skills.stream()
        .map(SkillsDTO::new)
        .toList();
    }
  }
}
