'use client';
import { useMemo } from 'react';
import Card from './Card';

export type Job = {
  id: string;
  title: string;
  salary?: string;
  tags?: Tags[];
  description?: string;
  postedBy: PostedBy;
};

export type PostedBy = {
  name: string;
};

export type Tags = {
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
    const tech = (job.tags ?? []).map(s => s.name.toLowerCase());
    const hits = tech.filter(t => mine.has(t)).length;
    const pct = tech.length ? Math.round((hits / tech.length) * 100) : 0;
    const sorted = job.tags ?? []
    return { hits, total: tech.length, pct, highlights: sorted };
  }, [job.tags, mySkills]);

  return (
    <Card className="flex h-full flex-col p-5">
      <div className="flex items-start justify-between pb-3">
        <div className="flex items-center gap-3">
          <div>
            <h3 className="text-base font-semibold leading-tight text-black">{job.title}</h3>
            <p className="text-xs text-black">{job.postedBy.name}</p>
          </div>
        </div>
        {job.salary && (
          <span className="rounded-full border border-gray-200 bg-white/70 px-3 py-1 text-xs font-medium text-black">
            {job.salary}
          </span>
        )}
      </div>

      <div className="h-px w-full bg-gradient-to-r from-gray-200/70 to-transparent" />

      <div className="mt-3 grid gap-4 md:grid-cols-[1fr_auto]">
        <div>
          {job.description && (
            <p className="mt-3 text-sm text-black">
              {job.description}
            </p>
          )}

          {/* Match bar (fast signal) */}
          {/* <div
            className="mt-3 flex items-center gap-2"
            role="meter"
            aria-valuemin={0}
            aria-valuemax={100}
            aria-valuenow={match.pct}
            aria-label="Skills match"
          >
            <div className="h-1.5 w-[46%] rounded-full bg-gray-200">
              <div
                className="h-full rounded-full bg-emerald-500"
                style={{ width: `${match.pct}%` }}
              />
            </div>
            <span className="text-xs text-black">{match.pct}% skills match</span>
          </div> */}

          {match.highlights.length > 0 && (
            <div className="mt-3 flex flex-wrap gap-2">
              {match.highlights.map(t => (
                <span
                  key={t.name}
                  className="rounded-full border border-gray-200 bg-gray-50 px-2 py-1 text-xs text-gray-700"
                >
                  {t.name}
                </span>
              ))}
            </div>
          )}
        </div>

        <aside className="hidden md:flex min-w-[7.5rem] flex-col items-end gap-3">
          {/* Circular match ring */}
          {/* <div
            className="relative h-12 w-12 rounded-full"
            style={{
              background: `conic-gradient(#10b981 ${match.pct}%, #e5e7eb 0)`,
            }}
            role="img"
            aria-label={`${match.pct}% skills match`}
          >
            <div className="absolute inset-1 rounded-full bg-white/90 grid place-items-center text-[10px] font-medium text-gray-700">
              {match.pct}%
            </div>
          </div> */}
        </aside>
      </div>
    </Card>
  );
}
