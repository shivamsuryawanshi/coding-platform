#!/usr/bin/env python3
"""
CodeNexus Unified Judge Service
================================
Multi-language code execution and evaluation engine.

Features:
- Supports Python, C++, Java, JavaScript
- Fetches testcases from S3 (via request from backend)
- Isolated code execution
- Comprehensive verdicts

Port: 5000

Flow:
1. Backend sends code + testcases (fetched from S3)
2. Judge executes code against testcases
3. Judge returns verdict

This service NEVER touches the database.
"""

import subprocess
import tempfile
import os
import shutil
import logging
from flask import Flask, request, jsonify

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

app = Flask(__name__)

# Configuration
TIMEOUT_SECONDS = int(os.getenv('TIMEOUT_SECONDS', '5'))
MAX_OUTPUT_SIZE = 10000  # characters


class Verdict:
    """Verdict constants."""
    ACCEPTED = "Accepted"
    WRONG_ANSWER = "Wrong Answer"
    RUNTIME_ERROR = "Runtime Error"
    TIME_LIMIT_EXCEEDED = "Time Limit Exceeded"
    COMPILATION_ERROR = "Compilation Error"


class LanguageRunner:
    """Base class for language-specific code runners."""
    
    def __init__(self, temp_dir: str):
        self.temp_dir = temp_dir
    
    def compile(self, code: str) -> dict:
        """Compile code if needed. Returns {"success": bool, "error": str}"""
        return {"success": True, "error": None}
    
    def run(self, input_data: str) -> dict:
        """Run the code with input. Returns execution result."""
        raise NotImplementedError


class PythonRunner(LanguageRunner):
    """Python code runner."""
    
    def compile(self, code: str) -> dict:
        self.code_file = os.path.join(self.temp_dir, "solution.py")
        with open(self.code_file, 'w', encoding='utf-8') as f:
            f.write(code)
        return {"success": True, "error": None}
    
    def run(self, input_data: str) -> dict:
        try:
            process = subprocess.run(
                ["python3", self.code_file],
                cwd=self.temp_dir,
                input=input_data,
                capture_output=True,
                text=True,
                timeout=TIMEOUT_SECONDS
            )
            return {
                "success": process.returncode == 0,
                "stdout": process.stdout[:MAX_OUTPUT_SIZE],
                "stderr": process.stderr[:MAX_OUTPUT_SIZE],
                "timeout": False
            }
        except subprocess.TimeoutExpired:
            return {"success": False, "stdout": "", "stderr": "Time limit exceeded", "timeout": True}
        except Exception as e:
            return {"success": False, "stdout": "", "stderr": str(e), "timeout": False}


class CppRunner(LanguageRunner):
    """C++ code runner."""
    
    def compile(self, code: str) -> dict:
        code_file = os.path.join(self.temp_dir, "solution.cpp")
        self.exe_file = os.path.join(self.temp_dir, "solution")
        
        with open(code_file, 'w', encoding='utf-8') as f:
            f.write(code)
        
        try:
            process = subprocess.run(
                ["g++", "-std=c++17", "-O2", code_file, "-o", self.exe_file],
                cwd=self.temp_dir,
                capture_output=True,
                text=True,
                timeout=30
            )
            if process.returncode != 0:
                return {"success": False, "error": process.stderr[:MAX_OUTPUT_SIZE]}
            return {"success": True, "error": None}
        except subprocess.TimeoutExpired:
            return {"success": False, "error": "Compilation timeout"}
        except Exception as e:
            return {"success": False, "error": str(e)}
    
    def run(self, input_data: str) -> dict:
        try:
            process = subprocess.run(
                [self.exe_file],
                cwd=self.temp_dir,
                input=input_data,
                capture_output=True,
                text=True,
                timeout=TIMEOUT_SECONDS
            )
            return {
                "success": process.returncode == 0,
                "stdout": process.stdout[:MAX_OUTPUT_SIZE],
                "stderr": process.stderr[:MAX_OUTPUT_SIZE],
                "timeout": False
            }
        except subprocess.TimeoutExpired:
            return {"success": False, "stdout": "", "stderr": "Time limit exceeded", "timeout": True}
        except Exception as e:
            return {"success": False, "stdout": "", "stderr": str(e), "timeout": False}


