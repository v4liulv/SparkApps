package com.bigdata.spark.apps.sjzl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author liulv
 * @date 2019/4/28
 * @time 19:30
 *
 * @description 数据质量规则检验核心类
 * 特别注意：一切的变量和常量数值都不能通过外部或者配置获取，因为这里为MR调研
 */
public class SjzljcGz {
    private static final Logger LOG = LoggerFactory.getLogger(SjzljcGz.class);

    /**
     * 数据质量检查规则：
     * 类型1： 非空值
     * 类型2： 数据唯一性
     * 类型3： 姓名验证
     * 类型4：身份证校验
     * 类型5：字段长度 >=◎12◎AND◎>=◎20
     * 类型6：字段值域范围 >=◎12◎AND◎>=◎20
     * 类型7： 手机号码验证
     * 类型8：车牌毫秒验证
     * 类型9：代码缺少
     * 类型10：时间范围 >=◎2019-01-01 00:00:00◎AND◎<=◎2019-05-01 00:00:00
     * 类型11：实时性验证 rksj<24
     *
     * 其他：全部为false
     *
     * 验证为问题数据返回true,正常数据返回false
     */
    public static boolean sjjc(String sjzlFieldGzlx, String sjzlFieldGzz, String fieldValue) {
        boolean result;

        switch (sjzlFieldGzlx) {
            case "1":
                result = isNUll(fieldValue);
                break;
            case "2":
                result = false;
                break;
            case "3":
                result = invalidXM(fieldValue);
                break;
            case "4":
                result = invalidSFZH(fieldValue);
                break;
            case "7":
                result = invalidPhone(fieldValue);
                break;
            case "8":
                result = invalidCph(fieldValue);
                break;
            case "9":
                result = false;
                break;
            default:
                result = false;
                break;
        }

        return result;
    }

    /**
     * 是否是空值检验
     *
     * @param fieldValue 建议的字符值
     * @return 如果为空返回true，否则返回false
     */
    private static boolean isNUll(String fieldValue) {
        return (null == fieldValue || "".equals(fieldValue.trim()));
    }

    /**
     * 检验无效的身份证号码
     *
     * @param sfzh 身份证号
     * @return 如果为无效身份证号码则返回true，否则返回false
     */
    private static boolean invalidSFZH(String sfzh) {
        //String tipInfo = "该身份证有效！";// 记录错误信息
        String Ai;
        // 判断号码的长度 15位或18位
        if (sfzh.length() != 15 && sfzh.length() != 18) {
            return true;
        }

        // 18位身份证前17位位数字，如果是15位的身份证则所有号码都为数字
        if (sfzh.length() == 18) {
            Ai = sfzh.substring(0, 17);
            //15位身份证验证
        } else {
            Ai = sfzh.substring(0, 6) + "19" + sfzh.substring(6, 15);
        }

        if (!isNumeric(Ai)) {
            return true;
        }

        // 判断出生年月是否有效
        String strYear = Ai.substring(6, 10);// 年份
        String strMonth = Ai.substring(10, 12);// 月份
        String strDay = Ai.substring(12, 14);// 日期
        if (!isDate(strYear + "-" + strMonth + "-" + strDay)) {
            return true;
        }
        GregorianCalendar gc = new GregorianCalendar();
        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
        try {
            if ((gc.get(Calendar.YEAR) - Integer.parseInt(strYear)) > 150 || (gc.getTime().getTime() - s.parse(strYear + "-" + strMonth + "-" + strDay).getTime()) < 0) {
                return true;
            }
        } catch (NumberFormatException | ParseException e) {
            e.printStackTrace();
        }
        if (Integer.parseInt(strMonth) > 12 || Integer.parseInt(strMonth) == 0) {
            return true;
        }
        if (Integer.parseInt(strDay) > 31 || Integer.parseInt(strDay) == 0) {
            return true;
        }

        // 判断地区码是否有效
        Hashtable areacode = GetAreaCode();
        //如果身份证前两位的地区码不在Hashtable，则地区码有误
        if (areacode.get(Ai.substring(0, 2)) == null) {
            return true;
        }

        //判断第18位校验码是否正确
        if (!isVarifyCode(Ai, sfzh)) {
            return true;
        }

        return false;
    }


