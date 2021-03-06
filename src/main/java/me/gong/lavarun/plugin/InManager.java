package me.gong.lavarun.plugin;

import me.gong.lavarun.plugin.util.StringUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class InManager {
    private static InManager instance = new InManager();

    public static InManager get() {
        return instance;
    }

    private InManager() {

    }

    private List<Object> instances;

    public void clearInstances() {
        getInstances().forEach(s -> {
            try {
                s.getClass().getMethod("onDisable").invoke(s);
            } catch (Exception ex) {
                //no method
            }
        });
        getInstances().clear();
    }

    public <T> T addInstance(T instance) {
        getInstances().add(instance);
        if(!(instance instanceof JavaPlugin)) //boi
            try {
                instance.getClass().getMethod("onEnable").invoke(instance);
            } catch (Exception ex) {
                //no method
            }
        return instance;
    }

    public <T> T getInstance(Class<T> type) {
        //noinspection unchecked
        return (T) getInstances().stream().filter(type::isInstance).findFirst().orElse(null);
    }

    private List<Object> getInstances() {
        //lazy initialization
        if(instances == null) instances = new CopyOnWriteArrayList<>();
        return instances;
    }
}
