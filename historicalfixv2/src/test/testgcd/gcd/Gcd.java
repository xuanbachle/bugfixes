package test.testgcd.gcd;

/**
 * Created by xuanbach32bit on 4/25/15.
 */
public class Gcd {
    public int gcd(int a,int b) {
        int res=0;
        if (a == 0) {

            System.out.println(b);
        }
        {
            while (b != 0) {
                if (a > b) {
                    a = a - b;
                } else {
                    b = b - a;
                }
            }
            res=a;
            //printf("%g\n", a);
        }

        return res;
    }
}
