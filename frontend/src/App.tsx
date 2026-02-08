import { useState, useEffect } from 'react'

// Types
interface Problem {
  id: number
  title: string
  description: string
  input_format: string
  output_format: string
  constraints: string[]
  examples: {
    input: string
    output: string
    explanation?: string
  }[]
  difficulty: string
}

interface SubmissionResult {
  verdict: string
  passed: number
  total: number
  failedTest?: {
    id: number
    input: string
    expected: string
    actual: string
  }
  error?: string
  timestamp: string
}

// Language configurations with starter code
const LANGUAGES = {
  python: {
    name: 'Python',
    starterCode: `# Read two integers and print their sum
a, b = map(int, input().split())
print(a + b)
`
  },
  cpp: {
    name: 'C++',
    starterCode: `#include <iostream>
using namespace std;

int main() {
    int a, b;
    cin >> a >> b;
    cout << a + b << endl;
    return 0;
}
`
  },
  java: {
    name: 'Java',
    starterCode: `import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int a = sc.nextInt();
        int b = sc.nextInt();
        System.out.println(a + b);
    }
}
`
  },
  js: {
    name: 'JavaScript',
    starterCode: `const readline = require('readline');
const rl = readline.createInterface({ input: process.stdin });

rl.on('line', (line) => {
    const [a, b] = line.split(' ').map(Number);
    console.log(a + b);
    rl.close();
});
`
  }
}

// API Configuration
const API_BASE = '/api'

