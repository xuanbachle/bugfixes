package repair.handletests.junithandler;

/**
 * Created by xuanbach32bit on 8/27/15.
 */
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;

public class SingleJUnitTestRunner {
    public static String SUCCESS = "true";
    public static String FAILURE = "false";
    public static String RESULT_PREFIX_PRINT = "HISTORICALFIX EVALUATION RESULT";
    public static String SEPARATOR = "=";
    public static void main(String... args) throws ClassNotFoundException {
        String[] classAndMethod = args[0].split("#");
        Request request = null;
        if(classAndMethod.length==2) {
            request = Request.method(Class.forName(classAndMethod[0]),
                    classAndMethod[1]);
        }else{
            request = Request.aClass(Class.forName(classAndMethod[0]));
        }

        Result result = new JUnitCore().run(request);
        System.out.println(RESULT_PREFIX_PRINT+SEPARATOR+result.wasSuccessful());
        System.exit(result.wasSuccessful() ? 0 : 1);
    }
}
