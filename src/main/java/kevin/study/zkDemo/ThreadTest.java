package kevin.study.zkDemo;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: kevin
 * @Description:
 * @Company: 上海博般数据技术有限公司
 * @Version: 1.0.0
 * @Date: 2018/2/1
 * @ProjectName: zookeeperApp
 */
public class ThreadTest {

    public static void main(String[] args) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 5, 500, TimeUnit.MILLISECONDS, new ArrayBlockingQueue(4));
        for (int i = 0 ; i < 4 ; i++){
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    System.out.println("当前线程 : " + Thread.currentThread().getName());
                }
            });
        }
        boolean isDone = false;
/*        while (!isDone){
            long taskCount = executor.getTaskCount();
            long completedTaskCount = executor.getCompletedTaskCount();
            System.out.println("TaskCount : " + taskCount + " CompletedTaskCount : " + completedTaskCount);
            isDone = taskCount == completedTaskCount;
            if (isDone)
            {
                System.out.println("执行完毕...");
            }
        }*/
        do {
            long taskCount = executor.getTaskCount();
            long completedTaskCount = executor.getCompletedTaskCount();
            System.out.println("TaskCount : " + taskCount + " CompletedTaskCount : " + completedTaskCount);
            isDone = executor.getTaskCount() == executor.getCompletedTaskCount();
        }while (!isDone);
        if (isDone)
        {
            System.out.println("全部执行完...");
        }
    }

}
