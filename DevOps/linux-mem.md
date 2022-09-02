# linux 内存占用分析
## 基本信息
```shell
$ free -h
              total        used        free      shared  buff/cache   available
Mem:            15G         15G        169M        1.4M         97M         49M
Swap:           18G        9.5G        9.5G
```

## top 
```shell
  PID USER      PR  NI    VIRT    RES    SHR S  %CPU %MEM     TIME+ COMMAND                                                                                                                                                                
23315 root      20   0  565200  17972   2488 S  31.2  0.1  47:01.57 Southwest_Engin                                                                                                                                                        
   66 root      20   0       0      0      0 S  12.5  0.0   1:43.29 kswapd0                                                                                                                                                                
 1961 mysql     20   0 4755208 327676   3484 S   6.2  2.0 589:46.31 mysqld                                                                                                                                                                 
    1 root      20   0  191152   1768   1184 S   0.0  0.0   0:02.72 systemd                                                                                                                                                                
    2 root      20   0       0      0      0 S   0.0  0.0   0:00.01 kthreadd                                                                                                                                                               
    4 root       0 -20       0      0      0 S   0.0  0.0   0:00.00 kworker/0:0H                                                                                                                                                           
    6 root      20   0       0      0      0 S   0.0  0.0   0:00.83 ksoftirqd/0                                                                                                                                                            
    7 root      rt   0       0      0      0 S   0.0  0.0   0:00.03 migration/0                                                                                                                                                            
    8 root      20   0       0      0      0 S   0.0  0.0   0:00.00 rcu_bh                                                                                                                                                                 
    9 root      20   0       0      0      0 S   0.0  0.0   1:25.68 rcu_sched                                                                                                                                                              
   10 root       0 -20       0      0      0 S   0.0  0.0   0:00.00 lru-add-drain 
```

## 内存使用分类
`cat /proc/meminfo`
```shell
MemTotal:       16178732 kB
MemFree:          172064 kB
MemAvailable:      49140 kB
Buffers:            3520 kB
Cached:            29496 kB
SwapCached:      6839324 kB
Active:         13732768 kB
Inactive:        1599096 kB
Active(anon):   13713556 kB
Inactive(anon):  1586596 kB
Active(file):      19212 kB
Inactive(file):    12500 kB
Unevictable:         388 kB
Mlocked:               0 kB
SwapTotal:      19914748 kB
SwapFree:        9992956 kB
Dirty:                 0 kB
Writeback:             0 kB
AnonPages:      15296084 kB
Mapped:            17208 kB
Shmem:              1408 kB
Slab:             100572 kB
SReclaimable:      67064 kB
SUnreclaim:        33508 kB
KernelStack:        4256 kB
PageTables:        41072 kB
NFS_Unstable:          0 kB
Bounce:                0 kB
WritebackTmp:          0 kB
CommitLimit:    28004112 kB
Committed_AS:   19074444 kB
VmallocTotal:   34359738367 kB
VmallocUsed:      579364 kB
VmallocChunk:   34358423548 kB
Percpu:             2112 kB
HardwareCorrupted:     0 kB
AnonHugePages:   4511744 kB
CmaTotal:              0 kB
CmaFree:               0 kB
HugePages_Total:       0
HugePages_Free:        0
HugePages_Rsvd:        0
HugePages_Surp:        0
Hugepagesize:       2048 kB
DirectMap4k:      201472 kB
DirectMap2M:     3905536 kB
DirectMap1G:    12582912 kB
```

`DirectMap1G:    12582912 kB`  


## 应用使用内存分布
```shell
ps aux --sort -rss
```

输出
```shell
USER       PID %CPU %MEM    VSZ   RSS TTY      STAT START   TIME COMMAND
root      1738 13.0 92.2 18674828 14926928 ?   Sl   9月01 147:35 ./audit-server -c config-test.yaml
mysql     1961 52.2  2.1 4755208 346848 ?      Sl   9月01 590:25 /usr/local/mysql/bin/mysqld --basedir=/usr/local/mysql --datadir=/data/mysql --plugin-dir=/usr/local/mysql/lib/plugin --user=mysql --log-error=localhost.localdomain.err -
root     23315 15.2  0.1 565200 17952 ?        Ssl  06:28  48:36 /opt/audit/ids/bin/southwest_engine -c /opt/audit/ids/config/southwest_engine_3_0.yaml -i eth2
root      2007  0.0  0.0 1525192 15428 ?       Sl   9月01   0:21 ./watcher server -c /opt/watcher/config/settings.yml
root      1733  0.3  0.0 165772  3260 ?        Sl   9月01   3:37 /usr/local/redis/src/redis-server 0.0.0.0:6379
root     20897  0.0  0.0 116596  2204 pts/0    Ss   11:36   0:00 -bash
root      1729  0.0  0.0 246432  2168 ?        Ss   9月01   1:06 /usr/bin/python /usr/bin/supervisord -c /etc/supervisord.conf
nobody    1745  0.0  0.0  46924  2088 ?        S    9月01   0:03 nginx: worker process
root     21078  0.0  0.0 155584  2020 pts/0    R+   11:47   0:00 ps aux --sort -rss
root     20895  0.0  0.0 158988  1812 ?        Ss   11:36   0:00 sshd: root@pts/0
root       580  0.0  0.0  39060  1416 ?        Ss   9月01   0:00 /usr/lib/systemd/systemd-journald
root       891  0.0  0.0 562412  1220 ?        Ssl  9月01   0:08 /usr/sbin/NetworkManager --no-daemon
root       844  0.0  0.0 368608  1156 ?        Ssl  9月01   0:00 /usr/bin/python2 -Es /usr/sbin/firewalld --nofork --nopid
root         1  0.0  0.0 191152  1088 ?        Ss   9月01   0:02 /usr/lib/systemd/systemd --switched-root --system --deserialize 22
root      1239  0.0  0.0 574280   624 ?        Ssl  9月01   0:05 /usr/bin/python2 -Es /usr/sbin/tuned -l -P
root       818  0.0  0.0  21672   508 ?        Ss   9月01   0:09 /usr/sbin/irqbalance --foreground
polkitd    813  0.0  0.0 614872   500 ?        Ssl  9月01   0:00 /usr/lib/polkit-1/polkitd --no-debug
root      1244  0.0  0.0 224592   460 ?        Ssl  9月01   0:02 /usr/sbin/rsyslogd -n
root       830  0.0  0.0  26384   420 ?        Ss   9月01   0:00 /usr/lib/systemd/systemd-logind
dbus       814  0.0  0.0  58228   344 ?        Ss   9月01   0:00 /usr/bin/dbus-daemon --system --address=systemd: --nofork --nopidfile --systemd-activation
root       837  0.0  0.0 126388   280 ?        Ss   9月01   0:00 /usr/sbin/crond -n
root       789  0.0  0.0  55532   188 ?        S<sl 9月01   0:00 /sbin/auditd
root       610  0.0  0.0  46116    88 ?        Ss   9月01   0:00 /usr/lib/systemd/systemd-udevd
root      1242  0.0  0.0 112984    88 ?        Ss   9月01   0:00 /usr/sbin/sshd -D
root      1346  0.0  0.0 203380    80 ?        Ss   9月01   0:00 login -- root
root      6630  0.0  0.0 116468    72 ttyS0    Ss+  9月01   0:00 -bash
root      1734  0.0  0.0  46000    68 ?        S    9月01   0:00 nginx: master process /usr/local/nginx/sbin/nginx -g daemon off;
```


