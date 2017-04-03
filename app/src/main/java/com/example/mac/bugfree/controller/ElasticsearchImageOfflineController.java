package com.example.mac.bugfree.controller;

import android.content.Context;

import com.example.mac.bugfree.module.ImageForElasticSearch;
import com.example.mac.bugfree.module.MoodEvent;
import com.example.mac.bugfree.module.MoodEventList;
import com.example.mac.bugfree.module.User;
import com.example.mac.bugfree.util.InternetConnectionChecker;
import com.example.mac.bugfree.util.LoadFile;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class stores file from elastic search and stores user's image to local file when
 * corresponding method is called.
 * @author Zhi Li
 */
public class ElasticsearchImageOfflineController {
    private static final String IMAGEONLINE = "ImageOnlineList.sav";
    private static final String IMAGEUPLOADLIST = "ImageUploadList.sav";
    private static final String IMAGEDELETELIST = "ImageDeleteList.sav";
    private User user;
    private String base64str;
    private ArrayList<String> imageList;


    /**
     * Add image task (local). The image id will be add to uploadArrayList
     * and the base64String will be store to local file.
     *
     * @param context     the context
     * @param imageBase64 the image base 64
     * @param base64Id    the base 64 id
     * @param OriginId    the origin id
     */
    public void AddImageTask (Context context,String imageBase64, String base64Id,String OriginId){
        InternetConnectionChecker checker = new InternetConnectionChecker();
        final boolean isOnline = checker.isOnline(context);
        if (isOnline){
            updateOfflineArrayList(context, "online", base64Id, null);
        } else {
            updateOfflineArrayList(context, "update", base64Id, OriginId);
        }

        saveBase64(context,imageBase64, base64Id);

    }

    /**
     * Add the image to be deleted to the array list, need to delete the local file in the activity.
     *
     * @param context the context
     * @param imageId the image id
     */
    public void DeleteImageTask(Context context,String imageId){
        InternetConnectionChecker checker = new InternetConnectionChecker();
        final boolean isOnline = checker.isOnline(context);

        if(!isOnline) {
            updateOfflineArrayList(context, "delete", null, imageId);
        } else {
            updateOfflineArrayList(context, "online", null, imageId);
        }
    }

    /**
     * Get image for elastic search from local file by id.
     *
     * @param context the context
     * @param imageId the image id
     * @return the image for elastic search
     */
    public ImageForElasticSearch GetImageTask (Context context,String imageId){
        String base64 = loadBase64(context, imageId);
        return new ImageForElasticSearch(base64,imageId);
    }

