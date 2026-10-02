resource "aws_apprunner_service" "app" {
  count        = var.enable_service ? 1 : 0
  service_name = var.app_name

  source_configuration {
    auto_deployments_enabled = false

    authentication_configuration {
      access_role_arn = aws_iam_role.apprunner_ecr_access.arn
    }

    image_repository {
      image_repository_type = "ECR"
      image_identifier      = "${aws_ecr_repository.app.repository_url}:${var.image_tag}"

      image_configuration {
        port = "8080"

        runtime_environment_variables = {
          JWT_ISSUER     = var.jwt_issuer
          JWT_AUDIENCE   = var.jwt_audience
          JWT_EXPIRATION = var.jwt_expiration
        }

        runtime_environment_secrets = local.runtime_secret_arns
      }
    }
  }

  instance_configuration {
    cpu               = "1 vCPU"
    memory            = "2 GB"
    instance_role_arn = aws_iam_role.apprunner_instance.arn
  }

  health_check_configuration {
    protocol            = "HTTP"
    path                = "/actuator/health"
    interval            = 10
    timeout             = 5
    healthy_threshold   = 1
    unhealthy_threshold = 5
  }

  lifecycle {
    precondition {
      condition     = local.runtime_secrets_configured
      error_message = "App Runner requires four existing Secrets Manager ARNs before enable_service can be true."
    }
  }

  depends_on = [
    aws_iam_role_policy.apprunner_ecr_access,
    aws_iam_role_policy.apprunner_instance_secrets
  ]
}
