package com.FairMatch.FairMatch.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "jobs_tags")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobTags {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private String skillName;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "jobs_id", nullable = false, unique = true)
  private Jobs jobs;
}
