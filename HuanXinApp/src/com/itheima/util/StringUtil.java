package com.itheima.util;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * �ַ�����
 * @author hhb
 * @version 1.0
 */
public class StringUtil {
    private final static String[] hexDigits = {"0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    /**
     * ת���ֽ�����Ϊ16�����ַ�
     *
     * @param b �ֽ�����
     * @return 16�����ַ�
     */
    private static String byteArrayToHexString(byte[] bty) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < bty.length; i++) {
        	stringBuffer.append(byteToHexString(bty[i]));
        }

        return stringBuffer.toString();
    }

    /**
     * ת���ֽ���Ϊ16�����ַ�
     *
     * @param b byte��ֵ
     * @return 16�����ַ�
     */
    private static String byteToHexString(byte byt) {
        int now = byt;
        if (now < 0) {
            now = 256 + now;
        }
        int do1 = now / 16;
        int do2 = now % 16;

        return hexDigits[do1] + hexDigits[do2];
    }

    /**
     * ��ȡָ���ַ��MD5����
     * @param original �ַ�
     * @return MD5����
     */
    public static String MD5Encode(String original) {
        String ret = null;

        try {
            ret = original;
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            ret = byteArrayToHexString(messageDigest.digest(ret.getBytes()));
        } catch (Exception ex) {
        	ex.printStackTrace();
        }

        return ret;
    }

    /**
     * ���0-9��������ַ�
     *
     * @param length �����ַ�ĳ���
     * @return String
     */
    public static String getRandomNumber(int length) {
        Random random = new Random();
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < length; i++) {
            buffer.append(random.nextInt(10));
        }
        return buffer.toString();
    }

    /**
     * ���0-9,a-z,A-Z��Χ������ַ�
     *
     * @param length �ַ���
     * @return String
     */
    public static String getRandomChar(int length) {
        char[] chr = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
                'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y',
                'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K',
                'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
                'X', 'Y', 'Z'};

        Random random = new Random();
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < length; i++) {
            buffer.append(chr[random.nextInt(62)]);
        }

        return buffer.toString();
    }

    /**
     * �ж��ַ��������Ƿ��ĳ�ַ�
     *
     * @param substring ĳ�ַ�
     * @param source    Դ�ַ�����
     * @return ���򷵻�true�����򷵻�false
     */
    public static boolean isContains(String substring, String[] source) {
        if (source == null || source.length == 0) {
            return false;
        }

        for (int i = 0; i < source.length; i++) {
            String aSource = source[i];
            if (aSource.equals(substring)) {
                return true;
            }
        }

        return false;
    }

    /**
     * �ж��ַ��Ƿ�Ϊ��
     *
     * @param str ĳ�ַ�
     * @return Ϊnull��Ϊ�մ��򷵻�true�����򷵻�false
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
    
    /**
     * �ж��ַ��Ƿ�Ϊ��
     *
     * @param str ĳ�ַ�
     * @return Ϊnull��Ϊ�մ��򷵻�false�����򷵻�true
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * ����ĸ��д
     *
     * @param str �ַ�
     * @return ���ַ��д����ַ�
     */
    public static String upFirstChar(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * ����ĸСд
     *
     * @param str �ַ�
     * @return ���ַ��д����ַ�
     */
    public static String lowerFirstChar(String str) {
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }
    
    /**
     * �ַ�����ת���б�
     * @param arr
     * @return
     */
    public static List<String> StringsToList(String [] arr){
    	List<String> strList = null;
    	if(null == arr){
    		return strList;
    	}
    	strList = new ArrayList<String>();
    	for(int i = 0;i < arr.length;i++){
    		strList.add(arr[i]);
    	}
    	return strList;
    }
    
    /**
     * inputstream������string
     * @param input
     * @return
     */
    public static String inputStream2String(InputStream input) {
		try{
			StringBuffer out = new StringBuffer();
			byte[] byt = new byte[4096];
			for (int n; (n = input.read(byt)) != -1;) {
				out.append(new String(byt, 0, n));
			}
			return out.toString();
		}catch(Exception e){
			e.printStackTrace();
		}
		return "";
	}
}

