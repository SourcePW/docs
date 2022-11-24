- # linux 软件安装流程 

## ubuntu 
### dpkg 
Debian，以及基于 Debian 的系统，如 Ubuntu 等，所使用的包格式为 deb。以下为操作 deb 包的常用 Dpkg 指令表  

| 命令	                 | 作用 | 
| -- | -- |
| dpkg -i package.deb	 | 安装包  | 
| dpkg -r package	     | 删除包 | 
| dpkg -P package	     | 删除包（包括配置文件） | 
| dpkg -L package	     | 列出与该包关联的文件 | 
| dpkg -l package	     | 显示该包的版本 | 
| dpkg --unpack package.deb	 | 解开 deb 包的内容 | 
| dpkg -S keyword	 | 搜索所属的包内容 | 
| dpkg -l	 | 列出当前已安装的包 | 
| dpkg -c package.deb	 | 列出 deb 包的内容 | 
| dpkg --configure package	 | 配置包 | 

举例[python2.7-minimal](https://ubuntu.pkgs.org/20.04/ubuntu-updates-universe-amd64/python2.7-minimal_2.7.18-1~20.04.3_amd64.deb.html)  
```
python2.7-minimal_2.7.18-1~20.04.3_amd64.deb

安装
Update the package index:
# sudo apt-get update
Install python2.7-minimal deb package:
# sudo apt-get install python2.7-minimal

文件列表
/usr/bin/python2.7
/usr/share/binfmts/python2.7
/usr/share/doc/python2.7-minimal/README.Debian
/usr/share/doc/python2.7-minimal/copyright
/usr/share/lintian/overrides/python2.7-minimal
/usr/share/man/man1/python2.7.1.gz
```


### apt
高级包装工具（英语：`Advanced Packaging Tools`,简称：`APT` ）是Debian及其衍生发行版（如：ubuntu）的软件包管理器。APT可以自动下载，配置，安装二进制或者源代码格式的软 件包，因此简化了 Unix系统上管理软件的过程。


常用指令:
`apt-cache search packagename` 搜索包  
`apt-cache show packagename` 获取包的相关信息，如说明、大小、版本等  
`apt-get install packagename` 安装包   
`apt-get install packagename --reinstall` 重新安装包  
`apt-get -f install` 修复安装”-f = –fix-missing”  
`apt-get remove packagename` 删除包  
`apt-get remove packagename --purge` 删除包，包括删除配置文件等  
`apt-get update` 更新源  
`apt-get upgrade` 更新已安装的包  
`apt-get dist-upgrade` 升级系统  
`apt-get dselect-upgrade` 使用 dselect 升级  
`apt-cache depends packagename` 了解使用依赖  
`apt-cache rdepends packagename` 是查看该包被哪些包依赖  
`apt-get build-dep packagename` 安装相关的编译环境  
`apt-get source packagename` 下载该包的源代码  
`apt-get clean` 清理无用的包  
`apt-get autoclean` 清理无用的包  
`apt-get check` 检查是否有损坏的依赖  


### 工作原理
[apt github](https://github.com/Debian/apt) 


众所周知，在linux操作系统下，利用`apt-get`来安装软件是非常方便的。只要一个`sudo apt-get install` 软件名；就可以轻易的解决软件的安装，最关键的是他可以解决其中存在的各种复杂的依赖关系，让你不用为此头疼，而在`apt-get`出现之前，利用`dpkg`或者更早之前的`./configure;make;make install`来安装软件真是无法想象，安装完软件A，提示你要安装软件B，好不容易安装完了，又提示要安装软件C...

比如目前的`source.list`的配置
```shell
# See http://help.ubuntu.com/community/UpgradeNotes for how to upgrade to
# newer versions of the distribution.
deb http://cn.archive.ubuntu.com/ubuntu focal main restricted
# deb-src http://cn.archive.ubuntu.com/ubuntu focal main restricted
```

apt install 流程

1. 扫描本地存放的软件包更新列表（由“apt-get update”命令刷新更新列表，也就是/var/lib/apt/lists/），找到最新版本的软件包；
2. 进行软件包依赖关系检查，找到支持该软件正常运行的所有软件包；
3. 从软件源所指 的镜像站点中，下载相关软件包，并存放在/var/cache/apt/archive；
4. 解压软件包，并自动完成应用程序的安装和配置。

软件包列表文件:`/var/lib/apt/lists/cn.archive.ubuntu.com_ubuntu_dists_focal-updates_universe_binary-amd64_Packages`  

对应源的地址 http://cn.archive.ubuntu.com/ubuntu/dists/focal/universe/binary-amd64/

`python2.7-minimal`说明  
```shell
Package: python2.7-minimal
Architecture: amd64
Version: 2.7.18-1~20.04.3
Multi-Arch: allowed
Priority: optional
Section: universe/python
Source: python2.7
Origin: Ubuntu
Maintainer: Ubuntu Developers <ubuntu-devel-discuss@lists.ubuntu.com>
Original-Maintainer: Matthias Klose <doko@debian.org>
Bugs: https://bugs.launchpad.net/ubuntu/+filebug
Installed-Size: 3710
Pre-Depends: libc6 (>= 2.29), zlib1g (>= 1:1.2.0)
Depends: libpython2.7-minimal (= 2.7.18-1~20.04.3)
Recommends: python2.7
Suggests: binfmt-support
Conflicts: binfmt-support (<< 1.1.2)
Replaces: python2.7 (<< 2.7.8-7~)
Filename: pool/universe/p/python2.7/python2.7-minimal_2.7.18-1~20.04.3_amd64.deb
Size: 1279592
MD5sum: f00e3cb2ec2c4225003fade7ff920561
SHA1: 692e3590016255e30827f821cfebe4563176407c
SHA256: 2ce92e26e48a4b064b213b7643c848300a2435b7f04b5d92349decbcf01398a9
SHA512: 67158e045b5cfb0fff2946fc521799dfedc7b0c5cd1f5b9384037adef0ecc15c518ac7b7cd32a2d3b8938d88e62dfe0c56fb24575a6ec2d06cfc4fd621c9cbeb
Description: Minimal subset of the Python language (version 2.7)
Task: ubuntustudio-desktop-core, ubuntustudio-desktop
Cnf-Visible-Pkgname: python2.7
Description-md5: 4e3d580f5374e0e392e97c8e6fedf594
```

filename:`pool/universe/p/python2.7/python2.7-minimal_2.7.18-1~20.04.3_amd64.deb`  


软件包地址: `http://archive.ubuntu.com/ubuntu/`+`filename`,最终网址如下:  
http://archive.ubuntu.com/ubuntu/pool/universe/p/python2.7/python2.7-minimal_2.7.18-1~20.04.3_amd64.deb  





