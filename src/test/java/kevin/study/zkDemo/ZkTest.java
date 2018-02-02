package kevin.study.zkDemo;

import org.junit.Test;

import java.util.Scanner;

/**
 * @Auther: kevin
 * @Description:
 * @Company: 上海博般数据技术有限公司
 * @Version: 1.0.0
 * @Date: 2018/1/6
 * @ProjectName: zookeeperApp
 */
public class ZkTest {
    @Test
    public void mainTest(){
        int max = 0 , min = 0 ,sum = 0;
        double avg = 0.0 ;
        Scanner scan = new Scanner(System.in);
        System.out.println("输入参数个数 : ");
        int count = scan.nextInt();
        for (int i = 1; i <= count ; i++) {
            System.out.println("输入第" + i + "个参数 : ");
            int input = scan.nextInt();
            if ( i == 1)
            {
                max = input;
                min = input;
            }
            if (input > max)
            {
                max = input;
            }
             if (input < min)
            {
                min = input;
            }
            sum += input;
        }
        avg = sum * 1.0 / count;
        System.out.println("Max = " + max + " , Min = " + min + " , Avg = " + avg);
    }

}
