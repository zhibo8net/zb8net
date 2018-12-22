package website2018.spider;

import com.google.common.collect.Lists;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import website2018.MyApplication;
import website2018.base.BaseSpider;

import website2018.cache.CacheUtils;
import website2018.domain.Match;
import website2018.repository.MatchDao;
import website2018.service.BaoWeiService;
import website2018.utils.SysConstants;

import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class SinaLiveSpider extends BaseSpider {

    private static Logger logger = LoggerFactory.getLogger(SinaLiveSpider.class);


    @Autowired
    MatchDao matchDao;

    @Autowired
    BaoWeiService baoWeiService;

    @Scheduled(cron = "0 0 0/2 * * *")
    @Transactional
    public void runSchedule() throws Exception {
        if (MyApplication.DONT_RUN_SCHEDULED) {
            return;
        }

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DATE, 7);
                    Date d=calendar.getTime();
                   List<Date> dateList= getBetweenDates(new Date(),d);

                    fetchSinaLive(dateList);
                } catch (Exception e) {
                    logger.error(e.getLocalizedMessage());
                }
            }
        }, "SinaLiveSpider - " + System.currentTimeMillis());
        t.start();
    }
    private  List<Date> getBetweenDates(Date start, Date end) {
        List<Date> result = new ArrayList<Date>();
        Calendar tempStart = Calendar.getInstance();
        tempStart.setTime(start);
        tempStart.add(Calendar.DAY_OF_YEAR, 1);
        result.add(start);

        Calendar tempEnd = Calendar.getInstance();
        tempEnd.setTime(end);
        while (tempStart.before(tempEnd)) {
            result.add(tempStart.getTime());
            tempStart.add(Calendar.DAY_OF_YEAR, 1);
        }
        return result;
    }
    @Transactional
    public void fetchSinaLive(List<Date> listDate) throws Exception {
        try {

            List<Match> matchList=matchDao.findByPlayDateGreaterThan(new Date());
            for(Date d:listDate){
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String dateString = sdf.format(d);
                String url="http://platform.sina.com.cn/sports_other/livecast_dateschedule?app_key=3633771828&date="+dateString+"&callback=getLivecastScheculeCallback";
                String  doc = readDocFromByJsoupReqJson(url);
                if(doc!=null) {

                    System.out.println(doc.substring("getLivecastScheculeCallback(".length(), doc.length() - 2));
                    JSONObject jsonObject = JSONObject.fromObject(doc.substring("getLivecastScheculeCallback(".length(), doc.length() - 2));

                    JSONArray jsonArray = JSONArray.fromObject(jsonObject.getJSONObject("result").get("data"));
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject jsonObject2 = jsonArray.getJSONObject(i);

                        if( jsonObject2.get("Team1")!=null&& jsonObject2.get("Team2")!=null){

                            if(jsonObject2.get("live_url")!=null||jsonObject2.get("shuju_url")!=null){
                                updateMatch(matchList,jsonObject2.get("Team1").toString(),jsonObject2.get("Team2").toString(),jsonObject2.get("live_url"),jsonObject2.get("shuju_url"));
                            }
                        }

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("抓取sinalive错误{}", e);
        }

    }

    public void updateMatch(List<Match> matchList,String team1,String team2,Object sinaLiveUrl,Object resultUrl){
        Map<String, String> sysParamMap = CacheUtils.getSysMap();
        String lvStr= sysParamMap.get("LIVE_LV_SINA_TEAM")==null?"0.5": sysParamMap.get("LIVE_LV_SINA_TEAM");
        float lv1=Float.parseFloat(lvStr);

        for(Match m:matchList){
            if(m.guestTeam!=null&&m.masterTeam!=null){
                List<String> nameList= Lists.newArrayList();
                if(StringUtils.isNotEmpty(m.guestTeam.teamZh)){
                    nameList.add(m.guestTeam.teamZh);
                }
                if(StringUtils.isNotEmpty(m.guestTeam.teamName1)){
                    nameList.add(m.guestTeam.teamName1);
                }
                if(StringUtils.isNotEmpty(m.guestTeam.teamName2)){
                    nameList.add(m.guestTeam.teamName2);
                }
                if(StringUtils.isNotEmpty(m.guestTeam.teamName3)){
                    nameList.add(m.guestTeam.teamName3);
                }
                if(StringUtils.isNotEmpty(m.masterTeam.teamZh)){
                    nameList.add(m.masterTeam.teamZh);
                }
                if(StringUtils.isNotEmpty(m.masterTeam.teamName1)){
                    nameList.add(m.masterTeam.teamName1);
                }
                if(StringUtils.isNotEmpty(m.masterTeam.teamName2)){
                    nameList.add(m.masterTeam.teamName2);
                }
                if(StringUtils.isNotEmpty(m.masterTeam.teamName3)){
                    nameList.add(m.masterTeam.teamName3);
                }
            boolean flag=false;
             inner: for(String str:nameList){
                   float lv2=baoWeiService.checkNameAlike(str,team1);
                    float lv3=baoWeiService.checkNameAlike(str,team2);
                   if(lv2>lv1 && lv3>lv1 ){
                       if(sinaLiveUrl!=null){
                           m.sinaLiveUrl=sinaLiveUrl.toString();
                       }
                       if(resultUrl!=null){
                           m.sinaShujuUrl=resultUrl.toString();
                       }
                       matchDao.save(m);
                       flag=true;
                       break inner;
                    }
                }

                //跳过改比赛
                if(flag){
                    continue;
                }
                if(nameList.contains(team1)&&nameList.contains(team2)){
                    if(sinaLiveUrl!=null){
                        m.sinaLiveUrl=sinaLiveUrl.toString();
                    }
                    if(resultUrl!=null){
                        m.sinaShujuUrl=resultUrl.toString();
                    }
                    matchDao.save(m);
                }
            }
        }
    }

}