function App() {
  const [problem, setProblem] = useState<Problem | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  
  const [language, setLanguage] = useState<keyof typeof LANGUAGES>('python')
  const [code, setCode] = useState(LANGUAGES.python.starterCode)
  
  const [submitting, setSubmitting] = useState(false)
  const [result, setResult] = useState<SubmissionResult | null>(null)

  // Fetch problem on mount
  useEffect(() => {
    fetchProblem()
  }, [])

  // Update code when language changes
  const handleLanguageChange = (newLang: keyof typeof LANGUAGES) => {
    setLanguage(newLang)
    setCode(LANGUAGES[newLang].starterCode)
    setResult(null)
  }

  async function fetchProblem() {
    try {
      setLoading(true)
      setError(null)
      const response = await fetch(`${API_BASE}/problem`)
      if (!response.ok) {
        throw new Error('Failed to fetch problem')
      }
      const data = await response.json()
      setProblem(data)
    } catch (err) {
      setError('Unable to load problem. Make sure the backend is running.')
      console.error('Error fetching problem:', err)
    } finally {
      setLoading(false)
    }
  }

  async function handleSubmit() {
    if (!code.trim()) {
      setResult({
        verdict: 'Error',
        passed: 0,
        total: 0,
        error: 'Code cannot be empty',
        timestamp: new Date().toISOString()
      })
      return
    }

    try {
      setSubmitting(true)
      setResult(null)

      const response = await fetch(`${API_BASE}/submit`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          language,
          code
        })
      })

      const data = await response.json()
      setResult(data)
    } catch (err) {
      setResult({
        verdict: 'Error',
        passed: 0,
        total: 0,
        error: 'Submission failed. Check if backend is running.',
        timestamp: new Date().toISOString()
      })
      console.error('Error submitting code:', err)
    } finally {
      setSubmitting(false)
    }
  }

  // Render verdict icon and class
  function getVerdictInfo(verdict: string) {
    const v = verdict.toLowerCase()
    if (v === 'accepted') {
      return { icon: '✓', class: 'accepted' }
    } else if (v.includes('wrong')) {
      return { icon: '✗', class: 'wrong' }
    } else {
      return { icon: '!', class: 'error' }
    }
  }

  return (
    <div className="app-container">
      {/* Header */}
      <header className="header">
        <div className="logo">
          <div className="logo-icon">CN</div>
          <span className="logo-text">CodeNexus</span>
        </div>
        <div className="header-badge">
          Testing Environment
        </div>
      </header>

      {/* Main Content */}
      <main className="main-grid">
        {/* Problem Panel */}
        <div className="panel">
          <div className="panel-header">
            <span className="panel-title">Problem</span>
          </div>
          <div className="panel-content">
            {loading ? (
              <div className="loading-state">Loading problem...</div>
            ) : error ? (
              <div className="error-message">{error}</div>
            ) : problem ? (
              <>
                <div className="problem-header">
                  <h1 className="problem-title">{problem.title}</h1>
                  <span className={`difficulty-badge ${problem.difficulty.toLowerCase()}`}>
                    {problem.difficulty}
                  </span>
                </div>

                <p className="problem-description">{problem.description}</p>

                <div className="problem-section">
                  <div className="section-label">Input Format</div>
                  <div className="section-content">{problem.input_format}</div>
                </div>

                <div className="problem-section">
                  <div className="section-label">Output Format</div>
                  <div className="section-content">{problem.output_format}</div>
                </div>

                <div className="problem-section">
                  <div className="section-label">Constraints</div>
                  <ul className="constraints-list">
                    {problem.constraints.map((c, i) => (
                      <li key={i}>{c}</li>
                    ))}
                  </ul>
                </div>

                <div className="problem-section">
                  <div className="section-label">Examples</div>
                  {problem.examples.map((ex, i) => (
                    <div key={i} className="example-box">
                      <div className="example-label">Example {i + 1}</div>
                      <div className="example-io">
                        <div className="io-block">
                          <div className="io-label">Input</div>
                          <div className="io-value">{ex.input}</div>
                        </div>
                        <div className="io-block">
                          <div className="io-label">Output</div>
                          <div className="io-value">{ex.output}</div>
                        </div>
                      </div>
                      {ex.explanation && (
                        <div style={{ marginTop: 8, fontSize: 13, color: 'var(--text-muted)' }}>
                          Explanation: {ex.explanation}
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              </>
            ) : null}
          </div>
        </div>

        {/* Editor Panel */}
        <div className="panel">
          <div className="panel-header">
            <span className="panel-title">Code Editor</span>
            <div className="editor-controls">
              <select
                className="language-select"
                value={language}
                onChange={(e) => handleLanguageChange(e.target.value as keyof typeof LANGUAGES)}
              >
                {Object.entries(LANGUAGES).map(([key, value]) => (
                  <option key={key} value={key}>{value.name}</option>
                ))}
              </select>
            </div>
          </div>
          <div className="panel-content">
            <textarea
              className="code-editor"
              value={code}
              onChange={(e) => setCode(e.target.value)}
              placeholder="Write your code here..."
              spellCheck={false}
            />

            <div className="submit-section">
              <button
                className={`submit-btn ${submitting ? 'loading' : ''}`}
                onClick={handleSubmit}
                disabled={submitting}
              >
                {submitting ? 'Judging...' : 'Submit Code'}
              </button>
              {result && (
                <span style={{ color: 'var(--text-secondary)', fontSize: 13 }}>
                  Language: {LANGUAGES[language].name}
                </span>
              )}
            </div>

            {/* Verdict Display */}
            {result && (
              <div className="verdict-container">
                <div className="verdict-header">
                  <div className={`verdict-icon ${getVerdictInfo(result.verdict).class}`}>
                    {getVerdictInfo(result.verdict).icon}
                  </div>
                  <span className={`verdict-text ${getVerdictInfo(result.verdict).class}`}>
                    {result.verdict}
                  </span>
                </div>

                {result.error ? (
                  <div className="verdict-details">{result.error}</div>
                ) : (
                  <>
                    <div className="verdict-details">
                      Passed {result.passed} of {result.total} test cases
                    </div>
                    
                    <div className="test-progress">
                      {Array.from({ length: result.total }).map((_, i) => (
                        <div
                          key={i}
                          className={`test-dot ${i < result.passed ? 'passed' : 'failed'}`}
                        />
                      ))}
                    </div>

                    {result.failedTest && (
                      <div className="failed-test-info">
                        <h4>Failed Test Case</h4>
                        <div className="failed-test-details">
                          <div><strong>Input:</strong> {result.failedTest.input}</div>
                          <div><strong>Expected:</strong> {result.failedTest.expected}</div>
                          <div><strong>Your Output:</strong> {result.failedTest.actual}</div>
                        </div>
                      </div>
                    )}
                  </>
                )}
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  )
}

export default App

