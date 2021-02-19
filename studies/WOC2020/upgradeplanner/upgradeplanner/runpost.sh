sudo docker rm -f post
sudo docker run --name post -e POSTGRES_PASSWORD=123 \
                       -e POSTGRES_USER=arnout \
                       -e PGDATA=/var/lib/postgresql/data/pgdata \
                       -v /home/ubuntu/data:/var/lib/postgresql/data \
                       -p 5432:5432 \
                       -d postgres
