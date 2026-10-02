data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "public" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }

  filter {
    name   = "map-public-ip-on-launch"
    values = ["true"]
  }
}

resource "aws_cloudwatch_log_group" "app" {
  name              = "/ecs/${var.app_name}"
  retention_in_days = 3
}

resource "aws_secretsmanager_secret" "runtime" {
  name = "${var.app_name}/runtime"
}

resource "aws_ecs_cluster" "app" {
  name = var.app_name
}

resource "aws_security_group" "app" {
  name        = "${var.app_name}-fargate"
  description = "Public evaluation access to the Franchise Hub container"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    description = "HTTP API for temporary evaluation"
    protocol    = "tcp"
    from_port   = 8080
    to_port     = 8080
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    description = "Required outbound access to ECR, AWS APIs and MongoDB Atlas"
    protocol    = "-1"
    from_port   = 0
    to_port     = 0
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_ecs_task_definition" "app" {
  family                   = var.app_name
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = "256"
  memory                   = "1024"
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn

  container_definitions = jsonencode([{
    name      = var.app_name
    image     = "${aws_ecr_repository.app.repository_url}:${var.image_tag}"
    essential = true

    portMappings = [{
      containerPort = 8080
      hostPort      = 8080
      protocol      = "tcp"
    }]

    environment = [
      { name = "JWT_ISSUER", value = "franchise-hub" },
      { name = "JWT_AUDIENCE", value = "franchise-hub-api" },
      { name = "JWT_EXPIRATION", value = "PT30M" }
    ]

    secrets = [
      { name = "MONGODB_URI", valueFrom = "${aws_secretsmanager_secret.runtime.arn}:MONGODB_URI::" },
      { name = "JWT_SECRET", valueFrom = "${aws_secretsmanager_secret.runtime.arn}:JWT_SECRET::" },
      { name = "FRANCHISE_APP_USERNAME", valueFrom = "${aws_secretsmanager_secret.runtime.arn}:FRANCHISE_APP_USERNAME::" },
      { name = "FRANCHISE_APP_PASSWORD", valueFrom = "${aws_secretsmanager_secret.runtime.arn}:FRANCHISE_APP_PASSWORD::" }
    ]

    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.app.name
        "awslogs-region"        = var.aws_region
        "awslogs-stream-prefix" = var.app_name
      }
    }
  }])
}

resource "aws_ecs_service" "app" {
  name            = var.app_name
  cluster         = aws_ecs_cluster.app.id
  task_definition = aws_ecs_task_definition.app.arn
  desired_count   = var.enable_service ? 1 : 0
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = data.aws_subnets.public.ids
    security_groups  = [aws_security_group.app.id]
    assign_public_ip = true
  }

  deployment_minimum_healthy_percent = 0
  deployment_maximum_percent         = 100

  depends_on = [aws_iam_role_policy.ecs_task_execution]
}
