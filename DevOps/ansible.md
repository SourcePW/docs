- # Ansible自动化部署
- ## [Ansible中文权威指南](http://www.ansible.com.cn/index.html) 

- [环境搭建](#环境搭建)
  - [linux系统](#linux系统)
      - [在 Control Machine 上安装 Ansible](#在-control-machine-上安装-ansible)
      - [在Managed Node 安装 OpenSSH server 和 Python](#在managed-node-安装-openssh-server-和-python)
      - [设定Ansible](#设定ansible)
  - [docker](#docker)
  - [jenkins-ansible](#jenkins-ansible)
  - [ansible 可视化AWX](#ansible-可视化awx)
  - [Ansible Tower 破解](#ansible-tower-破解)
- [Ansible使用](#ansible使用)
  - [vscode 安装ansible插件](#vscode-安装ansible插件)
  - [常用模块](#常用模块)
  - [Playbook](#playbook)
    - [详细日志](#详细日志)
  - [Demo实例](#demo实例)
- [常用操作](#常用操作)
  - [安装组件](#安装组件)
  - [ssh配置](#ssh配置)
  - [ansible 任务重试](#ansible-任务重试)


# 环境搭建

## linux系统
[参考文章](https://www.w3cschool.cn/automate_with_ansible/automate_with_ansible-1khc27p1.html)  
#### 在 Control Machine 上安装 Ansible
- #### Ubuntu (Apt)
安装 add-apt-repository 必要套件
```sh
sudo apt-get install software-properties-common
```

使用 Ansible 官方的 PPA 套件来源
```sh
sudo apt-get install software-properties-common; sudo apt-get update
```

安装 Ansible
```sh
sudo apt-get install -y ansible
```

- #### CentOS (Yum)
新增 epel-release 第三方套件来源
```sh
sudo yum install -y epel-release
```

安装 Ansible
```sh
sudo yum install -y ansible
```

- #### macos
```sh
brew install ansible
```

- #### pip安装
```sh
# Debian, Ubuntu
$ sudo apt-get install -y python3-pip

# CentOS
$ sudo yum install -y python-pip

# macOS
$ sudo easy_install pip
```

升级pip及ansible  
```sh
# 升级
sudo pip install -U pip

# 安装ansible
sudo pip install ansible
```

macos ansible配置
```sh
▶ ansible --version
ansible [core 2.13.3]
  config file = None
  configured module search path = ['/Users/ymm/.ansible/plugins/modules', '/usr/share/ansible/plugins/modules']
  ansible python module location = /usr/local/Cellar/ansible/6.3.0/libexec/lib/python3.10/site-packages/ansible
  ansible collection location = /Users/ymm/.ansible/collections:/usr/share/ansible/collections
  executable location = /usr/local/bin/ansible
  python version = 3.10.6 (main, Aug 30 2022, 05:11:14) [Clang 13.0.0 (clang-1300.0.29.30)]
  jinja version = 3.1.2
  libyaml = True
```

#### 在Managed Node 安装 OpenSSH server 和 Python
```sh
# ubuntu
sudo apt-get install -y openssh-server python2.7

# centos
sudo yum install -y openssh-server python
```

#### 设定Ansible  
我们可以借由 `·`ansible.cfg` 来设定预设的 `inventory` 档案的路径、远端使用者名称和 SSH 金钥路径等相关设定

- 安装好 `Ansible` 后，我们可以在 `/etc/ansible/` 的目录底下找到 `Ansible` 的设定档
- 通常我们较偏爱把 `ansible.cfg` 和 `hosts` 这两个档案与其它的 Playbooks 放在同个专案目录底下，然后通过版本控制系统 (例如 Git) 把它们一起储存起来，以实现 Ansible 的 Infrastructure as Code！

inventory 是什么？
`inventory` 就单字本身有详细目录、清单和列表的意思。在这里我们可以把它当成是一份主机列表，我们可通过它对定义每个 Managed Node 的代号、IP 位址、连线相关资讯和群组。  


- 若有对 Control Machine 本机操作的需求，建议于 `/etc/ansible/hosts` 补上 `local` 的设定。  

```sh
# For root user.
$ /bin/echo -e "[local]\nlocalhost ansible_connection=local" >> /etc/ansible/hosts

# For sudo user.
$ sudo su -c '/bin/echo -e "[local]\nlocalhost ansible_connection=local" >> /etc/ansible/hosts'
```

当已上的设置都完成了，您可以试著在终端机里用 Ansible 呼叫本机印出 Hello World

```sh
$ ansible localhost -m command -a 'echo Hello World.'
localhost | SUCCESS | rc=0 >>
Hello World.
```



## docker

```sh
# 下载镜像
docker pull chusiang/ansible-managed-node:ubuntu-20.04

# 创建容器
docker run -itd --name ansible-test -P chusiang/ansible-managed-node:ubuntu-20.04

# 进入容器
docker exec -it ansible-test bash
```

安装软件
```sh
apt-get update
apt-get upgrade

apt install vim
apt install sudo
apt-get install bash-completion

# 设置root密码, 密码为root
passwd

sudo apt-get install -y openssh-server python2.7

# 修改sshd配置
vim /etc/ssh/sshd_config
#PermitRootLogin prohibit-password
PermitRootLogin yes


```

查看镜像
```sh
$ docker ps
CONTAINER ID   IMAGE                                        COMMAND               CREATED          STATUS              PORTS                   NAMES
027f952a8890   chusiang/ansible-managed-node:ubuntu-20.04   "/usr/sbin/sshd -D"   11 minutes ago   Up About a minute   0.0.0.0:55000->22/tcp   ansible-test
```


增加配置文件
`/etc/ansible/ansible.cfg`
```sh
[defaults]

hostfile = hosts
remote_user = docker
host_key_checking = False
```

`/etc/ansible/hosts`
```sh
server1  ansible_ssh_host=127.0.0.1  ansible_ssh_port=55000

[local]
server1
```

测试
```sh
ansible all -m command -a 'echo Hello World on Docker.'
server1 | SUCCESS | rc=0 >>
Hello World on Docker.
```

```sh
ansible server1 -m ping
server1 | SUCCESS => {
    "ansible_facts": {
        "discovered_interpreter_python": "/usr/bin/python3"
    },
    "changed": false,
    "ping": "pong"
}
```


## jenkins-ansible  

首先更新jenkins,界面更新失败后直接替换`jenkins.war`  
```sh
/ # find / -name jenkins.war
/usr/share/jenkins/jenkins.war
/jenkins.war
/ # cp jenkins.war /usr/share/jenkins/jenkins.war
/ # ls -l /usr/share/jenkins/jenkins.war
-rw-r--r-- 1 root root 89532729 Oct  7 07:19 /usr/share/jenkins/jenkins.war
/ # md5sum /usr/share/jenkins/jenkins.war
6b93eded92a85693f325a7dd03e45269  /usr/share/jenkins/jenkins.war
/ # md5sum jenkins.war 
6b93eded92a85693f325a7dd03e45269  jenkins.war
```

https://plugins.jenkins.io/ansible/  
> jenkins 更新版本后，需要更新jdk的版本  
> sudo apt-get install openjdk-11-jdk  

需要使用pipeline `Pipeline script ` 或者 `Pipeline script from SCM`  
```sh
pipeline {
    agent { label 'ud1' }

    stages {
        stage('Hello') {
            steps {
                echo 'Hello World'
            }
        }
    }
}
```

https://www.jenkins.io/doc/pipeline/steps/ansible/  

首先看看语法:  
- `Adhoc`  允许完成简单的操作，而无需编写完整的剧本。 这提供了一种使用 Ansible 快速执行任务的便捷方法。  
- `Playbook`  该插件提供了多种便利，例如轻松使用 Jenkins 凭证存储中的凭证、日志中的无缓冲颜色输出等。  
- `Vault`    


示例
```sh
ansibleAdhoc credentialsId: 'private_key', inventory: 'inventories/a/hosts', hosts: 'hosts_pattern', moduleArguments: 'module_arguments'

ansiColor('xterm') {
    ansiblePlaybook(
        playbook: 'path/to/playbook.yml',
        inventory: 'path/to/inventory.ini',
        credentialsId: 'sample-ssh-key',
        colorized: true)
}

ansibleVault action: 'encrypt', input: 'vars/secrets.yml', vaultCredentialsId: 'ansible_vault_credentials'
```

ansiblePlaybook参数示例:
```sh
ansiblePlaybook: Invoke an ansible playbook
Execute an Ansible playbook. Only the playbook parameter is mandatory.
playbook : String
become : boolean (optional)
becomeUser : String (optional)
colorized : boolean (optional)
credentialsId : String (optional)
disableHostKeyChecking : boolean (optional)
dynamicInventory : boolean (optional)
extraVars (optional)
Nested Choice of Objects
extras : String (optional)
forks : int (optional)
hostKeyChecking : boolean (optional)
installation : String (optional)
inventory : String (optional)
inventoryContent : String (optional)
limit : String (optional)
skippedTags : String (optional)
startAtTask : String (optional)
sudo : boolean (optional)
sudoUser : String (optional)
tags : String (optional)
vaultCredentialsId : String (optional)
```


测试文件:
```sh
# hosts
t1 ansible_ssh_host=10.211.55.9  ansible_ssh_port=22  ansible_ssh_pass='711214'

[target]
t1

# main.yml
# 运行指令
# ansible-playbook devops/ansible/main.yml -i /etc/ansible/hosts -vv

# 主要流程
# code: language=ansible
# 需要指定host: -e variable_host=server1
- hosts: target
  remote_user: root
  roles:
    - "{{ role_param }}"
      

# firewall_deploy/tasks/main.yml 
- name: hello
  shell: echo role firewall

# playbook
pipeline {
    agent { label 'ud1' }
    
    environment {
        CODE_DIR = '/root/ansible/'
    }

    parameters {
        string defaultValue: '10.25.10.255', description: '部署设备的IP地址', name: 'device_ip'
        choice choices: ['root'], description: '登录用户', name: 'user'
        password defaultValue: 'Netvine123', description: '密码: 默认N*****3', name: 'passwd'
        string description: '如果不想从头运行，可以选择从哪个阶段开始', name: 'start'
        choice choices: ['firewall_deploy', 'firewall_kylinv10_deploy'], description: 'role', name: 'role_param'
    }
    
    stages {
        stage('ansible') {
            steps {
            	dir("${CODE_DIR}") {
                    echo 'Hello World'
                    sh "ansible-playbook main.yml -i /root/hosts -e role_param=firewall_deploy"
                }
            }
        }
    }
}
```

```sh
ansiblePlaybook colorized: true, disableHostKeyChecking: true, extras: 'role_param=${params.role_param}', inventory: '/root/hosts', playbook: '/root/ansible/main.yml'  
```

命令行执行
```sh
ansible-playbook main.yml -i /root/hosts -e role_param=firewall_deploy
```

## ansible 可视化AWX 
https://github.com/ansible/awx  

https://github.com/ansible/awx/blob/devel/INSTALL.md#installing-the-awx-cli  

```sh
pip3 install awxkit

Collecting awxkit
  Downloading awxkit-23.2.0-py3-none-any.whl (123 kB)
     |████████████████████████████████| 123 kB 36 kB/s 
Requirement already satisfied: requests in /usr/lib/python3/dist-packages (from awxkit) (2.22.0)
Requirement already satisfied: PyYAML in /usr/lib/python3/dist-packages (from awxkit) (5.3.1)
Installing collected packages: awxkit
Successfully installed awxkit-23.2.0
```

https://github.com/ansible/awx/blob/23.2.0/tools/docker-compose/README.md  

安装依赖
- Docker 
- Docker Compose 
- Andible
- OpenSSL  

> 更新setuptools版本 pip3 install --upgrade setuptools
> pip3 install setuptools_scm
> pip3 install launchpadlib  

修改docker容器内的pip镜像源
```sh
vim tools/ansible/roles/dockerfile/templates/Dockerfile.j2

RUN pip3 config set global.index-url https://pypi.tuna.tsinghua.edu.cn/simple
RUN RUN pip3 install virtualenv build psycopg

# 这个是本机配置的结果  
Writing to /root/.config/pip/pip.conf

# 配置文件内容
[global]
index-url = https://pypi.tuna.tsinghua.edu.cn/simple
```

```sh
# 下载源码，需要使用源码，依赖其他git仓库  
git clone -b 23.2.0  https://github.com/ansible/awx.git 

# 修改host配置, 本地可以不修改
vim tools/docker-compose/inventory

# 
make docker-compose-build 

# make最终执行的指令
ansible-playbook tools/ansible/dockerfile.yml \
        -e dockerfile_name=Dockerfile.dev \
        -e build_dev=True \
        -e receptor_image=quay.io/ansible/receptor:devel


```

## Ansible Tower 破解  

> 需要是Centos系统

```sh
wget https://releases.ansible.com/ansible-tower/setup/ansible-tower-setup-3.8.6-2.tar.gz 

tar -zxvf ansible-tower-setup-3.8.6-2.tar.gz 

cd ansible-tower-setup-3.8.6-2
bash -x setup.sh 


```

# Ansible使用 
[ansible自动化运维详细教程及playbook详解](https://juejin.cn/post/6844903631066513421)  
[练手脚本](../code/ansible/firewall-env.yml)  

## vscode 安装ansible插件
插件名称为`ansible`, [官方地址](https://marketplace.visualstudio.com/items?itemName=redhat.ansible)  

使用方法:  
对于在编辑器窗口中打开的 `Ansible` 文件，请确保将语言模式设置为`Ansible`（VS 代码窗口的右下角）  

插件信息:
<div align=center>
<img src="../resources/images/devops/ansible-plugin.png" width="75%"></img>
</div>

安装`ansible-lint`  
```sh
brew install ansible-lint
```

提示错误:  
> Ansible-lint is not available. Kindly check the path or disable validation using ansible-lint  

<div align=center>
<img src="../resources/images/devops/ansible-lint.png" width="75%"></img>
</div>

## 常用模块

## Playbook
执行指令  `ansible-playbook playbook.yml [--step | --start-at="task name"]`  
> --step 这样ansible在每个任务前会自动停止,并询问是否应该执行该任务
> --start-at 以上命令就会在名为”task name”的任务开始执行你的playbook.  

常用组件
- tasks：任务
- variables：变量
- templates：模板
- handlers：处理器
- roles：角色

### 详细日志
增加参数`-vvv`  

```json
The full traceback is:
WARNING: The below traceback may *not* be related to the actual failure.
  File "/tmp/ansible_mysql_db_payload_iH7xCz/ansible_mysql_db_payload.zip/ansible_collections/community/mysql/plugins/modules/mysql_db.py", line 680, in main
  File "/tmp/ansible_mysql_db_payload_iH7xCz/ansible_mysql_db_payload.zip/ansible_collections/community/mysql/plugins/module_utils/mysql.py", line 106, in mysql_connect
    db_connection = mysql_driver.connect(autocommit=autocommit, **config)
  File "/usr/local/lib/python2.7/dist-packages/pymysql/__init__.py", line 94, in Connect
    return Connection(*args, **kwargs)
  File "/usr/local/lib/python2.7/dist-packages/pymysql/connections.py", line 327, in __init__
    self.connect()
  File "/usr/local/lib/python2.7/dist-packages/pymysql/connections.py", line 588, in connect
    self._request_authentication()
  File "/usr/local/lib/python2.7/dist-packages/pymysql/connections.py", line 853, in _request_authentication
    auth_packet = self._read_packet()
  File "/usr/local/lib/python2.7/dist-packages/pymysql/connections.py", line 676, in _read_packet
    packet.raise_for_error()
  File "/usr/local/lib/python2.7/dist-packages/pymysql/protocol.py", line 223, in raise_for_error
    err.raise_mysql_exception(self._data)
  File "/usr/local/lib/python2.7/dist-packages/pymysql/err.py", line 107, in raise_mysql_exception
    raise errorclass(errno, errval)
fatal: [server1]: FAILED! => {
    "changed": false,
    "invocation": {
        "module_args": {
            "ca_cert": null,
            "chdir": null,
            "check_hostname": null,
            "check_implicit_admin": false,
            "client_cert": null,
            "client_key": null,
            "collation": "",
            "config_file": "/root/.my.cnf",
            "config_overrides_defaults": false,
            "connect_timeout": 30,
            "dump_extra_args": null,
            "encoding": "",
            "force": false,
            "hex_blob": false,
            "ignore_tables": [],
            "login_host": "localhost",
            "login_password": null,
            "login_port": 3306,
            "login_unix_socket": null,
            "login_user": "root",
            "master_data": 0,
            "name": [
                "firewall"
            ],
            "pipefail": false,
            "quick": true,
            "restrict_config_file": false,
            "single_transaction": false,
            "skip_lock_tables": false,
            "state": "present",
            "target": null,
            "unsafe_login_password": false,
            "use_shell": false
        }
    },
    "msg": "unable to find /root/.my.cnf. Exception message: (1045, u\"Access denied for user 'root'@'localhost' (using password: NO)\")"
}
```

## [Demo实例](../code/ansible/main.yml)  

# 常用操作
## 安装组件

https://galaxy.ansible.com/search?deprecated=false&keywords=&order_by=-relevance

```sh
ERROR! couldn't resolve module/action 'mysql_db'. This often indicates a misspelling, missing collection, or incorrect module path.

ERROR! couldn't resolve module/action 'timezone'
```

```sh
# mysql
ansible-galaxy collection install community.mysql

# 安装常用模块
ansible-galaxy collection install rjlasko.ansible
```

比如搜索`firewalld`模块，首选出现的是`poisx`  
https://galaxy.ansible.com/ansible/posix  

```sh
Modules
Name	Description
ansible.posix.acl	Set and retrieve file ACL information.
ansible.posix.at	Schedule the execution of a command or script file via the at command
ansible.posix.authorized_key	Adds or removes an SSH authorized key
ansible.posix.firewalld	Manage arbitrary ports/services with firewalld
```

`ansible.posix.firewalld`  也是可用的

## 安装角色组件
比如我下载一个nginx相关的角色:  
https://galaxy.ansible.com/ui/standalone/roles/1davidmichael/ansible-role-nginx/  

```sh
ansible-galaxy role install 1davidmichael.ansible-role-nginx

- downloading role 'ansible-role-nginx', owned by 1davidmichael
- downloading role from https://github.com/1davidmichael/ansible-role-nginx/archive/2.5.0.tar.gz
```

目录结构:
```sh
|-- LICENSE
|-- README.md
|-- defaults
|   `-- main.yml
|-- handlers
|   `-- main.yml
|-- meta
|   `-- main.yml
|-- tasks
|   |-- main.yml
|   |-- setup-Archlinux.yml
|   |-- setup-Debian.yml
|   |-- setup-FreeBSD.yml
|   |-- setup-OpenBSD.yml
|   |-- setup-RedHat.yml
|   |-- setup-Ubuntu.yml
|   `-- vhosts.yml
|-- templates
|   |-- nginx.conf.j2
|   |-- nginx.repo.j2
|   `-- vhost.j2
|-- tests
|   |-- README.md
|   `-- test.yml
`-- vars
    |-- Archlinux.yml
    |-- Debian.yml
    |-- FreeBSD.yml
    |-- OpenBSD.yml
    `-- RedHat.yml
```

main.yml中模版相关代码
```sh
- name: Define nginx_user.
  set_fact:
    nginx_user: "{{ __nginx_user }}"
  when: nginx_user is not defined

# Nginx setup.
- name: Copy nginx configuration in place.
  template:
    src: "{{ nginx_conf_template }}"
    dest: "{{ nginx_conf_file_path }}"
    owner: root
    group: "{{ root_group }}"
    mode: 0644
  notify:
    - reload nginx

# nginx_user,在变量中声明的
./vars/RedHat.yml:__nginx_user: "nginx"
./vars/OpenBSD.yml:__nginx_user: "www"
./vars/Debian.yml:__nginx_user: "www-data"
./vars/FreeBSD.yml:__nginx_user: "www"
./vars/Archlinux.yml:__nginx_user: "http"
```

模版:
```sh
user  {{ nginx_user }};

error_log  {{ nginx_error_log }};
pid        {{ nginx_pidfile }};

{% block worker %}
worker_processes  {{ nginx_worker_processes }};
{% endblock %}

{% if nginx_extra_conf_options %}
{{ nginx_extra_conf_options }}
{% endif %}

{% block events %}
events {
    worker_connections  {{ nginx_worker_connections }};
    multi_accept {{ nginx_multi_accept }};
}
{% endblock %}

http {
    {% block http_begin %}{% endblock %}

{% block http_basic %}
    include       {{ nginx_mime_file_path }};
    default_type  application/octet-stream;

    server_names_hash_bucket_size {{ nginx_server_names_hash_bucket_size }};

    client_max_body_size {{ nginx_client_max_body_size }};

    log_format  main  {{ nginx_log_format|indent(23) }};

    access_log  {{ nginx_access_log }};

    sendfile        {{ nginx_sendfile }};
    tcp_nopush      {{ nginx_tcp_nopush }};
    tcp_nodelay     {{ nginx_tcp_nodelay }};
```

## ssh配置
`/etc/ssh/ssh_config`
```sh
   StrictHostKeyChecking no
   UserKnownHostsFile /dev/null
```

## ansible 任务重试

> 主要担心网络相关的  

在 Ansible 中，如果您希望在任务失败时重试执行，可以使用 `retries` 和 `until` 参数。这通常与 `register` 一同使用，以便您可以检查任务的结果。

以下是一个示例，该示例展示了如何重试一个可能失败的命令。该任务将重试最多3次，每次间隔5秒：

```yaml
- name: This task might fail
  command: /some/command
  register: result
  until: result is succeeded
  retries: 3
  delay: 5
```

- `register: result` 会捕获任务的输出到 `result` 变量。
- `until: result is succeeded` 会检查任务是否成功。如果任务成功，Ansible 不会再进行重试。
- `retries: 3` 定义任务失败时的最大重试次数。
- `delay: 5` 设置每次重试之间的延迟时间（单位：秒）。

使用这种方法，您可以为 Ansible 任务设置失败时的重试次数及重试间隔。

# 疑问拓展
## 使用场景  
<br>
<div align=center>
<img src="../resources/images/devops/%E8%87%AA%E5%8A%A8%E5%8C%96%E8%BF%90%E7%BB%B4%E5%B7%A5%E5%85%B7.png" width="50%"></img>  
</div>
<br>

公司计划在年底做一次大型市场促销活动全面冲刺下交易额，为明年的上市做准备。公司要求各业务组对年底大促做准备，运维部要求所有业务容量进行三倍的扩容，并搭建出多套环境可以共开发和测试人员做测试，运维老大为了在年底有所表现，要求运维部门同学尽快实现，当你接到这个任务时，有没有更快的解决方案 ?  

<br>
<div align=center>
<img src="../resources/images/devops/%E8%87%AA%E5%8A%A8%E5%8C%96%E8%BF%90%E7%BB%B4%E5%B7%A5%E5%85%B71.png" width="65%"></img>  
</div>
<br>

<br>
<div align=center>
<img src="../resources/images/devops/%E8%87%AA%E5%8A%A8%E5%8C%96%E8%BF%90%E7%BB%B4%E5%B7%A5%E5%85%B72.png" width="65%"></img>  
</div>
<br>