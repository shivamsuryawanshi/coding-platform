import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { api } from '../api';
import type { ProblemDetail, SubmissionResponse, Language } from '../types';
import { LANGUAGES } from '../types';
import { DifficultyBadge } from '../components/DifficultyBadge';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { CodeEditor } from '../components/CodeEditor';
import { VerdictDisplay } from '../components/VerdictDisplay';

export function ProblemDetailPage() {
  const { id } = useParams<{ id: string }>();
  
  const [problem, setProblem] = useState<ProblemDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Editor state
  const [language, setLanguage] = useState<Language>('python');
  const [code, setCode] = useState(LANGUAGES.python.starterCode);
  
  // Submission state
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState<SubmissionResponse | null>(null);

  // Fetch problem
  useEffect(() => {
    async function fetchProblem() {
      if (!id) return;
      
      try {
        setLoading(true);
        const data = await api.getProblem(id);
        setProblem(data);
        setError(null);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load problem');
      } finally {
        setLoading(false);
      }
    }
    fetchProblem();
  }, [id]);

  // Handle language change
  const handleLanguageChange = (newLang: Language) => {
    setLanguage(newLang);
    setCode(LANGUAGES[newLang].starterCode);
    setResult(null);
  };

  // Handle submit
  const handleSubmit = async () => {
    if (!id || !code.trim()) return;
    
    try {
      setSubmitting(true);
      setResult(null);
      
      const response = await api.submit({
        problemId: id,
        language: language,
        code: code
      });
      
      setResult(response);
    } catch (err) {
      setResult({
        submissionId: 0,
        problemId: id,
        language: language,
        verdict: 'Error',
        passed: 0,
        total: 0,
        error: err instanceof Error ? err.message : 'Submission failed',
        timestamp: new Date().toISOString()
      });
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (error || !problem) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="text-center">
          <div className="text-5xl mb-4">😕</div>
          <p className="text-gray-400">{error || 'Problem not found'}</p>
          <Link to="/" className="mt-4 inline-block text-indigo-400 hover:underline">
            ← Back to problems
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="h-[calc(100vh-64px)] flex">
      {/* Problem Panel */}
      <div className="w-1/2 border-r border-gray-800 overflow-y-auto">
        <div className="p-6">
          {/* Header */}
          <div className="mb-6">
            <Link to="/" className="text-gray-500 hover:text-gray-300 text-sm mb-2 inline-block">
              ← Back to problems
            </Link>
            <div className="flex items-center gap-3">
              <h1 className="text-2xl font-bold text-white">{problem.title}</h1>
              <DifficultyBadge difficulty={problem.difficulty} size="md" />
            </div>
            <div className="flex gap-2 mt-2">
              {problem.tags.map(tag => (
                <span key={tag} className="text-xs bg-dark-600 text-gray-400 px-2 py-1 rounded">
                  {tag}
                </span>
              ))}
            </div>
          </div>

          {/* Statement */}
          <section className="mb-6">
            <p className="text-gray-300 leading-relaxed">{problem.statement}</p>
          </section>

          {/* Input Format */}
          <section className="mb-6">
            <h3 className="text-sm font-semibold text-indigo-400 uppercase tracking-wide mb-2">
              Input Format
            </h3>
            <p className="text-gray-400 whitespace-pre-line">{problem.inputFormat}</p>
          </section>

          {/* Output Format */}
          <section className="mb-6">
            <h3 className="text-sm font-semibold text-indigo-400 uppercase tracking-wide mb-2">
              Output Format
            </h3>
            <p className="text-gray-400 whitespace-pre-line">{problem.outputFormat}</p>
          </section>

          {/* Constraints */}
          <section className="mb-6">
            <h3 className="text-sm font-semibold text-indigo-400 uppercase tracking-wide mb-2">
              Constraints
            </h3>
            <ul className="text-gray-400 font-mono text-sm space-y-1">
              {problem.constraints.map((c, i) => (
                <li key={i} className="flex items-start gap-2">
                  <span className="text-indigo-500">•</span>
                  {c}
                </li>
              ))}
            </ul>
          </section>

          {/* Examples */}
          <section className="mb-6">
            <h3 className="text-sm font-semibold text-indigo-400 uppercase tracking-wide mb-3">
              Examples
            </h3>
            <div className="space-y-4">
              {problem.examples.map((ex, i) => (
                <div key={i} className="bg-dark-700 rounded-xl p-4 border border-gray-700">
                  <div className="text-xs text-gray-500 mb-3">Example {i + 1}</div>
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <div className="text-xs text-gray-500 mb-1">Input</div>
                      <pre className="text-emerald-400 font-mono text-sm whitespace-pre-wrap">{ex.input}</pre>
                    </div>
                    <div>
                      <div className="text-xs text-gray-500 mb-1">Output</div>
                      <pre className="text-emerald-400 font-mono text-sm whitespace-pre-wrap">{ex.output}</pre>
                    </div>
                  </div>
                  {ex.explanation && (
                    <div className="mt-3 text-xs text-gray-500">
                      <strong>Explanation:</strong> {ex.explanation}
                    </div>
                  )}
                </div>
              ))}
            </div>
          </section>

          {/* Limits */}
          <section className="flex gap-6 text-sm text-gray-500">
            <div>
              <span className="text-gray-600">Time Limit:</span> {problem.timeLimit}s
            </div>
            <div>
              <span className="text-gray-600">Memory Limit:</span> {problem.memoryLimit}MB
            </div>
            <div>
              <span className="text-gray-600">Test Cases:</span> {problem.testcaseCount}
            </div>
          </section>
        </div>
      </div>

      {/* Editor Panel */}
      <div className="w-1/2 flex flex-col bg-dark-800">
        {/* Code Editor */}
        <div className="flex-1 overflow-hidden">
          <CodeEditor
            code={code}
            onChange={setCode}
            language={language}
            onLanguageChange={handleLanguageChange}
            disabled={submitting}
          />
        </div>

        {/* Submit Bar */}
        <div className="p-4 border-t border-gray-700 bg-dark-700">
          <div className="flex items-center gap-4">
            <button
              onClick={handleSubmit}
              disabled={submitting || !code.trim()}
              className="bg-gradient-to-r from-indigo-500 to-violet-500 hover:from-indigo-600 hover:to-violet-600 text-white font-semibold px-6 py-2.5 rounded-xl transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
            >
              {submitting ? (
                <>
                  <LoadingSpinner size="sm" />
                  Judging...
                </>
              ) : (
                <>
                  <span>▶</span>
                  Submit
                </>
              )}
            </button>
            <span className="text-sm text-gray-500">
              {LANGUAGES[language].name}
            </span>
          </div>
        </div>

        {/* Result Display */}
        {result && (
          <div className="p-4 border-t border-gray-700 max-h-[40%] overflow-y-auto">
            <VerdictDisplay result={result} />
          </div>
        )}
      </div>
    </div>
  );
}

