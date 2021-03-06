package website2018.spider;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import website2018.MyApplication;
import website2018.base.BaseSpider;
import website2018.cache.CacheUtils;
import website2018.domain.*;
import website2018.repository.*;
import website2018.service.TeamCheckService;

import java.util.*;

@Component
public class MatchStreamSpider extends BaseSpider {

    private static Logger logger = LoggerFactory.getLogger(MatchStreamSpider.class);

    @Autowired
    private MatchStreamDao matchStreamDao;

    @Autowired
    TeamDao teamDao;

    @Autowired
    TeamCheckService teamService;

    @Scheduled(cron = "0 0/5 * * * *")
    @Transactional
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
        }, "MatchStreamSpider - " + System.currentTimeMillis());
        t.start();
    }

    @Transactional
    public void matchStreamFetch() throws Exception {
    //    for(int fetchMatchId=1;fetchMatchId<=6;fetchMatchId++){
        int fetchMatchId=1;
            try {
                Map<String, String> sysParamMap = CacheUtils.getSysMap();
                String url=sysParamMap.get("LIVE_FETCH_URL");
                if(StringUtils.isEmpty(url)){
                    logger.info(" 抓取数据为空 地址为空 LIVE_FETCH_URL"+url);
                    return;
                }
                Document matchStreamDoc = readDocFrom(url);
                if (matchStreamDoc == null) {
                    logger.info(" 抓取数据为空"+url);
                    return;
                }
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.HOUR, -3);
                Date d=calendar.getTime();
                List<MatchStream> saveMatchList= Lists.newArrayList();
                Date date=new Date();
                Elements tbodys = matchStreamDoc.select("tbody");
                for (Element tbody : tbodys) {
                    for (Element tr : tbody.select("tr")) {
                        Elements tds = tr.select("td");
                        if (tds.size() >=6) {
                            try {
                                String matchName=tds.get(3).html();
                                if (StringUtils.isNotEmpty(matchName)) {
                                    matchName= matchName.replaceAll("&middot;",".");
                                }
                            List<MatchStream> matchStreamList=     matchStreamDao.findByMatchNameAndUpdateTimeGreaterThan(matchName, d);
                                if(matchStreamList!=null&&matchStreamList.size()>=1){
                                   if(matchStreamList.get(0).masterTeam==null||matchStreamList.get(0).guestTeam==null){
                                       List<Team> listTeam=checkSteamTeam(tds.get(1).select("b").html(),matchName);
                                       if(listTeam!=null&&listTeam.size()>=1){
                                           matchStreamList.get(0).masterTeam=listTeam.get(0);
                                           if(listTeam.size()>=2){
                                               matchStreamList.get(0).guestTeam=listTeam.get(1);
                                           }
                                           matchStreamList.get(0).updateTime=new Date();
                                           matchStreamDao.save(matchStreamList.get(0));
                                       }
                                   }
                                    logger.warn("该matchStream已经抓取过了，跳过该记录 matchName="+matchName);
                                    continue;
                                }
                                MatchStream matchStream=new MatchStream();
                                matchStream.matchName=matchName;
                                matchStream.playTime=tds.get(0).html();
                                matchStream.project=tds.get(1).select("b").html();
                                matchStream.game=tds.get(2).html();
                                matchStream.matchStreamId=tds.get(4).select("font").html();
                                matchStream.matchStreamName=tds.get(5).select("font").html();
                                matchStream.addTime=date;
                                matchStream.updateTime=date;

                               List<Team> listTeam=checkSteamTeam(matchStream.project,matchName);
                                if(listTeam!=null&&listTeam.size()>=1){
                                    matchStream.masterTeam=listTeam.get(0);
                                    if(listTeam.size()>=2){
                                        matchStream.guestTeam=listTeam.get(1);
                                    }
                                }
                                saveMatchList.add(matchStream);
                            }catch (Exception e){
                                logger.error("抓取stream错误",e);
                            }
                        }
                    }

                    break;//只抓取第一个tbody
                }

                if(saveMatchList.size()>=1){
                    matchStreamDao.save(saveMatchList);
                }

            }catch (Exception e){
                logger.error("抓取 fetchMatchId=" + fetchMatchId, e);
            }
      //  }

    }
    public List<Team> checkSteamTeam(String project,String matchName) {

        List<Team> list=Lists.newArrayList();
        try{
        if (StringUtils.isEmpty(matchName)) {
            return null;
        }
        if(matchName.indexOf("VS")<0){
            return null;
        }
        String[] teamZhs=matchName.split("VS");
        if(teamZhs.length<=1){
            return null;
        }
        Team team1=   teamService.checkTeamNotSave(teamZhs[0].trim(), project);

        if(team1!=null){
            list.add(team1);
        }
        Team team2=   teamService.checkTeamNotSave(teamZhs[1].trim(), project);
        if(team2!=null){
            list.add(team2);
        }}catch (Exception e){
            e.printStackTrace();
        }
       return list;

    }


}
