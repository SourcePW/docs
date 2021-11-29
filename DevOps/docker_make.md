# docker 镜像制作

镜像制作大致有两种方式:
1. 在基础镜像中扩展，直接打包镜像
2. 通过Dockerfile或者compose yml文件制作  

## 制作Dockerfile  
$ mkdir nginx-golang  
$ cd nginx-golang  
$ touch Dockerfile  

```
#Dockerfile for my httpd
	
FROM centos:7

LABEL maintainer="chuan <chuan@test.com>"

ENV doc_root=/var/www/html \
    listen_port=80 \
    server_name=localhost

RUN yum makecache && \
    yum install -y httpd php php-mysql && \
    yum clean all 

ADD phpinfo.php ${doc_root}
ADD entrypoint.sh /bin/

EXPOSE 80/tcp

VOLUME ${doc_root}

CMD ["/usr/sbin/httpd","-DFOREGROUND"]
ENTRYPOINT ["/bin/entrypoint.sh"] 

```

## docker-compose.yml  

示例: https://github.com/ymm135/goweb-gin-demo/blob/master/docker/docker-compose.yml  

[官方说明文档](https://docs.docker.com/compose/compose-file/compose-file-v3/#command)  

```
version: '3.7'
services:
  mysql:
    image: mysql:5.7
    container_name: mysql
    restart: always
    environment:
      MYSQL_DATABASE: weekly_report
      MYSQL_ROOT_PASSWORD: root
    ports:
      - 3306:3306
    volumes:
      - ./mysql/initdb.d:/docker-entrypoint-initdb.d
      - ./mysql/conf.d:/etc/mysql/conf.d

  nginx:
    depends_on:
      - mysql
    image: nginx:1.21.4-alpine
    container_name: nginx
    restart: always
    privileged: true
    ports:
      - 8980:8980
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ../web/dist:/usr/local/web

  golang:
    depends_on:
      - mysql
    image: golang:1.16.10-alpine
    container_name: weekly_report
    working_dir: /usr/local/weekly_report
    command: go run /usr/local/weekly_report/main.go
    environment:
      - GO111MODULE=on
      - GOPROXY=https://goproxy.cn
    privileged: true
    restart: always
    ports:
      - 8981:8981
    volumes:
      - ../server/:/usr/local/weekly_report
      - ./web/:/usr/local/web

```
