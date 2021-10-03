FROM mysql:5.7

# create our database
COPY ./mysql_config/post-deploy.sql /docker-entrypoint-initdb.d/

# turn on logging in mysql
COPY ./mysql_config/mysqld.cnf /etc/mysql/mysql.conf.d