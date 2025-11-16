package com.FairMatch.FairMatch.repository;

import com.FairMatch.FairMatch.model.Jobs;
import com.FairMatch.FairMatch.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface JobsRepository extends JpaRepository<Jobs, UUID> {
  List<Jobs> findAllByUser(User user);

  @Query("""
    SELECT DISTINCT j FROM Jobs j
    LEFT JOIN FETCH j.jobJobSeekers a
    LEFT JOIN FETCH a.user
    WHERE j.user.id = :posterId
    """)
  List<Jobs> findAllByPosterWithApplicants(UUID posterId);

  @Query("""
    SELECT new Jobs(
             j.id, j.title, j.description, j.salary,
             j.user, jjs
         )
         FROM JobJobSeeker jjs
         JOIN jjs.jobs j
         WHERE jjs.user.id = :applicantId
""")
  List<Jobs> findAllByJobJobSeekers_User_Id(UUID applicantId);
}
