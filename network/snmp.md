- # SNMP

## SNMP MANAGER
### 环境搭建
```sh
sudo apt-get install snmp
```

配置`/etc/snmp/snmp.conf`
```sh
# As the snmp packages come without MIB files due to license reasons, loading
# of MIBs is disabled by default. If you added the MIBs you can reenable
# loading them by commenting out the following line.
mibs :
```

## SNMP AGENT  
### 环境搭建(Ubuntu20.04)

```sh
# 查看系统自带的snmpd版本
sudo apt list -a snmpd
    Listing... Done
    snmpd/focal-updates,now 5.8+dfsg-2ubuntu2.7 amd64 [installed]
    snmpd/focal-security 5.8+dfsg-2ubuntu2.6 amd64
    snmpd/focal 5.8+dfsg-2ubuntu2 amd64

# 安装
sudo apt install snmpd libsnmp-dev

# 用来下载更新本地mib库的软件
sudo apt install snmp-mibs-downloader
    Downloading documents and extracting MIB files.
    This will take some minutes.

    In case this process fails, it can always be repeated later by executing
    /usr/bin/download-mibs again.

    RFC1155-SMI: 119 lines.
    RFC1213-MIB: 2613 lines.
    NOTE: SMUX: ignored.
    SMUX-MIB: 158 lines.
    CLNS-MIB: 1294 lines.
    RFC1381-MIB: 1007 lines.
    RFC1382-MIB: 2627 lines.
    ...
```

配置文件内容:`/var/lib/snmp/mibs/ietf`  

修改配置文件`/etc/snmp/snmpd.conf`  
- 创建v1/v2账户  
- 创建v3账户  
```sh
# V1/V2配置
view systemonly included .1.3.6.1.2.1.1
view systemonly included .1.3.6.1.2.1.25.1
rocommunity public default -V systemonly

# 创建v3用户 username:snmptest, password:Abcde12345
createUser bigdata MD5 Abcde12345 DES
rwuser bigdata priv
```



### 测试
```sh
# snmpget
snmpget -u snmptest -l authPriv -a MD5 -x DES -A Abcde12345 -X Abcde12345 localhost 1.3.6.1.2.1.1.1.0
    => iso.3.6.1.2.1.1.1.0 = STRING: "Linux matrix 5.4.0-155-generic #172-Ubuntu SMP Fri Jul 7 16:10:02 UTC 2023 x86_64"

# snmpwalk
snmpwalk -v 2c -c public localhost 1.3.6.1.2.1.1.1
    => iso.3.6.1.2.1.1.1.0 = STRING: "Linux matrix 5.4.0-155-generic #172-Ubuntu SMP Fri Jul 7 16:10:02 UTC 2023 x86_64"
```

> 设置完之后，想修改密码，发现不好使，应该是 /var/lib/snmp/snmpd.conf 有缓存，可以尝试把这个文件中对应的内容删掉。


### 自定义mib  

https://www.cnblogs.com/kiko2014551511/p/13367024.html  


