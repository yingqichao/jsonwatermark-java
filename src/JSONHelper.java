import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

public class JSONHelper {

    public static void main(String[] args) {
        jsonTest2();

//        String jsonStr = "{\\\"users\\\":[{\\\"loginname\\\":\\\"zhangfan\\\",\\\"password\\\":\\\"userpass\\\",\\\"email\\\":\\\"10371443@qq.com\\\"},{\\\"loginname\\\":\\\"zf\\\",\\\"password\\\":\\\"userpass\\\",\\\"email\\\":\\\"822393@qq.com\\\"}]}";
//        String jsonStr1 = "{\\\"users\\\":\\\"William\\\"}";
//        //json类型字符串转为jsonObject
//        JSONObject jsonObject = JSONObject.fromObject(jsonStr1);
//        //根据key值获取数据value
//        System.out.println(jsonObject.getString("users"));  //输出value1
//        System.out.println(jsonObject.getString("key2")); //输出value2
    }

//    //construct json and output it
//    public String jsonTest() throws JSONException {
//        JSONObject json=new JSONObject();
//        JSONArray jsonMembers = new JSONArray();
//        JSONObject member1 = new JSONObject();
//        member1.put("loginname", "zhangfan");
//        member1.put("password", "userpass");
//        member1.put("email","10371443@qq.com");
//        member1.put("sign_date", "2007-06-12");
//        jsonMembers.put(member1);
//
//        JSONObject member2 = new JSONObject();
//        member2.put("loginname", "zf");
//        member2.put("password", "userpass");
//        member2.put("email","8223939@qq.com");
//        member2.put("sign_date", "2008-07-16");
//        jsonMembers.put(member2);
//        json.put("users", jsonMembers);
//
//        return json.toString();
//    }
//
    //construct json from String and resolve it.
    public static String jsonTest2() throws JSONException {
        String str = "{\"loginname\":\"zhangfan\",\"password\":\"userpass\",\"email\":\"10371443@qq.com\"}";
        String jsonString="{\"users\":[{\"loginname\":\"zhangfan\",\"password\":\"userpass\",\"email\":\"10371443@qq.com\"},{\"loginname\":\"zf\",\"password\":\"userpass\",\"email\":\"822393@qq.com\"}]}";
        JSONObject json= new JSONObject();
        JSONObject jsonObject = json.getJSONObject(str);
        JSONArray jsonArray=json.getJSONArray("users");
        String loginNames="loginname list:";
//        for(int i=0;i<jsonArray.length();i++){
//            JSONObject user=(JSONObject) jsonArray.get(i);
//            String userName=(String) user.get("loginname");
//            if(i==jsonArray.length()-1){
//                loginNames+=userName;
//            }else{
//                loginNames+=userName+",";
//            }
//        }
        return loginNames;
    }

}
