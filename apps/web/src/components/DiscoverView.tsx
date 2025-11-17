// src/components/DiscoverView.tsx
'use client';
import { useState, useEffect } from 'react';
import SwipeDeck, { type SwipeDirection } from '@/components/SwipeDeck';
import EmployerCard, { type Employer } from '@/components/EmployerCard';
import JobCard, { type Job } from '@/components/JobCard';
import { sampleEmployers } from '@/data/sample';
import axios from 'axios';

type Role = "JOB_SEEKER" | "BUSINESS";

function EndOfDeck({
  total,
  liked,
  noped,
}: {
  total: number;
  liked: number;
  noped: number;
}) {
  const rate = total ? Math.round((liked / total) * 100) : 0;
  return (
    <div className="grid h-full place-items-center">
      <div className="rounded-2xl border bg-white/90 p-8 text-center shadow-sm">
        <div className="text-3xl mb-1">🎉</div>
        <h3 className="text-xl font-semibold">You’re all caught up!</h3>
        <p className="mt-1 text-sm text-gray-600">Nice work — here’s how you did.</p>

        <div className="mt-6 grid grid-cols-3 gap-4 max-w-md mx-auto">
          <div className="rounded-xl border bg-white px-4 py-3">
            <div className="text-lg font-semibold">{total}</div>
            <div className="text-[11px] text-gray-600">Reviewed</div>
          </div>
          <div className="rounded-xl border bg-white px-4 py-3">
            <div className="text-lg font-semibold">{liked}</div>
            <div className="text-[11px] text-gray-600">Saved</div>
          </div>
          <div className="rounded-xl border bg-white px-4 py-3">
            <div className="text-lg font-semibold">{noped}</div>
            <div className="text-[11px] text-gray-600">Dismissed</div>
          </div>
        </div>

        <div className="mt-4 text-sm text-gray-700">
          Save rate: <span className="font-medium">{rate}%</span>
        </div>
      </div>
    </div>
  );
}

export default function DiscoverView({
  userRole,
  onLogout,
}: {
  userRole: Role | null;
  onLogout: () => void;
}) {
  const [lastAction, setLastAction] = useState<string | null>(null);
  const [jobs, setJobs] = useState<Job[]>([]);

  const [jobStats, setJobStats] = useState({ liked: 0, noped: 0 });
  const [empStats, setEmpStats] = useState({ liked: 0, noped: 0 });

  const onSwipeJob = (dir: SwipeDirection, item: Job) => {
    setLastAction(`${dir === 'right' ? 'Saved' : 'Dismissed'}: ${item.title}`);
    setJobStats((s) => ({
      liked: s.liked + (dir === 'right' ? 1 : 0),
      noped: s.noped + (dir === 'left' ? 1 : 0),
    }));
  };

  const onSwipeEmployer = (dir: SwipeDirection, item: Employer) => {
    setLastAction(`${dir === 'right' ? 'Saved' : 'Dismissed'}: ${item.name}`);
    setEmpStats((s) => ({
      liked: s.liked + (dir === 'right' ? 1 : 0),
      noped: s.noped + (dir === 'left' ? 1 : 0),
    }));
  };

  const jobEmpty = (
    <EndOfDeck total={jobStats.liked + jobStats.noped} liked={jobStats.liked} noped={jobStats.noped} />
  );
  const empEmpty = (
    <EndOfDeck total={empStats.liked + empStats.noped} liked={empStats.liked} noped={empStats.noped} />
  );

  useEffect(() => {
    const fetchJobs = async () => {
      try {
        const response = await axios.get<Job[]>('/jobs/feed', {withCredentials: true});
        setJobs(response.data);
      } catch (err) {
        console.error("failed to load jobs", err);
      } 
    }

    fetchJobs()
  }, [])

  return (
    <div className="mx-auto grid h-[100dvh] max-w-6xl grid-rows-[auto_1fr_auto] gap-3 px-4 py-4 overflow-visible">
      <header className="grid grid-cols-[1fr_auto_1fr] items-center">
        <div className="flex items-center gap-2">
          <div className="grid h-8 w-8 place-items-center rounded-lg bg-white text-[0.9rem] font-black text-gray-900 shadow-sm">
            FM
          </div>
          <span className="text-sm font-medium text-white/90">Fair Match</span>
        </div>

        <div className="justify-self-end flex items-center gap-2">
          <button
            onClick={onLogout}
            className="rounded-lg border border-white/30 bg-white/10 px-3 py-1.5 text-sm text-white backdrop-blur transition hover:bg-white/20 active:translate-y-px"
          >
            Log out
          </button>
        </div>
      </header>

      <div className="flex items-start justify-center pt-2 md:pt-4 overflow-visible">
        {userRole === 'JOB_SEEKER' ? (
          <SwipeDeck<Job>
            items={jobs}
            onSwipe={onSwipeJob}
            width="clamp(36ch, 42vw, 60ch)"
            controlsInside
            showButtons={true}
            progressVariant="chip"
            emptyState={jobEmpty}
            renderItem={(job) => <JobCard job={job} />}
          />
        ) : (
          <SwipeDeck<Employer>
            items={sampleEmployers}
            onSwipe={onSwipeEmployer}
            width="clamp(36ch, 42vw, 60ch)"
            controlsInside
            showButtons={false}
            progressVariant="chip"
            emptyState={empEmpty}
            renderItem={(employer) => <EmployerCard employer={employer} />}
          />
        )}
      </div>

      <div className="pointer-events-none select-none text-center text-sm text-white/90">
        {lastAction ? lastAction : 'Tip: drag or use ← / → (A / D).'}
      </div>
    </div>
  );
}
