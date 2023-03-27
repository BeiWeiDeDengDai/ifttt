package com.hhz.ifttt.utils;

/**
 * @program realtime
 * @description: obj_id转作者uid
 * @author: zhangyinghao
 * @create: 2020/08/17 16:46
 **/
public class ConvFunction {
    static char chs[] = new char[36];
    static {
        for(int i = 0; i < 10 ; i++) {
            chs[i] = (char)('0' + i);
        }
        for(int i = 10; i < chs.length; i++) {
            chs[i] = (char)('a' + (i - 10));
        }
    }

    public static String transRadix(String num, int fromRadix, int toRadix) {
        int number = Integer.valueOf(num.trim(), fromRadix);
        StringBuilder sb = new StringBuilder();
        while (number != 0) {
            sb.append(chs[number%toRadix]);
            number = number / toRadix;
        }
        return sb.reverse().toString();
    }


    public static void main(String[] args) {
        System.out.println(transRadix("q200000olk5alduol".substring(10), 36, 10));
    }
}
