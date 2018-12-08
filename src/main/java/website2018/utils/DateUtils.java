package website2018.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2018/12/8.
 */
public class DateUtils {

    public static String getDateStr(Date date,String format){
        date=date==null?new Date():date;
        SimpleDateFormat dateStrForQueryFormat = new SimpleDateFormat(format);
        return dateStrForQueryFormat.format(date);
    }




}
