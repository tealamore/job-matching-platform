package com.FairMatch.FairMatch.model;

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

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "swiped_on", nullable = false, unique = true)
  private User user;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "job_id", nullable = false, unique = true)
  private Jobs jobs;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SwipeStatus status;

}
