# Quick Start

## 1. 下载最新的jar包

前往release页面中找到最新的jar，当前最新版本为`java-sql-cli-1.0.jar`

## 2. 创建配置文件

在`java-sql-cli-{version}.jar`的同一目录下，创建一个名为`application.properties`的文件

```shell
touch application.properties
```

在此文件中键入相关配置（花括号中的配置项需要改成自己的），下面分别是oracle和mysql的配置文件示例

```properties
# oracle
jdbc.driver=oracle.jdbc.OracleDriver
jdbc.url=jdbc:oracle:thin:@{db_ip}:{port}/{scheme}
jdbc.username={username}
jdbc.password={password}
outputfile=output.log
```

```properties
# mysql
jdbc.driver=com.mysql.cj.jdbc.Driver
jdbc.url=jdbc:mysql://{db_ip}:{port}/{databases}
jdbc.username={username}
jdbc.password={password}
outputfile=output.log
```



## 3. 启动

```shell
java -jar java-sql-cli-{version}.jar
```



## 4. 执行sql命令

注意此步骤是在启动jar之后的交互界面中进行的

```shell
exec "select * from help_keyword limit 10"
```

默认返回的是一个表格，如下
```
+---------------------------------------------+
|                   Result                    |
|---------------------------------------------|
|help_keyword_id|            name             |
|---------------------------------------------|
|      670      |            (JSON            |
|      475      |             ->              |
|      658      |             <>              |
|      510      |           ACCOUNT           |
|      642      |           ACTION            |
|      450      |             ADD             |
|      327      |         AES_DECRYPT         |
|      619      |         AES_ENCRYPT         |
|      497      |            AFTER            |
|      398      |           AGAINST           |
|      109      |          AGGREGATE          |
|      456      |          ALGORITHM          |
|      217      |             ALL             |
+---------------------------------------------+
```

还可以通过`—json`选项来指定返回值按照`json`的格式来输出

```shell
exec "select * from help_keyword limit 5" --json
```

返回的是一段`json`字符串，如下
```json
[
	{
		"name":"(JSON",
		"help_keyword_id":"670"
	},
	{
		"name":"->",
		"help_keyword_id":"475"
	},
	{
		"name":"<>",
		"help_keyword_id":"658"
	},
	{
		"name":"ACCOUNT",
		"help_keyword_id":"510"
	},
	{
		"name":"ACTION",
		"help_keyword_id":"642"
	}
]
```



## 5. 查询结果备份

执行过的`sql`，其结果会自动保存到`output.log`之中（文件名称可以在`application.properties`中修改）