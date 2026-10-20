FROM postgres:17.2-alpine

ENV POSTGRES_DB=db
ENV POSTGRES_USER=usr
ENV POSTGRES_PASSWORD=pwd

COPY init/ /docker-entrypoint-initdb.d/