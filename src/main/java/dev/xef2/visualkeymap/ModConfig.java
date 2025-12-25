package dev.xef2.visualkeymap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ModConfig {

    public boolean showNumpad = true;
    public boolean prioritizeConflictingKeybinds = true;

    private static final Logger LOGGER = LoggerFactory.getLogger(ModConfig.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final String CONFIG_FILE_NAME = "visualkeymap.json";
    private static final File CONFIG_FILE = new File(
            FabricLoader.getInstance().getConfigDir().toFile(),
            CONFIG_FILE_NAME
    );

    private static ModConfig INSTANCE;

    public static ModConfig getInstance() {
        return INSTANCE;
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                ModConfig config = GSON.fromJson(reader, ModConfig.class);

                if (config != null) {
                    INSTANCE = config;
                }
            } catch (IOException e) {
                LOGGER.error("Failed to load config file.", e);
            }
        }

        if (INSTANCE == null) {
            INSTANCE = new ModConfig();
        }

        INSTANCE.save();
    }

    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save config file.", e);
        }
    }
}