    /**
     * 判断第18位校验码是否正确
    * 第18位校验码的计算方式：
       　　1. 对前17位数字本体码加权求和
       　　公式为：S = Sum(Ai * Wi), i = 0, ... , 16
       　　其中Ai表示第i个位置上的身份证号码数字值，Wi表示第i位置上的加权因子，其各位对应的值依次为： 7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4 2
       　　2. 用11对计算结果取模
       　　Y = mod(S, 11)
       　　3. 根据模的值得到对应的校验码
       　　对应关系为：
       　　 Y值：     0  1  2  3  4  5  6  7  8  9  10
       　　校验码： 1  0  X  9  8  7  6  5  4  3   2
    **/
    private static boolean isVarifyCode(String Ai, String IDStr) {
        String[] VarifyCode = {"1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2"};
        String[] Wi = {"7", "9", "10", "5", "8", "4", "2", "1", "6", "3", "7", "9", "10", "5", "8", "4", "2"};
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum = sum + Integer.parseInt(String.valueOf(Ai.charAt(i))) * Integer.parseInt(Wi[i]);
        }
        int modValue = sum % 11;
        String strVerifyCode = VarifyCode[modValue];
        Ai = Ai + strVerifyCode;
        if (IDStr.length() == 18) {
            if (Ai.equals(IDStr) == false) {
                return false;

            }
        }
        return true;
    }


    /**
     * 将所有地址编码保存在一个Hashtable中
     *
     * @return Hashtable 对象
     */

    private static Hashtable GetAreaCode() {
        Hashtable hashtable = new Hashtable();
        hashtable.put("11", "北京");
        hashtable.put("12", "天津");
        hashtable.put("13", "河北");
        hashtable.put("14", "山西");
        hashtable.put("15", "内蒙古");
        hashtable.put("21", "辽宁");
        hashtable.put("22", "吉林");
        hashtable.put("23", "黑龙江");
        hashtable.put("31", "上海");
        hashtable.put("32", "江苏");
        hashtable.put("33", "浙江");
        hashtable.put("34", "安徽");
        hashtable.put("35", "福建");
        hashtable.put("36", "江西");
        hashtable.put("37", "山东");
        hashtable.put("41", "河南");
        hashtable.put("42", "湖北");
        hashtable.put("43", "湖南");
        hashtable.put("44", "广东");
        hashtable.put("45", "广西");
        hashtable.put("46", "海南");
        hashtable.put("50", "重庆");
        hashtable.put("51", "四川");
        hashtable.put("52", "贵州");
        hashtable.put("53", "云南");
        hashtable.put("54", "西藏");
        hashtable.put("61", "陕西");
        hashtable.put("62", "甘肃");
        hashtable.put("63", "青海");
        hashtable.put("64", "宁夏");
        hashtable.put("65", "新疆");
        hashtable.put("71", "台湾");
        hashtable.put("81", "香港");
        hashtable.put("82", "澳门");
        hashtable.put("91", "国外");
        return hashtable;
    }

    /**
     * 判断字符串是否为数字,0-9重复0次或者多次
     *
     * @param strnum 待验证的字符串
     * @return 如果为数字返回true
     */
    private static boolean isNumeric(String strnum) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(strnum);
        return isNum.matches();
    }

    /**
     * 功能：判断字符串出生日期是否符合正则表达式：包括年月日，闰年、平年和每月31天、30天和闰月的28天或者29天
     *
     * @param strDate String
     * @return 出生日期字符串是否满足正则表达式
     */
    public static boolean isDate(String strDate) {

        Pattern pattern = Pattern.compile("^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))?$");
        Matcher m = pattern.matcher(strDate);
        return m.matches();
    }

    /**
     * 1.可以是中文
     * 2.可以是英文，允许输入点（英文名字中的那种点）， 允许输入空格
     * 3.中文和英文不能同时出现
     * 4.长度在20个字符以内
     *
     * @param name 姓名
     * @return 姓名验证正常返回false, 如果姓名验证不通过则返回true
     */
    private static Boolean invalidXM(String name) {
        LOG.warn("姓名值为:{}", name);
        if (name == null || name.trim().equals("")) return true;

        String zwZZ = "^(王|李|张|刘|陈|杨|黄|赵|吴|周|徐|孙|马|朱|胡|郭|何|高|林|罗|郑|梁|谢|宋|唐|许|韩|冯|邓|曹|彭|曾|肖|田|董|袁|潘|于|蒋|蔡|余|杜|叶|程|苏|魏|吕|丁|任|沈|姚|卢|姜|崔|钟|谭|陆|汪|范|金|石|廖|贾|夏|韦|傅|方|白|邹|孟|熊|秦|邱|江|尹|薛|闫|段|雷|侯|龙|史|黎|贺|顾|毛|郝|龚|邵|万|钱|覃|武|戴|孔|汤|庞|樊|兰|殷|施|陶|洪|翟|安|颜|倪|严|牛|温|芦|季|俞|章|鲁|葛|伍|申|尤|毕|聂|柴|焦|向|柳|邢|岳|齐|沿|梅|莫|庄|辛|管|祝|左|涂|谷|祁|时|舒|耿|牟|卜|路|詹|关|苗|凌|费|纪|靳|盛|童|欧|甄|项|曲|成|游|阳|裴|席|卫|查|屈|鲍|位|覃|霍|翁|隋|植|甘|景|薄|单|包|司|柏|宁|柯|阮|桂|闵|欧阳|解|强|丛|华|车|冉|房|边|辜|吉|饶|刁|瞿|戚|丘|古|米|池|滕|晋|苑|邬|臧|畅|宫|来|嵺|苟|全|褚|廉|简|娄|盖|符|奚|木|穆|党|燕|郎|邸|冀|谈|姬|屠|连|郜|晏|栾|郁|商|蒙|计|喻|揭|窦|迟|宇|敖|糜|鄢|冷|卓|花|艾|蓝|都|巩|稽|井|练|仲|乐|虞|卞|封|竺|冼|原|官|衣|楚|佟|栗|匡|宗|应|台|巫|鞠|僧|桑|荆|谌|银|扬|明|沙|薄|伏|岑|习|胥|保|和|蔺|水|云|昌|凤|酆|常|皮|康|元|平|萧|湛|禹|无|贝|茅|麻|危|骆|支|咎|经|裘|缪|干|宣|贲|杭|诸|钮|嵇|滑|荣|荀|羊|於|惠|家|芮|羿|储|汲|邴|松|富|乌|巴|弓|牧|隗|山|宓|蓬|郗|班|仰|秋|伊|仇|暴|钭|厉|戎|祖|束|幸|韶|蓟|印|宿|怀|蒲|鄂|索|咸|籍|赖|乔|阴|能|苍|双|闻|莘|贡|逢|扶|堵|宰|郦|雍|却|璩|濮|寿|通|扈|郏|浦|尚|农|别|阎|充|慕|茹|宦|鱼|容|易|慎|戈|庚|终|暨|居|衡|步|满|弘|国|文|寇|广|禄|阙|东|殴|殳|沃|利|蔚|越|夔|隆|师|厍|晃|勾|融|訾|阚|那|空|毋|乜|养|须|丰|巢|蒯|相|后|红|权逯|盖益|桓|公|万俟|司马|上官|夏侯|诸葛|闻人|东方|赫连|皇甫|尉迟|公羊|澹台|公冶|宗政|濮阳|淳于|单于|太叔|申屠|公孙|仲孙|轩辕|令狐|钟离|宇文|长孙|慕容|鲜于|闾丘|司徒|司空|亓官|司寇|仉|督|子车|颛孙|端木|巫马|公西|漆雕|乐正|壤驷|公良|拓跋|夹谷|宰父|谷粱|法|汝|钦|段干|百里|东郭|南门|呼延|归海|羊舌|微生|帅|缑|亢|况|郈|琴|梁丘|左丘|东门|西门|佘|佴|伯|赏|南宫|墨|哈|谯|笪|年|爱|仝|代)\\s*[\\u4e00-\\u9fa5]{1,3}";
        return !name.matches("^([\\u4e00-\\u9fa5]{1,20}|[a-zA-Z\\.\\s]{1,20})$") || !name.matches(zwZZ);
    }

    /**
     * ^1[3|4|5|8][0-9]\d{4,8}$
     ^1代表以1开头，现在中国的手机号没有是其它开头的，以后不一定啊
     [3|4|5|8] 紧跟上面的1后面，可以是3或4或5或8的一个数字，如果以后出现190开始的手机号码了，就需要如下[3|4|5|8|9]
     [0-9]表示0-9中间的任何数字，可以是0或9
     \d{4,8} 这个\d跟[0-9]意思一样，都是0-9中间的数字。
     {4,8}表示匹配前面的最低4位数字最高8位数字。
     这里为什么不是直接的8呢，因为手机号码归属地查询的时候，根据前7位就可以知道具体的地址了，后面的4位没有影响的。

     * @param phone 电话号码检测值
     * @return 如果满足调整返回false，也就是不是问题数据是有效的数据，如果不满足检测条件无效的电话号码则返回true
     */
    private static Boolean invalidPhone(String phone) {
        if (phone == null || phone.trim().equals("")) return true;

        String zwZZ = "^1[3|4|5|7|8][0-9]\\d{4,8}$";
        return !phone.matches(zwZZ);
    }

    private static Boolean invalidCph(String cph) {
        if (cph == null || cph.trim().equals("")) return true;

        String zwZZ =  "([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}(([0-9]{5}[DF])|([DF]([A-HJ-NP-Z0-9])[0-9]{4})))" +
                "|([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}[A-HJ-NP-Z0-9]{4}[A-HJ-NP-Z0-9挂学警港澳]{1})";
        return !cph.matches(zwZZ);
    }


    public static void main(String[] args) throws ParseException {

        //String IdCard="61082120061222612X";
        //从控制端输入用户身份证
        Scanner s = new Scanner(System.in);

        s.useDelimiter("\n");
        System.out.println("请输入需要验证的类型：");
        String jylx = s.next();

        System.out.println("请输入需要验证的规则值如>=◎4◎AND◎<=◎20：");
        String gzz = s.next();

        System.out.println("请输入需要验证的值：");
        while (true){
            String IdCard = s.next();
            //将身份证最后一位的x转换为大写，便于统一
            //IdCard = IdCard.toUpperCase();

            //身份证验证
            //LOG.info(invalidSFZH(IdCard) ? "非法身份证":"正常身份证");

            //姓名验证
            LOG.info(invalidXM(IdCard) ? "非法姓名":"正常姓名");

            //字段长度验证 前端生成的值为>=◎4◎AND◎<=◎20
            //LOG.info(invalidFieldLength("!=◎4", IdCard, false) ? "字段长度不满足" : "字段长度满足条件");

            //数值范围验证 前端生成的验证值为类似>=◎4◎AND◎<=◎20
            //LOG.info(invalidFieldLength(">=◎3◎AND◎<=◎10", IdCard, true) ? "字段数值不满足条件": "字段数值满足条件");

            //时间范围检验 >=◎2019-01-01 00:00:00◎AND◎<=◎2019-05-01 00:00:00
            //LOG.info(sjjc(jylx,gzz, IdCard)? "字段数值不满足条件": "字段数值满足条件");
        }

    }

}
