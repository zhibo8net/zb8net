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
import website2018.domain.BasketballRank;
import website2018.domain.BasketballTelCommon;
import website2018.domain.FootballJsb;
import website2018.domain.FootballSsb;
import website2018.repository.BasketballRankDao;
import website2018.repository.BasketballTelCommonDao;
import website2018.repository.FootballJsbDao;
import website2018.repository.FootballSsbDao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class FootballJsbSpider extends BaseSpider {

    private static Logger logger = LoggerFactory.getLogger(FootballJsbSpider.class);

    @Autowired
    private FootballJsbDao footballJsbDao;

    @Autowired
    private FootballSsbDao footballSsbDao;
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

                    fetchFootballJsb();

                } catch (Exception e) {
                    logger.error(e.getLocalizedMessage());
                }
            }
        }, "FootballJsbSpider - " + System.currentTimeMillis());
        t.start();
    }

    @Transactional
    public void fetchFootballJsb() throws Exception {
        Document footballJsbDoc = readDocFromByJsoup("https://www.zhibo8.cc/zuqiu/");
        if (footballJsbDoc == null) {
            logger.info("从 https://www.zhibo8.cc/zuqiu/ 抓取数据为空");
            return;
        }

        //积分榜
         List<FootballJsb> footballJsbList=new ArrayList<>();
        //射手榜
        List<FootballSsb> footballSsbList=new ArrayList<>();

        Elements tbodys = footballJsbDoc.select("#jfb tbody");
        //循环bodys
        int i=0;
        for (Element tbody : tbodys) {

            for (Element tr : tbody.select("tr")) {
                Elements tds = tr.select("td");
                try{
                    if (tds.size() >=5) {
                        String type="积分榜";
                        String typeMold=(i==0?"中超":"中超");
                        typeMold=(i==1?"英超":typeMold);
                        typeMold=(i==2?"西甲":typeMold);
                        typeMold=(i==3?"意甲":typeMold);
                        typeMold=(i==4?"德甲":typeMold);
                        typeMold=(i==5?"法甲":typeMold);
                        String teamName=tds.get(1).select("a").html();
                        FootballJsb footballJsb= checkFootballJsb(type,typeMold,teamName);
                        footballJsb.teamName=teamName;
                        footballJsb.type=type;
                        footballJsb.typeMold=typeMold;
                        footballJsb.matchNum=tds.get(2).html();
                        footballJsb.score=tds.get(3).html();
                        footballJsb.ballNum=tds.get(3).html();
                        footballJsb.updateTime=new Date();
                        footballJsbList.add(footballJsb);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            i++;
        }


        Elements ogjfbtbodys = footballJsbDoc.select("#ogjfb tbody");
        //循环bodys
        int ogjfbi=0;
        for (Element tbody : ogjfbtbodys) {

            for (Element tr : tbody.select("tr")) {
                Elements tds = tr.select("td");
                try{
                    if (tds.size() >=5) {
                        String type="欧冠积分榜";
                        String typeMold=(ogjfbi==0?"A":"A");
                        typeMold=(ogjfbi==1?"B":typeMold);
                        typeMold=(ogjfbi==2?"C":typeMold);
                        typeMold=(ogjfbi==3?"D":typeMold);
                        typeMold=(ogjfbi==4?"E":typeMold);
                        typeMold=(ogjfbi==5?"F":typeMold);
                        typeMold=(ogjfbi==6?"G":typeMold);
                        typeMold=(ogjfbi==7?"H":typeMold);
                        String teamName=tds.get(1).html();
                        FootballJsb footballJsb= checkFootballJsb(type,typeMold,teamName);
                        footballJsb.teamName=teamName;
                        footballJsb.type=type;
                        footballJsb.typeMold=typeMold;
                        footballJsb.matchNum=tds.get(2).html();
                        footballJsb.score=tds.get(3).html();
                        footballJsb.ballNum=tds.get(4).html();
                        footballJsb.updateTime=new Date();
                        footballJsbList.add(footballJsb);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            ogjfbi++;
        }


        Elements ygjfbtbodys = footballJsbDoc.select("#ygjfb tbody");
        //循环bodys
        int ygjfbi=0;
        for (Element tbody : ygjfbtbodys) {

            for (Element tr : tbody.select("tr")) {
                Elements tds = tr.select("td");
                try{
                    if (tds.size() >=5) {
                        String type="亚冠积分榜";
                        String typeMold=(ogjfbi==0?"E":"E");
                        typeMold=(ogjfbi==1?"F":typeMold);
                        typeMold=(ogjfbi==2?"G":typeMold);
                        typeMold=(ogjfbi==3?"H":typeMold);
                        typeMold=(ogjfbi==4?"A":typeMold);
                        typeMold=(ogjfbi==5?"B":typeMold);
                        typeMold=(ogjfbi==6?"C":typeMold);
                        typeMold=(ogjfbi==7?"D":typeMold);
                        String teamName=tds.get(1).html();
                        FootballJsb footballJsb= checkFootballJsb(type,typeMold,teamName);
                        footballJsb.teamName=teamName;
                        footballJsb.type=type;
                        footballJsb.typeMold=typeMold;
                        footballJsb.matchNum=tds.get(2).html();
                        footballJsb.score=tds.get(3).html();
                        footballJsb.ballNum=tds.get(4).html();
                        footballJsb.updateTime=new Date();
                        footballJsbList.add(footballJsb);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            ygjfbi++;
        }
        //保存积分榜
        footballJsbDao.save(footballJsbList);

        //射手榜
        Elements ssbtbodys = footballJsbDoc.select("#ssb tbody");
        //循环bodys
        int ssbi=0;
        for (Element tbody : ssbtbodys) {

            for (Element tr : tbody.select("tr")) {
                Elements tds = tr.select("td");
                try{
                    if (tds.size() >=5) {
                        String type="射手榜";
                        String typeMold=(ssbi==0?"中超":"中超");
                        typeMold=(ssbi==1?"英超":typeMold);
                        typeMold=(ssbi==2?"西甲":typeMold);
                        typeMold=(ssbi==3?"意甲":typeMold);
                        typeMold=(ssbi==4?"德甲":typeMold);
                        typeMold=(ssbi==5?"法甲":typeMold);
                        typeMold=(ssbi==6?"欧冠":typeMold);
                        typeMold=(ssbi==7?"亚冠":typeMold);
                        String teamMember=tds.get(1).select("a").html();
                        String teamName=tds.get(2).html();
                        FootballSsb footballSsb= checkFootballSsb(type,typeMold,teamName,teamMember);
                        footballSsb.teamName=teamName;
                        footballSsb.type=type;
                        footballSsb.typeMold=typeMold;
                        footballSsb.teamMember=teamMember;
                        footballSsb.sumNum=tds.get(3).html();
                        footballSsb.dianBallNum=tds.get(4).html();

                        footballSsb.updateTime=new Date();
                        footballSsbList.add(footballSsb);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            ssbi++;
        }

        //保存射手榜
        footballSsbDao.save(footballSsbList);

    }

    public FootballJsb checkFootballJsb(String type,String typeMold,String teamName){
        FootballJsb footballJsb=footballJsbDao.findByTypeAndTypeMoldAndTeamName(type, typeMold, teamName);
        if(footballJsb==null){
            footballJsb=new FootballJsb();
            footballJsb.createTime=new Date();
        }
        return footballJsb;
    }
    public FootballSsb checkFootballSsb(String type,String typeMold,String teamName,String teamMember){
        FootballSsb footballSsb=null;
       List<FootballSsb>  footballSsbList=footballSsbDao.findByTypeAndTypeMoldAndTeamNameAndTeamMember(type, typeMold, teamName, teamMember);
        if(footballSsbList==null||footballSsbList.size()==0){
            footballSsb=new FootballSsb();
            footballSsb.createTime=new Date();
        }else{
            footballSsb=footballSsbList.get(0);
        }
        return footballSsb;
    }
}
