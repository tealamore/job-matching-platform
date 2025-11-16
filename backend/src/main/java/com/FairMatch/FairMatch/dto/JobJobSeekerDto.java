package com.FairMatch.FairMatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobJobSeekerDto {
    private String appliedDate;
    private UserDto user;
    private UUID jobsId;
    private String status;

    public JobJobSeekerDto(com.FairMatch.FairMatch.model.JobJobSeeker jobJobSeeker) {
        this.appliedDate = jobJobSeeker.getAppliedDate().toString();
        this.user = new UserDto(jobJobSeeker.getUser());
        this.jobsId = jobJobSeeker.getJobs().getId();
        this.status = jobJobSeeker.getStatus().name();
    }
}
