# Postgres in AGIEF
PostgreSQL can be utilised for data persistence, which can be defined in the `node.properties` file. It is important to note that we 
currently favour the 'in memory' persistence, and it is used by default.

## Requirements
- [PostgreSQL](http://www.postgresql.org/download)
- [PGAdmin](http://www.pgadmin.org/download) (Optional)

## Setup Instructions
Follow these instructions to correctly setup PostgreSQL on an EC2 instance.

1. Make sure the security group allows access to port 5432

2. Set security rules to allow connections from specific IP, or to the whole web by modifying the lines in `pg_hba.conf`

  - This line opens access to the whole web, with encrypted password
    - `host    all             all             0.0.0.0/0               md5`

  - This line is to give access to our coordinator node, without a password
    - `host    all             all             52.63.253.116/32        trust`

    - **Note:** this requires encrypted password. It could also be set to 'trust', which allows any connection.

3. On the instance, allow connections from anywhere, by having this line in `postgresql.conf`
  `listen_addresses='*'`
  
4. Restart the PostgreSQL server

5. Confirm that outgoing connections are not blocked on these ports

## Useful Commands

```
# restart
sudo /etc/init.d/postgresql  restart

# tell it to listen not just on localhost in here
sudo vi /etc/postgresql/9.3/main/postgresql.conf

# check which ports listening
netstat -pant

# set rules about security - who has access from where
sudo vi /etc/postgresql/9.3/main/pg_hba.conf 

# connect to postgres
psql -h db.x.agi.io -U postgres
```
