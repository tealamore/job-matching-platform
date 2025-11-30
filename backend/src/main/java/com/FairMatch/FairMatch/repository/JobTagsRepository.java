package com.FairMatch.FairMatch.repository;

import com.FairMatch.FairMatch.model.JobTags;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JobTagsRepository extends JpaRepository<JobTags, UUID> {

  List<JobTags> findAllBySkillNameIn(List<String> skillNames);

  List<JobTags> findAllByJobsId(UUID jobsId);
}
