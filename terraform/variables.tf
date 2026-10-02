variable "aws_region" {
  description = "AWS region for ECR, ECS and supporting resources."
  type        = string
  default     = "us-east-1"
}

variable "app_name" {
  description = "Base name for AWS and Atlas resources."
  type        = string
  default     = "franchise-hub"
}

variable "image_tag" {
  description = "Container image tag available in ECR for the ECS task definition."
  type        = string
  default     = "latest"
}

variable "enable_service" {
  description = "Keep the ECS service at desired_count 0 until deployment is authorized."
  type        = bool
  default     = false
}

variable "atlas_org_id" {
  description = "MongoDB Atlas organization ID."
  type        = string
}

variable "atlas_db_password" {
  description = "Temporary write-only password for the Atlas database user."
  type        = string
  sensitive   = true
}

variable "atlas_db_password_version" {
  description = "Version used to rotate the write-only Atlas database user password."
  type        = number
  default     = 1
}

variable "atlas_client_cidr" {
  description = "Local CIDR allowed to reach the Atlas project."
  type        = string
  default     = "190.69.39.48/32"
}

variable "atlas_allow_public_runtime" {
  description = "Temporarily allow all IPv4 sources for public Fargate evaluation. Not production-safe."
  type        = bool
  default     = false
}

variable "create_github_oidc_provider" {
  description = "Create the GitHub Actions OIDC provider if the AWS account does not already have it."
  type        = bool
  default     = false
}

variable "github_actions_oidc_provider_arn" {
  description = "Existing GitHub Actions OIDC provider ARN, when managed outside this stack."
  type        = string
  default     = null
  nullable    = true
}

variable "enable_github_actions_role" {
  description = "Create the deploy role trusted by the production GitHub environment subject."
  type        = bool
  default     = false
}

variable "github_repository" {
  description = "GitHub repository allowed to assume the deploy role."
  type        = string
  default     = "kevinmartinez07/franchise-hub"
}

variable "github_oidc_subject" {
  description = "Immutable GitHub OIDC subject for the production environment."
  type        = string
  default     = "repo:kevinmartinez07@121494810/franchise-hub@1398796900:environment:production"
}
