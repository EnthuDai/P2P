import java.util.Calendar;

public class Hex {
    /**
     * 数组转换成十六进制字符串
     * @return HexString
     */
    public static String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
//            if(i<bArray.length-1)   sb.append(", ");
        }
        return sb.toString();
    }

    /**
     * 16进制字符串转byte数组，字符串数组不带0x
     * @param str 字符串数组
     * @return  字符数组
     */
    public static byte[] hexStringToBytes(String str){
        byte[] bytes = new byte[str.length()/2];
        for(int i=0;i<str.length()/2;i++){
            byte high = (byte) (Character.digit(str.charAt(i*2), 16) & 0xff);
            byte low = (byte) (Character.digit(str.charAt(i*2 + 1), 16) & 0xff);
            bytes[i] = (byte) (high << 4 | low);
        }
        return bytes;
    }

    /**
     * 大于0的int转byte数组，分大小端
     * @param a 需要转换的int
     * @param isSmallEndian 是否小端在前
     * @return
     */
    public static byte[] intToBytes(int a , boolean isSmallEndian){
        if(isSmallEndian) return intToBytes(a);
        byte[] result = new byte[4];
        //低位至高位
        result[0] = (byte) ((a >> 24) & 0xFF);
        result[1] = (byte) ((a >> 16) & 0xFF);
        result[2] = (byte) ((a >> 8) & 0xFF);
        result[3] = (byte) (a & 0xFF);
        return result;
    }

    public static byte[] longToBytes(long a){
        byte[] result = new byte[8];
        //低位至高位
        result[0] = (byte) ((a >> 56) & 0xFF);
        result[1] = (byte) ((a >> 48) & 0xFF);
        result[2] = (byte) ((a >> 40) & 0xFF);
        result[3] = (byte) ((a >> 32) & 0xFF);
        result[4] = (byte) ((a >> 24) & 0xFF);
        result[5] = (byte) ((a >> 16) & 0xFF);
        result[6] = (byte) ((a >> 8) & 0xFF);
        result[7] = (byte) (a & 0xFF);
        return result;
    }


    /**
     * int到byte[] 小端在前
     * @param i
     * @return
     */
    public static byte[] intToBytes(int i) {
        byte[] result = new byte[4];
        //低位至高位
        result[3] = (byte) ((i >> 24) & 0xFF);
        result[2] = (byte) ((i >> 16) & 0xFF);
        result[1] = (byte) ((i >> 8) & 0xFF);
        result[0] = (byte) (i & 0xFF);
        return result;
    }


    /**
     * byte数组转int，数组长度不能大于4
     * @param bytes byte数组
     * @param beginIndex 从byte数组的哪一位开始
     * @param length 需要计算的数组长度
     * @param isSmallEndian 是否小端在前
     * @return Int
     */
    public static long bytesToLong(byte[] bytes,int beginIndex, int length, boolean isSmallEndian){
        length = length<8?length:8;         //如果长度超出4，则按4计算
        long value = 0;
        if(isSmallEndian){
            for(int i=beginIndex+length-1;i>=beginIndex;i--){
                value = (value << 8)|(bytes[i]&0xff);
            }
            return value;
        }
        else{
            for(int i=beginIndex;i<length+beginIndex;i++){
                value = (value << 8)|(bytes[i]&0xff);
            }
            return value;
        }
    }


    /**
     * byte数组转int，数组长度不能大于4
     * @param bytes byte数组
     * @param beginIndex 从byte数组的哪一位开始
     * @param length 需要计算的数组长度
     * @param isSmallEndian 是否小端在前
     * @return Int
     */
    public static int bytesToInt(byte[] bytes,int beginIndex, int length, boolean isSmallEndian){
        length = length<4?length:4;         //如果长度超出4，则按4计算
        int value = 0;
        if(isSmallEndian){
            for(int i=beginIndex+length-1;i>=beginIndex;i--){
                value = (value << 8)|(bytes[i]&0xff);
            }
            return value;
        }
        else{
            for(int i=beginIndex;i<length+beginIndex;i++){
                value = (value << 8)|(bytes[i]&0xff);
            }
            return value;
        }
    }

    /**
     * 累加校验，取低八位
     * @param bytes 源字节数组
     * @param beginIndex 从第几位开始
     * @param length    计算长度
     * @return 累加值的低八位
     */
    public static byte generateSumCheckBit(byte[] bytes, int beginIndex, int length){
        int sum = 0;
        for(int i=beginIndex; i<beginIndex+length;i++){
            sum += bytes[i]&0xFF;
        }
        return (byte)sum;
    }

    /**
     * 将两个byte数组进行或运算返回结果
     * @param bytes1 数组一
     * @param bytes2 数组二
     * @return 结果，长度与数组一 一致
     */
    public static byte[] ORArray(byte[] bytes1, byte[] bytes2){
        byte[] result = new byte[bytes1.length];
        for(int i=0;i<Math.min(bytes1.length, bytes2.length);i++){
            result[i] = (byte) (bytes1[i]|bytes2[i]);
        }
        return result;
    }

    public static byte[] timeArray(){
        byte[] time = new byte[7];
        Calendar calendar = Calendar.getInstance();
        time[0] = (byte)(calendar.get(Calendar.YEAR) / 100);//设置时间
        time[1] = (byte)(calendar.get(Calendar.YEAR) % 100);
        time[2] = (byte)(calendar.get(Calendar.MONTH) + 1);
        time[3] = (byte)(calendar.get(Calendar.DAY_OF_MONTH));
        time[4] = (byte)(calendar.get(Calendar.HOUR_OF_DAY));
        time[5] = (byte)(calendar.get(Calendar.MINUTE));
        time[6] = (byte)(calendar.get(Calendar.SECOND));
        return time;
    }

}
