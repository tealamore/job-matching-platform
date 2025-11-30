'use client';
import { useMemo } from 'react';
import Card from './Card';

export type Job = {
  id: string;
  title: string;
  salary?: string;
  jobTags?: string[];
  description?: string;
  postedBy: PostedBy;
};

export type PostedBy = {
  name: string;
};


export default function JobCard({
  job,
  mySkills = ['React', 'TypeScript', 'Node.js', 'Postgres', 'Tailwind'],
}: {
  job: Job;
  mySkills?: string[];
}) {
  const match = useMemo(() => {
    const mine = new Set(mySkills.map(s => s.toLowerCase()));
    const tech = (job.jobTags ?? []).map(s => s.toLowerCase());
    const hits = tech.filter(t => mine.has(t)).length;
    const pct = tech.length ? Math.round((hits / tech.length) * 100) : 0;
    const sorted = job.jobTags ?? []
    return { tech, total: tech.length, pct, highlights: sorted };
  }, [job.jobTags, mySkills]);

  return (
    <Card className="flex h-full flex-col p-5">
      <div className="flex items-start justify-between pb-3">
        <div className="flex items-center gap-3">
          <div>
            <h3 className="text-base font-semibold leading-tight text-black">{job.title}</h3>
            <p className="text-medium text-black">{job.postedBy.name}</p>
          </div>
        </div>
        {job.salary && (
          <span className="rounded-full border border-gray-200 bg-white/70 px-3 py-1 text-xs font-medium text-black">
            ${job.salary}
          </span>
        )}
      </div>

      <div className="h-px w-full bg-gradient-to-r from-gray-200/70 to-transparent" />

      <div className="mt-3 grid gap-4">
        <div>
          {job.description && (
            <p className="mt-3 text-medium text-black">
              {job.description}
            </p>
          )}

          {match.highlights.length > 0 && (
            <div className="mt-3 flex flex-wrap gap-2">
              {match.highlights.map(t => (
                <span
                  key={t}
                  className="rounded-full border border-gray-200 bg-gray-50 px-2 py-1 text-xs text-gray-700"
                >
                  {t}
                </span>
              ))}
            </div>
          )}
        </div>
      </div>
    </Card>
  );
}
