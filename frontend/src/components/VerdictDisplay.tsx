import type { SubmissionResponse } from '../types';

interface Props {
  result: SubmissionResponse;
}

export function VerdictDisplay({ result }: Props) {
  const isAccepted = result.verdict === 'Accepted';
  const isError = result.verdict === 'Error' || result.error;
  
  const getVerdictColor = () => {
    if (isAccepted) return 'text-emerald-400';
    if (isError) return 'text-amber-400';
    return 'text-red-400';
  };

  const getVerdictIcon = () => {
    if (isAccepted) return '✓';
    if (isError) return '!';
    return '✗';
  };

  const getVerdictBg = () => {
    if (isAccepted) return 'bg-emerald-500/10 border-emerald-500/30';
    if (isError) return 'bg-amber-500/10 border-amber-500/30';
    return 'bg-red-500/10 border-red-500/30';
  };

  return (
    <div className={`animate-fade-in rounded-xl border p-4 ${getVerdictBg()}`}>
      {/* Header */}
      <div className="flex items-center gap-3 mb-3">
        <div className={`w-10 h-10 rounded-full flex items-center justify-center text-xl font-bold ${getVerdictColor()} bg-current/10`}>
          {getVerdictIcon()}
        </div>
        <div>
          <h3 className={`text-lg font-semibold ${getVerdictColor()}`}>
            {result.verdict}
          </h3>
          {!isError && (
            <p className="text-sm text-gray-400">
              Passed {result.passed} of {result.total} test cases
            </p>
          )}
        </div>
      </div>

      {/* Error message */}
      {result.error && (
        <div className="mt-3 p-3 bg-dark-800 rounded-lg">
          <p className="text-sm text-gray-300">{result.error}</p>
        </div>
      )}

      {/* Test progress */}
      {!isError && result.total > 0 && (
        <div className="flex gap-1.5 mt-3">
          {Array.from({ length: result.total }).map((_, i) => (
            <div
              key={i}
              className={`w-3 h-3 rounded-full ${
                i < result.passed ? 'bg-emerald-500' : 'bg-red-500'
              }`}
            />
          ))}
        </div>
      )}

      {/* Failed test details */}
      {result.failedTest && (
        <div className="mt-4 p-3 bg-dark-800 rounded-lg border border-red-500/20">
          <h4 className="text-sm font-medium text-red-400 mb-2">
            Failed Test Case #{result.failedTest.testId}
          </h4>
          <div className="space-y-2 text-sm font-mono">
            <div>
              <span className="text-gray-500">Input: </span>
              <pre className="text-gray-300 whitespace-pre-wrap">{result.failedTest.input}</pre>
            </div>
            <div>
              <span className="text-gray-500">Expected: </span>
              <pre className="text-emerald-400 whitespace-pre-wrap">{result.failedTest.expected}</pre>
            </div>
            <div>
              <span className="text-gray-500">Your Output: </span>
              <pre className="text-red-400 whitespace-pre-wrap">{result.failedTest.actual}</pre>
            </div>
            {result.failedTest.error && (
              <div>
                <span className="text-gray-500">Error: </span>
                <pre className="text-amber-400 whitespace-pre-wrap">{result.failedTest.error}</pre>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

