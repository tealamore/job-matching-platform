package com.FairMatch.FairMatch.dto;

import com.FairMatch.FairMatch.model.Skills;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillsResponse {
  private String skillName;
  private UUID id;

  public SkillsResponse(Skills skills) {
    if (skills == null) {
      return;
    }
    this.skillName = skills.getSkillName();
    this.id = skills.getId();
  }
}
