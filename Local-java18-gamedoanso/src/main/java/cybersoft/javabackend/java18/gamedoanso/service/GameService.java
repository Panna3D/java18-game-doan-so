package cybersoft.javabackend.java18.gamedoanso.service;

import cybersoft.javabackend.java18.gamedoanso.model.GameSession;
import cybersoft.javabackend.java18.gamedoanso.model.Player;
import cybersoft.javabackend.java18.gamedoanso.repository.GameSessionRepository;
import cybersoft.javabackend.java18.gamedoanso.repository.GuessRepository;
import cybersoft.javabackend.java18.gamedoanso.repository.PlayerRepository;

import java.util.List;

public class GameService {
    private static GameService INSTANCE = null;
    private final GameSessionRepository gameSessionRepository;
    private final PlayerRepository playerRepository;
    private final GuessRepository guessRepository;

    private GameService() {
        gameSessionRepository = new GameSessionRepository();
        playerRepository = new PlayerRepository();
        guessRepository = new GuessRepository();
    }

    public static GameService getINSTANCE() {
        if (INSTANCE == null)
            INSTANCE = new GameService();
        return INSTANCE;
    }

    public GameSession createGame(String username) {
        var gameSession = new GameSession(username);
        gameSession.setActive(true);

        // deactivate other games
        deactiveAllGames(username);

        gameSessionRepository.save(gameSession);

        return gameSession;
    }

    private void deactiveAllGames(String username) {
        List<GameSession> gameSessions = gameSessionRepository.findByUsername(username);
        if (gameSessions == null)
            return;

        gameSessions.stream()
                .filter(GameSession::isActive)
                .toList()
                .forEach(g -> g.setActive(false));
    }

    public Player dangNhap(String username, String password) {
        Player player = playerRepository.findByUsername(username);

        if (player == null)
            return null;

        if (player.getPassword().equals(password))
            return player;

        return null;
    }

    public Player dangKy(String username, String password, String name) {
        if (!isValidUser(username, password, name))
            return null;

        boolean userExisted = playerRepository.existedByUsername(username);

        if (userExisted)
            return null;

        Player newUser = new Player(username, password, name);
        playerRepository.save(newUser);

        return newUser;
    }

    private boolean isValidUser(String username, String password, String name) {
        if (username == null || "".equals(username.trim()))
            return false;

        if (password == null || "".equals(password.trim()))
            return false;

        return name != null && !"".equals(name.trim());
    }

    public GameSession getCurrentGame(String username) {
        List<GameSession> games = gameSessionRepository.findByUsername(username);
        // get current active game, if there's no game -> create new one

        var activeGame = games.isEmpty()
                ? createGame(username)
                : games.stream()
                .filter(GameSession::isActive)
                .findFirst()
                .orElseGet(() -> createGame(username));

        // get guess list and add to game
        activeGame.getGuess()
                .addAll(guessRepository
                        .findBySession(activeGame.getId())
                );

        return activeGame;
    }

    public GameSession skipAndPlayNewGame(String username) {
        return createGame(username);
    }

    public GameSession getGameSession(String id) {
        return gameSessionRepository.findById(id);
    }

    public static class KetQua {
        public static final String GREATER_THAN = "S??? b???n ??o??n l???n h??n k???t qu???.";
        public static final String LESSER_THAN = "S??? b???n ??o??n b?? h??n k???t qu???.";
        public static final String PINGO = "??o??n ch??nh x??c!";

        private KetQua() {
        }
    }
}
