package com.FairMatch.FairMatch.repository;

import com.FairMatch.FairMatch.model.JobJobSeeker;
import com.FairMatch.FairMatch.model.Jobs;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface JobJobSeekerRepository extends JpaRepository<JobJobSeeker, UUID> {
  List<JobJobSeeker> findAllByJobsIn(Collection<Jobs> jobs);

}
