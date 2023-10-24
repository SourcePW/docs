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

### nmap 

目前无法连接设备的mysql，通过nmap扫描设备端口
扫描设备的端口:  
```sh
Starting Nmap 7.94 ( https://nmap.org ) at 2023-10-20 15:44 CST
Nmap scan report for gsud2 (10.25.17.233)
Host is up (0.0049s latency).
Not shown: 996 closed tcp ports (conn-refused)
PORT     STATE SERVICE
22/tcp   open  ssh
161/tcp  open  snmp
443/tcp  open  https
1443/tcp open  ies-lm
```

确实无法查看到3306，但是本地可以连接mysql，那就可能是mysql没有开放远程连接接口。
> 通过wireshark抓包发现，远程连接都是连接拒绝状态  
> 167	17.241441	10.25.17.233	10.25.17.114	TCP	54	3306 → 54339 [RST, ACK] Seq=1 Ack=1 Win=0 Len=0  

```sh
netstat -ano | grep -i 3306
tcp        0      0 127.0.0.1:3306          0.0.0.0:*               LISTEN      off (0.00/0/0)
```

查看mysql配置
```sh
 grep "address" /etc/mysql/ -rHn
/etc/mysql/mysql.conf.d/mysqld.cnf:35:bind-address      = 127.0.0.1
/etc/mysql/conf.d/mysqld.cnf:39:#bind-address   = 127.0.0.1
```

修改之后的状态
```sh
 netstat -anp | grep -i 3306
tcp        0      0 0.0.0.0:3306            0.0.0.0:*               LISTEN      76852/mysqld  
```

`nmap`（Network Mapper）是一款强大的网络扫描和安全审计工具。以下是`nmap`的一些常用操作指令：

1. **基本扫描**:
   ```bash
   nmap <target>
   ```

2. **指定端口扫描**:
   ```bash
   nmap -p 22,80,443 <target>
   ```

3. **端口范围扫描**:
   ```bash
   nmap -p 20-100 <target>
   ```

4. **快速扫描**（只检测目标是否在线，不进行端口扫描）:
   ```bash
   nmap -sn <target>
   ```

5. **全端口扫描**:
   ```bash
   nmap -p- <target>
   ```

6. **服务和版本检测**:
   ```bash
   nmap -sV <target>
   ```

7. **操作系统检测**:
   ```bash
   nmap -O <target>
   ```

8. **使用默认脚本扫描**:
   ```bash
   nmap -sC <target>
   ```

9. **组合扫描**（例如：同时进行版本检测和默认脚本扫描）:
   ```bash
   nmap -sV -sC <target>
   ```

10. **使用特定的Nmap脚本扫描**:
   ```bash
   nmap --script=<script-name> <target>
   ```

11. **扫描多个目标**:
   ```bash
   nmap <target1> <target2> <target3>
   ```

12. **从文件中读取目标列表**:
   ```bash
   nmap -iL <filename>
   ```

13. **在输出中增加详细性**:
   ```bash
   nmap -v <target>
   ```

14. **保存扫描结果**（多种格式）:
   ```bash
   nmap -oN output.txt <target>       # 保存为常规格式
   nmap -oX output.xml <target>       # 保存为XML格式
   nmap -oG output.greppable <target> # 保存为Grep可用格式
   ```

15. **避免主机发现，直接扫描**:
   ```bash
   nmap -Pn <target>
   ```

16. **指定使用的扫描技术**（例如：SYN扫描、UDP扫描等）:
   ```bash
   nmap -sS <target>  # SYN扫描
   nmap -sU <target>  # UDP扫描
   ```

这只是`nmap`功能的冰山一角。根据需要，您可能还需要查阅`nmap`的手册或在线文档来获取更多高级和特定的使用情况。

### netstat  

使用手册:
```sh
$ netstat -h
usage: netstat [-vWeenNcCF] [<Af>] -r         netstat {-V|--version|-h|--help}
       netstat [-vWnNcaeol] [<Socket> ...]
       netstat { [-vWeenNac] -i | [-cnNe] -M | -s [-6tuw] }

        -r, --route              display routing table
        -i, --interfaces         display interface table
        -g, --groups             display multicast group memberships
        -s, --statistics         display networking statistics (like SNMP)
        -M, --masquerade         display masqueraded connections

        -v, --verbose            be verbose
        -W, --wide               don't truncate IP addresses
        -n, --numeric            don't resolve names
        --numeric-hosts          don't resolve host names
        --numeric-ports          don't resolve port names
        --numeric-users          don't resolve user names
        -N, --symbolic           resolve hardware names
        -e, --extend             display other/more information
        -p, --programs           display PID/Program name for sockets
        -o, --timers             display timers
        -c, --continuous         continuous listing

        -l, --listening          display listening server sockets
        -a, --all                display all sockets (default: connected)
        -F, --fib                display Forwarding Information Base (default)
        -C, --cache              display routing cache instead of FIB
        -Z, --context            display SELinux security context for sockets

  <Socket>={-t|--tcp} {-u|--udp} {-U|--udplite} {-S|--sctp} {-w|--raw}
           {-x|--unix} --ax25 --ipx --netrom
  <AF>=Use '-6|-4' or '-A <af>' or '--<af>'; default: inet
  List of possible address families (which support routing):
    inet (DARPA Internet) inet6 (IPv6) ax25 (AMPR AX.25) 
    netrom (AMPR NET/ROM) ipx (Novell IPX) ddp (Appletalk DDP) 
    x25 (CCITT X.25) 
```

