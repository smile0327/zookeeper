package kevin.study.zkDemo;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: kevin
 * @Description:  zookeeper监听器
 * @Company: 上海博般数据技术有限公司
 * @Version: 1.0.0
 * @Date: 2017/12/27
 * @ProjectName: zookeeperApp
 */
public class ZKNodeWatcher implements IZKConfig , Watcher{

    private List<String> childrenList = new ArrayList<>();
    private CuratorFramework cf;

    public ZKNodeWatcher() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000 , 3);
        cf = CuratorFrameworkFactory.builder()
                .connectString(ZK_HOST).sessionTimeoutMs(SESSION_TIMEOUT).connectionTimeoutMs(CONNECTION_TIMEOUT).retryPolicy(retryPolicy).build();
        cf.start();//打开客户端
        System.out.println("打开zookeeper连接...");
        createWatcherNode(cf);
    }

    /**
     * 创建监听节点 实现Watcher接口  只监听子节点的变化  无法监听数据变化
     * @param cf
     */
    private void createWatcherNode(CuratorFramework cf){
        try {
            //在/kevin目录上注册监听器  返回所有孩子节点  一次性监视器
            childrenList = cf.getChildren().usingWatcher(this).forPath("/kevin");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 监听的节点发生变化会触发此方法
     * @param event
     */
    @Override
    public void process(WatchedEvent event) {
        System.out.println("触发监视器...... : " + event);
        try {
            List<String> currentChildrenList = cf.getChildren().usingWatcher(this).forPath("/kevin");
            for (String item : childrenList){
                if (!currentChildrenList.contains(item))
                {
                    System.out.println("节点消失 : " + item);
                }
            }
            for (String item : currentChildrenList){
                if (!childrenList.contains(item))
                {
                    System.out.println("创建新节点 : " + item);
                }
            }
            childrenList = currentChildrenList;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ZKNodeWatcher();
        while (true);
    }

}
