"""
Judge Service - Code Execution and Evaluation Engine

This service is responsible for:
1. Receiving user-submitted code via HTTP
2. Executing the code in a controlled subprocess
3. Running test cases against the submitted code
4. Returning verdicts (Accepted, Wrong Answer, Runtime Error, Time Limit Exceeded)

IMPORTANT: This service is intentionally separate from the backend.
User code is UNTRUSTED and must be executed in isolation.
In production, this service would run inside a sandboxed container.
"""

import subprocess
import tempfile
import os
import sys
from typing import Dict, Any
from flask import Flask, request, jsonify
from testcases import TEST_CASES, PROBLEM

app = Flask(__name__)

# Configuration
TIMEOUT_SECONDS = 5  # Maximum execution time per test case
PYTHON_EXECUTABLE = sys.executable  # Use the same Python interpreter


class Verdict:
    """Verdict constants for judge responses."""
    ACCEPTED = "Accepted"
    WRONG_ANSWER = "Wrong Answer"
    RUNTIME_ERROR = "Runtime Error"
    TIME_LIMIT_EXCEEDED = "Time Limit Exceeded"
    COMPILATION_ERROR = "Compilation Error"


def execute_code(code: str, input_data: str) -> Dict[str, Any]:
    """
    Execute user code with given input.
    
    Args:
        code: The Python code to execute (as a string)
        input_data: The input to pass via STDIN
        
    Returns:
        Dictionary containing:
        - success: bool indicating if execution completed
        - stdout: captured standard output
        - stderr: captured standard error
        - exit_code: process exit code
        - error_type: type of error if any
    """
    # Create a temporary file to store the user code
    with tempfile.NamedTemporaryFile(
        mode='w',
        suffix='.py',
        delete=False,
        encoding='utf-8'
    ) as temp_file:
        temp_file.write(code)
        temp_file_path = temp_file.name
    
    try:
        # Execute the code using subprocess
        process = subprocess.run(
            [PYTHON_EXECUTABLE, temp_file_path],
            input=input_data,
            capture_output=True,
            text=True,
            timeout=TIMEOUT_SECONDS
        )
        
        return {
            "success": process.returncode == 0,
            "stdout": process.stdout,
            "stderr": process.stderr,
            "exit_code": process.returncode,
            "error_type": None if process.returncode == 0 else "runtime"
        }
        
    except subprocess.TimeoutExpired:
        return {
            "success": False,
            "stdout": "",
            "stderr": "Execution timed out",
            "exit_code": -1,
            "error_type": "timeout"
        }
        
    except Exception as e:
        return {
            "success": False,
            "stdout": "",
            "stderr": str(e),
            "exit_code": -1,
            "error_type": "system"
        }
        
    finally:
        # Clean up temporary file
        if os.path.exists(temp_file_path):
            os.remove(temp_file_path)


def run_all_test_cases(code: str) -> Dict[str, Any]:
    """
    Run submitted code against all test cases.
    
    Args:
        code: The Python code to test
        
    Returns:
        Dictionary containing:
        - verdict: final verdict string
        - passed: number of test cases passed
        - total: total number of test cases
        - failed_test: details of first failed test (if any)
    """
    total_tests = len(TEST_CASES)
    passed_tests = 0
    
    for test_case in TEST_CASES:
        test_id = test_case["id"]
        input_data = test_case["input"]
        expected_output = test_case["expected_output"]
        
        # Execute code with this test case's input
        result = execute_code(code, input_data)
        
        # Handle execution errors
        if result["error_type"] == "timeout":
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
                    "actual": result["stdout"].strip() if result["stdout"] else "",
                    "error": result["stderr"]
                }
            }
        
        # Compare output (trim whitespace for comparison)
        actual_output = result["stdout"].strip()
        expected_trimmed = expected_output.strip()
        
        if actual_output != expected_trimmed:
            return {
                "verdict": Verdict.WRONG_ANSWER,
                "passed": passed_tests,
                "total": total_tests,
                "failed_test": {
                    "test_id": test_id,
                    "input": input_data,
                    "expected": expected_trimmed,
                    "actual": actual_output,
                    "error": None
                }
            }
        
        passed_tests += 1
    
    # All test cases passed
    return {
        "verdict": Verdict.ACCEPTED,
        "passed": passed_tests,
        "total": total_tests,
        "failed_test": None
    }


@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint for service monitoring."""
    return jsonify({
        "status": "healthy",
        "service": "judge",
        "version": "1.0.0"
    })


@app.route('/problem', methods=['GET'])
def get_problem():
    """
    Get the problem statement.
    
    Returns the problem definition including:
    - title, description, input/output format
    - constraints, examples, difficulty
    """
    return jsonify(PROBLEM)


@app.route('/judge', methods=['POST'])
def judge_submission():
    """
    Main judging endpoint.
    
    Expects JSON body:
    {
        "code": "<python code as string>"
    }
    
    Returns:
    {
        "verdict": "Accepted" | "Wrong Answer" | "Runtime Error" | "Time Limit Exceeded",
        "passed": <number>,
        "total": <number>,
        "failed_test": {...} | null
    }
    """
    # Validate request
    if not request.is_json:
        return jsonify({
            "error": "Request must be JSON",
            "verdict": None
        }), 400
    
    data = request.get_json()
    
    if 'code' not in data:
        return jsonify({
            "error": "Missing 'code' field in request body",
            "verdict": None
        }), 400
    
    code = data['code']
    
    if not isinstance(code, str):
        return jsonify({
            "error": "'code' must be a string",
            "verdict": None
        }), 400
    
    if len(code.strip()) == 0:
        return jsonify({
            "error": "Code cannot be empty",
            "verdict": None
        }), 400
    
    # Run the judge
    result = run_all_test_cases(code)
    
    return jsonify(result)


if __name__ == '__main__':
    """
    Entry point for development server.
    In production (Docker), use gunicorn instead:
    gunicorn --bind 0.0.0.0:5000 judge:app
    """
    import os
    
    # Configuration from environment variables (Docker-friendly)
    HOST = os.environ.get('JUDGE_HOST', '0.0.0.0')
    PORT = int(os.environ.get('JUDGE_PORT', 5000))
    DEBUG = os.environ.get('JUDGE_DEBUG', 'false').lower() == 'true'
    
    print("=" * 60)
    print("JUDGE SERVICE")
    print("=" * 60)
    print(f"Starting judge service on http://{HOST}:{PORT}")
    print(f"Debug mode: {DEBUG}")
    print("Endpoints:")
    print("  GET  /problem - Get problem statement")
    print("  POST /judge   - Submit code for judging")
    print("  GET  /health  - Health check")
    print("=" * 60)
    
    app.run(host=HOST, port=PORT, debug=DEBUG)

