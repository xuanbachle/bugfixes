package util;

import mainscala.RepairOptions;
import mainscala.RepairOptions$;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuanbach32bit on 8/26/15.
 */
public class FileFolderUtils {
    public static void main(String[] args) throws IOException {
        //System.out.println(relativePath("/var/data", "/var/data/stuff/xyz.dat"));
        //System.out.println(path2Package("var/data"));
        String testFolder = "/home/xuanbach32bit/workspace/v6/pre-fix/target/test-classes";
        String sourceFolder = "/home/xuanbach32bit/workspace/v6/pre-fix/target/classes";
        setupTempDirectories(sourceFolder, testFolder, RepairOptions$.MODULE$.variantOutputDir(), "temp1");

    }

    public static String relativePath(String base, String pathFull){
        Path pathAbsolute = Paths.get(pathFull);
        Path pathBase = Paths.get(base);
        Path pathRelative = pathBase.relativize(pathAbsolute);
        return pathRelative.toString();
    }

    public static String path2Package(String path){
        String normalize = path;
        if(path.charAt(0) == '/')
            normalize = path.substring(1, path.length());
        normalize =normalize.split("\\.")[0]; // Strip extentions such as .java
        normalize = normalize.split("\\$")[0];
        return normalize.replace(File.separator, ".");
    }

    public static void removeDir(String dir) throws IOException {
        File dirin = new File(dir);
        try{
            org.apache.commons.io.FileUtils.deleteDirectory(dirin);
        }catch(Exception ex){
            //Retry
            //FileFolderUtils.deleteDirectory(dirin);
            //logger.error("ex: "+ex.getMessage());
        }
        //dirin.mkdir();
    }

    public static boolean copyOriginalBin(String inDir, String baseIdenDir, String mutatorIdentifier) throws IOException {
        boolean copied = false;
        if(inDir != null){
            File original = new File(inDir);
            String tempDirFullPath = baseIdenDir + File.separator + mutatorIdentifier;
            File dest = new File(tempDirFullPath);
            dest.mkdirs();
            org.apache.commons.io.FileUtils.copyDirectory(original, dest);
            copied = true;
        }
        return copied;
    }


    /**
     * Set up a project for a given mutator identifier.
     * @param tempDirNumber
     * @throws IOException
     */
    public synchronized static String setupTempDirectories(String applicationBinDir,String testBinDir,String baseTempDir, String tempDirNumber) throws IOException {

        //cleanMutationResultDirectories();
        String fullTempDir = baseTempDir+ File.separator + tempDirNumber;
        removeDir(fullTempDir);
        //copyOriginalCode(currentMutatorIdentifier);
        try{
            boolean copied1 = copyOriginalBin(applicationBinDir,baseTempDir, tempDirNumber);// NEW
            // ADDED
            boolean copied2 = copyOriginalBin(testBinDir,baseTempDir, tempDirNumber);// NEW
        }catch(Exception e){
            e.printStackTrace();
        }																							// ADDED

        return fullTempDir;
        //copyData(currentMutatorIdentifier);

    }

    public static URL[] getURLforVariant(String variantID) throws MalformedURLException {
        ArrayList<String> deps= RepairOptions$.MODULE$.getDependenciesList();
        List<URL> classpath = new ArrayList<URL>();
        for(String dep: deps){
            classpath.add(new File(dep).toURI().toURL());
        }
        //bin
        URL urlBin = new File(getOutDirWithPrefix(variantID)).toURI().toURL();
        classpath.add(urlBin);

        URL[] cp = classpath.toArray(new URL[0]);
        return cp;
    }

    public static String getOutDirWithPrefix(String variantID) {
        String fullPath = RepairOptions.variantOutputDir() + File.separator + variantID;
        return fullPath;
    }

    public static URL[] redefineURL(File foutgen, URL[] originalURL) throws MalformedURLException {
        List<URL> urls = new ArrayList<URL>();
        urls.add(foutgen.toURL());// remember that we need to add the foutgen, the one we need to redefine at the beginning
        for (int i = 0; (originalURL != null) && i < originalURL.length; i++) {
            urls.add(originalURL[i]);
        }

        return (URL[]) urls.toArray(originalURL);
    }

    public static String fileNameAbsolutePath(String fileName){
        return RepairOptions.sourceFolder() + /*File.separator+*/ fileName.replace(".", File.separator) +".java";
    }
}
