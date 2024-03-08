- # airflow  

- ### https://github.com/apache/airflow  
- ### https://airflow.apache.org/

- ## 目录 

- [环境搭建](#环境搭建)
  - [本地安装](#本地安装)
  - [docker安装](#docker安装)
  - [配置](#配置)
  - [测试](#测试)
- [学习](#学习)
  - [Airflow术语](#airflow术语)
  - [vscode 插件](#vscode-插件)
  - [参数传递](#参数传递)
  - [Airflow自定义插件](#airflow自定义插件)
    - [插件分类](#插件分类)
    - [插件开发示例](#插件开发示例)
  - [ssh连接](#ssh连接)
- [疑问及拓展](#疑问及拓展)
  - [airflow使用场景](#airflow使用场景)
  - [vscode调试中断](#vscode调试中断)
  - [mysql操作](#mysql操作)
  - [redis操作](#redis操作)
  - [dag之间如何调用和传递参数](#dag之间如何调用和传递参数)
  - [vscode如何调试插件，比如内置插件BashOperator](#vscode如何调试插件比如内置插件bashoperator)


## 环境搭建
### 本地安装

```sh
sudo apt install python3-pip

# 安装目录
export AIRFLOW_HOME=~/airflow

# pip3 install apache-airflow

# 启动
airflow standalone

# 密码查看控制台日志
Login with username: admin  password: EgDhaF8nRetaQ3CH

# UI访问
http://localhost:8080  
```

启动日志:
```sh
standalone | Starting Airflow Standalone
standalone | Checking database is initialized
INFO  [alembic.runtime.migration] Context impl SQLiteImpl.
INFO  [alembic.runtime.migration] Will assume non-transactional DDL.
INFO  [alembic.runtime.migration] Running stamp_revision  -> 405de8318b3a
WARNI [airflow.models.crypto] empty cryptography key - values will not be stored encrypted.
standalone | Database ready
/usr/local/lib/python3.8/dist-packages/flask_limiter/extension.py:336 UserWarning: Using the in-memory storage for tracking rate limits as no storage was explicitly specified. This is not recommended for production use. See: https://flask-limiter.readthedocs.io#configuring-a-storage-backend for documentation about configuring the storage backend.
WARNI [airflow.www.fab_security.manager] No user yet created, use flask fab command to do it.
standalone | Creating admin user
standalone | Created admin user
 webserver | [2023-09-28T09:34:24.366+0000] {configuration.py:2066} INFO - Creating new FAB webserver config file in: /root/airflow/webserver_config.py
 triggerer | ____________       _____________
 triggerer | ____    |__( )_________  __/__  /________      __
 triggerer | ____  /| |_  /__  ___/_  /_ __  /_  __ \_ | /| / /
 triggerer | ___  ___ |  / _  /   _  __/ _  / / /_/ /_ |/ |/ /
 triggerer | _/_/  |_/_/  /_/    /_/    /_/  \____/____/|__/
 triggerer | [2023-09-28T09:34:26.025+0000] {triggerer_job_runner.py:171} INFO - Setting up TriggererHandlerWrapper with handler <FileTaskHandler (NOTSET)>
 triggerer | [2023-09-28T09:34:26.028+0000] {triggerer_job_runner.py:227} INFO - Setting up logging queue listener with handlers [<RedirectStdHandler <stdout> (NOTSET)>, <TriggererHandlerWrapper (NOTSET)>]
 triggerer | [2023-09-28T09:34:26.095+0000] {triggerer_job_runner.py:324} INFO - Starting the triggerer
 scheduler | ____________       _____________
 scheduler | ____    |__( )_________  __/__  /________      __
 scheduler | ____  /| |_  /__  ___/_  /_ __  /_  __ \_ | /| / /
 scheduler | ___  ___ |  / _  /   _  __/ _  / / /_/ /_ |/ |/ /
 scheduler | _/_/  |_/_/  /_/    /_/    /_/  \____/____/|__/
 scheduler | [2023-09-28T09:34:26.104+0000] {executor_loader.py:117} INFO - Loaded executor: SequentialExecutor
 triggerer | [2023-09-28 09:34:26 +0000] [3784] [INFO] Starting gunicorn 21.2.0
 triggerer | [2023-09-28 09:34:26 +0000] [3784] [INFO] Listening at: http://[::]:8794 (3784)
 triggerer | [2023-09-28 09:34:26 +0000] [3784] [INFO] Using worker: sync
 triggerer | [2023-09-28 09:34:26 +0000] [3787] [INFO] Booting worker with pid: 3787
 scheduler | [2023-09-28 09:34:26 +0000] [3786] [INFO] Starting gunicorn 21.2.0
 scheduler | [2023-09-28 09:34:26 +0000] [3786] [INFO] Listening at: http://[::]:8793 (3786)
 scheduler | [2023-09-28 09:34:26 +0000] [3786] [INFO] Using worker: sync
 triggerer | [2023-09-28 09:34:26 +0000] [3788] [INFO] Booting worker with pid: 3788
 scheduler | [2023-09-28 09:34:26 +0000] [3789] [INFO] Booting worker with pid: 3789
 scheduler | [2023-09-28 09:34:26 +0000] [3790] [INFO] Booting worker with pid: 3790
```

### docker安装
https://www.youtube.com/watch?v=aTaytcxy2Ck  

https://airflow.apache.org/docs/apache-airflow/stable/howto/docker-compose/index.html


```sh
curl -LfO 'https://airflow.apache.org/docs/apache-airflow/2.7.1/docker-compose.yaml'

# 创建目录:
容器的程序目录: `/opt/airflow/dags ...`

# 安装
docker-compose up  
```

访问:`http://localhost:8080/`  默认用户名及密码:`airflow`  

 `curl -X GET --user "airflow:airflow" "http://localhost:8080/api/v1/dags"`  能够访问api  

### 配置

配置文件:`$AIRFLOW_HOME/airflow.cfg`   

```sh
[core]
dags_folder = /root/airflow/dags
hostname_callable = airflow.utils.net.getfqdn
might_contain_dag_callable = airflow.utils.file.might_contain_dag_via_default_heuristic
default_timezone = utc
executor = SequentialExecutor
auth_manager = airflow.auth.managers.fab.fab_auth_manager.FabAuthManager
parallelism = 32
max_active_tasks_per_dag = 16
dags_are_paused_at_creation = True
max_active_runs_per_dag = 16
```

### 测试

```sh
airflow tasks test example_bash_operator runme_0 2015-01-01
```

## 学习
### Airflow术语

- `DAG`是Directed Acyclic Graph有向无环图的简称，描述其描述数据流的计算过程。  
- `Operators`描述DAG中一个具体task要执行的任务，可以理解为Airflow中的一系列`算子`，底层对应python class。不同的Operator实现了不同的功能，如：BashOperator为执行一条bash命令，EmailOperator用户发送邮件，HttpOperators用户发送HTTP请求，PythonOperator用于调用任意的Python函数。  
- `Task`是Operator的一个实例，也就是DAG中的一个节点，在某个Operator的基础上指定具体的参数或者内容就形成一个Task，DAG中包含一个或者多个Task。  
- `Task Instance`task每一次运行对应一个Task Instance，Task Instance有自己的状态，例如：running,success,failed,skipped等。  
- `Task Relationships`：一个DAG中可以有很多task，这些task执行可以有依赖关系，例如：task1执行后再执行task2，表明task2依赖于task1，这就是task之间的依赖关系。  

### vscode 插件

需要打开`API Authentication`  https://airflow.apache.org/docs/apache-airflow/stable/security/api.html  

```sh
[api]
auth_backends = airflow.api.auth.backend.session

# 替换
sed "s/auth_backends ="
sed -i "s/auth_backends =.*/auth_backends = airflow.api.auth.backend.basic_auth/g" ~/airflow/airflow.cfg

$ airflow config get-value api auth_backends
airflow.api.auth.backend.basic_auth
```

` curl -X GET --user "admin:admin" "http://localhost:8080/api/v1/dags"`  

查看授权是否成功.


vscode 连接docker容器`airflow-airflow-webserver-1`:


安装插件
- `Airflow`  Apache Airflow UI Extension to List/Trigger DAGs, View Logs and much more  
- `Airflow Snippets`  Apache Airflow snippets for VSCode  

https://marketplace.visualstudio.com/items?itemName=NecatiARSLAN.airflow-vscode-extension  


vscode 打开目录`/opt/airflow`   
查看dags目录: `dags_folder = /opt/airflow/dags`  

把自己写的dag放到dags的目录，web界面会自动刷新  
`my_bash_operator.py`
```py
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
"""Example DAG demonstrating the usage of the BashOperator."""
from __future__ import annotations

import datetime

import pendulum

from airflow import DAG
from airflow.operators.bash import BashOperator
from airflow.operators.empty import EmptyOperator

with DAG(
    dag_id="my_bash_operator",
    # schedule="0 0 * * *",
    start_date=pendulum.datetime(2021, 1, 1, tz="UTC"),
    catchup=False,
    dagrun_timeout=datetime.timedelta(minutes=60),
    tags=["example", "example2"],
    params={"example_key": "example_value"},
) as dag:
    run_this_last = EmptyOperator(
        task_id="run_this_last",
    )

    # [START howto_operator_bash]
    run_this = BashOperator(
        task_id="run_after_loop",
        bash_command="echo {{ params.example_key }}",
    )
    # [END howto_operator_bash]

    run_this >> run_this_last

    for i in range(3):
        task = BashOperator(
            task_id="runme_" + str(i),
            bash_command='echo "{{ task_instance_key_str }}" && sleep 1',
        )
        task >> run_this

    # [START howto_operator_bash_template]
    also_run_this = BashOperator(
        task_id="also_run_this",
        bash_command='echo "ti_key={{ task_instance_key_str }}"',
    )
    # [END howto_operator_bash_template]
    also_run_this >> run_this_last

# [START howto_operator_bash_skip]
this_will_skip = BashOperator(
    task_id="this_will_skip",
    bash_command='echo "hello world"; exit 99;',
    dag=dag,
)
# [END howto_operator_bash_skip]
this_will_skip >> run_this_last

if __name__ == "__main__":
    dag.test()

```

输入的参数:  


![[../resources/images/devops/airflow-param1.png]]  



依赖关系:


![[../resources/images/devops/airflow-param2.png]]  



日志


![[../resources/images/devops/airflow-param3.png]]  



启动指令:
```sh
 airflow dags test my_bash_operator --conf '{"example_key": "my_test_example_value"}' 2023-09-10

 # 或者启动某个task
 airflow tasks test my_bash_operator run_this_last 2023-09-10
```

```json
{
    "version": "0.2.0",
    "configurations": [
        {
            "name": "Airflow Test",
            "type": "python",
            "request": "launch",
            "program": "airflow",
            "console": "integratedTerminal",
            "args": [
                "dags",
                "my_bash_operator",
                "--conf",
                "{\"example_key\": \"my_test_example_value\"}"
            ],
            "env": {
                "AIRFLOW_HOME": "/opt/airflow"
            }
        }
    ]
}
```


### 参数传递

在 Apache Airflow 中，通过界面传递参数可以使用 Airflow 提供的 `变量`（Variables）和`连接`（Connections）功能，以及 DAG 运行时的`配置`（Conf）来实现。下面是一些具体的方法：

- ### 1. **使用 Airflow 变量**

Airflow 变量是一种存储静态值的方法，这些值可以在 DAG 运行时被读取。您可以在 Airflow Web UI 中的 `Admin` → `Variables` 部分手动设置变量，也可以通过 Airflow CLI 设置。



![[../resources/images/devops/airflow-variable.png]]  



- #### 在 Web UI 设置变量：

1. 进入 Airflow Web UI。
2. 在顶部导航栏选择 `Admin` → `Variables`。
3. 点击 `Create` 按钮创建新变量。
4. 填写 `Key` 和 `Val` 字段，然后点击 `Save` 保存变量。

- #### 在 DAG 中使用变量：

```python
from airflow.models import Variable

# 获取变量值
variable_value = Variable.get("your_variable_key", default_var="default_value")
```

- ### 2. **使用 DAG 运行时配置**

当您手动触发 DAG 时，可以通过 `配置`（Conf）传递 JSON 格式的参数。

- #### 触发 DAG 时传递参数：

1. 在 Airflow Web UI，找到您的 DAG 并点击 DAG 名称。
2. 点击 `Trigger DAG` 按钮。
3. 在弹出的窗口中，您可以看到一个 `Conf` 字段，您可以在这里输入 JSON 格式的参数，例如：`{"param_name": "param_value"}`。
4. 点击 `Trigger` 按钮运行 DAG。

- #### 在 DAG 中接收参数：

```python
from airflow.models import DAG
from airflow.operators.python_operator import PythonOperator
from datetime import datetime

def my_function(**kwargs):
    # 从 kwargs 中获取配置
    params = kwargs.get('params')
    param_name = params.get('param_name')

    # 执行您的逻辑
    print(param_name)

default_args = {
    'owner': 'airflow',
    'start_date': datetime(2023, 9, 28),
}

dag = DAG(
    'my_dag',
    default_args=default_args,
    schedule_interval=None,
)

task = PythonOperator(
    task_id='my_task',
    python_callable=my_function,
    params={"param_name": "default_value"}, # 设置默认值
    provide_context=True,
    dag=dag,
)
```

- ### 3. **使用 Airflow 连接**

Airflow 连接用于存储外部系统的连接信息，如数据库的 URI、用户名和密码等。虽然它们通常用于存储连接信息，但也可以用于存储其他任意类型的参数。您可以在 Web UI 的 `Admin` → `Connections` 部分创建和管理连接。

这些方法都可以实现通过 Airflow 界面传递参数给 DAG 的需求，您可以根据具体需求选择合适的方法。

### Airflow自定义插件

#### 插件分类
Airflow的插件分为Operator和Sensor两种。Operator是具体要执行的任务插件， Sensor则是条件传感器，当我需要设定某些依赖的时候可以通过不同的sensor来感知条件是否满足。  


#### 插件开发示例
在 Apache Airflow 中，插件允许你扩展内置的类以支持你的需求。你可以创建自己的操作符、钩子、传感器、接口等。

以下是开发 Airflow 插件的基本步骤：

1. **定义你的插件类**:

Airflow 使用插件管理器来加载插件，你需要定义一个插件类并使用它来告诉 Airflow 插件中包含哪些元素（例如操作符、传感器、钩子等）。

```python
from airflow.plugins_manager import AirflowPlugin

class MyAirflowPlugin(AirflowPlugin):
    name = "my_plugin"
    operators = []
    hooks = []
    executors = []
    macros = []
    admin_views = []
    flask_blueprints = []
    menu_links = []
```

2. **开发自定义操作符或其他组件**:

例如，如果你想创建一个新的操作符：

```python
from airflow.models import BaseOperator
from airflow.utils.decorators import apply_defaults

class MyCustomOperator(BaseOperator):
    @apply_defaults
    def __init__(self, my_parameter, *args, **kwargs):
        super(MyCustomOperator, self).__init__(*args, **kwargs)
        self.my_parameter = my_parameter

    def execute(self, context):
        # Your custom execution logic here
        pass
```

3. **将你的自定义组件添加到插件类**:

更新 `MyAirflowPlugin` 类，将你的自定义操作符添加到 `operators` 列表：

```python
class MyAirflowPlugin(AirflowPlugin):
    name = "my_plugin"
    operators = [MyCustomOperator]
    # ... (其他属性如前面所示)
```

4. **放置插件文件在正确的位置**:

确保你的插件 Python 文件位于 `AIRFLOW_HOME/plugins` 目录下。Airflow 将检查此目录下的所有文件，并尝试加载其中的插件。

5. **重新启动 Airflow**:

一旦你添加了新的插件或修改了现有的插件，你需要重新启动 Airflow Web Server 和 Scheduler 以加载和应用这些更改。

6. **使用你的自定义操作符**:

在你的 DAG 文件中，你现在可以像使用内置操作符一样使用你的自定义操作符。

7. **额外的集成**:

- 对于自定义的 UI 视图或页面，你可以添加 Flask 视图或蓝图。
- 如果你想扩展 Web UI，可以使用 `admin_views` 或 `flask_blueprints`。
- 对于自定义的数据库连接或其他外部系统的连接，可以使用 `hooks`。

这只是开发 Airflow 插件的一个简单指南。根据你的具体需求和要扩展的 Airflow 组件，可能还需要进行进一步的配置和开发。



### ssh连接



![[../resources/images/devops/airflow-connect.png]]  



全局连接:
如果多个 operators 需要使用到相同的 SSH 连接信息，确实可以考虑把它作为全局变量来管理。最佳的做法是设置一个 Airflow connection，然后在多个 operators 中复用这个 connection。

以下是如何操作的步骤：

1. **在 Airflow 中创建 SSH 连接**:

   在 Airflow 的 Web UI:
   - 转到 Admin > Connections。
   - 点击 "Create"。
   - 为 "Conn Id" 设置一个标识名，如 "my_ssh_conn"。
   - 选择 "Connection Type" 为 SSH。
   - 填写你的远程设备的 SSH 详细信息，例如 Hostname, Username, Password/SSH Key 等。
   - 点击 "Save"。

2. **在 DAG 中使用这个连接**:

   ```python
   from datetime import datetime
   from airflow import DAG
   from airflow.providers.ssh.operators.ssh import SSHOperator

   default_args = {
       'owner': 'me',
       'start_date': datetime(2023, 1, 1),
   }

   dag = DAG(
       'install_golang',
       default_args=default_args,
       description='Install Golang on remote machine A',
       schedule_interval=None,
   )

   install_golang = SSHOperator(
       task_id='install_golang',
       ssh_conn_id='my_ssh_conn',  # 使用你在第1步中创建的连接 ID
       command="""
       wget https://dl.google.com/go/go1.17.2.linux-amd64.tar.gz
       sudo tar -C /usr/local -xzf go1.17.2.linux-amd64.tar.gz
       echo "export PATH=$PATH:/usr/local/go/bin" >> ~/.bashrc
       source ~/.bashrc
       """,
       dag=dag,
   )

   install_golang
   ```

这样，只要你在 DAG 中使用 `ssh_conn_id='my_ssh_conn'`，就可以复用相同的 SSH 连接信息。如果未来 SSH 信息发生变化，你只需在 Airflow 的 Connections 页面更新一次即可，而无需修改任何 DAG 代码。这提供了集中管理连接的便利性和灵活性。


## 疑问及拓展
### airflow使用场景  
现在我的需求是想把工作用重复的事情使用python实现自动化，比如需要测试A，可能需要安装golang，检查系统版本及开发端口等一系列操作，使用airflow工具合适吗？  

使用 Apache Airflow 可以满足一些自动化工作流的需求，尤其是在需要`定时`、`依赖管理`和`执行日志记录`等场景。Airflow 主要用于构建 `ETL` 数据管道，但它的用途可以扩展到任何工作流，包括测试和系统管理任务。

> https://blog.csdn.net/allenhsu6/article/details/122460067  

对于您描述的场景，如果这些任务需要定期执行、有依赖关系，或者您希望有一个界面来管理和监控这些任务，那么 Airflow 是一个不错的选择。您可以为每个操作创建一个任务，如安装 golang、检查系统版本、开发端口等，然后将这些任务组织成一个 DAG。

但是，如果您的任务是一次性的、短生命周期的，或者您仅仅是想用脚本来简化手工操作，那么使用纯 Python 脚本或 Shell 脚本可能会更简单、更直接。此外，还有一些其他工具和库，如 Fabric、Ansible 等，也可以实现系统管理和自动化操作的需求。

总之，是否使用 Airflow 取决于您的具体需求。如果您需要强大的调度、依赖管理和日志记录，Airflow 是一个很好的工具；如果您的需求相对简单，那么其他更轻量级的工具和脚本可能更合适。

--- 

您完全可以使用 Apache Airflow 来管理和执行自动化的测试用例。Airflow 能够处理复杂的工作流程、任务依赖关系和调度，非常适合自动化测试场景。

下面是一些使用 Airflow 管理自动化测试用例的步骤：

- ### 1. **定义测试任务**

将每个测试用例或测试套件定义为 Airflow 中的一个任务。您可以使用 `PythonOperator` 来运行 Python 测试脚本，或者使用 `BashOperator` 来运行 Shell 脚本。

例如，如果您有一个 Python 测试脚本，可以这样定义任务：

```python
from airflow import DAG
from airflow.operators.python_operator import PythonOperator
from datetime import datetime

def run_test(**kwargs):
    # 在这里执行您的测试用例或调用测试脚本
    pass

dag = DAG(
    'test_dag',
    start_date=datetime(2023, 9, 29),
    schedule_interval=None  # 此处为手动触发，您也可以设置定时调度
)

test_task = PythonOperator(
    task_id='run_test',
    python_callable=run_test,
    provide_context=True,
    dag=dag,
)
```

- ### 2. **设置任务依赖**

如果测试用例之间有依赖关系，您可以在 DAG 中设置这些依赖。

```python
# 假设有另一个测试任务 test_task2
test_task2 = PythonOperator(
    task_id='run_test2',
    python_callable=run_another_test,
    provide_context=True,
    dag=dag,
)

# 设置任务依赖
test_task >> test_task2
```

- ### 3. **监控与日志**

Airflow 提供了 Web UI，您可以在其中监控任务的运行状态、查看日志、手动触发任务等。

- ### 4. **参数化测试**

如果需要参数化测试，可以通过 Airflow 的 Variables, Connections 或者传递给 DAG 的参数实现。

总之，使用 Apache Airflow 来管理和执行自动化测试用例是完全可行的，并且可以带来许多便利，特别是在测试工作流较为复杂的情况下。

### vscode调试中断  

```sh
[2023-10-01T13:45:03.717+0000] {timeout.py:68} ERROR - Process timed out, PID: 23599
Traceback (most recent call last):
  File "/home/airflow/.vscode-server/extensions/ms-python.python-2023.16.0/pythonFiles/lib/python/debugpy/_vendored/pydevd/_pydevd_bundle/pydevd_frame.py", line 988, in trace_dispatch
    self.do_wait_suspend(thread, frame, event, arg)
  File "/home/airflow/.vscode-server/extensions/ms-python.python-2023.16.0/pythonFiles/lib/python/debugpy/_vendored/pydevd/_pydevd_bundle/pydevd_frame.py", line 165, in do_wait_suspend
    self._args[0].do_wait_suspend(*args, **kwargs)
  File "/home/airflow/.vscode-server/extensions/ms-python.python-2023.16.0/pythonFiles/lib/python/debugpy/_vendored/pydevd/pydevd.py", line 2070, in do_wait_suspend
    keep_suspended = self._do_wait_suspend(thread, frame, event, arg, suspend_type, from_this_thread, frames_tracker)
  File "/home/airflow/.vscode-server/extensions/ms-python.python-2023.16.0/pythonFiles/lib/python/debugpy/_vendored/pydevd/pydevd.py", line 2106, in _do_wait_suspend
    time.sleep(0.01)
  File "/home/airflow/.local/lib/python3.8/site-packages/airflow/utils/timeout.py", line 69, in handle_timeout
    raise AirflowTaskTimeout(self.error_message)
airflow.exceptions.AirflowTaskTimeout: DagBag import timeout for /opt/airflow/dags/my_bash_operator.py after 30.0s.
Please take a look at these docs to improve your DAG import time:
* https://airflow.apache.org/docs/apache-airflow/2.7.1/best-practices.html#top-level-python-code
* https://airflow.apache.org/docs/apache-airflow/2.7.1/best-practices.html#reducing-dag-complexity, PID: 23599
[2023-10-01T13:45:03.721+0000] {dagbag.py:347} ERROR - Failed to import: /opt/airflow/dags/my_bash_operator.py
Traceback (most recent call last):
  File "/home/airflow/.local/lib/python3.8/site-packages/airflow/models/dagbag.py", line 343, in parse
    loader.exec_module(new_module)
  File "<frozen importlib._bootstrap_external>", line 843, in exec_module
  File "<frozen importlib._bootstrap>", line 219, in _call_with_frames_removed
  File "/opt/airflow/dags/my_bash_operator.py", line 43, in <module>
    run_this = BashOperator(
  File "/opt/airflow/dags/my_bash_operator.py", line 43, in <module>
    run_this = BashOperator(
  File "/home/airflow/.vscode-server/extensions/ms-python.python-2023.16.0/pythonFiles/lib/python/debugpy/_vendored/pydevd/_pydevd_bundle/pydevd_frame.py", line 988, in trace_dispatch
    self.do_wait_suspend(thread, frame, event, arg)
  File "/home/airflow/.vscode-server/extensions/ms-python.python-2023.16.0/pythonFiles/lib/python/debugpy/_vendored/pydevd/_pydevd_bundle/pydevd_frame.py", line 165, in do_wait_suspend
    self._args[0].do_wait_suspend(*args, **kwargs)
  File "/home/airflow/.vscode-server/extensions/ms-python.python-2023.16.0/pythonFiles/lib/python/debugpy/_vendored/pydevd/pydevd.py", line 2070, in do_wait_suspend
    keep_suspended = self._do_wait_suspend(thread, frame, event, arg, suspend_type, from_this_thread, frames_tracker)
  File "/home/airflow/.vscode-server/extensions/ms-python.python-2023.16.0/pythonFiles/lib/python/debugpy/_vendored/pydevd/pydevd.py", line 2106, in _do_wait_suspend
    time.sleep(0.01)
  File "/home/airflow/.local/lib/python3.8/site-packages/airflow/utils/timeout.py", line 69, in handle_timeout
    raise AirflowTaskTimeout(self.error_message)
airflow.exceptions.AirflowTaskTimeout: DagBag import timeout for /opt/airflow/dags/my_bash_operator.py after 30.0s.
Please take a look at these docs to improve your DAG import time:
* https://airflow.apache.org/docs/apache-airflow/2.7.1/best-practices.html#top-level-python-code
* https://airflow.apache.org/docs/apache-airflow/2.7.1/best-practices.html#reducing-dag-complexity, PID: 23599
[2023-10-01T13:45:04.775+0000] {dag.py:3931} INFO - dagrun id: my_bash_operator
[2023-10-01T13:45:04.793+0000] {dag.py:3947} INFO - created dagrun <DagRun my_bash_operator @ 2023-10-01T13:44:33.694960+00:00: manual__2023-10-01T13:44:33.694960+00:00, state:running, queued_at: None. externally triggered: False>
```

> 这个应该与调度器(Scheduler)的心跳机制（heartbeat）有关

### mysql操作  

> 主要是还可以操作位定时任务  

要在 Airflow 中操作 MySQL 数据库，你可以使用 `MySqlOperator` 和 `MySqlHook`。以下是一个简单的示例，演示如何使用 `MySqlOperator` 来执行一个简单的 SQL 查询：

1. **首先，确保你已经安装了 `apache-airflow-providers-mysql`**:

```bash
pip install apache-airflow-providers-mysql
```

2. **然后，设置 MySQL 连接**:

在 Airflow 的 Web UI 中：

- 转到 Admin > Connections。
- 添加一个新的连接：
  - Connection Type: MySQL
  - Host, Schema, Login, Password: 根据你的 MySQL 数据库设置填写。
  - Conn Id: 为此连接设置一个唯一的 ID，例如 `mysql_default`。

3. **创建一个 DAG 使用 `MySqlOperator`**:

```python
from datetime import datetime
from airflow import DAG
from airflow.providers.mysql.operators.mysql import MySqlOperator

default_args = {
    'owner': 'airflow',
    'start_date': datetime(2023, 1, 1),
}

dag = DAG(
    'mysql_example_dag',
    default_args=default_args,
    description='An example DAG to interact with MySQL',
    schedule_interval='@daily',
)

# A simple SQL operation
create_table = MySqlOperator(
    task_id='create_table',
    sql='''CREATE TABLE IF NOT EXISTS example_table (
        id INT AUTO_INCREMENT PRIMARY KEY,
        data VARCHAR(255) NOT NULL
    )''',
    mysql_conn_id='mysql_default',  # Use the connection ID from the Web UI
    database='your_database_name',  # Replace with your database name
    dag=dag,
)

create_table
```

在上述示例中，我们创建了一个新的表 `example_table` 在 MySQL 数据库中。你可以根据需要调整 `sql` 参数来执行任何其他的 SQL 操作。

注意：确保你的 MySQL 服务器允许来自 Airflow 执行机的连接，并确保使用的数据库用户具有适当的权限执行给定的 SQL 操作。

### redis操作  

要在 Airflow 中操作 Redis，你可以使用 `RedisHook` 和相应的操作符。但是，你首先需要确保安装了适当的 Airflow 供应商包：

```bash
pip install apache-airflow-providers-redis
```

以下是一个简单的示例，演示如何使用 Airflow 和 RedisHook 进行基本的 Redis 操作：

1. **设置 Redis 连接**:

在 Airflow 的 Web UI 中：

- 转到 Admin > Connections。
- 添加一个新的连接：
  - Connection Type: Redis
  - Host, Port, Password: 根据你的 Redis 实例设置。
  - Conn Id: 为此连接设置一个唯一的 ID，例如 `redis_default`。

2. **创建一个 DAG 来进行 Redis 操作**:

```python
from datetime import datetime
from airflow import DAG
from airflow.operators.python_operator import PythonOperator
from airflow.providers.redis.hooks.redis import RedisHook

default_args = {
    'owner': 'airflow',
    'start_date': datetime(2023, 1, 1),
}

dag = DAG(
    'redis_example_dag',
    default_args=default_args,
    description='An example DAG to interact with Redis',
    schedule_interval='@daily',
)

def redis_set_value():
    redis_hook = RedisHook(redis_conn_id='redis_default')
    redis_conn = redis_hook.get_conn()
    redis_conn.set('example_key', 'example_value')

def redis_get_value():
    redis_hook = RedisHook(redis_conn_id='redis_default')
    redis_conn = redis_hook.get_conn()
    value = redis_conn.get('example_key')
    print(value)

set_value = PythonOperator(
    task_id='set_redis_value',
    python_callable=redis_set_value,
    dag=dag,
)

get_value = PythonOperator(
    task_id='get_redis_value',
    python_callable=redis_get_value,
    dag=dag,
)

set_value >> get_value
```

在上述示例中，我们首先设置了一个键值对 (`example_key`, `example_value`) 在 Redis 中，然后我们检索并打印该值。这只是一个简单的示例，但你可以使用 `RedisHook` 的 `get_conn()` 方法获取的连接对象来执行任何其他的 Redis 操作。

注意：确保你的 Redis 服务器允许来自 Airflow 执行机的连接，并确保所使用的认证信息是正确的。

### dag之间如何调用和传递参数   
在 Apache Airflow 中，DAG 之间的调用和参数传递通常是通过以下两种主要方式实现的：

1. **使用 SubDagOperator**: 这允许你在一个 DAG 中调用另一个 DAG 作为子DAG。但需要注意的是，过度使用 SubDagOperator 可能会导致调度性能问题。

2. **使用 TriggerDagRun**: 这是一个更常用和推荐的方法，它允许你触发另一个 DAG 并在 DAG 之间传递参数。

下面是如何使用 `TriggerDagRun` 在 DAG 之间调用和传递参数的简单示例：

1. **定义一个 DAG 接收参数** (`dag_to_be_triggered.py`):

```python
from airflow import DAG
from airflow.operators.bash_operator import BashOperator
from datetime import datetime

default_args = {
    'owner': 'airflow',
    'start_date': datetime(2023, 1, 1),
}

dag = DAG(
    'dag_to_be_triggered',
    default_args=default_args,
    description='DAG to be triggered by another DAG',
    schedule_interval=None,
)

t1 = BashOperator(
    task_id='print_received_param',
    bash_command='echo "{{ dag_run.conf["example_key"] }}"',
    dag=dag,
)
```

2. **在另一个 DAG 中触发上述 DAG** (`triggering_dag.py`):

```python
from airflow import DAG
from airflow.operators.trigger_dagrun import TriggerDagRun
from datetime import datetime

default_args = {
    'owner': 'airflow',
    'start_date': datetime(2023, 1, 1),
}

dag = DAG(
    'triggering_dag',
    default_args=default_args,
    description='DAG that triggers another DAG',
    schedule_interval='@daily',
)

trigger = TriggerDagRun(
    task_id='trigger_dag',
    trigger_dag_id="dag_to_be_triggered",  
    conf={"example_key": "example_value"},  
    dag=dag,
)
```

在上述示例中，`triggering_dag` 触发了 `dag_to_be_triggered` DAG，并向它传递了一个参数 `example_key`。然后，在 `dag_to_be_triggered` DAG 中，我们通过使用 `{{ dag_run.conf["example_key"] }}` Jinja 模板获取并打印这个参数。

这种方法允许你灵活地在不同的 DAG 之间调用和传递参数，而不需要过度使用 SubDagOperator。

### vscode如何调试插件，比如内置插件BashOperator  

要在 VS Code 中调试 Airflow 插件（例如内置插件 `BashOperator`），你可以使用以下步骤：

1. **设置虚拟环境**:
   为了保证你的开发环境与生产环境隔离，建议在虚拟环境中运行和调试 Airflow。你可以使用 `virtualenv` 或 `conda` 创建一个。

2. **安装 Airflow**:
   在你的虚拟环境中，使用 `pip` 安装 Airflow 和所需的其他依赖。

3. **克隆 Airflow 代码** (可选):
   如果你想深入调试或修改 Airflow 的内置代码（例如 `BashOperator`），你可以克隆 Apache Airflow 的 GitHub 仓库。这允许你直接在源代码上设置断点。

4. **配置 VS Code**:
   - 打开你的 Airflow 工作区或 Airflow 代码库（如果你已克隆）。
   - 在 VS Code 中，点击左侧的调试图标，然后点击 "create a launch.json file"。选择 "Python"。
   - 在 `launch.json` 中添加一个新的配置项，例如：

```json
{
    "name": "Python: Airflow DAG",
    "type": "python",
    "request": "launch",
    "program": "${workspaceFolder}/path_to_your_dag.py",
    "console": "integratedTerminal",
    "justMyCode": false
}
```

确保将 `path_to_your_dag.py` 替换为你想调试的 DAG 文件的实际路径。

5. **设置断点**:
   在 VS Code 中，打开你想调试的代码文件（例如 `BashOperator` 的源代码或你的 DAG 文件），并点击行号左侧来设置断点。

6. **运行调试器**:
   选择你在 `launch.json` 中创建的配置项，并点击运行。

7. **执行代码**:
   当代码执行到你设置的断点时，VS Code 的调试器将暂停执行，允许你查看变量、单步执行、观察调用堆栈等。

注意: 调试 Airflow 可能比其他 Python 项目复杂一些，因为它涉及到多进程执行和调度。确保你的 Airflow 设置与调试环境相匹配，并且你正在调试的是正确的进程和执行路径。  

### ETL是什么？

https://www.bilibili.com/video/BV1V341167Ci  


ETL 是 "Extract, Transform, Load" 的缩写，它描述了数据仓库的三个主要步骤：

1. **Extract (提取)**: 从多个源系统中提取数据。这些源系统可以是数据库、API、文件系统等。
2. **Transform (转换)**: 将提取出的数据进行清洗、转换和整合。这可能包括数据清洗、类型转换、应用业务逻辑、数据聚合等。
3. **Load (加载)**: 将经过转换的数据加载到目标数据仓库或其他系统中，如关系型数据库、OLAP 数据库、Hadoop 或其他大数据平台。

Apache Airflow 通常被用作 ETL 工具，因为它可以轻松地定义、组织和监视数据流程。使用 Airflow，你可以创建一个包含多个任务的 DAG (Directed Acyclic Graph)，其中每个任务可能对应 ETL 中的一个或多个步骤。

尽管 ETL 过程是 Airflow 的主要应用之一，但 Airflow 本身并不局限于 ETL。它是一个更为通用的工作流程自动化和调度工具，可以用于各种复杂的数据处理和计算工作流程。

总的来说，ETL 描述了数据处理的三个关键步骤，而 Airflow 提供了定义、执行和监控这些步骤的框架。


### 一般使用场景  
- 数据迁移、转储、备份  
- 使用自己的operator，封装api  
- 定期清理数据库、清理磁盘空间  
- 开发、测试、生产环境的分离。  
- 连接信息的统一管理。(数据库的地址、而不会在前端显示密码登)  



