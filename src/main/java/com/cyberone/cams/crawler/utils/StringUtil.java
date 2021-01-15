package com.cyberone.cams.crawler.utils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyberone.cams.crawler.core.ForgeryDetection;

public class StringUtil {
    
	private static final Logger logger = LoggerFactory.getLogger(ForgeryDetection.class);
    
    private static Map<String, String> replaceMap = null;
    
    /**
    * <pre>
    * 스트링을 일정한 길이로 잘라서 append 로 (...) 축약한다
    * 길이를 자를때는 바이트 단위로 자르므로, 한글은 2byte 이다.
    * </pre>
    *
    * @param str 자를 문자열
    * @param append 자르고 나서 append 할 문자열
    * @param len 잘라낼 길이
    */
    public static String cutString(String str, String append, int len) {
        int i = 0;
        byte[] b = str.getBytes();

        if (b.length <= len) {
            return str;
        }
        for (i = 0; i < len;) {
            if (b[i] < 0) {
                i += 2;
                continue;
            }
            i++;
        }
        return (new String(b, 0, i) + append);
    }

    /**
    * 지정된 코드로 분리된 문자열을 입력받아서 단일쿼터와 콤마로 분리된 문자열을 생성한다.
    * 입력된 문자의 경우 공백은 무시된다.
    * 사용예
    * StringUtil.convertQuoteNComma("ABC,BCD,EFG", ",");
    * @param str    지정된 딜리미터로 구분된 문자열 (test,abc,efg)
    * @return 단일 쿼터와 콤마로 분리된 문자열 'test', 'abc', 'efg'
    */
    public static String convertQuoteNComma(String str, String delim) {
        String delimStr = "[" + delim + "]";

        if (str == null) {
            return str;
        }

        String[] splitStr = str.split(delimStr);

        if (splitStr == null || splitStr.length == 0) {
            return str;
        }

        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < splitStr.length; i++) {
            sb.append("'");
            sb.append(splitStr[i].trim());
            sb.append("',");
        }

