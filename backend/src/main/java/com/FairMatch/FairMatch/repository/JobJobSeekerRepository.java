package com.FairMatch.FairMatch.repository;

import com.FairMatch.FairMatch.model.JobJobSeeker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JobJobSeekerRepository extends JpaRepository<JobJobSeeker, UUID> {


}
