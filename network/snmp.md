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
# mibs :
```

> 需要注释 `mibs`  

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
#  system + hrSystem groups only
#view   systemonly  included   .1.3.6.1.2.1.1
#view   systemonly  included   .1.3.6.1.2.1.25.1
view all included .1

# 开启
# Read-only access to everyone to the systemonly view
#rocommunity  public default -V systemonly
#rocommunity6 public default -V systemonly
rocommunity public default

# 创建v3用户 username:snmptest, password:Abcde12345
createUser bigdata MD5 Abcde12345 DES
rwuser bigdata priv
```

> 设置完之后，想修改密码，发现不好使，应该是 /var/lib/snmp/snmpd.conf 有缓存，可以尝试把这个文件中对应的内容删掉。


### 测试
```sh
# snmpget
snmpget -u snmptest -l authPriv -a MD5 -x DES -A Abcde12345 -X Abcde12345 localhost 1.3.6.1.2.1.1.1.0
    => iso.3.6.1.2.1.1.1.0 = STRING: "Linux matrix 5.4.0-155-generic #172-Ubuntu SMP Fri Jul 7 16:10:02 UTC 2023 x86_64"

# snmpwalk
snmpwalk -v 2c -c public localhost 1.3.6.1.2.1.1.1
    => iso.3.6.1.2.1.1.1.0 = STRING: "Linux matrix 5.4.0-155-generic #172-Ubuntu SMP Fri Jul 7 16:10:02 UTC 2023 x86_64"
```

测试:`1.3.6.1.4.1.2021.11`
```sh
snmpwalk -v2c -c public localhost 1.3.6.1.4.1.2021.11
UCD-SNMP-MIB::ssIndex.0 = INTEGER: 1
UCD-SNMP-MIB::ssErrorName.0 = STRING: systemStats
UCD-SNMP-MIB::ssSwapIn.0 = INTEGER: 0 kB
UCD-SNMP-MIB::ssSwapOut.0 = INTEGER: 0 kB
UCD-SNMP-MIB::ssIOSent.0 = INTEGER: 10 blocks/s
UCD-SNMP-MIB::ssIOReceive.0 = INTEGER: 0 blocks/s
UCD-SNMP-MIB::ssSysInterrupts.0 = INTEGER: 5722 interrupts/s
UCD-SNMP-MIB::ssSysContext.0 = INTEGER: 12539 switches/s
UCD-SNMP-MIB::ssCpuUser.0 = INTEGER: 0
UCD-SNMP-MIB::ssCpuSystem.0 = INTEGER: 1
UCD-SNMP-MIB::ssCpuIdle.0 = INTEGER: 97
UCD-SNMP-MIB::ssCpuRawUser.0 = Counter32: 38977
UCD-SNMP-MIB::ssCpuRawNice.0 = Counter32: 271
UCD-SNMP-MIB::ssCpuRawSystem.0 = Counter32: 47037
UCD-SNMP-MIB::ssCpuRawIdle.0 = Counter32: 3832448
UCD-SNMP-MIB::ssCpuRawWait.0 = Counter32: 28942
UCD-SNMP-MIB::ssCpuRawKernel.0 = Counter32: 0
UCD-SNMP-MIB::ssCpuRawInterrupt.0 = Counter32: 0
UCD-SNMP-MIB::ssIORawSent.0 = Counter32: 600200
UCD-SNMP-MIB::ssIORawReceived.0 = Counter32: 1248468
UCD-SNMP-MIB::ssRawInterrupts.0 = Counter32: 134802104
UCD-SNMP-MIB::ssRawContexts.0 = Counter32: 283183106
UCD-SNMP-MIB::ssCpuRawSoftIRQ.0 = Counter32: 9730
UCD-SNMP-MIB::ssRawSwapIn.0 = Counter32: 0
UCD-SNMP-MIB::ssRawSwapOut.0 = Counter32: 0
UCD-SNMP-MIB::ssCpuRawSteal.0 = Counter32: 0
UCD-SNMP-MIB::ssCpuRawGuest.0 = Counter32: 0
UCD-SNMP-MIB::ssCpuRawGuestNice.0 = Counter32: 0
UCD-SNMP-MIB::ssCpuNumCpus.0 = INTEGER: 2
```

内存使用
```sh
snmpwalk -v2c -c public localhost 1.3.6.1.4.1.2021.4
UCD-SNMP-MIB::memIndex.0 = INTEGER: 0
UCD-SNMP-MIB::memErrorName.0 = STRING: swap
UCD-SNMP-MIB::memTotalSwap.0 = INTEGER: 2097148 kB
UCD-SNMP-MIB::memAvailSwap.0 = INTEGER: 2097148 kB
UCD-SNMP-MIB::memTotalReal.0 = INTEGER: 10187476 kB
UCD-SNMP-MIB::memAvailReal.0 = INTEGER: 8817632 kB
UCD-SNMP-MIB::memTotalFree.0 = INTEGER: 10914780 kB
UCD-SNMP-MIB::memMinimumSwap.0 = INTEGER: 16000 kB
UCD-SNMP-MIB::memShared.0 = INTEGER: 1348 kB
UCD-SNMP-MIB::memBuffer.0 = INTEGER: 291000 kB
UCD-SNMP-MIB::memCached.0 = INTEGER: 372888 kB
UCD-SNMP-MIB::memTotalSwapX.0 = Counter64: 2097148 kB
UCD-SNMP-MIB::memAvailSwapX.0 = Counter64: 2097148 kB
UCD-SNMP-MIB::memTotalRealX.0 = Counter64: 10187476 kB
UCD-SNMP-MIB::memAvailRealX.0 = Counter64: 8817632 kB
UCD-SNMP-MIB::memTotalFreeX.0 = Counter64: 10914780 kB
UCD-SNMP-MIB::memMinimumSwapX.0 = Counter64: 16000 kB
UCD-SNMP-MIB::memSharedX.0 = Counter64: 1348 kB
UCD-SNMP-MIB::memBufferX.0 = Counter64: 291000 kB
UCD-SNMP-MIB::memCachedX.0 = Counter64: 372888 kB
UCD-SNMP-MIB::memSwapError.0 = INTEGER: noError(0)
UCD-SNMP-MIB::memSwapErrorMsg.0 = STRING: 
```

cpu使用:`snmpwalk -v2c -c public localhost ssCpuIdle `  
内存使用`snmpwalk -v2c -c public localhost memTotalFree`



### 自定义mib  

https://www.cnblogs.com/kiko2014551511/p/13367024.html  


