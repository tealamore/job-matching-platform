package com.FairMatch.FairMatch.repository;

import com.FairMatch.FairMatch.model.Jobs;
import com.FairMatch.FairMatch.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JobsRepository extends JpaRepository<Jobs, UUID> {
  List<Jobs> findAllByUser(User user);
}
