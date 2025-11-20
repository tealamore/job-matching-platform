// src/components/DiscoverView.tsx
'use client';
import { useState, useEffect } from 'react';
import SwipeDeck, { type SwipeDirection } from '@/components/SwipeDeck';
import EmployerCard, { type Employer } from '@/components/EmployerCard';
import JobCard, { type Job } from '@/components/JobCard';
import { sampleEmployers } from '@/data/sample';
import { fetchJobs } from '@/requests/requests';

type Role = "JOB_SEEKER" | "BUSINESS";

function EndOfDeck({}: {}) {
  return (
    <div className="grid h-full place-items-center">
      <div className="rounded-2xl border bg-white/90 p-8 text-center shadow-sm">
        <div className="text-3xl mb-1">🎉</div>
        <h3 className="text-xl font-semibold">You’re all caught up!</h3>
        <p className="mt-1 text-sm text-gray-600">Nice work — here’s how you did.</p>
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

  const onSwipeJob = (dir: SwipeDirection, item: Job) => {
    setLastAction(`${dir === 'right' ? 'Saved' : 'Dismissed'}: ${item.title}`);
  };

  const onSwipeEmployer = (dir: SwipeDirection, item: Employer) => {
    setLastAction(`${dir === 'right' ? 'Saved' : 'Dismissed'}: ${item.name}`);
  };

  const empty = (
    <EndOfDeck />
  );

  useEffect(() => {
    fetchJobs().then(setJobs);
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
            emptyState={empty}
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
            emptyState={empty}
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
