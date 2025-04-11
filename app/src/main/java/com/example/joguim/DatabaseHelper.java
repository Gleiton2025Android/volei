package com.example.joguim;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "JoguimDB";
    private static final int DATABASE_VERSION = 6;

    // Tabelas
    private static final String TABLE_USERS = "users";
    private static final String TABLE_MATCHES = "matches";
    private static final String TABLE_PLAYERS = "players";
    private static final String TABLE_MATCH_PLAYERS = "match_players";
    private static final String TABLE_TEAMS = "teams";
    private static final String TABLE_TEAM_PLAYERS = "team_players";

    // Colunas comuns
    private static final String COLUMN_ID = "id";

    // Colunas de Users
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_USER_LEVEL = "user_level";
    private static final String COLUMN_USER_POSITION = "user_position";

    // Colunas de Matches
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_LOCATION = "location";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_USER_ID = "user_id";

    // Colunas de Players
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_POSITION = "position";
    private static final String COLUMN_LEVEL = "level";

    // Colunas de Match Players
    private static final String COLUMN_MATCH_ID = "match_id";
    private static final String COLUMN_PLAYER_ID = "player_id";
    private static final String COLUMN_CONFIRMED = "confirmed";

    // Colunas de Teams
    private static final String COLUMN_TEAM_NUMBER = "team_number";
    private static final String COLUMN_TEAM_ID = "team_id";

    // SQL de criação das tabelas
    private static final String CREATE_TABLE_USERS = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, " +
            COLUMN_PASSWORD + " TEXT NOT NULL, " +
            COLUMN_USER_LEVEL + " INTEGER DEFAULT 1, " +
            COLUMN_USER_POSITION + " TEXT DEFAULT 'PONTEIRO')";

    private static final String CREATE_TABLE_MATCHES = "CREATE TABLE IF NOT EXISTS " + TABLE_MATCHES + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_DATE + " INTEGER NOT NULL, " +
            COLUMN_LOCATION + " TEXT NOT NULL, " +
            COLUMN_DESCRIPTION + " TEXT, " +
            COLUMN_STATUS + " TEXT NOT NULL DEFAULT 'PENDING', " +
            COLUMN_USER_ID + " INTEGER NOT NULL, " +
            "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "))";

    private static final String CREATE_TABLE_PLAYERS = "CREATE TABLE IF NOT EXISTS " + TABLE_PLAYERS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_NAME + " TEXT NOT NULL, " +
            COLUMN_POSITION + " TEXT NOT NULL, " +
            COLUMN_LEVEL + " INTEGER NOT NULL)";

    private static final String CREATE_TABLE_MATCH_PLAYERS = "CREATE TABLE IF NOT EXISTS " + TABLE_MATCH_PLAYERS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_MATCH_ID + " INTEGER NOT NULL, " +
            COLUMN_PLAYER_ID + " INTEGER NOT NULL, " +
            COLUMN_CONFIRMED + " INTEGER NOT NULL DEFAULT 0, " +
            "FOREIGN KEY(" + COLUMN_MATCH_ID + ") REFERENCES " + TABLE_MATCHES + "(" + COLUMN_ID + "), " +
            "FOREIGN KEY(" + COLUMN_PLAYER_ID + ") REFERENCES " + TABLE_PLAYERS + "(" + COLUMN_ID + "), " +
            "UNIQUE(" + COLUMN_MATCH_ID + ", " + COLUMN_PLAYER_ID + "))";

    private static final String CREATE_TABLE_TEAMS = "CREATE TABLE IF NOT EXISTS " + TABLE_TEAMS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_MATCH_ID + " INTEGER NOT NULL, " +
            COLUMN_TEAM_NUMBER + " INTEGER NOT NULL, " +
            "FOREIGN KEY(" + COLUMN_MATCH_ID + ") REFERENCES " + TABLE_MATCHES + "(" + COLUMN_ID + "))";

    private static final String CREATE_TABLE_TEAM_PLAYERS = "CREATE TABLE IF NOT EXISTS " + TABLE_TEAM_PLAYERS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "team_id INTEGER NOT NULL, " +
            COLUMN_PLAYER_ID + " INTEGER NOT NULL, " +
            "FOREIGN KEY(team_id) REFERENCES " + TABLE_TEAMS + "(" + COLUMN_ID + "), " +
            "FOREIGN KEY(" + COLUMN_PLAYER_ID + ") REFERENCES " + TABLE_PLAYERS + "(" + COLUMN_ID + "), " +
            "UNIQUE(team_id, " + COLUMN_PLAYER_ID + "))";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Criando banco de dados...");
        try {
            db.execSQL(CREATE_TABLE_USERS);
            db.execSQL(CREATE_TABLE_MATCHES);
            db.execSQL(CREATE_TABLE_PLAYERS);
            db.execSQL(CREATE_TABLE_MATCH_PLAYERS);
            db.execSQL(CREATE_TABLE_TEAMS);
            db.execSQL(CREATE_TABLE_TEAM_PLAYERS);
            Log.d(TAG, "Banco de dados criado com sucesso!");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao criar banco de dados: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Atualizando banco de dados da versão " + oldVersion + " para " + newVersion);
        try {
            if (oldVersion < 4) {
                db.execSQL(CREATE_TABLE_MATCH_PLAYERS);
            }
            if (oldVersion < 5) {
                db.execSQL(CREATE_TABLE_TEAMS);
                db.execSQL(CREATE_TABLE_TEAM_PLAYERS);
            }
            if (oldVersion < 6) {
                try {
                    // Verificar se as colunas já existem antes de tentar adicioná-las
                    Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " LIMIT 1", null);
                    List<String> columns = new ArrayList<>();
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        columns.add(cursor.getColumnName(i).toLowerCase());
                    }
                    cursor.close();

                    if (!columns.contains(COLUMN_USER_LEVEL.toLowerCase())) {
                        db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + 
                            COLUMN_USER_LEVEL + " INTEGER DEFAULT 1");
                    }
                    if (!columns.contains(COLUMN_USER_POSITION.toLowerCase())) {
                        db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + 
                            COLUMN_USER_POSITION + " TEXT DEFAULT 'PONTEIRO'");
                    }

                    // Atualizar registros existentes que não têm as novas colunas
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_USER_LEVEL, 1);
                    values.put(COLUMN_USER_POSITION, Player.Position.PONTEIRO.name());
                    db.update(TABLE_USERS, values, 
                        COLUMN_USER_LEVEL + " IS NULL OR " + COLUMN_USER_POSITION + " IS NULL", null);

                    Log.d(TAG, "Colunas de nível e posição adicionadas com sucesso");
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao adicionar colunas: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao atualizar banco de dados: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Métodos para Usuários
    public long addUser(String username, String password, int level, Player.Position position) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_USER_LEVEL, level);
        values.put(COLUMN_USER_POSITION, position.name());
        
        long userId = db.insert(TABLE_USERS, null, values);
        
        if (userId != -1) {
            // Criar jogador automaticamente
            Player player = new Player(username, position, level);
            long playerId = addPlayer(player);
            Log.d(TAG, "Jogador criado automaticamente para usuário: " + userId + ", playerID: " + playerId);
        }
        
        return userId;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID},
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username, password}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public long getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID},
                COLUMN_USERNAME + "=?", new String[]{username}, null, null, null);
        long userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
        }
        cursor.close();
        return userId;
    }

    public void updateUser(long userId, String username, String password, int level, Player.Position position) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_USERNAME, username);
            if (password != null && !password.isEmpty()) {
                values.put(COLUMN_PASSWORD, password);
            }
            values.put(COLUMN_USER_LEVEL, level);
            values.put(COLUMN_USER_POSITION, position.name());
            
            int rows = db.update(TABLE_USERS, values,
                    COLUMN_ID + "=?", new String[]{String.valueOf(userId)});
            
            if (rows > 0) {
                // Atualizar jogador correspondente
                String query = "SELECT " + COLUMN_ID + " FROM " + TABLE_PLAYERS +
                        " WHERE " + COLUMN_NAME + "=?";
                Cursor cursor = db.rawQuery(query, new String[]{username});
                
                if (cursor.moveToFirst()) {
                    long playerId = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
                    Player player = new Player(username, position, level);
                    player.setId(playerId);
                    updatePlayer(player);
                }
                cursor.close();
            }
            
            Log.d(TAG, "Usuário atualizado: ID=" + userId + ", Rows=" + rows);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao atualizar usuário: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public User getUser(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;
        
        try {
            Log.d(TAG, "Buscando usuário com ID: " + userId);
            
            Cursor cursor = db.query(TABLE_USERS,
                    new String[]{COLUMN_USERNAME, COLUMN_USER_LEVEL, COLUMN_USER_POSITION},
                    COLUMN_ID + "=?",
                    new String[]{String.valueOf(userId)},
                    null, null, null);

            Log.d(TAG, "Cursor criado, tem registros: " + cursor.getCount());

            if (cursor.moveToFirst()) {
                String username = cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME));
                int level = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_LEVEL));
                String positionStr = cursor.getString(cursor.getColumnIndex(COLUMN_USER_POSITION));
                
                Log.d(TAG, "Dados encontrados - Username: " + username + 
                    ", Level: " + level + ", Position: " + positionStr);
                
                user = new User(username);
                user.setId(userId);
                user.setLevel(level);
                user.setPosition(Player.Position.valueOf(positionStr));
                
                Log.d(TAG, "Objeto User criado com sucesso");
            } else {
                Log.e(TAG, "Nenhum usuário encontrado com o ID: " + userId);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao buscar usuário: " + e.getMessage());
            e.printStackTrace();
        }
        
        return user;
    }

    // Métodos para Partidas
    public long addMatch(Match match, long userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, match.getDateTime().getTime());
        values.put(COLUMN_LOCATION, match.getLocation());
        values.put(COLUMN_DESCRIPTION, match.getDescription());
        values.put(COLUMN_STATUS, match.getStatus().name());
        values.put(COLUMN_USER_ID, userId);
        
        long id = db.insert(TABLE_MATCHES, null, values);
        Log.d(TAG, "Partida adicionada: ID=" + id + ", Local=" + match.getLocation());
        return id;
    }

    public List<Match> getMatches(long userId) {
        List<Match> matches = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        try {
            // Ordenar por status (CONFIRMED primeiro, depois PENDING, depois CANCELLED)
            // E dentro de cada status, ordenar por data (mais recente primeiro)
            String orderBy = "CASE " + COLUMN_STATUS + 
                " WHEN 'CONFIRMED' THEN 0" +
                " WHEN 'PENDING' THEN 1" +
                " WHEN 'CANCELLED' THEN 2 END, " +
                COLUMN_DATE + " DESC";

            // Removendo o filtro por usuário para mostrar todas as partidas
            Cursor cursor = db.query(TABLE_MATCHES, null,
                    null, null, null, null, orderBy);

            Log.d(TAG, "Buscando todas as partidas. Encontradas: " + cursor.getCount());

            if (cursor.moveToFirst()) {
                do {
                    try {
                        Match match = new Match(
                            new Date(cursor.getLong(cursor.getColumnIndex(COLUMN_DATE))),
                            cursor.getString(cursor.getColumnIndex(COLUMN_LOCATION)),
                            cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION))
                        );
                        match.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
                        match.setStatus(Match.MatchStatus.valueOf(
                            cursor.getString(cursor.getColumnIndex(COLUMN_STATUS))
                        ));
                        matches.add(match);
                        Log.d(TAG, "Partida carregada: ID=" + match.getId() + 
                            ", Local=" + match.getLocation() + 
                            ", Status=" + match.getStatus());
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao carregar partida do cursor: " + e.getMessage());
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao buscar partidas: " + e.getMessage());
            e.printStackTrace();
        }
        
        return matches;
    }

    public void updateMatchStatus(long matchId, Match.MatchStatus status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STATUS, status.name());
        
        int rows = db.update(TABLE_MATCHES, values,
                COLUMN_ID + "=?", new String[]{String.valueOf(matchId)});
        Log.d(TAG, "Status da partida atualizado: ID=" + matchId + ", Status=" + status + ", Rows=" + rows);
    }

    public void updateMatch(Match match) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, match.getDateTime().getTime());
        values.put(COLUMN_LOCATION, match.getLocation());
        values.put(COLUMN_DESCRIPTION, match.getDescription());
        values.put(COLUMN_STATUS, match.getStatus().name());
        
        int rows = db.update(TABLE_MATCHES, values,
                COLUMN_ID + "=?", new String[]{String.valueOf(match.getId())});
        Log.d(TAG, "Partida atualizada: ID=" + match.getId() + ", Local=" + match.getLocation() + ", Rows=" + rows);
    }

    // Métodos para Jogadores
    public long addPlayer(Player player) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = -1;
        
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, player.getName());
            values.put(COLUMN_POSITION, player.getPosition().name());
            values.put(COLUMN_LEVEL, player.getLevel());
            
            id = db.insertOrThrow(TABLE_PLAYERS, null, values);
            Log.d(TAG, "Jogador adicionado com sucesso: ID=" + id + ", Nome=" + player.getName());
        } catch (Exception e) {
            Log.e(TAG, "Erro ao adicionar jogador: " + e.getMessage());
            e.printStackTrace();
        }
        
        return id;
    }

    public List<Player> getAllPlayers() {
        List<Player> players = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        try {
            Cursor cursor = db.query(TABLE_PLAYERS, null,
                    null, null, null, null, COLUMN_NAME + " ASC");

            Log.d(TAG, "Buscando jogadores. Encontrados: " + cursor.getCount());

            if (cursor.moveToFirst()) {
                do {
                    try {
                        String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                        String positionStr = cursor.getString(cursor.getColumnIndex(COLUMN_POSITION));
                        int level = cursor.getInt(cursor.getColumnIndex(COLUMN_LEVEL));
                        long playerId = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));

                        Player player = new Player(name, Player.Position.valueOf(positionStr), level);
                        player.setId(playerId);
                        players.add(player);
                        
                        Log.d(TAG, "Jogador carregado: ID=" + playerId + ", Nome=" + name);
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao carregar jogador do cursor: " + e.getMessage());
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao buscar jogadores: " + e.getMessage());
            e.printStackTrace();
        }
        
        return players;
    }

    public void deletePlayer(long playerId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            Log.d(TAG, "Iniciando exclusão do jogador " + playerId);
            
            // Primeiro, remover todas as confirmações de presença do jogador
            int matchPlayersDeleted = db.delete(TABLE_MATCH_PLAYERS, 
                COLUMN_PLAYER_ID + "=?", 
                new String[]{String.valueOf(playerId)});
            Log.d(TAG, "Confirmações de presença removidas: " + matchPlayersDeleted);
            
            // Depois, remover o jogador de todos os times
            int teamPlayersDeleted = db.delete(TABLE_TEAM_PLAYERS, 
                COLUMN_PLAYER_ID + "=?", 
                new String[]{String.valueOf(playerId)});
            Log.d(TAG, "Registros de times removidos: " + teamPlayersDeleted);
            
            // Por fim, excluir o jogador
            int playerDeleted = db.delete(TABLE_PLAYERS, 
                COLUMN_ID + "=?", 
                new String[]{String.valueOf(playerId)});
            Log.d(TAG, "Jogador excluído: " + playerDeleted);
            
            db.setTransactionSuccessful();
            Log.d(TAG, "Jogador e todos seus registros relacionados foram excluídos com sucesso");
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao deletar jogador: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                db.endTransaction();
            } catch (Exception e) {
                Log.e(TAG, "Erro ao finalizar transação: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void updatePlayer(Player player) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, player.getName());
            values.put(COLUMN_POSITION, player.getPosition().name());
            values.put(COLUMN_LEVEL, player.getLevel());
            
            int rows = db.update(TABLE_PLAYERS, values,
                    COLUMN_ID + "=?", new String[]{String.valueOf(player.getId())});
            Log.d(TAG, "Jogador atualizado: ID=" + player.getId() + 
                ", Nome=" + player.getName() + ", Rows=" + rows);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao atualizar jogador: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Métodos para confirmação de presença
    public void confirmPresence(long matchId, long playerId, boolean confirmed) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            Log.d(TAG, "Confirmando presença - Match: " + matchId + ", Player: " + playerId + 
                ", Confirmado: " + confirmed);
            
            // Verificar se o jogador existe
            String playerQuery = "SELECT * FROM " + TABLE_PLAYERS + " WHERE " + COLUMN_ID + " = ?";
            Cursor playerCursor = db.rawQuery(playerQuery, new String[]{String.valueOf(playerId)});
            if (!playerCursor.moveToFirst()) {
                Log.e(TAG, "Jogador não encontrado: " + playerId);
                playerCursor.close();
                return;
            }
            String playerName = playerCursor.getString(playerCursor.getColumnIndex(COLUMN_NAME));
            playerCursor.close();
            
            // Verificar se a partida existe
            String matchQuery = "SELECT * FROM " + TABLE_MATCHES + " WHERE " + COLUMN_ID + " = ?";
            Cursor matchCursor = db.rawQuery(matchQuery, new String[]{String.valueOf(matchId)});
            if (!matchCursor.moveToFirst()) {
                Log.e(TAG, "Partida não encontrada: " + matchId);
                matchCursor.close();
                return;
            }
            String matchLocation = matchCursor.getString(matchCursor.getColumnIndex(COLUMN_LOCATION));
            matchCursor.close();
            
            ContentValues values = new ContentValues();
            values.put(COLUMN_MATCH_ID, matchId);
            values.put(COLUMN_PLAYER_ID, playerId);
            values.put(COLUMN_CONFIRMED, confirmed ? 1 : 0);

            // Primeiro tenta atualizar
            int updated = db.update(TABLE_MATCH_PLAYERS, values,
                COLUMN_MATCH_ID + "=? AND " + COLUMN_PLAYER_ID + "=?",
                new String[]{String.valueOf(matchId), String.valueOf(playerId)});
            
            // Se não atualizou nenhum registro, tenta inserir
            if (updated == 0) {
                long id = db.insert(TABLE_MATCH_PLAYERS, null, values);
                Log.d(TAG, "Novo registro de presença criado - ID: " + id + 
                    ", Jogador: " + playerName + 
                    ", Partida: " + matchLocation);
            } else {
                Log.d(TAG, "Registro de presença atualizado - Jogador: " + playerName + 
                    ", Partida: " + matchLocation);
            }
            
            // Verificar se o registro foi realmente salvo
            String checkQuery = "SELECT " + COLUMN_CONFIRMED + " FROM " + TABLE_MATCH_PLAYERS + 
                " WHERE " + COLUMN_MATCH_ID + " = ? AND " + COLUMN_PLAYER_ID + " = ?";
            Cursor checkCursor = db.rawQuery(checkQuery, 
                new String[]{String.valueOf(matchId), String.valueOf(playerId)});
            
            if (checkCursor.moveToFirst()) {
                int savedConfirmation = checkCursor.getInt(checkCursor.getColumnIndex(COLUMN_CONFIRMED));
                if (savedConfirmation != (confirmed ? 1 : 0)) {
                    Log.e(TAG, "Erro: Confirmação salva (" + savedConfirmation + 
                        ") diferente da solicitada (" + (confirmed ? 1 : 0) + ")");
                } else {
                    Log.d(TAG, "Confirmação verificada e correta");
                }
            } else {
                Log.e(TAG, "Erro: Registro de confirmação não encontrado após salvar");
            }
            checkCursor.close();
            
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao confirmar presença: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                db.endTransaction();
            } catch (Exception e) {
                Log.e(TAG, "Erro ao finalizar transação: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public boolean isPlayerConfirmed(long matchId, long playerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Log.d(TAG, "Verificando confirmação - Match: " + matchId + ", Player: " + playerId);
            
            String query = "SELECT " + COLUMN_CONFIRMED + " FROM " + TABLE_MATCH_PLAYERS +
                " WHERE " + COLUMN_MATCH_ID + " = ? AND " + COLUMN_PLAYER_ID + " = ?";
            
            Cursor cursor = db.rawQuery(query, 
                new String[]{String.valueOf(matchId), String.valueOf(playerId)});

            boolean confirmed = false;
            if (cursor.moveToFirst()) {
                confirmed = cursor.getInt(cursor.getColumnIndex(COLUMN_CONFIRMED)) == 1;
                Log.d(TAG, "Jogador está " + (confirmed ? "confirmado" : "não confirmado"));
            } else {
                Log.d(TAG, "Nenhum registro de confirmação encontrado");
            }
            cursor.close();
            return confirmed;
        } catch (Exception e) {
            Log.e(TAG, "Erro ao verificar confirmação: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public int getConfirmedPlayersCount(long matchId) {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Log.d(TAG, "Contando jogadores confirmados para partida " + matchId);
            
            String query = "SELECT COUNT(*) FROM " + TABLE_MATCH_PLAYERS +
                " WHERE " + COLUMN_MATCH_ID + " = ? AND " + COLUMN_CONFIRMED + " = 1";
            
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(matchId)});

            int count = 0;
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
            
            Log.d(TAG, "Total de jogadores confirmados: " + count);
            return count;
        } catch (Exception e) {
            Log.e(TAG, "Erro ao contar jogadores confirmados: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    // Métodos para Times
    public long saveTeam(Team team) {
        SQLiteDatabase db = this.getWritableDatabase();
        long teamId = -1;
        
        try {
            Log.d(TAG, "Iniciando salvamento do time " + team.getTeamNumber() + 
                " para partida " + team.getMatchId());
            
            db.beginTransaction();
            
            // Salvar o time
            ContentValues teamValues = new ContentValues();
            teamValues.put(COLUMN_MATCH_ID, team.getMatchId());
            teamValues.put(COLUMN_TEAM_NUMBER, team.getTeamNumber());
            
            teamId = db.insert(TABLE_TEAMS, null, teamValues);
            Log.d(TAG, "Time criado com ID: " + teamId);
            
            if (teamId != -1) {
                // Salvar os jogadores do time
                for (Player player : team.getPlayers()) {
                    ContentValues playerValues = new ContentValues();
                    playerValues.put(COLUMN_TEAM_ID, teamId);
                    playerValues.put(COLUMN_PLAYER_ID, player.getId());
                    
                    long playerTeamId = db.insert(TABLE_TEAM_PLAYERS, null, playerValues);
                    Log.d(TAG, "Jogador " + player.getName() + " (ID: " + player.getId() + 
                        ") adicionado ao time com ID: " + playerTeamId);
                }
                
                db.setTransactionSuccessful();
                Log.d(TAG, "Time salvo com sucesso - ID: " + teamId + 
                    ", Número: " + team.getTeamNumber() + 
                    ", Jogadores: " + team.getPlayers().size());
            } else {
                Log.e(TAG, "Erro ao criar o time - ID inválido retornado");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao salvar time: " + e.getMessage());
            e.printStackTrace();
            teamId = -1;
        } finally {
            try {
                db.endTransaction();
            } catch (Exception e) {
                Log.e(TAG, "Erro ao finalizar transação: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return teamId;
    }

    public void deleteTeamsForMatch(long matchId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // Primeiro, obter todos os IDs dos times desta partida
            String[] columns = {COLUMN_ID};
            String selection = COLUMN_MATCH_ID + "=?";
            String[] selectionArgs = {String.valueOf(matchId)};
            Cursor cursor = db.query(TABLE_TEAMS, columns, selection, selectionArgs, null, null, null);
            
            // Deletar os jogadores de cada time
            while (cursor.moveToNext()) {
                long teamId = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
                db.delete(TABLE_TEAM_PLAYERS, "team_id=?", new String[]{String.valueOf(teamId)});
            }
            cursor.close();
            
            // Deletar os times
            db.delete(TABLE_TEAMS, COLUMN_MATCH_ID + "=?", new String[]{String.valueOf(matchId)});
            
            db.setTransactionSuccessful();
            Log.d(TAG, "Times da partida " + matchId + " deletados com sucesso");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao deletar times: " + e.getMessage());
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public boolean deleteTeam(long teamId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // Primeiro, deletar os jogadores do time
            db.delete(TABLE_TEAM_PLAYERS, "team_id=?", new String[]{String.valueOf(teamId)});
            
            // Depois, deletar o time
            int rowsAffected = db.delete(TABLE_TEAMS, COLUMN_ID + "=?", new String[]{String.valueOf(teamId)});
            
            db.setTransactionSuccessful();
            Log.d(TAG, "Time " + teamId + " deletado com sucesso");
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Erro ao deletar time: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }

    public List<Team> getTeamsForMatch(long matchId) {
        List<Team> teams = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        try {
            // Obter todos os times da partida
            String[] columns = {COLUMN_ID, COLUMN_TEAM_NUMBER};
            String selection = COLUMN_MATCH_ID + "=?";
            String[] selectionArgs = {String.valueOf(matchId)};
            Cursor cursor = db.query(TABLE_TEAMS, columns, selection, selectionArgs, null, null, COLUMN_TEAM_NUMBER);
            
            while (cursor.moveToNext()) {
                long teamId = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
                int teamNumber = cursor.getInt(cursor.getColumnIndex(COLUMN_TEAM_NUMBER));
                
                Team team = new Team(matchId, teamNumber);
                team.setId(teamId);
                
                // Obter jogadores do time
                String query = "SELECT p.* FROM " + TABLE_PLAYERS + " p " +
                             "INNER JOIN " + TABLE_TEAM_PLAYERS + " tp ON p." + COLUMN_ID + "=tp." + COLUMN_PLAYER_ID + " " +
                             "WHERE tp.team_id=?";
                             
                Cursor playersCursor = db.rawQuery(query, new String[]{String.valueOf(teamId)});
                
                while (playersCursor.moveToNext()) {
                    Player player = new Player(
                        playersCursor.getString(playersCursor.getColumnIndex(COLUMN_NAME)),
                        Player.Position.valueOf(playersCursor.getString(playersCursor.getColumnIndex(COLUMN_POSITION))),
                        playersCursor.getInt(playersCursor.getColumnIndex(COLUMN_LEVEL))
                    );
                    player.setId(playersCursor.getLong(playersCursor.getColumnIndex(COLUMN_ID)));
                    team.addPlayer(player);
                }
                playersCursor.close();
                
                teams.add(team);
            }
            cursor.close();
            
            Log.d(TAG, "Times carregados para partida " + matchId + ": " + teams.size() + " times");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao obter times da partida: " + e.getMessage());
            e.printStackTrace();
        }
        
        return teams;
    }

    public List<Player> getConfirmedPlayersForMatch(long matchId) {
        List<Player> players = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        
        try {
            Log.d(TAG, "Iniciando busca de jogadores confirmados para partida " + matchId);
            
            // Primeiro verificar se a partida existe
            String matchQuery = "SELECT * FROM " + TABLE_MATCHES + " WHERE " + COLUMN_ID + " = ?";
            Cursor matchCursor = db.rawQuery(matchQuery, new String[]{String.valueOf(matchId)});
            if (!matchCursor.moveToFirst()) {
                Log.e(TAG, "Partida não encontrada: " + matchId);
                matchCursor.close();
                return players;
            }
            matchCursor.close();
            
            // Verificar confirmações na tabela match_players
            String countQuery = "SELECT " + COLUMN_PLAYER_ID + ", " + COLUMN_CONFIRMED + 
                " FROM " + TABLE_MATCH_PLAYERS + 
                " WHERE " + COLUMN_MATCH_ID + " = ?";
            
            Cursor confirmationsCursor = db.rawQuery(countQuery, new String[]{String.valueOf(matchId)});
            Log.d(TAG, "Total de registros em match_players: " + confirmationsCursor.getCount());
            
            while (confirmationsCursor.moveToNext()) {
                long playerId = confirmationsCursor.getLong(confirmationsCursor.getColumnIndex(COLUMN_PLAYER_ID));
                int confirmed = confirmationsCursor.getInt(confirmationsCursor.getColumnIndex(COLUMN_CONFIRMED));
                Log.d(TAG, "Registro encontrado - Player: " + playerId + ", Confirmado: " + confirmed);
            }
            confirmationsCursor.close();
            
            // Buscar jogadores confirmados com INNER JOIN
            String query = "SELECT DISTINCT p.* FROM " + TABLE_PLAYERS + " p " +
                         "INNER JOIN " + TABLE_MATCH_PLAYERS + " mp ON p." + COLUMN_ID + " = mp." + COLUMN_PLAYER_ID + " " +
                         "WHERE mp." + COLUMN_MATCH_ID + " = ? AND mp." + COLUMN_CONFIRMED + " = 1 " +
                         "ORDER BY p." + COLUMN_NAME + " ASC";
                         
            Log.d(TAG, "Executando query SQL: " + query);
            cursor = db.rawQuery(query, new String[]{String.valueOf(matchId)});
            
            Log.d(TAG, "Número de jogadores encontrados: " + cursor.getCount());
            
            while (cursor != null && cursor.moveToNext()) {
                try {
                    String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                    String positionStr = cursor.getString(cursor.getColumnIndex(COLUMN_POSITION));
                    int level = cursor.getInt(cursor.getColumnIndex(COLUMN_LEVEL));
                    long playerId = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
                    
                    Player player = new Player(name, Player.Position.valueOf(positionStr), level);
                    player.setId(playerId);
                    players.add(player);
                    
                    Log.d(TAG, "Jogador carregado - ID: " + playerId + 
                        ", Nome: " + name + 
                        ", Posição: " + positionStr + 
                        ", Nível: " + level);
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao carregar jogador do cursor: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // Verificar se há inconsistências
            if (players.isEmpty()) {
                Log.e(TAG, "Nenhum jogador confirmado encontrado para a partida " + matchId);
                
                // Verificar se há registros órfãos
                String orphanQuery = "SELECT mp." + COLUMN_PLAYER_ID + " FROM " + TABLE_MATCH_PLAYERS + " mp " +
                    "LEFT JOIN " + TABLE_PLAYERS + " p ON mp." + COLUMN_PLAYER_ID + " = p." + COLUMN_ID + " " +
                    "WHERE mp." + COLUMN_MATCH_ID + " = ? AND mp." + COLUMN_CONFIRMED + " = 1 " +
                    "AND p." + COLUMN_ID + " IS NULL";
                
                Cursor orphanCursor = db.rawQuery(orphanQuery, new String[]{String.valueOf(matchId)});
                if (orphanCursor.moveToFirst()) {
                    Log.e(TAG, "Encontrados registros órfãos de confirmação!");
                    do {
                        long orphanPlayerId = orphanCursor.getLong(0);
                        Log.e(TAG, "Registro órfão encontrado - Player ID: " + orphanPlayerId);
                        
                        // Remover registro órfão
                        db.delete(TABLE_MATCH_PLAYERS, 
                            COLUMN_MATCH_ID + " = ? AND " + COLUMN_PLAYER_ID + " = ?",
                            new String[]{String.valueOf(matchId), String.valueOf(orphanPlayerId)});
                    } while (orphanCursor.moveToNext());
                }
                orphanCursor.close();
            }
            
            Log.d(TAG, "Total de jogadores confirmados carregados: " + players.size());
            return players;
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao buscar jogadores confirmados: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    public void deleteMatch(long matchId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // Primeiro, deletar as confirmações de presença
            db.delete(TABLE_MATCH_PLAYERS,
                    COLUMN_MATCH_ID + "=?",
                    new String[]{String.valueOf(matchId)});

            // Depois, deletar a partida
            int rows = db.delete(TABLE_MATCHES,
                    COLUMN_ID + "=?",
                    new String[]{String.valueOf(matchId)});
            
            Log.d(TAG, "Partida deletada: ID=" + matchId + ", Rows=" + rows);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao deletar partida: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Match getMatchById(long matchId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Match match = null;
        
        try {
            String[] columns = {COLUMN_ID, COLUMN_DATE, COLUMN_LOCATION, COLUMN_DESCRIPTION, COLUMN_STATUS, COLUMN_USER_ID};
            String selection = COLUMN_ID + "=?";
            String[] selectionArgs = {String.valueOf(matchId)};
            
            Cursor cursor = db.query(TABLE_MATCHES, columns, selection, selectionArgs, null, null, null);
            
            if (cursor.moveToFirst()) {
                // Create match with the basic constructor
                match = new Match(
                    new Date(cursor.getLong(cursor.getColumnIndex(COLUMN_DATE))),
                    cursor.getString(cursor.getColumnIndex(COLUMN_LOCATION)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION))
                );
                
                // Set additional properties
                match.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
                match.setStatus(Match.MatchStatus.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_STATUS))));
            }
            
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao obter partida: " + e.getMessage());
            e.printStackTrace();
        }
        
        return match;
    }
} 