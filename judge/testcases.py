"""
Problem and test cases for the coding platform.
"""

# Problem Definition
PROBLEM = {
    "id": 1,
    "title": "Sum of Two Numbers",
    "description": "Given two integers a and b, print their sum.",
    "input_format": "Two space-separated integers a and b",
    "output_format": "A single integer representing a + b",
    "constraints": [
        "-1000 <= a, b <= 1000"
    ],
    "examples": [
        {
            "input": "1 2",
            "output": "3",
            "explanation": "1 + 2 = 3"
        },
        {
            "input": "5 7",
            "output": "12",
            "explanation": "5 + 7 = 12"
        }
    ],
    "difficulty": "Easy"
}

# Test Cases (used by judge for evaluation)
TEST_CASES = [
    {
        "id": 1,
        "input": "1 2",
        "expected_output": "3",
        "description": "Basic addition"
    },
    {
        "id": 2,
        "input": "5 7",
        "expected_output": "12",
        "description": "Single digit addition"
    },
    {
        "id": 3,
        "input": "100 200",
        "expected_output": "300",
        "description": "Three digit addition"
    }
]
