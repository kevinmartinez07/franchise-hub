output "ecr_repository_url" {
  description = "Private ECR repository URL for the application image."
  value       = aws_ecr_repository.app.repository_url
}

output "apprunner_service_url" {
  description = "App Runner service URL when the service is enabled."
  value       = try(aws_apprunner_service.app[0].service_url, null)
}

output "apprunner_service_arn" {
  description = "App Runner service ARN when the service is enabled."
  value       = try(aws_apprunner_service.app[0].arn, null)
}

output "github_actions_deploy_role_arn" {
  description = "GitHub Actions OIDC deploy role ARN when enabled."
  value       = try(aws_iam_role.github_actions_deploy[0].arn, null)
}
