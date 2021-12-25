# 解密
[参考文章](https://juejin.cn/post/6844903638117122056)  
[License授权管理实现](https://gitee.com/smartboot/smart-license)  

## [简介](https://zh.wikipedia.org/wiki/%E5%8A%A0%E5%AF%86)   
在密码学中，加密（英语：Encryption）是将明文信息改变为难以读取的密文内容，使之不可读的过程。只有拥有解密方法的对象，经由解密过程，才能将密文还原为正常可读的内容。  
理想情况下，只有经授权的人员能够读取密文所要传达的信息。加密本身并不能防止信息传输被截取，但加密能防止截取者理解其内容。因为种种技术原因，加密方法通常使用一个通过算法生成的伪随机密钥。  
虽然任何加密后的消息都可能被破解，但对于一个良好的加密算法而言，破解需要相当多的技术和算力。授权读取信息的人可以轻松通过发信人所提供的密钥解密信息，但未经授权的人员则不行。  
密码学历史中有众多加密方法；早期的加密方法常用于军事通讯。从此开始，现代计算中也出现了众多加密技术，并且加密在现代计算中也变得越来越常见。 现代的加密方式通常使用公钥或对称密钥。  
现代加密技术依赖现代计算机在破解密钥上并不高效的事实来保证其安全性。  

## 分类  
- 对称密钥加密  
  在对称密钥加密方案中，加密和解密密钥是相同的。通信方必须具有相同的密钥才能实现安全通信。对称密钥的一个典型例子：德国军方的恩尼格玛密码机。   
  这种密码机每天都有密钥设置。当盟军弄清楚机器如何工作时，他们能够在发现给定日期传输的加密密钥后立即解密消息中编码的信息。  

- 公钥加密  
  在公钥加密（即公开密钥加密)方案中，发布加密密钥供任何人使用和加密消息。但是，只有接收方才能访问能够读取消息的解密密钥。  
  公钥加密最早是在1973年的一份秘密文件中描述的; 之前所有加密方案都是对称密钥加密（也称为私钥）。  

## MD5算法
MD5 用的是 **哈希函数**，它的典型应用是对一段信息产生 **信息摘要**，以 **防止被篡改**。严格来说，
MD5 不是一种 **加密算法** 而是 **摘要算法**。无论是多长的输入，MD5 都会输出长度为 **128bits** 
的一个串 (通常用 16 进制 表示为 32 个字符)。  

```
public static final byte[] computeMD5(byte[] content) {
    try {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        return md5.digest(content);
    } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException(e);
    }
}
```  

##  SHA1算法
SHA1 是和 MD5 一样流行的 **消息摘要算法**，然而 SHA1 比 MD5 的 安全性更强。对于长度小于 2 ^ 64 位的消息，
SHA1 会产生一个 **160** 位的 消息摘要。基于 MD5、SHA1 的信息摘要特性以及 不可逆 (一般而言)，
可以被应用在检查 **文件完整性** 以及 **数字签名** 等场景。  

```
public static byte[] computeSHA1(byte[] content) {
    try {
        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        return sha1.digest(content);
    } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException(e);
    }
}
```  

> text:Hello Java  
> len32  
> MD5: 3A79FB63BB4A36EB6421836A59E2AF10  
> len40  
> SHA1: 63522446BAA1F65EDB79D5EF8AB684C5CFD08815  

## HMAC算法 
HMAC 是密钥相关的 哈希运算消息认证码（Hash-based Message Authentication Code），HMAC 运算利用 **哈希算法** (MD5、SHA1 等)，
以 **一个密钥** 和 **一个消息** 为输入，生成一个 **消息摘要** 作为 输出。  
HMAC 发送方 和 接收方 都有的 key 进行计算，而没有这把 key 的第三方，则是 无法计算 出正确的 散列值的，这样就可以 防止数据被篡改。  

```
package net.pocrd.util;
import net.pocrd.annotation.NotThreadSafe;
import net.pocrd.define.ConstField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;


@NotThreadSafe
public class HMacHelper {
    private static final Logger logger = LoggerFactory.getLogger(HMacHelper.class);
    private Mac mac;

    /**
     * MAC算法可选以下多种算法
     * HmacMD5/HmacSHA1/HmacSHA256/HmacSHA384/HmacSHA512
     */
    private static final String KEY_MAC = "HmacMD5";
    public HMacHelper(String key) {
        try {
            SecretKey secretKey = new SecretKeySpec(key.getBytes(ConstField.UTF8), KEY_MAC);
            mac = Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);
        } catch (Exception e) {
            logger.error("create hmac helper failed.", e);
        }
    }
    public byte[] sign(byte[] content) {
        return mac.doFinal(content);
    }
    
    public boolean verify(byte[] signature, byte[] content) {
        try {
            byte[] result = mac.doFinal(content);
            return Arrays.equals(signature, result);
        } catch (Exception e) {
            logger.error("verify sig failed.", e);
        }
        return false;
    }
}

```  

## AES/DES/3DES算法  
AES、DES、3DES 都是 对称 的 **块加密算法**，加解密 的过程是 可逆的。
常用的有 AES128、AES192、AES256 (默认安装的 JDK 尚不支持 AES256，需要安装对应的 jce 补丁进行升级 jce1.7，jce1.8)。  

### DES算法  
DES 加密算法是一种 **分组密码**，以 64 位为 分组对数据 加密，它的 密钥长度 是 56 位，加密解密 用 同一算法。
DES 加密算法是对 **密钥** 进行保密，而 **公开算法**，包括加密和解密算法。
这样，只有掌握了和发送方 相同密钥 的人才能解读由 DES加密算法加密的密文数据。
因此，破译 DES 加密算法实际上就是 **搜索密钥的编码**。对于 56 位长度的 密钥 来说，如果用 穷举法 来进行搜索的话，其运算次数为 **2 ^ 56** 次。  

### 3DES算法
是基于 DES 的 **对称算法**，对 一块数据 用 三个不同的密钥 进行 三次加密，强度更高。  

### AES算法  
AES 加密算法是密码学中的 高级加密标准，该加密算法采用 **对称分组密码体制**，密钥长度的最少支持为 128 位、 192 位、256 位，分组长度 128 位，
算法应易于各种硬件和软件实现。这种加密算法是美国联邦政府采用的 区块加密标准。
**AES** 本身就是为了取代 **DES** 的，AES 具有更好的 安全性、效率 和 灵活性。  

```
import net.pocrd.annotation.NotThreadSafe;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

@NotThreadSafe
public class AesHelper {
    private SecretKeySpec keySpec;
    private IvParameterSpec iv;

    public AesHelper(byte[] aesKey, byte[] iv) {
        if (aesKey == null || aesKey.length < 16 || (iv != null && iv.length < 16)) {
            throw new RuntimeException("错误的初始密钥");
        }
        if (iv == null) {
            iv = Md5Util.compute(aesKey);
        }
        keySpec = new SecretKeySpec(aesKey, "AES");
        this.iv = new IvParameterSpec(iv);
    }

    public AesHelper(byte[] aesKey) {
        if (aesKey == null || aesKey.length < 16) {
            throw new RuntimeException("错误的初始密钥");
        }
        keySpec = new SecretKeySpec(aesKey, "AES");
        this.iv = new IvParameterSpec(Md5Util.compute(aesKey));
    }

    public byte[] encrypt(byte[] data) {
        byte[] result = null;
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/CFB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
            result = cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public byte[] decrypt(byte[] secret) {
        byte[] result = null;
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/CFB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
            result = cipher.doFinal(secret);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static byte[] randomKey(int size) {
        byte[] result = null;
        try {
            KeyGenerator gen = KeyGenerator.getInstance("AES");
            gen.init(size, new SecureRandom());
            result = gen.generateKey().getEncoded();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}

```  

## RSA算法
RSA 加密算法是目前最有影响力的 公钥加密算法，并且被普遍认为是目前 **最优秀的公钥方案** 之一。
RSA 是第一个能同时用于 **加密** 和 **数字签名** 的算法，它能够 抵抗 到目前为止已知的 所有密码攻击，已被 ISO 推荐为公钥数据加密标准。  

> RSA 加密算法 基于一个十分简单的数论事实：将两个大 素数 相乘十分容易，但想要对其乘积进行 因式分解 却极其困难，因此可以将 乘积 公开作为 加密密钥。  

```
import net.pocrd.annotation.NotThreadSafe;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.Security;
import java.security.Signature;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@NotThreadSafe
public class RsaHelper {
    private static final Logger logger = LoggerFactory.getLogger(RsaHelper.class);
    private RSAPublicKey publicKey;
    private RSAPrivateCrtKey privateKey;

    static {
        Security.addProvider(new BouncyCastleProvider()); //使用bouncycastle作为加密算法实现
    }

    public RsaHelper(String publicKey, String privateKey) {
        this(Base64Util.decode(publicKey), Base64Util.decode(privateKey));
    }

    public RsaHelper(byte[] publicKey, byte[] privateKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            if (publicKey != null && publicKey.length > 0) {
                this.publicKey = (RSAPublicKey)keyFactory.generatePublic(new X509EncodedKeySpec(publicKey));
            }
            if (privateKey != null && privateKey.length > 0) {
                this.privateKey = (RSAPrivateCrtKey)keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKey));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public RsaHelper(String publicKey) {
        this(Base64Util.decode(publicKey));
    }

    public RsaHelper(byte[] publicKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            if (publicKey != null && publicKey.length > 0) {
                this.publicKey = (RSAPublicKey)keyFactory.generatePublic(new X509EncodedKeySpec(publicKey));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] encrypt(byte[] content) {
        if (publicKey == null) {
            throw new RuntimeException("public key is null.");
        }

        if (content == null) {
            return null;
        }

        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            int size = publicKey.getModulus().bitLength() / 8 - 11;
            ByteArrayOutputStream baos = new ByteArrayOutputStream((content.length + size - 1) / size * (size + 11));
            int left = 0;
            for (int i = 0; i < content.length; ) {
                left = content.length - i;
                if (left > size) {
                    cipher.update(content, i, size);
                    i += size;
                } else {
                    cipher.update(content, i, left);
                    i += left;
                }
                baos.write(cipher.doFinal());
            }
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] decrypt(byte[] secret) {
        if (privateKey == null) {
            throw new RuntimeException("private key is null.");
        }

        if (secret == null) {
            return null;
        }

        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            int size = privateKey.getModulus().bitLength() / 8;
            ByteArrayOutputStream baos = new ByteArrayOutputStream((secret.length + size - 12) / (size - 11) * size);
            int left = 0;
            for (int i = 0; i < secret.length; ) {
                left = secret.length - i;
                if (left > size) {
                    cipher.update(secret, i, size);
                    i += size;
                } else {
                    cipher.update(secret, i, left);
                    i += left;
                }
                baos.write(cipher.doFinal());
            }
            return baos.toByteArray();
        } catch (Exception e) {
            logger.error("rsa decrypt failed.", e);
        }
        return null;
    }

    public byte[] sign(byte[] content) {
        if (privateKey == null) {
            throw new RuntimeException("private key is null.");
        }
        if (content == null) {
            return null;
        }
        try {
            Signature signature = Signature.getInstance("SHA1WithRSA");
            signature.initSign(privateKey);
            signature.update(content);
            return signature.sign();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean verify(byte[] sign, byte[] content) {
        if (publicKey == null) {
            throw new RuntimeException("public key is null.");
        }
        if (sign == null || content == null) {
            return false;
        }
        try {
            Signature signature = Signature.getInstance("SHA1WithRSA");
            signature.initVerify(publicKey);
            signature.update(content);
            return signature.verify(sign);
        } catch (Exception e) {
            logger.error("rsa verify failed.", e);
        }
        return false;
    }
}
```  

比如使用[在线网站](http://www.metools.info/code/c80.html)生成的公私钥:  
```
密钥长度: 2048bit ;密钥格式: PKCS#8 ; 私钥密码：Netvine123;

RSA加密公钥：
-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxbHDnt+VS/zjSgqeNFA/
tAEY4WSeQ3IkbifbBo8W0qtFb2YPXR0HFCARoqKhb3xlqf+sGmBJiKEj8u1i29Mb
3jgQwFzZMAh/kjtDnNvbyfl4M1ZqhsN23aqCIXEGthuXFEgKMbGYVIpF0L4187I6
4bupEsVOfRLhu9i1h2VOFLN3FnLV4XQQFZQ5LGRB1/TjPBrs8eBaLSkMRNI7OmsZ
t+7BIW6vaZfOHplUPXXR+/la6cjs42dcrH87BxBYcTg3RhLwbFxqlcoMMpxpktmH
M/KZZMJ2QFbBx6AikHWM7n4XZmnrycwZEVWIB/zopGeqC1PT+WheBDkL9Agkj8RY
DQIDAQAB
-----END PUBLIC KEY-----

RSA加密私钥：
-----BEGIN ENCRYPTED PRIVATE KEY-----
MIIE6TAbBgkqhkiG9w0BBQMwDgQIPRAgEqJHBCsCAggABIIEyNxpa6JxXXoNh7YE
PsqNV+xBHZobxXnB7/XIHZSxuN8F94S5H8nIkJGKb8IfyHNuhKHcgv+HIvHRAWXf
b8l1jhR3VZLinUSAKFvxG/Nz4d8IS5S6F1GoM3g9azUO9BbnecB2D5fHgZwhv1DH
R2g9CsQnPLkrJqLAWBA3QjhLmEdp+YgV6lWHrxQ7pLj94aBmrwj+Rsa1sVNQAp2+
NbExXTQwS27NA0WNBoipa34xh4BEhgSCcCO0r34r04vPy0hAND97GkJook0IY1Rc
UNm2qkfjZsTmvfQff9Fm9XLjti4wlrx9hKoMvie+hTTSQANawp6b7ppvr8P+/E1t
nGXH1DhEAilUUgjhOjsk3YNmIRsZY3t3rPR78z+B98YeBAPT4DoaD9u37dneUJgp
wgtagtn1SGst0bZ6Axc5unBentDomGlyVVEQGtbH+ug9+gFl3KS0Hso2lykjnfui
JH7skfg9dWaPHYh5jJve0wTSwvKEBdRtww+D0s12BNbdyvJJN/dzBfVk+9M6VhbA
Cwk9hUdGuOm7dK3aN3W7F6nd6aQlvu0VnLdPn2z4Ky7xqHsxa90uEjXT5v7mQBL2
K4UFMDxWhv/IrKCVBt/yo0bui6uKdc9Uw26aVbUlMlJeZ9E1dKI0AYNJ4PZdvHXo
Q4inaaH5bv8LvNH1RRDwDI8Cby31PMdxFev/To5JdV7Vsy6d78rsnb6PtYFAnpCl
k7D4Bs6Z/rTtGhW07RTJ9a3N2uNqDMU6hhHDdv9qDFqIDyHQybNAqKiDeBE7I2Wv
7GnXmfR+vvX6A3kn7ffKKQogxxLXVhpkOEz2XuCSj9LsSwo3CjOUL/CzFvRcF4mT
vMs3dxjYqtbbGZMuJCbusvb0tjYua6wQPdmXDghW/U4qG7hc01TMfZTi8+k26lFZ
p0vr72ngfrEoWO+WEUPry65j+2SSwca6wUPqWo8jpg7crLpbhZIFh2yhwsDndY+N
TB3B43QiTPzPdBwzM10uBXRGPlcKk8jf+vLCDv/b6iowtpsGT7RDCAmh91436KL8
KtE7MpIqSJKTXCnvc1YgV7sms+PYMh7QHiWguduDFFKXVfKRapmOJGvlrczrctHP
EZ56ZlcVIiGbes8M+XXYArzXXi208U1yf7DFW0eiP1MNPFRfLCuNkoNxqD04GvI+
yNSJbnf9/QVS27Zkn/gMLwK3InD0PXCSw+IHMnsDeKXZ7FE+AIPV7OHpejtr08pC
G4Se+TXJER3CInOBgT0ugm/yEu2276aaoeNoHbFacS9sXeMs+geWoFuFEvdUl7xn
UAHS9ZhcRoviS+PWi7PC9WTAgBUFN/BGgtcs9beIkw+yknoZmv0Uf/6EMYbYua0D
OM2k4u8nhPND/1rCy2xmlNZOurWL9WHhSTRCXH4Fx6FTRXC/yGvm/DEqeWblF+ZB
7kuTKWHwi84K21jr1kl2Efwaiq4C90k14wMNcpjs+/ItNH0mMP8Umj2XHp7fAHM4
MXDkF8H0o5CviE2Kd1qT8K6FOh6CHpiK3L5+cI8YWFkO81eMfi1nuOF+6FrSwTwI
mymVMwTUq1f06qBASlbj8Mvz+Dv4llbHSO7ok1sYe5KQ1+r5JSzh+p8ivEQtb/hu
hQFJI8zvyrgNb6CzKw==
-----END ENCRYPTED PRIVATE KEY-----
```  

加密测试 
```
输入加密公钥（以“-----BEGIN PUBLIC KEY-----”开头 “-----END PUBLIC KEY-----” 结尾）
待加密、解密的文本: Hello World 

RSA公钥加密、解密转换结果(base64了):
CQaWMS55k528q/IUYUFy+sc6H0somBLEM8BcIbGwFy+3H3eBa67KIcWYKo9oTiwmjUQf9XvdL1IWJBf7Snvpxw/9LzEw05HYpTQtAsgIY5UrBsH/y14iEq0stLSnI5wcZJJ3/rS60dIgGTsnVBDwljL3AKMA9L4KzTvwgIbOXH+0A6bVTMxbIC8ABlZK0oH6OSOkB0/tbRUadN5LFWWv84HI4bkIvguo7BjOqnMPuV+LtrOkOBJpQQZkLHesVzMxQUT66HnN5N5tgEC2BPzjsa2o0yj74SntGHmNZPZQyiYsj0PZkBoWl54wQePIUrWJpNRWi+tn97eNtRplaXS4Zw==  
```

解密结果:  
![解密结果](../resources/images/work/ras-test.png)  


## ECC算法  
ECC 也是一种 非对称加密算法，主要优势是在某些情况下，它比其他的方法使用 **更小的密钥**，比如 RSA 加密算法，提供 相当的或更高等级 的安全级别。
不过一个缺点是 **加密和解密操作** 的实现比其他机制 时间长 (相比 RSA 算法，该算法对 CPU 消耗严重)。  

```
import net.pocrd.annotation.NotThreadSafe;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.Security;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@NotThreadSafe
public class EccHelper {
    private static final Logger logger = LoggerFactory.getLogger(EccHelper.class);
    private static final int SIZE = 4096;
    private BCECPublicKey  publicKey;
    private BCECPrivateKey privateKey;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public EccHelper(String publicKey, String privateKey) {
        this(Base64Util.decode(publicKey), Base64Util.decode(privateKey));
    }

    public EccHelper(byte[] publicKey, byte[] privateKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
            if (publicKey != null && publicKey.length > 0) {
                this.publicKey = (BCECPublicKey)keyFactory.generatePublic(new X509EncodedKeySpec(publicKey));
            }
            if (privateKey != null && privateKey.length > 0) {
                this.privateKey = (BCECPrivateKey)keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKey));
            }
        } catch (ClassCastException e) {
            throw new RuntimeException("", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public EccHelper(String publicKey) {
        this(Base64Util.decode(publicKey));
    }

    public EccHelper(byte[] publicKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
            if (publicKey != null && publicKey.length > 0) {
                this.publicKey = (BCECPublicKey)keyFactory.generatePublic(new X509EncodedKeySpec(publicKey));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] encrypt(byte[] content) {
        if (publicKey == null) {
            throw new RuntimeException("public key is null.");
        }
        try {
            Cipher cipher = Cipher.getInstance("ECIES", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            int size = SIZE;
            ByteArrayOutputStream baos = new ByteArrayOutputStream((content.length + size - 1) / size * (size + 45));
            int left = 0;
            for (int i = 0; i < content.length; ) {
                left = content.length - i;
                if (left > size) {
                    cipher.update(content, i, size);
                    i += size;
                } else {
                    cipher.update(content, i, left);
                    i += left;
                }
                baos.write(cipher.doFinal());
            }
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] decrypt(byte[] secret) {
        if (privateKey == null) {
            throw new RuntimeException("private key is null.");
        }
        try {
            Cipher cipher = Cipher.getInstance("ECIES", "BC");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            int size = SIZE + 45;
            ByteArrayOutputStream baos = new ByteArrayOutputStream((secret.length + size + 44) / (size + 45) * size);
            int left = 0;
            for (int i = 0; i < secret.length; ) {
                left = secret.length - i;
                if (left > size) {
                    cipher.update(secret, i, size);
                    i += size;
                } else {
                    cipher.update(secret, i, left);
                    i += left;
                }
                baos.write(cipher.doFinal());
            }
            return baos.toByteArray();
        } catch (Exception e) {
            logger.error("ecc decrypt failed.", e);
        }
        return null;
    }

    public byte[] sign(byte[] content) {
        if (privateKey == null) {
            throw new RuntimeException("private key is null.");
        }
        try {
            Signature signature = Signature.getInstance("SHA1withECDSA", "BC");
            signature.initSign(privateKey);
            signature.update(content);
            return signature.sign();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean verify(byte[] sign, byte[] content) {
        if (publicKey == null) {
            throw new RuntimeException("public key is null.");
        }
        try {
            Signature signature = Signature.getInstance("SHA1withECDSA", "BC");
            signature.initVerify(publicKey);
            signature.update(content);
            return signature.verify(sign);
        } catch (Exception e) {
            logger.error("ecc verify failed.", e);
        }
        return false;
    }
}

```  

[网文加密算法对比图]  
![网文加密算法对比图](../resources/images/work/jiamiduibi.jpg)    


