#!/usr/bin/env groovy


def SAVE_FILE_PATH = '/home/jenkins/workspace/zenteam/test_result/' + env.JOB_NAME + "/job_" + env.BUILD_NUMBER + "_" + params.sn + "/"


//https://javadoc.jenkins-ci.org/index-all.html
pipeline{
    agent {label 'd1'}
    
    environment {
        SPRD_PROJECT = "MS16"
        
        //job name
        TOP_JOB = 'top_info_statistic_d1.y'
        MEM_INFO_JOB = 'memory_info_statistic_d1.y'
        SPRD_FLASH_ROM_JOB = 'sprd_flash_rom'
        MTK_FLASH_ROM_JOB = 'mtk_flash_rom'
        NXCARSERVICE_JOB = 'nxCarService_TestCase_d1.y'
        
        //test file save
        MEM_FILE = 'mem_info.txt'
        TOP_FILE = 'top_info.txt'
        
        //并行的job，需要等待其完成才能结束
        //不能等于""，要不然访问不了
        concurrent_jobs = "NULL"
        
        ZEN_URL = 'http://zenportal.intranet.cn:9095/zen_interior_web/deploy/updatePipelineStatus'
        
        //status 0 未开始 1开始 2 结束
        STATUS_PIPLINE_NONE = 0
        STATUS_PIPLINE_START = 1
        STATUS_PIPLINE_END = 2
        
        //Test Case top,meminfo,nxCarService
        TEST_CASE_TOP = 'top'
        TEST_CASE_MEM = 'meminfo'
        TEST_CASE_NXCARSERVICE = 'nxCarService'
        
    }
    
    stages {
        stage('Init') {
            steps{
                echo 'Init step'
                echo "SAVE_FILE_PATH = ${SAVE_FILE_PATH}"
                echo "test_item = ${params.test_item}"
                
                script{
                    //上报状态给Zen
                    sendPiplineStausToZen(ZEN_URL,params.sn,"Pipline",params.key,"Stage","Init",STATUS_PIPLINE_START,System.currentTimeMillis(),"");
                    
                    sleepRandom(0,60)
                    
                    //获取在运行的JOB(除了自己)，如果job参数中sn号和自己一致，那就需要等待另一个测试结束
                    getJobSNAndWait(env.JOB_NAME, env.BUILD_NUMBER, params.sn)
                    
                    sendPiplineStausToZen(ZEN_URL,params.sn,"Pipline",params.key,"Stage","Init",STATUS_PIPLINE_END,"",System.currentTimeMillis());
                }
                
            }
        }
        
        stage('Build') {
            steps{
                echo 'Build step'
                //上报状态给Zen
                sendPiplineStausToZen(ZEN_URL,params.sn,"Pipline",params.key,"Stage","Build",STATUS_PIPLINE_START,System.currentTimeMillis(),"");
                    
                    
                sendPiplineStausToZen(ZEN_URL,params.sn,"Pipline",params.key,"Stage","Build",STATUS_PIPLINE_END,"",System.currentTimeMillis());
            }
        }
        
        stage('Deploy') {
            steps{
                echo "Deploy step ,path=${rom_path} ,build_id=${env.BUILD_ID}"
                //上报状态给Zen
                sendPiplineStausToZen(ZEN_URL,params.sn,"Pipline",params.key,"Stage","Deploy",STATUS_PIPLINE_START,System.currentTimeMillis(),"");
                    
                script{
                    def path = params.rom_path
                    println "path=" + path + ",length=" + path.length()
                    
                    if(path.length() > 0){
                        if(path.toUpperCase().contains(SPRD_PROJECT)){
                            println "展讯设备"
                            build job: SPRD_FLASH_ROM_JOB, parameters: [string(name: 'rom_path', value: params.rom_path), string(name: 'device_sn', value: params.sn)]
                        }else{
                            println "MTK设备"
                            build job: MTK_FLASH_ROM_JOB, parameters: [string(name: 'rom_path', value: params.rom_path), string(name: 'features', value: 'format-download'), string(name: 'device_sn', value: params.sn)]
                        }
                        
                        def maxTry = 60
                        def count = 0
                        
                        while(true){
                            
                            //wait device online  "ssh jenkins@d1.y adb devices";
                            String control = "adb devices";
                            //need bash 需要使用bash登录，权限问题,运行在jenkins.y上 //String[] command = ["/bin/bash", "-c", "ifconfig"];
                            String[] command = ["/bin/bash", "-c", "ssh jenkins@d1.y " + control];
                            
                            def devices = execShell(command)
                            def deviceSn = params.sn
                            
                            println "command=" + command + ",devices=" + devices
                        
                            if(devices != null && devices.contains(deviceSn)){
                                println "device online " + deviceSn
                                break;
                            }else{
                                println "--------------------- device offline " + deviceSn + " try " + count + " s---------------------"
                                sleep(10)
                            }
                            
                            if(count >= maxTry){
                                 println "--------------------- device offline " + deviceSn + " try max conut ,exit---------------------"
                                break
                            }
                            
                            count ++
                        }
                    }else{
                        println "rom path is null ------------------------------------------------------------------------"
                    }
                    
                }
                
                //end
                sendPiplineStausToZen(ZEN_URL,params.sn,"Pipline",params.key,"Stage","Deploy",STATUS_PIPLINE_END,"",System.currentTimeMillis());
            }
        }
        stage('Test') {
            steps{
                echo 'Test step'  
                sendPiplineStausToZen(ZEN_URL,params.sn,"Pipline",params.key,"Stage","Test",STATUS_PIPLINE_START,System.currentTimeMillis(),"");
                    
                script{
                    //top info
                    def testItem = params.test_item
                   
                    if(testItem != null && testItem.contains(TEST_CASE_TOP)){
                         echo 'top info'
                        def nextTopBuildNumber = getJobNumberAndWait(TOP_JOB)
                        
                        concurrent_jobs += ',' + TOP_JOB + ":" + nextTopBuildNumber
                        build job: TOP_JOB, parameters: [string(name: 'device_sn', value: params.sn ), string(name: 'gap', value: '1'), string(name: 'num', value: '60'), string(name: 'max_num', value: '10'), string(name: 'save_file', value: SAVE_FILE_PATH + TOP_FILE)], wait: false
                    }
                    
                    if(testItem != null && testItem.contains(TEST_CASE_MEM)){
                        echo 'memory info'
                        def nextMemBuildNumber = getJobNumberAndWait(MEM_INFO_JOB)
                        
                        concurrent_jobs += ',' + MEM_INFO_JOB + ":" + nextMemBuildNumber
                        build job: MEM_INFO_JOB, parameters: [string(name: 'num', value: '60'), string(name: 'gap', value: '2'), string(name: 'save_file', value: SAVE_FILE_PATH + MEM_FILE), string(name: 'device_sn', value: '106D121903006381')], wait: false
                    }
                    
                    if(testItem != null && testItem.contains(TEST_CASE_NXCARSERVICE)){
                        echo 'nxCarService TestCase'
                        build job: NXCARSERVICE_JOB, parameters: [string(name: 'device_id', value: params.sn), string(name: 'save_file', value: SAVE_FILE_PATH)]
                    }
                    
                    //wait all jobs done
                    echo "concurrent_jobs=${concurrent_jobs}"
                    def starttime = System.currentTimeSeconds()
                    sleep(10)
                    
                    //等待所有并行任务完成后，制作测试概要、统计分析
                    String jobString = concurrent_jobs;
                    String[] jobs = jobString.split(',');
        
                    def allJobsDone = true
                    
                    while (true){
                        allJobsDone = true;
                        def time = System.currentTimeSeconds() - starttime
                        println "------------------------------------------- wait all job complete ("+ time +" s) -------------------------------------------"
                        
                        for( String job : jobs){
                            println "job=" + job
                            if(!job.contains("NULL")){
                                def job_name = job.split(':')[0];
                                def job_id = job.split(':')[1];
                                
                                def item = Jenkins.instance.getItem(job_name)
                                def alljobs = item.getAllJobs()
                                
                                if(alljobs == null || alljobs.isEmpty()){
                                    println "get job fail ,name=" + item.getName()
                                    continue
                                }
                                
                                println item.getName() + ",alljob size=" + alljobs.size()
                                def count = 0;
                                for(Job jenkins_job : alljobs){
                                    count++
                                    
                                    def build = jenkins_job.getBuildByNumber(Integer.parseInt(job_id));
                                    
                                    println "jenkins_job=" + jenkins_job + "isInQueue=" + jenkins_job.isInQueue() + ",build=" + build
                                    
                                    if(build != null){
                                        
                                        def result = build.getResult();
                                        println "isBuilding=" + build.isBuilding() + ",result=" + result;
                                        
                                        if(build.isBuilding()){//如果在运行中请等待
                                            allJobsDone = false
                                        }
                                        
                                    }else{//如果队列中有任务，请等待
                                        allJobsDone = false
                                    }
                                    
                                    
                                    if(count > 0){//only one
                                        break;
                                    }
                                    
                                }
                            }
                        }
                        
                        if(allJobsDone){
                            break
                        }else{
                            //10s
                            sleep(10)
                        }
                    }
                    
                }
                
                //end
                sendPiplineStausToZen(ZEN_URL,params.sn,"Pipline",params.key,"Stage","Test",STATUS_PIPLINE_END,"",System.currentTimeMillis());
            }
        }
        stage('Delivery') {
            steps{
                echo 'Delivery step'
                sendPiplineStausToZen(ZEN_URL,params.sn,"Pipline",params.key,"Stage","Delivery",STATUS_PIPLINE_START,System.currentTimeMillis(),"");
                
                //end
                sendPiplineStausToZen(ZEN_URL,params.sn,"Pipline",params.key,"Stage","Delivery",STATUS_PIPLINE_END,"",System.currentTimeMillis());
            }
        }
    }
    
     post {
        always {
            echo '构建结束...'
            sendPiplineStausToZen(ZEN_URL,params.sn,"Pipline",params.key,"Post","always",STATUS_PIPLINE_START,System.currentTimeMillis(),"");
        }
        success {
            echo '恭喜您，构建成功！！！'
            sendPiplineStausToZen(ZEN_URL,params.sn,"Pipline",params.key,"Post","success",STATUS_PIPLINE_START,System.currentTimeMillis(),"");
        }
        
        failure {
            echo '抱歉，构建失败！！！'
            sendPiplineStausToZen(ZEN_URL,params.sn,"Pipline",params.key,"Post","failure",STATUS_PIPLINE_START,System.currentTimeMillis(),"");
        }
        
        unstable {
            echo '该任务已经被标记为不稳定任务....'
            sendPiplineStausToZen(ZEN_URL,params.sn,"Pipline",params.key,"Post","unstable",STATUS_PIPLINE_START,System.currentTimeMillis(),"");
        }
         
     }
}


