# Horus: Log analysis and threat detection

## How to run

Create docker containers

```bash
  docker-compose up -d
```

Add elastic user

```bash
  docker exec elasticsearch bin/elasticsearch-users useradd <username> -p <password> -r superuser
```

Check if connection between elasticsearch, kibana and logstash works

```bash
docker logs <elasticsearch | kibana | logstash>
```

If there are no errors open [Kibana web server](https://localhost:5601)

### What to do if kibana/logstash can't reach elasticsearch

Generate new certificate authorities

```bash
  docker exec bin/elasticsearch-certutil ca --pem --out /usr/share/elasticsearch/config/certs/elastic-stack-ca.zip
```

Unzip this archive. Inside it there should be 2 files: `ca.crt` and `ca.key`

Copy these files to:

- elasticsearchh/config/certs
- logstash/config/certs
- kibana/certs

Now run this command to generate certificate for elasticsearch with Subject Alternative Names (SAN):

```bash
docker exec elasticsearch bin/elasticsearch-certutil cert --ca-cert /usr/share/elasticsearch/config/certs/ca.crt \
  --ca-key /usr/share/elasticsearch/config/certs/ca.key \
  --name elasticsearch-http \
  --dns localhost,127.0.0.1,elasticsearch,logstash \
  --ip 127.0.0.1,172.26.0.3,192.168.32.1,192.168.32.2,192.168.32.3,192.168.32.4,192.168.32.5 \
  --out /usr/share/elasticsearch/config/certs/elasticsearch-http.p12
```

Restart all containers:

```bash
docker restart elasticsearch
docker restart logstash
docker restart kibana
```

### FakeLogger

In order to create fake logs build and run horus kotlin application.

To generate fake geoips
download [GeoLite-City-Block-IPv4.csv](https://dev.maxmind.com/geoip/docs/databases/city-and-country/) and place it
there:
`src/main/resources/geoip/GeoLite2-City-Blocks-IPv4.csv`

When it's done simply run the application.

## Configuration files

### Elasticsearch

Elasticsearch configuration file can be found at `elasticsearch/config/elasticsearchh.yml`

Key fields:

- `xpack.security.http.ssl.keystore.path` - path to keystore (.p12 file)
- `xpack.security.http.ssl.keystore.password` - password to keystore

### Kibana

Kibana configuration file can be found at `kibana/kibana.yml`

Key fields:

- `elasticsearch.username`
- `elasticsearch.password`

### Logstash

Logstash configuration files can be found at:

- `logstash/config/logstash.yml`
- `logstash/config/pipelines.yml`
- `logstash/pipeline/logstash.conf`

Key fields in `logstash.yml`:

- `xpack.monitoring.elasticsearch.username`
- `xpack.monitoring.elasticsearch.password`

Key fields in `pipelines.yml`:

- `path.config` - path to config file with pipeline

File `logstash.conf` is file where you can create pipelines for logstash. Exmaple file takes logs from postgres database
and filters them with grok. At the end it outputs them to elasticsearch.

## Authors

- [@mkaniowski](https://www.github.com/mkaniowski)

