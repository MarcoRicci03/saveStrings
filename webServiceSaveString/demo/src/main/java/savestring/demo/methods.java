package savestring.demo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
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
            // set the preparedstatement parameters
            pstmt.setString(1, MD5(LocalDate.now().toString() + username));
            pstmt.setString(2, username);
            pstmt.setString(3, MD5(pass));
            if (pstmt.executeUpdate() > 0) {
                map.put("status", "ok");
                map.put("token", MD5(LocalDate.now().toString() + username));
            } else {
                map.put("status", "error");
                map.put("message", "Errore nella registrazione al db");
            }
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
