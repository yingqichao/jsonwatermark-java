import java.util.List;

/**
 * @author Qichao Ying
 * @date 2019/8/29 10:47
 * @Description DEFAULT
 */
public class printer {
    public static void print(List<String> list, List<String> chinese_list){
        System.out.println("-----------提取得到的信息是------------");
        //打印提取结果
        System.out.println("The ExcelWatermarkHelper is SUCCESSFULLY retrieved "+list.size()+" time(s)！");
        for(String str:list){
            System.out.println(str);
        }

        System.out.println("----如果您发现上面的解析内容是乱码，那么也可以参考以下gbk中文解码的水印内容----");
        for(String str:chinese_list){
            System.out.println(str);
        }

    }

}
