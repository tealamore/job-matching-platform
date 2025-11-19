import { Job } from "@/components/JobCard";
import axios from "axios";

export const fetchJobs = async () => {
    try {
        const response = await axios.get<Job[]>('/api/jobs/feed', { withCredentials: true });
        return response.data;
    } catch (err) {
        console.error("failed to load jobs", err);
        return [];
    }
};

export const interactWithJob = async (jobId: string, action: "right" | "left") => {
    var swipeStatus;
    if (action == 'right') {
        swipeStatus = 'LIKE';
    } else {
        swipeStatus = 'DISLIKE';
    }

    try {
        await axios.post('/api/jobs/interact', {
          swipeStatus,
          jobId
        }, {withCredentials: true});
    } catch (err) {
        console.error("failed to load jobs", err);
    }
};

export const validateAuthToken = async () => {
    try {
        await axios.get('/api/auth/valid', {withCredentials: true});
        return true;
    } catch (err) {
        localStorage.removeItem("role");
        return false;
    } 
}

export const login = async (email: string, password: string) => {
    const response = await axios.post('/api/auth/login', {
        email,
        password
      }, {
        withCredentials: true
      });

    return response.data;
};

export const register = async (email: string, password: string, name: string, phone: string, userType: "JOB_SEEKER" | "BUSINESS" = "JOB_SEEKER") => {
    const response = await axios.post('/api/auth/signup', {
        email,
        password,
        name,
        phone, 
        userType
      }, {
        withCredentials: true
      });

    return response.data;
};