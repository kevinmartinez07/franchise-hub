resource "mongodbatlas_project" "app" {
  name   = var.app_name
  org_id = var.atlas_org_id
}

resource "mongodbatlas_advanced_cluster" "app" {
  project_id   = mongodbatlas_project.app.id
  name         = var.app_name
  cluster_type = "REPLICASET"

  replication_specs = [{
    region_configs = [{
      electable_specs = {
        instance_size = "M0"
      }
      provider_name         = "TENANT"
      backing_provider_name = "AWS"
      region_name           = "US_EAST_1"
      priority              = 7
    }]
  }]
}

resource "mongodbatlas_database_user" "app" {
  project_id          = mongodbatlas_project.app.id
  username            = "franchise_app"
  auth_database_name  = "admin"
  password_wo         = var.atlas_db_password
  password_wo_version = var.atlas_db_password_version

  roles {
    role_name     = "readWrite"
    database_name = "franchise_hub"
  }
}

resource "mongodbatlas_project_ip_access_list" "local" {
  project_id = mongodbatlas_project.app.id
  cidr_block = var.atlas_client_cidr
  comment    = "Local evaluation access"
}

resource "mongodbatlas_project_ip_access_list" "public_runtime" {
  count      = var.atlas_allow_public_runtime ? 1 : 0
  project_id = mongodbatlas_project.app.id
  cidr_block = "0.0.0.0/0"
  comment    = "Temporary public runtime access for evaluation only"
}
