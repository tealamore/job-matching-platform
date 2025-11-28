// src/components/DiscoverView.tsx
'use client';
import { useState, useEffect } from 'react';
import SwipeDeck, { type SwipeDirection } from '@/components/SwipeDeck';
import EmployerDashboard from '@/components/EmployerDashboard';
import JobCard, { type Job } from '@/components/JobCard';
import { fetchJobs } from '@/requests/requests';

type Role = "JOB_SEEKER" | "BUSINESS";

function EndOfDeck({ }: {}) {
  return (
    <div className="grid h-full place-items-center">
      <div className="rounded-2xl border bg-white/90 p-8 text-center shadow-sm">
        <h3 className="text-xl font-semibold text-black">You're all caught up!</h3>
        <p className="mt-1 text-sm text-black">Nice work</p>
      </div>
    </div>
  );
}

export default function DiscoverView({
  userRole,
  onLogout,
  onSettings,
}: {
  userRole: Role | null;
  onLogout: () => void;
  onSettings: () => void;
}) {
  const [lastAction, setLastAction] = useState<string | null>(null);
  const [jobs, setJobs] = useState<Job[]>([]);

  const onSwipeJob = (dir: SwipeDirection, item: Job) => {
    setLastAction(`${dir === 'right' ? 'Saved' : 'Dismissed'}: ${item.title}`);
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
            onClick={onSettings}
            className="rounded-lg border border-white/30 bg-white/10 px-3 py-1.5 text-sm text-white backdrop-blur transition hover:bg-white/20 active:translate-y-px"
          >
            Settings
          </button>
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
          <EmployerDashboard />
        )}
      </div>
    </div>
  );
}