```sh
netstat -a
Active Internet connections (servers and established)
Proto Recv-Q Send-Q Local Address           Foreign Address         State      
tcp        0      0 0.0.0.0:snmp            0.0.0.0:*               LISTEN     
tcp        0      0 localhost:mysql         0.0.0.0:*               LISTEN     
tcp        0      0 0.0.0.0:6379            0.0.0.0:*               LISTEN     
tcp        0      0 localhost:domain        0.0.0.0:*               LISTEN     
tcp        0      0 0.0.0.0:ssh             0.0.0.0:*               LISTEN     
tcp        0      0 0.0.0.0:https           0.0.0.0:*               LISTEN     
tcp        0      0 netvine:ssh             10.25.17.114:54300      ESTABLISHED
tcp        0    192 netvine:ssh             10.25.17.114:62214      ESTABLISHED
tcp6       0      0 [::]:1443               [::]:*                  LISTEN     
tcp6       0      0 [::]:ssh                [::]:*                  LISTEN     
udp        0      0 localhost:domain        0.0.0.0:*                          
udp        0      0 netvine:ntp             0.0.0.0:*                          
udp        0      0 localhost:ntp           0.0.0.0:*                          
udp        0      0 0.0.0.0:ntp             0.0.0.0:*                          
udp        0      0 0.0.0.0:snmp            0.0.0.0:*   
```

显示应用名称
```sh
netstat -anp | grep -i mysql
tcp        0      0 127.0.0.1:3306          0.0.0.0:*               LISTEN      1085/mysqld         
unix  2      [ ACC ]     STREAM     LISTENING     40465    1085/mysqld          /var/run/mysqld/mysqld.sock
```

路由表
```sh
netstat -nr
Kernel IP routing table
Destination     Gateway         Genmask         Flags   MSS Window  irtt Iface
0.0.0.0         10.25.17.1      0.0.0.0         UG        0 0          0 ens33
10.25.17.0      0.0.0.0         255.255.255.0   U         0 0          0 ens33
```

### socket 

```sh
usage: nc [-46CDdFhklNnrStUuvZz] [-I length] [-i interval] [-M ttl]
          [-m minttl] [-O length] [-P proxy_username] [-p source_port]
          [-q seconds] [-s source] [-T keyword] [-V rtable] [-W recvlimit] [-w timeout]
          [-X proxy_protocol] [-x proxy_address[:port]]           [destination] [port]
```

shell发送socket数据
```sh
echo "hello" | nc -U /tmp/my_socket
```

shell接收socket数据
```sh
nc -lU /tmp/my_socket
```


### sar `System Activity Reporter`
`sar` 是 `System Activity Reporter` 的缩写，它是 sysstat 软件包中的一个工具。sysstat 软件包包含了一组可以用来监控系统性能和进行故障诊断的实用程序。`sar` 提供了对系统的多方面的监控，包括 CPU、内存、I/O、网络、进程和更多的相关信息。

**历史**：
`sar` 已经有很长的历史了。它最初是为 Unix 系统设计的，随着时间的推移，它也被引入到了 Linux 和其他类 Unix 系统中。

**主要作用**：
1. **性能监控**：`sar` 可以帮助系统管理员识别系统的性能瓶颈。
2. **历史数据收集**：`sar` 可以定期收集和存储系统的性能数据，使管理员能够查看历史性能数据。
3. **故障诊断**：当系统出现性能问题或其他故障时，`sar` 提供的数据可以帮助定位问题的原因。

**常用指令**：
1. **CPU 使用率**：`sar -u [interval]`
2. **内存使用率**：`sar -r [interval]`
3. **I/O 使用率**：`sar -b [interval]`
4. **网络使用情况**：`sar -n DEV [interval]`
5. **查看历史数据**：`sar -[option] -f /var/log/sysstat/sa[day]` 
   例如，查看前一天的 CPU 使用情况，可以使用 `sar -u -f /var/log/sysstat/sa$(date +%d -d yesterday)`
