package hp.harsh.tictacbee.enums

/**
 * @purpose GameStatus - Enum to decide game status. Game is won or lost or draw?
 *
 * @author Harsh Patel
 */
enum class GameStatus(val statusName: String) {
    WIN("win"),
    LOST("lost"),
    DRAW("draw")
}