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
