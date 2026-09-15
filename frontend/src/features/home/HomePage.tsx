import React from 'react';
import { useAuth } from '../../context/AuthContext';
import { Activity, ShieldCheck, LogOut, User, Key, Clock } from 'lucide-react';

export const HomePage: React.FC = () => {
  const { user, logout } = useAuth();

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col justify-between p-6 md:p-12">
      <header className="max-w-4xl mx-auto w-full flex items-center justify-between border-b border-slate-200 pb-4">
        <div className="flex items-center space-x-3">
          <div className="bg-emerald-600 text-white p-2 rounded-lg">
            <Activity className="h-6 w-6" />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Nutri AI</h1>
            <p className="text-xs text-slate-500 font-medium">Authentication & Security Verification</p>
          </div>
        </div>

        <button
          onClick={() => logout()}
          className="inline-flex items-center space-x-2 px-3.5 py-2 border border-slate-300 rounded-lg text-xs font-semibold text-slate-700 bg-white hover:bg-slate-50 transition-colors shadow-sm"
        >
          <LogOut className="h-4 w-4 text-slate-500" />
          <span>Sign Out</span>
        </button>
      </header>

      <main className="max-w-4xl mx-auto w-full my-auto py-10">
        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-8">
          <div className="flex items-center justify-between mb-6">
            <div className="inline-flex items-center space-x-2 px-3 py-1 bg-emerald-50 text-emerald-700 text-xs font-semibold rounded-full uppercase tracking-wider">
              <ShieldCheck className="h-4 w-4 text-emerald-600" />
              <span>Session Authenticated (M03)</span>
            </div>
            <span className="text-xs text-slate-400 font-mono">
              Status: {user?.status}
            </span>
          </div>

          <h2 className="text-2xl font-extrabold text-slate-900 tracking-tight mb-2">
            Welcome, {user?.email}
          </h2>
          <p className="text-slate-600 text-sm mb-6">
            You are securely authenticated. Your short-lived access token is held strictly in memory, and your refresh session is protected via an HttpOnly, SameSite=Lax cookie.
          </p>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 pt-4 border-t border-slate-100">
            <div className="p-4 bg-slate-50 rounded-xl border border-slate-100">
              <div className="flex items-center space-x-2 text-slate-800 font-semibold mb-1">
                <User className="h-4 w-4 text-emerald-600" />
                <span>Account ID</span>
              </div>
              <p className="text-xs text-slate-500 font-mono truncate">{user?.id}</p>
            </div>

            <div className="p-4 bg-slate-50 rounded-xl border border-slate-100">
              <div className="flex items-center space-x-2 text-slate-800 font-semibold mb-1">
                <Key className="h-4 w-4 text-emerald-600" />
                <span>Assigned Roles</span>
              </div>
              <div className="flex flex-wrap gap-1 mt-1">
                {user?.roles.map((role) => (
                  <span
                    key={role}
                    className="inline-block px-2 py-0.5 bg-emerald-100 text-emerald-800 text-[10px] font-semibold rounded"
                  >
                    {role}
                  </span>
                ))}
              </div>
            </div>

            <div className="p-4 bg-slate-50 rounded-xl border border-slate-100">
              <div className="flex items-center space-x-2 text-slate-800 font-semibold mb-1">
                <Clock className="h-4 w-4 text-emerald-600" />
                <span>Created At</span>
              </div>
              <p className="text-xs text-slate-500">
                {user?.createdAt ? new Date(user.createdAt).toLocaleString() : 'N/A'}
              </p>
            </div>
          </div>
        </div>
      </main>

      <footer className="max-w-4xl mx-auto w-full text-center text-xs text-slate-400 pt-6 border-t">
        Nutri AI &copy; 2026. Milestone M03 Authentication & Security Foundation.
      </footer>
    </div>
  );
};

