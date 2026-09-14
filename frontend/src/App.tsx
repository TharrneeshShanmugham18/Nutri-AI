import React from 'react';
import { Activity, Shield, Cpu, Database } from 'lucide-react';

export const App: React.FC = () => {
  return (
    <div className="min-h-screen bg-slate-50 flex flex-col justify-between p-6 md:p-12">
      <header className="max-w-4xl mx-auto w-full flex items-center justify-between border-b pb-4">
        <div className="flex items-center space-x-3">
          <div className="bg-emerald-600 text-white p-2 rounded-lg">
            <Activity className="h-6 w-6" />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Nutri AI</h1>
            <p className="text-xs text-slate-500 font-medium">Budget-Aware Personalized Nutrition Platform</p>
          </div>
        </div>
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-800">
          Foundation Active
        </span>
      </header>

      <main className="max-w-4xl mx-auto w-full my-auto py-12">
        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-8">
          <div className="inline-block px-3 py-1 bg-slate-100 text-slate-700 text-xs font-semibold rounded-full uppercase tracking-wider mb-4">
            Milestone M01 Verification
          </div>
          <h2 className="text-3xl font-extrabold text-slate-900 tracking-tight mb-4">
            Project Foundation Established
          </h2>
          <p className="text-slate-600 leading-relaxed mb-8">
            The full-stack modular monolith baseline is cleanly configured. Core business logic, deterministic nutrition engines, and database migrations will be introduced in subsequent authorized milestones.
          </p>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 pt-4 border-t border-slate-100">
            <div className="p-4 bg-slate-50 rounded-xl border border-slate-100">
              <div className="flex items-center space-x-2 text-slate-800 font-semibold mb-1">
                <Cpu className="h-4 w-4 text-emerald-600" />
                <span>Backend Engine</span>
              </div>
              <p className="text-xs text-slate-500">Java 25 LTS • Spring Boot 4.1.1</p>
            </div>

            <div className="p-4 bg-slate-50 rounded-xl border border-slate-100">
              <div className="flex items-center space-x-2 text-slate-800 font-semibold mb-1">
                <Shield className="h-4 w-4 text-emerald-600" />
                <span>Frontend Architecture</span>
              </div>
              <p className="text-xs text-slate-500">React 19 • TypeScript • Vite 6</p>
            </div>

            <div className="p-4 bg-slate-50 rounded-xl border border-slate-100">
              <div className="flex items-center space-x-2 text-slate-800 font-semibold mb-1">
                <Database className="h-4 w-4 text-emerald-600" />
                <span>Persistence</span>
              </div>
              <p className="text-xs text-slate-500">PostgreSQL 16.8 (ARM64 Docker)</p>
            </div>
          </div>
        </div>
      </main>

      <footer className="max-w-4xl mx-auto w-full text-center text-xs text-slate-400 pt-6 border-t">
        Nutri AI &copy; 2026. Modular Monolith Architecture.
      </footer>
    </div>
  );
};

export default App;

