import { Job } from "@/components/JobCard";
import axios from "axios";

export const fetchJobs = async () => {
    try {
        const response = await axios.get<Job[]>('/api/jobs/feed', { withCredentials: true });
        return response.data;
    } catch (err) {
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
        }, { withCredentials: true });
    } catch (err) {
    }
};

export const validateAuthToken = async () => {
    try {
        await axios.get('/api/auth/valid', { withCredentials: true });
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

export const fetchMyJobs = async () => {
    const response = await axios.get('/api/me/jobs', { withCredentials: true });
    return response.data;
};

export const getMe = async () => {
    const response = await axios.get('/api/me', { withCredentials: true });
    return response.data;
};

export const updateMe = async (data: { name: string; email: string; phone: string; password?: string }) => {
    const response = await axios.post('/api/me/', data, { withCredentials: true });
    return response.data;
};

export const updateTitles = async (data: { desiredTitles: string[] }) => {
    const response = await axios.post('/api/me/title', data, { withCredentials: true });
    return response.data;
};

export const updateSkills = async (data: { skills: string[] }) => {
    const response = await axios.post('/api/me/skill', data, { withCredentials: true });
    return response.data;
};

export const createJob = async (title: string, description: string, salary: number, tags: string[]) => {
    const response = await axios.post('/api/jobs', {
        title,
        description,
        salary,
        tags
    }, {
        withCredentials: true
    });
    return response.data;
};