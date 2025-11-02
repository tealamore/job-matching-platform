package com.FairMatch.FairMatch.repository;

import com.FairMatch.FairMatch.model.Skills;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SkillsRepository extends JpaRepository<Skills, UUID> {
  List<Skills> findByUserId(UUID userId);
}
