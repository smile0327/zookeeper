package kevin.study.zkDemo;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.util.List;
import java.util.Scanner;

/**
 * @Auther: kevin
 * @Description:  zookeeper中znode操作
 * @Company: 上海博般数据技术有限公司
 * @Version: 1.0.0
 * @Date: 2017/12/21
 * @ProjectName: zookeeperApp
 */
public class ZkZnodeApp implements IZKConfig{

    public static void main(String[] args) {
        try {
            // 1000 : 连接zk的超时时间  3 : 重连次数
            RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000 , 3);
            CuratorFramework cf = CuratorFrameworkFactory.builder()
                .connectString(ZK_HOST).sessionTimeoutMs(SESSION_TIMEOUT).connectionTimeoutMs(CONNECTION_TIMEOUT).retryPolicy(retryPolicy).build();
            cf.start();//打开客户端
            System.out.println("连接zookeeper...");
//            createZnode(cf , "/kevin/test3");
//            dataOperation(cf , "/kevin/test");
            deleteNode(cf , "/kevin/test2");
            System.out.println("断开连接...");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 创建节点
     * @param cf
     * @throws Exception
     */
    private static void createZnode(CuratorFramework cf , String path) throws Exception {
        cf.create().creatingParentsIfNeeded()   //如果父节点不存在则创建
                //指定节点类型  有序节点(递增)   节点后添加一串10位数字 累加（高位补0） 例 /kevin/test0000000003
                //临时节点：EPHEMERAL
                //临时有序节点：EPHEMERAL_SEQUENTIAL
                //持久节点：PERSISTENT
                //持久有序节点：PERSISTENT_SEQUENTIAL
                   .withMode(CreateMode.PERSISTENT)
                   .forPath(path);
        System.out.println("创建节点成功 : " + path);

    }

    private static void deleteNode(CuratorFramework cf , String path){
        //删除节点  deletingChildrenIfNeeded ：子节点存在同时删除子节点   若不加改判断，存在删除节点存在子节点时抛出异常：NotEmptyException
        try {
            Stat stat = cf.checkExists().forPath(path);  //判断节点是否存在
            if (stat != null)
            {
                cf.delete()
                        .guaranteed()         //安全删除
                        .deletingChildrenIfNeeded()
                        .forPath(path);
                System.out.println("删除节点成功 : " + path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void dataOperation(CuratorFramework cf , String path) throws Exception {
//        Stat stat = cf.checkExists().forPath("/kevin/test");  //判断节点是否存在
//        System.out.println(stat);
        String data = new String(cf.getData().forPath(path)) + "update"; //获取节点数据  若节点不存在则抛出异常NoNodeException
//        System.out.println(data);
        cf.setData().forPath(path,data.getBytes());
        System.out.println("更新节点数据成功 : " + path);
//        List<String> childrens = cf.getChildren().forPath("/storm");
//        for (String item : childrens){
//            System.out.println(item);
//        }

}



}
