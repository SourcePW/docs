- # Linux 系统知识

- ## 目录
- [基础知识](#基础知识)
  - [逻辑卷](#逻辑卷)
  - [分区](#分区)
- [疑问拓展](#疑问拓展)
  - [系统依赖库的优先级](#系统依赖库的优先级)


## 基础知识
### 逻辑卷  
ESXI创建一个虚拟机，使用600G硬盘空间，创建完成之后，发现只有100G在根目录  
```sh
Filesystem                         Size  Used Avail Use% Mounted on
tmpfs                              593M  1.3M  591M   1% /run
/dev/mapper/ubuntu--vg-ubuntu--lv   98G  7.4G   86G   8% /
tmpfs                              2.9G     0  2.9G   0% /dev/shm
tmpfs                              5.0M     0  5.0M   0% /run/lock
/dev/sda2                          2.0G  129M  1.7G   8% /boot
tmpfs                              593M  4.0K  593M   1% /run/user/0

# 查看总大小
Disk /dev/sda: 600 GiB, 644245094400 bytes, 1258291200 sectors
Disk model: Virtual disk    
Units: sectors of 1 * 512 = 512 bytes
Sector size (logical/physical): 512 bytes / 512 bytes
I/O size (minimum/optimal): 512 bytes / 512 bytes
Disklabel type: gpt
Disk identifier: 53A777EA-B49E-4F45-BFDA-AF915D6F72C3

Device       Start        End    Sectors  Size Type
/dev/sda1     2048       4095       2048    1M BIOS boot
/dev/sda2     4096    4198399    4194304    2G Linux filesystem
/dev/sda3  4198400 1258289151 1254090752  598G Linux filesystem
```

使用挂载命令失败:`mount -t exf4 /dev/sda3 /data`  
> 查看具体错误: ` mount -v /dev/sda3 /data `, mount: /data: unknown filesystem type 'LVM2_member'.  

查看分区:
```sh
root@netvine:~# lsblk 
NAME                      MAJ:MIN RM   SIZE RO TYPE MOUNTPOINTS
sda                         8:0    0   600G  0 disk 
├─sda1                      8:1    0     1M  0 part 
├─sda2                      8:2    0     2G  0 part /boot
└─sda3                      8:3    0   598G  0 part 
  └─ubuntu--vg-ubuntu--lv 253:0    0   100G  0 lvm  /
```

发现sda3下有一个逻辑分区，挂载到根目录

显示物理卷信息:
```sh
pvdisplay

  PV Name               /dev/sda3
  VG Name               ubuntu-vg
  PV Size               <598.00 GiB / not usable 0   
  Allocatable           yes 
  PE Size               4.00 MiB
  Total PE              153087
  Free PE               127487
  Allocated PE          25600
  PV UUID               Me96g0-dbJO-CgHj-286n-Ljx6-I5v5-kqQDRj
```

显示卷组信息：
```sh
vgdisplay

  --- Volume group ---
  VG Name               ubuntu-vg
  System ID             
  Format                lvm2
  Metadata Areas        1
  Metadata Sequence No  2
  VG Access             read/write
  VG Status             resizable
  MAX LV                0
  Cur LV                1
  Open LV               1
  Max PV                0
  Cur PV                1
  Act PV                1
  VG Size               <598.00 GiB
  PE Size               4.00 MiB
  Total PE              153087
  Alloc PE / Size       25600 / 100.00 GiB
  Free  PE / Size       127487 / <498.00 GiB
  VG UUID               MAaKfA-0zsN-MMML-Twsc-wKrM-WjvV-2J39JS
```
查看可以空间498G  

显示逻辑卷信息
```sh
lvdisplay

  --- Logical volume ---
  LV Path                /dev/ubuntu-vg/ubuntu-lv
  LV Name                ubuntu-lv
  VG Name                ubuntu-vg
  LV UUID                cyG8F6-E2X2-IszI-0tic-5cCF-f7Py-iFeaxw
  LV Write Access        read/write
  LV Creation host, time ubuntu-server, 2023-10-08 10:14:50 +0000
  LV Status              available
  # open                 1
  LV Size                100.00 GiB
  Current LE             25600
  Segments               1
  Allocation             inherit
  Read ahead sectors     auto
  - currently set to     256
  Block device           253:0
```

```sh
ls -l /dev/ubuntu-vg/
total 0
lrwxrwxrwx 1 root root 7 Oct 12 11:26 ubuntu-lv -> ../dm-0
```

如何使用剩余空间呢?

```sh
# 新建一个逻辑卷:
sudo lvcreate -L 498G -n data_lv ubuntu-vg
>  Logical volume "data_lv" created.
# 格式化
sudo mkfs.ext4 /dev/ubuntu-vg/data_lv

# 手动挂载
sudo mount /dev/ubuntu-vg/data_lv /data

# 自动挂载 /etc/fstab
/dev/ubuntu-vg/data_lv /data ext4 defaults 0 0
```


在 `/etc/fstab` 文件中，每一行代表一个文件系统或设备的挂载信息，每一行又分为六个字段。`defaults 0 2` 代表了其中的三个字段。我们一一来解释：

1. **挂载选项 (`defaults`)**:
   - `defaults` 是挂载选项，它是以下几个选项的组合：`rw`, `suid`, `dev`, `exec`, `auto`, `nouser`, 和 `async`。
     - `rw`: 允许读写。
     - `suid`: 允许运行 suid 和 sgid 位设置的程序。
     - `dev`: 解释块特殊设备或字符特殊设备上的字符和块设备文件。
     - `exec`: 允许执行程序。
     - `auto`: 启动时自动挂载。
     - `nouser`: 只允许 root 挂载设备。
     - `async`: 默认使用异步 I/O。

2. **dump 字段 (`0`)**:
   - 这个字段被 `dump` 程序用来确定是否需要备份该文件系统。如果这个字段的值是 `0`，那么 `dump` 程序将不会备份该文件系统。

3. **文件系统检查顺序 (`2`)**:
   - 这是 `fsck` 命令在启动时检查文件系统的顺序。根文件系统（/）应该是 `1`。其他文件系统可以是 `2` 或 `0`。如果设置为 `2`，`fsck` 会检查它，但如果设置为 `0`，`fsck` 不会在启动时检查该文件系统。

总结一下，`defaults 0 2` 的意思是使用默认的挂载选项，不让 `dump` 备份该文件系统，并在启动时检查该文件系统的完整性，但检查的优先级低于根文件系统。


### 分区
第一个给虚拟机分配20G的硬盘空间，后面想扩容，有增加500G，但是不知如何使用
```sh
 lsblk 
NAME                      MAJ:MIN RM  SIZE RO TYPE MOUNTPOINT
sda                         8:0    0  600G  0 disk 
├─sda1                      8:1    0    1M  0 part 
├─sda2                      8:2    0    2G  0 part /boot
└─sda3                      8:3    0  598G  0 part 
  └─ubuntu--vg-ubuntu--lv 253:0    0  100G  0 lvm  /
sr0                        11:0    1 1024M  0 rom  
```

可以看到多分配的空间都在sda3上

## 疑问拓展
### 系统依赖库的优先级  

