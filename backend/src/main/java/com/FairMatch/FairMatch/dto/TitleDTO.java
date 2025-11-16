package com.FairMatch.FairMatch.dto;

import com.FairMatch.FairMatch.model.JobTitles;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TitleDTO {
  private UUID id;
  private String title;
  public TitleDTO(JobTitles jobTitle) {
      this.title = jobTitle.getTitle();
      this.id = jobTitle.getId();
  }
}
