# database
[JpaRepository](https://docs.spring.io/spring-data/jpa/docs/current/api/org/springframework/data/jpa/repository/JpaRepository.html) is used 
as a generic repository implementation to access the underlying data layer via the [datasource](https://docs.spring.io/spring-boot/docs/current/reference/html/howto-data-access.html).

# flyway configuration
flyway is used to configure and update the database to the osb required schema.
The configuration of [Flyway](https://flywaydb.org/)