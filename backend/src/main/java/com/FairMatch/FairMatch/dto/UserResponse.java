package com.FairMatch.FairMatch.dto;

import com.FairMatch.FairMatch.model.*;
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
public class UserResponse {
  private String name;
  private String email;
  private String phone;

  private UUID id;

  private UserType userType;

  @Builder.Default
  private List<TitleResponse> desiredTitles = null;

  @Builder.Default
  private List<SkillsResponse> skills = null;

  @Builder.Default
  private List<JobsResponse> jobs = null;

  public UserResponse(User user) {
    if (user == null) {
      return;
    }
    this.name = user.getName();
    this.email = user.getEmail();
    this.phone = user.getPhone();
    this.id = user.getId();
    this.userType = user.getUserType();
  }

  public UserResponse(User user, List<Jobs> jobs) {
    if (user == null) {
      return;
    }
    this.name = user.getName();
    this.email = user.getEmail();
    this.phone = user.getPhone();
    this.id = user.getId();
    this.userType = user.getUserType();

    jobs.forEach(it -> it.setJobJobSeekers(null));
    jobs.forEach(it -> it.setUser(null));

    this.jobs = jobs.stream().map(JobsResponse::new).toList();
  }

  public UserResponse(User user, List<JobTitles> jobTitles, List<Skills> skills) {
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
        .map(TitleResponse::new)
        .toList();
    }

    if (skills == null) {
      this.skills = new ArrayList<>();
    } else {
      this.skills = skills.stream()
        .map(SkillsResponse::new)
        .toList();
    }
  }
}
