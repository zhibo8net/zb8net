package website2018.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import website2018.domain.SysParam;
import website2018.repository.SysParamDao;
import website2018.utils.SysConstants;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/10/14.
 */
@Service
public class BaoWeiService {

    public float checkNameAlike(String firstName, String secondName) {
        try {

            if (StringUtils.isEmpty(firstName) || StringUtils.isEmpty(secondName)) {
                return 0;
            }
            float lv1 =checkAlike(firstName,secondName);
            float lv2 =checkAlike(secondName,firstName);
            if(lv1>lv2){
                return lv1;
            }else{
                return lv2;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public float checkAlike(String firstName, String secondName) {
        try {

            if (StringUtils.isEmpty(firstName) || StringUtils.isEmpty(secondName)) {
                return 0;
            }
            firstName = firstName.replaceAll(" ", "");
            secondName = secondName.replaceAll(" ", "");
            int len = secondName.length();
            int key = 0;
            int count = 0;
            for (int i = 0; i < len; i++) {
                int bj = firstName.indexOf(secondName.charAt(i));
                if (bj >= 0 && bj >= key) {
                    count++;
                    key++;
                }
            }

            float lv1 = (float) count / (float) len;
             return lv1;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }


    public static void main(String[] args) {
        float lv1 = Float.parseFloat("0.60");
        float lv = (float) 0.75;
        if (lv > lv1) {
            System.out.print("dd");
        }
    }

    public boolean isConnect(String urlStr) {

        if (urlStr == null || urlStr.length() <= 0) {
            return false;
        }

        try {
            URL url = new URL(urlStr);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5000);
            con.setReadTimeout(30000);
            int state = con.getResponseCode();

            if (state == 200) {
                return true;
            }

        } catch (Exception ex) {
            ex.printStackTrace();

        }

        return false;
    }
}

