package website2018.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import website2018.domain.SysParam;
import website2018.repository.SysParamDao;
import website2018.utils.SysConstants;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/10/14.
 */
@Service
public class BaoWeiService {

    public  float checkNameAlike(String firstName,String secondName){
        try {

            if(StringUtils.isEmpty(firstName)||StringUtils.isEmpty(secondName)){
                return 0;
            }
            firstName=firstName.replaceAll(" ","");
            secondName=secondName.replaceAll(" ","");
            int len=secondName.length();
            int key=0;
            int count=0;
            for(int i = 0; i < len; i++){
              int bj=  firstName.indexOf(secondName.charAt(i));
                if(bj>=0&&bj>=key){
                    count++;
                    key++;
                }
            }
            float lv=(float)count/(float)len;

           // System.out.println("  count=" + count + "   key=" + key + " len=" + len+" lv="+lv);
            return lv;
        }catch (Exception e){
            e.printStackTrace();
        }

        return 0;
    }

    public static void main(String[] args){
        float lv1=Float.parseFloat("0.60");
        float lv= (float) 0.75;
        if(lv>lv1){
            System.out.print("dd");
        }
    }
}

