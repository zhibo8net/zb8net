package website2018.spider;

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
public class BasketballRankSpider extends BaseSpider {

    private static Logger logger = LoggerFactory.getLogger(BasketballRankSpider.class);

    @Autowired
    private BasketballRankDao basketballRankDao;

    @Autowired
    private BasketballTelCommonDao basketballTelCommonDao;

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

                    fetchBasketballRank();

                } catch (Exception e) {
                    logger.error(e.getLocalizedMessage());
                }
            }
        }, "BasketballRankSpider - " + System.currentTimeMillis());
        t.start();
    }

    @Transactional
    public void fetchBasketballRank() throws Exception {
        Document basketballRankDoc = readDocFromByJsoup("https://www.zhibo8.cc/nba/");
        if (basketballRankDoc == null) {
            logger.info("从 https://www.zhibo8.cc/nba/ 抓取数据为空");
            return;
        }

         List<BasketballRank> basketballRankList=new ArrayList<>();

        List<BasketballTelCommon> basketballTelCommonList=new ArrayList<>();

        Elements tbodys = basketballRankDoc.select("tbody");
        //循环bodys
        int i=0;
        for (Element tbody : tbodys) {

            for (Element tr : tbody.select("tr")) {
                Elements tds = tr.select("td");
                try{
                    if (tds.size() >=6) {
                       // System.out.println(tds.html());
                        //NBA西部 NBA东部抓取
                        if(i==0||i==1||i==7){
                            String type=(i==0?"NBA西部":"NBA东部");
                            type=(i==7?"CBA排名":type);
                            String teamName=tds.get(1).select("a").html();
                            Date date=new Date();
                             BasketballRank basketballRank=   checkBasketballRank(type,teamName);
                            basketballRank.type=type;
                            basketballRank.teamName=teamName;
                            basketballRank.winNum=tds.get(2).html();
                            basketballRank.failNum=tds.get(3).html();
                            basketballRank.winRate=tds.get(4).html();
                            if(i==7){
                                basketballRank.victories="-";
                                basketballRank.currentRemark=tds.get(5).html();
                            }else{
                                basketballRank.victories=tds.get(5).html();
                                basketballRank.currentRemark=tds.get(6).html();
                            }


                            basketballRank.updateTime=date;
                            basketballRankList.add(basketballRank);
                        }else if(i>=2&&i<=6){
                            //统计技术排名
                            String type=(i==2?"得分":"得分");
                            type=(i==3?"篮板":type);
                            type=(i==4?"助攻":type);
                            type=(i==5?"抢断":type);
                            type=(i==6?"盖帽":type);
                            String teamName=tds.get(1).select("a").html();
                            Date date=new Date();
                            BasketballTelCommon basketballTelCommon=   checkBasketballTelCommon(type,teamName);
                            basketballTelCommon.type=type;
                            basketballTelCommon.teamName=teamName;
                            basketballTelCommon.updateTime=date;
                            if(i==2){
                                basketballTelCommon.score=tds.get(2).html();
                                basketballTelCommon.rate=tds.get(3).html();
                                basketballTelCommon.matchNum=tds.get(4).html();
                                basketballTelCommon.timeNum=tds.get(5).html();
                            }else if(i==3){
                                basketballTelCommon.backboard=tds.get(2).html();
                                basketballTelCommon.sumNum=tds.get(3).html();
                                basketballTelCommon.timeNum=tds.get(4).html();
                                basketballTelCommon.matchNum=tds.get(5).html();
                            }else{
                                basketballTelCommon.matchAvg=tds.get(2).html();
                                basketballTelCommon.sumNum=tds.get(3).html();
                                basketballTelCommon.timeNum=tds.get(4).html();
                                basketballTelCommon.matchNum=tds.get(5).html();
                            }
                            basketballTelCommonList.add(basketballTelCommon);
                        }
                     }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            i++;
        }

        //保存篮球排名
        basketballRankDao.save(basketballRankList);
        //保存篮球技术排名
        basketballTelCommonDao.save(basketballTelCommonList);
    }

    public BasketballRank checkBasketballRank(String type,String teamName){
        BasketballRank basketballRank=basketballRankDao.findByTypeAndTeamName(type, teamName);
        if(basketballRank==null){
            basketballRank=new BasketballRank();
            basketballRank.createTime=new Date();
        }
        return basketballRank;
    }

    public BasketballTelCommon checkBasketballTelCommon(String type,String teamName){
        BasketballTelCommon basketballTelCommon=basketballTelCommonDao.findByTypeAndTeamName(type,teamName);
        if(basketballTelCommon==null){
            basketballTelCommon=new BasketballTelCommon();
            basketballTelCommon.createTime=new Date();
        }
        return basketballTelCommon;
    }
}
