# Java调试  
## vscode 监测JVM状态
[graalvm](https://www.graalvm.org/)  
[VisualVM官网](https://visualvm.github.io/)

vscode安装`GraalVM`
1. 安装`GraalVM Tools for Java`插件
2. 选中插件，点击`Download && Install GraalVM` 
3. 选择版本>>安装位置


## macos安装VisualVM  
[VisualVM官网](https://visualvm.github.io/)  
需要修改JDK配置

```
# 找到应用程序，右击查看内容
/Applications/VisualVM.app/Contents/Resources/visualvm/etc/visualvm.conf
visualvm_jdkhome="/Library/Java/JavaVirtualMachines/jdk1.8.0_212.jdk/Contents/Home”  
```  









