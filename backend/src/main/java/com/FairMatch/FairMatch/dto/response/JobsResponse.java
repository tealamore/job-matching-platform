package com.FairMatch.FairMatch.dto.response;

import com.FairMatch.FairMatch.model.JobJobSeeker;
import com.FairMatch.FairMatch.model.JobTags;
import com.FairMatch.FairMatch.model.Jobs;
import com.FairMatch.FairMatch.model.SwipeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobsResponse {
    private String title;
    private String description;
    private Double salary;
    private UUID id;

    private UserResponse postedBy;

    private List<JobJobSeekerResponse> jobJobSeekers;

    private List<String> jobTags;

    public JobsResponse(Jobs jobs) {
      this.title = jobs.getTitle();
      this.description = jobs.getDescription();
      this.salary = jobs.getSalary();
      this.id = jobs.getId();

      if (jobs.getUser() == null) {
        this.postedBy = null;
      } else {
        this.postedBy = new UserResponse(jobs.getUser());
      }

      if (jobs.getJobJobSeekers() == null) {
        this.jobJobSeekers = Collections.emptyList();
      } else {
        this.jobJobSeekers = jobs.getJobJobSeekers()
          .stream()
          .filter(it -> it.getStatus().equals(SwipeStatus.LIKE))
          .collect(toMap(it -> it.getUser().getName(),
              p -> p,
              (p, q) -> p))
          .values()
          .stream()
          .map(JobJobSeekerResponse::new)
          .toList();
      }
    }

    public JobsResponse(Jobs jobs, List<JobTags> jobTags) {
      this(jobs);

      this.jobTags = jobTags.stream().map(JobTags::getSkillName).toList();
    }
}
