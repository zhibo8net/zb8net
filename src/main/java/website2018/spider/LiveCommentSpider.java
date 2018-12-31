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
import website2018.domain.Comment;
import website2018.domain.Match;
import website2018.domain.User;
import website2018.repository.CommentDao;
import website2018.repository.MatchDao;
import website2018.repository.UserDao;
import website2018.service.BaoWeiService;
import website2018.service.FetchCommentService;
import website2018.utils.DateUtils;
import website2018.utils.StrUtils;

import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class LiveCommentSpider extends BaseSpider {

    private static Logger logger = LoggerFactory.getLogger(LiveCommentSpider.class);


    @Autowired
    MatchDao matchDao;

    @Autowired
    BaoWeiService baoWeiService;

    @Autowired
    FetchCommentService fetchCommentService;

    @Autowired
    UserDao userDao;

    @Autowired
    CommentDao commentDao;

    @Scheduled(cron = "0 0/5 * * * *")
    @Transactional
    public void runSchedule() throws Exception {


        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    liveCommentFetch();
                } catch (Exception e) {
                    logger.error(e.getLocalizedMessage());
                }
            }
        }, "LiveCommentSpider - " + System.currentTimeMillis());
        t.start();
    }


    public void liveCommentFetch() throws Exception {
        try {
            Calendar tempcreate = Calendar.getInstance();
            tempcreate.add(Calendar.MINUTE,-6);

            Calendar tempEnd = Calendar.getInstance();
            tempEnd.add(Calendar.HOUR,-3);
            List<Match> matchList=matchDao.findByPlayDateGreaterThan(tempEnd.getTime());

            for(Match match:matchList){
                try {
                    if(StringUtils.isEmpty(match.source)){
                        logger.warn("该比赛没有直播源地址source,退出抓取评论 比赛名称{}",match.name);
                        continue;
                    }

                    //拆分source地址
                    String[] sources=match.source.split("\\/");
                    int len=sources.length;
                    if(len<=5){
                        logger.warn("该比赛直播源地址source 长度不够{},退出抓取评论 比赛名称{}",len,match.name);
                        continue;
                    }

                    String ccId=sources[len-1].split("\\.")[0];
                    String ccYear=sources[len-2];
                    String ccType=sources[len-3];

                    String pageNumUrl="http://dan.zhibo8.cc/data/"+ccYear+"/"+ccType+"/"+ccId+"_count.htm";
                    String  doc = readDocFromByJsoupReqJson(pageNumUrl);
                    if(doc!=null) {
                        JSONObject jsonObject = JSONObject.fromObject(doc);
                        Integer num= (Integer) jsonObject.get("num");
                       if(num==null){
                           continue;
                       }
                        int pageLast=num%100==0?num/100:num/100+1;
                        pageLast=pageLast-1;
                    String commentPageUrl="http://cache.zhibo8.cc/json/"+ccYear+"/"+ccType+"/"+ccId+"_"+pageLast+".htm";
                    String  comnentDoc = readDocFromByJsoupReqJson(commentPageUrl);

                        if(comnentDoc==null){
                            logger.warn("抓取评论数据为空{}, {}",match.name,commentPageUrl);
                            continue;
                        }
                        JSONArray jsonArray = JSONArray.fromObject(comnentDoc);
                      innerBreak:  for (int i = 0; i < jsonArray.size(); i++) {

                            if(i>=6){
                                break innerBreak;
                            }
                            JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                            String userName= jsonObject2.get("username")==null?null: (String) jsonObject2.get("username");
                            String content= jsonObject2.get("content")==null?null: (String) jsonObject2.get("content");
                            Date createTime= DateUtils.getDate((String) jsonObject2.get("createtime"), "yyyy-MM-dd HH:mm:ss");

                           if(tempcreate.getTime().after(createTime)){
                               break innerBreak;
                           }
                            List<String> sensitiveList= CacheUtils.getSensitiveList();
                            for(String str:sensitiveList){
                                content=content.replaceAll(str,"*");
                            }
                          String st= (String) CacheUtils.fecthCacheComment.getIfPresent("ZHIBO_" + match.id + "_" + DateUtils.getDateStr(createTime, "yyyyMMddHHmmss"));
                            if (StringUtils.isNotEmpty(st)){
                                logger.warn("缓存 该评论已经抓取过，{}", content);
                                continue;
                            }

                            if( jsonObject2.get("username")!=null&& jsonObject2.get("content")!=null){

                                content= StrUtils.delHTMLTag(content);
                                content=StrUtils.fliterFourUnicode(content);
                                userName= StrUtils.delHTMLTag(userName);
                                userName=StrUtils.fliterFourUnicode(userName);
                                List<Comment> commentList= commentDao.findByRelIdAndTypeAndUserTypeAndCommentAndAddTime(Integer.parseInt(match.id + ""), 1, 1, content, createTime);
                                if(commentList!=null&&commentList.size()>0){
                                    logger.warn("数据库 该评论已经抓取过，{}",content);
                                    CacheUtils.fecthCacheComment.put("ZHIBO_" + match.id + "_" + DateUtils.getDateStr(createTime, "yyyyMMddHHmmss"), "1");

                                    continue;
                                }
                                    User userRandowm=fetchCommentService.getUserRandom();
                                    if(userRandowm==null){
                                        continue;
                                    }
                                    if(StringUtils.isEmpty(userRandowm.userNickName)){
                                        userRandowm.userNickName=userName;
                                        userRandowm.updateTime=new Date();
                                        userDao.save(userRandowm);
                                        CacheUtils.fecthUserCache.cleanUp();
                                    }

                                   Comment comment=new Comment();
                                    comment.type=1;
                                    comment.user=userRandowm;
                                    comment.userType=1;
                                    comment.relId=Integer.parseInt(match.id+"");

                                    comment.comment=content;
                                    comment.addTime=createTime;
                                    comment.updateTime=createTime;

                                    commentDao.save(comment);
                                    CacheUtils.fecthCacheComment.put("ZHIBO_"+match.id+"_"+DateUtils.getDateStr(createTime,"yyyyMMddHHmmss"),"1");


                            }

                        }

                    }

                } catch (Exception e) {
                    logger.error("抓取比赛评论错误，{} {}",match.name, e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("liveCommentFetch{}", e);
        }

    }

    public static void main(String[] args) throws Exception {
        LiveCommentSpider l=new LiveCommentSpider();
        String  doc =l.readDocFromByJsoupReqJson("http://dan.zhibo8.cc/data/2018/nba/1222131155_count.htm");

        System.out.println(doc);
    }
}
