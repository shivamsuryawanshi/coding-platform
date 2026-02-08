#!/bin/bash
# Test all judges on EC2

echo "=========================================="
echo "Testing C++ Judge (port 5002)"
echo "=========================================="
curl -s -X POST http://localhost:5002/judge \
  -H "Content-Type: application/json" \
  -d '{"code": "#include <iostream>\nusing namespace std;\nint main() { int a, b; cin >> a >> b; cout << a + b; return 0; }"}'
echo -e "\n"

echo "=========================================="
echo "Testing Java Judge (port 5003)"
echo "=========================================="
curl -s -X POST http://localhost:5003/judge \
  -H "Content-Type: application/json" \
  -d '{"code": "import java.util.Scanner;\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        int a = sc.nextInt();\n        int b = sc.nextInt();\n        System.out.println(a + b);\n    }\n}"}'
echo -e "\n"

echo "=========================================="
echo "Testing JavaScript Judge (port 5004)"
echo "=========================================="
curl -s -X POST http://localhost:5004/judge \
  -H "Content-Type: application/json" \
  -d '{"code": "const readline = require(\"readline\");\nconst rl = readline.createInterface({ input: process.stdin });\nrl.on(\"line\", (line) => { const [a, b] = line.split(\" \").map(Number); console.log(a + b); rl.close(); });"}'
echo -e "\n"

echo "=========================================="
echo "Testing Python Judge (port 5000)"
echo "=========================================="
curl -s -X POST http://localhost:5000/judge \
  -H "Content-Type: application/json" \
  -d '{"code": "a, b = map(int, input().split())\nprint(a + b)"}'
echo -e "\n"

echo "=========================================="
echo "ALL TESTS COMPLETE"
echo "=========================================="

