- # USB设备  

目录:  
- [问题记录](#问题记录)
	- [非法拔插U盘后fsck检查出错](#非法拔插u盘后fsck检查出错)



## 基础知识  
### 基础概念  
U盘（USB闪存驱动器）设备的开发主要涉及硬件设计、固件编程和与主机操作系统（如Linux）的交互。以下是U盘设备开发的常用操作和相关的Linux指令：

- #### U盘设备开发常用操作

1. **硬件选择和设计**：选择合适的USB控制器和存储介质（如NAND闪存）。设计电路板和封装。

2. **固件编程**：编写或定制固件来管理USB通信和存储操作。

3. **USB通信协议实现**：实现USB大容量存储设备类（Mass Storage Class）协议，使得U盘能够与主机系统通信。

4. **分区和文件系统**：设置和管理U盘的分区以及文件系统（如FAT32, exFAT, NTFS）。

5. **测试和调试**：使用各种工具（如USB分析器）进行硬件和软件的调试和测试。

6. **性能优化**：优化存储器的读写速度和设备的响应时间。

7. **安全性考虑**：实现数据加密和安全访问控制功能。

8. **符合USB标准和认证**：确保设备符合USB标准，并通过相关认证。

- #### Linux中操作U盘的常用指令

在Linux系统中，常用以下指令来操作U盘：

1. **检测U盘**：
   - `lsusb`：列出USB设备。
   - `lsblk`：列出所有块设备，包括U盘。

`lsblk -f`：这个命令会列出所有块设备的文件系统类型，包括U盘。在"FSTYPE"列中可以找到相应的文件系统类型。  

2. **挂载U盘**：
   - `sudo mount /dev/sdx1 /mnt/usb`：将U盘（例如/dev/sdx1）挂载到/mnt/usb目录。

3. **查看U盘信息**：
   - `sudo fdisk -l`：显示所有磁盘分区信息，包括U盘。
   - `df -h`：显示挂载的文件系统的磁盘空间使用情况。

4. **格式化U盘**：
   - `sudo mkfs.vfat /dev/sdx1`：将U盘格式化为FAT32文件系统。
   - `sudo mkfs.ntfs /dev/sdx1`：将U盘格式化为NTFS文件系统。

5. **数据拷贝和备份**：
   - `cp`：拷贝文件到U盘。
   - `dd if=/dev/sdx of=backup.img`：创建U盘的完整镜像。

6. **卸载U盘**：
   - `sudo umount /mnt/usb`：卸载U盘。


### 实际应用    
`fsck`文件系统检测  
```sh
$ fsck /dev/sdb1 
fsck，来自 util-linux 2.23.2
fsck.fat 3.0.20 (12 Jun 2013)
0x41: Dirty bit is set. Fs was not properly unmounted and some data may be corrupt.
1) Remove dirty bit
2) No action
```

`blkid`  
```sh
/dev/sr1: UUID="2022-08-31-07-37-40-00" LABEL="Ubuntu-Server 20.04.5 LTS amd64" TYPE="iso9660" PTUUID="36c74be4" PTTYPE="dos"
/dev/sda2: UUID="cf07fa09-99e3-4771-bf48-08e26f567c81" TYPE="ext4" PARTUUID="59b55583-5187-451f-ae79-52fac3f8e880"
/dev/sda3: UUID="yqcwAO-1rtf-IDU3-1b7v-2hgF-Tts6-gRkBTe" TYPE="LVM2_member" PARTUUID="cf8054cc-dabe-48ab-9a70-e28120dcc72a"
/dev/mapper/ubuntu--vg-ubuntu--lv: UUID="2bea57e9-4187-4294-ab6b-d536c1cdb520" TYPE="ext4"
/dev/loop0: TYPE="squashfs"
/dev/loop1: TYPE="squashfs"
/dev/loop2: TYPE="squashfs"
/dev/loop3: TYPE="squashfs"
/dev/loop4: TYPE="squashfs"
/dev/sda1: PARTUUID="09608683-5a0d-4636-bd51-03d8e34ff67c"
/dev/sdb1: LABEL="UBUNTU-AUTO" UUID="A836BAA836BA76C2" TYPE="ntfs" PARTUUID="177e8a1b-01"
```

`lsblk`  
```sh
NAME                      MAJ:MIN RM  SIZE RO TYPE MOUNTPOINT
fd0                         2:0    1    4K  0 disk 
loop0                       7:0    0   62M  1 loop /snap/core20/1611
loop1                       7:1    0 67.8M  1 loop /snap/lxd/22753
loop2                       7:2    0 63.9M  1 loop /snap/core20/2105
loop3                       7:3    0 40.4M  1 loop /snap/snapd/20671
loop4                       7:4    0 91.9M  1 loop /snap/lxd/24061
sda                         8:0    0  200G  0 disk 
├─sda1                      8:1    0    1M  0 part 
├─sda2                      8:2    0    2G  0 part /boot
└─sda3                      8:3    0  198G  0 part 
  └─ubuntu--vg-ubuntu--lv 253:0    0   99G  0 lvm  /
sdb                         8:16   1 57.7G  0 disk 
└─sdb1                      8:17   1 57.7G  0 part 
sr0                        11:0    1 1024M  0 rom  
sr1                        11:1    1  1.3G  0 rom  
```

```sh
mount /dev/sdb1 /data/usb/

mount | grep -i usb
/dev/sdb1 on /data/usb type fuseblk (rw,relatime,user_id=0,group_id=0,allow_other,blksize=4096)
```

 **`lspci`**   
```sh
lspci 
00:00.0 Host bridge: Intel Corporation 440BX/ZX/DX - 82443BX/ZX/DX Host bridge (rev 01)
00:01.0 PCI bridge: Intel Corporation 440BX/ZX/DX - 82443BX/ZX/DX AGP bridge (rev 01)
00:07.0 ISA bridge: Intel Corporation 82371AB/EB/MB PIIX4 ISA (rev 08)
00:07.1 IDE interface: Intel Corporation 82371AB/EB/MB PIIX4 IDE (rev 01)
00:07.3 Bridge: Intel Corporation 82371AB/EB/MB PIIX4 ACPI (rev 08)
00:07.7 System peripheral: VMware Virtual Machine Communication Interface (rev 10)
00:0f.0 VGA compatible controller: VMware SVGA II Adapter
00:10.0 SCSI storage controller: Broadcom / LSI 53c1030 PCI-X Fusion-MPT Dual Ultra320 SCSI (rev 01)
00:11.0 PCI bridge: VMware PCI bridge (rev 02)
```

 **`lspci`**：
   - `lspci`（List PCI）的功能是列出系统中所有PCI（外围组件互连标准）总线上的设备。
   - 它通常用于显示连接到PCI和PCIe（PCI Express）总线的设备，如显卡、网卡、声卡、USB控制器等。
   - `lspci` 提供的信息有助于识别系统中的硬件组件，特别是那些需要特定驱动程序的组件。
   - 例如，运行 `lspci` 可能会显示类似于 “NVIDIA Corporation GP102 [GeForce GTX 1080 Ti]” 的信息，表明系统有一个NVIDIA的GeForce GTX 1080 Ti显卡。

> PCI的全称是`Peripheral Component Interconnect`。这是一种计算机总线标准，用于连接计算机母板上的外围设备。PCI总线支持硬件设备之间的连接，例如显卡、网络卡、声卡等，允许它们与中央处理器（CPU）和其他硬件通信。这种标准最初在1990年代被广泛采用，随着时间的推移，PCI已经发展成多种变体，包括PCI-X和PCI Express（PCIe）。其中，PCI Express在现代计算机系统中更为常见，因为它提供了更高的传输速率和更好的性能。  

查看u盘内核日志`dmesg -w`:  
```sh
[339132.047715] usb 1-1: new high-speed USB device number 3 using ehci-pci
[339132.418249] usb 1-1: New USB device found, idVendor=0951, idProduct=1666, bcdDevice= 1.10
[339132.418253] usb 1-1: New USB device strings: Mfr=1, Product=2, SerialNumber=3
[339132.418255] usb 1-1: Product: DataTraveler 3.0
[339132.418258] usb 1-1: Manufacturer: Kingston
[339132.418259] usb 1-1: SerialNumber: E0D55EA573CCE741087A041F
[339132.421564] usb-storage 1-1:1.0: USB Mass Storage device detected
[339132.422822] scsi host33: usb-storage 1-1:1.0
[339133.452603] scsi 33:0:0:0: Direct-Access     Kingston DataTraveler 3.0 PMAP PQ: 0 ANSI: 6
[339133.453100] sd 33:0:0:0: Attached scsi generic sg3 type 0
[339133.460924] sd 33:0:0:0: [sdb] 120938496 512-byte logical blocks: (61.9 GB/57.7 GiB)
[339133.465166] sd 33:0:0:0: [sdb] Write Protect is off
[339133.465167] sd 33:0:0:0: [sdb] Mode Sense: 45 00 00 00
[339133.467222] sd 33:0:0:0: [sdb] Write cache: disabled, read cache: enabled, doesn't support DPO or FUA
[339133.517736]  sdb: sdb1
[339133.557127] sd 33:0:0:0: [sdb] Attached SCSI removable disk
[339137.848368] blk_update_request: I/O error, dev fd0, sector 0 op 0x0:(READ) flags 0x0 phys_seg 1 prio class 0
[339137.848777] floppy: error 10 while reading block 0
[339230.635087] usb 1-1: USB disconnect, device number 3
[339250.916421] usb 1-1: new high-speed USB device number 4 using ehci-pci
[339251.284569] usb 1-1: New USB device found, idVendor=0951, idProduct=1666, bcdDevice= 1.10
[339251.284570] usb 1-1: New USB device strings: Mfr=1, Product=2, SerialNumber=3
[339251.284570] usb 1-1: Product: DataTraveler 3.0
[339251.284571] usb 1-1: Manufacturer: Kingston
[339251.284571] usb 1-1: SerialNumber: E0D55EA573CCE741087A041F
[339251.287095] usb-storage 1-1:1.0: USB Mass Storage device detected
[339251.287546] scsi host33: usb-storage 1-1:1.0
[339252.298975] scsi 33:0:0:0: Direct-Access     Kingston DataTraveler 3.0 PMAP PQ: 0 ANSI: 6
[339252.299353] sd 33:0:0:0: Attached scsi generic sg3 type 0
[339252.306847] sd 33:0:0:0: [sdb] 120938496 512-byte logical blocks: (61.9 GB/57.7 GiB)
[339252.309349] sd 33:0:0:0: [sdb] Write Protect is off
[339252.309350] sd 33:0:0:0: [sdb] Mode Sense: 45 00 00 00
[339252.311861] sd 33:0:0:0: [sdb] Write cache: disabled, read cache: enabled, doesn't support DPO or FUA
[339252.590246]  sdb: sdb1
[339252.631762] sd 33:0:0:0: [sdb] Attached SCSI removable disk
[339289.304281] usb 1-1: USB disconnect, device number 4
```


### FSCK  
在Ubuntu 20.04（或类似的Linux系统）中，`fsck`（File System Consistency Check）是一个用于检查和修复文件系统错误的工具。它对文件系统的健康和一致性进行检查，特别是在非正常关机或系统崩溃后。以下是`fsck`的检测流程和原理：

### 检测流程

1. **启动检查**：
   - 通常，`fsck`会在系统启动时自动运行，尤其是如果文件系统被标记为需要检查，或者系统没有正确关机。
   - 系统管理员也可以手动运行`fsck`，但这通常在`非挂载状态`下对文件系统进行。

2. **检查阶段**：
   - `fsck`首先检查文件系统的超级块或控制结构，这是文件系统的元数据核心。
   - 然后，它检查和比对文件系统的不同部分，如`索引节点`（inodes）、`数据块`、`目录结构`等。  

3. **修复错误**：
   - 如果发现错误，`fsck`会尝试自动修复。这可能包括修复索引节点、重建文件系统日志、删除损坏的文件等。
   - 在某些情况下，`fsck`可能会提示用户输入，以决定如何处理特定的问题。

4. **完成检查**：
   - 一旦完成所有检查和修复，`fsck`会报告其操作结果，包括修复的错误数量和状态。

### 内核日志  
```sh

[ 5418.314844] scsi 13:0:0:0: Direct-Access              USB DISK 3.0     PMAP PQ: 0 ANSI: 6
[ 5418.315096] sd 13:0:0:0: Attached scsi generic sg1 type 0
[ 5418.316022] sd 13:0:0:0: [sdb] 61440000 512-byte logical blocks: (31.4 GB/29.2 GiB)
[ 5418.316288] sd 13:0:0:0: [sdb] Write Protect is off
[ 5418.316292] sd 13:0:0:0: [sdb] Mode Sense: 23 00 00 00
[ 5418.316421] sd 13:0:0:0: [sdb] No Caching mode page found
[ 5418.316427] sd 13:0:0:0: [sdb] Assuming drive cache: write through
[ 5418.318777]  sdb: sdb4
[ 5418.320955] sd 13:0:0:0: [sdb] Attached SCSI removable disk
[ 5418.465628] FAT-fs (sdb4): utf8 is not a recommended IO charset for FAT filesystems, filesystem will be case sensitive!
[ 5418.474982] FAT-fs (sdb4): Volume was not properly unmounted. Some data may be corrupt. Please run fsck.
[ 5427.400835] FAT-fs (sdb4): error, invalid access to FAT (entry 0x30303030)
[ 5427.400842] FAT-fs (sdb4): Filesystem has been set read-only
[ 5989.551713] FAT-fs (sdb4): error, invalid access to FAT (entry 0x64656e6d)
[ 5989.552875] FAT-fs (sdb4): error, invalid access to FAT (entry 0x6573745c)
[ 5989.554045] FAT-fs (sdb4): error, invalid access to FAT (entry 0x2f2f7473)
[ 5989.555199] FAT-fs (sdb4): error, invalid access to FAT (entry 0x7265694c)
[ 5989.556385] FAT-fs (sdb4): error, invalid access to FAT (entry 0x225c2068)
```

### uevent  
```sh
$ udevadm monitor  
monitor will print the received events for:
UDEV - the event which udev sends out after rule processing
KERNEL - the kernel uevent
```

`U盘挂载事件`  
```sh
KERNEL[342952.971965] add      /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1 (usb)
KERNEL[342952.975992] add      /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0 (usb)
KERNEL[342952.977552] add      /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33 (scsi)
KERNEL[342952.977590] add      /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/scsi_host/host33 (scsi_host)
KERNEL[342952.977623] bind     /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0 (usb)
KERNEL[342952.977661] bind     /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1 (usb)
UDEV  [342952.980135] add      /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1 (usb)
UDEV  [342952.983372] add      /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0 (usb)
UDEV  [342952.984097] add      /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33 (scsi)
UDEV  [342952.984712] add      /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/scsi_host/host33 (scsi_host)
UDEV  [342952.985141] bind     /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0 (usb)
UDEV  [342952.985639] bind     /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1 (usb)
KERNEL[342955.032643] add      /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0 (scsi)
KERNEL[342955.032662] add      /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0/33:0:0:0 (scsi)
KERNEL[342955.032667] add      /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0/33:0:0:0/scsi_device/33:0:0:0 (scsi_device)
KERNEL[342955.032670] add      /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0/33:0:0:0/scsi_disk/33:0:0:0 (scsi_disk)
KERNEL[342955.032676] add      /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0/33:0:0:0/scsi_generic/sg3 (scsi_generic)
KERNEL[342955.032694] add      /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0/33:0:0:0/bsg/33:0:0:0 (bsg)
UDEV  [342955.033443] add      /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0 (scsi)
UDEV  [342955.034073] add      /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0/33:0:0:0 (scsi)
UDEV  [342955.035410] add      /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0/33:0:0:0/scsi_device/33:0:0:0 (scsi_device)
UDEV  [342955.035971] add      /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0/33:0:0:0/scsi_disk/33:0:0:0 (scsi_disk)
UDEV  [342955.036279] add      /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0/33:0:0:0/scsi_generic/sg3 (scsi_generic)
UDEV  [342955.036289] add      /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0/33:0:0:0/bsg/33:0:0:0 (bsg)
KERNEL[342955.061188] add      /devices/virtual/bdi/8:16 (bdi)
UDEV  [342955.061616] add      /devices/virtual/bdi/8:16 (bdi)
KERNEL[342955.084117] add      /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0/33:0:0:0/block/sdb (block)
KERNEL[342955.084133] add      /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0/33:0:0:0/block/sdb/sdb4 (block)
KERNEL[342955.129193] bind     /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0/33:0:0:0 (scsi)
UDEV  [342955.430407] add      /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0/33:0:0:0/block/sdb (block)

UDEV  [342955.684020] add      /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0/33:0:0:0/block/sdb/sdb4 (block)
UDEV  [342955.686663] bind     /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0/33:0:0:0 (scsi)
```

U盘卸载事件 
```sh
KERNEL[342936.790640] remove   /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0/33:0:0:0/bsg/33:0:0:0 (bsg)
KERNEL[342936.791257] remove   /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0/33:0:0:0/scsi_generic/sg3 (scsi_generic)
KERNEL[342936.791266] remove   /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0/33:0:0:0/scsi_device/33:0:0:0 (scsi_device)
KERNEL[342936.791281] remove   /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0/33:0:0:0/scsi_disk/33:0:0:0 (scsi_disk)
KERNEL[342936.791648] remove   /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0/33:0:0:0/block/sdb/sdb4 (block)
KERNEL[342936.792275] remove   /devices/virtual/bdi/8:16 (bdi)
KERNEL[342936.792334] remove   /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0/33:0:0:0/block/sdb (block)
UDEV  [342936.792445] remove   /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0/33:0:0:0/bsg/33:0:0:0 (bsg)
UDEV  [342936.793410] remove   /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0/33:0:0:0/scsi_device/33:0:0:0 (scsi_device)
UDEV  [342936.793594] remove   /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0/33:0:0:0/block/sdb/sdb4 (block)
UDEV  [342936.793772] remove   /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0/33:0:0:0/scsi_disk/33:0:0:0 (scsi_disk)
UDEV  [342936.794191] remove   /devices/virtual/bdi/8:16 (bdi)
UDEV  [342936.794723] remove   /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0/33:0:0:0/scsi_generic/sg3 (scsi_generic)
UDEV  [342936.795099] remove   /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0/33:0:0:0/block/sdb (block)
KERNEL[342936.812197] unbind   /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0/33:0:0:0 (scsi)
KERNEL[342936.812240] remove   /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0/33:0:0:0 (scsi)
UDEV  [342936.812900] unbind   /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0/33:0:0:0 (scsi)
UDEV  [342936.813401] remove   /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0/33:0:0:0 (scsi)
KERNEL[342936.838454] remove   /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0 (scsi)
KERNEL[342936.838479] remove   /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/scsi_host/host33 (scsi_host)
KERNEL[342936.838485] remove   /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33 (scsi)
UDEV  [342936.839123] remove   /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/target33:0:0 (scsi)
UDEV  [342936.839326] remove   /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33/scsi_host/host33 (scsi_host)
UDEV  [342936.839806] remove   /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0/host33 (scsi)
KERNEL[342936.856759] unbind   /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0 (usb)
KERNEL[342936.856788] remove   /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0 (usb)
KERNEL[342936.856911] unbind   /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1 (usb)
KERNEL[342936.856958] remove   /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1 (usb)
UDEV  [342936.857531] unbind   /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0 (usb)
UDEV  [342936.857961] remove   /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1/1-1:1.0 (usb)
UDEV  [342936.858451] unbind   /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1 (usb)
UDEV  [342936.858825] remove   /devices/pci0000:00/0000:00:11.0/0000:02:03.0/usb1/1-1 (usb)
```

查看U盘是否接入:
```sh
$ lsusb
Bus 001 Device 011: ID 13fe:6300 Kingston Technology Company Inc. USB DISK 3.0
Bus 001 Device 001: ID 1d6b:0002 Linux Foundation 2.0 root hub
Bus 002 Device 003: ID 0e0f:0002 VMware, Inc. Virtual USB Hub
Bus 002 Device 002: ID 0e0f:0003 VMware, Inc. Virtual Mouse
Bus 002 Device 001: ID 1d6b:0001 Linux Foundation 1.1 root hub
```

`lspci`  
```sh
lspci | grep -i usb
02:00.0 USB controller: VMware USB1.1 UHCI Controller
02:03.0 USB controller: VMware USB2 EHCI Controller
```

### golang-U盘事件监听  
golang使用`https://github.com/vishvananda/netlink`  

```go
type NetlinkListener struct {
	fd int
	sa *syscall.SockaddrNetlink
}

func UdiskListen() {
	l, _ := ListenNetlink()
	for {
		if err := l.ReadMsgs(); err != nil {
			fmt.Println("=========   ", err)
		}
	}
}

func ListenNetlink() (*NetlinkListener, error) {
	groups := syscall.RTNLGRP_LINK |
		syscall.RTNLGRP_IPV4_IFADDR |
		syscall.RTNLGRP_IPV6_IFADDR
	fd, err := syscall.Socket(syscall.AF_NETLINK, syscall.SOCK_DGRAM,
		syscall.NETLINK_KOBJECT_UEVENT)
	if err != nil {
		global.NETVINE_LOG.Error("syscall socket error: ", zap.Error(err))
		return nil, err
	}
	sa := &syscall.SockaddrNetlink{
		Family: syscall.AF_NETLINK,
		Pid:    uint32(0),
		Groups: uint32(groups),
	}
	if err = syscall.Bind(fd, sa); err != nil {
		global.NETVINE_LOG.Error("syscall bind error: ", zap.Error(err))
		return nil, err
	}
	return &NetlinkListener{fd: fd, sa: sa}, nil
}

func (l *NetlinkListener) ReadMsgs() error {
	defer func() {
		recover()
	}()
	pkt := make([]byte, 2048)
	if _, err := syscall.Read(l.fd, pkt); err != nil {
		global.NETVINE_LOG.Error("syscall read error: ", zap.Error(err))
		return err
	}
	outMsg := string(pkt)

	if find := strings.Contains(outMsg, "DEVTYPE=disk") && strings.Contains(outMsg, "SUBSYSTEM=block") &&
		(strings.Contains(outMsg, "add") || strings.Contains(outMsg, "remove")); find {
		if strings.Contains(outMsg, "add") {
			//u盘刚插入，等一秒钟再执行检测程序，否则会出现进程阻塞的现象
			time.Sleep(1 * time.Second)
		}
		global.NETVINE_LOG.Info("检测到u盘插拔：" + outMsg)
		CheckUkey()
	}
	return nil
}
```

# 问题记录  
## 非法拔插U盘后fsck检查出错  

```sh
$ fsck /dev/sdb1 
fsck from util-linux 2.34
fsck.fat 4.1 (2017-01-24)
0x41: Dirty bit is set. Fs was not properly unmounted and some data may be corrupt.
1) Remove dirty bit
2) No action
```

挂载使用U盘前，先修复一下:
```sh
fsck /dev/sdb1 -y
fsck from util-linux 2.34
fsck.fat 4.1 (2017-01-24)
0x41: Dirty bit is set. Fs was not properly unmounted and some data may be corrupt.
 Automatically removing dirty bit.
Performing changes.
/dev/sdb1: 608 files, 21385/1048192 clusters
$ fsck /dev/sdb1 
fsck from util-linux 2.34
fsck.fat 4.1 (2017-01-24)
/dev/sdb1: 608 files, 21385/1048192 clusters
```

> 这样就没有问题了。  



无法使用的U盘，使用`fsck`也无法修复:
```sh
# 第一次修复
$ fsck /dev/sdb4 
fsck from util-linux 2.34
fsck.fat 4.1 (2017-01-24)
0x41: Dirty bit is set. Fs was not properly unmounted and some data may be corrupt.
1) Remove dirty bit
2) No action
? 1
There are differences between boot sector and its backup.
This is mostly harmless. Differences: (offset:original/backup)
  71:43/4e, 72:65/4f, 73:6e/20, 74:74/4e, 75:4f/41, 76:53/4d, 77:20/45
  , 78:37/20, 80:78/20, 81:38/20, 282:70/ef, 283:26/be, 284:05/ad, 285:00/de
  , 288:00/ce, 289:00/fa, 290:00/ed, 291:00/fe
1) Copy original to backup
2) Copy backup to original
3) No action
? 1
Filesystem has 3836224 clusters but only space for 3833854 FAT entries.

# 第二次修复  
$ fsck /dev/sdb4 
fsck from util-linux 2.34
fsck.fat 4.1 (2017-01-24)
0x41: Dirty bit is set. Fs was not properly unmounted and some data may be corrupt.
1) Remove dirty bit
2) No action
? 1
There are differences between boot sector and its backup.
This is mostly harmless. Differences: (offset:original/backup)
  71:43/4e, 72:65/4f, 73:6e/20, 74:74/4e, 75:4f/41, 76:53/4d, 77:20/45
  , 78:37/20, 80:78/20, 81:38/20, 282:70/ef, 283:26/be, 284:05/ad, 285:00/de
  , 288:00/ce, 289:00/fa, 290:00/ed, 291:00/fe
1) Copy original to backup
2) Copy backup to original
3) No action
? 2
Filesystem has 3836224 clusters but only space for 3833854 FAT entries.

# 第三次修复  
$ fsck /dev/sdb4 
fsck from util-linux 2.34
fsck.fat 4.1 (2017-01-24)
0x41: Dirty bit is set. Fs was not properly unmounted and some data may be corrupt.
1) Remove dirty bit
2) No action
```