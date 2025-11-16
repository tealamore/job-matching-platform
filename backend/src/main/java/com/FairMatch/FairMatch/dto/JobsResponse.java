package com.FairMatch.FairMatch.dto;

import com.FairMatch.FairMatch.model.Jobs;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
        this.jobJobSeekers = jobs.getJobJobSeekers().stream().map(JobJobSeekerResponse::new).toList();
      }
    }

}
