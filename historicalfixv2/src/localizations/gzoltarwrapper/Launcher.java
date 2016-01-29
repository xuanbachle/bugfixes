package localizations.gzoltarwrapper;

import com.gzoltar.core.agent.RegistrySingleton;
import com.gzoltar.core.instr.message.Message;
import com.gzoltar.core.instr.message.Response;
import mainscala.RepairOptions;
import myLib.MyjavaLib;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

/**
 * Created by xledinh on 9/15/15.
 */
public class Launcher {
    private static boolean debug = true;
    private static final String pathSeparator = System.getProperty("path.separator");
    private static final String fileSeparator = System.getProperty("file.separator");
    private static final String runner = "com.gzoltar.core.instr.Runner";
    private static final String javaHome;

    public Launcher() {
    }

    public static Response launch(GZoltar gz, File agent) {
        RegistrySingleton.createSingleton();
        Response response = null;

        try {
            Message e = new Message();
            e.setClassParameters(gz.getClassParameters());
            e.setTestParameters(gz.getTestParameters());
            String messageName = UUID.randomUUID().toString();
            RegistrySingleton.register(messageName, e);
            StringBuilder classpath = new StringBuilder(System.getProperty("java.class.path") + pathSeparator + gz.getWorkingDirectory());
            Iterator cmds = gz.getClasspaths().iterator();

            while(cmds.hasNext()) {
                String pb = (String)cmds.next();
                classpath.append(pathSeparator + pb);
            }

            //classpath.append(pathSeparator + "/Users/larcuser/workspace/historicalfixv2/tempOutput/default/");


            ArrayList cmds1 = new ArrayList();
            if(System.getProperty("os.name").toLowerCase().contains("windows")) {
                cmds1.add(javaHome + ".exe");
            } else {
                cmds1.add(javaHome);
            }

            cmds1.add("-javaagent:" + agent.getAbsolutePath());
            for (String dep: RepairOptions.getDependenciesList()){
                System.out.println("LIB: "+dep);
                classpath.append(pathSeparator+ dep);
            }
            System.out.println("ClassPaths: "+classpath.toString());
            /*classpath.append(":/home/xledinh/workspace/bug2run/Closure/v1/lib/ant.jar");
            classpath.append(":/home/xledinh/workspace/bug2run/Closure/v1/lib/ant-launcher.jar");
            classpath.append(":/home/xledinh/workspace/bug2run/Closure/v1/lib/args4j.jar");
            classpath.append(":/home/xledinh/workspace/bug2run/Closure/v1/lib/caja-r4314.jar.jar");
            classpath.append(":/home/xledinh/workspace/bug2run/Closure/v1/lib/guava.jar");
            classpath.append(":/home/xledinh/workspace/bug2run/Closure/v1/lib/jarjar.jar");
            classpath.append(":/home/xledinh/workspace/bug2run/Closure/v1/lib/json.jar");*/
            //classpath.append(":/home/xledinh/workspace/bug2run/Closure/v10b/lib/protobuf-java.jar");
            //classpath.append(":/home/xuanbach32bit/workspace/bug2run/Closure/v10b/build/lib/rhino.jar");
            /*classpath.append(":/home/xledinh/workspace/bug2run/Closure/v1/lib/rhino");*/
            cmds1.add("-cp");
            cmds1.add(classpath.toString());
            cmds1.add("com.gzoltar.core.instr.Runner");
            cmds1.add(Integer.toString(RegistrySingleton.getPort()));
            cmds1.add(messageName);
            ProcessBuilder pb1 = new ProcessBuilder(cmds1);
            pb1.directory(new File(gz.getWorkingDirectory()));
            pb1.redirectErrorStream(true);
            Process p = pb1.start();
            InputStream is = p.getInputStream();
            BufferedInputStream isl = new BufferedInputStream(is);
            byte[] buffer = new byte[1024];
            boolean len = false;
            if(debug) {
                System.err.println(">>> Begin subprocess output");
            }

            int len1;
            while((len1 = isl.read(buffer)) != -1) {
                if(debug) {
                    System.err.write(buffer, 0, len1);
                }
            }

            if(debug) {
                System.err.println("<<< End subprocess output");
            }

            p.waitFor();
            response = e.getResponse();
        } catch (Exception var13) {
            var13.printStackTrace();
        }

        RegistrySingleton.unregister();
        return response;
    }

    static {
        javaHome = System.getProperty("java.home") + fileSeparator + "bin" + fileSeparator + "java";
    }
}

