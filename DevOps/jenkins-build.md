- # jenkins构建

- [增加节点](#增加节点)
  - [认证方式](#认证方式)
  - [安装JDK8](#安装jdk8)
- [开发调试](#开发调试)
  - [vscode shell脚本](#vscode-shell脚本)
  - [vscode sftp](#vscode-sftp)
  - [指令提示补全](#指令提示补全)
  - [升级git](#升级git)
  - [shell脚本之间引用](#shell脚本之间引用)
  - [npm安装](#npm安装)
- [jenkins任务构建](#jenkins任务构建)
  - [参数](#参数)
  - [gitlab-token](#gitlab-token)
  - [git插件](#git插件)
  - [通过脚本获取数据作为参数](#通过脚本获取数据作为参数)
  - [工作空间及编译结果](#工作空间及编译结果)
  - [远程访问](#远程访问)
- [pipeline使用及调试](#pipeline使用及调试)
  - [环境搭建](#环境搭建)
  - [vscode jenkins pipeline debug](#vscode-jenkins-pipeline-debug)
  - [jenkins jack(微软)](#jenkins-jack微软)
- [bash](#bash)
  - [引用脚本](#引用脚本)
- [jenkins插件](#jenkins插件)
  - [更改插件URL](#更改插件url)
  - [`jobConfigHistory`任务找回及配置历史](#jobconfighistory任务找回及配置历史)
  - [`rebuild`重新构建](#rebuild重新构建)
  - [AnsiColor](#ansicolor)
  - [user build vars](#user-build-vars)
  - [Active Choices Plugin](#active-choices-plugin)
  - [build-name-setter](#build-name-setter)
  - [description setter](#description-setter)
  - [Email Extension Template](#email-extension-template)
  - [Git Parameter](#git-parameter)
  - [extended-choice-parameter](#extended-choice-parameter)
  - [MySQL Database](#mysql-database)
  - [Configuration Slicing](#configuration-slicing)
- [疑难杂症](#疑难杂症)
  - [jenkins插件自动更新了，到时节点连接不上](#jenkins插件自动更新了到时节点连接不上)



## 增加节点
### 认证方式
- Launch agents via SSH 使用ssh协议，从master向slave发起连接，由master主动发起请求
- Non verifying Verification Strategy 

### 安装JDK8

```shell
yum install java-1.8.0-openjdk
```

## 开发调试
### vscode shell脚本
需要安装`Bash Debug`插件调试shell, `shellman`插件自动补全功能 ,`shell-format`shell格式化`format document`  

> 需要安装`https://github.com/mvdan/sh`,使用指令`go install mvdan.cc/sh/v3/cmd/shfmt@latest` 安装即可  

可以增加文件保存自动格式化:
```json
"[shellscript]": {
        "editor.formatOnSave": true,
        "files.eol": "\n"
    },
```

shell debug
```json
{
    // 使用 IntelliSense 了解相关属性。 
    // 悬停以查看现有属性的描述。
    // 欲了解更多信息，请访问: https://go.microsoft.com/fwlink/?linkid=830387
    "version": "0.2.0",
    "configurations": [
        {
            "type": "bashdb",
            "request": "launch",
            "name": "Bash-Debug (simplest configuration)",
            "program": "${workspaceFolder}/build.sh"
        }
    ]
}
```

### vscode sftp
安装`SFTP`插件

`> SFTP: config` 打开sftp配置  


Debug插件
Debug
    Open User Settings.

On Windows/Linux - File > Preferences > Settings
- On `macOS` - `Code` > `Preferences` > `Settings`
- Set `sftp.debug` to true and reload vscode.

View the `logs` in `View` > `Output` > `sftp`.


- #### centos7 配置sftp 

```
# vim /etc/ssh/sshd_config

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

`systemctl restart sshd` 重启

> 如果重启sshd失败，可以把配置放在最后面试试  

测试登录:
```
sftp root@ip
sftp> ls -l
-rw-r--r--    1 0        0              30 Jul 30 03:43 a.sh
drwxr-xr-x   10 0        0            4096 Jul 19 04:09 allow-list
drwxr-xr-x    3 0        0            4096 Jul 29 13:30 audit
drwxr-xr-x    2 0        0            4096 Jul 23 06:05 audit-log
sftp> exit
```

vscode sftp配置
```json
{
    "name": "gs6",
    "host": "10.25.10.126",
    "protocol": "sftp",
    "port": 22,
    "username": "root",
    "password": "Netvine123",
    "remotePath": "/data/work/build-scripts",
    "connectTimeout": 20000,
    "uploadOnSave": true
}
```

提示`Timed out while waiting for handshake`，可能是超时时间太短了，需要增加`"connectTimeout": 20000,`  

`remotePath`是一个相对路径，比如说设置sftp的路径为`/data`,需要这里` "remotePath": "/work",`, 那就相当于访问`/data/work`  


如图:  

![[../resources/images/sftp-config.png]]


### 指令提示补全
```shell
yum install -y bash-completion
```

### 升级git
```shell
wget https://www.kernel.org/pub/software/scm/git/git-2.14.0.tar.gz --no-check-certificate
tar -zxvf git-2.14.0.tar.gz
```

安装
```shell
cd git-2.14.0
./configure --prefix=/usr/local/git all
make -j4 && make install

# 配置环境变量
ln -s /usr/local/git/bin/git /usr/bin/git
```

### shell脚本之间引用
可以通过`source`、`.filename`指令引入。但是需要确定被引入的脚本**具备可执行权限**`chmod +x`  

### npm安装
```shell
yum install nodejs npm 
```

但是需要安装指定版本`yum remove nodejs npm`
```shell
node --version
v12.22.12

npm --version
6.14.16
```

安装指定版本
```shell
curl -sL https://rpm.nodesource.com/setup_12.x | sudo bash -
sudo yum install –y nodejs npm 
node –version  # node v12.22.12 , npm 6.14.16
```

## jenkins任务构建
- ### [jenkins handbook](https://www.jenkins.io/doc/book/)  
### 参数
[官方文档](https://plugins.jenkins.io/uno-choice/) 

多选参数，需要安装`Extended Choice Parameter` 插件

![[../resources/images/jenkins-params.jpg]]


这里分隔符使用空格，方便把参数传递给脚本

### gitlab-token


![[../resources/images/jenkins-gitlab-token.png]]


使用凭据
git 插件支持Jenkins 凭证插件提供的`用户名/密码凭证`和`私钥凭证`。它不支持其他凭证类型，如秘密文本、秘密文件或证书。  

当使用HTTP 或 HTTPS 协议访问远程存储库时，插件需要用户名/密码凭据。其他凭证类型不适用于 HTTP 或 HTTPS 协议。  

当使用ssh 协议访问远程存储库时，插件需要`ssh 私钥凭证`。其他凭证类型不适用于 ssh 协议。  

### git插件
https://plugins.jenkins.io/git/  



### 通过脚本获取数据作为参数
目前的需求是通过jenkins界面选择升级包的路径，升级包路径在服务器，需要通过指令列举指定路径下所有文件夹

### 工作空间及编译结果  
每个Jenkins都有工作空间`WorkSpace`, 可以通过编译后的操作`Archive the artifacts`,过滤出自己需要展示并下载的文件 


![[../resources/images/jenkins-workspace.png]]


> jenkins执行结果，需要依赖脚本返回值，只要返回值为非0就是失败的。  



### 远程访问  
Jobs with parameters
You merely need to perform an HTTP POST on `JENKINS_URL/job/JOBNAME/build`. 


Jobs with parameters  
```shell
# String Parameters
curl JENKINS_URL/job/JOB_NAME/buildWithParameters \
  --user USER:TOKEN \
  --data id=123 --data verbosity=high

# File Parameter
curl JENKINS_URL/job/JOB_NAME/buildWithParameters \
  --user USER:PASSWORD \
  --form FILE_LOCATION_AS_SET_IN_JENKINS=@PATH_TO_FILE
```

## pipeline使用及调试

### 环境搭建

[jenkins安装官方文档](https://www.jenkins.io/zh/doc/book/installing/) 

```sh
# 本地创建jenkins目录
# 备份数据 docker cp $ID:/var/jenkins_home, 如果想使用本地数据:  -v jenkins:/var/jenkins_home
docker run  -u root -p 9898:8080 -p 50000:50000 --name jenkins -itd jenkinsci/blueocean 
```

访问网址``
查看密码
```sh
docker exec -it jenkins cat /var/jenkins_home/secrets/initialAdminPassword
a21fab6d39014246803f2a81ef446bf4
```

安装插件后重启即可。  
> http://localhost:9898/restart   安装插件过程中报错，需要重启一下  
> 用户密码: root/root

### vscode jenkins pipeline debug

> Pipeline是Jenkins2.X的最核心的特性，帮助Jenkins实现从CI到CD与DevOps的转变。Pipeline是一组插件，让Jenkins可以实现持续交付管道的落地和实施。  
- 安装groovy插件 `code-groovy`  
- `Jenkinsfile Support`  
- `Jenkins Jack`  
- `Jenkins Pipeline Linter Connector`  

> macos系统中vscode插件存储位置:`~/.vscode/extensions`  

- ### https://github.com/jenkinsci/pipeline-examples  

`Jenkins Jack`配置
```json
    "jenkins-jack.jenkins.connections": [
        {
            "name": "LocalJenkins",
            "uri": "http://10.25.10.112:8096",
            "username": "yangmingming",
            "crumbIssuer": true,
            "active": true
        }
    ],
```

> 如果Jack 连接不了jenkins，可能是jenkins的URL配置问题，最好使用IP访问。  

`@ext:janjoerke.jenkins-pipeline-linter-connector`

获取jenkins 用户和密码  
打开jenkins server -> 个人中心 -> 设置 -> API token
拿到user id 和 api token  
```sh
vscode 11503122041ab6fa6dd53a1724dee7e243
```

配置`linter`插件的参数，连接`jenkins`的server 做校验
command + ',' 打开 settings 搜索 jenkins 插件配置
配置以下几部分内容
```sh
user = root
token = 116f25bd335afcbaf7e183890fa4c37634
url =  http://localhost:8080/pipeline-model-converter/validate
```


![[../resources/images/devops/jenkins-pipeline-1.png]]


在Jenkins JACK中打开脚本，编写完后，直接运行  

编写groovy
- pipeline文件名称选择 .groovy结尾
- 在文件首行声明 groovy解释器 #!groovy  

```groovy
pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                echo 'Building..'
            }
        }
        stage('Test') {
            steps {
                echo 'Testing..'
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying....'
            }
        }
    }
}
```

### jenkins jack(微软)  

可以在vscode的中管理jenkins，比如创建流水线，运行及调试等。  
另外文件是保存到本地的，会上传到jenkins中，另外也可以下载jenkins任务的脚本，保存到本地，还是挺方便的。  


![[../resources/images/devops/jenkins-pipeline-2.png]]


> 脚本运行错误，需要允许。系统管理—>In-process Script Approval

## bash 
### 引用脚本
`build.sh`通过相对路径引用其他脚本文件
```shell
source config/config.sh
source git/git_utils.sh
source build/build_soft.sh
```

通过jenkins调用时
```shell
bash /path/to/build.sh
```

> 提示错误找不到`/config/config.sh`, 因为运行的根目录不在`build.sh`根目录，所以会提示错误。  

修改为:
```shell
source $(dirname "$0")/config/config.sh
source $(dirname "$0")/git/git_utils.sh
source $(dirname "$0")/build/build_soft.sh
```

`$(dirname "$0")` 运行的根目录。

但是还存在另一个问题,如果其他脚本再引用`config/config.sh`,如何引用,`config`文件夹在`build`之外，按照常理应该为`source ./../config/config.sh`
```shell
source $(dirname "$0")/../config/config.sh
```

目录结构如下:
```shell
build-scripts/
├── build
│   └── build_soft.sh
├── build.sh
├── config
│   └── config.sh
├── git
│   └── git_utils.sh
└── README.md
```

但是还是找不到。因为目前是`build.sh`引用的`source $(dirname "$0")/build/build_soft.sh`, 相当于把`build_soft.sh`脚本内容放到`build.sh`,还需要改为`source $(dirname "$0")/config/config.sh`  

> `source` `.` 相当于把其他脚本文件内容直接放到引用脚本内。所以工具类都不要引用脚本文件，全由`调用脚本`引用。  


最终调用
```shell
bash /data/jenkins-audit/build-scripts/update_script.sh
bash /data/jenkins-audit/build-scripts/build.sh $device_ips
```

## jenkins插件
### 更改插件URL

```sh
Dashboard>>系统管理>>插件管理>>高级设置>>升级站点  
默认是:https://updates.jenkins.io/update-center.json

修改为:
https://mirrors.tuna.tsinghua.edu.cn/jenkins/updates/update-center.json
```

### `jobConfigHistory`任务找回及配置历史  

Job Configuration History  

https://plugins.jenkins.io/jobConfigHistory/


### `rebuild`重新构建
https://plugins.jenkins.io/rebuild/


### AnsiColor  
官方地址：https://plugins.jenkins.io/ansicolor
安装方式：在Jenkins插件当中直接搜索即可安装。
功能说明：扩展支持我们在shell当中定义的echo -e指令，从而给一定的输出上颜色。
使用方式：点此跳转到使用介绍。(opens new window)

### user build vars  

官方地址：https://wiki.jenkins.io/display/JENKINS/Build+User+Vars+Plugin
安装方式：在Jenkins插件当中直接搜索即可安装。
功能说明：通过此插件，让整个Jenkins系统中的用户参数成为一个可调用的变量。
使用方式：在构建环境中选中Set Jenkins user build variables。


### Active Choices Plugin

官方地址：https://wiki.jenkins.io/display/JENKINS/Active+Choices+Plugin
安装方式：在Jenkins插件当中直接搜索即可安装。
功能说明：根据所选参数，自动调出对应参数所依赖的后续参数。
使用方式：点此跳转到使用介绍。(opens new window)

### build-name-setter 
官方地址：http://wiki.jenkins.io/display/JENKINS/Build+Name+Setter+Plugin
安装方式：在Jenkins插件当中直接搜索即可安装。
功能说明：通过这个插件，可以动态更改项目构建的名称。不要小瞧这个功能，有时候合理应用对于工作的效率提升，可是非常高的。比如，常用的钉钉插件推送的信息过于简单，有一些信息无法从中得知，其实它推送的就是项目构建的名称，这个时候我们可以通过更改项目名称，来直接将一些构建的变量进行传递。



![[../resources/images/devops/jenkins-build-param.png]]  



全局变量参考: http://localhost:9898/job/ansible-deploy/pipeline-syntax/globals

> 可以显示构建人  ${BUILD_USER}  

### description setter

官方地址：https://wiki.jenkins.io/display/JENKINS/Description+Setter+Plugin
安装方式：在Jenkins插件当中直接搜索即可安装。
功能说明：可以在构建名称下，定义一些描述信息的插件，也是非常好用的插件。




![[../resources/images/devops/build-name-setter-1.png]]  



### Email Extension Template
官方地址：https://wiki.jenkins.io/display/JENKINS/Email-ext+plugin
安装方式：在Jenkins插件当中直接搜索即可安装。
功能说明：Jenkins部署状态邮件通知插件。

### Git Parameter
官方地址：http://wiki.jenkins-ci.org/display/JENKINS/Git+Parameter+Plugin
安装方式：在Jenkins插件当中直接搜索即可安装。
功能说明：在参数化构建步骤当中，可添加Git的branch或者tag来作为参数进行构建。

### extended-choice-parameter  

官方地址：https://plugins.jenkins.io/extended-choice-parameter
安装方式：在Jenkins插件当中直接搜索即可安装。
功能说明：提供了一种灵活地参数化能力。回滚使用的这个插件。
使用方式：实现回滚版本选项列表的插件。

### MySQL Database  

官方地址：https://plugins.jenkins.io/database-mysql/
安装方式：在Jenkins插件当中直接搜索即可安装。
功能说明：提供了pipeline中调用MySQL进行增删改查的能力。

### Configuration Slicing
官方地址: https://plugins.jenkins.io/configurationslicing
这个插件支持批量修改项目配置  

## 疑难杂症  
### jenkins插件自动更新了，到时节点连接不上  

查看jenkins日志
```sh
{"log":"2023-11-06 02:57:43.258+0000 [id=84]\u0009INFO\u0009jenkins.InitReactorRunner$1#onAttained: Listed all plugins\r\n","stream":"stdout","time":"2023-11-06T02:57:43.259894055Z"}
{"log":"2023-11-06 02:57:43.641+0000 [id=95]\u0009SEVERE\u0009jenkins.InitReactorRunner$1#onTaskFailed: Failed Loading plugin Caffeine API Plugin v3.1.8-133.v17b_1ff2e0599 (caffeine-api)\r\n","stream":"stdout","time":"2023-11-06T02:57:43.643349604Z"}
{"log":"java.io.IOException: Failed to load: Caffeine API Plugin (caffeine-api 3.1.8-133.v17b_1ff2e0599)\r\n","stream":"stdout","time":"2023-11-06T02:57:43.643396129Z"}
{"log":" - Jenkins (2.361.4) or higher required\r\n","stream":"stdout","time":"2023-11-06T02:57:43.643405621Z"}
{"log":"\u0009at hudson.PluginWrapper.resolvePluginDependencies(PluginWrapper.java:1018)\r\n","stream":"stdout","time":"2023-11-06T02:57:43.64341305Z"}
{"log":"\u0009at hudson.PluginManager$2$1$1.run(PluginManager.java:542)\r\n","stream":"stdout","time":"2023-11-06T02:57:43.643420446Z"}
{"log":"\u0009at org.jvnet.hudson.reactor.TaskGraphBuilder$TaskImpl.run(TaskGraphBuilder.java:175)\r\n","stream":"stdout","time":"2023-11-06T02:57:43.643427332Z"}
{"log":"\u0009at org.jvnet.hudson.reactor.Reactor.runTask(Reactor.java:305)\r\n","stream":"stdout","time":"2023-11-06T02:57:43.643445611Z"}
{"log":"\u0009at jenkins.model.Jenkins$5.runTask(Jenkins.java:1158)\r\n","stream":"stdout","time":"2023-11-06T02:57:43.643452734Z"}
{"log":"\u0009at org.jvnet.hudson.reactor.Reactor$2.run(Reactor.java:222)\r\n","stream":"stdout","time":"2023-11-06T02:57:43.643459122Z"}
{"log":"\u0009at org.jvnet.hudson.reactor.Reactor$Node.run(Reactor.java:121)\r\n","stream":"stdout","time":"2023-11-06T02:57:43.643465648Z"}
{"log":"\u0009at jenkins.security.ImpersonatingExecutorService$1.run(ImpersonatingExecutorService.java:68)\r\n","stream":"stdout","time":"2023-11-06T02:57:43.64347225Z"}
{"log":"\u0009at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1128)\r\n","stream":"stdout","time":"2023-11-06T02:57:43.643479014Z"}
{"log":"\u0009at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:628)\r\n","stream":"stdout","time":"2023-11-06T02:57:43.643485594Z"}
{"log":"\u0009at java.base/java.lang.Thread.run(Thread.java:829)\r\n","stream":"stdout","time":"2023-11-06T02:57:43.643496847Z"}
{"log":"2023-11-06 02:57:43.644+0000 [id=99]\u0009SEVERE\u0009jenkins.InitReactorRunner$1#onTaskFailed: Failed Loading plugin Script Security Plugin v1275.v23895f409fb_d (script-security)\r\n","stream":"stdout","time":"2023-11-06T02:57:43.645574362Z"}
{"log":"java.io.IOException: Failed to load: Script Security Plugin (script-security 1275.v23895f409fb_d)\r\n","stream":"stdout","time":"2023-11-06T02:57:43.645610218Z"}
{"log":" - Failed to load: Caffeine API Plugin (caffeine-api 3.1.8-133.v17b_1ff2e0599)\r\n","stream":"stdout","time":"2023-11-06T02:57:43.645621489Z"}
{"log":" - Jenkins (2.387.3) or higher required\r\n","stream":"stdout","time":"2023-11-06T02:57:43.645665858Z"}
{"log":"\u0009at hudson.PluginWrapper.resolvePluginDependencies(PluginWrapper.java:1018)\r\n","stream":"stdout","time":"2023-11-06T02:57:43.645677016Z"}
{"log":"\u0009at hudson.PluginManager$2$1$1.run(PluginManager.java:542)\r\n","stream":"stdout","time":"2023-11-06T02:57:43.645686777Z"}
{"log":"\u0009at org.jvnet.hudson.reactor.TaskGraphBuilder$TaskImpl.run(TaskGraphBuilder.java:175)\r\n","stream":"stdout","time":"2023-11-06T02:57:43.645699382Z"}
{"log":"\u0009at org.jvnet.hudson.reactor.Reactor.runTask(Reactor.java:305)\r\n","stream":"stdout","time":"2023-11-06T02:57:43.645715574Z"}
{"log":"\u0009at jenkins.model.Jenkins$5.runTask(Jenkins.java:1158)\r\n","stream":"stdout","time":"2023-11-06T02:57:43.645726185Z"}
{"log":"\u0009at org.jvnet.hudson.reactor.Reactor$2.run(Reactor.java:222)\r\n","stream":"stdout","time":"2023-11-06T02:57:43.645753073Z"}
{"log":"\u0009at org.jvnet.hudson.reactor.Reactor$Node.run(Reactor.java:121)\r\n","stream":"stdout","time":"2023-11-06T02:57:43.645815625Z"}
{"log":"\u0009at jenkins.security.ImpersonatingExecutorService$1.run(ImpersonatingExecutorService.java:68)\r\n","stream":"stdout","time":"2023-11-06T02:57:43.645829965Z"}
{"log":"\u0009at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1128)\r\n","stream":"stdout","time":"2023-11-06T02:57:43.646008499Z"}
{"log":"\u0009at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:628)\r\n","stream":"stdout","time":"2023-11-06T02:57:43.646058022Z"}
{"log":"\u0009at java.base/java.lang.Thread.run(Thread.java:829)\r\n","stream":"stdout","time":"2023-11-06T02:57:43.646069923Z"}
{"log":"2023-11-06 02:57:43.647+0000 [id=99]\u0009SEVERE\u0009jenkins.InitReactorRunner$1#onTaskFailed: Failed Loading plugin Command Agent Launcher Plugin v1.6 (command-launcher)\r\n","stream":"stdout","time":"2023-11-06T02:57:43.64790055Z"}
{"log":"java.io.IOException: Failed to load: Command Agent Launcher Plugin (command-launcher 1.6)\r\n","stream":"stdout","time":"2023-11-06T02:57:43.647930544Z"}
{"log":" - Failed to load: Script Security Plugin (script-security 1275.v23895f409fb_d)\r\n","stream":"stdout","time":"2023-11-06T02:57:43.647944808Z"}
{"log":"\u0009at hudson.PluginWrapper.resolvePluginDependencies(PluginWrapper.java:1018)\r\n","stream":"stdout","time":"2023-11-06T02:57:43.647966455Z"}
{"log":"\u0009at hudson.PluginManager$2$1$1.run(PluginManager.java:542)\r\n","stream":"stdout","time":"2023-11-06T02:57:43.647980367Z"}
{"log":"\u0009at org.jvnet.hudson.reactor.TaskGraphBuilder$TaskImpl.run(TaskGraphBuilder.java:175)\r\n","stream":"stdout","time":"2023-11-06T02:57:43.648009963Z"}
```
> 日志显示`Caffeine API`和` Agent Launcher` 插件加载失败，通过界面查看`已安装`插件，发现需要升级jenkins版本。

























