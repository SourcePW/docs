
- new和make的区别？

- 作为函数参数时，array和slice的区别？

- slice的底层结构和原理?

- panic和recover的作用?  
//panic 能够改变程序的控制流，调用 panic 后会立刻停止执行当前函数的剩余代码，并在当前 Goroutine 中递归执行调用方的 defer；
//recover 可以中止 panic 造成的程序崩溃。它是一个只能在 defer 中发挥作用的函数，在其他作用域中调用不会发挥作用；

- 有很多goroutine,其中一个panic了会发生什么？

- defer作用？defer fmt.Println(a) fmt.Println(b) fmt.Println(c)?  cba

- sync包里提供了哪些同步原语？  
//sync.Mutex、sync.RWMutex、sync.WaitGroup、sync.Once sync.Cond sync.Pool sync.Map sync.atomic

- 接口的作用，听说过空接口吗，空接口用途？
//它的用途类似面向对象里的跟类型，赋值为任何类型的对象。

- 如果goroutine一直占用资源怎么办？  
//1.13前是有问题的。  
//现在是基于异步调度的抢占模式。  

- 进程、线程、协程区别?

- 如果 goroutine 发生了OOM 呢？  

- goroutine的原理，goroutine的优势？  
- goroutine之间的通信手段？  
- GMP的代表什么，具体调度策略能不能大致说说？  

- golang channel close后，是否可以读取剩余的数据？往一个已经被close的channel中继续发送数据会导致什么?  
可以，run-time panic。

- 反射,用字符串函数名调用函数?    
//value := reflect.ValueOf(&animal)  
//f := value.MethodByName("Eat") //通过反射获取它对应的函数，然后通过call来调用  
//f.Call([]reflect.Value{})  

- 实现消息队列?  
//使用切片加锁可以实现

- go为什么高并发场景里很快？  
//Golang实现了 CSP 并发模型做为并发基础，底层使用goroutine做为并发实体，goroutine非常轻量级可以创建几十万个实体。实体间通过 channel 继续匿名消息传递使之解耦，在语言层面实现了自动调度，这样屏蔽了很多内部细节，对外提供简单的语法关键字，大大简化了并发编程的思维转换和管理线程的复杂性。  
//一句话总结：go语言在设计的时候从关键字层面实现了多协程开发，好像语言天生支持高并发一样。  

- 做一个http服务，提供病毒特征文件上传，下载，特征计算功能，
其中特征计算时请将文件头取10K大小做md5，之后将文件按大小分成10等分，
后9等分取前1KB的特征做md5，之后再将所有md5拼接做sha512。（如果文件只有6K，
则实施结果为将文件整体做一次md5,再将后5KB每KB做一次md5）
下载时将特征码放入包头某字段，将文件内容放入包体。  


