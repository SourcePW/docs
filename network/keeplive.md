- # 双机热备

- [环境搭建](#环境搭建)
  - [配置](#配置)
- [疑问](#疑问)
  - [接口必须配置IP，所以需要选择，如果没有IP，无法配置](#接口必须配置ip所以需要选择如果没有ip无法配置)
  - [Master模式日志](#master模式日志)
  - [虚拟IP与Keepalive的关系](#虚拟ip与keepalive的关系)
  - [master切换为backup](#master切换为backup)
  - [广播、单播、组播](#广播单播组播)


## 环境搭建

### 配置

PC1
```sh
ip 10.10.10.1 24 10.10.10.254
```

PC2
```sh
ip 10.20.20.1 24 10.20.20.254
```

> 第一次配置网关不存在，无法发送给网关，也就无法跨网段访问.  

Uubntu20-1配置
```sh
ifconfig ens38 10.10.10.2 broadcast 10.10.10.255 netmask 255.255.255.0
ifconfig ens39 10.20.20.2 broadcast 10.20.20.255 netmask 255.255.255.0
```

vip
```sh
10.25.10.210
```

安装`keepalived`
```sh
apt-get install keepalived
```

> 高可用性的一个关键点就是外界并不关心有几个fw，主备fw对外呈现的均为同一个`vip`，当主fw故障后，vip会漂移到备fw上。实现这个主备fw之间互相通信的协议就是`vrrp`协议。fw之间需要通过一根心跳线相连，注意该心跳线非常关键，不应出现故障，最稳定的情况是将主备fw的心跳口直连。因为如果心跳口出现任何异常，会导致两端无法通信，从而导致双方均认为对端故障，两台fw都自立为王的情况，这就是常说的双主异常，该异常需要通过手动、另外的管理软件来进行排除。  

查看软件相关信息`dpkg -S keepalived`
```sh
dpkg -S keepalived
keepalived: /etc/init.d/keepalived
keepalived: /etc/dbus-1/system.d/org.keepalived.Vrrp1.conf
keepalived: /etc/default/keepalived
keepalived: /etc/keepalived
```

服务的配置文件:
```sh
[Unit]
Description=Keepalive Daemon (LVS and VRRP)
After=network-online.target
Wants=network-online.target
# Only start if there is a configuration file
ConditionFileNotEmpty=/etc/keepalived/keepalived.conf

[Service]
Type=simple
# Read configuration variable file if it is present
EnvironmentFile=-/etc/default/keepalived
ExecStart=/usr/sbin/keepalived --dont-fork $DAEMON_ARGS
ExecReload=/bin/kill -HUP $MAINPID

[Install]
WantedBy=multi-user.target
```

拷贝配置文件:
```sh
apt install conntrackd

cp /usr/share/doc/conntrackd/examples/sync/primary-backup.sh /etc/conntrackd/
chmod 755 /etc/conntrackd/primary-backup.sh
```

配置文件`/etc/keepalived/keepalived.conf`
```sh
vrrp_sync_group G1 {
    group {
        EXT
        INT
    }
    notify_master "/etc/conntrackd/primary-backup.sh primary"
    notify_backup "/etc/conntrackd/primary-backup.sh backup"
    notify_fault "/etc/conntrackd/primary-backup.sh fault"
}
 
vrrp_instance INT {
    state MASTER
    interface ens38
    virtual_router_id 11
    priority 50
    advert_int 1

    authentication {
        auth_type PASS
        auth_pass 1111
    }
    virtual_ipaddress {
        10.10.10.254/24 dev ens38
    }

    nopreempt
    garp_master_delay 1
}
 
vrrp_instance EXT {
    state MASTER
    interface ens39
    virtual_router_id 22
    priority 50
    advert_int 1

    authentication {
        auth_type PASS
        auth_pass 1111
    }
    virtual_ipaddress {
        10.20.20.254/24 dev ens39
    }
    nopreempt
    garp_master_delay 1
}
```

重启keepalive后的网卡配置
```sh
2: ens33: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc fq_codel state UP group default qlen 1000
    link/ether 00:0c:29:7d:25:73 brd ff:ff:ff:ff:ff:ff
    inet 192.168.214.4/24 brd 192.168.214.255 scope global dynamic ens33
       valid_lft 82885sec preferred_lft 82885sec
    inet6 fe80::20c:29ff:fe7d:2573/64 scope link 
       valid_lft forever preferred_lft forever
3: ens38: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc fq_codel state UP group default qlen 1000
    link/ether 00:0c:29:7d:25:7d brd ff:ff:ff:ff:ff:ff
    inet 10.10.10.2/24 brd 10.10.10.255 scope global ens38
       valid_lft forever preferred_lft forever
    inet 10.10.10.210/24 scope global secondary ens38
       valid_lft forever preferred_lft forever
    inet6 fe80::20c:29ff:fe7d:257d/64 scope link 
       valid_lft forever preferred_lft forever
4: ens39: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc fq_codel state UP group default qlen 1000
    link/ether 00:0c:29:7d:25:87 brd ff:ff:ff:ff:ff:ff
    inet 10.20.20.2/24 brd 10.20.20.255 scope global ens39
       valid_lft forever preferred_lft forever
    inet 10.20.20.211/24 scope global secondary ens39
       valid_lft forever preferred_lft forever
    inet6 fe80::20c:29ff:fe7d:2587/64 scope link 
       valid_lft forever preferred_lft forever
```


通过pc1的arp表可以确定网络连接正确:
```sh
PC1> show arp       

00:0c:29:7d:25:7d  10.10.10.2 expires in 117 seconds 
```

VRRP协议详情:
```sh
Frame 6: 60 bytes on wire (480 bits), 60 bytes captured (480 bits) on interface -, id 0
Ethernet II, Src: VMware_7d:25:7d (00:0c:29:7d:25:7d), Dst: Private_66:68:02 (00:50:79:66:68:02)
Internet Protocol Version 4, Src: 10.10.10.2, Dst: 10.10.10.3
Virtual Router Redundancy Protocol
    Version 2, Packet type 1 (Advertisement)
    Virtual Rtr ID: 11
    Priority: 50 (Non-default backup priority)
    Addr Count: 1
    Auth Type: Simple Text Authentication [RFC 2338] / Reserved [RFC 3768] (1)
    Adver Int: 1
    Checksum: 0x34b4 [correct]
    [Checksum Status: Good]
    IP Address: 10.10.10.210
    Authentication String: 1111
```

组播报文:
```sh

```


启动另一台设备
```sh
ifconfig enp0s8 10.10.10.3 broadcast 10.10.10.255 netmask 255.255.255.0
ifconfig enp0s9 10.20.20.3 broadcast 10.20.20.255 netmask 255.255.255.0
```

配置:
```sh
vrrp_sync_group G1 {
    group {
        EXT
        INT
    }
    notify_master "/etc/conntrackd/primary-backup.sh primary"
    notify_backup "/etc/conntrackd/primary-backup.sh backup"
    notify_fault "/etc/conntrackd/primary-backup.sh fault"
}
 
vrrp_instance INT {
    state BACKUP
    interface enp0s8
    virtual_router_id 11
    priority 25
    advert_int 1

    authentication {
        auth_type PASS
        auth_pass 1111
    }
    virtual_ipaddress {
        10.10.10.254/24 dev enp0s8
    }

    nopreempt
    garp_master_delay 1
}
 
vrrp_instance EXT {
    state BACKUP
    interface enp0s9
    virtual_router_id 22
    priority 25
    advert_int 1

    authentication {
        auth_type PASS
        auth_pass 1111
    }
    virtual_ipaddress {
        10.20.20.254/24 dev enp0s9
    }
    nopreempt
    garp_master_delay 1
}
```

## 疑问
### 接口必须配置IP，所以需要选择，如果没有IP，无法配置
- 配置虚拟IP的接口必须配置IP
```sh
Aug 09 08:34:25 matrix Keepalived_vrrp[1787]: (INT) Warning - nopreempt will not work with initial state MASTER - clearing
Aug 09 08:34:25 matrix Keepalived_vrrp[1787]: (EXT) Warning - nopreempt will not work with initial state MASTER - clearing
Aug 09 08:34:25 matrix Keepalived_vrrp[1787]: (INT): entering FAULT state (interface ens38 down)
Aug 09 08:34:25 matrix Keepalived_vrrp[1787]: (EXT): entering FAULT state (interface ens39 down)
Aug 09 08:34:25 matrix Keepalived_vrrp[1787]: (INT) entering FAULT state (no IPv4 address for interface)
Aug 09 08:34:25 matrix Keepalived_vrrp[1787]: VRRP_Group(G1): Syncing INT to FAULT state
Aug 09 08:34:25 matrix Keepalived_vrrp[1787]: (EXT) entering FAULT state (no IPv4 address for interface)
Aug 09 08:34:25 matrix Keepalived_vrrp[1787]: (INT) entering FAULT state
Aug 09 08:34:25 matrix Keepalived_vrrp[1787]: (EXT) entering FAULT state
Aug 09 08:34:25 matrix Keepalived_vrrp[1787]: Registering gratuitous ARP shared channel
```


### Master模式日志

```sh
Aug 09 08:37:26 matrix Keepalived_vrrp[1994]: Registering Kernel netlink reflector
Aug 09 08:37:26 matrix Keepalived_vrrp[1994]: Registering Kernel netlink command channel
Aug 09 08:37:26 matrix Keepalived_vrrp[1994]: Opening file '/etc/keepalived/keepalived.conf'.
Aug 09 08:37:26 matrix Keepalived_vrrp[1994]: WARNING - default user 'keepalived_script' for script execution does not exist - please >
Aug 09 08:37:26 matrix Keepalived_vrrp[1994]: SECURITY VIOLATION - scripts are being executed but script_security not enabled.
Aug 09 08:37:26 matrix Keepalived_vrrp[1994]: (INT) Warning - nopreempt will not work with initial state MASTER - clearing
Aug 09 08:37:26 matrix Keepalived_vrrp[1994]: (EXT) Warning - nopreempt will not work with initial state MASTER - clearing
Aug 09 08:37:26 matrix Keepalived_vrrp[1994]: Registering gratuitous ARP shared channel
Aug 09 08:37:26 matrix Keepalived_vrrp[1994]: (INT) Entering BACKUP STATE (init)
Aug 09 08:37:26 matrix Keepalived_vrrp[1994]: (EXT) Entering BACKUP STATE (init)
```

过滤状态， 获取最后一次的状态
```sh
root@matrix:~# systemctl  status keepalived.service | grep -E "Entering.*STATE" | tail -n 1
Aug 09 08:37:30 matrix Keepalived_vrrp[1994]: (INT) Entering MASTER STATE
```

`FAULT` 状态
```sh
ug 09 09:28:23 matrix Keepalived_vrrp[3955]: (INT) entering FAULT state
Aug 09 09:28:23 matrix Keepalived_vrrp[3955]: (EXT) entering FAULT state
```

### 虚拟IP与Keepalive的关系

目前发现如果`keepalive`退出了，虚拟ip也就不存在了  

### master切换为backup
```sh
Aug 10 10:09:23 matrix Keepalived_vrrp[1448]: Registering gratuitous ARP shared channel
Aug 10 10:09:23 matrix Keepalived_vrrp[1448]: (INT) Entering BACKUP STATE (init)
Aug 10 10:09:23 matrix Keepalived_vrrp[1448]: (EXT) Entering BACKUP STATE (init)
Aug 10 10:11:20 matrix Keepalived_vrrp[1448]: (EXT) Entering MASTER STATE
Aug 10 10:11:20 matrix Keepalived_vrrp[1448]: VRRP_Group(G1) Syncing instances to MASTER state
Aug 10 10:11:20 matrix Keepalived_vrrp[1448]: (INT) Entering MASTER STATE
Aug 10 10:16:25 matrix Keepalived_vrrp[1448]: (INT) Master received advert from 10.10.10.2 with higher priority 50, ours 25
Aug 10 10:16:25 matrix Keepalived_vrrp[1448]: (INT) Entering BACKUP STATE
Aug 10 10:16:25 matrix Keepalived_vrrp[1448]: VRRP_Group(G1) Syncing instances to BACKUP state
Aug 10 10:16:25 matrix Keepalived_vrrp[1448]: (EXT) Entering BACKUP STATE
```

### 广播、单播、组播
- 单播(`unicast`)：在同一网络内，两个设备点对点的通信就是单播通信。
- 组播(`multicast`)：在同一网络可达范围内，一个网络设备与关心其数据的部分设备进行通信就是组播。二层目标Mac是01-00-5e开头的或者三层目标ip地址是234~239之间的，例如224.5.5.5。
- 广播(`broadcast`)：在同一网络可达范围内，一个网络设备向本网络内所有设备进行通信就是广播. 二层目标Mac全F或者IP地址是255.255.255.255.比如DHCP.

> 交换机所有的接口都在一个广播域中  



![[../resources/images/network/组播.png]]  



简单地说，单播->组播->广播，是通信数量不断增加的通信方式。当然，通信数量的增多，带来的是通信设备的资源消耗更大，整体网络环境的复杂度更高。

通常，我们使用组播、广播完成两件事：

1）将同一份数据交互到多个目的地。比如，视频会议、新闻分发，都需要将一份数据同时传输到多个设备上，供大家使用。

2）通过客户端请求或发现服务器。有时，我们并不知道服务器的具体信息（如IP地址），这时，我们可以采取“盲发”的方式去广播或组播信息，等待服务器收到消息盲发的消息后，返回数据，如此找到对应目标设备。

众所周知，`TCP`是可靠传输（先与另一个通信端点建立可靠连接，再传输数据），因此TCP一般只支持单播这种通信方式，而DUP通信不需要建立连接就可以发送数据，因此，通常我们说的广播、组播，都是在`UDP`下概念。

此外，广播又可以分为两类：本地广播、定向广播。

1）本地广播：广播地址为255.255.255.255.

2）定向广播：广播地址类似192.168.4.255.





