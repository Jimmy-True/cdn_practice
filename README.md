# cdn_practice
A project practicing my skills of CDN, Nginx, SpringBoot (Java), Python3, Kafka, Redis, Docker, and MongoDB

## Installtion
#### DNSMasq configuration (Optional)
I use `dnsmasq` to configure DNS.
edit /etc/dnsmasq.conf and add the following line:
```
server=8.8.8.8
user=root
log-facility=/your_path/dnsmasq.log
strict-order
# Only resolve requests made by local applications
listen-address=127.0.0.1
# Any request matches <cdn_domain> will be redirected to 127.0.0.1#8053
server=/<cdn_domain>/127.0.0.1#8053
# <speedup_domain>.<cdn_domain> is the URL of cdn provider
cname=<speedup_domain>,<speedup_domain>.<cdn_domain>
```

This section is optional, should update nginx.conf accordingly to test the cname, ns, etc. But this is not in the scope of playing around with CDN.
#### Build SpringBoot Application
under `source_server` dir, run `mvn clean package -DskipTests`

#### Docker compose
under the root dir, run `docker-compose up -d`

This step will pull Redis as Caching, MongoDB as Database, and wrap the SpringBoot into a docker container to compose a source server. This step also pulls Nginx as edge node, and wrap a simple py script into a docker container as the DNS of the CDN provider.

#### Test
Make a request within the same OS of Dnsmasq as it only responds to requests made locally:

```
<!-- upload image -->
curl -X POST -F "file=@PATH_TO_IMAGE.jpg" -F "name=IMAGE_NAME" http://<speedup_domain>.<cdn_domain>/images/upload

<!-- get image -->
curl http://<speedup_domain>.<cdn_domain>/images/IMAGE_NAME.jpg -output IMAGE_NAME.jpg

<!-- delete image -->
curl -X POST -F "name=IMAGE_NAME" http://<speedup_domain>.<cdn_domain>/images/delete
```