        return sb.substring(0, sb.length() - 1);
    }

    /**
    * 배열로 넘어온 값을 오라클 콤마값으로 변환한다.
    * @param str    변환할 문자열 배열값
    * @return 오라클 타입의 값 'test','111'
    */
    public static String convertQuoteNComma(String[] str) {
        if (str == null) {
            return "";
        }

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < str.length; i++) {
            sb.append("'");
            sb.append(str[i].trim());
            sb.append("',");
        }
        return sb.substring(0, sb.length() - 1);
    }

    /**
    * 문자열을 정제한다. 널인경우 ""을 반환한다.
    * @param object    널 검사를 수행할 객체
    * @return    널이 들어온경우 ""을, 그렇지 않은경우 문자열 자체를 전달한다.
    */
    public static String nullToStr(String object) {
        if (object == null || "null".equals(object)) {
            return "";
        }
        return object;
    }

    /**
    * 문자열을 정제한다. 문자열이 널인경우 대체 문자를 지정한다.
    * @param object    널 검사를 수행할 객체
    * @return    널이 들어온경우 ""을, 그렇지 않은경우 문자열 자체를 전달한다.
    */
    public static String nullToStr(String object, String convertStr) {
        String tempStr = convertStr == null ? "" : convertStr;

        if (object == null || object.equals("")) {
            return tempStr;
        }

        return object;
    }

    public static String convertString(Object o) {
        if (o == null) {
            return "";
        }
        if (o instanceof Integer) {
            return String.valueOf(o);
        } else if (o instanceof String) {
            return (String) o;
        }
        return (String) o;
    }

    public static String toString(Object o) {
        if (o == null) {
            return "";
        }
        try {
            return String.valueOf(o).trim();
        } catch (Exception e) {
            return "not casting";
        }
    }
    
    /**
    * 문자열의 앞 뒤 공백을 제거한다.
    * @param object 제거할 문자열
    * @return 공백에 제거된 문자열
    */
    public static String trimStr(String object) {
        if (object == null) {
            return "";
        }
        return object.trim();
    }

    /**
    * 문자열에 왼쪽 패딩을 수행한다.
    * @param sourceTxt    대상 문자열
    * @param padCode    패딩할 문자
    * @param precise    패딩을 처리할 자리수
    * @return    패딩된 글자
    */
    public static String paddingLeft(String sourceTxt, char padCode, int precise) {
        String source = sourceTxt;
        if (sourceTxt == null) {
            source = "";
        }
        StringBuffer sb = new StringBuffer(precise);
        int remainder = precise - sourceTxt.getBytes().length;
        for (int i = 0; i < remainder; i++) {
            sb.append(padCode);
        }
        sb.append(source);

        return sb.toString();
    }

    /**
    * 오른쪽 패딩을 수행한다.
    * @param sourceTxt    패딩할 문자열
    * @param padCode    패딩할 문자값
    * @param precise    총 문자 길이
    * @return    패딩 처리된 문자
    */
    public static String paddingRight(String sourceTxt, char padCode, int precise) {
        String source = sourceTxt;
        if (sourceTxt == null) {
            source = "";
        }
        StringBuffer sb = new StringBuffer(precise);
        sb.append(source);

        int remainder = precise - sourceTxt.getBytes().length;
        for (int i = 0; i < remainder; i++) {
            sb.append(padCode);
        }

        return sb.toString();
    }

    /**
    * 문자를 UTF-8로 변경한다.
    * @param str 변경할 문자열
    * @return    변경된 문자열
    */
    public static String toUtf8(String str) throws Exception {
        if (str == null) {
            return str;
        }

        return new String(str.getBytes("ISO-8859-1"), "UTF-8");
    }

    /**
    * 뉴라인을 <br>로 변경한다.
    * @param message    변경할 메시지
    * @return    변경된 문자
    * @throws Exception 오류 발생시
    */
    public static String spaceToBr(String message) throws Exception {
        if (message == null) {
            return message;
        }

        return message.replaceAll("\n", "<br/>");
    }

    /**
    * 뉴라인을 <br>로 변경한다.
    * @param message    변경할 메시지
    * @return    변경된 문자
    * @throws Exception 오류 발생시
    */
    public static String newlineToBr(String message) throws Exception {
        if (message == null) {
            return message;
        }

        return message.replaceAll("\r\n", "<br/>");
    }

    /**
    * 입력된 텍스트를 원하는 길이만큼 자르고 특정 문자열을 덧붙인다.
    * @param strText 자를려고 하는 Text
    * @param cutLength 자를려는 길이
    * @param strSuffix 자르고 붙일려는 특정문자.
    * @return
    */
    public static String cutByLength(String strText, int cutLength, String strSuffix) {
        if (strText == null || "".equals(strText)) {
            return "";
        }
        if (strText.trim().length() < cutLength) {
            return strText.trim();
        }
        return strText.trim().substring(0, cutLength) + strSuffix;
    }

    /**
    * 날짜값을 입력받아 다른 날짜 타입으로 변경한다.
    * @param dateStr    20080808
    * @param srcForm    yyyyMMdd
    * @param desForm    yyyy/MM/dd
    * @return    변경된 날자 타입으로 반환한다.2008/08/08
    * @throws Exception 예외 발생시
    */
    public static String convertDate(String dateStr, String srcForm, String desForm) throws Exception {
        if (dateStr == null || dateStr.equals("")) {
            return dateStr;
        }

        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern(srcForm);

        Date date = new Date();
        date = sdf.parse(dateStr);

        return StringUtil.convertDate(date, desForm);

    }

    /**
    * 날짜값을 입력받아 타입을 변경한다.
    * @param date
    * @param pattern
    * @return
    */
    public static String convertDate(Object date, String pattern) {

        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern(pattern);

        return sdf.format((Date)date);
    }

    /**
    * 쿠키값을 획득한다.
    * @param key    쿠키값을 획득할 키값
    * @param request    쿠키값을 획득할 요청 객체
    * @return    쿠키값이 있는경우 해당 값을 없을경우 null을 반환
    */
    public static String getCookieValue(String key, HttpServletRequest request) {
        // 고객 데이터로 부터 기 설정된 쿠키값을 읽어온다.
        Cookie[] cookies = request.getCookies();
        Cookie prdtCookie = null;
        String prdtCodes = "";
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                if (cookies[i].getName().equals(key)) {
                    prdtCookie = cookies[i];
                    break;
                }
            }

            // 쿠키값을 획득한다.
            if (prdtCookie != null) {
                prdtCodes = prdtCookie.getValue();
            } else {
                prdtCodes = "";
            }

        } else {
            prdtCodes = "";
        }

        return prdtCodes;
    }

    /**
    * 쿠키값을 설정한다.
    * @param key    값을 설정할 키값
    * @param Value    설정할 값
    * @param time    설정 시간
    * @param response    응답객체
    */
    public static void setCookie(String key, String value, int time, HttpServletResponse response) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(time);
        response.addCookie(cookie);
    }

    /**
    * 배열을 옵션 값으로 바꿔서 출력한다.
    * @param valueArr value란들어갈 값
    * @param titleArr    화면에 표시될 값
    * @return
    */
    public static String arrayToOption(String[] valueArr, String[] titleArr, String selectedValue) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < valueArr.length; i++) {
            if (valueArr[i].equals(selectedValue)) {
                sb.append("<option value='").append(valueArr[i]).append("' selected >");
            } else {
                sb.append("<option value='").append(valueArr[i]).append("'>");
            }
            sb.append(titleArr[i]);
            sb.append("</option>");
        }
        return sb.toString();
    }

    /**
    *
    * @param money 변경하고자 하는 금액 모두 붙어있는 금액이어야 함 10002222
    * @return 반환값은 콤마 3자리로 구분된 값 10,002,222
    */
    public static String commaForMoney(String money) {
        if (money == null) {
            return "0";
        }

        long value = Long.parseLong(money);
        DecimalFormat df = new DecimalFormat("###,###,###");

        return df.format(value);
    }

    /**
    * 한글 체크 처리 (한글이 포함되어 있는지 검사한다.)
    * @param check 체크할 문자열
    * @return    true인겨우 한글 포함, false인경우 한글 미포함
    */
    public static boolean isContainKor(String check) {
        if (check == null) {
            return false;
        }

        for (int i = 0; i < check.length(); i++) {
            String target = String.valueOf(check.charAt(i));
            if (target.matches("[가-힝]")) {
                return true;
            }
        }
        return false;
    }

    /**
    * URL 혹은 URI정보에서 한글이 들어 있는경우 해당 인코딩으로 인코딩 수행하여 새로운 경로를 만든다.
    * @param uri 변환을 수행할 URI 정보
    * @param encType    인코딩 타입
    * @return    변환된 URL정보가 출력된다.
    */
    public static String hanUriEncoding(String uri, String encType) {
        if (uri == null) {
            return null;
        }

        StringBuffer retStr = new StringBuffer();
        String[] targetArray = uri.split("[\\|/]");
        for (int i = 0; i < targetArray.length; i++) {
            if (isContainKor(targetArray[i])) {
                try {
                    retStr.append(URLEncoder.encode(targetArray[i], encType));
                } catch (UnsupportedEncodingException e) {
                    logger.debug("An error occurs in hanUriEncoding(): {} {}", uri, encType);
                }
            } else {
                retStr.append(targetArray[i]);
            }

            //    마지막 라인은 /를 넣지 않는다.
            if (i < targetArray.length - 1) {
                retStr.append("/");
            }

        }

        return retStr.toString();
    }

    /**
    * 문자열로 된 날짜를 이용하여 날짜 연산을 처리한다.
    * @param dateStr  원본 날짜
    * @param sourceType    소스 날짜 타입 'YYYYMMDD'
    * @param targetType 바꾸고자 하는 날짜 'YYYY.MM.DD'
    * @param calCd    바꾸고자 하는 날자 필드 Calendar.DATE
    * @param dateVal    변경하고자 하는 날자    -1 (하루 이전날짜가 됨)
    * @return 변경된 날자 형태 문자열
    */
    public static String getDate(String dateStr, String sourceType, String targetType, int calCd, int dateVal) {
        String returnStr = dateStr;
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern(sourceType);
        try {
            Date date = sdf.parse(dateStr);
            returnStr = getDate(date, targetType, calCd, dateVal);
        } catch (ParseException e) {
            // TODO 자동 생성된 catch 블록
            logger.debug("An error occurs in getDate(): {} {} {}", dateStr, sourceType, targetType);
        }

        return returnStr;
    }

    /**
    * 날짜 타입을 변경한다.
    * @param date    데이트 타입
    * @param targetType 바꾸고자 하는 날짜 'YYYY.MM.DD'
    * @param calCd    바꾸고자 하는 날자 필드 Calendar.DATE
    * @param dateVal    변경하고자 하는 날자    -1 (하루 이전날짜가 됨)
    * @return 변경된 날자 형태 문자열
    * @return
    */
    public static String getDate(Date date, String targetType, int calCd, int dateVal) {

        SimpleDateFormat sdf = new SimpleDateFormat();

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        cal.add(calCd,dateVal);

        sdf.applyPattern(targetType);

        return  sdf.format(cal.getTime());
    }

    /**
    * 디코딩 처리를 수행한다.
    * @param content    디코딩을 수행할 문자
    * @param encType    디코딩을 수행할 캐릭터 타입
    */
    public static String decode(String content, String encType) throws Exception {
        if (content == null || "".equals(content)) {
            return content;
        }

        return URLDecoder.decode(content, encType);
    }

    /**
    * 현재일에서 +/-한 날짜 얻기
    * @param add
    * @return
    */
    public static String getAddDate(int add) {    //add=+,-기호 사용
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE,add); //현재 날짜에서 add한  날짜 가져오기
        Date currentTime = cal.getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.KOREA);
        String strTimestr = formatter.format(currentTime);
        return strTimestr;

    }

    /**
    * 현재일에서 +/-한 날짜 얻기 (format 지정)
    * @param add
    * @return
    */
    public static String getAddDate(int add, String format) {    //add=+,-기호 사용
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE,add); //현재 날짜에서 add한  날짜 가져오기
        Date currentTime = cal.getTime();
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.KOREA);
        String strTimestr = formatter.format(currentTime);
        return strTimestr;

    }

    /**
    * 현재일에서 +/- 한 날짜(달) 얻기 (
    * @param add
    * @return
    */
    public static String getAddMonth(int add) {    //add=+,-기호 사용
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, add); //현재 날짜에서 add한  날짜 가져오기
        Date currentTime = cal.getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.KOREA);
        String strTimestr = formatter.format(currentTime);
        return strTimestr;

    }

    public static String queryFilter(String str) {
        if (str == null || "".equals(str)) {
            return str;
        }

        String retStr = str;
        retStr = retStr.replaceAll("[;]", "&#39;");
        retStr = retStr.replaceAll("[']", "&#59;");
        retStr = retStr.replaceAll("[:]", "&#58;");
        retStr = retStr.replaceAll("[--]", "&#45;&#45;");
        retStr = retStr.replaceAll("[+]", "&#43;");
        retStr = retStr.replaceAll("[/]", "&#47;");
        // retStr = retStr.replaceAll("[\]", "&#92;");
        retStr = retStr.replaceAll("[%]", "&#37;");
        retStr = retStr.replaceAll("[<]", "&#lt;");
        retStr = retStr.replaceAll("[>]", "&#gt;");
        retStr = retStr.replaceAll("[(]", "&#40;");
        retStr = retStr.replaceAll("[)]", "&#41;");
        retStr = queryFilterMat(retStr, "(select)+", "");
        retStr = queryFilterMat(retStr, "(delete)+", "");
        retStr = queryFilterMat(retStr, "(from)+", "");
        retStr = queryFilterMat(retStr, "(insert)+", "");
        retStr = queryFilterMat(retStr, "(update)+", "");
        retStr = queryFilterMat(retStr, "(drop)+", "");
        retStr = queryFilterMat(retStr, "(create)+", "");

        return retStr;
    }

    public static String queryFilterMat(String str, String pattern, String extStr) {
        if (str == null || "".equals(str)) {
            return str;
        }
        String retStr = str;
        return retStr.replaceAll("(?i)" + pattern, extStr);
    }

    public static Date getToday() {
        return new Date();
    }
    
    public static String getToday(String format) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.KOREA);
        String sdate = sdf.format(cal.getTime());
        return sdate;
    }

    /**
    * 해당되는 달의 마지막날을 획득한다.
    * @param Year : 연도, Month : 월, Day : 날짜
    * @return 해당되는 마지막 날짜
    */
    public static String getLastDay(String year, String month, String day) {
        String lastDay = null;

        String[] args = {year, month, day};
        Calendar cal = Calendar.getInstance();
        cal.set(Integer.parseInt(args[0]), Integer.parseInt(args[1]) - 1, Integer.parseInt(args[2]));
        lastDay = cal.getActualMaximum(Calendar.DATE) + "";
        return lastDay;
    }

    /**
    * String 날짜를 입력받아서 +,- 한 날짜를 얻는다.
    * @param date
    * @param day
    * @return
    */
    public static String getConvertDate(String date, int day) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.KOREA);
        Date now;
        try {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.set(Integer.parseInt(date.substring(0,4)),Integer.parseInt(date.substring(4,6)) - 1, Integer.parseInt(date.substring(6)));
            calendar.add(Calendar.DATE,day);
            now = calendar.getTime();
        } catch (Exception ie) {
            return "";
        }
        return formatter.format(now);
    }

    /**
    * @param fileName 검사할 파일이름 (한글 파일은 파일검사에서 false로 나온다.
    * @return    파일이름으로 적합한경우 true, 그렇지 않은경우 false
    */
    public static boolean isEngFileName(String fileName) {
        Pattern p = Pattern.compile("[a-zA-Z0-9\\s]*\\.[a-z]*");
        Matcher m = p.matcher(fileName);
        return m.matches();
    }

    public static String getString(Date date, String format) {

        if (date != null) {

            SimpleDateFormat simpleDate = new SimpleDateFormat(format, Locale.KOREA);

            return simpleDate.format(date);
        } else {
            return null;
        }
    }

    /**
    * 문자열에서 정의된 HTML 태그를 제거한다. 현재는, &lt;pre&gt; 태그만 제거한다
    *
    * @param org 대상문자열
    * @return 태그가 제거된 문자열
    */
    public static String removeTag(String org) {
        try {
            if (org == null) {
                return null;
            }

            StringBuffer sb = new StringBuffer();

            char[] charList =  org.toCharArray();

            boolean isTag = false;

            int i = 0;
            int length = charList.length;

            for (i = 0; i < length; i++) {
                if (charList[i] == '<') {
                    isTag = true;
                } else if (charList[i] == '>') {
                    isTag = false;
                } else if (!isTag) {
                    sb.append(charList[i]);
                }
            }

            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }


    private static void setReplaceMap() {
        if (replaceMap == null) {
            replaceMap = new HashMap<String, String>();
            
            //이부분에 추가적으로 필요한 구분 추가
            replaceMap.put("<", "&lt;");
            replaceMap.put(">", "&gt;");
            replaceMap.put("\"", "\\\"");
        }
    }
    
    /**
    * 문자열에서 정의된 HTML 태그를 제거한다. 현재는, &lt;&gt; 태그만 제거한다
    * 긴문자열에 사용시 성능 안좋음 
    * @param org 대상문자열
    * @return 태그가 제거된 문자열
    */
    public static String replaceHtml(String s) {
        return replaceHtml(s, new String[] {"<",">","\""});
    }
    
    public static String replaceHtml(String s, String[] replaceTag) {
        StringBuffer sb = new StringBuffer();
        try {
            if (s != null && s.length() != 0) {
                Pattern p;
                Matcher m;
                setReplaceMap();
                for (int i = 0; i < replaceTag.length; i++) {
                    if (replaceMap.get(replaceTag[i]) != null) {
                        p = Pattern.compile(replaceTag[i]);
                        m = p.matcher(s);
                        while (m.find()) {
                            m.appendReplacement(sb, (String)replaceMap.get(replaceTag[i]));
                        }
                        m.appendTail(sb);
                        s = sb.toString();
                        sb.setLength(0);
                    }
                }
            } else {
                s = "";
            }
        } catch (Exception e) {
            return "";
        }
        return s;
    }

    /**
    * 나모 에디터에서 body내부 내용만 가져오고 싶을경우
    * @param cnt
    * @return body태그 내부내용
    */
    public static String getNamoContent(String cnt) {
        if (cnt == null) {
            return "";
        } else {
            Pattern bodyContent = Pattern.compile("<body>*.*</body>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Pattern s = Pattern.compile("<script>*.*</script>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            String result = "";
            StringBuffer sb = new StringBuffer();
            Matcher m = bodyContent.matcher(cnt);
            if (m.find()) {
                Matcher sc = s.matcher(m.group());
                while (sc.find()) {
                    sc.appendReplacement(sb, "");
                }
                sc.appendTail(sb);
            } else {
                sb.append(cnt);
            }
            result = sb.toString().replaceAll("(?i)<body.*?>", "").replaceAll("(?i)</body.*?>(?i)", "");
            sb.setLength(0);
            return result;
        }
    }

    /**
    * 두 날짜(A, B)를 비교한다.
    * @param YYYY-MM-DD HH-mm-ss
    * @return 어떤 날짜가 최근일인지 'A' or 'B' 같은경우 에는 'E'를 RETURN
    */
    public static String getCompareDate(String a, String b) {
        String result = null;
        
        int year = 0;
        int month = 0;
        int day = 0;
        int hour = 0;
        int min = 0;
        int sec = 0;

        int year2 = 0;
        int month2 = 0;
        int day2 = 0;
        int hour2 = 0;
        int min2 = 0;
        int sec2 = 0;
        
        if (a.length() == 19) {
            year = Integer.parseInt(a.substring(0,4));
            month = Integer.parseInt(a.substring(5,7));
            day = Integer.parseInt(a.substring(8,10));
            hour = Integer.parseInt(a.substring(11,13));
            min = Integer.parseInt(a.substring(14,16));
            sec = Integer.parseInt(a.substring(17,19));
        }
        if (b.length() == 19) {
            year2 = Integer.parseInt(b.substring(0,4));
            month2 = Integer.parseInt(b.substring(5,7));
            day2 = Integer.parseInt(b.substring(8,10));
            hour2 = Integer.parseInt(b.substring(11,13));
            min2 = Integer.parseInt(b.substring(14,16));
            sec2 = Integer.parseInt(b.substring(17,19));
        }

        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day, hour, min, sec);

        Calendar cal2 = Calendar.getInstance();
        cal2.set(year2, month2, day2, hour2, min2, sec2);

        if (cal.after(cal2)) {
            result = "A";
        } else if (cal.before(cal2)) {
            result = "B";
        } else {
            result = "E";
        }

        return result;
    }

    /**
    * 배열에 있는 String을 가져와서 패턴과 일치하면 리턴배열에 담고  일치하지 않으면 빈값으로 담아 리턴함.
    * @param sString
    * @param chkStr
    * @return String[]
    */
    public static String[] rtnArr(String[] saString, String chkStr) {
        for (int i = 0; i < saString.length; i++) {
            if (saString[i].indexOf(chkStr) == -1) {
                saString[i] = "";
            }
        }
        return saString;
    }

    /**
    * test for Map,Collection,String,Array isEmpty
    *
    * @param o
    * @return
    */
    @SuppressWarnings("rawtypes")
    public static boolean isEmpty(Object o) throws IllegalArgumentException {
        if (o == null) {
            return true;
        }
        if (o instanceof String) {
            if (((String) o).length() == 0) {
                return true;
            }
        } else if (o instanceof Collection) {
            if (((Collection) o).isEmpty()) {
                return true;
            }
        } else if (o.getClass().isArray()) {
            if (Array.getLength(o) == 0) {
                return true;
            }
        } else if (o instanceof Map) {
            if (((Map) o).isEmpty()) {
                return true;
            }
        } else {
            return false;
        }
        return false;
    }
    
    public static boolean isEmpty(String str) {
        if (str == null) {
            return true;
        }
        return str.trim().equals("");
    }

    @SuppressWarnings("rawtypes")
    public static boolean isEmptyList(Collection list) {
        return (list == null || list.isEmpty());
    }

    @SuppressWarnings("rawtypes")
    public static boolean isNotEmptyList(Collection list) {
        return !isEmptyList(list);
    }
    
    public static boolean isEmptyArray(Object[] objs) {
        return (objs == null || objs.length == 0);
    }
    
    //문자열내 문자열 변환    - 2011.02.15 한광흠 추가
    public static String getReplace(String s, String s1, String s2)    {
        if (s == null) {
            return "";
        }
        String s3 = s;
        StringBuffer stringbuffer = new StringBuffer();
        stringbuffer.append("");
        int i;
        for (; s3.indexOf(s1) > -1; s3 = s3.substring(i + s1.length())) {
            i = s3.indexOf(s1);
            stringbuffer.append(s3.substring(0, i));
            stringbuffer.append(s2);
        }

        stringbuffer.append(s3);
        return stringbuffer.toString();
    }

    public static String imgRemovePattern(String str) {
        Pattern pattern = Pattern.compile("<IMG[^>]*>",Pattern.DOTALL);
        Matcher m;
        m = pattern.matcher(str);
        String pstr = m.replaceAll("");
        return pstr;
    }

    @SuppressWarnings("rawtypes")
    public static void xyDebugList(List list) {
        if (list != null) {
            for (int i = 0; list != null && i < list.size(); i++) {
                Object o = list.get(i);
                xyDebugBean(o);
            }
        } else {
            logger.info(">>>>>>>>>>>> List NULL");
        }
    }

    public static void xyDebugBean(Object o) {
        if (o != null) {
            Method[] m = o.getClass().getDeclaredMethods();
            for (int i = 0; i < m.length; i++) {
                if (m[i].getName().startsWith("get")) {
                    try {
                        logger.info(m[i].getName() + " : " + m[i].invoke(o, null) + " ");
                    } catch (Exception e) {
                        logger.error("", e);
                    }
                }
            }
        } else {
            logger.info(">>>>>>>>>>>> Object NULL");
        }
    }

    /**
    * test for Map,Collection,String,Array isNotEmpty
    *
    * @param c
    * @return
    */
    public static boolean isNotEmpty(Object o) {
        return !isEmpty(o);
    }

    //입력받은 yyyymmdd 형식을 yyyy-mm-dd으로 리턴해준다
    public static String makeHyphen(String yyyymmdd, String ch) {
        return yyyymmdd.substring(0, 4) + ch + yyyymmdd.substring(4,6) + ch + yyyymmdd.substring(6);
    }

    // 하이픈을 제거해준다.
    public static String removeHyphen(String yyyymmdd) {
        return yyyymmdd.replace("-", "");
    }

    // 특수문자 -> 2바이트 문자 변경
    public static String replace2ByteChar(String value) {

        value = nullToStr(value);
        if (!"".equals(value)) {
            value = value.replaceAll("!",    "&#33;");
            value = value.replaceAll("\"",   "&#34;");  // &quot;
            //value = value.replaceAll("#",    "&#35;");
            value = value.replaceAll("[$]",  "&#36;");
            value = value.replaceAll("%",    "&#37;");
            // check
            //value = value.replaceAll("&",    "&#38;");  // &amp;
            value = value.replaceAll("'",    "&#39;");
            //value = value.replaceAll("\\(",  "&#40;");
            //value = value.replaceAll("\\)",  "&#41;");
            value = value.replaceAll("[*]",  "&#42;");
            value = value.replaceAll("[+]",  "&#43;");
            value = value.replaceAll(",",    "&#44;");
            //value = value.replaceAll("-",    "&#45;");
            value = value.replaceAll("\\.",  "&#46;");
            value = value.replaceAll("/",    "&#47;");
            value = value.replaceAll(":",    "&#58;");
            // check
            //value = value.replaceAll(";",    "&#59;");
            value = value.replaceAll("<",    "&#60;");  // &lt;
            value = value.replaceAll("=",    "&#61;");
            value = value.replaceAll(">",    "&#62;");  // &gt;
            value = value.replaceAll("\\?",  "&#63;");
            value = value.replaceAll("@",    "&#64;");
            value = value.replaceAll("\\[",  "&#91;");
            value = value.replaceAll("\\\\", "&#92;");
            value = value.replaceAll("\\]",  "&#93;");
            value = value.replaceAll("\\^",  "&#94;");
            //value = value.replaceAll("_",    "&#95;");
            value = value.replaceAll("`",    "&#96;");
            value = value.replaceAll("\\{",  "&#123;");
            value = value.replaceAll("[|]",  "&#124;");
            value = value.replaceAll("\\}",  "&#125;");
            value = value.replaceAll("~",    "&#126;");
        }
        return value;
    }

    /*    해당 문자열에서 지정 문자열 이전 후 문자열 추출 리턴
     * ex)
     *         str : AC클리닉라인에비비가나왔었나요jQuery191010455549508333206&#95;1384091669105
     *         idxStr : jQuery
     *         prevNext : prev or next
     *             prev : AC클리닉라인에비비가나왔었나요
     *             next : jQuery191010455549508333206&#95;1384091669105
     * */
    public static String indexOfsubString(String str, String idxStr, String prevNext) {

        str = nullToStr(str);
        idxStr = nullToStr(idxStr);
        prevNext = nullToStr(prevNext, "prev");
        if (!"".equals(str) && !"".equals(idxStr)) {
            int idx = str.toUpperCase().indexOf(idxStr);

            if (idx > -1) {
                if ("prev".equals(prevNext)) {
                    str = str.substring(0, idx) + str.substring(idx + 45, str.length());
                } else {
                    str = str.substring(idx + 45, str.length());
                }
            }
        }

        return str;
    }
    
    public static int parseInt(String str, int def) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return def;
        }
    }
    
    public static boolean isImageFile(String filePath) {
        int pos = filePath.lastIndexOf(".");
        String fileExt = filePath.substring(pos + 1);
        if (fileExt.toLowerCase().equals("gif") || fileExt.toLowerCase().equals("jpg") || fileExt.toLowerCase().equals("png") || fileExt.toLowerCase().equals("bmp")) {
            return true;
        }
        return false;
    }
    
    /**********************************************************
    * 입력된 문자열에서 숫자만을 리턴한다.
    * @param : String str(Input Value)
    * @return : int reLength(Output Value)
    **********************************************************/
    public static String deleteChar(String str) {
        String strNumber = "0123456789";
        String addString = "";
        if (str.length() > 0) {        
            for (int i = 0; i < str.length(); i++) {
                if (strNumber.indexOf(str.charAt(i)) >= 0) {
                    addString = addString + str.charAt(i);
                }
            }
        }
        return addString;
    }
    
    public static String lastStrReplace(String str, String separator) {
        if (!"".equals(nullToStr(str))) {
            int strLen = str.length();
            str = str.substring(strLen - 1, strLen).equals(separator) ? str.substring(0, strLen - 1) : str;
        }
        return str;
    }
    
	public static boolean patternMatcher(String regx, String value) throws Exception {
		Pattern pattern = Pattern.compile(regx);
		Matcher matcher = pattern.matcher(value);
        return matcher.matches();
	}
	
	public static String dateFormatter(Object object) {
	    //날짜 형식을 String으로 혹은 String을 날짜 형식으로 변환하기 위한 포맷형식이다.
	    DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

	    // Calendar형식에서 날짜를 가져온다. 특이점은 Calendar가 singleton 형식이다.
	    // Date 값을 가져와서 String으로 변환한다.
	    String datestr = format.format(object);
	    
	    return datestr;
	}
    
}
