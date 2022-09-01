// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below
const vscode = require('vscode');
const fs = require('fs');  


/**
 * @param {vscode.ExtensionContext} context
 */
function activate(context) {

	// Use the console to output diagnostic information (console.log) and errors (console.error)
	// This line of code will only be executed once when your extension is activated
	console.log('Congratulations, vscode 插件 "test" is now active!');

	// The command has been defined in the package.json file
	// Now provide the implementation of the command with  registerCommand
	// The commandId parameter must match the command field in package.json
	let disposable = vscode.commands.registerCommand('test.getFileState', uri => {
		// The code you place here will be executed every time your command is executed
        console.log(uri);
        
		// 文件路径
        const filePath = uri.path.substring(1);
        fs.stat(filePath, (err, stats) => {
            if (err) {
                vscode.window.showErrorMessage(`获取文件时遇到错误了${err}!!!`)
            }

            if (stats.isDirectory()) {
                vscode.window.showWarningMessage(`检测的是文件夹，不是文件，请重新选择！！！`);
            }

            if (stats.isFile()) {
                const size = stats.size;
                const createTime = stats.birthtime.toLocaleString();
                const modifyTime = stats.mtime.toLocaleString();

                vscode.window.showInformationMessage(`
                    文件大小为:${size}字节;
                    文件创建时间为:${createTime};
                    文件修改时间为:${modifyTime}
                `, { modal: true });
            }
        });
        
        const stats = fs.statSync(filePath);
        console.log('stats', stats);
        console.log('isFile', stats.isFile());
	});

	context.subscriptions.push(disposable);
}

// this method is called when your extension is deactivated
function deactivate() {}

module.exports = {
	activate,
	deactivate
}
