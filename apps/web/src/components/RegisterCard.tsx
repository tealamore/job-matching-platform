"use client";

import { useState } from "react";
import { register } from "@/util/requests";
import { Role } from "@/util/types";

export default function RegisterCard({ onLogin }: { onLogin: (role: Role) => void }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [error, setError] = useState("");
  const [role, setRole] = useState<Role>("JOB_SEEKER");

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await register(email, password, name, phone, role);
      onLogin(role);
    } catch (error: any) {
      setError("Invalid username or password");
    }
  };

  return (
    <div className="w-[420px] rounded-2xl border bg-white/95 p-8 shadow-sm">
      <h1 className="text-xl font-semibold tracking-tight text-black">Create account as {role === "JOB_SEEKER" ? "Job Seeker" : "Employer"}</h1>

      <button className="mt-2 text-sm text-black" onClick={() => setRole(role === "JOB_SEEKER" ? "BUSINESS" : "JOB_SEEKER")}>
        Did you mean to sign up as {role === "JOB_SEEKER" ? "Employer" : "Job Seeker"}?
      </button>

      <form onSubmit={submit} className="mt-6 space-y-4">
        <div>
          <label className="mb-1 block text-xs font-medium text-gray-700">Email</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="w-full rounded-lg border text-black px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900/20"
            placeholder="you@example.com"
            required
          />
        </div>
        <div>
          <label className="mb-1 block text-xs font-medium text-gray-700">Password</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full rounded-lg border text-black px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900/20"
            placeholder="••••••••"
            required
          />
        </div>

        <div>
          <label className="mb-1 block text-xs font-medium text-gray-700">Name</label>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="w-full rounded-lg border text-black px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900/20"
            placeholder="name"
            required
          />
        </div>

        <div>
          <label className="mb-1 block text-xs font-medium text-gray-700">Phone</label>
          <input
            type="text"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
            className="w-full rounded-lg border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900/20 text-black"
            placeholder="phone"
            required
          />
        </div>

        <button
          type="submit"
          className="w-full rounded-lg bg-gray-900 px-4 py-2 text-sm font-medium text-white transition hover:shadow-sm active:translate-y-px"
        >
          Sign up
        </button>

        {error && <p className="text-red-500">{error}</p>}
      </form>
    </div>
  );
}
