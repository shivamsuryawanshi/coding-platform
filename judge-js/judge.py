"""
JavaScript Judge Service - Code Execution and Evaluation Engine
Port: 5004

Run: node solution.js
"""

import subprocess
import tempfile
import os
import shutil
from flask import Flask, request, jsonify
from testcases import TEST_CASES, PROBLEM

app = Flask(__name__)

# Configuration
TIMEOUT_SECONDS = 5  # Maximum execution time per test case


class Verdict:
    ACCEPTED = "Accepted"
    WRONG_ANSWER = "Wrong Answer"
    RUNTIME_ERROR = "Runtime Error"
    TIME_LIMIT_EXCEEDED = "Time Limit Exceeded"


def execute_code(code: str, input_data: str, temp_dir: str):
    """Execute JavaScript code with Node.js."""
    # Save code to temp file
    code_file = os.path.join(temp_dir, "solution.js")
    with open(code_file, 'w', encoding='utf-8') as f:
        f.write(code)
    
    try:
        process = subprocess.run(
            ["node", code_file],
            cwd=temp_dir,
            input=input_data,
            capture_output=True,
            text=True,
            timeout=TIMEOUT_SECONDS
        )
        return {
            "success": process.returncode == 0,
            "stdout": process.stdout,
            "stderr": process.stderr,
            "timeout": False
        }
    except subprocess.TimeoutExpired:
        return {"success": False, "stdout": "", "stderr": "Time limit exceeded", "timeout": True}
    except Exception as e:
        return {"success": False, "stdout": "", "stderr": str(e), "timeout": False}


def run_all_test_cases(code: str):
    """Run code against all test cases."""
    total_tests = len(TEST_CASES)
    passed_tests = 0
    
    temp_dir = tempfile.mkdtemp(prefix="judge_js_")
    
    try:
        for test_case in TEST_CASES:
            test_id = test_case["id"]
            input_data = test_case["input"]
            expected_output = test_case["expected_output"]
            
            result = execute_code(code, input_data, temp_dir)
            
            if result["timeout"]:
                return {
                    "verdict": Verdict.TIME_LIMIT_EXCEEDED,
                    "passed": passed_tests,
                    "total": total_tests,
                    "failed_test": {
                        "test_id": test_id,
                        "input": input_data,
                        "expected": expected_output,
                        "actual": "Execution timed out",
                        "error": result["stderr"]
                    }
                }
            
            if not result["success"]:
                return {
                    "verdict": Verdict.RUNTIME_ERROR,
                    "passed": passed_tests,
                    "total": total_tests,
                    "failed_test": {
                        "test_id": test_id,
                        "input": input_data,
                        "expected": expected_output,
                        "actual": result["stdout"].strip()[:500],
                        "error": result["stderr"][:500]
                    }
                }
            
            actual = result["stdout"].strip()
            expected = expected_output.strip()
            
            if actual != expected:
                return {
                    "verdict": Verdict.WRONG_ANSWER,
                    "passed": passed_tests,
                    "total": total_tests,
                    "failed_test": {
                        "test_id": test_id,
                        "input": input_data,
                        "expected": expected,
                        "actual": actual[:500],
                        "error": None
                    }
                }
            
            passed_tests += 1
        
        return {
            "verdict": Verdict.ACCEPTED,
            "passed": passed_tests,
            "total": total_tests,
            "failed_test": None
        }
        
    finally:
        shutil.rmtree(temp_dir, ignore_errors=True)


@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint for container orchestration."""
    return jsonify({
        "status": "healthy",
        "service": "judge-js",
        "language": "JavaScript",
        "version": "1.0.0"
    })


@app.route('/problem', methods=['GET'])
def get_problem():
    """Return problem statement."""
    return jsonify(PROBLEM)


@app.route('/judge', methods=['POST'])
def judge_submission():
    """Judge submitted code against test cases."""
    if not request.is_json:
        return jsonify({"error": "Request must be JSON", "verdict": None}), 400
    
    data = request.get_json()
    
    if 'code' not in data:
        return jsonify({"error": "Missing 'code' field", "verdict": None}), 400
    
    code = data['code']
    
    if not isinstance(code, str) or len(code.strip()) == 0:
        return jsonify({"error": "Code cannot be empty", "verdict": None}), 400
    
    result = run_all_test_cases(code)
    return jsonify(result)


if __name__ == '__main__':
    print("=" * 60)
    print("JAVASCRIPT JUDGE SERVICE")
    print("=" * 60)
    print("Starting on http://0.0.0.0:5004")
    print("Endpoints:")
    print("  GET  /health  - Health check")
    print("  GET  /problem - Get problem")
    print("  POST /judge   - Submit code")
    print("=" * 60)
    
    app.run(host='0.0.0.0', port=5004, debug=False)

