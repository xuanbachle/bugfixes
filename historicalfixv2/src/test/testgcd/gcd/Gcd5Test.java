package test.testgcd.gcd;

/**
 * Created by xuanbach32bit on 4/25/15.
 */
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Gcd5Test extends Gcd {
    //    @Rule
//    public Timeout globalTimeout = Timeout.seconds(10);
    public static void main(String[] args){

    }

    @Test(timeout=2000)
    public void evaluatesExpression() {
        Gcd gcdCal = new Gcd();
        int sum = gcdCal.gcd(0,50);
        assertEquals(0, sum);
    }
}

