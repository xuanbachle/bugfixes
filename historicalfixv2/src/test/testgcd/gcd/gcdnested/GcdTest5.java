package test.testgcd.gcd.gcdnested;

/**
 * Created by xuanbach32bit on 4/25/15.
 */
import test.testgcd.gcd.Gcd;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GcdTest5 extends test.testgcd.gcd.Gcd {
    //    @Rule
//    public Timeout globalTimeout = Timeout.seconds(10);
    public static void main(String[] args){

    }

    @Test(timeout=2000)
    public void evaluatesExpression() {
        test.testgcd.gcd.Gcd gcdCal = new Gcd();
        int sum = gcdCal.gcd(0,50);
        assertEquals(0, sum);
    }
}