class JavaRunner(LanguageRunner):
    """Java code runner."""
    
    def compile(self, code: str) -> dict:
        # Java requires class name to match filename
        code_file = os.path.join(self.temp_dir, "Main.java")
        
        # Replace public class name with Main
        import re
        code = re.sub(r'public\s+class\s+\w+', 'public class Main', code, count=1)
        
        with open(code_file, 'w', encoding='utf-8') as f:
            f.write(code)
        
        try:
            process = subprocess.run(
                ["javac", code_file],
                cwd=self.temp_dir,
                capture_output=True,
                text=True,
                timeout=30
            )
            if process.returncode != 0:
                return {"success": False, "error": process.stderr[:MAX_OUTPUT_SIZE]}
            return {"success": True, "error": None}
        except subprocess.TimeoutExpired:
            return {"success": False, "error": "Compilation timeout"}
        except Exception as e:
            return {"success": False, "error": str(e)}
    
    def run(self, input_data: str) -> dict:
        try:
            process = subprocess.run(
                ["java", "-Xmx256m", "Main"],
                cwd=self.temp_dir,
                input=input_data,
                capture_output=True,
                text=True,
                timeout=TIMEOUT_SECONDS
            )
            return {
                "success": process.returncode == 0,
                "stdout": process.stdout[:MAX_OUTPUT_SIZE],
                "stderr": process.stderr[:MAX_OUTPUT_SIZE],
                "timeout": False
            }
        except subprocess.TimeoutExpired:
            return {"success": False, "stdout": "", "stderr": "Time limit exceeded", "timeout": True}
        except Exception as e:
            return {"success": False, "stdout": "", "stderr": str(e), "timeout": False}


class JavaScriptRunner(LanguageRunner):
    """JavaScript (Node.js) code runner."""
    
    def compile(self, code: str) -> dict:
        self.code_file = os.path.join(self.temp_dir, "solution.js")
        with open(self.code_file, 'w', encoding='utf-8') as f:
            f.write(code)
        return {"success": True, "error": None}
    
    def run(self, input_data: str) -> dict:
        try:
            process = subprocess.run(
                ["node", self.code_file],
                cwd=self.temp_dir,
                input=input_data,
                capture_output=True,
                text=True,
                timeout=TIMEOUT_SECONDS
            )
            return {
                "success": process.returncode == 0,
                "stdout": process.stdout[:MAX_OUTPUT_SIZE],
                "stderr": process.stderr[:MAX_OUTPUT_SIZE],
                "timeout": False
            }
        except subprocess.TimeoutExpired:
            return {"success": False, "stdout": "", "stderr": "Time limit exceeded", "timeout": True}
        except Exception as e:
            return {"success": False, "stdout": "", "stderr": str(e), "timeout": False}


def get_runner(language: str, temp_dir: str) -> LanguageRunner:
    """Get the appropriate runner for a language."""
    runners = {
        'python': PythonRunner,
        'cpp': CppRunner,
        'java': JavaRunner,
        'javascript': JavaScriptRunner,
        'js': JavaScriptRunner
    }
    
    runner_class = runners.get(language.lower())
    if not runner_class:
        raise ValueError(f"Unsupported language: {language}")
    
    return runner_class(temp_dir)


