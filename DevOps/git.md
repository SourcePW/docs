- # git常用操作  

[git 在线](https://learngitbranching.js.org/?locale=zh_CN)  

## rebase/pull/merge 区别及应用场景  

首先有个A分支，因为项目需要，从A分支复制一个B分支，两个分支同时开发修改，目前想把A分支的最新修改都合并到B分支上?

如果使用`rebase`，两个分支的`commit`都会一个个合并，很多冲突可能需要多次解决，很麻烦  

如果使用`merge`，一次性会显示出所有冲突，修改后直接`git commit`，有一个`merge`的提交记录，包含修改冲突的。 
如果使用`pull`, 其实效果和`merge`类似  
> You have unmerged paths.  
  (fix conflicts and run "git commit")  
  (use "git merge --abort" to abort the merge)    

## git patch  

Git中的`git patch`操作通常用于创建、应用和管理补丁文件。补丁文件包含了文件或文件夹的差异信息，允许您将更改应用到不同的代码库中或者与他人分享。

以下是一些常见的Git补丁相关操作：

1. **生成补丁文件**：

   使用`git format-patch`命令可以生成补丁文件，将当前分支与另一个分支或提交之间的差异保存到文件中。例如，要将当前分支与`master`分支之间的差异保存到补丁文件中：

   ```bash
   git format-patch master --stdout > my_patch.patch
   ```

   这将生成一个名为`my_patch.patch`的补丁文件。

   两个提交之间的:

   ```sh
   git format-patch <start-commit>..<end-commit> --stdout > my_patch.patch
   ```

   最新提交:
   ```sh
   git format-patch HEAD~1..HEAD --stdout > my_patch.patch
   ```

2. **应用补丁文件**：

   使用`git apply`命令可以将补丁文件应用到代码库中。例如，要将补丁文件`my_patch.patch`应用到当前分支：

   ```bash
   git apply my_patch.patch
   ```

   如果要将补丁文件应用并提交更改，可以使用`git am`命令：

   ```bash
   git am < my_patch.patch
   ```

   > git am相当于执行完git apply之后，再执行:git commit -m "Applied changes from my_patch.patch"  

   如果git打补丁出错，可以手动修改补丁包后再应用  



![[../resources/images/devops/git-patch修改.png]]  




3. **查看补丁信息**：

   使用`git log`命令可以查看提交历史，包括补丁的作者、提交消息等信息。例如，要查看最近的5个提交：

   ```bash
   git log -n 5
   ```

   使用`git show`命令可以查看某个提交的详细信息，包括更改的内容。

4. **编辑和修改补丁**：

   如果需要编辑和修改补丁文件，您可以使用文本编辑器打开补丁文件，然后手动修改其中的内容。然后，使用`git apply`或`git am`重新应用修改后的补丁。

这些是Git中的一些基本的补丁相关操作。补丁文件通常用于将更改传递给其他代码库、合作伙伴或团队成员，以便进行代码审查、问题修复等操作。它们也可用于将更改从一个分支或存储库应用到另一个分支或存储库。

