# 虚拟机搭建centos与ubuntu开发环境
## parallels安装centos

```
sudo yum install -y epel-release
sudo yum install -y dkms hpijs 
```

> 如果某些指令找不到，比如`makeinfo`, 可以通过`yum provides makeinfo`查找

centos8安装parallels tools, 如果出现问题，可以执行如下脚本
```shell
#!/usr/bin/env bash

PD_TOOL_PATH="/run/media/$(whoami)/Parallels Tools"
if [[ ! -d $PD_TOOL_PATH ]]; then
    echo "Please mount parallels tools disk before install"
    exit
fi
echo "Copy install files to /tmp/parallels_fixed"
cp -rf $PD_TOOL_PATH /tmp/parallels_fixed
chmod -R 755 /tmp/parallels_fixed
cd /tmp/parallels_fixed/kmods
echo "Unpack prl_mod.tar.gz"
tar -xzf prl_mod.tar.gz
rm prl_mod.tar.gz
echo "Patch prl_fs/SharedFolders/Guest/Linux/prl_fs/super.c"
sed '1i\#include <uapi/linux/mount.h>' -i prl_fs/SharedFolders/Guest/Linux/prl_fs/super.c
echo "Repack prl_mod.tar.gz"
tar -zcvf prl_mod.tar.gz . dkms.conf Makefile.kmods > /dev/null
cd /tmp/parallels_fixed
echo "Start install"
sudo ./install
echo "Remove /tmp/parallels_fixed"
rm -rf /tmp/parallels_fixed
```

clion远程调试:
- [cmake](https://blog.jetbrains.com/clion/2018/09/initial-remote-dev-support-clion/)  
- [makefile]()  

## vm安装ubuntu

> 系统安装成功后，可以在设置>>显示器>>切换分辨率为4k 200%  
> 另外要在虚拟机设置中开启`使用Retina全分辨率显示`  

### 开发工具
- [vscode](https://code.visualstudio.com/Download)  
- [clion](https://www.jetbrains.com/clion/download/#section=linux)  

vscode安装完成后，在命令行输入`code`指令启动，可以把它的图标加载启动栏中。

> clion进入界面后，选择工具(Tools)>>Create Command-line Launcher 和 Create Desktop Entry     
> 然后在应用程序中选择Clion图标启动，最终加入启动栏即可  

### 依赖库

- ifconfig工具  
```
sudo apt install net-tools
```

- openssh
```
sudo apt install openssh-server
```

- git
```
sudo apt install git
```

- gcc/g++
```
sudo apt install gcc g++ 
```

- [gdb](https://www.sourceware.org/gdb/download/)   
一般会自带，如果没有，那就下载源码安装

- make/cmake
```
sudo apt install make cmake 
```

默认版本
```
matrix@ubuntu:~$ make -version
GNU Make 4.3

matrix@ubuntu:~$ cmake -version
cmake version 3.18.4
```

- tcpdump 
默认安装





