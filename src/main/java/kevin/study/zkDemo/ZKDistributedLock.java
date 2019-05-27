package kevin.study.zkDemo;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @Auther: kevin
 * @Description:
 * @Company: 上海博般数据技术有限公司
 * @Version: 1.0.0
 * @Date: 2017/12/27
 * @ProjectName: zookeeperApp
 */
public class ZKDistributedLock implements Lock , Watcher , IZKConfig{

    private ZooKeeper zk;
    private String root = "/locks"; //根目录
    private String lockName;    //竞争资源的标志
    private String waitNode;    //等待前一个锁
    private String myZnode;     //当前锁
    private CountDownLatch latch;   //计数器
    private List<Exception> exceptions = new ArrayList<>();

    /**
     * 在zk上创建/locks 节点
     * @param lockName  竞争资源标识  lockName中不能包含单词lock
     */
    public ZKDistributedLock(String lockName) {
        this.lockName = lockName;
        try {
            zk = new ZooKeeper(ZK_HOST , SESSION_TIMEOUT , this);
            Stat stat = zk.exists(root, false);
            if (stat == null)   //节点不存在
            {
                zk.create(root , new byte[0] , ZooDefs.Ids.OPEN_ACL_UNSAFE , CreateMode.PERSISTENT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    /**
     * 获取锁
     */
    @Override
    public void lock() {
        if (exceptions.size() > 0)
        {
            throw new LockException(exceptions.get(0));
        }
        try {
            if (this.tryLock())
            {
                //拿到锁
                System.out.println("Thread : " + Thread.currentThread().getId() + " " + myZnode + " get lock true");
                return;
            }else
            {
                //等待获取锁
                waitForLock(waitNode, WAIT_TIMEOUT);//等待获取锁
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }



    /**
     * 尝试获取锁
     * @return
     */
    @Override
    public boolean tryLock() {
        try {
            String splitStr = "_lock_";
            if(lockName.contains(splitStr))
                throw new LockException("lockName can not contains \\u000B");
            //创建临时有序子节点     /locks/test_lock_0000000001
            myZnode = zk.create(root + "/" + lockName + splitStr, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);
            System.err.println(myZnode + " is created ");
            //取出所有子节点
            List<String> subNodes = zk.getChildren(root, false);
            //取出所有lockName的锁
            List<String> lockObjNodes = new ArrayList<String>();
            for (String node : subNodes) {
                String _node = node.split(splitStr)[0];
                if(_node.equals(lockName)){
                    lockObjNodes.add(node);
                }
            }
            //对所有节点进行默认排序，从小到大
            Collections.sort(lockObjNodes);
            System.out.println("MyZnode : " + myZnode + " MinChildrenZnode : " + lockObjNodes.get(0));
            if(myZnode.equals(root+"/"+lockObjNodes.get(0))){
                //如果是最小的节点,则表示取得锁
                return true;
            }
            //如果不是最小的节点，找到比自己小1的节点
            String subMyZnode = myZnode.substring(myZnode.lastIndexOf("/") + 1);
            //获取比当前节点小一级的节点(Collections.binarySearch(lockObjNodes, subMyZnode):获取当前节点的角标)
            waitNode = lockObjNodes.get(Collections.binarySearch(lockObjNodes, subMyZnode) - 1);
        } catch (KeeperException e) {
            throw new LockException(e);
        } catch (InterruptedException e) {
            throw new LockException(e);
        }
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        try {
            if(this.tryLock()){
                return true;
            }
            return waitForLock(waitNode,time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 取消锁监控
     */
    @Override
    public void unlock() {
        try {
            System.out.println(Thread.currentThread().getId()+",unlock " + myZnode);
            zk.delete(myZnode,-1);
            myZnode = null;
            //zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    /**
     * 等待获取锁
     * @param lower ：等待的锁
     * @param waitTime 最大等待时间
     * @return
     * @throws InterruptedException
     * @throws KeeperException
     */
    private boolean waitForLock(String lower, long waitTime) throws InterruptedException, KeeperException {
        Stat stat = zk.exists(root + "/" + lower,true);
        //判断比自己小一个数的节点是否存在,如果不存在则无需等待锁,同时注册监听
        if(stat != null){
            System.out.println("Thread " + Thread.currentThread().getId() + " waiting for " + root + "/" + lower);
            this.latch = new CountDownLatch(1);
            //当前线程会等待，直到计数器为0
            this.latch.await(waitTime, TimeUnit.MILLISECONDS);
            this.latch = null;
        }
        return true;
    }

    @Override
    public Condition newCondition() {
        return null;
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    /**
     * 监听器
     * @param event
     */
    @Override
    public void process(WatchedEvent event) {
        if (this.latch != null)
        {
            latch.countDown();
        }
    }

    /**
     * 关闭zk链接
     */
    public void closeZk(){
        try {
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 自定义异常信息
     * @author lenovo
     *
     */
    public class LockException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public LockException(String e){
            super(e);
        }
        public LockException(Exception e){
            super(e);
        }
    }

    public static void main(String[] args) {
        ExecutorService service = Executors.newScheduledThreadPool(5);
        for (int i = 0 ; i < 5 ; i++){
            service.submit(new Runnable() {
                @Override
                public void run() {
                    doSomething();
                }
            });
        }
    }

    private static void doSomething(){
        ZKDistributedLock lock = null;
        try {
            lock = new ZKDistributedLock("test");
            //获取锁
            lock.lock();
            System.out.println("Thread : " + Thread.currentThread().getId() + " 获得锁...  Get Lock Time : " + System.currentTimeMillis());
            Thread.sleep(5000);
            //do something...
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (lock != null)
            {
                lock.unlock();
                System.out.println("Thread : " + Thread.currentThread().getId() + " 释放锁...  Release Lock Time : " + System.currentTimeMillis());
            }
            lock.closeZk();
        }
    }

}
