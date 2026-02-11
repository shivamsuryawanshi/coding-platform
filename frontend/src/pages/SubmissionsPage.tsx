import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api';
import type { SubmissionHistory, PaginatedResponse } from '../types';
import { LoadingSpinner } from '../components/LoadingSpinner';

function getStatusColor(status: string): string {
  switch (status) {
    case 'ACCEPTED': return 'text-green-400';
    case 'WRONG_ANSWER': return 'text-red-400';
    case 'TLE': return 'text-yellow-400';
    case 'RE': return 'text-orange-400';
    case 'CE': return 'text-purple-400';
    case 'QUEUED': return 'text-blue-400';
    case 'RUNNING': return 'text-indigo-400';
    default: return 'text-gray-400';
  }
}

export function SubmissionsPage() {
  const [submissions, setSubmissions] = useState<PaginatedResponse<SubmissionHistory> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);

  useEffect(() => {
    fetchSubmissions();
  }, [page]);

  const fetchSubmissions = async () => {
    try {
      setLoading(true);
      const data = await api.getMySubmissions(page, 20);
      setSubmissions(data);
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load submissions');
    } finally {
      setLoading(false);
    }
  };

  if (loading && !submissions) {
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
          <div className="text-5xl mb-4">😕</div>
          <p className="text-gray-400">{error}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto p-6">
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-white mb-2">My Submissions</h1>
        <p className="text-gray-400">View your submission history</p>
      </div>

      {submissions && submissions.content.length === 0 ? (
        <div className="text-center py-12">
          <div className="text-5xl mb-4">📝</div>
          <p className="text-gray-400">No submissions yet</p>
          <Link to="/" className="mt-4 inline-block text-indigo-400 hover:underline">
            Start solving problems →
          </Link>
        </div>
      ) : (
        <>
          <div className="bg-dark-800 rounded-xl border border-gray-700 overflow-hidden">
            <table className="w-full">
              <thead className="bg-dark-700">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-400 uppercase">ID</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-400 uppercase">Problem</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-400 uppercase">Language</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-400 uppercase">Status</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-400 uppercase">Tests</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-400 uppercase">Time</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-700">
                {submissions?.content.map((sub) => (
                  <tr key={sub.id} className="hover:bg-dark-700/50">
                    <td className="px-6 py-4 text-sm text-gray-300">#{sub.id}</td>
                    <td className="px-6 py-4">
                      <Link
                        to={`/problem/${sub.problemId}`}
                        className="text-indigo-400 hover:text-indigo-300 font-medium"
                      >
                        {sub.problemTitle}
                      </Link>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-400">{sub.language}</td>
                    <td className="px-6 py-4">
                      <span className={`text-sm font-medium ${getStatusColor(sub.status)}`}>
                        {sub.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-400">
                      {sub.passedTests}/{sub.totalTests}
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-500">
                      {new Date(sub.submittedAt).toLocaleString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {submissions && submissions.totalPages > 1 && (
            <div className="mt-6 flex items-center justify-center gap-2">
              <button
                onClick={() => setPage(p => Math.max(0, p - 1))}
                disabled={!submissions.hasPrevious}
                className="px-4 py-2 bg-dark-800 border border-gray-700 rounded-lg text-gray-300 hover:bg-dark-700 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Previous
              </button>
              <span className="px-4 py-2 text-gray-400">
                Page {page + 1} of {submissions.totalPages}
              </span>
              <button
                onClick={() => setPage(p => p + 1)}
                disabled={!submissions.hasNext}
                className="px-4 py-2 bg-dark-800 border border-gray-700 rounded-lg text-gray-300 hover:bg-dark-700 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}

