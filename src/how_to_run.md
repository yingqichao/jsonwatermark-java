# JSON与Excel水印算法

## JSON水印运行

### 1.将json放入test.json(或者也可以在函数中修改pathname变量)

### 2.将待嵌入的信息放入watermark.txt，注意不要有多余空行或者空格，目前只支持小写字母，建议控制在10个字符以内

### 3.运行MainEmbed.java（直接右键运行）

### 4.embedded.json 自动存入含水印JSON

### 5.运行MainExtract.java 获取水印信息（打印在控制台）

### 目前已知缺陷：

1.目前不支持最外层结构就是列表（JsonArray）的JSON作为载体，例如下面的这个例子：

[
  {
    “name”:"A",
    "cellPhone":"123"
  },
  {
      “name”:"B",
      "cellPhone":"111"
  }
]

而以下这个例子则是一个合法输入：

{
  “data”:{
    JsonObject
  
  }
}

## EXCEL水印运行

### 1.pathname填写EXCEL载体的文件名，append填写文件后缀名，注意要保留前面的.

### 2.将待嵌入的信息放入watermark.txt，注意不要有多余空行或者空格，目前只支持小写字母，建议控制在10个字符以内

### 3.运行MainEmbed_excel.java（直接右键运行）

### 4.含水印结果存在embedded_results文件夹下

### 5.运行MainExtract_excel.java 获取水印信息（打印在控制台）

### 目前已知缺陷：

1.不能抵抗整列删除的攻击（待改进）

2.对Excel载体的格式有一定的要求，第一行需要是表头，第二行开始是数据（不能是格式太随意的Excel）