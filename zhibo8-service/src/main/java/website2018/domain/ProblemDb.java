package website2018.domain;

import website2018.base.BaseEntity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "zhibo_problem_db")
public class ProblemDb extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    //题目标志 是否默认
    public String problemFlag;

    //题目类型
    public String problemType;

    //题目标题
    public String problemTitle;

    //答案1
    public String answerOne;

    //答案1
    public String answerTwo;

    //答案1
    public String answerThree;

    //答案1
    public String answerFour;

    //正确答案
    public String answer;

    public Date addTime;

    public Date updateTime;




}
