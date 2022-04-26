package savestring.demo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/SaveStrings")
public class methods {
    Connection conn;

    public methods() throws FileNotFoundException, IOException {
        conn = DBConnection.createNewDBconnection();
    }

    @GetMapping("/register")
    @ResponseBody
    public Map<String, String> register(@RequestParam(name = "username", required = true) String username,
            @RequestParam(name = "password", required = true) String pass) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "SELECT * FROM users WHERE username = '" + username + "'");
        HashMap<String, String> map = new HashMap<>();
        ResultSet r = pstmt.executeQuery();
        if (r.next()) {
            map.put("status", "error");
            map.put("message", "Utente gia registrato");
        } else {
            pstmt = conn.prepareStatement(
                    "INSERT INTO users (username, pass, token) VALUES (?,?,?)");
            pstmt.setString(1, username);
            pstmt.setString(2, pass = MD5(pass));
            pstmt.setString(3, MD5(LocalDate.now().toString() + username));
            if (pstmt.executeUpdate() > 0) {
                map.put("status", "ok");
                map.put("message", "Utente registrato");
            } else {
                map.put("status", "error");
                map.put("message", "Errore nella registrazione al db");
            }
        }
        return map;
    }

    @GetMapping("/getToken")
    @ResponseBody
    public Map<String, String> getToken(@RequestParam(name = "username", required = true) String username,
            @RequestParam(name = "password", required = true) String pass) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "SELECT * FROM users WHERE username = '" + username + "'");
        HashMap<String, String> map = new HashMap<>();
        ResultSet r = pstmt.executeQuery();
        if (!r.next()) {
            map.put("status", "error");
            map.put("message", "Utente gia registrato");
        } else {
            pstmt = conn.prepareStatement(
                    "UPDATE users SET token = ? WHERE username = ? AND pass = ?");
            String token = MD5(LocalDateTime.now().toString() + username);
            pstmt.setString(1, token);
            pstmt.setString(2, username);
            pstmt.setString(3, MD5(pass));
            if (pstmt.executeUpdate() > 0) {
                map.put("status", "ok");
                map.put("token", token);
            } else {
                map.put("status", "error");
                map.put("message", "Errore nella registrazione al db");
            }
        }
        return map;
    }

    /*
     * http://HOST_CATTEDRA/SaveStrings/getKeys.php?token=
     * 697ab188731ec4861e1eb72eca7a18d2
     * permette di ottenere la lista di tutte le key presenti nell'account relativo
     * al token 697ab188731ec4861e1eb72eca7a18d2
     */
    @GetMapping("/getKeys")
    @ResponseBody
    public Map<String, Object> getKeys(@RequestParam(name = "token", required = true) String token)
            throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "SELECT keyy FROM strings INNER JOIN users ON strings.id_user = users.id_user WHERE token = '" + token
                        + "'");
        ResultSet r = pstmt.executeQuery();
        HashMap<String, Object> map = new HashMap<>();
        ArrayList<Integer> vKeys = new ArrayList<>();
        while (r.next()) {
            vKeys.add(r.getInt("keyy"));
        }
        map.put("status", "ok");
        map.put("keys", vKeys);
        return map;
    }

    /*
     * http://HOST_CATTEDRA/SaveStrings/setString.php?token=
     * 697ab188731ec4861e1eb72eca7a18d2&key=IDENTIFICATIVO&string=HELLO_WORLD
     * permette di settare nell'account relativo al token
     * 697ab188731ec4861e1eb72eca7a18d2
     * una stringa identificata ( key ) da "IDENTIFICATIVO" e contenente
     * "HELLO_WORLD"
     */
    @GetMapping("/setString")
    @ResponseBody
    public Map<String, String> setString(@RequestParam(name = "token", required = true) String token,
            @RequestParam(name = "key", required = true) Integer key,
            @RequestParam(name = "string", required = true) String stringa)
            throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "SELECT keyy FROM strings INNER JOIN users ON strings.id_user = users.id_user WHERE token = '" + token
                        + "' AND keyy = " + key);
        ResultSet r = pstmt.executeQuery();
        HashMap<String, String> map = new HashMap<>();
        // da sistemare
        if (r.next()) {
            pstmt = conn.prepareStatement(
                    "UPDATE strings INNER JOIN users ON strings.id_user = users.id_user SET strings.string = ? WHERE users.token = ? AND strings.keyy = ?");
            pstmt.setString(1, stringa);
            pstmt.setString(2, token);
            pstmt.setInt(3, key);

            if (pstmt.executeUpdate() > 0) {
                map.put("status", "ok");
                map.put("message", "Stringa aggiornata con successo.");
            } else {
                map.put("status", "error");
                map.put("message", "Errore nell'aggiornamento della stringa.");
            }
        } else {
            map.put("status", "error");
            map.put("message", "Nessuna stringa trovata con questi parametri.");
        }
        return map;
    }

    /*
     * http://HOST_CATTEDRA/SaveStrings/deleteString.php?token=
     * 697ab188731ec4861e1eb72eca7a18d2&key=IDENTIFICATIVO
     * permette di cancellare la stringa identificata dalla key "IDENTIFICATIVO"
     * presente nell'account relativo al token 697ab188731ec4861e1eb72eca7a18d2
     */
    @GetMapping("/deleteString")
    @ResponseBody
    public Map<String, String> deleteString(@RequestParam(name = "token", required = true) String token,
            @RequestParam(name = "key", required = true) Integer key)
            throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "SELECT keyy FROM strings INNER JOIN users ON strings.id_user = users.id_user WHERE token = '" + token
                        + "' AND keyy = " + key);
        ResultSet r = pstmt.executeQuery();
        HashMap<String, String> map = new HashMap<>();
        if (r.next()) {
            pstmt = conn.prepareStatement(
                    "DELETE strings FROM strings INNER JOIN users ON strings.id_user = users.id_user WHERE users.token = ? AND strings.keyy = ?");
            pstmt.setString(1, token);
            pstmt.setInt(2, key);
            if (pstmt.executeUpdate() > 0) {
                map.put("status", "ok");
                map.put("message", "Stringa eliminata con successo.");
            } else {
                map.put("status", "error");
                map.put("message", "Errore nella cancellazione della stringa.");
            }
        } else {
            map.put("status", "error");
            map.put("message", "Nessuna stringa trovata con questi parametri.");
        }
        return map;
    }

    /*
     * http://HOST_CATTEDRA/SaveStrings/getString.php?token=
     * 697ab188731ec4861e1eb72eca7a18d2&key=IDENTIFICATIVO
     * permette di leggere il contenuto della stringa identificata dalla key
     * "IDENTIFICATIVO"
     * presente nell'account relativo al token 697ab188731ec4861e1eb72eca7a18d2
     */
    @GetMapping("/getString")
    @ResponseBody
    public Map<String, String> getString(@RequestParam(name = "token", required = true) String token,
            @RequestParam(name = "key", required = true) Integer key)
            throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "SELECT * FROM strings INNER JOIN users ON strings.id_user = users.id_user WHERE token = '" + token
                        + "' AND keyy = " + key);
        ResultSet r = pstmt.executeQuery();
        HashMap<String, String> map = new HashMap<>();
        if (r.next()) {
            map.put("status", "ok");
            map.put("string", r.getString("string"));
        } else {
            map.put("status", "error");
            map.put("message", "Nessuna stringa trovata con questi parametri.");
        }
        return map;
    }

    public String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }

}
