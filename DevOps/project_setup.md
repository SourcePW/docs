- ## 审计开发环境搭建

- [平台开发环境搭建](#平台开发环境搭建)
  - [基础服务搭建(centos7.9)](#基础服务搭建centos79)
    - [nacos](#nacos)
    - [docker rabbitmq](#docker-rabbitmq-1)
    - [redis](#redis-1)
    - [mysql](#mysql)
    - [elasticsearch](#elasticsearch)
    - [MongoDB](#mongodb-1)
  - [配置](#配置-1)
    - [springboot nacos配置](#springboot-nacos配置)
    - [nvsp-web配置](#nvsp-web配置)
    - [gateway数据库配置](#gateway数据库配置)
  - [调试](#调试-1)
    - [必备服务](#必备服务)


### 基础服务搭建(centos6.10)
centos6安装成功后，需要修改网络配置及更换国内镜像源！
首先是修改网络，把onboot改成yes重启网络，然后就是修改国内镜像源[国内镜像更换方式](https://mirror.tuna.tsinghua.edu.cn/help/centos-vault/）

```
/etc/yum.repos.d/

#这个是版本号，根据自己系统版本自行修改
minorver=6.10

#替换镜像源
sudo sed -e "s|^mirrorlist=|#mirrorlist=|g" \
         -e "s|^#baseurl=http://mirror.centos.org/centos/\$releasever|baseurl=https://mirrors.tuna.tsinghua.edu.cn/centos-vault/$minorver|g" \
         -i.bak \
         /etc/yum.repos.d/CentOS-*.repo
         
```



#### python
```
# 需要下载依赖环境,要不然python很多模块加载不了(ssl)
yum -y groupinstall development

yum install -y zlib-devel bzip2-devel openssl-devel ncurses-devel sqlite-devel readline-devel tk-devel

# 下载源码
wget https://www.python.org/ftp/python/3.6.0/Python-3.6.0.tgz
tar -zxvf Python-3.6.0.tgz

#编译安装
./configure && make -j4 && make install

# 安装依赖库,安装有问题的库
chardet==3.0.4 

# 额外需要安装的库
pip3 install flask-wtf pygal docx image lxml python-docx xlrd xlwt M2Crypto 

certifi==2017.11.5
chardet==3.0.4
click==6.7
Flask==0.12.2
Flask-Compress==1.4.0
Flask-Login==0.4.1
Flask-SQLAlchemy==2.3.2
gevent==1.2.2
greenlet==0.4.12
grpcio==1.8.4
grpcio-tools==1.8.4
idna==2.6
IPy==0.83
itsdangerous==0.24
Jinja2==2.10
MarkupSafe==1.0
mysql-connector-python==8.0.5
olefile==0.44
Pillow==4.0.0
protobuf==3.5.1
psutil==5.4.3
pymongo==3.6.0
redis==2.10.6
requests==2.18.4
six==1.11.0
SQLAlchemy==1.2.0
urllib3==1.22
Werkzeug==0.12.1
pycrypto==2.6.1


```

#### docker mysql


使用docker安装,先安装docker
[Install Docker Engine on CentOS](https://docs.docker.com/engine/install/centos/)

```
sudo yum install -y yum-utils;
sudo yum-config-manager \
    --add-repo \
    https://download.docker.com/linux/centos/docker-ce.repo
    
sudo yum install docker-ce docker-ce-cli containerd.io

sudo systemctl start docker

sudo curl -L "https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose

sudo chmod +x /usr/local/bin/docker-compose

sudo ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose


#先停止docker服务
$ systemctl stop docker docker.socket

#修改docker data目录
$ mkdir /data
$ vim /etc/docker/daemon.json
增加如下内容
{ 
   "data-root": "/data/docker" 
}

#启动服务
systemctl start docker

#查看data目录
$ docker info | grep "Docker Root Dir"
Docker Root Dir: /data/docker

#开机自启动
systemctl enable docker
```

```
docker pull mysql:5.5.62

docker run --name mysql -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 --restart=always -d mysql:5.5.62

# 安装client
wget http://repo.mysql.com/mysql57-community-release-el7.rpm

rpm -Uvh ysql57-community-release-el7.rpm

yum install mysql-community-client


# mysql root用户远程登录
# 查看权限
mysql> select User,Password,authentication_string,Host from mysql.user;
mysql> GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY 'root' WITH GRANT OPTION;
mysql> FLUSH PRIVILEGES;

```


#### redis
```
docker pull redis:5.0

docker run -itd --name redis -p 6379:6379 --restart=always redis:5.0 --requirepass 'mima123#@!'

#进入镜像 redis-cli 设置密码
# 审计平台密码 mima123#@!
> config set requirepass mima123#@!

```

#### MongoDB

```
docker pull mongo:4.0.26-xenial

# 不用加 --auth
docker run -itd --name mongo -p 27017:27017 --restart=always mongo:4.0.26-xenial 

# 登录设置密码
docker exec -it mongo mongo admin
# 创建管理员账号
> db.createUser({ user: 'root', pwd: 'root', roles: [ { role: "root", db: "admin" } ] });

# 使用管理员账号登录
> db.auth('root', 'root')

# 创建audit数据库
> use audit;

# 创建一个名为 audit，密码为 audit!@#mima 的数据库用户。 
# readWrite 所有数据库
> db.createUser({ user: 'audit', pwd: 'audit!@#mima', roles: [{ role: "readWrite", db: "audit" }] });

# 查看表格
> show dbs;

# 查询所有角色权限(包含内置角色)
> db.runCommand({ rolesInfo: 1, showBuiltinRoles: true })

```

> pymongo.errors.OperationFailure: Authentication failed. 问题
> 在配置的过程中,python 连接mongo的授权是有问题的

#### docker rabbitmq

```
# 携带管理界面的
docker pull rabbitmq:3.7.7-management

docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 -v /data/rabbitmq:/var/lib/rabbitmq -e RABBITMQ_DEFAULT_USER=admin -e RABBITMQ_DEFAULT_PASS=admin --restart=always rabbitmq:3.7.7-management

# 进入管理界面
http://Server-IP:15672
```



### 配置
#### 审计项目配置信息
> web/config/database.py

需要更改host为目标机器域名！

```
DATABASE_CONFIG = {
    'host': '192.168.0.100',
    #'host': 'localhost',
    'port': 3306,
    'database': 'auditweb',
    'user': 'root',
    'password': 'root',
    'charset': 'utf8',
    'use_unicode': True,
    'get_warnings': True,
    'autocommit': True,
}

STREAT_DATABASE_CONFIG = {
    'host': '192.168.0.100',
    #'host': 'localhost',
    'port': 3306,
    'database': 'snort',
    'user': 'root',
    'password': 'root',
    'charset': 'utf8',
    'use_unicode': True,
    'get_warnings': True,
    'autocommit': True,
}

MONGODB_CONFIG = {
    'host': '192.168.0.100',
    'port': 27017,
    'user': "audit",
    'password': 'audit!@#mima'
}

REDIS_CONFIG = {
    'host': '192.168.0.100',
    'port': 6379,
    'passwd': 'mima123#@!',
    'channel': 'sensorinfo'
}
PAGE_PER_TOTAL = 5
PAGE_PER_PAGES = 10
```

#### 配置mysql数据库
目前有两个数据库: auditweb 和 snort 可以通过sql文件导入

导出平台目前数据
mysqldump 

#### 配置mongodb
目前只需要一张表nvsp表


### 调试
##### 调试模式
> 修改awsd.py内容为调试模式,不适用https
> ws = WebServer(debug=True, https=False, host="0.0.0.0", port=5050)

#### python debug
[vscode python debug](https://code.visualstudio.com/docs/python/debugging)

vscode launch.json配置

```
{
    "version": "0.2.0",
    "configurations": [
        {
            "name": "awsd.py debug",
            "type": "python",
            "request": "launch",
            "module": "flask",
            "env": {
                "FLASK_APP": "awsd.py",
                "GEVENT_SUPPORT":"True"
            },
            "args": [
                "start"
            ],
            "jinja": true
        }
    ]
}
```



## 平台开发环境搭建
### 基础服务搭建(centos7.9)
#### nacos

1. 通过docker镜像安装

```
cd nacos-docker ;

docker-compose -f example/standalone-derby.yaml  build && docker-compose -f example/standalone-derby.yaml up -d
```

2. 更改nacos配置，替换为服务器ip地址，如果所有jar都在服务器运行，保持127.0.0.1就行
服务地址
http://10.211.55.15:8848/nacos
nacos nacos

登录后导入配置,更改public.yaml中的host为服务器的ip地址，这里修改为p1

```
spring:
  rabbitmq:
    host: ${RABBIT_MQ_HOST:p1}
    port: ${RABBIT_MQ_PORT:5672}
    username: ${RABBIT_MQ_USERNAME:admin}
    password: ${RABBIT_MQ_PASSWORD:admin}
  redis:
    host: ${REDIS_HOST:p1}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:mima123#@!}
    jedis:
      pool:
        max-idle: 8
        min-idle: 0
        max-active: 8
        max-wait: -1
    timeout: 10000
  datasource:
    url: jdbc:mysql://${MYSQL_HOST:p1}:${MYSQL_PORT:3306}/nvsp?characterEncoding=utf8&useTimezone=true&serverTimezone=GMT%2B8
    username: ${MYSQL_USERNAME:root}
    password: ${MYSQL_PASSWORD:root}
    driver-class-name: com.mysql.cj.jdbc.Driver
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      test-on-borrow: true
      test-while-idle: true
  data:
    elasticsearch:
      cluster-name: es-cluster
      hosts: p1:9300,p1:9301,p1:9302
      pool : 5
      logPath : 1 # 选择日志存入的方式，1 将日志存入ElasticSearch；2存入数据库
    mongodb:
      uri: mongodb://nvsp:123456@p1:27017/nvsp

  elasticsearch:
    rest:
      uris: ["http://p1:9200","http://p1:9201","http://p1:9202"]
  servlet:
    multipart:
      max-file-size: 1GB
      max-request-size: 1GB

  zipkin:
    enabled: true
    sender:
      type: rabbit
  sleuth:
    sampler:
      probability: 1.0

#  mvc:
#    throw-exception-if-no-handler-found: true
jetcache:
  statIntervalMinutes: 15
  areaInCacheName: false
  hidePackages: com.netvine.gateway
  local:
    default:
      type: caffeine
      keyConvertor: fastjson
  remote:
    default:
      type: redis
      keyConvertor: fastjson
      valueEncoder: kryo
      valueDecoder: kryo
      poolConfig:
        minIdle: 5
        maxIdle: 20
        maxTotal: 50
      host: ${REDIS_HOST:p1}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:mima123#@!}
mybatis:
  configuration:
    map-underscore-to-camel-case: true
ribbon:
  ReadTimeout: 60000
  ConnectTimeout: 60000
logging:
  level:
    org.springframework.web: debug
    org.springframework.security: DEBUG
    com.springboot.auth: DEBUG
    org.apache.ibatis: debug
    java.sql.PreparedStatement: debugg
  path: logs/
  file:
    max-size: 1GB
pagehelper:
  helper-dialect: mysql
  reasonable: true
  support-methods-arguments: true
  params: count=countSql

management:
  endpoints:
    web:
      exposure:
        include: '*'
```

#### docker rabbitmq

```
# 携带管理界面的
docker pull rabbitmq:3.7.7-management

docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 -v /data/rabbitmq:/var/lib/rabbitmq -e RABBITMQ_DEFAULT_USER=admin -e RABBITMQ_DEFAULT_PASS=admin --restart=always rabbitmq:3.7.7-management

# 进入管理界面
http://Server-IP:15672
```

#### redis
```
docker pull redis:5.0

docker run -itd --name redis -p 6379:6379 --restart=always redis:5.0 --requirepass 'mima123#@!'

#进入镜像 redis-cli 设置密码
# 审计平台密码 mima123#@!
> config set requirepass mima123#@!

```

#### mysql 
[参考审计的mysql安装](#shenji-mysql-install)

#### elasticsearch

```
docker pull docker.io/elasticsearch:7.1.1

docker run -d --name elasticsearch -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" --restart=always docker.io/elasticsearch:7.1.1
```

#### MongoDB

```
docker pull mongo:4.0.26-xenial

docker run -itd --name mongo -p 27017:27017 --restart=always mongo:4.0.26-xenial --auth

# 登录设置密码
docker exec -it mongo mongo admin
# 创建管理员账号
> db.createUser({ user: 'nvsp', pwd: '123456', roles: [ { role: "root", db: "admin" } ] });

# 使用管理员账号登录
> db.auth('nvsp', '123456')

# 创建nvsp数据库
> use nvsp;

# 创建一个名为 audit，密码为 audit!@#mima 的数据库用户。
> db.createUser({ user: 'nvsp', pwd: '123456', roles: [{ role: "readWrite", db: "nvsp" }] });

# 查看表格
> show dbs;

```

> pymongo.errors.OperationFailure: Authentication failed. 问题
> 在配置的过程中,python 连接mongo的授权是有问题的

### 配置
#### springboot nacos配置
替换所有yml文件中的nacos配置:
127.0.0.1:8848=>server-ip:8848


#### nvsp-web配置
目前服务及数据库都在服务器运行，所有的jar包都在本机运行，只需要修改前端地址为本地ip就行了，修改协议为http，另外还要修改端口:默认是8444,这是nginx代理的端口，8443是真实端口
static/config/config_dev.js

比如: baseURL: 'http://192.168.31.122:8443', 

#### gateway数据库配置



### 调试
#### 必备服务
- GatewayAdminApplication
- GatewayApplication 
- Oauth2AuthenticationApplication 
- Oauth2AuthorizationApplication 
- SettingApplication
- WebApplication 
