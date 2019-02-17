package website2018.domain;

import website2018.base.BaseEntity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "zhibo_issue_problem")
public class IssueProblem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public Date addTime;
    public Date updateTime;

    public String answer;

    @OneToOne
    @JoinColumn(name = "problem_id")
    public ProblemDb problemDb;

    @ManyToOne
    @JoinColumn(name = "issue_id")
    public Issue issue;
}
