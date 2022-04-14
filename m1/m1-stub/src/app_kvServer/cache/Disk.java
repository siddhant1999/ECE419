package app_kvServer.cache;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;

import java.util.Scanner;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;    
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import app_kvServer.KeyValue;
import java.lang.Exception;
import java.lang.ProcessBuilder.Redirect.Type;

public class Disk {
    
    String filepath;
    // private Map<Character, String> fileSystem;

    public Disk(){
        // this.filepath = "/filesystem/";
        this.filepath = "filepath/";
        Path dirpath = Paths.get(this.filepath);
        boolean dirExists = Files.exists(dirpath);
        if (!dirExists){
            try {
                Files.createDirectories(dirpath);
            } catch (Exception e) {
                // TODO: IMPLEMENT LOGGING HERE
                System.out.println(" Exception Creating Dir - " + e.toString());
            }
        } 
    }

    public synchronized void clearDisk(){
        Path dirpath = Paths.get(this.filepath);
        boolean dirExists = Files.exists(dirpath);
        try {
            if (dirExists) {
                File index = new File(dirpath.toString());
                String[]entries = index.list();
                for(String s: entries){
                    File currentFile = new File(index.getPath(),s);
                    currentFile.delete();
                }
            }
        } catch (Exception e) {
            System.out.println(" Exception Clearing Disk - " + e.toString());
        }
    }

    private synchronized String deleteFromDisk( String key, String value, String fname, Path path) throws Exception {
        File inputFile = new File(this.filepath, fname);
        File tmpFile = new File(this.filepath, "tmpfile.txt");
        Path tmpfilepath = tmpFile.toPath();
        Files.createFile(tmpfilepath);

        try (Stream<String> lines = Files.lines(path)) {
            Iterator<String> itr = lines.iterator();
            while (itr.hasNext()){
                String line = itr.next();
                String[] kv = line.split("\t");
                if (kv[0].equals(key)) {
                    continue;
                }
                String txt = kv[0] + "\t" + kv[1] + "\n";
                Files.write(tmpfilepath, txt.getBytes(), StandardOpenOption.APPEND);
            }
        } catch (Exception e) {
            throw new Exception("deleteFromDisk - Error Caught inside Rewrite: " + e.toString());
        }
        boolean success = tmpFile.renameTo(inputFile);
        if (!success) {
            throw new Exception("Error Inside deleteFromDisk - Unable to rewrite tmpFile");
        }
        return this.filepath + fname;      
    }

    private synchronized String replaceOnDisk( String key, String value, String fname, Path path ) throws Exception {
        File inputFile = new File(this.filepath, fname);
        File tmpFile = new File(this.filepath, "tmpfile.txt");
        Path tmpfilepath = tmpFile.toPath();
        Files.createFile(tmpfilepath);
        try (Stream<String> lines = Files.lines(path)) {
            Iterator<String> itr = lines.iterator();
            while (itr.hasNext()){
                String line = itr.next();
                String[] kv = line.split("\t");
                if (kv[0].equals(key)) {
                    kv[1] = value;
                }
                String txt = kv[0] + "\t" + kv[1] + "\n";
                Files.write(tmpfilepath, txt.getBytes(), StandardOpenOption.APPEND);
            }
        } catch (Exception e) { 
            throw new Exception("replaceOnDisk - Error Caught inside Rewrite: " + e.toString());
        }
        boolean success = tmpFile.renameTo(inputFile);
        if (!success) {
            throw new Exception("replaceOnDisk - Unable to rewrite tmpFile");
        }
        return this.filepath + fname;      
    }

    public synchronized String putOnDisk(KeyValue KV) throws Exception{
        String key = KV.getKey();
        String value = KV.getValue();
        String onDisk = getFromDisk(key); 
        char kk = key.charAt(0);
        String fname = kk + ".txt";
        Path path = Path.of(this.filepath, fname);
        Boolean exists = Files.exists(path);
        if (exists && !(onDisk == null) && value == "null") { // delete KV
            return deleteFromDisk(key, value, fname, path);
        } else if (exists && !(onDisk == null) && !(value == "null")) { // replace KV
            return replaceOnDisk(key, value, fname,  path);
        } else if ((onDisk == null) && !(value == "null")) { // if KV unseen
            if (!exists){ // if k never seen before make new
                try {
                    String text = key + "\t" + value + "\n";
                    Files.write(path, text.getBytes(), StandardOpenOption.CREATE_NEW);   
                    return this.filepath + fname;
                } catch (Exception e) {
                    throw new Exception("Unable to write unseen key, value in creation: " + e.toString());
                }
            } else if (exists) {// if k has been seen, append
                if (value != null ) {
                    try {
                        String text = key + "\t" + value + "\n";
                        Files.write(Paths.get(this.filepath + fname), text.getBytes(), StandardOpenOption.APPEND);  
                        return this.filepath + fname;
                    } catch (Exception e) {
                        throw new Exception("Unable to write unseen key, value in appending: " + e.toString());
                    }  
                }
            }
        } 

        throw new Exception("Not one of the chosen parameters - \n { \n \tkey: " + key + ", \n \tvalue: " + value + "\n } \n") ;

    }


    public String getFromDisk(String key) throws Exception{
        char kk = key.charAt(0);
        String fname = kk + ".txt";
        Path path = Path.of(this.filepath, fname);
        boolean exists = Files.exists(path);
        if (!exists){ // if file does not exist
            return null;
        } else {
            try (Stream<String> lines = Files.lines(path)) {
                Iterator<String> itr = lines.iterator();
                while (itr.hasNext()){
                    String line = itr.next();
                    String[] kv = line.split("\t");
                    if (kv[0].equals(key)) {
                        return (kv[1].equals("null")) ? null : kv[1];
                    }
                }
            } catch (Exception e) {
                throw new Exception("An exception occured when trying to read file: " + e.toString());
            }
        }        
        // if file exists, but not inside the file  
        return null;
    }   

    public void printFileContent(String key) {
        char kk = key.charAt(0);
        String fname = kk + ".txt";
        Path path = Path.of(this.filepath, fname);
        boolean exists = Files.exists(path);
        try (Stream<String> lines = Files.lines(path)) {
            Iterator<String> itr = lines.iterator();
            while (itr.hasNext()){
                String line = itr.next();
                System.out.println("# reading file " + fname + " >> "  + line);
                // String[] kv = line.split("\t");

            }
        } catch (Exception e){
            System.out.println(e);
        }
    }

}