def sleepRandom(def base,def rand){
    Random ra =new Random();
    def sec = ra.nextInt(rand) + base
    println "sleepRandom sec=" + sec
    
    sleep(sec)
}


def execShell(def shell){
    InputStream is = null;
    InputStream err = null;
    
    try {
        Process pro = Runtime.getRuntime().exec(shell);
        //Process pro = 'adb devices'.execute()
        pro.waitFor();


        is = pro.getInputStream();
        err = pro.getErrorStream();


        BufferedReader read = new BufferedReader(new InputStreamReader(is));
        BufferedReader errRead = new BufferedReader(new InputStreamReader(err));
        
        List<String> lines = read.readLines();
        List<String> errLines = errRead.readLines();
        
        println "shell=" + shell + "pro=" + pro + ",is=" + is + ",text=" + lines.toString() + ",err=" + errLines.toString()
        
        if(lines != null){
            return lines.toString()
        }
        
        return "null"
    } catch (Exception e) {
        println "execShell Exception=" + e.getMessage()
        e.printStackTrace();
    }finally {
        if(is != null) {
            try {
                is.close()
            }catch(Exception e) {
                e.printStackTrace()
            }
        }
        
        if(err != null) {
            try {
                err.close()
            }catch(Exception e) {
                e.printStackTrace()
            }
        }
    }
}




