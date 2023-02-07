- # 构建环境搭建

## docker jenkins
[jenkins docker](https://hub.docker.com/_/jenkins/tags)  

```shell
docker pull jenkins:2.32.3
docker run --name jenkins -p 8080:8080 -p 50000:50000 jenkins:2.32.3
```