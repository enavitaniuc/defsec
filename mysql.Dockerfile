# Use the official MySQL image as a parent image
FROM mysql:8.0

# The MySQL image automatically creates a user and database when these env vars are set:
# MYSQL_ROOT_PASSWORD - root password
# MYSQL_DATABASE - database to create
# MYSQL_USER - user to create (with full privileges on MYSQL_DATABASE)
# MYSQL_PASSWORD - password for MYSQL_USER
# These will be set via docker-compose.yml environment section

# Expose the default MySQL port
EXPOSE 3306