6. **块设备 I/O**：`sar -d [interval]`
7. **运行队列和系统负载**：`sar -q [interval]`
8. **上下文切换**：`sar -w [interval]`

其中，`[interval]` 是指更新频率，以秒为单位。例如，`sar -u 5` 将每5秒提供一次CPU使用情况的更新。
为了使 `sar` 正常工作并收集历史数据，您需要确保 `sysstat` 服务正在运行，并且已配置为定期收集数据。


`sar -n DEV 1`
```sh
07:01:11 PM     IFACE   rxpck/s   txpck/s    rxkB/s    txkB/s   rxcmp/s   txcmp/s  rxmcst/s   %ifutil
07:01:12 PM    enp1s0      4.00      1.00      0.24      0.19      0.00      0.00      0.00      0.00
07:01:12 PM    enp4s0      0.00      0.00      0.00      0.00      0.00      0.00      0.00      0.00
07:01:12 PM    enp2s0      0.00      0.00      0.00      0.00      0.00      0.00      0.00      0.00
07:01:12 PM    enp5s0      0.00      0.00      0.00      0.00      0.00      0.00      0.00      0.00
07:01:12 PM        lo      0.00      0.00      0.00      0.00      0.00      0.00      0.00      0.00
07:01:12 PM    enp3s0      0.00      0.00      0.00      0.00      0.00      0.00      0.00      0.00
07:01:12 PM    enp6s0      0.00      0.00      0.00      0.00      0.00      0.00      0.00      0.00
```

`sar -u 5 3`  
```sh
Linux 5.4.0-162-generic (netvine)       10/24/2023      _x86_64_        (8 CPU)

07:07:16 PM     CPU     %user     %nice   %system   %iowait    %steal     %idle
07:07:21 PM     all      0.05      0.00      0.03      0.03      0.00     99.90
07:07:26 PM     all      0.20      0.00      0.03      0.00      0.00     99.77
07:07:31 PM     all      0.03      0.00      0.00      0.00      0.00     99.97
Average:        all      0.09      0.00      0.02      0.01      0.00     99.88
```

### lsof  `list open files`
`lsof` 是 `list open files` 的缩写。在 Unix 和 Unix-like 系统中，几乎所有的事物都被视为文件（例如：文件、目录、网络套接字等），因此 `lsof` 是一个非常强大的工具，用于列出由进程打开的文件。

**历史**:
- `lsof` 最初是在 1987 年由 Victor A. Abell 为 UNIX 系统开发的。
- 自那时以来，`lsof` 已经被移植到许多 Unix 和 Unix-like 系统上，包括 AIX、FreeBSD、Linux、Solaris 等。
  
**主要作用**:
1. **识别打开文件**：列出系统上所有打开的文件。
2. **网络诊断**：列出所有打开的网络套接字。
3. **进程监视**：识别特定进程打开的文件。
4. **文件系统诊断**：确定哪些进程正在使用某个特定的文件或目录。
5. **解决 "Resource busy" 问题**：当尝试卸载文件系统或设备时，如果它被进程使用，可以用 `lsof` 确定哪些进程正在使用它。

**常用指令**:

1. **列出所有打开的文件**：`lsof`
2. **列出特定用户打开的文件**：`lsof -u username`
3. **列出特定进程打开的文件**：`lsof -p PID`，其中 PID 是进程ID。
4. **查找谁在使用特定端口**：`lsof -i :port`
5. **查看所有网络连接**：`lsof -i`
6. **查找打开特定文件的进程**：`lsof /path/to/file`
7. **列出指定文件系统或目录中打开的文件**：`lsof +D /path/to/directory`

这只是 `lsof` 能做的事情的冰山一角。由于其强大的功能和灵活性，`lsof` 已成为系统管理员日常工具箱中的一个重要工具。

