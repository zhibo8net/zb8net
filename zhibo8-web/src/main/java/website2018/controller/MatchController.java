package website2018.controller;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springside.modules.utils.mapper.BeanMapper;
import website2018.base.BaseEndPoint;
import website2018.cache.CacheUtils;
import website2018.domain.FriendLink;
import website2018.domain.Live;
import website2018.dto.LiveDTO;
import website2018.dto.MatchDTO;
import website2018.service.IndexService;
import website2018.service.KeyUrlService;
import website2018.service.LiveService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/9/16.
 */
@Controller
public class MatchController extends BaseEndPoint {

    @Autowired
    LiveService liveService;
    @Autowired
    IndexService indexService;

    @Autowired
    KeyUrlService keyUrlService;

    @RequestMapping(value = "/mwrap/{id}")
    public String mwrap(@PathVariable Long id, Model model) {
        try {
            MatchDTO matchDTO = indexService.findMatchDTO(id);
            if (matchDTO == null) {
                return "redirect:http://www.zhibo8.net/mindex.html";
            }
            List<FriendLink> friendLinks = liveService.findFriendLinks();
            model.addAttribute("friendLinks", friendLinks);
            model.addAttribute("matchDTO", matchDTO);

            return "mwrap";
        } catch (Exception e) {
            return "redirect:http://www.zhibo8.net/mindex.html";
        }

    }

    @RequestMapping(value = "/match_1/{id}")
    public String live(@PathVariable Long id, Model model) {
        try {
            MatchDTO matchDTO = indexService.findMatchDTO(id);
            if (matchDTO == null) {
                return "redirect:http://www.zhibo8.net/";
            }
            List<FriendLink> friendLinks = liveService.findFriendLinks();
            model.addAttribute("friendLinks", friendLinks);
            model.addAttribute("matchDTO", matchDTO);

            return "detail";
        } catch (Exception e) {
            return "redirect:http://www.zhibo8.net/";
        }

    }

    @RequestMapping(value = "/match_old_1/{id}")
    public String match_old_1(@PathVariable Long id, Model model) {
        try {
            MatchDTO matchDTO = indexService.findMatchDTO(id);
            if (matchDTO == null) {
                return "redirect:http://www.zhibo8.net/";
            }
            model.addAttribute("matchDTO", matchDTO);

            return "detail-old";
        } catch (Exception e) {
            return "redirect:http://www.zhibo8.net/";
        }
    }

    @RequestMapping(value = "/live_play_inner/{id}")
    public String live_play_inner(@PathVariable Long id, Model model, HttpServletRequest request) {
        try {
            Live live = liveService.findById(id);
            if (live == null || live.match == null) {
                return "redirect:http://www.zhibo8.net/";
            }
            Map<String, String> sysParamMap = CacheUtils.getSysMap();
            String interfaceChange = sysParamMap.get("LIVE_ZHIBO_IS_INTERFACE_CHANAGE") == null ? "" : sysParamMap.get("LIVE_ZHIBO_IS_INTERFACE_CHANAGE");
            String change = sysParamMap.get("LIVE_ZHIBO_IS_PLAY_CHANAGE") == null ? "" : sysParamMap.get("LIVE_ZHIBO_IS_PLAY_CHANAGE");
            String CONTAIN_URL = sysParamMap.get("LIVE_ZHIBO_CONTAIN_URL") == null ? "" : sysParamMap.get("LIVE_ZHIBO_CONTAIN_URL");
            String FILTERS_STREAM = sysParamMap.get("LIVE_ZHIBO_FILTERS_STREAM") == null ? "" : sysParamMap.get("LIVE_ZHIBO_FILTERS_STREAM");
            String PLAY_CHANGE_IPHONE = sysParamMap.get("LIVE_ZHIBO_PLAY_CHANGE_IPHONE") == null ? "" : sysParamMap.get("LIVE_ZHIBO_PLAY_CHANGE_IPHONE");
            String PLAY_CHANGE_PC = sysParamMap.get("LIVE_ZHIBO_PLAY_CHANGE_PC") == null ? "" : sysParamMap.get("LIVE_ZHIBO_PLAY_CHANGE_PC");
            String PLAY_CHANGE_ANDRIOD = sysParamMap.get("LIVE_ZHIBO_PLAY_CHANGE_ANDRIOD") == null ? "" : sysParamMap.get("LIVE_ZHIBO_PLAY_CHANGE_ANDRIOD");


            LiveDTO liveDTO = BeanMapper.map(live, LiveDTO.class);
            liveDTO.link = live.link;
            model.addAttribute("liveDTO", liveDTO);
            if (StringUtils.equals("TRUE", interfaceChange)) {
                liveDTO.link = keyUrlService.getKeyUrl(liveDTO.link);
            }
            if (StringUtils.isNotEmpty(liveDTO.link)) {

                if (StringUtils.equals("TRUE", change)) {
                    List<String> containList = Lists.newArrayList(CONTAIN_URL.split("\\|"));
                    boolean flag = false;
                    for (String str : containList) {
                        if (liveDTO.link.indexOf(str) >= 0) {
                            flag = true;
                            break;
                        }
                    }
                    if (flag) {
                        List<String> filtersList = Lists.newArrayList(FILTERS_STREAM.split("\\|"));

                        String agent = request.getHeader("user-agent");

                        boolean filters = false;
                        for (String str : filtersList) {
                            if (liveDTO.link.indexOf(str) >= 0) {
                                filters = true;
                                break;
                            }
                        }
                        if (!filters) {
                            if (agent.contains("iPhone") || agent.contains("iPod") || agent.contains("iPad")) {
                                if(StringUtils.equals(PLAY_CHANGE_IPHONE,"TRUE")){
                                    liveDTO.link = liveDTO.link.replace("m3u8", "flv");
                                }

                            } else if (agent.contains("Android") || agent.contains("android")) {
                                if(StringUtils.equals(PLAY_CHANGE_ANDRIOD,"TRUE")){
                                    liveDTO.link = liveDTO.link.replace("m3u8", "flv");
                                }
                            } else {
                                if(StringUtils.equals(PLAY_CHANGE_PC,"TRUE")){
                                    liveDTO.link =liveDTO.link.replace("m3u8", "flv");
                                }
                            }
                        }
                    }
                }


            }
            MatchDTO matchDTO = indexService.findMatchDTO(live.match.id);
            if (matchDTO == null) {
                return "redirect:http://www.zhibo8.net/";
            }
            model.addAttribute("matchDTO", matchDTO);

            return "live-old";
        } catch (Exception e) {
            return "redirect:http://www.zhibo8.net/";
        }
    }
}
