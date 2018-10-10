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
import website2018.domain.League;
import website2018.domain.Match;
import website2018.domain.Project;
import website2018.domain.Team;
import website2018.repository.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ProjectSpider extends BaseSpider {

    private static Logger logger = LoggerFactory.getLogger(ProjectSpider.class);

    @Autowired
    ProjectDao projectDao;
    @Autowired
    MatchDao matchDao;

    @Autowired
    LeagueDao leagueDao;

    @Autowired
    TeamDao teamDao;

    @Scheduled(cron = "0 0/15 * * * *")
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
                    calendar.add(Calendar.DATE, -1);
                    Date d=calendar.getTime();
                    fetchProject(d);
                    fetcLeague(d);
                } catch (Exception e) {
                    logger.error(e.getLocalizedMessage());
                }
            }
        }, "ProjectSpider - " + System.currentTimeMillis());
        t.start();
    }

    @Transactional
    public void fetchProject(Date d) throws Exception {
        try {
            List list = matchDao.findByAddTimeGreaterThan(d);
            for (Object object : list) {
                try {
                    Object[] obj = (Object[]) object;
                    Project project;
                    project = projectDao.findByProjectZh(obj[1] + "");
                    if (project == null) {
                        project = new Project();
                        project.projectZh = obj[1] + "";
                        project.addTime = new Date();
                    }
                    project.updateTime = new Date();
                    project.leagueNum = Integer.parseInt(obj[0] + "");
                    logger.info("保存项目{}",project.projectZh);
                    projectDao.save(project);
                } catch (Exception e) {
                    logger.error("抓取project项目错误{}", e);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("抓取project项目错误{}", e);
        }

    }

    @Transactional
    public void fetcLeague(Date d) throws Exception {

        List<Project> projectList= (List<Project>) projectDao.findAll();
        for(Project project:projectList){
            List<Match> matchList=matchDao.findByAddTimeGreaterThanAndProject(d,project.projectZh);
            saveLeagueAndTeam(matchList,project);
        }
    }

    public void saveLeagueAndTeam( List<Match> matchList,Project project ){
        List<String> league=new ArrayList<>();//总联赛
        List<String> team=new ArrayList<>();//总球队数
        Map<String,List<String>> leagueTeamNum=new HashMap<String,List<String>>();//联赛球队数
        for(Match match:matchList){
            if(StringUtils.isNotEmpty(match.game)){
                if(!league.contains(match.game)){
                    league.add(match.game);
                }
                if(StringUtils.isNotEmpty(match.name)){
                   List<String> leagueTeams=leagueTeamNum.get(match.game);
                    if(leagueTeams==null||leagueTeams.size()==0){
                        leagueTeams=new ArrayList<>();
                    }
                    String nm=match.name;

                    if(nm.indexOf("：")>=0){
                        nm=nm.split("\\：")[1];
                    }
                    if(nm.indexOf("： ")>=0){
                        nm=nm.split("\\： ")[1];
                    }
                    nm= nm.replaceAll(" ", "");
                    String[] teames1=nm.split("VS");
                    if(teames1.length>=2){
                        if(!leagueTeams.contains(teames1[0])){
                            leagueTeams.add(teames1[0]);
                        }
                        if(!leagueTeams.contains(teames1[1])){
                            leagueTeams.add(teames1[1]);
                        }
                        if(!team.contains(teames1[0])){
                            team.add(teames1[0]);
                        }
                        if(!team.contains(teames1[1])){
                            team.add(teames1[1]);
                        }
                    }
                    String[] teames2=match.name.split("vs");
                    if(teames2.length>=2){
                        if(!leagueTeams.contains(teames2[0])){
                            leagueTeams.add(teames2[0]);
                        }
                        if(!leagueTeams.contains(teames2[1])){
                            leagueTeams.add(teames2[1]);
                        }
                        if(!team.contains(teames2[0])){
                            team.add(teames2[0]);
                        }
                        if(!team.contains(teames2[1])){
                            team.add(teames2[1]);
                        }
                    }

                    leagueTeamNum.put(match.game,leagueTeams);
                }
            }
        }
        //保存联赛数目
           for(String lgStr:league){
               try {
                   League lg=leagueDao.findByLeagueZh(lgStr);
                   if(lg==null){
                       lg=new League();
                       lg.leagueZh=lgStr;
                       lg.projectId=project.id;
                       lg.projectZh=project.projectZh;
                       lg.addTime=new Date();
                   }

                   lg.updateTime=new Date();
                   List lt=leagueTeamNum.get(lgStr);
                   if(lt!=null){
                       lg.teamNum=lt.size();
                   }
                   logger.info("保存联赛{}",lg.leagueZh);
                   leagueDao.save(lg);
               }catch (Exception e){
                   logger.error("保存联赛错误{}",e);
               }


           }

        //保存球队
        for(String str:team){
            try{
                List<Team> tmList=teamDao.findByTeamZh(str);
                if(tmList!=null){
                    continue;
                }

              Team  tm=new Team();
                tm.addTime=new Date();
                tm.updateTime=new Date();
                tm.teamZh=str;
                logger.info("保存球队{}",tm.teamZh);
               // teamDao.save(tm);
            }catch (Exception e){
                logger.error("保存球队错误{}",e);
            }

        }

    }
    public void test() throws Exception {
    }
}
