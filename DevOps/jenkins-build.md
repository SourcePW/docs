# jenkins构建
## 增加节点
### 认证方式
- Launch agents via SSH 使用ssh协议，从master向slave发起连接，由master主动发起请求
- Non verifying Verification Strategy 

### 安装JDK8

```shell
yum install java-1.8.0-openjdk
```

## 开发调试
### vscode shell脚本
需要安装`Bash Debug`插件调试shell, `shellman`插件自动补全功能 ,`shell-format`shell格式化`format document`  

> 需要安装`https://github.com/mvdan/sh`,使用指令`go install mvdan.cc/sh/v3/cmd/shfmt@latest` 安装即可  

可以增加文件保存自动格式化:
```json
"[shellscript]": {
        "editor.formatOnSave": true,
        "files.eol": "\n"
    },
```

shell debug
```json
{
    // 使用 IntelliSense 了解相关属性。 
    // 悬停以查看现有属性的描述。
    // 欲了解更多信息，请访问: https://go.microsoft.com/fwlink/?linkid=830387
    "version": "0.2.0",
    "configurations": [
        {
            "type": "bashdb",
            "request": "launch",
            "name": "Bash-Debug (simplest configuration)",
            "program": "${workspaceFolder}/build.sh"
        }
    ]
}
```

### vscode sftp
安装`SFTP`插件

`> SFTP: config` 打开sftp配置  



Debug插件
Debug
    Open User Settings.

On Windows/Linux - File > Preferences > Settings
- On `macOS` - `Code` > `Preferences` > `Settings`
- Set `sftp.debug` to true and reload vscode.

View the `logs` in `View` > `Output` > `sftp`.






### 指令提示补全
```shell
yum install -y bash-completion
```

### 升级git
```shell
wget https://www.kernel.org/pub/software/scm/git/git-2.14.0.tar.gz --no-check-certificate
tar -zxvf git-2.14.0.tar.gz
```

安装
```shell
cd git-2.14.0
./configure --prefix=/usr/local/git all
make -j4 && make install

# 配置环境变量
ln -s /usr/local/git/bin/git /usr/bin/git
```

### shell脚本之间引用
可以通过`source`、`.filename`指令引入。但是需要确定被引入的脚本**具备可执行权限**`chmod +x`  

### npm安装
```shell
yum install nodejs npm 
```

但是需要安装指定版本`yum remove nodejs npm`
```shell
node --version
v12.22.12

npm --version
6.14.16
```

安装指定版本
```shell
curl -sL https://rpm.nodesource.com/setup_12.x | sudo bash -
sudo yum install –y nodejs npm 
node –version  # node v12.22.12 , npm 6.14.16
```















