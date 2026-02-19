# To connect to supabase via SSL, run this command to register the certificate with postgress
cat {location of downloaded prod-ca-2021.crt} >> ~/.postgresql/root.crt

# From loving-backend directory:
mkdir -p ~/.postgresql
cat certs/supabase-ca.crt >> ~/.postgresql/root.crt

# To generate schema DDL, add this in the application-dev.yml
jpa:
    hibernate:
      ddl-auto: none
    properties:
      jakarta:
        persistence:
          schema-generation:
            database:
              action: none
            scripts:
              action: create
              create-source: metadata
              create-target: target/generated-schema-dev.sql
