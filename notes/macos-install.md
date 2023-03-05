- # 安装指定版本的MacOS  

苹果系统最好不要随意升级，不然会有很多兼容性问题.  

[官方文档](https://support.apple.com/zh-cn/HT201372)  

## 下载镜像  

[如何下载MacOS](https://support.apple.com/zh-cn/HT211683)  

<div align=center>
  <img src="../resources/images/network/macos-install-1.png" width="70%"></img>
</div>

## 条件  
- USB 闪存驱动器或其他备用宗卷（格式化为 Mac OS 扩展格式），至少有 14GB 可用储存空间。
- OS X El Capitan 或更高版本的安装器。要获取完整的安装器，Mac 必须使用最新版本的 OS X El Capitan、最新版本的 macOS Sierra 或任何更高版本的 macOS。下载时所用的 Mac 必须与你要下载的 macOS 兼容  

<div align=center>
  <img src="../resources/images/network/macos-install-2.png" width="70%"></img>
</div>

## 使用`终端`创建可引导安装器

1. 插入要用于保存可引导安装器的 USB 闪存驱动器或其他宗卷。 
2. 打开`应用程序`文件夹内`实用工具`文件夹中的`终端`。  
3. 在`终端`中键入或粘贴以下命令之一，然后按下 Return 键以输入这个命令。下述每个命令都假设安装器位于你的`应用程序`文件夹中，并且`MyVolume`是你所使用的 USB 闪存驱动器或其他宗卷的名称。如果宗卷不是这个名称，请将命令中的 `MyVolume` 替换为相应名称。   

4. 当系统提示你键入管理员密码时，请照做。在你键入密码时，`终端`不会显示任何字符。然后按下 Return 键  
5. 当系统提示键入 Y 来确认你要抹掉宗卷时，请照做，然后按下 Return 键。在抹掉宗卷的过程中，`终端`会显示进度。
6. 宗卷被抹掉后，你可能会看到一条提醒，提示`终端`要访问可移除宗卷上的文件。点按`好`以允许继续拷贝。 
7. 当`终端`显示操作已完成时，相应宗卷将拥有与你下载的安装器相同的名称，例如`安装 macOS Monterey`。你现在可以退出`终端`并弹出宗卷。

- Ventura
```sh
sudo /Applications/Install\ macOS\ Monterey.app/Contents/Resources/createinstallmedia --volume /Volumes/MyVolume
```

- Big Sur  
```sh
sudo /Applications/Install\ macOS\ Big\ Sur.app/Contents/Resources/createinstallmedia --volume /Volumes/MyVolume
```

> sudo /Applications/Install\ macOS\ Big\ Sur.app/Contents/Resources/createinstallmedia --volume /Volumes/macos-install

## 使用可引导安装器
1. 将可引导安装器插入已连接到互联网且与你要安装的 macOS 版本兼容的 Mac。（可引导安装器不会从互联网下载 macOS，但却需要互联网连接才能获取特定于 Mac 机型的固件和其他信息。）
2. 将 Mac 开机，并立即按住 `Option (Alt)` 键。
3. 当你看到显示可引导宗卷的黑屏时，松开 `Option` 键。
4. 选择包含可引导安装器的宗卷。然后点按屏幕上的箭头或按下 Return 键。 
5. 如果你无法从可引导安装器启动，请确保`启动安全性实用工具`已设为允许从外部介质或可移动介质启动。
6. 根据提示选取你的语言。
7. 从`实用工具`窗口中选择`安装 macOS`（或`安装 OS X`），然后点按`继续`，并按照屏幕上的说明进行操作。