    /**
     * This method is for newly signed in user, it creates all the empty file for offline function for image
     * This image offline function is for the current user's image only.
     * Being called once after valid signin and at online.
     * There are three files to be created:
     * 1. Several image file (file stores base64String, file name is the unique id)
     * 2. ImageOriginList.sav (Contains the [arraylist of [strings of [image unique id]]] user Originally have in the ElasticSearch)
     * 3. ImageUploadList.sav (Contains the [arraylist of [strings of [image unique id]]] to be uploaded to the ElasticSearch)
     * 4. ImageDeleteList.sav (Contains the [arraylist of [strings of [image unique id]]] to be deleted from the ElasticSearch)
     *
     * @param context the context
     * @param user    the user
     */
    public void prepImageOffline(Context context, User user){
        try {
            // Arraylist, [strings of [image unique id]]] already in ElasticSearch
            FileOutputStream fos0 = context.openFileOutput(IMAGEONLINE, Context.MODE_PRIVATE);
            BufferedWriter out0= new BufferedWriter(new OutputStreamWriter(fos0));

            ArrayList<String> alreadyUp = new ArrayList<String>();
            MoodEventList MEL = user.getMoodEventList();
            for (int i = 0; i < MEL.getCount(); i++ ){
                String s = MEL.getMoodEvent(i).getPicId();
                if (s!=null) {
                    alreadyUp.add(s);
                    ElasticsearchImageController.GetImageTask  getImageTask = new ElasticsearchImageController.GetImageTask();
                    getImageTask.execute(s);
                    ImageForElasticSearch imageForElasticSearch = new ImageForElasticSearch();
                    try {
                        imageForElasticSearch = getImageTask.get();
                        String base64 = imageForElasticSearch.getImageBase64();
                        saveBase64(context, base64,s);  // filename = moodevent.getId()
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            Gson gson0 = new Gson();
            gson0.toJson(alreadyUp, out0);
            out0.flush();
            fos0.close();

            // Arraylist, [strings of [image unique id]]] to be uploaded to the ElasticSearch
            FileOutputStream fos1 = context.openFileOutput(IMAGEUPLOADLIST, Context.MODE_PRIVATE);
            BufferedWriter out1 = new BufferedWriter(new OutputStreamWriter(fos1));

            ArrayList<String> upload = new ArrayList<String>();
            Gson gson1 = new Gson();
            gson1.toJson(upload, out1);
            out1.flush();
            fos1.close();

            // Arraylist, [strings of [image unique id]]] to be deleted from ElasticSearch
            FileOutputStream fos2 = context.openFileOutput(IMAGEDELETELIST, Context.MODE_PRIVATE);
            BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(fos2));

            ArrayList<String> delete = new ArrayList<String>();
            Gson gson2 = new Gson();
            gson2.toJson(delete, out2);
            out2.flush();
            fos2.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    /**
     * Clear the local arraylists
     */

    /**
     * Save the base64String to file name by the uniqueID
     *
     * @param context     the context
     * @param imageBase64 the image base 64 String
     * @param base64Id    the base 64 id
     */
    public void saveBase64(Context context, String imageBase64, String base64Id){
        try {
            FileOutputStream fos = context.openFileOutput(base64Id, Context.MODE_PRIVATE);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(fos));
            Gson gson = new Gson();
            gson.toJson(imageBase64, out);
            out.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    /**
     * Load the origin list and add/delete the ids in the array list appropriately
     *
     * @param context           the context
     * @param mode              the mode
     * @param imageIdToBeSave   the image id to be save
     * @param imageIdToBeDelete the image id to be delete
     * @return the boolean
     */
    public boolean updateOfflineArrayList(Context context, String mode, String imageIdToBeSave, String imageIdToBeDelete){
        try {
            ArrayList<String> updateList = loadImageList(context, "upload");
            ArrayList<String> deleteList = loadImageList(context, "delete");
            ArrayList<String> onlineList = loadImageList(context, "online");
            boolean contains = onlineList.contains(imageIdToBeDelete);
            // Change accordingly
            if (mode.equals("update")) {
                if(imageIdToBeDelete != null && contains) {
                    deleteList.add(imageIdToBeDelete);
                }
                updateList.add(imageIdToBeSave);
            }else if (mode.equals("delete")) {
                updateList.remove(imageIdToBeDelete);
                if(contains) {
                    deleteList.add(imageIdToBeDelete);
                }
            } else if (mode.equals("online")){
                if (imageIdToBeSave != null) {
                    onlineList.add(imageIdToBeSave);
                } else if (imageIdToBeDelete != null){
                    onlineList.remove(imageIdToBeDelete);
                }
                FileOutputStream fos = context.openFileOutput(IMAGEONLINE, Context.MODE_PRIVATE);
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(fos));
                Gson gson = new Gson();
                gson.toJson(onlineList, out);
                out.flush();
                fos.close();
            }

            FileOutputStream fos = context.openFileOutput(IMAGEUPLOADLIST, Context.MODE_PRIVATE);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(fos));
            Gson gson = new Gson();
            gson.toJson(updateList, out);
            out.flush();
            fos.close();

            FileOutputStream fos1 = context.openFileOutput(IMAGEDELETELIST, Context.MODE_PRIVATE);
            BufferedWriter out1 = new BufferedWriter(new OutputStreamWriter(fos1));
            Gson gson1 = new Gson();
            gson1.toJson(deleteList, out1);
            out1.flush();
            fos.close();
            if (contains ){
                return true;
            } else {
                return false;
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    /**
     * Load base 64 string.
     *
     * @param context  the context
     * @param fileName the String image file name
     * @return the string
     */
    public String loadBase64(Context context, String fileName){
        try {
            FileInputStream fis = context.openFileInput(fileName);
            BufferedReader in = new BufferedReader(new InputStreamReader(fis));
            Gson gson = new Gson();
            Type type = new TypeToken<String>(){}.getType();
            base64str = gson.fromJson(in, type); // deserializes json into Map
            fis.close();
            return base64str;
        } catch (FileNotFoundException e) {
            return null;
        } catch (NullPointerException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Receives parameter of current context and the mode of loading, to load the online arraylist,
     * use "online",to load the upload list, use "upload", if want to have delete list, use "delete"
     *
     * @param context the context
     * @param mode    the mode ("upload", "delete", "online")
     * @return array list
     */
    public ArrayList<String> loadImageList(Context context, String mode){
        try {
            String filename;
            if (mode.equals("upload")){
                filename = IMAGEUPLOADLIST;
            } else if (mode.equals("delete")){
                filename = IMAGEDELETELIST;
            } else if (mode.equals("online")){
                filename = IMAGEONLINE;
            } else{
                return null;
            }
            FileInputStream fis = context.openFileInput(filename);
            BufferedReader in = new BufferedReader(new InputStreamReader(fis));
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<String>>(){}.getType();
            imageList = gson.fromJson(in, type); // deserializes json into ArrayList<String>
            fis.close();
            return imageList;
        } catch (FileNotFoundException e) {
            return null;
        } catch (NullPointerException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
