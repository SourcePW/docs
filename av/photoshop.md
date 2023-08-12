- # photoshop  

- [快捷按键](#快捷按键)
- [抠图](#抠图)
  - [选择并遮住](#选择并遮住)
- [去除水印](#去除水印)
  - [内容填充](#内容填充)
  - [画笔工具](#画笔工具)
- [图层](#图层)


## 快捷按键  
https://helpx.adobe.com/cn/photoshop/using/default-keyboard-shortcuts.html  

Option + Shift + Command + K (Mac)  

- `cmd+-` 或者`opt+滚轮` 放大缩小  
- 滚轮上下移动，`cmd+滚轮`左右移动  
- `[]` 画刷大小调整  
- `X`前景色与背景色切换， `D`默认前景色与背景色  
- `/` 前景拾取器，自定义的快捷键    
- `opt+左键` 前景拾取器
## 抠图  
原图: 
 
<div align=center>
  <img src="./../resources/images/av/koutu-test.jpg" width="75%" ></img>
</div>

### 选择并遮住  
https://helpx.adobe.com/cn/photoshop/using/select-mask.html  

首先使用`对象选择工具`快速选择，如果有些细节问题，使用快速选择工具进行进行修正,选好选区后，可以在创建一个图层，使用遮罩即可。

> 选择选区，右击选择取消选区  

<div align=center>
  <img src="./../resources/images/av/koutu-test-1.png" width="75%" ></img>
</div>

## 去除水印 
### 内容填充  
首先使用选择工具，选择水印区域，使用`选择->色彩范围`选择水印的色彩范围，再使用`选择->修改->拓展`扩大选中范围，最终选择`编辑->填充->内容识别`去除水印，残留的地方需要使用`修复工具`。  

### 画笔工具  

使用`/`拾取前景色，通过`[]`调整画笔大小，再去覆盖水印。  

<div align=center>
  <img src="./../resources/images/av/koutu-test-2.png" width="75%" ></img>
</div>

## 图层  

## 证件照

https://zhuanlan.zhihu.com/p/123315900  

整体步骤:
1. 前期拍摄注意事项
1. 照片色调校正
1. 人像磨皮修复
1. 抠图换背景
1. 服装合成

### 照片色调校正
1. 首先第一个是进行颜色的校正，颜色的校正一般我是直接在`camera raw`滤镜中进行，raw文件用PS打开，会自动进`入camera raw`滤镜，普通的JPG文件的话，菜单栏`滤镜`-`camera raw`滤镜打开，快捷键是Ctrl+shift+A  
2. 在这里面我们主要调节的是色温和曝光，如果你的照片有点偏黄偏红，就把他的数值往青蓝方向调节，如果是青蓝就往黄红方向调节，最终效果就是中规中矩，没有偏向
3. 大部分的时候，一般证件照影楼直接使用`插件`快速`磨皮`，这里我节约时间也使用`DR磨皮插件`，如果你没有插件的话看这篇获取 https://www.zhihu.com/question/20600585/answer/796188803  

> 使用DR， 找到`窗口`->`扩展`->`DR5`  

下面就是进行抠图，抠图有很多种方法，背景单一的：色彩范围、魔棒工具；复杂背景的一般好用的有三种：第一种选区+调整边缘+蒙版；第二种通道+选区+蒙版；第三种：路径+通道+蒙版；（有时间我一定要把这些抠图的所有方法讲一遍）  

很明显我这个背景比较单一，包括我们在证件照换底色的时候，背景多是纯白色或红色等等  
这里我使用`魔棒工具`，容差调小一点，模式选择加选，我们一次次加选，就会得到最精准的选区  
选中背景后，`cmd+shift+I`键执行反选，然后给选区添加一个`蒙版`，把其他的图层小眼睛关掉，看一下效果  

> 图层->图层蒙版->显示选区  

下面开始证件照的制作，新建一个证件照文档，一寸是2.5x3.5cm，二寸是3.5x5.3cm。`菜单栏`-`文件`-`置入嵌入对象`，把刚才PNG格式保存的人物导入进来，调整位置和大小

## 疑问及拓展
### 如何卸载
不要使用第三方卸载，在`Application`中找到卸载按钮进行卸载。  

如果打开报错，但不影响使用，可以卸载后，删除以下文件并重装  
> 联网时才会提示报错，不联网没问题。  

```sh
# macOS
/Library
/Library/Application Support
/Library/Preferences
~/Library
~/Library/Application Support
~/Library/Preferences

# Win
C:Program Files
C:Program Files (x86)
C:ProgramData
```

```sh
sudo rm -fr /Library/Application\ Support/Adobe
sudo rm -fr /Library/LaunchAgents/com.adobe.ccxprocess.plist
sudo rm -fr /Applications/Utilities/Adobe*
rm -fr ~/Library/Application\ Support/Adobe/
rm -fr ~/Library/Preferences/Adobe 
rm -fr ~/Library/Preferences/adobe.* 
rm -fr ~/Library/Preferences/com.Adobe* 
rm -fr ~/Library/Preferences/com.adobe* 
rm -fr /Users/ymm/Documents/Adobe
rm -fr /Users/Shared/Adobe
```



