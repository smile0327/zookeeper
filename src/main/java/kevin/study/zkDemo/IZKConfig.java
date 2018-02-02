package kevin.study.zkDemo;

/**
 * @Auther: kevin
 * @Description:
 * @Company: 上海博般数据技术有限公司
 * @Version: 1.0.0
 * @Date: 2017/12/27
 * @ProjectName: zookeeperApp
 */
public interface IZKConfig {

    public static final String ZK_HOST = "app01:2181,app02:2181,app03:2181";    //zk地址
    public static final int SESSION_TIMEOUT = 30000; //session会话超时时间
    public static final int CONNECTION_TIMEOUT = 30000; //连接超时时间
    public static final int WAIT_TIMEOUT = 30000;        //等待节点失效最大时间

}
