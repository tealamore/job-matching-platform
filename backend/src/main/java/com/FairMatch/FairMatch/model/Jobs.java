package com.FairMatch.FairMatch.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Jobs {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String description;

  @Column
  private Double salary;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "posted_by", nullable = false)
  private User user;

  @OneToMany(mappedBy = "jobs", fetch = FetchType.LAZY)
  private List<JobJobSeeker> jobJobSeekers = List.of();

  public Jobs(UUID id, String title, String description, Double salary, User user, JobJobSeeker jobJobSeeker) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.salary = salary;
    this.user = user;
    this.jobJobSeekers = List.of(jobJobSeeker);
  }
}