def getJobSNAndWait(def jobName, def buildNumber,def sn){
    def item = Jenkins.instance.getItem(jobName)
    //println "getTotalExecutors=" + Jenkins.instance.getComputer().getTotalExecutors()
     
    def number = -1
    println "getJobSNAndWait=" + item.getName()
                    
    def jobs = item.getAllJobs()
  
    def nextBuildNumber = -1
    def starttime = System.currentTimeSeconds()
    
    while(true){
        //SN相同的Build，同时只能有一个执行
      
        def isNeedWait = false
        for(Job job : jobs){
            def isInQueue = job.isInQueue()
            def builds = job.getBuilds()
            def max_num = 20    
          
            if(builds != null && builds.size() > 0){
                if(builds.size() > max_num){
                  builds = builds.subList(0,max_num)
                }
            }
          
            for(def build:builds){
                def build_sn = build.getProperties().get("envVars").get("sn")
                def logs = build.getProperties().get("log")
                def isOverBuildStep = false
              
                if(logs != null){
                    isOverBuildStep =  logs.contains("Build step")
                }
              
                //数据类型要保持一致
                if((build.number != buildNumber) && build.isBuilding() && (build_sn == sn) && isOverBuildStep){
    
                    isNeedWait = true
                    println "wait job buildNumber=" + buildNumber + ",build_sn=" + build_sn
                    break
                }
              
                if(build.isBuilding()){
                    println "build=" + build + ",isBuilding=" + build.isBuilding() + ",number=" + build.number + ",build_sn=" + build_sn + ",sn=" + sn + ",isOverBuildStep=" + isOverBuildStep + ",isNeedWait=" + isNeedWait
                }
            }
          
        }
        
        if(isNeedWait){
            Random ra =new Random();
            def sec = ra.nextInt(10) + 10
            println "sec=" + sec  
    
            sleep(sec)
            
            def time = System.currentTimeSeconds() - starttime
            println "=========================== wait (" + time + ")s ,because of the same sn ==========================="
            continue
        }
        
        break;
    }
    
    return number
}


