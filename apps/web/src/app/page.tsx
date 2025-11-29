// src/app/page.tsx
"use client";

import { useEffect, useState } from "react";
import AppBackground from "@/components/AppBackground";
import Splash from "@/components/Splash";
import LandingHero from "@/components/LandingHero";
import LoginPage from "@/components/LoginPage";
import DiscoverView from "@/components/DiscoverView";
import RegisterCard from "@/components/RegisterCard";
import { validateAuthToken } from "@/util/requests";
import SettingsView from "@/components/SettingsView";
import { Role } from "@/util/types";

type View = "landing" | "login" | "register" | "discover" | "settings";

export default function AppPage() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [userRole, setUserRole] = useState<Role | null>(null);

  const [loading, setLoading] = useState(true);
  const [view, setView] = useState<View>("landing");

  const [showSplash, setShowSplash] = useState(false);

  const setViewWrapper = (view: View) => {
    const didChange = checkToken();
    if (!!didChange) return;
    setView(view);
  };

  useEffect(() => {
    const seen = sessionStorage.getItem("splashSeen");
    if (!seen) setShowSplash(true);
  }, []);
  const finishSplash = () => {
    sessionStorage.setItem("splashSeen", "1");
    setShowSplash(false);
  };

  const getAuthToken = () => {
    const match = document.cookie.match(new RegExp('(^| )authToken=([^;]+)'));
    return match ? match[2] : null;
  }

  useEffect(() => {
    checkToken();
    setLoading(false);
  }, []);

  const checkToken: () => boolean = () => {
    const authToken = getAuthToken();
    if (authToken !== null) {
      validateAuthToken().then((valid) => {
        if (valid) {
          setIsLoggedIn(true);
          setUserRole(localStorage.getItem("role") as Role);
          return false;
        } else {
          setIsLoggedIn(false);
          setUserRole(null);
          setView("landing");
          return true;
        }
      });
    }
    return false;
  }

  const handleLogin = (role: Role) => {
    localStorage.setItem("role", role);
    setUserRole(role);
    setIsLoggedIn(true);
    setViewWrapper("discover");
  };

  const handleLogout = () => {
    document.cookie = "authToken=; Max-Age=-99999999;";
    localStorage.removeItem("role");
    localStorage.removeItem("isAuthenticated");
    setIsLoggedIn(false);
    setUserRole(null);
    setViewWrapper("landing");
  };

  const onExplore = () => setViewWrapper(isLoggedIn ? "discover" : "login");
  const onLogin = () => setViewWrapper("login");
  const onCreate = () => setViewWrapper("register");
  const onSettings = () => setViewWrapper("settings");
  const onBackFromSettings = () => setViewWrapper("discover");

  if (loading) return <div className="p-6 text-center">Loading</div>;

  return (
    <main className="relative min-h-screen overflow-hidden text-black">
      <AppBackground />

      {showSplash && (
        <Splash onFinish={finishSplash} brand="Job Matching" />
      )}

      {view === "landing" && (
        <LandingHero onExplore={onExplore} onLogin={onLogin} onCreate={onCreate} />
      )}

      {view === "login" && (
        <div className="grid min-h-screen place-items-center px-4">
          <LoginPage onLogin={handleLogin} />
        </div>
      )}

      {view === "register" && (
        <div className="grid min-h-screen place-items-center px-4">
          <RegisterCard onLogin={handleLogin} />
        </div>
      )}

      {view === "discover" && (
        <DiscoverView userRole={userRole} onLogout={handleLogout} onSettings={onSettings} />
      )}

      {view === "settings" && (
        <div className="grid min-h-screen place-items-center px-4">
          <SettingsView userRole={userRole} onBack={onBackFromSettings} />
        </div>
      )}
    </main>
  );
}
