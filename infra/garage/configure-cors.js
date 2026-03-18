import { S3Client, PutBucketCorsCommand } from "@aws-sdk/client-s3";
import dotenv from "dotenv";

dotenv.config({ quiet: true });

const { S3_ENDPOINT, S3_ACCESS_KEY, S3_SECRET_KEY, S3_BUCKET } = process.env;

const client = new S3Client({
  endpoint: S3_ENDPOINT,
  region: "garage",
  credentials: {
    accessKeyId: S3_ACCESS_KEY,
    secretAccessKey: S3_SECRET_KEY,
  },
  // Importante: O Garage geralmente não suporta caminhos virtuais (bucket.dominio)
  // forcePathStyle garante o formato dominio/bucket
  forcePathStyle: true,
});

const command = new PutBucketCorsCommand({
  Bucket: S3_BUCKET,
  CORSConfiguration: {
    CORSRules: [
      {
        AllowedHeaders: ["*"],
        AllowedMethods: ["GET", "PUT", "POST"],
        AllowedOrigins: ["http://localhost:5173"],
        MaxAgeSeconds: 3000,
      },
    ],
  },
});

await client.send(command);
