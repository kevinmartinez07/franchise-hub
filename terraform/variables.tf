variable "aws_region" {
  description = "AWS region for ECR, IAM and App Runner."
  type        = string
  default     = "us-east-1"
}

variable "app_name" {
  description = "Base name for AWS resources."
  type        = string
  default     = "franchise-hub"
}

variable "image_tag" {
  description = "Image tag that already exists in ECR when App Runner is enabled."
  type        = string
  default     = "latest"
}

variable "enable_service" {
  description = "Create the App Runner service. Keep false until ECR contains the first image."
  type        = bool
  default     = false
}

variable "mongodb_uri_secret_arn" {
  description = "Existing Secrets Manager ARN containing MONGODB_URI."
  type        = string
  sensitive   = true
  default     = null
  nullable    = true
}

variable "jwt_secret_arn" {
  description = "Existing Secrets Manager ARN containing JWT_SECRET."
  type        = string
  sensitive   = true
  default     = null
  nullable    = true
}

variable "franchise_app_username_secret_arn" {
  description = "Existing Secrets Manager ARN containing FRANCHISE_APP_USERNAME."
  type        = string
  sensitive   = true
  default     = null
  nullable    = true
}

variable "franchise_app_password_secret_arn" {
  description = "Existing Secrets Manager ARN containing FRANCHISE_APP_PASSWORD."
  type        = string
  sensitive   = true
  default     = null
  nullable    = true
}

variable "jwt_issuer" {
  description = "JWT issuer passed as a non-sensitive App Runner environment variable."
  type        = string
  default     = "franchise-hub"
}

variable "jwt_audience" {
  description = "JWT audience passed as a non-sensitive App Runner environment variable."
  type        = string
  default     = "franchise-hub-api"
}

variable "jwt_expiration" {
  description = "ISO-8601 JWT duration passed as a non-sensitive App Runner environment variable."
  type        = string
  default     = "PT30M"
}

variable "create_github_oidc_provider" {
  description = "Create the GitHub Actions OIDC provider. Enable only when the AWS account does not already have it."
  type        = bool
  default     = false
}

variable "github_actions_oidc_provider_arn" {
  description = "Existing GitHub Actions OIDC provider ARN, if it is managed outside this stack."
  type        = string
  default     = null
  nullable    = true
}

variable "enable_github_actions_role" {
  description = "Create the deploy role trusted by the configured GitHub repository and branch."
  type        = bool
  default     = false
}

variable "github_repository" {
  description = "GitHub repository allowed to assume the deploy role."
  type        = string
  default     = "kevinmartinez07/franchise-hub"
}

variable "github_deploy_branch" {
  description = "Git branch allowed to assume the deploy role."
  type        = string
  default     = "main"
}
