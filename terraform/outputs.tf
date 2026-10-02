output "ecr_repository_url" {
  description = "Private ECR repository URL for the application image."
  value       = aws_ecr_repository.app.repository_url
}

output "ecs_cluster_name" {
  description = "ECS cluster name."
  value       = aws_ecs_cluster.app.name
}

output "ecs_service_name" {
  description = "ECS service name."
  value       = aws_ecs_service.app.name
}

output "runtime_secret_arn" {
  description = "Secrets Manager ARN for the single runtime JSON secret."
  value       = aws_secretsmanager_secret.runtime.arn
}

output "atlas_project_id" {
  description = "MongoDB Atlas project ID."
  value       = mongodbatlas_project.app.id
}

output "atlas_cluster_name" {
  description = "MongoDB Atlas M0 cluster name."
  value       = mongodbatlas_advanced_cluster.app.name
}

output "codepipeline_name" {
  description = "AWS CodePipeline V1 name."
  value       = aws_codepipeline.app.name
}

output "codebuild_project_name" {
  description = "AWS CodeBuild project name."
  value       = aws_codebuild_project.app.name
}

output "github_connection_arn" {
  description = "Pending AWS CodeConnections connection ARN for GitHub authorization."
  value       = aws_codestarconnections_connection.github.arn
}

output "pipeline_artifact_bucket_name" {
  description = "S3 bucket used for CodePipeline artifacts."
  value       = aws_s3_bucket.pipeline_artifacts.bucket
}
