import { useState, useEffect, useMemo } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api';
import type { ProblemListItem, Stats } from '../types';
import { DifficultyBadge } from '../components/DifficultyBadge';
import { LoadingSpinner } from '../components/LoadingSpinner';

export function ProblemListPage() {
  const [problems, setProblems] = useState<ProblemListItem[]>([]);
  const [categories, setCategories] = useState<string[]>([]);
  const [stats, setStats] = useState<Stats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Filters
  const [selectedCategory, setSelectedCategory] = useState<string>('');
  const [selectedDifficulty, setSelectedDifficulty] = useState<string>('');
  const [searchQuery, setSearchQuery] = useState('');

  // Fetch data
  useEffect(() => {
    async function fetchData() {
      try {
        setLoading(true);
        const [problemsData, categoriesData, statsData] = await Promise.all([
          api.getProblems(),
          api.getCategories(),
          api.getStats()
        ]);
        setProblems(problemsData);
        setCategories(categoriesData);
        setStats(statsData);
        setError(null);
      } catch (err) {
        setError('Failed to load problems. Make sure the backend is running.');
        console.error(err);
      } finally {
        setLoading(false);
      }
    }
    fetchData();
  }, []);

  // Filter problems
  const filteredProblems = useMemo(() => {
    return problems.filter(p => {
      if (selectedCategory && p.category !== selectedCategory) return false;
      if (selectedDifficulty && p.difficulty !== selectedDifficulty) return false;
      if (searchQuery && !p.title.toLowerCase().includes(searchQuery.toLowerCase())) return false;
      return true;
    });
  }, [problems, selectedCategory, selectedDifficulty, searchQuery]);

  // Group by category
  const groupedProblems = useMemo(() => {
    const groups: Record<string, ProblemListItem[]> = {};
    filteredProblems.forEach(p => {
      if (!groups[p.category]) groups[p.category] = [];
      groups[p.category].push(p);
    });
    return groups;
  }, [filteredProblems]);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="text-center">
          <div className="text-5xl mb-4">⚠️</div>
          <p className="text-gray-400">{error}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Stats */}
      {stats && (
        <div className="grid grid-cols-2 md:grid-cols-5 gap-4 mb-8">
          <StatCard label="Total" value={stats.total} color="indigo" />
          <StatCard label="Easy" value={stats.easy} color="emerald" />
          <StatCard label="Medium" value={stats.medium} color="amber" />
          <StatCard label="Hard" value={stats.hard} color="red" />
          <StatCard label="Categories" value={stats.categories} color="violet" />
        </div>
      )}

      {/* Filters */}
      <div className="flex flex-wrap gap-4 mb-8">
        <input
          type="text"
          placeholder="Search problems..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="flex-1 min-w-[200px] bg-dark-700 border border-gray-700 rounded-xl px-4 py-2.5 text-gray-200 placeholder-gray-500 focus:outline-none focus:border-indigo-500"
        />
        
        <select
          value={selectedCategory}
          onChange={(e) => setSelectedCategory(e.target.value)}
          className="bg-dark-700 border border-gray-700 rounded-xl px-4 py-2.5 text-gray-200 focus:outline-none focus:border-indigo-500"
        >
          <option value="">All Categories</option>
          {categories.map(cat => (
            <option key={cat} value={cat}>{formatCategory(cat)}</option>
          ))}
        </select>

        <select
          value={selectedDifficulty}
          onChange={(e) => setSelectedDifficulty(e.target.value)}
          className="bg-dark-700 border border-gray-700 rounded-xl px-4 py-2.5 text-gray-200 focus:outline-none focus:border-indigo-500"
        >
          <option value="">All Difficulties</option>
          <option value="easy">Easy</option>
          <option value="medium">Medium</option>
          <option value="hard">Hard</option>
        </select>
      </div>

      {/* Problem List */}
      {Object.keys(groupedProblems).length === 0 ? (
        <div className="text-center py-12 text-gray-500">
          No problems found matching your filters.
        </div>
      ) : (
        <div className="space-y-8">
          {Object.entries(groupedProblems).map(([category, probs]) => (
            <div key={category}>
              <h2 className="text-lg font-semibold text-gray-300 mb-4 flex items-center gap-2">
                <span className="w-2 h-2 bg-indigo-500 rounded-full"></span>
                {formatCategory(category)}
                <span className="text-gray-600 text-sm font-normal">({probs.length})</span>
              </h2>
              
              <div className="bg-dark-800 rounded-xl border border-gray-800 overflow-hidden">
                <table className="w-full">
                  <thead>
                    <tr className="border-b border-gray-800">
                      <th className="text-left px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Title</th>
                      <th className="text-left px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Difficulty</th>
                      <th className="text-left px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider hidden md:table-cell">Time Limit</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-800">
                    {probs.map(problem => (
                      <tr key={problem.id} className="hover:bg-dark-700 transition-colors">
                        <td className="px-6 py-4">
                          <Link 
                            to={`/problem/${problem.id}`}
                            className="text-gray-200 hover:text-indigo-400 transition-colors font-medium"
                          >
                            {problem.title}
                          </Link>
                        </td>
                        <td className="px-6 py-4">
                          <DifficultyBadge difficulty={problem.difficulty} />
                        </td>
                        <td className="px-6 py-4 text-gray-500 text-sm hidden md:table-cell">
                          {problem.timeLimit}s
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

function StatCard({ label, value, color }: { label: string; value: number; color: string }) {
  const colorClasses: Record<string, string> = {
    indigo: 'from-indigo-500/20 to-indigo-500/5 border-indigo-500/30',
    emerald: 'from-emerald-500/20 to-emerald-500/5 border-emerald-500/30',
    amber: 'from-amber-500/20 to-amber-500/5 border-amber-500/30',
    red: 'from-red-500/20 to-red-500/5 border-red-500/30',
    violet: 'from-violet-500/20 to-violet-500/5 border-violet-500/30',
  };

  return (
    <div className={`bg-gradient-to-br ${colorClasses[color]} border rounded-xl p-4`}>
      <div className="text-2xl font-bold text-white">{value}</div>
      <div className="text-sm text-gray-400">{label}</div>
    </div>
  );
}

function formatCategory(category: string): string {
  return category
    .split('_')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
}

