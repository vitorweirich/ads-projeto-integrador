import boto3
from botocore.config import Config

s3 = boto3.client(
    "s3",
    endpoint_url="http://garage:3900",
    aws_access_key_id="GKafc12d36554df7f8d5c4b7ba",
    aws_secret_access_key="abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789",
    region_name="garage",
    config=Config(s3={"addressing_style": "path"}),
)

s3.put_bucket_cors(
    Bucket="files",
    CORSConfiguration={
        "CORSRules": [
            {
                "AllowedOrigins": ["http://localhost:5173"],
                "AllowedMethods": ["GET", "PUT", "POST", "DELETE"],
                "AllowedHeaders": ["*"],
                "ExposeHeaders": ["ETag"],
                "MaxAgeSeconds": 3000,
            }
        ]
    },
)

print("✅ CORS configured on bucket files")
