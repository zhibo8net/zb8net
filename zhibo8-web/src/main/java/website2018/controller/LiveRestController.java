package website2018.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springside.modules.utils.mapper.BeanMapper;
import website2018.base.BaseEndPoint;
import website2018.domain.FriendLink;
import website2018.domain.Live;
import website2018.dto.LiveDTO;
import website2018.dto.MatchDTO;
import website2018.dto.VedioDTO;
import website2018.service.IndexService;
import website2018.service.LiveService;

import java.util.List;

/**
 * Created by Administrator on 2018/9/16.
 */
@RestController
public class LiveRestController extends BaseEndPoint {

    @Autowired
    LiveService liveService;

    @RequestMapping(value = "/liveRest/{id}")
    public LiveDTO liveRest(@PathVariable Long id) {
        try {
            Live live = liveService.findById(id);
            if (live == null || live.match == null) {
                return null;
            }
            LiveDTO liveDTO = BeanMapper.map(live, LiveDTO.class);
            liveDTO.link = live.link;


            return liveDTO;
        } catch (Exception e) {
            return null;
        }
    }

    @RequestMapping(value = "/liveVedioRest/{id}")
    public VedioDTO vedioDTO(@PathVariable Long id) {
        VedioDTO vedioDTO=new VedioDTO();
        try {
            Live live = liveService.findById(id);
            if (live == null || live.match == null) {
                return vedioDTO;
            }

            vedioDTO.video = live.link;


            return vedioDTO;
        } catch (Exception e) {
            return vedioDTO;
        }
    }
}
