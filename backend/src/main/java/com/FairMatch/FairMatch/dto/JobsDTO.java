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
public class JobsDTO {
    private String title;
    private String description;
    private Double salary;
    private UUID id;

    private UserDto postedBy;

    private List<JobJobSeekerDto> jobJobSeekers;

    public JobsDTO(Jobs jobs) {
      this.title = jobs.getTitle();
      this.description = jobs.getDescription();
      this.salary = jobs.getSalary();
      this.id = jobs.getId();
      this.postedBy = new UserDto(jobs.getUser());

      if (jobs.getJobJobSeekers() == null) {
        this.jobJobSeekers = Collections.emptyList();
        return;
      }

      this.jobJobSeekers = jobs.getJobJobSeekers().stream().map(JobJobSeekerDto::new).toList();

    }

}
