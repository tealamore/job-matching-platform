package com.FairMatch.FairMatch.repository;

import com.FairMatch.FairMatch.model.Skills;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SkillsRepository extends JpaRepository<Skills, UUID> {
  List<Skills> findByUserId(UUID userId);

  Optional<Skills> findByUserIdAndSkillName(UUID id, String skill);

  void deleteAllByUserId(UUID userId);
}
