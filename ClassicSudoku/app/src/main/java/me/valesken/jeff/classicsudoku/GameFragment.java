package me.valesken.jeff.classicsudoku;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import me.valesken.jeff.sudoku_structure.Board;

/**
 * Created by Jeff on 7/12/2015.
 *
 * This fragment contains the actual board and UI that comprise the main game.
 */
public class GameFragment extends Fragment {

    //region Member Variables
    private LayoutInflater inflater;
    private View rootView;
    private ViewGroup container;
    private MainActivity activity;
    private int difficulty;
    private int boardSize;
    private Board board;
    private GridManager gridManager;
    private TableLayout grid;
    private View currentTile;
    private int currentPosition;
    private View save_dialog_view;
    private AlertDialog.Builder save_alert;
    private String filename;
    private File saveFile;
    private File[] files;
    private String[] filenames;
    private TextView clock_tv;
    private volatile boolean paused = false;
    private volatile boolean gameOver = false;
    //endregion

    //region Clock Thread
    private Thread clockThread = new Thread(new Runnable() {
        @Override
        public void run() {
            String clock = (String)clock_tv.getText();
            int seconds = Integer.parseInt(clock.substring(3, 5));
            int minutes = Integer.parseInt(clock.substring(0, 2));
            while(!gameOver) {
                try {
                    do {
                        TimeUnit.SECONDS.sleep(1);
                    }while(paused);
                    if(gameOver)
                        break;
                    char[] temp = clock.toCharArray();
                    seconds += 1;
                    if (seconds > 59) {
                        seconds = 0;
                        minutes += 1;
                    }

                    if (minutes < 10)
                        temp[0] = '0';
                    else
                        temp[0] = Character.forDigit(minutes / 10, 10);
                    temp[1] = Character.forDigit(minutes % 10, 10);
                    if (seconds < 10)
                        temp[3] = '0';
                    else
                        temp[3] = Character.forDigit(seconds / 10, 10);
                    temp[4] = Character.forDigit(seconds % 10, 10);

                    final String clock_string = String.valueOf(temp);
                    clock_tv.post(new Runnable() {
                        @Override
                        public void run() {
                            clock_tv.setText(clock_string);
                        }
                    });
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
                    Thread.currentThread().interrupt();
                }
            }
        }
    });
    //endregion

    public GameFragment() {
        difficulty = 0;
    }

    public void newGame(int _boardSize, int _difficulty)
    {
        boardSize = _boardSize;
        board = new Board(boardSize);
        difficulty = board.NewGame(_difficulty);
        saveFile = null;
    }

