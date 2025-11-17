package com.FairMatch.FairMatch.repository;

import com.FairMatch.FairMatch.model.JobTitles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobTitlesRepository extends JpaRepository<JobTitles, UUID> {
  List<JobTitles> findByUserId(UUID userId);

  Optional<JobTitles> findByUserIdAndTitle(UUID id, String title);
}
