package com.FairMatch.FairMatch.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSkillsRequest {
  @Builder.Default
  private List<String> skills = new ArrayList<>();
}