    public void loadGame(int _boardSize, File _saveFile)
    {
        saveFile = _saveFile;
        boardSize = _boardSize;
        board = new Board(boardSize);

        try {
            BufferedReader buff = new BufferedReader(new FileReader(saveFile));
            JSONObject jsonObject = new JSONObject(buff.readLine());
            buff.close();
            difficulty = board.LoadGame(jsonObject);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater _inflater, ViewGroup _container, Bundle savedInstanceState) {
        container = _container;
        inflater = _inflater;
        rootView = inflater.inflate(R.layout.fragment_game, container, false);
        activity = (MainActivity)getActivity();
        activity.setTitle(getResources().getString(R.string.app_name).concat(" - Game"));

            /* This OnTouchListener ensures the user cannot accidentally touch Views from the previous fragment. */
        rootView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event) { return true; }
        });

        files = activity.getFiles();
        filenames = activity.getFilenames();

        //region Difficulty Level
        TextView difficulty_tv = (TextView)rootView.findViewById(R.id.difficulty_text);
        switch (difficulty)
        {
            case 1:
                difficulty_tv.setText("Easy");
                break;
            case 2:
                difficulty_tv.setText("Medium");
                break;
            default:
                difficulty_tv.setText("Hard");
                break;
        }
        //endregion

        //region Playing Grid Setup
        grid = (TableLayout) rootView.findViewById(R.id.grid);
        gridManager = new GridManager(rootView.getContext(), board, boardSize, grid, this);
        gridManager.initializeGrid();
        //endregion

        //region Control Grid OnClickListeners
        Button one = (Button) rootView.findViewById(R.id.one);
        one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentTile != null && !gameOver) {
                    gameOver = gridManager.updateTile(currentPosition, 1);
                    if(gameOver)
                        updateHighScore();
                }
                if(gameOver)
                    Toast.makeText(rootView.getContext(), "Game Over! You win!", Toast.LENGTH_LONG).show();
            }
        });
        Button two = (Button) rootView.findViewById(R.id.two);
        two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentTile != null && !gameOver) {
                    gameOver = gridManager.updateTile(currentPosition, 2);
                    if(gameOver)
                        updateHighScore();
                }
                if(gameOver)
                    Toast.makeText(rootView.getContext(), "Game Over! You win!", Toast.LENGTH_LONG).show();
            }
        });
        Button three = (Button) rootView.findViewById(R.id.three);
        three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentTile != null && !gameOver) {
                    gameOver = gridManager.updateTile(currentPosition, 3);
                    if(gameOver)
                        updateHighScore();
                }
                if(gameOver)
                    Toast.makeText(rootView.getContext(), "Game Over! You win!", Toast.LENGTH_LONG).show();
            }
        });
        Button four = (Button) rootView.findViewById(R.id.four);
        four.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentTile != null && !gameOver) {
                    gameOver = gridManager.updateTile(currentPosition, 4);
                    if(gameOver)
                        updateHighScore();
                }
                if(gameOver)
                    Toast.makeText(rootView.getContext(), "Game Over! You win!", Toast.LENGTH_LONG).show();
            }
        });
        Button five = (Button) rootView.findViewById(R.id.five);
        five.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentTile != null && !gameOver) {
                    gameOver = gridManager.updateTile(currentPosition, 5);
                    if(gameOver)
                        updateHighScore();
                }
                if(gameOver)
                    Toast.makeText(rootView.getContext(), "Game Over! You win!", Toast.LENGTH_LONG).show();
            }
        });
        Button six = (Button) rootView.findViewById(R.id.six);
        six.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentTile != null && !gameOver) {
                    gameOver = gridManager.updateTile(currentPosition, 6);
                    if(gameOver)
                        updateHighScore();
                }
                if(gameOver)
                    Toast.makeText(rootView.getContext(), "Game Over! You win!", Toast.LENGTH_LONG).show();
            }
        });
        Button seven = (Button) rootView.findViewById(R.id.seven);
        seven.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentTile != null && !gameOver) {
                    gameOver = gridManager.updateTile(currentPosition, 7);
                    if(gameOver)
                        updateHighScore();
                }
                if(gameOver)
                    Toast.makeText(rootView.getContext(), "Game Over! You win!", Toast.LENGTH_LONG).show();
            }
        });
        Button eight = (Button) rootView.findViewById(R.id.eight);
        eight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentTile != null && !gameOver) {
                    gameOver = gridManager.updateTile(currentPosition, 8);
                    if(gameOver)
                        updateHighScore();
                }
                if(gameOver)
                    Toast.makeText(rootView.getContext(), "Game Over! You win!", Toast.LENGTH_LONG).show();
            }
        });
        Button nine = (Button) rootView.findViewById(R.id.nine);
        nine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentTile != null && !gameOver) {
                    gameOver = gridManager.updateTile(currentPosition, 9);
                    if(gameOver)
                        updateHighScore();
                }
                if(gameOver)
                    Toast.makeText(rootView.getContext(), "Game Over! You win!", Toast.LENGTH_LONG).show();
            }
        });
        Button clear = (Button) rootView.findViewById(R.id.clear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentTile != null && !gameOver)
                    gridManager.clearTile(currentPosition);
                if(gameOver)
                    Toast.makeText(rootView.getContext(), "Game Over! You win!", Toast.LENGTH_LONG).show();
            }
        });
        Button noteMode_button = (Button) rootView.findViewById(R.id.mode_switch);
        noteMode_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gameOver)
                    Toast.makeText(rootView.getContext(), "Game Over! You win!", Toast.LENGTH_LONG).show();
                else if(currentTile != null)
                    gridManager.toggleMode(currentPosition);
            }
        });
        Button hint_button = (Button) rootView.findViewById(R.id.hint);
        hint_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!gameOver) {
                    gameOver = gridManager.getHint();
                    if(gameOver)
                        updateHighScore();
                }
                if(gameOver)
                    Toast.makeText(rootView.getContext(), "Game Over! You win!", Toast.LENGTH_LONG).show();
            }
        });
        Button solve_button = (Button) rootView.findViewById(R.id.solve);
        solve_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!gameOver) {
                    gridManager.solve();
                    gameOver = true;
                }
                if(gameOver)
                    Toast.makeText(rootView.getContext(), "Game Over! You win!", Toast.LENGTH_LONG).show();
            }
        });
        Button pause_button = (Button) rootView.findViewById(R.id.pause);
        pause_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gameOver)
                    Toast.makeText(rootView.getContext(), "Game Over! You win!", Toast.LENGTH_LONG).show();
                else {
                    paused = true;
                    new AlertDialog.Builder(rootView.getContext())
                            .setMessage("Game is paused.")
                            .setNeutralButton("Resume", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    paused = false;
                                }
                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    paused = false;
                                }
                            })
                            .show();
                }
            }
        });

        //endregion

        //region Save Logic & Button
        final AlertDialog.Builder overwrite_alert = new AlertDialog.Builder(rootView.getContext())
                .setNegativeButton("Go Back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showSaveDialog();
                    }
                })
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveGame(activity.getSaveDir(), filename);
                        paused = false;
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        showSaveDialog();
                    }
                });

        save_alert = new AlertDialog.Builder(rootView.getContext())
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        paused = false;
                    }
                })
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        filename = ((EditText) (save_dialog_view.findViewById(R.id.save_textbox))).getText().toString();
                        activity.loadFiles();
                        files = activity.getFiles();
                        filenames = activity.getFilenames();
                        boolean found = false;
                        for (String f : filenames)
                            if (f.equals(filename)) {
                                found = true;
                                break;
                            }
                        if (found) {
                            InputMethodManager imm = (InputMethodManager) rootView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(save_dialog_view.findViewById(R.id.save_textbox).getWindowToken(), 0);
                            overwrite_alert.setMessage(String.format("%s already exists. Overwrite it?", filename));
                            overwrite_alert.show();
                        } else {
                            saveGame(activity.getSaveDir(), filename);
                            paused = false;
                        }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        paused = false;
                    }
                });

        Button save_button = (Button) rootView.findViewById(R.id.save);
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gameOver)
                    Toast.makeText(rootView.getContext(), "Game Over! You win!", Toast.LENGTH_LONG).show();
                else {
                    paused = true;

                    // Get filename to display
                    if (saveFile != null)
                        filename = saveFile.getName().replace(".txt", "");
                    else {
                        int num, maxNum = 0;
                        String name;
                        filename = difficulty == 1 ? "Easy" : (difficulty == 2 ? "Medium" : "Hard");
                        for (File f : files) {
                            name = f.getName().replace(".txt", "");
                            if (name.startsWith(filename) && name.length() > filename.length()) {
                                try {
                                    num = Integer.parseInt(name.replace(filename, ""));
                                    if (num > maxNum)
                                        maxNum = num;
                                }
                                catch (NumberFormatException e) { } // e.g. ignore "EasyTown"
                            }
                        }
                        filename = filename.concat(Integer.toString(maxNum + 1));
                    }
                    showSaveDialog();
                }
            }
        });
        //endregion

        //region Start Clock
        clock_tv = (TextView)rootView.findViewById(R.id.clock);
        clock_tv.setText(board.getTime());
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                clockThread.start();
                rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        //endregion

        return rootView;
    }

    public void setCurrentPosition(int _currentPosition, View _currentTile) {
        this.currentPosition = _currentPosition;
        this.currentTile = _currentTile;
    }

    public void showSaveDialog() {
        save_dialog_view = inflater.inflate(R.layout.save_dialog_layout, container, false);
        ((EditText)(save_dialog_view.findViewById(R.id.save_textbox))).setText(filename);
        save_alert.setView(save_dialog_view);
        save_alert.show();
    }

    public void handleAutoSave() {
        File autoSaveFile = activity.getAutoSaveFile();
        if (!gameOver) {
            saveGame(activity.getFilesDir(), "AutoSave");
            activity.enableResumeGameButton(true);
            activity.loadFiles();
            files = activity.getFiles();
            filenames = activity.getFilenames();
        }
        else if (autoSaveFile != null) {
            activity.enableResumeGameButton(false);
            autoSaveFile.delete();
            activity.loadFiles();
            files = activity.getFiles();
            filenames = activity.getFilenames();
        }
    }

    public void saveGame(File saveDir, String _filename) {
        try {
            File saveFile = new File(saveDir, _filename.concat(".txt"));
            BufferedWriter buff = new BufferedWriter(new FileWriter(saveFile, false));
            JSONObject jsonObject = board.save((String) clock_tv.getText());
            buff.write(jsonObject.toString());
            buff.flush();
            buff.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Update high score and save to file
    public void updateHighScore() {
        try {
            JSONObject highScoresJSON = activity.getHighScoresJSON();
            JSONArray jsonScores;
            switch (difficulty) {
                case 1:
                    jsonScores = highScoresJSON.getJSONArray("Easy");
                    break;
                case 2:
                    jsonScores = highScoresJSON.getJSONArray("Medium");
                    break;
                default:
                    jsonScores = highScoresJSON.getJSONArray("Hard");
                    break;
            }

            String currentTime = clock_tv.getText().toString();
            int index = currentTime.indexOf(':');
            int minutes = Integer.parseInt(currentTime.substring(0, index));
            int seconds = Integer.parseInt(currentTime.substring(index+1));
            String scoreTime;
            int scoreMinutes, scoreSeconds;
            ArrayList<String> scores = new ArrayList<String>();

            //region Find Current Score Place
            boolean inserted = false;
            for(int i = 0; i < 10 && !jsonScores.getString(i).isEmpty(); ++i) {
                scoreTime = jsonScores.getString(i);
                index = scoreTime.indexOf(':');
                scoreMinutes = Integer.parseInt(scoreTime.substring(0, index));
                scoreSeconds = Integer.parseInt(scoreTime.substring(index+1));
                if(!inserted && scoreMinutes > minutes) {
                    scores.add(currentTime);
                    inserted = true;
                }
                else if(!inserted && scoreMinutes == minutes && scoreSeconds > seconds) {
                    scores.add(currentTime);
                    inserted = true;
                }
                scores.add(scoreTime);
            }
            if(!inserted && scores.size() < 10)
                scores.add(currentTime);
            //endregion

            //region Update High Scores File
            for(int i = 0; i < 10 && i < scores.size(); ++i)
                jsonScores.put(i, scores.get(i));
            switch (difficulty) {
                case 1:
                    highScoresJSON.put("Easy", jsonScores);
                    break;
                case 2:
                    highScoresJSON.put("Medium", jsonScores);
                    break;
                default:
                    highScoresJSON.put("Hard", jsonScores);
                    break;
            }
            BufferedWriter buff = new BufferedWriter(new FileWriter(activity.getHighScoresFile(), false));
            buff.write(highScoresJSON.toString());
            buff.flush();
            buff.close();
            //endregion
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}