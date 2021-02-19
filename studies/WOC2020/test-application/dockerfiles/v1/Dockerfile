FROM cedricdd/mt-api:base
WORKDIR /app

COPY common.js ./utils/common.js

ENV NODE_ENV='production'

EXPOSE 8080

ENTRYPOINT npm start
