- # docker
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

