package com.FairMatch.FairMatch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobJobSeekerResponse {
    private String appliedDate;
    private UserResponse user;
    private UUID jobsId;
    private String status;

    public JobJobSeekerResponse(com.FairMatch.FairMatch.model.JobJobSeeker jobJobSeeker) {
        this.appliedDate = jobJobSeeker.getAppliedDate().toString();
        this.user = new UserResponse(jobJobSeeker.getUser());
        this.jobsId = jobJobSeeker.getJobs().getId();
        this.status = jobJobSeeker.getStatus().name();
    }
}
