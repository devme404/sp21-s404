package byow.Core;

import java.util.List;
import java.util.Optional;

public class HallwayGenerationResult {
    public final boolean success;
    public final List<Hallway> hallways;
    public final Optional<String> message;

    HallwayGenerationResult(boolean success, List<Hallway> hallways) {
        this.success = success;
        this.hallways = hallways;
        this.message = Optional.empty();
    }

    HallwayGenerationResult(boolean success, List<Hallway> hallways, String message) {
        this.success = success;
        this.hallways = hallways;
        this.message = Optional.of(message);
    }
}
