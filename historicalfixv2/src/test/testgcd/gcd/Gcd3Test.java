package test.testgcd.gcd;

/**
 * Created by xuanbach32bit on 4/25/15.
 */
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Gcd3Test extends Gcd {
    //    @Rule
//    public Timeout globalTimeout = Timeout.seconds(10);
    public static void main(String[] args){

    }

    @Test(timeout=1000)
    public void evaluatesExpression() {
        Gcd gcdCal = new Gcd();
        int sum = gcdCal.gcd(12,9);
        assertEquals(3, sum);
    }
}

