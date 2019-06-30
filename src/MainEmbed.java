import java.util.*;
import java.io.*;

public class MainEmbed {
    public static void main(String[] args){
//        //Test
//        System.out.println(Util.isNumeric("12.345"));
//        System.out.println(Util.isNumeric("12345"));
//        System.out.println(Util.isNumeric("-12.345"));
//        System.out.println(Util.isInteger("12.345"));
//        System.out.println(Util.isInteger("-123345"));
//        System.out.println(Util.isInteger("-12.345"));
//        System.out.println(Util.toBinary(2,5));

        Map<String,String> JSON = Util.readFakeJSON("src/fakeJSON.txt");
        String watermark = Util.readWatermark("src/watermark.txt");
        Encoder encoder = new Encoder(watermark);
        encoder.run(JSON);


        Map<String,String> watermarkedJSON = Util.readFakeJSON("src/fakeJSON.txt");

    }



}