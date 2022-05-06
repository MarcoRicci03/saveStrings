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
import java.util.LinkedHashMap;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins="*")
@RestController
@RequestMapping("/SaveStrings")
public class methods {

  Connection conn;

  public methods() throws FileNotFoundException, IOException {
    conn = DBConnection.createNewDBconnection();
  }

  @RequestMapping(value = "/register", method = RequestMethod.GET)
  public LinkedHashMap<String, String> register(
    @RequestParam(name = "username", required = true) String username,
    @RequestParam(name = "password", required = true) String pass
  )
    throws SQLException {
    PreparedStatement pstmt = conn.prepareStatement(
      "SELECT * FROM users WHERE username = '" + username + "'"
    );
    System.out.println(pstmt.toString());
    LinkedHashMap<String, String> map = new LinkedHashMap<>();
    ResultSet r = pstmt.executeQuery();
    if (r.next()) {
      map.put("status", "error");
      map.put("message", "Utente gia registrato");
    } else {
      pstmt =
        conn.prepareStatement(
          "INSERT INTO users (username, pass, token) VALUES (?,?,?)"
        );
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
  @CrossOrigin(origins="*")
  @RequestMapping(value = "/getToken", method = RequestMethod.GET)
  public LinkedHashMap<String, String> getToken(
    @RequestParam(name = "username", required = true) String username,
    @RequestParam(name = "password", required = true) String pass
  )
    throws SQLException {
    PreparedStatement pstmt = conn.prepareStatement(
      "SELECT * FROM users WHERE username = '" + username + "'"
    );
    LinkedHashMap<String, String> map = new LinkedHashMap<>();
    ResultSet r = pstmt.executeQuery();
    if (!r.next()) {
      map.put("status", "error");
      map.put("message", "Utente gia registrato");
    } else {
      pstmt =
        conn.prepareStatement(
          "UPDATE users SET token = ? WHERE username = ? AND pass = ?"
        );
      String token = MD5(LocalDateTime.now().toString() + username);
      pstmt.setString(1, token);
      pstmt.setString(2, username);
      pstmt.setString(3, MD5(pass));
      if (pstmt.executeUpdate() > 0) {
        map.put("status", "ok");
        map.put("token", token);
      } else {
        map.put("status", "error");
        map.put("message", "Errore nella login.");
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
  @RequestMapping(value = "/getKeys", method = RequestMethod.GET)
  public LinkedHashMap<String, Object> getKeys(
    @RequestParam(name = "token", required = true) String token
  )
    throws SQLException {
    PreparedStatement pstmt = conn.prepareStatement(
      "SELECT keyy FROM strings INNER JOIN users ON strings.id_user = users.id_user WHERE token = '" +
      token +
      "'"
    );
    ResultSet r = pstmt.executeQuery();
    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
    ArrayList<String> vKeys = new ArrayList<>();
    if (r.next()) {
      do {
        vKeys.add(r.getString("keyy"));
      } while (r.next());
      map.put("status", "ok");
      map.put("keys", vKeys);
    } else {
      map.put("status", "error");
      map.put("message", "Nessuna key trovata.");
    }
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
  @RequestMapping(value = "/setString", method = RequestMethod.GET)
  public LinkedHashMap<String, String> setString(
    @RequestParam(name = "token", required = true) String token,
    @RequestParam(name = "key", required = true) String key,
    @RequestParam(name = "string", required = true) String stringa
  )
    throws SQLException {
      LinkedHashMap<String, String> map = new LinkedHashMap<>();
    PreparedStatement pstmt = conn.prepareStatement(
      "SELECT id_user FROM users WHERE token ='" + token + "'"
    );
    ResultSet r = pstmt.executeQuery();
    if (r.next()) {
      Integer id_user = r.getInt("id_user");
      pstmt =
        conn.prepareStatement(
          "SELECT keyy FROM strings WHERE id_user =" +
          id_user +
          " AND keyy = '" +
          key +
          "'"
        );
      r = pstmt.executeQuery();
      if (!r.next()) {
        pstmt =
          conn.prepareStatement(
            "INSERT INTO strings (id_user, string,  keyy) VALUES (?,?,?)"
          );
        System.out.println(id_user);
        pstmt.setInt(1, id_user);
        pstmt.setString(2, stringa);
        pstmt.setString(3, key);
        System.out.println(pstmt.toString());
        if (pstmt.executeUpdate() > 0) {
          map.put("status", "ok");
          map.put("message", "Stringa aggiunta con successo.");
        } else {
          map.put("status", "error");
          map.put("message", "Errore nell'aggiunta della stringa.");
        }
      } else {
        map.put("status", "error");
        map.put(
          "message",
          "La key inserita è già assegnata a quella di un'altra stringa."
        );
      }
    } else {
      map.put("status", "error");
      map.put("message", "Il token inserito non corrisponde a nessun utente.");
    }
    return map;
  }

  /*
   * http://HOST_CATTEDRA/SaveStrings/deleteString.php?token=
   * 697ab188731ec4861e1eb72eca7a18d2&key=IDENTIFICATIVO
   * permette di cancellare la stringa identificata dalla key "IDENTIFICATIVO"
   * presente nell'account relativo al token 697ab188731ec4861e1eb72eca7a18d2
   */
  @RequestMapping(value = "/deleteString", method = RequestMethod.GET)
  public LinkedHashMap<String, String> deleteString(
    @RequestParam(name = "token", required = true) String token,
    @RequestParam(name = "key", required = true) String key
  )
    throws SQLException {
    PreparedStatement pstmt = conn.prepareStatement(
      "SELECT keyy FROM strings INNER JOIN users ON strings.id_user = users.id_user WHERE token = '" +
      token +
      "' AND keyy = '" +
      key +
      "'"
    );
    ResultSet r = pstmt.executeQuery();
    LinkedHashMap<String, String> map = new LinkedHashMap<>();
    if (r.next()) {
      pstmt =
        conn.prepareStatement(
          "DELETE strings FROM strings INNER JOIN users ON strings.id_user = users.id_user WHERE users.token = ? AND strings.keyy = ?"
        );
      pstmt.setString(1, token);
      pstmt.setString(2, key);
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
  @RequestMapping(value = "/getString", method = RequestMethod.GET)
  public LinkedHashMap<String, String> getString(
    @RequestParam(name = "token", required = true) String token,
    @RequestParam(name = "key", required = true) String key
  )
    throws SQLException {
    PreparedStatement pstmt = conn.prepareStatement(
      "SELECT * FROM strings INNER JOIN users ON strings.id_user = users.id_user WHERE token = '" +
      token +
      "' AND keyy = '" +
      key +
      "'"
    );
    ResultSet r = pstmt.executeQuery();
    LinkedHashMap<String, String> map = new LinkedHashMap<>();
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
      java.security.MessageDigest md = java.security.MessageDigest.getInstance(
        "MD5"
      );
      byte[] array = md.digest(md5.getBytes());
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < array.length; ++i) {
        sb.append(
          Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3)
        );
      }
      return sb.toString();
    } catch (java.security.NoSuchAlgorithmException e) {}
    return null;
  }
}
