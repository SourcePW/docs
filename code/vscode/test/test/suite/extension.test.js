const assert = require('assert');

// You can import and use all API from the 'vscode' module
// as well as import your extension to test it
const vscode = require('vscode');
// const myExtension = require('../extension');

suite('Extension Test Suite', uri => {
	vscode.window.showInformationMessage('Start all tests.');

	console.log(uri)

	test('Sample test', () => {
		assert.strictEqual(null, uri.path);
	});
});
