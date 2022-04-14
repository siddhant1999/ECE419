package app_kvServer.cache;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

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
    public Disk(String serverName){
        //Absolute path: Paths.get("").toAbsolutePath().toString() + "/"+
        this.filepath = "filepath/"+serverName+"/";
        Path dirpath = Paths.get(this.filepath);
        boolean dirExists = Files.exists(dirpath);
        if (!dirExists){
            try {
                Files.createDirectories(dirpath);
            } catch (Exception e) {
                System.out.println(" Exception Creating Dir - " + e.toString());
            }
        } 
    }

    /**
     * Clears disk by deleting all the files created
     * @return
     */
    public synchronized boolean clearDisk(){
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
            } else {
                System.out.println("Disk Directory does not exist: " + this.filepath);
            }
            return true;
        } catch (Exception e) {
            System.out.println(" Exception Clearing Disk - " + e.toString());
            return false;
        }
    }

    private synchronized String deleteFromDisk( String key, String fname, Path path) throws Exception {
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

    public synchronized String putOnDisk(String key, String value) throws Exception{
        Boolean onDisk = onDisk(key);
        char kk = key.charAt(0);
        String fname = kk + ".txt";
        Path path = Paths.get(this.filepath,fname);
        Boolean exists = Files.exists(path); //checks if file exists
        if (exists && onDisk && value.equals("null")) { // value exists and delete from disk
            return deleteFromDisk(key, fname, path);
        } else if (exists && onDisk && !value.equals("null")) { // replace KV
            return replaceOnDisk(key, value, fname,  path);
        } else if (!onDisk && !value.equals("null")) { // if KV unseen
            if (!exists){ // file has not been seen, need to create a new file
                try {
                    String text = key + "\t" + value + "\n";
                    Files.write(path, text.getBytes(), StandardOpenOption.CREATE_NEW);   
                    return this.filepath + fname;
                } catch (Exception e) {
                    throw new Exception("Unable to write unseen key, value in creation: " + e.toString());
                }
            } else if (exists) {// file has been seen before, append onto the current available file
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
        }  else if (!onDisk && value.equals("null")) {
            throw new Exception("Unable to Delete, Key not in Disk");
        }

        throw new Exception("Not one of the chosen parameters - \n { \n \tkey: " + key + ", \n \tvalue: " + value + "\n } \n") ;

    }

    public Boolean onDisk(String key) throws Exception {
        String val = getFromDisk(key);
        return val!=null;
    }


    public String getFromDisk(String key) throws Exception{
        char kk = key.charAt(0);
        String fname = kk + ".txt";
//        Path path = Path.of(this.filepath, fname);
        Path path = Paths.get(this.filepath, fname);
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
        Path path = Paths.get(this.filepath, fname);
        boolean exists = Files.exists(path);
        try (Stream<String> lines = Files.lines(path)) {
            Iterator<String> itr = lines.iterator();
            while (itr.hasNext()){
                String line = itr.next();
                System.out.println("# reading file " + fname + " >> "  + line);

            }
        } catch (Exception e){
            System.out.println(e);
        }
    }

    public LinkedList<KeyValue> getAllDiskData(){
        Path dirpath = Paths.get(this.filepath);
        boolean dirExists = Files.exists(dirpath);
        LinkedList<KeyValue> allData = new LinkedList<>();
        try {
            if (dirExists) {
                File index = new File(dirpath.toString());
                String[]entries = index.list();
                //iterate over all the files
                for(String s: entries){
                    File currentFile = new File(index.getPath(),s);
                    Scanner scanner = new Scanner(currentFile);
                    //iterate over each line in the file
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        String[] kv = line.split("\t");
                        KeyValue kvObj = new KeyValue(kv[0], kv[1]);
                        allData.add(kvObj);
                    }
                }
            } else {
                System.out.println("Disk Directory does not exist: " + this.filepath);
            }
            return allData;
        } catch (Exception e) {
            System.out.println(" Exception Clearing Disk - " + e.toString());
            return null;
        }
    }

}
