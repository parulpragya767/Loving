GRANT loving_migration TO postgres;

-- Make migration role own schema
ALTER SCHEMA loving OWNER TO loving_migration;

-- Remove public access
REVOKE ALL ON SCHEMA loving FROM PUBLIC;

-- Allow DB connection
GRANT CONNECT ON DATABASE postgres TO loving_migration, loving_app, loving_readonly;

-- Schema privileges
GRANT USAGE, CREATE ON SCHEMA loving TO loving_migration;
GRANT USAGE ON SCHEMA loving TO loving_app, loving_readonly;

-- Existing objects
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA loving TO loving_migration;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA loving TO loving_migration;

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA loving TO loving_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA loving TO loving_app;

GRANT SELECT ON ALL TABLES IN SCHEMA loving TO loving_readonly;

-- Default privileges for future objects created by loving_migration
ALTER DEFAULT PRIVILEGES FOR ROLE loving_migration IN SCHEMA loving
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO loving_app;

ALTER DEFAULT PRIVILEGES FOR ROLE loving_migration IN SCHEMA loving
GRANT USAGE, SELECT ON SEQUENCES TO loving_app;

ALTER DEFAULT PRIVILEGES FOR ROLE loving_migration IN SCHEMA loving
GRANT SELECT ON TABLES TO loving_readonly;