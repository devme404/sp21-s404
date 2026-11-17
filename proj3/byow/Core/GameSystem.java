package byow.Core;

import java.util.List;
import java.util.Map;

import byow.TileEngine.TETile;

import java.util.ArrayList;
import java.util.HashMap;

public class GameSystem {
    private final int width, height;
    private final Map<Character, CommandHandler> handlers = new HashMap<>();
    private World world;

    private class Command {
        public final char command;
        public final String argument;

        Command(char command, String argument) {
            this.command = command;
            this.argument = argument;
        }
    }

    private void register(char type, CommandHandler handler) {
        handlers.put(type, handler);
    }

    GameSystem(int width, int height) {
        this.width = width;
        this.height = height;
        initialize();
    }

    @FunctionalInterface
    public interface CommandHandler {
        void execute(String value);
    }

    public void dispatch(Command command) {
        CommandHandler handler = handlers.get(command.command);
        if (handler == null) {
            throw new IllegalArgumentException("unknown command:" + command.command);
        }
        handler.execute(command.argument);
    }

    public void dispatchAll(List<Command> commands) {
        for (Command command : commands) {
            dispatch(command);
        }
    }

    void initialize() {
        register('n', value -> {
            WorldGenerator generator = new WorldGenerator(width, height, Long.parseLong(value));
            world = generator.generate();
        });
        register('w', value -> world.move('w'));
        register('s', value -> world.move('s'));
        register('a', value -> world.move('a'));
        register('d', value -> world.move('d'));
    }

    public List<Command> parseInput(String input) {
        List<Command> result = new ArrayList<>();
        int i = 0;

        while (i < input.length()) {
            char command = input.charAt(i++);
            String argument = null;

            if (command == 'n') {
                int start = i;
                while (i < input.length() && Character.isDigit(input.charAt(i))) {
                    i++;
                }
                if (start == i) {
                    throw new IllegalArgumentException("expected seed after n");
                }
                argument = input.substring(start, i);
            }

            result.add(new Command(command, argument));
        }

        return result;
    }

    public void runInput(String input) {
        List<Command> commands = parseInput(input);
        dispatchAll(commands);
    }

    public TETile[][] getTiles() {
        return world.getTiles();
    }
}
