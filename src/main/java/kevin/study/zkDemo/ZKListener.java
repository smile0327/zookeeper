package kevin.study.zkDemo;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.net.InetAddress;

/**
 * @Auther: kevin
 * @Description:  zk监听
 * @Company: 上海博般数据技术有限公司
 * @Version: 1.0.0
 * @Date: 2017/12/28
 * @ProjectName: zookeeperApp
 */
public class ZKListener implements IZKConfig {

    private CuratorFramework cf;

    public ZKListener() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000 , 3);
        cf = CuratorFrameworkFactory.builder()
                .connectString(ZK_HOST).sessionTimeoutMs(SESSION_TIMEOUT).connectionTimeoutMs(CONNECTION_TIMEOUT).retryPolicy(retryPolicy).build();
        cf.start();//打开客户端
        System.out.println("打开zookeeper连接...");
    }

    public CuratorFramework getCf() {
        return cf;
    }

    /**
     * 监听节点创建和修改  删除不会监听
     * @param cf
     */
    private void nodeCache(CuratorFramework cf){
        try {
            NodeCache cache = new NodeCache(cf, "/kevin/test", false);
            cache.start();
            cache.getListenable().addListener(() -> {
                System.out.println("/kevin/test  开始监听...");
                System.out.println("路径：" + cache.getCurrentData().getPath());
                System.out.println("数据：" + new String(cache.getCurrentData().getData()));
                System.out.println("状态：" + cache.getCurrentData().getStat());
            });

            cf.setData().forPath("/kevin/test","listener".getBytes());
            Thread.sleep(10000);
            cf.setData().forPath("/kevin/test", InetAddress.getLocalHost().getHostAddress().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 子节点监听
     * @param cf
     */
    private void childrenCache(CuratorFramework cf){
        //设置true  表示监听节点数据更新
        try {
            PathChildrenCache cache = new PathChildrenCache(cf, "/kevin", true);
            /**
             *
             如果参数为PathChildrenCache.StartMode.BUILD_INITIAL_CACHE，则会预先创建之前指定的/super节点
             如果参数为PathChildrenCache.StartMode.POST_INITIALIZED_EVENT，效果与BUILD_INITIAL_CACHE相同，只是不会预先创建/super节点
             参数为PathChildrenCache.StartMode.NORMAL时，与不填写参数是同样的效果
             */
            cache.start();
            cache.getListenable().addListener((framework,event) -> {
                switch (event.getType()){
                    case CHILD_ADDED:
                        System.out.println("新增节点 : " + event.getData().getPath() + " 数据 : " + new String(event.getData().getData()) );
                        break;
                    case CHILD_UPDATED:
                        System.out.println("更新数据 : " + event.getData().getPath() + " 数据 : " + new String(event.getData().getData()) );
                        break;
                    case CHILD_REMOVED:
                        System.out.println("删除节点 : " + event.getData().getPath() + " 数据 : " + new String(event.getData().getData()) );
                        break;
                    default:
                        break;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 父节点、子节点监听   相当于上面两种组合
     * @param cf
     */
    private void treeCache(CuratorFramework cf){
        try {
            TreeCache cache = new TreeCache(cf, "/kevin");
            cache.start();
            cache.getListenable().addListener((curatorFramework, treeCacheEvent) -> {
                switch (treeCacheEvent.getType()){
                    case NODE_ADDED:
                        System.out.println("新增节点 : " + treeCacheEvent.getData().getPath() + " 数据 : " + new String(treeCacheEvent.getData().getData()) );
                        break;
                    case NODE_UPDATED:
                        System.out.println("更新数据 : " + treeCacheEvent.getData().getPath() + " 数据 : " + new String(treeCacheEvent.getData().getData()) );
                        break;
                    case NODE_REMOVED:
                        System.out.println("删除节点 : " + treeCacheEvent.getData().getPath() + " 数据 : " + new String(treeCacheEvent.getData().getData()) );
                        break;
                    default:
                        break;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ZKListener listener = new ZKListener();
//        listener.nodeCache(listener.getCf());
//        listener.childrenCache(listener.getCf());
        listener.treeCache(listener.getCf());
        while (true);
    }

}
