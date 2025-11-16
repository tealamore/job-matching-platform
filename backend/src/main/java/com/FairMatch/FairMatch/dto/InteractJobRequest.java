package com.FairMatch.FairMatch.dto;

import com.FairMatch.FairMatch.model.SwipeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InteractJobRequest {
  private UUID jobId;
  private SwipeStatus swipeStatus;
}
