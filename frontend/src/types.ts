// API Response Types

export interface ProblemListItem {
  id: string;
  title: string;
  category: string;
  difficulty: 'easy' | 'medium' | 'hard';
  timeLimit: number;
  memoryLimit: number;
}

export interface ProblemDetail {
  id: string;
  title: string;
  category: string;
  difficulty: 'easy' | 'medium' | 'hard';
  statement: string;
  inputFormat: string;
  outputFormat: string;
  constraints: string[];
  timeLimit: number;
  memoryLimit: number;
  tags: string[];
  examples: Example[];
  testcaseCount: number;
}

export interface Example {
  input: string;
  output: string;
  explanation?: string;
}

export interface SubmissionRequest {
  problemId: string;
  language: string;
  code: string;
}

export interface SubmissionResponse {
  submissionId: number;
  problemId: string;
  language: string;
  verdict: string;
  passed: number;
  total: number;
  failedTest?: FailedTest;
  error?: string;
  timestamp: string;
}

export interface FailedTest {
  testId: number;
  input: string;
  expected: string;
  actual: string;
  error?: string;
}

export interface Stats {
  total: number;
  easy: number;
  medium: number;
  hard: number;
  categories: number;
}

export interface AuthRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  email: string;
  userId: number;
}

export interface SubmissionHistory {
  id: number;
  problemId: string;
  problemTitle: string;
  language: string;
  status: string;
  verdict: string;
  passedTests: number;
  totalTests: number;
  submittedAt: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export type Language = 'python' | 'cpp' | 'java' | 'javascript';

export const LANGUAGES: Record<Language, { name: string; extension: string; starterCode: string }> = {
  python: {
    name: 'Python 3',
    extension: 'py',
    starterCode: `# Write your solution here
def solve():
    # Read input
    n = int(input())
    arr = list(map(int, input().split()))
    
    # Your code here
    result = sum(arr)
    
    print(result)

solve()
`
  },
  cpp: {
    name: 'C++ 17',
    extension: 'cpp',
    starterCode: `#include <iostream>
#include <vector>
using namespace std;

int main() {
    ios_base::sync_with_stdio(false);
    cin.tie(nullptr);
    
    int n;
    cin >> n;
    
    vector<int> arr(n);
    for (int i = 0; i < n; i++) {
        cin >> arr[i];
    }
    
    // Your code here
    long long sum = 0;
    for (int x : arr) sum += x;
    
    cout << sum << endl;
    return 0;
}
`
  },
  java: {
    name: 'Java 17',
    extension: 'java',
    starterCode: `import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        
        int n = sc.nextInt();
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = sc.nextInt();
        }
        
        // Your code here
        long sum = 0;
        for (int x : arr) sum += x;
        
        System.out.println(sum);
    }
}
`
  },
  javascript: {
    name: 'Node.js',
    extension: 'js',
    starterCode: `const readline = require('readline');

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

const lines = [];

rl.on('line', (line) => {
    lines.push(line);
});

rl.on('close', () => {
    const n = parseInt(lines[0]);
    const arr = lines[1].split(' ').map(Number);
    
    // Your code here
    const sum = arr.reduce((a, b) => a + b, 0);
    
    console.log(sum);
});
`
  }
};

