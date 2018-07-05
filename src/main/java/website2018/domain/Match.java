package website2018.domain;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.google.common.collect.Lists;

import website2018.base.BaseEntity;

@Entity
@Table(name = "zhibo_match")
public class Match extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public Date addTime;

    public Date playDate;
    public String playDateStr;
    public String playTime;
    public String project;
    public String game;
    public String name;
    public int locked;// 是否锁定
    public Date unlockTime;// 解锁时间
    public int emphasis;// 是否重点
    public String source;

    @OneToMany(mappedBy = "match", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    public List<Live> lives = Lists.newArrayList();

    @OneToMany(mappedBy = "match", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    public List<Ad> ads = Lists.newArrayList();
}
