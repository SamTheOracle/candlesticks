docker run -p 27017:27017 -e MONGO_INITDB_ROOT_USERNAME="root" -e MONGO_INITDB_ROOT_PASSWORD="changeit" -e MONGO_INITDB_DATABASE="streamevents" --name mongodbdev -d  mongo