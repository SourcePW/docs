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