```sh
$ lsof -p 1023
COMMAND    PID USER   FD      TYPE    DEVICE SIZE/OFF    NODE NAME
redis-ser 1023 root  cwd       DIR       8,2     4096 1835415 /var/lib/redis
redis-ser 1023 root  rtd       DIR       8,2     4096       2 /
redis-ser 1023 root  txt       REG       8,2  1029680 7350314 /usr/bin/redis-check-rdb
redis-ser 1023 root  mem       REG       8,2  3035952 7352049 /usr/lib/locale/locale-archive
redis-ser 1023 root  mem       REG       8,2   104984 7346287 /usr/lib/x86_64-linux-gnu/libgcc_s.so.1
redis-ser 1023 root  mem       REG       8,2  1956992 7346451 /usr/lib/x86_64-linux-gnu/libstdc++.so.6.0.28
redis-ser 1023 root  mem       REG       8,2  2029592 7346240 /usr/lib/x86_64-linux-gnu/libc-2.31.so
redis-ser 1023 root  mem       REG       8,2   157224 7346424 /usr/lib/x86_64-linux-gnu/libpthread-2.31.so
redis-ser 1023 root  mem       REG       8,2    71808 7351805 /usr/lib/x86_64-linux-gnu/libhiredis.so.0.14
redis-ser 1023 root  mem       REG       8,2    35960 7346431 /usr/lib/x86_64-linux-gnu/librt-2.31.so
redis-ser 1023 root  mem       REG       8,2  1369384 7346355 /usr/lib/x86_64-linux-gnu/libm-2.31.so
redis-ser 1023 root  mem       REG       8,2   744776 7351896 /usr/lib/x86_64-linux-gnu/libjemalloc.so.2
redis-ser 1023 root  mem       REG       8,2   196384 7351927 /usr/lib/x86_64-linux-gnu/liblua5.1.so.0.0.0
redis-ser 1023 root  mem       REG       8,2    10328 7351781 /usr/lib/x86_64-linux-gnu/liblua5.1-bitop.so.0.0.0
redis-ser 1023 root  mem       REG       8,2    31240 7352008 /usr/lib/x86_64-linux-gnu/liblua5.1-cjson.so.0.0.0
redis-ser 1023 root  mem       REG       8,2    30968 7351821 /usr/lib/x86_64-linux-gnu/libatomic.so.1.2.0
redis-ser 1023 root  mem       REG       8,2    18848 7346258 /usr/lib/x86_64-linux-gnu/libdl-2.31.so
redis-ser 1023 root  mem       REG       8,2   191504 7346199 /usr/lib/x86_64-linux-gnu/ld-2.31.so
redis-ser 1023 root    0r     FIFO      0,13      0t0   25191 pipe
redis-ser 1023 root    1w     FIFO      0,13      0t0   25192 pipe
redis-ser 1023 root    2w     FIFO      0,13      0t0   25192 pipe
redis-ser 1023 root    3r     FIFO      0,13      0t0   27200 pipe
redis-ser 1023 root    4w     FIFO      0,13      0t0   27200 pipe
redis-ser 1023 root    5u  a_inode      0,14        0   10331 [eventpoll]
redis-ser 1023 root    6u     IPv4     29269      0t0     TCP *:6379 (LISTEN)
```

### ps `process status`
`ps` 是一个命令行工具，用于在 UNIX-like 系统中提供关于正在运行的进程的信息。"ps" 的全称是 "process status"，即进程状态。

**历史**:
- `ps` 命令起源于早期版本的 UNIX，这一版本在 20 世纪 70 年代开发。
- 在 UNIX 的演进过程中，出现了各种版本和风格的 `ps` 命令，每种命令都针对每个 UNIX 变种的特点进行了定制。
- 当 POSIX 标准开始定义命令的预期行为时，已经有两种主要的 `ps` 风格：BSD 风格和 System V 风格。

**主要作用**:
- 显示系统中正在运行的进程的信息。
- 可以查看进程的 PID（进程ID）、TTY（终端类型）、时间、命令名称等信息。

**常用指令及执行结果展示**:
1. **基本用法**: `ps`
   ```
   PID TTY          TIME CMD
   1234 pts/1    00:00:00 bash
   5678 pts/1    00:00:00 ps
   ```

2. **显示所有进程**: `ps -e` 或 `ps aux` (BSD 风格)
   ```
   USER       PID  %CPU %MEM    VSZ   RSS TTY      STAT START   TIME COMMAND
   root         1  0.0  0.1  16148  5236 ?        Ss   Jan01   0:06 /usr/lib/systemd/systemd
   root         2  0.0  0.0      0     0 ?        S    Jan01   0:00 [kthreadd]
   ...
   ```

3. **显示特定用户的进程**: `ps -u [用户名]`
   ```
   PID TTY          TIME CMD
   1234 pts/1    00:00:00 bash
   5678 pts/1    00:00:00 ps
   ```

4. **按进程名筛选**: `ps -C [命令名]`
   ```
   PID TTY          TIME CMD
   1234 pts/1    00:00:00 bash
   ```

这只是 `ps` 命令的一些基本用法，它还有许多其他的选项和功能。你可以使用 `man ps` 来查看更多的详细信息和用法。

## MACOS
### 查看文件内容
```sh
find . -type f -name '**.log*'  -exec grep "error" {} + > output.txt   
```



## 疑问拓展
### 系统依赖库的优先级  

