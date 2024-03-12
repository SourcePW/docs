- # Linux 系统备份与还原

- [前提准备](#前提准备)
  - [selinux](#selinux)
  - [调整分区与挂载表](#调整分区与挂载表)
- [备份](#备份)
- [还原](#还原)
- [疑问](#疑问)
  - [修改Linux系统用户密码](#修改linux系统用户密码)
  - [网卡名称没有备份成功](#网卡名称没有备份成功)
  - [linux tab提示](#linux-tab提示)
  - [防火墙增加规则](#防火墙增加规则)
  - [串口设置(centos7)](#串口设置centos7)
  - [系统无法启动](#系统无法启动)
  - [双盘挂载](#双盘挂载)


## 前提准备
### selinux
关闭selinux
```
setenforce 0

vim /etc/selinux/config
SELINUX=enforcing => SELINUX=disabled
```

> 如果不关闭Selinux,用户名及密码需要重置，要不然无法登陆  



### 调整分区与挂载表

```
root@host1:~# parted /dev/sdb
GNU Parted 3.2
Using /dev/sdb
Welcome to GNU Parted! Type 'help' to view a list of commands.
(parted) mklabel msdos
Warning: The existing disk label on /dev/sdb will be destroyed and all data on this disk will be lost. Do you want to continue?
Yes/No? yes
(parted) quit
Information: You may need to update /etc/fstab.
```

```shell
mkdir /data

#创建物理卷 pvscan 查看创建列表信息 pvremove 
pvcreate /dev/sdb
pvcreate /dev/sdc

#创建卷组 vgscan查看卷组信息 vgremove
vgcreate DATA_LVM /dev/sdb
vgextend DATA_LVM /dev/sdc

#创建逻辑卷 lvcreate -L[自定义分区大小] -n[自定义分区名称] [vg名称]  删除指令是pvremove DATA_LVM/DATA,  16T只是一般值，具体根据free space为准
lvcreate -L16T -n DATA  DATA_LVM

#格式化分区，默认回车就行
mkfs.ext4 /dev/DATA_LVM/DATA

#挂载
mount /dev/DATA_LVM/DATA /data
```

修改挂载表`vim /etc/fstab`
```shell
/dev/mapper/DATA_LVM-DATA /data                 ext4    defaults        0 0
```

## 备份
```shell
sudo tar -cvpzf backup.tar.gz / \
    --exclude=backup.tar.gz \
    --exclude=/lost+found \
    --exclude=/proc \
    --exclude=/mnt \
    --exclude=/etc/fstab \
    --exclude=/sys \
    --exclude=/dev \
    --exclude=/boot \
    --exclude=/tmp \
    --exclude=/var/cache/apt/archives \
    --exclude=/run \
    --exclude=/root \
    --exclude=/home \
    --warning=no-file-changed 
```

如果不需要备份网卡`--exclude=/etc/sysconfig/network-scripts \`  

> 这里不备份网卡，如需修改网卡名称，可以手动修改。如果备份网卡，需要修改IP地址及网关  
> 如果备份网卡，需要删除原有网卡名称 `rm -fr /etc/sysconfig/network-scripts/ifcfg-enp*;grub2-mkconfig -o /boot/grub2/grub.cfg`  

> 还需要注意防火墙策略: `systemctl stop firewalld.service`

## 还原
```
sudo tar -xvpzf backup.tar.gz -C / \
    --numeric-owner
```

还完完成后需要更新密码，不然会登录不进去(原因未知) :sweat_smile: 
```
# passwd root
更改用户 root 的密码
新的 密码：
重新输入新的 密码：
passwd：所有的身份验证令牌已经成功更新。
```

## 疑问
### 修改Linux系统用户密码
1. 在启动GRUB菜单中选择编辑选项，按键 `e` 进入编辑模式  
2. 找到 `ro` 将 `ro` 修改为 `rw init=/sysroot/bin/bash`  
```shell
rw init=/sysroot/bin/bash
```

3. 按下 `ctrl + x`，进入单用户模式  
4. 用 `chroot /sysroot` 命令进入系统  
5. `passwd root`  重置root密码  
6. `touch /.autorelabel` 更新SELinux信息  
7. 输入 `exit` 退出 `chroot`  
8. 用 `reboot -f` 重启你的系统  


### 网卡名称没有备份成功

重新生成grub文件  
```
# grub2-mkconfig -o /boot/grub2/grub.cfg
Generating grub configuration file ...
Found linux image: /boot/vmlinuz-3.10.0-1160.el7.x86_64
Found initrd image: /boot/initramfs-3.10.0-1160.el7.x86_64.img
Found linux image: /boot/vmlinuz-0-rescue-7ae9c5f396684f1590725c73f8a418ea
Found initrd image: /boot/initramfs-0-rescue-7ae9c5f396684f1590725c73f8a418ea.img
done
```

查看配置文件:`vim /boot/grub2/grub.cfg`
```shell
linux16 /vmlinuz-3.10.0-1160.el7.x86_64 root=/dev/mapper/centos-root ro crashkernel=auto net.ifnames=0 biosdevname=0 rd.lvm.lv=centos/swap rhgb quiet
```

在启动界面，删除`rd.lvm.lv=/dev/mapper/centos-swap`,`ctrl+x`可以正常启动。  

> 提示错误: `/dev/mapper/centos-root dose not exist`  
> 网上的解决办法是在紧急模式输入指令`dracut -f`  

### linux tab提示
```shell
yum install -y bash-completion
```

### 防火墙增加规则
```
systemctl start firewalld.service
firewall-cmd --zone=public --add-port=443/tcp --permanent
firewall-cmd --reload

# 查看规则
firewall-cmd --zone=public --list-ports
```

### 串口设置(centos7)
[csdn参考文章](https://blog.csdn.net/mao2553319/article/details/79496684)  

1. 修改`/etc/default/grub`

追加
```shell
GRUB_TERMINAL="console serial"
GRUB_SERIAL_COMMAND="serial --speed=115200 --unit=0 --word=8 --parity=no --stop=1"
GRUB_CMDLINE_LINUX_DEFAULT="console=tty1 console=ttyS0,115200"
```

2. 更新grub

```shell
grub2-mkconfig -o /boot/grub2/grub.cfg
```

### 系统无法启动  
`dracut`,还有找不到`/dev/mapper/centos-swap`,可能之前`/etc/default/grub`被人修改过  

增加串口后的配置
```shell
GRUB_TIMEOUT=5
GRUB_DISTRIBUTOR="$(sed 's, release .*$,,g' /etc/system-release)"
GRUB_DEFAULT=saved
GRUB_DISABLE_SUBMENU=true
GRUB_TERMINAL_OUTPUT="console"
GRUB_CMDLINE_LINUX="crashkernel=auto spectre_v2=retpoline rd.lvm.lv=centos/root rd.lvm.lv=centos/swap  net.ifnames=0 biosdevname=0 rhgb quiet"
GRUB_CMDLINE_LINUX_DEFAULT="console=tty0 console=ttyS0,115200"
GRUB_DISABLE_RECOVERY="true"
```

重新执行`grub2-mkconfig -o /boot/grub2/grub.cfg`  
```shell
Generating grub configuration file ...
Found linux image: /boot/vmlinuz-3.10.0-1160.el7.x86_64
Found initrd image: /boot/initramfs-3.10.0-1160.el7.x86_64.img
Found linux image: /boot/vmlinuz-0-rescue-a8ffef200b50493c86e299323d4235df
Found initrd image: /boot/initramfs-0-rescue-a8ffef200b50493c86e299323d4235df.img
done
```


> 无法启动后，编辑启动参数  

<div align=center>
<img src="../resources/images/centos-boot-edit.png" width="60%"></img>
</div>

### 双盘挂载  

```sh
# 停止所有服务
systemctl stop mysql
supervisorctl stop all

mv /data/ /data_bak

mkdir /data

#格式化分区，默认回车就行
mkfs.ext4 /dev/sdb

#挂载   卸载 unmount /data
mount /dev/sdb /data

# 拷贝数据
cp -frp /data_bak/* /data/

# 修改fstab
/dev/sdb /data                 ext4    defaults        0 0

# 重启系统
reboot  
```