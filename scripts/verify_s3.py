#!/usr/bin/env python3
"""
Verify S3 Testcases Upload
==========================
Quick script to check if testcases are uploaded to S3
"""

import boto3
import os
from botocore.exceptions import ClientError

S3_BUCKET = os.getenv('S3_BUCKET', 'coding-platform-testcases')
AWS_REGION = os.getenv('AWS_REGION', 'eu-north-1')

def main():
    print("=" * 60)
    print("S3 Testcases Verification")
    print("=" * 60)
    
    # Initialize S3 client (uses IAM Role on EC2)
    s3_client = boto3.client('s3', region_name=AWS_REGION)
    
    try:
        # List all objects in the problems/ prefix
        print(f"\nChecking S3 bucket: {S3_BUCKET}")
        print(f"Region: {AWS_REGION}\n")
        
        response = s3_client.list_objects_v2(
            Bucket=S3_BUCKET,
            Prefix='problems/'
        )
        
        if 'Contents' not in response:
            print("❌ No testcases found in S3!")
            print("   Bucket is empty or prefix 'problems/' doesn't exist")
            return
        
        files = response['Contents']
        print(f"✅ Found {len(files)} files in S3\n")
        
        # Group by problem_id
        problems = {}
        for file in files:
            key = file['Key']
            # Extract problem_id from key: problems/{problem_id}/input1.txt
            parts = key.split('/')
            if len(parts) >= 3:
                problem_id = parts[1]
                if problem_id not in problems:
                    problems[problem_id] = []
                problems[problem_id].append(parts[2])
        
        print(f"📦 Problems with testcases: {len(problems)}\n")
        
        # Show first 10 problems
        for i, (problem_id, files) in enumerate(sorted(problems.items())[:10], 1):
            print(f"{i}. {problem_id}: {len(files)} files")
            print(f"   Files: {', '.join(sorted(files)[:4])}{'...' if len(files) > 4 else ''}")
        
        if len(problems) > 10:
            print(f"\n   ... and {len(problems) - 10} more problems")
        
        print("\n" + "=" * 60)
        print("✅ S3 Verification Complete!")
        print("=" * 60)
        
    except ClientError as e:
        error_code = e.response.get('Error', {}).get('Code', '')
        if error_code == '403':
            print(f"❌ Access denied to bucket: {S3_BUCKET}")
            print("   Check IAM Role permissions")
        elif error_code == '404':
            print(f"❌ Bucket does not exist: {S3_BUCKET}")
        else:
            print(f"❌ Error: {e}")
    except Exception as e:
        print(f"❌ Unexpected error: {e}")

if __name__ == '__main__':
    main()