def getJobNumberAndWait(def jobName){
    def item = Jenkins.instance.getItem(jobName)
    def number = -1
    println "getJobNumberAndWait=" + item.getName()
                    
    def jobs = item.getAllJobs()
    def nextBuildNumber = -1
    def starttime = System.currentTimeSeconds()
    
    while(true){
        //获取下一个build number，如果有任务在队列中，不会计算，获取的时已经运行的下一个编译号，可能时队列中的任务
        //如果队列中有任务，请等待
        def isNeedWait = false
        for(Job job : jobs){
            def isInQueue = job.isInQueue()
            
            println "job=" + job.getName() + ",isInQueue=" + isInQueue + ", nextBuildNumber=" + job.nextBuildNumber
            
            if(isInQueue){
                isNeedWait = true
                break
            }
            
            number = job.nextBuildNumber
            println "Finally number=" + job.nextBuildNumber
        }
        
        if(isNeedWait){
            sleep(5)
            
            def time = System.currentTimeSeconds() - starttime
            println "=========================== wait (" + time + ")s ,because of job inQueue ==========================="
           
            continue
        }
        
        break;
    }
    
    return number
}


def sendPiplineStausToZen(def url,def sn,def title,def key,def type,def value,def status,def startdata,def enddata){
    //{title:"Pipline",key:"",stage:"",status:"",startdata:"" ,enddate:""}
    def data = "%7B%22title%22:%22" + title + "%22,%22key%22:%22"+ key +"%22,%22" + type +"%22:%22" + value + "%22,%22status%22:%22" + status+ "%22,%22startdata%22:%22"+ startdata +"%22,%22enddate%22:%22" + enddata + "%22%7D"


    println "sendPiplineStausToZen data=" + data
    return doHttpGet(url, sn, data)
}


//HTTP GET请求，上报状态给Zen
def doHttpGet(String httpurl, String sn, String json) {


    if(sn != null){
        httpurl += "?sn=" + sn;
    }else {
        httpurl += "?sn=" + "";
    }


    if(json != null){
        httpurl += "&json=" + json
    }


    //println "doHttpGet sn=" + sn + ",json=" + json + ",httpurl=" + httpurl


    HttpURLConnection connection = null;
    InputStream is = null;
    BufferedReader br = null;
    String result = null;// 返回结果字符串
    try {
        // 创建远程url连接对象
        URL url = new URL(httpurl);
        // 通过远程url连接对象打开一个连接，强转成httpURLConnection类
        connection = (HttpURLConnection) url.openConnection();
        // 设置连接方式：get
        connection.setRequestMethod("GET");
        // 设置连接主机服务器的超时时间：15000毫秒
        connection.setConnectTimeout(15000);
        // 设置读取远程返回的数据时间：60000毫秒
        connection.setReadTimeout(60000);
        connection.setDoOutput(true)
        // 发送请求
        connection.connect();


        // 通过connection连接，获取输入流
        if (connection.getResponseCode() == 200) {
            is = connection.getInputStream();


            // 封装输入流is，并指定字符集
            br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            // 存放数据
            StringBuffer sbf = new StringBuffer();
            String temp = null;
            while ((temp = br.readLine()) != null) {
                sbf.append(temp);
                sbf.append("\r\n");
            }
            result = sbf.toString();
        }
    } catch (MalformedURLException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    } finally {
        // 关闭资源
        if (null != br) {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        if (null != is) {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        if(connection != null){
            connection.disconnect();// 关闭远程连接
        }
    }


    return result;
}