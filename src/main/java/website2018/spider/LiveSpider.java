package website2018.spider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.google.common.collect.Lists;

import website2018.MyApplication;
import website2018.base.BaseSpider;
import website2018.domain.*;
import website2018.repository.*;
import website2018.utils.SpringContextHolder;

@Component
public class LiveSpider extends BaseSpider {
    private static Logger logger = LoggerFactory.getLogger(LiveSpider.class);

    @Autowired
    LiveSourceDao liveSourceDao;

    @Autowired
    MatchDao matchDao;

    @Autowired
    LiveDao liveDao;

    @Autowired
    TeamDao teamDao;

    @Autowired
    LeagueDao leagueDao;
    @Scheduled(cron = "0 0/3 * * * *")
    @Transactional
    public void runSchedule() throws Exception {
        if (MyApplication.DONT_RUN_SCHEDULED && !MyApplication.TEST_LIVE_SPIDER) {
            return;
        }

        final List<LiveSource> liveSources = liveSourceDao.findByActiveOrderByIdDesc(1);
        for (LiveSource liveSource : liveSources) {
            if (liveSource != null) {
                // 到了下一次抓取时间
                if (MyApplication.TEST_LIVE_SPIDER || (new Date().getTime() > ((liveSource.lastFetch.getTime()) + (liveSource.fetchInterval * 60 * 1000)))) {

                    liveSource.lastFetch = new Date();
                    liveSourceDao.save(liveSource);

                    // 启动新线程来抓取
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // 在新线程中打开Session
                                EntityManagerFactory entityManagerFactory = SpringContextHolder.getApplicationContext().getBean(EntityManagerFactory.class);
                                EntityManager entityManager = entityManagerFactory.createEntityManager();
                                EntityManagerHolder entityManagerHolder = new EntityManagerHolder(entityManager);
                                TransactionSynchronizationManager.bindResource(entityManagerFactory, entityManagerHolder);

                                fetchLive(liveSource);

                                // 关闭新线程的Session
                                TransactionSynchronizationManager.unbindResource(entityManagerFactory);
                                EntityManagerFactoryUtils.closeEntityManager(entityManager);
                            } catch (Exception e) {
                                e.printStackTrace();
                                logger.error(e.getLocalizedMessage());
                            }
                        }
                    }, "LiveSpider - " + System.currentTimeMillis());
                    t.start();

                } else {
                    logger.warn("当前直播源是" + liveSource.name + "，未到抓取时间，不抓取");
                }
            }
        }
    }

    @Transactional
    public void testFetch() {
        // LiveSource liveSource = liveSourceDao.findByActive(1);
        //fetchLive(liveSource);
    }

    @Transactional
    public void fetchLive(LiveSource liveSource) {

        String channels = ".*(" + liveSource.channels.replace(",", "|") + ").*";
        List<Match> entitys = Lists.newArrayList();
        try {
            if (liveSource.name.equals("直播吧")) {
                fetchFromZhibo8(entitys, channels);
            } else if (liveSource.name.equals("CCAV5")) {
                fetchFromCcav5(entitys, channels);
            } else if (liveSource.name.equals("A直播")) {
                fetchFromAzhibo(entitys, channels);
            } else if (liveSource.name.equals("无插件直播")) {
                fetchFromWuchajian(entitys, channels, liveSource.link);
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        // 确实解析到entitys了，才入库，如果读取网页超时则不
        if (entitys.size() > 0) {
            //CCTV5播的，视为重点
            for (Match m : entitys) {
                for (Live l : m.lives) {
                    if (l.name.contains("CCTV5")) {
                        m.emphasis = 1;
                    }
                }
            }
            matchDao.save(entitys);

            logger.warn("从" + liveSource.name + "抓取了首页比赛");
        } else {
            logger.warn("未能从" + liveSource.name + "抓取首页比赛");
        }
    }

    public void fetchFromZhibo8(List<Match> entitys, String channels) throws Exception {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        Document mzhibo8 = readDocFromByJsoup("https://m.zhibo8.cc");

        // 如果返回值不为空，即获得网页没有超时
        if (mzhibo8 != null) {

            Elements saishis = mzhibo8.select(".saishi");
            for (Element saishi : saishis) {
                // String playDateStr = saishi.select("h2.current").html();
                for (Element li : saishi.select("li")) {
                    boolean willSaveMatch = true;// 标识是否将要把Entity入库，如果锁定且未过期，则为false

                    String source = "http:" + li.select("h2 a").attr("href");
                    System.out.println(source);
                    Match maybeExistedEntity = matchDao.findBySource(source);

                    if (maybeExistedEntity != null) {// 已经抓取过
                        if (maybeExistedEntity.locked == 1) {// 如果是锁定状态
                            if(maybeExistedEntity.unlockTime!=null){
                            if (maybeExistedEntity.unlockTime.getTime() > new Date().getTime()) {// 锁定未过期
                                willSaveMatch = false;
                            } else {// 锁定已过期
                                // 解锁
                                maybeExistedEntity.locked = 0;
                            }
                            }else{
                                willSaveMatch = false;
                            }
                        }
                        if (willSaveMatch) {// 不是锁定状态，或者已经解锁，清空
                            //System.out.println("将清空Match的直播链接，比赛：" + maybeExistedEntity.name + "长度："
                            //        + maybeExistedEntity.lives.size());

                            List<Live> oldLives = Lists.newArrayList();

                            for (Live live : maybeExistedEntity.lives) {
                                oldLives.add(live);
                            }

                            maybeExistedEntity.lives.clear();
                            liveDao.delete(oldLives);
                        }
                    } else {
                        //System.out.println("将进行全新的入库……");
                        maybeExistedEntity = new Match();
                    }

                    // 至此，maybeExistedEntity可能是新对象（新增的情况）或已清空直播的老对象（修改的情况），用于将抓取到的数据填充进去

                    if (willSaveMatch) {

                        String type = li.attr("type").trim();
                        String project = "其他";
                        if (type.equals("basketball")) {
                            project = "篮球";
                        } else if (type.equals("football")) {
                            project = "足球";
                        }
                        Elements tds = li.select("td");

                        // 赛事名称
                        String game;
                        Elements sname = li.select(".s_name");
                        if (sname.select("b").size() > 0) {
                            game = sname.select("b").html();
                        } else {
                            game = sname.html();
                        }
                        //将“NBA第24轮”格式化为“NBA”
                        String formated = game(game);
                        if (!StringUtils.isBlank(formated)) {
                            game = formated;
                        }

                        // String timeStr = tds.get(0).html();
                        Elements homeDivs = tds.get(1).select("div");
                        String home = homeDivs.size() == 1 ? homeDivs.html() : homeDivs.select("b").html();
                        Elements awayDivs = tds.get(3).select("div");
                        String away = awayDivs.size() == 1 ? awayDivs.html() : awayDivs.select("b").html();

                        String playDateStr = tds.select(".hideTime").html();
                        Date playDate = sdf.parse(playDateStr);
                        String dateStr = playDateStr.split(" ")[0];
                        String timeStr = playDateStr.split(" ")[1];

                        maybeExistedEntity.playDate = playDate;
                        maybeExistedEntity.playDateStr = dateStr;
                        maybeExistedEntity.playTime = timeStr;
                        maybeExistedEntity.project = project;
                        maybeExistedEntity.game = game;
                        maybeExistedEntity.name = StringUtils.isNotBlank(away) ? (home + " VS " + away) : home;
                        if(StringUtils.isNotBlank(away)){
                            Team team1=  checkTeam(home,project);
                            Team team2=  checkTeam(away,project);
                            maybeExistedEntity.masterTeam=team1;
                            maybeExistedEntity.guestTeam=team2;
                        }
                        maybeExistedEntity.source = source;
                        maybeExistedEntity.locked = 0;
                        maybeExistedEntity.emphasis = 0;

                        source = source.replace("http://m.zhibo8.cc/", "https://www.zhibo8.cc/");
                        Document lives = readDocFrom(source);
                        if (lives != null) {
                            Elements es = lives.select(".video a,.ft_video a");
                            for (Element l : es) {
                                String text = l.html();
                                String link = l.attr("href");
                                if (text.matches(channels)) {
                                    //System.out.println(text + "匹配");
                                    Live live = new Live();
                                    live.match = maybeExistedEntity;
                                    live.name = text;
                                    live.link = link;
                                    live.addTime = new Date();
                                    maybeExistedEntity.lives.add(live);
                                } else {
                                    //System.out.println(text + "不匹配");
                                }
                            }
                            maybeExistedEntity.addTime = new Date();
                            entitys.add(maybeExistedEntity);
                        }
                        Thread.sleep(100);
                    }

                }
            }
        }
    }

    public void fetchFromCcav5(List<Match> entitys, String channels) throws Exception {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        logger.error("CCAV5网页：");
        logger.error(readFromUrl("http://www.ccav5.com/"));

        Document mzhibo8 = readDocFrom("http://www.ccav5.com/");
        Elements matchs = mzhibo8.select(".live_lists .left");

        System.out.println("CCAV5比赛数量：" + matchs.size());
        for (Element m : matchs) {
            try {
                boolean willSaveMatch = true;// 标识是否将要把Entity入库，如果锁定且未过期，则为false

                String source = m.select("a").get(1).attr("href");
                String playDateStr = m.select("a").get(1).attr("timefrom");
                Match maybeExistedEntity = matchDao.findBySource(source);

                if (maybeExistedEntity != null) {// 已经抓取过
                    if (maybeExistedEntity.locked == 1) {// 如果是锁定状态
                        if (maybeExistedEntity.unlockTime.getTime() > new Date().getTime()) {// 锁定未过期
                            willSaveMatch = false;
                        } else {// 锁定已过期
                            // 解锁
                            maybeExistedEntity.locked = 0;
                        }
                    }
                    if (willSaveMatch) {// 不是锁定状态，或者已经解锁，清空
                        System.out.println("将清空Match的直播链接，ID：" + maybeExistedEntity.id + "长度："
                                + maybeExistedEntity.lives.size());

                        List<Live> oldLives = Lists.newArrayList();

                        for (Live live : maybeExistedEntity.lives) {
                            oldLives.add(live);
                        }

                        maybeExistedEntity.lives.clear();
                        liveDao.delete(oldLives);
                    }
                } else {
                    System.out.println("将进行全新的入库……");
                    maybeExistedEntity = new Match();
                }

                // 至此，maybeExistedEntity可能是新对象（新增的情况）或已清空直播的老对象（修改的情况），用于将抓取到的数据填充进去

                if (willSaveMatch) {
                    Document innerDoc = readDocFrom(source);

                    String type = innerDoc.select(".breadcrumbs a").get(1).html();
                    String project = "其他";
                    if (type.contains("篮球")) {
                        project = "篮球";
                    } else if (type.contains("足球")) {
                        project = "足球";
                    }

                    // 赛事名称
                    String game = innerDoc.select(".breadcrumbs a").get(2).html().replace("直播", "");
                    if (game.equals("")) {
                        game = game(innerDoc.select(".event").html());
                    }

                    String[] homeAndAway = innerDoc.select(".breadcrumbs a").get(3).html().split("vs");

                    String home = homeAndAway[0];
                    String away = homeAndAway.length == 2 ? homeAndAway[1] : "";

                    Date playDate = sdf.parse(playDateStr);
                    String dateStr = playDateStr.split(" ")[0];
                    String timeStr = playDateStr.split(" ")[1];

                    maybeExistedEntity.playDate = playDate;
                    maybeExistedEntity.playDateStr = dateStr;
                    maybeExistedEntity.playTime = timeStr;
                    maybeExistedEntity.project = project;
                    maybeExistedEntity.game = game;
                    maybeExistedEntity.name = StringUtils.isNotBlank(away) ? (home + " VS " + away) : home;
                    maybeExistedEntity.source = source;
                    maybeExistedEntity.locked = 0;
                    maybeExistedEntity.emphasis = 0;

                    for (Element l : innerDoc.select("#live_channel_container .left a")) {
                        String text = l.html();
                        String link = l.attr("href");
                        if (text.matches(channels)) {
                            Live live = new Live();
                            live.match = maybeExistedEntity;
                            live.name = text;
                            live.link = link;
                            live.addTime = new Date();
                            maybeExistedEntity.lives.add(live);
                        }
                    }
                    maybeExistedEntity.addTime = new Date();
                    entitys.add(maybeExistedEntity);
                }
            } catch (Exception e) {
                //Syso...
            } finally {
                Thread.sleep(100);
            }
        }
    }

    public void fetchFromAzhibo(List<Match> entitys, String channels) throws Exception {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        Document mzhibo8 = readDocFrom("http://m.azhibo.com/");
        Elements dayBlocks = mzhibo8.select("#match-container .box");
        for (int i = 1; i < dayBlocks.size(); i++) {
            Element dayBlock = dayBlocks.get(i);
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE, i - 1);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);

            Elements items = dayBlock.select(".item");
            itEach:
            for (Element it : items) {
                try {
                    String time = it.select(".time").html();
                    c.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time.split(":")[0]));
                    c.set(Calendar.MINUTE, Integer.valueOf(time.split(":")[1]));
                    Date playDate = c.getTime();

                    String project = "其他";
                    if (it.classNames().contains("lanqiu")) {
                        project = "篮球";
                    } else if (it.classNames().contains("zuqiu")) {
                        project = "足球";
                    }

                    String game = it.select(".league").html();
                    String[] homeAndAway = it.select(".match-name").html().split("-");
                    String home = homeAndAway[0];
                    String away = homeAndAway[1];

                    boolean willSaveMatch = true;// 标识是否将要把Entity入库，如果锁定且未过期，则为false

                    Elements channelLinks = it.select("a.channel");

                    if (channelLinks.size() > 0) {

                        String source = "http://m.azhibo.com" + channelLinks.get(0).attr("href");
                        if (source.indexOf("#") != -1) {
                            source = source.substring(0, source.indexOf("#"));
                        }
                        Match maybeExistedEntity = matchDao.findBySource(source);

                        if (maybeExistedEntity != null) {// 已经抓取过
                            if (maybeExistedEntity.locked == 1) {// 如果是锁定状态
                                if (maybeExistedEntity.unlockTime.getTime() > new Date().getTime()) {// 锁定未过期
                                    willSaveMatch = false;
                                } else {// 锁定已过期
                                    // 解锁
                                    maybeExistedEntity.locked = 0;
                                }
                            }
                            if (willSaveMatch) {// 不是锁定状态，或者已经解锁，清空
                                //System.out.println("将清空Match的直播链接，ID：" + maybeExistedEntity.id + "长度："
                                //        + maybeExistedEntity.lives.size());

                                List<Live> oldLives = Lists.newArrayList();

                                for (Live live : maybeExistedEntity.lives) {
                                    oldLives.add(live);
                                }

                                maybeExistedEntity.lives.clear();
                                liveDao.delete(oldLives);
                            }
                        } else {
                            //System.out.println("将进行全新的入库……");
                            maybeExistedEntity = new Match();
                        }

                        // 至此，maybeExistedEntity可能是新对象（新增的情况）或已清空直播的老对象（修改的情况），用于将抓取到的数据填充进去

                        if (willSaveMatch) {
                            Document innerDoc = readDocFrom(source);

                            String playDateStr = sdf.format(playDate);
                            String dateStr = playDateStr.split(" ")[0];
                            String timeStr = playDateStr.split(" ")[1];

                            maybeExistedEntity.playDate = playDate;
                            maybeExistedEntity.playDateStr = dateStr;
                            maybeExistedEntity.playTime = timeStr;
                            maybeExistedEntity.project = project;
                            maybeExistedEntity.game = game;
                            maybeExistedEntity.name = StringUtils.isNotBlank(away) ? (home + " VS " + away) : home;
                            maybeExistedEntity.source = source;
                            maybeExistedEntity.locked = 0;
                            maybeExistedEntity.emphasis = 0;

                            Elements es = innerDoc.select("#plugin-list a.item");
                            System.out.println(es.size());
                            for (Element l : es) {
                                String text = l.text();
                                String link = l.attr("href");
                                if (text.matches(channels)) {
                                    //System.out.println(text + "匹配");
                                    Live live = new Live();
                                    live.match = maybeExistedEntity;
                                    live.name = text;
                                    live.link = link;
                                    live.addTime = new Date();
                                    maybeExistedEntity.lives.add(live);
                                } else {
                                    //System.out.println(text + "不匹配");
                                }
                            }
                            maybeExistedEntity.addTime = new Date();
                            entitys.add(maybeExistedEntity);
                            Thread.sleep(100);
                        }
                    }
                } catch (Exception e) {
                    continue itEach;
                }

            }

        }

    }

    public void fetchFromWuchajian(List<Match> entitys, String channels, String link) throws Exception {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        Document mzhibo8 = readDocFromByJsoup(link);

        // 如果返回值不为空，即获得网页没有超时
        if (mzhibo8 != null) {

            Elements saishis = mzhibo8.select(".against");
            for (Element saishi : saishis) {
                boolean willSaveMatch = true;// 标识是否将要把Entity入库，如果锁定且未过期，则为false
                String source = null;
                for (Element asource : saishi.select(".live_link a")) {
                    source = link + asource.attr("href").replace("..", "");
                    break;
                }
                Elements tds = saishi.select("td");
                String name = "";
                String playDateStr = "";
                if (tds.size() <= 8) {
                    name = tds.get(4).html();
                    playDateStr = tds.get(7).attr("t");
                } else {
                    name = tds.get(4).select("strong").html() + " VS " + tds.get(6).select("strong").html();
                    playDateStr = tds.get(9).attr("t");
                }


                Date playDate = sdf.parse(playDateStr);

                Match maybeExistedEntity = null;
                try {
                    maybeExistedEntity = matchDao.findByNameAndPlayDate(name,playDate);

                    if (maybeExistedEntity != null) {// 已经抓取过
                        if (maybeExistedEntity.locked == 1) {// 如果是锁定状态
                            if (maybeExistedEntity.unlockTime.getTime() > new Date().getTime()) {// 锁定未过期
                                willSaveMatch = false;
                            } else {// 锁定已过期
                                // 解锁
                                maybeExistedEntity.locked = 0;
                            }
                        }
                        if (willSaveMatch) {// 不是锁定状态，或者已经解锁，清空
                            //System.out.println("将清空Match的直播链接，比赛：" + maybeExistedEntity.name + "长度："
                            //        + maybeExistedEntity.lives.size());

                            List<Live> oldLives = Lists.newArrayList();

                            for (Live live : maybeExistedEntity.lives) {
                                oldLives.add(live);
                            }

                            maybeExistedEntity.lives.clear();
                            liveDao.delete(oldLives);
                        }
                    } else {
                        //System.out.println("将进行全新的入库……");
                        maybeExistedEntity = new Match();
                    }
                } catch (Exception e) {
                   continue;
                }
                try {
                    // 至此，maybeExistedEntity可能是新对象（新增的情况）或已清空直播的老对象（修改的情况），用于将抓取到的数据填充进去

                    if (willSaveMatch) {

                        String type = tds.get(0).select("img").attr("alt");
                        String project = "其他";
                        if (type.equals("篮球")) {
                            project = "篮球";
                        } else if (type.equals("足球")) {
                            project = "足球";
                        }


                        // 赛事名称
                        String game = tds.get(1).select("a").html();

                        //将“NBA第24轮”格式化为“NBA”
                        String formated = game(game);
                        if (!StringUtils.isBlank(formated)) {
                            game = formated;
                        }
                        if (tds.size() > 8) {
                            Team team1=  checkTeam(tds.get(4).select("strong").html(),project);
                            Team team2=  checkTeam(tds.get(6).select("strong").html(),project);
                            maybeExistedEntity.masterTeam=team1;
                            maybeExistedEntity.guestTeam=team2;

                        }

                        String dateStr = playDateStr.split(" ")[0];
                        String timeStr = playDateStr.split(" ")[1];

                        maybeExistedEntity.playDate = playDate;
                        maybeExistedEntity.playDateStr = dateStr;
                        maybeExistedEntity.playTime = timeStr;
                        maybeExistedEntity.project = project;
                        League lg=checkLeague(game);
                        maybeExistedEntity.league=lg;
                        maybeExistedEntity.game = game;
                        maybeExistedEntity.name = name;
                        maybeExistedEntity.source = source;
                        maybeExistedEntity.locked = 0;
                        maybeExistedEntity.emphasis = 0;


                        for (Element asource : saishi.select(".live_link a")) {

                            if ("更多".equals(asource.html())) {
                                break;
                            }
                            String sc = asource.attr("href").replace("..", "");
                            if (!sc.startsWith("http")) {
                                source = link + sc;
                            }else {
                                source=sc;
                            }
                            Document lives = readDocFrom(source);
                            if (lives != null) {
                                Elements es = lives.select(".mediabg");
                                for (Element l : es) {
                                    String text = l.select(".video_tit").html();
                                    String lk = l.select(".media iframe").attr("src");
                                    if (text.matches(channels)) {
                                        //System.out.println(text + "匹配");

                                        if (StringUtils.isNotBlank(lk)) {
//
                                            text = text.replace("无插件直播", "");

                                            Live live = new Live();

                                            live.match = maybeExistedEntity;
                                            live.name = text;
                                            live.gameId= lk.substring(lk.indexOf("?id=") + 4);
                                            live.addTime = new Date();
                                            if (text.matches(".*(PPTV).*")) {
                                                live.link = "http://pub.pptv.com/player/iframe/index.html?#w=1000&h=480&rcc_id=500W&id=" + lk.substring(lk.indexOf("?id=") + 4);
                                                live.name ="PPTV";
                                                live.videoLink="http://pub.pptv.com/player/iframe/index.html?rcc_id=500W#w=1000&h=480&id="+ lk.substring(lk.indexOf("?id=") + 4);
                                            } else if (text.matches(".*(CCTV).*")) {
                                                live.link = "http://tv.cctv.com/live/cctv5/";
                                                if(text.matches(".*(CCTV5).*")){
                                                    live.name ="CCTV5";
                                                }else{
                                                    live.name ="CCTV";
                                                }
                                             //   live.videoLink="http://mgzb.live.miguvideo.com:8088/wd_r2/cctv/cctv5hdnew/350/index.m3u8?msisdn=&mdspid=&spid=699004&netType=0&sid=5500516171&pid=2028597139&timestamp=20180811120501&Channel_ID=1004_10010001005&ProgramID=641886683&ParentNodeID=-99&assertID=5500516171&client_ip=122.228.208.14&SecurityKey=20180811120501&promotionId=&mvid=&mcid=&mpid=&encrypt=6019c8625cff77c97cffde213ef753c2";


                                            } else if (text.matches(".*(企鹅).*")) {
                                                live.link = "http://live.qq.com/" + lk.substring(lk.indexOf("?id=") + 4);
                                                live.name ="企鹅超清直播";
                                            } else if (text.matches(".*(章鱼).*")) {
                                                live.link = "http://www.zhangyu.tv/" + lk.substring(lk.indexOf("?id=") + 4);
                                                live.name ="章鱼直播";
                                            } else if (text.matches(".*(直播迷).*")) {
                                                live.link = "http://v.zhibo.leagueoffists.com/" + lk.substring(lk.indexOf("?id=") + 4);
                                                live.name ="直播迷(直播TV)";
                                            } else if (text.matches(".*(龙珠体育频道).*")) {
                                                live.link = "http://star.longzhu.com/" + lk.substring(lk.indexOf("?id=") + 4);
                                                live.name ="龙珠体育频道";
                                            } else if (text.matches(".*(腾讯).*")) {
                                                live.link = "https://sports.qq.com/kbsweb/game.htm?mid=" + lk.substring(lk.indexOf("?id=") + 4);
                                                live.name ="腾讯高清直播";
                                            }else if (text.matches(".*(ZF).*")) {
                                                live.link = "http://www.entgroup.com/" + lk.substring(lk.indexOf("?id=") + 4);
                                                live.name ="ZF直播";
                                            }else if (text.matches(".*(上海).*")) {
                                                live.link = "http://www.qwball.com/tv/wxty.html";
                                                live.name ="上海五星体育";
                                            }else if (text.matches(".*(俄罗斯).*")) {
                                                Document els = readDocFrom("http://sportstream365.com/");
                                                Elements elsurl = els.select("script");
                                                for( Element e1:elsurl){

                                                    if(e1.html().matches(".*(var tagz =).*")){

                                                        String s=e1.html();


                                                        String tagz=s.substring(12,s.length()-2);
                                                        System.out.println(tagz);
                                                        live.link = "http://sportstream365.com/viewer?sport=1&game="+ lk.substring(lk.indexOf("?id=") + 4)+"&tagz="+tagz;
                                                        live.name ="俄罗斯体育频道";
                                                        Document vd= readDocFromByJsoup(lk);
                                                        Elements velsurl = vd.select("script");
                                                        for( Element ve1:velsurl) {

                                                            int b=ve1.html().indexOf("data=\\\"");
                                                            String v=  ve1.html().substring(b+7, b + 274);
                                                            live.videoLink=v;
                                                            break;
                                                        }
                                                        break;
                                                    }
                                                }

                                            }
                                            if (!StringUtils.isEmpty(live.link)) {

                                                maybeExistedEntity.lives.add(live);
                                            }

                                        }


                                    } else {
                                        //System.out.println(text + "不匹配");
                                    }
                                }
                                maybeExistedEntity.addTime = new Date();
                                entitys.add(maybeExistedEntity);
                            }
                            Thread.sleep(100);
                        }


                    }
                } catch (Exception e) {
                    System.out.println("抓取失败，跳过" + e);
                }

            }

        }
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
    public League checkLeague(String leagueZh){
        League league=leagueDao.findByLeagueZh(leagueZh);
        if(league!=null){
            return  league;
        }

        league=new League();
        league.addTime=new Date();
        league.updateTime=new Date();
        league.leagueZh = leagueZh;
        logger.info("保存球队{}",league.leagueZh);
        return leagueDao.save(league);

    }
    public static void main(String[] args)throws Exception{




    }
}
