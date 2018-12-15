package website2018.spider;


import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import website2018.MyApplication;
import website2018.base.BaseSpider;
import website2018.domain.Live;
import website2018.domain.Match;
import website2018.domain.MatchStream;
import website2018.repository.MatchDao;
import website2018.repository.MatchStreamDao;
import website2018.service.BaoWeiService;
import website2018.utils.SysConstants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class MatchStreamUrlSpider extends BaseSpider {

    private static Logger logger = LoggerFactory.getLogger(MatchStreamUrlSpider.class);

    @Autowired
    private MatchStreamDao matchStreamDao;

    @Autowired
    MatchDao matchDao;

    @Autowired
    BaoWeiService baoWeiService;

    @Scheduled(cron = "0 0/5 * * * *")
    public void runSchedule() throws Exception {
        if (MyApplication.DONT_RUN_SCHEDULED) {
            return;
        }

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    matchStreamFetch();

                } catch (Exception e) {
                    logger.error(e.getLocalizedMessage());
                }
            }
        }, "MatchStreamUrlSpider - " + System.currentTimeMillis());
        t.start();
    }

    @Transactional
    public void matchStreamFetch() throws Exception {
        Map<String, String> sysParamMap = SysConstants.sysParamMap;
        String url=sysParamMap.get("LIVE_URL_PRE")==null?"https://liveplay.oadql.cn/live/":sysParamMap.get("LIVE_URL_PRE");

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -150);
        Date d=calendar.getTime();
        calendar.add(Calendar.MINUTE, 200);
        Date d1=calendar.getTime();
        List<Match> matchList=matchDao.findByPlayDateGreaterThanAndPlayDateLessThan(d,d1);
        List<MatchStream> matchStreamList=matchStreamDao.findByUpdateTimeGreaterThanAndUpdateTimeLessThan(d, d1);
        outer: for(MatchStream matchStream:matchStreamList){
            String matchStreamUrl=url+matchStream.matchStreamName+".m3u8";
            if(!baoWeiService.isConnect(matchStreamUrl)){
                logger.warn("连接地址无效："+matchStreamUrl);
                continue;
            }
           boolean liveFlag=false;

            inner: for(Match m:matchList){
                Match match=matchDao.findById(m.id);
               if(StringUtils.isEmpty(match.name)){
                   logger.warn("比赛名称为空退出循环");
                   continue;
               }

                if(StringUtils.isEmpty(matchStream.matchStreamName)){
                    logger.warn("比赛名称matchStreamName 为空退出循环");
                    continue;
                }
                if (match.name.equals(matchStream.matchName)){
                    liveFlag=true;

                    match.matchStreamUrl=matchStreamUrl;
                    addLive(match,matchStreamUrl);
                    matchDao.save(match);
                    break inner;
               }
                String lvStr= sysParamMap.get("LIVE_LV_MATCH")==null?"0.5": sysParamMap.get("LIVE_LV_MATCH");
                float lv1=Float.parseFloat(lvStr);
                float lv=  baoWeiService.checkNameAlike(match.name, matchStream.matchName);
                if(lv>lv1){
                    logger.warn("相似度匹配成功 "+match.name);
                    liveFlag=true;

                    match.matchStreamUrl=matchStreamUrl;
                    addLive(match,matchStreamUrl);
                    matchDao.save(match);
                    break inner;
                }



                if(match.guestTeam==null||match.masterTeam==null){
                    logger.warn("match 比赛guestTeam masterTeam 为空退出循环");
                    continue;
                }
                if(matchStream.guestTeam==null||matchStream.masterTeam==null){
                    logger.warn("matchStream 比赛guestTeam masterTeam 为空退出循环");
                    continue;
                }
                if(match.masterTeam.id.equals(matchStream.masterTeam.id)&&match.guestTeam.id.equals(matchStream.guestTeam.id)){
                    liveFlag=true;

                    match.matchStreamUrl=matchStreamUrl;
                    addLive(match,matchStreamUrl);
                    matchDao.save(match);
                    break inner;
                }
                if(match.masterTeam.id.equals(matchStream.guestTeam.id)&&match.guestTeam.id.equals(matchStream.masterTeam.id)){
                    liveFlag=true;

                    match.matchStreamUrl=matchStreamUrl;
                    addLive(match,matchStreamUrl);
                    matchDao.save(match);
                    break inner;
                }
              String mn= match.masterTeam.teamZh+"VS"+match.guestTeam.teamZh;
                lv=  baoWeiService.checkNameAlike(mn, matchStream.matchName);
                if(lv>lv1){
                    logger.warn("相似度匹配成功 "+match.name);
                    liveFlag=true;

                    match.matchStreamUrl=matchStreamUrl;
                    addLive(match,matchStreamUrl);
                    matchDao.save(match);
                    break inner;
                }


                String mn2= match.guestTeam.teamZh +"VS"+match.masterTeam.teamZh;
                lv=  baoWeiService.checkNameAlike(mn2, matchStream.matchName);
                if(lv>lv1){
                    logger.warn("相似度匹配成功 "+match.name);
                    liveFlag=true;

                    match.matchStreamUrl=matchStreamUrl;
                    addLive(match,matchStreamUrl);
                    matchDao.save(match);
                    break inner;
                }


            }

           if(! liveFlag){
               saveMatch(matchStream);
           }
        }
    }

    public void saveMatch(MatchStream matchStream){
        try{


        Map<String, String> sysParamMap = SysConstants.sysParamMap;
        if(!"TRUE".equals(sysParamMap.get("LIVE_SAVE_MATCH"))){
           return;
        }
        String url=sysParamMap.get("LIVE_URL_PRE")==null?"http://liveplay.oadql.cn/live/":sysParamMap.get("LIVE_URL_PRE");

        if(StringUtils.isEmpty( matchStream.playTime)){
           return;
       }

        String[] playTimeStr= matchStream.playTime.split(" ");
        Date date=new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");

        List<Match> matchListLive=matchDao.findByNameAndPlayTimeAndPlayDateStr(matchStream.matchName,playTimeStr[1], sdf.format(date)+"-"+playTimeStr[0]);

        if(matchListLive!=null&&matchListLive.size()>=1){
            return;
        }

        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Match matchSave=new Match();
        matchSave.name=matchStream.matchName;
        matchSave.project=matchStream.project;
        matchSave.game=matchStream.game;
        matchSave.matchStreamUrl=url+matchStream.matchStreamName+".m3u8";
        matchSave.playDate=sdf1.parse(sdf.format(date)+"-"+playTimeStr[0]+" "+playTimeStr[1]);
        matchSave.playTime=playTimeStr[1];
        matchSave.playDateStr=sdf.format(date)+"-"+playTimeStr[0];
        matchSave.addTime=new Date();
        Live live=new Live();
        live.playFlag = "INNER";
        live.match = matchSave;
        live.name = "直播视频";
        live.link = matchSave.matchStreamUrl;
        live.addTime = new Date();
        matchSave.lives.add(live);
        matchDao.save(matchSave);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void addLive(Match match,String url){
        try{
            List<Live> liveList= match.lives;
            boolean flag=false;
            for(Live live:liveList){
                if(url.equals(live.link)){
                    flag=true;
                    break;
                }
            }
            if(flag){
                return;
            }
            if(!flag){
                Live live=new Live();
                live.playFlag = "INNER";
                live.match = match;
                live.name = "直播视频";
                live.link =url;
                live.addTime = new Date();
                match.lives.add(live);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
