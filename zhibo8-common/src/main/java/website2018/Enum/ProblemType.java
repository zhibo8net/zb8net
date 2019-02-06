package website2018.Enum;

/**
 * Created by Administrator on 2019/1/27.
 */
public enum ProblemType {

    RADIO_2("RADIO_2","单选-2选项"),
    RADIO_3("RADIO_3","单选-3选项"),
    RADIO_4("RADIO_4","单选-4选项"),
    CHECKBOX_2("CHECKBOX_2","多选-2选项"),
    CHECKBOX_3("CHECKBOX_3","多选-3选项"),
    CHECKBOX_4("CHECKBOX_4","多选-4选项")
    ;
    private ProblemType(String code, String desc){
        this.code=code;
        this.desc=desc;
    }
    private String code;
    private String desc;
    public static ProblemType getEnum(String value) {
        for (ProblemType paymentTypeEnum : ProblemType.values()) {
            if (value == paymentTypeEnum.getCode()) {
                return paymentTypeEnum;
            }
        }
        return null;
    }
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
