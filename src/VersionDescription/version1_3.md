新增：
1.  可以规定哪些列的数据不可修改
2.  包长度不再指定在文件中

11.5
1.由于csv的每一行长度可能不一样，所以在寻找关键列的时候需要做额外判断
// CSV
for(int i=startRow;i<exclRow[0];i++)
    //每一行的长度可能不一样
    if(csvArray[i].length<colIndex)
        col.add(csvArray[i][colIndex]);
        
11.19
1.  修复xls会报错的bug
2.  JSON对于原始包的数量，从原来的隐藏在json内部改成了隐藏在自定义字段中  


12.3
1.  JSON目前也支持设定不可修改字段了，使用说明如下：
```
{
  "logData": 
  {
    "eventInfo": {
      "securemode": 1,
      "eventType": 305,
      *"detectStart": 1554090837.7057,*
      "subtype": 3,
      "reqid": 1871851855734609,
      "ipcType": 2,
      "taskid": "746",
      "home": "0",
      "type": 3,
      "createtime": 15674754090801,
      "alarmtype": 5,
      "sn": 360054825461175,
      "detectEnd": 1554090851.3645,
      "detectSpent": 13.658761024475
    },
    
    ...
```
 图1 一个JSON载体的样例    
 
 其中带*号的字段是不希望改动的，同时本身也是一个可用于修改的字段
 
```
    logData
          eventInfo
                    securemode
                    eventType
                    *detectStart*
                      
```
 图2 该JSON的树形结构
 
 这里我们看到，这个字段是位于JSON的第三层，我们将第一层的key简写为A，第二层的简写为B，以此类推，
 则我们规定对于一个三层的不希望改动字段，我们用“ABC”来标识它，也就是说，上面的detectStart应当被表示为logDataeventInfodetectStart
 
```
String[] ban = new String[]{"logDataeventInfodetectStart"};
        Set<String> banList = new HashSet<>();
        for(String str:ban)
            banList.add(str);
            ···
```
 图3 创建不可修改字段列表
 
 在ban中添加所有不希望被修改的字段，就可以在信息嵌入时避开它们