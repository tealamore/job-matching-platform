// src/components/EmployerDashboard.tsx
'use client';

import { useState, useEffect } from 'react';
import { fetchMyJobs, createJob } from '@/util/requests';

type UserResponse = {
    id: string;
    name: string;
    email: string;
    phone: string;
    userType: string;
};

type JobJobSeekerResponse = {
    appliedDate: string;
    user: UserResponse;
    jobsId: string;
    status: string;
};

type JobsResponse = {
    id: string;
    title: string;
    description: string;
    salary: number;
    postedBy: UserResponse;
    jobJobSeekers: JobJobSeekerResponse[];
};

export default function EmployerDashboard() {
    const [selectedJobId, setSelectedJobId] = useState<string | null>(null);
    const [jobs, setJobs] = useState<JobsResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [newJobForm, setNewJobForm] = useState({
        title: '',
        description: '',
        salary: '',
        tags: ''
    });
    const [isSubmitting, setIsSubmitting] = useState(false);

    useEffect(() => {
        fetchMyJobs()
            .then((data) => {
                setJobs(data);
                setLoading(false);
            })
            .catch((err) => {
                console.error("Failed to fetch jobs", err);
                setLoading(false);
            });
    }, []);

    const selectedJob = jobs.find(j => j.id === selectedJobId);

    const handleCreateJob = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsSubmitting(true);

        try {
            const tagsArray = newJobForm.tags.split(',').map(tag => tag.trim()).filter(tag => tag.length > 0);
            const salary = parseFloat(newJobForm.salary);

            await createJob(
                newJobForm.title,
                newJobForm.description,
                salary,
                tagsArray
            );

            setNewJobForm({ title: '', description: '', salary: '', tags: '' });
            setShowCreateModal(false);

            const updatedJobs = await fetchMyJobs();
            setJobs(updatedJobs);
        } catch (err) {
            console.error("Failed to create job", err);
        } finally {
            setIsSubmitting(false);
        }
    };

    if (loading) {
        return <div className="flex h-full items-center justify-center text-gray-500">Loading jobs</div>;
    }

    return (
        <>
            <div className="h-full w-full max-w-6xl mx-auto grid grid-cols-1 md:grid-cols-[350px_1fr] gap-6 p-4">
                <div className="flex flex-col gap-4 overflow-hidden h-full">
                    <div className="flex items-center justify-between">
                        <h2 className="text-xl font-bold text-white">Your Jobs</h2>
                        <button
                            onClick={() => setShowCreateModal(true)}
                            className="rounded-lg bg-white/10 px-[clamp(12px,1vw,16px)] py-[clamp(8px,.8vw,10px)] text-[clamp(.82rem,1vw,.92rem)] text-white backdrop-blur transition hover:bg-white/20"
                        >
                            + New Job
                        </button>
                    </div>

                    <div className="flex-1 overflow-y-auto space-y-3 pr-2">
                        {jobs.length === 0 ? (
                            <div className="text-center py-8 text-black text-sm">
                                You haven't posted any jobs yet.
                            </div>
                        ) : (
                            jobs.map(job => (
                                <div
                                    key={job.id}
                                    onClick={() => setSelectedJobId(job.id)}
                                    className={`p-4 rounded-xl border cursor-pointer transition-all duration-200 group
                                        ${selectedJobId === job.id
                                            ? 'bg-blue-50 border-blue-200 shadow-sm'
                                            : 'bg-white border-gray-100 hover:border-blue-100 hover:shadow-sm'
                                        }`}
                                >
                                    <div className="flex justify-between items-start mb-1">
                                        <h3 className="font-semibold text-black">
                                            {job.title}
                                        </h3>
                                        <span className="text-xs px-2 py-0.5 rounded-full font-medium bg-green-100 text-green-700">
                                            ACTIVE
                                        </span>
                                    </div>
                                    <div className="flex items-center gap-2 text-xs text-black mt-2">
                                        <span className="flex items-center gap-1">
                                            {job.jobJobSeekers.length} Applicants
                                        </span>
                                        <span>-</span>
                                        <span>{`$${job.salary}`}</span>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                </div>

                <div className="bg-white rounded-2xl border border-gray-100 shadow-sm flex flex-col overflow-hidden h-full">
                    {selectedJob ? (
                        <>
                            <div className="p-6 border-b border-gray-100 bg-gray-50/50">
                                <div className="flex justify-between items-start">
                                    <div>
                                        <h2 className="text-2xl font-bold text-black">{selectedJob.title}</h2>
                                        <p className="text-black mt-1">
                                            {`$${selectedJob.salary}`}
                                        </p>
                                        <p className="text-black mt-1">
                                            Description: {selectedJob.description}
                                        </p>
                                    </div>
                                </div>
                            </div>

                            <div className="flex-1 overflow-y-auto p-6">
                                <h3 className="text-lg font-semibold text-black mb-4">
                                    Applicants ({selectedJob.jobJobSeekers.length})
                                </h3>

                                {selectedJob.jobJobSeekers.length === 0 ? (
                                    <div className="text-center py-12 text-black">
                                        <p>No applicants yet.</p>
                                    </div>
                                ) : (
                                    <div className="space-y-4">
                                        {selectedJob.jobJobSeekers.map(application => (
                                            <div key={application.user.id} className="flex items-center justify-between p-4 rounded-xl border border-gray-100 hover:border-gray-200 transition-colors">
                                                <div className="flex items-center gap-4">
                                                    <div className="h-10 w-10 rounded-full bg-gradient-to-br from-blue-100 to-indigo-100 flex items-center justify-center text-blue-700 font-bold">
                                                        {application.user.name.charAt(0)}
                                                    </div>
                                                    <div>
                                                        <h4 className="font-semibold text-black">{application.user.name}</h4>
                                                        <p className="text-sm text-black">{application.user.email}</p>
                                                    </div>
                                                </div>

                                                <div className="flex items-center gap-6">
                                                    <div className="text-right">
                                                        <div className="text-xs text-black">
                                                            Applied {new Date(application.appliedDate).toLocaleDateString()}
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>
                        </>
                    ) : (
                        <div className="flex-1 flex flex-col items-center justify-center text-black">
                            <p className="text-lg font-medium">Select a job to view applicants</p>
                        </div>
                    )}
                </div>
            </div>

            {showCreateModal && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
                    <div className="bg-white rounded-2xl shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
                        <div className="p-6 border-b border-gray-100">
                            <h2 className="text-2xl font-bold text-black">Create New Job</h2>
                        </div>

                        <form onSubmit={handleCreateJob} className="p-6 space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-black mb-1">
                                    Job Title *
                                </label>
                                <input
                                    type="text"
                                    required
                                    value={newJobForm.title}
                                    onChange={(e) => setNewJobForm({ ...newJobForm, title: e.target.value })}
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-black"
                                    placeholder="e.g. Senior Software Engineer"
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-black mb-1">
                                    Description *
                                </label>
                                <textarea
                                    required
                                    value={newJobForm.description}
                                    onChange={(e) => setNewJobForm({ ...newJobForm, description: e.target.value })}
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent min-h-[120px] text-black"
                                    placeholder="Describe the role, responsibilities, and requirements..."
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-black mb-1">
                                    Salary *
                                </label>
                                <input
                                    type="number"
                                    required
                                    step="100"
                                    min="0"
                                    value={newJobForm.salary}
                                    onChange={(e) => setNewJobForm({ ...newJobForm, salary: e.target.value })}
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-black"
                                    placeholder="e.g. 75000"
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-black mb-1">
                                    Tags (comma-separated)
                                </label>
                                <input
                                    type="text"
                                    value={newJobForm.tags}
                                    onChange={(e) => setNewJobForm({ ...newJobForm, tags: e.target.value })}
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-black"
                                    placeholder="e.g. JavaScript, React, Node.js"
                                />
                            </div>

                            <div className="flex gap-3 pt-4">
                                <button
                                    type="button"
                                    onClick={() => {
                                        setShowCreateModal(false);
                                        setNewJobForm({ title: '', description: '', salary: '', tags: '' });
                                    }}
                                    className="flex-1 px-4 py-2 text-sm font-medium text-black bg-white border border-gray-300 rounded-lg hover:bg-gray-50"
                                    disabled={isSubmitting}
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    className="flex-1 px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
                                    disabled={isSubmitting}
                                >
                                    {isSubmitting ? 'Creating...' : 'Create Job'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </>
    );
}
