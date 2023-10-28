- # IPSec VPN测试

- ## 目录
- [环境搭建](#环境搭建)
  - [strongswan](#strongswan)
  - [证书生成](#证书生成)
  - [服务端配置](#服务端配置)
  - [客户端配置](#客户端配置)
- [配置](#配置)
  - [拓扑](#拓扑)
  - [客户端配置](#客户端配置-1)
  - [服务端配置](#服务端配置-1)
  - [PC1 配置](#pc1-配置)
  - [PC2 配置](#pc2-配置)


## 环境搭建
### strongswan
```
sudo apt install strongswan
```

### 证书生成

1）生成CA私钥
```sh
sudo ipsec pki --gen --size 4096 --type rsa --outform pem > /etc/ipsec.d/private/ca.key.pem
```

1) 使用私钥创建根证书，CN后跟服务器名称或者地址。
```sh
ipsec pki --self --in /etc/ipsec.d/private/ca.key.pem --type rsa --dn "CN=192.168.100.200" --ca --lifetime 3650 --outform pem > /etc/ipsec.d/cacerts/ca.cert.pem
```

3）生成服务端私钥
```sh
ipsec pki --gen --size 4096 --type rsa --outform pem > /etc/ipsec.d/private/server.key.pem
```

4）使用服务端私钥创建服务端证书
```sh
ipsec pki --pub --in /etc/ipsec.d/private/server.key.pem --type rsa | ipsec pki --issue --lifetime 3650 --cacert /etc/ipsec.d/cacerts/ca.cert.pem --cakey /etc/ipsec.d/private/ca.key.pem --dn "CN=192.168.100.200" –san=”192.168.100.200” --san="192.168.100.200" --flag serverAuth --flag ikeIntermediate --outform pem > /etc/ipsec.d/certs/server.cert.pem
```

### 服务端配置

1）配置ipv4转发设置生效

修改文件`/etc/sysctl.conf`  
```sh
net.ipv4.ip_forward=1
net.ipv6.conf.all.forwarding=1
net.ipv4.conf.all.accept_redirects = 0
net.ipv6.conf.all.accept_redirects = 0

```

2）配置IPSec`/etc/ipsec.conf`  

```sh
config setup
    charondebug="ike 1, knl 1, cfg 0, net 1"
    strictcrlpolicy=no
    uniqueids=yes
    cachecrls=no

conn ipsec-ikev2-vpn
    auto=add
    compress=no
    type=tunnel
    keyexchange=ikev2
    fragmentation=yes
    forceencaps=yes
    dpdaction=clear
    dpddelay=300s
    rekey=no
    left=%any
    leftid=192.168.100.200   # 服务器IP
    leftcert=server.cert.pem
    leftsendcert=always
    leftsubnet=0.0.0.0/0
    right=%any
    rightid=%any
    rightauth=eap-mschapv2
    rightsourceip=192.169.200.128/28  # 分配给VPN服务器和客户端的虚拟地址，通常可以选择VPN服务器的私网地址
    rightdns= 192.168.31.1
    rightsendcert=never
    eap_identity=%identity
    ike=chacha20poly1305-sha512-curve25519-prfsha512,aes256gcm16-sha384-prfsha384-ecp384,aes256-sha1-modp1024,aes128-sha1-modp1024,3des-sha1-modp1024!
    esp=chacha20poly1305-sha512,aes256gcm16-ecp384,aes256-sha256,aes256-sha1,3des-sha1!

```

3）配置IPSec密钥文件

文件: `/etc/ipsec.secrets`
```
# This file holds shared secrets or RSA private keys for authentication.

# RSA private key for this host, authenticating it to any other host
# which knows the public part.
test : EAP "Test123"
```

4）启动IPSec服务端
```
ipsec start
```

### 客户端配置

1）下载服务端CA证书   
```
/etc/ipsec.d/cacerts/ca.cert.pem  => /etc/ipsec.d/cacerts目录
```

2）配置IPSec密钥

文件：`/etc/ipsec.secrets`
```
test : EAP "Test123"
```

2）配置IPSec`/etc/ipsec.conf`

```
config setup
conn ikev2-rw
    right=192.168.100.200    # 服务器地址
    rightid=192.168.100.200
    rightsubnet=0.0.0.0/0
    rightauth=pubkey
    leftsourceip=%config
    leftid=netvine
    leftauth=eap-mschapv2
    eap_identity=%identity
    auto=start
```

3）启动IPSec客户端
```
ipsec start
```

5、确认

使用`ipsec statusall` 查看VPN连接状态。

## 配置
PC2 ping PC1 的报文
```sh
1	0.000000	192.168.200.1	192.168.200.2	ESP	178	ESP (SPI=0xc2b6c907)
2	0.000000	192.168.150.1	192.168.250.2	ICMP	98	Echo (ping) request  id=0x0035, seq=9/2304, ttl=64 (no response found!)
3	0.000147	192.168.200.2	192.168.200.1	ESP	178	ESP (SPI=0xc4beb2d7)
4	0.000744	10.25.10.251	10.25.16.208	SSH	166	Server: Encrypted packet (len=112)
5	1.024110	192.168.200.1	192.168.200.2	ESP	178	ESP (SPI=0xc2b6c907)
6	1.024110	192.168.150.1	192.168.250.2	ICMP	98	Echo (ping) request  id=0x0035, seq=10/2560, ttl=64 (no response found!)
7	1.024267	192.168.200.2	192.168.200.1	ESP	178	ESP (SPI=0xc4beb2d7)
8	1.024806	10.25.10.251	10.25.16.208	SSH	182	Server: Encrypted packet (len=128)
9	2.048199	192.168.200.1	192.168.200.2	ESP	178	ESP (SPI=0xc2b6c907)
10	2.048199	192.168.150.1	192.168.250.2	ICMP	98	Echo (ping) request  id=0x0035, seq=11/2816, ttl=64 (no response found!)
11	2.048326	192.168.200.2	192.168.200.1	ESP	178	ESP (SPI=0xc4beb2d7)
12	2.049097	10.25.10.251	10.25.16.208	SSH	182	Server: Encrypted packet (len=128)
```
> 通信走公网IP，最终服务端转给PC1时需要解包，最终会以子网ip`192.168.150.1`为源IP，访问`192.168.250.2`  
> 公网加密的报文显示为`ESP`协议  

### 拓扑  
<br>
<div align=center>
<img src="../resources/images/network/VPN%E6%B5%8B%E8%AF%95.png" width="100%"></img>  
</div>
<br>

> 客户端配置`rightsubnet=192.168.250.0/24,192.168.150.0/24` 这是客户端那些流量走隧道  
> 服务端配置`rightsourceip=192.168.150.0/24  # 分配给VPN服务器和客户端的虚拟地址，通常可以选择VPN服务器的私网地址`  这时服务器也要增加一个同网段ip  
>

### 客户端配置
网卡配置`enp7s0 192.168.240.2`,`enp8s0 192.168.200.1 192.168.150.1`
```sh
6: enp7s0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc mq state UP group default qlen 1000
    link/ether 8c:1c:da:44:1d:39 brd ff:ff:ff:ff:ff:ff
    inet 192.168.240.2/24 brd 192.168.240.255 scope global enp7s0
       valid_lft forever preferred_lft forever
    inet6 fe80::8e1c:daff:fe44:1d39/64 scope link 
       valid_lft forever preferred_lft forever
7: enp8s0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc mq state UP group default qlen 1000
    link/ether 8c:1c:da:44:1d:3a brd ff:ff:ff:ff:ff:ff
    inet 192.168.200.1/24 brd 192.168.200.255 scope global enp8s0
       valid_lft forever preferred_lft forever
    inet 192.168.150.1/32 scope global enp8s0
       valid_lft forever preferred_lft forever
    inet6 fe80::8e1c:daff:fe44:1d3a/64 scope link 
       valid_lft forever preferred_lft forever
```

路由表
```sh
route -n
Kernel IP routing table
Destination     Gateway         Genmask         Flags Metric Ref    Use Iface
0.0.0.0         10.25.10.1      0.0.0.0         UG    0      0        0 enp3s0
10.25.10.0      0.0.0.0         255.255.255.0   U     0      0        0 enp3s0
192.168.1.0     0.0.0.0         255.255.255.0   U     0      0        0 enp3s0
192.168.92.0    0.0.0.0         255.255.255.0   U     0      0        0 enp5s0
192.168.200.0   0.0.0.0         255.255.255.0   U     0      0        0 enp8s0
192.168.240.0   0.0.0.0         255.255.255.0   U     0      0        0 enp7s0
192.168.250.0   0.0.0.1         255.255.255.0   UG    0      0        0 enp8s0
```

客户端下方所有的流量都需要通过`SNAT`转换
```sh
table ip netvine-nat-table {
        chain src-nat-chain {
                type nat hook postrouting priority srcnat; policy accept;
                ip daddr 192.168.250.1 iifname != "lo" oifname != "lo" log prefix "2261907479538450713_snat__nftables" snat to 192.168.150.1
        }

        chain dst-nat-chain {
                type nat hook prerouting priority dstnat; policy accept;
        }
}
```
> 只要访问服务端下方的设备，都需要经过snat转换，这里是目的ip为`192.168.250.1`,都把源ip修改为`192.168.150.1`  

ipsec配置`/etc/ipsec.conf`
```sh
config setup
conn ikev2-rw
    right=192.168.200.2    # 服务器地址
    rightid=192.168.200.2
    rightsubnet=192.168.250.0/24,192.168.150.0/24
    rightauth=pubkey
    leftsourceip=%config
    leftid=netvine
    leftauth=eap-mschapv2
    eap_identity=%identity
    auto=start
```

ipsec信息
```sh
$ ipsec statusall
Status of IKE charon daemon (strongSwan 5.8.2, Linux 5.4.0-125-generic, x86_64):
  uptime: 82 minutes, since Oct 28 17:11:50 2023
  malloc: sbrk 3219456, mmap 0, used 1196432, free 2023024
  worker threads: 11 of 16 idle, 5/0/0/0 working, job queue: 0/0/0/0, scheduled: 2
  loaded plugins: charon test-vectors ldap pkcs11 tpm aes rc2 sha2 sha1 md5 mgf1 rdrand random nonce x509 revocation constraints pubkey pkcs1 pkcs7 pkcs8 pkcs12 pgp dnskey sshkey pem openssl gcrypt af-alg fips-prf gmp curve25519 agent chapoly xcbc cmac hmac ctr ccm gcm ntru drbg curl attr kernel-netlink resolve socket-default connmark farp stroke vici updown eap-identity eap-aka eap-md5 eap-gtc eap-mschapv2 eap-dynamic eap-radius eap-tls eap-ttls eap-peap eap-tnc xauth-generic xauth-eap xauth-pam tnc-tnccs dhcp lookip error-notify certexpire led addrblock unity counters
Listening IP addresses:
  10.25.10.251
  192.168.1.100
  192.168.92.2
  192.168.240.2
  192.168.200.1
Connections:
    ikev2-rw:  %any...192.168.200.2  IKEv1/2
    ikev2-rw:   local:  [netvine] uses EAP_MSCHAPV2 authentication with EAP identity '%any'
    ikev2-rw:   remote: [192.168.200.2] uses public key authentication
    ikev2-rw:   child:  dynamic === 192.168.250.0/24 192.168.150.0/24 TUNNEL
Security Associations (1 up, 0 connecting):
    ikev2-rw[1]: ESTABLISHED 82 minutes ago, 192.168.200.1[netvine]...192.168.200.2[192.168.200.2]
    ikev2-rw[1]: IKEv2 SPIs: 5a17ccaaca7c9bb0_i* 7dc72e68ee5e2cdb_r, EAP reauthentication in 88 minutes
    ikev2-rw[1]: IKE proposal: CHACHA20_POLY1305/PRF_HMAC_SHA2_512/CURVE_25519
    ikev2-rw{2}:  INSTALLED, TUNNEL, reqid 1, ESP in UDP SPIs: cccd5724_i c7992f80_o
    ikev2-rw{2}:  AES_CBC_256/HMAC_SHA2_256_128, 0 bytes_i, 171444 bytes_o (2041 pkts, 0s ago), rekeying in 9 minutes
    ikev2-rw{2}:   192.168.150.1/32 === 192.168.150.0/24 192.168.250.0/24
```
> 目的ip为`192.168.250.0/24 192.168.150.0/24`走隧道  
> ikev2-rw:   child:  dynamic === 192.168.250.0/24 192.168.150.0/24 TUNNEL  

### 服务端配置

网卡配置`enp7s0 192.168.250.2`,`enp8s0 192.168.200.2 192.168.150.10`
```sh
7: enp7s0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc mq state UP group default qlen 1000
    link/ether a4:53:ee:70:86:25 brd ff:ff:ff:ff:ff:ff
    inet 192.168.250.2/24 brd 192.168.250.255 scope global enp7s0
       valid_lft forever preferred_lft forever
    inet6 fe80::a653:eeff:fe70:8625/64 scope link 
       valid_lft forever preferred_lft forever
8: enp8s0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc htb state UP group default qlen 1000
    link/ether a4:53:ee:70:86:26 brd ff:ff:ff:ff:ff:ff
    inet 192.168.200.2/24 brd 192.168.200.255 scope global enp8s0
       valid_lft forever preferred_lft forever
    inet 192.168.150.10/32 scope global enp8s0
       valid_lft forever preferred_lft forever
    inet6 fe80::a653:eeff:fe70:8626/64 scope link 
       valid_lft forever preferred_lft forever
```

路由表`enp7s0`、`enp8s0`:
```sh
route -n
Kernel IP routing table
Destination     Gateway         Genmask         Flags Metric Ref    Use Iface
0.0.0.0         10.25.10.1      0.0.0.0         UG    0      0        0 enp3s0
10.25.10.0      0.0.0.0         255.255.255.0   U     0      0        0 enp3s0
192.168.1.0     0.0.0.0         255.255.255.0   U     0      0        0 enp3s0
192.168.200.0   0.0.0.0         255.255.255.0   U     0      0        0 enp8s0
192.168.250.0   0.0.0.0         255.255.255.0   U     0      0        0 enp7s0
```
> ip addr add 192.168.250.2/24 dev enp7s0  
> ip route add 192.168.240.0/24 via 192.168.240.1 dev enp7s0
> ip route del 192.168.240.0/24 via 192.168.240.1


nftables规则: 测试ping通，会禁用icmp协议
```sh
table ip netvine-table {
        chain base-rule-chain {
                type filter hook forward priority filter; policy drop;
                ip protocol icmp log prefix "1292555540231801979_icmp_baseRuleTag_@L#D_nftables" counter drop
        }
}
```

ipsec配置`/etc/ipsec.conf`
```sh
config setup
    charondebug="ike 1, knl 1, cfg 0, net 1"
    strictcrlpolicy=no
    uniqueids=yes
    cachecrls=no

conn ipsec-ikev2-vpn
    auto=add
    compress=no
    type=tunnel
    keyexchange=ikev2
    fragmentation=yes
    forceencaps=yes
    dpdaction=clear
    dpddelay=300s
    rekey=no
    left=%any
    leftid=192.168.200.2   # 服务器IP
    leftsubnet=0.0.0.0/0
    leftcert=server.cert.pem
    leftsendcert=always
    right=%any
    rightid=%any
    rightauth=eap-mschapv2
    rightsourceip=192.168.150.0/24  # 分配给VPN服务器和客户端的虚拟地址，通常可以选择VPN服务器的私网地址
    rightdns= 192.168.200.2
    rightsendcert=never
    eap_identity=%identity
    ike=chacha20poly1305-sha512-curve25519-prfsha512,aes256gcm16-sha384-prfsha384-ecp384,aes256-sha1-modp1024,aes128-sha1-modp1024,3des-sha1-modp1024!
    esp=chacha20poly1305-sha512,aes256gcm16-ecp384,aes256-sha256,aes256-sha1,3des-sha1!
```

ipsec状态`ipsec statusall`
```sh
$ ipsec statusall
Status of IKE charon daemon (strongSwan 5.9.5, Linux 5.4.0-165-generic, x86_64):
  uptime: 83 minutes, since Oct 28 17:11:43 2023
  malloc: sbrk 3952640, mmap 0, used 2013296, free 1939344
  worker threads: 11 of 16 idle, 5/0/0/0 working, job queue: 0/0/0/0, scheduled: 1
  loaded plugins: charon test-vectors ldap pkcs11 tpm aesni aes rc2 sha2 sha1 md5 mgf1 rdrand random nonce x509 revocation constraints pubkey pkcs1 pkcs7 pkcs8 pkcs12 pgp dnskey sshkey pem openssl gcrypt af-alg fips-prf gmp curve25519 agent chapoly xcbc cmac hmac ctr ccm gcm ntru drbg curl attr kernel-netlink resolve socket-default connmark forecast farp stroke updown eap-identity eap-aka eap-md5 eap-gtc eap-mschapv2 eap-dynamic eap-radius eap-tls eap-ttls eap-peap eap-tnc xauth-generic xauth-eap xauth-pam tnc-tnccs dhcp lookip error-notify certexpire led addrblock unity counters
Virtual IP pools (size/online/offline):
  192.168.150.0/24: 254/1/0
Listening IP addresses:
  10.25.10.127
  192.168.1.100
  192.168.250.2
  192.168.200.2
  192.168.150.10
Connections:
ipsec-ikev2-vpn:  %any...%any  IKEv2, dpddelay=300s
ipsec-ikev2-vpn:   local:  [192.168.200.2] uses public key authentication
ipsec-ikev2-vpn:    cert:  "CN=192.168.200.2"
ipsec-ikev2-vpn:   remote: uses EAP_MSCHAPV2 authentication with EAP identity '%any'
ipsec-ikev2-vpn:   child:  0.0.0.0/0 === dynamic TUNNEL, dpdaction=clear
Security Associations (1 up, 0 connecting):
ipsec-ikev2-vpn[2]: ESTABLISHED 83 minutes ago, 192.168.200.2[192.168.200.2]...192.168.200.1[netvine]
ipsec-ikev2-vpn[2]: IKEv2 SPIs: 5a17ccaaca7c9bb0_i 7dc72e68ee5e2cdb_r*, rekeying disabled
ipsec-ikev2-vpn[2]: IKE proposal: CHACHA20_POLY1305/PRF_HMAC_SHA2_512/CURVE_25519
ipsec-ikev2-vpn{2}:  INSTALLED, TUNNEL, reqid 1, ESP in UDP SPIs: c7992f80_i cccd5724_o
ipsec-ikev2-vpn{2}:  AES_CBC_256/HMAC_SHA2_256_128, 174972 bytes_i (2083 pkts, 0s ago), 0 bytes_o, rekeying disabled
ipsec-ikev2-vpn{2}:   192.168.150.0/24 192.168.250.0/24 === 192.168.150.1/32
```

### PC1 配置
```sh
3: enp3s0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc mq state UP group default qlen 1000
    link/ether 0c:73:eb:92:7f:aa brd ff:ff:ff:ff:ff:ff
    inet 192.168.91.1/24 brd 192.168.91.255 scope global enp3s0
       valid_lft forever preferred_lft forever
    inet 192.168.250.1/24 scope global enp3s0
       valid_lft forever preferred_lft forever
    inet6 fe80::e73:ebff:fe92:7faa/64 scope link 
       valid_lft forever preferred_lft forever
```

路由表
```sh
root@netvine:~# route -n
Kernel IP routing table
Destination     Gateway         Genmask         Flags Metric Ref    Use Iface
0.0.0.0         10.25.10.1      0.0.0.0         UG    0      0        0 enp2s0
10.25.10.0      0.0.0.0         255.255.255.0   U     0      0        0 enp2s0
192.168.0.0     192.168.250.2   255.255.255.0   UG    0      0        0 enp3s0
192.168.0.0     192.168.250.2   255.255.0.0     UG    0      0        0 enp3s0
192.168.240.0   192.168.250.2   255.255.255.0   UG    0      0        0 enp3s0
192.168.250.0   0.0.0.0         255.255.255.0   U     0      0        0 enp3s0
```
> 目的ip为`192.168.240.0/24` 全走`enp3s0`网关未`192.168.250.2`  


### PC2 配置
```sh
3: enp2s0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc mq state UP group default qlen 1000
    link/ether 00:03:2d:2a:af:67 brd ff:ff:ff:ff:ff:ff
    inet 192.168.240.1/24 brd 192.168.240.255 scope global enp2s0
       valid_lft forever preferred_lft forever
    inet6 fe80::203:2dff:fe2a:af67/64 scope link 
       valid_lft forever preferred_lft forever
```

路由表
```sh
route -n
Kernel IP routing table
Destination     Gateway         Genmask         Flags Metric Ref    Use Iface
0.0.0.0         10.25.10.1      0.0.0.0         UG    0      0        0 enp1s0
10.25.10.0      0.0.0.0         255.255.255.0   U     0      0        0 enp1s0
192.168.0.0     192.168.240.2   255.255.0.0     UG    0      0        0 enp2s0
192.168.240.0   0.0.0.0         255.255.255.0   U     0      0        0 enp2s0
192.168.250.0   192.168.240.2   255.255.255.0   UG    0      0        0 enp2s0
```