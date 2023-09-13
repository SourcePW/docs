- # 漏洞修复

## HTTP证书问题

### 一般步骤  
生成 HTTPS 证书一般包括以下几个步骤：

1. **生成私钥和公钥**
    
   你首先需要创建一个私钥。使用 `openssl` 工具可以很容易地完成此操作。

   ```bash
   openssl genpkey -algorithm RSA -out private.key
   ```

   这会生成一个 RSA 私钥。

2. **创建证书签名请求 (CSR)**

   使用私钥创建一个证书签名请求 (CSR)，它包含公钥和一些证书应有的其他信息。

   ```bash
   openssl req -new -sha512 -key private.key -out request.csr
   ```

   这将提示您输入一些信息，如你的国家，城市，组织名等。这些信息将会包含在证书中。

3. **生成自签名证书 (如果你不打算从权威证书颁发机构获取证书的话)**

   如果你不需要向权威 CA 提交 CSR，而只是想为测试或内部使用创建一个自签名证书，你可以这样做：

   ```bash
   openssl x509 -req -days 3650 -sha512 -in request.csr -signkey private.key -out certificate.crt
   ```

   这会生成一个有效期为 1 年的自签名证书。

4. **从权威证书颁发机构获取证书**

   如果你需要一个由权威 CA 颁发的证书（如 Let's Encrypt、DigiCert、Comodo 等），你需要将步骤 2 中生成的 CSR 提交给它们。一旦验证完成，它们会为你提供一个证书。

   例如，使用 [Let's Encrypt](https://letsencrypt.org/) 的 `certbot` 工具，你可以免费获取证书。

5. **安装和配置证书**

   一旦你有了证书，你需要将其安装到你的服务器上。具体的步骤取决于你的服务器类型（如 Apache、Nginx、Tomcat 等）。

请注意，使用自签名证书的网站在大多数浏览器中都会显示安全警告，因为浏览器无法验证证书的颁发者。为了在公共互联网上的生产环境中提供安全的 HTTPS，你应该使用权威 CA 颁发的证书。


### 加密方式
`openssl` 支持的加密算法有很多，以下是一些常见的算法：

1. **对称加密算法**：在这类加密中，加密和解密使用的是相同的密钥。

   - `AES`（高级加密标准）：常见的有 AES-128, AES-192 和 AES-256。
   - `DES`（数据加密标准）：现已被认为是不安全的。
   - `3DES`（或称为 Triple-DES）：是 DES 的一个更安全的变种。
   - `RC4`：以前在很多应用中都很流行，但现在已经不推荐使用了。
   - `Blowfish`
   - `Camellia`
   
2. **非对称加密算法**：在这类加密中，有一对密钥（一个公钥和一个私钥）。公钥用于加密，私钥用于解密。

   - `RSA`：最常见的非对称加密算法，广泛应用于 SSL/TLS。
   - `DSA`（数字签名算法）
   - `ECDSA`（椭圆曲线数字签名算法）
   - `EdDSA`：比如 `Ed25519` 和 `Ed448`。
   - `DH`（Diffie-Hellman）：用于密钥交换。
   - `ECDH`（椭圆曲线 Diffie-Hellman）：椭圆曲线版本的 DH。
   
3. **散列算法**：这些不是加密算法，而是用于生成数据的固定大小的摘要或哈希值。

   - `MD5`：不再被认为是安全的。
   - `SHA-1`：安全性受到了挑战，不推荐用于安全关键的应用。
   - `SHA-2`：包括 `SHA-224`, `SHA-256`, `SHA-384`, `SHA-512`, `SHA-512/224`, 和 `SHA-512/256`。
   - `SHA-3`：是 SHA-2 的后继者，但与之有很大的差异。

这些算法中，很多都已经存在了很长时间，并在多个应用和协议中被广泛采用。当选择一个算法时，很重要的一点是确保它还是安全的，并且适合你的特定需求。在写作本文时（2021 年），像 DES、RC4 和 MD5 这样的算法已经被认为是不安全的，因此在新的应用中应当避免使用。



### 查看  

https://www.ssleye.com/ssltool/cer_check.html  

<br>
<div align=center>
<img src="../resources/images/network/%E7%AD%BE%E5%90%8D%E5%9C%A8%E7%BA%BF%E6%9F%A5%E7%9C%8B.png" width="80%"></img>  
</div>
<br>


## Nginx 
### 修改加密方式
```sh
ssl_ciphers 'ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES256-GCM-SHA384:ECDHE-ECDSA-AES256-GCM-SHA384:DHE-RSA-AES128-GCM-SHA256:DHE-RSA-AES256-GCM-SHA384:!aNULL:!eNULL:!EXPORT:!DES:!RC4:!MD5:!PSK';
```


CVE-2016-2183 漏洞涉及 OpenSSL 的 BLOCKS（Birthday Attack Against 64-bit block ciphers in TLS）攻击，这是对使用 64 位块密码（如 3DES）的 TLS 连接的攻击。这个漏洞的出现意味着攻击者可以更容易地解密 TLS 会话中捕获的数据。  

### F5 Nginx 缓冲区错误漏洞（CVE-2022-41742）

升级nginx，离线包下载:  

https://pkgs.org/download/nginx  

查看已安装版本的编译选项:
```sh
 nginx -V
nginx version: nginx/1.18.0 (Ubuntu)
built with OpenSSL 1.1.1f  31 Mar 2020
TLS SNI support enabled
configure arguments: --with-cc-opt='-g -O2 -fdebug-prefix-map=/build/nginx-lUTckl/nginx-1.18.0=. -fstack-protector-strong -Wformat -Werror=format-security -fPIC -Wdate-time -D_FORTIFY_SOURCE=2' --with-ld-opt='-Wl,-Bsymbolic-functions -Wl,-z,relro -Wl,-z,now -fPIC' --prefix=/usr/share/nginx --conf-path=/etc/nginx/nginx.conf --http-log-path=/var/log/nginx/access.log --error-log-path=/var/log/nginx/error.log --lock-path=/var/lock/nginx.lock --pid-path=/run/nginx.pid --modules-path=/usr/lib/nginx/modules --http-client-body-temp-path=/var/lib/nginx/body --http-fastcgi-temp-path=/var/lib/nginx/fastcgi --http-proxy-temp-path=/var/lib/nginx/proxy --http-scgi-temp-path=/var/lib/nginx/scgi --http-uwsgi-temp-path=/var/lib/nginx/uwsgi --with-debug --with-compat --with-pcre-jit --with-http_ssl_module --with-http_stub_status_module --with-http_realip_module --with-http_auth_request_module --with-http_v2_module --with-http_dav_module --with-http_slice_module --with-threads --with-http_addition_module --with-http_gunzip_module --with-http_gzip_static_module --with-http_image_filter_module=dynamic --with-http_sub_module --with-http_xslt_module=dynamic --with-stream=dynamic --with-stream_ssl_module --with-mail=dynamic --with-mail_ssl_module
```

源码编译:
```sh
./configure \
--prefix=/usr/share/nginx \
--sbin-path=/usr/sbin/nginx \
--conf-path=/etc/nginx/nginx.conf \
--error-log-path=/var/log/nginx/error.log \
--http-log-path=/var/log/nginx/access.log \
--with-http_ssl_module \
--with-http_stub_status_module \
--with-http_realip_module \
--with-http_auth_request_module \
--with-http_v2_module 

make -j4
make install
```

>  --with-openssl=<path>  

发现没有安装`libssl-dev`  
```sh
dpkg -l | grep libssl-dev
```

查看安装路径:
```sh
dpkg-query -L openssl
```

离线安装:
https://ubuntu.pkgs.org/20.04/ubuntu-main-amd64/libssl-dev_1.1.1f-1ubuntu2_amd64.deb.html  

```sh
dpkg -i libssl-dev_1.1.1f-1ubuntu2_amd64.deb
```

