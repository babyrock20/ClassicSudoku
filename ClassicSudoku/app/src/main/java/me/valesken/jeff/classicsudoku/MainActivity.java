package me.valesken.jeff.classicsudoku;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Jeff on 5/1/2015.
 * Last updated on 7/5/2015.
 */

public class MainActivity extends Activity {

    private static FragmentManager fm;
    private static File[] files;
    private static String[] filenames;
    private static File saveDir;
    private static File highScores;
    private static File autoSaveFile;
    private static JSONObject highScoresJSON;
    private static GameFragment game;
    private static MainFragment main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        main = new MainFragment();

        // Set up high scores file (if doesn't exist) and load high scores
        try {
            highScores = new File(getFilesDir(), "HighScores.txt");
            if (!highScores.exists()) {
                BufferedWriter buff = new BufferedWriter(new FileWriter(highScores, false));
                buff.write(getResources().getString(R.string.high_scores_init));
                buff.flush();
                buff.close();
            }
            BufferedReader buff_r = new BufferedReader(new FileReader(highScores));
            highScoresJSON = new JSONObject(buff_r.readLine());
            buff_r.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        // Set up save directory and load files
        saveDir = new File(getFilesDir(), "save");
        saveDir.mkdir();
        loadFiles();

        fm = getFragmentManager();

        if (savedInstanceState == null) {
            fm.beginTransaction()
                .add(R.id.container, main)
                .commit();
        }
    }

    public void loadFiles() {
        // Load AutoSave.txt (if it exists)
        autoSaveFile = null;
        files = getFilesDir().listFiles();
        for(File f : files)
            if (f.getName().equals("AutoSave.txt")) {
                autoSaveFile = f;
                break;
            }

        // Load all other save files
        files = saveDir.listFiles();
        if(files == null)
            filenames = null;
        else {
            filenames = new String[files.length];
            for (int i = 0; i < files.length; ++i)
                filenames[i] = files[i].getName().replace(".txt", "");
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(fm.getBackStackEntryCount() == 1) {
            setTitle(getResources().getString(R.string.app_name));
            if(fm.getBackStackEntryAt(fm.getBackStackEntryCount()-1).getName().equals("Game"))
                game.handleAutoSave();
        }
        super.onBackPressed();
    }

    /*
     * Getters
     */

    public JSONObject getHighScoresJSON() { return highScoresJSON; }

    public File getHighScoresFile() { return highScores; }

    public File getSaveDir() { return saveDir; }

    public File getAutoSaveFile() { return autoSaveFile; }

    public File[] getFiles() { return files; }

    public String[] getFilenames() { return filenames; }

    public GameFragment getGame() { return game; }

    /*
     * Setters
     */

    public void enableResumeGameButton(boolean enabled) { main.enableResumeGameButton(enabled); }

    public void setGame(GameFragment _game) { game = _game; }

}