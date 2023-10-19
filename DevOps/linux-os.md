- # Linux 系统知识

- ## 目录
- [基础知识](#基础知识)
  - [逻辑卷](#逻辑卷)
  - [分区](#分区)
  - [内核日志](#内核日志)
  - [防火墙](#防火墙)
  - [网络报文异常](#网络报文异常)
    - [ICMP](#icmp)
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

### 内核日志 

> dmesg的日志来源  
`dmesg` 是一个用于检查内核`环形缓冲区`的消息的命令。这些消息来自Linux内核本身，并提供了与硬件、设备驱动、以及其他内核子系统交互的关键信息。  
>当您启动计算机或连接新的硬件设备时，内核将与硬件进行交互，并在环形缓冲区中生成日志消息。这些消息为管理员提供了关于硬件状态、驱动程序加载、硬件识别、错误、警告等的重要信息。  
> 简而言之，`dmesg` 提供的日志直接来自Linux内核，是关于系统硬件和内核子系统状态的低级消息。这与`/var/log/syslog`或`/var/log/messages`中的日志略有不同，后者可能包含来自各种系统和应用程序的更广泛的消息，而不仅仅是内核消息。


```sh
dmesg --help

Usage:
 dmesg [options]

Display or control the kernel ring buffer.

Options:
 -C, --clear                 clear the kernel ring buffer
 -c, --read-clear            read and clear all messages
 -D, --console-off           disable printing messages to console
 -E, --console-on            enable printing messages to console
 -F, --file <file>           use the file instead of the kernel log buffer
 -f, --facility <list>       restrict output to defined facilities
 -H, --human                 human readable output
 -k, --kernel                display kernel messages
 -L, --color[=<when>]        colorize messages (auto, always or never)
                               colors are enabled by default
 -l, --level <list>          restrict output to defined levels
 -n, --console-level <level> set level of messages printed to console
 -P, --nopager               do not pipe output into a pager
 -p, --force-prefix          force timestamp output on each line of multi-line messages
 -r, --raw                   print the raw message buffer
 -S, --syslog                force to use syslog(2) rather than /dev/kmsg
 -s, --buffer-size <size>    buffer size to query the kernel ring buffer
 -u, --userspace             display userspace messages
 -w, --follow                wait for new messages
 -x, --decode                decode facility and level to readable string
 -d, --show-delta            show time delta between printed messages
 -e, --reltime               show local time and time delta in readable format
 -T, --ctime                 show human-readable timestamp (may be inaccurate!)
 -t, --notime                don't show any timestamp with messages
     --time-format <format>  show timestamp using the given format:
                               [delta|reltime|ctime|notime|iso]
Suspending/resume will make ctime and iso timestamps inaccurate.

 -h, --help                  display this help
 -V, --version               display version

Supported log facilities:
    kern - kernel messages
    user - random user-level messages
    mail - mail system
  daemon - system daemons
    auth - security/authorization messages
  syslog - messages generated internally by syslogd
     lpr - line printer subsystem
    news - network news subsystem

Supported log levels (priorities):
   emerg - system is unusable
   alert - action must be taken immediately
    crit - critical conditions
     err - error conditions
    warn - warning conditions
  notice - normal but significant condition
    info - informational
   debug - debug-level messages

For more details see dmesg(1).
```

- ### `/var/log/syslog`
```sh
# 杀死进程
kill -9 1083

# 查看日志
tail -f /var/log/syslog
Oct 17 19:44:32 netvine supervisord[862]: 2023-10-17 19:44:32,406 INFO exited: superv-server (terminated by SIGKILL; not expected)
Oct 17 19:44:33 netvine supervisord[862]: 2023-10-17 19:44:33,416 INFO spawned: 'superv-server' with pid 78760
```

- ### `dmesg`  

```sh
# 查看最后
dmesg | tail

[94333.895551] systemd-journald[365]: /var/log/journal/c90819070a134b8387a5897b86411be1/system.journal: Journal file has been deleted, rotating.
[94333.895894] systemd-journald[365]: Failed to create new system journal: No such file or directory
[96133.643234] systemd-journald[365]: /var/log/journal/c90819070a134b8387a5897b86411be1/system.journal: Journal file has been deleted, rotating.
[96133.643551] systemd-journald[365]: Failed to create new system journal: No such file or directory
[104904.597918] systemd-sysv-generator[79153]: Overwriting existing symlink /run/systemd/generator.late/init_config.service with real service.
[104905.592310] systemd-sysv-generator[79231]: Overwriting existing symlink /run/systemd/generator.late/init_config.service with real service.

# 持续输出

```

### 防火墙  

查看防火墙已有规则:
```sh
# firewall-cmd --list-all
public (active)
  target: default
  icmp-block-inversion: no
  interfaces: eth0 eth1
  sources: 
  services: dhcpv6-client ssh
  ports: 5011/tcp 443/tcp 514/tcp 514/udp 8412/tcp 80/tcp
  protocols: 
  masquerade: no
  forward-ports: 
  source-ports: 
  icmp-blocks: 
  rich rules: 

iptables -nL
Chain INPUT (policy ACCEPT)
target     prot opt source               destination         
ACCEPT     udp  --  0.0.0.0/0            0.0.0.0/0            udp dpt:53
ACCEPT     tcp  --  0.0.0.0/0            0.0.0.0/0            tcp dpt:53
ACCEPT     udp  --  0.0.0.0/0            0.0.0.0/0            udp dpt:67
ACCEPT     tcp  --  0.0.0.0/0            0.0.0.0/0            tcp dpt:67
ACCEPT     all  --  0.0.0.0/0            0.0.0.0/0            ctstate RELATED,ESTABLISHED
ACCEPT     all  --  0.0.0.0/0            0.0.0.0/0           
INPUT_direct  all  --  0.0.0.0/0            0.0.0.0/0           
INPUT_ZONES_SOURCE  all  --  0.0.0.0/0            0.0.0.0/0           
INPUT_ZONES  all  --  0.0.0.0/0            0.0.0.0/0           
DROP       all  --  0.0.0.0/0            0.0.0.0/0            ctstate INVALID
REJECT     all  --  0.0.0.0/0            0.0.0.0/0            reject-with icmp-host-prohibited

Chain FORWARD (policy ACCEPT)
target     prot opt source               destination         
ACCEPT     all  --  0.0.0.0/0            192.168.122.0/24     ctstate RELATED,ESTABLISHED
ACCEPT     all  --  192.168.122.0/24     0.0.0.0/0           
ACCEPT     all  --  0.0.0.0/0            0.0.0.0/0           
REJECT     all  --  0.0.0.0/0            0.0.0.0/0            reject-with icmp-port-unreachable
REJECT     all  --  0.0.0.0/0            0.0.0.0/0            reject-with icmp-port-unreachable
ACCEPT     all  --  0.0.0.0/0            0.0.0.0/0            ctstate RELATED,ESTABLISHED
ACCEPT     all  --  0.0.0.0/0            0.0.0.0/0           
FORWARD_direct  all  --  0.0.0.0/0            0.0.0.0/0           
FORWARD_IN_ZONES_SOURCE  all  --  0.0.0.0/0            0.0.0.0/0           
FORWARD_IN_ZONES  all  --  0.0.0.0/0            0.0.0.0/0           
FORWARD_OUT_ZONES_SOURCE  all  --  0.0.0.0/0            0.0.0.0/0           
FORWARD_OUT_ZONES  all  --  0.0.0.0/0            0.0.0.0/0           
DROP       all  --  0.0.0.0/0            0.0.0.0/0            ctstate INVALID
REJECT     all  --  0.0.0.0/0            0.0.0.0/0            reject-with icmp-host-prohibited

Chain OUTPUT (policy ACCEPT)
target     prot opt source               destination         
ACCEPT     udp  --  0.0.0.0/0            0.0.0.0/0            udp dpt:68
ACCEPT     all  --  0.0.0.0/0            0.0.0.0/0           
OUTPUT_direct  all  --  0.0.0.0/0            0.0.0.0/0           

Chain FORWARD_IN_ZONES (1 references)
target     prot opt source               destination         
FWDI_public  all  --  0.0.0.0/0            0.0.0.0/0           [goto] 
FWDI_public  all  --  0.0.0.0/0            0.0.0.0/0           [goto] 
FWDI_public  all  --  0.0.0.0/0            0.0.0.0/0           [goto] 

Chain FORWARD_IN_ZONES_SOURCE (1 references)
target     prot opt source               destination         

Chain IN_public_allow (1 references)
target     prot opt source               destination         
ACCEPT     tcp  --  0.0.0.0/0            0.0.0.0/0            tcp dpt:22 ctstate NEW,UNTRACKED
ACCEPT     tcp  --  0.0.0.0/0            0.0.0.0/0            tcp dpt:5011 ctstate NEW,UNTRACKED
ACCEPT     tcp  --  0.0.0.0/0            0.0.0.0/0            tcp dpt:443 ctstate NEW,UNTRACKED
ACCEPT     tcp  --  0.0.0.0/0            0.0.0.0/0            tcp dpt:514 ctstate NEW,UNTRACKED
ACCEPT     udp  --  0.0.0.0/0            0.0.0.0/0            udp dpt:514 ctstate NEW,UNTRACKED
ACCEPT     tcp  --  0.0.0.0/0            0.0.0.0/0            tcp dpt:8412 ctstate NEW,UNTRACKED
ACCEPT     tcp  --  0.0.0.0/0            0.0.0.0/0            tcp dpt:80 ctstate NEW,UNTRACKED
```

### 网络报文异常  

`[TCP ACKed unseen segment]`  

<br>
<div align=center>
<img src="../resources/images/devops/tcp%E6%8A%A5%E6%96%87%E5%BC%82%E5%B8%B81.png" width="80%"></img>  
</div>
<br>

> tcpdump 抓取的是网卡的数据，并非内核协议栈的数据。   

`tcpdump` 抓取的是网卡的数据。具体来说，`tcpdump` 依赖于一个库叫做 `libpcap`（在 Windows 上是 `WinPcap` 或 `Npcap`），该库提供了一个系统独立的接口，用于捕获网络交通。

当你使用 `tcpdump` 时，它会将相关的网络接口置于`混杂模式`（promiscuous mode）。在混杂模式下，网卡会捕获通过它的所有数据包，而不仅仅是那些目标地址与网卡硬件地址匹配的数据包。这允许你捕获到所有的流经该接口的数据包。

这些数据包是从数据链路层（例如，以太网）捕获的，所以你可以看到从 MAC 地址到上层协议（如 IP、TCP、UDP 等）的所有信息。


然而，请注意，有些操作系统或驱动可能不完全支持混杂模式，或者在虚拟化环境中的行为可能与在物理硬件上的行为略有不同。


> 现在网络连接不同或者被拒绝时，可能影响因素有`限速`和`精准匹配丢弃`  

#### ICMP 

现在有两台设备，`PC1`与`PC2`, 现在`PC1`能够ping通`PC2`, 但是`PC2`无法ping通`PC1`, 通过tcpdump 抓包，查看`PC2`的ICMP包是正常的，有request和reply，但是Ping应用无法收到ICMP的回包，因为被ICMP网络攻击规则拦住了，只到达了网卡，没有到达应用层。  

> icmp type echo-request limit rate over 200/second burst 500 packets log prefix "icmpFlood_nftables" drop  

正常情况，应该怀疑`PC2`的防火墙拦截了ICMP的请求。    



## 疑问拓展
### 系统依赖库的优先级  

