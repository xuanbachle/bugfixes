package test.testgcd.gcd.gcdnested;

/**
 * Created by xuanbach32bit on 4/25/15.
 */


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GcdTest1 extends Gcd {
//    @Rule
//    public Timeout globalTimeout = Timeout.seconds(10);
    public static void main(String[] args){

    }

    @Test(timeout=1000)
    public void evaluatesExpression() {
        Gcd gcdCal = new Gcd();
        int sum = gcdCal.gcd(1,4);
        assertEquals(1, sum);
    }
}
