# Use the official MySQL image as a parent image
FROM mysql:8.0

# Set environment variables for MySQL
ENV MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD}
ENV MYSQL_DATABASE=${MYSQL_DATABASE}
ENV MYSQL_USER=${MYSQL_USER}
ENV MYSQL_PASSWORD=${MYSQL_PASSWORD}

# Create a new user and grant privileges using environment variables
RUN echo "CREATE USER '${MYSQL_USER}'@'%' IDENTIFIED BY '${MYSQL_PASSWORD}';" >> /docker-entrypoint-initdb.d/init-user.sql
RUN echo "GRANT ALL PRIVILEGES ON ${MYSQL_DATABASE}.* TO '${MYSQL_USER}'@'%';" >> /docker-entrypoint-initdb.d/init-user.sql
RUN echo "FLUSH PRIVILEGES;" >> /docker-entrypoint-initdb.d/init-user.sql

# Expose the default MySQL port
EXPOSE 3306
