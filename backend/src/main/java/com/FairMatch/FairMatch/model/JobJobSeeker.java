package com.FairMatch.FairMatch.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "job_job_seeker")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobJobSeeker {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private Date appliedDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "swiped_on", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "job_id", nullable = false)
  @JsonIgnore
  private Jobs jobs;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SwipeStatus status;

}