def judge_code(language: str, code: str, testcases: list) -> dict:
    """
    Judge code against testcases.
    
    Args:
        language: Programming language
        code: User's source code
        testcases: List of {"id": int, "input": str, "expectedOutput": str}
    
    Returns:
        Verdict result dict
    """
    temp_dir = tempfile.mkdtemp(prefix="judge_")
    
    try:
        runner = get_runner(language, temp_dir)
        
        # Compile (if needed)
        compile_result = runner.compile(code)
        if not compile_result["success"]:
            return {
                "verdict": Verdict.COMPILATION_ERROR,
                "passed": 0,
                "total": len(testcases),
                "failed_test": None,
                "error": compile_result["error"]
            }
        
        # Run each testcase
        passed = 0
        for tc in testcases:
            test_id = tc.get("id", passed + 1)
            input_data = tc.get("input", "")
            expected = tc.get("expectedOutput", "").strip()
            
            result = runner.run(input_data)
            
            if result["timeout"]:
                return {
                    "verdict": Verdict.TIME_LIMIT_EXCEEDED,
                    "passed": passed,
                    "total": len(testcases),
                    "failed_test": {
                        "testId": test_id,
                        "input": input_data[:500],
                        "expected": expected[:500],
                        "actual": "Execution timed out",
                        "error": None
                    }
                }
            
            if not result["success"]:
                return {
                    "verdict": Verdict.RUNTIME_ERROR,
                    "passed": passed,
                    "total": len(testcases),
                    "failed_test": {
                        "testId": test_id,
                        "input": input_data[:500],
                        "expected": expected[:500],
                        "actual": result["stdout"][:500],
                        "error": result["stderr"][:500]
                    }
                }
            
            actual = result["stdout"].strip()
            
            if actual != expected:
                return {
                    "verdict": Verdict.WRONG_ANSWER,
                    "passed": passed,
                    "total": len(testcases),
                    "failed_test": {
                        "testId": test_id,
                        "input": input_data[:500],
                        "expected": expected[:500],
                        "actual": actual[:500],
                        "error": None
                    }
                }
            
            passed += 1
        
        return {
            "verdict": Verdict.ACCEPTED,
            "passed": passed,
            "total": len(testcases),
            "failed_test": None
        }
        
    finally:
        shutil.rmtree(temp_dir, ignore_errors=True)


# ============================================
# API Endpoints
# ============================================

@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint."""
    return jsonify({
        "status": "healthy",
        "service": "judge",
        "version": "2.0.0",
        "languages": ["python", "cpp", "java", "javascript"]
    })


@app.route('/judge', methods=['POST'])
def judge_submission():
    """
    Judge submitted code.
    
    Request body:
    {
        "language": "python",
        "code": "...",
        "testcases": [
            {"id": 1, "input": "...", "expectedOutput": "..."},
            ...
        ]
    }
    """
    if not request.is_json:
        return jsonify({"error": "Request must be JSON"}), 400
    
    data = request.get_json()
    
    # Validate request
    language = data.get('language')
    code = data.get('code')
    testcases = data.get('testcases', [])
    
    if not language:
        return jsonify({"error": "Missing 'language' field"}), 400
    if not code or not isinstance(code, str) or len(code.strip()) == 0:
        return jsonify({"error": "Code cannot be empty"}), 400
    if not testcases or not isinstance(testcases, list):
        return jsonify({"error": "Testcases must be a non-empty list"}), 400
    
    logger.info(f"Judging {language} code with {len(testcases)} testcases")
    
    try:
        result = judge_code(language, code, testcases)
        logger.info(f"Verdict: {result['verdict']} ({result['passed']}/{result['total']})")
        return jsonify(result)
    except ValueError as e:
        return jsonify({"error": str(e)}), 400
    except Exception as e:
        logger.error(f"Judge error: {e}")
        return jsonify({"error": "Internal judge error"}), 500


# ============================================
# Main
# ============================================

if __name__ == '__main__':
    port = int(os.getenv('PORT', 5000))
    
    print("=" * 60)
    print("CODENEXUS UNIFIED JUDGE SERVICE")
    print("=" * 60)
    print(f"Starting on http://0.0.0.0:{port}")
    print("Endpoints:")
    print("  GET  /health  - Health check")
    print("  POST /judge   - Submit code for judging")
    print("=" * 60)
    print("Supported languages: Python, C++, Java, JavaScript")
    print("=" * 60)
    
    app.run(host='0.0.0.0', port=port, debug=False)

