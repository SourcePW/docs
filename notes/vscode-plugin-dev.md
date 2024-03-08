- # vscode 插件开发  
[源码](../code/vscode/test)  
[官方vscode插件开发文档](https://code.visualstudio.com/api) 


## 初始化
macos 插件安装位置`/Users/ymm/.vscode/extensions`  

> 官方为了方便开发人员进行vscode插件的开发，提供了对yo应的脚手架来生成对应的项目  

```shell
// 安装需要的包
npm install -g yo generator-code  

// 运行
yo code
```

日志输出
```shell

     _-----_     ╭──────────────────────────╮
    |       |    │   Welcome to the Visual  │
    |--(o)--|    │   Studio Code Extension  │
   `---------´   │        generator!        │
    ( _´U`_ )    ╰──────────────────────────╯
    /___A___\   /
     |  ~  |     
   __'.___.'__   
 ´   `  |° ´ Y ` 

? What type of extension do you want to create? New Extension (JavaScript)
? What's the name of your extension? hello-vs
? What's the identifier of your extension? test
? What's the description of your extension? vscode test dev
? Enable JavaScript type checking in 'jsconfig.json'? Yes
? Initialize a git repository? No
? Which package manager to use? npm
```

插件基本结构
```shell
├── CHANGELOG.md
├── README.md
├── extension.js
├── jsconfig.json
├── package-lock.json
├── package.json
├── test
│   ├── runTest.js
│   └── suite
└── vsc-extension-quickstart.md
```

### package.json
`package.json`
```js
{
  "name": "test",
  "displayName": "hello-vs",
  "description": "vscode test dev",
  "version": "0.0.1",
  "engines": {
    "vscode": "^1.70.0"
  },
  "categories": [
    "Other"
  ],
  "activationEvents": [
    "onCommand:test.helloWorld"
  ],
  "main": "./extension.js",
  "contributes": {
    "commands": [
      {
        "command": "test.helloWorld",
        "title": "Hello World"
      }
    ]
  },
  "scripts": {
    "lint": "eslint .",
    "pretest": "npm run lint",
    "test": "node ./test/runTest.js"
  },
  "devDependencies": {
    "@types/vscode": "^1.70.0",
    "@types/glob": "^7.2.0",
    "@types/mocha": "^9.1.1",
    "@types/node": "16.x",
    "eslint": "^8.20.0",
    "glob": "^8.0.3",
    "mocha": "^10.0.0",
    "typescript": "^4.7.4",
    "@vscode/test-electron": "^2.1.5"
  }
}
```  

在这份清单文件中，重点关注的主要有三部分内容：main、activationEvents以及contributes，是整个文件中的重中之重。

- main 指明了该插件的主入口在哪，只有找到主入口整个项目才能正常的运转
- activationEvents 指明该插件在何种情况下才会被激活，因为只有激活后插件才能被正常使用
- contributes 通过扩展注册contributes用来扩展Visual Studio Code中的各项技能

## extension.js

```js
// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below
const vscode = require('vscode');

// this method is called when your extension is activated
// your extension is activated the very first time the command is executed

/**
 * @param {vscode.ExtensionContext} context
 */
function activate(context) {

	// Use the console to output diagnostic information (console.log) and errors (console.error)
	// This line of code will only be executed once when your extension is activated
	console.log('Congratulations, your extension "test" is now active!');

	// The command has been defined in the package.json file
	// Now provide the implementation of the command with  registerCommand
	// The commandId parameter must match the command field in package.json
	let disposable = vscode.commands.registerCommand('test.helloWorld', function () {
		// The code you place here will be executed every time your command is executed

		// Display a message box to the user
		vscode.window.showInformationMessage('Hello World from hello-vs!');
	});

	context.subscriptions.push(disposable);
}

// this method is called when your extension is deactivated
function deactivate() {}

module.exports = {
	activate,
	deactivate
}
```

该文件时其入口文件，即package.json中main字段对应的文件  
- activate 这是插件被激活时执行的函数
- deactivate 这是插件被销毁时调用的方法，比如释放内存等

## 获取文件大小插件  
- 通过在文件编辑区域或文件名上右击弹出按钮，点击按钮获取文件的大小、创建时间和修改时间  
- 如果获取的是文件夹，则指明该文件是文件夹，不是文件，给予提示


### 修改package.json 



### 修改extension.js  


### 编译打包安装
安装对应的模块vsce
```shell
npm i vsce -g
```

> npm WARN notsup Unsupported engine for vsce@2.11.0: wanted: {"node":">= 14"} (current: {"node":"12.22.2","npm":"6.14.13"})

查看安装版本并切换
```shell
▶ nvm ls
       v10.24.1
->     v12.22.2
       v14.17.6
         system
nvm use v14.17.6
```

利用vsce进行打包，生成对应的vsix文件
```shell
vsce package
```

安装到vscode
错误
```shell
▶ vsce package    
 ERROR  Make sure to edit the README.md file before you package or publish your extension.
```

> 编辑README.md 只有在开始打包  

```shell
▶ vsce package
 WARNING  A 'repository' field is missing from the 'package.json' manifest file.
Do you want to continue? [y/N] y
 WARNING  LICENSE.md, LICENSE.txt or LICENSE not found
Do you want to continue? [y/N] y
 DONE  Packaged: /Users/ymm/work/mygithub/docs/code/vscode/test/test-0.0.1.vsix (6 files, 3.99KB)
```

在插件管理中找到`从VSIX安装`  


![[../resources/images/notes/vsix-install.png]]


#### 使用 
可以在插件中`@installed hello` 查看插件信息


![[../resources/images/notes/vscode-test-plugin.png]]


在文件目录窗口，右击，选择`获取文件信息`，即可使用  


![[../resources/images/notes/vscode-plugin-test-1.png]]




![[../resources/images/notes/vscode-plugin-test-2.png]]



也可以在文件内容区域右击，选择`获取文件信息`


![[../resources/images/notes/vscode-plugin-test-4.png]]



#### 命令面板中调用  



![[../resources/images/notes/vscode-plugin-test-3.png]]



但是报错


![[../resources/images/notes/vscode-plugin-test-5.png]]



查看vscode的`日志(拓展宿主)`  

```shell
[2022-09-01 14:35:09.685] [exthost] [error] TypeError: Cannot read properties of undefined (reading 'path')
	at /Users/ymm/.vscode/extensions/undefined.test-0.0.2/extension.js:23:30
	at a._executeContributedCommand (/Applications/Visual Studio Code.app/Contents/Resources/app/out/vs/workbench/api/node/extensionHostProcess.js:85:63922)
	at a.$executeContributedCommand (/Applications/Visual Studio Code.app/Contents/Resources/app/out/vs/workbench/api/node/extensionHostProcess.js:85:64626)
	at o._doInvokeHandler (/Applications/Visual Studio Code.app/Contents/Resources/app/out/vs/workbench/api/node/extensionHostProcess.js:95:13691)
```

这个问题的原因在于资源管理器或者文件内容区，可以获取当前的文件路径。  
如果在命令面板，就无法传递`uri`  

## 调试插件 

```shell
{
  "version": "0.2.0",
  "configurations": [
    {
      "name": "Extension Tests",
      "type": "extensionHost",
      "request": "launch",
      "runtimeExecutable": "${execPath}",
      "args": [
        "--extensionDevelopmentPath=${workspaceFolder}",
        "--extensionTestsPath=${workspaceFolder}/out/test/suite/index"
      ],
      "outFiles": ["${workspaceFolder}/out/test/**/*.js"]
    }
  ]
}
```

调试本插件
```shell
{
  "version": "0.2.0",
  "configurations": [
    {
      "name": "Extension Tests",
      "type": "extensionHost",
      "request": "launch",
      "args": [
        "--extensionDevelopmentPath=${workspaceFolder}/code/vscode/test",
        "--extensionTestsPath=${workspaceFolder}/code/vscode/test/test/suite/index"
      ],
    }
  ]
}
```



![[../resources/images/notes/vscode-plugin-test-6.png]]














