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

output "github_actions_deploy_role_arn" {
  description = "GitHub Actions OIDC deploy role ARN when enabled."
  value       = try(aws_iam_role.github_actions_deploy[0].arn, null)
}
