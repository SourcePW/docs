# Linux 系统备份与还原
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
    --warning=no-file-changed 
```



## 还原
```
sudo tar -xvpzf backup.tar.gz -C / \
    --numeric-owner
```

更新密码:
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
linux16 /vmlinuz-3.10.0-1160.el7.x86_64 root=/dev/mapper/centos-root ro crashkernel=auto net.ifnames=0 biosdevname=0 rd.lvm.lv=/dev/mapper/centos-swap rhgb quiet
```

在启动界面，删除`rd.lvm.lv=/dev/mapper/centos-swap`,`ctrl+x`可以正常启动。  

### linux tab提示
```shell
yum install -y bash-completion
```

