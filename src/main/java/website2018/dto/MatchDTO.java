package website2018.dto;

import java.util.List;

import com.google.common.collect.Lists;

public class MatchDTO {

    public String playTime;
    public String project;
    public String game;
    public String name;

    public List<LiveDTO> lives = Lists.newArrayList();
    public List<AdDTO> ads = Lists.newArrayList();

    public int emphasis;
    public int important;

}
