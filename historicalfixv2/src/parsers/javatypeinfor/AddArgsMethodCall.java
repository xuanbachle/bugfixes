package parsers.javatypeinfor;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * Created by larcuser on 18/10/15.
 */
public class AddArgsMethodCall {
    public static MethodInvocation addArg(MethodInvocation mt, Name arg) {
        mt.arguments().add(arg);
        return mt;
    }
}
