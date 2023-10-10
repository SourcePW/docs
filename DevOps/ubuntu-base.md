- # Ubuntu基础环境搭建  

- [基础配置](#基础配置)
- [ssh](#ssh)
- [sftp](#sftp)
- [docker](#docker)
- [git](#git)
- [go](#go)
- [gdb](#gdb)
- [vscode](#vscode)
  - [c/c++环境](#cc环境)
  - [go环境](#go环境)
  - [shell](#shell)
  - [宿主机安装sftp](#宿主机安装sftp)

## 基础配置

```sh
# 设置root用户密码
sudo passwd root
```

命令补全
```sh
sudo apt install bash-completion
```

开发者基础包
```sh
sudo apt update
sudo apt install build-essential
```

> 包括gcc,g++,和make  

静态ip配置
```sh
# This is the network config written by 'subiquity'
network:
  ethernets:
    ens33:
      dhcp4: no
      addresses:
        - 10.25.17.233/24
      gateway4: 10.25.17.1
      nameservers:
          addresses: [8.8.8.8, 114.114.114.114]
  version: 2
```

## 更改源

`mv /etc/apt/sources.list /etc/apt/sources.list.bak`  
```sh
deb http://mirrors.aliyun.com/ubuntu/ focal main restricted universe multiverse
deb-src http://mirrors.aliyun.com/ubuntu/ focal main restricted universe multiverse
deb http://mirrors.aliyun.com/ubuntu/ focal-security main restricted universe multiverse
deb-src http://mirrors.aliyun.com/ubuntu/ focal-security main restricted universe multiverse
deb http://mirrors.aliyun.com/ubuntu/ focal-updates main restricted universe multiverse
deb-src http://mirrors.aliyun.com/ubuntu/ focal-updates main restricted universe multiverse
deb http://mirrors.aliyun.com/ubuntu/ focal-proposed main restricted universe multiverse
deb-src http://mirrors.aliyun.com/ubuntu/ focal-proposed main restricted universe multiverse
deb http://mirrors.aliyun.com/ubuntu/ focal-backports main restricted universe multiverse
deb-src http://mirrors.aliyun.com/ubuntu/ focal-backports main restricted universe multiverse
```

更改后执行`apt update`  


## ssh 

```sh
sudo apt install openssh-server

# root用户登录
PermitRootLogin yes
```

## sftp
`/etc/ssh/sshd_config`  

```sh
#注释掉原有Subsystem，添加新的Sbusystem配置
#Subsystem  sftp    /usr/libexec/openssh/sftp-server                 
Subsystem sftp internal-sftp

#在添加如下配置
Match User taxue,root
ChrootDirectory /          # 这里需要配置根目录，如果不配置根目录，ssh登录时找不到/bin/bash

# 打开ssh、sftp
AllowTcpForwarding yes      # 设置为yes,否则ssh无法登陆
#ForceCommand internal-sftp
```

重启`systemctl restart sshd.service`  

## docker
使用官方安装脚本自动安装

```sh
curl -fsSL https://get.docker.com | bash -s docker --mirror Aliyun
```

也可以使用国内 daocloud 一键安装命令  
```sh
curl -sSL https://get.daocloud.io/docker | sh
```

## git

添加git-core PPA
```sh
sudo apt install software-properties-common -y
sudo add-apt-repository ppa:git-core/ppa
```

安装Git的新版本
```sh
sudo apt update
sudo apt install git
```

升级后的版本
```sh
# git version
git version 2.39.0
```

## go
官网地址: https://go.dev/dl/ 

```sh
# 下载
wget https://go.dev/dl/go1.18.9.linux-amd64.tar.gz

# 解压到指定位置
tar -zxvf go1.18.9.linux-amd64.tar.gz -C /usr/local/

# 添加到环境变量
vim ~/.bashrc 
PATH=$PATH:/usr/local/go/bin

source ~/.bashrc 

# 更新代理
go env -w GO111MODULE=on
go env -w  GOPROXY=https://goproxy.cn,direct
```

## gdb 
```sh
apt install gdb -y
gdb --version
GNU gdb (Ubuntu 9.2-0ubuntu1~20.04.1) 9.2
```

## vscode  
插件安装路径`/root/.vscode-server`  

vscode工程demo地址: https://github.com/ymm135/tools/tree/main/my/vscode_prj , 其中包含c/c++/go/java工程,拷贝到虚拟机中`scp -r my/vscode_prj root@ud1:/root/work`    

### c/c++环境
安装插件`C/C++ Extension Pack`  


### go环境
安装插件`Go`

dlv版本可能存在兼容问题:
```sh
# vscode默认安装最新
github.com/go-delve/delve/cmd/dlv@latest 

# 删除
rm -fr $GOPATH/pkg/mod/github.com/go-delve/delve@v1.21.1/
# 可能需要安装指定版本
go install github.com/go-delve/delve/cmd/dlv@v1.8.3 
```
### shell
安装插件`Bash Debug` 断点调试，`shellman` shell不全，`shell-format`代码不全  

```sh
        {
            "type": "bashdb",
            "request": "launch",
            "name": "bash debug",
            "program": "${file}"
        }
```

### 宿主机安装sftp  

安装`SFTP`插件

`> SFTP: config` 打开sftp配置  


Debug插件
Debug
    Open User Settings.

On Windows/Linux - File > Preferences > Settings
- On `macOS` - `Code` > `Preferences` > `Settings`
- Set `sftp.debug` to true and reload vscode.

View the `logs` in `View` > `Output` > `sftp`.


安装`SFTP`插件  
```json
{
    "name": "gs6",
    "host": "10.25.10.126",
    "protocol": "sftp",
    "port": 22,
    "username": "root",
    "password": "root",
    "remotePath": "/data/work/code",
    "connectTimeout": 20000,
    "uploadOnSave": true,
    "ignore": [
        ".vscode",
        ".git",
        ".DS_Store"
    ]
}
```

多个设备的配置:
```json
[
    {
      "name": "server1",
      "context": "server",
      "host": "host",
      "username": "root",
      "password": "pass",
      "remotePath": "/root/firewall-web-server/",
      "profiles": {
        "ud1": {
          "name": "Ud1 Server",
          "username": "root",
          "password": "root",
          "host": "ud1",
          "uploadOnSave": false
        },
        "gs0": {
          "name": "Gs0 Server",
          "host": "gs0",
          "remotePath": "/home/firewall-web-server/"
        }
      },
      
      "defaultProfile": "ud1"
   }
]
```



