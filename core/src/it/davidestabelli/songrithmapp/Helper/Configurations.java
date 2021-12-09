package it.davidestabelli.songrithmapp.Helper;

import com.badlogic.gdx.Input;

public class Configurations {
    private static Configurations instance;

    // KEYS
    @KeyConfiguration
    public int Edit_Left_Beat_Key = Input.Keys.F;
    @KeyConfiguration
    public int Edit_Right_Beat_Key = Input.Keys.J;
    @KeyConfiguration
    public int Delete_Beat_Key = Input.Keys.BACKSPACE;
    @KeyConfiguration
    public int Pause_Music_Key = Input.Keys.SPACE;

    // SAMPLING
    public int Beat_Samples_Per_Second = 10;

    // ANIMATION
    public float START_ANIMATION_CIRCLE_DIAMETER_FRACTION = 2.5f;
    public float ANIMATION_DIAMETER_DELTA = 60;

    public static Configurations getInstance() {
        if (instance == null) {
            instance = ImportedFileHandler.readConfigurations();
        }
        return instance;
    }
}