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
import website2018.domain.Match;
import website2018.domain.MatchStream;
import website2018.repository.MatchDao;
import website2018.repository.MatchStreamDao;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class MatchStreamUrlSpider extends BaseSpider {

    private static Logger logger = LoggerFactory.getLogger(MatchStreamUrlSpider.class);

    @Autowired
    private MatchStreamDao matchStreamDao;

    @Autowired
    MatchDao matchDao;

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
        }, "MatchStreamUrlSpider - " + System.currentTimeMillis());
        t.start();
    }

    @Transactional
    public void matchStreamFetch() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, -4);
        Date d=calendar.getTime();
        List<Match> matchList=matchDao.findByPlayDateGreaterThan(d);
        List<MatchStream> matchStreamList=matchStreamDao.findByUpdateTimeGreaterThan(d);

        for(Match match:matchList){
            if(StringUtils.isEmpty(match.name)){
                logger.warn("比赛名称为空退出循环");
                continue;
            }
            for(MatchStream matchStream:matchStreamList){
                if(StringUtils.isEmpty(matchStream.matchStreamName)){
                    logger.warn("比赛名称matchStreamName 为空退出循环");
                    continue;
                }
                if (match.name.equals(matchStream.matchName)){
                    String matchStreamUrl="http://27498.liveplay.myqcloud.com/live/27498_"+matchStream.matchStreamName+".m3u8";
                    match.matchStreamUrl=matchStreamUrl;
                    matchDao.save(match);
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
                    String matchStreamUrl="http://27498.liveplay.myqcloud.com/live/27498_"+matchStream.matchStreamName+".m3u8";
                    match.matchStreamUrl=matchStreamUrl;
                    matchDao.save(match);
                }
            }
        }
    }
}
