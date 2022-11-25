- # docker

- [系统安装docker](#系统安装docker)
- [docker安装centos，再次打包为镜像](#docker安装centos再次打包为镜像)
- [iptables转发流量到docker容器](#iptables转发流量到docker容器)
- [手动制作自定义centos镜像包](#手动制作自定义centos镜像包)
  - [安装环境](#安装环境)
    - [C端](#c端)
    - [基础环境](#基础环境)
    - [前端](#前端)
  - [后端](#后端)
  - [docker网卡设置-搭建网桥](#docker网卡设置-搭建网桥)
  - [运行配置](#运行配置)
- [把系统备份打包为docker镜像](#把系统备份打包为docker镜像)
- [问题](#问题)
  - [Failed to get D-Bus connection: 不允许的操作](#failed-to-get-d-bus-connection-不允许的操作)
- [docker harbor](#docker-harbor)
  - [安装docker](#安装docker)
  - [harbor安装](#harbor安装)
  - [镜像操作](#镜像操作)
  - [常用命令](#常用命令)

## 系统安装docker
```shell
curl -fsSL https://get.docker.com | bash -s docker --mirror Aliyun
```

也可以使用国内 daocloud 一键安装命令

```shell
curl -sSL https://get.daocloud.io/docker | sh
```

## docker安装centos，再次打包为镜像  

下载`centos:7.9.2009`镜像  
```shell
docker pull centos:7.9.2009
```

```shell
$ docker images | grep -i centos 
centos                                          7.9.2009           eeb6ee3f44bd   13 months ago   204MB
```


```shell
mkdir data
docker run --name centos7 -itd -v path/to/data:/root/work centos:7.9.2009  
```

解压数据到docker
```shell
sudo tar -xvpzf backup.tar.gz -C / --numeric-owner  
```

## iptables转发流量到docker容器

[参考文章](https://yeasy.gitbook.io/docker_practice/advanced_network/bridge)  

docker服务启动时，会有一个默认网桥
```shell
docker0: flags=4163<UP,BROADCAST,RUNNING,MULTICAST>  mtu 1500
        inet 172.17.0.1  netmask 255.255.0.0  broadcast 172.17.255.255
        inet6 fe80::42:cbff:feca:454f  prefixlen 64  scopeid 0x20<link>
        ether 02:42:cb:ca:45:4f  txqueuelen 0  (Ethernet)
        RX packets 1568  bytes 17640614 (16.8 MiB)
        RX errors 0  dropped 0  overruns 0  frame 0
        TX packets 3844  bytes 452141 (441.5 KiB)
        TX errors 0  dropped 0 overruns 0  carrier 0  collisions 0
```

容器配置端口的的转发规则:  
```shell
$ iptables -nvL

Chain DOCKER (1 references)
 pkts bytes target     prot opt in     out     source               destination         
    0     0 ACCEPT     tcp  --  !docker0 docker0  0.0.0.0/0            172.17.0.2           tcp dpt:6379
    0     0 ACCEPT     tcp  --  !docker0 docker0  0.0.0.0/0            172.17.0.2           tcp dpt:3306
   18  1152 ACCEPT     tcp  --  !docker0 docker0  0.0.0.0/0            172.17.0.2           tcp dpt:443
```

如果想通过网口流量直接转发`eth1->docker-audit`  

除了默认的 `docker0` 网桥，用户也可以指定网桥来连接各个容器。  
在启动 Docker 服务的时候，使用 `-b BRIDGE`或`--bridge=BRIDGE` 来指定使用的网桥。  

安装工具:`yum install bridge-utils`  

删除网卡
```shell
$ sudo ip link set dev br0 down
$ sudo brctl delbr br0
```

设置网卡为混杂模式
```shell
sudo ifconfig enp2s0 promisc
```

```shell
# 创建网桥
sudo brctl addbr br0

# 添加物理网卡
sudo brctl addif br0 enp2s0

# 设置up状态
sudo ip link set dev br0 up

# 查看网卡状态
ip addr show br0

# 关闭docker原网桥
sudo systemctl stop docker
sudo ifconfig docker0 down

sudo systemctl start docker

vim /etc/docker/daemon.json
{
  "bridge": "br0"
}

```



## 手动制作自定义centos镜像包  


下载`centos:7.9.2009`镜像  
```shell
docker pull centos:7.9.2009
```

```shell
$ docker images | grep -i centos 
centos                                          7.9.2009           eeb6ee3f44bd   13 months ago   204MB
```


```shell
mkdir data
docker run --name centos7 -itd -v /root/work/docker/data:/root/work centos:7.9.2009  

docker exec -it centos7 bash
```

打包成镜像
```shell
# 提交
$ docker commit 13745a0f939b centos_self:1.0
sha256:769c93d51d0b74cfd39ae0623abec17f440c2c71737d45b36e3c177de10be5ea

REPOSITORY                                      TAG                IMAGE ID       CREATED              SIZE
centos_self                                     1.0                769c93d51d0b   About a minute ago   442MB
# 

# 打包
$ docker save -o centos_self_1.0.tar centos_self:1.0


# 拷贝到目标设备，导入运行
$ docker load --input centos_self_1.0.tar
174f56854903: Loading layer [==================================================>]  211.7MB/211.7MB
58ba7edb40a1: Loading layer [==================================================>]  241.7MB/241.7MB
Loaded image: centos_self:1.0


# 再次运行
docker run --name centos7 -itd -v /root/work/docker/data:/root/work centos_self:1.0

# 安装抓包工具
yum install net-tools tcpdump  
yum install wget sudo 
```

> 如果遇到权限问题:`Failed to get D-Bus connection: Operation not permitted`, docker run --name centos7.1 `--privileged=true` -itd -v /root/work/docker/data:/root/work centos_self:2.0 `/usr/sbin/init`  


### 安装环境
#### C端

首先更新
```
# 源
yum install epel-release

# 命令补全
yum install bash-completion bash-completion-extras
```

安装依赖
```shell
yum install gcc gcc-c++ gdb -y
yum install pcre-devel pcre -y
yum install zlib-devel ncurses-devel readline-devel -y
yum install mlocate -y
yum install samba-devel -y
yum install glib-devel -y 
yum install glib2-devel -y
yum install thrift-devel thrift-glib thrift -y 
yum install compat-glibc-headers -y
yum install libffi-devel -y
yum install bzip2 -y
yum install openssl-libs -y
yum install the_silver_searcher.x86_64 -y 
yum install openssl openssl-libs openssl-devel -y
yum install libpcap-devel libnet-devel libnfnetlink-devel libnetfilter_queue-devel -y
yum install gperftools-libs jansson-devel libcap-ng-devel file-devel libyaml-devel sqlite-devel -y
yum install awk bison flex mawk -y 
yum install dpkg -y 
yum install cscope ctags -y
```

建立一个文件
```
touch /usr/include/stropts.h
```

增加`.lib`的动态库
```shell
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH/usr/local/lib64/
```

重新启动后发现环境变量无法加载，拷贝到`/usr/lib64`

#### 基础环境

建立目录:
```shell
#  真实目录
mkdir /data/socket/
mkdir /data/allow-list/
mkdir /data/self-proto/
mkdir -p /opt/root/orangin/backend
mkdir -p /opt/root/orangin/frontend
mkdir -p /opt/root/orangin/ids


# 软连接目录
ln -s /opt/root/origin/backend /opt/audit


mkdir /data 
mkdir /data/audit 
touch /data/audit/device_info.conf
echo -e "sn=WT0210205222071009\nmn=2u\nmanager_network_card=eth0\ndetect_network_card=eth2" > /data/audit/device_info.conf

mkdir /data/socket
mkdir /data/supervisord
```


安装supervisior
```shell
yum install supervisor -y

# 配置文件`audit-server.ini`
[program:audit-server]
stopasgroup=true
user=root
startsecs=0
directory=/opt/audit/backend
command=sh a.sh
redirect_stderr=true
stdout_logfile=/data/supervisord/audit-server.log
priority=50

[program:audit-web]
stopasgroup=true
user=root
startsecs=0
directory=/opt/audit/frontend
command=/usr/local/nginx/sbin/nginx -g 'daemon off;'
redirect_stderr=true
stdout_logfile=/data/supervisord/audit-web.log
priority=30

[program:redis-server]
stopasgroup=true
user=root
startsecs=0
directory=/usr/local/redis/src
command=/usr/local/redis/src/redis-server /usr/local/redis/redis.conf
redirect_stderr=true
stdout_logfile=/data/supervisord/redis.log
priority=10

[program:engine-server]
stopasgroup=true
user=root
startsecs=0
directory=/opt/audit/ids/bin/
command=/opt/audit/ids/bin/southwest_engine -c  /opt/audit/ids/config/southwest_engine_3_0.yaml -i eth0
redirect_stderr=true
stdout_logfile=/data/supervisord/engine.log
priority=10

[program:mysql-server]
stopasgroup=true
user=mysql
autorestart=true
startsecs=0
command=/usr/sbin/mysqld --basedir=/usr --datadir=/var/lib/mysql --plugin-dir=/usr/lib64/mysql/plugin --user=mysql --log-error=/var/log/mysqld.log --pid-file=/var/run/mysqld/mysqld.pid --socket=/var/lib/mysql/mysql.sock
redirect_stderr=true
stdout_logfile=/data/supervisord/mysqld.log
priority=5
```


#### 前端

nginx安装及配置
```shell
wget http://nginx.org/download/nginx-1.12.2.tar.gz
tar -zxvf nginx-1.12.2.tar.gz
cd nginx-1.12.2/
# 执行配置
./configure --with-http_ssl_module
# 编译安装(默认安装在/usr/local/nginx)
make
make install
mkdir logs

```

### 后端

依赖库安装
```
# 用于时间更新
yum install ntp -y 
# 用于网卡数据统计 
yum install sysstat -y 
```


go 环境安装
```shell
tar -zxvf go1.18.8.linux-amd64.tar.gz -C /usr/local/

vim ~/.bash_profile
export PATH=$PATH:/usr/local/go/bin

# 修改代理
go env -w GOPROXY=https://goproxy.cn,direct

# 清理 
go mod tidy

```

mysql安装
```shell
# 通过捆绑包安装
https://downloads.mysql.com/archives/get/p/23/file/mysql-5.7.39-1.el7.x86_64.rpm-bundle.tar

tar -xvf   mysql-5.7.39-1.el7.x86_64.rpm-bundle.tar 

# 安装, 与下载源安装是一样的
$ yum install -y mysql-community-{server,client,common,libs}-*

$ systemctl start mysqld

$ grep password /var/log/mysqld.log
2022-11-03T09:21:57.020473Z 1 [Note] A temporary password is generated for root@localhost: 4tFfHg7ul3<O

# 修改密码:
alter user 'root'@'localhost' identified by 'root123#@!';

create user 'root'@'%' identified by 'root@123'; 
grant all privileges on *.* to 'root'@'%';
flush privileges;

# 导入数据
mysql -D audit -u root -p < /root/work/audit.sql 

```

nginx
```shell
wget http://nginx.org/download/nginx-1.12.2.tar.gz
tar -zxvf nginx-1.12.2.tar.gz
cd nginx-1.12.2/
# 执行配置
./configure
# 编译安装(默认安装在/usr/local/nginx)
make
make install
```

nginx配置
```shell
worker_processes  1;
events {
    worker_connections  1024;
}

http {
    include       mime.types;
    default_type  application/octet-stream;
                client_max_body_size 1024m;
    map $http_upgrade $connection_upgrade {
                  default upgrade;
                  ''   close;
                }
    sendfile        on;

    keepalive_timeout  300;
    proxy_connect_timeout  1800s;
    proxy_send_timeout  1800s;
    proxy_read_timeout  1800s;
    fastcgi_connect_timeout 1800s;
    fastcgi_send_timeout 1800s;
    fastcgi_read_timeout 1800s;
    ssl_protocols  TLSv1.2;
    ssl_ciphers 'ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES256-GCM-SHA384:ECDHE-ECDSA-AES256-GCM-SHA384:DHE-RSA-AES128-GCM-SHA256:DHE-DSS-AES128-GCM-SHA256:kEDH+AESGCM:ECDHE-RSA-AES128-SHA256:ECDHE-ECDSA-AES128-SHA256:ECDHE-RSA-AES128-SHA:ECDHE-ECDSA-AES128-SHA:ECDHE-RSA-AES256-SHA384:ECDHE-ECDSA-AES256-SHA384:ECDHE-RSA-AES256-SHA:ECDHE-ECDSA-AES256-SHA:DHE-RSA-AES128-SHA256:DHE-RSA-AES128-SHA:DHE-DSS-AES128-SHA256:DHE-RSA-AES256-SHA256:DHE-DSS-AES256-SHA:DHE-RSA-AES256-SHA:AES128-GCM-SHA256:AES256-GCM-SHA384:AES128-SHA256:AES256-SHA256:AES128-SHA:AES256-SHA:AES:CAMELLIA:!DES-CBC3-SHA:!aNULL:!eNULL:!EXPORT:!DES:!RC4:!MD5:!PSK:!aECDH:!EDH-DSS-DES-CBC3-SHA:!EDH-RSA-DES-CBC3-SHA:!KRB5-DES-CBC3-SHA';


    #gzip  on;

    server {
        listen       443  ssl; 
        server_name  localhost;
        #ssl on;
        ssl_certificate /usr/local/nginx/crt/ssl.crt;
        ssl_certificate_key /usr/local/nginx/crt/ssl.key;
	ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:HIGH:!aNULL:!MD5:!DES:!3DES;
        #ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE:ECDH:AES:HIGH:!NULL:!aNULL:!MD5:!ADH:!RC4;
       location ^~ /ws/  {
            proxy_pass http://127.0.0.1:3333/ws/;
            proxy_set_header  X-Real-IP  $remote_addr;
            proxy_set_header Host $host:3333;
            proxy_http_version 1.1;
            proxy_set_header Connection keep-alive;
            proxy_set_header Keep-Alive 600;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection "upgrade";
            proxy_connect_timeout 60;
            proxy_read_timeout 600;
        }

        location / {
            root   /opt/audit/frontend;
            try_files $uri $uri/ /index.html;
        }
        location /api {
            proxy_set_header Host $http_host;
            proxy_set_header  X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            rewrite ^/api/(.*)$ /$1 break;
            proxy_pass http://localhost:3333;
         }

         location /api/swagger/index.html {
            proxy_pass http://localhost:3333/swagger/index.html;
         }
    }
}
```

redis安装
```
#安装gcc
yum install -y gcc gcc-c++
#下载redis包
wget http://download.redis.io/releases/redis-5.0.7.tar.gz

mkdir /usr/local/redis
tar -zxvf redis-5.0.7.tar.gz -C /usr/local

#编译
cd /usr/local/redis-5.0.7
make
make install

#修改配置文件
vim redis.conf

bind 0.0.0.0	# 把 127.0.0.1 改为 0.0.0.0，监听所有 IPV4 地址，可以根据需求设置
daemonize no
requirepass root123#@!

```

### docker网卡设置-搭建网桥  

查看容器网络配置
```shell
$ docker inspect 62e505a0fcb0
```

```json
          "NetworkSettings": {
            "Bridge": "",
            "SandboxID": "f8358fdd46e6b22f5ba4ed6bcf4867931d2e069566f249dcd4b5e300e551deef",
            "HairpinMode": false,
            "LinkLocalIPv6Address": "",
            "LinkLocalIPv6PrefixLen": 0,
            "Ports": {},
            "SandboxKey": "/var/run/docker/netns/f8358fdd46e6",
            "SecondaryIPAddresses": null,
            "SecondaryIPv6Addresses": null,
            "EndpointID": "a855b6d98c3d0228afc106b1540ca54da7ea4dbe1130fce37c57abb037bdb281",
            "Gateway": "172.17.0.1",
            "GlobalIPv6Address": "",
            "GlobalIPv6PrefixLen": 0,
            "IPAddress": "172.17.0.2",
            "IPPrefixLen": 16,
            "IPv6Gateway": "",
            "MacAddress": "02:42:ac:11:00:02",
            "Networks": {
                "bridge": {
                    "IPAMConfig": null,
                    "Links": null,
                    "Aliases": null,
                    "NetworkID": "4ab5dabf9aec780a295ce6cb98640d990ea58e25c0adf9596662c7c2ab9bd978",
                    "EndpointID": "a855b6d98c3d0228afc106b1540ca54da7ea4dbe1130fce37c57abb037bdb281",
                    "Gateway": "172.17.0.1",
                    "IPAddress": "172.17.0.2",
                    "IPPrefixLen": 16,
                    "IPv6Gateway": "",
                    "GlobalIPv6Address": "",
                    "GlobalIPv6PrefixLen": 0,
                    "MacAddress": "02:42:ac:11:00:02",
                    "DriverOpts": null
                }
            }
        }
    }
```

```shell
"NetworkID": "4ab5dabf9aec780a295ce6cb98640d990ea58e25c0adf9596662c7c2ab9bd978",

[root@localhost ~]# docker network list
NETWORK ID     NAME      DRIVER    SCOPE
4ab5dabf9aec   bridge    bridge    local
c12a2625cdbc   host      host      local
084f295c1e61   none      null      local
```

docker容器抓取宿主机网卡数据
```shell
UUID=`uuidgen`
echo -e "TYPE=\"Bridge\"\nUUID=\"${UUID}\"\nDEVICE=\"br0\"\nONBOOT=\"yes\"" > /etc/sysconfig/network-scripts/ifcfg-br0

echo -e "BRIDGE=\"br0\"" > /etc/sysconfig/network-scripts/ifcfg-eth0

# 重启网卡
service network restart

# 
```

### 运行配置
```shell
# docker import centos_audit_4.0.tar centos_audit:4.0
docker load --input centos_audit_4.0.tar

docker run --privileged -itd -p 443:443 -p 3305:3306 -p 6378:6379 --restart=always --name centos_audit centos_audit:4.0 /usr/sbin/init
```

启动异常，找不到启动文件`/usr/sbin/init`
```
[root@d1 data]# docker run --privileged -itd --restart=always --name centos_audit1.0 centos_audit:1.0 /usr/sbin/init
a9d9637ba4fb39be1b6efda6ec8cd39a14057db56495e72284dc141bf4ce795d
docker: Error response from daemon: OCI runtime create failed: container_linux.go:380: starting container process caused: exec: "/usr/sbin/init": stat /usr/sbin/init: no such file or directory: unknown.
```


官方回复

`import` is used with the tarball which are created with `docker export`. `load` is used with the tarball which are created with `docker save`. If you want to look at those options check the below article.

```shell
[root@localhost work]# docker load --input centos_audit_4.0.tar 
174f56854903: Loading layer [==================================================>]  211.7MB/211.7MB
df315b7ffdda: Loading layer [==================================================>]  2.409GB/2.409GB
9912ff886c76: Loading layer [==================================================>]  3.168GB/3.168GB
Loaded image: centos_audit:4.0
```


## 把系统备份打包为docker镜像  

```shell
Usage:  docker load [OPTIONS]

Load an image from a tar archive or STDIN

Options:
  -i, --input string   Read from tar archive file, instead of STDIN
  -q, --quiet          Suppress the load output
``` 

比如centos系统备份文件为`centos.tar.gz`  

```shell
docker load -i centos.tar.gz
```

也可以使用
```shell
"docker import" requires at least 1 argument.
See 'docker import --help'.

Usage:  docker import [OPTIONS] file|URL|- [REPOSITORY[:TAG]]

Import the contents from a tarball to create a filesystem image
```

使用实例
```shell
docker import backup.tar.gz centos7-audit
```

启动
```shell

docker run --privileged -itd -p 4443:443 --restart=always --name Centos7-Audit centos7-audit /usr/sbin/init

docker exec -it Centos7-Audit /bin/bash

docker stop Centos7-Audit
docker rm Centos7-Audit

docker image rm centos7-audit
```




## 问题
### Failed to get D-Bus connection: 不允许的操作  

```shell
docker run -it --privileged=true -p 443:443 mycentos1 /bin/bash  
```

## [docker harbor](https://www.jianshu.com/p/467e8cdd9eec)    
1、Harbor是构建企业级私有docker镜像的仓库的开源解决方案，它是Docker Registry的更高级封装，它除了提供友好的Web UI界面，角色和用户权限管理，用户操作审计等功能外，它还整合了K8s的插件(Add-ons)仓库，即Helm通过chart方式下载，管理，安装K8s插件，而chartmuseum可以提供存储chart数据的仓库【注:helm就相当于k8s的yum】。另外它还整合了两个开源的安全组件，一个是Notary，另一个是Clair，Notary类似于私有CA中心，而Clair则是容器安全扫描工具，它通过各大厂商提供的CVE漏洞库来获取最新漏洞信息，并扫描用户上传的容器是否存在已知的漏洞信息，这两个安全功能对于企业级私有仓库来说是非常具有意义的。  

2、简单来说harbor就是VMWare公司提供的一个docker私有仓库构建程序，功能非常强大.  

harbor git 地址：https://github.com/goharbor/harbor
harbor支持k8s的helm安装和本地安装，我这次先择的安装方式是本地安装。  

### 安装docker 

1.需要安装docker并运行，docker安装可以参考：
https://blog.csdn.net/qq_35887546/article/details/105366356  


2.需要安装docker-compose  
```shell
curl -L "https://github.com/docker/compose/releases/download/1.23.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
#权限
chmod +x /usr/local/bin/docker-compose
# 查看版本
docker-compose --version
```

### harbor安装
下载地址：https://github.com/goharbor/harbor/releases  

```shell
tar zxf harbor-offline-installer-v1.10.1.tgz -C /data/
cd /data/harbor/

$ ls
common.sh  harbor.v1.10.1.tar.gz  harbor.yml  install.sh  LICENSE  prepare
```

编辑配置文件
harbor.yml 就是harbor的配置文件
harbor的数据目录为/data
编辑harbor.yml，修改hostname、https证书路径、admin密码

```shell
./prepare 
./install.sh 
```

### 镜像操作  

制作镜像
```shell
docker tag nginx:latest harbor域名地址:端口号/cicd/nginx:latest
```

本地上传
```shell
docker login harbor域名地址:端口号
docker push harbor域名地址:端口号/cicd/nginx
```

可以在后台看到镜像   

本地拉取
```shell
docker push harbor域名地址:端口号/cicd/nginx
```


### 常用命令
```shell
# 通过dockerfile构建镜像(本地)
docker build -f DockerfileProd -t server-prod:latest .

# 打tag
docker tag server-prod:latest 10.25.10.111/license/server-prod:latest

# 上传到私有仓库  
docker push 10.25.10.111/license/server-prod:latest
```

