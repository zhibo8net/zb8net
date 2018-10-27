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
import website2018.domain.*;
import website2018.repository.*;

import java.util.*;

@Component
public class MatchStreamSpider extends BaseSpider {

    private static Logger logger = LoggerFactory.getLogger(MatchStreamSpider.class);

    @Autowired
    private MatchStreamDao matchStreamDao;

    @Autowired
    TeamDao teamDao;

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
        for(int fetchMatchId=1;fetchMatchId<=6;fetchMatchId++){
            try {
                Document matchStreamDoc = readDocFrom("http://api.sstream365.com/?id=" + fetchMatchId);
                if (matchStreamDoc == null) {
                    logger.info("http://api.sstream365.com/?id="+fetchMatchId+" 抓取数据为空");
                    return;
                }
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, -1);
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
                logger.error("抓取 fetchMatchId="+fetchMatchId,e);
            }
        }

    }
    public List<Team> checkSteamTeam(String project,String matchName) {

        List<Team> list=Lists.newArrayList();

        if (StringUtils.isEmpty(matchName)) {
            return null;
        }
        if(matchName.indexOf("VS")<0){
            return null;
        }
        String[] teamZhs=matchName.split("VS");
        Team team1=   checkTeam(teamZhs[0].trim(), project);

        if(team1!=null){
            list.add(team1);
        }
        Team team2=   checkTeam(teamZhs[1].trim(),project);
        if(team2!=null){
            list.add(team2);
        }
       return list;

    }

    public Team checkTeam(String teamZh,String project){
        List<Team> tmListproject=teamDao.findByTeamZh(teamZh+project);
        if(tmListproject!=null&&tmListproject.size()>=1){
            return  tmListproject.get(0);
        }
        List<Team> tmList11=teamDao.findByTeamName1(teamZh+project);
        if(tmList11!=null&&tmList11.size()>=1){
            return  tmList11.get(0);
        }
        List<Team> tmList=teamDao.findByTeamZh(teamZh);
        if(tmList!=null&&tmList.size()>=1){
            return  tmList.get(0);
        }
        List<Team> tmList1=teamDao.findByTeamName1(teamZh);
        if(tmList1!=null&&tmList1.size()>=1){
            return  tmList1.get(0);
        }
        List<Team> tmList2=teamDao.findByTeamName2(teamZh);
        if(tmList2!=null&&tmList2.size()>=1){
            return  tmList2.get(0);
        }
        List<Team> tmList3=teamDao.findByTeamName3(teamZh);
        if(tmList3!=null&&tmList3.size()>=1){
            return  tmList3.get(0);
        }
        Team tm=new Team();
        tm.addTime=new Date();
        tm.updateTime=new Date();
        tm.teamZh = teamZh;
        logger.info("保存球队{}",tm.teamZh);
        return teamDao.save(tm);

    }
}
