FROM nginx
WORKDIR /app

COPY package.json .
COPY package-lock.json .

RUN apt-get update \
    && apt-get install -y curl \
    && apt-get -y autoclean

RUN apt-get -y install curl gnupg
RUN curl -sL https://deb.nodesource.com/setup_11.x  | bash -
RUN apt-get -y install nodejs
RUN npm install --production

RUN apt-get update \
    && apt-get install -y lsof \
    && apt-get -y autoclean

COPY . .

ENV NODE_ENV='production'

EXPOSE 8080

ENTRYPOINT npm start
