locals {
  runtime_secret_arns = {
    MONGODB_URI            = var.mongodb_uri_secret_arn
    JWT_SECRET             = var.jwt_secret_arn
    FRANCHISE_APP_USERNAME = var.franchise_app_username_secret_arn
    FRANCHISE_APP_PASSWORD = var.franchise_app_password_secret_arn
  }

  runtime_secrets_configured = alltrue([
    for secret_arn in values(local.runtime_secret_arns) : secret_arn != null && secret_arn != ""
  ])

  ecr_repository_arn = aws_ecr_repository.app.arn
  app_runner_arn     = "arn:aws:apprunner:${var.aws_region}:${data.aws_caller_identity.current.account_id}:service/${var.app_name}/*"
}

data "aws_iam_policy_document" "apprunner_ecr_assume" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["build.apprunner.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "apprunner_ecr_access" {
  name               = "${var.app_name}-ecr-access"
  assume_role_policy = data.aws_iam_policy_document.apprunner_ecr_assume.json
}

data "aws_iam_policy_document" "apprunner_ecr_access" {
  statement {
    actions   = ["ecr:GetAuthorizationToken"]
    resources = ["*"]
  }

  statement {
    actions = [
      "ecr:BatchCheckLayerAvailability",
      "ecr:BatchGetImage",
      "ecr:GetDownloadUrlForLayer"
    ]
    resources = [local.ecr_repository_arn]
  }
}

resource "aws_iam_role_policy" "apprunner_ecr_access" {
  name   = "${var.app_name}-ecr-access"
  role   = aws_iam_role.apprunner_ecr_access.id
  policy = data.aws_iam_policy_document.apprunner_ecr_access.json
}

data "aws_iam_policy_document" "apprunner_instance_assume" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["tasks.apprunner.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "apprunner_instance" {
  name               = "${var.app_name}-instance"
  assume_role_policy = data.aws_iam_policy_document.apprunner_instance_assume.json
}

data "aws_iam_policy_document" "read_runtime_secrets" {
  count = local.runtime_secrets_configured ? 1 : 0

  statement {
    actions   = ["secretsmanager:GetSecretValue"]
    resources = compact(values(local.runtime_secret_arns))
  }
}

resource "aws_iam_role_policy" "apprunner_instance_secrets" {
  count  = local.runtime_secrets_configured ? 1 : 0
  name   = "${var.app_name}-read-runtime-secrets"
  role   = aws_iam_role.apprunner_instance.id
  policy = data.aws_iam_policy_document.read_runtime_secrets[0].json
}

resource "aws_iam_openid_connect_provider" "github" {
  count = var.create_github_oidc_provider ? 1 : 0

  url             = "https://token.actions.githubusercontent.com"
  client_id_list  = ["sts.amazonaws.com"]
  thumbprint_list = ["6938fd4d98bab03faadb97b34396831e3780aea1"]
}

data "aws_iam_policy_document" "github_actions_assume" {
  count = var.enable_github_actions_role ? 1 : 0

  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type = "Federated"
      identifiers = [coalesce(
        var.github_actions_oidc_provider_arn,
        try(aws_iam_openid_connect_provider.github[0].arn, "arn:aws:iam::${data.aws_caller_identity.current.account_id}:oidc-provider/token.actions.githubusercontent.com")
      )]
    }

    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:aud"
      values   = ["sts.amazonaws.com"]
    }

    condition {
      test     = "StringLike"
      variable = "token.actions.githubusercontent.com:sub"
      values   = ["repo:${var.github_repository}:ref:refs/heads/${var.github_deploy_branch}"]
    }
  }
}

resource "aws_iam_role" "github_actions_deploy" {
  count              = var.enable_github_actions_role ? 1 : 0
  name               = "${var.app_name}-github-actions-deploy"
  assume_role_policy = data.aws_iam_policy_document.github_actions_assume[0].json

  lifecycle {
    precondition {
      condition     = var.github_actions_oidc_provider_arn != null || var.create_github_oidc_provider
      error_message = "Configure an existing GitHub OIDC provider ARN or enable its creation before enabling the deploy role."
    }
  }
}

data "aws_iam_policy_document" "github_actions_deploy" {
  count = var.enable_github_actions_role ? 1 : 0

  statement {
    actions   = ["ecr:GetAuthorizationToken"]
    resources = ["*"]
  }

  statement {
    actions = [
      "ecr:BatchCheckLayerAvailability",
      "ecr:CompleteLayerUpload",
      "ecr:InitiateLayerUpload",
      "ecr:PutImage",
      "ecr:UploadLayerPart"
    ]
    resources = [local.ecr_repository_arn]
  }

  statement {
    actions = [
      "apprunner:DescribeService",
      "apprunner:StartDeployment",
      "apprunner:UpdateService"
    ]
    resources = [local.app_runner_arn]
  }

  statement {
    actions   = ["iam:PassRole"]
    resources = [aws_iam_role.apprunner_ecr_access.arn, aws_iam_role.apprunner_instance.arn]
  }
}

resource "aws_iam_role_policy" "github_actions_deploy" {
  count  = var.enable_github_actions_role ? 1 : 0
  name   = "${var.app_name}-github-actions-deploy"
  role   = aws_iam_role.github_actions_deploy[0].id
  policy = data.aws_iam_policy_document.github_actions_deploy[0].json
}
